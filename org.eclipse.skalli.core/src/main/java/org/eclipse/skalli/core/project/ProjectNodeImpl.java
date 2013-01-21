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
package org.eclipse.skalli.core.project;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.project.ProjectNode;
import org.eclipse.skalli.services.project.ProjectService;

public class ProjectNodeImpl implements ProjectNode {

    protected Project project = null;
    protected List<ProjectNode> children = new LinkedList<ProjectNode>();

    protected ProjectNodeImpl(ProjectService service, UUID uuid, Comparator<Project> c) {
        Project project = service.getByUUID(uuid);
        initialize(service, project, c);
    }

    protected ProjectNodeImpl(ProjectService service, Project project, Comparator<Project> c) {
        initialize(service, project, c);
    }

    protected void initialize(ProjectService service, Project project, Comparator<Project> c) {
        this.project = project;
        List<Project> orderedSubProjects = service.getSubProjects(project.getUuid());
        if (c != null) {
            Collections.sort(orderedSubProjects, c);
        }
        for (Project subProject : orderedSubProjects) {
            ProjectNodeImpl subNode = new ProjectNodeImpl(service, subProject, c);
            children.add(subNode);
        }
    }

    @Override
    public List<ProjectNode> getSubProjects() {
        return children;
    }

    @Override
    public Project getProject() {
        return project;
    }

}
