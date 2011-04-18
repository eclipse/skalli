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

import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class ScrumConverter extends AbstractConverter<ScrumProjectExt> {

  public static final String API_VERSION = "1.0"; //$NON-NLS-1$
  public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-Scrum"; //$NON-NLS-1$

  public ScrumConverter(String host) {
    super(ScrumProjectExt.class, "scrum", host);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    ScrumProjectExt ext = (ScrumProjectExt) source;
    writeNode(writer, "backlogUrl", ext.getBacklogUrl()); //$NON-NLS-1$
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
    // don't support that yet
    return null;
  }

  @Override
  public String getApiVersion() {
    return API_VERSION;
  }

  @Override
  public String getNamespace() {
    return NAMESPACE;
  }

  @Override
  public String getXsdFileName() {
    return "extension-scrum.xsd";
  }
}

