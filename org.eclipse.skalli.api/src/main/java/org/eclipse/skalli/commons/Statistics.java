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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

public class Statistics {

    public static class StatisticsInfo implements Comparable<StatisticsInfo> {
        private final String userHash;
        private final long timestamp;
        private final long sequenceNumber;

        public StatisticsInfo(String userId, long sequenceNumber) {
            this.timestamp = System.currentTimeMillis();
            this.sequenceNumber = sequenceNumber;
            if (StringUtils.isNotBlank(userId)) {
                this.userHash = DigestUtils.shaHex(userId);
            } else {
                this.userHash = ANONYMOUS;
            }
        }
        public String getUserHash() {
            return userHash;
        }
        public long getTimestamp() {
            return timestamp;
        }
        public long getSequenceNumber() {
            return sequenceNumber;
        }
        public boolean inRange(long startDate, long endDate) {
            return (startDate <= 0 || startDate <=  timestamp) && (endDate <= 0 || timestamp <= endDate);
        }
        @Override
        public int compareTo(StatisticsInfo o) {
            return Long.signum(sequenceNumber - o.sequenceNumber);
        }
    }

    public static class UserInfo extends StatisticsInfo {
        private final String department;
        private final String location;

        public UserInfo(String userId, String department, String location, long id) {
            super(userId, id);
            this.department = department;
            this.location = location;
        }
        public String getDepartment() {
            return department;
        }
        public String getLocation() {
            return location;
        }
    }

    public static class UsageInfo extends StatisticsInfo {
        private final String path;
        private final String referer;

        public UsageInfo(String userId, String path, String referer, long id) {
            super(userId, id);
            this.path = path;
            this.referer = referer;
        }
        public String getPath() {
            return path;
        }
        public String getReferer() {
            return referer;
        }
    }

    public static class RefererInfo extends StatisticsInfo {
        private final String referer;

        public RefererInfo(String userId, String referer, long id) {
            super(userId, id);
            this.referer = referer;
        }
        public String getReferer() {
            return referer;
        }
    }

    public static class BrowserInfo extends StatisticsInfo {
        private final String userAgent;

        public BrowserInfo(String userId, String userAgent, long id) {
            super(userId, id);
            this.userAgent = userAgent;
        }
        public String getUserAgent() {
            return userAgent;
        }
    }

    public static class SearchInfo extends StatisticsInfo {
        private final String queryString;
        private final int resultCount;
        private final long duration;

        public SearchInfo(String userId, String queryString, int resultCount, long duration, long id) {
            super(userId, id);
            this.queryString = queryString;
            this.resultCount = resultCount;
            this.duration = duration;
        }
        public String getQueryString() {
            return queryString;
        }
        public int getResultCount() {
            return resultCount;
        }
        public long getDuration() {
            return duration;
        }
    }

    public static class ResponseTimeInfo extends StatisticsInfo {
        private final String path;
        private final long responseTime; // in milliseconds

        public ResponseTimeInfo(String userId, String path, long responseTime, long id) {
            super(userId, id);
            this.path = path;
            this.responseTime = responseTime > 0 ? responseTime : 1L;
        }
        public String getPath() {
            return path;
        }
        public long getResponseTime() {
            return responseTime;
        }
    }

    private static final String ANONYMOUS = "anonymous"; //$NON-NLS-1$

    // instance startup timestamp
    private final long startupTime;

    // timestamp of the oldest/newest tracked info entry
    private long startDate;
    private long endDate;

    // sequence number to be assigned to the next tracked info entry
    private long sequenceNumber;

    private SortedSet<UserInfo> users = new ConcurrentSkipListSet<UserInfo>();
    private SortedSet<UsageInfo> usages = new ConcurrentSkipListSet<UsageInfo>();
    private SortedSet<RefererInfo> referers = new ConcurrentSkipListSet<RefererInfo>();
    private SortedSet<BrowserInfo> browsers = new ConcurrentSkipListSet<BrowserInfo>();
    private SortedSet<SearchInfo> searches = new ConcurrentSkipListSet<SearchInfo>();
    private SortedSet<ResponseTimeInfo> responseTimes = new ConcurrentSkipListSet<ResponseTimeInfo>();

    private static Statistics instance = new Statistics();

    /**
     * Creates an empty statistics resource.
     * Initializes the {@link #getStartupTime() startup time} to be the current time.
     */
    public Statistics() {
        this(System.currentTimeMillis(), 0, 0, 0);
    }

