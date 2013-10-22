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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.URLUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.feed.FeedProvider;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.eclipse.skalli.services.feed.URLFeedUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsFeedProvider implements FeedProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsFeedProvider.class);

    private static final String JENKINS_RSS_ALL = "/rssAll"; //$NON-NLS-1$

    @Override
    public List<FeedUpdater> getFeedUpdaters(Project project) {
        List<FeedUpdater> result = new ArrayList<FeedUpdater>();

        DevInfProjectExt ext = project.getExtension(DevInfProjectExt.class);
        if (ext != null) {
            String ciServer = ext.getCiUrl();
            if (StringUtils.isNotBlank(ciServer)) {
                String url = null;
                try {
                    url = StringUtils.removeEnd(ciServer, "/") + JENKINS_RSS_ALL; //$NON-NLS-1$
                    URLFeedUpdater feedUpdater = new URLFeedUpdater(URLUtils.stringToURL(url),
                            "jenkins", "Jenkins", project.getUuid()); //$NON-NLS-1$
                    result.add(feedUpdater);
                } catch (MalformedURLException e) {
                    LOG.info(MessageFormat.format("{0} is not a valid URL. Fetching Jenkins feed for {1} not possible.",
                            url, project.getName()));
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("No CI URL defined. Fetching Jenkins feed for {0} not possible.",
                            project.getName()));
                }
            }
        }
        return result;
    }
}
