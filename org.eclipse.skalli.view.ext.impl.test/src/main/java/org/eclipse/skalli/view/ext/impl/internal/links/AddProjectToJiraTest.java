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
package org.eclipse.skalli.view.ext.impl.internal.links;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class AddProjectToJiraTest {

    @Test
    public void testGetUri() {
        Project project = new Project();
        project.setUuid(PropertyHelperUtils.TEST_UUIDS[0]);
        project.setProjectId("foobar");
        AddProjectToJira jira = new AddProjectToJira();
        Assert.assertEquals("/create/jira?id=foobar", jira.getUri(project).toString());
    }

}
