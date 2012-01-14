package org.eclipse.skalli.model.core.internal;

import org.eclipse.skalli.testutil.MigrationTestUtil;
import org.junit.Test;

public class DataMigration16Test {

    @Test
    public void testMigrate() throws Exception {
        DataMigration16 migration = new DataMigration16();
        MigrationTestUtil.testMigration(migration, "skalli");
    }
}
