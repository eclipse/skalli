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
package org.eclipse.skalli.core.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.core.persistence.DataMigrator;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class DataMigratorTest {

    private DataMigration mock1;
    private DataMigration mock2;
    private Document mockDoc;
    private Object[] mocks;
    private Set<DataMigration> migrations;
    private Element mockRoot;

    private static class MigrationWithAlias extends DataMigrationBase {
        private boolean migrateCalled;
        public MigrationWithAlias() {
            super(TestEntityBase1.class, 0);
        }
        @Override
        public void migrate(Document doc) throws MigrationException {
            migrateCalled = true;
        }
        public boolean migrateCalled() {
            return migrateCalled;
        }
    }

    @Before
    public void setup() throws SAXException, IOException, ParserConfigurationException {

        mock1 = EasyMock.createMock(DataMigration.class);
        mock2 = EasyMock.createMock(DataMigration.class);
        mockDoc = EasyMock.createMock(Document.class);
        mockRoot = EasyMock.createMock(Element.class);
        mocks = new Object[] { mock1, mock2, mockDoc, mockRoot };

        migrations = new HashSet<DataMigration>();
        migrations.add(mock1);
        migrations.add(mock2);

        EasyMock.reset(mocks);

        mock1.getFromVersion();
        EasyMock.expectLastCall().andReturn(1).anyTimes();
        mock2.getFromVersion();
        EasyMock.expectLastCall().andReturn(2).anyTimes();

        mock1.handlesType(EasyMock.eq("mock"));
        EasyMock.expectLastCall().andReturn(true).anyTimes();
        mock2.handlesType(EasyMock.eq("mock"));
        EasyMock.expectLastCall().andReturn(true).anyTimes();

        mock1.compareTo(EasyMock.isA(DataMigration.class));
        EasyMock.expectLastCall().andReturn(-1).anyTimes();
        mock2.compareTo(EasyMock.isA(DataMigration.class));
        EasyMock.expectLastCall().andReturn(1).anyTimes();

        mockDoc.getDocumentElement();
        EasyMock.expectLastCall().andReturn(mockRoot).anyTimes();

        mockRoot.getNodeName();
        EasyMock.expectLastCall().andReturn("mock").anyTimes();

        mockRoot.getChildNodes();
        EasyMock.expectLastCall().andReturn(new NodeList() {

            @Override
            public Node item(int index) {
                // Node must have a uuid
                try {
                    return XMLUtils.documentFromString("<uuid>e4d78581-08da-4f04-8a90-a7dac41f6247</uuid>")
                            .getDocumentElement();
                } catch (Exception e) {
                    //nothing to do
                }
                return null;
            }

            @Override
            public int getLength() {
                return 1;
            }
        }).anyTimes();
    }

    @Test
    public void testMigrate() throws Exception {
        mock1.migrate(EasyMock.isA(Document.class));
        mock2.migrate(EasyMock.isA(Document.class));

        EasyMock.replay(mocks);

        DataMigrator migrator = new DataMigrator(migrations, null);
        migrator.migrate(mockDoc, 0, 3);

        EasyMock.verify(mocks);
    }

    @Test
    public void testMigrate_onlyPart() throws Exception {
        mock1.migrate(EasyMock.isA(Document.class));

        EasyMock.replay(mocks);

        DataMigrator migrator = new DataMigrator(migrations, null);
        migrator.migrate(mockDoc, 0, 2);

        EasyMock.verify(mocks);
    }

    @Test
    public void testMigrate_onlyLast() throws Exception {
        mock2.migrate(EasyMock.isA(Document.class));

        EasyMock.replay(mocks);

        DataMigrator migrator = new DataMigrator(migrations, null);
        migrator.migrate(mockDoc, 2, 3);

        EasyMock.verify(mocks);
    }

    @Test
    public void testMigrate_nothingToDo() throws Exception {
        EasyMock.replay(mocks);

        DataMigrator migrator = new DataMigrator(migrations, null);
        migrator.migrate(mockDoc, 2, 2);

        EasyMock.verify(mocks);
    }

    @Test
    public void testMigrate_noMigrations() throws Exception {
        DataMigrator migrator = new DataMigrator(null, null);
        migrator.migrate(mockDoc, 2, 2);
    }

    @Test
    public void testMigrateWithAlias() throws Exception {
        MigrationWithAlias migration = new MigrationWithAlias();
        Set<DataMigration> migrations = new HashSet<DataMigration>();
        migrations.add(migration);
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put("alias", TestEntityBase1.class);
        DataMigrator migrator = new DataMigrator(migrations, aliases);
        Document doc = XMLUtils.documentFromString("<alias><uuid>e4d78581-08da-4f04-8a90-a7dac41f6247</uuid></alias>");
        migrator.migrate(doc, 0, 1);
        Assert.assertTrue(migration.migrateCalled());
    }
}
