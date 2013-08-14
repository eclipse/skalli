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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.tagging.TagCount;
import org.eclipse.skalli.services.tagging.TaggingService;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class TagCloudTest {

    private static class TestTaggingService implements TaggingService {

        private TreeMap<String,Integer> byName =  new TreeMap<String,Integer>();
        private TreeSet<TagCount> byCount =  new TreeSet<TagCount>();

        public TestTaggingService() {
            byName.put("a", 4);
            byName.put("b", 14);
            byName.put("c", 1);
            byName.put("d", 4);
            byName.put("e", 7);
            byName.put("f", 1);
            byName.put("g", 4);
            byName.put("h", 25);
            byName.put("i", 1);
            byName.put("j", 1);
            byName.put("k", 2);
            byName.put("l", 12);
            byName.put("m", 14);
            byName.put("n", 1);
            byName.put("o", 6);
            byName.put("p", 4);
            for (Entry<String,Integer> entry: byName.entrySet()) {
                byCount.add(new TagCount(entry.getKey(), entry.getValue()));
            }
        }

        @Override
        public <T extends EntityBase> SortedMap<String, Integer> getTags(Class<T> entityClass) {
            return byName;
        }

        @Override
        public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass, int count) {
            if (count < 0) {
                return byCount;
            }
            TreeSet<TagCount> result = new TreeSet<TagCount>();
            int i = 0;
            Iterator<TagCount> it = byCount.iterator();
            while (i < count && it.hasNext()) {
                result.add(it.next());
                ++i;
            }
            return result;
        }

        @Override
        public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass) {
            return byCount;
        }
    }

    private static class EmptyTaggingService implements TaggingService {

        @Override
        public <T extends EntityBase> SortedMap<String, Integer> getTags(Class<T> entityClass) {
            return CollectionUtils.emptySortedMap();
        }

        @Override
        public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass, int count) {
            return CollectionUtils.emptySortedSet();
        }

        @Override
        public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass) {
            return CollectionUtils.emptySortedSet();
        }

    }

    private static class TestTagCloud extends TagCloud {

        public TestTagCloud(int count, boolean initialize) {
            super(count);
        }

        @Override
        protected TaggingService getTaggingService() {
            return new TestTaggingService();
        }
    }

    private static class EmptyTagCloud extends TagCloud {

        public EmptyTagCloud() {
            super();
        }

        @Override
        protected TaggingService getTaggingService() {
            return new EmptyTaggingService();
        }
    }

    @Test
    public void testShow3MostPopular() throws IOException {
        TagCloud tagCloud = new TestTagCloud(3, true);
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
        TagCloud tagCloud = new TestTagCloud(25, true);
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
        TagCloud tagCloud = new TestTagCloud(Integer.MAX_VALUE, true);
        assertAllTags(tagCloud.doLayout());
    }

    @Test
    public void testShowAllTagsNegativeCount() throws IOException {
        TagCloud tagCloud = new TestTagCloud(-1, true);
        assertAllTags(tagCloud.doLayout());
    }

    private void assertAllTags(String xhtml) {
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
        TagCloud tagCloud = new EmptyTagCloud();
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>(no tags defined yet)</center>", xhtml);
    }

    @Test
    public void testShow0MostPopular() throws IOException {
        TagCloud tagCloud = new TestTagCloud(0, true);
        String xhtml = tagCloud.doLayout();

        Assert.assertEquals("xhtml",
                "<center>(no tags defined yet)</center>", xhtml);
    }

}

