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
package org.eclipse.skalli.model.ext.devinf;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class DevInfProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-DevInf"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_SCM_URL = "scmUrl"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_SCM_LOCATIONS = "scmLocations"; //$NON-NLS-1$

    @PropertyName(position = 2)
    public static final String PROPERTY_BUGTRACKER_URL = "bugtrackerUrl"; //$NON-NLS-1$

    @PropertyName(position = 3)
    public static final String PROPERTY_CI_URL = "ciUrl"; //$NON-NLS-1$

    @PropertyName(position = 4)
    public static final String PROPERTY_METRICS_URL = "metricsUrl"; //$NON-NLS-1$

    @PropertyName(position = 5)
    public static final String PROPERTY_REVIEW_URL = "reviewUrl"; //$NON-NLS-1$

    @PropertyName(position = 6)
    public static final String PROPERTY_JAVADOCS_URL = "javadocs"; //$NON-NLS-1$

    private String scmUrl = ""; //$NON-NLS-1$
    private String bugtrackerUrl = ""; //$NON-NLS-1$
    private String ciUrl = ""; //$NON-NLS-1$
    private String metricsUrl = ""; //$NON-NLS-1$
    private String reviewUrl = ""; //$NON-NLS-1$

    // using a linked hash set to preserve ordering
    private LinkedHashSet<String> scmLocations = new LinkedHashSet<String>();
    private LinkedHashSet<String> javadocs = new LinkedHashSet<String>();

    public String getBugtrackerUrl() {
        return bugtrackerUrl;
    }

    public void setBugtrackerUrl(String bugtrackerUrl) {
        this.bugtrackerUrl = bugtrackerUrl;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public String getMetricsUrl() {
        return metricsUrl;
    }

    public void setMetricsUrl(String metricsUrl) {
        this.metricsUrl = metricsUrl;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public String getScmLocation() {
        if (scmLocations == null) {
            scmLocations = new LinkedHashSet<String>();
        }
        return scmLocations.isEmpty() ? null : scmLocations.iterator().next();
    }

    public synchronized Set<String> getScmLocations() {
        if (scmLocations == null) {
            scmLocations = new LinkedHashSet<String>();
        }
        return scmLocations;
    }

    public void setScmLocations(Collection<String> scmLocations) {
        this.scmLocations = new LinkedHashSet<String>(scmLocations);
    }

    public void addScmLocation(String scmLocation) {
        if (scmLocation != null) {
            getScmLocations().add(scmLocation);
        }
    }

    public void removeScmLocation(String scmLocation) {
        if (scmLocation != null) {
            getScmLocations().remove(scmLocation);
        }
    }

    public boolean hasScmLocation(String scmLocation) {
        return getScmLocations().contains(scmLocation);
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public synchronized Set<String> getJavadocs() {
        if (javadocs == null) {
            javadocs = new LinkedHashSet<String>();
        }
        return javadocs;
    }

    public void setJavadocs(Collection<String> javadocs) {
        this.javadocs = new LinkedHashSet<String>(javadocs);
    }

    public void addJavadoc(String javadoc) {
        if (javadoc != null) {
            getJavadocs().add(javadoc);
        }
    }

    public void removeJavadoc(String javadoc) {
        if (javadoc != null) {
            getJavadocs().remove(javadoc);
        }
    }

    public boolean hasJavadoc(String javadoc) {
        return getJavadocs().contains(javadoc);
    }
}
