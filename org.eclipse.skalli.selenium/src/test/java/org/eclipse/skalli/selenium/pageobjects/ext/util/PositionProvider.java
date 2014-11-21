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
package org.eclipse.skalli.selenium.pageobjects.ext.util;

import java.util.HashMap;

import org.eclipse.skalli.selenium.pageobjects.concrete.EditPage;
import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * This is a provider for the extension positions
 *
 * It computes the positions once. After that you can get the position of an extension
 * which is linked to the name.
 *
 * You can force the position provider to recompute the extension positions
 * if they changed.
 *
 * ATTENTION: must be located on the edit page
 */
public class PositionProvider {
    private static String BASICS_EXTENSION_EDIT_FORM_NAME = "Basics";

    private static String PROJECT_MEMBERS_EXTENSION_EDIT_FORM_NAME = "Project Members";

    private static String INFO_EXTENSION_EDIT_FORM_NAME = "Info";

    private static String ADDITIONAL_INFORMATION_EXTENSION_EDIT_FORM_NAME = "Additional Links";

    private static String RATINGS_AND_REVIEWS_EXTENSION_EDIT_FORM_NAME = "Ratings & Reviews";

    private static String RELATED_PROJECTS_EXTENSION_EDIT_FORM_NAME = "Related Projects";

    private static String DEVELOPMENT_INFRASTRUCTURE_EXTENSION_EDIT_FORM_NAME = "Development Infrastructure";

    private static String MAVEN_EXTENSION_EDIT_FORM_NAME = "Maven";

    private static String SCRUM_EXTENSION_EDIT_FORM_NAME = "Scrum";

    private static HashMap<String, Integer> map = new HashMap<String, Integer>();

    //must be positioned on edit page
    public static int getPositionNumberOfExtensionsEditForm(WebDriver driver, String extensionName) {
        initializePositions(driver, false);

        Integer positionOfExtension = map.get(extensionName);
        return (positionOfExtension == null) ? -100 : positionOfExtension;
    }

    public static void initializePositions(WebDriver driver, boolean forceInitialization) {
        if (!forceInitialization && map.size() > 0) {
            return;
        }

        //check if the edit page is displayed
        PageFactory.initElements(driver, EditPage.class).isDisplayedWithExplicitWait();

        int numberOfExtensions = AbstractExtensionEditForm.getNumberOfExtensions(driver);
        for (int i = 0; i < numberOfExtensions; i++) {
            //make abstract extension
            AbstractExtensionEditForm abstractExtensionEditForm = new AbstractExtensionEditForm(driver, i) {
                @Override
                protected boolean isExtensionContentDisplayed() {
                    return true;
                }
            };

            //wait for it to be displayed
            abstractExtensionEditForm.isDisplayedWithExplicitWait();

            map.put(abstractExtensionEditForm.getTitle(), i);
        }
    }

    public static String getBascisExtensionEditFormName() {
        return BASICS_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getProjectMembersExtensionEditFormName() {
        return PROJECT_MEMBERS_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getInfoExtensionEditFormName() {
        return INFO_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getAdditionalInformationExtensionEditFormName() {
        return ADDITIONAL_INFORMATION_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getRatingsAndReviewExtensionEditFormName() {
        return RATINGS_AND_REVIEWS_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getRelatedProjectsExtensionEditFormName() {
        return RELATED_PROJECTS_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getDevelopmentInfrastructureExtensionEditFormName() {
        return DEVELOPMENT_INFRASTRUCTURE_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getMavenExtensionEditFormName() {
        return MAVEN_EXTENSION_EDIT_FORM_NAME;
    }

    public static String getScrumExtensionEditFormName() {
        return SCRUM_EXTENSION_EDIT_FORM_NAME;
    }
}
