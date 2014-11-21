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

import org.eclipse.skalli.selenium.pageobjects.SearchFieldPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the main page
 *
 * The main page is a page with search field and the main page specific elements (links like
 *  "All Projects", "All Projects", "My Projects", "Create Project" and "(show all tags)")
 */
public class MainPage extends SearchFieldPage {
    @FindBy(how = How.CLASS_NAME, using = "searchsyntax-link")
    private WebElement searchSyntaxLink;

    @FindBy(how = How.ID, using = "linkAllProjects")
    private WebElement linkAllProjects;

    @FindBy(how = How.ID, using = "linkMyProjects")
    private WebElement linkMyProjects;

    @FindBy(how = How.ID, using = "linkMyFavorites")
    private WebElement linkMyFavorites;

    @FindBy(how = How.ID, using = "linkCreateProject")
    private WebElement linkCreateProject;

    @FindBy(how = How.CLASS_NAME, using = "tagcloud")
    private WebElement tagCloud;

    @FindBy(how = How.XPATH, using = "//div[@class=\'tagcloud\']/div/a")
    private WebElement linkShowAllTags;

    public MainPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getSearchSyntaxLink().isDisplayed() && getLinkAllProjects().isDisplayed()
                && getLinkMyProjects().isDisplayed() && getLinkMyFavorites().isDisplayed()
                && getLinkCreateProject().isDisplayed() && getTagCloud().isDisplayed()
                && getLinkShowAllTags().isDisplayed();
    }

    public void clickSearchSyntaxLink() {
        getSearchSyntaxLink().click();
    }

    public void clickAllProjectsLink() {
        getLinkAllProjects().click();
    }

    public void clickMyProjectsLink() {
        getLinkMyProjects().click();
    }

    public void clickMyFavoritesLink() {
        getLinkMyFavorites().click();
    }

    public void clickCreateProjectLink() {
        getLinkCreateProject().click();
    }

    public void clickShowAllTagsLink() {
        getLinkShowAllTags().click();
    }

    protected WebElement getSearchSyntaxLink() {
        return searchSyntaxLink;
    }

    protected WebElement getLinkAllProjects() {
        return linkAllProjects;
    }

    protected WebElement getLinkMyProjects() {
        return linkMyProjects;
    }

    protected WebElement getLinkMyFavorites() {
        return linkMyFavorites;
    }

    protected WebElement getLinkCreateProject() {
        return linkCreateProject;
    }

    protected WebElement getTagCloud() {
        return tagCloud;
    }

    protected WebElement getLinkShowAllTags() {
        return linkShowAllTags;
    }
}
