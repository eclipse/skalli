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
package org.eclipse.skalli.ext.mapping.scm;

import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.commons.LinkMapping;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.LinkMapper;

/**
 * Utility that maps a source location, to a list of {@link Link links} by
 * comparing it to given regular expressions and generating links from associated templates that
 * may contain placeholders.
 */
public class ScmLocationMapper extends LinkMapper<ScmLocationMapping> {

    /** Purpose for mappings that allow browsing content of source locations. */
    public static final String PURPOSE_BROWSE = "browse"; //$NON-NLS-1$

    /** Purpose for mappings that allow reviewing content of source locations. */
    public static final String PURPOSE_REVIEW = "review"; //$NON-NLS-1$

    /**
     * Purpose for mappings that determines how the source location should be transformed
     * before copying it to the clipboard.
     */
    public static final String PURPOSE_COPY_TO_CLIPBOARD = "copy_to_clipboard"; //$NON-NLS-1$

    /** Purpose for mappings used for resolving of Maven POMs */
    public static final String MAVEN_RESOLVER = "maven-resolver"; //$NON-NLS-1$

    /** Purpose for mappings that provide activity information for a project. */
    public static final String PURPOSE_ACTIVITY = "activity"; //$NON-NLS-1$

    /** Purpose for mappings that provide activity details for a project. */
    public static final String PURPOSE_ACTIVITY_DETAILS = "activitydetails"; //$NON-NLS-1$

    /** Purpose for mappings that provide the "Create Bug" url for the bug tracking system. */
    public static final String PURPOSE_CREATE_BUG = "create_bug"; //$NON-NLS-1$

    /** Purpose for mappings that provide feeds for source locations. */
    public static final String PURPOSE_FEED = "feed"; //$NON-NLS-1$

    /** Purpose for links in feed entries. */
    public static final String PURPOSE_FEED_LINK = "feed-link"; //$NON-NLS-1$

    /** Purpose for indexing of repositories. **/
    public static final String PURPOSE_INDEXING = "indexing"; //$NON-NLS-1$

    /** Purpose for mappings that provide destinations, e.g. to gitweb. */
    public static final String DESTINATION = "destination"; //$NON-NLS-1$

    /** Accepts any provider (see {@link #getScmLocationMappings(List, String, String...)}) */
    public static final String ALL_PROVIDERS = "*"; //$NON-NLS-1$

    protected final String provider;

    /**
     * Creates a source location mapper for a given provider and a collection of purposes.
     *
     * @param provider  the provider to accept when evaluating source location mappings.
     * A mapping that doesn't match the given provider is skipped.
     * {@link #ALL_PROVIDERS}, <code>"*"</code> and <code>null</code> match any provider.
     * @param purposes  the purposes to accept when evaluating source location mappings.
     * A mapping that doesn't match any of the given purposes is skipped.
     * {@link LinkMapper#ALL_PURPOSE}, <code>"*"</code> and <code>null</code> match any purpose.
     * An empty array excludes all purposes.
     */
    public ScmLocationMapper(String provider, String... purposes) {
        super(purposes);
        this.provider = provider != null? provider : ALL_PROVIDERS;
    }

    /**
     * Returns all configured {@link ScmLocationMappings source location mappings}.
     *
     * @param configService  the configuration service that provides the source location mappings.
     *
     * @return  the configured source location mappings, or an empty list.
     */
    public static List<ScmLocationMapping> getAllMappings(ConfigurationService configService) {
        if (configService != null) {
            ScmLocationMappings mappingsConfig = configService.readConfiguration(ScmLocationMappings.class);
            if (mappingsConfig != null && CollectionUtils.isNotBlank(mappingsConfig.getScmMappings())) {
                return mappingsConfig.getScmMappings();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns configured {@link ScmLocationMappings source location mappings} that match
     * the provider and any of the purposes of this souce location mapper.
     *
     * @param configService  the configuration service that provides the source location mappings.
     *
     * @return  the configured source location mappings, or an empty list.
     */
    public List<ScmLocationMapping> getMappings(ConfigurationService configService) {
        return filter(getAllMappings(configService));
    }

    /**
     * Maps a given string to a collection of links by matching the string with a given collection of
     * {@link LinkMapping#getPattern() regular expressions} and, in case of a successful match,
     * by converting the string into a link based on a {@link LinkMapping#getTemplate() template}
     * associated with the regular expression. The template may contain placeholders that are
     * resolved during the link generation. The regular expressions and templates are derived
     * from the configured {@link ScmLocationMappings source location mappings}.
     *
     * @param s  the string to match to configured source location mappings and to
     * convert to links in case of successful matches.
     * @param userId  the unique identifier of a user.
     * @param entity  any (probably extensible) entity, usually a project.
     * @param configService  the configuration service that provides the source location mappings.
     *
     * @return  a list of links, or an empty list.
     */
    public List<Link> getMappedLinks(String s, String userId, EntityBase entity, ConfigurationService configService) {
        return getMappedLinks(s, getMappings(configService), userId, entity);
    }

    /**
     * Returns <code>true</code> if the given mapping should be taken into account for
     * the link generation. This implementation checks the provider and purpose of the mapping.
     *
     * @param mapping  the mapping to check.
     * @return <code>true</code>, if the mapping is accepted.
     */
    @Override
    protected boolean accept(ScmLocationMapping mapping) {
        return super.accept(mapping)
                && (ALL_PROVIDERS.equals(provider) || ComparatorUtils.equals(provider, mapping.getProvider()));
    }
}
