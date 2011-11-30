/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.common.util.ComparatorUtils;

public class MavenCoordinate {

    private static String DEFAULT_PACKAGING = "jar";
    private String groupId;
    private String artefactId;
    private String packaging;
    private String classifier;

    public MavenCoordinate() {
        super();
    }

    public MavenCoordinate(MavenCoordinate coordinate) {
        this(coordinate.getGroupId(), coordinate.getArtefactId(), coordinate.getPackaging());
    }

    public MavenCoordinate(String groupId, String artefactId, String packaging) {
        this.groupId = groupId;
        this.artefactId = artefactId;
        setPackaging(packaging);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtefactId() {
        return artefactId;
    }

    public void setArtefactId(String artefactId) {
        this.artefactId = artefactId;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        if (packaging == null || packaging.length() == 0) {
            this.packaging = DEFAULT_PACKAGING;
        } else {
            this.packaging = packaging;
        }
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        if (StringUtils.isNotBlank(classifier)) {
            this.classifier = classifier;
        } else {
            this.classifier = null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artefactId == null) ? 0 : artefactId.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((packaging == null) ? 0 : packaging.hashCode());
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MavenCoordinate)) {
            return false;
        } else {
            return 0 == compareTo((MavenCoordinate) obj);
        }
    }

    public int compareTo(MavenCoordinate obj) {
        int result = ComparatorUtils.compare(getGroupId(), obj.getGroupId());
        if (result == 0) {
            result = ComparatorUtils.compare(getArtefactId(), obj.getArtefactId());
            if (result == 0) {
                result = ComparatorUtils.compare(getPackaging(), obj.getPackaging());
                if (result == 0) {
                    result = ComparatorUtils.compare(getClassifier(), obj.getClassifier());
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupId);
        sb.append(':');
        sb.append(artefactId);
        if (StringUtils.isNotBlank(packaging)) {
            sb.append(':');
            sb.append(packaging);
        }
        if (StringUtils.isNotBlank(classifier)) {
            sb.append("classifier=\'");
            sb.append(classifier);
            sb.append("\'");
        }
        return sb.toString();
    }

}