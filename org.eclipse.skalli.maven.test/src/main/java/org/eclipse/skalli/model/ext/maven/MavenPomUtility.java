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

import org.eclipse.skalli.model.ext.maven.internal.MavenPom;

@SuppressWarnings("nls")
public class MavenPomUtility {

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
        sb.append("<groupId>").append(PARENT_GROUPID).append("</groupId>");
        sb.append("<artifactId>").append(PARENT_ARTIFACT).append("</artifactId>");
        sb.append("<relativePath>").append(PARENT_RELATIVE_PATH).append("</relativePath>");
        sb.append("</parent>");
    }

    public static void addCoordinatesWithGroupId(StringBuilder sb) {
        sb.append("<groupId>").append(GROUPID).append("</groupId>");
        sb.append("<artifactId>").append(ARTIFACT).append("</artifactId>");
        sb.append("<packaging>").append(PACKAGING).append("</packaging>");
        sb.append("<name>").append("name-").append(GROUPID).append("-").append(ARTIFACT).append("</name>");
        sb.append("<description>").append("description-").append(GROUPID).append("-").append(ARTIFACT)
                .append("</description>");
    }

    public static void addCoordinatesWithoutGroupId(StringBuilder sb) {
        sb.append("<artifactId>").append(ARTIFACT).append("</artifactId>");
        sb.append("<packaging>").append(PACKAGING).append("</packaging>");
    }

    public static void addModules(StringBuilder sb) {
        sb.append("<modules>");
        sb.append("<module>").append(MODULE1).append("</module>");
        sb.append("<module>").append(MODULE2).append("</module>");
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

    public static String getPomForModule(String moduleName, String parentPath) {
        StringBuilder testContent = new StringBuilder();
        beginXml(testContent);
        beginProject(testContent);
        testContent.append("<parent>");
        testContent.append("<groupId>").append(PARENT_GROUPID).append("</groupId>");
        testContent.append("<artifactId>").append(PARENT_ARTIFACT).append("</artifactId>");
        if (parentPath != null) {
            testContent.append("<relativePath>").append(parentPath).append("</relativePath>");
        }
        testContent.append("</parent>");
        testContent.append("<artifactId>").append(moduleName).append("</artifactId>");
        testContent.append("<packaging>").append(PACKAGING).append("</packaging>");
        endProject(testContent);
        return testContent.toString();
    }
}
