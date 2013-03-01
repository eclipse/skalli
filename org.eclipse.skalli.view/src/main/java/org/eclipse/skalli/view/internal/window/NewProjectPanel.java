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

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.view.component.RadioSelect;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.ext.ProjectEditMode;
import org.eclipse.skalli.view.internal.application.ProjectApplication;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class NewProjectPanel extends Panel implements ProjectPanel {

    private static final long serialVersionUID = 4432418022051563046L;

    private static final String STYLE_TEMPLATE_SELECT = "tplsel"; //$NON-NLS-1$
    private static final String STYLE_TEMPLATE_SELECT_BUTTONS = "tplsel-buttons"; //$NON-NLS-1$

    private static final String DEBUG_ID_CONTENT = "templateSelectPanelContent"; //$NON-NLS-1$

    private final ThemeResource ICON_BUTTON_OK = new ThemeResource("icons/button/ok.png"); //$NON-NLS-1$
    private final ThemeResource ICON_BUTTON_CANCEL = new ThemeResource("icons/button/cancel.png"); //$NON-NLS-1$

    private final ProjectApplication application;
    private final Navigator navigator;
    private final ProjectWindow window;
    private RadioSelect select;

    private final Set<ProjectTemplate> projectTemplates;

    public NewProjectPanel(ProjectApplication application, ProjectWindow window, Navigator navigator) {
        this.application = application;
        this.navigator = navigator;
        this.window = window;

        ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
        projectTemplates = templateService.getAllTemplates();

        setSizeFull();
        setStyleName(STYLE_TEMPLATE_SELECT);
        renderContent((VerticalLayout) getContent());
    }

    @Override
    public Project getProject() {
        return null;
    }

    /**
     * Renders the content of the panel.
     */
    private void renderContent(VerticalLayout content) {

        CssLayout layout = new CssLayout();
        layout.setWidth("600px"); //$NON-NLS-1$
        content.addComponent(layout);
        content.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

        Label title = new Label("<h2>" + "Available Project Templates" + "</h2>", Label.CONTENT_XHTML); //$NON-NLS-1$//$NON-NLS-3$
        layout.addComponent(title);

        TreeSet<RadioSelect.Entry> entries = new TreeSet<RadioSelect.Entry>();
        for (ProjectTemplate projectTemplate : projectTemplates) {
            entries.add(new RadioSelect.Entry(projectTemplate.getId(),
                    projectTemplate.getDisplayName(), projectTemplate.getDescription(), projectTemplate.getRank()));
        }
        select = new RadioSelect("", entries); //$NON-NLS-1$
        layout.addComponent(select);

        renderButtons(content);

        // for ui debugging
        content.setDebugId(DEBUG_ID_CONTENT);
    }

    /**
     * Renders the OK/Cancel button bar.
     */
    private void renderButtons(VerticalLayout content) {
        CssLayout buttons = new CssLayout();
        buttons.addStyleName(STYLE_TEMPLATE_SELECT_BUTTONS);

        Button okButton = new Button("Create Project");
        okButton.setIcon(ICON_BUTTON_OK);
        okButton.addListener(new OKButtonListener());
        buttons.addComponent(okButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setIcon(ICON_BUTTON_CANCEL);
        cancelButton.addListener(new CancelButtonListener());
        buttons.addComponent(cancelButton);

        content.addComponent(buttons);
        content.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
    }

    private class OKButtonListener implements Button.ClickListener {
        private static final long serialVersionUID = 6531396291087032954L;

        @Override
        public void buttonClick(ClickEvent event) {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            Project newProject = projectService.createProject(select.getSelected(), application.getLoggedInUser());

            PermitService permitService = Services.getRequiredService(PermitService.class);
            permitService.switchProject(newProject);

            ProjectEditPanel panel = new ProjectEditPanel(application, navigator, newProject, ProjectEditMode.NEW_PROJECT);
            window.renderPanel(panel);
        }
    }

    private class CancelButtonListener implements Button.ClickListener {
        private static final long serialVersionUID = 4567366927195161150L;

        @Override
        public void buttonClick(ClickEvent event) {
            navigator.navigateWelcomeView();
        }
    }
}
