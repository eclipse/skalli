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
package org.eclipse.skalli.core.internal.persistence.xstream;

import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Special mapper so that unknown XML fields are ignored, as XStream does not
 * support that yet. See http://pvoss.wordpress.com/2009/01/08/xstream and JIRA
 * entry http://jira.codehaus.org/browse/XSTR-30
 */
public class IgnoreUnknownFieldsMapperWrapper extends MapperWrapper {

    public IgnoreUnknownFieldsMapperWrapper(MapperWrapper next) {
        super(next);
    }

    @Override
    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
        if (definedIn == Object.class) {
            return false;
        }
        return super.shouldSerializeMember(definedIn, fieldName);
    }
}
