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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ProjectNature;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.view.component.SubprojectComparator;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class SubprojectsInfoBox extends InfoBox implements ProjectInfoBox {

    private static final int DELTA_INDENT = 20;

    private static final String STYLE_SUBPROJECTS_INFOBOX = "infobox-subprojects";  //$NON-NLS-1$

    private ProjectService projectService;
    private ProjectTemplateService templateService;

    protected void bindProjectService(ProjectService srvc) {
        this.projectService = srvc;
    }

    protected void unbindProjectService(ProjectService srvc) {
        this.projectService = null;
    }

    protected void bindProjectTemplateService(ProjectTemplateService srvc) {
        this.templateService = srvc;
    }

    protected void unbindProjectTemplateService(ProjectTemplateService srvc) {
        this.templateService = null;
    }

    @Override
    public String getIconPath() {
        return "res/icons/subprojects.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Project Hierarchy";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_SUBPROJECTS_INFOBOX);
        layout.setSizeFull();

        UUID uuid = project.getUuid();

        List<Project> parents = projectService.getParentChain(uuid);
        List<Project> subprojects = projectService.getSubProjects(uuid);
        Collections.sort(subprojects, new SubprojectComparator(templateService));
        int indent = 0;
        StringBuilder sb = new StringBuilder();

        // render the parents of the project as links in reverse order and
        // with increasing indentation; finally render the project
        // itself (just as emphasized text, no link)
        for (int i = parents.size() - 1; i >= 0; --i) {
            renderProject(sb, parents.get(i), templateService, indent, i > 0);
            indent += DELTA_INDENT;
        }

        // render the subprojects as links in alphabetical order
        // and with same indentation
        for (Project subproject : subprojects) {
            renderProject(sb, subproject, templateService, indent, true);
        }

        Label content = new Label(sb.toString(), Label.CONTENT_XHTML);
        content.setSizeUndefined();
        layout.addComponent(content);
        return layout;
    }

    private void renderProject(StringBuilder sb, Project project, ProjectTemplateService templateService, int indent,
            boolean asLink) {
        sb.append("<div"); //$NON-NLS-1$
        if (indent > 0) {
            sb.append(" style=\"margin-left: ").append(indent).append("px\">"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            sb.append(">"); //$NON-NLS-1$
        }
        ProjectTemplate template = templateService.getProjectTemplateById(project.getProjectTemplateId());
        ProjectNature nature = template != null ? template.getProjectNature() : null;
        if (nature != null) {
            sb.append("<img class=\"natureicon\" src=\""); //$NON-NLS-1$
            if (ProjectNature.PROJECT.equals(nature)) {
                sb.append("/VAADIN/themes/simple/icons/nature/project16x16.png"); //$NON-NLS-1$
                sb.append("\" title=\"").append("Project of People").append("\" />"); //$NON-NLS-1$ //$NON-NLS-3$
            } else if (ProjectNature.COMPONENT.equals(nature)) {
                sb.append("/VAADIN/themes/simple/icons/nature/component16x16.png"); //$NON-NLS-1$
                sb.append("\" title=\"").append("Component").append("\" />"); //$NON-NLS-1$ //$NON-NLS-3$
            }
        }
        if (asLink) {
            sb.append("<a class=\"link\" href=\"/projects/"); //$NON-NLS-1$
            sb.append(project.getProjectId());
            sb.append("\" target=\"_top\">"); //$NON-NLS-1$
            sb.append(project.getName());
            sb.append("</a>"); //$NON-NLS-1$
        }
        else {
            sb.append("<em>").append(project.getName()).append("</em>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sb.append("</div>"); //$NON-NLS-1$
    }

    @Override
    public float getPositionWeight() {
        return 1.02f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        List<Project> subprojects = projectService.getSubProjects(project.getUuid());
        return project.getParentProject() != null || subprojects.size() > 0;
    }
}
