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
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.rest.RestConverter;

public class ExtensionServiceRelatedProjects extends ExtensionServiceBase<RelatedProjectsExt>
        implements ExtensionService<RelatedProjectsExt> {

    private static final String DESCRIPTION = "Information about projects that might be related to this projects or could also be of interest.";

    private static final String CAPTION = "Related Projects";

    private static final Map<String, String> CAPTIONS = CollectionUtils
            .asMap(new String[][] {
                    { RelatedProjectsExt.PROPERTY_RELATED_PROJECT, "Related Projects" },
                    { RelatedProjectsExt.PROPERTY_CALCULATED,
                            "Calculate Related Projects (based on similarities to this project)" } });

    @Override
    public Class<RelatedProjectsExt> getExtensionClass() {
        return RelatedProjectsExt.class;
    }

    @Override
    public String getModelVersion() {
        return RelatedProjectsExt.MODEL_VERSION;
    }

    @Override
    public String getShortName() {
        return "relatedProjects";
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public String getCaption(String propertyName) {
        String caption = CAPTIONS.get(propertyName);
        if (caption == null) {
            caption = super.getCaption(propertyName);
        }
        return caption;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getNamespace() {
        return RelatedProjectsExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return null;
    }

    @Deprecated
    @Override
    public RestConverter getRestConverter(String host) {
        return new RelatedProjectsConverter(host);
    }

    @Override
    public RestConverter<RelatedProjectsExt> getRestConverter() {
        return new RelatedProjectsConverter();
    }
}
