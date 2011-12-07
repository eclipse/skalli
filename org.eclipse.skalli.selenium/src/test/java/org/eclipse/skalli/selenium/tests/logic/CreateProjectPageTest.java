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
package org.eclipse.skalli.selenium.tests.logic;

import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.selenium.pageobjects.concrete.CreateProjectPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.ProjectDetailsPage;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.BasicsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.DevelopmentInfrastructureExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.InfoExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.MavenExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.ProjectMembersExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.RatingsAndReviewExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.RelatedProjectsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.ScrumExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.AddUserForm;
import org.eclipse.skalli.selenium.tests.TestUtilities;
import org.eclipse.skalli.selenium.utils.DriverProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

/**
 * Some tests for the create project page
 */
public class CreateProjectPageTest {
    private static WebDriver driver;
    private static CreateProjectPage createProjectPage;
    private static MainPage mainPage;
    private static EditPage editPage;
    private static ProjectDetailsPage projectDetailsPage;

    @BeforeClass
    public static void setupClass() {
        driver = TestUtilities.initializeDriver();
        initializePageObjects();
    }

    private static void initializePageObjects() {
        createProjectPage = PageFactory.initElements(driver, CreateProjectPage.class);
        mainPage = PageFactory.initElements(driver, MainPage.class);
        editPage = PageFactory.initElements(driver, EditPage.class);
        projectDetailsPage = PageFactory.initElements(driver, ProjectDetailsPage.class);
    }

