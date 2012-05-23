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
package org.eclipse.skalli.services.scheduler;

import org.apache.commons.lang.StringUtils;


/**
 * Base class for schedules that provide a runnable.
 */
public abstract class RunnableSchedule extends Schedule {

    private final String caption;

    private volatile long lastRun = -1L;

    /**
     * Creates a <code>RunnableSchedule</code> from the given schedule
     * and defines a caption for the schedule.
     *
     * @param schedule  the schedule to initialize from.
     * @param caption  the caption to display for the schedule.
     */
    protected RunnableSchedule(Schedule schedule, String caption) {
        super(schedule);
        if (StringUtils.isBlank(caption)) {
            throw new IllegalArgumentException("caption must not be blank");
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
     * Returns the time of the last run.
     * @return the time (in milliseconds) of the last run, or -1
     *         if this schedule has not run yet.
     */
    public long getLastRun() {
        return lastRun;
    }

    /**
     * Sets the time of the last run.
     * @param  lastRun  the time (in milliseconds) of the last run, or -1
     *         if this schedule has not run yet.
     */
    public void setLastRun(long lastRun) {
        this.lastRun = lastRun;
    }

    /**
     * Returns the runnable associated with the task that this schedule describes.
     *
     * @return  a runnable, or <code>null</code>.
     */
    public abstract Runnable getRunnable();

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(caption).append(' ');
        sb.append("'").append(super.toString()).append("'");
        sb.append(" running " + getRunnable().getClass().getName());
        return sb.toString();
    }
}
