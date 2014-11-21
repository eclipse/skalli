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
package org.eclipse.skalli.services.scheduler;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.skalli.services.scheduler.Schedule.SortedIntSet;
import org.junit.Test;

@SuppressWarnings("nls")
public class ScheduleTest {

    @Test
    public void testBasics() {
        Schedule schedule = new Schedule("*", "*", "*");
        assertEquals("*", schedule.getDaysOfWeek());
        schedule.setDaysOfWeek(Schedule.WEEKDAYS[0]);
        assertEquals(Schedule.WEEKDAYS[0], schedule.getDaysOfWeek());
        assertEquals("*", schedule.getHours());
        schedule.setHours("0-23");
        assertEquals("0-23", schedule.getHours());
        assertEquals("*", schedule.getMinutes());
        schedule.setMinutes("10-20");
        assertEquals("10-20", schedule.getMinutes());

        Schedule newSchedule = new Schedule(schedule);
        assertEquals(Schedule.WEEKDAYS[0], newSchedule.getDaysOfWeek());
        assertEquals("0-23", newSchedule.getHours());
        assertEquals("10-20", newSchedule.getMinutes());
        assertEquals(Schedule.WEEKDAYS[0] + " 0-23 10-20", newSchedule.toString());

        assertEquals(schedule, newSchedule);

        schedule = new Schedule();
        assertEquals("*", schedule.getDaysOfWeek());
        assertEquals("*", schedule.getHours());
        assertEquals("*", schedule.getMinutes());

        //  simulate an uninitialized Schedule -> XStream
        schedule = new Schedule();
        schedule.setDaysOfWeek(null);
        schedule.setHours(null);
        schedule.setMinutes(null);
        assertEquals("*", schedule.getDaysOfWeek());
        assertEquals("*", schedule.getHours());
        assertEquals("*", schedule.getMinutes());
    }

    @Test
    public void testCreateFromCalendar() throws Exception {
        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        date.set(Calendar.HOUR_OF_DAY, 14);
        date.set(Calendar.MINUTE, 53);
        for (int i = 1; i <= 7; ++i) {
            date.set(Calendar.DAY_OF_WEEK, i);
            Schedule schedule = new Schedule(date);
            // Calendar uses 1=Sunday,2=Monday,..,7=Saturday, while cron uses 0=Sunday,1=Monday,...6=Saturday.
            // So day of week in result is always 1 less than in the Calendar setting!
            assertEquals(Integer.toString(i - 1) + " 14 53", schedule.getSchedule());
        }
    }

    @Test
    public void testScheduleNow() throws Exception {
        Calendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        assertTrue(Schedule.NOW.isDue(now));
    }

    @Test
    public void testSortedIntSet() {
        SortedIntSet set = new SortedIntSet();
        for (int i = 99; i >= 0; --i) {
            set.add(i);
        }
        assertEquals(100, set.size());
        for (int i = 0; i <= 99; ++i) {
            assertEquals(i, set.get(i));
            assertTrue(set.contains(i));
        }
        set.addAll(0, 99, 1);
        assertEquals(100, set.size());
        for (int i = 0; i <= 99; ++i) {
            assertEquals(i, set.get(i));
        }
        set = new SortedIntSet();
        set.addAll(0, 99, 5);
        assertEquals(20, set.size());
    }

