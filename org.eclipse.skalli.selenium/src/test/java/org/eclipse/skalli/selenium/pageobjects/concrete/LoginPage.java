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

import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.selenium.pageobjects.MainHeaderPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the login page
 *
 * The login page is a page with login page specific elements (user name field,
 *  password field and the submit button)
 */
public class LoginPage extends MainHeaderPage {
    @FindBy(how = How.NAME, using = "j_username")
    private WebElement userName;

    @FindBy(how = How.NAME, using = "j_password")
    private WebElement password;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getUserName().isDisplayed() && getPassword().isDisplayed();
    }

    public void sendKeysToUserNameField(String text) {
        getUserName().sendKeys(text);
    }

    public void sendKeysToPasswordField(String text) {
        getPassword().sendKeys(text);
    }

    //does not click the submit button directly
    //uses the submit method of the form
    public void submitUserNameAndPasswordContent() {
        getUserName().submit();
    }

    public void sendKeysToUserNameAndPasswordFieldAndSubmit(String userName, String password) {
        sendKeysToUserNameField(userName);
        sendKeysToPasswordField(password);
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        submitUserNameAndPasswordContent();
    }

    protected WebElement getUserName() {
        return userName;
    }

    protected WebElement getPassword() {
        return password;
    }
}
