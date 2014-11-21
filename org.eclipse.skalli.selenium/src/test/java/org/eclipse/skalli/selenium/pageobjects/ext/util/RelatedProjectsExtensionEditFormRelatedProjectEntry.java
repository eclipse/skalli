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
 * This is related project entry used in the related projects extension edit form
 *
 * It contains elements to be able to remove the related project, get the link of
 * the related project, send keys to the related project link field and to click the
 * selection button
 */
public class RelatedProjectsExtensionEditFormRelatedProjectEntry extends AbstractPage {
    //the XPath to the element (div) containing
    //  /div[1]/div/div/input       -> the realted project field
    //  /div[2]/div/div/span/span   -> the links like remove / add
    //  /div[1]/div/div/div         -> the realted project selection button
    private String baseXPath;

    public RelatedProjectsExtensionEditFormRelatedProjectEntry(WebDriver driver) {
        super(driver);
    }

    //it is essential that you set the base XPath
    public RelatedProjectsExtensionEditFormRelatedProjectEntry setBaseXPath(String baseXPath) {
        this.baseXPath = baseXPath;

        return this;
    }

    @Override
    public boolean isDisplayed() {
        return getRelatedProjectElement().isDisplayed() && getRemoveLinkElement().isDisplayed()
                && getRelatedProjectsSelectionButton().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getRelatedProjectElement();
    }

    public String getRelatedProject() {
        return getRelatedProjectElement().getAttribute("value");
    }

    //TODO find a safe way to send keys to a field with a selection button
    public void sendKeysToRelatedProjectField(String text, boolean click) {
        getRelatedProjectElement().sendKeys(text + "\n");

        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

        if (click) {
            getRelatedProjectElement().click();
        }
    }

    public void clickRemoveRelatedProjectsLink() {
        getRemoveLinkElement().click();
    }

    public void clickRelatedProjectsSelectionButton() {
        getRelatedProjectsSelectionButton().click();
    }

    protected String getBaseXPath() {
        return baseXPath;
    }

    protected WebElement getRelatedProjectElement() {
        return driver.findElement(By.xpath(getXPathRelatedProject()));
    }

    protected WebElement getRemoveLinkElement() {
        return driver.findElement(By.xpath(getXPathToRemoveLink()));
    }

    protected WebElement getRelatedProjectsSelectionButton() {
        return driver.findElement(By.xpath(getXPathToRelatedProjectsSelectionButton()));
    }

    private String getXPathRelatedProject() {
        return getBaseXPath() + "/div[1]/div/div/input";
    }

    private String getXPathToRemoveLink() {
        return getBaseXPath() + "/div[2]/div/div/span/span";
    }

    private String getXPathToRelatedProjectsSelectionButton() {
        return getBaseXPath() + "/div[1]/div/div/div";
    }
}
