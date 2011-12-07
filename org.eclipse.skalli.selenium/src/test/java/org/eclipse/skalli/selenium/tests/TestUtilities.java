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
package org.eclipse.skalli.selenium.tests;

import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.ProjectDetailsPage;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class TestUtilities {
    public static WebDriver initializeDriver() {
        WebDriver driver = DriverProvider.getDriver();
        DriverProvider.navigateToBaseUrl(driver);
        DriverProvider.login(driver);
        return driver;
    }

    public static void navigateToExistingProjectsEditPage(WebDriver driver) {
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL + "?action=edit");
        PageFactory.initElements(driver, EditPage.class).isDisplayedWithExplicitWait();
    }

    public static void navigateToExistingProject(WebDriver driver) {
        DriverProvider.navigateToSubUrl(driver, Constants.SKALLI_PROJECT_SUB_URL);
        PageFactory.initElements(driver, ProjectDetailsPage.class).isDisplayedWithExplicitWait();
    }
}
