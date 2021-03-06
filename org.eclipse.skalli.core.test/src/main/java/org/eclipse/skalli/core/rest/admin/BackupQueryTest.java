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
package org.eclipse.skalli.core.rest.admin;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.testutil.AssertUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class BackupQueryTest {

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
        BackupQuery query = new BackupQuery(getParams(null, null, period), now);
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
        BackupQuery query = new BackupQuery(getParams(fromStr, null, null), now);
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
        BackupQuery query = new BackupQuery(getParams(null, toStr, null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());
    }

    @Test
    public void testToPeriodQuery() throws Exception {
        assertToPeriodQuery("3m", 3, TimeUnit.MINUTES);
        assertToPeriodQuery("-3m", 3, TimeUnit.MINUTES);
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
        BackupQuery query = new BackupQuery(getParams(fromStr, toStr, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());

        // period is ignored, if both from and to are specified
        query = new BackupQuery(getParams(fromStr, toStr, "3d"), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());

        // from == to
        query = new BackupQuery(getParams(fromStr, fromStr, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(fromMillis, query.getTo());

        // from > to
        query = new BackupQuery(getParams(toStr, fromStr, null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(fromMillis, query.getTo());
    }

    @Test
    public void testFromToIntervals() throws Exception {
        long now = System.currentTimeMillis();
        BackupQuery query = new BackupQuery(getParams("-1d", null, null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new BackupQuery(getParams(null, "-1d", null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        query = new BackupQuery(getParams("-2d", "-1d", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        query = new BackupQuery(getParams("-1d", null, "1h"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS) +
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), query.getTo());

        query = new BackupQuery(getParams(null, "-1d", "1h"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS) -
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        // from + period > now => to = now
        query = new BackupQuery(getParams("-1d", null, "2d"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        // period is ignored, if from and to are specified
        query = new BackupQuery(getParams("-2d", "-1d", "1h"), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        // positive to interval is treated as not specified => to = now
        query = new BackupQuery(getParams("-1d", "4711d", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        // positive from interval is treated as not specified => from = 0
        query = new BackupQuery(getParams("4711d", "-1d", null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getTo());

        // positive from interval are treated as not specified => show 1 day
        query = new BackupQuery(getParams("4711d", "4711d", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), query.getFrom());
        Assert.assertEquals(now, query.getTo());
    }

    @Test
    public void testFromToNow() throws Exception {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();

        BackupQuery query = new BackupQuery(getParams(null, "now", null), now);
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new BackupQuery(getParams("-1h", "now", null), now);
        Assert.assertEquals(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), query.getFrom());
        Assert.assertEquals(now, query.getTo());

        long fromMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(fromMillis);
        String fromStr = DatatypeConverter.printDateTime(cal);
        query = new BackupQuery(getParams(fromStr, "now", null), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new BackupQuery(getParams("now", null, null), now);
        Assert.assertEquals(now, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        query = new BackupQuery(getParams("now", "now", null), now);
        Assert.assertEquals(now, query.getFrom());
        Assert.assertEquals(now, query.getTo());

        // period is ignored, if from and to are specified
        query = new BackupQuery(getParams("now", "now", "1d"), now);
        Assert.assertEquals(now, query.getFrom());
        Assert.assertEquals(now, query.getTo());
    }

    @Test
    public void testFromPeriodQuery() throws Exception {
        assertFromPeriodQuery("3m", 3, TimeUnit.MINUTES);
        assertFromPeriodQuery("2d", 1, TimeUnit.DAYS); // to must not be in the future
        assertFromPeriodQuery("-3m", 3, TimeUnit.MINUTES);
    }

    @Test
    public void testAllQuery() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("all", null);
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        Assert.assertEquals(0, query.getFrom());
        Assert.assertEquals(0, query.getTo());
    }

    @Test
    public void testSection() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("include", "a,b");
        params.put("exclude", "c");
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        query.setSection("foobar");
        Assert.assertTrue(query.showSection("foobar"));
        Assert.assertFalse(query.showSection("a"));
        Assert.assertFalse(query.showSection("b"));
        Assert.assertFalse(query.showSection("c"));
    }

    @Test
    public void testNoFilters() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        Assert.assertTrue(query.showByFilter("byDate"));
    }

    @Test
    public void testSetFilter() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        query.setFilter("byDate");
        Assert.assertTrue(query.showByFilter("byDate"));
        Assert.assertFalse(query.showByFilter("foobar"));
    }

    @Test
    public void testFiltersAttribute() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("filters", "byDate,byRole");
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        Assert.assertTrue(query.showByFilter("byDate"));
        Assert.assertTrue(query.showByFilter("byRole"));
        Assert.assertFalse(query.showByFilter("foobar"));
    }

    @Test
    public void testSetFilterOverwritesFiltersAttribute() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("filters", "byDate,byRole");
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        query.setFilter("byDate");
        Assert.assertTrue(query.showByFilter("byDate"));
        Assert.assertFalse(query.showByFilter("byRole"));
        Assert.assertFalse(query.showByFilter("foobar"));
    }

    @Test
    public void testSummaryFilter() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("filters", "byDate,byRole,summary");
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        Assert.assertFalse(query.showByFilter("byDate"));
        Assert.assertFalse(query.showByFilter("byRole"));
        Assert.assertTrue(query.showByFilter("summary"));
    }

    @Test
    public void testShowSection() throws Exception {
        assertShow(new String[]{"a", "b", "c"}, new String[]{"foo"}, "a,b,c", null);
        assertShow(new String[]{"a;b;c"}, new String[]{"a", "b", "c", "foo"}, "a;b;c", null);
        assertShow(new String[]{"a", "b", "c"}, new String[]{"a;b;c"}, null, "a;b;c");
        assertShow(new String[]{"a", "b", "c"}, new String[]{"foo"}, "a,b,c", "");
        assertShow(new String[]{"foo", "bar"}, new String[]{"a", "b", "c"}, null, "a,b,c");
        assertShow(new String[]{"foo", "bar"}, new String[]{"a", "b", "c"}, "", "a,b,c");
        assertShow(new String[]{"foo", "bar"}, new String[]{"a", "b", "c", "foobar"}, "foo,bar", "a,b,c");
        assertShow(new String[]{"a", "b", "c"}, new String[0], null, null);
        assertShow(new String[]{"a", "b", "c"}, new String[0], "", "");
    }

    @Test
    public void testIncluded() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("include", "a,b,c");
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        AssertUtils.assertEqualsAnyOrder("includes", CollectionUtils.asSet("a", "b", "c"), query.getIncluded());
    }

    @Test
    public void testExcluded() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("exclude", "foo,bar");
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        AssertUtils.assertEqualsAnyOrder("includes", CollectionUtils.asSet("foo", "bar"), query.getExcluded());
    }

    private void assertShow(String[] included, String[] excluded, String include, String exclude) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("include", include);
        params.put("exclude", exclude);
        BackupQuery query = new BackupQuery(params, System.currentTimeMillis());
        for (String s: included) {
            Assert.assertTrue(query.showSection(s));
        }
        for (String s: excluded) {
            Assert.assertFalse(query.showSection(s));
        }
    }

    private void assertFromPeriodQuery(String period, int value, TimeUnit unit) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long fromMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(fromMillis);
        String fromStr = DatatypeConverter.printDateTime(cal);
        BackupQuery query = new BackupQuery(getParams(fromStr, null, period), now);
        Assert.assertEquals(fromMillis, query.getFrom());
        Assert.assertEquals(fromMillis + TimeUnit.MILLISECONDS.convert(value, unit), query.getTo());
    }

    private void assertToPeriodQuery(String period, int value, TimeUnit unit) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long toMillis = now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        cal.setTimeInMillis(toMillis);
        String toStr = DatatypeConverter.printDateTime(cal);
        BackupQuery query = new BackupQuery(getParams(null, toStr, period), now);
        Assert.assertEquals(toMillis - TimeUnit.MILLISECONDS.convert(value, unit), query.getFrom());
        Assert.assertEquals(toMillis, query.getTo());
    }

    private Map<String, String> getParams(String from, String to, String period) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (StringUtils.isNotBlank(from)) {
            params.put(BackupQuery.PARAM_FROM, from);
        }
        if (StringUtils.isNotBlank(to)) {
            params.put(BackupQuery.PARAM_TO, to);
        }
        if (StringUtils.isNotBlank(period)) {
            params.put(BackupQuery.PARAM_PERIOD, period);
        }
        return params;
    }
}
