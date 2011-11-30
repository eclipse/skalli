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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.core.internal.persistence.EntityHelper;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensibleEntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.Historized;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issuer;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

/**
 * Helper class for the conversion of entities to XML documents.
 * Requires a {@link StorageService storage service}.
 */
public class XStreamPersistence implements Issuer {

    private static final String TAG_VERSION = "version"; //$NON-NLS-1$
    private static final String TAG_MODIFIED_BY = "modifiedBy"; //$NON-NLS-1$
    private static final String TAG_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(XStreamPersistence.class);

    private StorageService storageService;

    public XStreamPersistence(StorageService storageService) {
        this.storageService = storageService;
    }

    // TODO "central" versioning together with extensibility sucks!
    /*
     * Alternative idea: Instead of a version number just use unique IDs. A
     * migration then defines a set of other migration's ids which have to be
     * applied as a prerequisite. The Migrator then first collects all registered
     * migrations an then sorts them by their prerequisites. Hypothetis: A
     * migration logically cannot depend on another migration if the unique id of
     * that migration is not known. Open issues: - What should be used as a
     * "version" information in the persisted xml? => Maybe something like a
     * checksum (must be stable with respect to the order of calculation)
     */
    private static final int CURRENT_VERISON = 19;

    int getCurrentVersion() {
        return CURRENT_VERISON;
    }

    StorageService getStorageService() {
        return storageService;
    }

    void postProcessXML(Document newDoc, Document oldDoc, Map<String, Class<?>> aliases, String userId)
            throws ValidationException {
        Element newDocElement = newDoc.getDocumentElement();
        Element oldDocElement = oldDoc != null ? oldDoc.getDocumentElement() : null;
        if (!XMLDiff.identical(newDocElement, oldDocElement)) {
            setLastModifiedAttribute(newDocElement);
            setLastModifiedByAttribute(newDocElement, userId);
            SortedMap<String, Element> newExts = getExtensionsByAlias(newDoc, aliases);
            SortedMap<String, Element> oldExts = oldDoc != null ? getExtensionsByAlias(oldDoc, aliases) : null;
            for (String alias : newExts.keySet()) {
                Element newExt = newExts.get(alias);
                Element oldExt = oldExts != null ? oldExts.get(alias) : null;
                if (!XMLDiff.identical(newExt, oldExt)) {
                    setLastModifiedAttribute(newExt);
                    setLastModifiedByAttribute(newExt, userId);
                } else if (oldExt != null) {
                    setLastModifiedAttribute(newExt, getLastModifiedAttribute(oldExt));
                    setLastModifiedByAttribute(newExt, getLastModifiedByAttribute(oldExt));
                }
            }
        }
        setVersionAttribute(newDoc);
    }

    void preProcessXML(Document doc, Set<DataMigration> migrations, Map<String, Class<?>> aliases) throws ValidationException {
        int version = getVersionAttribute(doc);
        if (migrations != null) {
            DataMigrator migrator = new DataMigrator(migrations, aliases);
            migrator.migrate(doc, version, getCurrentVersion());
        }
    }

