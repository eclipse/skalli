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
import java.util.LinkedHashSet;

import org.eclipse.skalli.model.Project;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("subprojects")
public class Subprojects {

    private LinkedHashSet<Project> subprojects;

    public Subprojects() {
    }

    public Subprojects(Collection<Project> projects) {
        this.subprojects = new LinkedHashSet<Project>(projects);
    }

    public LinkedHashSet<Project> getSubprojects() {
        return subprojects;
    }

    public void setSubprojects(LinkedHashSet<Project> subprojects) {
        this.subprojects = subprojects;
    }

    public void addAll(Collection<Project> projects) {
        subprojects.addAll(projects);
    }
}
