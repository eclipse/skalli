/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/

package org.eclipse.skalli.view.ext.impl.internal.infobox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.HtmlBuilder;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedManager;
import org.eclipse.skalli.services.feed.FeedProvider;
import org.eclipse.skalli.services.feed.FeedService;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.view.component.FloatLayout;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;

public class FeedInfoBox extends InfoBoxBase implements InfoBox {

    private static final String CAPTION = "Timeline"; //$NON-NLS-1$

    private static final String ICON = "res/icons/feed.png"; //$NON-NLS-1$

    private static final String STYLE_TIMELINE_INFOBOX = "infobox-timeline"; //$NON-NLS-1$
    private static final String STYLE_TIMELINE_ENTRY = "timeline-entry"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(FeedInfoBox.class);

    private FeedService feedService;
    private Set<FeedProvider> feedProviders = new HashSet<FeedProvider>();

    private static final int MAX_DISPLAY_LENGTH_TITLE = 80;
    private static final int INITAL_MAX_FEED_ENTRIES = 7;
    private int maxFeedEntries = INITAL_MAX_FEED_ENTRIES;

    private class SourceDetails {
        private boolean selected;
        private String caption;

        public SourceDetails(boolean selected, String caption) {
            this.selected = selected;
            this.caption = caption;
        }
    }

    protected void bindFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    protected void unbindFeedService(FeedService feedService) {
        this.feedService = null;
    }

    protected void bindFeedProvider(FeedProvider feedProvider) {
        feedProviders.add(feedProvider);
    }

    protected void unbindFeedProvider(FeedProvider feedProvider) {
        feedProviders.remove(feedProvider);
    }

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = null;
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public String getShortName() {
        return "timeline"; //$NON-NLS-1$
    }

    @Override
    public float getPositionWeight() {
        // some high value to have it displayed as one of the last extensions
        return 100;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String userId) {
        return feedService != null;
    }

    @Override
    public String getIconPath() {
        return ICON;
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        List<String> sources = getSources(project);
        if (sources.isEmpty()) {
            return null; // nothing to render
        }

        Layout contentLayout = new CssLayout();
        contentLayout.addStyleName(STYLE_TIMELINE_INFOBOX);
        contentLayout.setSizeFull();

        maxFeedEntries = INITAL_MAX_FEED_ENTRIES;
        HashMap<String, SourceDetails> sourceFilter = new HashMap<String, SourceDetails>();
        Map<String, String> captions = getCaptions(project, sources);
        for (String source : sources) {
            String caption = captions.get(source);
            if (StringUtils.isBlank(caption)) {
                caption = source;
            }
            sourceFilter.put(source, new SourceDetails(true, caption));
        }

        renderContentPanel(contentLayout, project, util.getLoggedInUserId(), sourceFilter);
        return contentLayout;
    }

    private void renderContentPanel(Layout layout, Project project, String userId,
            HashMap<String, SourceDetails> sourceFilter) {
        layout.removeAllComponents();
        renderSourceFilters(layout, project, userId, sourceFilter);
        renderSubscribeLink(layout, project);
        renderTimelineContent(layout, project, userId, sourceFilter);
    }

    private List<String> getSources(Project project) {
        try {
            return feedService.findSources(project.getUuid());
        } catch (StorageException e) {
            LOG.error(MessageFormat.format("Failed to retrieve feed sources for project {0}",
                    project.getProjectId()), e);
        }
        return Collections.emptyList();
    }

    private void renderSourceFilters(Layout parentLayout, Project project, String userId,
            HashMap<String, SourceDetails> sourceFilter) {
        FloatLayout grid = new FloatLayout();
        Set<String> keys = sourceFilter.keySet();
        for (String source : keys) {
            addSourceFilter(parentLayout, grid, source, sourceFilter, project, userId);
        }
        parentLayout.addComponent(grid);
    }

    private void renderSubscribeLink(Layout parentLayout, Project project) {
        createLink(parentLayout, "Subscribe to Feed",
                MessageFormat.format("/api/projects/{0}/timeline", //$NON-NLS-1$
                project.getProjectId()),
                DEFAULT_TARGET, STYLE_TIMELINE_ENTRY);
    }

    private Map<String, String> getCaptions(Project project, List<String> sources) {
        Map<String, String> captions = new HashMap<String, String>();
        for (FeedProvider feedProvider : feedProviders) {
            List<FeedUpdater> updaters = feedProvider.getFeedUpdaters(project);
            for (FeedUpdater updater : updaters) {
                if (sources.contains(updater.getSource())) {
                    captions.put(updater.getSource(), updater.getCaption());
                }
            }
        }
        return captions;
    }

    private void renderTimelineContent(Layout layout, Project project, String userId,
            HashMap<String, SourceDetails> sourceFilter) {
        try {

            List<String> selectedSources = getSelecedSources(sourceFilter);
            if (!CollectionUtils.isEmpty(selectedSources)) {
                List<Entry> entries = feedService.findEntries(project.getUuid(), selectedSources,
                        maxFeedEntries + 1);
                createLabel(layout, getEntriesAsHtml(project, userId, entries,
                        Math.min(entries.size(), maxFeedEntries)).toString());
                if (entries.size() > maxFeedEntries) {
                    addMoreButton(layout, project, userId, sourceFilter);
                }
            }
        } catch (StorageException e) {
            LOG.error(MessageFormat.format("Failed to retrieve feed entries for project {0}",
                    project.getProjectId()), e);
        }
    }

