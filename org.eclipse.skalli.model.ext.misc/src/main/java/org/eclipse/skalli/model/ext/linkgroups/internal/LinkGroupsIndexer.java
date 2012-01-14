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
package org.eclipse.skalli.model.ext.linkgroups.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.services.extension.IndexerBase;

public class LinkGroupsIndexer extends IndexerBase<LinkGroupsProjectExt> {

    @Override
    protected void indexFields(LinkGroupsProjectExt linkGroupsExt) {
    }

    @Override
    public Set<String> getDefaultSearchFields() {
        Set<String> ret = new HashSet<String>();
        return ret;
    }

}