    /**
     * Creates a statistics resource from a given statistics resource.
     * Copies the {@link #getStartupTime() startup time} from the given
     * statistics resource.
     *
     * @param statistics  the statistics resource to copy from.
     * @param startDate  begin of the time interval to copy
     *                   in milliseconds since January 1, 1970, 00:00:00 GMT.
     * @param endDate  end of the time interval to copy
     *                 in milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    public Statistics(Statistics statistics, long startDate, long endDate) {
        this(statistics.getStartupTime(), 0, 0, 0);
        copy(statistics, startDate, endDate);
        updateAttributes();
    }

    // package protected for testing purposes
    Statistics(long startupTime, long startDate, long endDate, long sequenceNumber) {
        this.startupTime = startupTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Returns the default statistics resource of this Skalli instance.
     */
    public static Statistics getDefault() {
        return instance;
    }

    /**
     * Returns the startup time of the current Skalli instance,
     * i.e. the begin of the statistics recording.
     *
     * @return the startup time in milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    public long getStartupTime() {
        return startupTime;
    }

    /**
     * Returns the timestamp of the earliest data entry in this statistics resource,
     * or the instance startup time, if no statistics information has been tracked yet.
     *
     * @return the timestamp of the earliest data entry in
     * milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public synchronized long getStartDate() {
        return startDate > 0? startDate : startupTime;
    }

    /**
     * Returns the timestamp of the latest data entry in this statistics resource,
     * or the current time, if no statistics information has been tracked yet.
     *
     * @return the timestamp of the latest data entry in
     * milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public synchronized long getEndDate() {
        return endDate > 0? endDate : System.currentTimeMillis();
    }

    /**
     * Tracks the path and <code>Referer</code> header of a request.
     *
     * @param userId  the user that issued the request, or <code>null</code>,
     * which is interpreted as the Anonymous user.
     * @param path  the resource path that has been requested.
     * @param referer  the <code>Referer</code> header specified in the request,
     * or <code>null</code>.
     */
    public synchronized void trackUsage(String userId, String path, String referer) {
        UsageInfo entry = new UsageInfo(userId, path, referer, sequenceNumber++);
        usages.add(entry);
        endDate = entry.getTimestamp();
        if (startDate == 0) {
            startDate = endDate;
        }
    }

    /**
     * Tracks the user of a request.
     *
     * @param userId  the user that issued the request, or <code>null</code>,
     * which is interpreted as the Anonymous user.
     * @param department  the department the user belongs to, or <code>null</code>.
     * @param location  the location of the user, or <code>null</code>.
     */
    public synchronized void trackUser(String userId, String department, String location) {
        UserInfo entry = new UserInfo(userId, department, location, sequenceNumber++);
        users.add(entry);
        endDate = entry.getTimestamp();
        if (startDate == 0) {
            startDate = endDate;
        }
    }

    /**
     * Tracks the <code>User-Agent</code> header of a request.
     *
     * @param userId  the user that issued the request, or <code>null</code>,
     * which is interpreted as the Anonymous user.
     * @param userAgent  the <code>User-Agent</code> header specified in the request.
     */
    public synchronized void trackBrowser(String userId, String userAgent) {
        BrowserInfo entry = new BrowserInfo(userId, userAgent, sequenceNumber++);
        browsers.add(entry);
        endDate = entry.getTimestamp();
        if (startDate == 0) {
            startDate = endDate;
        }
    }

    /**
     * Tracks a search request.
     *
     * @param userId  the user that performed the search, or <code>null</code>,
     * which is interpreted as the Anonymous user.
     * @param queryString  the query string entered by the user.
     * @param resultCount  the number of search results.
     * @param duration  the amount of time it took to calculate the search
     * result in milliseconds.
     */
    public synchronized void trackSearch(String userId, String queryString, int resultCount, long duration) {
        SearchInfo entry = new SearchInfo(userId, queryString, resultCount, duration, sequenceNumber++);
        searches.add(entry);
        endDate = entry.getTimestamp();
        if (startDate == 0) {
            startDate = endDate;
        }
    }

    /**
     * Tracks the <code>Referer</code> header of a request.
     *
     * @param userId  the user that issued the request, or <code>null</code>,
     * which is interpreted as the Anonymous user.
     * @param referer  the <code>Referer</code> header specified in the request.
     */
    public synchronized void trackReferer(String userId, String referer) {
        RefererInfo entry = new RefererInfo(userId, referer, sequenceNumber++);
        referers.add(entry);
        endDate = entry.getTimestamp();
        if (startDate == 0) {
            startDate = endDate;
        }
    }

    /**
     * Tracks the response time of a request.
     *
     * @param userId  the user that issued the request, or <code>null</code>,
     * which is interpreted as the Anonymous user.
     * @param path
     * @param responseTime
     */
    public synchronized void trackResponseTime(String userId, String path, long responseTime) {
        ResponseTimeInfo entry = new ResponseTimeInfo(userId, path, responseTime, sequenceNumber++);
        responseTimes.add(entry);
        endDate = entry.getTimestamp();
        if (startDate == 0) {
            startDate = endDate;
        }
    }

