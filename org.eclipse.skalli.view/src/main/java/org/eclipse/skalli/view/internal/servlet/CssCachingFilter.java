/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.Services;

/**
 * <strong>Temporary</strong> solution to avoid caching of CSS files.
 *
 * Note that this sends a redirect to the client so that it doubles the number of requests.
 * However for internal usage the solution is &quot;good enough&quot; and appending a query
 * string with the version should be done in the HTML tag that includes the stylesheet as
 * soon as the UI gets an overhaul.
 *
 * TODO Remove as soon as views are reworked
 */
public class CssCachingFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    @SuppressWarnings("nls")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String queryString = httpServletRequest.getQueryString();
        if (StringUtils.isBlank(queryString)) {
            httpServletResponse.sendRedirect(httpServletRequest.getRequestURI() + "?" + Services.API_VERSION);
        } else {
            chain.doFilter(request, response);
        }
    }

}
