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
package org.eclipse.skalli.core.storage;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.skalli.core.storage.Historian.HistoryEntry;
import org.eclipse.skalli.core.storage.Historian.HistoryIterator;
import org.eclipse.skalli.testutil.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class HistorianTest {

    private File tmpDir;
    private File fileOrig;
    private File file0;
    private File file1;
    private File fileOther;
    private File fileNext;
    private File fileHistory;

    @Before
    public void setup() throws Exception {
        tmpDir = TestUtils.createTempDir("HistorianTest");

        fileOrig = new File(tmpDir.getAbsolutePath() + "/bla.xml");
        FileUtils.writeStringToFile(fileOrig, "TEST CONTENT");

        file0 = new File(tmpDir.getAbsolutePath() + "/bla.xml.0.history");
        FileUtils.writeStringToFile(file0, "TEST CONTENT");

        file1 = new File(tmpDir.getAbsolutePath() + "/bla.xml.1.HISTORY");
        FileUtils.writeStringToFile(file1, "TEST CONTENT");

        fileNext = new File(tmpDir.getAbsolutePath() + "/bla.xml.2.history");

        fileOther = new File(tmpDir.getAbsolutePath() + "/blubb.xml");
        FileUtils.writeStringToFile(fileOther, "TEST CONTENT");

        fileHistory = new File(tmpDir.getAbsolutePath() + "/.history");
    }

    @After
    public void tearDown() throws Exception {
        if (tmpDir != null) {
            FileUtils.forceDelete(tmpDir);
        }
    }

    @Test
    public void testGetLastHistoryNumber() {
        Historian h = new Historian();
        int res = h.getLastHistoryNumber(fileOrig);
        Assert.assertEquals(1, res);
    }

    @Test
    public void testGetLastHistoryNumber_wrongFilename() {
        Historian h = new Historian();
        int res = h.getLastHistoryNumber(fileOther);
        Assert.assertEquals(-1, res);
    }

    @Test
    public void testHistorizeMultipleFiles() {
        Historian h = new Historian();
        Assert.assertFalse(fileNext.exists());
        h.historize(fileOrig, false);
        Assert.assertTrue(fileNext.exists());
    }

    @Test
    public void testHistorizeSingleFile() throws Exception {
        Historian h = new Historian();
        Assert.assertFalse(fileHistory.exists());
        h.historize(fileOrig, true);

        h = new Historian();
        h.historize(fileOrig, true);

        h = new Historian();
        h.historize(fileOther, true);

        h = new Historian();
        h.historize(fileOrig, true);

        h = new Historian();
        h.historize(fileOrig, true);

        Assert.assertTrue(fileHistory.exists());
        assertHistoryEntries(h, 5);
        assertHistoryEntries(h, "bla", 4);
        assertHistoryEntries(h, "blubb", 1);
    }

    private void assertHistoryEntries(Historian h, int size) throws Exception {
        int i = 0;
        HistoryIterator it = null;
        try {
            it = h.getHistory();
            while (it.hasNext()) {
                it.next();
                ++i;
            }
        } finally {
            it.close();
        }
        Assert.assertEquals(size, i);
    }

    private void assertHistoryEntries(Historian h, String id, int size) throws Exception {
        int i = 0;
        HistoryIterator it = null;
        try {
            it = h.getHistory(id);
            while (it.hasNext()) {
                HistoryEntry next = it.next();
                Assert.assertEquals(id, next.getId());
                Assert.assertEquals(i, next.getVersion());
                Assert.assertEquals("TEST CONTENT", next.getContent());
                Assert.assertTrue(next.getTimestamp() > 0);
                ++i;
            }
        } finally {
            it.close();
        }
        Assert.assertEquals(size, i);
    }
}