    @Before
    public void setup() {
        DriverProvider.navigateToBaseUrl(driver);

        mainPage.isDisplayedWithExplicitWait();

        //navigate to the create project page
        mainPage.clickCreateProjectLink();
        createProjectPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void createLightWeightProjectTest() {
        createProject();

        //fill the required fields
        BasicsExtensionEditForm basicsExtensionEditForm = editPage.getBasicsExtensionEditForm();
        basicsExtensionEditForm.isDisplayedWithExplicitWait();

        String projectId = "new_project";
        String displayName = "My new Project";

        //check that the project does not exist
        DriverProvider.navigateToSubUrl(driver, "/projects/" + projectId);
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        //project exists
        try {
            if (projectDetailsPage.isDisplayed()) {
                deleteProject(basicsExtensionEditForm);
                mainPage.clickCreateProjectLink();
            }
        } catch (Exception e) {
            //project details page not shown -> project does not exist
        }

        createProject();

        basicsExtensionEditForm.sendKeysToProjectIdField(projectId);
        basicsExtensionEditForm.sendKeysToDisplayNameField(displayName);

        basicsExtensionEditForm.isDisplayedWithExplicitWait();

        //create the project
        editPage.clickUpperOkButton();

        //if the project was created the project details page should be displayed
        projectDetailsPage.isDisplayedWithExplicitWait();

        deleteProject(basicsExtensionEditForm);
    }

    private void createProject() {
        //make a free style project
        createProjectPage.clickFreeStyleProjectButton();
        createProjectPage.isDisplayedWithExplicitWait();

        //go to the edit form of the project to be created
        createProjectPage.clickCreateProjectButton();
        editPage.isDisplayedWithExplicitWait();
    }

    private void deleteProject(BasicsExtensionEditForm basicsExtensionEditForm) {
        //delete the project
        projectDetailsPage.clickEditLink();
        editPage.isDisplayedWithExplicitWait();

        basicsExtensionEditForm.checkDeletedCheckBox(true);
        basicsExtensionEditForm.isDisplayedWithExplicitWait();

        editPage.clickUpperOkButton();

        //if it was deleted the main page should be displayed
        mainPage.isDisplayedWithExplicitWait();
    }

    @Test
    public void createHeavyWeightProjectTest() {
        createProject();

        //fill the required fields
        BasicsExtensionEditForm basicsExtensionEditForm = editPage.getBasicsExtensionEditForm();
        basicsExtensionEditForm.isDisplayedWithExplicitWait();

        String projectId = "new_project";
        String displayName = "My new Project";

        //check that the project does not exist
        DriverProvider.navigateToSubUrl(driver, "/projects/" + projectId);
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        //project exists
        try {
            if (projectDetailsPage.isDisplayed()) {
                deleteProject(basicsExtensionEditForm);
                mainPage.clickCreateProjectLink();
            }
        } catch (Exception e) {
            //project details page not shown -> project does not exist
        }

        createProject();

        AddUserForm addUserForm = PageFactory.initElements(driver, AddUserForm.class);

        //fill extensions
        fillBasicExtension(basicsExtensionEditForm, projectId, displayName);
        fillProjectMembersExtension(addUserForm);
        fillInfoExtension();
        //ignored as setting a value to a comboBox field yields unpredictable results
        //fillAdditionalLinksExtension();
        fillRatingsAndReviewsExtension();
        fillRelatedProjectsExtension();
        fillDevelopmentInfrastructureExtension();
        fillMavenExtension();
        fillScrumExtension(addUserForm);

        //create the project
        editPage.clickUpperOkButton();

        //if the project was created the project details page should be displayed
        projectDetailsPage.isDisplayedWithExplicitWait();

        deleteProject(basicsExtensionEditForm);
    }

    public void fillScrumExtension(AddUserForm addUserForm) {
        ScrumExtensionEditForm scrumExtensionEditForm = editPage.getScrumExtensionEditForm();
        scrumExtensionEditForm.isDisplayedWithExplicitWait();

        scrumExtensionEditForm.makeExtensionEditable();

        scrumExtensionEditForm.clickScrumMastersAddUserLink();
        addUserForm.isDisplayedWithExplicitWait();

        addUserForm.sendKeysToSearchForField("j");
        addUserForm.submitSearchForContent();

        //select entry
        addUserForm.getSelect().selectByIndex(0);

        addUserForm.clickAddAndCloseButton();

        scrumExtensionEditForm.sendKeysToBacklogField("");
    }

    public void fillMavenExtension() {
        MavenExtensionEditForm mavenExtensionEditForm = editPage.getMavenExtensionEditForm();
        mavenExtensionEditForm.isDisplayedWithExplicitWait();

        mavenExtensionEditForm.makeExtensionEditable();

        //mavenExtensionEditForm.sendKeysToReactorPomPathField("");

        //mavenExtensionEditForm.sendKeysToMavenSiteField("");

        mavenExtensionEditForm.isDisplayedWithExplicitWait();
    }

    public void fillDevelopmentInfrastructureExtension() {
        DevelopmentInfrastructureExtensionEditForm developmentInfrastructureExtensionEditForm = editPage
                .getDevelopmentInfrastructureExtensionEditForm();
        developmentInfrastructureExtensionEditForm.isDisplayedWithExplicitWait();

        developmentInfrastructureExtensionEditForm.makeExtensionEditable();

        developmentInfrastructureExtensionEditForm
                .sendKeysToSourceCodeField("http://git.eclipse.org/c/skalli/org.eclipse.skalli.git/");

        developmentInfrastructureExtensionEditForm.sendKeysToRepositoryField(
                "scm:git:git://git.wdf.sap.corp/eclipse/org.eclipse.skalli.git", 0);

        developmentInfrastructureExtensionEditForm
                .sendKeysToBugTrackerField("https://bugs.eclipse.org/bugs/buglist.cgi?query_format=specific&order=relevance+desc&bug_status=__open__&product=Skalli");

        developmentInfrastructureExtensionEditForm
                .sendKeysToBuildField("https://hudson.eclipse.org/hudson/job/skalli/");

        //developmentInfrastructureExtensionEditForm.sendKeysToQualityField("");

        //developmentInfrastructureExtensionEditForm.sendKeysToCodeReviewField("");

        //developmentInfrastructureExtensionEditForm.sendKeysToJavadocField("", 0);
    }

    public void fillRelatedProjectsExtension() {
        RelatedProjectsExtensionEditForm relatedProjectsExtensionEditForm = editPage
                .getRelatedProjectsExtensionEditForm();
        relatedProjectsExtensionEditForm.isDisplayedWithExplicitWait();

        relatedProjectsExtensionEditForm.makeExtensionEditable();

        relatedProjectsExtensionEditForm.checkCalculateRelatedProjectCheckBox(false);

        relatedProjectsExtensionEditForm.clickAddRelatedProjectLink();
        relatedProjectsExtensionEditForm.isDisplayedWithExplicitWait();

        relatedProjectsExtensionEditForm.sendKeysToRelatedProjectField("test", true, 1);
        relatedProjectsExtensionEditForm.isDisplayedWithExplicitWait();

        relatedProjectsExtensionEditForm.clickRemoveRelatedProjectLink(0);
        relatedProjectsExtensionEditForm.isDisplayedWithExplicitWait();
    }

    public void fillRatingsAndReviewsExtension() {
        RatingsAndReviewExtensionEditForm ratingsAndReviewExtensionEditForm = editPage
                .getRatingsAndReviewExtensionEditForm();
        ratingsAndReviewExtensionEditForm.isDisplayedWithExplicitWait();

        ratingsAndReviewExtensionEditForm.makeExtensionEditable();

        //reset rating style field
        ratingsAndReviewExtensionEditForm.sendKeysToRatingStyleField(new String(
                new char[ratingsAndReviewExtensionEditForm
                        .getRatingStyleFieldContent().length()]).replace('\0', '\b'), true);
        ratingsAndReviewExtensionEditForm.sendKeysToRatingStyleField(" \"Smiley\" Style (5 levels of consent) ", true);

        ratingsAndReviewExtensionEditForm.checkAllowAnonymusReviewsCheckBox(true);
    }

    //ignored as setting a value to a comboBox field yields unpredictable results
   /* public void fillAdditionalLinksExtension() {
        AdditionalLinksExtensionEditForm additionalLinksExtensionEditForm = editPage
                .getAdditionalLinksExtensionEditForm();
        additionalLinksExtensionEditForm.isDisplayedWithExplicitWait();

        additionalLinksExtensionEditForm.makeExtensionEditable();

        additionalLinksExtensionEditForm.clickAddLink();

        AddLinkForm addLinkForm = PageFactory.initElements(driver, AddLinkForm.class);
        addLinkForm.isDisplayedWithExplicitWait();

        addLinkForm.sendKeysToLinkGroupField("test", true);
        addLinkForm.sendKeysToPageTitleField("my test link");
        addLinkForm.sendKeysToUrlField("http://www.test.org");
        addLinkForm.isDisplayedWithExplicitWait();

        addLinkForm.clickOkAndCloseButton();
        additionalLinksExtensionEditForm.isDisplayedWithExplicitWait();

        //verify the link has been added
        //(for full verification process see EditPageAdditionalLinksExtensionEditFormTest.additionalLinksEditFormAddLinkTest())
        int size = additionalLinksExtensionEditForm.getLinkEntries().size();
        Assert.assertTrue("the number of entries is incorrect (2 expected - " + size + " found)", size == 2);
    }*/

    public void fillInfoExtension() {
        InfoExtensionEditForm infoExtensionEditForm = editPage.getInfoExtensionEditForm();
        infoExtensionEditForm.isDisplayedWithExplicitWait();

        //set project homepage
        infoExtensionEditForm.sendKeysToProjectHomepageField("http://www.test.org");

        //add a mailing list
        infoExtensionEditForm.clickAddMailingListLink();
        infoExtensionEditForm.sendKeysToMailingListField("skalli-dev@eclipse.org", 1);

        //remove the first empty mailing list
        infoExtensionEditForm.clickRemoveMailingListLink(0);
    }

    public void fillProjectMembersExtension(AddUserForm addUserForm) {
        ProjectMembersExtensionEditForm projectMembersExtensionEditForm = editPage.getProjectMembersExtensionEditForm();
        projectMembersExtensionEditForm.isDisplayedWithExplicitWait();

        //add project lead user
        projectMembersExtensionEditForm.clickProjectLeadsAddUserLink();
        addUserForm.isDisplayedWithExplicitWait();

        addUserForm.sendKeysToSearchForField("j");
        addUserForm.submitSearchForContent();

        //select entry
        Select select = addUserForm.getSelect();
        select.selectByIndex(0);

        addUserForm.clickAddAndCloseButton();

        //add committer user
        projectMembersExtensionEditForm.clickCommittersAddUserLink();
        addUserForm.isDisplayedWithExplicitWait();

        addUserForm.sendKeysToSearchForField("j");
        addUserForm.submitSearchForContent();

        //select entry
        select = addUserForm.getSelect();
        select.selectByIndex(0);

        addUserForm.clickAddAndCloseButton();

        //remove project lead user
        projectMembersExtensionEditForm.getProjectLeadUsers().get(0).clickRemoveLink();
    }

    public void fillBasicExtension(BasicsExtensionEditForm basicsExtensionEditForm, String projectId, String displayName) {
        basicsExtensionEditForm.sendKeysToProjectIdField(projectId);

        basicsExtensionEditForm.sendKeysToDisplayNameField(displayName);

        basicsExtensionEditForm.sendKeysToShortNameField("shortName");

        basicsExtensionEditForm.sendKeysToDescriptionAreaField("description\n\nline1\nline2");

        //reset selection field
        basicsExtensionEditForm.sendKeysToProjectTemplateField(new String(new char[basicsExtensionEditForm
                .getProjectTemplateFieldContent().length()]).replace('\0', '\b'), true);
        basicsExtensionEditForm.sendKeysToProjectTemplateField("Free-Style Project", true);

        //reset selection field
        basicsExtensionEditForm.sendKeysToParentProjectField(new String(new char[basicsExtensionEditForm
                .getParentProjectFieldContent().length()]).replace('\0', '\b'), true);
        basicsExtensionEditForm.sendKeysToParentProjectField("Eclipse <eclipse>", true);

        //reset selection field
        basicsExtensionEditForm.sendKeysToParentProjectField(new String(new char[basicsExtensionEditForm
                .getParentProjectFieldContent().length()]).replace('\0', '\b'), true);
        basicsExtensionEditForm.sendKeysToParentProjectField("Proposal", true);

        basicsExtensionEditForm.isDisplayedWithExplicitWait();
    }
}
