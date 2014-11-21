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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Abstract base class for all model entities that can have extensions.
 * The most prominent (and currently only) representative of an extensible entity is
 * {@link org.eclipse.skalli.services.projects.Project}.
 */
public abstract class ExtensibleEntityBase extends ExtensionEntityBase {

    private ExtensionsMap extensions;
    private HashSet<String> inheritedExtensions;

    /**
     * Retrieves the model extension matching the given extension class.
     * If the extension is inherited and this entity has a parent entity,
     * then the corresponding extension of the parent entity is returned, if any.
     *
     * @param <T>  type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extensionClass  the class of the model extension to retrieve.
     */
    public <T extends ExtensionEntityBase> T getExtension(Class<T> extensionClass) {
        T extension = null;
        if (inheritedExtensions != null && inheritedExtensions.contains(extensionClass.getName())) {
            EntityBase parent = getParentEntity();
            if (parent instanceof ExtensibleEntityBase) {
                extension = ((ExtensibleEntityBase) parent).getExtension(extensionClass);
            }
        }
        else if (extensions != null) {
            extension = extensions.getExtension(extensionClass);
        }
        return extension;
    }

    /**
     * Retrieves model extensions that implement a given additional interface.
     * Iterates through all extensions and checks with {@link Class#isAssignableFrom(Class)}
     * whether the extension is compatible with the given argument.
     *
     * @param implementedInterface  the interface to search for.
     * @return a set of extensions that implement the given interface, or an
     * empty set.
     */
    public <T> Set<T> getExtensionsImplementing(Class<T> implementedInterface) {
        HashSet<T> result = new HashSet<T>();
        for (ExtensionEntityBase ext: getAllExtensions()) {
            if (implementedInterface.isAssignableFrom(ext.getClass())) {
                result.add(implementedInterface.cast(ext));
            }
        }
        return result;
    }

    /**
     * Retrieves the model extensions that implements a given additional interface.
     *
     * @param implementedInterface the interface to search for.
     * @return  the extension that implements the given interface, or <code>null</code>
     * if there is no such extension.
     *
     * @throws IllegalStateException  if more than one extension implements the
     * given interface.
     */
    public <T> T getExtensionImplementing(Class<T> implementedInterface) {
        Set<T> implementigExtensions = getExtensionsImplementing(implementedInterface);
        if (implementigExtensions.size() > 1) {
            throw new IllegalStateException(MessageFormat.format("Entity has multiple extensions implementing {0}",
                    implementedInterface.getName()));
        }
        return implementigExtensions.isEmpty() ? null : implementigExtensions.iterator().next();
    }

    /**
     * Retrieves all assigned model extensions.
     *
     * @return  an unmodifiable but sorted set of all extensions (see {@link ExtensionsComparator}).
     */
    public SortedSet<ExtensionEntityBase> getAllExtensions() {
        if (extensions == null) {
            return new TreeSet<ExtensionEntityBase>(new ExtensionsComparator());
        }
        return extensions.getAllExtensions();
    }

    /**
     * Adds the given extension instance to this extensible entity.
     *
     * Calls {@link ExtensionEntityBase#setExtensibleEntity(ExtensibleEntityBase)
     * ExtensionEntityBase#setExtensibleEntity(this)} on the model extension.
     * If the model extension corresponding to the given extension instance is inherited,
     * inheritance is switched off.
     *
     * @param <T>  type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extension  the model extension to add.
     */
    public <T extends ExtensionEntityBase> void addExtension(T extension) {
        removeInherited(extension);
        add(extension);
    }

    /**
     * Removes the model extension matching the given extension class.
     * Calls {@link ExtensionEntityBase#setExtensibleEntity(ExtensibleEntityBase)
     * ExtensionEntityBase#setExtensibleEntity(null)} on the removed model extension.
     * If the model extension corresponding to the given extension instance is inherited,
     * inheritance is switched off.
     *
     * @param <T>  type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extensionClass  the class of the model extension to remove.
     *
     * @return  the model extension that has been removed from this extensible entity,
     * or <code>null</code> if no such model extension was assigned to this extensible entity.
     */
    public <T extends ExtensionEntityBase> T removeExtension(Class<T> extensionClass) {
        removeInherited(extensionClass);
        return remove(extensionClass);
    }

    /**
     * Determines whether the given model extension should be inherited from a parent entity.
     *
     * An instance of the given extension currently associated with this entity will be removed
     * even if the entity currently has no parent entity. In that case, {@link #getExtension(Class)}
     * will return <code>null</code> unless a parent entity with a suitable extension instance
     * is assigned. If there is a parent entity, {@link #getExtension(Class)} will retrieve the
     * extension instance from the parent entity or an entity further up the parent hierarchy
     * if the direct parent has no instance of the extension registered. Note that switching off
     * inheritance for a given model extension does not automatically create an extension instance.
     * A new instance must be added explicitly with {@link #addExtension(ExtensionEntityBase)}.
     *
     * @param <T>  type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extensionClass  the class of the model extension to inherit.
     * @param inherit  if <code>true</code> the model extension is inherited.
     *
     * @return  the model extension that has been removed from this extensible entity,
     * or <code>null</code> if no such model extension was assigned to this extensible entity.
     */
    public <T extends ExtensionEntityBase> T setInherited(Class<T> extensionClass, boolean inherit) {
        T extension = null;
        if (inherit) {
            extension = remove(extensionClass);
            addInherited(extensionClass);
        }
        else {
            removeInherited(extensionClass);
        }
        return extension;
    }

    /**
     * Checks whether the given model extension is inherited from a parent entity.
     * Note that this method may return <code>true</code> even if this entity
     * currently has no parent entity. In that case {@link #getExtension(Class)}
     * will return <code>null</code> unless a parent entity with a suitable extension
     * instance is assigned.
     *
     * @param <T>  type of a model extension derived from <code>ExtensionEntityBase</code>.
     * @param extensionClass  the class of the model extension to check.
     * @return  <code>true</code>, if the extension is inherited.
     */
    public <T extends ExtensionEntityBase> boolean isInherited(Class<T> extensionClass) {
        return inheritedExtensions != null ? inheritedExtensions.contains(extensionClass.getName()) : false;
    }

    private <T extends ExtensionEntityBase> void add(T extension) {
        if (extension != null) {
            if (extensions == null) {
                extensions = new ExtensionsMap();
            }
            extension.setExtensibleEntity(this);
            extensions.putExtension(extension);
        }
    }

    private <T extends ExtensionEntityBase> T remove(Class<T> extensionClass) {
        T extension = null;
        if (extensionClass != null && extensions != null) {
            extension = extensions.removeExtension(extensionClass);
            if (extensions.isEmpty()) {
                extensions = null;
            }
            if (extension != null) {
                extension.setExtensibleEntity(null);
            }
        }
        return extension;
    }

    private <T extends ExtensionEntityBase> void addInherited(Class<T> extensionClass) {
        if (extensionClass != null) {
            if (inheritedExtensions == null) {
                inheritedExtensions = new HashSet<String>();
            }
            inheritedExtensions.add(extensionClass.getName());
        }
    }

    private <T extends ExtensionEntityBase> void removeInherited(T extension) {
        if (extension != null) {
            removeInherited(extension.getClass());
        }
    }

    private <T extends ExtensionEntityBase> void removeInherited(Class<T> extensionClass) {
        if (extensionClass != null && inheritedExtensions != null) {
            inheritedExtensions.remove(extensionClass.getName());
            if (inheritedExtensions.isEmpty()) {
                inheritedExtensions = null;
            }
        }
    }
}
