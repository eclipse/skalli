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

import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.persistence.StorageService;

public class PersistenceServiceXStreamMock extends PersistenceServiceXStream {

    public PersistenceServiceXStreamMock(StorageService storageService, EntityService<?> entityService,
            ExtensionService<?>... extensionServices) {
        super(new XStreamPersistence(storageService));
        bindEntityService(entityService);
        for (ExtensionService<?> extensionService : extensionServices) {
            bindExtensionService(extensionService);
        }
    }
}