    void postProcessEntity(Document doc, EntityBase entity, Map<String, Class<?>> aliases)
            throws ValidationException {
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

    String getLastModifiedAttribute(Element element) {
        String value = element.getAttribute(TAG_LAST_MODIFIED);
        return StringUtils.isNotBlank(value) ? value : null;
    }

    void setLastModifiedAttribute(Element element) {
        if (element.hasAttribute(TAG_LAST_MODIFIED)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    element.getNodeName(), TAG_LAST_MODIFIED));
        }
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
        String lastModified = DatatypeConverter.printDateTime(now);
        setLastModifiedAttribute(element, lastModified);
    }

    void setLastModifiedAttribute(Element element, String lastModified) {
        if (StringUtils.isNotBlank(lastModified)) {
            element.setAttribute(TAG_LAST_MODIFIED, lastModified);
        }
    }

    String getLastModifiedByAttribute(Element element) {
        String value = element.getAttribute(TAG_MODIFIED_BY);
        return StringUtils.isNotBlank(value) ? value : null;
    }

    void setLastModifiedByAttribute(Element element, String userId) {
        if (element.hasAttribute(TAG_MODIFIED_BY)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    element.getNodeName(), TAG_MODIFIED_BY));
        }
        if (StringUtils.isNotBlank(userId)) {
            element.setAttribute(TAG_MODIFIED_BY, userId);
        }
    }

    SortedMap<String, Element> getExtensionsByAlias(Document doc, Map<String, Class<?>> aliases)
            throws ValidationException {
        return getExtensions(doc, aliases, false);
    }

    SortedMap<String, Element> getExtensions(Document doc, Map<String, Class<?>> aliases,
            boolean byClassName)
            throws ValidationException {
        TreeMap<String, Element> result = new TreeMap<String, Element>();
        if (aliases != null && aliases.size() > 0) {
            List<Element> extensionElements = XMLUtils.getExtensions(doc, aliases.keySet());
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

    void setVersionAttribute(Document doc) {
        setVersionAttribute(doc, getCurrentVersion());
    }

    void setVersionAttribute(Document doc, int version) {
        Element documentElement = doc.getDocumentElement();
        if (doc.getDocumentElement().hasAttribute(TAG_VERSION)) {
            throw new RuntimeException(MessageFormat.format("<{0}> element already has a ''{1}'' attribute",
                    documentElement.getNodeName(), TAG_VERSION));
        }
        documentElement.setAttribute(TAG_VERSION, Integer.toString(version));
    }

    SortedMap<String, Element> getExtensionsByClassName(Document doc, Map<String, Class<?>> aliases)
            throws ValidationException {
        return getExtensions(doc, aliases, true);
    }

    int getVersionAttribute(Document doc) {
        int version = 0;
        String versionAttr = doc.getDocumentElement().getAttribute(TAG_VERSION);
        if (StringUtils.isNotBlank(versionAttr)) {
            version = Integer.parseInt(versionAttr);
        }
        return version;
    }

    void saveEntity(EntityBase entity, String userId, Map<String, Class<?>> aliases)
            throws StorageException, ValidationException {

        Class<?> entityClass = entity.getClass();
        String category = entityClass.getSimpleName();
        String key = entity.getUuid().toString();

        if (entityClass.isAnnotationPresent(Historized.class)) {
            storageService.archive(category, key);
        }

        Document newDoc = entityToDom(entity, aliases);
        Document oldDoc = readEntityAsDom(entity.getClass(), entity.getUuid());
        try {
            postProcessXML(newDoc, oldDoc, aliases, userId);
        } catch (ValidationException e) {
            throw new ValidationException(new Issue(Severity.FATAL, getClass(), entity.getUuid(), e.getMessage()));
        }

        InputStream is;
        try {
            is = XMLUtils.documentToStream(newDoc);
        } catch (TransformerException e) {
            throw new StorageException("Failed to transform entity " + entity + " to XML", e);
        }

        storageService.write(category, key, is);
    }

    private Set<Converter> getConverters() {
        return CollectionUtils.asSet(new NoopConverter(), new UUIDListConverter(), new ExtensionsMapConverter());
    }

    private Document entityToDom(EntityBase entity, Map<String, Class<?>> aliases) throws StorageException {
        Document newDoc = null;
        try {
            XStream xstream = IgnoreUnknownElementsXStream.getXStreamInstance(getConverters(), null, aliases);
            String xml = xstream.toXML(entity);
            newDoc = XMLUtils.documentFromString(xml);
        } catch (SAXException e) {
            throw new StorageException("Failed to transform entity " + entity + " to XML", e);
        } catch (IOException e) {
            throw new StorageException("Failed to transform entity " + entity + " to XML", e);
        } catch (ParserConfigurationException e) {
            throw new StorageException("Failed to transform entity " + entity + " to XML", e);
        }
        return newDoc;
    }

    private EntityBase domToEntity(Set<ClassLoader> entityClassLoaders, Map<String, Class<?>> aliases, Document doc)
            throws StorageException {

        String xml = null;
        try {
            xml = XMLUtils.documentToString(doc);
        } catch (TransformerException e) {
            throw new StorageException("Failed to transform XML to entity", e);
        }
        XStream xstream = IgnoreUnknownElementsXStream.getXStreamInstance(getConverters(), entityClassLoaders, aliases);
        EntityBase entity = (EntityBase) xstream.fromXML(xml);
        return entity;
    }

    <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid,
            Set<ClassLoader> entityClassLoaders,
            Set<DataMigration> migrations, Map<String, Class<?>> aliases) throws StorageException, ValidationException {

        Document doc = readEntityAsDom(entityClass, uuid);
        if (doc == null) {
            return null;
        }

        try {
            preProcessXML(doc, migrations, aliases);
        } catch (ValidationException e) {
            throw new ValidationException(new Issue(Severity.FATAL, getClass(), uuid, e.getMessage()));
        }

        EntityBase entity = domToEntity(entityClassLoaders, aliases, doc);
        if (entity == null) {
            throw new StorageException("Could not load entity " + uuid + " of type " + entityClass);
        }

        try {
            postProcessEntity(doc, entity, aliases);
        } catch (ValidationException e) {
            throw new ValidationException(new Issue(Severity.FATAL, getClass(), uuid, e.getMessage()));
        }

        LOG.info(MessageFormat.format("Loaded entity {0}", entity.getUuid()));

        EntityBase loadedEntityBase = entity;
        return entityClass.cast(loadedEntityBase);
    }

    private Document readEntityAsDom(Class<? extends EntityBase> entityClass, UUID uuid) throws StorageException {
        InputStream stream = storageService.read(entityClass.getSimpleName(), uuid.toString());
        if (stream == null) {
            // no entity available
            return null;
        }

        Document doc;
        try {
            doc = XMLUtils.documentFromStream(stream);
        } catch (SAXException e) {
            throw new StorageException("Failed to convert stream to dom for entity " + uuid + " of type " + entityClass, e);
        } catch (IOException e) {
            throw new StorageException("Failed to convert stream to dom for entity " + uuid + " of type " + entityClass, e);
        } catch (ParserConfigurationException e) {
            throw new StorageException("Failed to convert stream to dom for entity " + uuid + " of type " + entityClass, e);
        }
        return doc;
    }

    <T extends EntityBase> List<T> loadEntities(Class<T> entityClass, Set<ClassLoader> entityClassLoaders,
            Set<DataMigration> migrations, Map<String, Class<?>> aliases) throws StorageException, ValidationException {

        List<T> loadEntities = new ArrayList<T>();
        List<String> keys = storageService.keys(entityClass.getSimpleName());
        for (String key : keys) {
            if (!UUIDUtils.isUUID(key)) {
                throw new StorageException(key + " is not a valid UUID");
            }
            UUID uuid = UUID.fromString(key);
            LOG.info("  Loading entity " + uuid + " of type " + entityClass); //$NON-NLS-1$
            loadEntities.add(loadEntity(entityClass, uuid, entityClassLoaders, migrations, aliases));
        }
        return loadEntities;
    }
}
