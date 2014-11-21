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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.view.internal.ViewBundleUtil;

public class ResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 9116962436984160014L;
    private static final String RESOURCE_PATH_IN_VAADIN_BUNDLE = "/VAADIN"; // Vaadin expects resources to reside in a VAADIN folder... //$NON-NLS-1$

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        String resourcePath = RESOURCE_PATH_IN_VAADIN_BUNDLE + path;

        List<URL> urls = ViewBundleUtil.findThemeResources(FilenameUtils.getPath(resourcePath),
                FilenameUtils.getName(resourcePath), false, FilterMode.SHORT_CIRCUIT);
        if (urls.size() == 0) {
            // requested resource not found in any view related bundles
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // requested resource found in at least one bundle.
            // If there are multiple matches, pick the first one.
            InputStream in = urls.get(0).openStream();
            try {
                OutputStream out = resp.getOutputStream();
                IOUtils.copy(in, out);
            } finally {
                if (in != null) {
                    IOUtils.closeQuietly(in);
                }
            }
        }
    }

}
