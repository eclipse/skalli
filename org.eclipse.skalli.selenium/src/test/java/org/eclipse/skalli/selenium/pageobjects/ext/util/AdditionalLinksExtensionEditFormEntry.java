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
import java.util.Iterator;

import org.eclipse.skalli.selenium.pageobjects.AbstractPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is additional link entry
 *
 * It can be a sub link entry or a link group entry.
 *
 * It contains the link group to what it is related and the links. The map contains the name
 * of the links and the {@link WebElement}. If it is group entry the map only can contain
 * links like "remove", "up" and "down". If it is a sub link entry it contains one more link
 * which has a variable name
 */
public class AdditionalLinksExtensionEditFormEntry extends AbstractPage {
    private WebElement linkGroup = null;
    private HashMap<String, WebElement> links;

    public AdditionalLinksExtensionEditFormEntry(WebDriver driver) {
        super(driver);

        links = new HashMap<String, WebElement>();
    }

    @Override
    public boolean isDisplayed() {
        return areLinksDisplayed() && getLinkGroupElement().isDisplayed();
    }

    @Override
    protected WebElement explicitWaitReturn() {
        return getLinkGroupElement();
    }

    private boolean areLinksDisplayed() {
        HashMap<String, WebElement> linkEntries = getLinks();

        for (Iterator<WebElement> iterator = linkEntries.values().iterator(); iterator.hasNext();) {
            WebElement element = (WebElement) iterator.next();
            if (!element.isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    public String getLinkGroup() {
        return linkGroup.getText();
    }

    public void setLinkGroup(WebElement linkGroup) {
        this.linkGroup = linkGroup;
    }

    public HashMap<String, WebElement> getLinks() {
        return links;
    }

    public void setLinks(HashMap<String, WebElement> links) {
        this.links = links;
    }

    public void addLink(String name, WebElement element) {
        links.put(name, element);
    }

    protected WebElement getLinkGroupElement() {
        return this.linkGroup;
    }
}
