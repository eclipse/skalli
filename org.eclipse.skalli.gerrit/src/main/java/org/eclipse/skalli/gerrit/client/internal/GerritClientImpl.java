/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.gerrit.client.GerritClient;
import org.eclipse.skalli.gerrit.client.GerritFeature;
import org.eclipse.skalli.gerrit.client.GerritPlugin;
import org.eclipse.skalli.gerrit.client.GerritVersion;
import org.eclipse.skalli.gerrit.client.InheritableBoolean;
import org.eclipse.skalli.gerrit.client.ProjectOptions;
import org.eclipse.skalli.gerrit.client.SubmitType;
import org.eclipse.skalli.gerrit.client.config.GerritServerConfig;
import org.eclipse.skalli.gerrit.client.exception.CommandException;
import org.eclipse.skalli.gerrit.client.exception.ConnectionException;
import org.eclipse.skalli.gerrit.client.internal.GSQL.ResultFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@SuppressWarnings("nls")
public class GerritClientImpl implements GerritClient {

    private final static Logger LOG = LoggerFactory.getLogger(GerritClientImpl.class);

    private final static int TIMEOUT = 2500;
    private static final int SLEEP_INTERVAL = 500;
    private static final char[] REPO_NAME_INVALID_CHARS = { '\\', ':', '~', '?', '*', '<', '>', '|', '%', '"' };

    enum Cache {
        ALL, PROJECTS, GROUPS
    }

    private static final String GERRIT_VERSION_PREFIX = "gerrit version ";

    private final String ACCOUNTS_PREFIX = "username:";
    private final int ACCOUNTS_QUERY_BLOCKSIZE = 100;

    private final Pattern UNSUPPORTED_GSQL = Pattern.compile(
            ".*(show|insert|update|delete|merge|create|alter|rename|truncate|drop)\\s.*", Pattern.CASE_INSENSITIVE
                    | Pattern.MULTILINE);

    final GerritServerConfig gerritConfig;
    final int port;
    final String onBehalfOf;

    JSch client = null;
    Session session = null;
    ChannelExec channel = null;

    GerritVersion serverVersion = null;
    HashMap<String,GerritPlugin> plugins = null;

    GerritClientImpl(GerritServerConfig gerritConfig, String onBehalfOf) {
        this.gerritConfig = gerritConfig;
        this.port = NumberUtils.toInt(gerritConfig.getPort(),  GerritClient.DEFAULT_PORT);
        this.onBehalfOf = onBehalfOf;
    }

    @Override
    public void connect() throws ConnectionException {
        LOG.info(MessageFormat.format("Trying to connect to Gerrit {0}:{1}.", gerritConfig.getHost(), port));

        File privateKeyFile = null;
        try {
            client = new JSch();
            JSch.setLogger(new JschLogger());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("server_host_key", "ssh-rsa");
            JSch.setConfig(config);

            privateKeyFile = getPrivateKeyFile(gerritConfig.getPrivateKey());
            client.addIdentity(privateKeyFile.getAbsolutePath(), gerritConfig.getPassphrase());
            session = client.getSession(gerritConfig.getUser(), gerritConfig.getHost(), port);
            session.setTimeout(TIMEOUT);
            session.connect();
        } catch (JSchException e) {
            throw andDisconnect(new ConnectionException("Failed to connect to Gerrit", e));
        } finally {
            if (privateKeyFile != null) {
                privateKeyFile.delete();
            }
        }
        LOG.info(String.format("Connected to Gerrit %s:%s (%s)",
                gerritConfig.getHost(), port, session.getServerVersion()));
    }

    @Override
    public GerritVersion getVersion() throws ConnectionException, CommandException {
        if (serverVersion == null) {
            List<String> result = null;
            try {
                result = sshCommand("gerrit version");
            } catch (CommandException e) {
                throw andDisconnect(new CommandException("Failed to retrieve Gerrit version", e));
            }
            if (result.size() != 1) {
                throw andDisconnect(new CommandException(MessageFormat.format(
                        "Failed to retrieve Gerrit version: Invalid result size ({0})",
                        CollectionUtils.toString(result, ','))));
            }
            String versionString = result.get(0);
            if (StringUtils.isBlank(versionString)) {
                return GerritVersion.GERRIT_UNKNOWN_VERSION;
            }
            if (!versionString.startsWith(GERRIT_VERSION_PREFIX)) {
                return GerritVersion.GERRIT_UNKNOWN_VERSION;
            }
            serverVersion = GerritVersion.asGerritVersion(versionString.substring(GERRIT_VERSION_PREFIX.length()));
        }
        return serverVersion;
    }

