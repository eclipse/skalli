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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.skalli.model.Project;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("projects")
public class Projects {

    private Set<Project> projects;

    public Projects() {
    }

    public Projects(Collection<Project> projects) {
        this.projects = new HashSet<Project>(projects);
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
}
