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
package org.eclipse.skalli.model.ext.mapping.mail;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.LinkMapper;
import org.eclipse.skalli.ext.mapping.mail.MailingListMapper;
import org.eclipse.skalli.ext.mapping.mail.MailingListMappingConfig;
import org.eclipse.skalli.ext.mapping.mail.MailingListMappingsConfig;
import org.eclipse.skalli.ext.mapping.mail.internal.MailingListMappingResource;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class MailingListMapperTest {

    @Test
    public void testGetMappedLinks() {
        MailingListMappingConfig m1 = new MailingListMappingConfig("1", "purpose1", "^(hello) (world)$", "{1}_1_{2}",
                "Mapping 1");
        MailingListMappingConfig m2 = new MailingListMappingConfig("2", "purpose1", "^(hello) (world)$", "{1}_2_{2}",
                "Mapping 2");
        MailingListMappingConfig m3 = new MailingListMappingConfig("3", "purpose1", "^(hello)no(world)$", "{1}_1_{2}",
                "Mapping 3");
        ArrayList<MailingListMappingConfig> ms = new ArrayList<MailingListMappingConfig>(3);
        ms.add(m1);
        ms.add(m2);
        ms.add(m3);
        MailingListMappingsConfig mappings = new MailingListMappingsConfig(ms);

        final ConfigurationService mockConfigService = EasyMock.createMock(ConfigurationService.class);
        Object[] mocks = new Object[] { mockConfigService };

        EasyMock.reset(mocks);

        mockConfigService.readCustomization(EasyMock.eq(MailingListMappingResource.MAPPINGS_KEY),
                EasyMock.isA(Class.class));
        EasyMock.expectLastCall().andReturn(mappings);

        EasyMock.replay(mocks);

        MailingListMapper mapper = new MailingListMapper();
        List<Link> res = mapper.getMappedLinks(mockConfigService, "some.project", "hello world",
                LinkMapper.ALL_PURPOSES);
        EasyMock.verify(mocks);

        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
    }

    @Test
    public void testGetMappedLinks_noConfig() {
        final ConfigurationService mockConfigService = EasyMock.createMock(ConfigurationService.class);
        Object[] mocks = new Object[] { mockConfigService };

        EasyMock.reset(mocks);

        mockConfigService.readCustomization(EasyMock.eq(MailingListMappingResource.MAPPINGS_KEY),
                EasyMock.isA(Class.class));
        EasyMock.expectLastCall().andReturn(null);

        EasyMock.replay(mocks);

        MailingListMapper mapper = new MailingListMapper();
        List<Link> res = mapper.getMappedLinks(mockConfigService, "some.project", "hello world",
                LinkMapper.ALL_PURPOSES);
        EasyMock.verify(mocks);

        Assert.assertNotNull(res);
        Assert.assertEquals(0, res.size());

    }

}
