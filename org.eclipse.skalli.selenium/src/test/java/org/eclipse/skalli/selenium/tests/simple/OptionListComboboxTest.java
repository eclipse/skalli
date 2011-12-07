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
import org.eclipse.skalli.selenium.pageobjects.ext.editform.BasicsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.OptionListCombobox;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * Simple test for the option list combobox of vaadin
 */
public class OptionListComboboxTest {
    private static WebDriver driver;
    private static CreateProjectPage createProjectPage;
    private static MainPage mainPage;
    private static EditPage editPage;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();

        initializePageObjects();

        mainPage.isDisplayedWithExplicitWait();

        DriverProvider.navigateToSubUrl(driver, "/create");
        createProjectPage.isDisplayedWithExplicitWait();

        createProjectPage.clickCreateProjectButton();
    }

    private static void initializePageObjects() {
        createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        mainPage = PageFactory.initElements(driver, MainPage.class);
        editPage = PageFactory.initElements(driver, EditPage.class);
    }

    @Before
    public void setup() {
        editPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void printProjectPhaseEntriesTest() {
        BasicsExtensionEditForm basicsExtensionEditForm = editPage.getBasicsExtensionEditForm();
        basicsExtensionEditForm.isDisplayedWithExplicitWait();
        basicsExtensionEditForm.clickProjectPhaseSelectionButton();

        OptionListCombobox combobox = PageFactory.initElements(driver, OptionListCombobox.class);
        combobox.isDisplayedWithExplicitWait();

        System.out.println("info bar content: " + combobox.getInfoBarContent());
        System.out.println("from number: " + combobox.getFromNumber());
        System.out.println("to number: " + combobox.getToNumber());
        System.out.println("count: " + combobox.getCount());

        System.out.println("Entries");

        while (true) {
            System.out.println("previous entries exist?: " + combobox.arePreviousEntriesExisting());
            System.out.println("next entries exist?: " + combobox.areNextEntriesExisting());

            printActualEntries(combobox);

            //if no next entries exist break. otherwise click next scroll button
            if (!combobox.areNextEntriesExisting()) {
                break;
            }

            combobox.clickNextScrollButton();
            combobox.isDisplayedWithExplicitWait();
        }
    }

    private void printActualEntries(OptionListCombobox combobox) {
        List<WebElement> actualEntries = combobox.getActualEntries();
        for (Iterator<WebElement> iterator = actualEntries.iterator(); iterator.hasNext();) {
            WebElement webElement = (WebElement) iterator.next();
            System.out.println(webElement.getText());
        }
    }
}
