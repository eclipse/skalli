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
package org.eclipse.skalli.model.ext.maven.internal;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceMavenReactor
        extends ExtensionServiceBase<MavenReactorProjectExt>
        implements ExtensionService<MavenReactorProjectExt>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceMavenReactor.class);

    private static final String CAPTION = "Maven Reactor";
    private static final String DESCRIPTION = "Information related to a Maven reactor project and its modules";

    @Override
    public Class<MavenReactorProjectExt> getExtensionClass() {
        return MavenReactorProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "mavenReactor"; //$NON-NLS-1$
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
    public RestConverter<?> getRestConverter(String host) {
        return new MavenReactorConverter(host);
    }

    @Override
    public RestConverter<MavenReactorProjectExt> getRestConverter() {
        return new MavenReactorConverter();
    }

    @Override
    public String getModelVersion() {
        return MavenReactorProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return MavenReactorProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-maven-reactor.xsd"; //$NON-NLS-1$
    }

    @Override
    public Indexer<MavenReactorProjectExt> getIndexer() {
        return new MavenReactorIndexer();
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.model.ext.ExtensionServiceBase#getMigrations()
     */
    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = new TreeSet<DataMigration>();
        migrations.add(new DataMigrationMavenModule());
        return migrations;
    }
}
