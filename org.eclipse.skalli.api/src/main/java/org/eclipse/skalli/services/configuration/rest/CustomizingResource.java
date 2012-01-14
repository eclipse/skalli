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
package org.eclipse.skalli.services.configuration.rest;

import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.services.configuration.ConfigurationService;

import com.thoughtworks.xstream.XStream;

/**
 * Representation of application customization parameters.
 */
public abstract class CustomizingResource<T> extends ConfigResourceBase<T> {

    /**
     * Defines the name which will be used to store the customizing entity in the file system.
     * @return
     */
    protected abstract String getKey();

    /**
     * Defines classes (in addition to  {@link #getConfigClass()}) that should be known to (and parsed for annotations by) the serializer.
     * @return
     */
    protected List<Class<?>> getAdditionalConfigClasses() {
        return Collections.emptyList();
    };

    @Override
    protected final XStream getXStream() {
        XStream xstream = super.getXStream();
        for (Class<?> additionalClass : getAdditionalConfigClasses()) {
            xstream.processAnnotations(additionalClass);
        }
        return xstream;
    }

    @Override
    protected final void storeConfig(ConfigurationService configService, T configObject) {
        configService.writeCustomization(getKey(), configObject);
    }

    @Override
    protected final T readConfig(ConfigurationService configService) {
        return configService.readCustomization(getKey(), getConfigClass());
    }

}
