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
package org.eclipse.skalli.model.ext.scrum.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.validators.HostReachableValidator;
import org.eclipse.skalli.services.extension.validators.MembersValidator;
import org.eclipse.skalli.services.extension.validators.URLValidator;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceScrum
        extends ExtensionServiceBase<ScrumProjectExt>
        implements ExtensionService<ScrumProjectExt>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceScrum.class);

    private static final String CAPTION = "Scrum";
    private static final String DESCRIPTION = "Information related to a SCRUM project like " +
            "SCRUM master, product owner and project backlog.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { ScrumProjectExt.PROPERTY_SCRUM_MASTERS, "SCRUM Masters" },
                    { ScrumProjectExt.PROPERTY_PRODUCT_OWNERS, "Product Owners" },
                    { ScrumProjectExt.PROPERTY_BACKLOG_URL, "Backlog" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { ScrumProjectExt.PROPERTY_SCRUM_MASTERS, "The SCRUM masters of this project" },
                    { ScrumProjectExt.PROPERTY_PRODUCT_OWNERS, "The product owners assigned to this project" },
                    { ScrumProjectExt.PROPERTY_BACKLOG_URL, "Browsable link to the project's backlog" } });

    private static final Map<String, String> INPUT_PROMPTS = CollectionUtils.asMap(new String[][] {
            { ScrumProjectExt.PROPERTY_BACKLOG_URL, URL_INPUT_PROMPT } });

    private static final String ALIAS_MEMBER = "member"; //$NON-NLS-1$

    @Override
    public Class<ScrumProjectExt> getExtensionClass() {
        return ScrumProjectExt.class;
    }

    @Override
    public ScrumProjectExt newExtension() {
        return new ScrumProjectExt();
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "scrum"; //$NON-NLS-1$
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
        aliases.put(ALIAS_MEMBER, Member.class);
        return aliases;
    }

    @Override
    public RestConverter getRestConverter(String host) {
        return new ScrumConverter(host);
    }

    @Override
    public RestConverter<ScrumProjectExt> getRestConverter() {
        return new ScrumConverter();
    }

    @Override
    public String getModelVersion() {
        return ScrumProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return ScrumProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-scrum.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<ScrumProjectExt> getIndexer() {
        return new ScrumIndexer();
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
        List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
        if (ScrumProjectExt.PROPERTY_BACKLOG_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        } else if (ScrumProjectExt.PROPERTY_PRODUCT_OWNERS.equals(propertyName)
                || ScrumProjectExt.PROPERTY_SCRUM_MASTERS.equals(propertyName)) {
            validators.add(new MembersValidator(Severity.ERROR, getExtensionClass(), propertyName, caption));
        }
        return validators;
    }
}
