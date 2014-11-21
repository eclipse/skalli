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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

/**
 * This is a option list combobox which appears if you click the selection button of a field with selection
 */
public class OptionListCombobox extends AbstractPage {
    public OptionListCombobox(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isDisplayed() {
        //info bar and content area elements can be closed tags -> isDisplayed returns false
        //previous and next scroll button elements can be hidden -> isDisplayed returns false
        return getComboboxSection().isDisplayed() && (getInfoBar() instanceof RemoteWebElement)
                && (getPreviousScrollButton() instanceof RemoteWebElement)
                && (getNextScrollButton() instanceof RemoteWebElement)
                && (getContentArea() instanceof RemoteWebElement)
                && areActualEntriesDisplayed();
    }

    private boolean areActualEntriesDisplayed() {
        List<WebElement> actualEntries = getActualEntries();

        for (Iterator<WebElement> iterator = actualEntries.iterator(); iterator.hasNext();) {
            WebElement webElement = (WebElement) iterator.next();
            if (!webElement.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getComboboxSection();
    }

    public String getInfoBarContent() {
        return getInfoBar().getText();
    }

    private int[] getInfoBarNumbers() {
        StringTokenizer tokenizer = new StringTokenizer(getInfoBarContent(), "-/", false);

        //parse the numbers
        String fromNumber = tokenizer.nextToken();
        String toNumber = tokenizer.nextToken();
        String count = tokenizer.nextToken();

        //convert them
        int[] tmp = new int[3];
        try {
            tmp[0] = Integer.parseInt(fromNumber);
            tmp[1] = Integer.parseInt(toNumber);
            tmp[2] = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            System.err.println("content not parsable - (maybe you forgot to check that the info bar contains text)\n"
                    + e);
            e.printStackTrace();
        }

        return tmp;
    }

    public int getFromNumber() {
        return getInfoBarNumbers()[0];
    }

    public int getToNumber() {
        return getInfoBarNumbers()[1];
    }

    public int getCount() {
        return getInfoBarNumbers()[2];
    }

    public List<WebElement> getActualEntries() {
        List<WebElement> tmp = new ArrayList<WebElement>();

        //can process the elements
        if (!getInfoBarContent().equals("")) {
            int count = getToNumber() - getFromNumber() + 1;

            for (int i = 0; i < count; i++) {
                tmp.add(getEntry(i));
            }
        }

        return tmp;
    }

    public boolean arePreviousEntriesExisting() {
        return getFromNumber() != 1;
    }

    public boolean areNextEntriesExisting() {
        return getToNumber() != getCount();
    }

    public void clickPreviousScrollButton() {
        getPreviousScrollButton().click();
    }

    public void clickNextScrollButton() {
        getNextScrollButton().click();
    }

    protected WebElement getComboboxSection() {
        return driver.findElement(By.xpath(getXPathToComboboxSection()));
    }

    protected WebElement getInfoBar() {
        return driver.findElement(By.xpath(getXPathToInfoBar()));
    }

    protected WebElement getPreviousScrollButton() {
        return driver.findElement(By.xpath(getXPathToPreviousScrollButton()));
    }

    protected WebElement getNextScrollButton() {
        return driver.findElement(By.xpath(getXPathToNextScrollButton()));
    }

    protected WebElement getContentArea() {
        return driver.findElement(By.xpath(getXPathToContentArea()));
    }

    //number is zero indexed -> number = 0 for the first entry
    protected WebElement getEntry(int number) {
        return driver.findElement(By.xpath(getXPathToEntry(number)));
    }

    private String getXPathToComboboxSection() {
        return ".//*[@id='VAADIN_COMBOBOX_OPTIONLIST']/div";
    }

    private String getXPathToInfoBar() {
        return getXPathToComboboxSection() + "/div[4]";
    }

    private String getXPathToPreviousScrollButton() {
        return getXPathToComboboxSection() + "/div[1]/span";
    }

    private String getXPathToNextScrollButton() {
        return getXPathToComboboxSection() + "/div[3]/span";
    }

    private String getXPathToContentArea() {
        return getXPathToComboboxSection() + "/div[2]/table/tbody";
    }

    //number is zero indexed -> number = 0 for the first entry
    private String getXPathToEntry(int number) {
        return getXPathToContentArea() + "/tr[" + (number + 1) + "]/td/span";
    }
}
