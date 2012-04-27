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

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * implements the project portal page that renders information about a
 * particular project
 */
@SuppressWarnings("serial")
public class ProjectWindow extends Window {

    private static final String STYLE_PROJECT = "projectarea"; //$NON-NLS-1$
    private static final String STYLE_WINDOW = "projectview"; //$NON-NLS-1$

    private final ProjectApplication application;
    private final HorizontalLayout mainContainer = new HorizontalLayout();
    private final Navigator navigator = new ProjectNavigator(this);

    private Project project;
    private boolean repaint;

    public ProjectWindow(ProjectApplication application, Project project) {
        this.addListener(new CloseListener() {
            @Override
            public void windowClose(CloseEvent e) {
                // set repaint=true to repaint this window when we come
                // back (if necessary, checked in getContent())
                repaint = true;
            }
        });

        this.application = application;
        this.project = project;

        addStyleName(STYLE_WINDOW);
        doLayout();
    }

    @Override
    public ComponentContainer getContent() {
        if (repaint == true && project != null && isProjectView()) {
            // project could have been changed in the meantime, refresh project instance
            project = Services.getRequiredService(ProjectService.class).getByUUID(project.getUuid());
            doLayout();
            repaint = false;
        }
        return super.getContent();
    }

    public Project getProject() {
        return project;
    }

    public void refreshProject(Project project) {
        this.project = project;
        repaint = true;
    }

    void setMainContent(Component component) {
        mainContainer.removeAllComponents();
        mainContainer.addComponent(component);
    }

    @SuppressWarnings("deprecation")
    private void doLayout() {
        Panel mainPanel = new Panel();
        mainPanel.addStyleName(Panel.STYLE_LIGHT);

        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        vlayout.setMargin(false);
        vlayout.addStyleName(STYLE_WINDOW);

        mainContainer.setSizeFull();
        mainContainer.addStyleName(STYLE_PROJECT);
        mainContainer.setMargin(false);

        if (project != null) {
            setMainContent(getProjectView());
        } else {
            setMainContent(getTemplateSelectView());
        }

        vlayout.addComponent(mainContainer);

        mainPanel.setContent(vlayout);
        setContent(mainPanel);

        setSizeFull();
    }

    private boolean isProjectView() {
        return mainContainer.getComponent(0) instanceof ProjectViewPanel;
    }

    private boolean isProjectEditView() {
        return mainContainer.getComponent(0) instanceof ProjectEditPanel;
    }

    private Component getProjectView() {
        return new ProjectViewPanel(application, navigator, project);
    }

    private Component getProjectEditView() {
        return new ProjectEditPanel(application, navigator, project, ProjectEditMode.EDIT_PROJECT);
    }

    private Component getTemplateSelectView() {
        return new TemplateSelectPanel(application, this, navigator);
    }

    public void handleRelativeURI(String relativeUri) {
        if ("edit".equals(relativeUri)) { //$NON-NLS-1$
            if (!isProjectEditView()) {
                setMainContent(getProjectEditView());
            }
        } else {
            if (project == null) {
                return;
            } else if (isProjectEditView()) {
                setMainContent(getProjectView());
            }
        }
    }
}
