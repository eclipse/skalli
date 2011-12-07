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
package org.eclipse.skalli.selenium.pageobjects.concrete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.SearchAndNavigationbarPage;
import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.AdditionalLinksExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.BasicsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.DevelopmentInfrastructureExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.InfoExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.MavenExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.ProjectMembersExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.RatingsAndReviewExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.RelatedProjectsExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.editform.ScrumExtensionEditForm;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

/**
 * This page object contains the {@link org.openqa.selenium.WebElement}s for the edit page
 *
 * The edit page is a page with search field, navigation bar and the edit page specific elements (buttons like "Ok",
 * "Cancel", "Expand All", "Collapse All" and the extensions)
 */
public class EditPage extends SearchAndNavigationbarPage {

    @FindBy(how = How.XPATH, using = "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
            + 1 + "]/div/div/div/div/div[" + 1 + "]")
    private WebElement upperOkButton;

    @FindBy(how = How.XPATH, using = "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
            + 1 + "]/div/div/div/div/div[" + 2 + "]")
    private WebElement upperCancelButton;

    @FindBy(how = How.XPATH, using = "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
            + 1 + "]/div/div/div/div/div[" + 3 + "]")
    private WebElement upperValidateButton;

    @FindBy(how = How.XPATH, using = "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
            + 1 + "]/div/div/div/div/div[" + 4 + "]")
    private WebElement upperExpandAllButton;

    @FindBy(how = How.XPATH, using = "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
            + 1 + "]/div/div/div/div/div[" + 5 + "]")
    private WebElement upperCollapseAllButton;

    private List<AbstractExtensionEditForm> extensions;

    private BasicsExtensionEditForm basicsExtensionEditForm;

    private ProjectMembersExtensionEditForm projectMembersExtensionEditForm;

    private InfoExtensionEditForm infoExtensionEditForm;

    private AdditionalLinksExtensionEditForm additionalLinksExtensionEditForm;

    private RatingsAndReviewExtensionEditForm ratingsAndReviewExtensionEditForm;

    private RelatedProjectsExtensionEditForm relatedProjectsExtensionEditForm;

    private DevelopmentInfrastructureExtensionEditForm developmentInfrastructureExtensionEditForm;

    private MavenExtensionEditForm mavenExtensionEditForm;

    private ScrumExtensionEditForm scrumExtensionEditForm;

    public EditPage(WebDriver driver) {
        super(driver);

        extensions = new ArrayList<AbstractExtensionEditForm>();
    }

    //cannot be initialized in constructor because the page may not be loaded -> elements not found -> exception
    private void initializeExtensions() {
        //initialize only once
        if (extensions.size() > 0) {
            return;
        }

        basicsExtensionEditForm = PageFactory.initElements(driver, BasicsExtensionEditForm.class);
        extensions.add(basicsExtensionEditForm);

        projectMembersExtensionEditForm = PageFactory.initElements(driver, ProjectMembersExtensionEditForm.class);
        extensions.add(projectMembersExtensionEditForm);

        infoExtensionEditForm = PageFactory.initElements(driver, InfoExtensionEditForm.class);
        extensions.add(infoExtensionEditForm);

        additionalLinksExtensionEditForm = PageFactory.initElements(driver, AdditionalLinksExtensionEditForm.class);
        extensions.add(additionalLinksExtensionEditForm);

        ratingsAndReviewExtensionEditForm = PageFactory.initElements(driver, RatingsAndReviewExtensionEditForm.class);
        extensions.add(ratingsAndReviewExtensionEditForm);

        relatedProjectsExtensionEditForm = PageFactory.initElements(driver, RelatedProjectsExtensionEditForm.class);
        extensions.add(relatedProjectsExtensionEditForm);

        developmentInfrastructureExtensionEditForm = PageFactory.initElements(driver,
                DevelopmentInfrastructureExtensionEditForm.class);
        extensions.add(developmentInfrastructureExtensionEditForm);

        mavenExtensionEditForm = PageFactory.initElements(driver, MavenExtensionEditForm.class);
        extensions.add(mavenExtensionEditForm);

        scrumExtensionEditForm = PageFactory.initElements(driver, ScrumExtensionEditForm.class);
        extensions.add(scrumExtensionEditForm);
    }

    @Override
    public boolean isDisplayed() {
        return super.isDisplayed()
                && getUpperOkButton().isDisplayed()
                && getUpperCancelButton().isDisplayed()
                && getUpperValidateButton().isDisplayed()
                && getUpperExpandAllButton().isDisplayed()
                && getUpperCollapseAllButton().isDisplayed()
                && ((extensions.size() > 0) ? (getLowerOkButton().isDisplayed() && getLowerCancelButton().isDisplayed()
                        && getLowerValidateButton().isDisplayed() && getLowerExpandAllButton().isDisplayed() && getLowerCollapseAllButton()
                        .isDisplayed())
                        : true);
    }

