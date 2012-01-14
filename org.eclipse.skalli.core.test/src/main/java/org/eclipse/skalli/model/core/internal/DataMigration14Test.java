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
package org.eclipse.skalli.model.core.internal;

import org.junit.Test;

import org.eclipse.skalli.testutil.MigrationTestUtil;

@SuppressWarnings("nls")
public class DataMigration14Test {

    @Test
    public void testMigrate() throws Exception {
        DataMigration14 migration1 = new DataMigration14();
        MigrationTestUtil.testMigration(migration1, "whitespaces");

        DataMigration14 migration2 = new DataMigration14();
        MigrationTestUtil.testMigration(migration2, "nowhitespaces");
    }

}
