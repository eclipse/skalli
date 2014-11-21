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
package org.eclipse.skalli.view.internal.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("branding")
public class BrandingConfig {

    private String pageTitle;
    private String searchPluginTitle;
    private String searchPluginDescription;

    // do not remove: required by xstream
    public BrandingConfig() {
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getSearchPluginTitle() {
        return searchPluginTitle;
    }

    public void setSearchPluginTitle(String searchPluginTitle) {
        this.searchPluginTitle = searchPluginTitle;
    }

    public String getSearchPluginDescription() {
        return searchPluginDescription;
    }

    public void setSearchPluginDescription(String searchPluginDescription) {
        this.searchPluginDescription = searchPluginDescription;
    }
}

