package org.eclipse.skalli.model.core.internal;

import org.eclipse.skalli.testutil.MigrationTestUtil;
import org.junit.Test;

public class DataMigration19Test {

    @Test
    public void testMigrate() throws Exception {
        DataMigration19 migration = new DataMigration19();
        MigrationTestUtil.testMigration(migration, "skalli");
    }

}
