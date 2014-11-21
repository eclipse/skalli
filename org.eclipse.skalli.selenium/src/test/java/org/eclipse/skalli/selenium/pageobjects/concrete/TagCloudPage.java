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
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the tag cloud page
 *
 * The tag cloud page is a page with search field, navigation bar and the tag cloud specific
 * elements (tag cloud itself)
 */
public class TagCloudPage extends SearchAndNavigationbarPage {
    @FindBy(how = How.CLASS_NAME, using = "tagcloud")
    private WebElement tagCloud;

    public TagCloudPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getTagCloud().isDisplayed();
    }

    protected WebElement getTagCloud() {
        return tagCloud;
    }
}