    @Override
    public Map<String,GerritPlugin> getPlugins() throws ConnectionException, CommandException {
        if (plugins == null) {
            plugins = new HashMap<String,GerritPlugin>();
            if (getVersion().supports(GerritFeature.LS_PLUGINS)) {
                List<String> sshResponse = null;
                try {
                    sshResponse = sshCommand("gerrit plugin ls --all");
                    if (sshResponse.size() > 2) {
                        // skip the first two lines; they contain just a table header
                        for (int i = 2; i < sshResponse.size(); ++i) {
                            GerritPlugin plugin = GerritPlugin.valueOf(sshResponse.get(i));
                            if (plugin != null) {
                                plugins.put(plugin.getName(), plugin);
                                if (LOG.isInfoEnabled()) {
                                    LOG.info(MessageFormat.format("plugin: ''{0}''", plugin));
                                }
                            }
                        }
                    }
                } catch (CommandException e) {
                    // fail gracefully and assume that there are no plugins available
                    LOG.error("Failed to retrieve Gerrit plugins", e);
                    disconnect();
                }
            }
        }
        return plugins;
    }

    private File getPrivateKeyFile(String privateKey) {
        File privateKeyFile = null;
        try {
            privateKeyFile = File.createTempFile("gerrit_key", "ssh");
            FileUtils.writeStringToFile(privateKeyFile, privateKey, "ISO8859_1");
        } catch (IOException e) {
            LOG.error("Failed to write key file."); //$NON-NLS-1$
            throw new RuntimeException("Failed to write key file.", e); //$NON-NLS-1$
        }
        return privateKeyFile;
    }

