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

import static org.easymock.EasyMock.*;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.skalli.testutil.BundleManager;

@SuppressWarnings("nls")
public class StaticContentServletTest {

  @Before
  public void setup() throws Exception {
    new BundleManager(this.getClass()).startProjectPortalBundles();
    sb = new StringBuilder();
  }

  private boolean written = false;
  private StringBuilder sb;

  @Test
  public void testDoGetProjectSchema() throws Exception {
    HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
    HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
    Object[] mocks = new Object[] {mockRequest, mockResponse};
    StaticContentServlet servlet = new StaticContentServlet();
    reset(mocks);

    mockRequest.getRequestURI();
    expectLastCall().andReturn("/schemas/project.xsd");

    mockResponse.setContentType(eq("text/xml"));
    expectLastCall();

    mockResponse.getOutputStream();
    expectLastCall().andReturn(new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        written = true;
        sb.append((char)b);
      }
    });

    replay(mocks);

    servlet.doGet(mockRequest, mockResponse);
    Assert.assertTrue(written);
    Assert.assertTrue(sb.toString().startsWith(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" +
        "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "xmlns=\"http://xml.sap.com/2010/08/ProjectPortal/API\" " +
        "attributeFormDefault=\"unqualified\" " +
        "elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://xml.sap.com/2010/08/ProjectPortal/API\" " +
        "version=\"1.2\">"));

    verify(mocks);
  }

  @Test
  public void testDoGetExtensionSchema() throws Exception {
    HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
    HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
    Object[] mocks = new Object[] {mockRequest, mockResponse};
    StaticContentServlet servlet = new StaticContentServlet();
    reset(mocks);

    mockRequest.getRequestURI();
    expectLastCall().andReturn("/schemas/extension-devinf.xsd");

    mockResponse.setContentType(eq("text/xml"));
    expectLastCall();

    mockResponse.getOutputStream();
    expectLastCall().andReturn(new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        written = true;
        sb.append((char)b);
      }
    });

    replay(mocks);

    servlet.doGet(mockRequest, mockResponse);
    Assert.assertTrue(written);
    Assert.assertTrue(sb.toString().startsWith(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" +
        "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "xmlns=\"http://xml.sap.com/2010/08/ProjectPortal/API/Extension-DevInf\" " +
        "attributeFormDefault=\"unqualified\" " +
        "elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://xml.sap.com/2010/08/ProjectPortal/API/Extension-DevInf\" " +
        "version=\"1.0\">"));

    verify(mocks);
  }

  @Test
  public void testDoGetUnknown() throws Exception {
    HttpServletRequest mockRequest = createMock(HttpServletRequest.class);
    HttpServletResponse mockResponse = createMock(HttpServletResponse.class);
    Object[] mocks = new Object[] {mockRequest, mockResponse};
    StaticContentServlet servlet = new StaticContentServlet();
    reset(mocks);

    mockRequest.getRequestURI();
    expectLastCall().andReturn("/schemas/gibtsnich");

    mockResponse.sendError(eq(404));
    expectLastCall();

    replay(mocks);

    servlet.doGet(mockRequest, mockResponse);
    verify(mocks);
  }
}

