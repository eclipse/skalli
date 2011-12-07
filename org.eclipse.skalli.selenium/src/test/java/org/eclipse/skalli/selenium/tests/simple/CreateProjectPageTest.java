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

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class CreateProjectPageTest {
    private static WebDriver driver;
    private static CreateProjectPage createProjectPage;
    private static MainPage mainPage;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        initializePageObjects();

        mainPage.isDisplayedWithExplicitWait();
    }

    private static void initializePageObjects() {
        createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        mainPage = PageFactory.initElements(driver, MainPage.class);
    }

    @Before
    public void setup() {
        //navigate to the create project page before every test
        DriverProvider.navigateToSubUrl(driver, "/create");
        createProjectPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void cancelProjectCreationTest() {
        createProjectPage.clickCancelButton();

        //result page check
        mainPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void freeStyleComponentSelectionTest() {
        createProjectPage.clickFreeStyleComponentButton();

        //result page check
        createProjectPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void freeStyleProjectSelectionTest() {
        createProjectPage.clickFreeStyleProjectButton();

        //result page check
        createProjectPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void createProjectTest() {
        createProjectPage.clickCreateProjectButton();

        //result page check
        EditPage editPage = PageFactory.initElements(driver, EditPage.class);
        editPage.isDisplayedWithExplicitWait();
    }
}
