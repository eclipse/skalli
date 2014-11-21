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
package org.eclipse.skalli.core.rest.resources;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("nls")
public class InheritanceTest {

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<ExtensionService> serviceRegistration;

    private UUID uuidParent;
    private UUID uuidProject;
    private UUID uuidExtension;
    private Project parent;
    private Project project;
    private ExtensionEntityBase extension;

    @Before
    public void setup() throws BundleException {
        uuidParent = UUID.randomUUID();
        uuidProject = UUID.randomUUID();
        uuidExtension = UUID.randomUUID();

        parent = new Project();
        parent.setUuid(uuidParent);
        parent.setProjectId("parent");

        project = new Project();
        project.setUuid(uuidProject);
        project.setProjectId("project");
        project.setParentEntity(parent);

        extension = new TestExtension();
        extension.setUuid(uuidExtension);

        parent.addExtension(extension);

        serviceRegistration = BundleManager.registerService(ExtensionService.class, new TestExtensionService(), null);
        Assert.assertNotNull(serviceRegistration);
    }

    @After
    public void tearDown() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Test
    public void test() throws IOException {
        ProjectConverter projectConverter = new ProjectConverter("localhost", false);

        // Verify that the parent has the extension, but not inherited
        ResourceRepresentation<Project> rep1 = new ResourceRepresentation<Project>(
                parent, projectConverter);
        String res1 = rep1.getText();
        Assert.assertTrue(res1.contains("<testExtension"));
        Assert.assertFalse(res1.contains("inherited=\"true\""));

        // Verify that the project doesn't have the extension
        ResourceRepresentation<Project> rep2 = new ResourceRepresentation<Project>(
                project, projectConverter);
        String res2 = rep2.getText();
        Assert.assertFalse(res2.contains("<testExtension"));
        Assert.assertFalse(res2.contains("inherited=\"true\""));

        project.setInherited(TestExtension.class, true);
        // Verify that now the project inherits the extension
        ResourceRepresentation<Project> rep3 = new ResourceRepresentation<Project>(
                project, projectConverter);
        String res3 = rep3.getText();
        Assert.assertTrue(res3.contains("<testExtension"));
        Assert.assertTrue(res3.contains("inherited=\"true\""));
    }

}
