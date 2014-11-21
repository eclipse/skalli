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
package org.eclipse.skalli.commons;

import static org.junit.Assert.*;

import java.util.SortedSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.skalli.commons.Statistics.BrowserInfo;
import org.eclipse.skalli.commons.Statistics.RefererInfo;
import org.eclipse.skalli.commons.Statistics.ResponseTimeInfo;
import org.eclipse.skalli.commons.Statistics.SearchInfo;
import org.eclipse.skalli.commons.Statistics.StatisticsInfo;
import org.eclipse.skalli.commons.Statistics.UsageInfo;
import org.eclipse.skalli.commons.Statistics.UserInfo;
import org.junit.Test;

@SuppressWarnings("nls")
public class StatisticsTest {

    @Test
    public void testStatisticsInfo() throws Exception {
        long now = System.currentTimeMillis();
        StatisticsInfo info = new StatisticsInfo("homer", 4711L);
        assertEquals(DigestUtils.shaHex("homer"), info.getUserHash());
        assertEquals(4711L, info.getSequenceNumber());
        assertTrue(info.getTimestamp() >= now);
    }

    @Test
    public void testStatisticsInfoInRange() throws Exception {
        StatisticsInfo info = new StatisticsInfo("homer", 4711L);
        long timestamp = info.getTimestamp();
        assertTrue(info.inRange(timestamp - 10L, timestamp + 10L));
        assertTrue(info.inRange(timestamp, timestamp));
        assertTrue(info.inRange(0, timestamp + 10L));
        assertTrue(info.inRange(-1L, timestamp + 10L));
        assertTrue(info.inRange(timestamp - 10L, 0));
        assertTrue(info.inRange(timestamp - 10L, -1L));
        assertTrue(info.inRange(0, 0));
        assertTrue(info.inRange(-1L, -1L));
        assertFalse(info.inRange(timestamp + 1L, timestamp + 10L));
        assertFalse(info.inRange(timestamp -10L, timestamp - 1L));
        assertFalse(info.inRange(timestamp + 1L, timestamp - 1L));
        assertFalse(info.inRange(0, timestamp - 1L));
        assertFalse(info.inRange(timestamp + 1L, 0));
    }

    @Test
    public void testStatisticsInfoCompareTo() throws Exception {
        StatisticsInfo info1 = new StatisticsInfo("homer", 4711L);
        StatisticsInfo info2 = new StatisticsInfo("homer", 815L);
        StatisticsInfo info3 = new StatisticsInfo("marge", 4711L);
        assertEquals(1, info1.compareTo(info2));
        assertEquals(-1, info2.compareTo(info1));
        assertEquals(0, info1.compareTo(info1));
        assertEquals(0, info2.compareTo(info2));
        assertEquals(0, info1.compareTo(info3));
        assertEquals(0, info3.compareTo(info1));
        assertEquals(1, info3.compareTo(info2));
        assertEquals(-1, info2.compareTo(info3));
    }

