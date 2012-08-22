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
package org.eclipse.skalli.api.rest.internal.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.skalli.model.Project;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("projects")
public class Projects {

    private List<Project> projects;

    public Projects() {
    }

    public Projects(Collection<Project> projects) {
        this.projects = new ArrayList<Project>(projects);
    }

    public List<Project> getProjects() {
        if (projects == null) {
            projects = new ArrayList<Project>();
        }
        return projects;
    }

    public void setProjects(Collection<Project> projects) {
        this.projects = new ArrayList<Project>(projects);
    }

    public void addProject(Project project) {
        getProjects().add(project);
    }

    public void addProjects(Collection<Project> projects) {
        getProjects().addAll(projects);
    }
}
