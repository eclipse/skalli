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

import org.apache.commons.lang.StringUtils;

/**
 * Base class for schedules that can be executed.
 * Implementations must implement a suitable {@link Runnable#run() run} method
 * according to the following pattern to ensure that the {@link #getLastStarted()}
 * and {@link #getLastCompleted()} timestamps are correctly set:
 * <pre>
 *  public void run() {
 *      try {
 *          setLastStarted(System.currentTimeMillis());
 *          LOG.info("Updating X...");
 *          // actual code goes here
 *          LOG.info("Updating X: Finished");
 *      } catch (Exception e) {
 *          LOG.error("Updating X: Failed", e);
 *      } finally {
 *          setLastCompleted(System.currentTimeMillis());
 *      }
 *  }
 * </pre>
 */
public abstract class RunnableSchedule extends Schedule implements Runnable {

    private final String caption;

    private volatile long lastStarted = -1L;
    private volatile long lastCompleted = -1L;

    /**
     * Creates a <code>RunnableSchedule</code> from the given schedule
     * and defines a caption for the schedule.
     *
     * @param schedule  the schedule to initialize from. If no
     * schedule is spezified, {@link Schedule#Schedule()} is assumed.
     *
     * @param caption  the caption to display for the schedule.
     * If not caption is specified, the name of the schedule class
     * is applied.
     */
    protected RunnableSchedule(Schedule schedule, String caption) {
        super(schedule);
        if (StringUtils.isBlank(caption)) {
            caption = schedule.getClass().getName();
        }
        this.caption = caption;
    }

    /**
     * Returns the caption of the schedule.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the time (in milliseconds since midnight, January 1, 1970 UTC)
     * when excution of the scheduled action has been started last.
     */
    public long getLastStarted() {
        return lastStarted;
    }

    /**
     * Sets the time when excution of the scheduled action has been started last.
     * Derived classes should call this method at the beginning of their run methods
     * and set the timestamp to the current system time.
     *
     * @param  lastStarted  the time (in milliseconds since midnight, January 1, 1970 UTC)
     * when excution of the scheduled action has been started last.
     */
    protected void setLastStarted(long lastStarted) {
        this.lastStarted = lastStarted;
    }

    /**
     * Returns the time (in milliseconds since midnight, January 1, 1970 UTC)
     * when excution of the scheduled action has completed last, or -1 if
     * the execution has not completed yet, or execution was never started.
     */
    public long getLastCompleted() {
        return lastCompleted;
    }

    /**
     * Sets the time when excution of the scheduled action has completed last.
     * Derived classes should call this method at the end of their run methods
     * and set the timestamp to the current system time, preferably in a
     * finally block.
     *
     * @param  lastCompleted  the time (in milliseconds since midnight, January 1, 1970 UTC)
     * when excution of the scheduled action has completed last.
     */
    protected void setLastCompleted(long lastCompleted) {
        this.lastCompleted = lastCompleted;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(caption).append(' ');
        sb.append("'").append(super.toString()).append("'");
        return sb.toString();
    }
}
