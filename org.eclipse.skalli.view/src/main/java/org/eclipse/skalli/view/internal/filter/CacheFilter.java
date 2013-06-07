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
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.CollectionUtils;

/**
 * A filter that adds Cache-Control and Expires headers for
 * static resources, e.g. images, scripts, Vaadin theme artifacts etc.
 */
public class CacheFilter implements Filter {

    private Set<String> cachedResources;
    private long maxAge = -1L; //

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String cacheResourcesParam = filterConfig.getInitParameter("cachedResources"); //$NON-NLS-1$
        if (StringUtils.isBlank(cacheResourcesParam)) {
            throw new ServletException("Filter parameter 'cachedResources' must not be empty");
        }
        cachedResources = CollectionUtils.asSet(StringUtils.split(cacheResourcesParam, ','));
        maxAge = NumberUtils.toLong(filterConfig.getInitParameter("maxAge"), -1); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String extension = FilenameUtils.getExtension(requestURI);
        if (cachedResources.contains(extension)) {
            if (maxAge != -1L) {
                httpResponse.setDateHeader("Expires", System.currentTimeMillis() + maxAge * 1000);
            }
            httpResponse.setHeader("Cache-Control",
                    "public" + (maxAge != -1? ", maxAge=" + Long.toString(maxAge) : "") + ", must-revalidate");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
