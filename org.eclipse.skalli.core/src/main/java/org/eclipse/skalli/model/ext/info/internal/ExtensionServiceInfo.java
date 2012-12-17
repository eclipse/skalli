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
package org.eclipse.skalli.model.ext.info.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.validators.EmailValidator;
import org.eclipse.skalli.services.extension.validators.HostReachableValidator;
import org.eclipse.skalli.services.extension.validators.URLValidator;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceInfo
        extends ExtensionServiceBase<InfoExtension>
        implements ExtensionService<InfoExtension>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceInfo.class);

    private static final String CAPTION = "Info";
    private static final String DESCRIPTION = "Additional information related to the project";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { InfoExtension.PROPERTY_MAILING_LIST, "Mailing Lists" },
                    { InfoExtension.PROPERTY_PAGE_URL, "Project Homepage" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { InfoExtension.PROPERTY_MAILING_LIST, "Mailing lists provided by this project" },
                    { InfoExtension.PROPERTY_PAGE_URL, "Browsable link to the project's homepage" } });

    private static final Map<String, String> INPUT_PROMPTS = CollectionUtils.asMap(new String[][] {
            { InfoExtension.PROPERTY_PAGE_URL, URL_INPUT_PROMPT } });

    @Override
    public Class<InfoExtension> getExtensionClass() {
        return InfoExtension.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "info"; //$NON-NLS-1$
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
    public RestConverter getRestConverter(String host) {
        return new InfoConverter(host);
    }

    @Override
    public String getModelVersion() {
        return InfoExtension.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return InfoExtension.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-info.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<InfoExtension> getIndexer() {
        return new InfoIndexer();
    }

    @Override
    public String getCaption(String propertyName) {
        return CAPTIONS.get(propertyName);
    }

    @Override
    public String getDescription(String propertyName) {
        return DESCRIPTIONS.get(propertyName);
    }

    @Override
    public String getInputPrompt(String propertyName) {
        return INPUT_PROMPTS.get(propertyName);
    }

    @Override
    public List<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        caption = getCaption(propertyName, caption);
        List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
        if (InfoExtension.PROPERTY_MAILING_LIST.equals(propertyName)) {
            validators.add(new EmailValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
        }
        else if (InfoExtension.PROPERTY_PAGE_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
