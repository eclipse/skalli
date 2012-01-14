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

/**
 * Base class for schedules that provide a runnable.
 */
public abstract class RunnableSchedule extends Schedule {

    /**
     * Creates a <code>RunnableSchedule</code> from the given schedule.
     *
     * @param schedule  the schedule to initialize from.
     */
    protected RunnableSchedule(Schedule schedule) {
        super(schedule);
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
        sb.append("'").append(super.toString()).append("'");
        sb.append(" running " + getRunnable().getClass().getName());
        return sb.toString();
    }
}
