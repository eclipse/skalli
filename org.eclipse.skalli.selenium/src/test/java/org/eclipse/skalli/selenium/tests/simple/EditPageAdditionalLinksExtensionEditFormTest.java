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
package org.eclipse.skalli.selenium.tests.simple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.AdditionalLinksExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.AddLinkForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.AdditionalLinksExtensionEditFormEntry;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class EditPageAdditionalLinksExtensionEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static AdditionalLinksExtensionEditForm editForm;

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
        editForm = PageFactory.initElements(driver, AdditionalLinksExtensionEditForm.class);

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
    public void additionalLinksEditFormAddLinkTest() {
        //add an additional link
        editForm.clickAddLink();

        //initialize the add additional link form
        AddLinkForm addForm = PageFactory.initElements(driver,
                AddLinkForm.class);
        addForm.isDisplayedWithExplicitWait();

        String linkGroup = "test";
        String pageTitle = "my test";
        String url = "http://www.test.org";
        String removeLinkName = "remove";

        //sends the keys to the fields
        addForm.sendKeysToLinkGroupField(linkGroup, true);
        addForm.sendKeysToPageTitleField(pageTitle);
        addForm.sendKeysToUrlField(url);

        //click ok and close button of the form
        addForm.clickOkAndCloseButton();

        editForm.isDisplayedWithExplicitWait();

        //get the link entries
        List<AdditionalLinksExtensionEditFormEntry> linkEntries = editForm.getLinkEntries();

        //verify them
        int size = linkEntries.size();
        Assert.assertTrue("the number of entries is incorrect (2 expected - " + size + " found)", size == 2);

        //link group
        String linkGroup0 = linkEntries.get(0).getLinkGroup();
        HashMap<String, WebElement> links0 = linkEntries.get(0).getLinks();
        int linksSize0 = links0.size();

        Assert.assertTrue("link group of the link group entry is incorrect (" + linkGroup + " expected - " + linkGroup0
                + " found)", linkGroup0.equals(linkGroup));

        Assert.assertTrue("the number of option links differs (1 expected - " + linksSize0 + " found)", linksSize0 == 1);

        Assert.assertTrue("the link entry is incorrect (" + removeLinkName + " expected)",
                links0.containsKey(removeLinkName));

        //sub link
        String linkGroup1 = linkEntries.get(1).getLinkGroup();
        HashMap<String, WebElement> links1 = linkEntries.get(1).getLinks();
        int linksSize1 = links1.size();

        Assert.assertTrue("link group of the link group entry is incorrect (" + linkGroup + " expected - " + linkGroup1
                + " found)", linkGroup1.equals(linkGroup));

        Assert.assertTrue("the number of option links differs (2 expected - " + linksSize1 + " found)", linksSize1 == 2);

        Assert.assertTrue("the link entry is incorrect (" + removeLinkName + " expected)",
                links1.containsKey(removeLinkName));

        Assert.assertTrue("the link entry is incorrect (" + pageTitle + " expected)", links1.containsKey(pageTitle));
    }

    @Test
    public void additionalLinksEditFormPrintInformationTest() {
        //get the link groups
        List<WebElement> linkGroups = editForm.getLinkGroups();

        //print them
        for (Iterator<WebElement> iterator = linkGroups.iterator(); iterator.hasNext();) {
            WebElement linkGroup = (WebElement) iterator.next();
            System.out.println("link group: " + linkGroup.getText());
        }

        System.out.println("-------");

        //get the link entries (contains the link groups as entries too - with links)
        List<AdditionalLinksExtensionEditFormEntry> linkEntries = editForm.getLinkEntries();

        //print some information
        for (Iterator<AdditionalLinksExtensionEditFormEntry> iterator = linkEntries.iterator(); iterator.hasNext();) {
            AdditionalLinksExtensionEditFormEntry entry = (AdditionalLinksExtensionEditFormEntry) iterator.next();

            System.out.println("Link entry");
            System.out.println("link group: " + entry.getLinkGroup());

            //get the links
            HashMap<String, WebElement> links = entry.getLinks();

            //and print them
            for (Iterator<String> iterator2 = links.keySet().iterator(); iterator2.hasNext();) {
                String linkName = (String) iterator2.next();
                System.out.println("link name: " + linkName);
            }

            System.out.println();
        }
    }
}
