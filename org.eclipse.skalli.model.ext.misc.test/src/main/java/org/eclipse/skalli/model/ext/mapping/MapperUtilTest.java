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
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class MapperUtilTest {

    @Test
    public void testConvertGit() {
        String scmLocation = "scm:git:git.blubb.corp/eclipse/skalli.git";
        String pattern = "^scm:git:(git.blubb.corp)/(.*).git$";
        String template = "https://{1}:8080/#project,open,{2},n,z";
        String projectId = "bla.blubb";

        String res = MapperUtil.convert(projectId, scmLocation, pattern, template);
        Assert.assertEquals("https://git.blubb.corp:8080/#project,open,eclipse/skalli,n,z", res);
    }

    @Test
    public void testConvertUsingProjectId() {
        String scmLocation = "scm:git:git.blubb.corp/eclipse/skalli.git";
        String pattern = "^scm:git:(git.blubb.corp)/(.*).git$";
        String template = "https://server/{0}/index.html";
        String projectId = "bla.blubb";

        String res = MapperUtil.convert(projectId, scmLocation, pattern, template);
        Assert.assertEquals("https://server/bla.blubb/index.html", res);
    }

    @Test
    public void testConvertMailingList() {
        String mailingList = "razzmatazz@listserv.sap.corp";
        String pattern = "^(.+)@listserv.sap.corp$";
        String template = "http://some/{1}";
        String projectId = "bla.blubb";

        String res = MapperUtil.convert(projectId, mailingList, pattern, template);
        Assert.assertEquals("http://some/razzmatazz", res);
    }

}
