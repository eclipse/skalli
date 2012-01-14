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
package org.eclipse.skalli.model.ext.people.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServicePeople
        extends ExtensionServiceBase<PeopleExtension>
        implements ExtensionService<PeopleExtension>
{
    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServicePeople.class);

    private static final String CAPTION = "Project Members";
    private static final String DESCRIPTION =
            "Information related to project members and leads.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { PeopleExtension.PROPERTY_LEADS, "Project Leads" },
                    { PeopleExtension.PROPERTY_MEMBERS, "Committers" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { PeopleExtension.PROPERTY_LEADS, "The leads of this project" },
                    { PeopleExtension.PROPERTY_MEMBERS, "The comitters of this project" } });

    private static final String ALIAS_MEMBER = "member"; //$NON-NLS-1$

    @Override
    public Class<PeopleExtension> getExtensionClass() {
        return PeopleExtension.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getModelVersion() {
        return PeopleExtension.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return PeopleExtension.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-people.xsd"; //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "people"; //$NON-NLS-1$
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
        return new PeopleConverter(host);
    }

    @Override
    public Indexer<PeopleExtension> getIndexer() {
        return new PeopleIndexer();
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
    public List<String> getConfirmationWarnings(ExtensibleEntityBase entity, ExtensibleEntityBase modifiedEntity, User modifier) {
        List<String> warnings = new ArrayList<String>();
        PeopleExtension extension = entity.getExtension(PeopleExtension.class);
        PeopleExtension modifiedExtension = modifiedEntity.getExtension(PeopleExtension.class);
        if (extension != null && modifiedExtension != null) {
            Member member = new Member(modifier.getUserId());
            if (extension.hasLead(member) && !modifiedExtension.hasLead(member)) {
                warnings.add("You are trying to remove yourself from the list of <i>project leads</i>.");
            }
        }
        return warnings;
    }
}
