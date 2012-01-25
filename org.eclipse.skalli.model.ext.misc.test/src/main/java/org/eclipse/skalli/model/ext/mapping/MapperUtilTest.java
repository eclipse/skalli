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
package org.eclipse.skalli.model.ext.mapping;

import org.eclipse.skalli.ext.mapping.MapperUtil;
import org.eclipse.skalli.model.Project;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class MapperUtilTest {

    @Test
    public void testConvertGit() {
        String scmLocation = "scm:git:git.blubb.corp/eclipse/skalli.git";
        String pattern = "^scm:git:(git.blubb.corp)/(.*).git$";
        String template = "https://${1}:8080/#project,open,${2},n,z";
        String projectId = "bla.blubb";

        String res = MapperUtil.convert(scmLocation, pattern, template, projectId);
        Assert.assertEquals("https://git.blubb.corp:8080/#project,open,eclipse/skalli,n,z", res);
    }

    @Test
    public void testConvertUsingProjectId() {
        String scmLocation = "scm:git:git.blubb.corp/eclipse/skalli.git";
        String pattern = "^scm:git:(git.blubb.corp)/(.*).git$";
        String template = "https://server/${0}/index.html";
        String projectId = "bla.blubb";

        String res = MapperUtil.convert(scmLocation, pattern, template, projectId);
        Assert.assertEquals("https://server/bla.blubb/index.html", res);
    }

    @Test
    public void testConvertUsingProjectIdAndUserId() {
        String scmLocation = "scm:git:git.blubb.corp/eclipse/skalli.git";
        String pattern = "^scm:git:(git.blubb.corp)/(.*).git$";
        String template = "ssh://${userId}@${1}/${0}/index.html";
        String userId = "hugo";
        Project project = new Project("bla.blubb", null, null);

        String res = MapperUtil.convert(scmLocation, pattern, template, project, userId);
        Assert.assertEquals("ssh://hugo@git.blubb.corp/bla.blubb/index.html", res);
    }

    @Test
    public void testConvertWithProjectProperties() {
        String scmLocation = "scm:git:git.blubb.corp/eclipse/skalli.git";
        String pattern = "^scm:git:(git.blubb.corp)/(.*).git$";
        String template = "ssh://${userId}@${1}/${0}/${name}/index.html";
        String userId = "hugo";
        Project project = new Project("bla.blubb", null, "Blubber Project");

        String res = MapperUtil.convert(scmLocation, pattern, template, project, userId);
        Assert.assertEquals("ssh://hugo@git.blubb.corp/bla.blubb/Blubber Project/index.html", res);
    }

    @Test
    public void testConvertMailingList() {
        String mailingList = "razzmatazz@listserv.sap.corp";
        String pattern = "^(.+)@listserv.sap.corp$";
        String template = "http://some/${1}";
        String projectId = "bla.blubb";

        String res = MapperUtil.convert(mailingList, pattern, template, projectId);
        Assert.assertEquals("http://some/razzmatazz", res);
    }

}
