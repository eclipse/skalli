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

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

/**
 * Defines a schedule for recurring tasks.
 * <p>
 * This class supports a cron-like notation for specifying the day of week, hour and minute
 * when a task should be executed. Note this class uses 24-hour time format and treats Sunday as the
 * first day of the week.
 */
public class Schedule {

    public static final String ASTERISK = "*"; //$NON-NLS-1$

    /** Days of a week, see {@link #Schedule(String, String, String). */
    @SuppressWarnings("nls")
    public static final String[] WEEKDAYS = { "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY",
            "SATURDAY" };

    /** Three-letter abbreviations for the days of a week, see {@link #Schedule(String, String, String). */
    @SuppressWarnings("nls")
    public static final String[] WEEKDAYS_SHORT = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

    private String daysOfWeek;
    private String hours;
    private String minutes;

    // transient so that Schedule can be XStreamed
    private transient SortedIntSet daysOfWeekSet;
    private transient SortedIntSet hoursSet;
    private transient SortedIntSet minutesSet;

    /**
     * Creates a schedule for the current day of week and time.
     * {@link #isDue(Calendar)} returns <code>true</code> immediately when
     * applied to this schedule.
     */
    public static final Schedule NOW =
            new Schedule(new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)); //$NON-NLS-1$

    /**
     * Creates a <tt>"* * *"</tt> schedule, i.e. a schedule that
     * is due every minute.
     */
    public Schedule() {
        this(ASTERISK, ASTERISK, ASTERISK);
    }

    /**
     * Creates a schedule with the day of week, hour of day and minute
     * as specified by the given calendar.
     *
     * @param date  the date at which the schedule is due.
     */
    public Schedule(Calendar date) {
        this(Integer.toString(date.get(Calendar.DAY_OF_WEEK) - 1),
                Integer.toString(date.get(Calendar.HOUR_OF_DAY)),
                Integer.toString(date.get(Calendar.MINUTE)));
    }

    /**
     * Creates a schedule from a given day of week and time.
     * <p>
     * Each of the parameters can be a comma-separated list of ranges, where a range is either
     * a single integer number, or two integer numbers separated by <tt>"-"</tt>. Additionally,
     * <tt>"*"</tt> denotes the range <tt>"first-last"</tt>, where <tt>first</tt> and <tt>last</tt>
     * are the minium and maximum value of a parameter, respectively. For example, <tt>hour="*"</tt>
     * is equivalent to <tt>hour="0-23"</tt>. A range can be followed by <tt>"/n"</tt> with
     * <tt>n</tt> a positive number, which means "every nth ..." (e.g. <tt>hour="0-23/2"</tt> means
     * "every second hour").
     *
     * @param daysOfWeek
     *        the day of the week, denoted as number between 0 and 7 (0=Sunday,1=Monday,...,7=Sunday),
     *        with one of the long names from {@link #WEEKDAYS} or with a short name from
     *        {@link WEEKDAYS_SHORT}. Numbers and names can be mixed.
     * @param hours
     *        the hour of day, denoted as number between 0 and 23.
     * @param minutes
     *        the minute in an hour, denotes as number between 0 and 59.
     */
    public Schedule(String daysOfWeek, String hours, String minutes) {
        setDaysOfWeek(daysOfWeek);
        setHours(hours);
        setMinutes(minutes);
    }

    /**
     * Creates a schedule and copies all data from a given schedule.
     *
     * @param schedule  the schedule to initialize from.
     */
    public Schedule(Schedule schedule) {
        this();
        if (schedule != null) {
            setDaysOfWeek(schedule.getDaysOfWeek());
            setHours(schedule.getHours());
            setMinutes(schedule.getMinutes());
        }
    }

    /**
     * Returns <code>true</code> if the recurring task that this schedule describes
     * is due, i.e. the runnable associated with the task (see {@link #getRunnable()})
     * should be executed <code>now</code>.
     *
     * @param now  the current day of week/hour/minute.
     * @return  <code>true</code>, if the task is due.
     */
    public boolean isDue(Calendar now) {
        return getDaysOfWeekSet().contains(now.get(Calendar.DAY_OF_WEEK))
                && getHoursSet().contains(now.get(Calendar.HOUR_OF_DAY))
                && getMinutesSet().contains(now.get(Calendar.MINUTE));
    }

    /**
     * Returns <code>true</code> if the recurring task that this schedule describes
     * was due somewhen between <code>first</code> and <code>last</code> (including the
     * interval boundaries).
     *
     * @param first  the begin of the interval to check, or <code>null</code>. In that case
     * the method is equivalent to {@link #isDue(Calendar)}.
     * @param last  the end of the interval to check
     *
     * @return  <code>true</code>, if the task is due.
     */
    public boolean isDue(Calendar first, Calendar last) {
        if (isDue(last)) {
            return true;
        }

        if (first == null) {
            return false;
        }

        Calendar i = new GregorianCalendar(first.getTimeZone());
        i.setTime(first.getTime());
        i.add(Calendar.MINUTE, 1);
        while (i.before(last)) {
            if (isDue(i)) {
                return true;
            }
            i.add(Calendar.MINUTE, 1);
        }

        return false;
    }

    /**
     * Returns the minutes setting of the schedule.
     *
     * @return either <tt>"*"</tt> or <tt>"&#42/n"</tt>, or a comma-separated list of ranges.
     *      A range is either a number between 0 and 59 or two numbers separated by <tt>"-"</tt>,
     *      optionally followed by <tt>"/n"</tt> which means: "every n minutes".
     *      Examples: <tt>"&#42/10"</tt> denotes a task scheduled every 10 minutes.
     */
    public String getMinutes() {
        if (StringUtils.isBlank(minutes)) {
            setMinutes(ASTERISK);
        }
        return minutes;
    }

    /**
     * Specifies the minutes setting of the schedule.
     *
     * @param minutes
     *      either <tt>"*"</tt> or <tt>"&#42/n"</tt>, or a comma-separated list of ranges.
     *      A range is either a number between 0 and 59 or two numbers separated by <tt>"-"</tt>,
     *      optionally followed by <tt>"/n"</tt> which means: "every n minutes".
     */
    public void setMinutes(String minutes) {
        this.minutes = StringUtils.isNotBlank(minutes)? minutes : ASTERISK;
        initMinutesSet();
    }

    /**
     * Returns the hours setting of the schedule.
     *
     * @return either <tt>"*"</tt> or <tt>"&#42/n"</tt>, or a comma-separated list of ranges.
     *      A range is either a number between 0 and 23 (24-hour format!) or two numbers separated
     *      by <tt>"-"</tt> optionally followed by <tt>"/n"</tt> which means: "every nth minute".
     *      Examples: <tt>"&#42/10"</tt> denotes a task scheduled every 10 minutes.
     */
    public String getHours() {
        if (StringUtils.isBlank(hours)) {
            setHours(ASTERISK);
        }
        return hours;
    }

    /**
     * Specifies the hours setting of the schedule.
     *
     * @param hours
     *      either <tt>"*"</tt> or <tt>"&#42/n"</tt>, or a comma-separated list of ranges.
     *      A range is either a number between 0 and 23 (24-hour format!) or two numbers separated
     *      by <tt>"-"</tt>, optionally followed by <tt>"/n"</tt> which means: "every nth hour".
     */
    public void setHours(String hours) {
        this.hours = StringUtils.isNotBlank(hours)? hours : ASTERISK;
        initHoursSet();
    }

    /**
     * Returns the days of the week setting of the schedule.
     *
     * @return either <tt>"*"</tt> or <tt>"&#42/n"</tt>, or a comma-separated list of ranges.
     *      A range is either a number between 0 and 7 (0=Sunday,1=Monday,...,7=Sunday), a long name from
     *      {@link #WEEKDAYS}, a short name from {@link WEEKDAYS_SHORT}, or two numbers/names separated
     *      by <tt>"-"</tt> optionally followed by <tt>"/n"</tt> which means: "every nth day".
     *      Numbers and symbolic names can be mixed arbitrarily.
     */
    public String getDaysOfWeek() {
        if (StringUtils.isBlank(daysOfWeek)) {
            setDaysOfWeek(ASTERISK);
        }
        return daysOfWeek;
    }

    /**
     * Specifies the days of the week setting of the schedule.
     *
     * @param  daysOfWeek
     *      either <tt>"*"</tt> or <tt>"&#42/n"</tt>, or a comma-separated list of ranges.
     *      A range is either a number between 0 and 7 (0=Sunday,1=Monday,...,7=Sunday), a long name from
     *      {@link #WEEKDAYS}, a short name from {@link WEEKDAYS_SHORT}, or two numbers/names separated
     *      by <tt>"-"</tt> optionally followed by <tt>"/n"</tt> which means: "every nth day".
     *      Numbers and symbolic names can be mixed arbitrarily.
     */
    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = StringUtils.isNotBlank(daysOfWeek)? daysOfWeek : ASTERISK;
        initDaysOfWeekSet();
    }

    public String getSchedule() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDaysOfWeek()).append(' ');
        sb.append(getHours()).append(' ');
        sb.append(getMinutes());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSchedule();
    }

    @Override
    public int hashCode() {
        int result = 31 + getDaysOfWeek().hashCode();
        result = 31 * result + getHours().hashCode();
        result = 31 * result + getMinutes().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Schedule) {
            Schedule s = (Schedule) obj;
            return Arrays.equals(getDaysOfWeekSet().toArray(), s.getDaysOfWeekSet().toArray())
                    && Arrays.equals(getHoursSet().toArray(), s.getHoursSet().toArray())
                    && Arrays.equals(getMinutesSet().toArray(), s.getMinutesSet().toArray());
        }
        return super.equals(obj);
    }

    private void initDaysOfWeekSet() {
        daysOfWeekSet = normalizeDaysOfWeek(getDaysOfWeekSet(getDaysOfWeek(), 0, 7, Schedule.WEEKDAYS,
                Schedule.WEEKDAYS_SHORT));
    }

    private SortedIntSet getDaysOfWeekSet() {
        if (daysOfWeekSet == null) {
            initDaysOfWeekSet();
        }
        return daysOfWeekSet;
    }

    private void initHoursSet() {
        hoursSet = getTimeSet(getHours(), 0, 23);
    }

    private SortedIntSet getHoursSet() {
        if (hoursSet == null) {
            initHoursSet();
        }
        return hoursSet;
    }

    private void initMinutesSet() {
        minutesSet = getTimeSet(getMinutes(), 0, 59);
    }

    private SortedIntSet getMinutesSet() {
        if (minutesSet == null) {
            initMinutesSet();
        }
        return minutesSet;
    }

    static SortedIntSet getTimeSet(String s, int min, int max) {
        return getIntSet(s, min, max, null, null);
    }

    static SortedIntSet getDaysOfWeekSet(String s, int min, int max, String[] names, String[] shortNames) {
        return getIntSet(s, min, max, names, shortNames);
    }

    private static SortedIntSet getIntSet(String s, int min, int max, String[] names, String[] shortNames) {
        SortedIntSet result = new SortedIntSet();
        String[] ranges = StringUtils.split(s, ',');
        for (String range : ranges) {
            int first = min;
            int last = max;
            int step = 1;
            if (range.startsWith("*/")) { //$NON-NLS-1$
                step = Integer.parseInt(range.substring(2));
            } else if (!"*".equals(range)) { //$NON-NLS-1$
                int m = range.indexOf('/');
                if (m > 0) {
                    step = Integer.parseInt(range.substring(m + 1));
                    range = range.substring(0, m);
                }
                int n = range.indexOf('-');
                String left = n > 0 ? range.substring(0, n) : range;
                String right = n > 0 ? range.substring(n + 1) : range;
                first = indexOf(left, names, shortNames, min);
                last = indexOf(right, names, shortNames, min);
                if (first < 0) {
                    first = Math.min(Math.max(Integer.parseInt(left), min), max);
                }
                if (last < 0) {
                    last = Math.min(Math.max(Integer.parseInt(right), min), max);
                }
            }
            if (first > last) {
                // e.g. FRI-TUE equivalent to FRI-SUN + MON-TUE
                result.addAll(first, max, step);
                result.addAll(min, last, step);
            } else {
                result.addAll(first, last, step);
            }
        }
        return result;
    }

    private static int indexOf(String s, String[] names, String[] shortNames, int min) {
        int i = indexOf(s, names, min);
        return i < 0 ? indexOf(s, shortNames, min) : i;
    }

    private static int indexOf(String s, String[] names, int min) {
        if (names != null) {
            for (int i = 0; i < names.length; ++i) {
                if (s.equalsIgnoreCase(names[i])) {
                    return i + min;
                }
            }
        }
        return -1;
    }

    // Calendar needs 1=Sunday,2=Monday,..,7=Saturday, while cron uses 0=Sunday,1=Monday,...7=Sunday;
    // so we map 7 to 1 and add +1 otherwise
    static SortedIntSet normalizeDaysOfWeek(SortedIntSet daysOfWeek) {
        SortedIntSet result = new SortedIntSet();
        for (int i = 0; i < daysOfWeek.size(); ++i) {
            int dayOfWeek = daysOfWeek.get(i);
            result.add(dayOfWeek == 7 ? 1 : dayOfWeek + 1);
        }
        return result;
    }

    /**
     * Simple implementations of a resizeable, sorted integer set.
     */
    static class SortedIntSet {
        private int[] array = new int[8];
        private int count = 0;

        public void add(int value) {
            if (!contains(value)) {
                if (count == array.length) {
                    int[] newarray = new int[array.length * 2];
                    System.arraycopy(array, 0, newarray, 0, count);
                    array = newarray;
                }
                array[count++] = value;
                Arrays.sort(array, 0, count);
            }
        }

        public void addAll(int first, int last, int step) {
            for (int value = first; value <= last; value += step) {
                add(value);
            }
        }

        public int size() {
            return count;
        }

        public int get(int index) {
            if (index >= count) {
                throw new IndexOutOfBoundsException();
            }
            return array[index];
        }

        public boolean contains(int value) {
            return Arrays.binarySearch(array, 0, count, value) >= 0;
        }

        public int[] toArray() {
            return array;
        }
    }
}
