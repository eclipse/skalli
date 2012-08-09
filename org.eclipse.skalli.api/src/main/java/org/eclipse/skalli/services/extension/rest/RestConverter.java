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
package org.eclipse.skalli.services.extension.rest;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;

import com.thoughtworks.xstream.converters.Converter;

/**
 * Interfacing that represents REST converters for {@link EntityBase entities} and
 * their {@link ExtensionEntityBase extensions} or, more general, Java beans with
 * a REST representation. REST converters for entities are provided through
 * {@link ExtensionServices extension services}.
 * <p>
 * Implementations of this interface should usually be derived from {@link RestConverterBase}.
 *<p>
 * This interface is an extension to the {@link Converter XStream converter} interface and
 * adds certain methods for defining aliases, namespaces/schemas and API versions.
 */
public interface RestConverter extends Converter {

    /**
     * Defines the alias that should be used to represent an entity or extension
     * in the REST API. The alias is rendered as root tag of the XML representation.
     *
     * @return an alias, preferably a short and precise string, all lower-case and
     * compatible with the rules for naming of XML tags. Must not be <code>null</code>
     * or an empty string.
     */
    public String getAlias();

    /**
     * The class of the entity, extension or bean with which this converter is associated.
     * The result is, among others, required to check whether a given REST converter
     * is able to handle a certain entity or bean type, or not.
     *
     * @return the entity, extension or bean class. Must not be <code>null</code>.
     */
    public Class<?> getConversionClass();

    /**
     * The API version to render in the REST representation.
     *
     * @return the API version. Must not be <code>null</code>
     * or an empty string.
     */
    public String getApiVersion();

    /**
     * The namespace URI to render in the REST representation.
     * Note, the {@link RestUtils#API_NAMESPACE default namespace} is reserved for use by Skalli's
     * core entity model (projects, issues, configurations etc.). Extension should derived
     * their namespace by adding a <tt>/Extension-&lt;short name&gt;</tt> suffix to the default
     * namespace, where <tt>&lt;short name&gt;</tt> is the {@link ExtensionService#getShortName()
     * short name} of the extension.<br>
     * For example: <tt>"http://www.eclipse.org/skalli/2010/API/Extension-DevInf"</tt>.
     *
     * @return a valid XML namespace URI. Must not be <code>null</code>
     * or an empty string.
     */
    public String getNamespace();

    /**
     * Returns the name of an XML schema file that specifies the converter's XML output
     * format. Note, schema file must reside in the same bundle as the converter implementation
     * in a directory named <tt>/schemas</tt> below the bundles root directory.
     *
     * @return the name of the XML schema file, or <code>null</code> if there is no such file.
     * Since the REST API should be self describing, not providing a schema is strongly
     * discouraged.
     */
    public String getXsdFileName();
}
