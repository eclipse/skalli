/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.selenium.pageobjects.ext.util;

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * This is a form to add a user
 *
 * Can be found in project members extension edit form -> add user -> the form appears
 */
public class AddUserForm extends AbstractPage {
    public AddUserForm(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return getCaption().isDisplayed() && getSearchForField().isDisplayed() && getSearchButton().isDisplayed()
                && getSelectField().isDisplayed() && getAddButton().isDisplayed()
                && getAddAndCloseButton().isDisplayed()
                && getCloseButton().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getCaption();
    }

    @Override
    public String getTitle() {
        return getCaption().getText();
    }

    public void sendKeysToSearchForField(String text) {
        getSearchForField().sendKeys(text);
    }

    public void submitSearchForContent() {
        getSearchForField().sendKeys("\n");
    }

    //for some reasons it fails to click the search button
    //(maybe the result is not loaded fast enough)
    //use submitSearchForContent() instead
    public void clickSearchButton() {
        getSearchButton().click();
    }

    public void clickAddButton() {
        getAddButton().click();
    }

    public void clickAddAndCloseButton() {
        getAddAndCloseButton().click();
    }

    public void clickCloseButton() {
        getCloseButton().click();
    }

    public Select getSelect() {
        return new Select(getSelectField());
    }

    protected WebElement getCaption() {
        return driver.findElement(By.xpath(getXPathToCaption()));
    }

    protected WebElement getSearchForField() {
        return driver.findElement(By.xpath(getXPathToSearchForField()));
    }

    protected WebElement getSearchButton() {
        return driver.findElement(By.xpath(getXPathToSearchButton()));
    }

    protected WebElement getSelectField() {
        return driver.findElement(By.xpath(getXPathToSelectField()));
    }

    protected WebElement getAddButton() {
        return driver.findElement(By.xpath(getXPathToAddButton()));
    }

    protected WebElement getAddAndCloseButton() {
        return driver.findElement(By.xpath(getXPathToAddAndCloseButton()));
    }

    protected WebElement getCloseButton() {
        return driver.findElement(By.xpath(getXPathToCloseButton()));
    }

    private String getXPathToCaption() {
        return "html/body/div[7]/div/div/div/div[2]/div";
    }

    private String getXPathToSearchForField() {
       return "html/body/div[7]/div/div/div/div[3]/div/div/div/div[1]/div/div/div/div[1]/div[2]/input";
    }

    private String getXPathToSearchButton() {
        return "html/body/div[7]/div/div/div/div[3]/div/div/div/div[1]/div/div/div/div[2]/div/button";
    }

    private String getXPathToSelectField() {
        return "html/body/div[7]/div/div/div/div[3]/div/div/div/div[2]/div[2]/div/select";
    }

    private String getXPathToAddButton() {
        return "html/body/div[7]/div/div/div/div[3]/div/div/div/div[3]/div/div/div/div[1]/div/div";
    }

    private String getXPathToAddAndCloseButton() {
        return "html/body/div[7]/div/div/div/div[3]/div/div/div/div[3]/div/div/div/div[2]/div/div";
    }

    private String getXPathToCloseButton() {
        return "html/body/div[7]/div/div/div/div[1]";
    }
}
