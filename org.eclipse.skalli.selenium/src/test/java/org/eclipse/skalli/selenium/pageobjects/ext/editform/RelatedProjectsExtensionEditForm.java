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
import org.eclipse.skalli.selenium.pageobjects.ext.util.RelatedProjectsExtensionEditFormRelatedProjectEntry;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * This is the extension edit form for the related projects extension
 */
public class RelatedProjectsExtensionEditForm extends AbstractExtensionEditForm {
    public RelatedProjectsExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getRelatedProjectsExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return getCalculateRelatedProjectsCheckBox().isDisplayed() && getRelatedProjectsSection().isDisplayed()
                && getAddRelatedProjectLink().isDisplayed() && areRelatedProjectsDisplayed();
    }

    private boolean areRelatedProjectsDisplayed() {
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> relatedProjectEntries = getRelatedProjectEntries();

        for (Iterator<RelatedProjectsExtensionEditFormRelatedProjectEntry> iterator = relatedProjectEntries.iterator(); iterator
                .hasNext();) {
            RelatedProjectsExtensionEditFormRelatedProjectEntry relatedProjectEntry = (RelatedProjectsExtensionEditFormRelatedProjectEntry) iterator
                    .next();
            if (!relatedProjectEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    //driver must be located on the edit form
    public int getNumberOfRelatedProjects() {
        WebElement root = driver.findElement(By.xpath(getXPathToRelatedProjectsSection()));

        List<WebElement> relatedProjectsRoot = root.findElements(By.xpath("./div"));

        //                                subtract one because there is one hidden div with no functionality
        return relatedProjectsRoot.size() - 1;
    }

    //driver must be located on the edit form
    public List<RelatedProjectsExtensionEditFormRelatedProjectEntry> getRelatedProjectEntries() {
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> users = new ArrayList<RelatedProjectsExtensionEditFormRelatedProjectEntry>();

        for (int i = 1; i <= getNumberOfRelatedProjects(); i++) {
            users.add(PageFactory.initElements(driver, RelatedProjectsExtensionEditFormRelatedProjectEntry.class)
                    .setBaseXPath(getXPathToRelatedProjectsSection() + "/div[" + i + "]/div/div/div"));
        }

        return users;
    }

    //numberOfRelatedProjectLink starting from 0 (first index)
    public void clickRemoveRelatedProjectLink(int numberOfRelatedProjectLink) {
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> relatedProjectEntries = getRelatedProjectEntries();
        RelatedProjectsExtensionEditFormRelatedProjectEntry relatedProject = relatedProjectEntries
                .get(numberOfRelatedProjectLink);

        relatedProject.clickRemoveRelatedProjectsLink();
    }

    //numberOfRelatedProjectLink starting from 0 (first index)
    public void clickRelatedProjectsSelectionButton(int numberOfRelatedProjectLink) {
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> relatedProjectEntries = getRelatedProjectEntries();
        RelatedProjectsExtensionEditFormRelatedProjectEntry relatedProject = relatedProjectEntries
                .get(numberOfRelatedProjectLink);

        relatedProject.clickRelatedProjectsSelectionButton();
    }

    //numberOfRelatedProjectLink starting from 0 (first index)
    public void sendKeysToRelatedProjectField(String text, boolean click, int numberOfRelatedProjectLink) {
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> relatedProjectEntries = getRelatedProjectEntries();
        RelatedProjectsExtensionEditFormRelatedProjectEntry relatedProject = relatedProjectEntries
                .get(numberOfRelatedProjectLink);

        relatedProject.sendKeysToRelatedProjectField(text, click);
    }

    //numberOfRelatedProjectLink starting from 0 (first index)
    public String getRelatedProject(int numberOfRelatedProjectLink) {
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> relatedProjectEntries = getRelatedProjectEntries();
        RelatedProjectsExtensionEditFormRelatedProjectEntry relatedProject = relatedProjectEntries
                .get(numberOfRelatedProjectLink);

        return relatedProject.getRelatedProject();
    }

    public void checkCalculateRelatedProjectCheckBox(boolean checked) {
        WebElement calculateRelatedProjectCheckBox = getCalculateRelatedProjectsCheckBox();

        if (checked && !calculateRelatedProjectCheckBox.isSelected()) {
            calculateRelatedProjectCheckBox.click();
        }

        if (!checked && calculateRelatedProjectCheckBox.isSelected()) {
            calculateRelatedProjectCheckBox.click();
        }
    }

    public boolean isCalculateRelatedProjectCheckBoxChecked() {
        WebElement calculateRelatedProjectCheckBox = getCalculateRelatedProjectsCheckBox();

        return calculateRelatedProjectCheckBox.isSelected();
    }

    public void clickAddRelatedProjectLink() {
        getAddRelatedProjectLink().click();
    }

    protected WebElement getCalculateRelatedProjectsCheckBox() {
        return driver.findElement(By.xpath(getXPathToCalculateRelatedProjectsCheckBox()));
    }

    protected WebElement getRelatedProjectsSection() {
        return driver.findElement(By.xpath(getXPathToRelatedProjectsSection()));
    }

    protected WebElement getAddRelatedProjectLink() {
        return driver.findElement(By.xpath(getXPathToAddRelatedProjectLink()));
    }

    private String getXPathToCalculateRelatedProjectsCheckBox() {
        return getXPathToExtensionContainer()
                + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[1]/td[3]/span/input";
    }

    private String getXPathToRelatedProjectsSection() {
        return getXPathToExtensionContainer()
                + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[2]/td[3]/div/div/div";
    }

    private String getXPathToAddRelatedProjectLink() {
        return getXPathToRelatedProjectsSection() + "/div[" + getNumberOfRelatedProjects() + "]/div/div/div" + "/div["
                //    1 if number is 1 because there is no remove link displayed
                //    2 if number is greater than 1 because the remove link is displayed
                + (getNumberOfRelatedProjects() == 1 ? 2 : 3) + "]/div/div/span/span";
    }
}
