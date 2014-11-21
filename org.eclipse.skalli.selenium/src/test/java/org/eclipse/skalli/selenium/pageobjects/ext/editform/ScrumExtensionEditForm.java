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
import org.eclipse.skalli.selenium.pageobjects.ext.util.RemovableUserEntry;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * This is the extension edit form for the scrum extension
 */
//###could be extended from ProjectMembersExtensionEditForm -> the methods would be called different
//###Project Leads = Scrum Masters
//###Committers = Product Owners
//###Backlog field is the only thing which is added to ProjectMembersExtensionEditForm
public class ScrumExtensionEditForm extends AbstractExtensionEditForm {
    public ScrumExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getScrumExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        //scrum masters user section and product owners user section element can be a closed tag -> isDisplayed returns false
        return getScrumMastersAddUserLink().isDisplayed() && getProductOwnersAddUserLink().isDisplayed()
                && (getScrumMastersUsersSection() instanceof RemoteWebElement)
                && (getProductOwnersUsersSection() instanceof RemoteWebElement) && getBacklogField().isDisplayed()
                && areScrumMasterUsersDisplayed() && areProductOwnersUsersDisplayed();
    }

    private boolean areScrumMasterUsersDisplayed() {
        List<RemovableUserEntry> scrumMasterUserEntries = getScrumMastersUsers();

        for (Iterator<RemovableUserEntry> iterator = scrumMasterUserEntries.iterator(); iterator.hasNext();) {
            RemovableUserEntry scrumMasterUserEntry = (RemovableUserEntry) iterator.next();
            if (!scrumMasterUserEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    private boolean areProductOwnersUsersDisplayed() {
        List<RemovableUserEntry> productOwnersUserEntries = getProductOwnersUsers();

        for (Iterator<RemovableUserEntry> iterator = productOwnersUserEntries.iterator(); iterator.hasNext();) {
            RemovableUserEntry productOwnersUserEntry = (RemovableUserEntry) iterator.next();
            if (!productOwnersUserEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    //driver must be located on the edit form
    public int getNumberOfScrumMastersUsers() {
        WebElement root = driver.findElement(By.xpath(getXPathToScrumMastersUsersSection()));

        List<WebElement> userRoots = root.findElements(By.xpath("./tr"));

        return userRoots.size();
    }

    //driver must be located on the edit form
    public List<RemovableUserEntry> getScrumMastersUsers() {
        List<RemovableUserEntry> users = new ArrayList<RemovableUserEntry>();

        for (int i = 1; i <= getNumberOfScrumMastersUsers(); i++) {
            users.add(PageFactory.initElements(driver, RemovableUserEntry.class).setBaseXPath(
                    getXPathToScrumMastersUsersSection() + "/tr[" + i + "]/td[1]/div/div/div/div/div/div"));
        }

        return users;
    }

    //driver must be located on the edit form
    public int getNumberOfProductOwnersUsers() {
        WebElement root = driver.findElement(By.xpath(getXPathToProductOwnersUsersSection()));

        List<WebElement> userRoots = root.findElements(By.xpath("./tr"));

        return userRoots.size();
    }

    //driver must be located on the edit form
    public List<RemovableUserEntry> getProductOwnersUsers() {
        List<RemovableUserEntry> users = new ArrayList<RemovableUserEntry>();

        for (int i = 1; i <= getNumberOfProductOwnersUsers(); i++) {
            users.add(PageFactory.initElements(driver, RemovableUserEntry.class).setBaseXPath(
                    getXPathToProductOwnersUsersSection() + "/tr[" + i + "]/td[1]/div/div/div/div/div/div"));
        }

        return users;
    }

    public void clickScrumMastersAddUserLink() {
        getScrumMastersAddUserLink().click();
    }

    public void clickProductOwnersAddUserLink() {
        getProductOwnersAddUserLink().click();
    }

    public void sendKeysToBacklogField(String text) {
        getBacklogField().sendKeys(text);
    }

    public String getBacklogFieldContent() {
        return getBacklogField().getAttribute("value");
    }

    protected WebElement getScrumMastersAddUserLink() {
        return driver.findElement(By.xpath(getXPathToScrumMastersAddUserLink()));
    }

    protected WebElement getProductOwnersAddUserLink() {
        return driver.findElement(By.xpath(getXPathToProductOwnersAddUserLink()));
    }

    protected WebElement getScrumMastersUsersSection() {
        return driver.findElement(By.xpath(getXPathToScrumMastersUsersSection()));
    }

    protected WebElement getProductOwnersUsersSection() {
        return driver.findElement(By.xpath(getXPathToProductOwnersUsersSection()));
    }

    protected WebElement getBacklogField() {
        return driver.findElement(By.xpath(getXPathToBacklogField()));
    }

    private String getXPathToScrumMastersAddUserLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/div/div/div/div[" + 2 + "]" + "/div/div/span/span";
    }

    private String getXPathToProductOwnersAddUserLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/div/div/div/div[" + 2 + "]" + "/div/div/span/span";
    }

    private String getXPathToScrumMastersUsersSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/div/div/div/div[" + 1 + "]" + "/div/div/div[2]/div/table/tbody";
    }

    private String getXPathToProductOwnersUsersSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/div/div/div/div[" + 1 + "]" + "/div/div/div[2]/div/table/tbody";
    }

    private String getXPathToBacklogField() {
        return getXPathToExtensionContainer()
                + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[3]/td[3]/input";
    }
}
