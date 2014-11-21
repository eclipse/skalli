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
 * This is the extension edit form for the project members extension
 */
public class ProjectMembersExtensionEditForm extends AbstractExtensionEditForm {
    public ProjectMembersExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getProjectMembersExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        //project leads user section and committers user section element can be a closed tag -> isDisplayed returns false
        return getProjectLeadsAddUserLink().isDisplayed() && getCommittersAddUserLink().isDisplayed()
                && (getProjectLeadsUsersSection() instanceof RemoteWebElement)
                && (getCommittersUsersSection() instanceof RemoteWebElement) && areProjectLeadUsersDisplayed()
                && areCommitterUsersDisplayed();
    }

    private boolean areProjectLeadUsersDisplayed() {
        List<RemovableUserEntry> projectLeadUserEntries = getProjectLeadUsers();

        for (Iterator<RemovableUserEntry> iterator = projectLeadUserEntries.iterator(); iterator.hasNext();) {
            RemovableUserEntry projectLeadUserEntry = (RemovableUserEntry) iterator.next();
            if (!projectLeadUserEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    private boolean areCommitterUsersDisplayed() {
        List<RemovableUserEntry> committerUserEntries = getCommittersUsers();

        for (Iterator<RemovableUserEntry> iterator = committerUserEntries.iterator(); iterator.hasNext();) {
            RemovableUserEntry committerUserEntry = (RemovableUserEntry) iterator.next();
            if (!committerUserEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    //driver must be located on the edit form
    public int getNumberOfProjectLeadUsers() {
        WebElement root = driver.findElement(By.xpath(getXPathToProjectLeadsUsersSection()));

        List<WebElement> userRoots = root.findElements(By.xpath("./tr"));

        return userRoots.size();
    }

    //driver must be located on the edit form
    public List<RemovableUserEntry> getProjectLeadUsers() {
        List<RemovableUserEntry> users = new ArrayList<RemovableUserEntry>();

        for (int i = 1; i <= getNumberOfProjectLeadUsers(); i++) {
            users.add(PageFactory.initElements(driver, RemovableUserEntry.class).setBaseXPath(
                    getXPathToProjectLeadsUsersSection() + "/tr[" + i + "]/td[1]/div/div/div/div/div/div"));
        }

        return users;
    }

    //driver must be located on the edit form
    public int getNumberOfCommittersUsers() {
        WebElement root = driver.findElement(By.xpath(getXPathToCommittersUsersSection()));

        List<WebElement> userRoots = root.findElements(By.xpath("./tr"));

        return userRoots.size();
    }

    //driver must be located on the edit form
    public List<RemovableUserEntry> getCommittersUsers() {
        List<RemovableUserEntry> users = new ArrayList<RemovableUserEntry>();

        for (int i = 1; i <= getNumberOfCommittersUsers(); i++) {
            users.add(PageFactory.initElements(driver, RemovableUserEntry.class).setBaseXPath(
                    getXPathToCommittersUsersSection() + "/tr[" + i + "]/td[1]/div/div/div/div/div/div"));
        }

        return users;
    }

    public void clickProjectLeadsAddUserLink() {
        getProjectLeadsAddUserLink().click();
    }

    public void clickCommittersAddUserLink() {
        getCommittersAddUserLink().click();
    }

    protected WebElement getProjectLeadsAddUserLink() {
        return driver.findElement(By.xpath(getXPathToProjectLeadsAddUserLink()));
    }

    protected WebElement getCommittersAddUserLink() {
        return driver.findElement(By.xpath(getXPathToCommittersAddUserLink()));
    }

    protected WebElement getProjectLeadsUsersSection() {
        return driver.findElement(By.xpath(getXPathToProjectLeadsUsersSection()));
    }

    protected WebElement getCommittersUsersSection() {
        return driver.findElement(By.xpath(getXPathToCommittersUsersSection()));
    }

    private String getXPathToProjectLeadsAddUserLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/div/div/div/div[" + 2 + "]" + "/div/div/span/span";
    }

    private String getXPathToCommittersAddUserLink() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/div/div/div/div[" + 2 + "]" + "/div/div/span/span";
    }

    private String getXPathToProjectLeadsUsersSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/div/div/div/div[" + 1 + "]" + "/div/div/div[2]/div/table/tbody";
    }

    private String getXPathToCommittersUsersSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/div/div/div/div[" + 1 + "]" + "/div/div/div[2]/div/table/tbody";
    }
}