    /**
     * check needs a lot of time so it have to be executed separately
     * @return whether the extensions are displayed or not
     */
    public boolean areExtensionsDisplayed() {
        initializeExtensions();

        for (Iterator<AbstractExtensionEditForm> iterator = extensions.iterator(); iterator.hasNext();) {
            AbstractExtensionEditForm editForm = (AbstractExtensionEditForm) iterator.next();
            if (!editForm.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    public void clickUpperOkButton() {
        getUpperOkButton().click();
    }

    public void clickUpperCancelButton() {
        getUpperCancelButton().click();
    }

    public void clickUpperValidateButton() {
        getUpperValidateButton().click();
    }

    public void clickUpperExpandAllButton() {
        getUpperExpandAllButton().click();
    }

    public void clickUpperCollapseAllButton() {
        getUpperCollapseAllButton().click();
    }

    public void clickLowerOkButton() {
        getLowerOkButton().click();
    }

    public void clickLowerCancelButton() {
        getLowerCancelButton().click();
    }

    public void clickLowerValidateButton() {
        getLowerValidateButton().click();
    }

    public void clickLowerExpandAllButton() {
        getLowerExpandAllButton().click();
    }

    public void clickLowerCollapseAllButton() {
        getLowerCollapseAllButton().click();
    }

    public List<AbstractExtensionEditForm> getExtensions() {
        return extensions;
    }

    public BasicsExtensionEditForm getBasicsExtensionEditForm() {
        initializeExtensions();

        return basicsExtensionEditForm;
    }

    public ProjectMembersExtensionEditForm getProjectMembersExtensionEditForm() {
        initializeExtensions();

        return projectMembersExtensionEditForm;
    }

    public InfoExtensionEditForm getInfoExtensionEditForm() {
        initializeExtensions();

        return infoExtensionEditForm;
    }

    public AdditionalLinksExtensionEditForm getAdditionalLinksExtensionEditForm() {
        initializeExtensions();

        return additionalLinksExtensionEditForm;
    }

    public RatingsAndReviewExtensionEditForm getRatingsAndReviewExtensionEditForm() {
        initializeExtensions();

        return ratingsAndReviewExtensionEditForm;
    }

    public RelatedProjectsExtensionEditForm getRelatedProjectsExtensionEditForm() {
        initializeExtensions();

        return relatedProjectsExtensionEditForm;
    }

    public DevelopmentInfrastructureExtensionEditForm getDevelopmentInfrastructureExtensionEditForm() {
        initializeExtensions();

        return developmentInfrastructureExtensionEditForm;
    }

    public MavenExtensionEditForm getMavenExtensionEditForm() {
        initializeExtensions();

        return mavenExtensionEditForm;
    }

    public ScrumExtensionEditForm getScrumExtensionEditForm() {
        initializeExtensions();

        return scrumExtensionEditForm;
    }

    protected WebElement getUpperOkButton() {
        return upperOkButton;
    }

    protected WebElement getUpperCancelButton() {
        return upperCancelButton;
    }

    protected WebElement getUpperValidateButton() {
        return upperValidateButton;
    }

    protected WebElement getUpperExpandAllButton() {
        return upperExpandAllButton;
    }

    protected WebElement getUpperCollapseAllButton() {
        return upperCollapseAllButton;
    }

    protected WebElement getLowerOkButton() {
        return driver.findElement(By.xpath(getXPathToLowerOkButton()));
    }

    protected WebElement getLowerCancelButton() {
        return driver.findElement(By.xpath(getXPathToLowerCancelButton()));
    }

    protected WebElement getLowerValidateButton() {
        return driver.findElement(By.xpath(getXPathToLowerValidateButton()));
    }

    protected WebElement getLowerExpandAllButton() {
        return driver.findElement(By.xpath(getXPathToLowerExpandAllButton()));
    }

    protected WebElement getLowerCollapseAllButton() {
        return driver.findElement(By.xpath(getXPathToLowerCollapseAllButton()));
    }

    private String getXPathToLowerOkButton() {
        initializeExtensions();
        return "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
                + (extensions.size() + AbstractExtensionEditForm.LOWER_BUTTON_AREA_OFFSET) + "]/div/div/div/div/div["
                + 1 + "]";
    }

    private String getXPathToLowerCancelButton() {
        initializeExtensions();

        return "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
                + (extensions.size() + AbstractExtensionEditForm.LOWER_BUTTON_AREA_OFFSET) + "]/div/div/div/div/div["
                + 2 + "]";
    }

    private String getXPathToLowerValidateButton() {
        initializeExtensions();

        return "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
                + (extensions.size() + AbstractExtensionEditForm.LOWER_BUTTON_AREA_OFFSET) + "]/div/div/div/div/div["
                + 3 + "]";
    }

    private String getXPathToLowerExpandAllButton() {
        initializeExtensions();

        return "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
                + (extensions.size() + AbstractExtensionEditForm.LOWER_BUTTON_AREA_OFFSET) + "]/div/div/div/div/div["
                + 4 + "]";
    }

    private String getXPathToLowerCollapseAllButton() {
        initializeExtensions();

        return "//div[@id='project']/div/div[2]/div[2]/div/div/div[1]/div/div/div/div[1]/div/div/div[2]/div/div/div["
                + (extensions.size() + AbstractExtensionEditForm.LOWER_BUTTON_AREA_OFFSET) + "]/div/div/div/div/div["
                + 5 + "]";
    }
}
