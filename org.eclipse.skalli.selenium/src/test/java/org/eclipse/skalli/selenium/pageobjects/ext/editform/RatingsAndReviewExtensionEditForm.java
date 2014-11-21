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
package org.eclipse.skalli.selenium.pageobjects.ext.editform;

import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.PositionProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is the extension edit form for the ratings and reviews extension
 */
public class RatingsAndReviewExtensionEditForm extends AbstractExtensionEditForm {
    public RatingsAndReviewExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getRatingsAndReviewExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return getRatingStyleField().isDisplayed() && getRatingStyleSelectionButton().isDisplayed()
                && getAllowAnonymusReviewsCheckBox().isDisplayed();
    }

    //TODO find a safe way to send keys to a field with a selection button
    public void sendKeysToRatingStyleField(String text, boolean click) {
        getRatingStyleField().sendKeys(text + "\n");

        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

        if (click) {
            getRatingStyleField().click();
        }
    }

    public String getRatingStyleFieldContent() {
        return getRatingStyleField().getAttribute("value");
    }

    public void clickRatingStyleSelectionButton() {
        getRatingStyleSelectionButton().click();
    }

    public void checkAllowAnonymusReviewsCheckBox(boolean checked) {
        WebElement allowAnonymusReviewsCheckBox = getAllowAnonymusReviewsCheckBox();

        if (checked && !allowAnonymusReviewsCheckBox.isSelected()) {
            allowAnonymusReviewsCheckBox.click();
        }

        if (!checked && allowAnonymusReviewsCheckBox.isSelected()) {
            allowAnonymusReviewsCheckBox.click();
        }
    }

    public boolean isAllowAnonymusReviewsCheckBoxChecked() {
        return getAllowAnonymusReviewsCheckBox().isSelected();
    }

    protected WebElement getRatingStyleField() {
        return driver.findElement(By.xpath(getXPathToRatingStyleField()));
    }

    protected WebElement getRatingStyleSelectionButton() {
        return driver.findElement(By.xpath(getXPathToRatingStyleSelectionButton()));
    }

    protected WebElement getAllowAnonymusReviewsCheckBox() {
        return driver.findElement(By.xpath(getXPathToAllowAnonymusReviewsCheckBox()));
    }

    private String getXPathToRatingStyleField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]" + "/div/input";
    }

    private String getXPathToRatingStyleSelectionButton() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]" + "/div/div";
    }

    private String getXPathToAllowAnonymusReviewsCheckBox() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]" + "/span/input";
    }
}
