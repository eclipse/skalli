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

import org.openqa.selenium.WebDriver;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the my projects page
 *
 * The my projects page is simply a search page where the name is searched
 */
public class MyProjectsPage extends SearchPage {
    public MyProjectsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed();
    }
}
