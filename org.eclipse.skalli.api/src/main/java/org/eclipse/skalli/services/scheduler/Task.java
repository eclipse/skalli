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

import java.text.MessageFormat;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.skalli.commons.FormatUtils;

public class Task {

    private Runnable runnable;
    private long initialDelay = 0; //immediately
    private long period = -1L; // single-shot

    /**
     * Creates a one-shot <code>Task</code> for a given runnable.
     *
     * @param runnable  the runnable assigned to the task (mandatory).
     */
    public Task(Runnable runnable) {
        this(runnable, 0, -1L);
    }

    /**
     * Creates a <code>Task</code> for a given runnable.
     *
     * @param runnable  the runnable assigned to the task (mandatory).
     * @param initialDelay
     *      the initial delay of the task in milliseconds, or
     *      <code>0</code> if the task should run immediately.
     * @param period
     *      the time between consecutive runs of the task in milliseconds,
     *      or <code>-1L</code> for a single-shot task. Minimum period is 10ms.
     */
    public Task(Runnable runnable, long initialDelay, long period) {
        if (runnable == null) {
            throw new IllegalArgumentException("argument 'runnable' must not be null");
        }
        this.runnable = runnable;
        this.initialDelay = initialDelay >= 0 ? initialDelay : 0;
        if (period < 0) {
            period = -1L;
        } else if (period <= 10) {
            period = 10; // at least 10ms between runs
        }
        this.period = period;
    }

    /**
     * Returns the runnable assigned to the task.
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Returns the initial delay of the task in milliseconds.
     */
    public long getInitialDelay() {
        return initialDelay;
    }

    /**
     * Returns the time between consecutive runs of the task in milliseconds.
     * @return  the task period, or <code>-1L</code> if the task is a single-shot task.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Returns <code>true</code> if the task is a single-shot task.
     */
    public boolean isOneShot() {
        return initialDelay == 0 && period == -1L;
    }

    @Override
    public String toString() {
        String sRunnable = runnable.getClass().getName();
        String sInitialDelay = FormatUtils.formatUTCWithMillis(System.currentTimeMillis() + initialDelay);
        String sPeriod = DurationFormatUtils.formatDurationHMS(period);
        if (isOneShot()) {
            return MessageFormat.format("running {0} now", sRunnable);
        } else if (period == -1L) {
            return MessageFormat.format("running {0} once at {1}", sRunnable, sInitialDelay);
        }
        return MessageFormat.format("running {0} every {1} first at {2}", sRunnable, sPeriod, sInitialDelay);
    }
}
