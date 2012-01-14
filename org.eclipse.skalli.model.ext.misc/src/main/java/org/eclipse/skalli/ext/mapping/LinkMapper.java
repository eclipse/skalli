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
package org.eclipse.skalli.ext.mapping;

import java.util.List;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.services.configuration.ConfigurationService;

/**
 * Interface for an utility that maps input strings (e.g. source locations)
 * to {@link Link links} for rendering based on a configuration entry.
 * For example, an implementation could calculate links to a web frontend
 * for mailboxes out of given mail addresses.
 */
public interface LinkMapper {

    public static final String[] ALL_PURPOSES = new String[] { "*" }; //$NON-NLS-1$

    /**
     * Maps a given string to a list of links. The mappings are provided by the
     * {@link ConfigurationService}. The <code>purposes</code> arguments serve as filter for
     * mappings retrieved from the configuration service: only mappings that match the named
     * purposes should be applied to the given string.
     *
     * @param configService  the configuration service that provides the mappings.
     * @param projectId  the identifier of a the project.
     * @param source the string to map.
     * @param purposes  a list of purpose filters, or {@value #ALL_PURPOSES},
     * i.e. mappings for any purpose should be applied.
     * @return a list of links, or an empty list.
     */
    public List<Link> getMappedLinks(ConfigurationService configService, String projectId, String source,
            String... purposes);
}
