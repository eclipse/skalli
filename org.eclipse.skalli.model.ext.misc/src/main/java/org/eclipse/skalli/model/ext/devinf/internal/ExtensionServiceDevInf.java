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
package org.eclipse.skalli.model.ext.devinf.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapping;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappings;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.validators.HostReachableValidator;
import org.eclipse.skalli.services.extension.validators.URLValidator;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceDevInf
        extends ExtensionServiceBase<DevInfProjectExt>
        implements ExtensionService<DevInfProjectExt>, EventListener<EventConfigUpdate>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceDevInf.class);

    private static final String CAPTION = "Development Infrastructure";
    private static final String DESCRIPTION =
            "Information related to the project's development infrastructure like " +
                    "used source repositories, continous integration server and quality metrics.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { DevInfProjectExt.PROPERTY_SCM_URL, "Source Code" },
                    { DevInfProjectExt.PROPERTY_SCM_LOCATIONS, "Repositories" },
                    { DevInfProjectExt.PROPERTY_BUGTRACKER_URL, "Bugtracker" },
                    { DevInfProjectExt.PROPERTY_CI_URL, "Build" },
                    { DevInfProjectExt.PROPERTY_METRICS_URL, "Quality" },
                    { DevInfProjectExt.PROPERTY_REVIEW_URL, "Code Review" },
                    { DevInfProjectExt.PROPERTY_JAVADOCS_URL, "Javadoc" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils
            .addAll(ExtensionServiceBase.DESCRIPTIONS,
                    new String[][] {
                            { DevInfProjectExt.PROPERTY_SCM_URL,
                                    "Browsable link to the project's source code repository" },
                            { DevInfProjectExt.PROPERTY_SCM_LOCATIONS,
                                    "Source code repositories used by this project" },
                            { DevInfProjectExt.PROPERTY_BUGTRACKER_URL,
                                    "Browsable link to the project's issue management system" },
                            { DevInfProjectExt.PROPERTY_CI_URL,
                                    "Browsable link to the project's continous integration and build system" },
                            { DevInfProjectExt.PROPERTY_METRICS_URL,
                                    "Browsable link to the project's quality metrics system" },
                            { DevInfProjectExt.PROPERTY_REVIEW_URL,
                                    "Browsable link to the project's code review system" },
                            { DevInfProjectExt.PROPERTY_JAVADOCS_URL,
                                    "Browsable link to the Javadoc of this project" } });

    private static final Map<String, String> INPUT_PROMPTS = CollectionUtils.asMap(new String[][] {
            { DevInfProjectExt.PROPERTY_SCM_LOCATIONS, "scm:<scm-type>:<scm-location>" },
            { DevInfProjectExt.PROPERTY_SCM_URL, URL_INPUT_PROMPT },
            { DevInfProjectExt.PROPERTY_BUGTRACKER_URL, URL_INPUT_PROMPT },
            { DevInfProjectExt.PROPERTY_CI_URL, URL_INPUT_PROMPT },
            { DevInfProjectExt.PROPERTY_METRICS_URL, URL_INPUT_PROMPT },
            { DevInfProjectExt.PROPERTY_REVIEW_URL, URL_INPUT_PROMPT },
            { DevInfProjectExt.PROPERTY_JAVADOCS_URL, URL_INPUT_PROMPT } });

    private ConfigurationService configService;
    private List<Pattern> indexPatterns;

    @Override
    public Class<DevInfProjectExt> getExtensionClass() {
        return DevInfProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.indexPatterns = getIndexPatterns(configService);
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.indexPatterns = null;
        this.configService = null;
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventConfigUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "devInf"; //$NON-NLS-1$
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
        return new DevInfConverter(host);
    }

    @Override
    public RestConverter<DevInfProjectExt> getRestConverter() {
        return new DevInfConverter();
    }

    @Override
    public String getModelVersion() {
        return DevInfProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return DevInfProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-devinf.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<DevInfProjectExt> getIndexer() {
        return new DevInfIndexer(indexPatterns);
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
        if (DevInfProjectExt.PROPERTY_SCM_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_SCM_LOCATIONS.equals(propertyName)) {
            validators.add(new SCMValidator(caption));
        }
        else if (DevInfProjectExt.PROPERTY_BUGTRACKER_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_CI_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_METRICS_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_REVIEW_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_JAVADOCS_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }

    private List<Pattern> getIndexPatterns(ConfigurationService configService) {
        List<Pattern> patterns = new ArrayList<Pattern>();
        if (configService != null) {
            ScmLocationMappings mappingsConfig = configService.readConfiguration(ScmLocationMappings.class);
            if (mappingsConfig != null) {
                List<ScmLocationMapping> mappings = mappingsConfig.getScmMappings();
                if (mappings != null) {
                    for (ScmLocationMapping mapping : mappings) {
                        if (ComparatorUtils.equals(ScmLocationMapper.PURPOSE_INDEXING, mapping.getPurpose())
                                && StringUtils.isNotBlank(mapping.getPattern())) {
                            try {
                                patterns.add(Pattern.compile(mapping.getPattern()));
                            } catch (PatternSyntaxException e) {
                                LOG.warn(MessageFormat.format("''{0}'' is not a valid regular expression",
                                        mapping.getPattern()), e);
                            }
                        }
                    }
                }
            }
        }
        return patterns;
    }

    @Override
    public void onEvent(EventConfigUpdate event) {
        if (ScmLocationMappings.class.equals(event.getConfigClass())) {
            indexPatterns = getIndexPatterns(configService);
        }
    }
}
