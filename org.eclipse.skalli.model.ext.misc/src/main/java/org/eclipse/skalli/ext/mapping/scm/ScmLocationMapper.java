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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.ext.mapping.LinkMapperBase;
import org.eclipse.skalli.ext.mapping.LinkMappingConfig;
import org.eclipse.skalli.model.ext.devinf.internal.config.ScmLocationMappingResource;
import org.eclipse.skalli.services.configuration.ConfigurationService;

public class ScmLocationMapper extends LinkMapperBase {

    /** Purpose filter for mappings that allow to browse content of source locations. */
    public static final String PURPOSE_BROWSE = "browse"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to review content of source locations. */
    public static final String PURPOSE_REVIEW = "review"; //$NON-NLS-1$

    /**
     * Purpose filter for mappings that determines how the source location should be transformed
     * before copying it to the clipboard.
     */
    public static final String PURPOSE_COPY_TO_CLIPBOARD = "copy_to_clipboard"; //$NON-NLS-1$

    /** Purpose filter for mappings used for resolving of Maven POMs */
    public static final String MAVEN_RESOLVER = "maven-resolver"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide activity information for a project. */
    public static final String PURPOSE_ACTIVITY = "activity"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide activity details for a project. */
    public static final String PURPOSE_ACTIVITY_DETAILS = "activitydetails"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide the "Create Bug" url for the bug tracking system. */
    public static final String PURPOSE_CREATE_BUG = "create_bug"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide feeds for source locations. */
    public static final String PURPOSE_FEED = "feed"; //$NON-NLS-1$

    /** Purpose filter for links in feed entries. */
    public static final String PURPOSE_FEED_LINK = "feed-link"; //$NON-NLS-1$

    /** Purpose filter for indexing of repositories **/
    public static final String PURPOSE_INDEXING = "indexing"; //$NON-NLS-1$

    /** Purpose filter for mappings that provide destinations, e.g. to gitweb. */
    public static final String DESTINATION = "destination"; //$NON-NLS-1$

    public static final String ALL_PROVIDERS = "*"; //$NON-NLS-1$

    public List<ScmLocationMappingConfig> getMappings(ConfigurationService configService, String provider,
            String... purposes) {
        List<ScmLocationMappingConfig> mappings = new ArrayList<ScmLocationMappingConfig>();
        if (configService != null) {
            List<? extends LinkMappingConfig> allMappings = getAllMappings(configService);
            if (allMappings != null) {
                for (LinkMappingConfig mapping : allMappings) {
                    if (matches((ScmLocationMappingConfig) mapping, provider, purposes)) {
                        mappings.add((ScmLocationMappingConfig) mapping);
                    }
                }
            }
        }
        return mappings;
    }

    private boolean matches(ScmLocationMappingConfig mapping, String provider, String... purposes) {
        if (provider == null) {
            provider = ALL_PROVIDERS;
        }
        if (!ALL_PROVIDERS.equals(provider) && !ComparatorUtils.equals(provider, mapping.getProvider())) {
            return false;
        }
        return super.matches(mapping, purposes);
    }

    @Override
    protected List<? extends LinkMappingConfig> getAllMappings(ConfigurationService configService) {
        ScmLocationMappingsConfig mappingsConfig =
                configService.readCustomization(ScmLocationMappingResource.MAPPINGS_KEY,
                        ScmLocationMappingsConfig.class);
        return mappingsConfig != null ? mappingsConfig.getScmMappings() : null;
    }
}
