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
package org.eclipse.skalli.view.internal.application;

import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.window.ProjectWindow;

import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

/**
 * Project Portal Vaadin application. Implements the layout and screen flow of this application.
 */
@SuppressWarnings("serial")
public class ProjectApplication extends com.vaadin.Application implements HttpServletRequestListener {

    public static final String DEFAULT_THEME = "simple"; //$NON-NLS-1$
    public static final String WINDOW_TITLE = "ProjectPortal"; //$NON-NLS-1$

    private String userId;

    public ProjectApplication(User user) {
        this.userId = user != null ? StringUtils.lowerCase(user.getUserId()) : null;
        this.setUser(user);
    }

    @Override
    public void init() {
        setTheme(DEFAULT_THEME);
        setMainWindow(new Window(WINDOW_TITLE));
    }

    public void refresh(Project project) {
        if (project != null) {
            ProjectWindow projectWindow = (ProjectWindow) getWindow(project.getProjectId());
            if (projectWindow != null) {
                projectWindow.refreshProject(project);
                projectWindow.requestRepaint();
            }
        }
    }

    @Override
    public Window getWindow(String name) {
        Window window = super.getWindow(name);
        if (window == null) {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            Project project = projectService.getProjectByProjectId(name);
            if (project == null) {
                UUID uuid = UUIDUtils.asUUID(name);
                if (uuid != null) {
                    project = projectService.getByUUID(uuid);
                    if (project == null) {
                        project = projectService.getDeletedProject(uuid);
                    }
                }
            }
            // make sure that we have no "dangling" template select views
            if (project == null) {
                ArrayList<Window> allWindows = new ArrayList<Window>(getWindows());
                for (Window w : allWindows) {
                    removeWindow(w);
                }
            }
            window = new ProjectWindow(this, project); // project==null opens create dialog
            window.setName(name);
            addWindow(window);
        }
        return window;
    }

    public String getLoggedInUser() {
        return userId;
    }

    /**************************************
     * Interface HttpServletRequestListener
     ***************************************/

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        PermitService permitService = Services.getRequiredService(PermitService.class);
        String windowName = request.getParameter(Consts.ATTRIBUTE_WINDOWNAME);
        if (StringUtils.isNotBlank(windowName)) {
            String prefix = Consts.URL_VAADIN_PROJECTS + windowName;
            String requestUri = request.getRequestURI();
            if (requestUri.startsWith(prefix)) {
                String relativeUri = null;
                if (requestUri.indexOf("/edit/") > 0) { //$NON-NLS-1$
                    relativeUri = "edit";  //$NON-NLS-1$
                }
                ProjectWindow window = (ProjectWindow) getWindow(windowName);
                permitService.login(request, window.getProject());
                window.handleRelativeURI(relativeUri);
            }
        } else {
            permitService.login(request, null);
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
    }
}
