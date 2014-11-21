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
import org.openqa.selenium.support.PageFactory;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the search field and navigation bar
 *
 * The search field and the navigation bar is on nearly every page except to the main page.
 * It contains the search field elements and the elements of the navigation bar which are the links to
 *  "All Projects", "Tag Cloud", "My Projects" and "My Favorites"
 */
public class SearchAndNavigationbarPage extends MainHeaderPage {
    //SearchAndNavigationbarPage could also inherit from SearchFieldPage or NavigationbarPage instead of MainHeaderPage
    private SearchFieldPage searchPage;

    private NavigationbarPage navigationbarPage;

    public SearchAndNavigationbarPage(WebDriver driver) {
        super(driver);
        searchPage = PageFactory.initElements(driver, SearchFieldPage.class);
        navigationbarPage = PageFactory.initElements(driver, NavigationbarPage.class);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getSearchPage().isDisplayed() && getNavigationbarPage().isDisplayed();
    }

    public void clickSearchSubmitButton() {
        getSearchPage().clickSearchSubmitButton();
    }

    public void clickAllProjectsLink() {
        getNavigationbarPage().clickAllProjectsLink();
    }

    public void clickTagCloudLink() {
        getNavigationbarPage().clickTagCloudLink();
    }

    public void clicMyProjectsLink() {
        getNavigationbarPage().clicMyProjectsLink();
    }

    public void clickMyFavoritesLink() {
        getNavigationbarPage().clickMyFavoritesLink();
    }

    public void sendKeysToSearchField(String text) {
        getSearchPage().sendKeysToSearchField(text);
    }

    /**
     * does not click the submit button directly - uses the submit method of the form
     */
    public void submitSearchFieldContent() {
        getSearchPage().submitSearchFieldContent();
    }

    public void sendKeysToSearchFieldAndSubmit(String text) {
        getSearchPage().sendKeysToSearchFieldAndSubmit(text);
    }

    protected SearchFieldPage getSearchPage() {
        return searchPage;
    }

    protected NavigationbarPage getNavigationbarPage() {
        return navigationbarPage;
    }
}
