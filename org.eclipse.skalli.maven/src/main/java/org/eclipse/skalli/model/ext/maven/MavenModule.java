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
package org.eclipse.skalli.model.ext.maven;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.util.ComparatorUtils;

public class MavenModule extends MavenCoordinate implements Comparable<MavenModule> {

    private TreeSet<String> versions = new TreeSet<String>();

    private String name;
    private String description;

    public MavenModule() {
    }

    public MavenModule(MavenModule module) {
        super(module);
        versions.addAll(module.getVersions());
    }

    public MavenModule(String groupId, String artefactId, String packaging) {
        super(groupId, artefactId, packaging);
    }

    /**
     * @return the name or null if no name is set
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (StringUtils.isNotBlank(name)) {
            this.name = name;
        } else {
            this.name = null;
        }
    }

    /**
     * @return the description or Null if no description is set.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (StringUtils.isNotBlank(description)) {
            this.description = description;
        } else {
            this.description = null;
        }
    }

    public synchronized Set<String> getVersions() {
        if (versions == null) {
            versions = new TreeSet<String>();
        }
        return versions;
    }

    /**
     * @return String - the last (highest) version currently in {@link #getVersions()}.
     */
    public String getLatestVersion() {
        try {
            return getSortedVersions().first();
        } catch (NoSuchElementException e) {
            return null;
        }

    }

    /**
     * @return an unmodifiable set of available artifact versions sorted according to {@link MavenVersionsGreatestFirstComparator}.
     */
    public SortedSet<String> getSortedVersions() {
        TreeSet<String> sortedVersions = new TreeSet<String>(new MavenVersionsComparator(
                MavenVersionsComparator.SortOrder.DESCENDING));
        sortedVersions.addAll(getVersions());
        return Collections.unmodifiableSortedSet(sortedVersions);
    }

    public void addVersion(String version) {
        getVersions().add(version);
    }

    public boolean hasVersion(String version) {
        return getVersions().contains(version);
    }

    public void removeVersion(String version) {
        getVersions().remove(version);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        for (String version : getVersions()) {
            result = prime * result + ((version == null) ? 0 : version.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof MavenModule)) {
            return false;
        } else {
            return 0 == compareTo((MavenModule) obj);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MavenModule obj) {
        int result = super.compareTo(obj);
        if (result == 0) {
            if (result == 0) {
                result = ComparatorUtils.compare(versions, obj.versions);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());

        if (StringUtils.isNotBlank(name) || StringUtils.isNotBlank(description)) {
            sb.append("(");
            if (StringUtils.isNotBlank(name)) {
                sb.append("name:\'");
                sb.append(name);
                sb.append("\'");
            }
            if (StringUtils.isNotBlank(description)) {
                sb.append("description=\'");
                sb.append(description);
                sb.append("\'");
            }
            sb.append(")");
        }
        if (getVersions().size() > 0) {
            sb.append(':');
            sb.append(StringUtils.join(getVersions(), ','));
        }
        return sb.toString();

    }

}
