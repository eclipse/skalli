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
package org.eclipse.skalli.model.core.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.validators.RegularExpressionValidator;
import org.eclipse.skalli.services.extension.validators.StringLengthValidator;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceCore
        extends ExtensionServiceBase<Project>
        implements ExtensionService<Project>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceCore.class);

    private static final String CAPTION = "Basics";
    private static final String DESCRIPTION = "Information related to all projects.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { EntityBase.PROPERTY_PARENT_ENTITY_ID, "Parent Project ID" },
                    { EntityBase.PROPERTY_PARENT_ENTITY, "Parent Project" },
                    { Project.PROPERTY_PROJECTID, "Project ID" },
                    { Project.PROPERTY_TEMPLATEID, "Project Template" },
                    { Project.PROPERTY_PARENT_PROJECT, "Parent Project" },
                    { Project.PROPERTY_NAME, "Display Name" },
                    { Project.PROPERTY_SHORT_NAME, "Short Name" },
                    { Project.PROPERTY_DESCRIPTION_FORMAT, "Description Format" },
                    { Project.PROPERTY_DESCRIPTION, "Description" },
                    { Project.PROPERTY_LOGO_URL, "Project Logo" },
                    { Project.PROPERTY_PHASE, "Project Phase" },
                    { Project.PROPERTY_REGISTERED, "Registered at" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { EntityBase.PROPERTY_PARENT_ENTITY, "Project to which this project is assigned as subproject" },
                    { EntityBase.PROPERTY_PARENT_ENTITY_ID,
                            "Unique identifier of a project to which this project is assigned as subproject" },
                    { Project.PROPERTY_PROJECTID, "Unique symbolic name for this project" },
                    { Project.PROPERTY_TEMPLATEID, "Identifier of the project template this project is derived from" },
                    { Project.PROPERTY_PARENT_PROJECT, "Project to which this project is a subproject" },
                    { Project.PROPERTY_NAME, "A human readable name for this project" },
                    { Project.PROPERTY_SHORT_NAME, "Abbreviation of the project name. If not maintained, it will be " +
                            "contructed automatically from the display name if needed." },
                    { Project.PROPERTY_DESCRIPTION_FORMAT, "Format of the description" },
                    { Project.PROPERTY_DESCRIPTION, "Description of this project" },
                    { Project.PROPERTY_LOGO_URL, "Link to a picture that serves as logo of this project" },
                    { Project.PROPERTY_PHASE, "Current lifecycle phase of this project" },
                    { Project.PROPERTY_REGISTERED, "Creation date of this project" } });

    private static final int PROJECTID_MIN_LENGHTH = 3;
    private static final int PROJECTID_MAX_LENGHTH = 64;
    private static final String PROJECTID_REGEX = "[a-z][a-z0-9_\\-.]*"; //$NON-NLS-1$

    private static final int NAME_MIN_LENGHTH = 3;
    private static final int NAME_MAX_LENGHTH = 255;

    private static final int SHORT_NAME_MIN_LENGHTH = 2;
    private static final int SHORT_NAME_MAX_LENGHTH = 10;
    private static final String SHORT_NAME_REGEX = "[a-zA-Z0-9]*"; //$NON-NLS-1$

    @Override
    public Class<Project> getExtensionClass() {
        return Project.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated core model"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated core model"); //$NON-NLS-1$
    }

    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = new HashSet<DataMigration>();
        migrations.add(new DataMigration0());
        migrations.add(new DataMigration2());
        migrations.add(new DataMigration3());
        migrations.add(new DataMigration5());
        migrations.add(new DataMigration8());
        migrations.add(new DataMigration9());
        migrations.add(new DataMigration10());
        migrations.add(new DataMigration11());
        migrations.add(new DataMigration12());
        migrations.add(new DataMigration13());
        migrations.add(new DataMigration14());
        migrations.add(new DataMigration15());
        migrations.add(new DataMigration16());
        migrations.add(new DataMigration19());
        return migrations;
    }

    @Override
    public String getShortName() {
        return "project"; //$NON-NLS-1$
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
        return Project.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return Project.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "project.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<Project> getIndexer() {
        return new ProjectIndexer();
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
    public List<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        caption = getCaption(propertyName, caption);
        List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
        if (Project.PROPERTY_PROJECTID.equals(propertyName)) {
            validators.add(new StringLengthValidator(Severity.FATAL, getExtensionClass(), propertyName, caption,
                    PROJECTID_MIN_LENGHTH, PROJECTID_MAX_LENGHTH));
            validators.add(new RegularExpressionValidator(Severity.FATAL, getExtensionClass(), propertyName, caption,
                    PROJECTID_REGEX));
        }
        else if (Project.PROPERTY_NAME.equals(propertyName)) {
            validators.add(new StringLengthValidator(Severity.FATAL, getExtensionClass(), propertyName, caption,
                    NAME_MIN_LENGHTH, NAME_MAX_LENGHTH));
        }
        else if (Project.PROPERTY_SHORT_NAME.equals(propertyName)) {
            validators.add(new StringLengthValidator(Severity.FATAL, getExtensionClass(), propertyName, caption,
                    SHORT_NAME_MIN_LENGHTH, SHORT_NAME_MAX_LENGHTH));
            validators.add(new RegularExpressionValidator(Severity.FATAL, getExtensionClass(), propertyName,
                    MessageFormat.format("{0} must contain letters and digits only", caption),
                    null, SHORT_NAME_REGEX));
        }
        return validators;
    }
}
