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
package org.eclipse.skalli.model.ext.mapping;

import java.util.Collections;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class LinkGroupTest {

    private static final String TESTURL = "testurl";
    private static final String TESTLABEL = "testlabel";

    @Test
    public void testLink() throws Exception {
        Link link = new Link();
        Assert.assertNull(link.getLabel());
        Assert.assertNull(link.getUrl());

        link.setLabel(TESTLABEL);
        link.setUrl(TESTURL);
        Assert.assertEquals(TESTLABEL, link.getLabel());
        Assert.assertEquals(TESTURL, link.getUrl());

        Link otherLink = new Link(TESTURL, TESTLABEL);
        Assert.assertEquals(link, otherLink);
    }

    @Test
    public void testLinkGroupBasic() throws Exception {
        LinkGroup linkGroup = new LinkGroup();
        Assert.assertNull(linkGroup.getCaption());

        linkGroup.setCaption(TESTLABEL);
        Assert.assertEquals(TESTLABEL, linkGroup.getCaption());

        LinkGroup otherLinkGroup = new LinkGroup(TESTLABEL, Collections.<Link> emptySet());
        Assert.assertEquals(linkGroup, otherLinkGroup);
    }

    @Test
    public void testLinkGroupExtended() throws Exception {
        LinkGroup linkGroup = new LinkGroup();

        Link movingTarget = new Link(TESTURL, TESTLABEL);
        Link unknownStranger = new Link("unknown", "stranger");

        linkGroup.add(movingTarget);
        for (int i = 0; i < 10; i++) {
            linkGroup.add(new Link(String.valueOf(i), String.valueOf(i)));
        }

        Assert.assertTrue(linkGroup.moveDown(movingTarget));
        Assert.assertTrue(linkGroup.moveDown(movingTarget));
        Assert.assertTrue(linkGroup.moveDown(movingTarget));

        movingTarget.setLabel(TESTLABEL + "1");
        movingTarget.setUrl(TESTURL + "1");

        Assert.assertTrue(linkGroup.moveUp(movingTarget));
        Assert.assertTrue(linkGroup.moveUp(movingTarget));
        Assert.assertTrue(linkGroup.moveUp(movingTarget));
        Assert.assertFalse(linkGroup.moveUp(movingTarget));

        Assert.assertTrue(linkGroup.hasItem(movingTarget));
        Assert.assertFalse(linkGroup.hasItem(unknownStranger));
    }
}
