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
import java.text.MessageFormat;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.eclipse.skalli.commons.URLUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.services.destination.Destinations;
import org.eclipse.skalli.testutil.XMLDiffUtil;
import org.junit.Test;
import org.w3c.dom.Document;

@SuppressWarnings("nls")
public class CompareAPIVersionsTest {

    private static final String SKALLI_ITEST_HOST = System.getProperty("skalli.itest.host");

    @Test
    public void testCompareAllProjects() throws Exception {
        if (SKALLI_ITEST_HOST != null) {
            HttpClient client = Destinations.getClient(URLUtils.stringToURL(SKALLI_ITEST_HOST));
            int start = 0;
            int count = 10;
            while (count > 0) {
                HttpGet reqV1 = new HttpGet(getAllProjectSliceRestPath("v1", start, count));
                HttpResponse respV1 = client.execute(reqV1);
                assertEquals(200, respV1.getStatusLine().getStatusCode());
                HttpGet reqV2 = new HttpGet(getAllProjectSliceRestPath("v2", start, count));
                HttpResponse respV2 = client.execute(reqV2);
                assertEquals(200, respV2.getStatusLine().getStatusCode());
                Document expected = XMLUtils.documentFromString(getContent(respV1));
                Document actual = XMLUtils.documentFromString(getContent(respV2));
                ProjectsV1V2Diff projectsDiff = new ProjectsV1V2Diff();
                XMLDiffUtil.assertEquals(expected, actual, projectsDiff);
                start += count;
                count = NumberUtils.toInt(expected.getDocumentElement().getAttribute("count"), 0);
            }
        }
    }

    private String getAllProjectSliceRestPath(String apiVersion, int start, int count) {
        return URLUtils.concat(SKALLI_ITEST_HOST, MessageFormat.format(
                "/api/projects?orderBy=uuid&rest={0}&start={1}&count={2}",
                apiVersion, Integer.toString(start), Integer.toString(count)));
    }

    private String getContent(HttpResponse resp) throws IOException {
        HttpEntity responseEntity = resp.getEntity();
        if (responseEntity != null) {
            byte[] bytes = EntityUtils.toByteArray(responseEntity);
            return new String(bytes, "UTF-8");
        }
        return null;
    }

}