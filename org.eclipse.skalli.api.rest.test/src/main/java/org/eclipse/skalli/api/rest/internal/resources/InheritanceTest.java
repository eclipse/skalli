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
package org.eclipse.skalli.api.rest.internal.resources;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

import org.eclipse.skalli.api.rest.internal.util.IgnoreUnknownElementsXStreamRepresentation;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestExtension;

@SuppressWarnings("nls")
public class InheritanceTest {

    private UUID uuidParent;
    private UUID uuidProject;
    private UUID uuidExtension;
    private Project parent;
    private Project project;
    private ExtensionEntityBase extension;
    protected Set<ExtensionService> testExtensionServices;

    @Before
    public void setup() throws BundleException {
        new BundleManager(this.getClass()).startBundles();
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

        testExtensionServices = new HashSet<ExtensionService>();
        testExtensionServices.add(new TestExtensionService());
    }

    @Test
    public void test() throws IOException {
        ProjectConverter projectConverter = new ProjectConverter("localhost", false) {
            @Override
            Set<ExtensionService> getExtensionServices() {
                return testExtensionServices;
            }
        };

        // Verify that the parent has the extension, but not inherited
        IgnoreUnknownElementsXStreamRepresentation<Project> rep1 = new IgnoreUnknownElementsXStreamRepresentation<Project>(
                parent, new AliasedConverter[] { projectConverter });
        String res1 = rep1.getText();
        Assert.assertTrue(res1.contains("<testExtension"));
        Assert.assertFalse(res1.contains("inherited=\"true\""));

        // Verify that the project doesn't have the extension
        IgnoreUnknownElementsXStreamRepresentation<Project> rep2 = new IgnoreUnknownElementsXStreamRepresentation<Project>(
                project, new AliasedConverter[] { projectConverter });
        String res2 = rep2.getText();
        Assert.assertFalse(res2.contains("<testExtension"));
        Assert.assertFalse(res2.contains("inherited=\"true\""));

        project.setInherited(TestExtension.class, true);
        // Verify that now the project inherits the extension
        IgnoreUnknownElementsXStreamRepresentation<Project> rep3 = new IgnoreUnknownElementsXStreamRepresentation<Project>(
                project, new AliasedConverter[] { projectConverter });
        String res3 = rep3.getText();
        Assert.assertTrue(res3.contains("<testExtension"));
        Assert.assertTrue(res3.contains("inherited=\"true\""));
    }

}
