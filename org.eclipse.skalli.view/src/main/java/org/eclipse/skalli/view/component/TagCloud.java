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

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.tagging.TagCount;
import org.eclipse.skalli.services.tagging.TaggingService;
import org.eclipse.skalli.view.Consts;

public class TagCloud {

    private static final int FONT_SIZE_MIN = 1;
    private static final int FONT_SIZE_NORMAL = 3;
    private static final double FONT_SIZE_DELTA_MAX = 5.0;

    private SortedSet<TagCount> mostPopular = CollectionUtils.emptySortedSet();
    private SortedMap<String,Integer> sortedByName= CollectionUtils.emptySortedMap();

    public TagCloud() {
        this(-1);
    }

    public TagCloud(int count) {
        TaggingService tagService = getTaggingService();
        if (tagService != null) {
            if (count >= 0) {
                mostPopular = tagService.getMostPopular(Project.class, count);
                sortedByName = TagCount.asMap(mostPopular);
            } else {
                mostPopular = tagService.getMostPopular(Project.class);
                sortedByName = tagService.getTags(Project.class);
            }
        }
    }

    protected TaggingService getTaggingService() {
        return Services.getService(TaggingService.class);
    }

    public String doLayout() {
        StringBuilder html = new StringBuilder();
        html.append("<center>"); //$NON-NLS-1$
        int thresholdMin = 1;
        int thresholdMax = 1;
        if (mostPopular.size() > 0) {
            TagCount first = mostPopular.first();
            TagCount last = mostPopular.last();
            thresholdMax = first.getCount();
            thresholdMin = last.getCount();
            for (Entry<String,Integer> entry : sortedByName.entrySet()) {
                int fontSize = FONT_SIZE_NORMAL;
                if (thresholdMin != thresholdMax) {
                    int value = (int) Math.ceil((FONT_SIZE_DELTA_MAX * (entry.getValue() - thresholdMin)) / (thresholdMax - thresholdMin));
                    fontSize = value + FONT_SIZE_MIN;
                }
                String tagUrl = Consts.URL_PROJECTS_TAG + entry.getKey();
                html.append("<a href='"); //$NON-NLS-1$
                html.append(tagUrl);
                html.append("'><font class='tag"); //$NON-NLS-1$
                html.append(fontSize);
                html.append("'>"); //$NON-NLS-1$
                html.append(entry.getKey());
                html.append("</font></a> "); //$NON-NLS-1$
            }
        } else {
            html.append("(no tags defined yet)");
        }
        html.append("</center>"); //$NON-NLS-1$
        return html.toString();
    }
}
