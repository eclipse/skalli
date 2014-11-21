/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.feed.jpa;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.services.feed.Entry;
import org.junit.Test;

public class JPAFeedPersistenceServiceTest {

    @Test
    public void testCreateEntry() throws Exception {
        JPAFeedPersistenceComponent s = new JPAFeedPersistenceComponent();
        Entry newEntry = s.createEntry();
        assertNotNull(newEntry);

        assertTrue(
                "JPAFeedPersistenceService.createEntry() should return an Element instanceof "
                        + EntryJPA.class.getName(), newEntry instanceof EntryJPA);
    }


}
