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

import org.eclipse.skalli.selenium.pageobjects.concrete.AllProjectsViewPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MyFavoritesPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MyProjectsPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.SearchPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.TagCloudPage;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class MainPageTest {
    private static WebDriver driver;
    private static MainPage page;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        initializePageObjects();

        page.isDisplayedWithExplicitWait();
    }

    private static void initializePageObjects() {
        page = PageFactory.initElements(driver, MainPage.class);
    }

    @Before
    public void setup() {
        //navigate to the base url an check if the main page is displayed before every test
        DriverProvider.navigateToBaseUrl(driver);

        page.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickSearchSyntaxLinkTest() {
        //how to verify that the link works!?
        page.clickSearchSyntaxLink();
    }

    @Test
    public void clickAllProjectsLinkTest() {
        page.clickAllProjectsLink();

        //result page check
        AllProjectsViewPage allProjectsViewPage = PageFactory.initElements(driver, AllProjectsViewPage.class);
        allProjectsViewPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickMyProjectsLinkTest() {
        page.clickMyProjectsLink();

        //result page check
        MyProjectsPage myProjectsPage = PageFactory.initElements(driver, MyProjectsPage.class);
        myProjectsPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickMyFavoritesLinkTest() {
        page.clickMyFavoritesLink();

        //result page check
        MyFavoritesPage myFavoritesPage = PageFactory.initElements(driver, MyFavoritesPage.class);
        myFavoritesPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickCreateProjectLinkTest() {
        page.clickCreateProjectLink();

        //result page check
        CreateProjectPage createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        createProjectPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickShowAllTagsLinkTest() {
        page.clickShowAllTagsLink();

        //result page check
        TagCloudPage tagCloudPage = PageFactory.initElements(driver, TagCloudPage.class);
        tagCloudPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void searchMethod1Test() {
        page.sendKeysToSearchField("test");
        page.clickSearchSubmitButton();

        //result page check
        SearchPage searchPage = PageFactory.initElements(driver, SearchPage.class);
        searchPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void searchMethod2Test() {
        page.sendKeysToSearchFieldAndSubmit("test");

        //result page check
        SearchPage searchPage = PageFactory.initElements(driver, SearchPage.class);
        searchPage.isDisplayedWithExplicitWait();
    }
}
