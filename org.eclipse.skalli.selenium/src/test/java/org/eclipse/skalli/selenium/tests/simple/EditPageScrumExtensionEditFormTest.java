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
import org.eclipse.skalli.selenium.pageobjects.ext.editform.ScrumExtensionEditForm;
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

public class EditPageScrumExtensionEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static ScrumExtensionEditForm editForm;

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
        editForm = PageFactory.initElements(driver, ScrumExtensionEditForm.class);

        editForm.makeExtensionEditable();
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
    public void projectMembersEditFormScrumMastersAddUserLinkTest() {
        //add a scrum master
        editForm.clickScrumMastersAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        //close the form
        addUserForm.clickCloseButton();
    }

    @Test
    public void projectMembersEditFormProductOwnersAddUserLinkTest() {
        //add a product owners
        editForm.clickProductOwnersAddUserLink();

        //initialize the add user form
        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);
        addUserForm.isDisplayedWithExplicitWait();

        //close the form
        addUserForm.clickCloseButton();
    }

    @Test
    public void projectMembersEditFormScrumMastersUserNumberTest() {
        Assert.assertTrue("scrum masters user number differs", editForm.getNumberOfScrumMastersUsers() == 0);
    }

    @Test
    public void projectMembersEditFormProductOwnersUserNumberTest() {
        Assert.assertTrue("project leads user number differs", editForm.getNumberOfProductOwnersUsers() == 0);
    }

    @Test
    public void projectMembersEditFormScrumMastersAddUserTest() {
        //add a scrum master
        editForm.clickScrumMastersAddUserLink();

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
    public void projectMembersEditFormProductOwnersAddUserTest() {
        //add a product owners
        editForm.clickProductOwnersAddUserLink();

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
    public void projectMembersEditFormProductOwnersAddMultipleUsersTest() {
        //click add product owners
        editForm.clickProductOwnersAddUserLink();

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
        }

        //close the form
        addUserForm.clickCloseButton();
    }

    @Test
    public void projectMembersEditFormPrintScrumMastersUserInformationTest() {
        //get the scrum masters
        List<RemovableUserEntry> projectLeadUsers = editForm.getScrumMastersUsers();

        //and print some information
        for (Iterator<RemovableUserEntry> iterator = projectLeadUsers.iterator(); iterator.hasNext();) {
            RemovableUserEntry projectLeadUser = (RemovableUserEntry) iterator.next();
            System.out.println("Scrum Master User: " + projectLeadUser.getName());
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
    public void projectMembersEditFormPrintProjectLeadsUserInformationTest() {
        //get the product owners
        List<RemovableUserEntry> committersUsers = editForm.getProductOwnersUsers();

        //and print some information
        for (Iterator<RemovableUserEntry> iterator = committersUsers.iterator(); iterator.hasNext();) {
            RemovableUserEntry committersUser = (RemovableUserEntry) iterator.next();
            System.out.println("Project Lead User: " + committersUser.getName());
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

    @Test
    public void projectMembersEditFormBacklogFieldTest() {
        String text = "test";

        editForm.sendKeysToBacklogField(text);
        Assert.assertTrue("value of backlog is not \"" + text + "\"", editForm.getBacklogFieldContent().equals(text));
    }
}
