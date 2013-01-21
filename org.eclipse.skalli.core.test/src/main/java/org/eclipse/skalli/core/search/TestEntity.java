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
package org.eclipse.skalli.core.search;

import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;

class TestEntity extends ExtensionEntityBase {
    private String value;
    private String facet;

    public TestEntity(String value, String facet) {
        this.value = value;
        this.facet = facet;
        setUuid(UUID.randomUUID());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFacet() {
        return facet;
    }

    public void setFacet(String facet) {
        this.facet = facet;
    }

}
