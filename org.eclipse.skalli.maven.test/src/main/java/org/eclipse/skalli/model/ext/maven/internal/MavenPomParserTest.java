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

import static org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil.PARENT_RELATIVE_PATH;
import static org.eclipse.skalli.model.ext.maven.MavenPomUtility.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenPomParserTest {

    @Test
    public void testParsePomNoParent() throws Exception {
        MavenPom expectedPom = new MavenPom();
        expectedPom.setSelf(getCoordinatesWithGroupId());

        parseAndAssertPom(getPomNoParent(), expectedPom);
    }

    @Test
    public void testParsePomWithParent() throws Exception {
        MavenPom expectedPom = new MavenPom();
        expectedPom.setSelf(getCoordinatesWithoutGroupId());
        expectedPom.setParent(getParentCoordinates());
        expectedPom.setParentRelativePath(PARENT_RELATIVE_PATH);

        parseAndAssertPom(getPomWithParent(), expectedPom);
    }

    @Test
    public void testParsePomWithModules() throws Exception {
        MavenPom expectedPom = new MavenPom();
        expectedPom.setSelf(getCoordinatesWithGroupId());
        addModules(expectedPom);

        parseAndAssertPom(getPomWithModules(), expectedPom);
    }

    @Test
    public void testParsePomWithParentAndModules() throws Exception {
        MavenPom expectedPom = new MavenPom();
        expectedPom.setSelf(getCoordinatesWithoutGroupId());
        expectedPom.setParent(getParentCoordinates());
        expectedPom.setParentRelativePath(PARENT_RELATIVE_PATH);
        addModules(expectedPom);

        parseAndAssertPom(getPomWithParentAndModules(), expectedPom);
    }

    private void parseAndAssertPom(String testContent, MavenPom expectedPom) throws Exception {
        MavenPomParser parser = new MavenPomParserImpl();
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(testContent.getBytes("UTF-8"));
            MavenPom pom = parser.parse(in);
            assertEquals(expectedPom, pom);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
