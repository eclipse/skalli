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
 * This page object contains the {@link org.openqa.selenium.WebElement}s of the search field
 *
 * The search field is positioned on the main page and on nearly every other page
 *
 * @see SearchAndNavigationbarPage
 */
public class SearchFieldPage extends MainHeaderPage {
    @FindBy(how = How.CLASS_NAME, using = "searchfield")
    private WebElement searchField;

    @FindBy(how = How.ID, using = "searchsubmit")
    private WebElement searchSubmitButton;

    public SearchFieldPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getSearchField().isDisplayed() && getSearchSubmitButton().isDisplayed();
    }

    public void clickSearchSubmitButton() {
        getSearchSubmitButton().click();
    }

    public void sendKeysToSearchField(String text) {
        getSearchField().sendKeys(text);
    }

    //does not click the submit button directly
    //uses the submit method of the form
    public void submitSearchFieldContent() {
        getSearchField().submit();
    }

    public void sendKeysToSearchFieldAndSubmit(String text) {
        sendKeysToSearchField(text);
        submitSearchFieldContent();
    }

    protected WebElement getSearchField() {
        return searchField;
    }

    protected WebElement getSearchSubmitButton() {
        return searchSubmitButton;
    }
}
