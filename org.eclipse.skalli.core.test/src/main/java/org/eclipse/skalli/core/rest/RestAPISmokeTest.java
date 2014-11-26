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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Verifies that the REST API "is there" and the contexts are bound properly.
 * Uses an embedded Jetty server running {@link RestletServlet}.
 */
@SuppressWarnings("nls")
public class RestAPISmokeTest {

    private static EmbeddedRestServer server;
    private static String basePath;

    @BeforeClass
    public static void beforeClass() throws Exception {
        BundleManager.startBundles(RestAPISmokeTest.class);
        server = new EmbeddedRestServer();
        basePath = server.getWebLocator() + "/api";
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testGetProjects() throws Exception {
        HttpClient client = getClient();
        HttpGet req = new HttpGet(basePath + "/projects");
        HttpResponse resp = client.execute(req);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String body = getContent(resp);
        assertTrue(body.contains("<projects"));
        assertTrue(body.endsWith("</projects>"));
    }

    @Test
    public void testGetProjectsWithQuery() throws Exception {
        HttpClient client = getClient();
        HttpGet req = new HttpGet(basePath + "/projects?query=skalli");
        HttpResponse resp = client.execute(req);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String body = getContent(resp);
        assertTrue(body.contains("<projects"));
        assertTrue(body.endsWith("</projects>"));
        assertEquals(body.indexOf("<project>"), body.lastIndexOf("<project>"));
    }

    @Test
    public void testGetProject() throws Exception {
        HttpClient client = getClient();
        HttpGet req = new HttpGet(basePath + "/projects/5856b08a-0f87-4d91-b007-ac367ced247a");
        HttpResponse resp = client.execute(req);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String body = getContent(resp);
        assertTrue(body.contains("<project"));
        assertTrue(body.endsWith("</project>"));
    }

    private static HttpClient getClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 300000);
        HttpConnectionParams.setTcpNoDelay(params, true);
        DefaultHttpClient client = new DefaultHttpClient(params);
        return client;
    }

    private static String getContent(HttpResponse resp) throws IOException {
        HttpEntity responseEntity = resp.getEntity();
        if (responseEntity != null) {
            byte[] bytes = EntityUtils.toByteArray(responseEntity);
            return new String(bytes, "UTF-8");
        }
        return null;
    }
}
