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
package org.eclipse.skalli.api.rest.internal.admin;

import org.osgi.framework.Bundle;

import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.ext.AbstractConverter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class StatusConverter extends AbstractConverter<Object> {

  public static final String API_VERSION = "1.0";
  public static final String NAMESPACE = "http://xml.sap.com/2010/08/ProjectPortal/API/Admin";

  public StatusConverter(String host) {
    super(Object.class, "status", host); //$NON-NLS-1$
  }

  private String getBundleState(int state) {
    switch(state) {
    case Bundle.ACTIVE:
      return "Active"; //$NON-NLS-1$
    case Bundle.INSTALLED:
      return "Installed"; //$NON-NLS-1$
    case Bundle.UNINSTALLED:
      return "Uninstalled"; //$NON-NLS-1$
    case Bundle.STARTING:
      return "Starting"; //$NON-NLS-1$
    case Bundle.STOPPING:
      return "Stopping"; //$NON-NLS-1$
    case Bundle.RESOLVED:
      return "Resolved"; //$NON-NLS-1$
    default:
      return "(unknown)"; //$NON-NLS-1$
    }
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    writer.startNode("bundles"); //$NON-NLS-1$
    for (Bundle bundle : Services.getBundles()) {
      writer.startNode("bundle"); //$NON-NLS-1$
      writeNode(writer, "name", bundle.getSymbolicName()); //$NON-NLS-1$
      writeNode(writer, "version", bundle.getVersion().toString()); //$NON-NLS-1$
      writeNode(writer, "state", getBundleState(bundle.getState())); //$NON-NLS-1$
      writer.endNode();
    }
    writer.endNode();
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
    return "admin-status.xsd";
  }
}

