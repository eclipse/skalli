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
package org.eclipse.skalli.services.extension;

import java.util.List;
import java.util.Set;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.search.IndexEntry;

public interface Indexer<T extends EntityBase> {

    public Set<String> getDefaultSearchFields();
    public void indexEntity(List<IndexEntry> fields, Object entity);

}
