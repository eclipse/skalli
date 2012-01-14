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
package org.eclipse.skalli.model.ext.devinf.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.extension.IndexerBase;

public class DevInfIndexer extends IndexerBase<DevInfProjectExt> {

    @Override
    protected void indexFields(DevInfProjectExt devInf) {
        addField(DevInfProjectExt.PROPERTY_BUGTRACKER_URL, devInf.getBugtrackerUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_CI_URL, devInf.getCiUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_METRICS_URL, devInf.getMetricsUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_SCM_URL, devInf.getScmUrl(), true, false);
        addField(DevInfProjectExt.PROPERTY_SCM_LOCATIONS, devInf.getScmLocations(), true, true);
        addField(DevInfProjectExt.PROPERTY_JAVADOCS_URL, devInf.getJavadocs(), true, false);
    }

    @Override
    public Set<String> getDefaultSearchFields() {
        Set<String> ret = new HashSet<String>();
        ret.add(DevInfProjectExt.PROPERTY_BUGTRACKER_URL);
        ret.add(DevInfProjectExt.PROPERTY_CI_URL);
        ret.add(DevInfProjectExt.PROPERTY_METRICS_URL);
        ret.add(DevInfProjectExt.PROPERTY_SCM_LOCATIONS);
        ret.add(DevInfProjectExt.PROPERTY_JAVADOCS_URL);
        return ret;
    }

}
