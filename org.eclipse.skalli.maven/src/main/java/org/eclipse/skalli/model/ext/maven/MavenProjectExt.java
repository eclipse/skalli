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
package org.eclipse.skalli.model.ext.maven;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class MavenProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-Maven"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_GROUPID = "groupID"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_REACTOR_POM = "reactorPOM"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_SITE_URL = "siteUrl"; //$NON-NLS-1$

    @Deprecated
    private String groupID = ""; //$NON-NLS-1$

    private String reactorPOM = ""; //$NON-NLS-1$
    private String siteUrl = ""; //$NON-NLS-1$

    /**
     * @deprecated use {@link MavenReactorProjectExt} instead
     */
    @Deprecated
    public String getGroupID() {
        return groupID;
    }

    /**
     * @deprecated use {@link MavenReactorProjectExt} instead
     */
    @Deprecated
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getReactorPOM() {
        return reactorPOM;
    }

    public void setReactorPOM(String reactorPOM) {
        this.reactorPOM = reactorPOM;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }
}
