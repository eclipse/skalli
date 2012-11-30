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
package org.eclipse.skalli.api.rest.internal.admin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.CollectionUtils;

public class StatisticsQuery {

    public static final String PARAM_ALL = "all"; //$NON-NLS-1$
    public static final String PARAM_PERIOD = "period"; //$NON-NLS-1$
    public static final String PARAM_FROM = "from"; //$NON-NLS-1$
    public static final String PARAM_TO = "to"; //$NON-NLS-1$
    public static final String PARAM_INCLUDE = "include"; //$NON-NLS-1$
    public static final String PARAM_EXCLUDE = "exclude"; //$NON-NLS-1$

    private String unitSymbols = "dDhHmM"; //$NON-NLS-1$

    private long period = 0;
    private long from = 0;
    private long to = 0;
    private Set<String> included;
    private Set<String> excluded;

    public StatisticsQuery(Map<String, String> params)  {
        this(params, System.currentTimeMillis());
    }

    StatisticsQuery(Map<String, String> params, long now)  {
        if (params.containsKey(PARAM_ALL)) {
            from = 0;
            to = 0;
        } else {
            period = Math.abs(getTimeInterval(params.get(PARAM_PERIOD)));
            from = parseDateTime(params.get(PARAM_FROM), now);
            to = parseDateTime(params.get(PARAM_TO), now);
            if (from <= 0 && to <= 0) {
                to = now;
                from = period != 0 ? to - period : to - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
            } else if (from > 0 && to <= 0) {
                to = period  != 0 ? from + period : now;
            } else if (from <= 0 && to > 0) {
                from = period  != 0 ? to - period : 0;
            } else {
                // both from/to given: ignore period
            }
            from = Math.min(now, from);
            to = Math.min(now, to);
            if (from > to) {
                from = to;
            }
        }

        included =  CollectionUtils.asSet(StringUtils.split(params.get(PARAM_INCLUDE), ','));
        excluded =  CollectionUtils.asSet(StringUtils.split(params.get(PARAM_EXCLUDE), ','));
    }

    private long getTimeInterval(String param) {
        long timeInterval = 0;
        if (StringUtils.isNotBlank(param)) {
            int defaultValue = 0;
            TimeUnit unit = TimeUnit.DAYS;
            char unitSymbol = param.charAt(param.length() - 1);
            if (unitSymbols.indexOf(unitSymbol) >=0 ) {
                if (unitSymbol == 'h' || unitSymbol == 'H') {
                    unit = TimeUnit.HOURS;
                } else if (unitSymbol == 'm' || unitSymbol == 'M') {
                    unit = TimeUnit.MINUTES;
                }
                param = StringUtils.chop(param);
                defaultValue = 1;
            }
            int value = NumberUtils.toInt(param, defaultValue);
            if (value != 0) {
                timeInterval = TimeUnit.MILLISECONDS.convert(value, unit);
            }
        }
        return timeInterval;
    }

    private long parseDateTime(String s, long now) {
        long dateTime = 0;
        if (StringUtils.isNotBlank(s)) {
            try {
                dateTime = DatatypeConverter.parseDateTime(s).getTimeInMillis();
            } catch (IllegalArgumentException e) {
                if ("now".equalsIgnoreCase(s)) { //$NON-NLS-1$
                    dateTime = now;
                } else if (s.startsWith("-")) { //$NON-NLS-1$
                    //check if s is something like "-7d" indicating a time interval
                    long duration = getTimeInterval(s);
                    dateTime = duration < 0 ? now + duration : 0;
                }
            }
        }
        return dateTime;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public void setIncludes(String id) {
        included = CollectionUtils.asSet(id);
        excluded = Collections.emptySet();
    }

    public boolean marshal(String tagName) {
        if (excluded.contains(tagName)) {
            return false;
        }
        if (included.isEmpty()) {
            return true;
        }
        return included.contains(tagName);
    }
}
