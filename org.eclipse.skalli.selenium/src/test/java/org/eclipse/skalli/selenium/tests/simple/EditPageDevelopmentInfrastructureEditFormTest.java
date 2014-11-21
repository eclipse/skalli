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

import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.DevelopmentInfrastructureExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.RemovableFieldEntry;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPageDevelopmentInfrastructureEditFormTest {
    private static WebDriver driver;
    private static MainPage mainPage;
    private static CreateProjectPage createProjectPage;
    private static EditPage editPage;

    private static DevelopmentInfrastructureExtensionEditForm editForm;

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
        editForm = PageFactory.initElements(driver, DevelopmentInfrastructureExtensionEditForm.class);

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
    public void developmentInfrastructureEditFormPrintInformationTest() {
        System.out.println("Source Code: " + editForm.getSourceCodeFieldContent());

        //print the repository entries
        //repositories
        List<RemovableFieldEntry> repositoryEntries = editForm.getRepositoryEntries();

        for (Iterator<RemovableFieldEntry> iterator = repositoryEntries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry repositoryEntry = (RemovableFieldEntry) iterator.next();
            System.out.println("Repository: " + repositoryEntry.getFieldContent());
        }

        System.out.println("Bugtracker: " + editForm.getBugTrackerFieldContent());

        System.out.println("Build: " + editForm.getBuildFieldContent());

        System.out.println("Quality: " + editForm.getQualityFieldContent());

        System.out.println("Code Review: " + editForm.getCodeReviewFieldContent());

        //print the javadoc entries
        //javadocs
        List<RemovableFieldEntry> javadocEntries = editForm.getRepositoryEntries();

        for (Iterator<RemovableFieldEntry> iterator = javadocEntries.iterator(); iterator.hasNext();) {
            RemovableFieldEntry javadocEntry = (RemovableFieldEntry) iterator.next();
            System.out.println("Javadoc: " + javadocEntry.getFieldContent());
        }
    }

    @Test
    public void developmentInfrastructureEditFormAddJavadocTest() {
        //get the number of javadocs
        int num = editForm.getNumberOfJavadocs();

        //add a javadoc
        editForm.clickAddJavadocLink();
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not add javadoc", editForm.getNumberOfJavadocs() == num + 1);
    }

    @Test
    public void developmentInfrastructureEditFormAddRemoveRepositoryTest() {
        //get the number of repositories
        int num = editForm.getNumberOfRepositories();

        //add a repository
        editForm.clickAddRepositoryLink();
        editForm.isDisplayedWithExplicitWait();

        //remove a repository
        editForm.clickRemoveRepositoryLink(0);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not add and remove repository", editForm.getNumberOfRepositories() == num);
    }

    @Test
    public void developmentInfrastructureEditFormAddRepositoryTest() {
        //get the number of repositories
        int num = editForm.getNumberOfRepositories();

        //add a repository
        editForm.clickAddRepositoryLink();
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not add repository", editForm.getNumberOfRepositories() == num + 1);
    }

    @Test
    public void developmentInfrastructureEditFormAddRemoveJavadocTest() {
        //get the number of javadocs
        int num = editForm.getNumberOfJavadocs();

        //add a javadoc
        editForm.clickAddJavadocLink();
        editForm.isDisplayedWithExplicitWait();

        //remove a javadoc
        editForm.clickRemoveJavadocsLink(0);
        editForm.isDisplayedWithExplicitWait();

        Assert.assertTrue("could not add and remove javadoc", editForm.getNumberOfJavadocs() == num);
    }
}
