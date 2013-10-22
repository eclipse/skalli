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
package org.eclipse.skalli.ext.feed.misc;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.URLUtils;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapping;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappings;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.PropertyMapper;
import org.eclipse.skalli.services.feed.FeedProvider;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.eclipse.skalli.services.feed.URLFeedUpdater;
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
            ScmLocationMapper mapper = new ScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS,
                    ScmLocationMapper.PURPOSE_FEED);
            for (String scmLocation : scmLocations) {
                List<ScmLocationMapping> feedMappings = mapper.filter(getScmLocationMappings());
                for (ScmLocationMapping mappingConfig : feedMappings) {
                    String urlStr = PropertyMapper.convert(scmLocation, mappingConfig.getPattern(),
                            mappingConfig.getTemplate(), project, ""); //$NON-NLS-1$
                    if (StringUtils.isNotBlank(urlStr)) {
                        try {
                            URLFeedUpdater feedUpdater = new URLFeedUpdater(URLUtils.stringToURL(urlStr),
                                    mappingConfig.getProvider(), mappingConfig.getName(),
                                    project.getUuid());
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

    private List<ScmLocationMapping> getScmLocationMappings() {
        ScmLocationMappings mappingsConfig = configService.readConfiguration(ScmLocationMappings.class);
        return mappingsConfig != null? mappingsConfig.getScmMappings() : null;
    }
}
