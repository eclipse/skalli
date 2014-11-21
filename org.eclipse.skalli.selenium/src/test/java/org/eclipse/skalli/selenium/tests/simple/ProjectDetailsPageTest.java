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

import org.eclipse.skalli.selenium.pageobjects.concrete.AddProjectToJiraPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.CreateGitGerritLinkPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.ProjectDetailsPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.RequestPerforceProjectLinkPage;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class ProjectDetailsPageTest {
    private static WebDriver driver;
    private static ProjectDetailsPage page;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        PageFactory.initElements(driver, MainPage.class).isDisplayedWithExplicitWait();

        initializePageObjects();
    }

    private static void initializePageObjects() {
        page = PageFactory.initElements(driver, ProjectDetailsPage.class);
    }

    @Before
    public void setup() {
        TestUtilities.navigateToExistingProject(driver);
    }

    @Test
    public void projectDetailsPageIsDisplayedTest() {
    }

    @Test
    public void clickEditLinkTest() {
        page.clickEditLink();

        //check the result page
        EditPage editPage = PageFactory.initElements(driver, EditPage.class);
        editPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickAddProjectToJiraLinkTest() {
        page.clickAddProjectToJiraLink();

        //check the result page
        AddProjectToJiraPage addProjectToJiraPage = PageFactory.initElements(driver, AddProjectToJiraPage.class);
        addProjectToJiraPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickCreateGitGerritRepoLinkTest() {
        page.clickCreateGitGerritRepoLink();

        //check the result page
        CreateGitGerritLinkPage createGitGerritLinkPage = PageFactory.initElements(driver,
                CreateGitGerritLinkPage.class);
        createGitGerritLinkPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void clickRequestPerforceProjectLinkTest() {
        page.clickRequestPerforceProjectLink();

        //check the result page
        RequestPerforceProjectLinkPage requestPerforceProjectLinkPage = PageFactory.initElements(driver,
                RequestPerforceProjectLinkPage.class);
        requestPerforceProjectLinkPage.isDisplayedWithExplicitWait();
    }
}
