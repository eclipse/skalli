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
package org.eclipse.skalli.core.internal.destination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.skalli.services.destination.DestinationService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.HttpServerMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class DestinationServiceImplTest {

    private static HttpServerMock mmus;
    private static final String TEST_CONTENT = "BODY";

    protected DestinationService destinationService;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        BundleManager.startBundles();
        mmus = new HttpServerMock();
        mmus.start();
    }

    @AfterClass
    public static void tearDownOnce() throws Exception {
        mmus.stop();
    }

    @Before
    public void setup() throws Exception {
        destinationService = new DestinationServiceImpl();
    }

    @Test
    public void testGetContent() throws Exception {
        mmus.addContent("testGetContent", TEST_CONTENT);
        assertGetRequest("http", 200, "testGetContent", TEST_CONTENT);
    }

    private void assertGetRequest(String protocol, int responseCode, String contentId, String content) throws Exception {
        URL url = new URL(protocol + "://" + mmus.getHost() + ":" + mmus.getPort() + "/" + contentId + "/"
                + responseCode);
        HttpClient client = destinationService.getClient(url);
        assertNotNull(client);

        HttpGet method = new HttpGet(url.toExternalForm());
        HttpResponse response = client.execute(method);
        assertEquals(200, response.getStatusLine().getStatusCode());
        if (content != null) {
            String contents = IOUtils.toString(response.getEntity().getContent());
            assertEquals(content, contents);
        }
    }
}
