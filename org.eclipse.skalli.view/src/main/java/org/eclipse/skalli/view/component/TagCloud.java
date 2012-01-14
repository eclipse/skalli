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
package org.eclipse.skalli.view.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.tagging.TaggingService;
import org.eclipse.skalli.view.Consts;

public class TagCloud {

    private static final int FONT_SIZE_MIN = 1;
    private static final int FONT_SIZE_NORMAL = 3;
    private static final double FONT_SIZE_DELTA_MAX = 5.0;

    private final int viewMax;

    private int thresholdMin = 1;
    private int thresholdMax = 1;

    private final Map<String, Set<Project>> tags;

    /**
     * Set of most popular tags sorted alphanumerically.
     * This set contains only the <code>viewMax</code>
     * most popluar tags.
     */
    private SortedSet<String> mostPopular;

    public static TagCloud getInstance() {
        return getInstance(Integer.MAX_VALUE);
    }

    public static TagCloud getInstance(int maxTags) {
        TagCloud tagCloud = null;
        TaggingService service = Services.getService(TaggingService.class);
        if (service != null) {
            tagCloud = new TagCloud(service.getTaggables(Project.class), maxTags);
        }
        return tagCloud;
    }

    TagCloud(Map<String, Set<Project>> tags, int viewMax) {
        if (tags == null) {
            tags = new HashMap<String, Set<Project>>(0);
        }
        this.tags = tags;
        this.viewMax = Math.min(tags.size(), viewMax);
        determineMostPopular(tags);
    }

    /**
     * Sorts tags by their frequency (most frequent tags at the beginning).
     * Determines the thresholds for the font size calculation.
     */
    private void determineMostPopular(final Map<String, Set<Project>> tags) {
        // sort all tags by frequency
        LinkedList<String> sortedByFrequency = new LinkedList<String>(tags.keySet());
        Collections.sort(sortedByFrequency, new Comparator<String>() {
            @Override
            public int compare(String tag1, String tag2) {
                int result = tags.get(tag2).size() - tags.get(tag1).size();
                if (result == 0) {
                    result = tag1.compareTo(tag2);
                }
                return result;
            }
        });

        // determine the largest and smallest frequency of tags
        if (sortedByFrequency.size() > 0) {
            thresholdMax = tags.get(sortedByFrequency.getFirst()).size();
            thresholdMin = tags.get(sortedByFrequency.get(Math.max(viewMax - 1, 0))).size();
        }

        // sort the first viewMax tags of sortedByFrequency alphanumerically
        mostPopular = new TreeSet<String>();
        for (int i = 0; i < viewMax; ++i) {
            String next = sortedByFrequency.get(i);
            mostPopular.add(next);
        }
    }

    public String doLayout() {
        StringBuilder xhtmlCloud = new StringBuilder();

        xhtmlCloud.append("<center>"); //$NON-NLS-1$
        if (mostPopular.size() > 0) {
            for (String tag : mostPopular) {
                int fontSize = calculateFontSize(tag);
                String tagUrl = Consts.URL_PROJECTS_TAG + tag;
                xhtmlCloud.append("<a href='"); //$NON-NLS-1$
                xhtmlCloud.append(tagUrl);
                xhtmlCloud.append("'><font class='tag"); //$NON-NLS-1$
                xhtmlCloud.append(fontSize);
                xhtmlCloud.append("'>"); //$NON-NLS-1$
                xhtmlCloud.append(tag);
                xhtmlCloud.append("</font></a> "); //$NON-NLS-1$
            }
        } else {
            xhtmlCloud.append("no tags at the moment");
        }
        xhtmlCloud.append("</center>"); //$NON-NLS-1$

        return xhtmlCloud.toString();
    }

    /**
     * Calculates the font size for the given tag (min=1, max=6).
     */
    private int calculateFontSize(String tag) {
        if (thresholdMin == thresholdMax) {
            return FONT_SIZE_NORMAL;
        }
        int count = tags.get(tag).size();
        int value = (int) Math.ceil((FONT_SIZE_DELTA_MAX * (count - thresholdMin)) / (thresholdMax - thresholdMin));
        return value + FONT_SIZE_MIN;
    }
}
