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
package org.eclipse.skalli.selenium.pageobjects.ext.util;

import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is a form to add a link
 *
 * Can be found in additional links extension edit form -> add link -> the form appears
 */
public class AddLinkForm extends AbstractPage {
    public AddLinkForm(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return getCaption().isDisplayed() && getLinkGroupField().isDisplayed()
                && getLinkGroupSelectionButton().isDisplayed() && getPageTitleField().isDisplayed()
                && getUrlField().isDisplayed() && getOkAndCloseButton().isDisplayed() && getCloseButton().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getCaption();
    }

    @Override
    public String getTitle() {
        return getCaption().getText();
    }

    //TODO find a safe way to send keys to a field with a selection button
    public void sendKeysToLinkGroupField(String text, boolean click) {
        getLinkGroupField().sendKeys(text + "\n");

        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

        if (click) {
            getLinkGroupField().click();
        }
    }

    public void clickLinkGroupSelectionButton() {
        getLinkGroupSelectionButton().click();
    }

    public void sendKeysToPageTitleField(String text) {
        getPageTitleField().sendKeys(text);
    }

    public void sendKeysToUrlField(String text) {
        getUrlField().sendKeys(text);
    }

    public void clickOkAndCloseButton() {
        getOkAndCloseButton().click();
    }

    public void clickCloseButton() {
        getCloseButton().click();
    }

    protected WebElement getCaption() {
        return driver.findElement(By.xpath(getXPathToCaption()));
    }

    protected WebElement getLinkGroupField() {
        return driver.findElement(By.xpath(getXPathToLinkGroupField()));
    }

    protected WebElement getLinkGroupSelectionButton() {
        return driver.findElement(By.xpath(getXPathToLinkGroupSelectionButton()));
    }

    protected WebElement getPageTitleField() {
        return driver.findElement(By.xpath(getXPathToPageTitleField()));
    }

    protected WebElement getUrlField() {
        return driver.findElement(By.xpath(getXPathToUrlField()));
    }

    protected WebElement getOkAndCloseButton() {
        return driver.findElement(By.xpath(getXPathToOkAndCloseButton()));
    }

    protected WebElement getCloseButton() {
        return driver.findElement(By.xpath(getXPathToCloseButton()));
    }

    private String getXPathToCaption() {
        return "html/body/div[7]/div/div/div/div[" + 2 + "]/div";
    }

    private String getXPathToLinkGroupField() {
        return "html/body/div[7]/div/div/div/div[" + 3 + "]/div/div/div/div[1]/div[2]/div/input";
    }

    private String getXPathToLinkGroupSelectionButton() {
        return "html/body/div[7]/div/div/div/div[" + 3 + "]/div/div/div/div[1]/div[2]/div/div";
    }

    private String getXPathToPageTitleField() {
        return "html/body/div[7]/div/div/div/div[" + 3 + "]/div/div/div/div[2]/div[2]/input";
    }

    private String getXPathToUrlField() {
        return "html/body/div[7]/div/div/div/div[" + 3 + "]/div/div/div/div[3]/div[2]/input";
    }

    private String getXPathToOkAndCloseButton() {
        return "html/body/div[7]/div/div/div/div[" + 3 + "]/div/div/div/div[4]/div/div";
    }

    private String getXPathToCloseButton() {
        return "html/body/div[7]/div/div/div/div[" + 1 + "]";
    }
}
