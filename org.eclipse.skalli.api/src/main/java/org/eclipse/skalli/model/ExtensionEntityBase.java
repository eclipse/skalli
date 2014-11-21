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
package org.eclipse.skalli.model;


public abstract class ExtensionEntityBase extends EntityBase {

    /** Non-persistent pointer of an extension entity to an extensible entity. */
    private transient ExtensibleEntityBase extensibleEntity;

    /**
     * Returns the extensible entity this extension entity is assigned to,
     * e.g. points to the project an extension is assigned to.
     */
    public ExtensibleEntityBase getExtensibleEntity() {
        return extensibleEntity;
    }

    /**
     * Sets the extensible entity this extension entity is assigned to.
     */
    public void setExtensibleEntity(ExtensibleEntityBase extensibleEntity) {
        this.extensibleEntity = extensibleEntity;
    }
}
