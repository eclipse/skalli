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
package org.eclipse.skalli.model.ext.linkgroups.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupValidator;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceLinkGroups
        extends ExtensionServiceBase<LinkGroupsProjectExt>
        implements ExtensionService<LinkGroupsProjectExt>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceLinkGroups.class);

    private static final String CAPTION = "Additional Links";
    private static final String DESCRIPTION = "Information related to the project that is maintained elsewhere and linkable.";

    @Override
    public Class<LinkGroupsProjectExt> getExtensionClass() {
        return LinkGroupsProjectExt.class;
    }

    @Override
    public LinkGroupsProjectExt newExtension() {
        return new LinkGroupsProjectExt();
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "linkGroups"; //$NON-NLS-1$
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
    public Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = super.getAliases();
        aliases.put("linkGroup", LinkGroup.class); //$NON-NLS-1$
        aliases.put("link", Link.class); //$NON-NLS-1$
        return aliases;
    }

    @Deprecated
    @Override
    public RestConverter getRestConverter(String host) {
        return new LinkGroupsConverter(host);
    }

    @Override
    public RestConverter<LinkGroupsProjectExt> getRestConverter() {
        return new LinkGroupsConverter();
    }

    @Override
    public String getModelVersion() {
        return LinkGroupsProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return LinkGroupsProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "linksgroups.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<LinkGroupsProjectExt> getIndexer() {
        return new LinkGroupsIndexer();
    }

    @Override
    public List<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
        if (LinkGroupsProjectExt.PROPERTY_LINKGROUPS.equals(propertyName)) {
            validators.add(new LinkGroupValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
