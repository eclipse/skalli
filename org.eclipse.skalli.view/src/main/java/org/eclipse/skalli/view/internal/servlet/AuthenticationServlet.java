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
package org.eclipse.skalli.view.internal.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class AuthenticationServlet extends HttpServlet {

    private static final long serialVersionUID = -6622092406596651354L;

    private static final String JSP_LOGIN = "/search/login.jsp"; //$NON-NLS-1$
    private static final String PARAM_RETURNURL = "returnUrl"; //$NON-NLS-1$

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getUserPrincipal() != null) {
            // principal provided, go back to calling page
            String returnUrl = (String) request.getParameter(PARAM_RETURNURL);
            if (StringUtils.isBlank(returnUrl)) {
                returnUrl = "/"; //$NON-NLS-1$
            }
            response.sendRedirect(returnUrl);

        } else {
            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getRequestDispatcher(JSP_LOGIN);
            dispatcher.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}
