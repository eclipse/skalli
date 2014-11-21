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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.PositionProvider;
import org.eclipse.skalli.selenium.pageobjects.ext.util.RemovableFieldEntry;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * This is the extension edit form for the info extension
 */
public class InfoExtensionEditForm extends AbstractExtensionEditForm {
    public InfoExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getInfoExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return getProjectHomepageField().isDisplayed() && getMailingListsSection().isDisplayed()
                && getAddMailingListsLink().isDisplayed() && areMailingListsDisplayed();
    }

    private boolean areMailingListsDisplayed() {
        List<RemovableFieldEntry> mailingListEntries = getMailingsListEntries();

        for (Iterator<RemovableFieldEntry> iterator = mailingListEntries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry mailingListEntry = (RemovableFieldEntry) iterator.next();
            if (!mailingListEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    //driver must be located on the edit form
    public int getNumberOfMailingLists() {
        WebElement root = driver.findElement(By.xpath(getXPathToMailingListsSection()));

        List<WebElement> mailingListsRoots = root.findElements(By.xpath("./div"));

        return mailingListsRoots.size();
    }

    //driver must be located on the edit form
    public List<RemovableFieldEntry> getMailingsListEntries() {
        List<RemovableFieldEntry> users = new ArrayList<RemovableFieldEntry>();

        for (int i = 1; i <= getNumberOfMailingLists(); i++) {
            users.add(PageFactory.initElements(driver, RemovableFieldEntry.class).setBaseXPath(
                    getXPathToMailingListsSection() + "/div[" + i + "]/div/div"));
        }

        return users;
    }

    public String getProjectHomepage() {
        return getProjectHomepageField().getAttribute("value");
    }

    public void sendKeysToProjectHomepageField(String text) {
        WebElement projectHomepageField = getProjectHomepageField();
        projectHomepageField.click();
        projectHomepageField.sendKeys(text);
    }

    public void clickAddMailingListLink() {
        getAddMailingListsLink().click();
    }

    //numberOfMailingListLink starting from 0 (first index)
    public void clickRemoveMailingListLink(int numberOfMailingListLink) {
        List<RemovableFieldEntry> mailingListEntries = getMailingsListEntries();
        RemovableFieldEntry mailingListEntry = mailingListEntries.get(numberOfMailingListLink);

        mailingListEntry.clickRemoveLink();
    }

    //numberOfMailingListLink starting from 0 (first index)
    public void sendKeysToMailingListField(String text, int numberOfMailingListLink) {
        List<RemovableFieldEntry> mailingListEntries = getMailingsListEntries();
        RemovableFieldEntry mailingListEntry = mailingListEntries.get(numberOfMailingListLink);

        mailingListEntry.sendKeysToField(text);
    }

    //numberOfMailingListLink starting from 0 (first index)
    public String getMailingListLink(int numberOfMailingListLink) {
        List<RemovableFieldEntry> mailingListEntries = getMailingsListEntries();
        RemovableFieldEntry mailingListEntry = mailingListEntries.get(numberOfMailingListLink);

        return mailingListEntry.getFieldContent();
    }

    protected WebElement getProjectHomepageField() {
        return driver.findElement(By.xpath(getXPathToProjectHomepageField()));
    }

    protected WebElement getMailingListsSection() {
        return driver.findElement(By.xpath(getXPathToMailingListsSection()));
    }

    protected WebElement getAddMailingListsLink() {
        return driver.findElement(By.xpath(getXPathToAddMailingListLink()));
    }

    private String getXPathToProjectHomepageField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/input";
    }

    private String getXPathToMailingListsSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/div/div/div/div";
    }

    private String getXPathToAddMailingListLink() {
        return getXPathToMailingListsSection() + "/div[" + getNumberOfMailingLists() + "]/div/div" + "/div["
                //    1 if number is 1 because there is no remove link displayed
                //    2 if number is greater than 1 because the remove link is displayed
                + (getNumberOfMailingLists() == 1 ? 1 : 2) + "]/span/span";
    }
}
