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
package org.eclipse.skalli.core.internal.persistence.xstream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.eclipse.skalli.testutil.HashMapStorageService.Key;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class XStreamPersistenceTest {

    private static final String ALIAS_EXT1 = "ext1";
    private static final String ALIAS_EXT2 = "ext2";
    private static final String TIME0 = "2002-12-05T05:22:11Z";
    private static final String TIME1 = "2011-02-25T14:33:48";
    private static final String TIME2 = "2010-06-17T21:00:23Z";
    private static final String USER0 = "hugo";
    private static final String USER1 = "homer";
    private static final String USER2 = "marge";

    private static final String TEXT1 = "XStreamPersistenceTest greets the world!";

    private static final String XML_WITH_VERSION = "<bla version=\"42\"><uuid>" + PropertyHelperUtils.TEST_UUIDS[0]
            + "</uuid><hello>world</hello><blubb>noop</blubb></bla>";
    private static final String XML_WITHOUT_VERSION = "<bla><uuid>" + PropertyHelperUtils.TEST_UUIDS[0]
            + "</uuid><hello>world</hello><blubb>noop</blubb></bla>";

    private static final String XML_WITH_MODIFIED_ATTRIBUTES =
            "<root lastModified=\"" + TIME0 + "\" modifiedBy=\"" + USER0 + "\"><extensions>" +
                    "<" + ALIAS_EXT1 + " lastModified=\"" + TIME1 + "\" modifiedBy=\"" + USER1 + "\"></" + ALIAS_EXT1
                    + ">" +
                    "<" + ALIAS_EXT2 + " lastModified=\"" + TIME2 + "\" modifiedBy=\"" + USER2 + "\"></" + ALIAS_EXT2
                    + ">" +
                    "</extensions></root>";

    private static final String XML_WITH_EXTENSIONS =
            "<root><extensions>" +
                    "<" + ALIAS_EXT1 + "><string>string</string></" + ALIAS_EXT1 + ">" +
                    "<" + ALIAS_EXT2 + "></" + ALIAS_EXT2 + ">" +
                    "</extensions></root>";

    private static final String XML_WITH_EXTENSIONS_MODIFIED =
            "<root><extensions>" +
                    "<" + ALIAS_EXT1 + "><string>modified_string</string></" + ALIAS_EXT1 + ">" +
                    "<" + ALIAS_EXT2 + "></" + ALIAS_EXT2 + ">" +
                    "</extensions></root>";

    private static Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put(ALIAS_EXT1, TestExtension.class);
        aliases.put(ALIAS_EXT2, TestExtension1.class);
        return aliases;
    }

    private static Map<String, Class<?>> getNotMatchingAliases() {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put("notext1", TestExtension.class);
        aliases.put("notext2", TestExtension1.class);
        return aliases;
    }

    private static TestExtensibleEntityBase getExtensibleEntity() {
        TestExtensibleEntityBase entity = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[0]);
        entity.addExtension(new TestExtension());
        entity.addExtension(new TestExtension1());
        return entity;
    }

    private static DataMigration getMigrationMock() throws Exception {
        DataMigration mockMigration = EasyMock.createMock(DataMigration.class);
        EasyMock.reset(mockMigration);
        mockMigration.handlesType(EasyMock.isA(String.class));
        EasyMock.expectLastCall().andReturn(true).anyTimes();
        mockMigration.getFromVersion();
        EasyMock.expectLastCall().andReturn(42).anyTimes();
        mockMigration.migrate(EasyMock.isA(Document.class));
        EasyMock.expectLastCall();
        return mockMigration;
    }

    private static class TestXStreamPersistence extends XStreamPersistence {

        private int currentVersion;

        public TestXStreamPersistence(int currentVersion) {
            super(new HashMapStorageService());
            this.currentVersion = currentVersion;
        }

        public TestXStreamPersistence() {
            this(CURRENT_XSTREAM_PERSISTENCE_VERSION);
        }

        @Override
        public int getCurrentVersion() {
            return currentVersion;
        }

        private HashMapStorageService getHashMapStorageService() {
            return (HashMapStorageService) super.getStorageService();
        }

        private static Key getHashMapKeyForEntiry(TestExtensibleEntityBase entity)
        {
            return new HashMapStorageService.Key(TestExtensibleEntityBase.class.getSimpleName(),
                    entity.getUuid().toString());
        }

        private byte[] getContentFromHashMap(TestExtensibleEntityBase entityKey) {
            return getHashMapStorageService().getBlobStore().get(getHashMapKeyForEntiry(entityKey));
        }

        private Document getDocumentFromHashMap(TestExtensibleEntityBase entityKey) throws Exception {
            return XMLUtils.documentFromString(new String(getContentFromHashMap(entityKey)));
        }

    }

    private static final int CURRENT_XSTREAM_PERSISTENCE_VERSION = 43;

    @Test
    public void testSaveLoadCycle() throws Exception {

        TestExtensibleEntityBase entity = getExtensibleEntity();
        TestExtension ext1 = entity.getExtension(TestExtension.class);
        ext1.setStr(TEXT1);
        ext1.setBool(false);

        Map<String, Class<?>> aliases = getAliases();

        TestXStreamPersistence xp = new TestXStreamPersistence(CURRENT_XSTREAM_PERSISTENCE_VERSION);

        //saveEntity
        Calendar beforeSaveDate = Calendar.getInstance();
        xp.saveEntity(entity, USER0, aliases);
        Calendar afterSaveDate = Calendar.getInstance();

        //test that entiy is now in the HashMap available and lastModified is set.
        Document savedHashMapDoc = xp.getDocumentFromHashMap(entity);
        String lastModified = xp.getLastModifiedAttribute(savedHashMapDoc.getDocumentElement());
        assertNotNull(lastModified);
        assertIsXsdDateTime(lastModified);
        Calendar lastModifiedDate = DatatypeConverter.parseDateTime(lastModified);
        assertTrue(beforeSaveDate.compareTo(lastModifiedDate) <= 0);
        assertTrue(lastModifiedDate.compareTo(afterSaveDate) <= 0);

        //test that lastModifieddate is set for extensions as well
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(savedHashMapDoc, aliases);
        assertEquals(2, extensions.size());
        String lastModifiedExt1 = xp.getLastModifiedAttribute(extensions.get(ALIAS_EXT1));
        assertIsXsdDateTime(lastModifiedExt1);
        String lastModifiedExt2 = xp.getLastModifiedAttribute(extensions.get(ALIAS_EXT1));
        assertIsXsdDateTime(lastModifiedExt2);

        //loadEntity again
        Set<ClassLoader> entityClassLoaders = getTestExtensibleEntityBaseClassLodades();
        TestExtensibleEntityBase loadedEntity = xp.loadEntity(entity.getClass(), entity.getUuid(), entityClassLoaders,
                null, aliases);
        assertLoadeEntityIsExpectedOne(loadedEntity, USER0, USER0, USER0, lastModified, lastModifiedExt1,
                lastModifiedExt2, TEXT1, false);

        // and check that loadEntities can read it
        List<? extends TestExtensibleEntityBase> loadedEndities = xp.loadEntities(entity.getClass(),
                entityClassLoaders, null, aliases);
        assertEquals(1, loadedEndities.size());
        assertLoadeEntityIsExpectedOne(loadedEndities.get(0), USER0, USER0, USER0, lastModified, lastModifiedExt1,
                lastModifiedExt2, TEXT1,
                false);

        //change the enity and save  again
        ext1 = entity.getExtension(TestExtension.class);
        ext1.setStr(TEXT1 + " is now updated");
        xp.saveEntity(entity, USER1, aliases);

        TestExtensibleEntityBase updatedEntity = xp.loadEntity(entity.getClass(), entity.getUuid(), entityClassLoaders,
                null, aliases);
        Document updatedHashMapDoc = xp.getDocumentFromHashMap(entity);
        lastModified = xp.getLastModifiedAttribute(updatedHashMapDoc.getDocumentElement());
        SortedMap<String, Element> updatedExtensions = xp.getExtensionsByAlias(updatedHashMapDoc,
                aliases);
        lastModifiedExt1 = xp.getLastModifiedAttribute(updatedExtensions.get(ALIAS_EXT1));
        assertLoadeEntityIsExpectedOne(updatedEntity, USER1, USER1, USER0, lastModified, lastModifiedExt1,
                lastModifiedExt2, TEXT1
                        + " is now updated",
                false);

    }

    private void assertLoadeEntityIsExpectedOne(TestExtensibleEntityBase loadedEntity, String user, String userExt1,
            String userExt2, String lastModified,
            String lastModifiedExt1,
            String lastModifiedExt2, String ext1Text, boolean ext1boolean) {
        assertNotNull(loadedEntity);
        assertEquals("wrong lastModified", lastModified, loadedEntity.getLastModified());
        assertEquals(user, loadedEntity.getLastModifiedBy());
        TestExtension ext1 = ((TestExtensibleEntityBase) loadedEntity).getExtension(TestExtension.class);
        assertNotNull(ext1);
        assertEquals("wrong lastModifiedExt1", lastModifiedExt1, ext1.getLastModified());
        assertEquals(ext1Text, ext1.getStr());
        assertEquals(ext1boolean, ext1.isBool());
        assertEquals(userExt1, ext1.getLastModifiedBy());
        TestExtension1 ext2 = ((TestExtensibleEntityBase) loadedEntity).getExtension(TestExtension1.class);
        assertNotNull(ext2);
        assertEquals("wrong lastModifiedExt2", lastModifiedExt2, ext2.getLastModified());
        assertEquals(userExt2, ext2.getLastModifiedBy());
    }

    private Set<ClassLoader> getTestExtensibleEntityBaseClassLodades() {
        Set<ClassLoader> entityClassLoaders = new HashSet<ClassLoader>();
        entityClassLoaders.add(TestExtensibleEntityBase.class.getClassLoader());
        entityClassLoaders.add(TestExtension.class.getClassLoader());
        entityClassLoaders.add(TestExtension1.class.getClassLoader());
        return entityClassLoaders;
    }

    @Test
    public void testPreProcessXML() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence(CURRENT_XSTREAM_PERSISTENCE_VERSION);
        DataMigration mockMigration = getMigrationMock();
        EasyMock.replay(mockMigration);
        Document doc = XMLUtils.documentFromString(XML_WITH_VERSION);
        xp.preProcessXML(doc, Collections.singleton(mockMigration), null);
        String res = XMLUtils.documentToString(doc);

        // TODO: Insnt there something wrong here?
        // isn't the expect version CURRENT_XSTREAM_PERSISTENCE_VERSION instead of the one out of the XML_WITH_VERSION=42
        String expected = "version=\"" + 42 + "\"";
        assertTrue(res.contains(expected));
        EasyMock.verify(mockMigration);
    }

    @Test
    public void testPostProcessEntity() throws Exception {
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> aliases = getAliases();
        TestExtensibleEntityBase entity = getExtensibleEntity();
        XStreamPersistence xp = new TestXStreamPersistence();
        xp.postProcessEntity(doc, entity, aliases);
        assertEquals(TIME0, entity.getLastModified());
        assertEquals(USER0, entity.getLastModifiedBy());
        TestExtension ext1 = entity.getExtension(TestExtension.class);
        assertNotNull(ext1);
        assertEquals(TIME1, ext1.getLastModified());
        assertEquals(USER1, ext1.getLastModifiedBy());
        TestExtension ext2 = entity.getExtension(TestExtension1.class);
        assertNotNull(ext2);
        assertEquals(TIME2, ext2.getLastModified());
        assertEquals(USER2, ext2.getLastModifiedBy());
    }

    @Test
    public void testPostProcessXML() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document oldDoc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        Document newDoc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS_MODIFIED);
        Map<String, Class<?>> aliases = getAliases();
        xp.postProcessXML(newDoc, oldDoc, aliases, USER0);
        Element documentElement = newDoc.getDocumentElement();
        assertIsXsdDateTime(xp.getLastModifiedAttribute(documentElement));
        assertEquals(USER0, xp.getLastModifiedByAttribute(documentElement));
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(newDoc, aliases);
        Element ext1 = extensions.get(ALIAS_EXT1);
        assertIsXsdDateTime(xp.getLastModifiedAttribute(ext1));
        assertEquals(USER0, xp.getLastModifiedByAttribute(ext1));
        Element ext2 = extensions.get(ALIAS_EXT2);
        assertNull(xp.getLastModifiedAttribute(ext2));
        assertNull(xp.getLastModifiedByAttribute(ext2));
        assertEquals(xp.getCurrentVersion(), xp.getVersionAttribute(newDoc));
    }

    @Test
    public void testPostProcessXMLUnchangedDocument() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        Map<String, Class<?>> aliases = getAliases();
        xp.postProcessXML(doc, doc, aliases, USER0);
        Element documentElement = doc.getDocumentElement();
        assertNull(xp.getLastModifiedAttribute(documentElement));
        assertNull(xp.getLastModifiedByAttribute(documentElement));
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, aliases);
        for (Element element : extensions.values()) {
            assertNull(xp.getLastModifiedAttribute(element));
            assertNull(xp.getLastModifiedByAttribute(element));
        }
        assertEquals(xp.getCurrentVersion(), xp.getVersionAttribute(doc));
    }

    @Test
    public void testGetExtensionsByAlias() throws Exception {
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        Map<String, Class<?>> aliases = getAliases();
        XStreamPersistence xp = new TestXStreamPersistence();
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, aliases);
        assertEquals(2, extensions.size());
        for (String alias : extensions.keySet()) {
            assertTrue(aliases.containsKey(alias));
            assertEquals(alias, extensions.get(alias).getNodeName());
        }

        //check that the content of ext1 is the expected one
        assertEquals("string", extensions.get("ext1").getFirstChild().getNodeName());
        assertEquals("string", extensions.get("ext1").getFirstChild().getTextContent());
    }

    @Test
    public void testGetExtensionsByClassName() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> aliases = getAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByClassName(doc, aliases);
        assertEquals(2, extensions.size());
        for (String alias : aliases.keySet()) {
            String className = aliases.get(alias).getName();
            assertTrue(extensions.containsKey(className));
            assertEquals(alias, extensions.get(className).getNodeName());
        }
    }

    @Test
    public void testGetExtensionsNoMatchingAliases() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_MODIFIED_ATTRIBUTES);
        Map<String, Class<?>> notMatchingAliases = getNotMatchingAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(doc, notMatchingAliases);
        assertNotNull(extensions);
        assertTrue(extensions.isEmpty());
        extensions = xp.getExtensionsByClassName(doc, notMatchingAliases);
        assertNotNull(extensions);
        assertTrue(extensions.isEmpty());
    }

    @Test
    public void testVersionAttribute() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        assertEquals(0, xp.getVersionAttribute(XMLUtils.documentFromString(XML_WITHOUT_VERSION)));
        assertEquals(42, xp.getVersionAttribute(XMLUtils.documentFromString(XML_WITH_VERSION)));
    }

    @Test
    public void testSetVersionAttribute() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        xp.setVersionAttribute(doc);
        assertEquals(xp.getCurrentVersion(), xp.getVersionAttribute(doc));
    }

    @Test
    public void testSetLastModiefiedAttribute() throws SAXException, IOException, ParserConfigurationException
    {
        XStreamPersistence xp = new TestXStreamPersistence();
        Element element = XMLUtils.documentFromString("<dummy></dummy>").getDocumentElement();
        xp.setLastModifiedAttribute(element);
        Attr lastMod = element.getAttributeNode("lastModified");
        assertIsXsdDateTime(lastMod.getTextContent());

    }

    @Test
    public void testCallSetVersionAttributeTwice() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        xp.setVersionAttribute(doc);
        try {
            xp.setVersionAttribute(doc);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("element already has a 'version' attribute"));
        }

    }

    @Test
    public void testLastModified() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedAttribute(documentElement);
        assertIsXsdDateTime(xp.getLastModifiedAttribute(documentElement));
    }

    @Test(expected = RuntimeException.class)
    public void testCallSetLastModifiedTwice() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedAttribute(documentElement);
        xp.setLastModifiedAttribute(documentElement);
    }

    @Test
    public void testLastModifiedBy() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedByAttribute(documentElement, USER0);
        assertEquals(USER0, xp.getLastModifiedByAttribute(documentElement));
    }

    @Test
    public void testCallSetLastModifiedByTwice() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedByAttribute(documentElement, USER0);
        try {
            xp.setLastModifiedByAttribute(documentElement, USER0);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("element already has a 'modifiedBy' attribute"));
        }

    }

    @Test
    public void testNonIdenticalElements() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        Element documentElement = doc.getDocumentElement();
        Map<String, Class<?>> aliases = getAliases();
        SortedMap<String, Element> extensions = xp.getExtensionsByClassName(doc, aliases);
        assertTrue(extensions.size() > 0);
        for (Element element : extensions.values()) {
            assertFalse(XMLDiff.identical(documentElement, element));
        }
    }

    @Test
    public void testIdenticalWithNullParams() throws Exception {
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        assertTrue(XMLDiff.identical(null, null));
        assertFalse(XMLDiff.identical(null, documentElement));
        assertFalse(XMLDiff.identical(documentElement, null));
        assertTrue(XMLDiff.identical(documentElement, documentElement));
    }

    private void assertIsXsdDateTime(String lexicalXSDDateTime) {
        assertTrue(StringUtils.isNotBlank(lexicalXSDDateTime));
        DatatypeConverter.parseDateTime(lexicalXSDDateTime);
    }

    @Test
    public void testStorageService() {
        StorageService storageService = new HashMapStorageService();
        XStreamPersistence xp = new XStreamPersistence(storageService);
        assertEquals(storageService, xp.getStorageService());
    }

}
