/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.selenium.tests.simple;

import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.ProjectMembersExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.AddUserForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.RemovableUserEntry;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class EditPageProjectMembersExtensionEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static ProjectMembersExtensionEditForm editForm;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        initializePageObjects();

        mainPage.isDisplayedWithExplicitWait();

        //navigate to the edit page
        mainPage.clickCreateProjectLink();
        createProjectPage.isDisplayedWithExplicitWait();
        createProjectPage.clickCreateProjectButton();

        editPage.isDisplayedWithExplicitWait();

        //initialize extensions
        editForm = PageFactory.initElements(driver, ProjectMembersExtensionEditForm.class);
    }

    private static void initializePageObjects() {
        mainPage = PageFactory.initElements(driver, MainPage.class);
        createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        editPage = PageFactory.initElements(driver, EditPage.class);
    }

    @Before
    public void setup() {
        //checks if the edit page is displayed before every test
        editPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void projectMembersEditFormProjectLeadsAddUserLinkTest() {
        //add a project lead
        editForm.clickProjectLeadsAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        //close the form
        addUserForm.clickCloseButton();
    }

    @Test
    public void projectMembersEditFormCommittersAddUserLinkTest() {
        //add a committer
        editForm.clickCommittersAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        //close the form
        addUserForm.clickCloseButton();
    }

    @Test
    public void projectMembersEditFormProjectLeadsUserNumberTest() {
        Assert.assertTrue("project leads user number differs", editForm.getNumberOfProjectLeadUsers() == 1);
    }

    @Test
    public void projectMembersEditFormCommittersUserNumberTest() {
        Assert.assertTrue("committers user number differs", editForm.getNumberOfCommittersUsers() == 0);
    }

    @Test
    public void projectMembersEditFormProjectLeadsAddUserTest() {
        //add a project lead
        editForm.clickProjectLeadsAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        //search for j
        addUserForm.sendKeysToSearchForField("j");
        addUserForm.submitSearchForContent();

        //select the first entry found by the search
        addUserForm.getSelect().selectByIndex(0);

        //click add and close button of the form
        addUserForm.clickAddAndCloseButton();
    }

    @Test
    public void projectMembersEditFormCommittersAddUserTest() {
        //add a committer
        editForm.clickCommittersAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        //search for j
        addUserForm.sendKeysToSearchForField("j");
        addUserForm.submitSearchForContent();

        //select the first entry found by the search
        addUserForm.getSelect().selectByIndex(0);

        //click add and close button of the form
        addUserForm.clickAddAndCloseButton();
    }

    @Test
    public void projectMembersEditFormCommittersAddMultipleUsersTest() {
        //click add committer
        editForm.clickCommittersAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        for (int i = 0; i < 3; i++) {
            //search for j
            addUserForm.sendKeysToSearchForField("j");
            addUserForm.submitSearchForContent();

            //select entry 0, 1, 2 and...
            Select select = addUserForm.getSelect();
            select.selectByIndex(i);

            //...add the entry
            addUserForm.clickAddButton();
            addUserForm.isDisplayedWithExplicitWait();
        }

        //close the form
        addUserForm.clickCloseButton();
    }

    @Test
    public void projectMembersEditFormPrintProjectLeadsUserInformationTest() {
        //get the project leads
        List<RemovableUserEntry> projectLeadUsers = editForm.getProjectLeadUsers();

        //and print some information
        for (Iterator<RemovableUserEntry> iterator = projectLeadUsers.iterator(); iterator.hasNext();) {
            RemovableUserEntry projectLeadUser = (RemovableUserEntry) iterator.next();
            System.out.println("Project Leads User: " + projectLeadUser.getName());
            System.out.println("number of links: " + projectLeadUser.getNumberOfUserLinks());

            //print the links
            List<WebElement> userLinks = projectLeadUser.getUserLinks();
            for (Iterator<WebElement> iterator2 = userLinks.iterator(); iterator2.hasNext();) {
                WebElement webElement = (WebElement) iterator2.next();
                System.out.println("Link name: " + webElement.getText());
                System.out.println("Link: " + webElement.getAttribute("href"));
            }
        }
    }

    @Test
    public void projectMembersEditFormPrintCommittersUserInformationTest() {
        //get the committers
        List<RemovableUserEntry> committersUsers = editForm.getCommittersUsers();

        //and print some information
        for (Iterator<RemovableUserEntry> iterator = committersUsers.iterator(); iterator.hasNext();) {
            RemovableUserEntry committersUser = (RemovableUserEntry) iterator.next();
            System.out.println("Committers User: " + committersUser.getName());
            System.out.println("number of links: " + committersUser.getNumberOfUserLinks());

            //print the links
            List<WebElement> userLinks = committersUser.getUserLinks();
            for (Iterator<WebElement> iterator2 = userLinks.iterator(); iterator2.hasNext();) {
                WebElement webElement = (WebElement) iterator2.next();
                System.out.println("Link name: " + webElement.getText());
                System.out.println("Link: " + webElement.getAttribute("href"));
            }
        }
    }
}
