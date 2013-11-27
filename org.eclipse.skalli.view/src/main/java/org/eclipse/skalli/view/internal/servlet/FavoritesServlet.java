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
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.favorites.Favorites;
import org.eclipse.skalli.services.favorites.FavoritesService;
import org.eclipse.skalli.view.LoginUtils;

public class FavoritesServlet extends HttpServlet {

    private static final long serialVersionUID = 6099308935155360539L;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/favorites")) {
            String query = request.getQueryString();
            if (query != null) {
                String[] params = StringUtils.split(query, '&');
                String action = null;
                UUID project = null;
                for (String param : params) {
                    if (param.startsWith("action=")) {
                        action = param.substring(7);
                    }
                    else if (param.startsWith("project=")) {
                        try {
                            project = UUID.fromString(param.substring(8));
                        } catch (IllegalArgumentException ex) {
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST, param + " is not a UUID");
                            return;
                        }
                    }
                }
                if (action != null && project != null) {
                    LoginUtils login = new LoginUtils(request);
                    String userId = login.getLoggedInUserId();
                    FavoritesService favService = Services.getService(FavoritesService.class);
                    boolean isFavorite = false;
                    try {
                        if (favService != null) {
                            if ("toggle".equals(action)) {
                                Favorites favorites = favService.getFavorites(userId);
                                if (favorites.hasProject(project)) {
                                    favService.removeFavorite(userId, project);
                                    isFavorite = false;
                                } else {
                                    favService.addFavorite(userId, project);
                                    isFavorite = true;
                                }
                            }
                            else if ("add".equals(action)) {
                                favService.addFavorite(userId, project);
                                isFavorite = true;
                            }
                            else if ("remove".equals(action)) {
                                favService.removeFavorite(userId, project);
                                isFavorite = false;
                            }
                        }
                    } catch (ValidationException e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                "Validation of favorites failed for user " + userId);
                    }
                    response.setContentType("text/xml");
                    response.setHeader("Cache-Control", "no-cache");
                    response.getWriter().write(project.toString());
                    response.getWriter().write("&");
                    response.getWriter().write(Boolean.toString(isFavorite));
                    response.getWriter().close();
                    return;
                }
                else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters: action, project");
                    return;
                }
            }
        }
    }

}
