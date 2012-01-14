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
package org.eclipse.skalli.view.component;

import java.util.Comparator;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ProjectNature;
import org.eclipse.skalli.services.template.NoSuchTemplateException;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;

public class SubprojectComparator implements Comparator<Project> {

    private ProjectTemplateService templateService;

    public SubprojectComparator(ProjectTemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public int compare(Project o1, Project o2) {
        ProjectTemplate t1 = templateService.getProjectTemplateById(o1.getProjectTemplateId());
        if (t1 == null) {
            throw new NoSuchTemplateException(o1.getUuid(), o1.getProjectTemplateId());
        }
        ProjectTemplate t2 = templateService.getProjectTemplateById(o2.getProjectTemplateId());
        if (t2 == null) {
            throw new NoSuchTemplateException(o2.getUuid(), o2.getProjectTemplateId());
        }
        if (t1.getProjectNature().equals(t2.getProjectNature())) {
            return o1.getName().compareTo(o2.getName());
        }
        if (ProjectNature.PROJECT.equals(t1.getProjectNature())) {
            return ProjectNature.COMPONENT.equals(t2.getProjectNature()) ? -1 : 1;
        }
        return ProjectNature.PROJECT.equals(t2.getProjectNature()) ? 1 : -1;
    }
}
