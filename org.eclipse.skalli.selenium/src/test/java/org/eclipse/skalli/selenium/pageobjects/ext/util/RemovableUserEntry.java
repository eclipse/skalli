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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is a removable user entry which can be found in the project members extension edit form
 *
 * It contains the "remove" link of the entry and the options inherited from {@link UserEntry}
 */
public class RemovableUserEntry extends UserEntry {
    public RemovableUserEntry(WebDriver driver) {
        super(driver);
    }

    @Override
    public RemovableUserEntry setBaseXPath(String baseXPath) {
        super.setBaseXPath(baseXPath);

        return this;
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getRemoveLink().isDisplayed();
    }

    public void clickRemoveLink() {
        getRemoveLink().click();
    }

    protected WebElement getRemoveLink() {
        return driver.findElement(By.xpath(getXPathToRemoveLink()));
    }

    private String getXPathToRemoveLink() {
        return getBaseXPath() + "/../../../../../../../td[2]/div/div/span/span";
    }
}
