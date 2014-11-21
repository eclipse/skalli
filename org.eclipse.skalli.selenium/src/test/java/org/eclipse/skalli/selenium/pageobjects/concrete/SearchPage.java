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

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the search page
 *
 * The search page is a page with search field, navigation bar and the search page specific
 * elements (search results, results per page)
 */
public class SearchPage extends SearchAndNavigationbarPage {
    //implement results per page (occurs if the search result contains "a lot of" entries)
    //implement project (occurs if the search found a project) [project -> projectlink / favicon / projectdetails -> projectlinks]
    public SearchPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed();
    }
}
