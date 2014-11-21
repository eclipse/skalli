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
package org.eclipse.skalli.view.internal.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.config.BrandingConfig;
import org.eclipse.skalli.view.internal.config.FeedbackConfig;
import org.eclipse.skalli.view.internal.config.NewsConfig;
import org.eclipse.skalli.view.internal.config.TopLinksConfig;
import org.eclipse.skalli.view.internal.config.UserDetailsConfig;

public class ConfigFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        request.setAttribute(Consts.ATTRIBUTE_PAGETITLE, Consts.DEFAULT_PAGETITLE);
        request.setAttribute(Consts.ATTRIBUTE_SEARCH_PLUGIN_TITLE, Consts.DEFAULT_PAGETITLE);
        request.setAttribute(Consts.ATTRIBUTE_SEARCH_PLUGIN_DESCRIPTION, Consts.DEFAULT_SEARCH_PLUGIN_DESCRIPTION);

        ConfigurationService confService = Services.getService(ConfigurationService.class);
        if (confService != null) {
            FeedbackConfig feedbackConfig = confService.readConfiguration(FeedbackConfig.class);
            if (feedbackConfig != null) {
                request.setAttribute(Consts.ATTRIBUTE_FEEDBACKCONFIG, feedbackConfig);
            }
            TopLinksConfig toplinksConfig = confService.readConfiguration(TopLinksConfig.class);
            if (toplinksConfig != null) {
                request.setAttribute(Consts.ATTRIBUTE_TOPLINKSCONFIG, toplinksConfig);
            }
            NewsConfig newsConfig = confService.readConfiguration(NewsConfig.class);
            if (newsConfig != null) {
                request.setAttribute(Consts.ATTRIBUTE_NEWSCONFIG, newsConfig);
            }
            BrandingConfig brandingConfig = confService.readConfiguration(BrandingConfig.class);
            if (brandingConfig != null) {
                request.setAttribute(Consts.ATTRIBUTE_BRANDINGCONFIG, brandingConfig);
                if (StringUtils.isNotBlank(brandingConfig.getPageTitle())) {
                    request.setAttribute(Consts.ATTRIBUTE_PAGETITLE, brandingConfig.getPageTitle());
                }
                if (StringUtils.isNotBlank(brandingConfig.getSearchPluginTitle())) {
                    request.setAttribute(Consts.ATTRIBUTE_SEARCH_PLUGIN_TITLE, brandingConfig.getSearchPluginTitle());
                }
                if (StringUtils.isNotBlank(brandingConfig.getSearchPluginDescription())) {
                    request.setAttribute(Consts.ATTRIBUTE_SEARCH_PLUGIN_DESCRIPTION, brandingConfig.getSearchPluginDescription());
                }
            }
            UserDetailsConfig userDetailsConfig = confService.readConfiguration(UserDetailsConfig.class);
            if (userDetailsConfig != null) {
                request.setAttribute(Consts.ATTRIBUTE_USERDETAILSCONFIG, userDetailsConfig);
            }
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
