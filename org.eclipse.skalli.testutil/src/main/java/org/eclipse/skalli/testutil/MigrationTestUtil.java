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

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.services.extension.DataMigration;
import org.junit.Assert;
import org.w3c.dom.Document;

@SuppressWarnings("nls")
public class MigrationTestUtil extends XMLDiffUtil {

    /**
     * Tests a {@link DataMigration} by applying the migration to a file and comparing it to a control file.
     *
     * <p>
     * Naming conventions for the test data:
     * <li>in the test fragment, use a folder called <code>/res/migrations/&lt;MigrationClassName&gt;/</code>
     * <li>add an original file <code>&lt;filenamePrefix&gt;.xml.before</code>
     * <li>add the manually converted control file <code>&lt;filenamePrefix&gt;.xml.after</code>
     * As an example, have a look at the DataMigration11 test in the model.core bundle.
     * </p>
     *
     * @param migration  the data migration to execute.
     * @param filenamePrefix  the prefix of the <code>&lt;filenamePrefix&gt;.xml.before</code> and
     * <code>&lt;filenamePrefix&gt;.xml.after</code> files.
     */
    public static void testMigration(DataMigration migration, String filenamePrefix) throws Exception {
        String pathPrefix = "/res/migrations/" + migration.getClass().getSimpleName() + "/" + filenamePrefix;
        Class<?> migrationClass = migration.getClass();
        Document docBefore = XMLUtils.documentFromResource(migrationClass, pathPrefix + ".xml.before");
        migration.migrate(docBefore);
        Document docAfter = XMLUtils.documentFromResource(migrationClass, pathPrefix + ".xml.after");

        Assert.assertEquals(String.valueOf(migration.getFromVersion()),
                docBefore.getDocumentElement().getAttribute("version"));
        assertEquals(docBefore, docAfter, true);
    }
}