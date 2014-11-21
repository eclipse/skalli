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
package org.eclipse.skalli.services.extension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.rest.RestConverter;

/**
 * Interface of a service that defines a model extension.
 *
 * An implementation of this interface should be derived from the
 * base class {@link ExtensionServiceBase} and must be registered as
 * an OSGI service component.
 * <br>
 * For example:
 * <pre>
 * &lt;component immediate="true" name="org.eclipse.skalli.ext.info"&gt;
 *   &lt;implementation class="org.eclipse.skalli.core.extension.info.ExtensionServiceInfo"/&gt;
 *   &lt;service&gt;
 *     &lt;provide interface="org.eclipse.skalli.services.extension.ExtensionService"/&gt;
 *   &lt;/service&gt;
 * &lt;/component&gt;
 * </pre>
 * Note, extension services should be started as early as possible, hence
 * the declaration as <i>immediate</i>.
 */
public interface ExtensionService<T extends ExtensionEntityBase> {

    /**
     * Returns the class of the model extension associated with this extension service.
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
     * Returns a short name for the model extension.
     * The short name of an extension must be unique among all extensions in the system.
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
     * Returns aliases for <b>additional</b> classes used to defined the data model
     * of the extension. Defining aliases makes marshaling/unmarshaling of the extension
     * independent of the concrete class names.
     * <p>
     * Note, an alias for the {@link #getExtensionClass() extension class} is ignored by
     * the persistence service.
     *
     * @return a map which maps aliases to classes, or an empty map.
     */
    public Map<String, Class<?>> getAliases();

    /**
     * Returns classloaders for <b>additional</b> classes used to defined the data model
     * of the extension. The persistence service adds the classloader of the extension
     * service by default, so classes contained in the same bundle as the extension service
     * do not need a custom classloader.
     *
     * @return a set of classloaders, or an empty set.
     */
    public Set<ClassLoader> getClassLoaders();

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
    @Deprecated
    public RestConverter<?> getRestConverter(String host);

    /**
     * Returns an XStream converter to render model extensions represented by this
     * extension service as REST resources.
     *
     * @return a converter, or <code>null</code> if the extension has no REST API.
     */
    public RestConverter<T> getRestConverter();

    /**
     * Returns the indexer that should be used to index instances of
     * the model extension.
     *
     * @return an indexer, or <code>null</code> if the extension has nothing to index.
     */
    public Indexer<T> getIndexer();

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
     * Returns the input prompt to be displayed in form fields
     * as help for the user what to enter in a field.
     *
     * @return  the input prompt, or <code>null</code> if there is
     * no input prompt defined for the given property.
     */
    public String getInputPrompt(String propertyName);

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
    public List<PropertyValidator> getPropertyValidators(String propertyName, String caption);

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
    public List<ExtensionValidator<T>> getExtensionValidators(Map<String, String> captions);
}
