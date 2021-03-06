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
package org.eclipse.skalli.model.ext.mapping.mail;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.mail.MailingListMapper;
import org.eclipse.skalli.ext.mapping.mail.MailingListMapping;
import org.eclipse.skalli.ext.mapping.mail.MailingListMappings;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.LinkMapper;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class MailingListMapperTest {

    @Test
    public void testGetMappedLinks() {
        MailingListMapping m1 = new MailingListMapping("1", "purpose1", "^(hello) (world)$", "{1}_1_{2}",
                "Mapping 1");
        MailingListMapping m2 = new MailingListMapping("2", "purpose1", "^(hello) (world)$", "{1}_2_{2}",
                "Mapping 2");
        MailingListMapping m3 = new MailingListMapping("3", "purpose1", "^(hello)no(world)$", "{1}_1_{2}",
                "Mapping 3");
        ArrayList<MailingListMapping> ms = new ArrayList<MailingListMapping>(3);
        ms.add(m1);
        ms.add(m2);
        ms.add(m3);
        MailingListMappings mappings = new MailingListMappings(ms);

        final ConfigurationService mockConfigService = EasyMock.createMock(ConfigurationService.class);
        Object[] mocks = new Object[] { mockConfigService };

        EasyMock.reset(mocks);

        mockConfigService.readConfiguration(MailingListMappings.class);
        EasyMock.expectLastCall().andReturn(mappings);

        EasyMock.replay(mocks);

        MailingListMapper mapper = new MailingListMapper(LinkMapper.ALL_PURPOSES);
        Project project = new Project("some.project", "Some Project", "Some Project");
        List<Link> res = mapper.getMappedLinks("hello world", "homer", project, mockConfigService);
        EasyMock.verify(mocks);

        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
    }

    @Test
    public void testGetMappedLinks_noConfig() {
        final ConfigurationService mockConfigService = EasyMock.createMock(ConfigurationService.class);
        Object[] mocks = new Object[] { mockConfigService };

        EasyMock.reset(mocks);

        mockConfigService.readConfiguration(MailingListMappings.class);
        EasyMock.expectLastCall().andReturn(null);

        EasyMock.replay(mocks);

        MailingListMapper mapper = new MailingListMapper(LinkMapper.ALL_PURPOSES);
        Project project = new Project("some.project", "Some Project", "Some Project");
        List<Link> res = mapper.getMappedLinks("hello world" ,"homer", project, mockConfigService);
        EasyMock.verify(mocks);

        Assert.assertNotNull(res);
        Assert.assertEquals(0, res.size());

    }

}
