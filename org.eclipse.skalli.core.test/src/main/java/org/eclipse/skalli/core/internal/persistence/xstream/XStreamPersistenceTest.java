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

import static org.hamcrest.Matchers.startsWith;
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
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.eclipse.skalli.testutil.HashMapStorageService.Key;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtensibleEntityEntityService;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.converters.Converter;

@SuppressWarnings("nls")
public class XStreamPersistenceTest {

    private static final String ALIAS_EXT1 = "ext1";
    private static final String ALIAS_EXT2 = "ext2";
    private static final String TIME0 = "2002-12-05T05:22:11Z";
    private static final String UPDATED_TIME0 = ">" + TIME0;
    private static final String TIME1 = "2011-02-25T14:33:48";
    private static final String TIME2 = "2010-06-17T21:00:23Z";
    private static final String USER0 = "hugo";
    private static final String USER1 = "homer";
    private static final String USER2 = "marge";
    private static final String PERSIST_USER = "skalli";

    private static final String ANY_XSD_TIME = "ANY_XSD_TIME";
    private static final String VALUE1 = "string";
    private static final String VALUE2 = "another_string";
    private static final String MODIFIED_VALUE = "modified_string";

    private static final String[] ALIASES = { ALIAS_EXT1, ALIAS_EXT2 };
    private static final String[] VALUES = { VALUE1, VALUE2 };
    private static final String[] LAST_MODIFIED = { TIME1, TIME2 };
    private static final String[] LAST_MODIFIED_BY = { USER1, USER2 };

    private static final String TEXT1 = "XStreamPersistenceTest greets the world!";

    private static final String XML_WITH_VERSION = "<bla version=\"42\"><uuid>" + TestUUIDs.TEST_UUIDS[0]
            + "</uuid><hello>world</hello><blubb>noop</blubb></bla>";
    private static final String XML_WITHOUT_VERSION = "<bla><uuid>" + TestUUIDs.TEST_UUIDS[0]
            + "</uuid><hello>world</hello><blubb>noop</blubb></bla>";

    private static final String XML_WITH_EXTENSIONS = createXML(TIME0, USER0, ALIASES, VALUES, LAST_MODIFIED, LAST_MODIFIED_BY);

