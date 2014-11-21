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
package org.eclipse.skalli.model.ext.misc.internal;

import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.ext.misc.ReviewProjectExt;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceReview
        extends ExtensionServiceBase<ReviewProjectExt>
        implements ExtensionService<ReviewProjectExt>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceReview.class);

    private static final String CAPTION = "Ratings & Reviews";
    private static final String DESCRIPTION = "Enables ratings and reviews for the project.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { ReviewProjectExt.PROPERTY_ALLOW_ANONYMOUS, "Allow anonymous reviews" },
                    { ReviewProjectExt.PROPERTY_RATING_STYLE, "Rating Style" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { ReviewProjectExt.PROPERTY_ALLOW_ANONYMOUS,
                            "If checked, users can review this project anonymously" },
                    { ReviewProjectExt.PROPERTY_RATING_STYLE, "Selects from a variety of rating styles" } });

    @Override
    public Class<ReviewProjectExt> getExtensionClass() {
        return ReviewProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "reviews"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Deprecated
    @Override
    public RestConverter getRestConverter(String host) {
        return new ReviewConverter(host);
    }

    @Override
    public RestConverter<ReviewProjectExt> getRestConverter() {
        return new ReviewConverter();
    }

    @Override
    public String getModelVersion() {
        return ReviewProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return ReviewProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-review.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<ReviewProjectExt> getIndexer() {
        return new ReviewIndexer();
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