    @Test
    public void testGetTimeSet() {
        SortedIntSet list = RunnableSchedule.getTimeSet("8", 0, 23);
        assertEquals(1, list.size());
        assertEquals(8, list.get(0));

        list = RunnableSchedule.getTimeSet("1,2,3", 0, 23);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));

        list = RunnableSchedule.getTimeSet("3,1,2", 0, 23);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));

        list = RunnableSchedule.getTimeSet("1-3", 0, 23);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));

        list = RunnableSchedule.getTimeSet("0-23/2", 0, 23);
        assertEquals(12, list.size());
        int n = 0;
        for (int i = 0; i < 12; ++i) {
            assertEquals(n, list.get(i));
            n += 2;
        }

        list = RunnableSchedule.getTimeSet("4,6-10/2,1-3/1,4,5,5-10/2", 0, 23);
        assertEquals(10, list.size());
        for (int i = 0; i < 10; ++i) {
            assertEquals(i + 1, list.get(i));
        }

        list = RunnableSchedule.getTimeSet("18-5", 0, 23);
        assertEquals(12, list.size());
        n = 0;
        for (int i = 0; i < 23; ++i) {
            if (i <= 5 || i >= 18) {
                assertTrue(list.contains(i));
                assertEquals(i, list.get(n));
                ++n;
            } else {
                assertFalse(list.contains(i));
            }
        }

        list = RunnableSchedule.getTimeSet("*", 0, 6);
        assertEquals(7, list.size());
        for (int i = 0; i <= 6; ++i) {
            assertEquals(i, list.get(i));
        }

        list = RunnableSchedule.getTimeSet("*/2", 0, 6);
        assertEquals(4, list.size());
        n = 0;
        for (int i = 0; i < 4; ++i) {
            assertEquals(n, list.get(i));
            n += 2;
        }

        list = RunnableSchedule.getTimeSet("*/10,3,13-14", 0, 23);
        assertEquals(6, list.size());
        assertEquals(0, list.get(0));
        assertEquals(3, list.get(1));
        assertEquals(10, list.get(2));
        assertEquals(13, list.get(3));
        assertEquals(14, list.get(4));
        assertEquals(20, list.get(5));

        list = RunnableSchedule.getTimeSet("13-14,*/10,3", 0, 23);
        assertEquals(6, list.size());
        assertEquals(0, list.get(0));
        assertEquals(3, list.get(1));
        assertEquals(10, list.get(2));
        assertEquals(13, list.get(3));
        assertEquals(14, list.get(4));
        assertEquals(20, list.get(5));

        // out of range
        list = RunnableSchedule.getTimeSet("24", 0, 23);
        assertEquals(1, list.size());
        assertEquals(23, list.get(0));

        list = RunnableSchedule.getTimeSet("1", 22, 23);
        assertEquals(1, list.size());
        assertEquals(22, list.get(0));

        // invalid
        try {
            list = RunnableSchedule.getTimeSet("foobar", 0, 23);
            fail("foobar");
        } catch (NumberFormatException e) {
        }
        try {
            list = RunnableSchedule.getTimeSet("*/foobar", 0, 23);
            fail("*/foobar");
        } catch (NumberFormatException e) {
        }
        try {
            list = RunnableSchedule.getTimeSet("1-foobar", 0, 23);
            fail("1-foobar");
        } catch (NumberFormatException e) {
        }
        try {
            list = RunnableSchedule.getTimeSet("1,foobar,2", 0, 23);
            fail("1,foobar,2");
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testGetDaysOfWeekSet() {
        SortedIntSet list = RunnableSchedule.getDaysOfWeekSet("MONDAY-SAT/2,5,SUNDAY,7", 0, 7, Schedule.WEEKDAYS,
                Schedule.WEEKDAYS_SHORT);
        assertEquals(0, list.get(0));
        assertEquals(1, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(5, list.get(3));
        assertEquals(7, list.get(4));
        list = RunnableSchedule.normalizeDaysOfWeek(list);
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(4, list.get(2));
        assertEquals(6, list.get(3));

        list = RunnableSchedule.getDaysOfWeekSet("FRI-TUE", 0, 7, Schedule.WEEKDAYS, Schedule.WEEKDAYS_SHORT);
        assertEquals(0, list.get(0));
        assertEquals(1, list.get(1));
        assertEquals(2, list.get(2));
        assertEquals(5, list.get(3));
        assertEquals(6, list.get(4));
        list = RunnableSchedule.normalizeDaysOfWeek(list);
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(6, list.get(3));
        assertEquals(7, list.get(4));

        list = RunnableSchedule.getDaysOfWeekSet("0-7", 0, 7, Schedule.WEEKDAYS, Schedule.WEEKDAYS_SHORT);
        for (int i = 0; i <= 7; ++i) {
            assertEquals(i, list.get(i));
        }
        list = RunnableSchedule.normalizeDaysOfWeek(list);
        for (int i = 0; i <= 6; ++i) {
            assertEquals(i + 1, list.get(i));
        }

        list = RunnableSchedule.getDaysOfWeekSet("*", 0, 7, Schedule.WEEKDAYS, Schedule.WEEKDAYS_SHORT);
        for (int i = 0; i <= 6; ++i) {
            assertEquals(i, list.get(i));
        }
        list = RunnableSchedule.normalizeDaysOfWeek(list);
        for (int i = 0; i <= 6; ++i) {
            assertEquals(i + 1, list.get(i));
        }

        list = RunnableSchedule.getDaysOfWeekSet("*/2", 0, 7, Schedule.WEEKDAYS, Schedule.WEEKDAYS_SHORT);
        assertEquals(0, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(4, list.get(2));
        assertEquals(6, list.get(3));
        list = RunnableSchedule.normalizeDaysOfWeek(list);
        assertEquals(1, list.get(0));
        assertEquals(3, list.get(1));
        assertEquals(5, list.get(2));
        assertEquals(7, list.get(3));

        list = RunnableSchedule.getDaysOfWeekSet("0,7,SUNDAY,*/2,MON,SUN", 0, 7, Schedule.WEEKDAYS,
                Schedule.WEEKDAYS_SHORT);
        assertEquals(0, list.get(0));
        assertEquals(1, list.get(1));
        assertEquals(2, list.get(2));
        assertEquals(4, list.get(3));
        assertEquals(6, list.get(4));
        assertEquals(7, list.get(5));
        list = RunnableSchedule.normalizeDaysOfWeek(list);
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(5, list.get(3));
        assertEquals(7, list.get(4));
    }

    @Test
    public void testIsDue() {
        Schedule schedule = new Schedule("MONDAY", "2", "0"); // Monday 2:00am
        Calendar calendar = getUTCCalendar(2, 2, 0); // 2=Monday
        assertTrue(schedule.isDue(calendar)); // first time we get a runnable
        calendar.add(Calendar.MINUTE, 1);
        assertFalse(schedule.isDue(calendar)); // one minute later, it's gone

        schedule = new Schedule("*", "2", "0"); // Every day at 2:00am
        calendar = getUTCCalendar(1, 2, 0);
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; ++h) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    if (h == 2 && m == 0) {
                        assertTrue("d=" + d, schedule.isDue(calendar));
                    } else {
                        assertFalse("d=" + d, schedule.isDue(calendar));
                    }
                }
            }
        }

        schedule = new Schedule("*/2", "2", "0"); // Every second day at 2:00am
        calendar = getUTCCalendar(1, 2, 0); //Mon 2:00
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; ++h) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    if ((d == 1 || d == 3 || d == 5 || d == 7) && h == 2 && m == 0) {
                        assertTrue("d=" + d, schedule.isDue(calendar));
                    } else {
                        assertFalse("d=" + d, schedule.isDue(calendar));
                    }
                }
            }
        }

        schedule = new Schedule("*", "*", "*"); // Every minute
        calendar = getUTCCalendar(1, 2, 0);
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; ++h) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    assertTrue("m=" + m, schedule.isDue(calendar));
                }
            }
        }

        schedule = new Schedule("*", "*", "*/5"); // Every 5th minute
        calendar = getUTCCalendar(1, 2, 0); //Mon 2:00
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; ++h) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    if (m % 5 == 0) {
                        assertTrue("m=" + m, schedule.isDue(calendar));
                    } else {
                        assertFalse("m=" + m, schedule.isDue(calendar));
                    }
                }
            }
        }

        schedule = new Schedule("*", "*/2", "*/5"); // Every 5th minute of every second hour each day
        calendar = getUTCCalendar(1, 0, 0); //Mon 0:00
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; ++h) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    if ((h % 2 == 0) && (m % 5 == 0)) {
                        assertTrue("h=" + h + ",m=" + m, schedule.isDue(calendar));
                    } else {
                        assertFalse("h=" + h + ",m=" + m, schedule.isDue(calendar));
                    }
                }
            }
        }

        schedule = new Schedule("MON,FRI", "*/2", "*"); // Every minute of every second hour on Monday only
        calendar = getUTCCalendar(1, 0, 0);
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; h += 2) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    if ((h % 2 == 0) && (d == 2 || d == 6)) {
                        assertTrue("d=" + d + ",h=" + h + ",m=" + m, schedule.isDue(calendar));
                    } else {
                        assertFalse("d=" + d + ",h=" + h + ",m=" + m, schedule.isDue(calendar));
                    }
                }
            }
        }

        // Every 10th minute (plus at xx:03, xx:13 and xx:14) at even hours (except 06:xx, plus 15:xx) from Monday-Friday and Sunday
        schedule = new Schedule("7,MON-FRIDAY", "0,2,4,8-12/2,14,15,16-23/2,", "*/10,3,13-14");
        calendar = getUTCCalendar(1, 0, 0);
        for (int d = 1; d <= 7; ++d) {
            for (int h = 0; h <= 23; ++h) {
                for (int m = 0; m <= 59; ++m) {
                    calendar.set(Calendar.DAY_OF_WEEK, d);
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, m);
                    if ((h % 2d == 0 && h != 6 || h == 15) && (m % 10 == 0 || m == 3 || m == 13 || m == 14) && d <= 6) {
                        assertTrue("d=" + d + ",h=" + h + ",m=" + m, schedule.isDue(calendar));
                    } else {
                        assertFalse("d=" + d + ",h=" + h + ",m=" + m, schedule.isDue(calendar));
                    }
                }
            }
        }
    }

    @Test
    public void testIsDueWithLastRunnerDate() {
        Schedule schedule = new Schedule("MONDAY", "2", "10"); // Monday 2:10am
        Calendar now = getUTCCalendar(Calendar.MONDAY, 2, 15); // Monday 2:15am
        Calendar lastRun1= getUTCCalendar(Calendar.MONDAY, 2, 0);  //  2:00am -> last run was before now
        Calendar lastRun2 = getUTCCalendar(Calendar.MONDAY, 2, 10); // 2:10am -> last run was at schedule time
        Calendar lastRun3 = getUTCCalendar(Calendar.MONDAY, 2, 15); // 2:15am -> last run had same time as now
        Calendar lastRun4 = getUTCCalendar(Calendar.MONDAY, 2, 20); // 2:20am -> value after now ; should never be called
        assertFalse(schedule.isDue(null, now));
        assertTrue(schedule.isDue(lastRun1, now));
        assertFalse(schedule.isDue(lastRun2, now));
        assertFalse(schedule.isDue(lastRun3, now));
        assertFalse(schedule.isDue(lastRun4, now));
    }

    private Calendar getUTCCalendar(int dayOfWeek, int hour, int minute) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return calendar;
    }
}
