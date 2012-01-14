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
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.view.Consts;

public class StatisticsFilter implements Filter {

    private static final String ANONYMOUS = "anonymous"; //$NON-NLS-1$

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        // browser statistics
        User user = (User) request.getAttribute(Consts.ATTRIBUTE_USER);
        String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);
        String department = StatisticsFilter.ANONYMOUS;
        String location = StatisticsFilter.ANONYMOUS;
        if (StringUtils.isBlank(userId)) {
            userId = StatisticsFilter.ANONYMOUS;
        } else if (user != null) {
            department = user.getDepartment();
            location = user.getLocation();
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String browser = httpRequest.getHeader("User-Agent"); //$NON-NLS-1$
        Statistics.getDefault().trackBrowser(userId, browser, department, location);

        Statistics.getDefault().trackUsage(httpRequest.getRequestURI());

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
