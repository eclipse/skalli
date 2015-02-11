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
package org.eclipse.skalli.core.extension;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.project.ProjectService;

class NoSubprojectsValidator implements PropertyValidator, Issuer {
    @Override
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        Boolean deleted = (Boolean)value;
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        if (deleted && projectService.getSubProjects(entity).size() > 0) {
            issues.add(new Issue(Severity.FATAL, getClass(), entity, Project.class, Project.PROPERTY_DELETED,
                "Projects with subprojects cannot be deleted - first delete all subprojects " +
                "or assign them to other projects. Then try again."));
        }
        return issues;
    }
}