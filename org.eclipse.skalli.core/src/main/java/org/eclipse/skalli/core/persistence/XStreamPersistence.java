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
package org.eclipse.skalli.core.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.core.xstream.IgnoreUnknownElementsXStream;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Historized;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.eclipse.skalli.services.persistence.StorageConsumer;
import org.eclipse.skalli.services.persistence.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;

/**
 * Helper class for the conversion of entities to XML documents.
 * Requires a {@link StorageService storage service}.
 */
public class XStreamPersistence implements Issuer {

    private static final String TAG_VERSION = "version"; //$NON-NLS-1$
    private static final String TAG_MODIFIED_BY = "modifiedBy"; //$NON-NLS-1$
    private static final String TAG_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$
    private static final String TAG_INHERITED_EXTENSIONS = "inheritedExtensions"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(XStreamPersistence.class);

    private static class XStreamConsumer<T extends EntityBase> implements StorageConsumer {

        private final EntityService<T> entityService;
        private final Set<ClassLoader> classLoaders;
        private final Set<DataMigration> migrations;
        private final Map<String, Class<?>> aliases;
        private final Set<Converter> converters;

        private List<T> loadEntities = new ArrayList<T>();

        public XStreamConsumer(EntityService<T> entityService, Set<ClassLoader> classLoaders,
                Set<DataMigration> migrations, Map<String, Class<?>> aliases, Set<Converter> converters) {
            this.entityService = entityService;
            this.classLoaders = classLoaders;
            this.migrations = migrations;
            this.aliases = aliases;
            this.converters = converters;
        }

        public List<T> getLoadedEntities() {
            return loadEntities;
        }

        @Override
        public void consume(String category, String key, long lastModified, InputStream stream)
                throws IOException {
            try {
                Document doc = XMLUtils.documentFromStream(stream);
                preProcessXML(doc, migrations, aliases, entityService.getModelVersion());
                mapInheritedExtensions(doc, byAlias(aliases));

                EntityBase entity = domToEntity(classLoaders, aliases, converters, doc);
                if (entity != null) {
                    postProcessEntity(doc, entity, aliases);
                    loadEntities.add(entityService.getEntityClass().cast(entity));
                    LOG.info(MessageFormat.format("Loaded entity {0}", entity.getUuid()));
                }
            } catch (Exception e) {
                throw new IOException(MessageFormat.format(
                        "Failed to process entity {0} of type {1}", key, category), e);
            }
        }
    }

    StorageService storageService;

    public XStreamPersistence(StorageService storageService) {
        this.storageService = storageService;
    }

    public <T extends EntityBase> T loadEntity(EntityService<T> entityService, String key, Set<ClassLoader> classLoaders,
            Set<DataMigration> migrations, Map<String, Class<?>> aliases, Set<Converter> converters)
            throws IOException {
        if (entityService == null) {
            throw new IOException(MessageFormat.format(
                    "Could not load entity {0}: No corresponding entity service available", key));
        }
        XStreamConsumer<T> consumer = new XStreamConsumer<T>(entityService, classLoaders, migrations, aliases, converters);
        storageService.read(entityService.getEntityClass().getSimpleName(), key, consumer);
        List<T> loadedEntities = consumer.getLoadedEntities();
        if (loadedEntities.isEmpty()) {
            return null;
        }
        return entityService.getEntityClass().cast(loadedEntities.get(0));
    }

    public <T extends EntityBase> List<T> loadEntities(EntityService<T> entityService, Set<ClassLoader> classLoaders,
            Set<DataMigration> migrations, Map<String, Class<?>> aliases, Set<Converter> converters)
            throws IOException {
        // Read all entities with one request to the storage service
        // instead of one by one. This will improve the startup time
        // considerably.
        XStreamConsumer<T> consumer = new XStreamConsumer<T>(entityService, classLoaders, migrations, aliases, converters);
        storageService.readAll(entityService.getEntityClass().getSimpleName(), consumer);
        return consumer.getLoadedEntities();
    }

    public void saveEntity(EntityService<?> entityService, EntityBase entity, String userId,
            Map<String, Class<?>> aliases, Set<Converter> converters) throws MigrationException, IOException {
        Class<? extends EntityBase> entityClass = entity.getClass();
        String category = entityClass.getSimpleName();
        String key = entity.getUuid().toString();

        if (entityClass.isAnnotationPresent(Historized.class)) {
            storageService.archive(category, key);
        }

        Document newDoc = entityToDom(entity, aliases, converters);
        mapInheritedExtensions(newDoc, byClassNames(aliases));

        Document oldDoc = entityToDom(entityClass, entity.getUuid().toString());
        postProcessXML(newDoc, oldDoc, aliases, userId, entityService.getModelVersion());

        InputStream is;
        try {
            is = XMLUtils.documentToStream(newDoc);
        } catch (TransformerException e) {
            throw new IOException(MessageFormat.format("Failed to transform entity {0} to XML", entity), e);
        }

        storageService.write(category, key, is);
    }

