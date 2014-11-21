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
package org.eclipse.skalli.selenium.tests.simple;

import junit.framework.Assert;

import org.eclipse.skalli.selenium.pageobjects.MainHeaderPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.LoginFailedPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.LoginPage;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class PageAccessTest {
    private static WebDriver driver;
    private static MainHeaderPage page;

    @BeforeClass
    public static void setupClass() {
        initializeDriver();

        initializePageObjects();
    }

    private static void initializeDriver() {
        driver = DriverProvider.getDriver();
    }

    private static void initializePageObjects() {
        page = PageFactory.initElements(driver, MainHeaderPage.class);
    }

    @Before
    public void setup() {
        //navigate to the base url before every test
        DriverProvider.navigateToBaseUrl(driver);
    }

    @After
    public void proof() {
        //check after every test if MainHeaderPage page is displayed
        //should be a page of the project which meants that the page is accessible
        try {
            page.isDisplayedWithExplicitWait();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("page title is: " + page.getTitle()
                    + " (Maybe the page couldn't be loaded!)\n"
                    + e.getMessage(), e.getCause());
        }
    }

    /**
     * Tests the accessibility of the page
     */
    @Test
    public void pageAccessTest() {
        //check the login page or the main page (depends on what test is executed first - maybe we are logged in)
    }

    /**
     * Tests whether the login is possible or not
     */
    @Test
    public void loginTest() {
        //reset driver if we are not on login page
        if (!DriverProvider.isOnLoginPage(driver)) {
            DriverProvider.goToLoginPage();
        }

        //check if the login page is displayed
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        loginPage.isDisplayedWithExplicitWait();

        //test login failed page
        String foo = "foo";
        DriverProvider.login(driver, foo, "");

        //check if the LoginFailedPage is displayed
        LoginFailedPage loginFailedPage = PageFactory.initElements(driver, LoginFailedPage.class);
        loginFailedPage.isDisplayedWithExplicitWait();

        //navigate back to the login page
        driver.navigate().back();
        loginPage.isDisplayedWithExplicitWait();

        //deleting the user name (clear)
        loginPage.sendKeysToUserNameField(new String(new char[foo.length()]).replace('\0', '\b'));

        Assert.assertTrue("Could not login!", DriverProvider.login(driver));
    }
}
