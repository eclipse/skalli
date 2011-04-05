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
package org.eclipse.skalli.view.internal.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.model.core.Project;

public class ProjectFilter implements Filter {

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String pathInfo = httpRequest.getPathInfo();
    String paramProjectId = request.getParameter(Consts.PARAM_ID);

    ProjectService projectService = Services.getService(ProjectService.class);

    Project project = null;

    // first check if project is provided in pathInfo
    if (StringUtils.isNotBlank(pathInfo)) {
      if (pathInfo.startsWith(FilterUtil.PATH_SEPARATOR)) {
        pathInfo = pathInfo.replaceFirst(FilterUtil.PATH_SEPARATOR, StringUtils.EMPTY);
      }
      if (pathInfo.contains(FilterUtil.PATH_SEPARATOR)) {
        pathInfo = pathInfo.substring(0, pathInfo.indexOf(FilterUtil.PATH_SEPARATOR));
      }
      project = projectService.getProjectByProjectId(pathInfo);

      // project not found by name, search by UUID
      if (project == null && UUIDUtils.isUUID(pathInfo)) {
        project = projectService.getByUUID(UUIDUtils.asUUID(pathInfo));
      }
      // project not found by UUID, search for deleted project
      if (project == null && UUIDUtils.isUUID(pathInfo)) {
        project = projectService.getDeletedProject(UUIDUtils.asUUID(pathInfo));
      }

      if (project == null) {
        // refactored, don't know if we still need this???
        request.setAttribute(Consts.ATTRIBUTE_PATHINFO, httpRequest.getPathInfo());
      }
    }

    // check if project is provided via URL parameter
    if (StringUtils.isNotBlank(paramProjectId)) {
      project = Services.getRequiredService(ProjectService.class).getProjectByProjectId(paramProjectId);
      if (project == null) {
        // currently we don't support a scenario where projects are passed via UUID
        FilterUtil.handleException(
            request,
            response,
            new FilterException(String.format("Project id '%s' set in url parameter '%s' not valid.", paramProjectId,
                Consts.PARAM_ID)));
      }
    }

    if (project != null) {
      request.setAttribute(Consts.ATTRIBUTE_PROJECT, project);
      request.setAttribute(Consts.ATTRIBUTE_PROJECTID, project.getProjectId());
    } //else {
      // do nothing if project is null as this filter supports
      // creation of projects and search result also
      // }

    // proceed along the chain
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }
}

