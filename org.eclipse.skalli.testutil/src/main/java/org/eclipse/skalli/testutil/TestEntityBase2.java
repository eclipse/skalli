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
package org.eclipse.skalli.testutil;

import java.util.UUID;

public class TestEntityBase2 extends TestEntityBase {

    public TestEntityBase2() {
        super();
    }

    public TestEntityBase2(UUID uuid) {
        super(uuid);
    }

    public TestEntityBase2(UUID uuid, UUID parentEntityId) {
        super(uuid, parentEntityId);
    }
}
