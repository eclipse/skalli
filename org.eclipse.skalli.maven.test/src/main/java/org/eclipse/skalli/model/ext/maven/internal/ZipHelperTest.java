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
package org.eclipse.skalli.model.ext.maven.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ZipHelperTest {

    private static final String RES_EXPECTED_PATH = "/res/zip/expected/";
    private static final String RES_ZIP_PATH = "/res/zip/zips/";

    private String getResourceAsString(String expectedResourceName) throws IOException {
        return IOUtils.toString(this.getClass().getResource(expectedResourceName).openStream());
    }

    @Test
    public void testGetEntryAsInputStream_rootPom() throws Exception {
        URL url = this.getClass().getResource(RES_ZIP_PATH + "org.eclipse.skalli-pom.xml-HEAD.zip");
        InputStream openUrlStream = url.openStream();
        try {
            ZipInputStream zip = new ZipInputStream(openUrlStream);
            String fileName = "pom.xml";
            InputStream inputStream = ZipHelper.getEntry(zip, fileName);
            String result = IOUtils.toString(inputStream);
            assertThat(result, is(getResourceAsString(RES_EXPECTED_PATH + "org.eclipse.skalli-pom.xml")));
        } finally {
            openUrlStream.close();
        }
    }

    @Test
    public void testGetEntryAsInputStream_nonRrootPom() throws Exception {
        URL url = this.getClass().getResource(
                RES_ZIP_PATH + "org.eclipse.skalli-org.eclipse.skalli.maven.test_pom.xml-HEAD.zip");
        ZipInputStream zip = new ZipInputStream(url.openStream());
        String fileName = "org.eclipse.skalli.maven.test/pom.xml";
        InputStream inputStream = ZipHelper.getEntry(zip, fileName);
        String result = IOUtils.toString(inputStream);
        assertThat(result, is(getResourceAsString(RES_EXPECTED_PATH + "org.eclipse.skalli.maven.test_pom.xml")));
    }
}
