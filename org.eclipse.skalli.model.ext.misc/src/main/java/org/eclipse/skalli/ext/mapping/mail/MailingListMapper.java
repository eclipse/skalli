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
package org.eclipse.skalli.ext.mapping.mail;

import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.commons.LinkMapping;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.LinkMapper;

public class MailingListMapper extends LinkMapper {

    /** Purpose filter for mappings that allow to browse content of mailing lists. */
    public static final String PURPOSE_BROWSE = "browse"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to browse content of mailing list archives. */
    public static final String PURPOSE_BROWSE_ARCHIVE = "browse-archive"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to register to mailing lists. */
    public static final String PURPOSE_REGISTER = "register"; //$NON-NLS-1$

    /** Purpose filter for mappings that allow to unregister from mailing lists. */
    public static final String PURPOSE_UNREGISTER = "unregister"; //$NON-NLS-1$

    public MailingListMapper(String... purposes) {
        super(purposes);
    }

    /**
     * Returns all configured {@link MailingListMappings mailing list mappings}.
     *
     * @param configService  the configuration service that provides the mailing list mappings.
     *
     * @return  the configured mailing list mappings, or an empty list.
     */
    public static List<MailingListMapping> getAllMappings(ConfigurationService configService) {
        if (configService != null) {
            MailingListMappings mappingsConfig = configService.readConfiguration(MailingListMappings.class);
            if (mappingsConfig != null && CollectionUtils.isNotBlank(mappingsConfig.getMailingListMappings())) {
                return mappingsConfig.getMailingListMappings();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns configured {@link MailingListMappings mailing list mappings} that match
     * any of the purposes of this mailing list mapper.
     *
     * @param configService  the configuration service that provides the mailing list mappings.
     *
     * @return  the configured mailing list mappings, or an empty list.
     */
    public List<MailingListMapping> getMappings(ConfigurationService configService) {
        return filter(getAllMappings(configService));
    }

    /**
     * Maps a given string to a collection of links by matching the string with a given collection of
     * {@link LinkMapping#getPattern() regular expressions} and, in case of a successful match,
     * by converting the string into a link based on a {@link LinkMapping#getTemplate() template}
     * associated with the regular expression. The template may contain placeholders that are
     * resolved during the link generation. The regular expressions and templates are derived
     * from the configured {@link MailingListMappings mailing list mappings}.
     *
     * @param s  the string to match to configured mailing list mappings and to
     * convert to links in case of successful matches.
     * @param userId  the unique identifier of a user.
     * @param entity  any (probably extensible) entity, usually a project.
     * @param configService  the configuration service that provides the mailing list mappings.
     *
     * @return  a list of links, or an empty list.
     */
    public List<Link> getMappedLinks(String s, String userId, EntityBase entity, ConfigurationService configService) {
        return getMappedLinks(s, getAllMappings(configService), userId, entity);
    }
}
