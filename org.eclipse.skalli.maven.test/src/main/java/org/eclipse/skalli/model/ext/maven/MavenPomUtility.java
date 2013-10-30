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
package org.eclipse.skalli.model.ext.maven;

import static org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil.*;

import java.util.Random;

import org.eclipse.skalli.model.ext.maven.internal.MavenPom;

@SuppressWarnings("nls")
public class MavenPomUtility {

    private static final char[] WHITESPACE = new char[] {' ', '\r', '\n', '\t'};

    public static final String MODULE2 = "module2";
    public static final String MODULE1 = "module1";

    public static void beginXml(StringBuilder sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    }

    public static void beginProject(StringBuilder sb) {
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><modelVersion>4.0.0</modelVersion>");
    }

    public static void endProject(StringBuilder sb) {
        sb.append("</project>");
    }

    public static void addParentTag(StringBuilder sb) {
        sb.append("<parent>");
        append(sb, "groupId", PARENT_GROUPID);
        append(sb, "artifactId", PARENT_ARTIFACT);
        append(sb, "relativePath", PARENT_RELATIVE_PATH);
        sb.append("</parent>");
    }

    public static void addCoordinatesWithGroupId(StringBuilder sb) {
        append(sb, "groupId", GROUPID);
        append(sb, "artifactId", ARTIFACT);
        append(sb, "packaging", PACKAGING);
        append(sb, "name", "name-", GROUPID, "-", ARTIFACT);
        append(sb, "description", "description-", GROUPID, "-", ARTIFACT);
    }

    public static void addCoordinatesWithoutGroupId(StringBuilder sb) {
        append(sb, "artifactId", ARTIFACT);
        append(sb, "packaging", PACKAGING);
    }

    public static void addModules(StringBuilder sb) {
        sb.append("<modules>");
        append(sb, "module", MODULE1);
        append(sb, "module", MODULE2);
        sb.append("</modules>");
    }

    public static MavenModule getCoordinatesWithGroupId() {
        MavenModule c = new MavenModule(GROUPID, ARTIFACT, PACKAGING);
        c.setName("name-" + GROUPID + "-" + ARTIFACT);
        c.setDescription("description-" + GROUPID + "-" + ARTIFACT);
        return c;
    }

    public static MavenModule getCoordinatesWithoutGroupId() {
        return new MavenModule(null, ARTIFACT, PACKAGING);
    }

    public static MavenModule getParentCoordinates() {
        return new MavenModule(PARENT_GROUPID, PARENT_ARTIFACT, null);
    }

    public static void addModules(MavenPom pom) {
        pom.getModuleTags().add(MODULE1);
        pom.getModuleTags().add(MODULE2);
    }

    public static String getPomWithParent() {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        addParentTag(testContent);
        addCoordinatesWithoutGroupId(testContent);
        endProject(testContent);
        return testContent.toString();
    }

    public static String getPomWithModules() {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        addCoordinatesWithGroupId(testContent);
        addModules(testContent);
        endProject(testContent);
        return testContent.toString();
    }

    public static String getPomNoParent() {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        addCoordinatesWithGroupId(testContent);
        endProject(testContent);
        return testContent.toString();
    }

    public static String getPomWithParentAndModules() {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        addParentTag(testContent);
        addCoordinatesWithoutGroupId(testContent);
        addModules(testContent);
        endProject(testContent);
        return testContent.toString();
    }

    public static String getPomWithWhitespace() {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        addParentTag(testContent);
        addCoordinatesWithoutGroupId(testContent);
        addModules(testContent);
        endProject(testContent);
        return testContent.toString();
    }

    public static String getPomForModule(String moduleName, String parentPath) {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        testContent.append("<parent>");
        append(testContent, "groupId", PARENT_GROUPID);
        append(testContent, "artifactId", PARENT_ARTIFACT);
        if (parentPath != null) {
            append(testContent, "relativePath", parentPath);
        }
        testContent.append("</parent>");
        append(testContent, "artifactId", moduleName);
        append(testContent, "packaging", PACKAGING);
        endProject(testContent);
        return testContent.toString();
    }

    private static void append(StringBuilder sb, String nodeName, String... values) {
        sb.append('<').append(nodeName).append('>');
        appendRandomWhitespace(sb);
        if (values != null) {
            for (String value: values) {
                sb.append(value);
            }
        }
        appendRandomWhitespace(sb);
        sb.append("</").append(nodeName).append('>');
    }

    private static void appendRandomWhitespace(StringBuilder sb) {
        Random rd = new Random();
        int count = rd.nextInt(10);
        for (int i = 0; i < count; ++i) {
            sb.append(WHITESPACE[rd.nextInt(WHITESPACE.length-1)]);
        }
    }
}
