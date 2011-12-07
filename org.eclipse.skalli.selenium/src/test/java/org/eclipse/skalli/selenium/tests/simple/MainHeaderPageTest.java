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

import org.eclipse.skalli.selenium.pageobjects.MainHeaderPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.ProjectDetailsPage;
import org.eclipse.skalli.selenium.tests.Constants;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * This tests whether all pages have the standard layout
 * ({@link org.eclipse.skalli.selenium.pageobjects.MainHeaderPage})
 */
public class MainHeaderPageTest {
    private static WebDriver driver;
    private static MainHeaderPage page;
    private static MainPage mainPage;
    private static ProjectDetailsPage projectDetailsPage;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        initializePageObjects();
    }

    private static void initializePageObjects() {
        page = PageFactory.initElements(driver, MainHeaderPage.class);
        mainPage = PageFactory.initElements(driver, MainPage.class);
        projectDetailsPage = PageFactory.initElements(driver, ProjectDetailsPage.class);
    }

    @Before
    public void setup() {
        //navigates to the main page before every test
        DriverProvider.navigateToBaseUrl(driver);
        mainPage.isDisplayedWithExplicitWait();
    }

    @After
    public void proof() {
        //checks after the test whether a MainHeaderPage page is displayed or not
        page.isDisplayedWithExplicitWait();
    }

    @Test
    public void mainHeaderPageLayoutOnLoginPageTest() {
        if (!DriverProvider.isOnLoginPage(driver)) {
            DriverProvider.goToLoginPage();

            //proof standard page layout here instead of doing it in proof()
            page.isDisplayedWithExplicitWait();
            DriverProvider.login(driver);
        }
    }

    @Test
    public void mainHeaderPageLayoutOnMainPageTest() {
        mainPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void mainHeaderPageLayoutOnAllProjectsLinkTest() {
        mainPage.isDisplayedWithExplicitWait();
        mainPage.clickAllProjectsLink();
    }

    @Test
    public void mainHeaderPageLayoutOnMyProjectsLinkTest() {
        mainPage.isDisplayedWithExplicitWait();
        mainPage.clickMyProjectsLink();
    }

    @Test
    public void mainHeaderPageLayoutOnMyFavoritesLinkTest() {
        mainPage.isDisplayedWithExplicitWait();
        mainPage.clickMyFavoritesLink();
    }

    @Test
    public void mainHeaderPageLayoutOnCreateProjectLinkTest() {
        mainPage.isDisplayedWithExplicitWait();
        mainPage.clickCreateProjectLink();
    }

    @Test
    public void mainHeaderPageLayoutOnShowAllTagsLinkTest() {
        mainPage.isDisplayedWithExplicitWait();
        mainPage.clickShowAllTagsLink();
    }

    @Test
    public void mainHeaderPageLayoutOnSearchTest() {
        mainPage.isDisplayedWithExplicitWait();
        mainPage.sendKeysToSearchFieldAndSubmit("test");
    }

    @Test
    public void mainHeaderPageLayoutOnEditLinkTest() {
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        projectDetailsPage.isDisplayedWithExplicitWait();
        projectDetailsPage.clickEditLink();
    }

    @Test
    public void mainHeaderPageLayoutOnAddProjectToJiraLinkTest() {
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        projectDetailsPage.isDisplayedWithExplicitWait();
        projectDetailsPage.clickAddProjectToJiraLink();
    }

    @Test
    public void mainHeaderPageLayoutOnCreateGitGerritRepoLinkTest() {
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        projectDetailsPage.isDisplayedWithExplicitWait();
        projectDetailsPage.clickCreateGitGerritRepoLink();
    }

    @Test
    public void mainHeaderPageLayoutOnRequestPerforceProjectLinkTest() {
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        projectDetailsPage.isDisplayedWithExplicitWait();
        projectDetailsPage.clickRequestPerforceProjectLink();
    }
}
