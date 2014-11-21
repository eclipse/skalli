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
package org.eclipse.skalli.model;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helper class for the persistence of model extensions,
 * used by {@link ExtensibleEntityBase}.
 */
public class ExtensionsMap {

    /**
     * Map of extensions. Note, only the value set of this map is persisted,
     * see {@link org.eclipse.skalli.core.internal.persistence.xstream.ExtensionsMapConverter}.
     */
    private HashMap<String, ExtensionEntityBase> extensions;

    /**
     * Creates a new empty <code>ExtensionsMap</code>.
     */
    public ExtensionsMap() {
        extensions = new HashMap<String, ExtensionEntityBase>();
    }

    /**
     * Returns the extension instance for the given model extension,
     * or <code>null</code> if this no such instance exists.
     * @param <T> type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extensionClass  the model extension for which to retriebe an instance.
     */
    public <T extends ExtensionEntityBase> T getExtension(Class<T> extensionClass) {
        return extensionClass.cast(extensions.get(extensionClass.getName()));
    }

    /**
     * Returns all currently stored extensions sorted according to
     * {@link ExtensionsComparator}, i.e. by class name. Note, the returned
     * set is not backed by the underlying storage.
     */
    public SortedSet<ExtensionEntityBase> getAllExtensions() {
        TreeSet<ExtensionEntityBase> result = new TreeSet<ExtensionEntityBase>(new ExtensionsComparator());
        result.addAll(extensions.values());
        return result;
    }

    /**
     * Adds another extension instance.
     * @param <T>  type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extension  the extension to add.
     */
    public <T extends ExtensionEntityBase> void putExtension(T extension) {
        if (extension != null) {
            extensions.put(extension.getClass().getName(), extension);
        }
    }

    /**
     * Removes the extension instance corresponding to the given model extension.
     * @param <T> type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extensionClass  the model extension to remove.
     * @return  the extension instance that has previously been added for the given
     * model extension, or <code>null<code> if there is no such instance.
     */
    public <T extends ExtensionEntityBase> T removeExtension(Class<T> extensionClass) {
        return extensionClass.cast(extensions.remove(extensionClass.getName()));
    }

    /**
     * Returns <code>true</code> if there are no extension instances in this map.
     */
    public boolean isEmpty() {
        return extensions.isEmpty();
    }
}
