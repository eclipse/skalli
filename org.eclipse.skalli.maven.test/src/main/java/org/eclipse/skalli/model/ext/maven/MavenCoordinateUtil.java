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

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class MavenCoordinateUtil {

    public static final String PACKAGING = "jar";
    public static final String ARTIFACT = "artifact";
    public static final String GROUPID = "org.example.test";

    public static final String PARENT_PACKAGING = "pom";
    public static final String PARENT_ARTIFACT = "parent";
    public static final String PARENT_GROUPID = "org.example.test";
    public static final String PARENT_RELATIVE_PATH = "../parentpath";

    public static MavenModule TEST_COORD = new MavenModule(GROUPID, ARTIFACT, PACKAGING);
    public static MavenModule TEST_PARENT_COORD = new MavenModule(PARENT_GROUPID, PARENT_ARTIFACT,
            PARENT_PACKAGING);

    public static List<MavenModule> TEST_MODULES = Arrays.asList(
            new MavenModule("a.b.c", "art1", "jar"),
            new MavenModule("a.b.c", "art2", "jar"),
            new MavenModule("a.b.c", "art3", "war"),
            new MavenModule("d.e", "art4711", "jar"),
            new MavenModule("f.g.h", "art1", "jar"));
}
