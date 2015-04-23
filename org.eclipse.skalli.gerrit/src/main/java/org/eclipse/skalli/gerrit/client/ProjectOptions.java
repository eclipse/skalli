/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;

/**
 * Representation of the Gerrit <tt>ProjectInput</tt> entity that
 * contains information for the creation of a new project.
 */
public class ProjectOptions {

    private String name;
    private String parent;
    private String description;
    private boolean permissionsOnly;
    private boolean createEmptyCommit;
    private SubmitType submitType;
    private SortedSet<String> branches;
    private SortedSet<String> owners;
    private InheritableBoolean useContributorAgreements;
    private InheritableBoolean useSignedOffBy;
    private InheritableBoolean useContentMerge;
    private InheritableBoolean requiredChangeId;
    private String maxObjectSizeLimit;
    private Map<String,Map<String,String>> pluginConfigValues;

    /**
     * Returns the name of the new project to create.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the new project to create. If the name ends with <tt>.git</tt> Gerrit
     * will remove that suffix automatically.
     *
     * @param name  the name of the project to create, not <code>null</code> or blank.
     */
    public void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name;
    }


    /**
     * Returns the name of the parent project to inherit access rights from.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the name of the parent project to inherit access rights from.
     * If not specified, the parent will be either taken from the Gerrit server
     * configuration, or Gerrit will assign the default project <tt>All-Projects</tt>.
     *
     * @param parent  the name of the parent project.
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Returns the initial description of the project.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the initial description of the project. If not specified, a description
     * will be created based on the <tt>projectDescription</tt> template specified
     * in the Gerrit server configuration.
     *
     * @param description  the description of the project.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks whether the project will be created as permissions-only project.
     */
    public boolean isPermissionsOnly() {
        return permissionsOnly;
    }

    /**
     * Determines whether the project will be created as permissions-only proejct.
     * This kind of project serves as parent for other projects.
     *
     * @param permissionsOnly  if <code>true</code> the project will be created
     * as permissions-only project.
     */
    public void setPermissionsOnly(boolean permissionsOnly) {
        this.permissionsOnly = permissionsOnly;
    }

    /**
     * Checks whether an initial empty commit in the Git repository of the new project
     * will be created.
     */
    public boolean isCreateEmptyCommit() {
        return createEmptyCommit;
    }

    /**
     * Determines whether an initial empty commit in the Git repository of the new project
     * will be created.
     *
     * @param createEmptyCommit if <code>true</code> an initial commit will be created.
     */
    public void setCreateEmptyCommit(boolean createEmptyCommit) {
        this.createEmptyCommit = createEmptyCommit;
    }

    /**
     * Returns the submit method Gerrit will apply to approved change.
     */
    public SubmitType getSubmitType() {
        return submitType;
    }

    /**
     * Sets the submit method Gerrit will apply to approved change.
     *
     * @param submitType  the submit method to apply to changes.
     */
    public void setSubmitType(SubmitType submitType) {
        this.submitType = submitType;
    }

    /**
     * Returns the name(s) of the initial branch(es) in the newly created project,
     * or an empty set.
     */
    public SortedSet<String> getBranches() {
        if (branches == null) {
            return CollectionUtils.emptySortedSet();
        }
        return branches;
    }

    /**
     * Sets the name of the initial branch in the newly created project.
     * If not specified Gerrit will create a default <tt>master</tt> branch.
     *
     * @param branch  a branch name.
     */
    public void setBranch(String branch) {
        if (StringUtils.isNotBlank(branch)) {
            this.branches = CollectionUtils.asSortedSet(branch);
        }
    }

    /**
     * Sets the name(s) of the initial branch(es) in the newly created project.
     * If not specified Gerrit will create a default <tt>master</tt> branch.
     *
     * @param branch  a collection of branch names.
     */
    public void setBranches(Collection<String> branches) {
        if (CollectionUtils.isNotBlank(branches)) {
            this.branches = new TreeSet<String>(branches);
        }
    }

    /**
     * Returns the name(s) of the group(s) which will initially own the Git repository
     * of the new project.
     */
    public SortedSet<String> getOwners() {
        if (owners == null) {
            return CollectionUtils.emptySortedSet();
        }
        return owners;
    }

    /**
     * Sets the name of the group which will initially own the Git repository
     * of the new project.
     *
     * @param owner the owner's name.
     */
    public void setOwner(String owner) {
        if (StringUtils.isNotBlank(owner)) {
            this.owners = CollectionUtils.asSortedSet(owner);
        }
    }

    /**
     * Sets the name(s) of the group(s) which will initially own the Git repository
     * of the new project.
     *
     * @param owners a collection of owners.
     */
    public void setOwners(Collection<String> owners) {
        if (CollectionUtils.isNotBlank(owners)) {
            this.owners = new TreeSet<String>(owners);
        }
    }

    /**
     * Checks whether authors must sign a contributor agreement prior to pushing
     * changes to the new project.
     */
    public InheritableBoolean getUseContributorAgreements() {
        return useContributorAgreements;
    }

    /**
     * Determines whether authors must sign a contributor agreement prior to pushing
     * changes to the new project.
     *
     * @param useContributorAgreements   if <code>true</code> the project requires
     * a contributor agreements.
     */
    public void setUseContributorAgreements(InheritableBoolean useContributorAgreements) {
        this.useContributorAgreements = useContributorAgreements;
    }

    /**
     * Checks whether authors must provide a <tt>Signed-off-by</tt> footer in their
     * commits for the project.
     */
    public InheritableBoolean getUseSignedOffBy() {
        return useSignedOffBy;
    }

    /**
     * Determines whether authors must provide a <tt>Signed-off-by</tt> footer in their
     * commits for the project.
     *
     * @param useSignedOffBy if <code>true</code> the project requires
     * <tt>Signed-off-by</tt> footers in commits.
     */
    public void setUseSignedOffBy(InheritableBoolean useSignedOffBy) {
        this.useSignedOffBy = useSignedOffBy;
    }

    /**
     * Checks whether Gerrit should perform 3-way merges for the new project.
     */
    public InheritableBoolean getUseContentMerge() {
        return useContentMerge;
    }

    /**
     * Determines whether Gerrit should perform 3-way merges for the new project.
     *
     * @param useContentMerge  if <code>true</code> 3-way merges will applied
     * when submitting commits.
     */
    public void setUseContentMerge(InheritableBoolean useContentMerge) {
        this.useContentMerge = useContentMerge;
    }

    /**
     * Checks whether Gerrit will require a <tt>Change-Id</tt> footer for all commits.
     */
    public InheritableBoolean getRequiredChangeId() {
        return requiredChangeId;
    }

    /**
     * Determines whether Gerrit should require a <tt>Change-Id</tt> footer for all commits.
     *
     * @param requiredChangeId  if <code>true</code>, a change id will be required.
     */
    public void setRequiredChangeId(InheritableBoolean requiredChangeId) {
        this.requiredChangeId = requiredChangeId;
    }

    /**
     * Returns the maximum allowed size for individual repository objects.
     */
    public String getMaxObjectSizeLimit() {
        return maxObjectSizeLimit;
    }

    /**
     * Sets the maximum allowed size for individual repository objects.
     *
     * @param maxObjectSizeLimit  the maximum allowed size in the format
     * <tt>&lt;number&gt;&lt;unit&gt;</tt>, e.g. seomthing like <tt>"10m"</tt>.
     */
    public void setMaxObjectSizeLimit(String maxObjectSizeLimit) {
        this.maxObjectSizeLimit = maxObjectSizeLimit;
    }

    /**
     * Returns the names of all plugins with configuration options.
     *
     * @return an (unmodifiable) set of plugin names, or an empty set.
     */
    public Set<String> getPluginConfigKeys() {
        if (pluginConfigValues == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(pluginConfigValues.keySet());
    }

    /**
     * Returns the configuration options for the given plugin.
     *
     * @param pluginName  the name of the plugin.
     *
     * @return an (unmodifiable) map of configuration options, or <code>null</code>
     * if the plugin has no configuration options.
     */
    public Map<String,String> getPluginConfig(String pluginName) {
        if (pluginConfigValues == null || StringUtils.isBlank(pluginName)) {
            return null;
        }
        Map<String,String> pluginConfig = pluginConfigValues.get(pluginName);
        return pluginConfig != null? Collections.unmodifiableMap(pluginConfig) : null;
    }

    /**
     * Returns the value of a configuration option of a given plugin.
     *
     * @param pluginName  the name of the plugin.
     * @param configName  the name of the configuration option.
     *
     * @return  the value of a configuration option, or <code>null</code> if the
     * plugin does not have the specified configuration option.
     */
    public String getPluginConfigValue(String pluginName, String configName) {
        Map<String,String> pluginConfig = getPluginConfig(pluginName);
        return pluginConfig != null && StringUtils.isNotBlank(configName)? pluginConfig.get(configName) : null;
    }

    /**
     * Associates the specified value to a configuration option of a given plugin.
     * Note that the method does nothing if the given plugin name, configuration
     * name or configuration value are <code>null</code> or blank.
     *
     * @param pluginName  the name of the plugin.
     * @param configName  the name of the configuration option.
     * @param configValue  the value of the configuration option.
     */
    public void putPluginConfig(String pluginName, String configName, String configValue) {
        if (StringUtils.isNotBlank(pluginName) && StringUtils.isNotBlank(configName)
                && StringUtils.isNotBlank(configValue)) {
            if (pluginConfigValues == null) {
                pluginConfigValues = new TreeMap<String,Map<String,String>>();
            }
            Map<String,String> pluginConfig = pluginConfigValues.get(pluginName);
            if (pluginConfig == null) {
                pluginConfig = new TreeMap<String,String>();
                pluginConfigValues.put(pluginName, pluginConfig);
            }
            pluginConfig.put(configName, configValue);
        }
    }
}
