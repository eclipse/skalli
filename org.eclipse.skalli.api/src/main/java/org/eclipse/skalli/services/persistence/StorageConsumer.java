/*******************************************************************************
 * Copyright (c) 2010-2016 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.persistence;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represent a consumer of stored items.
 */
public interface StorageConsumer {

    /**
     * Consumes the provided content.
     *
     * @param category
     * @param key
     * @param lastModified
     * @param blob
     *
     * @throws IOException
     */
    public void consume(String category, String key, long lastModified, InputStream blob) throws IOException;
}
