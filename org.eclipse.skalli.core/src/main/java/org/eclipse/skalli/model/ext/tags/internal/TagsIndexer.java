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
package org.eclipse.skalli.model.ext.tags.internal;

import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.extension.IndexerBase;

public class TagsIndexer extends IndexerBase<TagsExtension> {

    @Override
    protected void indexFields(TagsExtension entity) {
        addField(TagsExtension.PROPERTY_TAGS, entity.getTags(), true, true);
    }

}
