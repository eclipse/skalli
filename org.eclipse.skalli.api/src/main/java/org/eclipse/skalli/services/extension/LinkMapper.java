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
package org.eclipse.skalli.services.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;

import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.commons.LinkMapping;
import org.eclipse.skalli.model.EntityBase;

/**
 * Utility that maps an input string, e.g. a source location, to a list of {@link Link links} by
 * comparing it to given regular expressions and generating links from associated templates that
 * may contain placeholders. For example, one might generate various links for a source location,
 * such as a link to a repository viewer or a link to an associated code review system.
 */
public class LinkMapper<T extends LinkMapping> {

    protected final String[] purposes;

    /**
     * Constant to accept mappings of any purpose
     * (see {@link #getMappedLinks(String, List, String, EntityBase, String...)}).
     */
    public static final String[] ALL_PURPOSES = new String[] { "*" }; //$NON-NLS-1$

    /**
     * Creates a link mapper for a collection of purposes. The purpose of a link mapping
     * determines in which situations a certain link mapping is relevant.
     *
     * @param purposes  the purposes to accept when evaluating link mappings. A mapping that doesn't match
     * any of the given purposes is skipped. {@link #ALL_PURPOSE} and <code>"*"</code> match any purpose.
     * An empty array excludes all purposes.
     */
    public LinkMapper(String... purposes) {
        this.purposes = purposes != null? purposes : new String[] { null };
    }

    /**
     * Maps a given string to a collection of links by matching the string with a given collection of
     * {@link LinkMapping#getPattern() regular expressions} and, in case of a successful match,
     * by converting the string into a link based on a {@link LinkMapping#getTemplate() template}
     * associated with the regular expression. The template may contain placeholders that are
     * resolved during the link generation.
     * <br>
     * If specified, the <code>userId</code> parameter is mapped to the placeholder <tt>${userId}</tt>.
     * The properties of the entity, if specified, are mapped to placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>. The placeholders <tt>${1},${2},...</tt> provide the
     * {@link MatchResult#group(int) groups} of the match result.
     *
     * @param s  the string to match to the given mappings and to convert to links in case
     * of successful matches.
     * @param mappings  the mappings, each comprising a regular expression to compare the string with,
     * a template with optional placeholders from which to generate a link, and a purpose to exclude
     * certain mappings that are not relevant.
     * @param userId  the unique identifier of a user.
     * @param entity  any (probably extensible) entity.

     *
     * @return a list of links, or an empty list.
     */
    public List<Link> getMappedLinks(String s, List<T> mappings, String userId, EntityBase entity) {
        List<Link> links = new ArrayList<Link>();
        if (mappings != null) {
            for (T mapping : mappings) {
                if (accept(mapping)) {
                    String url = PropertyMapper.convert(s, mapping.getPattern(), mapping.getTemplate(),
                            entity, userId);
                    if (url != null) {
                        Link location = new Link(url, mapping.getName());
                        links.add(location);
                    }
                }
            }
        }
        return links;
    }

    /**
     * Filters a given collection of link mappings and returns only those that
     * pass {@link #accept(LinkMapping)}.
     *
     * @param mappings  the collection of link mappings to filter.
     *
     * @return  a list of link mappings, or an empty list.
     */
    public List<T> filter(List<T> mappings) {
        List<T> filteredMappings = new ArrayList<T>();
        if (mappings != null) {
            for (T mapping : mappings) {
                if (mapping != null && accept(mapping)) {
                    filteredMappings.add(mapping);
                }
            }
        }
        return filteredMappings;
    }

    /**
     * Returns <code>true</code> if the given mapping should be taken into account for
     * the link generation. This implementation checks only that the purpose of the mapping
     * matches any of the purposes provided in the constructor and may be overwritten to conduct
     * more sophisticated checks.
     *
     * @param mapping  the mapping to check.
     * @return <code>true</code>, if the mapping is accepted.
     */
    protected boolean accept(T mapping) {
         for (String purpose : purposes) {
            if (ALL_PURPOSES[0].equals(purpose) || ComparatorUtils.equals(purpose, mapping.getPurpose())) {
                return true;
            }
        }
        return false;
    }
}
