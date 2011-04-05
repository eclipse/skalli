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

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.data.Protocol;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.eclipse.skalli.testutil.BundleManager;

/**
 * Verifies that the REST API "is there" and the contexts are bound properly.
 *
 * @author d049863
 *
 */
@SuppressWarnings("nls")
public class TestRestAPISmoke {

  private static Component component;
  private static int port = 8182;

  @BeforeClass
  public static void beforeClass() throws Exception {
    new BundleManager(TestRestAPISmoke.class).startProjectPortalBundles();

    String portParam = System.getProperty("PORT1");
    if (!StringUtils.isBlank(portParam)) {
      port = Integer.parseInt(portParam);
    }

    component = new Component();
    component.getServers().add(Protocol.HTTP, port);
    component.getDefaultHost().attach(new RestApplication());
    component.start();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    component.stop();
  }

  /**
   * tests GET /projects
   * @throws Exception
   */
  @Test
  public void testGetProjects() throws Exception {
    WebConversation wc = new WebConversation();
    WebRequest     req = new GetMethodWebRequest("http://localhost:" + port + "/projects");
    WebResponse   resp = wc.getResponse(req);
    Assert.assertEquals(200, resp.getResponseCode());
    Assert.assertTrue(resp.getText().contains("<projects"));
    Assert.assertTrue(resp.getText().endsWith("</projects>"));
  }

  /**
   * tests GET /projects?query=portal
   * @throws Exception
   */
  @Test
  public void testGetProjectsWithQuery() throws Exception {
    WebConversation wc = new WebConversation();
    WebRequest     req = new GetMethodWebRequest("http://localhost:" + port + "/projects?query=portal");
    WebResponse   resp = wc.getResponse(req);
    Assert.assertEquals(200, resp.getResponseCode());
    Assert.assertTrue(resp.getText().contains("<projects"));
    Assert.assertTrue(resp.getText().endsWith("</projects>"));
    Assert.assertEquals(resp.getText().indexOf("<project>"), resp.getText().lastIndexOf("<project>"));
  }

  /**
   * tests GET /projects/&lt;id&gt;
   * @throws Exception
   */
  @Test
  public void testGetProject() throws Exception {
    WebConversation wc = new WebConversation();
    WebRequest     req = new GetMethodWebRequest("http://localhost:" + port + "/projects/00c5cdb4-fb0f-4f11-913c-44a7f24531bd");
    WebResponse   resp = wc.getResponse(req);
    Assert.assertEquals(200, resp.getResponseCode());
    Assert.assertTrue(resp.getText().contains("<project"));
    Assert.assertTrue(resp.getText().endsWith("</project>"));
  }


}

