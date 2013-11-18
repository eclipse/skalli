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
package org.eclipse.skalli.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

@Historized
public class Project extends ExtensibleEntityBase {

    /**
     * Comparator for comparing projects by their {@link Project#getProjectId() symbolic names}.
     */
    public static class CompareByProjectId implements Comparator<Project> {
        @Override
        public int compare(Project o1, Project o2) {
            return o1.getProjectId().compareTo(o2.getProjectId());
        }
    }

    /**
     * Comparator for comparing projects by their {@link Project#getName() display names}.
     * If two projects happen ton have the same display name, they arem compared by
     * symbolic name.
     * <p>
     * Note, display names are compared case-insensitive.
     */
    public static class CompareByProjectName implements Comparator<Project> {
        @Override
        public int compare(Project o1, Project o2) {
            int result = o1.getName().compareToIgnoreCase(o2.getName());
            if (result == 0) {
                result = o1.getProjectId().compareTo(o2.getProjectId());
            }
            return result;
        }
    }

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_PROJECTID = "projectId"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

    @PropertyName(position = 2)
    public static final String PROPERTY_SHORT_NAME = "shortName"; //$NON-NLS-1$

    @PropertyName(position = 3)
    public static final String PROPERTY_DESCRIPTION_FORMAT = "descriptionFormat"; //$NON-NLS-1$

    @PropertyName(position = 4)
    public static final String PROPERTY_DESCRIPTION = "description"; //$NON-NLS-1$

    @PropertyName(position = 5)
    public static final String PROPERTY_TEMPLATEID = "projectTemplateId"; //$NON-NLS-1$

    @Derived
    @PropertyName(position = 6)
    public static final String PROPERTY_PARENT_PROJECT = "parentProject"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_LOGO_URL = "logoUrl"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_PHASE = "phase"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_REGISTERED = "registered"; //$NON-NLS-1$

    private static final String DEFAULT_TEMPLATE_ID = "default"; //$NON-NLS-1$

    public static final String DEFAULT_FORMAT = "text"; //$NON-NLS-1$
    public static final String FORMAT_HTML = "html"; //$NON-NLS-1$
    public static final List<String> TEXT_FORMATS = Arrays.asList(DEFAULT_FORMAT, FORMAT_HTML);

    private static final String INITIAL_PHASE = "initial"; //$NON-NLS-1$

    private String projectId = ""; //$NON-NLS-1$
    private String projectTemplateId = DEFAULT_TEMPLATE_ID;
    private String name = ""; //$NON-NLS-1$
    private String shortName = ""; //$NON-NLS-1$
    private String descriptionFormat = TEXT_FORMATS.get(0);
    private String description = ""; //$NON-NLS-1$
    private String logoUrl = ""; //$NON-NLS-1$
    private String phase = INITIAL_PHASE;
    private long registered = 0;

    // do not remove: required by xstream
    public Project() {
    }

    public Project(String projectTemplateId) {
        this.projectTemplateId = projectTemplateId;
    }

    public Project(String projectid, String description, String name) {
        this.name = name;
        this.description = description;
        this.projectId = projectid;
    }

    public String getDescriptionFormat() {
        if (StringUtils.isBlank(descriptionFormat)) {
            descriptionFormat = DEFAULT_FORMAT;
        }
        return descriptionFormat;
    }

