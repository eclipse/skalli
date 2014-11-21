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
package org.eclipse.skalli.selenium.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the project options
 *
 * The project options are displayed on the project details page.
 * It contains the elements of the project options which are the links
 *  "Edit", "Add Project to JIRA", "Create Git/Gerrit Repo" and "Request Perforce Project"
 */
public class ProjectOptionsPage extends MainHeaderPage {
    @FindBy(how = How.LINK_TEXT, using = "Edit")
    private WebElement edit;

    @FindBy(how = How.ID, using = "org.eclipse.skalli.view.ext.impl.internal.links.AddProjectToJira")
    private WebElement addProjectToJira;

    @FindBy(how = How.ID, using = "org.eclipse.skalli.view.ext.impl.internal.links.CreateGitGerritRepo")
    private WebElement createGitGerritRepo;

    @FindBy(how = How.ID, using = "org.eclipse.skalli.view.ext.impl.internal.links.RequestP4Project")
    private WebElement requestPerforceProject;

    public ProjectOptionsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getEdit().isDisplayed() && getAddProjectToJira().isDisplayed()
                && getCreateGitGerritRepo().isDisplayed() && getRequestPerforceProject().isDisplayed();
    }

    public void clickEditLink() {
        getEdit().click();
    }

    public void clickAddProjectToJiraLink() {
        getAddProjectToJira().click();
    }

    public void clickCreateGitGerritRepoLink() {
        getCreateGitGerritRepo().click();
    }

    public void clickRequestPerforceProjectLink() {
        getRequestPerforceProject().click();
    }

    protected WebElement getEdit() {
        return edit;
    }

    protected WebElement getAddProjectToJira() {
        return addProjectToJira;
    }

    protected WebElement getCreateGitGerritRepo() {
        return createGitGerritRepo;
    }

    protected WebElement getRequestPerforceProject() {
        return requestPerforceProject;
    }
}
