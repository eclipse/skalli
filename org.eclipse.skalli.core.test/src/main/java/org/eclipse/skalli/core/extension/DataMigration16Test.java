/*******************************************************************************
 * Copyright (c) 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.extension;

import org.eclipse.skalli.core.extension.DataMigration16;
import org.eclipse.skalli.testutil.MigrationTestUtil;
import org.junit.Test;

public class DataMigration16Test {

    @Test
    public void testMigrate() throws Exception {
        DataMigration16 migration = new DataMigration16();
        MigrationTestUtil.testMigration(migration, "skalli");
    }
}