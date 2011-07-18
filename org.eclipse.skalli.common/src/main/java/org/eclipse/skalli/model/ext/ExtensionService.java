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
package org.eclipse.skalli.model.ext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.common.User;

/**
 * Interface of a service that defines a model extension.
 *
 * An implementation of this interface must be registered as an OSGI component
 * (see for example <tt>OSGI_INF/ExtensionServiceCore.xml</tt> in the bundle
 * <tt>org.eclipse.skalli.model.core</tt> that provides the basis model
 * for projects).
 */
public interface ExtensionService<T extends ExtensionEntityBase> {

    /**
     * Returns the class of the model extension provided by this extension service.
     */
    public Class<T> getExtensionClass();

    /**
     * Returns the current version of the model extension.
     */
    public String getModelVersion();

    /**
     * Returns the namespace of the model extension.
     * The namespace should be a valid XML namespace.
     */
    public String getNamespace();

    /**
     * Returns the name of an XML schema file that describes
     * the persistence format of the model extension.
     */
    public String getXsdFileName();

    /**
     * Returns a set of data migrators used to migrate persisted
     * instances of the model extension from previous versions of
     * the model extension to the current version of the model extension
     * as defined by {@link #getModelVersion()}.
     *
     * @return a set of migrations, or an empty set.
     */
    public Set<DataMigration> getMigrations();

    /**
     * Returns a short name for the model extension.
     */
    public String getShortName();

    /**
     * Returns a caption for the model extension.
     */
    public String getCaption();

    /**
     * Returns a description for the model extension.
     */
    public String getDescription();

    /**
     * Returns a set of project templates indentifiers to which instances of
     * the model extension are compatible.
     *
     * @return a set of project template identifiers, or null to indicate that
     * the ExtensionService is allowed for all ProjectTemplates.
     */
    public Set<String> getProjectTemplateIds();

    /**
     * Returns an XStream converter to render model extensions represented by this
     * extension service as REST resources.
     *
     * @return a converter, or <code>null</code> if the extension has no REST API.
     */
    public AliasedConverter getConverter(String host);

    /**
     * Returns the indexer that should be used to index instances of
     * the model extension.
     *
     * @return an indexer, or <code>null</code> if the extension has nothing to index.
     */
    public AbstractIndexer<T> getIndexer();

    /**
     * Returns the default caption for the given property.
     *
     * @return the default caption, or <code>null</code> if there is
     * no caption defined for the given property.
     */
    public String getCaption(String propertyName);

    /**
     * Returns the default description for the given property.
     *
     * @return the default description, or <code>null</code> if there is
     * no description defined for the given property.
     */
    public String getDescription(String propertyName);

    /**
     * Returns a list of confirmation warnings to display to the user when an extensible entity, e.g. a project,
     * it to be modified. Note that <code>entity</code> and/or <code>modifiedEntity</code> might not have
     * extensions of type {@link ExtensionService#getExtensionClass()} at all. In that case, the method should
     * return an empty warnings list, unless it performs some kind of cross-check with another extension. For
     * example, removing an extension from aproject, for which this extension service is responsible, might lead
     * to a serious problem in another extension.
     *
     * @param entity   the original entity.
     * @param modifiedEntity  the entity with modifications.
     * @param modifier  the person that tries to modify the entity.
     * @return  a list of confirmation warnings, or an empty list.
     */
    public List<String> getConfirmationWarnings(ExtensibleEntityBase entity, ExtensibleEntityBase modifiedEntity, User modifier);

    /**
     * Returns a set of property validators for a given property
     * of the model extensions represented by this extension service.
     *
     * @param propertyName  the identifier of a property.
     * @param caption  the caption of the property, or a blank string (
     *                 <code>null</code> or <code>""</code>), if the
     *                 default caption should be used to render meaningful
     *                 validation messages.
     *
     * @return a set of validators, or an empty set, if there are
     *         no validators for the given property.
     */
    public Set<PropertyValidator> getPropertyValidators(String propertyName, String caption);

    /**
     * Returns a set of extension validators for the model extensions
     * represented by this extension service.
     *
     * @param captions  a map of property captions with property names as keys.
     *                  If no caption is provided for a given property name,
     *                  the default caption of that property is used to render
     *                  meaningful validation messages.
     *
     * @return a set of validators, or an empty set, if there are
     *         no validators for the model extension.
     */
    public Set<ExtensionValidator<T>> getExtensionValidators(Map<String, String> captions);
}
