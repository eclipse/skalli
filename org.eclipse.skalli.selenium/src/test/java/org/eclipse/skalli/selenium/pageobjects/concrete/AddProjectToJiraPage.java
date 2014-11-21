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
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the add project to JIRA page
 *
 * The add project to JIRA page is a page with search field, navigation bar and the
 * add project to JIRA page specific elements (open JIRA dialog on JIRA server link,
 * JIRA project key, checkboxes like use to track bugs and use as a scrum backlog
 * and buttons like save and cancel)
 */
public class AddProjectToJiraPage extends SearchAndNavigationbarPage {
    private static final String ADD_PROJECT_TO_JIRA_CAPTION = "Add Project to JIRA";

    @FindBy(how = How.XPATH, using = "//div[@class=\'projectarearight\']/h3")
    private WebElement caption;

    @FindBy(how = How.PARTIAL_LINK_TEXT, using = "Follow this link to open a JIRA dialog on JIRA server")
    private WebElement jiraDialogOnJiraServerLink;

    @FindBy(how = How.ID, using = "jiraprojectkey")
    private WebElement jiraProjectKeyField;

    @FindBy(how = How.XPATH, using = "//form[@id='jiraform']/table/tbody/tr[2]/td[2]/input[1]")
    private WebElement projectUsedToTrackBugsCheckBox;

    @FindBy(how = How.XPATH, using = "//form[@id='jiraform']/table/tbody/tr[2]/td[2]/input[3]")
    private WebElement projectUsedAsAScrumBacklogCheckBox;

    @FindBy(how = How.XPATH, using = "//input[@class=\'searchsubmit\'][@value=\'Save\']")
    private WebElement saveButton;

    @FindBy(how = How.XPATH, using = "//input[@class=\'searchsubmit\'][@value=\'Cancel\']")
    private WebElement cancelButton;

    public AddProjectToJiraPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed() && getCaption().isDisplayed()
                && getCaption().getText().contains(ADD_PROJECT_TO_JIRA_CAPTION)
                && getJiraDialogOnJiraServerLink().isDisplayed() && getJiraProjectKeyField().isDisplayed()
                && getProjectUsedToTrackBugsCheckBox().isDisplayed()
                && getProjectUsedAsAScrumBacklogCheckBox().isDisplayed()
                && getSaveButton().isDisplayed() && getCancelButton().isDisplayed();
    }

    public void clickJiraDialogOnJiraServerLink() {
        getJiraDialogOnJiraServerLink().click();
    }

    public void sendKeysToJiraProjectKeyField(String text) {
        getJiraProjectKeyField().sendKeys(text);
    }

    public void checkProjectUsedToTrackBugsCheckBox(boolean checked) {
        if (checked && !getProjectUsedToTrackBugsCheckBox().isSelected()) {
            getProjectUsedToTrackBugsCheckBox().click();
        }

        if (!checked && getProjectUsedToTrackBugsCheckBox().isSelected()) {
            getProjectUsedToTrackBugsCheckBox().click();
        }
    }

    public void checkProjectUsedAsAScrumBacklogCheckBox(boolean checked) {
        if (checked && !getProjectUsedAsAScrumBacklogCheckBox().isSelected()) {
            getProjectUsedAsAScrumBacklogCheckBox().click();
        }

        if (!checked && getProjectUsedAsAScrumBacklogCheckBox().isSelected()) {
            getProjectUsedAsAScrumBacklogCheckBox().click();
        }
    }

    public void clickSaveButton() {
        getSaveButton().click();
    }

    public void clickCancelButton() {
        getCancelButton().click();
    }

    protected WebElement getCaption() {
        return caption;
    }

    protected WebElement getJiraDialogOnJiraServerLink() {
        return jiraDialogOnJiraServerLink;
    }

    protected WebElement getJiraProjectKeyField() {
        return jiraProjectKeyField;
    }

    protected WebElement getProjectUsedToTrackBugsCheckBox() {
        return projectUsedToTrackBugsCheckBox;
    }

    protected WebElement getProjectUsedAsAScrumBacklogCheckBox() {
        return projectUsedAsAScrumBacklogCheckBox;
    }

    protected WebElement getSaveButton() {
        return saveButton;
    }

    protected WebElement getCancelButton() {
        return cancelButton;
    }
}
