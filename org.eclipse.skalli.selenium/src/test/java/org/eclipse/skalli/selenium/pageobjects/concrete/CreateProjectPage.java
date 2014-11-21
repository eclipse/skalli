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
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the create project page
 *
 * The create project page is a page with search field, navigation bar and the create project page
 * specific elements ("checkboxes" for a free style project or component and buttons like "Create" and "Cancel")
 */
public class CreateProjectPage extends SearchAndNavigationbarPage {
    private static final String AVAILABLE_PROJECT_TEMPLATES_CAPTION = "Available Project Templates";

    @FindBy(how = How.XPATH, using = ".//*[@id='templateSelectPanelContent']/div/div[1]/div/div/div/div/div[1]/h2")
    private WebElement caption;

    //                                                                                                                                      number of the lamp
    @FindBy(how = How.XPATH, using = "//div[@id='templateSelectPanelContent']/div/div[1]/div/div/div/div/div[3]/div/div/div/div/div/div/div[1]/div/div/span/img")
    private WebElement freeStyleProjectButton;

    //                                                                                                                                      number of the lamp
    @FindBy(how = How.XPATH, using = "//div[@id='templateSelectPanelContent']/div/div[1]/div/div/div/div/div[3]/div/div/div/div/div/div/div[2]/div/div/span/img")
    private WebElement freeStyleComponentButton;

    @FindBy(how = How.XPATH, using = "//div[@id='templateSelectPanelContent']/div/div[2]/div/div/div/div/div[1]")
    private WebElement createProjectButton;

    @FindBy(how = How.XPATH, using = "//div[@id='templateSelectPanelContent']/div/div[2]/div/div/div/div/div[2]")
    private WebElement cancelButton;

    public CreateProjectPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getCaption().isDisplayed()
                && getCaption().getText().contains(AVAILABLE_PROJECT_TEMPLATES_CAPTION)
                && getFreeStyleProjectButton().isDisplayed() && getFreeStyleComponentButton().isDisplayed()
                && getCreateProjectButton().isDisplayed() && getCancelButton().isDisplayed();
    }

    public void clickFreeStyleProjectButton() {
        getFreeStyleProjectButton().click();
    }

    public void clickFreeStyleComponentButton() {
        getFreeStyleComponentButton().click();
    }

    public void clickCreateProjectButton() {
        getCreateProjectButton().click();
    }

    public void clickCancelButton() {
        getCancelButton().click();
    }

    protected WebElement getCaption() {
        return caption;
    }

    protected WebElement getFreeStyleProjectButton() {
        return freeStyleProjectButton;
    }

    protected WebElement getFreeStyleComponentButton() {
        return freeStyleComponentButton;
    }

    protected WebElement getCreateProjectButton() {
        return createProjectButton;
    }

    protected WebElement getCancelButton() {
        return cancelButton;
    }
}
