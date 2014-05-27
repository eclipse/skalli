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
package org.eclipse.skalli.model.ext.mapping.scm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapping;
import org.eclipse.skalli.model.Project;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ScmLocationMapperTest {

    private static final String USER_ID = "homer";
    private static final String HELLO_WORLD = "hello world";
    private static final String PROVIDER1 = "provider1";
    private static final String PROVIDER2 = "provider2";
    private static final String PURPOSE1 = "purpose1";
    private static final String PURPOSE2 = "purpose2";
    private static final String PURPOSE3 = "purpose3";
    private static final String TEMPLATE = "{1}_1_{2}";
    private static final String TEMPLATE1 = "{1}_2_{2}";
    private static final String PATTERN = "^(hello) (world)$";
    private static final String PATTERN1 = "^(hello)no(world)$";

    private static class TestScmLocationMapper extends ScmLocationMapper {
        public TestScmLocationMapper(String provider, String... purposes) {
            super(provider, purposes);
        }

        @Override
        protected List<ScmLocationMapping> getAllMappings() {
            return Arrays.asList(
                new ScmLocationMapping("1", PROVIDER1, PURPOSE1, PATTERN, TEMPLATE, "Mapping 1"),
                new ScmLocationMapping("2", PROVIDER1, PURPOSE1, PATTERN, TEMPLATE1, "Mapping 2"),
                new ScmLocationMapping("3", PROVIDER1, PURPOSE2, PATTERN1, TEMPLATE, "Mapping 3"),
                new ScmLocationMapping("4", PROVIDER1, null, PATTERN, TEMPLATE, "Mapping 4"),
                new ScmLocationMapping("5", PROVIDER1, PURPOSE3, PATTERN, TEMPLATE, "Mapping 5"),
                new ScmLocationMapping("6", null, PURPOSE3, PATTERN, TEMPLATE, "Mapping 6"),
                new ScmLocationMapping("7", null, null, PATTERN, TEMPLATE, "Mapping 7"),
                new ScmLocationMapping("8", PROVIDER2, PURPOSE2, PATTERN, TEMPLATE, "Mapping 8"));
        }
    }

    @Test
    public void testGetMappedLinks() {
        TestScmLocationMapper mapper = new TestScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS,
                ScmLocationMapper.ALL_PURPOSES);
        Project project = new Project("some.project", "Some Project", "Some Project");
        List<Link> res = mapper.getMappedLinks(HELLO_WORLD, USER_ID, project);
        assertNotNull(res);
        assertEquals(7, res.size());
    }

    @Test
    public void testGetMappedLinks_noConfig() {
        ScmLocationMapper mapper = new ScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS,
                ScmLocationMapper.ALL_PURPOSES);
        Project project = new Project("some.project", "Some Project", "Some Project");
        List<Link> res = mapper.getMappedLinks(HELLO_WORLD, USER_ID, project);
        assertNotNull(res);
        Assert.assertEquals(0, res.size());
    }

    public void testGetFilteredMappings() throws Exception {
        TestScmLocationMapper mapper = new TestScmLocationMapper(PROVIDER1, PURPOSE1);
        List<ScmLocationMapping> mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(2, mappings.size());

        mapper = new TestScmLocationMapper(PROVIDER1, PURPOSE2);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(1, mappings.size());

        mapper = new TestScmLocationMapper(PROVIDER1, PURPOSE2, PURPOSE1, PURPOSE3);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(4, mappings.size());

        mapper = new TestScmLocationMapper(PROVIDER1, ScmLocationMapper.ALL_PURPOSES);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(5, mappings.size());

        mapper = new TestScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS, PURPOSE2);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(2, mappings.size());

        mapper = new TestScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS, ScmLocationMapper.ALL_PURPOSES);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(8, mappings.size());

        mapper = new TestScmLocationMapper(null, ScmLocationMapper.ALL_PURPOSES);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(8, mappings.size());

        mapper = new TestScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS, (String[]) null);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(8, mappings.size());

        mapper = new TestScmLocationMapper(null, (String[]) null);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertEquals(8, mappings.size());

        mapper = new TestScmLocationMapper("foobar", PURPOSE2);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());

        mapper = new TestScmLocationMapper(PROVIDER1, "foobar");
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());

        mapper = new TestScmLocationMapper(PROVIDER1, PURPOSE1);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());

        mapper = new TestScmLocationMapper(null);
        mappings = mapper.getFilteredMappings();
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
    }
}
