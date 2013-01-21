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
package org.eclipse.skalli.core.extension.info;

import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.services.extension.IndexerBase;

public class InfoIndexer extends IndexerBase<InfoExtension> {

    @Override
    protected void indexFields(InfoExtension infoProjectExt) {
        addField(InfoExtension.PROPERTY_PAGE_URL, infoProjectExt.getPageUrl(), true, false);
    }

}
