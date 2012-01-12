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
package org.eclipse.skalli.model.ext.maven.recommendedupdatesites;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.PropertyName;

public class RecommendedUpdateSites extends EntityBase {

    @PropertyName
    public static final String PROPERTY_ID = "id"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_USERID = "userId"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_DESCIPTION = "description"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_SHORT_NAME = "shortName"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_UPDATESITES = "updateSites"; //$NON-NLS-1$

    private String id = ""; //$NON-NLS-1$
    private String userId = ""; //$NON-NLS-1$
    private String name = ""; //$NON-NLS-1$
    private String shortName = ""; //$NON-NLS-1$
    private String description = ""; //$NON-NLS-1$

    private List<UpdateSite> updateSites = new ArrayList<UpdateSite>();

    public RecommendedUpdateSites() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<UpdateSite> getUpdateSites() {
        if (updateSites == null) {
            updateSites = new ArrayList<UpdateSite>();
        }
        return updateSites;
    }

    public void setUpdateSites(List<UpdateSite> updateSites) {
        this.updateSites = updateSites;
    }

    public void addUpdateSite(UpdateSite updateSites) {
        getUpdateSites().add(updateSites);
    }

    public void removeUpdateSite(UpdateSite updateSites) {
        getUpdateSites().remove(updateSites);
    }

    public boolean hasUpdateSite(UpdateSite updateSites){
        return getUpdateSites().contains(updateSites);
    }

    @Override
    public String toString() {
        return "RecommendedUpdateSites [id=" + id + ", userId=" + userId + ", name=" + name + ", description="
                + description + ", updateSites=" + updateSites + "]";
    }
}
