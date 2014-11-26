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
package org.eclipse.skalli.core.rest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Wrapper for an embedded Jetty server serving the Skalli REST API.
 * <p>
 * The returned instance maps {@link RestletServlet} to the context path <tt>/api</tt>
 * and redirects all requests to {@link RestApplication} and the org.restlet engine.
 * <p>
 * The test should start the Jetty instance with {@link #start()}, e.g. in a
 * {@literal @BeforeClass} method, and ensure that it is stopped properly after
 * test execution, e.g. with an {@literal @AfterClass} method.
 * <p>
 * The server port can be configured with the system property <tt>PORT1</tt> (default: 8182).
 */
public class EmbeddedRestServer {

    private static final int DEFAULT_PORT = 8182;

    private Server server;
    private String webLocator;
    private int port;

    public EmbeddedRestServer() {
        port = DEFAULT_PORT;
        String portParam = System.getProperty("PORT1"); //$NON-NLS-1$
        if (!StringUtils.isBlank(portParam)) {
            port = Integer.parseInt(portParam);
        }
        webLocator = "http://localhost:" + Integer.toString(port); //$NON-NLS-1$

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/api"); //$NON-NLS-1$
        ServletHandler handler = new ServletHandler();
        ServletHolder sh = new ServletHolder(RestletServlet.class);
        sh.setInitParameter("org.restlet.application", //$NON-NLS-1$
                "org.eclipse.skalli.core.rest.RestApplication"); //$NON-NLS-1$
        context.addServlet(sh, "/*"); //$NON-NLS-1$
        context.setHandler(handler);
        server = new Server(port);
        server.setHandler(context);
    }

    public int getPort() {
        return port;
    }

    public String getWebLocator() {
        return webLocator;
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
