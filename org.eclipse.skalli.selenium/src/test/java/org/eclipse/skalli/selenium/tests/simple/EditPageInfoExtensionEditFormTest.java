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
import org.eclipse.skalli.selenium.pageobjects.ext.editform.InfoExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.RemovableFieldEntry;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPageInfoExtensionEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static InfoExtensionEditForm editForm;

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
        editForm = PageFactory.initElements(driver, InfoExtensionEditForm.class);
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
    public void infoEditFormPrintMailingsListsInformationTest() {
        //print the project homepage
        System.out.println("project homepage: " + editForm.getProjectHomepage());

        //get the mailing list entries...
        List<RemovableFieldEntry> mailingListEntries = editForm.getMailingsListEntries();

        //and print them
        for (Iterator<RemovableFieldEntry> iterator = mailingListEntries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry mailingListEntry = (RemovableFieldEntry) iterator.next();
            System.out.println("mailing lists link: " + mailingListEntry.getFieldContent());
        }
    }

    @Test
    public void infoEditFormAddMailingListTest() {
        //check the number of mailing lists
        int num = editForm.getNumberOfMailingLists();

        //add a mailing list entry
        editForm.clickAddMailingListLink();
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not add mailing list", editForm.getNumberOfMailingLists() == num + 1);

        //clean up
        editForm.clickRemoveMailingListLink(0);
        editForm.isDisplayedWithExplicitWait();
    }

    @Test
    public void infoEditFormSetMailingListTest() {
        String text = "test@test.org";

        //sends the keys to the field
        editForm.sendKeysToMailingListField(text, 0);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not set mailing list", editForm.getMailingListLink(0).equals(text));

        //clear
        editForm.sendKeysToMailingListField(new String(new char[text.length()]).replace('\0', '\b'), 0);
        editForm.isDisplayedWithExplicitWait();
    }

    @Test
    public void infoEditFormSetProjectHomepageTest() {
        String text = "http://www.test.org";

        //sends the keys to the field
        editForm.sendKeysToProjectHomepageField(text);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not set project homepage", editForm.getProjectHomepage().equals(text));
    }

    @Test
    public void infoEditFormAddRemoveTest() {
        //add a mailing lists entry
        editForm.clickAddMailingListLink();
        editForm.isDisplayedWithExplicitWait();

        //change the mailing list to "foo<number>"
        for (int i = 0; i < editForm.getNumberOfMailingLists(); i++) {
            editForm.sendKeysToMailingListField("foo" + i, i);
            editForm.isDisplayedWithExplicitWait();
        }

        //get the number of mailing lists
        int num = editForm.getNumberOfMailingLists();

        //remove the first mailing list
        editForm.clickRemoveMailingListLink(0);
        editForm.isDisplayedWithExplicitWait();

        //check the content of the mailing list entries
        for (int i = 0; i < editForm.getNumberOfMailingLists(); i++) {
            Assert.assertTrue("false content - maybe removing an element doesn't work correctly", editForm
                    .getMailingListLink(i).equals("foo" + (i + 1)));
        }

        Assert.assertTrue("could not remove mailing list", editForm.getNumberOfMailingLists() == num - 1);
    }
}
