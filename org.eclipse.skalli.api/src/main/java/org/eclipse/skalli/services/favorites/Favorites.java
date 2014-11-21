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
package org.eclipse.skalli.services.favorites;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.PropertyName;

public class Favorites extends EntityBase {

    @PropertyName
    public static final String PROPERTY_USERID = "userId"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_PROJECTS = "projects"; //$NON-NLS-1$

    private String userId = ""; //$NON-NLS-1$

    // XStream by default is not able to unmarshal ArrayList<UUID>, so we
    // need a special converter for that, see UUIDListConverter
    private UUIDList projects = new UUIDList();

    public Favorites() {
    }

    public Favorites(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<UUID> getProjects() {
        if (projects == null) {
            projects = new UUIDList();
        }
        return projects;
    }

    public void setProjects(UUIDList projects) {
        this.projects = projects != null ? new UUIDList(projects) : null;
    }

    public boolean hasProject(UUID project) {
        return project != null ? getProjects().contains(project) : false;
    }

    public void addProject(UUID project) {
        if (project != null) {
            List<UUID> list = getProjects();
            if (!list.contains(project)) {
                list.add(project);
            }
        }
    }

    public void removeProject(UUID project) {
        if (project != null && projects != null && projects.size() > 0) {
            projects.remove(project);
        }
    }

    public void moveUp(UUID project) {
        if (project != null && projects != null && projects.size() > 1) {
            List<UUID> list = getProjects();
            int idx = list.indexOf(project);
            if (idx > 0) {
                list.remove(idx);
                list.add(idx - 1, project);
            }
        }
    }

    public void moveDown(UUID project) {
        if (project != null && projects != null && projects.size() > 1) {
            List<UUID> list = getProjects();
            int idx = list.indexOf(project);
            if (idx >= 0 && idx < list.size() - 1) {
                list.remove(idx);
                list.add(idx + 1, project);
            }
        }
    }

    public Map<String, UUID> asMap() {
        List<UUID> uuids = getProjects();
        HashMap<String, UUID> result = new HashMap<String, UUID>();
        for (UUID uuid : uuids) {
            result.put(uuid.toString(), uuid);
        }
        return result;
    }
}
