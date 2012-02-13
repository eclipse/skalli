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
import org.eclipse.skalli.selenium.pageobjects.ext.editform.RelatedProjectsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.RelatedProjectsExtensionEditFormRelatedProjectEntry;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPageRelatedProjectsExtensionEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static RelatedProjectsExtensionEditForm editForm;

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
        editForm = PageFactory.initElements(driver, RelatedProjectsExtensionEditForm.class);

        editForm.makeExtensionEditable();
    }

    private static void initializePageObjects() {
        mainPage = PageFactory.initElements(driver, MainPage.class);
        createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        editPage = PageFactory.initElements(driver, EditPage.class);
    }

    @Before
    public void setup() {
        //check if the edit page is displayed before every test
        editPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void relatedProjectsEditFormPrintRelatedProjectsInformationTest() {
        System.out.println("calculate related projects?: " + editForm.isCalculateRelatedProjectCheckBoxChecked());

        //get the related projects
        List<RelatedProjectsExtensionEditFormRelatedProjectEntry> relatedProjectEntries = editForm
                .getRelatedProjectEntries();

        //and print them
        for (Iterator<RelatedProjectsExtensionEditFormRelatedProjectEntry> iterator = relatedProjectEntries.iterator(); iterator
                .hasNext();) {
            RelatedProjectsExtensionEditFormRelatedProjectEntry relatedProjectEntry = (RelatedProjectsExtensionEditFormRelatedProjectEntry) iterator
                    .next();
            System.out.println("related project: " + relatedProjectEntry.getRelatedProject());
        }
    }

    @Test
    public void relatedProjectsEditFormAddRelatedProjectTest() {
        //get the number of related projects
        int num = editForm.getNumberOfRelatedProjects();

        //add a related project
        editForm.clickAddRelatedProjectLink();
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not add related project", editForm.getNumberOfRelatedProjects() == num + 1);
    }

    @Test
    public void relatedProjectsEditFormSetRelatedProjectTest() {
        String text = "test <test>";

        //sends the keys to the field
        editForm.sendKeysToRelatedProjectField(text, false, 0);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not set related project", editForm.getRelatedProject(0).equals(text));
    }

    @Test
    public void relatedProjectsEditFormCheckCalculateRelatedProjectsCheckBoxTest() {
        editForm.checkCalculateRelatedProjectCheckBox(true);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not check calculate related projects checkbox",
                editForm.isCalculateRelatedProjectCheckBoxChecked());
    }

    //TODO https://bugs.eclipse.org/bugs/show_bug.cgi?id=366372
    @Test
    public void relatedProjectsEditFormAddRemoveTest() {
        //uncheck calculate related projects to be able to add links
        editForm.checkCalculateRelatedProjectCheckBox(false);
        editForm.isDisplayedWithExplicitWait();

        //add a related project
        editForm.clickAddRelatedProjectLink();
        editForm.isDisplayedWithExplicitWait();

        //sends the keys to the field
        editForm.sendKeysToRelatedProjectField("Skalli <skalli>", true, 0);
        editForm.isDisplayedWithExplicitWait();

        //sends the keys to the field
        editForm.sendKeysToRelatedProjectField("Eclipse <eclipse>", true, 1);
        editForm.isDisplayedWithExplicitWait();

        //get the number of related projects
        int num = editForm.getNumberOfRelatedProjects();

        //remove the first related project
        editForm.clickRemoveRelatedProjectLink(0);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("false content - maybe removing an element doesn't work correctly", editForm
                .getRelatedProject(0)
                .equals("Eclipse <eclipse>"));

        Assert.assertTrue("could not remove related project", editForm.getNumberOfRelatedProjects() == num - 1);
    }
}
