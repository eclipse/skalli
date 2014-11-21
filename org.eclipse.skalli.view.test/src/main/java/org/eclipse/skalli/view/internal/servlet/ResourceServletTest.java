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

import static org.easymock.EasyMock.*;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class ResourceServletTest {

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();
    }

    boolean written = false;

    @Test
    public void testDoGet() throws Exception {
        HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
        HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
        Object[] mocks = new Object[] { mockRequest, mockResponse };
        ResourceServlet servlet = new ResourceServlet();
        reset(mocks);

        mockRequest.getPathInfo();
        expectLastCall().andReturn("/widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet/hosted.html");

        mockResponse.getOutputStream();
        expectLastCall().andReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                written = true;
            }
        });

        replay(mocks);

        servlet.doGet(mockRequest, mockResponse);
        Assert.assertTrue(written);

        verify(mocks);
    }

    @Test
    public void testDoGet_notExisting() throws Exception {
        HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
        HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
        Object[] mocks = new Object[] { mockRequest, mockResponse };
        ResourceServlet servlet = new ResourceServlet();

        reset(mocks);

        mockRequest.getPathInfo();
        expectLastCall().andReturn("/widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet/volllustig.gibtsnich");

        mockResponse.sendError(eq(404));
        expectLastCall();

        replay(mocks);

        servlet.doGet(mockRequest, mockResponse);

        verify(mocks);
    }

}
