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
package org.eclipse.skalli.core.internal.tasks;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.api.java.tasks.RunnableSchedule;
import org.eclipse.skalli.api.java.tasks.SchedulerService;
import org.eclipse.skalli.api.java.tasks.Task;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$

    // thread pool for single shot tasks
    private ExecutorService singleShotExecutor = Executors.newCachedThreadPool();

    // thread pool for periodic and/or delayed tasks
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private Map<UUID, Future<?>> futures = new HashMap<UUID, Future<?>>();

    /** Schedules currently registered with this service */
    private Map<UUID, RunnableSchedule> schedules = new HashMap<UUID, RunnableSchedule>();

    /** Activates this service and starts the runner for recurring tasks. */
    protected void activate(ComponentContext context) {
        // starts the cron task (every minute with no initial delay)
        registerCron(1, TimeUnit.MINUTES);
        // starts a cleanup task for done single shot tasks (every 12 hours with an initial delay of 12 hours)
        registerCleanupDoneTasksRunner(12, TimeUnit.HOURS);
        LOG.info("Scheduler Service activated"); //$NON-NLS-1$
    }

    /** Deactivates this service and cancels all scheduled tasks. */
    protected void deactivate(ComponentContext context) {
        for (Future<?> future : futures.values()) {
            future.cancel(true);
        }
        futures.clear();
        scheduler.shutdownNow();
        singleShotExecutor.shutdownNow();
        LOG.info("Scheduler Service deactivated"); //$NON-NLS-1$
    }

    @Override
    public synchronized UUID registerTask(Task task) {
        UUID taskId = UUID.randomUUID();
        Runnable runnable = task.getRunnable();
        Future<?> future = null;
        if (task.isOneShot()) {
            future = singleShotExecutor.submit(runnable);
            LOG.info(MessageFormat.format("Task id=''{0}'' {1}: submitted", taskId, task));
        } else {
            future = scheduler.scheduleAtFixedRate(runnable, task.getInitialDelay(), task.getPeriod(),
                    TimeUnit.MILLISECONDS);
            LOG.info(MessageFormat.format("Task id=''{0}'' {1}: registered", taskId, task));
        }
        futures.put(taskId, future);
        return taskId;
    }

    @Override
    public synchronized void unregisterTask(UUID taskId) {
        Future<?> future = futures.get(taskId);
        if (future != null) {
            future.cancel(true);
            futures.remove(taskId);
            LOG.info(MessageFormat.format("Task id=''{0}'': unregistered", taskId));
        }
    }

    @Override
    public synchronized boolean isRegisteredTask(UUID taskId) {
        return futures.get(taskId) != null;
    }

    @Override
    public synchronized boolean cancel(UUID taskId, boolean hard) {
        Future<?> future = futures.get(taskId);
        if (future != null) {
            return future.cancel(hard);
        }
        return false;
    }

    @Override
    public synchronized boolean isDone(UUID taskId) {
        Future<?> future = futures.get(taskId);
        return future != null ? future.isDone() : false;
    }

    @Override
    public synchronized UUID registerSchedule(RunnableSchedule schedule) {
        UUID scheduleId = UUID.randomUUID();
        schedules.put(scheduleId, schedule);
        LOG.info(MessageFormat.format("Schedule id=''{0}'' {1}: registered", scheduleId, schedule));
        return scheduleId;
    }

    @Override
    public synchronized RunnableSchedule unregisterSchedule(UUID scheduleId) {
        RunnableSchedule schedule = schedules.get(scheduleId);
        if (schedule != null) {
            schedules.remove(scheduleId);
        }
        LOG.info(MessageFormat.format("Schedule id=''{0}'': unregistered", scheduleId));
        return schedule;
    }

    @Override
    public synchronized boolean isRegisteredSchedule(UUID scheduleId) {
        return schedules.get(scheduleId) != null;
    }

    /**
     * Registers a cron runner that executes registered schedules.
     */
    void registerCron(long period, TimeUnit unit) {
        registerTask(new Task(new CronRunner(), 0, unit.toMillis(period)));
    }

    /**
     * Registers a cleanup task for done single shot tasks.
     */
    void registerCleanupDoneTasksRunner(long period, TimeUnit unit) {
        registerTask(new Task(new CleanupDoneTasksRunner(), unit.toMillis(period), unit.toMillis(period)));
    }

    /**
     * Runnable that is executed periodically asking all registered
     * schedules, if there are any due task. Starts due tasks as one-shot actions.
     */
    private class CronRunner implements Runnable {
        @Override
        public void run() {
            Calendar now = new GregorianCalendar(TIMEZONE, Locale.ENGLISH);
            for (RunnableSchedule schedule : schedules.values()) {
                if (schedule.isDue(now)) {
                    Runnable runnable = schedule.getRunnable();
                    if (runnable != null) {
                        singleShotExecutor.submit(runnable);
                    }
                }
            }
        }
    }

    private class CleanupDoneTasksRunner implements Runnable {
        @Override
        public void run() {
            for (UUID taskId : futures.keySet()) {
                Future<?> future = futures.get(taskId);
                if (future.isDone()) {
                    futures.remove(taskId);
                    LOG.info(MessageFormat.format("Task id=''{0}'': done", taskId));
                }
            }
        }
    }
}
