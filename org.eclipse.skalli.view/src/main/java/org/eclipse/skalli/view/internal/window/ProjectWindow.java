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
package org.eclipse.skalli.view.internal.window;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.ext.ProjectEditMode;
import org.eclipse.skalli.view.internal.application.ProjectApplication;
import org.eclipse.skalli.view.internal.application.ProjectNavigator;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ProjectWindow extends Window {

    private static final String STYLE_WINDOW = "projectwindow"; //$NON-NLS-1$
    private static final String STYLE_PROJECT = "projectarea"; //$NON-NLS-1$

    private final ProjectApplication application;
    private final VerticalLayout layout = new VerticalLayout();
    private final Navigator navigator = new ProjectNavigator(this);

    private ProjectPanel activePanel;

    private Project project;
    private boolean repaint;

    public ProjectWindow(ProjectApplication application, Project project) {
        this.addListener(new CloseListener() {
            @Override
            public void windowClose(CloseEvent e) {
                // repaint the window when back button was used for navigation
                repaint = true;
            }
        });

        this.application = application;
        this.project = project;
        renderPanel();
    }

    @Override
    public ComponentContainer getContent() {
        if (repaint == true && project != null && isProjectView()) {
            renderPanel();
            repaint = false;
        }
        return super.getContent();
    }

    public Project getProject() {
        if (project != null) {
            return project;
        }
        return activePanel != null? activePanel.getProject() : null;
    }

    public void setProject(Project project) {
        this.project = project;
        repaint = true;
    }

    private void renderPanel() {
        ProjectPanel panel = null;
        if (project != null) {
            // refresh project since it might have changed in the meantime
            project = getLastKnownVersion(project);
            panel = getProjectDetailsView();
        } else {
            panel = getNewProjectPanel();
        }
        renderPanel(panel);
    }

    void renderPanel(ProjectPanel panel) {
        layout.removeAllComponents();
        if (panel != null) {
            activePanel = panel;
            activePanel.addStyleName(STYLE_PROJECT);
            layout.addComponent(activePanel);
            layout.setExpandRatio(activePanel, 1.0f);
        }
        addStyleName(STYLE_WINDOW);
        setContent(layout);
        repaint = false;
    }

    private Project getLastKnownVersion(Project project) {
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project lastKnown = projectService.getByUUID(project.getUuid());
        return lastKnown != null ? lastKnown : project;
    }

    private boolean isProjectView() {
        return activePanel instanceof ProjectDetailsPanel;
    }

    private boolean isProjectEditView() {
        return activePanel instanceof ProjectEditPanel;
    }

    private ProjectPanel getProjectDetailsView() {
        return new ProjectDetailsPanel(application, navigator, project);
    }

    private ProjectPanel getProjectEditView() {
        return new ProjectEditPanel(application, navigator, project, ProjectEditMode.EDIT_PROJECT);
    }

    protected ProjectPanel getNewProjectPanel() {
        return new NewProjectPanel(application, this, navigator);
    }

    public void handleRelativeURI(String relativeUri) {
        if ("edit".equals(relativeUri)) { //$NON-NLS-1$
            if (!isProjectEditView()) {
                renderPanel(getProjectEditView());
            }
        } else {
            if (project == null) {
                return;
            } else if (isProjectEditView()) {
                renderPanel(getProjectDetailsView());
            }
        }
    }
}
