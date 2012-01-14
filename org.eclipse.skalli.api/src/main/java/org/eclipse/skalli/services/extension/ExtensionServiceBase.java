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
package org.eclipse.skalli.services.extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.rest.RestConverter;

/**
 * Basic implementation of an extension service.
 * This implementation provides no data migrations, no converters
 * for the REST API and neither property nor extension validators.
 */
public abstract class ExtensionServiceBase<T extends ExtensionEntityBase> implements ExtensionService<T> {

    /**
     * Captions for the properties of an entity.
     * Note, the captions for {@link EntityBase#PROPERTY_PARENT_ENTITY} and
     * {@link EntityBase#PROPERTY_PARENT_ENTITY_ID} should be overwritten in extension
     * service implementations for derived entity classes (see for example {@link ExtensionServiceCore}).
     */
    protected static final Map<String, String> CAPTIONS = CollectionUtils.asMap(new String[][] {
            { EntityBase.PROPERTY_UUID, "Unique Identifier" },
            { EntityBase.PROPERTY_DELETED, "Deleted" },
            { EntityBase.PROPERTY_PARENT_ENTITY, "Parent Entity" },
            { EntityBase.PROPERTY_PARENT_ENTITY_ID, "Parent Entity ID" } });

    /**
     * Descriptions for the properties of an entity.
     * Note, the descriptions for {@link EntityBase#PROPERTY_PARENT_ENTITY} and
     * {@link EntityBase#PROPERTY_PARENT_ENTITY_ID} should be overwritten in extension
     * service implementations for derived entity classes (see for example {@link ExtensionServiceCore}).
     */
    protected static final Map<String, String> DESCRIPTIONS = CollectionUtils.asMap(new String[][] {
            { EntityBase.PROPERTY_UUID, "Globally unique identifier of this entity" },
            { EntityBase.PROPERTY_DELETED, "Checked if the entity has been deleted" },
            { EntityBase.PROPERTY_PARENT_ENTITY, "Entity to which this entity is assigned as subentity" },
            { EntityBase.PROPERTY_PARENT_ENTITY_ID,
                    "Unique identifier of an entity to which this entity is assigned as subentity" } });

    /**
     * Input prompt for URL-like form fields, i.e. <tt>{@value}</tt>.
     */
    protected static final String URL_INPUT_PROMPT = "http://<host>:<port>/<path>";

    @Override
    public Map<String, Class<?>> getAliases() {
        return new HashMap<String, Class<?>>();
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        return new HashSet<ClassLoader>();
    }

    @Override
    public Set<DataMigration> getMigrations() {
        return  new HashSet<DataMigration>();
    }

    @Override
    public RestConverter getRestConverter(String host) {
        return null;
    }

    @Override
    public Indexer<T> getIndexer() {
        return null;
    }

    @Override
    public Set<String> getProjectTemplateIds() {
        return null;
    }

    @Override
    public String getCaption(String propertyName) {
        return CAPTIONS.get(propertyName);
    }

    @Override
    public String getDescription(String propertyName) {
        return DESCRIPTIONS.get(propertyName);
    }

    protected String getCaption(String propertyName, String caption) {
        if (StringUtils.isBlank(caption)) {
            caption = getCaption(propertyName);
            if (StringUtils.isBlank(caption)) {
                caption = propertyName;
            }
        }
        return caption;
    }

    @Override
    public String getInputPrompt(String propertyName) {
        return null;
    }

    @Override
    public List<String> getConfirmationWarnings(ExtensibleEntityBase entity, ExtensibleEntityBase modifiedEntity, User modifier) {
        return Collections.emptyList();
    }

    @Override
    public List<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        return Collections.emptyList();
    }

    @Override
    public List<ExtensionValidator<T>> getExtensionValidators(Map<String, String> captions) {
        return Collections.emptyList();
    }
}
