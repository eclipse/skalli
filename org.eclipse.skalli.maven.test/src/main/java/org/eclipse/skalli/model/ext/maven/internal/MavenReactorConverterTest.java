/*******************************************************************************
 * Copyright (c) 2010-2016 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenReactorConverterTest extends RestWriterTestBase {

    private static final String INITIAL_MAVEN_REACTOR_EXTENSION_XML = "<mavenReactor/>";
    private static final String MAVEN_REACTOR_EXTENSION_XML = "<mavenReactor>"
            + "<coordinate>"
            + "<groupId>org.eclipse.skalli</groupId>"
            + "<artifactId>org.eclipse.skalli</artifactId>"
            + "<versions><version>2.0</version><version>1.0</version></versions>"
            + "<packaging>pom</packaging>"
            + "</coordinate><modules>"
            + "<module>"
            + "<groupId>org.eclipse.skalli</groupId>"
            + "<artifactId>org.eclipse.skalli.module1</artifactId>"
            + "<versions><version>1.0</version></versions>"
            + "<packaging>jar</packaging>"
            + "</module>"
            + "<module>"
            + "<groupId>org.eclipse.skalli</groupId>"
            + "<artifactId>org.eclipse.skalli.module2</artifactId>"
            + "<versions/>"
            + "<packaging>jar</packaging>"
            + "</module>"
            + "</modules></mavenReactor>";
    private static final String INITIAL_MAVEN_REACTOR_EXTENSION_JSON = "{}";
    private static final String MAVEN_REACTOR_EXTENSION_JSON =
            "{\"coordinate\":{"
            + "\"groupId\":\"org.eclipse.skalli\","
            + "\"artifactId\":\"org.eclipse.skalli\","
            + "\"versions\":[\"2.0\",\"1.0\"],"
            + "\"packaging\":\"pom\"},"
            + "\"modules\":[{"
            + "\"groupId\":\"org.eclipse.skalli\","
            + "\"artifactId\":\"org.eclipse.skalli.module1\","
            + "\"versions\":[\"1.0\"],"
            + "\"packaging\":\"jar\"},{"
            + "\"groupId\":\"org.eclipse.skalli\","
            + "\"artifactId\":\"org.eclipse.skalli.module2\","
            + "\"versions\":[],"
            + "\"packaging\":\"jar\"}"
            + "]}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        MavenReactorProjectExt ext = new MavenReactorProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalMavenReactorExtension(ext, restWriter);
        assertEqualsXML(INITIAL_MAVEN_REACTOR_EXTENSION_XML);
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        MavenReactorProjectExt ext = newMavenReatorExt();
        RestWriter restWriter = getRestWriterXML();
        marshalMavenReactorExtension(ext, restWriter);
        assertEqualsXML(MAVEN_REACTOR_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        MavenReactorProjectExt ext = new MavenReactorProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalMavenReactorExtension(ext, restWriter);
        assertEqualsJSON(INITIAL_MAVEN_REACTOR_EXTENSION_JSON);
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        MavenReactorProjectExt ext = newMavenReatorExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalMavenReactorExtension(ext, restWriter);
        assertEqualsJSON(MAVEN_REACTOR_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_MAVEN_REACTOR_EXTENSION_JSON);
        MavenReactorProjectExt ext = unmarshalMavenReactorExtension(restReader);
        MavenReactor mavenReactor = ext.getMavenReactor();
        assertNull(mavenReactor.getCoordinate());
        assertTrue(mavenReactor.getModules().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(MAVEN_REACTOR_EXTENSION_JSON);
        MavenReactorProjectExt ext = unmarshalMavenReactorExtension(restReader);
        MavenReactor mavenReactor = ext.getMavenReactor();
        MavenModule coordinate = mavenReactor.getCoordinate();
        assertEquals("org.eclipse.skalli", coordinate.getGroupId());
        assertEquals("org.eclipse.skalli", coordinate.getArtefactId());
        assertEquals("pom", coordinate.getPackaging());
        AssertUtils.assertEquals("getVersions()", coordinate.getVersions(), "1.0", "2.0");
        Iterator<MavenModule> modules = mavenReactor.getModules().iterator();
        MavenModule module1 = modules.next();
        assertEquals("org.eclipse.skalli", module1.getGroupId());
        assertEquals("org.eclipse.skalli.module1", module1.getArtefactId());
        assertEquals("jar", module1.getPackaging());
        AssertUtils.assertEquals("getVersions()", module1.getVersions(), "1.0");
        MavenModule module2 = modules.next();
        assertEquals("org.eclipse.skalli", module2.getGroupId());
        assertEquals("org.eclipse.skalli.module2", module2.getArtefactId());
        assertEquals("jar", module2.getPackaging());
        assertTrue(module2.getVersions().isEmpty());
        assertFalse(modules.hasNext());
    }

    private MavenReactorProjectExt newMavenReatorExt() {
        MavenReactorProjectExt ext = new MavenReactorProjectExt();
        MavenReactor mavenReactor = new MavenReactor();
        mavenReactor.setCoordinate(newMavenModule("org.eclipse.skalli", "org.eclipse.skalli", "pom", "1.0", "2.0"));
        mavenReactor.addModule(newMavenModule("org.eclipse.skalli", "org.eclipse.skalli.module1", "jar", "1.0"));
        mavenReactor.addModule(newMavenModule("org.eclipse.skalli", "org.eclipse.skalli.module2", null, new String[0]));
        ext.setMavenReactor(mavenReactor);
        return ext;
    }

    private MavenModule newMavenModule(String groupId, String artifactId, String packaging, String...versions) {
        MavenModule module = new MavenModule(groupId, artifactId, packaging);
        for (String version : versions) {
            module.addVersion(version);
        }
        return module;
    }

    private void marshalMavenReactorExtension(MavenReactorProjectExt ext, RestWriter restWriter) throws Exception {
        MavenReactorConverter converter = new MavenReactorConverter();
        restWriter.object("mavenReactor");
        converter.marshal(ext, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private MavenReactorProjectExt unmarshalMavenReactorExtension(RestReader restReader) throws Exception {
        MavenReactorConverter converter = new MavenReactorConverter();
        restReader.object();
        MavenReactorProjectExt ext = converter.unmarshal(restReader);
        restReader.end();
        return ext;
    }
}