    @Test
    public void testTrackUsage() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackUsage("homer", "/projects", "x");
        stats.trackUsage("marge", "/projects/a", "y");
        stats.trackUsage("marge", "/projects/b", "z");
        stats.trackUsage("homer", "/projects", "y");
        stats.trackUsage("bart", "/projects/a", "y");
        SortedSet<UsageInfo> info = stats.getUsageInfo();
        assertInfoSet(stats, info);
        assertEquals(DigestUtils.shaHex("bart"), info.last().getUserHash());
        assertEquals("/projects/a", info.last().getPath());
        assertEquals("y", info.last().getReferer());
    }

    @Test
    public void testTrackUser() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackUser("homer", "A", "X");
        stats.trackUser("marge", "B", "Y");
        stats.trackUser("homer", "A", "X");
        stats.trackUser("bart", "C", "Z");
        SortedSet<UserInfo> info = stats.getUserInfo();
        assertInfoSet(stats, info);
        assertEquals(DigestUtils.shaHex("bart"), info.last().getUserHash());
        assertEquals("C", info.last().getDepartment());
        assertEquals("Z", info.last().getLocation());
    }

    @Test
    public void testTrackBrowser() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackBrowser("homer", "UserAgent1");
        stats.trackBrowser("marge", "UserAgent2");
        stats.trackBrowser("bart", "UserAgent1");
        SortedSet<BrowserInfo> info = stats.getBrowserInfo();
        assertInfoSet(stats, info);
        assertEquals(DigestUtils.shaHex("bart"), info.last().getUserHash());
        assertEquals("UserAgent1", info.last().getUserAgent());
    }

    @Test
    public void testTrackSearch() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackSearch("homer", "queryA", 25, 1234L);
        stats.trackSearch("marge", "queryB", 2, 344L);
        stats.trackSearch("bart", "queryC", 0, 723L);
        stats.trackSearch("marge", "queryC", 0, 567L);
        SortedSet<SearchInfo> info = stats.getSearchInfo();
        assertInfoSet(stats, info);
        assertEquals(DigestUtils.shaHex("marge"), info.last().getUserHash());
        assertEquals("queryC", info.last().getQueryString());
        assertEquals(0, info.last().getResultCount());
        assertEquals(567L, info.last().getDuration());
    }

    @Test
    public void testTrackReferer() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackReferer("homer", "ref35");
        stats.trackReferer("marge", null);
        stats.trackReferer("bart", "ref12");
        SortedSet<RefererInfo> info = stats.getRefererInfo();
        assertInfoSet(stats, info);
        assertEquals(DigestUtils.shaHex("bart"), info.last().getUserHash());
        assertEquals("ref12", info.last().getReferer());
    }

    @Test
    public void testResponseTime() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackResponseTime("homer", "/projects", 12L);
        stats.trackResponseTime("marge", "/favorites", 2837L);
        stats.trackResponseTime("bart", "/projects/a", 54L);
        SortedSet<ResponseTimeInfo> info = stats.getResponseTimeInfo();
        assertInfoSet(stats, info);
        assertEquals(DigestUtils.shaHex("bart"), info.last().getUserHash());
        assertEquals("/projects/a", info.last().getPath());
        assertEquals(54L, info.last().getResponseTime());
    }

    @Test
    public void testMixedInfo() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = initMixedStatistics(now);
        assertEquals(now, stats.getStartupTime());
        assertEquals(stats.getUserInfo().first().getTimestamp(), stats.getStartDate());
        assertEquals(stats.getUsageInfo().last().getTimestamp(), stats.getEndDate());
        assertEquals(10, stats.getSequenceNumber());
        assertMixedStatistics(stats);
    }

    @Test
    public void testStatisticsCopyAll() throws Exception {
        long now = System.currentTimeMillis();
        Statistics source = initMixedStatistics(now);
        assertMixedStatistics(source);
        Statistics target = new Statistics(source, source.getStartDate(), source.getEndDate());
        assertMixedStatistics(target);
        assertEquals(now, target.getStartupTime());
        assertEquals(source.getStartDate(), target.getStartDate());
        assertEquals(source.getEndDate(), target.getEndDate());
        assertEquals(10, target.getSequenceNumber());
    }

    @Test
    public void testStatisticsCopySelected() throws Exception {
        long now = System.currentTimeMillis();
        Statistics source = initMixedStatistics(now);
        assertMixedStatistics(source);
        long startDate = source.getSearchInfo().first().getTimestamp(); // copy from first SearchInfo entry
        long endDate = source.getResponseTimeInfo().first().getTimestamp(); // to ResponseTimeInfo entry
        Statistics target = new Statistics(source, startDate, endDate);
        assertEquals(now, target.getStartupTime());
        assertEquals(startDate, target.getStartDate());
        assertEquals(endDate, target.getEndDate());

        // note: only 6 entries are actually copied, but
        // sequenceNumber of ResponseTimeInfo entry was 7,
        // so the new global sequence number becomes 8
        assertEquals(8, target.getSequenceNumber());
    }

    @Test
    public void testClearAll() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = initMixedStatistics(now);
        stats.clear();
        assertEquals(now, stats.getStartupTime());
        assertEquals(now, stats.getStartDate());
        assertEquals(0, stats.getSequenceNumber());
    }

    @Test
    public void testClearRange() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = initMixedStatistics(now);
        long startDate = stats.getSearchInfo().first().getTimestamp(); // delete from first SearchInfo entry
        long endDate = stats.getResponseTimeInfo().first().getTimestamp(); // to ResponseTimeInfo entry
        stats.clear(startDate, endDate);
        assertEquals(stats.getUserInfo().first().getTimestamp(), stats.getStartDate());
        assertEquals(stats.getUsageInfo().last().getTimestamp(), stats.getEndDate());
        assertEquals(10, stats.getSequenceNumber());
    }

    @Test
    public void testClearHead() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = initMixedStatistics(now);
        long startDate = stats.getStartDate(); // delete from start date
        long endDate = stats.getSearchInfo().first().getTimestamp(); // to first SearchInfo entry
        stats.clear(startDate, endDate);
        assertEquals(stats.getRefererInfo().first().getTimestamp(), stats.getStartDate());
        assertEquals(stats.getUsageInfo().last().getTimestamp(), stats.getEndDate());
        assertEquals(10, stats.getSequenceNumber());
    }

    @Test
    public void testClearTail() throws Exception {
        long now = System.currentTimeMillis();
        Statistics stats = initMixedStatistics(now);
        long startDate = stats.getSearchInfo().first().getTimestamp(); // delete from first SearchInfo entry
        long endDate = stats.getEndDate(); // to end of dataset
        stats.clear(startDate, endDate);

        assertEquals(stats.getUserInfo().first().getTimestamp(), stats.getStartDate());

        // dataset now ends with first UsageInfo!
        assertEquals(stats.getUsageInfo().first().getTimestamp(), stats.getEndDate());
        assertEquals(2, stats.getSequenceNumber());
    }

    @Test
    public void testStatisticsRestore() throws Exception {
        long now = System.currentTimeMillis();
        Statistics source = initMixedStatistics(now);
        assertMixedStatistics(source);
        Statistics target = new Statistics(now + 1000L, 0, 0, 0);
        target.restore(source);
        assertEquals(now + 1000L, target.getStartupTime());
        assertEquals(source.getStartDate(), target.getStartDate());
        assertEquals(source.getEndDate(), target.getEndDate());
        assertEquals(source.getSequenceNumber(), target.getSequenceNumber());
        assertMixedStatistics(target);
    }

    // Thread.sleep() between tracking calls ensure that
    // entries have the different timestamps
    private Statistics initMixedStatistics(long now) throws Exception {
        Statistics stats = new Statistics(now, 0, 0, 0);
        stats.trackUser("homer", "A", "X");
        Thread.sleep(10L);
        stats.trackUsage("marge", "/projects/b", "z");
        Thread.sleep(5L);
        stats.trackSearch("homer", "queryA", 25, 1234L);
        Thread.sleep(3L);
        stats.trackReferer("marge", null);
        Thread.sleep(27L);
        stats.trackUsage("marge", "/projects", "x");
        Thread.sleep(3L);
        stats.trackUser("bart", "B", "Y");
        Thread.sleep(1L);
        stats.trackBrowser("bart", "UserAgent1");
        Thread.sleep(10L);
        stats.trackResponseTime("marge", "/favorites", 2837L);
        Thread.sleep(6L);
        stats.trackSearch("marge", "queryB", 2, 344L);
        Thread.sleep(8L);
        stats.trackUsage("homer", "/projects", "y");
        return stats;
    }

    private void assertMixedStatistics(Statistics stats) {
        SortedSet<UsageInfo> usageInfo = stats.getUsageInfo();
        assertEquals(3, usageInfo.size());
        SortedSet<UserInfo> userInfo = stats.getUserInfo();
        assertEquals(2, userInfo.size());
        assertEquals(0, userInfo.first().getSequenceNumber());
        assertEquals(5, userInfo.last().getSequenceNumber());
        SortedSet<BrowserInfo> browserInfo = stats.getBrowserInfo();
        assertEquals(1, browserInfo.size());
        assertEquals(6, browserInfo.first().getSequenceNumber());
        SortedSet<SearchInfo> searchInfo = stats.getSearchInfo();
        assertEquals(2, searchInfo.size());
        assertEquals(2, searchInfo.first().getSequenceNumber());
        assertEquals(8, searchInfo.last().getSequenceNumber());
        SortedSet<RefererInfo> referInfo = stats.getRefererInfo();
        assertEquals(1, referInfo.size());
        assertEquals(3, referInfo.first().getSequenceNumber());
        SortedSet<ResponseTimeInfo> responseInfo = stats.getResponseTimeInfo();
        assertEquals(1, responseInfo.size());
        assertEquals(7, responseInfo.first().getSequenceNumber());

        assertEquals(userInfo.first().getTimestamp(), stats.getStartDate());
        assertEquals(usageInfo.last().getTimestamp(), stats.getEndDate());
        assertEquals(usageInfo.last().getSequenceNumber() + 1, stats.getSequenceNumber());
    }

    private <T extends StatisticsInfo> void assertInfoSet(Statistics stats, SortedSet<T> info) {
        assertEquals(info.size(), stats.getSequenceNumber());
        assertEquals(info.last().getSequenceNumber() + 1, stats.getSequenceNumber());
        assertEquals(info.first().getTimestamp(), stats.getStartDate());
        assertEquals(info.last().getTimestamp(), stats.getEndDate());
        int i = 0;
        for (T next: info) {
            assertEquals(i, next.getSequenceNumber());
            ++i;
        }
    }
}
