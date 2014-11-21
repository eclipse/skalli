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
package org.eclipse.skalli.view.internal.application;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.view.Consts;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Window;

public class ProjectNavigator implements org.eclipse.skalli.view.ext.Navigator {

    private final Window window;

    public ProjectNavigator(Window window) {
        this.window = window;
    }

    @Override
    public void navigateProjectView(Project project) {
        //    Application application = window.getApplication();
        //    if (!project.getProjectId().equals(window.getName()) && application.getWindow(project.getProjectId()) != null) {
        // TODO we need to remove the current window from application in case the project id was modified.
        // But if we do so, navigation to a external resource won't work
        //    }

        window.open(new ExternalResource(Consts.URL_PROJECTS + "/" + project.getProjectId())); //$NON-NLS-1$
    }

    @Override
    public void navigateProjectEditView(Project project) {
        throw new RuntimeException("navigate Project edit not implemented");
    }

    @Override
    public void navigateProjectNewView() {
        window.open(new ExternalResource(Consts.URL_CREATEPROJECT));
    }

    @Override
    public void navigateBrowseView() {
        window.open(new ExternalResource(Consts.URL_PROJECTS));
    }

    @Override
    public void navigateWelcomeView() {
        window.open(new ExternalResource(Consts.URL_WELCOME));
    }

    @Override
    public void navigateSearchResultView(String query) {
        window.open(new ExternalResource(Consts.URL_PROJECTS_QUERY + query));
    }

    @Override
    public void showFeedbackWindow() {
        throw new RuntimeException("navigate Feedback not implemented");
    }

    @Override
    public void navigateUserView(User user) {
        window.open(new ExternalResource(Consts.URL_PROJECTS_USER + user.getUserId()));
    }

    @Override
    public void navigateLoginUserView() {
        window.open(new ExternalResource(Consts.URL_MYPROJECTS));
    }

    @Override
    public void navigateTagView(String tag) {
        window.open(new ExternalResource(Consts.URL_PROJECTS_TAG + tag));
    }

    @Override
    public void navigateAllTagView() {
        window.open(new ExternalResource(Consts.URL_TAGCLOUD));
    }

}
