package org.eclipse.skalli.services.extension;

import java.util.List;
import java.util.Set;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.search.IndexEntry;

public interface Indexer<T extends EntityBase> {

    public Set<String> getDefaultSearchFields();
    public void indexEntity(List<IndexEntry> fields, Object entity);

}
