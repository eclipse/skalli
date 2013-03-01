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

import java.util.Comparator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.ServiceFilter;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.project.ProjectUtils;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.services.user.UserUtils;
import org.eclipse.skalli.view.component.InformationBox;
import org.eclipse.skalli.view.ext.ExtensionStreamSource;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.IconProvider;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.internal.application.ProjectApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window.Notification;

public class ProjectDetailsPanel extends CssLayout implements ProjectPanel {

    private static final long serialVersionUID = -2756706292280384313L;

    private static final Logger LOG = LoggerFactory.getLogger(ProjectDetailsPanel.class);

    private static final String STYLE_EAST_COLUMN = "east-column"; //$NON-NLS-1$
    private static final String STYLE_WEST_COLUMN = "west-column"; //$NON-NLS-1$

    private final ProjectApplication application;
    private final Navigator navigator;
    private final Project project;

    private final CssLayout leftLayout;
    private final CssLayout rightLayout;

    public ProjectDetailsPanel(ProjectApplication application, Navigator navigator, Project project) {
        super();

        this.application = application;
        this.project = project;
        this.navigator = navigator;

        this.setSizeFull();

        leftLayout = new CssLayout();
        leftLayout.addStyleName(STYLE_EAST_COLUMN);
        leftLayout.setWidth("50%"); //$NON-NLS-1$
        addComponent(leftLayout);

        rightLayout = new CssLayout();
        rightLayout.addStyleName(STYLE_WEST_COLUMN);
        rightLayout.setWidth("50%"); //$NON-NLS-1$
        addComponent(rightLayout);

        renderContent();
    }

    @Override
    public Project getProject() {
        return project;
    }

    private void renderContent() {
        int leftCounter = 0;
        int rightCounter = 0;

        Set<InfoBox> infoBoxes = getOrderedVisibleInfoBoxList();
        for (InfoBox projectInfoBox : infoBoxes) {
            ExtensionUtil context = new ProjectViewContextImpl(projectInfoBox.getClass());
            Component content;
            try {
                content = projectInfoBox.getContent(project, context);
            } catch (RuntimeException e) {
                LOG.error("Can't display project info box '" + projectInfoBox.getCaption() + "'", e);
                content = getInternalErrorContent();
            }
            if (content != null) {
                InformationBox infoBox = InformationBox.getInformationBox("&nbsp;" + projectInfoBox.getCaption()); //$NON-NLS-1$
                infoBox.getContent().addComponent(content);

                String icon = projectInfoBox.getIconPath();
                if (StringUtils.isNotBlank(icon)) {
                    infoBox.setIcon(new StreamResource(new ExtensionStreamSource(projectInfoBox.getClass(), icon),
                            FilenameUtils.getName(icon), application));
                }

                if (projectInfoBox.getPreferredColumn() == InfoBox.COLUMN_WEST) {
                    leftLayout.addComponent(infoBox);
                    leftCounter++;
                } else if (projectInfoBox.getPreferredColumn() == InfoBox.COLUMN_EAST) {
                    rightLayout.addComponent(infoBox);
                    rightCounter++;
                } else {
                    if (leftCounter <= rightCounter) {
                        leftLayout.addComponent(infoBox);
                        leftCounter++;
                    } else {
                        rightLayout.addComponent(infoBox);
                        rightCounter++;
                    }
                }
            }
        }
    }

    private Component getInternalErrorContent() {
        Layout errorContent = new CssLayout();
        errorContent.setSizeFull();
        Label label = new Label("Internal Error: The extension content cannot be displayed. " +
                "An internal error occurred. Please notify the administrator.", Label.CONTENT_XHTML);
        label.addStyleName("infobox-internalerror");
        errorContent.addComponent(label);
        return errorContent;
    }

    private Set<InfoBox> getOrderedVisibleInfoBoxList() {
        Set<InfoBox> set = Services.getServices(InfoBox.class,
                new ServiceFilter<InfoBox>() {
                    @Override
                    public boolean accept(InfoBox infoBox) {
                        return infoBox.isVisible(project, application.getLoggedInUser());
                    }
                },
                new Comparator<InfoBox>() {
                    @Override
                    public int compare(InfoBox o1, InfoBox o2) {
                        if (o1.getPositionWeight() != o2.getPositionWeight()) {
                            return new Float(o1.getPositionWeight()).compareTo(o2.getPositionWeight());
                        } else {
                            // in case the position weight is equal, compare by class name to prevent that
                            // one of both info boxes is sorted out of the result set
                            return (o1.getClass().toString().compareTo(o2.getClass().toString()));
                        }
                    }
                });
        return set;
    }

    @Override
    protected String getCss(Component c) {
        if (c instanceof CssLayout) {
            return "float: left"; //$NON-NLS-1$
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private class ProjectViewContextImpl implements ExtensionUtil {

        private Class<? extends IconProvider> iconProvider;

        public ProjectViewContextImpl(Class<? extends IconProvider> iconProvider) {
            this.iconProvider = iconProvider;
        }

        @Override
        public void persist(Project project) {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            try {
                projectService.persist(project, getLoggedInUser().getUserId());
            } catch (ValidationException e) {
                getWindow().showNotification("Project could not be saved",
                        Issue.asHTMLList(null, e.getIssues()),
                        Notification.TYPE_ERROR_MESSAGE);
            }
        }

        @Override
        public boolean isUserAdmin() {
            return GroupUtils.isAdministrator(getLoggedInUser());
        }

        @Override
        public boolean isUserProjectAdmin(Project project) {
            return ProjectUtils.isProjectAdmin(getLoggedInUser(), project)
                    || GroupUtils.isAdministrator(getLoggedInUser());
        }

        @Override
        public User getLoggedInUser() {
            return UserUtils.getUser(application.getLoggedInUser());
        }

        @Override
        public Resource getBundleResource(String path) {
            return new StreamResource(new ExtensionStreamSource(iconProvider, path),
                    FilenameUtils.getName(path), application);
        }

        @Override
        public Navigator getNavigator() {
            return navigator;
        }

        @Override
        public ProjectTemplate getProjectTemplate() {
            ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
            return templateService.getProjectTemplateById(project.getProjectTemplateId());
        }
    }
}
