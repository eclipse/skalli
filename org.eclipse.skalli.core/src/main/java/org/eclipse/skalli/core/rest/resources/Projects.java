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
package org.eclipse.skalli.core.rest.resources;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.skalli.model.Project;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("projects")
public class Projects {

    private LinkedHashSet<Project> projects;

    // do not remove: required by xstream
    public Projects() {
    }

    public Projects(Collection<Project> projects) {
        this.projects = new LinkedHashSet<Project>(projects);
    }

    public Set<Project> getProjects() {
        if (projects == null) {
            projects = new LinkedHashSet<Project>();
        }
        return projects;
    }

    public void addProject(Project project) {
        getProjects().add(project);
    }

    public void addAll(Collection<Project> projects) {
        getProjects().addAll(projects);
    }

    public int size() {
        return getProjects().size();
    }
}
