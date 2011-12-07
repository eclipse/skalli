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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class AbstractEditPageTest {
    private static WebDriver driver;
    private static EditPage editPage;
    private static ProjectDetailsPage projectDetailsPage;
    private static List<AbstractExtensionEditForm> extensions;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        PageFactory.initElements(driver, MainPage.class).isDisplayedWithExplicitWait();

        initializePageObjects();

        //navigate to an existing sub project
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        projectDetailsPage.isDisplayedWithExplicitWait();

        //go to the edit page
        projectDetailsPage.clickEditLink();
        editPage.isDisplayedWithExplicitWait();

        //initialize the abstract extensions
        extensions = new ArrayList<AbstractExtensionEditForm>();
        for (int i = 0; i < AbstractExtensionEditForm.getNumberOfExtensions(driver); i++) {
            extensions.add(new AbstractExtensionEditForm(driver, i) {
                @Override
                protected boolean isExtensionContentDisplayed() {
                    return true;
                }
            });
        }
    }

    private static void initializePageObjects() {
        editPage = PageFactory.initElements(driver, EditPage.class);
        projectDetailsPage = PageFactory.initElements(driver, ProjectDetailsPage.class);
    }

    @Before
    public void setup() {
        //check if the edit page is displayed before every test
        editPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void printExtensionInformationTest() {
        System.out.println("Number of extensions: " + AbstractExtensionEditForm.getNumberOfExtensions(driver));
        System.out.println();

        //print abstract extension information
        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();
            System.out.println(editForm.getTitle());
            System.out.println(editForm.getDescription());
            System.out.println("isEditable: " + editForm.isEditable());
            System.out.println("isInherited: " + editForm.isInherited());
            System.out.println("isDiabled: " + editForm.isDisabled());
            System.out.println("isShown: " + editForm.isShown());
            System.out.println();
        }
    }

    @Test
    public void numberOfExtensionsTest() {
        Assert.assertTrue("the number of extensions is not correct",
                AbstractExtensionEditForm.getNumberOfExtensions(driver) == 9);
    }

    @Test
    public void extensionIsDisplayedTest() {
        //check if every extension is displayed (only the abstract extension elements)
        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();
            Assert.assertTrue("extension " + editForm.getTitle() + " is not displayed",
                    editForm.isDisplayedWithExplicitWait());
        }
    }

    @Test
    public void makeExtensionsEditableTest() {
        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();

            editForm.makeExtensionEditable();

            editForm.isDisplayedWithExplicitWait();

            Assert.assertTrue(editForm.getTitle() + " is not editable", editForm.isEditable());

            Assert.assertTrue("extension " + editForm.getTitle() + " is not displayed",
                    editForm.isDisplayedWithExplicitWait());
        }
    }

    @Test
    public void makeExtensionsInheritedTest() {
        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();

            editForm.makeExtensionInherited();

            editForm.isDisplayedWithExplicitWait();

            //not checking Basics... cannot be inherited
            if (!editForm.getTitle().equals("Basics")) {
                Assert.assertTrue(editForm.getTitle() + " is not inherited", editForm.isInherited());
            }

            Assert.assertTrue("extension " + editForm.getTitle() + " is not displayed",
                    editForm.isDisplayedWithExplicitWait());
        }
    }

    @Test
    public void makeExtensionsDisabledTest() {
        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();

            editForm.makeExtensionDisabled();

            editForm.isDisplayedWithExplicitWait();

            //not checking Basics, Project Members, Info... cannot be disabled
            if (!editForm.getTitle().equals("Basics") && !editForm.getTitle().equals("Project Members")
                    && !editForm.getTitle().equals("Info")) {
                Assert.assertTrue(editForm.getTitle() + " is not disabled", editForm.isDisabled());
            }

            Assert.assertTrue("extension " + editForm.getTitle() + " is not displayed",
                    editForm.isDisplayedWithExplicitWait());
        }
    }

    @Test
    public void makeExtensionsShownTest() {
        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();

            editForm.makeExtensionEditable();

            editForm.isDisplayedWithExplicitWait();

            Assert.assertTrue(editForm.getTitle() + " is not editable", editForm.isEditable());

            editForm.makeExtensionShown();

            Assert.assertTrue(editForm.getTitle() + " is not shown", editForm.isShown());

            Assert.assertTrue("extension " + editForm.getTitle() + " is not displayed",
                    editForm.isDisplayedWithExplicitWait());
        }
    }
}
