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
package org.eclipse.skalli.core.internal.mail;

import static org.easymock.EasyMock.*;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("nls")
public class MailTemplateUtilTest {

    private UUID projectUuid;
    private UUID parentUuid;
    private Project project;
    private Project parent;
    private String projectId = "test.TestProject";
    private String projectName = "Unit Test Project";
    private String parentId = "test.TestParent";
    private String parentName = "Unit Test Parent Project";

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();

        projectUuid = UUID.randomUUID();
        project = new Project();
        project.setUuid(projectUuid);
        project.setProjectId(projectId);
        project.setName(projectName);

        parentUuid = UUID.randomUUID();
        parent = new Project();
        parent.setUuid(parentUuid);
        parent.setProjectId(parentId);
        parent.setName(parentName);

        project.setParentEntity(parent);
    }

    /*
     * TODO enable tests again
     */

    @Ignore
    @Test
    public void testGetBody() {
        MailTemplate mockMailTemplate = createNiceMock(MailTemplate.class);
        expect(mockMailTemplate.getBodyTemplate()).andReturn("org/eclipse/skalli/core/internal/mail/TestContent.vm");
        replay(mockMailTemplate);
        try {
            String body = MailTemplateUtil.getBody(mockMailTemplate, project);
            Assert.assertEquals("This is a test for project test.TestProject.", body);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void testGetBodyParentChanged() {
        try {
            String body = MailTemplateUtil.getBody(MailTemplateUtil.PARENT_CHANGED, project);
            if (StringUtils.isBlank(body)) {
                Assert.fail("body is blank");
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void testGetSubject() {
        MailTemplate mockMailTemplate = createNiceMock(MailTemplate.class);
        expect(mockMailTemplate.getSubjectTemplate()).andReturn("org/eclipse/skalli/core/internal/mail/TestContent.vm");
        replay(mockMailTemplate);
        try {
            String subject = MailTemplateUtil.getSubject(mockMailTemplate, project);
            Assert.assertEquals("This is a test for project test.TestProject.", subject);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void testGetSubjectParentChanged() {
        try {
            String subject = MailTemplateUtil.getSubject(MailTemplateUtil.PARENT_CHANGED, project);
            if (StringUtils.isBlank(subject)) {
                Assert.fail("subject is blank");
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    // this test is for internal use only.
    // enable to test to see the output of a mail template on the console.
    //  @Test
    public void testLayoutParentChanged() {
        MailTemplate mailTemplate = MailTemplateUtil.PARENT_CHANGED;

        try {
            String subject = MailTemplateUtil.getSubject(mailTemplate, project);
            if (StringUtils.isBlank(subject)) {
                Assert.fail("subject is blank");
            } else {
                System.out.println("HEADER:\n" + subject);
            }
            String body = MailTemplateUtil.getBody(mailTemplate, project);
            if (StringUtils.isBlank(body)) {
                Assert.fail("body is blank");
            } else {
                System.out.println("BODY:\n" + body);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }
}
