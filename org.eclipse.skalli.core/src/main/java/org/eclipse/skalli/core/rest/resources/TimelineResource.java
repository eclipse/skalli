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
package org.eclipse.skalli.core.rest.resources;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedService;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.project.ProjectService;
import org.jsoup.safety.Whitelist;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class TimelineResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(TimelineResource.class);

    private static final String RSS_FORMAT = "rss"; //$NON-NLS-1$
    private static final String ATOM_FORMAT = "atom"; //$NON-NLS-1$
    private static final String DEFAULT_RSS_FORMAT = "rss_2.0"; //$NON-NLS-1$
    private static final String DEFAULT_ATOM_FORMAT = "atom_1.0"; //$NON-NLS-1$
    private static final int DEFAULT_COUNT = 10;

    public static final String PARAM_SOURCES = "sources"; //$NON-NLS-1$
    public static final String PARAM_FORMAT = "format"; //$NON-NLS-1$
    public static final String PARAM_COUNT = "count"; //$NON-NLS-1$

    @SuppressWarnings("nls")
    private static final Set<String> SUPPORTED_FORMATS = CollectionUtils.asSet(
        "atom_0.3", DEFAULT_ATOM_FORMAT, "rss_0.9", "rss_0.91N", "rss_0.91U",
        "rss_0.92", "rss_0.93", "rss_0.94", DEFAULT_RSS_FORMAT, "rss_2.0",
        "atom", "rss"
    );

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }
        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        String format = getQueryAttribute(PARAM_FORMAT);
        if (StringUtils.isBlank(format)) {
            format = DEFAULT_RSS_FORMAT;
        }
        format = format.toLowerCase(Locale.ENGLISH);
        if (!SUPPORTED_FORMATS.contains(format)) {
            format = DEFAULT_RSS_FORMAT;
        }
        if (RSS_FORMAT.equals(format)) {
            format = DEFAULT_RSS_FORMAT;
        } else if (ATOM_FORMAT.equals(format)) {
            format = DEFAULT_ATOM_FORMAT;
        }
        MediaType mediaType = format.startsWith(ATOM_FORMAT)?
                MediaType.APPLICATION_ATOM : MediaType.APPLICATION_RSS;
        int count = NumberUtils.toInt(getQueryAttribute(PARAM_COUNT), DEFAULT_COUNT);
        String[] sources = StringUtils.split(getQueryAttribute(PARAM_SOURCES), ',');

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project project = null;
        try {
            UUID uuid = UUID.fromString(id);
            project = projectService.getByUUID(uuid);
        } catch (IllegalArgumentException e) {
            project = projectService.getProjectByProjectId(id);
        }
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        FeedService feedService = Services.getRequiredService(FeedService.class);
        List<Entry> entries = null;
        try {
             entries = feedService.findEntries(project.getUuid(),
                     sources != null? CollectionUtils.asSet(sources) : null, count);
        } catch (StorageException e) {
            LOG.error(MessageFormat.format("Failed to retrieve timeline entries for {0}", project.getUuid()), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }

        final SyndFeedOutput output = new SyndFeedOutput();
        final SyndFeed feed = getFeed(project, getHost(), entries);
        feed.setFeedType(format);
        return new WriterRepresentation(mediaType) {
            @Override
            public void write(Writer writer) throws IOException {
                try {
                    output.output(feed, writer);
                } catch (FeedException e) {
                    LOG.error(MessageFormat.format("Failed to render ''{0}''", feed.getTitle()), e);
                }
            }
        };

    }

    private SyndFeed getFeed(Project project, String host, List<Entry> entries) {
        SyndFeed feed = new SyndFeedImpl();
        String projectName = HtmlUtils.clean(project.getName(), Whitelist.none());
        feed.setTitle(MessageFormat.format("{0} | Timeline", projectName));
        feed.setDescription(MessageFormat.format(
                "Latest changes to project ''{0}''.", projectName));
        feed.setLink(host + RestUtils.URL_PROJECTS + project.getProjectId() + "/timeline"); //$NON-NLS-1$
        List<SyndEntry> feedEntries = new ArrayList<SyndEntry>();
        for (Entry entry : entries) {
            SyndEntry feedEntry = new SyndEntryImpl();
            feedEntry.setTitle(MessageFormat.format("{0} | {1}", entry.getTitle(), entry.getSource()));
            feedEntry.setLink(entry.getLink().getHref());
            feedEntry.setPublishedDate(entry.getPublished());
            SyndContent entryDescription = new SyndContentImpl();
            entryDescription.setType(entry.getContent().getType());
            entryDescription.setValue(entry.getContent().getValue());
            feedEntry.setDescription(entryDescription);
            feedEntries.add(feedEntry);
        }
        feed.setEntries(feedEntries);
        return feed;
    }
}
