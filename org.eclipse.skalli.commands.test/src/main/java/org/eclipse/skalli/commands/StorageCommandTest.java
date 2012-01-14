/*******************************************************************************
 * Copyright (c) 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.commands;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

public class StorageCommandTest {
    static final String CATEGORY_PROJECT = "Project";
    static final String KEY_1 = PropertyHelperUtils.TEST_UUIDS[0].toString();
    static final String KEY_2 = PropertyHelperUtils.TEST_UUIDS[1].toString();
    static final String TEST_CONTENT_1 = "test content bla bal";
    static final String TEST_CONTENT_2 = "test content hello world";

    @Test
    public void testCopy() throws StorageException {
        HashMapStorageService source = new HashMapStorageService();
        source.write(CATEGORY_PROJECT, KEY_1, new ByteArrayInputStream(
                TEST_CONTENT_1.getBytes()));

        source.write(CATEGORY_PROJECT, KEY_2, new ByteArrayInputStream(
                TEST_CONTENT_2.getBytes()));

        HashMapStorageService destination = new HashMapStorageService();
        CommandInterpreter intr = createMock(CommandInterpreter.class);
        StorageCommand.copy(source, destination, CATEGORY_PROJECT, intr);

        assertEquals(
                TEST_CONTENT_1,
                new String(destination.getBlobStore().get(
                        new HashMapStorageService.Key(CATEGORY_PROJECT, KEY_1))));
        assertEquals(
                TEST_CONTENT_2,
                new String(destination.getBlobStore().get(
                        new HashMapStorageService.Key(CATEGORY_PROJECT, KEY_2))));
    }

}
