/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.config;

import org.eclipse.skalli.gerrit.client.SubmitType;
import org.eclipse.skalli.services.configuration.rest.Protect;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("gerrit")
public class GerritConfig {

    private String protocol;
    private String host;
    private String port;
    @Protect
    private String user;
    @Protect
    private String privateKey;
    @Protect
    private String passphrase;
    private String contact;
    private String scmTemplate;
    private String groupDescription;
    private String projectDescription;
    private String parent;
    private String branch;
    private SubmitType submitType;
    private boolean useContributorAgreement;
    private boolean useSignedOffBy;
    private boolean subprojectsOnly;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    /**
     * Returns the template from which the SCM location is
     * derived for new Gerrit projects.
     * @return the configured SCM templates, or <code>null</code>.
     */
    public String getScmTemplate() {
        return scmTemplate;
    }

    /**
     * Sets the template from which the SCM location is derived for new Gerrit projects.
     * <p>
     * If no template is specified the SCM location is built using the pattern
     * <tt>"scm:git:${protocol}://${gerritHost}:${gerritPort}/${repository}.git"</tt>,
     * where the parameters are taken from the corresponding configuration
     * parameters (except <tt>"${protocol}"</tt>).
     * <p>
     * Available variables are: <tt>"${protocol}"</tt>, <tt>"${gerritHost}"</tt>, <tt>"${gerritPort}"</tt>,
     * <tt>"${repository}"</tt>, <tt>"${branch}"</tt>, <tt>"${parent}"</tt> and <tt>"${userId}"</tt>
     * (identifier of the user requesting the project creation).
     *
     * @param scmTemplate the template to use, or <code>null</code>.
     *
     */
    public void setScmTemplate(String scmTemplate) {
        this.scmTemplate = scmTemplate;
    }

    /**
     * Returns the default description to be assigned to new Gerrit projects.
     * The description might contains variables.
     * @returns the configured description, or <code>null</code>.
     */
    public String getProjectDescription() {
        return projectDescription;
    }

    /**
     * Sets the default description to be assigned to new Gerrit groups.
     * @param description  the description to set, or <code>null</code>.
     */
    public void setGrouptDescription(String description) {
        this.groupDescription = description;
    }

    /**
     * Returns the default description to be assigned to new Gerrit groups.
     * The description might contains variables.
     * @returns the configured description, or <code>null</code>.
     */
    public String getGroupDescription() {
        return groupDescription;
    }

    /**
     * Sets the default description to be assigned to new Gerrit projects.
     * @param description  the description to set, or <code>null</code>.
     */
    public void setProjectDescription(String description) {
        this.projectDescription = description;
    }

    /**
     * Returns the default parent project to be assigned to new Gerrit projects.
     * @return the configured parent projects to be assigned, or <code>null</code>.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the default parent project to be assigned to new Gerrit projects.
     * @param parent  the parent to set, or <code>null</code>. If not specified
     * upon project creation Gerrit assigns the built-in <tt>"All Projects"</tt>
     * project as parent.
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Returns the name of the default branch to be created for new Gerrit projects.
     * @return the initial branch to create, or <code>null</code>.
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Sets the name of the default branch to be created for new Gerrit projects.
     * @param branch the branch to set, or <code>null</code>. If not specified
     * upon project creation Gerrit creates a <tt>"master"</tt> branch by default.
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Returns the submit type (merge strategy) to be applied for new Gerrit projects.
     * @return the submitType  the submit type.
     */
    public SubmitType getSubmitType() {
        return submitType;
    }

    /**
     * Sets the submit type (merge strategy) to be applied for new Gerrit projects.
     * @param submitType the submitType to set.
     */
    public void setSubmitType(SubmitType submitType) {
        this.submitType = submitType;
    }

    /**
     * Returns <code>true</code>, if commits for the new project are subject to a
     * contributor agreement.
     */
    public boolean isUseContributorAgreement() {
        return useContributorAgreement;
    }

    /**
     * Defines whether commits for new Gerrit project shall be subject to a
     * contributor agreement.
     * @param useContributorAgreement if <code>true</code> commits for new
     * Gerrit projects shall be subject to a contributor agreement.
     */
    public void setUseContributorAgreement(boolean useContributorAgreement) {
        this.useContributorAgreement = useContributorAgreement;
    }

    /**
     * Returns <code>true</code>, if commits for new Gerrit projects must have
     * a <tt>"Signed-off-by:"</tt> header.
     */
    public boolean isUseSignedOffBy() {
        return useSignedOffBy;
    }

    /**
     * Defines whether commits for new Gerrit projects must have a
     * <tt>"Signed-off-by:"</tt> header.
     * @param useSignedOffBy  if <code>true</code> commits for new Gerrit projects
     * must have a <tt>"Signed-off-by:"</tt> header.
     */
    public void setUseSignedOffBy(boolean useSignedOffBy) {
        this.useSignedOffBy = useSignedOffBy;
    }

    /**
     * Returns <code>true</code>, if only subprojects are allowed to have
     * Git repositories, but not  top-level projects.
     */
    public boolean isSubprojectsOnly() {
        return subprojectsOnly;
    }

    /**
     * Defines whether top-level projects or only subprojects can have
     * Git repositories. By default, all projects can have Git repositories.
     *
     * @param subprojectsOnly  if <code>true</code> only subprojects are allowed to have
     * Git repositories.
     */
    public void setSubprojectsOnly(boolean subprojectsOnly) {
        this.subprojectsOnly = subprojectsOnly;
    }


}
