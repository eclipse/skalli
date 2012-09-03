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
package org.eclipse.skalli.view.internal.filter;

import java.io.IOException;
import java.security.AccessControlException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.skalli.view.Consts;

public class FilterUtil {

    public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    private FilterUtil() {
        // don't allow instances of this class
    }

    public static void handleException(ServletRequest request, ServletResponse response, Exception e)
            throws ServletException, IOException {
        if (!response.isCommitted()) {
            // dispatch this request to /error
            RequestDispatcher rd = request.getRequestDispatcher(Consts.URL_ERROR);
            request.setAttribute(Consts.ATTRIBUTE_EXCEPTION, e);
            rd.forward(request, response);
        }
    }

    public static void handleACException(ServletRequest request, ServletResponse response, AccessControlException e)
            throws ServletException, IOException {
        if (!response.isCommitted()) {
            // dispatch this request to /unauthorized
            RequestDispatcher rd = request.getRequestDispatcher(Consts.URL_UNAUTHORIZED);
            request.setAttribute(Consts.ATTRIBUTE_EXCEPTION, e);
            rd.forward(request, response);
        }
    }

    public static void forward(ServletRequest request, ServletResponse response, String url) throws ServletException,
            IOException {
        if (!response.isCommitted()) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(url);
            dispatcher.forward(request, response);
        }
    }
}
