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
 * This is the extension edit form for the development infrastructure extension
 */
public class DevelopmentInfrastructureExtensionEditForm extends AbstractExtensionEditForm {
    public DevelopmentInfrastructureExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getDevelopmentInfrastructureExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return getSourceCodeField().isDisplayed() && getBugTrackerField().isDisplayed()
                && getBuildField().isDisplayed()
                && getQualityField().isDisplayed() && getCodeReviewField().isDisplayed()
                && getRepositoriesSection().isDisplayed() && getJavadocsSection().isDisplayed()
                && getAddRepositoryLink().isDisplayed() && getAddJavadocLink().isDisplayed()
                && areRepositoriesDisplayed()
                && areJavadocsDisplayed();
    }

    private boolean areRepositoriesDisplayed() {
        List<RemovableFieldEntry> repositoryEntries = getRepositoryEntries();

        for (Iterator<RemovableFieldEntry> iterator = repositoryEntries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry repositoryEntry = (RemovableFieldEntry) iterator.next();
            if (!repositoryEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    private boolean areJavadocsDisplayed() {
        List<RemovableFieldEntry> javadocEntries = getJavadocEntries();

        for (Iterator<RemovableFieldEntry> iterator = javadocEntries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry javadocEntry = (RemovableFieldEntry) iterator.next();
            if (!javadocEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    public void sendKeysToSourceCodeField(String text) {
        getSourceCodeField().sendKeys(text);
    }

    public void sendKeysToBugTrackerField(String text) {
        getBugTrackerField().sendKeys(text);
    }

    public void sendKeysToBuildField(String text) {
        getBuildField().sendKeys(text);
    }

    public void sendKeysToQualityField(String text) {
        getQualityField().sendKeys(text);
    }

    public void sendKeysToCodeReviewField(String text) {
        getCodeReviewField().sendKeys(text);
    }

    public String getSourceCodeFieldContent() {
        return getSourceCodeField().getAttribute("value");
    }

    public String getBugTrackerFieldContent() {
        return getBugTrackerField().getAttribute("value");
    }

    public String getBuildFieldContent() {
        return getBuildField().getAttribute("value");
    }

    public String getQualityFieldContent() {
        return getQualityField().getAttribute("value");
    }

    public String getCodeReviewFieldContent() {
        return getCodeReviewField().getAttribute("value");
    }

    //driver must be located on the edit form
    public int getNumberOfRepositories() {
        WebElement root = driver.findElement(By.xpath(getXPathToRepositoriesSection()));

        List<WebElement> repositoriesRoot = root.findElements(By.xpath("./div"));

        return repositoriesRoot.size();
    }

    //driver must be located on the edit form
    public List<RemovableFieldEntry> getRepositoryEntries() {
        List<RemovableFieldEntry> repositories = new ArrayList<RemovableFieldEntry>();

        for (int i = 1; i <= getNumberOfRepositories(); i++) {
            repositories.add(PageFactory.initElements(driver, RemovableFieldEntry.class).setBaseXPath(
                    getXPathToRepositoriesSection() + "/div[" + i + "]/div/div"));
        }

        return repositories;
    }

    //numberOfRepository starting from 0 (first index)
    public void clickRemoveRepositoryLink(int numberOfRepository) {
        List<RemovableFieldEntry> repositoryEntries = getRepositoryEntries();
        RemovableFieldEntry repositoryEntry = repositoryEntries.get(numberOfRepository);

        repositoryEntry.clickRemoveLink();
    }

    //numberOfRepository starting from 0 (first index)
    public void sendKeysToRepositoryField(String text, int numberOfRepository) {
        List<RemovableFieldEntry> repositoryEntries = getRepositoryEntries();
        RemovableFieldEntry repositoryEntry = repositoryEntries.get(numberOfRepository);

        repositoryEntry.sendKeysToField(text);
    }

    //numberOfRepository starting from 0 (first index)
    public String getRepository(int numberOfRepository) {
        List<RemovableFieldEntry> repositoryEntries = getRepositoryEntries();
        RemovableFieldEntry repositoryEntry = repositoryEntries.get(numberOfRepository);

        return repositoryEntry.getFieldContent();
    }

    //driver must be located on the edit form
    public int getNumberOfJavadocs() {
        WebElement root = driver.findElement(By.xpath(getXPathToJavadocsSection()));

        List<WebElement> javadocRoot = root.findElements(By.xpath("./div"));

        return javadocRoot.size();
    }

    //driver must be located on the edit form
    public List<RemovableFieldEntry> getJavadocEntries() {
        List<RemovableFieldEntry> javadocs = new ArrayList<RemovableFieldEntry>();

        for (int i = 1; i <= getNumberOfJavadocs(); i++) {
            javadocs.add(PageFactory.initElements(driver, RemovableFieldEntry.class).setBaseXPath(
                    getXPathToJavadocsSection() + "/div[" + i + "]/div/div"));
        }

        return javadocs;
    }

    //numberOfJavadoc starting from 0 (first index)
    public void clickRemoveJavadocsLink(int numberOfJavadoc) {
        List<RemovableFieldEntry> javadocEntries = getJavadocEntries();
        RemovableFieldEntry javadocEntry = javadocEntries.get(numberOfJavadoc);

        javadocEntry.clickRemoveLink();
    }

    //numberOfJavadoc starting from 0 (first index)
    public void sendKeysToJavadocField(String text, int numberOfJavadoc) {
        List<RemovableFieldEntry> javadocEntries = getJavadocEntries();
        RemovableFieldEntry javadocEntry = javadocEntries.get(numberOfJavadoc);

        javadocEntry.sendKeysToField(text);
    }

    //numberOfJavadoc starting from 0 (first index)
    public String getJavadoc(int numberOfJavadoc) {
        List<RemovableFieldEntry> javadocEntries = getJavadocEntries();
        RemovableFieldEntry javadocEntry = javadocEntries.get(numberOfJavadoc);

        return javadocEntry.getFieldContent();
    }

    public void clickAddRepositoryLink() {
        getAddRepositoryLink().click();
    }

    public void clickAddJavadocLink() {
        getAddJavadocLink().click();
    }

    protected WebElement getSourceCodeField() {
        return driver.findElement(By.xpath(getXPathToSourceCodeField()));
    }

    protected WebElement getBugTrackerField() {
        return driver.findElement(By.xpath(getXPathToBugTrackerField()));
    }

    protected WebElement getBuildField() {
        return driver.findElement(By.xpath(getXPathToBuildField()));
    }

    protected WebElement getQualityField() {
        return driver.findElement(By.xpath(getXPathToQualityField()));
    }

    protected WebElement getCodeReviewField() {
        return driver.findElement(By.xpath(getXPathToCodeReviewField()));
    }

    protected WebElement getRepositoriesSection() {
        return driver.findElement(By.xpath(getXPathToRepositoriesSection()));
    }

    protected WebElement getJavadocsSection() {
        return driver.findElement(By.xpath(getXPathToJavadocsSection()));
    }

    protected WebElement getAddRepositoryLink() {
        return driver.findElement(By.xpath(getXPathToAddRepositoryLink()));
    }

    protected WebElement getAddJavadocLink() {
        return driver.findElement(By.xpath(getXPathToAddJavadocLink()));
    }

    private String getXPathToSourceCodeField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/input";
    }

    private String getXPathToBugTrackerField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 3
                + "]/td[3]/input";
    }

    private String getXPathToBuildField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 4
                + "]/td[3]/input";
    }

    private String getXPathToQualityField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 5
                + "]/td[3]/input";
    }

    private String getXPathToCodeReviewField() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 6
                + "]/td[3]/input";
    }

    private String getXPathToRepositoriesSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/div/div/div/div";
    }

    private String getXPathToJavadocsSection() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 7
                + "]/td[3]/div/div/div/div";
    }

    private String getXPathToAddRepositoryLink() {
        return getXPathToRepositoriesSection() + "/div[" + getNumberOfRepositories() + "]/div/div" + "/div["
                //    1 if number is 1 because there is no remove link displayed
                //    2 if number is greater than 1 because the remove link is displayed
                + (getNumberOfRepositories() == 1 ? 1 : 2) + "]/span/span";
    }

    private String getXPathToAddJavadocLink() {
        return getXPathToJavadocsSection() + "/div[" + getNumberOfJavadocs() + "]/div/div" + "/div["
                //    1 if number is 1 because there is no remove link displayed
                //    2 if number is greater than 1 because the remove link is displayed
                + (getNumberOfJavadocs() == 1 ? 1 : 2) + "]/span/span";
    }
}