    @SuppressWarnings({ "deprecation", "serial" })
    private void addMoreButton(final Layout layout, final Project project, final String userId,
            final HashMap<String, SourceDetails> sourceFilter) {
        final Button moreButton = new Button("more ...");
        moreButton.setStyle(Button.STYLE_LINK);
        moreButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                maxFeedEntries = maxFeedEntries + 30;
                renderContentPanel(layout, project, userId, sourceFilter);
            }
        });
        layout.addComponent(moreButton);
    }

    private HtmlBuilder getEntriesAsHtml(Project project, String userId, List<Entry> entries, int maxCount) {
        HtmlBuilder html = new HtmlBuilder();
        for (int i = 0; i < maxCount; i++) {
            addEntry(project, userId, html, entries.get(i));
        }
        return html;
    }

    private List<String> getSelecedSources(HashMap<String, SourceDetails> sourceFilter) {
        List<String> result = new ArrayList<String>();
        Set<String> sources = sourceFilter.keySet();
        for (String source : sources) {
            if (sourceFilter.get(source).selected) {
                result.add(source);
            }
        }
        return result;
    }

    private void addSourceFilter(final Layout parentLayout, FloatLayout layout, final String source,
            final HashMap<String, SourceDetails> sourceFilter, final Project project, final String userId) {
        final SourceDetails value = sourceFilter.get(source);
        CheckBox cb = new CheckBox(value.caption, value.selected);
        cb.setImmediate(true);
        cb.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 7364120771141334914L;

            @Override
            public void buttonClick(ClickEvent event) {
                boolean checked = event.getButton().booleanValue();
                value.selected = checked;
                renderContentPanel(parentLayout, project, userId, sourceFilter);
            }
        });
        layout.addComponent(cb, "margin-right:10px;"); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    private void addEntry(Project project, String userId, HtmlBuilder html, Entry entry) {
        html.append("<p class=\"").append(STYLE_TIMELINE_ENTRY).append("\">");

        String title = StringUtils.abbreviate(entry.getTitle(), MAX_DISPLAY_LENGTH_TITLE);

        String link = null;
        if (entry.getLink() != null) {
            link = entry.getLink().getHref();
        }

        link = mapLink(project, userId, link);

        html.appendLink(HtmlUtils.clean(title), link);

        html.append("<br />");

        String source = entry.getSource();
        if (StringUtils.isNotBlank(HtmlUtils.clean(source))) {
            html.append(source);
        }

        String date = getDate(entry);
        if (StringUtils.isNotBlank(date)) {
            html.append(" - ").append(HtmlUtils.clean(date));
        }

        String author = getAuthor(entry);
        if (StringUtils.isNotBlank(author)) {
            html.append(" - ").append(HtmlUtils.clean(author));
        }
        html.append("</p>");
    }

    private String mapLink(Project project, String userId, String link) {
        if (configService == null) {
            return link;
        }
        ScmLocationMapper mapper = new ScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS,
                ScmLocationMapper.PURPOSE_FEED_LINK);
        List<Link> mappedScmLinks = mapper.getMappedLinks(link, userId, project, configService);
        if (mappedScmLinks.size() == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("no mapping for feed link =''{0}'' with purpose = ''{1}'' defined.",
                        link, ScmLocationMapper.PURPOSE_FEED_LINK));
            }
            return link;
        }

        // take the first found mapping
        return mappedScmLinks.get(0).getUrl();
    }

    private String getDate(Entry entry) {
        String date = null;
        Date published = entry.getPublished();
        if (published != null) {
            date = FormatUtils.formatUTC(published.getTime());
        }
        return date;
    }

    private String getAuthor(Entry entry) {
        HtmlBuilder author = new HtmlBuilder();
        if (entry.getAuthor() != null) {
            String link = null;
            String caption = null;

            if (StringUtils.isNotBlank(entry.getAuthor().getName())) {
                caption = entry.getAuthor().getName();
            }

            if (StringUtils.isNotBlank(entry.getAuthor().getEmail())) {
                link = entry.getAuthor().getEmail();
                if (caption == null) {
                    caption = link;
                }
            }

            author.appendMailToLink(null, link, caption);
        }
        return author.toString();
    }

    @Override
    public void perform(String action, Project project, String userId) {
        if (REFRESH_ACTION.equalsIgnoreCase(action)) {
            try {
                FeedManager feedManager = Services.getService(FeedManager.class);
                if (feedManager != null) {
                    feedManager.updateFeeds(project.getUuid());
                } else {
                    LOG.error(MessageFormat.format(
                        "Failed to perform \''{0}\'' action on project \''{1}\'' for user \''{2}\'': No feed manager available",
                         action, project.getUuid(), userId));
                }
            } catch (Exception e) {
                LOG.error(MessageFormat.format("Failed to perform \''{0}\'' action on project \''{1}\'' for user \''{2}\''",
                    action, project.getUuid(), userId), e);
            }
        }
    }
}
