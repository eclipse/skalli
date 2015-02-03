/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.rest.resources;

import org.eclipse.skalli.model.ExtensionEntityBase;

public class InheritableExtension {

    private Boolean inherited;
    private ExtensionEntityBase extension;

    public InheritableExtension() {
    }

    public InheritableExtension(ExtensionEntityBase extension, Boolean inherited) {
        this.extension = extension;
        this.inherited = inherited;
    }

    /**
     * Returns the state of inheritance of this extension.
     *
     * @returns <code>Boolean.TRUE</code>/<code>Boolean.FALSE</code>, if the extension
     * is inherited/not inherited, or <code>null</code> if the state of inheritance
     * is unknown or undetermined.
     */
    public Boolean isInherited() {
        return inherited;
    }

    /**
     * Sets the state of inheritance of this extension.
     *
     * @param inherited <code>true</code>/<code>false</code>, if the extension is
     * inherited/not inherited, or <code>null</code> if the state of inheritance
     * is unknown or undetermined.
     */
    public void setInherited(Boolean inherited) {
        this.inherited = inherited;
    }

    /**
     * Returns the wrapped extension.
     */
    public ExtensionEntityBase getExtension() {
        return extension;
    }

    /**
     * Sets the wrapped extension.
     */
    public void setExtension(ExtensionEntityBase extension) {
        this.extension = extension;
    }

}
