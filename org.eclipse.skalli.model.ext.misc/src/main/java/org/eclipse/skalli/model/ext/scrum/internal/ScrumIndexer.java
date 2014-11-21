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

import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.services.extension.IndexerBase;

public class ScrumIndexer extends IndexerBase<ScrumProjectExt> {

    @Override
    protected void indexFields(ScrumProjectExt scrum) {
        addField(ScrumProjectExt.PROPERTY_BACKLOG_URL, scrum.getBacklogUrl(), true, false);
    }

}
