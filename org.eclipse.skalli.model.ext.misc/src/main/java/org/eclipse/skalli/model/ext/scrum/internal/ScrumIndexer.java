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
package org.eclipse.skalli.model.ext.scrum.internal;

import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;

public class ScrumIndexer extends AbstractIndexer<ScrumProjectExt> {

  @Override
  protected void indexFields(ScrumProjectExt scrum) {
    addField(ScrumProjectExt.PROPERTY_BACKLOG_URL, scrum.getBacklogUrl(), true, false);
  }

}

