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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.extension.IndexerBase;

public class MavenReactorIndexer extends IndexerBase<MavenReactorProjectExt> {

    private static final String MAVEN_PREFIX = "maven.";
    private static final String MAVEN_MODULE_PREFIX = "maven.module.";

    private static final String MAVEN_GROUPID = "groupId";
    private static final String MAVEN_ARTIFACTID = "artifactId";
    private static final String MAVEN_PACKAGING = "packaging";

    private final boolean doStore = false;
    private final boolean doIndexed = true;

    @Override
    public Set<String> getDefaultSearchFields() {
        Set<String> ret = new HashSet<String>();

        ret.add(MAVEN_PREFIX + MAVEN_GROUPID);
        ret.add(MAVEN_PREFIX + MAVEN_ARTIFACTID);
        ret.add(MAVEN_PREFIX + MAVEN_PACKAGING);

        ret.add(MAVEN_MODULE_PREFIX + MAVEN_GROUPID);
        ret.add(MAVEN_MODULE_PREFIX + MAVEN_ARTIFACTID);
        ret.add(MAVEN_MODULE_PREFIX + MAVEN_PACKAGING);

        return ret;
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.model.ext.AbstractIndexer#indexFields(org.eclipse.skalli.model.ext.EntityBase)
     */
    @Override
    protected void indexFields(MavenReactorProjectExt mavenReactorProjectExt) {
        indexMavenReactor(mavenReactorProjectExt.getMavenReactor());
    }

    /**
     * @param mavenReactor
     */
    private void indexMavenReactor(MavenReactor mavenReactor) {
        if (mavenReactor != null) {
            indexCoordinates(mavenReactor.getCoordinate(), MAVEN_PREFIX);
            indexModuleCoodinates(mavenReactor.getModules());
        }
    }

    private void indexModuleCoodinates(TreeSet<MavenModule> mavenCoordinates) {
        if (mavenCoordinates == null) {
            return;
        }

        for (MavenCoordinate mavenCoordinate : mavenCoordinates) {
            indexCoordinates(mavenCoordinate, MAVEN_MODULE_PREFIX);
        }
    }

    private void indexCoordinates(MavenCoordinate coordinate, String filedNamePrefix) {
        if (coordinate == null) {
            return;
        }

        if (StringUtils.isNotBlank(coordinate.getGroupId())) {
            addField(filedNamePrefix + MAVEN_GROUPID, coordinate.getGroupId(), doStore, doIndexed);
        }

        if (StringUtils.isNotBlank(coordinate.getArtefactId())) {
            addField(filedNamePrefix + MAVEN_ARTIFACTID, coordinate.getArtefactId(), doStore, doIndexed);
        }

        if (StringUtils.isNotBlank(coordinate.getPackaging())) {
            addField(filedNamePrefix + MAVEN_PACKAGING, coordinate.getPackaging(), doStore, doIndexed);
        }
    }
}
