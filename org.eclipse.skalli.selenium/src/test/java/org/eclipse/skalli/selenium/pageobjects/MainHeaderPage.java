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
 * This page object contains the {@link org.openqa.selenium.WebElement}s every page
 * contains
 */
public class MainHeaderPage extends AbstractPage {
    @FindBy(how = How.CLASS_NAME, using = "mainheader")
    private WebElement mainheader;

    @FindBy(how = How.CLASS_NAME, using = "mainheader-left")
    private WebElement mainheaderLeft;

    @FindBy(how = How.CLASS_NAME, using = "mainheader-right")
    private WebElement mainheaderRight;

    public MainHeaderPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return getMainheader().isDisplayed() && getMainheaderLeft().isDisplayed() && getMainheaderRight().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getMainheader();
    }

    protected WebElement getMainheader() {
        return mainheader;
    }

    protected WebElement getMainheaderLeft() {
        return mainheaderLeft;
    }

    protected WebElement getMainheaderRight() {
        return mainheaderRight;
    }
}
