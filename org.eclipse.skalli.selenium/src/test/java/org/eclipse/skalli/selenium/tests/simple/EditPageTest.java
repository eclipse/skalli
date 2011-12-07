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
package org.eclipse.skalli.selenium.tests.simple;

import java.util.Iterator;

import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.ProjectDetailsPage;
import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.tests.Constants;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPageTest {
    private static WebDriver driver;
    private static EditPage editPage;
    private static ProjectDetailsPage projectDetailsPage;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        PageFactory.initElements(driver, MainPage.class).isDisplayedWithExplicitWait();

        initializePageObjects();

        //navigate to an existing project and wait for the ProjectDetailsPage to be displayed
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        projectDetailsPage.isDisplayedWithExplicitWait();

        //navigate to the edit page of the existing project
        projectDetailsPage.clickEditLink();
        editPage.isDisplayedWithExplicitWait();
    }

    private static void initializePageObjects() {
        editPage = PageFactory.initElements(driver, EditPage.class);
        projectDetailsPage = PageFactory.initElements(driver, ProjectDetailsPage.class);
    }

    @Before
    public void setup() {
        //checks if the EditPage is displayed without extensions before every test
        editPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void editPageAreExtensionsDisplayedTest() {
        //make extensions editable
        for (Iterator<AbstractExtensionEditForm> iterator = editPage.getExtensions().iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();

            editForm.makeExtensionEditable();

            editForm.isDisplayedWithExplicitWait();

            Assert.assertTrue(editForm.getTitle() + " is not editable", editForm.isEditable());

            Assert.assertTrue("extension " + editForm.getTitle() + " is not displayed",
                    editForm.isDisplayedWithExplicitWait());
        }

        //checks if the extensions are displayed with sub elements if they exist
        editPage.areExtensionsDisplayed();
    }
}
