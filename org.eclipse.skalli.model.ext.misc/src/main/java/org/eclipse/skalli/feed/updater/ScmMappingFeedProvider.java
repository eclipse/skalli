/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.feed.updater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingConfig;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.PropertyMapper;
import org.eclipse.skalli.services.feed.FeedProvider;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScmMappingFeedProvider implements FeedProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ScmMappingFeedProvider.class);

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = null;
    }

    @Override
    public List<FeedUpdater> getFeedUpdaters(Project project) {
        List<FeedUpdater> result = new ArrayList<FeedUpdater>();

        DevInfProjectExt ext = project.getExtension(DevInfProjectExt.class);
        if (ext != null) {
            Set<String> scmLocations = ext.getScmLocations();
            ScmLocationMapper mapper = new ScmLocationMapper();
            for (String scmLocation : scmLocations) {

                //we want to have all providers
                List<ScmLocationMappingConfig> feedMappings = mapper.getMappings(configService, null,
                        ScmLocationMapper.PURPOSE_FEED);

                for (ScmLocationMappingConfig mappingConfig : feedMappings) {
                    String urlStr = PropertyMapper.convert(scmLocation, mappingConfig.getPattern(),
                            mappingConfig.getTemplate(), project, "");
                    if (StringUtils.isNotBlank(urlStr)) {
                        try {
                            URL url = new URL(urlStr);
                            SyndFeedUpdater feedUpdater = new SyndFeedUpdater(url, project.getName(),
                                    mappingConfig.getProvider(), mappingConfig.getName()); //$NON-NLS-1$
                            result.add(feedUpdater);
                        } catch (MalformedURLException e) {
                            LOG.error("The mapping of scmLocation ='" + scmLocation + "' with purpose = '"
                                    + ScmLocationMapper.PURPOSE_FEED + "' got an invalid URL = '" + urlStr + "'");
                        }
                    }
                }

                if (result.isEmpty()) {
                    LOG.debug("no mapping for scmLocation ='" + scmLocation + "' with purpose = '"
                            + ScmLocationMapper.PURPOSE_FEED + "' defined.");
                }
            }
        }
        return result;
    }
}
