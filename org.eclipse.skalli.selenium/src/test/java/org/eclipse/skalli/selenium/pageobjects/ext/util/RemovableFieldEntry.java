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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is removable field entry which can be found in many extensions
 *
 * It is a normal field in a section where fields can be added and removed
 * (for example in the info extensions edit form -> see mailing lists area)
 */
public class RemovableFieldEntry extends AbstractPage {
    //the XPath to the element (div) containing
    //  /input                 -> the field
    //  /div/span/span         -> the remove link
    private String baseXPath;

    public RemovableFieldEntry(WebDriver driver) {
        super(driver);
    }

    public static List<String> getFieldContentsAsStrings(List<RemovableFieldEntry> entries) {
        List<String> tmp = new ArrayList<String>();

        for (Iterator<RemovableFieldEntry> iterator = entries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry removableFieldEntry = (RemovableFieldEntry) iterator.next();
            tmp.add(removableFieldEntry.getFieldContent());
        }

        return tmp;
    }

    //it is essential that you set the base XPath
    public RemovableFieldEntry setBaseXPath(String baseXPath) {
        this.baseXPath = baseXPath;

        return this;
    }

    @Override
    public boolean isDisplayed() {
        return getFieldElement().isDisplayed() && getRemoveLinkElement().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getFieldElement();
    }

    public String getFieldContent() {
        return getFieldElement().getAttribute("value");
    }

    public void sendKeysToField(String text) {
        getFieldElement().sendKeys(text);
    }

    public void clickRemoveLink() {
        getRemoveLinkElement().click();
    }

    protected String getBaseXPath() {
        return baseXPath;
    }

    protected WebElement getFieldElement() {
        return driver.findElement(By.xpath(getXPathToField()));
    }

    protected WebElement getRemoveLinkElement() {
        return driver.findElement(By.xpath(getXPathToRemoveLink()));
    }

    private String getXPathToField() {
        return getBaseXPath() + "/input";
    }

    private String getXPathToRemoveLink() {
        return getBaseXPath() + "/div[1]/span/span";
    }
}
