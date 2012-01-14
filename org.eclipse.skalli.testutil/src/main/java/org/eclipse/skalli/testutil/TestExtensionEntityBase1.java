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

import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;

public class TestExtensionEntityBase1 extends ExtensionEntityBase {
    public TestExtensionEntityBase1() {
    }

    public TestExtensionEntityBase1(UUID uuid) {
        setUuid(uuid);
    }
}
