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
package org.eclipse.skalli.api.rest.internal;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.services.user.LoginUtils;
import org.restlet.ext.servlet.ServerServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestletServlet extends ServerServlet {
    private static final long serialVersionUID = -7953560055729006206L;
    private static final Logger LOG = LoggerFactory.getLogger(RestletServlet.class);

    @Override
    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> ret = null;

        // Try restlet classloader first
        if (ret == null) {
            try {
                ret = super.loadClass(className);
            } catch (ClassNotFoundException e) {
                // Ignore, because that's the whole point here...
                LOG.debug(MessageFormat.format("Class {0} not found in current bundle", className)); //$NON-NLS-1$
            }
        }

        // Next, try the current context classloader
        if (ret == null) {
            try {
                ret = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                // Ignore, because that's the whole point here...
                LOG.debug(MessageFormat.format("Class {0} not found by context class loader", className)); //$NON-NLS-1$
            }
        }

        if (ret == null) {
            throw new ClassNotFoundException("Class not found: " + className); //$NON-NLS-1$
        }

        return ret;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoginUtils loginUtils = new LoginUtils(request);
        String loggedInUser = loginUtils.getLoggedInUserId();

        if (StringUtils.isNotBlank(loggedInUser)) {
            Statistics.getDefault().trackReferer(loggedInUser);
        } else {
            // anonymous user => enforcing a "Referer" header
            String referer = request.getHeader("Referer"); //$NON-NLS-1$
            if (StringUtils.isBlank(referer)) {
                // referer not defined in request header, try to fetch it from url
                referer = request.getParameter("referer"); //$NON-NLS-1$
            }
            Statistics.getDefault().trackReferer(referer);
        }
        super.service(request, response);
    }

}
