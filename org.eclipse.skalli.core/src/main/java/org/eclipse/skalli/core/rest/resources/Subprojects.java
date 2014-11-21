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
import java.util.UUID;

import org.eclipse.skalli.model.Project;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("subprojects")
public class Subprojects {

    private UUID uuid;
    private LinkedHashSet<Project> subprojects;

    // do not remove: required by xstream
    public Subprojects() {
    }

    public Subprojects(Collection<Project> projects) {
        this.subprojects = new LinkedHashSet<Project>(projects);
    }

    public Subprojects(UUID uuid, Collection<Project> projects) {
        this.uuid = uuid;
        this.subprojects = new LinkedHashSet<Project>(projects);
    }

    public UUID getUUID() {
        return uuid;
    }

    public Set<Project> getSubprojects() {
        if (subprojects == null) {
            subprojects = new LinkedHashSet<Project>();
        }
        return subprojects;
    }

    public void setSubprojects(LinkedHashSet<Project> subprojects) {
        this.subprojects = new LinkedHashSet<Project>(subprojects);
    }

    public void addAll(Collection<Project> projects) {
        getSubprojects().addAll(projects);
    }
}
