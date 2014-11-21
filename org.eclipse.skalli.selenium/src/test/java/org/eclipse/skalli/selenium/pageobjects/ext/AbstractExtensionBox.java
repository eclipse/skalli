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
package org.eclipse.skalli.selenium.pageobjects.ext;

import org.openqa.selenium.WebDriver;

/**
 * The abstract extension box is the super type of every info box
 */
public abstract class AbstractExtensionBox extends AbstractExtension {
    public AbstractExtensionBox(WebDriver driver) {
        super(driver);
    }
}
