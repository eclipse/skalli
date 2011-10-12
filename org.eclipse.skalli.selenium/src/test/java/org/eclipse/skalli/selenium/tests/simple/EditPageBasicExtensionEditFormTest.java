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

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.BasicsExtensionEditForm;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPageBasicExtensionEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static BasicsExtensionEditForm editForm;

    @BeforeClass
    public static void setupClass() {
        TestUtilities.initializeDriver(driver);

        initializePageObjects();

        mainPage.isDisplayedWithExplicitWait();

        //navigate to the edit page
        mainPage.clickCreateProjectLink();
        createProjectPage.isDisplayedWithExplicitWait();
        createProjectPage.clickCreateProjectButton();

        editPage.isDisplayedWithExplicitWait();

        //initialize extensions
        editForm = PageFactory.initElements(driver, BasicsExtensionEditForm.class);
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
    public void basicExtensionEditFormProjectIdFieldTest() {
        String text = "testProjectId";

        //sends the keys to the field
        editForm.sendKeysToProjectIdField(text);
        Assert.assertTrue("value of project id is not \"" + text + "\"",
                editForm.getProjectIdFieldContent().equals(text));
    }

    @Test
    public void basicExtensionEditFormDisplayNameFieldTest() {
        String text = "testDisplayName";

        //sends the keys to the field
        editForm.sendKeysToDisplayNameField(text);
        Assert.assertTrue("value of display name is not \"" + text + "\"", editForm.getDisplayNameFieldContent()
                .equals(text));
    }

    @Test
    public void basicExtensionEditFormShortNameFieldTest() {
        String text = "testShortName";

        //sends the keys to the field
        editForm.sendKeysToShortNameField(text);
        Assert.assertTrue("value of short name is not \"" + text + "\"",
                editForm.getShortNameFieldContent().equals(text));
    }

    @Test
    public void basicExtensionEditFormDescriptionAreaFieldTest() {
        String text = "testDescriptionArea";

        //sends the keys to the field
        editForm.sendKeysToDescriptionAreaField(text);
        Assert.assertTrue("value of description area is not \"" + text + "\"",
                editForm.getDescriptionAreaFieldContent().equals(text));
    }

    @Test
    public void basicExtensionEditFormProjectTemplateFieldTest() {
        String text = "testProjectTemplate";

        //clear
        editForm.sendKeysToProjectTemplateField(
                new String(new char[editForm.getProjectTemplateFieldContent().length()]).replace('\0', '\b'), true);

        //sends the keys to the field
        editForm.sendKeysToProjectTemplateField(text, true);
        Assert.assertTrue("value of project template is not \"" + text + "\"",
                editForm.getProjectTemplateFieldContent().equals(text));
    }

    @Test
    public void basicExtensionEditFormParentProjectFieldTest() {
        String text = "testParentProject";

        //sends the keys to the field
        editForm.sendKeysToParentProjectField(text, true);
        Assert.assertTrue("value of parent project is not \"" + text + "\"", editForm.getParentProjectFieldContent()
                .equals(text));
    }

    @Test
    public void basicExtensionEditFormProjectPhaseFieldTest() {
        String text = "testProjectPhase";

        //clear
        editForm.sendKeysToProjectPhaseField(
                new String(new char[editForm.getProjectPhaseFieldContent().length()]).replace('\0', '\b'), true);

        //sends the keys to the field
        editForm.sendKeysToProjectPhaseField(text, true);
        Assert.assertTrue("value of project phase is not \"" + text + "\"", editForm.getProjectPhaseFieldContent()
                .equals(text));
    }

    @Test
    public void basicExtensionEditFormTemplateSelectionButtonTest() {
        editForm.clickTemplateSelectionButton();
        editForm.clickTemplateSelectionButton();
    }

    @Test
    public void basicExtensionEditFormParentProjectSelectionButtonTest() {
        editForm.clickParentProjectSelectionButton();
        editForm.clickParentProjectSelectionButton();
    }

    @Test
    public void basicExtensionEditFormProjectPhaseSelectionButtonTest() {
        editForm.clickProjectPhaseSelectionButton();
        editForm.clickProjectPhaseSelectionButton();
    }

    @Test
    public void basicExtensionEditFormDeletedCheckBoxTest() {
        editForm.checkDeletedCheckBox(true);
    }
}
