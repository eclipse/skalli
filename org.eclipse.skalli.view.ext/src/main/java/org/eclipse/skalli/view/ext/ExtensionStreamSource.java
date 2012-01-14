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
package org.eclipse.skalli.view.ext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.StreamResource;

public final class ExtensionStreamSource implements StreamResource.StreamSource {

    private static final long serialVersionUID = -3343186536633039815L;
    private static final Logger LOG = LoggerFactory.getLogger(ExtensionStreamSource.class);

    private final Class<? extends IconProvider> clazz;
    private final String path;

    public ExtensionStreamSource(Class<? extends IconProvider> clazz, String path) {
        this.clazz = clazz;
        this.path = path;
    }

    @Override
    public InputStream getStream() {
        Bundle bundle = FrameworkUtil.getBundle(clazz);
        if (bundle == null) {
            LOG.warn(MessageFormat.format("Could not load ''{0}'': icon provider ''{1}'' not found in any bundle", //$NON-NLS-1$
                    path, clazz.getName()));
            return null;
        }
        InputStream in = null;
        URL resource = bundle.getResource(path);
        if (resource != null) {
            try {
                in = resource.openStream();
            } catch (IOException e) {
                LOG.warn("I/O problems while opening stream", e); //$NON-NLS-1$
            }
        }
        if (in == null) {
            LOG.warn(MessageFormat.format("Could not load ''{0}'' from ''{1}'' of bundle ''{2}''", //$NON-NLS-1$
                    path, clazz.getName(), bundle.getSymbolicName()));
        }
        return in;
    }
}