    static void preProcessXML(Document doc, Set<DataMigration> migrations, Map<String, Class<?>> aliases, int modelVersion)
            throws MigrationException {
        int version = getVersionAttribute(doc);
        if (migrations != null) {
            DataMigrator migrator = new DataMigrator(migrations, aliases);
            migrator.migrate(doc, version, modelVersion);
        }
    }

    static void postProcessXML(Document newDoc, Document oldDoc, Map<String, Class<?>> aliases, String userId, int modelVersion)
            throws MigrationException {
        Element newDocElement = newDoc.getDocumentElement();
        Element oldDocElement = oldDoc != null ? oldDoc.getDocumentElement() : null;

        boolean identical = XMLDiff.identical(newDocElement, oldDocElement);
        setLastModifiedAttributes(newDocElement, identical? oldDocElement : null, userId);

        SortedMap<String, Element> newExts = getExtensionsByAlias(newDoc, aliases);
        SortedMap<String, Element> oldExts = oldDoc != null ? getExtensionsByAlias(oldDoc, aliases) : null;
        for (String alias : newExts.keySet()) {
            Element newExt = newExts.get(alias);
            Element oldExt = oldExts != null ? oldExts.get(alias) : null;
            setLastModifiedAttributes(newExt, (identical || XMLDiff.identical(newExt, oldExt))? oldExt : null, userId);
        }
        setVersionAttribute(newDoc, modelVersion);
    }

    static void postProcessEntity(Document doc, EntityBase entity, Map<String, Class<?>> aliases)
            throws MigrationException {
        EntityHelper.normalize(entity);
        Element docElement = doc.getDocumentElement();
        entity.setLastModified(getLastModifiedAttribute(docElement));
        entity.setLastModifiedBy(getLastModifiedByAttribute(docElement));
        if (entity instanceof ExtensibleEntityBase) {
            ExtensibleEntityBase extensible = (ExtensibleEntityBase) entity;
            Map<String, Element> extensionElements = getExtensionsByClassName(doc, aliases);
            SortedSet<ExtensionEntityBase> extensions = extensible.getAllExtensions();
            for (ExtensionEntityBase extension : extensions) {
                String extensionClassName = extension.getClass().getName();
                Element extensionElement = extensionElements.get(extensionClassName);
                if (extensionElement != null) {
                    extension.setLastModified(getLastModifiedAttribute(extensionElement));
                    extension.setLastModifiedBy(getLastModifiedByAttribute(extensionElement));
                }
            }
        }
    }

    static int getVersionAttribute(Document doc) {
        int version = 0;
        String versionAttr = doc.getDocumentElement().getAttribute(TAG_VERSION);
        if (StringUtils.isNotBlank(versionAttr)) {
            version = Integer.parseInt(versionAttr);
        }
        return version;
    }

