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

import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.PositionProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is the extension edit form for the maven extension
 */
public class MavenExtensionEditForm extends AbstractExtensionEditForm {
    public MavenExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getMavenExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return getReactorPomPathField().isDisplayed() && getMavenSiteField().isDisplayed();
    }

    public void sendKeysToReactorPomPathField(String text) {
        getReactorPomPathField().sendKeys(text);
    }

    public void sendKeysToMavenSiteField(String text) {
        getMavenSiteField().sendKeys(text);
    }

    public String getReactorPomPathFieldContent() {
        return getReactorPomPathField().getAttribute("value");
    }

    public String getMavenSiteFieldContent() {
        return getMavenSiteField().getAttribute("value");
    }

    protected WebElement getReactorPomPathField() {
        return driver.findElement(By.xpath(getXPathToReactorPomPathField()));
    }

    protected WebElement getMavenSiteField() {
        return driver.findElement(By.xpath(getXPathToMavenSiteField()));
    }

    private String getXPathToReactorPomPathField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/input";
    }

    private String getXPathToMavenSiteField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/input";
    }
}
