package org.eclipse.skalli.model.ext.maven.internal;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.ext.mapping.MapperUtil;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingConfig;
import org.eclipse.skalli.model.ext.maven.MavenPomResolver;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MavenPomResolverBase implements MavenPomResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MavenPomResolverBase.class);

    protected static final String DEFAULT_POM_FILENAME = "pom.xml"; //$NON-NLS-1$

    private ConfigurationService configService;

    abstract protected String getProvider();

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
    }

    @Override
    public boolean canResolve(String scmLocation) {
         ScmLocationMappingConfig mapping = getScmLocationMapping(scmLocation);
         return (mapping != null)? getProvider().equals(mapping.getProvider()) : false;
    }


    protected MavenPom parse(InputStream pomInputStream) throws MavenValidationException, IOException {
        MavenPomParser parser = new MavenPomParserImpl();
        return parser.parse(pomInputStream);
    }

    protected ScmLocationMappingConfig getScmLocationMapping(String scmLocation) {
        if (configService == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no configuration service available");
            }
            return null;
        }

        ScmLocationMapper mapper = new ScmLocationMapper();
        List<ScmLocationMappingConfig> mappings = mapper.getMappings(configService, ScmLocationMapper.ALL_PROVIDERS
                , ScmLocationMapper.MAVEN_RESOLVER);
        if (mappings.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "no suitable scm mapping found for purpose=''{0}''",
                        ScmLocationMapper.MAVEN_RESOLVER));
            }
            return null;
        }

        for (ScmLocationMappingConfig mapping : mappings) {
            if (MapperUtil.matches(scmLocation, mapping)) {
                return mapping;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "no suitable scm mapping found matching scmLocation=''{0}'' && purpose=''{1}''",
                    scmLocation, ScmLocationMapper.MAVEN_RESOLVER));
        }
        return null;
    }

    protected String getRepositoryRoot(String scmLocation) {
        ScmLocationMappingConfig scmMappingConfig = getScmLocationMapping(scmLocation);
        if (scmMappingConfig == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "no scm location mapping available for location {0}", scmLocation));
        }

        String repsitoryRoot = MapperUtil.convert(scmLocation, scmMappingConfig.getPattern(),
                scmMappingConfig.getTemplate(), "");
        return repsitoryRoot;
    }

    protected String getPomFileName(String relativePath) {
        StringBuilder fileName = new StringBuilder();
        if (StringUtils.isBlank(relativePath) || ".".equals(relativePath)) { //$NON-NLS-1$
            fileName.append(DEFAULT_POM_FILENAME);
        }
        else if (!relativePath.endsWith(DEFAULT_POM_FILENAME)) {
            appendPath(fileName, relativePath);
            if (!relativePath.endsWith("/")) { //$NON-NLS-1$
                fileName.append("/"); //$NON-NLS-1$
            }
            fileName.append(DEFAULT_POM_FILENAME);
        }
        else {
            appendPath(fileName, relativePath);
        }
        return fileName.toString();
    }

    protected void appendPath(StringBuilder rootPath, String relativePath) {
        if (relativePath.charAt(0) == '/') {
            rootPath.append(relativePath.substring(1));
        } else {
            rootPath.append(relativePath);
        }
    }

    @SuppressWarnings("nls")
    protected boolean isValidNormalizedPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            if (path.indexOf('\\') >= 0) {
                return false;
            }
            if (path.indexOf("..") >= 0 ||
                    path.startsWith("./") ||
                    path.endsWith("/.") ||
                    path.indexOf("/./") >= 0) {
                return false;
            }
        }
        return true;
    }

}
