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
package org.eclipse.skalli.api.rest.internal.resources;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.core.PeopleProvider;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

class ProjectConverter extends CommonProjectConverter {

  public static final String API_VERSION = "1.4"; //$NON-NLS-1$
  public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

  public ProjectConverter(String host, boolean omitNSAttributes) {
    super(host, omitNSAttributes);
  }

  public ProjectConverter(String host, String[] extensions, boolean omitNSAttributes) {
    super(host, extensions, omitNSAttributes);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Project project = iterateProjectNodes(null, reader, context);
    return project;
  }

  private Project iterateProjectNodes(Project project, HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    if (project == null) {
      project = new Project();
    }
    while (reader.hasMoreChildren()) {
      reader.moveDown();

      String field = reader.getNodeName();
      String value = reader.getValue();

      if ("members".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        iterateProjectNodes(project, reader, context);
      } else if ("extensions".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        Set<ExtensionEntityBase> extensions = iterateExtensions(reader, context);
        for (ExtensionEntityBase extension : extensions) {
          project.addExtension(extension);
        }
      } else if ("id".equals(field)) { //$NON-NLS-1$
        project.setProjectId(value);
      } else if ("name".equals(field)) { //$NON-NLS-1$
        project.setName(value);
      } else if ("uuid".equals(field)) { //$NON-NLS-1$
        project.setUuid(UUID.fromString(value));
      } else if ("description".equals(field)) { //$NON-NLS-1$
        project.setDescription(value);
      } else if ("member".equals(field)) { //$NON-NLS-1$
        iterateMemberNodes(project, reader);
      }
      reader.moveUp();
    }
    return project;
  }

  private void iterateMemberNodes(Project project, HierarchicalStreamReader reader) {
    ProjectMember member = new ProjectMember(null);
    while (reader.hasMoreChildren()) {
      reader.moveDown();

      String field = reader.getNodeName();
      String value = reader.getValue();

      if ("userId".equals(field)) { //$NON-NLS-1$
        member.setUserID(value);
      } else if ("role".equals(field)) { //$NON-NLS-1$
        for (PeopleProvider pp : Services.getServices(PeopleProvider.class)) {
          pp.addPerson(project, value, member);
        }
      }
      reader.moveUp();
    }
  }

  @SuppressWarnings("rawtypes")
  private Set<ExtensionEntityBase> iterateExtensions(HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    Set<ExtensionEntityBase> extensions = new TreeSet<ExtensionEntityBase>();

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      String field = reader.getNodeName();
      Iterator<ExtensionService> extensionServices = Services.getServiceIterator(ExtensionService.class);
      while (extensionServices.hasNext()) {
        ExtensionService extensionService = extensionServices.next();
        if (extensionService.getExtensionClass().equals(Project.class)) {
          continue;
        }
        if (extensionService.getShortName().equals(field)) {
          Converter converter = extensionService.getConverter(getHost());
          ExtensionEntityBase extension = (ExtensionEntityBase) context.convertAnother(null,
              extensionService.getExtensionClass(), converter);
          extensions.add(extension);
        }
      }
      reader.moveUp();
    }

    return extensions;
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
    return "project.xsd"; //$NON-NLS-1$
  }
}

