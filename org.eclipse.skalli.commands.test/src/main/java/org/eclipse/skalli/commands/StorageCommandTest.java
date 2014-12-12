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
package org.eclipse.skalli.commands;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.eclipse.skalli.testutil.HashMapStorageService.Key;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class StorageCommandTest {
    static final String CATEGORY_PROJECT = "Project";
    static final String KEY_1 = TestUUIDs.TEST_UUIDS[0].toString();
    static final String KEY_2 = TestUUIDs.TEST_UUIDS[1].toString();
    static final byte[] TEST_CONTENT_1 = getBytesUTF8("test content bla bal");
    static final byte[] TEST_CONTENT_2 = getBytesUTF8("test content hello world");

    @Test
    public void testCopy() throws Exception {
        HashMapStorageService source = new HashMapStorageService();
        source.write(CATEGORY_PROJECT, KEY_1, new ByteArrayInputStream(TEST_CONTENT_1));
        source.write(CATEGORY_PROJECT, KEY_2, new ByteArrayInputStream(TEST_CONTENT_2));

        HashMapStorageService destination = new HashMapStorageService();
        CommandInterpreter intr = createMock(CommandInterpreter.class);
        StorageCommand.copy(source, destination, CATEGORY_PROJECT, intr);

        assertTrue(Arrays.equals(
                TEST_CONTENT_1,
                destination.asMap().get(new Key(CATEGORY_PROJECT, KEY_1))));
        assertTrue(Arrays.equals(
                TEST_CONTENT_2,
                destination.asMap().get(new Key(CATEGORY_PROJECT, KEY_2))));
    }

    private static byte[] getBytesUTF8(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8");
        }
    }

}
