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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.eclipse.skalli.testutil.TestEntityBase;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtensibleEntityEntityService;
import org.eclipse.skalli.testutil.TestExtensibleEntityExtensionService;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class XStreamPersistenceComponentTest {

    private XStreamPersistenceComponent persistenceService;
    private HashMapStorageService hashMapStorageService;

    private TestEntityBase parent1;
    private TestEntityBase parent2;
    private TestEntityBase entity1;
    private TestEntityBase entity2;
    private TestEntityBase entity3;
    private TestEntityBase entity4;
    private TestEntityBase entity5;
    private TestEntityBase entity6;
    private TestEntityBase entity7;

    private List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<ServiceRegistration<?>>();

     @Before
    public void setup() throws Exception {
        serviceRegistrations.add(BundleManager.registerService(ExtensionService.class,
                new TestExtensibleEntityExtensionService(), null));
        serviceRegistrations.add(BundleManager.registerService(EntityService.class,
                new TestExtensibleEntityEntityService(0), null));
        Assert.assertEquals(2, serviceRegistrations.size());
        hashMapStorageService = new HashMapStorageService();
        persistenceService = new XStreamPersistenceComponent(hashMapStorageService);

        parent1 = new TestEntityBase(TestUUIDs.TEST_UUIDS[0]);
        parent2 = new TestEntityBase(TestUUIDs.TEST_UUIDS[1]);
        parent2.setDeleted(true);

        // non-deleted entities referencing non-deleted parent entities
        // hierarchy: entity2 -> entity1 -> parent1
        entity1 = new TestEntityBase(TestUUIDs.TEST_UUIDS[2], TestUUIDs.TEST_UUIDS[0]);
        entity2 = new TestEntityBase(TestUUIDs.TEST_UUIDS[3], TestUUIDs.TEST_UUIDS[2]);

        // non-deleted entity referencing a deleted parent entity
        entity3 = new TestEntityBase(TestUUIDs.TEST_UUIDS[4], TestUUIDs.TEST_UUIDS[1]);

        // deleted entity referencing a deleted parent entity
        entity4 = new TestEntityBase(TestUUIDs.TEST_UUIDS[5], TestUUIDs.TEST_UUIDS[1]);
        entity4.setDeleted(true);

        // deleted entity referencing a non-deleted parent entity
        entity5 = new TestEntityBase(TestUUIDs.TEST_UUIDS[6], TestUUIDs.TEST_UUIDS[0]);
        entity5.setDeleted(true);

        // siblings of entity1
        entity6 = new TestEntityBase(TestUUIDs.TEST_UUIDS[7], TestUUIDs.TEST_UUIDS[0]);
        entity7 = new TestEntityBase(TestUUIDs.TEST_UUIDS[8], TestUUIDs.TEST_UUIDS[0]);
    }

    @After
    public void tearDown() {
        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
    }

    @Test
    public void testPersistAndLoad() throws Exception {
        // create and persist test entities
        List<TestExtensibleEntityBase> expectedEntities = createTestEntityHierarchy();
        for (ExtensibleEntityBase entity : expectedEntities) {
            persistenceService.persist(entity.getClass(), entity, "anyonomous");
        }

        // read the persisted files as DOM and do some asserts
        for (TestExtensibleEntityBase entity : expectedEntities) {
            Assert.assertNotNull(entity);

            byte[] blob = hashMapStorageService.asMap().get(
                    new HashMapStorageService.Key(entity.getClass().getSimpleName(), entity.getUuid().toString()));
            Document doc = XMLUtils.documentFromString(new String(blob, "UTF-8"));

            // check that the extensible entity has been persisted with its default alias
            // and check some nodes (uuid, extensions etc.)
            NodeList nodes = doc.getElementsByTagName("entity-foobar");
            Assert.assertEquals(1, nodes.getLength());
            nodes = ((Element)nodes.item(0)).getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                assertNode(nodes.item(i), entity);
            }
        }

        // create a new instance for loading of persisted entities
        List<TestExtensibleEntityBase> actualEntities = persistenceService.getEntities(TestExtensibleEntityBase.class);
        List<TestExtensibleEntityBase> actualDeletedEntities = persistenceService.getDeletedEntities(TestExtensibleEntityBase.class);
        Assert.assertEquals(3, actualEntities.size());
        Assert.assertEquals(2, actualDeletedEntities.size());

        // assert that unmarshalled entities equal the original entities
        // see assertEquals() methods in TestExtensibleEntityBase and TestExtension
        for (TestExtensibleEntityBase actualEntity : actualEntities) {
            UUID uuid = actualEntity.getUuid();
            boolean found = false;
            for (TestExtensibleEntityBase expected : expectedEntities) {
                if (expected.getUuid().equals(uuid)) {
                    TestExtensibleEntityBase.assertEquals(expected, actualEntity);
                    found = true;
                }
            }
            if (!found) {
                Assert.fail("Should have been found");
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void testPersistUnknownParent() throws Exception {
        List<TestExtensibleEntityBase> expectedEntities = createTestEntityHierarchy();
        TestExtensibleEntityBase entity = expectedEntities.get(1);
        persistenceService.persist(entity.getClass(), entity, "anonymous");
    }

    @Test
    public void testResolveEntityRelations() throws Exception {
        XStreamPersistenceComponent p = new XStreamPersistenceComponent();
        p.registerEntityClass(TestEntityBase.class);

        // insert all the entities and calculate the relations
        updateCache(p, parent1, parent2, entity1, entity2, entity3, entity4, entity5, entity6, entity7);
        p.resolveEntityRelations(TestEntityBase.class);

        // assert that the caches contain the expected entities
        AssertUtils.assertEqualsAnyOrder("keySet()",
                CollectionUtils.asSet(TestUUIDs.TEST_UUIDS[0], TestUUIDs.TEST_UUIDS[2], TestUUIDs.TEST_UUIDS[3],
                        TestUUIDs.TEST_UUIDS[4], TestUUIDs.TEST_UUIDS[7], TestUUIDs.TEST_UUIDS[8]),
                p.keySet(TestEntityBase.class));
        AssertUtils.assertEqualsAnyOrder("deletedSet()",
                CollectionUtils.asSet(TestUUIDs.TEST_UUIDS[1], TestUUIDs.TEST_UUIDS[5], TestUUIDs.TEST_UUIDS[6]),
                p.deletedSet(TestEntityBase.class));

        // assert the parent and siblings pointers
        TestEntityBase testEntity = p.getEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[0]);
        TestEntityBase.assertEquals(parent1, testEntity);
        Assert.assertNull(testEntity.getParentEntity());
        Assert.assertNull(testEntity.getNextSibling());
        TestEntityBase.assertEquals(entity6, testEntity.getFirstChild());
        TestEntityBase.assertEquals(entity1, entity6.getNextSibling());
        TestEntityBase.assertEquals(entity2, entity1.getFirstChild());
        TestEntityBase.assertEquals(entity7, entity1.getNextSibling());
        Assert.assertNull(entity7.getNextSibling());
        TestEntityBase.assertEquals(parent1, p.getEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[2]).getParentEntity());
        TestEntityBase.assertEquals(entity1, p.getEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[3]).getParentEntity());
        Assert.assertNull(p.getDeletedEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[1]).getParentEntity());

        // non-deleted entity MUST NOT reference a deleted parent entity:
        Assert.assertNull(p.getEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[4]).getParentEntity());

        // deleted entity MAY reference a deleted parent entity
        TestEntityBase.assertEquals(parent2, p.getDeletedEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[5]).getParentEntity());

        // deleted entity MAY reference a non-deleted parent entity
        TestEntityBase.assertEquals(parent1, p.getDeletedEntity(TestEntityBase.class, TestUUIDs.TEST_UUIDS[6]).getParentEntity());

    }

    @Test
    public void testGetParentEntity() throws Exception {
        XStreamPersistenceComponent p = new XStreamPersistenceComponent();
        p.registerEntityClass(TestEntityBase.class);

        // insert all the entities and calculate the relations
        updateCache(p, parent1, parent2, entity1, entity2, entity3, entity4, entity5, entity6, entity7);
        p.resolveEntityRelations(TestEntityBase.class);

        TestEntityBase.assertEquals(parent1, entity1.getParentEntity());
        TestEntityBase.assertEquals(entity1, entity2.getParentEntity());
        Assert.assertNull(entity3.getParentEntity());
        TestEntityBase.assertEquals(parent2, entity4.getParentEntity());
        TestEntityBase.assertEquals(parent1, entity5.getParentEntity());
        TestEntityBase.assertEquals(parent1, entity6.getParentEntity());
        TestEntityBase.assertEquals(parent1, entity7.getParentEntity());

        assertParentEntity(p, null, parent1);
        assertParentEntity(p, null, parent2);
        assertParentEntity(p, parent1, entity1);
        assertParentEntity(p, entity1, entity2);
        assertParentEntity(p, null, entity3);
        assertParentEntity(p, parent2, entity4);
        assertParentEntity(p, parent1, entity5);
        assertParentEntity(p, parent1, entity6);
        assertParentEntity(p, parent1, entity7);
    }

    @Test
    public void testResolveSingleEntityRelations() throws Exception {
        XStreamPersistenceComponent p = new XStreamPersistenceComponent();
        p.registerEntityClass(TestEntityBase.class);

        updateCache(p, parent1, parent2, entity1, entity2, entity3, entity4, entity5, entity6, entity7);
        resolveRelations(p, parent1, parent2, entity1, entity2, entity3, entity4, entity5, entity6, entity7);

        assertSiblingChain(parent1, entity1, entity6, entity7);
        assertSiblingChain(parent2, entity4);
        assertSiblingChain(entity1, entity2);
        assertSiblingChain(entity2, (EntityBase[])null);
        assertSiblingChain(entity3, (EntityBase[])null);
        assertSiblingChain(entity4, (EntityBase[])null);
        assertSiblingChain(entity5, (EntityBase[])null);
        assertSiblingChain(entity6, (EntityBase[])null);
        assertSiblingChain(entity7, (EntityBase[])null);
    }

    @Test
    public void testAdjustEntityRelations() throws Exception {
        XStreamPersistenceComponent p = new XStreamPersistenceComponent();
        p.registerEntityClass(TestEntityBase.class);

        TestEntityBase parent3 = new TestEntityBase(TestUUIDs.TEST_UUIDS[10]);
        updateCache(p, parent1, parent2, parent3, entity1, entity4);
        resolveRelations(p, parent1, parent2, parent3, entity1, entity4);
        assertSiblingChain(parent1, entity1);
        assertSiblingChain(parent2, entity4);
        assertSiblingChain(parent3, (EntityBase[])null);

        // adding new children not previously in the child list
        p.adjustEntityRelations(TestEntityBase.class, null, entity6);
        assertSiblingChain(parent1, entity1, entity6);
        p.adjustEntityRelations(TestEntityBase.class, null, entity7);
        assertSiblingChain(parent1, entity1, entity6, entity7);

        // replacing an unchanged entity with itself does nothing
        p.adjustEntityRelations(TestEntityBase.class, entity1, entity1);
        assertSiblingChain(parent1, entity1, entity6, entity7);
        p.adjustEntityRelations(TestEntityBase.class, entity6, entity6);
        assertSiblingChain(parent1, entity1, entity6, entity7);
        p.adjustEntityRelations(TestEntityBase.class, entity7, entity7);
        assertSiblingChain(parent1, entity1, entity6, entity7);
        p.adjustEntityRelations(TestEntityBase.class, entity4, entity4);
        assertSiblingChain(parent2, entity4);

        // adding a deleted child to a non-deleted parent does not change
        // the child list of that parent, but the deleted child gets a parent
        p.adjustEntityRelations(TestEntityBase.class, null, entity5);
        assertSiblingChain(parent1, entity1, entity6, entity7);
        TestEntityBase.assertEquals(parent1, entity5.getParentEntity());

        // changing the content of an entity just replaces the instance in the child list
        TestEntityBase entity8 = new TestEntityBase(TestUUIDs.TEST_UUIDS[2], TestUUIDs.TEST_UUIDS[0]);
        entity8.setStr("foobar");
        p.adjustEntityRelations(TestEntityBase.class, entity1, entity8);
        assertSiblingChain(parent1, entity8, entity6, entity7);
        TestEntityBase.assertEquals(parent1, entity1.getParentEntity());
        TestEntityBase.assertEquals(parent1, entity8.getParentEntity());

        // removing the parent pointer from an entity removes it from the child list of that parent
        TestEntityBase entity9 = new TestEntityBase(TestUUIDs.TEST_UUIDS[2], null);
        p.adjustEntityRelations(TestEntityBase.class, entity8, entity9);
        assertSiblingChain(parent1, entity6, entity7);
        TestEntityBase.assertEquals(parent1, entity8.getParentEntity());
        Assert.assertNull(entity9.getParentEntity());

        // setting the parent pointer of an entity adds it to the child list of that parent
        p.adjustEntityRelations(TestEntityBase.class, entity9, entity8);
        assertSiblingChain(parent1, entity6, entity7, entity8);
        TestEntityBase.assertEquals(parent1, entity8.getParentEntity());
        Assert.assertNull(entity9.getParentEntity());

        // setting the deleted flag on an entity removes that entity from the child list
        // of its parent but does not change its parent pointer: a deleted entity can have a
        // non-deleted parent
        TestEntityBase entity10 = new TestEntityBase(TestUUIDs.TEST_UUIDS[7], TestUUIDs.TEST_UUIDS[0]);
        entity10.setDeleted(true);
        p.adjustEntityRelations(TestEntityBase.class, entity6, entity10);
        assertSiblingChain(parent1, entity7, entity8);
        TestEntityBase.assertEquals(parent1, entity6.getParentEntity());
        TestEntityBase.assertEquals(parent1, entity10.getParentEntity());

        // clearing the deleted flag of a deleted entity with a pointer to a non-deleted parent
        // adds that entity to the child list of that parent
        TestEntityBase entity11 = new TestEntityBase(TestUUIDs.TEST_UUIDS[7], TestUUIDs.TEST_UUIDS[0]);
        p.adjustEntityRelations(TestEntityBase.class, entity10, entity11);
        assertSiblingChain(parent1, entity7, entity8, entity11);
        TestEntityBase.assertEquals(parent1, entity10.getParentEntity());
        TestEntityBase.assertEquals(parent1, entity11.getParentEntity());

        // adding another deleted entity to a deleted parent
        TestEntityBase entity12 = new TestEntityBase(TestUUIDs.TEST_UUIDS[9], TestUUIDs.TEST_UUIDS[1]);
        entity12.setDeleted(true);
        p.adjustEntityRelations(TestEntityBase.class, null, entity12);
        assertSiblingChain(parent2, entity4, entity12);
        TestEntityBase.assertEquals(parent2, entity12.getParentEntity());

        // clearing the deleted flag of a deleted entity removes that entity from the child list of its parent
        // and sets its parent pointer to null: a non-deleted entity must not have a deleted parent!
        TestEntityBase entity13 = new TestEntityBase(TestUUIDs.TEST_UUIDS[5], TestUUIDs.TEST_UUIDS[1]);
        p.adjustEntityRelations(TestEntityBase.class, entity4, entity13);
        assertSiblingChain(parent2, entity12);
        TestEntityBase.assertEquals(parent2, entity4.getParentEntity());
        Assert.assertNull(entity13.getParentEntity());

        // setting the deleted flag of an entity with a pointer to a deleted parent
        // adds the entity to the child list of that parent
        TestEntityBase entity14 = new TestEntityBase(TestUUIDs.TEST_UUIDS[5], TestUUIDs.TEST_UUIDS[1]);
        entity14.setDeleted(true);
        p.adjustEntityRelations(TestEntityBase.class, entity13, entity14);
        assertSiblingChain(parent2, entity12, entity14);
        TestEntityBase.assertEquals(parent2, entity14.getParentEntity());

        // changing the parent of an entity removes the entity from the children of the old parent
        // and adds it to the children of the new parent
        TestEntityBase entity15 = new TestEntityBase(TestUUIDs.TEST_UUIDS[8], TestUUIDs.TEST_UUIDS[10]);
        p.adjustEntityRelations(TestEntityBase.class, entity7, entity15);
        assertSiblingChain(parent1, entity8, entity11);
        assertSiblingChain(parent3, entity15);
        TestEntityBase.assertEquals(parent1, entity8.getParentEntity());
        TestEntityBase.assertEquals(parent3, entity15.getParentEntity());
    }

    @Test
    public void testInsertChildEntity() throws Exception {
        XStreamPersistenceComponent p = new XStreamPersistenceComponent();
        p.registerEntityClass(TestEntityBase.class);

        // inserting entity1: parent1 has now a child, but entity1 has no siblings yet
        p.insertChildEntity(parent1, entity1);
        assertSiblingChain(parent1, entity1);

        // inserting the unchanged entity1 again has no effect
        p.insertChildEntity(parent1, entity1);
        assertSiblingChain(parent1, entity1);

        // inserting entity6 with string content "foo"
        entity6.setStr("foo");
        p.insertChildEntity(parent1, entity6);
        assertSiblingChain(parent1, entity1, entity6);

        // inserting entity7 and again entity1
        p.insertChildEntity(parent1, entity7);
        p.insertChildEntity(parent1, entity1);
        assertSiblingChain(parent1, entity1, entity6, entity7);

        // inserting entity6 again, but with a different content "bar"
        entity6.setStr("bar");
        p.insertChildEntity(parent1, entity6);
        assertSiblingChain(parent1, entity1, entity6, entity7);

        // inserting entity8, which has the same uuid as entity6
        TestEntityBase entity8 = new TestEntityBase(TestUUIDs.TEST_UUIDS[7], TestUUIDs.TEST_UUIDS[0]);
        entity8.setStr("foobar");
        p.insertChildEntity(parent1, entity8);
        assertSiblingChain(parent1, entity1, entity8, entity7);

        // inserting entity9, which has the same uuid as entity1
        TestEntityBase entity9 = new TestEntityBase(TestUUIDs.TEST_UUIDS[2], TestUUIDs.TEST_UUIDS[0]);
        entity9.setStr("foobar");
        p.insertChildEntity(parent1, entity9);
        assertSiblingChain(parent1, entity9, entity8, entity7);

        // inserting entity10, which has the same uuid as entity7
        TestEntityBase entity10 = new TestEntityBase(TestUUIDs.TEST_UUIDS[8], TestUUIDs.TEST_UUIDS[0]);
        entity10.setStr("foobar");
        p.insertChildEntity(parent1, entity10);
        assertSiblingChain(parent1, entity9, entity8, entity10);

        // adding a deleted entity to a non-deleted parent does nothing
        TestEntityBase entity11 = new TestEntityBase(TestUUIDs.TEST_UUIDS[3], TestUUIDs.TEST_UUIDS[0]);
        entity11.setDeleted(true);
        p.insertChildEntity(parent1, entity11);
        assertSiblingChain(parent1, entity9, entity8, entity10);

        // adding a deleted entity to the deleted parent2 works:
        p.insertChildEntity(parent2, entity11);
        assertSiblingChain(parent2, entity11);

        // adding a non-deleted entity to the deleted parent2 does nothing
        p.insertChildEntity(parent2, entity1);
        assertSiblingChain(parent2, entity11);
    }

    @Test
    public void testRemoveChildEntity() throws Exception {
        XStreamPersistenceComponent p = new XStreamPersistenceComponent();
        p.registerEntityClass(TestEntityBase.class);

        // removing an entity that has never been inserted has no effect
        assertSiblingChain(parent1);
        p.removeChildEntity(parent1, entity1);
        assertSiblingChain(parent1);

        // now inserting entity1: parent1 has a child
        p.insertChildEntity(parent1, entity1);
        assertSiblingChain(parent1, entity1);

        // and removing it again: parent1 has no children
        p.removeChildEntity(parent1, entity1);
        assertSiblingChain(parent1);

        // inserting some entities, then removing entity2
        p.insertChildEntity(parent1, entity1);
        p.insertChildEntity(parent1, entity6);
        p.insertChildEntity(parent1, entity7);
        p.removeChildEntity(parent1, entity6);
        assertSiblingChain(parent1, entity1, entity7);

        // removing the tail of the siblings chain
        p.removeChildEntity(parent1, entity7);
        assertSiblingChain(parent1, entity1);

        // removing entity3 again has no effect: it was never a child
        p.removeChildEntity(parent1, entity3);
        assertSiblingChain(parent1, entity1);

        // finally removing entity1: parent1 has no children
        p.removeChildEntity(parent1, entity1);
        assertSiblingChain(parent1);
    }

    private List<TestExtensibleEntityBase> createTestEntityHierarchy()
            throws Exception {
        List<TestExtensibleEntityBase> entities = new LinkedList<TestExtensibleEntityBase>();

        TestExtensibleEntityBase parentParent = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[2]);
        entities.add(parentParent);
        ExtensionEntityBase exts2[] = { createExtension(TestExtension.class, true, "foo", "1", "2"),
                createExtension(TestExtension1.class, false, "bar", "3") };
        addExtensions(parentParent, exts2);

        TestExtensibleEntityBase parent = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[1]);
        entities.add(parent);
        ExtensionEntityBase exts1[] = { createExtension(TestExtension.class, true, "Z", "F", "G", "H", "I"),
                createExtension(TestExtension1.class, true, "Y", "D", "E") };
        addExtensions(parent, exts1);
        parent.setParentEntity(parentParent);

        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        entities.add(base);
        ExtensionEntityBase exts0[] = { createExtension(TestExtension.class, false, "X", "A", "B", "C") };
        base.setInherited(TestExtension1.class, true);
        addExtensions(base, exts0);
        base.setParentEntity(parent);

        TestExtensibleEntityBase deletedParent = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[4]);
        deletedParent.setDeleted(true);
        entities.add(deletedParent);
        ExtensionEntityBase exts4[] = { createExtension(TestExtension1.class, false, "deleted parent", "d", "e") };
        addExtensions(deletedParent, exts4);

        TestExtensibleEntityBase deleted = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[3]);
        deleted.setDeleted(true);
        entities.add(deleted);
        ExtensionEntityBase exts3[] = { createExtension(TestExtension.class, false, "deleted", "a", "b", "c") };
        addExtensions(deleted, exts3);
        deleted.setParentEntity(deletedParent);

        return entities;
    }

    private void addExtensions(ExtensibleEntityBase base, ExtensionEntityBase... exts) {
        for (ExtensionEntityBase ext : exts) {
            base.addExtension(ext);
        }
    }

    private ExtensionEntityBase createExtension(Class<? extends ExtensionEntityBase> clazz,
            boolean bool, String str, String... items) throws Exception {
        TestExtension ext = (TestExtension) clazz.newInstance();
        ext.setBool(bool);
        ext.setStr(str);
        for (String item : items) {
            ext.addItem(item);
        }
        return ext;
    }

    private void updateCache(XStreamPersistenceComponent p, EntityBase...entities) {
        for (EntityBase entity: entities) {
            p.updateCache(entity);
        }
    }

    private void resolveRelations(XStreamPersistenceComponent p, EntityBase...entities) {
        for (EntityBase entity: entities) {
            p.resolveEntityRelations(TestEntityBase.class, entity);
        }
    }

    private void assertParentEntity(XStreamPersistenceComponent p, EntityBase expectedParent, EntityBase entity) {
        TestEntityBase.assertEquals(expectedParent, p.getParentEntity(TestEntityBase.class, entity));
    }

    private void assertSiblingChain(EntityBase parent, EntityBase...expectedSiblings) {
        if (expectedSiblings == null || expectedSiblings.length == 0) {
            Assert.assertNull(parent.getFirstChild());
            return;
        }
        EntityBase sibling = parent.getFirstChild();
        for (EntityBase expectedSibling: expectedSiblings) {
            Assert.assertNotNull(parent + " misses expected sibling " + expectedSibling, sibling);
            Assert.assertEquals(expectedSibling, sibling);
            sibling = sibling.getNextSibling();
        }
        Assert.assertNull(parent + " has more siblings than expected", sibling);
    }

    private void assertNode(Node node, TestExtensibleEntityBase entity) {
        String nodeName = node.getNodeName();
        if ("uuid".equals(nodeName)) {
            Assert.assertEquals(entity.getUuid().toString(), node.getTextContent());
        }
        else if ("deleted".equals(nodeName)) {
            Assert.assertEquals(Boolean.toString(entity.isDeleted()), node.getTextContent());
        }
        else if ("parentEntityId".equals(nodeName)) {
            Assert.assertEquals(entity.getParentEntityId().toString(), node.getTextContent());
        }
        else if ("extensions".equals(nodeName)) {
            assertExtensions(node, entity);
        }
        else if ("inheritedExtensions".equals(nodeName)) {
            assertInheritedExtensions(node, entity);
        }
    }

    private void assertExtensions(Node node, TestExtensibleEntityBase entity) {
        NodeList extNodes = node.getChildNodes();
        for (int j = 0; j < extNodes.getLength(); ++j) {
            Node extNode = extNodes.item(j);
            String extNodeName = extNode.getNodeName();
            if (TestExtension.class.getName().equals(extNodeName)) {
                assertExtension(extNode, entity.getExtension(TestExtension.class));
            }
            else if (TestExtension1.class.getName().equals(extNodeName)) {
                assertExtension(extNode, entity.getExtension(TestExtension1.class));
            }
        }
    }

    private void assertExtension(Node extNode, TestExtension extension) {
        Assert.assertNotNull(extension);
        NodeList nodes = extNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            if ("bool".equals(nodeName)) {
                Assert.assertEquals(Boolean.toString(extension.isBool()), node.getTextContent());
            }
            else if ("str".equals(nodeName)) {
                Assert.assertEquals(extension.getStr(), node.getTextContent());
            }
            else if ("items".equals(nodeName)) {
                assertItems(node, extension.getItems());
            }
        }
    }

    private void assertItems(Node node, List<String> items) {
        NodeList itemNodes = node.getChildNodes();
        int n = 0;
        for (int j = 0; j < itemNodes.getLength(); ++j) {
            Node itemNode = itemNodes.item(j);
            if ("string".equals(itemNode.getNodeName())) {
                Assert.assertEquals(items.get(n++), itemNode.getTextContent());
            }
        }
    }

    private void assertInheritedExtensions(Node node, TestExtensibleEntityBase entity) {
        NodeList extNodes = node.getChildNodes();
        for (int j = 0; j < extNodes.getLength(); ++j) {
            Node extNode = extNodes.item(j);
            String extNodeName = extNode.getNodeName();
            if ("string".equals(extNodeName)) {
                String inheritedEntityName = extNode.getTextContent();
                if (TestExtension.class.getName().equals(inheritedEntityName)) {
                    Assert.assertTrue(entity.isInherited(TestExtension.class));
                }
                else if (TestExtension1.class.getName().equals(inheritedEntityName)) {
                    Assert.assertTrue(entity.isInherited(TestExtension1.class));
                }
            }
        }
    }
}