    /**
     * Removes all data entries from this statistics resource and resets the
     * {@link #getStartDate() start} and {@link #getEndDate() end} date
     * parameters. The {@link #getStartupTime() startup} timestamp
     * is not touched.
     */
    public synchronized void clear() {
        users.clear();
        usages.clear();
        referers.clear();
        browsers.clear();
        searches.clear();
        responseTimes.clear();
        startDate = 0;
        endDate = 0;
        sequenceNumber = 0;
    }

    /**
     * Removes all data entries from this statistics resource with a timestamp
     * within the given range.
     *
     * @param startDate  begin of the time intervall to remove
     *                   in milliseconds since January 1, 1970, 00:00:00 GMT.
     * @param endDate    end of the time intervall to remove
     *                   in milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    public synchronized void clear(long startDate, long endDate) {
        clear(usages, startDate, endDate);
        clear(users, startDate, endDate);
        clear(browsers, startDate, endDate);
        clear(searches, startDate, endDate);
        clear(referers, startDate, endDate);
        clear(responseTimes, startDate, endDate);
        updateAttributes();
    }

    /**
     * Restores the content of this statistics resource from a given statistics resource, e.g. a backup,
     * but retains the {@link #getStartupTime() startup time} of this statistics resource.
     * Removes all previously stored data entries from this statistics resource and copies all data
     * entries from the backup resource.
     *
     * @param statistics  the statistics backup from which to restore this
     * statistics resource. If the argument is <code>null</code> the method
     * does nothing.
     */
    public synchronized void restore(Statistics statistics) {
        if (statistics != null) {
            clear();
            copy(statistics, statistics.getStartDate(), statistics.getEndDate());
            updateAttributes();
        }
    }

