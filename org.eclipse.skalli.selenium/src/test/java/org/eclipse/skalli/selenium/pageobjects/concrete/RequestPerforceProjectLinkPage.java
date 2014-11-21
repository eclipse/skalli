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

import org.eclipse.skalli.selenium.pageobjects.SearchAndNavigationbarPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the request perforce project page
 *
 * The request perforce project page is a page with search field, navigation bar and the
 * request perforce project page specific elements (create a ticket now link, copy to clipboard button,
 * back to project link)
 */
public class RequestPerforceProjectLinkPage extends SearchAndNavigationbarPage {
    private static final String REQUEST_PERFORCE_PROJECT_CAPTION = "Request Perforce Project";

    @FindBy(how = How.XPATH, using = "//div[@class=\'projectarearight\']/h3")
    private WebElement caption;

    @FindBy(how = How.PARTIAL_LINK_TEXT, using = "Create a new ticket now")
    private WebElement createANewTicketNowLink;

    @FindBy(how = How.ID, using = "clippy")
    private WebElement copyToClipboardButton;

    @FindBy(how = How.PARTIAL_LINK_TEXT, using = "Back to project")
    private WebElement backToProjectLink;

    public RequestPerforceProjectLinkPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getCaption().isDisplayed()
                && getCaption().getText().contains(REQUEST_PERFORCE_PROJECT_CAPTION)
                && getCreateANewTicketNowLink().isDisplayed() && getCopyToClipboardButton().isDisplayed()
                && getBackToProjectLink().isDisplayed();
    }

    public void clickCreateANewTicketNowLink() {
        getCreateANewTicketNowLink().click();
    }

    public void clickCopyToClipboardButton() {
        getCopyToClipboardButton().click();
    }

    public void clickBackToProjectLink() {
        getBackToProjectLink().click();
    }

    protected WebElement getCaption() {
        return caption;
    }

    protected WebElement getCreateANewTicketNowLink() {
        return createANewTicketNowLink;
    }

    protected WebElement getCopyToClipboardButton() {
        return copyToClipboardButton;
    }

    protected WebElement getBackToProjectLink() {
        return backToProjectLink;
    }
}
