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
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the all projects page
 *
 * The all projects page is a page with search field, navigation bar and the all projects page
 * specific elements (the tree)
 */
public class AllProjectsViewPage extends SearchAndNavigationbarPage {
    //implement the tree
    public AllProjectsViewPage(WebDriver driver) {
        super(driver);
    }
}
