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
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.user.UserUtils;
import org.eclipse.skalli.view.Consts;

/**
 * This filter tries to fetch the current user from servlet request and
 * sets the user ID and user istance as request attributes.
 * In general this user can be used by other filters or
 * servlets to differentiate between
 * anonymous user or authenticated user scenario.
 */
public class UserFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        PermitService permitService = Services.getRequiredService(PermitService.class);
        String userId = permitService.login((HttpServletRequest) request, null);
        if (StringUtils.isNotBlank(userId)) {
            request.setAttribute(Consts.ATTRIBUTE_USERID, userId);
            User user = UserUtils.getUser(userId);
            if (user != null) {
                request.setAttribute(Consts.ATTRIBUTE_USER, user);
            }
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
