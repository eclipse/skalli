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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.skalli.view.LoginUtils;
import org.eclipse.skalli.view.internal.application.ProjectApplication;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/**
 * Extends the Vaadin application servlet to prevent class loading issue in OSGi
 * container: Overrides {@link AbstractApplicationServlet#getNewApplication(HttpServletRequest)}
 * to instantiate and return a {@link ProjectApplication}.
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
}
