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
package org.eclipse.skalli.selenium.tests.logic;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.BasicsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.InfoExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.RemovableFieldEntry;
import org.eclipse.skalli.selenium.tests.Constants;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class InheritanceTest {
    private static WebDriver driver;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        initializePageObjects();
    }

    private static void initializePageObjects() {
        createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        editPage = PageFactory.initElements(driver, EditPage.class);
    }

    @Before
    public void setup() {
        TestUtilities.navigateToExistingProjectsEditPage(driver);
    }

    @Test
    public void inheritInfoTest() {
        //get the values
        InfoExtensionEditForm infoExtensionEditForm = editPage.getInfoExtensionEditForm();
        infoExtensionEditForm.isDisplayedWithExplicitWait();

        String projectHomepage = infoExtensionEditForm.getProjectHomepage();
        List<String> mailingsListEntries = RemovableFieldEntry.getFieldContentsAsStrings(infoExtensionEditForm
                .getMailingsListEntries());

        createNewProject();

        //inherit
        BasicsExtensionEditForm basicsExtensionEditForm = editPage.getBasicsExtensionEditForm();
        basicsExtensionEditForm.isDisplayedWithExplicitWait();

        basicsExtensionEditForm.sendKeysToParentProjectField(Constants.EXISTING_PARENT_PROJECT, true);
        basicsExtensionEditForm.isDisplayedWithExplicitWait();

        //inherit info extensions content
        infoExtensionEditForm = editPage.getInfoExtensionEditForm();
        infoExtensionEditForm.isDisplayedWithExplicitWait();

        if (!infoExtensionEditForm.isInherited()) {
            infoExtensionEditForm.clickInheritInheritedLink();
        }

        //get inherited content
        String inheritedProjectHomepage = infoExtensionEditForm.getProjectHomepage();
        List<String> inheritedMailingsListEntries = RemovableFieldEntry.getFieldContentsAsStrings(infoExtensionEditForm
                .getMailingsListEntries());

        //check
        Assert.assertTrue("project homepage is not inherited (is: " + inheritedProjectHomepage + " should be: "
                + projectHomepage, projectHomepage.equals(inheritedProjectHomepage));

        Assert.assertTrue("does not contain the same number of entries",
                mailingsListEntries.size() == inheritedMailingsListEntries.size());

        for (Iterator<String> iterator = mailingsListEntries.iterator(); iterator.hasNext();) {
            String mailingListEntry = (String) iterator.next();

            Assert.assertTrue("mailing list (" + mailingListEntry + ") is not inherited",
                    inheritedMailingsListEntries.contains(mailingListEntry));
        }
        editPage.clickLowerCancelButton();
    }

    private void createNewProject() {
        DriverProvider.navigateToSubUrl(driver, "/create");
        createProjectPage.isDisplayedWithExplicitWait();

        createProjectPage.clickCreateProjectButton();
        editPage.isDisplayedWithExplicitWait();
    }
}
