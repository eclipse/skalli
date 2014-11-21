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
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the create git/gerrit page
 *
 * The create git/gerrit page is a page with search field, navigation bar and the
 * create git/gerrit page specific elements
 */
public class CreateGitGerritLinkPage extends SearchAndNavigationbarPage {
    //implement (think of the differences between git/gerrit server being configured and being not configured)
    public CreateGitGerritLinkPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed();
    }
}
