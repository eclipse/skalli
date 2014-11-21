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

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.RatingsAndReviewExtensionEditForm;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPageRatingsAndReviewsEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static RatingsAndReviewExtensionEditForm editForm;

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
        editForm = PageFactory.initElements(driver, RatingsAndReviewExtensionEditForm.class);

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
    public void ratingsAndReviewsExtensionEditFormRatingStyleFieldTest() {
        String text = " \"Caesar\" Style (thumbs up / thumbs down) ";

        //clear
        editForm.sendKeysToRatingStyleField(
                new String(new char[editForm.getRatingStyleFieldContent().length()]).replace('\0', '\b'), true);
        editForm.isDisplayedWithExplicitWait();

        //sends the keys to the field
        editForm.sendKeysToRatingStyleField(text, true);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("value of rating style is not \"" + text + "\"",
                editForm.getRatingStyleFieldContent().equals(text));
    }

    @Test
    public void ratingsAndReviewsExtensionEditFormRatingStyleSelectionButtonTest() {
        //tries to click the rating style selection button
        editForm.clickRatingStyleSelectionButton();
        editForm.clickRatingStyleSelectionButton();
    }

    @Test
    public void ratingsAndReviewsExtensionEditFormAllowAnonymusReviewsCheckBoxTest() {
        editForm.checkAllowAnonymusReviewsCheckBox(true);

        Assert.assertTrue("allow anonymus reviews is not checked", editForm.isAllowAnonymusReviewsCheckBoxChecked());
    }
}
