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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

public class Statistics {

    public static class StatisticsInfo implements Comparable<StatisticsInfo> {
        private final String userHash;
        private final long timestamp;

        public StatisticsInfo(String userId) {
            this.timestamp = System.currentTimeMillis();
            this.userHash = hash(userId);
        }
        public String getUserHash() {
            return userHash;
        }
        public long getTimestamp() {
            return timestamp;
        }
        public boolean inRange(long startDate, long endDate) {
            return (startDate <= 0 || startDate <=  timestamp) && (endDate <= 0 || timestamp <= endDate);
        }

        @Override
        public int compareTo(StatisticsInfo o) {
            int result = Long.signum(timestamp - o.timestamp);
            if (result == 0) {
                result = userHash.compareTo(o.userHash);
            }
            return result;
        }
    }

    public static class UserInfo extends StatisticsInfo {
        private final String department;
        private final String location;

        public UserInfo(String userId, String department, String location) {
            super(userId);
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

        public UsageInfo(String userId, String path, String referer) {
            super(userId);
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

        public RefererInfo(String userId, String referer) {
            super(userId);
            this.referer = referer;
        }
        public String getReferer() {
            return referer;
        }
    }

    public static class BrowserInfo extends StatisticsInfo {
        private final String userAgent;

        public BrowserInfo(String userId, String userAgent) {
            super(userId);
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

        public SearchInfo(String userId, String queryString, int resultCount, long duration) {
            super(userId);
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

        public ResponseTimeInfo(String userId, String path, long responseTime) {
            super(userId);
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

    public static final String ANONYMOUS = "anonymous"; //$NON-NLS-1$

    private static final SortedSet<UserInfo> users = new TreeSet<UserInfo>();
    private static final SortedSet<UsageInfo> usages = new TreeSet<UsageInfo>();
    private static final SortedSet<RefererInfo> referers = new TreeSet<RefererInfo>();
    private static final SortedSet<BrowserInfo> browsers = new TreeSet<BrowserInfo>();
    private static final SortedSet<SearchInfo> searches = new TreeSet<SearchInfo>();
    private static final SortedSet<ResponseTimeInfo> responseTimes = new TreeSet<ResponseTimeInfo>();

    private long startTimestamp;

    private Statistics() {
        startTimestamp = System.currentTimeMillis();
    }

    private static Statistics instance = null;

    public static synchronized Statistics getDefault() {
        if (instance == null) {
            instance = new Statistics();
        }
        return instance;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public synchronized void trackUsage(String userId, String path, String referer) {
        usages.add(new UsageInfo(userId, path, referer));
    }

    public synchronized void trackUser(String userId, String department, String location) {
        users.add(new UserInfo(userId, department, location));
    }

    public synchronized void trackBrowser(String userId, String userAgent) {
        browsers.add(new BrowserInfo(userId, userAgent));
    }

    public synchronized void trackSearch(String userId, String queryString, int resultCount, long duration) {
        searches.add(new SearchInfo(userId, queryString, resultCount, duration));
    }

    public synchronized void trackReferer(String userId, String referer) {
        referers.add(new RefererInfo(userId, referer));
    }

    public synchronized void trackResponseTime(String userId, String path, long responseTime) {
        responseTimes.add(new ResponseTimeInfo(userId, path, responseTime));
    }

    public synchronized void retain(long startDate, long endDate) {
        retain(usages, startDate, endDate);
        retain(users, startDate, endDate);
        retain(browsers, startDate, endDate);
        retain(searches, startDate, endDate);
        retain(referers, startDate, endDate);
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

    public Map<String, SortedSet<StatisticsInfo>> getUsageTracks(long startDate, long endDate) {
        Map<String, SortedSet<StatisticsInfo>> tracks = new TreeMap<String, SortedSet<StatisticsInfo>>();
        addAll(tracks, usages, startDate, endDate);
        addAll(tracks, searches, startDate, endDate);
        return tracks;
    }

    private void addAll(Map<String, SortedSet<StatisticsInfo>> tracks, SortedSet<? extends StatisticsInfo> entries,
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

    private void retain(SortedSet<? extends StatisticsInfo> entries, long startDate, long endDate) {
        Iterator<? extends StatisticsInfo> it = entries.iterator();
        while (it.hasNext()) {
            StatisticsInfo next = it.next();
            if (!next.inRange(startDate, endDate)) {
                it.remove();
            }
        }
    }

    private Map<String,Long> sortedByFrequencyDescending(Map<String,Long> map) {
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

    private static String hash(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            return DigestUtils.shaHex(userId);
        } else {
            return ANONYMOUS;
        }
    }
}
