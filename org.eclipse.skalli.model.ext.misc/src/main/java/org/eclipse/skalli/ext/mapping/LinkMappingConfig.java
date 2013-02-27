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
package org.eclipse.skalli.ext.mapping;

public class LinkMappingConfig {

    private String id;
    private String purpose;
    private String pattern;
    private String template;
    private String name;

    public LinkMappingConfig() {
    }

    public LinkMappingConfig(String id, String purpose, String pattern, String template, String name) {
        this.id = id;
        this.purpose = purpose;
        this.pattern = pattern;
        this.template = template;
        this.name = name;
    }

    /**
     * Returns the unique identifier of the mapping entry.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the mapping entry.
     * @param id  an identifier, e.g. <code>"git.eclipse.org"</code>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns a human readable name or description for the mapping entry.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a human readable name or description for the mapping entry.
     * @param name  the name or description to set, e.g.
     * <code>"Project Summary on eclipse.org"</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the intended purpose of the mapping, e.g. whether it specifies a mapping
     * to a browsable destination or to a review system.
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * Sets the intended purpose of the mapping, e.g. whether it specifies a mapping
     * to a browsable destination or to a review system.
     * @param purpose  the purpose of the mapping.
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * Returns the regular expression that should be matched to SCM location strings.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the regular expression that should be matched to SCM location strings.
     * @param pattern a regular expression, e.g.
     * <code>"^scm:git:git://git.eclipse.org/gitroot/(.+\.git)$"</code>.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the template for links to be calculated from a given SCM location
     * and the {@link #getPattern() location pattern}.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template for links to be calculated from a given SCM location and the
     * {@link #getPattern() location pattern}.
     *
     * @param template  the link template, e.g. <code>"http://git.eclipse.org/c/{1}/"</code>.
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", purpose=" + purpose + ", pattern="
                + pattern + ", template=" + template + ", name=" + name + "]";
    }

}
