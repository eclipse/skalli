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
package org.eclipse.skalli.services.extension;

import static org.eclipse.skalli.testutil.TestUUIDs.TEST_UUIDS;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.commons.LinkMapping;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.junit.Test;

@SuppressWarnings("nls")
public class LinkMapperTest {

    private static final String USER_ID = "homer";
    private static final String HELLO_WORLD = "hello world";
    private static final String HELLO_WORLD1 = "hello another world";
    private static final String PURPOSE1 = "purpose1";
    private static final String PURPOSE2 = "purpose2";
    private static final String PURPOSE3 = "purpose3";
    private static final String TEMPLATE = "http://example.org/${1}%20${2}";
    private static final String LINK_URL = "http://example.org/hello%20world";
    private static final String TEMPLATE1 = "http://example.org/${uuid}/${userId}/${1}%20${2}";
    private static final String LINK_URL1 = "http://example.org/" + TEST_UUIDS[0] + "/" + USER_ID + "/hello%20world";
    private static final String PATTERN = "^(hello) (world)$";
    private static final String PATTERN1 = "^(hello) another (world)$";
    private static final String STRING1 = "bra";
    private static final String STRING2= "ket";

    private static class LinkMapping1 extends LinkMapping {
        private String string;

        public LinkMapping1(String id, String purpose, String pattern, String template, String name, String string) {
            super(id, purpose, pattern, template, name);
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    private static class CustomAcceptLinkMappper1 extends LinkMapper<LinkMapping1> {
        private String string;

        public CustomAcceptLinkMappper1(String string, String... purposes) {
            super(purposes);
            this.string = string;
        }

        @Override
        protected boolean accept(LinkMapping1 mapping) {
            return super.accept(mapping) && ComparatorUtils.equals(string, mapping.getString());
        }
    }

    @Test
    public void testFilter() throws Exception {
        List<LinkMapping> mappings = getMappings();
        assertFiltered(2, mappings, PURPOSE1);
        assertFiltered(2, mappings, PURPOSE2);
        assertFiltered(1, mappings, PURPOSE3);
        assertFiltered(3, mappings, PURPOSE2, PURPOSE3);
        assertFiltered(5, mappings, PURPOSE1, PURPOSE2, PURPOSE3);
        assertFiltered(0, mappings, "foobar");
        assertFiltered(0, mappings, "");
        assertFiltered(0, mappings, new String[0]);
        assertFiltered(mappings.size(), mappings, LinkMapper.ALL_PURPOSES);
        assertFiltered(mappings.size(), mappings, "*");
        assertFiltered(3, mappings, null, PURPOSE1);
        assertFiltered(1, mappings, (String[])null);
        assertFiltered(1, mappings, new String[]{ null });
        assertFiltered(1, mappings, null); // ambigious varargs call
    }

    @Test
    public void testFilterWithCustomAccept() throws Exception {
        List<LinkMapping1> mappings = getCustomMappings();
        assertFiltered(2, mappings, STRING1, PURPOSE2);
        assertFiltered(1, mappings, STRING2, PURPOSE2);
        assertFiltered(3, mappings, STRING1, LinkMapper.ALL_PURPOSES);
        assertFiltered(0, mappings, "foobar", LinkMapper.ALL_PURPOSES);
        assertFiltered(1, mappings, "", LinkMapper.ALL_PURPOSES);
        assertFiltered(1, mappings, null, LinkMapper.ALL_PURPOSES);
        assertFiltered(0, mappings, null, PURPOSE1);
    }

    @Test
    public void testGetMappedLinks() throws Exception {
        List<LinkMapping> mappings = getMappings();
        assertMappedLinks(1, HELLO_WORLD, mappings, PURPOSE1);
        assertMappedLinks(1, HELLO_WORLD1, mappings, PURPOSE1);
        assertMappedLinks(2, HELLO_WORLD, mappings, PURPOSE1, PURPOSE2);
        assertMappedLinks(2, HELLO_WORLD1, mappings, PURPOSE1, PURPOSE2);
        assertMappedLinks(4, HELLO_WORLD, mappings, LinkMapper.ALL_PURPOSES);
        assertMappedLinks(2, HELLO_WORLD1, mappings, LinkMapper.ALL_PURPOSES);
        assertMappedLinks(0, "foobar", mappings, LinkMapper.ALL_PURPOSES);
        assertMappedLinks(0, "", mappings, LinkMapper.ALL_PURPOSES);
        assertMappedLinks(0, null, mappings, LinkMapper.ALL_PURPOSES);
    }

    @Test
    public void testPathologicalMappings() throws Exception {
        List<LinkMapping> mappings = getPathologicalMappings();
        assertEquals(7, mappings.size());
        assertMappedLinks(0, HELLO_WORLD, mappings, PURPOSE1);
        // only Mapping 1 survives because LinkMapper.ALL_PURPOSES comprises also purpose==null!
        assertMappedLinks(1, HELLO_WORLD, mappings, LinkMapper.ALL_PURPOSES);
    }

    private void assertMappedLinks(int expectedSize, String s, List<LinkMapping> mappings, String... purposes) {
        TestEntityBase1 entity = new TestEntityBase1(TEST_UUIDS[0]);
        LinkMapper<LinkMapping> mapper = new LinkMapper<LinkMapping>(purposes);
        List<Link> links = mapper.getMappedLinks(s, mappings, USER_ID, entity);
        assertEquals(expectedSize, links.size());
        for (Link link: links) {
            if (HELLO_WORLD.equals(s)) {
                assertEquals(LINK_URL, link.getUrl());
            } else {
                assertEquals(LINK_URL1, link.getUrl());
            }
        }
    }

    private List<LinkMapping> getMappings() {
        List<LinkMapping> mappings = new ArrayList<LinkMapping>();
        mappings.add(new LinkMapping("1", PURPOSE1, PATTERN, TEMPLATE, "Mapping 1"));
        mappings.add(new LinkMapping("2", PURPOSE1, PATTERN1, TEMPLATE1, "Mapping 2"));
        mappings.add(new LinkMapping("3", PURPOSE2, PATTERN, TEMPLATE, "Mapping 3"));
        mappings.add(new LinkMapping("4", null, PATTERN, TEMPLATE, "Mapping 4"));
        mappings.add(new LinkMapping("5", PURPOSE3, PATTERN, TEMPLATE, "Mapping 5"));
        mappings.add(new LinkMapping("6", PURPOSE2, PATTERN1, TEMPLATE1, "Mapping 6"));
        return mappings;
    }
    private List<LinkMapping1> getCustomMappings() {
        List<LinkMapping1> mappings = new ArrayList<LinkMapping1>();
        mappings.add(new LinkMapping1("1", PURPOSE3, PATTERN, TEMPLATE, "Mapping 1", STRING1));
        mappings.add(new LinkMapping1("2", null, PATTERN, TEMPLATE, "Mapping 2", STRING2));
        mappings.add(new LinkMapping1("3", PURPOSE2, PATTERN, TEMPLATE, "Mapping 3", STRING1));
        mappings.add(new LinkMapping1("4", PURPOSE2, PATTERN, TEMPLATE1, "Mapping 4", STRING1));
        mappings.add(new LinkMapping1("5", PURPOSE2, PATTERN, TEMPLATE1, "Mapping 5", STRING2));
        mappings.add(new LinkMapping1("6", PURPOSE3, PATTERN, TEMPLATE1, "Mapping 6", ""));
        mappings.add(new LinkMapping1("7", PURPOSE3, PATTERN, TEMPLATE1, "Mapping 7", null));
        return mappings;
    }

    private List<LinkMapping> getPathologicalMappings() {
        List<LinkMapping> mappings = new ArrayList<LinkMapping>();
        mappings.add(new LinkMapping("1", null, PATTERN, TEMPLATE, "Mapping 1"));
        mappings.add(new LinkMapping("2", PURPOSE1, null, TEMPLATE, "Mapping 2"));
        mappings.add(new LinkMapping("3", PURPOSE1, PATTERN, null, "Mapping 3"));
        mappings.add(new LinkMapping("4", null, null, TEMPLATE, "Mapping 4"));
        mappings.add(new LinkMapping("5", null, PATTERN, null, "Mapping 5"));
        mappings.add(new LinkMapping("6", PURPOSE1, null, null, "Mapping 6"));
        mappings.add(new LinkMapping("7", null, null, null, "Mapping 7"));
        return mappings;
    }

    private void assertFiltered(int expectedSize, List<LinkMapping> mappings, String... purposes) {
        LinkMapper<LinkMapping> mapper = new LinkMapper<LinkMapping>(purposes);
        List<LinkMapping> filtered = mapper.filter(mappings);
        assertEquals(expectedSize, filtered.size());
    }

    private void assertFiltered(int expectedSize, List<LinkMapping1> mappings, String string, String... purposes) {
        CustomAcceptLinkMappper1 mapper = new CustomAcceptLinkMappper1(string, purposes);
        List<LinkMapping1> filtered = mapper.filter(mappings);
        assertEquals(expectedSize, filtered.size());
    }
}
