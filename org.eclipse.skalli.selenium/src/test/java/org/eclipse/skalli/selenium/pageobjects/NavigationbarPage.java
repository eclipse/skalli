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
 * This page object contains the {@link org.openqa.selenium.WebElement}s for navigation bar
 *
 * The navigation bar is on nearly every page except to the main page.
 * It contains the elements of the navigation bar which are the links to "All Projects",
 * "Tag Cloud", "My Projects" and "My Favorites"
 *
 * @see SearchAndNavigationbarPage
 */
public class NavigationbarPage extends MainHeaderPage {
    @FindBy(how = How.ID, using = "linkAllProjects")
    private WebElement linkAllProjects;

    @FindBy(how = How.ID, using = "linkTagCloud")
    private WebElement linkTagCloud;

    @FindBy(how = How.ID, using = "linkMyProjects")
    private WebElement linkMyProjects;

    @FindBy(how = How.ID, using = "linkMyFavorites")
    private WebElement linkMyFavorites;

    public NavigationbarPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getLinkAllProjects().isDisplayed() && getLinkTagCloud().isDisplayed()
                && getLinkMyProjects().isDisplayed() && getLinkMyFavorites().isDisplayed();
    }

    public void clickAllProjectsLink() {
        getLinkAllProjects().click();
    }

    public void clickTagCloudLink() {
        getLinkTagCloud().click();
    }

    public void clicMyProjectsLink() {
        getLinkMyProjects().click();
    }

    public void clickMyFavoritesLink() {
        getLinkMyFavorites().click();
    }

    protected WebElement getLinkAllProjects() {
        return linkAllProjects;
    }

    protected WebElement getLinkTagCloud() {
        return linkTagCloud;
    }

    protected WebElement getLinkMyProjects() {
        return linkMyProjects;
    }

    protected WebElement getLinkMyFavorites() {
        return linkMyFavorites;
    }
}