    public Map<String, Long> getUserCount(long startDate, long endDate) {
        Map<String, Long> result = new HashMap<String, Long>();
        for (UserInfo userInfo : users) {
            if (userInfo.inRange(startDate, endDate)) {
                Long count = result.get(userInfo.getUserHash());
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(userInfo.getUserHash(), count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public SortedSet<UserInfo> getUserInfo() {
        return Collections.unmodifiableSortedSet(users);
    }

    public Map<String, Long> getDepartmentCount(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        for (UserInfo userInfo : users) {
            String department = userInfo.getDepartment();
            if (userInfo.inRange(startDate, endDate) && StringUtils.isNotBlank(department)) {
                Long count = result.get(department);
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(department, count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public Map<String, Long> getLocationCount(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        for (UserInfo userInfo : users) {
            String location = userInfo.getLocation();
            if (userInfo.inRange(startDate, endDate) && StringUtils.isNotBlank(location)) {
                Long count = result.get(location);
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(location, count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public Map<String, Long> getBrowserCount(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        for (BrowserInfo browserInfo: browsers) {
            if (browserInfo.inRange(startDate, endDate)) {
                Long count = result.get(browserInfo.getUserAgent());
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(browserInfo.getUserAgent(), count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public SortedSet<BrowserInfo> getBrowserInfo() {
        return Collections.unmodifiableSortedSet(browsers);
    }

    public Map<String, Long> getRefererCount(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        for (RefererInfo refererInfo: referers) {
            if (refererInfo.inRange(startDate, endDate)) {
                Long count = result.get(refererInfo.getReferer());
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(refererInfo.getReferer(), count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public SortedSet<RefererInfo> getRefererInfo() {
        return Collections.unmodifiableSortedSet(referers);
    }

    public Map<String, Long> getUsageCount(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        for (UsageInfo usageInfo: usages) {
            if (usageInfo.inRange(startDate, endDate)) {
                Long count = result.get(usageInfo.getPath());
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(usageInfo.getPath(), count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public SortedSet<UsageInfo> getUsageInfo() {
        return Collections.unmodifiableSortedSet(usages);
    }

    public Map<String, Long> getSearchCount(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        for (SearchInfo searchInfo: searches) {
            if (searchInfo.inRange(startDate, endDate)) {
                Long count = result.get(searchInfo.getQueryString());
                if (count == null) {
                    count = 0L;
                }
                ++count;
                result.put(searchInfo.getQueryString(), count);
            }
        }
        return sortedByFrequencyDescending(result);
    }

    public SortedSet<SearchInfo> getSearchInfo() {
        return Collections.unmodifiableSortedSet(searches);
    }

    public Map<String, Long> getAverageResponseTimes(long startDate, long endDate) {
        Map<String, Long> result = new TreeMap<String, Long>();
        Map<String, Integer> counts = new TreeMap<String, Integer>();

        for (ResponseTimeInfo responseTimeInfo: responseTimes) {
            if (responseTimeInfo.inRange(startDate, endDate)) {
                String path = responseTimeInfo.getPath();

                Long sumResponseTime = result.get(path);
                if (sumResponseTime == null) {
                    sumResponseTime = 0L;
                }
                sumResponseTime += responseTimeInfo.getResponseTime();
                result.put(path, sumResponseTime);

                Integer count = counts.get(path);
                if (count == null) {
                    count = 0;
                }
                ++count;
                counts.put(path, count);
            }
        }
        for (Entry<String,Long> entry: result.entrySet()) {
            Integer count = counts.get(entry.getKey());
            entry.setValue(entry.getValue() / count);
        }
        return sortedByFrequencyDescending(result);
    }

    public SortedSet<ResponseTimeInfo> getResponseTimeInfo() {
        return Collections.unmodifiableSortedSet(responseTimes);
    }

    public Map<String, SortedSet<StatisticsInfo>> getUsageTracks(long startDate, long endDate) {
        Map<String, SortedSet<StatisticsInfo>> tracks = new TreeMap<String, SortedSet<StatisticsInfo>>();
        addAll(tracks, usages, startDate, endDate);
        addAll(tracks, searches, startDate, endDate);
        return tracks;
    }

    long getSequenceNumber() {
        return sequenceNumber;
    }

    void addAll(Map<String, SortedSet<StatisticsInfo>> tracks, SortedSet<? extends StatisticsInfo> entries,
            long startDate, long endDate) {
        for (StatisticsInfo info: entries) {
            if (info.inRange(startDate, endDate)) {
                String userHash = info.getUserHash();
                SortedSet<StatisticsInfo> track = tracks.get(userHash);
                if (track == null) {
                    track = new TreeSet<StatisticsInfo>();
                    tracks.put(userHash, track);
                }
                track.add(info);
            }
        }

    }

    void clear(SortedSet<? extends StatisticsInfo> entries, long startDate, long endDate) {
        Iterator<? extends StatisticsInfo> it = entries.iterator();
        while (it.hasNext()) {
            StatisticsInfo next = it.next();
            if (next.inRange(startDate, endDate)) {
                it.remove();
            }
        }
    }

    /**
     * Copies all statistics entries from the given <code>Statistics</code>.
     */
    void copy(Statistics statistics, long startDate, long endDate) {
        copy(statistics.getUserInfo(), users, startDate, endDate);
        copy(statistics.getUsageInfo(), usages, startDate, endDate);
        copy(statistics.getRefererInfo(), referers, startDate, endDate);
        copy(statistics.getBrowserInfo(), browsers, startDate, endDate);
        copy(statistics.getSearchInfo(), searches, startDate, endDate);
        copy(statistics.getResponseTimeInfo(), responseTimes, startDate, endDate);
    }

    <T extends StatisticsInfo> void copy(SortedSet<T> source, SortedSet<T> target,
            long startDate, long endDate) {
        for (T next: source) {
            if (next.inRange(startDate, endDate)) {
                target.add(next);
            }
        }
    }

    /**
     * Recalculates <code>sequenceNumber</code>, <code>startDate</code>
     * and <code>endDate</code> from the entries in this dataset.
     */
    void updateAttributes() {
        sequenceNumber = 0;
        startDate = 0;
        endDate = 0;
        updateAttributes(users);
        updateAttributes(usages);
        updateAttributes(referers);
        updateAttributes(browsers);
        updateAttributes(searches);
        updateAttributes(responseTimes);
    }

    <T extends StatisticsInfo> void updateAttributes(SortedSet<T> source) {
        for (T next: source) {
            sequenceNumber = Math.max(sequenceNumber, next.getSequenceNumber() + 1);
            startDate = startDate > 0 ? Math.min(startDate, next.getTimestamp()) : next.getTimestamp();
            endDate = endDate > 0? Math.max(endDate, next.getTimestamp()) : next.getTimestamp();
        }
    }

    Map<String,Long> sortedByFrequencyDescending(Map<String,Long> map) {
        if (map == null || map.size() <= 1) {
            return map;
        }
        List<Entry<String,Long>> list = new ArrayList<Entry<String,Long>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<String,Long>>() {
            @Override
            public int compare(Entry<String,Long> o1, Entry<String,Long> o2) {
                int result = -Integer.signum(o1.getValue().compareTo(o2.getValue()));
                if (result == 0) {
                    result = o1.getKey().compareTo(o2.getKey());
                }
                return result;
            }
        });
        LinkedHashMap<String,Long> sortedMap = new LinkedHashMap<String,Long>();
        for (Entry<String,Long> entry: list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
