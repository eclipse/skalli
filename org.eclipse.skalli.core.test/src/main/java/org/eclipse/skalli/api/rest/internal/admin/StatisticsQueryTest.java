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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class StatisticsQueryTest {

    @Test
    public void testPeriod() throws Exception {
        assertPeriodQuery(null, 1, TimeUnit.DAYS);
        assertPeriodQuery("", 1, TimeUnit.DAYS);
        assertPeriodQuery("0", 1, TimeUnit.DAYS);
        assertPeriodQuery("1d", 1, TimeUnit.DAYS);
        assertPeriodQuery("27D", 27, TimeUnit.DAYS);
        assertPeriodQuery("3", 3, TimeUnit.DAYS);
        assertPeriodQuery("4711", 4711, TimeUnit.DAYS);
        assertPeriodQuery("d", 1, TimeUnit.DAYS);
        assertPeriodQuery("D", 1, TimeUnit.DAYS);
        assertPeriodQuery("5h", 5, TimeUnit.HOURS);
        assertPeriodQuery("15h", 15, TimeUnit.HOURS);
        assertPeriodQuery("h", 1, TimeUnit.HOURS);
        assertPeriodQuery("H", 1, TimeUnit.HOURS);
        assertPeriodQuery("4711m", 4711, TimeUnit.MINUTES);
        assertPeriodQuery("3M", 3, TimeUnit.MINUTES);
        assertPeriodQuery("m", 1, TimeUnit.MINUTES);
        assertPeriodQuery("M", 1, TimeUnit.MINUTES);
    }

    @Test
    public void testNegativePeriod() throws Exception {
        assertPeriodQuery("-1M", 1, TimeUnit.MINUTES);
        assertPeriodQuery("-12h", 12, TimeUnit.HOURS);
        assertPeriodQuery("-1", 1, TimeUnit.DAYS);
        assertPeriodQuery("-4711", 4711, TimeUnit.DAYS);
        assertPeriodQuery("-H", 1, TimeUnit.HOURS);
        assertPeriodQuery("-", 1, TimeUnit.DAYS);
        assertPeriodQuery("-0", 1, TimeUnit.DAYS);
    }

    @Test
    public void testNonNumericalPeriod() throws Exception {
        assertPeriodQuery("hugo", 1, TimeUnit.DAYS);
        assertPeriodQuery("hugod", 1, TimeUnit.DAYS);
        assertPeriodQuery("hugoD", 1, TimeUnit.DAYS);
        assertPeriodQuery("hugom", 1, TimeUnit.MINUTES);
        assertPeriodQuery("hugoM", 1, TimeUnit.MINUTES);
        assertPeriodQuery("hugoh", 1, TimeUnit.HOURS);
        assertPeriodQuery("hugoH", 1, TimeUnit.HOURS);
    }

    private void assertPeriodQuery(String period, int value, TimeUnit unit) {
        long now = System.currentTimeMillis();
        StatisticsQuery query = new StatisticsQuery(getParams(null, null, period), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(value, unit), query.getFrom());
        Assert.assertEquals(now, query.getTo());
    }

    @Test
    public void testFromQuery() throws Exception {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long fromMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(fromMillis);
        String fromStr = DatatypeConverter.printDateTime(cal);
        StatisticsQuery query = new StatisticsQuery(getParams(fromStr, null, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(now, query.getTo());
    }

    @Test
    public void testToQuery() throws Exception {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long toMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(toMillis);
        String toStr = DatatypeConverter.printDateTime(cal);
        StatisticsQuery query = new StatisticsQuery(getParams(null, toStr, null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());
    }

    @Test
    public void testFromToQuery() throws Exception {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long fromMillis = now - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS);
        long toMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(fromMillis);
        String fromStr = DatatypeConverter.printDateTime(cal);
        cal.setTimeInMillis(toMillis);
        String toStr = DatatypeConverter.printDateTime(cal);
        StatisticsQuery query = new StatisticsQuery(getParams(fromStr, toStr, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());

        // period is ignored, if both from and to are specified
        query = new StatisticsQuery(getParams(fromStr, toStr, "3d"), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());

        // from == to
        query = new StatisticsQuery(getParams(fromStr, fromStr, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(fromMillis, query.getTo());

        // from > to
        query = new StatisticsQuery(getParams(toStr, fromStr, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(fromMillis, query.getTo());
    }

    @Test
    public void testFromToIntervals() throws Exception {
        long now = System.currentTimeMillis();
        StatisticsQuery query = new StatisticsQuery(getParams("-1d", null, null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new StatisticsQuery(getParams(null, "-1d", null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        query = new StatisticsQuery(getParams("-2d", "-1d", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        query = new StatisticsQuery(getParams("-1d", null, "1h"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS) +
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), query.getTo());

        query = new StatisticsQuery(getParams(null, "-1d", "1h"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS) -
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        // from + period > now => to = now
        query = new StatisticsQuery(getParams("-1d", null, "2d"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        // period is ignored, if from and to are specified
        query = new StatisticsQuery(getParams("-2d", "-1d", "1h"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        // positive to interval is treated as not specified => to = now
        query = new StatisticsQuery(getParams("-1d", "4711d", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        // positive from interval is treated as not specified => from = 0
        query = new StatisticsQuery(getParams("4711d", "-1d", null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        // positive from interval are treated as not specified => show 1 day
        query = new StatisticsQuery(getParams("4711d", "4711d", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());
    }

    @Test
    public void testFromToNow() throws Exception {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();

        StatisticsQuery query = new StatisticsQuery(getParams(null, "now", null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new StatisticsQuery(getParams("-1h", "now", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        long fromMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(fromMillis);
        String fromStr = DatatypeConverter.printDateTime(cal);
        query = new StatisticsQuery(getParams(fromStr, "now", null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new StatisticsQuery(getParams("now", null, null), now);
        Assert.assertEquals(now, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new StatisticsQuery(getParams("now", "now", null), now);
        Assert.assertEquals(now, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        // period is ignored, if from and to are specified
        query = new StatisticsQuery(getParams("now", "now", "1d"), now);
        Assert.assertEquals(now, query.getFrom());
        Assert.assertEquals(now, query.getTo());
    }

    @Test
    public void testFromPeriodQuery() throws Exception {
        assertFromPeriodQuery("3m", 3, TimeUnit.MINUTES);
        assertFromPeriodQuery("2d", 1, TimeUnit.DAYS); // to must not be in the future
        assertFromPeriodQuery("-3m", 3, TimeUnit.MINUTES);
    }

    private void assertFromPeriodQuery(String period, int value, TimeUnit unit) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long fromMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(fromMillis);
        String fromStr = DatatypeConverter.printDateTime(cal);
        StatisticsQuery query = new StatisticsQuery(getParams(fromStr, null, period), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(fromMillis + TimeUnit.MILLISECONDS.convert(value, unit), query.getTo());
    }

    @Test
    public void testToPeriodQuery() throws Exception {
        assertToPeriodQuery("3m", 3, TimeUnit.MINUTES);
        assertToPeriodQuery("-3m", 3, TimeUnit.MINUTES);
    }

    private void assertToPeriodQuery(String period, int value, TimeUnit unit) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long toMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(toMillis);
        String toStr = DatatypeConverter.printDateTime(cal);
        StatisticsQuery query = new StatisticsQuery(getParams(null, toStr, period), now);
        Assert.assertEquals(toMillis - TimeUnit.MILLISECONDS.convert(value, unit), query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());
    }

    private Map<String, String> getParams(String from, String to, String period) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (StringUtils.isNotBlank(from)) {
            params.put(StatisticsQuery.PARAM_FROM, from);
        }
        if (StringUtils.isNotBlank(to)) {
            params.put(StatisticsQuery.PARAM_TO, to);
        }
        if (StringUtils.isNotBlank(period)) {
            params.put(StatisticsQuery.PARAM_PERIOD, period);
        }
        return params;
    }
}
