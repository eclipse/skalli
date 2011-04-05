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
package org.eclipse.skalli.core.internal.search;

import org.eclipse.skalli.model.ext.AbstractIndexer;

class TestEntityIndexer extends AbstractIndexer<TestEntity> {

  @Override
  protected void indexFields(TestEntity entity) {
    addField(LuceneIndexTest.FIELD, entity.getValue(), true, true);
    addField(LuceneIndexTest.FACET, entity.getFacet(), true, true);
  }

}