    static void setVersionAttribute(Document doc, int version) {
        Element documentElement = doc.getDocumentElement();
        if (doc.getDocumentElement().hasAttribute(TAG_VERSION)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    documentElement.getNodeName(), TAG_VERSION));
        }
        documentElement.setAttribute(TAG_VERSION, Integer.toString(version));
    }

    static String getLastModifiedAttribute(Element element) {
        String value = element.getAttribute(TAG_LAST_MODIFIED);
        return StringUtils.isNotBlank(value) ? value : null;
    }

    static String getLastModifiedByAttribute(Element element) {
        String value = element.getAttribute(TAG_MODIFIED_BY);
        return StringUtils.isNotBlank(value) ? value : null;
    }

    static void setLastModifiedAttribute(Element element) {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
        String lastModified = DatatypeConverter.printDateTime(now);
        setLastModifiedAttribute(element, lastModified);
    }

    static void setLastModifiedAttribute(Element element, String lastModified) {
        if (StringUtils.isNotBlank(lastModified)) {
            element.setAttribute(TAG_LAST_MODIFIED, lastModified);
        }
    }

    static void setLastModifiedByAttribute(Element element, String userId) {
        if (StringUtils.isNotBlank(userId)) {
            element.setAttribute(TAG_MODIFIED_BY, userId);
        }
    }

    static  void setLastModifiedAttributes(Element newElement, Element oldElement, String userId) {
        String lastModified = oldElement != null? getLastModifiedAttribute(oldElement) : null;
        if (lastModified != null) {
            setLastModifiedAttribute(newElement, lastModified);
        } else {
            setLastModifiedAttribute(newElement);
        }
        String lastModifiedBy = oldElement != null? getLastModifiedByAttribute(oldElement) : null;
        if (lastModifiedBy != null) {
            setLastModifiedByAttribute(newElement, lastModifiedBy);
        } else{
            setLastModifiedByAttribute(newElement, userId);
        }
    }

    static SortedMap<String, Element> getExtensions(Document doc, Map<String, Class<?>> aliases, boolean byClassName)
            throws MigrationException {
        TreeMap<String, Element> result = new TreeMap<String, Element>();
        if (aliases != null && aliases.size() > 0) {
            List<Element> extensionElements = MigrationUtils.getExtensions(doc, aliases.keySet());
            for (Element extensionElement : extensionElements) {
                String name = extensionElement.getNodeName();
                if (byClassName) {
                    Class<?> extensionClass = aliases.get(name);
                    if (extensionClass != null) {
                        result.put(extensionClass.getName(), extensionElement);
                    }
                } else {
                    result.put(name, extensionElement);
                }
            }
        }
        return result;
    }

    static SortedMap<String, Element> getExtensionsByAlias(Document doc, Map<String, Class<?>> aliases)
            throws MigrationException {
        return getExtensions(doc, aliases, false);
    }

    static SortedMap<String, Element> getExtensionsByClassName(Document doc, Map<String, Class<?>> aliases)
            throws MigrationException {
        return getExtensions(doc, aliases, true);
    }

    static void mapInheritedExtensions(Document doc, Map<String, String> aliases) throws MigrationException {
        if (aliases == null || aliases.isEmpty()) {
            return;
        }
        Element inheritedElement = MigrationUtils.getElementOfEntity(doc, TAG_INHERITED_EXTENSIONS);
        if (inheritedElement != null) {
            NodeList nodes = inheritedElement.getChildNodes();
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if ("string".equals(node.getNodeName())) { //$NON-NLS-1$
                        mapTextContent((Element)node, aliases);
                    }
                }
            }
        }
    }

    static void mapTextContent(Element stringElement, Map<String, String> aliases) {
        String alias = stringElement.getTextContent();
        if (StringUtils.isNotBlank(alias)) {
            String mapped = aliases.get(alias);
            if (mapped != null) {
                stringElement.setTextContent(mapped);
            }
        }
    }

    static Map<String, String> byAlias(Map<String, Class<?>> aliases) {
        Map<String, String> map = new HashMap<String, String>(aliases.size());
        for (Map.Entry<String, Class<?>> entry: aliases.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getName());
        }
        return map;
    }

    static Map<String, String> byClassNames(Map<String, Class<?>> aliases) {
        Map<String, String> map = new HashMap<String, String>(aliases.size());
        for (Map.Entry<String, Class<?>> entry: aliases.entrySet()) {
            map.put(entry.getValue().getName(), entry.getKey());
        }
        return map;
    }


    private static EntityBase domToEntity(Set<ClassLoader> entityClassLoaders, Map<String, Class<?>> aliases,
            Set<Converter> converters, Document doc) throws IOException {
        String xml = null;
        try {
            xml = XMLUtils.documentToString(doc);
        } catch (TransformerException e) {
            throw new IOException("Failed to transform XML to entity", e);
        }
        XStream xstream = IgnoreUnknownElementsXStream.getXStreamInstance(converters, entityClassLoaders, aliases);
        EntityBase entity = null;
        try {
             entity = (EntityBase) xstream.fromXML(xml);
        } catch (XStreamException e) {
            LOG.warn(MessageFormat.format("Failed to convert XML document to entity: {0}", xml), e);
        }
        return entity;
    }

    private static Document entityToDom(EntityBase entity, Map<String, Class<?>> aliases, Set<Converter> converters)
            throws IOException {
        Document newDoc = null;
        try {
            XStream xstream = IgnoreUnknownElementsXStream.getXStreamInstance(converters, null, aliases);
            String xml = xstream.toXML(entity);
            newDoc = XMLUtils.documentFromString(xml);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format("Failed to transform entity {0} to XML", entity), e);
        }
        return newDoc;
    }

    private Document entityToDom(Class<? extends EntityBase> entityClass, String key) throws IOException {
        InputStream stream = storageService.read(entityClass.getSimpleName(), key);
        if (stream == null) {
            LOG.warn(MessageFormat.format("Storage services has no entity with key {0}", key));
            return null;
        }

        Document doc;
        try {
            doc = XMLUtils.documentFromStream(stream);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to convert stream to dom for entity {0} of type {1}", key, entityClass), e);
        }
        return doc;
    }
}
