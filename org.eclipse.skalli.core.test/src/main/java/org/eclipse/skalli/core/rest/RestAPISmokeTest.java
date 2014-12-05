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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.testutil.XMLDiffUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Verifies that the REST API "is there" and the contexts are bound properly.
 * Uses an embedded Jetty server running {@link RestletServlet}.
 */
@SuppressWarnings("nls")
public class RestAPISmokeTest {

    private static final String RESOURCE_PATH = "/res/smoketest/";

    private static EmbeddedRestServer server;
    private static String basePath;
    private static int port;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new EmbeddedRestServer();
        basePath = server.getWebLocator() + "/api";
        port = server.getPort();
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testGetProjects() throws Exception {
        assertRestResponse("/projects?orderBy=projectId", "allprojects.xml");
    }

    @Test
    public void testGetProjectsWithQuery() throws Exception {
        assertRestResponse("/projects?query=skalli", "projectquery.xml");
    }

    @Test
    public void testGetProject() throws Exception {
        assertRestResponse("/projects/5856b08a-0f87-4d91-b007-ac367ced247a", "singleproject.xml");
    }

    private HttpClient getClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 300000);
        HttpConnectionParams.setTcpNoDelay(params, true);
        DefaultHttpClient client = new DefaultHttpClient(params);
        return client;
    }

    private String getContent(HttpResponse resp) throws IOException {
        HttpEntity responseEntity = resp.getEntity();
        if (responseEntity != null) {
            byte[] bytes = EntityUtils.toByteArray(responseEntity);
            return new String(bytes, "UTF-8");
        }
        return null;
    }

    private String getResource(Class<?> c, String filename) throws IOException {
        URL bundleURL = c.getResource(filename);
        if (bundleURL == null) {
            return null;
        }
        InputStream in = null;
        try {
            String content = IOUtils.toString(bundleURL.openStream(), "UTF-8");
            return StringUtils.replace(content, "${PORT1}", Integer.toString(port));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void assertRestResponse(String resourcePath, String fileName) throws Exception {
        HttpClient client = getClient();
        HttpGet req = new HttpGet(basePath + resourcePath);
        HttpResponse resp = client.execute(req);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Document expected = XMLUtils.documentFromString(getResource(RestAPISmokeTest.class, RESOURCE_PATH + fileName));
        Document actual = XMLUtils.documentFromString(getContent(resp));
        XMLDiffUtil.assertEquals(expected, actual);
    }

}
