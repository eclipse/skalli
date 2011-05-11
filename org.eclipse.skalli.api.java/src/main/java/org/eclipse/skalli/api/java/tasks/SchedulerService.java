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
package org.eclipse.skalli.api.java.tasks;

import java.util.UUID;

public interface SchedulerService {

    /**
     * Registers the given task.
     *
     * @param task  the task to register.
     * @return a unique identifier for the registration.
     */
    public UUID registerTask(Task task);

    /**
     * Unregisters and stops a given task.
     *
     * @param taskId  the task to unregister (and stop).
     */
    public void unregisterTask(UUID taskId);

    /**
     * Returns <code>true</code> if the given task is registered.
     *
     * @param taskId  the task to check.
     */
    public boolean isRegisteredTask(UUID taskId);

    /**
     * Attempts to cancel execution of a given task.
     * This attempt will fail if the task has already completed, has already
     * been cancelled, could not be cancelled for some other reason, or the given
     * task is not registered. If the task has not started yet, it should never run.
     *
     * @param taskId  the task to cancel.
     * @param hard  if <code>true</code>, the thread running the task is interrupted
     * in an attempt to stop the task.
     * @return <tt>false</tt> if the task could not be cancelled, typically because it has
     * already completed normally, or it is not registered at all.
     */
    public boolean cancel(UUID taskId, boolean hard);

    /**
     * Returns <tt>true</tt> if this task completed.
     *
     * Completion may be due to normal termination, an exception, or cancellation.
     *
     * @param taskId  the task to check.
     */
    public boolean isDone(UUID taskId);

    /**
     * Registers a schedule for a recurring task based on
     * a cron-like specifiation (day of week, hour, minute).
     *
     * @param schedule  the schedule describing the recurring task.
     * @return a unique identifier for the schedule registered.
     */
    public UUID registerSchedule(RunnableSchedule schedule);

    /**
     * Unregisters the schedule with the given <code>scheduleId</code> and returns the
     * previously registered schedule.
     *
     * @param scheduleId
     *      the unique identifier of the schedule as returned
     *      by {@link #registerSchedule(RunnableSchedule)}.
     *
     * @return the previously registered <code>RunnableSchedule</code> instance,
     *      or <code>null</code> if there was no schedule registered for the given identifier.
     */
    public RunnableSchedule unregisterSchedule(UUID scheduleId);

    /**
     * Returns <code>true</code> if the given schedule is registered.
     *
     * @param scheduleId
     *      the unique identifier of the schedule as returned
     *      by {@link #registerSchedule(RunnableSchedule)}.
     */
    public boolean isRegisteredSchedule(UUID scheduleId);
}
