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

import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is a user entry which can be found in the project members extension
 *
 * It contains the name of the user and the user links (can be "mail", "projects",...)
 */
public class UserEntry extends AbstractPage {
    //the XPath to the element (div) containing
    //  /span/img   -> the image of the user
    //  name        -> the name of the user
    //  /span/a     -> the link to the users projects
    private String baseXPath;

    public UserEntry(WebDriver driver) {
        super(driver);
    }

    //it is essential that you set the base XPath
    public UserEntry setBaseXPath(String baseXPath) {
        this.baseXPath = baseXPath;

        return this;
    }

    @Override
    public boolean isDisplayed() {
        return getNameElement().isDisplayed() && getProjectsLinks().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getNameElement();
    }

    public String getName() {
        //TODO how to get only the text of the current html tag and not including all sub elements -> returns the name of the sub links too (for example "admin admin mail projects")
        return getNameElement().getText();
    }

    //driver must be located on the edit form
    public int getNumberOfUserLinks() {
        return getUserLinks().size();
    }

    //driver must be located on the edit form
    public List<WebElement> getUserLinks() {
        WebElement root = driver.findElement(By.xpath(getXPathToProjectsLinks()));

        List<WebElement> userRoots = root.findElements(By.xpath("./a"));

        return userRoots;
    }

    protected WebElement getNameElement() {
        return driver.findElement(By.xpath(getXPathToName()));
    }

    protected WebElement getProjectsLinks() {
        return driver.findElement(By.xpath(getXPathToProjectsLinks()));
    }

    protected String getBaseXPath() {
        return baseXPath;
    }

    private String getXPathToName() {
        return getBaseXPath() + "";
    }

    private String getXPathToProjectsLinks() {
        return getBaseXPath() + "/span[2]";
    }
}
