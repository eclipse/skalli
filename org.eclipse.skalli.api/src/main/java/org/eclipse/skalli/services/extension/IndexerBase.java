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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.search.IndexEntry;

public abstract class IndexerBase<T extends EntityBase> implements Indexer<T> {

    private List<IndexEntry> fields;

    /**
     * Add entity fields to the search index.
     *
     * Implementations of this method should add all needed fields from the given entity
     * to the search index by using the {@link #addField(String, Set, boolean, boolean)} or {@link #addField(String, String, boolean, boolean)} method.
     *
     * @param entity the entity object to be indexed.
     */
    protected abstract void indexFields(T entity);

    /**
     * Determines which fields should be considered additionally when using the standard search facility.
     *
     * <p>
     * The SearchService implementation will obey those fields and therefore the search scope can be extended by overriding this method.
     * </p>
     * <p>
     * The field names must match those used in {@link #indexFields(EntityBase)}.
     * </p>
     *
     * @return a set of field names or <code>null</code>.
     */
    public Set<String> getDefaultSearchFields() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public final void indexEntity(List<IndexEntry> fields, Object entity) {
        if (entity == null) {
            return;
        }
        this.fields = fields;
        indexFields((T) entity);
    }

    /**
     * Adds a field with its value to the search index.
     *
     * @param fieldName name of the field in the search index. May be refered to in {@link #getDefaultSearchFields()}.
     * @param value value of the field.
     * @param stored true if the value should be stored in the index, otherwise only the statistics will be stored. Needed for highlighting.
     * @param indexed true if the field should searchable. Setting this to false only makes sense in rare cases.
     */
    protected final void addField(String fieldName, String value, boolean stored, boolean indexed) {
        if (!StringUtils.isEmpty(value)) {
            IndexEntry field = new IndexEntry(fieldName, value, stored ? IndexEntry.Stored.YES : IndexEntry.Stored.NO,
                    indexed ? IndexEntry.Indexed.TOKENIZED : IndexEntry.Indexed.NO);
            fields.add(field);
        }
    }

    /**
     * Adds a field with its multiple values to the search index.
     *
     * @param fieldName name of the field in the search index. May be refered to in {@link #getDefaultSearchFields()}.
     * @param values values of the field.
     * @param stored true if the value should be stored in the index, otherwise only the statistics will be stored. Needed for highlighting.
     * @param indexed true if the field should searchable. Setting this to false only makes sense in rare cases.
     */
    protected final void addField(String fieldName, Set<?> values, boolean stored, boolean indexed) {
        if (values == null) {
            return;
        }
        for (Object value : values) {
            if (value != null) {
                addField(fieldName, value.toString(), stored, indexed);
            }
        }
    }

}
