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
package org.eclipse.skalli.selenium.pageobjects.concrete;

import org.eclipse.skalli.selenium.pageobjects.ProjectOptionsPage;
import org.eclipse.skalli.selenium.pageobjects.SearchAndNavigationbarPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the project details page
 *
 * The project details page is a page with search field, navigation bar and the
 * project details page specific elements (the info boxes)
 */
public class ProjectDetailsPage extends SearchAndNavigationbarPage {
    //implement the dynamic elements from the extensions (info boxes) @see EditPage for the "pattern"
    private ProjectOptionsPage projectOptionsPage;

    public ProjectDetailsPage(WebDriver driver) {
        super(driver);
        projectOptionsPage = PageFactory.initElements(driver, ProjectOptionsPage.class);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getProjectOptionsPage().isDisplayed();
    }

    public void clickEditLink() {
        getProjectOptionsPage().clickEditLink();
    }

    public void clickAddProjectToJiraLink() {
        getProjectOptionsPage().clickAddProjectToJiraLink();
    }

    public void clickCreateGitGerritRepoLink() {
        getProjectOptionsPage().clickCreateGitGerritRepoLink();
    }

    public void clickRequestPerforceProjectLink() {
        getProjectOptionsPage().clickRequestPerforceProjectLink();
    }

    protected ProjectOptionsPage getProjectOptionsPage() {
        return projectOptionsPage;
    }
}