    @Override
    public void disconnect() {
        if (channel != null) {
            channel.disconnect();
            channel = null;
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
        LOG.info("Disconnected");
    }

    @Override
    public void createProject(String name, String branch, Set<String> ownerList, String parent,
            boolean permissionsOnly, String description, SubmitType submitType,
            boolean useContributorAgreements, boolean useSignedOffBy, boolean emptyCommit)
                    throws ConnectionException, CommandException {

        ProjectOptions options = new ProjectOptions();
        options.setName(name);
        options.setBranch(branch);
        options.setOwners(ownerList);
        options.setParent(parent);
        options.setPermissionsOnly(permissionsOnly);
        options.setDescription(description);
        options.setSubmitType(submitType);
        options.setUseContributorAgreements(InheritableBoolean.valueOf(useContributorAgreements));
        options.setUseSignedOffBy(InheritableBoolean.valueOf(useSignedOffBy));
        options.setRequiredChangeId(InheritableBoolean.TRUE);
        options.setUseContentMerge(InheritableBoolean.TRUE);
        options.setCreateEmptyCommit(emptyCommit);

        createProject(options);
    }

    @Override
    public void createProject(ProjectOptions options) throws ConnectionException, CommandException {
        String name = options.getName();
        if (name == null) {
            throw andDisconnect(new IllegalArgumentException("'name' is required"));
        }
        String checkFailedMsg = checkProjectName(name);
        if (checkFailedMsg != null) {
            throw andDisconnect(new IllegalArgumentException(checkFailedMsg));
        }
        GerritVersion version = getVersion();
        sshCommand(sshCreateProject(options, version));
    }

    String sshCreateProject(ProjectOptions options, GerritVersion version) {
        StringBuilder sb = new StringBuilder("gerrit create-project");
        appendOption(sb, "name", options.getName());
        appendOption(sb, "branch", options.getBranches());
        appendOption(sb, "owner", options.getOwners());
        appendOption(sb, "parent", options.getParent());
        appendOption(sb, "permissions-only", options.isPermissionsOnly());
        appendOption(sb, "description", options.getDescription());
        appendOption(sb, "submit-type", options.getSubmitType() != null ? options.getSubmitType().name() : null);
        appendOption(sb, "use-contributor-agreements", options.getUseContributorAgreements());
        appendOption(sb, "use-signed-off-by", options.getUseSignedOffBy());
        appendOption(sb, "require-change-id", options.getRequiredChangeId());
        appendOption(sb, "use-content-merge", options.getUseContentMerge());
        appendOption(sb, "empty-commit", options.isCreateEmptyCommit());
        if (version.supports(GerritFeature.CREATE_PROJECT_MAX_SIZE)) {
            appendOption(sb, "max-object-size-limit", options.getMaxObjectSizeLimit());
        }
        if (version.supports(GerritFeature.CREATE_PROJECT_PLUGIN_CONFIG)) {
            for (String pluginName: options.getPluginConfigKeys()) {
                for (Entry<String,String> pluginConfig: options.getPluginConfig(pluginName).entrySet()) {
                    appendOption(sb, "plugin-config", pluginName + "." + pluginConfig.getKey() + "=" + pluginConfig.getValue());
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> getProjects() throws ConnectionException, CommandException {
        return getProjects("all");
    }

    @Override
    public List<String> getProjects(String type) throws ConnectionException, CommandException {
        GerritVersion version = getVersion();
        StringBuilder sb = new StringBuilder("gerrit ls-projects");
        if (version.supports(GerritFeature.LS_PROJECTS_TYPE_ATTR)) {
            appendOption(sb, "type", type);
        }
        return sshCommand(sb.toString());
    }

    @Override
    public boolean projectExists(final String name) throws ConnectionException, CommandException {
        if (name == null) {
            return false;
        }

        return getProjects().contains(name);
    }

    @Override
    public void createGroup(final String name, final String owner, final String description,
            final Set<String> members)
            throws ConnectionException, CommandException {
        if (name == null) {
            throw andDisconnect(new IllegalArgumentException("'name' is required"));
        }
        String checkFailedMsg = checkGroupName(name);
        if (checkFailedMsg != null) {
            throw andDisconnect(new IllegalArgumentException(checkFailedMsg));
        }

        StringBuilder sb = new StringBuilder("gerrit create-group");
        appendOption(sb, "owner", owner);
        appendOption(sb, "description", description);
        appendOption(sb, "member", getKnownAccounts(members));
        appendOption(sb, "visible-to-all", true);
        appendArgument(sb, name);

        sshCommand(sb.toString());
    }

    @Override
    public List<String> getGroups() throws ConnectionException, CommandException {
        List<String> result = Collections.emptyList();
        GerritVersion version = getVersion();
        if (version.supports(GerritFeature.LS_GROUPS)) {
            StringBuilder sb = new StringBuilder("gerrit ls-groups");
            if (version.supports(GerritFeature.LS_GROUPS_VISIBLE_TO_ALL_ATTR)) {
                appendOption(sb, "visible-to-all", true);
            }
            result = sshCommand(sb.toString());
        } else {
            result = new ArrayList<String>();
            List<String> gsqlResult = gsql("SELECT name FROM " + GSQL.Tables.ACCOUNT_GROUPS, ResultFormat.JSON);
            for (final String entry : gsqlResult) {
                if (isRow(entry)) {
                    result.add(JSONUtil.getString(entry, "columns.name"));
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getGroups(String... projectNames) throws ConnectionException, CommandException {
        List<String> result = Collections.emptyList();

        if (projectNames == null || projectNames.length == 0) {
            return result;
        }

        // Gerrit throws exceptions for --project options that correspond to
        // no Gerrit project; thus, we have to filter out thise project names before
        // sending the ls-groups command
        Set<String> allProjects = new HashSet<String>(getProjects());

        GerritVersion version = getVersion();
        if (version.supports(GerritFeature.LS_GROUPS_PROJECT_ATTR)) {
            StringBuilder sb = new StringBuilder("gerrit ls-groups");
            if (version.supports(GerritFeature.LS_GROUPS_VISIBLE_TO_ALL_ATTR)) {
                appendOption(sb, "visible-to-all", true);
            }
            for (String projectName : projectNames) {
                if (allProjects.contains(projectName)) {
                    appendOption(sb, "project", projectName);
                }
            }
            result = sshCommand(sb.toString());
        } else if (version.supports(GerritFeature.REF_RIGHTS_TABLE)) {
            result = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT name FROM ").append(GSQL.Tables.ACCOUNT_GROUP_NAMES)
                    .append(" WHERE group_id IN (SELECT group_id FROM ").append(GSQL.Tables.REF_RIGHTS).append(" WHERE");
            for (String projectName : projectNames) {
                if (allProjects.contains(projectName)) {
                    sb.append(" project_name='").append(projectName).append("' OR");
                }
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append(");");

            List<String> gsqlResult = gsql(sb.toString(), ResultFormat.JSON);
            for (String entry : gsqlResult) {
                if (isRow(entry)) {
                    result.add(JSONUtil.getString(entry, "columns.name"));
                }
            }
        }

        return result;
    }

    @Override
    public boolean groupExists(final String name) throws ConnectionException, CommandException {
        if (name == null) {
            return false;
        }
        List<String> groups = getGroups();
        for (String group: groups) {
            if (name.equals(group)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getKnownAccounts(Set<String> variousAccounts) throws ConnectionException, CommandException {
        if (variousAccounts == null || variousAccounts.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> result = new HashSet<String>();
        GerritVersion version = getVersion();
        if (version.supports(GerritFeature.ACCOUNT_CHECK_OBSOLETE)) {
            for (String account: variousAccounts) {
                if (StringUtils.isNotBlank(account)) {
                    result.add(account);
                }
            }
        } else {
            int variousAccountsSize = variousAccounts.size();
            int blocks = (int) Math.ceil((float) variousAccountsSize / ACCOUNTS_QUERY_BLOCKSIZE);
            for (int i = 0; i < blocks; i++) {
                List<String> worklist = new ArrayList<String>(variousAccounts);
                int startIndex = i * ACCOUNTS_QUERY_BLOCKSIZE;
                int endIndex = Math.min((i + 1) * ACCOUNTS_QUERY_BLOCKSIZE, variousAccountsSize);
                result.addAll(queryKnownAccounts(worklist.subList(startIndex, endIndex)));
            }
        }
        return result;
    }

    @Override
    public String checkGroupName(String name) {
        if (StringUtils.isBlank(name)) {
            return "Group names must not be blank";
        }
        if (StringUtils.trim(name).length() < name.length()) {
            return "Group names must not start or end with whitespace";
        }
        if (containsWhitespace(name, true)) {
            return "Group names must not contain whitespace";
        }
        if (HtmlUtils.containsTags(name)) {
            return "Group names must not contain HTML tags";
        }
        return null;
    }

    @Override
    public String checkProjectName(String name) {
        if (StringUtils.isBlank(name)) {
            return "Repository names must not be blank";
        }
        if (StringUtils.trim(name).length() < name.length()) {
            return "Repository names must not start or end with whitespace";
        }
        if (containsWhitespace(name, false)) {
            return "Repository names must not contain whitespace";
        }
        if (name.startsWith("/")) {
            return "Repository names must not start with a slash";
        }
        if (name.endsWith("/")) {
            return "Repository names must not end with a trailing slash";
        }
        if (HtmlUtils.containsTags(name)) {
            return "Repository names must not contain HTML tags";
        }
        if (StringUtils.containsAny(name, REPO_NAME_INVALID_CHARS )) {
            return "Repository names must not contain any of the following characters: " +
                    "'\', ':', '~', '?', '*', '<', '>', '|', '%', '\"'";
        }
        if (name.startsWith("../") //$NON-NLS-1$
                || name.contains("/../") //$NON-NLS-1$
                || name.contains("/./")) { //$NON-NLS-1$
            return "Repository names must not contain \"../\", \"/../\" or \"/./\"";
        }
        return null;
    }

    private boolean containsWhitespace(String s, boolean allowBlanks) {
        for (int i = 0; i < s.length() ; i++) {
            char c = s.charAt(i);
            if (allowBlanks && c == ' ') {
                continue;
            }
            if (Character.isWhitespace(c)) {
                return true;
            }
            if (c == '\0') {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility method for checking accounts.
     *
     * This indirection was introduced to allow splitting the call if the parameter list is huge.
     * Depending on the database this could easily fail. Hence split it into separate SQL queries
     * and merge the results.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    private Collection<String> queryKnownAccounts(Collection<String> variousAccounts) throws ConnectionException,
            CommandException {
        final List<String> result = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT external_id FROM ").append(GSQL.Tables.ACCOUNT_EXTERNAL_IDS)
                .append(" WHERE external_id IN (");

        boolean noRealParameters = true;
        for (String variousAccount : variousAccounts) {
            if (!StringUtils.isBlank(variousAccount)) {
                sb.append("'").append(ACCOUNTS_PREFIX).append(variousAccount).append("', ");
                noRealParameters = false;
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(");");

        if (noRealParameters) {
            return result;
        }
        final List<String> gsqlResult = gsql(sb.toString(), ResultFormat.JSON);
        for (final String entry : gsqlResult) {
            if (isRow(entry)) {
                result.add(StringUtils.removeStart(JSONUtil.getString(entry, "columns.external_id"), ACCOUNTS_PREFIX));
            }
        }

        return result;

    }

    /**
     * Performs a single GSQL statement according to <a href=
     * "http://gerrit.googlecode.com/svn/documentation/2.1.5/cmd-gsql.html"
     * >gerrit gsql</a> (<a href=
     * "http://gerrit.googlecode.com/svn/documentation/2.1.5/cmd-gsql.html#options"
     * >options</a>).
     *
     * Note that only SELECT statements are allowed
     *
     * @param query
     *            the query to execute (only SELECT allowed)
     * @param format
     *            <code>PRETTY</code> or <code>JSON</code>
     *
     * @return the resulting lines depending in the specified format
     *         <code>format</code>. The last line includes query statistics.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    List<String> gsql(final String query, final ResultFormat format) throws ConnectionException, CommandException {
        if (StringUtils.isBlank(query)) {
            LOG.info("No query passed. Returning an empty result.");
            return Collections.emptyList();
        }

        // only allow READ access via gsql()
        if (UNSUPPORTED_GSQL.matcher(query).matches()) {
            throw new UnsupportedOperationException(
                    String.format("Your command contains unsupported GSQL: '%s'", query));
        }

        StringBuilder sb = new StringBuilder("gerrit gsql");

        sb.append(" --format ").append(format.name());
        sb.append(" -c \"").append(query).append("\"");

        return sshCommand(sb.toString());
    }

    /**
     * Performs a SSH command
     *
     * @param command
     *            the command to execute
     *
     * @return the resulting lines
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    private List<String> sshCommand(final String command) throws ConnectionException, CommandException {
        LOG.info(MessageFormat.format("Sending on behalf of ''{0}'': ''{1}''", onBehalfOf, command));

        boolean manuallyConnected = false;

        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        ByteArrayInputStream baisIn = new ByteArrayInputStream(new byte[0]);
        ChannelExec channel = null;
        try {
            if (client == null || session == null) {
                connect();
                manuallyConnected = true;
            }
            channel = (ChannelExec) session.openChannel("exec");
            channel.setInputStream(baisIn);
            channel.setOutputStream(baosOut);
            channel.setErrStream(baosErr);
            channel.setCommand(command);
            channel.connect();
            while (!channel.isClosed()) {
                try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    throw andDisconnect(new CommandException());
                }
            }
            List<String> result = new LinkedList<String>();
            InputStreamReader inR = new InputStreamReader(new ByteArrayInputStream(baosOut.toByteArray()), "ISO-8859-1");
            BufferedReader buf = new BufferedReader(inR);
            String line;
            while ((line = buf.readLine()) != null) {
                result.add(line);
            }

            if (result.size() > 0) {
                checkForErrorsInResponse(result.get(0));
            }

            if (baosErr.size() > 0) {
                InputStreamReader errISR = new InputStreamReader(new ByteArrayInputStream(baosErr.toByteArray()), "ISO-8859-1");
                BufferedReader errBR = new BufferedReader(errISR);
                StringBuilder errSB = new StringBuilder("Gerrit CLI returned with an error:");
                String errLine;
                while ((errLine = errBR.readLine()) != null) {
                    errSB.append("\n").append(errLine);
                }
                throw andDisconnect(new CommandException(errSB.toString()));
            }

            return result;
        } catch (JSchException e) {
            throw andDisconnect(new ConnectionException("Failed to create/open channel.", e));
        } catch (IOException e) {
            throw andDisconnect(new ConnectionException("Failed to read errors from channel.", e));
        } finally {
            closeQuietly(channel, baisIn, baosOut, baosErr, manuallyConnected);
        }
    }

    private void closeQuietly(ChannelExec channel, ByteArrayInputStream baisIn, ByteArrayOutputStream baosOut,
            ByteArrayOutputStream baosErr, boolean forceDisconnect) {
        if (channel != null) {
            IOUtils.closeQuietly(baisIn);
            IOUtils.closeQuietly(baosOut);
            IOUtils.closeQuietly(baosErr);
            channel.disconnect();
            if (forceDisconnect) {
                disconnect();
            }
        }
    }

    /**
     * Unfortunately Gerrit sometimes returns its error messages in the normal response instead of the error stream.
     * Therefore this utility method should check for common erros and could be extended accordingly.
     *
     * @param firstLine
     *
     * @throws CommandException    in case of unsuccessful commands
     */
    private void checkForErrorsInResponse(String firstLine) throws CommandException {
        if (firstLine == null) {
            return;
        }

        if (firstLine.startsWith("Error when trying to")) {
            throw andDisconnect(new CommandException(firstLine));
        }

        if (firstLine.startsWith("{\"type\":\"error\"")) {
            throw andDisconnect(new CommandException(String.format("Command returned with error: '%s'",
                    JSONUtil.getString(firstLine, "message"))));
        }
    }

    /**
     * Appends an argument to the string buffer, if the given <code>value</code>
     * is not null or blank. The value is enclosed in double quotes.
     *
     * @param sb  the buffer that is worked on.
     * @param value  the argument value.
     */
    private void appendArgument(StringBuilder sb, String value) {
        if (!StringUtils.isBlank(value)) {
            sb.append(" \"").append(value).append("\"");
        }
    }

    /**
     * Appends a boolean option to the string buffer, e.g. <tt>"--permissionsOnly"</tt>.
     * Note that the value is not rendered. If the given <code>value</code> evaluated
     * to <code>false</code> the option is not rendered at all.
     *
     * @param sb  the buffer that is worked on.
     * @param name  the name of the option.
     * @param value  the value of the option.
     */
    private void appendOption(StringBuilder sb, String name, boolean value) {
        if (StringUtils.isNotBlank(name) && value == true) {
            sb.append(" --").append(name);
        }
    }

    /**
     * Appends a boolean option with the possible values <code>TRUE</code>, <code>FALSE</code>
     * or <code>INHERITED</code> to the string buffer.
     * Note that the value is not rendered. If the given <code>value</code> does not evaluate
     * to {@link InheritableBoolean#TRUE} the option is not rendered at all.
     *
     * @param sb  the buffer that is worked on.
     * @param name  the name of the option.
     * @param value  the value of the option.
     */
    private void appendOption(StringBuilder sb, String name, InheritableBoolean value) {
        if (StringUtils.isNotBlank(name) && InheritableBoolean.TRUE == value) {
            sb.append(" --").append(name);
        }
    }

    /**
     * Appends a string option to the string buffer, if the given <code>value</code>
     * is not null or blank. The value is enclosed in double quotes.
     *
     * @param sb  the buffer that is worked on.
     * @param name  the name of the option.
     * @param value  the value of the option.
     */
    private void appendOption(StringBuilder sb, String name, String value) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
            sb.append(" --").append(name).append(" \"").append(value).append("\"");
        }
    }

    /**
     * Appends a multi-valued option to the string buffer, if the given <code>values</code>
     * is not null or empty. This method renders multiple options of the form <tt>--name=value</tt>.
     *
     * @param sb  the buffer that is worked on.
     * @param name  the name of the option.
     * @param value  the collection of values.
     */
    private void appendOption(StringBuilder sb, String name, Collection<String> values) {
        if (CollectionUtils.isNotBlank(values)) {
            for (String value : values) {
                appendOption(sb, name, value);
            }
        }
    }

    /**
     * Checks whether a returned (JSON) string is a GSQL table row
     *
     * @param entry
     *            the entry as serialized JSON String
     *
     * @return <code>true</code> if it starts with
     *         <code>&#123;&quot;type&quot;:&quot;row&quot;;</code>, otherwise
     *         <code>false</code>
     */
    boolean isRow(final String entry) {
        return entry.startsWith("{\"type\":\"row\"");
    }

    /**
     * Terminate connection in error case, before throwing the exception <code>e</code>
     *
     * @throws T
     */
    private <T extends Throwable> T andDisconnect(T e) {
        LOG.error("The last command could not be completed", e);
        disconnect();
        return e;
    }

}
