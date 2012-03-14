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
package org.eclipse.skalli.view.internal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.user.LoginUtils;
import org.eclipse.skalli.view.internal.application.ProjectApplication;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/**
 * Extends the Vaadin application servlet to prevent class loading issue in OSGi
 * container, use project portal application by default
 */
@SuppressWarnings("serial")
public class ProjectServlet extends AbstractApplicationServlet {

    @Override
    protected Class<? extends Application> getApplicationClass() {
        return ProjectApplication.class;
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request) throws ServletException {
        return new ProjectApplication(new LoginUtils(request).getLoggedInUser());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PermitService permitService = Services.getRequiredService(PermitService.class);
        permitService.login((HttpServletRequest) request, null);

        String browser = request.getHeader("User-Agent"); //$NON-NLS-1$
        LoginUtils loginUtil = new LoginUtils(request);
        User loggedInUser = loginUtil.getLoggedInUser();
        Statistics.getDefault().trackBrowser(loginUtil.getLoggedInUserId(), browser,
                (loggedInUser != null) ? loggedInUser.getDepartment() : null,
                (loggedInUser != null) ? loggedInUser.getLocation() : null);

        super.service(request, response);
    }

}
