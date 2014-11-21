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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.extension.IndexerBase;

public class DevInfIndexer extends IndexerBase<DevInfProjectExt> {

    private static final String PROPERTY_REPOSITORY = "repository"; //$NON-NLS-1$

    private List<Pattern> indexPatterns;

    public DevInfIndexer(List<Pattern> indexPatterns) {
        this.indexPatterns = indexPatterns;
    }

    @Override
    protected void indexFields(DevInfProjectExt devInf) {
        addField(DevInfProjectExt.PROPERTY_SCM_URL, devInf.getScmUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_BUGTRACKER_URL, devInf.getBugtrackerUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_CI_URL, devInf.getCiUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_METRICS_URL, devInf.getMetricsUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_JAVADOCS_URL, devInf.getJavadocs(), true, false);
        indexScmLocations(devInf.getScmLocations());
    }

    private void indexScmLocations(Set<String> scmLocations) {
        if (scmLocations == null || scmLocations.isEmpty()) {
            return;
        }
        addField(DevInfProjectExt.PROPERTY_SCM_LOCATIONS, scmLocations, true, true);
        for (String scmLocation: scmLocations) {
            for (Pattern indexPattern: indexPatterns) {
                Matcher matcher = indexPattern.matcher(scmLocation);
                if (matcher.matches()) {
                    for (int i = 0; i < matcher.groupCount(); ++i) {
                        addField(PROPERTY_REPOSITORY, matcher.group(i + 1), true, true);
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getDefaultSearchFields() {
        Set<String> ret = new HashSet<String>();
        ret.add(DevInfProjectExt.PROPERTY_BUGTRACKER_URL);
        ret.add(DevInfProjectExt.PROPERTY_CI_URL);
        ret.add(DevInfProjectExt.PROPERTY_METRICS_URL);
        ret.add(DevInfProjectExt.PROPERTY_SCM_LOCATIONS);
        ret.add(DevInfProjectExt.PROPERTY_JAVADOCS_URL);
        ret.add(PROPERTY_REPOSITORY);
        return ret;
    }
}
