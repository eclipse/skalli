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
package org.eclipse.skalli.selenium.pageobjects.ext.editform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.AdditionalLinksExtensionEditFormEntry;
import org.eclipse.skalli.selenium.pageobjects.ext.util.PositionProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * This is the extension edit form for the additional links extension
 */
public class AdditionalLinksExtensionEditForm extends AbstractExtensionEditForm {
    public AdditionalLinksExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getAdditionalInformationExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return areLinkEntriesDisplayed() && getAddLinkElement().isDisplayed();
    }

    private boolean areLinkEntriesDisplayed() {
        List<AdditionalLinksExtensionEditFormEntry> linkEntries = getLinkEntries();

        for (Iterator<AdditionalLinksExtensionEditFormEntry> iterator = linkEntries.iterator(); iterator.hasNext();) {
            AdditionalLinksExtensionEditFormEntry linkEntry = (AdditionalLinksExtensionEditFormEntry) iterator.next();
            if (!linkEntry.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    public List<AdditionalLinksExtensionEditFormEntry> getLinkEntries() {
        List<AdditionalLinksExtensionEditFormEntry> tmp = new ArrayList<AdditionalLinksExtensionEditFormEntry>();

        int numberOfLinkElements = getNumberOfLinkElements();
        List<WebElement> linkGroups = getLinkGroups();

        int currentLinkGroup = 0;

        boolean isLastElementAnOptionLinksElement = false;

        AdditionalLinksExtensionEditFormEntry entry = null;
        HashMap<String, WebElement> tmpLinks = new HashMap<String, WebElement>();
        WebElement tmpSubLink;

        //-> 1. -> option links for the link group (sub link section follows -> step 2.)
        //-> 2. -> sub link element (option links section follows -> step 3.)
        //-> 3. -> option links for the sub link element (see step 4.)
        //-> 4. -> 1. (isLastElementAnOptionLinksElement == true) or step 2. - 3.

        //subtracting one div for the add link section
        for (int currentDiv = 1; currentDiv <= numberOfLinkElements - linkGroups.size() - 1; currentDiv++) {
            //search sub link
            if (isLastElementAnOptionLinksElement && ((tmpSubLink = getSubLink(currentDiv)) != null)) {
                //sub link found
                entry.setLinkGroup(linkGroups.get(currentLinkGroup));
                tmpLinks.put(tmpSubLink.getText(), tmpSubLink);

                isLastElementAnOptionLinksElement = false;

                continue;
            }

            //search option links
            HashMap<String, WebElement> tmpMap;
            if ((tmpMap = getOptionLinks(currentDiv)) != null) {
                //option links found

                //tmpMap used to not overwrite the sub link
                tmpLinks.putAll(tmpMap);

                //option links section follows and option links section -> out of the sub link section
                if (isLastElementAnOptionLinksElement) {
                    ++currentLinkGroup;
                }

                //add links
                entry = PageFactory.initElements(driver, AdditionalLinksExtensionEditFormEntry.class);
                entry.setLinkGroup(linkGroups.get(currentLinkGroup));
                entry.setLinks(tmpLinks);

                tmp.add(entry);

                //reset links
                tmpLinks = new HashMap<String, WebElement>();
                //reset entry
                entry = PageFactory.initElements(driver, AdditionalLinksExtensionEditFormEntry.class);

                isLastElementAnOptionLinksElement = true;

                continue;
            }
        }

        return tmp;
    }

    private HashMap<String, WebElement> getOptionLinks(int currentDiv) {
        //try to get the option links
        try {
            HashMap<String, WebElement> links = new HashMap<String, WebElement>();

            //if it can be found it is a link group element (a WebElement containing the name of a link group)
            WebElement rootElement = driver.findElement(By.xpath(getXPathToLinkSection() + "/div[" + currentDiv
                    + "]/div/div/div"));

            //process all links
            By xPathToLinkElement = By.xpath("./div/div/span/span");
            List<WebElement> linkRootElements = rootElement.findElements(By.xpath("./div"));
            int size = linkRootElements.size();
            for (int i = 1; i <= size; i++) {
                WebElement linkRootElement = linkRootElements.get(i);

                //there can be empty div's
                if (linkRootElement.findElements(By.xpath("./*")).size() == 0) {
                    continue;
                }

                WebElement linkElement = linkRootElement.findElement(xPathToLinkElement);
                links.put(linkElement.getText(), linkElement);
            }

            return links;
        } catch (Exception e) {
        }

        return null;
    }

    private WebElement getSubLink(int currentDiv) {
        //try to get the sub link (under a link group)
        try {
            //if it can be found it is a sub link element (a WebElement containing the name of a sub link)
            WebElement subLinkElement = driver.findElement(By.xpath(getXPathToLinkSection() + "/div[" + currentDiv
                    + "]/div/div/span/span"));

            return subLinkElement;
        } catch (Exception e) {
        }

        return null;
    }

    public List<WebElement> getLinkGroups() {
        //try to find out the link groups
        List<WebElement> linkGroups = new ArrayList<WebElement>();

        WebElement root = driver.findElement(By.xpath(getXPathToLinkSection()));
        List<WebElement> linkElementsRoot = root.findElements(By.xpath("./div"));

        //the link groups are in the div's at the end of the link elements section
        for (int currentDiv = linkElementsRoot.size() - 1; currentDiv >= 0; currentDiv--) {
            try {
                //if it can be found it is a link group element (a WebElement containing the name of a link group)
                WebElement linkGroupElement = linkElementsRoot.get(currentDiv).findElement(By.xpath("./div/div"));

                //the path of linkGroupElement have to be complete (nothing follows)
                if (linkGroupElement.findElements(By.xpath("./*")).size() > 0) {
                    break;
                }

                linkGroups.add(0, linkGroupElement);
            } catch (Exception e) {
                break;
            }
        }

        return linkGroups;
    }

    public void clickAddLink() {
        getAddLinkElement().click();
    }

    protected WebElement getAddLinkElement() {
        int currentDiv = getNumberOfLinkElements() - getLinkGroups().size();

        //the add link element is directly before the link group elements
        WebElement addLinkElement = driver.findElement(By.xpath(getXPathToLinkSection() + "/div[" + currentDiv
                + "]/div/div/span/span"));

        return addLinkElement;
    }

    //driver must be located on the edit form
    private int getNumberOfLinkElements() {
        WebElement root = driver.findElement(By.xpath(getXPathToLinkSection()));

        List<WebElement> linkElementsRoot = root.findElements(By.xpath("./div"));

        return linkElementsRoot.size();
    }

    private String getXPathToLinkSection() {
        return getXPathToExtensionContainer()
                + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr/td[3]/div/div/div/div";
    }
}
