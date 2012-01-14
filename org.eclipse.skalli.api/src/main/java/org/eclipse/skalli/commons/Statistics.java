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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

public class Statistics {

    private static class UserInfo {
        private final String browser;
        private final String department;
        private final String location;
        private final String userId;

        public UserInfo(String userId, String browser, String department, String location) {
            super();
            this.userId = userId;
            this.browser = browser;
            this.department = department;
            this.location = location;
        }

        public String getDepartment() {
            return department;
        }

        public String getLocation() {
            return location;
        }

        public String getUserId() {
            return userId;
        }

    }

    private static final Map<String, Long> hits = new HashMap<String, Long>();
    private static final Map<String, Long> referers = new HashMap<String, Long>();
    private static final Map<String, UserInfo> browsers = new HashMap<String, UserInfo>();
    private static final Map<String, Long> searches = new HashMap<String, Long>();
    private long startTimestamp;

    private Statistics() {
        startTimestamp = new Date().getTime();
    }

    private static Statistics instance = null;

    public static synchronized Statistics getDefault() {
        if (instance == null) {
            instance = new Statistics();
        }
        return instance;
    }

    private String hash(String userId) {
        if (userId != null) {
            return DigestUtils.shaHex(userId);
        } else {
            return "null"; //$NON-NLS-1$
        }
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public synchronized void trackUsage(String key) {
        Long value = hits.get(key);
        if (value == null) {
            value = 0L;
        }
        value += 1;
        hits.put(key, value);
    }

    public synchronized void trackBrowser(String userId, String browser, String department,
            String location) {
        browsers.put(hash(userId), new UserInfo(userId, browser, department, location));
    }

    public synchronized void trackSearch(String searchQuery) {
        Long value = searches.get(searchQuery);
        if (value == null) {
            value = 0L;
        }
        value += 1;
        searches.put(searchQuery, value);
    }

    public synchronized void trackReferer(String referer) {
        Long value = referers.get(referer);
        if (value == null) {
            value = 0L;
        }
        value += 1;
        referers.put(referer, value);
    }

    public Map<String, Long> getHits() {
        return Collections.unmodifiableMap(hits);
    }

    public long getUniqueUserCount() {
        return browsers.size();
    }

    public Set<String> getUsers() {
        Set<String> ret = new HashSet<String>();
        for (UserInfo userInfo : browsers.values()) {
            ret.add(userInfo.getUserId());
        }
        return ret;
    }

    public Map<String, Integer> getDepartments() {
        Map<String, Integer> ret = new HashMap<String, Integer>();
        for (UserInfo userInfo : browsers.values()) {
            Integer current = ret.get(userInfo.getDepartment());
            if (current == null) {
                current = 0;
            }
            current++;
            ret.put(userInfo.getDepartment(), current);
        }
        return ret;
    }

    public Map<String, Integer> getLocations() {
        Map<String, Integer> ret = new HashMap<String, Integer>();
        for (UserInfo userInfo : browsers.values()) {
            Integer current = ret.get(userInfo.getLocation());
            if (current == null) {
                current = 0;
            }
            current++;
            ret.put(userInfo.getLocation(), current);
        }
        return ret;
    }

    public Map<String, Long> getBrowserCount() {
        Map<String, Long> ret = new HashMap<String, Long>();
        for (UserInfo userInfo : browsers.values()) {
            Long value = ret.get(userInfo.browser);
            if (value == null) {
                value = 0L;
            }
            value += 1;
            ret.put(userInfo.browser, value);
        }
        return ret;
    }

    public Map<String, Long> getRefererCount() {
        return Collections.unmodifiableMap(referers);
    }

    public Map<String, Long> getSearches() {
        return searches;
    }

}
