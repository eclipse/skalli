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
package org.eclipse.skalli.selenium.pageobjects.ext;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The abstract extension edit form is the super type of every edit form
 *
 * The abstract extension edit form links like "open", "disable", "inherit" and "edit"
 * and the title of the extensions edit form
 */
public abstract class AbstractExtensionEditForm extends AbstractExtension {
    //base path (xpath) to the extensions
    private static final String BASE_XPATH_TO_PROJECT_EXTENSIONS = "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div";

    //needed because in the buttons are in first "extension location" and a place holder in the second "extension location"
    //subtract 1 because we want to start with zero
    //subtract 1 for the upper buttons area
    //subtract 1 for the upper place holder
    public static final int CONTAINER_EXTENSION_OFFSET = 4;

    //needed because in the buttons are in first "extension location" and a place holder in the second "extension location"
    //subtract 1 because we want to start with zero
    //subtract 1 for the upper buttons area
    //subtract 1 for the upper place holder
    //subtract 1 for the lower place holder
    public static final int LOWER_BUTTON_AREA_OFFSET = 5;

    //subtract 1 because we want to start with zero
    //subtract 1 for the upper buttons area
    //subtract 1 for the upper place holder
    //subtract 1 for the lower buttons area
    //subtract 1 for the lower place holder
    //the rest is the number of extensions displayed
    public static final int EXTENSION_COUNT_DIFF = 6;

    private static final String EXTENSION_OPEN_CLASS_INDICATOR_STRING = "open";

    private static final String EXTENSION_DISABLED_INDICATOR_STRING = "disabled";

    private static final String EXTENSION_INHERITED_INDICATOR_STRING = "inherited";

    private static final String EXTENSION_EDITABLE_INDICATOR_STRING = "editable";

    private int numberOfExtension;

    //numberOfExtension -> first element is indexed with zero
    public AbstractExtensionEditForm(WebDriver driver, int numberOfExtension) {
        super(driver);
        this.numberOfExtension = numberOfExtension;
    }

    @Override
    public boolean isDisplayed() {
        return getEditEditableLink().isDisplayed() && getInheritInheritedLink().isDisplayed()
                && getDisableDisabledLink().isDisplayed() && getShowHideLink().isDisplayed()
                && getDescriptionElement().isDisplayed() && getCaption().isDisplayed()
                && (isEditable() ? isExtensionContentDisplayed() : true);
    }

    //use instead of isDisplayed to verify that the elements of the extension are displayed (only possible if it is editable)
    protected abstract boolean isExtensionContentDisplayed();

    @Override
    protected WebElement explicitWaitReturn() {
        return getCaption();
    }

    @Override
    public String getTitle() {
        return getCaption().getText();
    }

    public String getDescription() {
        return getDescriptionElement().getText();
    }

    public boolean isEditable() {
        return getEditEditableLink().getText().equals(EXTENSION_EDITABLE_INDICATOR_STRING);
    }

    public boolean isInherited() {
        return getInheritInheritedLink().getText().equals(EXTENSION_INHERITED_INDICATOR_STRING);
    }

    public boolean isDisabled() {
        return getDisableDisabledLink().getText().equals(EXTENSION_DISABLED_INDICATOR_STRING);
    }

    public boolean isShown() {
        return getShowHideLink().getAttribute("class").contains(EXTENSION_OPEN_CLASS_INDICATOR_STRING);
    }

    public void clickEditEditableLink() {
        getEditEditableLink().click();
    }

    public void makeExtensionEditable() {
        if (!isEditable()) {
            clickEditEditableLink();
        }
    }

    public void clickInheritInheritedLink() {
        getInheritInheritedLink().click();
    }

    public void makeExtensionInherited() {
        if (!isInherited()) {
            clickInheritInheritedLink();
        }
    }

    public void clickDisableDisabledLink() {
        getDisableDisabledLink().click();
    }

    public void makeExtensionDisabled() {
        if (!isDisabled()) {
            clickDisableDisabledLink();
        }
    }

    public void clickShowHideLink() {
        getShowHideLink().click();
    }

    public void makeExtensionShown() {
        if (!isShown()) {
            clickShowHideLink();
        }
    }

    //driver must be located on the edit form
    public static int getNumberOfExtensions(WebDriver driver) {
        WebElement root = driver.findElement(By.xpath(BASE_XPATH_TO_PROJECT_EXTENSIONS));

        List<WebElement> extensionRoots = root.findElements(By.xpath("./div"));

        return extensionRoots.size() - EXTENSION_COUNT_DIFF;
    }

    protected WebElement getCaption() {
        return driver.findElement(By.xpath(getXPathToCaption()));
    }

    protected WebElement getDescriptionElement() {
        return driver.findElement(By.xpath(getXPathToDescription()));
    }

    protected WebElement getShowHideLink() {
        return driver.findElement(By.xpath(getXPathToShowHideLink()));
    }

    protected WebElement getDisableDisabledLink() {
        return driver.findElement(By.xpath(getXPathToDisableDisabledLink()));
    }

    protected WebElement getInheritInheritedLink() {
        return driver.findElement(By.xpath(getXPathToInheritInheritedLink()));
    }

    protected WebElement getEditEditableLink() {
        return driver.findElement(By.xpath(getXPathToEditEditableLink()));
    }

    protected String getXPathToExtensionContainer() {
        return BASE_XPATH_TO_PROJECT_EXTENSIONS + "/div[" + (this.numberOfExtension + CONTAINER_EXTENSION_OFFSET) + "]";
    }

    private String getXPathToEditEditableLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[1]/div/div[" + 3 + "]/div/div/span/span";
    }

    private String getXPathToInheritInheritedLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[1]/div/div[" + 4 + "]/div/div/span/span";
    }

    private String getXPathToDisableDisabledLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[1]/div/div[" + 5 + "]/div/div/span/span";
    }

    private String getXPathToShowHideLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[1]/div/div[" + 6 + "]/div/button";
    }

    private String getXPathToDescription() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[2]/div/div/div";
    }

    private String getXPathToCaption() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[1]/div/div[2]/div/div";
    }
}
