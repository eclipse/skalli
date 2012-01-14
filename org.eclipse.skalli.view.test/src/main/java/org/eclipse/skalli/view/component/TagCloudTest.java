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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Taggable;
import org.eclipse.skalli.model.ext.commons.TaggingUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class TagCloudTest {

    private static class TestTaggable implements Taggable {
        @Override
        public SortedSet<String> getTags() {
            return CollectionUtils.emptySortedSet();
        }

        @Override
        public void addTag(String tag) {
        }

        @Override
        public void addTags(String... tags) {
        }

        @Override
        public void removeTag(String tag) {
        }

        @Override
        public boolean hasTag(String tag) {
            return false;
        }
    }

    private Map<String, Set<Project>> getTestProjects() {
        Map<String, Set<Project>> tags = new HashMap<String, Set<Project>>();
        addTestTaggables("a", 4, tags);
        addTestTaggables("b", 14, tags);
        addTestTaggables("c", 1, tags);
        addTestTaggables("d", 4, tags);
        addTestTaggables("e", 7, tags);
        addTestTaggables("f", 1, tags);
        addTestTaggables("g", 4, tags);
        addTestTaggables("h", 25, tags);
        addTestTaggables("i", 1, tags);
        addTestTaggables("j", 1, tags);
        addTestTaggables("k", 2, tags);
        addTestTaggables("l", 12, tags);
        addTestTaggables("m", 14, tags);
        addTestTaggables("n", 1, tags);
        addTestTaggables("o", 6, tags);
        addTestTaggables("p", 4, tags);
        return tags;
    }

    private void addTestTaggables(String tag, int n, Map<String, Set<Project>> taggables) {
        HashSet<Project> set = new HashSet<Project>(n);
        for (int i = 0; i < n; ++i) {
            Project p = new Project();
            p.setUuid(UUID.randomUUID());
            TaggingUtils.addTags(p, tag);
            set.add(p);
        }
        taggables.put(tag, set);
    }

    @Test
    public void testShow3MostPopular() throws IOException {
        TagCloud tagCloud = new TagCloud(getTestProjects(), 3);
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>" +
                        "<a href='/projects?tag=b'><font class='tag1'>b</font></a> " +
                        "<a href='/projects?tag=h'><font class='tag6'>h</font></a> " +
                        "<a href='/projects?tag=m'><font class='tag1'>m</font></a> " +
                        "</center>", xhtml);
    }

    @Test
    public void testShow25MostPopular() throws IOException {
        TagCloud tagCloud = new TagCloud(getTestProjects(), 25);
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>" +
                        "<a href='/projects?tag=a'><font class='tag2'>a</font></a> " +
                        "<a href='/projects?tag=b'><font class='tag4'>b</font></a> " +
                        "<a href='/projects?tag=c'><font class='tag1'>c</font></a> " +
                        "<a href='/projects?tag=d'><font class='tag2'>d</font></a> " +
                        "<a href='/projects?tag=e'><font class='tag3'>e</font></a> " +
                        "<a href='/projects?tag=f'><font class='tag1'>f</font></a> " +
                        "<a href='/projects?tag=g'><font class='tag2'>g</font></a> " +
                        "<a href='/projects?tag=h'><font class='tag6'>h</font></a> " +
                        "<a href='/projects?tag=i'><font class='tag1'>i</font></a> " +
                        "<a href='/projects?tag=j'><font class='tag1'>j</font></a> " +
                        "<a href='/projects?tag=k'><font class='tag2'>k</font></a> " +
                        "<a href='/projects?tag=l'><font class='tag4'>l</font></a> " +
                        "<a href='/projects?tag=m'><font class='tag4'>m</font></a> " +
                        "<a href='/projects?tag=n'><font class='tag1'>n</font></a> " +
                        "<a href='/projects?tag=o'><font class='tag3'>o</font></a> " +
                        "<a href='/projects?tag=p'><font class='tag2'>p</font></a> " +
                        "</center>", xhtml);
    }

    @Test
    public void testShowAllTags() throws IOException {
        TagCloud tagCloud = new TagCloud(getTestProjects(), Integer.MAX_VALUE);
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>" +
                        "<a href='/projects?tag=a'><font class='tag2'>a</font></a> " +
                        "<a href='/projects?tag=b'><font class='tag4'>b</font></a> " +
                        "<a href='/projects?tag=c'><font class='tag1'>c</font></a> " +
                        "<a href='/projects?tag=d'><font class='tag2'>d</font></a> " +
                        "<a href='/projects?tag=e'><font class='tag3'>e</font></a> " +
                        "<a href='/projects?tag=f'><font class='tag1'>f</font></a> " +
                        "<a href='/projects?tag=g'><font class='tag2'>g</font></a> " +
                        "<a href='/projects?tag=h'><font class='tag6'>h</font></a> " +
                        "<a href='/projects?tag=i'><font class='tag1'>i</font></a> " +
                        "<a href='/projects?tag=j'><font class='tag1'>j</font></a> " +
                        "<a href='/projects?tag=k'><font class='tag2'>k</font></a> " +
                        "<a href='/projects?tag=l'><font class='tag4'>l</font></a> " +
                        "<a href='/projects?tag=m'><font class='tag4'>m</font></a> " +
                        "<a href='/projects?tag=n'><font class='tag1'>n</font></a> " +
                        "<a href='/projects?tag=o'><font class='tag3'>o</font></a> " +
                        "<a href='/projects?tag=p'><font class='tag2'>p</font></a> " +
                        "</center>", xhtml);
    }

    @Test
    public void testNoTags() throws IOException {
        TagCloud tagCloud = new TagCloud(null, 3);
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>no tags at the moment</center>", xhtml);
    }

    @Test
    public void testShow0MostPopular() throws IOException {
        TagCloud tagCloud = new TagCloud(getTestProjects(), 0);
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>no tags at the moment</center>", xhtml);
    }

}
