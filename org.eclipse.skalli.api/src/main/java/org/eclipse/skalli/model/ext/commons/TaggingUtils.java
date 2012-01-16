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
package org.eclipse.skalli.model.ext.commons;

import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Taggable;

public class TaggingUtils {

    private TaggingUtils() {
    }

    public static boolean hasTag(ExtensibleEntityBase entity, String tag) {
        Taggable taggable = entity.getExtensionImplementing(Taggable.class);
        return taggable != null? taggable.hasTag(tag) : false;
    }

    public static SortedSet<String> getTags(ExtensibleEntityBase entity) {
        Taggable taggable = entity.getExtensionImplementing(Taggable.class);
        if (taggable == null) {
            return CollectionUtils.emptySortedSet();
        }
        return taggable.getTags();
    }

    public static void removeTag(ExtensibleEntityBase entity, String tag) {
        if (tag != null) {
            Taggable taggable = entity.getExtensionImplementing(Taggable.class);
            if (taggable != null) {
                taggable.removeTag(tag);
            }
        }
    }

    public static void addTags(ExtensibleEntityBase entity, String... tags) {
        if (tags != null && tags.length > 0) {
            Taggable taggable = entity.getExtensionImplementing(Taggable.class);
            if (taggable == null) {
                TagsExtension tagsExtension = new TagsExtension(tags);
                entity.addExtension(tagsExtension);
            } else {
                taggable.addTags(tags);
            }
        }
    }
}
