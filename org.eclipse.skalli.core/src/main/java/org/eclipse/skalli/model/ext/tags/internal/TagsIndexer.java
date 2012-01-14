package org.eclipse.skalli.model.ext.tags.internal;

import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.extension.IndexerBase;

public class TagsIndexer extends IndexerBase<TagsExtension> {

    @Override
    protected void indexFields(TagsExtension entity) {
        addField(TagsExtension.PROPERTY_TAGS, entity.getTags(), true, true);
    }

}
