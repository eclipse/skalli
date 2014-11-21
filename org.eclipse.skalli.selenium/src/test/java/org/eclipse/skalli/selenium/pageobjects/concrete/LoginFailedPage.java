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

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the login failed page
 *
 * The login failed page is a page with login failed page specific elements (only the login failed message)
 */
public class LoginFailedPage extends AbstractPage {
    private static String NOTIFICATION = "Login Failed!";

    @FindBy(how = How.XPATH, using = "//body")
    private WebElement loginFailedNotification;

    public LoginFailedPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return getLoginFailedNotification().isDisplayed()
                && getLoginFailedNotification().getText().contains(NOTIFICATION);
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getLoginFailedNotification();
    }

    protected WebElement getLoginFailedNotification() {
        return loginFailedNotification;
    }
}
