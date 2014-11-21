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
package org.eclipse.skalli.core.extension.tags;

import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceTags extends ExtensionServiceBase<TagsExtension> {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceTags.class);

    private static final String CAPTION = "Tags";
    private static final String DESCRIPTION = "Tag information assigned to a project";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { TagsExtension.PROPERTY_TAGS, "Tags" }});

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils
            .addAll(ExtensionServiceBase.DESCRIPTIONS,
                    new String[][] {
                            { TagsExtension.PROPERTY_TAGS,
                                    "Collection of tags assigned to a project" } });

    @Override
    public Class<TagsExtension> getExtensionClass() {
        return TagsExtension.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "tags"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getModelVersion() {
        return TagsExtension.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return TagsExtension.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-tags.xsd"; //$NON-NLS-1$;
    }

    @Deprecated
    @Override
    public RestConverter<?> getRestConverter(String host) {
        return new TagsConverter(host);
    }

    @Override
    public RestConverter<TagsExtension> getRestConverter() {
        return new TagsConverter();
    }


    @Override
    public Indexer<TagsExtension> getIndexer() {
        return new TagsIndexer();
    }

    @Override
    public String getCaption(String propertyName) {
        return CAPTIONS.get(propertyName);
    }

    @Override
    public String getDescription(String propertyName) {
        return DESCRIPTIONS.get(propertyName);
    }
}
