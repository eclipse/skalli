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
package org.eclipse.skalli.testutil;

import java.util.Iterator;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;

public class TestExtensibleEntityBase extends ExtensibleEntityBase {

    public TestExtensibleEntityBase() {
    }

    public TestExtensibleEntityBase(UUID uuid) {
        setUuid(uuid);
    }

    public static boolean assertEquals(TestExtensibleEntityBase o1, TestExtensibleEntityBase o2) {
        boolean equals = true;
        equals &= o1.getUuid().equals(o2.getUuid());
        equals &= o1.isDeleted() && o2.isDeleted();
        UUID parentId = o1.getParentEntityId();
        if (parentId != null) {
            equals &= parentId.equals(o2.getParentEntityId());
        } else {
            equals &= o2.getParentEntityId() == null;
        }
        Iterator<ExtensionEntityBase> it = o1.getAllExtensions().iterator();
        for (ExtensionEntityBase ext : o2.getAllExtensions()) {
            equals &= TestExtension.assertEquals((TestExtension) ext, (TestExtension) it.next());
            equals &= o1.isInherited(ext.getClass()) && o2.isInherited(ext.getClass());
        }
        return equals;
    }
}