    public void setDescriptionFormat(String descriptionFormat) {
        this.descriptionFormat = descriptionFormat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Returns the short name if it exists or constructs one from the project name otherwise.
     *
     * <p>
     * The constructed name is guaranteed to contain only alpha-numeric letters and to be 2 to 10 characters long.
     * It will consist of the first letters of each word, if the project name includes multiple words.
     * Otherwise the project name shortened to max. 10 characters will be returned.
     * </p>
     *
     * @return
     */
    public String getOrConstructShortName() {
        if (!StringUtils.isBlank(shortName)) {
            return shortName;
        } else {
            if (name.contains(" ")) { //$NON-NLS-1$
                // use first characters of each word, if they are alpha-numeric and the resulting length exceeds 1 char.
                StringBuilder retAbbrev = new StringBuilder();
                for (String n : name.split(" ")) { //$NON-NLS-1$
                    if (n.length() > 0) {
                        char ch = n.charAt(0);
                        if (CharUtils.isAsciiAlphanumeric(ch) && retAbbrev.length() < 10) {
                            retAbbrev.append(ch);
                        }
                    }
                }
                // if the first-char abbreviation is long enough, use it
                if (retAbbrev.length() > 1) {
                    return retAbbrev.toString();
                }
            }

            // Otherwise use the first 10 alpha-numeric characters of the project name, if they exceed 1 char.
            StringBuilder retShortenedName = new StringBuilder();
            for (int i = 0; i < name.length() && retShortenedName.length() < 10; i++) {
                char ch = name.charAt(i);
                if (CharUtils.isAsciiAlphanumeric(ch)) {
                    retShortenedName.append(ch);
                }
            }
            if (retShortenedName.length() > 1) {
                return retShortenedName.toString();
            }

            // If still no valid short name was found, then return the last 10 alpha-numeric characters of the project id
            StringBuilder retProjectId = new StringBuilder();
            for (int i = projectId.length() - 1; i >= 0 && retProjectId.length() < 10; i--) {
                char ch = projectId.charAt(i);
                if (CharUtils.isAsciiAlphanumeric(ch)) {
                    retProjectId.append(ch);
                }
            }
            return retProjectId.reverse().toString();
        }
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectid) {
        this.projectId = projectid;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getProjectTemplateId() {
        if (StringUtils.isBlank(projectTemplateId)) {
            projectTemplateId = DEFAULT_TEMPLATE_ID;
        }
        return projectTemplateId;
    }

    public void setProjectTemplateId(String projectTemplateId) {
        if (StringUtils.isBlank(projectTemplateId)) {
            projectTemplateId = DEFAULT_TEMPLATE_ID;
        }
        this.projectTemplateId = projectTemplateId;
    }

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        if (registered <= 0) {
            registered = System.currentTimeMillis();
        }
        this.registered = registered;
    }

    public String getPhase() {
        if (StringUtils.isBlank(phase)) {
            phase = INITIAL_PHASE;
        }
        return phase;
    }

    public void setPhase(String phase) {
        if (StringUtils.isBlank(phase)) {
            phase = INITIAL_PHASE;
        }
        this.phase = phase;
    }

    public UUID getParentProject() {
        return getParentEntityId();
    }

    /**
     * Returns the subprojects of this project sorted by their
     * {@link #getProjectId() symbolic names}.
     *
     * @return  the subprojects of this project, or an empty set if this
     * project has no subprojects.
     */
    public SortedSet<Project> getSubProjects() {
        return addSubProjects(new TreeSet<Project>(new CompareByProjectId()));
    }

    /**
     * Returns the subprojects of this project sorted with the
     * given comparator. If no compartor is specified the result is
     * the same as for {@link #getSubProjects()}.
     *
     * @param c  the comparator to use fir sorting of the result.
     * @return  the subprojects of this project, or an empty set if this
     * project has no subprojects.
     */
    public SortedSet<Project> getSubProjects(Comparator<Project> c) {
        if (c == null) {
            return getSubProjects();
        }
        return addSubProjects(new TreeSet<Project>(c));
    }

    private <T extends Collection<Project>> T addSubProjects(T c) {
        EntityBase next = getFirstChild();
        while (next != null) {
            c.add((Project)next);
            next = next.getNextSibling();
        }
        return c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(projectId)) {
            sb.append(projectId);
        }
        if (StringUtils.isNotBlank(name)) {
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(name);
        }
        String uuid = super.toString();
        if (sb.length() > 0) {
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(uuid);
        }
        return sb.toString();
    }
}
