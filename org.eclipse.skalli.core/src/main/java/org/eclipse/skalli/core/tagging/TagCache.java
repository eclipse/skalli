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
package org.eclipse.skalli.core.tagging;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Taggable;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.tagging.TagCount;

/**
 * Cache for tags of a given entity type. This cache is designed to support non-blocking
 * read access to the recorded tags while still being thread-safe on occasional updates.
 * Tags are provided either sorted by {@link #getByName() tag name}, or by decreasing
 * {@link #getByCount() number of occurences}.
 */
public class TagCache {

    /** Map that assigns tag names with the entities that have them */
    private Map<String,Set<UUID>> byEntity = new HashMap<String,Set<UUID>>();

    /** All known tags sorted alphanumerically */
    private volatile ConcurrentSkipListMap<String,Integer> byName =  new ConcurrentSkipListMap<String,Integer>();

    /** All known tags sorted by decreasing number of occurence */
    private volatile ConcurrentSkipListSet<TagCount> byCount =  new ConcurrentSkipListSet<TagCount>();


    SortedMap<String,Integer> getByName() {
        return Collections.unmodifiableSortedMap(byName);
    }

    SortedSet<TagCount> getByCount() {
        return Collections.unmodifiableSortedSet(byCount);
    }

    SortedSet<TagCount> getByCount(int size) {
        if (size < 0) {
            return Collections.unmodifiableSortedSet(byCount);
        }
        TreeSet<TagCount> result = new TreeSet<TagCount>();
        int i = 0;
        Iterator<TagCount> it = byCount.iterator();
        while (i < size && it.hasNext()) {
            result.add(it.next());
            ++i;
        }
        return result;
    }

    void update(EntityBase entity) {
        synchronized (byEntity) {
            removeTags(entity);
            addTags(entity);
            reindexTags();
        }
    }

    void initialize(EntityService<?> entityService) {
        synchronized (byEntity) {
            for (UUID entityId: entityService.keySet()) {
                EntityBase entity = entityService.getByUUID(entityId);
                if (entity != null) {
                    addTags(entity);
                }
            }
            reindexTags();
        }
    }

    private void reindexTags() {
        ConcurrentSkipListMap<String,Integer> byName =  new ConcurrentSkipListMap<String,Integer>();
        for (Entry<String,Set<UUID>> entry: byEntity.entrySet()) {
            byName.put(entry.getKey(), entry.getValue().size());
        }
        ConcurrentSkipListSet<TagCount> byCount =  new ConcurrentSkipListSet<TagCount>();
        for (Entry<String,Integer> entry: byName.entrySet()) {
            byCount.add(new TagCount(entry.getKey(), entry.getValue()));
        }
        this.byName = byName;
        this.byCount = byCount;
    }

    private void removeTags(EntityBase entity) {
        UUID entityId = entity.getUuid();
        Iterator<Set<UUID>> it = byEntity.values().iterator();
        while (it.hasNext()) {
            Set<UUID> next = it.next();
            next.remove(entityId);
            if (next.isEmpty()) {
                it.remove();
            }
        }
    }

    private void addTags(EntityBase entity) {
        if (!entity.isDeleted()) {
            if (entity instanceof Taggable) {
                addTags(entity.getUuid(), (Taggable)entity);
            } else if (entity instanceof ExtensibleEntityBase) {
                for (ExtensionEntityBase ext: ((ExtensibleEntityBase)entity).getAllExtensions()) {
                    if (ext instanceof Taggable) {
                        addTags(entity.getUuid(), (Taggable)ext);
                    }
                }
            }
        }
    }

    private void addTags(UUID entityId, Taggable taggable) {
        Set<String> tags = taggable.getTags();
        for (String tag: tags) {
            Set<UUID> entityIds = byEntity.get(tag);
            if (entityIds == null) {
                entityIds = new HashSet<UUID>();
                byEntity.put(tag, entityIds);
            }
            entityIds.add(entityId);
        }
    }
}