    private static Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put(ALIAS_EXT1, TestExtension.class);
        aliases.put(ALIAS_EXT2, TestExtension1.class);
        return aliases;
    }

    private static <T extends EntityBase> Set<Converter> getConverters() {
        return CollectionUtils.asSet(new NoopConverter(), new UUIDListConverter(), new ExtensionsMapConverter());
    }

    private static Map<String, Class<?>> getNotMatchingAliases() {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put("notext1", TestExtension.class);
        aliases.put("notext2", TestExtension1.class);
        return aliases;
    }

    private static TestExtensibleEntityBase getExtensibleEntity() {
        TestExtensibleEntityBase entity = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
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

        public TestXStreamPersistence() {
            super(new HashMapStorageService());
        }

        private HashMapStorageService getHashMapStorageService() {
            return (HashMapStorageService) super.getStorageService();
        }

        private static Key getHashMapKeyForEntry(TestExtensibleEntityBase entity)
        {
            return new HashMapStorageService.Key(TestExtensibleEntityBase.class.getSimpleName(),
                    entity.getUuid().toString());
        }

        private byte[] getContentFromHashMap(TestExtensibleEntityBase entityKey) {
            return getHashMapStorageService().getBlobStore().get(getHashMapKeyForEntry(entityKey));
        }

        private Document getDocumentFromHashMap(TestExtensibleEntityBase entityKey) throws Exception {
            return XMLUtils.documentFromString(new String(getContentFromHashMap(entityKey)));
        }

    }

    private static final int CURRENT_MODEL_VERSION = 43;

    @Test
    public void testSaveLoadCycle() throws Exception {

        TestExtensibleEntityBase entity = getExtensibleEntity();
        TestExtensibleEntityEntityService entityService =
                new TestExtensibleEntityEntityService(CURRENT_MODEL_VERSION);
        TestExtension ext1 = entity.getExtension(TestExtension.class);
        ext1.setStr(TEXT1);
        ext1.setBool(false);

        Map<String, Class<?>> aliases = getAliases();
        Set<Converter> converters = getConverters();

        TestXStreamPersistence xp = new TestXStreamPersistence();

        //saveEntity
        Calendar beforeSaveDate = Calendar.getInstance();
        xp.saveEntity(entityService, entity, USER0, aliases, converters);
        Calendar afterSaveDate = Calendar.getInstance();

        //test that entity is now in the HashMap available and lastModified is set.
        Document savedHashMapDoc = xp.getDocumentFromHashMap(entity);
        String lastModified = xp.getLastModifiedAttribute(savedHashMapDoc.getDocumentElement());
        assertNotNull(lastModified);
        assertIsXsdDateTime(lastModified);
        Calendar lastModifiedDate = DatatypeConverter.parseDateTime(lastModified);
        assertTrue(beforeSaveDate.compareTo(lastModifiedDate) <= 0);
        assertTrue(lastModifiedDate.compareTo(afterSaveDate) <= 0);

        //test that lastModifiedDate is set for extensions as well
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(savedHashMapDoc, aliases);
        assertEquals(2, extensions.size());
        String lastModifiedExt1 = xp.getLastModifiedAttribute(extensions.get(ALIAS_EXT1));
        assertIsXsdDateTime(lastModifiedExt1);
        String lastModifiedExt2 = xp.getLastModifiedAttribute(extensions.get(ALIAS_EXT1));
        assertIsXsdDateTime(lastModifiedExt2);

        //loadEntity again
        Set<ClassLoader> entityClassLoaders = getTestExtensibleEntityBaseClassLodades();
        TestExtensibleEntityBase loadedEntity = xp.loadEntity(entityService, entity.getUuid().toString(),
                entityClassLoaders, null, aliases, converters);
        assertLoadedEntityIsExpectedOne(loadedEntity, USER0, USER0, USER0, lastModified, lastModifiedExt1,
                lastModifiedExt2, TEXT1, false);

        // and check that loadEntities can read it
        List<? extends TestExtensibleEntityBase> loadedEntities = xp.loadEntities(entityService,
                entityClassLoaders, null, aliases, converters);
        assertEquals(1, loadedEntities.size());
        assertLoadedEntityIsExpectedOne(loadedEntities.get(0), USER0, USER0, USER0, lastModified, lastModifiedExt1,
                lastModifiedExt2, TEXT1,
                false);

        //change the entity and save  again
        ext1 = entity.getExtension(TestExtension.class);
        ext1.setStr(TEXT1 + " is now updated");
        xp.saveEntity(entityService, entity, USER1, aliases, converters);

        TestExtensibleEntityBase updatedEntity = xp.loadEntity(entityService, entity.getUuid().toString(), entityClassLoaders,
                null, aliases, converters);
        Document updatedHashMapDoc = xp.getDocumentFromHashMap(entity);
        lastModified = xp.getLastModifiedAttribute(updatedHashMapDoc.getDocumentElement());
        SortedMap<String, Element> updatedExtensions = xp.getExtensionsByAlias(updatedHashMapDoc,
                aliases);
        lastModifiedExt1 = xp.getLastModifiedAttribute(updatedExtensions.get(ALIAS_EXT1));
        assertLoadedEntityIsExpectedOne(updatedEntity, USER1, USER1, USER0, lastModified, lastModifiedExt1,
                lastModifiedExt2, TEXT1 + " is now updated", false);

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
        XStreamPersistence xp = new TestXStreamPersistence();
        DataMigration mockMigration = getMigrationMock();
        EasyMock.replay(mockMigration);
        Document doc = XMLUtils.documentFromString(XML_WITH_VERSION);
        xp.preProcessXML(doc, Collections.singleton(mockMigration), null, CURRENT_MODEL_VERSION);
        String res = XMLUtils.documentToString(doc);

        // TODO: Insnt there something wrong here?
        // isn't the expect version CURRENT_XSTREAM_PERSISTENCE_VERSION instead of the one out of the XML_WITH_VERSION=42
        String expected = "version=\"" + 42 + "\"";
        assertTrue(res.contains(expected));
        EasyMock.verify(mockMigration);
    }

    @Test
    public void testPostProcessEntity() throws Exception {
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
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
    public void testPostProcessXMLUnmodifiedDocument() throws Exception {
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
        assertPostProcessedXML(doc, doc, PERSIST_USER, TIME0, USER0, ALIASES, LAST_MODIFIED, LAST_MODIFIED_BY);

        // test initialization of missing lastModified and lastModifiedBy attributes
        doc = XMLUtils.documentFromString(createXML(null, null, ALIASES, VALUES,
                new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(doc, doc, PERSIST_USER, ANY_XSD_TIME, PERSIST_USER, ALIASES,
                new String[] { ANY_XSD_TIME, ANY_XSD_TIME }, new String[] { PERSIST_USER, PERSIST_USER });

        // test initialization of missing lastModified attributes
        doc = XMLUtils.documentFromString(createXML(TIME0, USER0, ALIASES, VALUES,
                new String[] { null, null }, LAST_MODIFIED_BY));
        assertPostProcessedXML(doc, doc, PERSIST_USER, TIME0, USER0, ALIASES,
                new String[] { ANY_XSD_TIME, ANY_XSD_TIME }, LAST_MODIFIED_BY);

        doc = XMLUtils.documentFromString(createXML(null, USER0, ALIASES, VALUES,
                new String[] { TIME1, null }, LAST_MODIFIED_BY));
        assertPostProcessedXML(doc, doc, PERSIST_USER, ANY_XSD_TIME, USER0, ALIASES,
                new String[] { TIME1, ANY_XSD_TIME }, LAST_MODIFIED_BY);

        doc = XMLUtils.documentFromString(createXML(null, USER0, ALIASES, VALUES,
                new String[] { null, TIME2 }, LAST_MODIFIED_BY));
        assertPostProcessedXML(doc, doc, PERSIST_USER, ANY_XSD_TIME, USER0, ALIASES,
                new String[] { ANY_XSD_TIME, TIME2 }, LAST_MODIFIED_BY);

        // test initialization of missing lastModifiedBy attributes
        doc = XMLUtils.documentFromString(createXML(TIME0, null, ALIASES, VALUES, LAST_MODIFIED,
                new String[] { null, null }));
        assertPostProcessedXML(doc, doc, PERSIST_USER, TIME0, PERSIST_USER, ALIASES,
                LAST_MODIFIED, new String[] { PERSIST_USER, PERSIST_USER });

        doc = XMLUtils.documentFromString(createXML(TIME0, USER1, ALIASES, VALUES, LAST_MODIFIED,
                new String[] { null, null }));
        assertPostProcessedXML(doc, doc, PERSIST_USER, TIME0, USER1, ALIASES,
                LAST_MODIFIED, new String[] { PERSIST_USER, PERSIST_USER });

        doc = XMLUtils.documentFromString(createXML(TIME0, null, ALIASES, VALUES, LAST_MODIFIED,
                new String[] { USER1, null }));
        assertPostProcessedXML(doc, doc, PERSIST_USER, TIME0, PERSIST_USER, ALIASES,
                LAST_MODIFIED, new String[] { USER1, PERSIST_USER });

        doc = XMLUtils.documentFromString(createXML(TIME0, null, ALIASES, VALUES, LAST_MODIFIED,
                new String[] { null, USER1 }));
        assertPostProcessedXML(doc, doc, PERSIST_USER, TIME0, PERSIST_USER, ALIASES,
                LAST_MODIFIED, new String[] { PERSIST_USER, USER1 });
    }

    @Test
    public void testPostProcessXMLModifiedExtension() throws Exception {
        Document oldDoc = XMLUtils.documentFromString(createXML(TIME0, USER0, ALIASES,
                new String[] { VALUE1, VALUE2 }, LAST_MODIFIED, LAST_MODIFIED_BY));
        Document newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, TIME2 }, new String[] { PERSIST_USER, USER2 });

        // test initialization of missing lastModified and lastModifiedBy attributes
        oldDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { VALUE1, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, ANY_XSD_TIME, PERSIST_USER, ALIASES,
                new String[] { ANY_XSD_TIME, ANY_XSD_TIME }, new String[] { PERSIST_USER, PERSIST_USER });

        // test initialization of missing lastModified attributes
        oldDoc = XMLUtils.documentFromString(createXML(TIME0, USER0, ALIASES,
                new String[] { VALUE1, VALUE2 }, new String[] { null, null }, LAST_MODIFIED_BY));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, ANY_XSD_TIME }, new String[] { PERSIST_USER, USER2 });

        oldDoc = XMLUtils.documentFromString(createXML(null, USER0, ALIASES,
                new String[] { VALUE1, VALUE2 }, new String[] { TIME0, null }, LAST_MODIFIED_BY));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, ANY_XSD_TIME }, new String[] { PERSIST_USER, USER2 });

        oldDoc = XMLUtils.documentFromString(createXML(null, USER0, ALIASES,
                new String[] { VALUE1, VALUE2 }, new String[] { null, TIME0 }, LAST_MODIFIED_BY));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, TIME0 }, new String[] { PERSIST_USER, USER2 });

        // test initialization of missing lastModifiedBy attributes
        oldDoc = XMLUtils.documentFromString(createXML(TIME0, null, ALIASES,
                new String[] { VALUE1, VALUE2 }, LAST_MODIFIED, new String[] { null, null }));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, TIME2 }, new String[] { PERSIST_USER, PERSIST_USER });

        oldDoc = XMLUtils.documentFromString(createXML(TIME0, null, ALIASES,
                new String[] { VALUE1, VALUE2 }, LAST_MODIFIED, new String[] { USER0, null }));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, TIME2 }, new String[] { PERSIST_USER, PERSIST_USER });

        oldDoc = XMLUtils.documentFromString(createXML(TIME0, null, ALIASES,
                new String[] { VALUE1, VALUE2 }, LAST_MODIFIED, new String[] { null, USER0 }));
        newDoc = XMLUtils.documentFromString(createXML(null, null, ALIASES,
                new String[] { MODIFIED_VALUE, VALUE2 }, new String[] { null, null }, new String[] { null, null }));
        assertPostProcessedXML(newDoc, oldDoc, PERSIST_USER, UPDATED_TIME0, PERSIST_USER, ALIASES,
                new String[] { UPDATED_TIME0, TIME2 }, new String[] { PERSIST_USER, USER0 });
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
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
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
        Document doc = XMLUtils.documentFromString(XML_WITH_EXTENSIONS);
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
        xp.setVersionAttribute(doc, CURRENT_MODEL_VERSION);
        assertEquals(CURRENT_MODEL_VERSION, xp.getVersionAttribute(doc));
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
        xp.setVersionAttribute(doc, CURRENT_MODEL_VERSION);
        try {
            xp.setVersionAttribute(doc, CURRENT_MODEL_VERSION);
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

    @Test
    public void testLastModifiedBy() throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Document doc = XMLUtils.documentFromString(XML_WITHOUT_VERSION);
        Element documentElement = doc.getDocumentElement();
        xp.setLastModifiedByAttribute(documentElement, USER0);
        assertEquals(USER0, xp.getLastModifiedByAttribute(documentElement));
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

    @Test
    public void testStorageService() {
        StorageService storageService = new HashMapStorageService();
        XStreamPersistence xp = new XStreamPersistence(storageService);
        assertEquals(storageService, xp.getStorageService());
    }

    private void assertPostProcessedXML(Document newDoc, Document oldDoc, String userId,
            String expectedGlobalLastModified, String expectedGlobalLastModifiedBy, String[] expectedAliases,
            String[] expectedExtLastModified, String[] expectedExtLastModifiedBy) throws Exception {
        XStreamPersistence xp = new TestXStreamPersistence();
        Map<String, Class<?>> aliases = getAliases();
        xp.postProcessXML(newDoc, oldDoc, aliases, userId, CURRENT_MODEL_VERSION);
        Element documentElement = newDoc.getDocumentElement();
        assertLastModifiedTime(expectedGlobalLastModified, xp.getLastModifiedAttribute(documentElement));
        assertEquals(expectedGlobalLastModifiedBy, xp.getLastModifiedByAttribute(documentElement));
        SortedMap<String, Element> extensions = xp.getExtensionsByAlias(newDoc, aliases);
        int i = 0;
        for (Element ext: extensions.values()) {
            assertEquals(expectedAliases[i], ext.getNodeName());
            assertLastModifiedTime(expectedExtLastModified[i], xp.getLastModifiedAttribute(ext));
            assertEquals(expectedExtLastModifiedBy[i], xp.getLastModifiedByAttribute(ext));
            ++i;
        }
        assertEquals(CURRENT_MODEL_VERSION, xp.getVersionAttribute(newDoc));
    }

    private void assertLastModifiedTime(String expected, String actual) {
        if (ANY_XSD_TIME.equals(expected)) {
            assertIsXsdDateTime(actual);
        } else if (expected.startsWith(">")) {
            assertIsXsdDateTime(actual);
            long actualMillis = DatatypeConverter.parseDateTime(actual).getTimeInMillis();
            long expectedMillis = DatatypeConverter.parseDateTime(expected.substring(1)).getTimeInMillis();
            assertTrue(actualMillis > expectedMillis);
        } else {
            assertEquals(actual, expected);
        }
    }

    private void assertIsXsdDateTime(String lexicalXSDDateTime) {
        assertTrue(StringUtils.isNotBlank(lexicalXSDDateTime));
        DatatypeConverter.parseDateTime(lexicalXSDDateTime);
    }


    /**
     * checks that in a range of seconds the dateString matches the expected one
     */
    private void assertLastModifiedDate(String dateString, String expectedDateString) {
        assertThat(dateString, startsWith(expectedDateString.substring(0, "YYYY-MM-DDTHH:MM:SS".length())));
    }

    private void assertLoadedEntityIsExpectedOne(TestExtensibleEntityBase loadedEntity, String user, String userExt1,
            String userExt2, String lastModified,
            String lastModifiedExt1,
            String lastModifiedExt2, String ext1Text, boolean ext1boolean) {
        //the length, up to times should be the same
        assertNotNull(loadedEntity);
        assertLastModifiedDate(loadedEntity.getLastModified(), lastModified);
        assertEquals(user, loadedEntity.getLastModifiedBy());
        TestExtension ext1 = ((TestExtensibleEntityBase) loadedEntity).getExtension(TestExtension.class);
        assertNotNull(ext1);
        assertLastModifiedDate(ext1.getLastModified(), lastModifiedExt1);
        assertEquals(ext1Text, ext1.getStr());
        assertEquals(ext1boolean, ext1.isBool());
        assertEquals(userExt1, ext1.getLastModifiedBy());
        TestExtension1 ext2 = ((TestExtensibleEntityBase) loadedEntity).getExtension(TestExtension1.class);
        assertNotNull(ext2);
        assertLastModifiedDate(ext2.getLastModified(), lastModifiedExt2);
    }

    private static String createXML(String docLastModified, String docLastModifiedBy,
            String[] aliases, String[] values, String[] extLastModified, String[] extLastModifiedBy) {
         StringBuilder sb = new StringBuilder();
         sb.append("<root");
         appendModifiedAttributes(sb, docLastModified, docLastModifiedBy);
         sb.append(">");
         if (aliases != null) {
             sb.append("<extensions>");
             for (int i = 0; i < aliases.length; ++i) {
                 sb.append("<").append(aliases[i]);
                 appendModifiedAttributes(sb, extLastModified[i], extLastModifiedBy[i]);
                 sb.append(">");
                 if (values[i] != null) {
                     sb.append("<string>").append(values[i]).append("</string>");
                 }
                 sb.append("</").append(aliases[i]).append(">");
             }
             sb.append("</extensions>");
         }
         sb.append("</root>");
         return sb.toString();
     }

     private static void appendModifiedAttributes(StringBuilder sb, String lastModified, String lastModifiedBy) {
         if (lastModified != null) {
             sb.append(" lastModified=\"").append(lastModified).append("\"");
         }
         if (lastModifiedBy != null) {
             sb.append(" modifiedBy=\"").append(lastModifiedBy).append("\"");
         }
     }
}
