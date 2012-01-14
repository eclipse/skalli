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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.LinkMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingConfig;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingsConfig;
import org.eclipse.skalli.model.ext.devinf.internal.config.ScmLocationMappingResource;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ScmLocationMapperTest {

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

    @Test
    public void testGetMappedLinks() {
        final ConfigurationService mockConfigService = getConfigServiceMock();
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<Link> res = mapper.getMappedLinks(mockConfigService, "", HELLO_WORLD, LinkMapper.ALL_PURPOSES);
        EasyMock.verify(mockConfigService);
        assertNotNull(res);
        assertEquals(7, res.size());
    }

    @Test
    public void testGetMappedLinks_noConfig() {
        final ConfigurationService mockConfigService = EasyMock.createMock(ConfigurationService.class);
        mockConfigService.readCustomization(EasyMock.eq(ScmLocationMappingResource.MAPPINGS_KEY),
                EasyMock.isA(Class.class));
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(mockConfigService);
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<Link> res = mapper.getMappedLinks(mockConfigService, "", HELLO_WORLD, LinkMapper.ALL_PURPOSES);
        EasyMock.verify(mockConfigService);
        assertNotNull(res);
        Assert.assertEquals(0, res.size());
    }

    public void testGetMappings() throws Exception {
        final ConfigurationService mockConfigService = getConfigServiceMock();
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<ScmLocationMappingConfig> mappings = mapper.getMappings(mockConfigService, PROVIDER1, PURPOSE1);
        assertNotNull(mappings);
        assertEquals(2, mappings.size());
        mappings = mapper.getMappings(mockConfigService, PROVIDER1, PURPOSE2);
        assertNotNull(mappings);
        assertEquals(1, mappings.size());
        mappings = mapper.getMappings(mockConfigService, PROVIDER1, PURPOSE2, PURPOSE1, PURPOSE3);
        assertNotNull(mappings);
        assertEquals(4, mappings.size());
        mappings = mapper.getMappings(mockConfigService, PROVIDER1, LinkMapper.ALL_PURPOSES);
        assertNotNull(mappings);
        assertEquals(5, mappings.size());
        mappings = mapper.getMappings(mockConfigService, ScmLocationMapper.ALL_PROVIDERS, PURPOSE2);
        assertNotNull(mappings);
        assertEquals(2, mappings.size());
        mappings = mapper.getMappings(mockConfigService, ScmLocationMapper.ALL_PROVIDERS, LinkMapper.ALL_PURPOSES);
        assertNotNull(mappings);
        assertEquals(8, mappings.size());
        mappings = mapper.getMappings(mockConfigService, null, LinkMapper.ALL_PURPOSES);
        assertNotNull(mappings);
        assertEquals(8, mappings.size());
        mappings = mapper.getMappings(mockConfigService, ScmLocationMapper.ALL_PROVIDERS, (String[]) null);
        assertNotNull(mappings);
        assertEquals(8, mappings.size());
        mappings = mapper.getMappings(mockConfigService, null, (String[]) null);
        assertNotNull(mappings);
        assertEquals(8, mappings.size());
        mappings = mapper.getMappings(mockConfigService, "foobar", PURPOSE2);
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
        mappings = mapper.getMappings(mockConfigService, PROVIDER1, "foobar");
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
        mappings = mapper.getMappings(null, PROVIDER1, PURPOSE1);
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
        mappings = mapper.getMappings(mockConfigService, null);
        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
    }

    private ConfigurationService getConfigServiceMock() {
        ScmLocationMappingConfig m1 = new ScmLocationMappingConfig("1", PROVIDER1, PURPOSE1, PATTERN, TEMPLATE,
                "Mapping 1");
        ScmLocationMappingConfig m2 = new ScmLocationMappingConfig("2", PROVIDER1, PURPOSE1, PATTERN, TEMPLATE1,
                "Mapping 2");
        ScmLocationMappingConfig m3 = new ScmLocationMappingConfig("3", PROVIDER1, PURPOSE2, PATTERN1, TEMPLATE,
                "Mapping 3");
        ScmLocationMappingConfig m4 = new ScmLocationMappingConfig("4", PROVIDER1, null, PATTERN, TEMPLATE, "Mapping 4");
        ScmLocationMappingConfig m5 = new ScmLocationMappingConfig("5", PROVIDER1, PURPOSE3, PATTERN, TEMPLATE,
                "Mapping 5");
        ScmLocationMappingConfig m6 = new ScmLocationMappingConfig("6", null, PURPOSE3, PATTERN, TEMPLATE, "Mapping 6");
        ScmLocationMappingConfig m7 = new ScmLocationMappingConfig("7", null, null, PATTERN, TEMPLATE, "Mapping 7");
        ScmLocationMappingConfig m8 = new ScmLocationMappingConfig("8", PROVIDER2, PURPOSE2, PATTERN, TEMPLATE,
                "Mapping 8");
        ArrayList<ScmLocationMappingConfig> ms = new ArrayList<ScmLocationMappingConfig>(3);
        ms.add(m1);
        ms.add(m2);
        ms.add(m3);
        ms.add(m4);
        ms.add(m5);
        ms.add(m6);
        ms.add(m7);
        ms.add(m8);
        ScmLocationMappingsConfig mappings = new ScmLocationMappingsConfig(ms);

        final ConfigurationService mockConfigService = EasyMock.createMock(ConfigurationService.class);
        mockConfigService.readCustomization(EasyMock.eq(ScmLocationMappingResource.MAPPINGS_KEY),
                EasyMock.isA(Class.class));
        EasyMock.expectLastCall().andReturn(mappings).anyTimes();
        EasyMock.replay(mockConfigService);
        return mockConfigService;
    }

}
