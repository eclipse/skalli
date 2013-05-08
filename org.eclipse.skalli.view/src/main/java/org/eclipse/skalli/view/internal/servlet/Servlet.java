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
package org.eclipse.skalli.view.internal.servlet;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchService;
import org.eclipse.skalli.services.user.LoginUtils;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.config.NewsConfig;

/**
 * Servlet implementation class Servlet.
 * This servlet currently is mapped to the following paths (see web.xml):
 * <ol>
 * <li><tt>/</tt></li>
 * <li><tt>/news</tt></li>
 * <li><tt>/reindex</tt></li>
 * <li><tt>/create</tt></li>
 * </ol>
 * This servlet is <b>not</b> mapped to <tt>/projects/&lt;projectid&gt;</tt> (see
 * {@link org.eclipse.skalli.view.internal.servlet.ProjectServlet}),
 * <tt>/myprojects</tt> (see <tt>myprojects.jsp</tt>), <tt>/tags</tt> (see
 * <tt>tagcloud.jsp</tt>), theme resources (<tt>/Vaadin/*</tt>, see
 * {@link org.eclipse.skalli.view.internal.servlet.ResourceServlet})
 * and the welcome page (see <tt>welcome.jsp</tt>).
 */
public class Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Servlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        User user = new LoginUtils(request).getLoggedInUser();

        if (requestURI.endsWith(Consts.URL_NEWS)) {
            ConfigurationService confService = Services.getRequiredService(ConfigurationService.class);
            if (confService != null) {
                NewsConfig newsConfig = confService.readConfiguration(NewsConfig.class);
                if (newsConfig != null) {
                    redirect(newsConfig.getUrl(), response);
                    return;
                }
            }
            // else - do nothing, news is not configured or configuration service not available,
            // welcome page should be rendered in this case
        } else if (requestURI.endsWith(Consts.URL_REINDEX)) {
            if (GroupUtils.isAdministrator(user)) {
                reindex();
                redirect(Consts.URL_WELCOME, response);
                return;
            }
        } else if (requestURI.equals(Consts.URL_CREATEPROJECT)) {
            if (user != null) {
                forward(Consts.URL_PROJECTS + "/" + UUID.randomUUID(), request, response); //$NON-NLS-1$
                return;
            }
        } else if (requestURI.equals(Consts.URL_FAVICON)) {
            forward(Consts.FILE_FAVICON, request, response);
            return;
        } else if (requestURI.equals(Consts.URL_SEARCH_PLUGIN)) {
            forward(Consts.FILE_SEARCH_PLUGIN, request, response);
            return;
        } else if (requestURI.equals(Consts.URL_MYPROJECTS)) {
            if (user != null) {
                forward(Consts.URL_PROJECTS_USER + user.getUserId(), request, response);
                return;
            } else {
                Exception e = new AccessControlException("User is not authorized to call this page.");
                request.setAttribute("exception", e); //$NON-NLS-1$
                forward(Consts.URL_ERROR, request, response);
                return;
            }
        }
        forward(Consts.JSP_WELCOME, request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
    }

    /***********
     * private
     **********/

    private void reindex() {
        SearchService searchService = Services.getService(SearchService.class);
        ProjectService projectService = Services.getService(ProjectService.class);

        Set<Project> projects = new HashSet<Project>();
        for (Project project : projectService.getAll()) {
            projects.add(project);
        }
        searchService.reindex(projects);

    }

    private void forward(String url, HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        if (!response.isCommitted()) {
            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getRequestDispatcher(url);
            dispatcher.forward(request, response);
        } // else {
          // do nothing as response is already committed, probably forwarded to error page because of a filter exception
          // }
    }

    private void redirect(String url, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(url);
    }

}
