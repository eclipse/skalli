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

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Special mapper that ignored unknown XML fields and collection entries, as XStream does not
 * support that yet. Collection entries that cannot be mapped to a suitable class are ignored.
 * Instead a {@link Noop} instance is put into the collection.
 *
 * @see http://pvoss.wordpress.com/2009/01/08/xstream and JIRA
 * entry http://jira.codehaus.org/browse/XSTR-30
 */
public class IgnoreUnknownElementsMapperWrapper extends IgnoreUnknownFieldsMapperWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(IgnoreUnknownElementsMapperWrapper.class);

    public IgnoreUnknownElementsMapperWrapper(MapperWrapper next) {
        super(next);
    }

    @Override
    public Class realClass(String elementName) {
        try {
            return super.realClass(elementName);
        } catch (CannotResolveClassException e) {
            LOG.warn(MessageFormat.format(
                    "No class for element named ''{0}'' found during entity deserialization: returning Noop instead",
                    elementName));
            return Noop.class;
        }
    }
}
