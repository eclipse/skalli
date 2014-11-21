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
package org.eclipse.skalli.core.rest.resources;

import org.eclipse.skalli.model.PropertyName;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("propertyUpdate")
public class PropertyUpdate {

    @PropertyName
    public static final String PROPERTY_TEMPLATE = "template"; //$NON-NLS-1$

    private String template = "";//$NON-NLS-1$

    // do not remove: required by xstream
    public PropertyUpdate() {
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
