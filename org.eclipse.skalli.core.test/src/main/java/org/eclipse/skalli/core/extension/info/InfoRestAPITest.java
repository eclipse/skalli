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
package org.eclipse.skalli.core.extension.info;

import java.util.List;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.SchemaValidationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InfoRestAPITest {

    private List<Project> projects;
    private ProjectService projectService;

    @Before
    public void setup() throws Exception {
        projectService = BundleManager.getRequiredService(ProjectService.class);
        projects = projectService.getAll();
        Assert.assertTrue("projects.size() > 0", projects.size() > 0);
    }

    @Test
    public void testValidate() throws Exception {
        SchemaValidationUtils.validate(projects, InfoExtension.class, "extension-info.xsd");
    }
}
