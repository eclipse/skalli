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
package org.eclipse.skalli.selenium.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This is the abstract page which should be the parent of every page object.
 *
 * <br/>
 * It is essential that you always implement the {@link #isDisplayed()} method. The test
 * should always have the following things in it:<br/><br/>
 * {@code AbstractPage page = PageFactory.initElements(driver, org.eclipse.skalli.selenium.pageobjects.MainHeaderPage.class);<br/>
 * page.{@link #isDisplayedWithExplicitWait()};}<br/><br/>
 * Otherwise there are problems recognizing if the page is loaded or not and if every element is on the page.
 * Elements which are not implemented in the {@link #isDisplayed()} method are not recognized.
 */
public abstract class AbstractPage {
    //needed to allow initialization with PageFactory
    @FindBy(how = How.XPATH, using = "//html")
    private WebElement html;

    /**
     * The time to wait for a page to load
     * (in seconds - system property "selenium.waitForPageToLoad.timeout" or 5 by default)
     */
    private static final long WAIT_FOR_PAGE_TO_LOAD_TIME = Long.parseLong(System.getProperty(
            "selenium.waitForPageToLoad.timeout", "5"));

    /**
     * The driver
     */
    protected WebDriver driver;

    /**
     * Creates an abstract page using the driver
     * @param driver The driver to be used
     */
    public AbstractPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Returns whether the page is displayed or not.
     * <br/>
     * It is essential that this method is implemented for every page and that it is called.
     * If you only instantiate the page with the {@link org.openqa.selenium.support.PageFactory}
     * it is for example not recognized that the page is not accessible.
     * <br/>
     * You have to ask {@link org.openqa.selenium.WebElement#isDisplayed()}
     * of every {@link org.openqa.selenium.WebElement}. Otherwise it cannot be
     * recognized that the element you did not check is on the page or not.
     * @return {@code true} if the page is displayed {@code false} otherwise
     */
    public abstract boolean isDisplayed();

    /**
     * The return value for {@link #explicitWaitForPage()} (one of the fields initialized in the page object - not null!!!)
     * @return The value returned by {@link #explicitWaitForPage()} (not null!!!)
     */
    protected abstract WebElement explicitWaitReturn();

    /**
     * Explicitly waits for the page to load (until {@link #isDisplayed()} returns {@code true})
     */
    public void explicitWaitForPage() {
        Wait<WebDriver> wait = new WebDriverWait(driver, WAIT_FOR_PAGE_TO_LOAD_TIME);

        wait.until(new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                try {
                    if (isDisplayed()) {
                        return explicitWaitReturn();
                    }
                } catch (Exception e) {
                    //avoid that any exception causes the wait to be stopped
                    //print out the error message to identify which UI element cannot be found
                    System.out.println(e.getMessage());
                }

                return null;
            }
        });
    }

    /**
     * Explicitly waits for the page to load and returns whether the page is displayed or not
     * @return Whether the page is displayed or not after the explicit wait
     */
    public boolean isDisplayedWithExplicitWait() {
        explicitWaitForPage();
        return isDisplayed();
    }

    /**
     * Returns the title of the page currently loaded by the {@link org.openqa.selenium.WebDriver}
     * @return The title of the page
     */
    public String getTitle() {
        return driver.getTitle();
    }
}
