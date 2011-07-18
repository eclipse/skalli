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
package org.eclipse.skalli.model.ext.maven.internal;

import org.eclipse.skalli.testutil.MigrationTestUtil;
import org.junit.Test;

@SuppressWarnings("nls")
public class DataMigration1Test {

    @Test
    public void testMigrate() throws Exception {
        DataMigration1 migration = new DataMigration1();
        MigrationTestUtil.testMigration(migration, "skalli");
    }

}
