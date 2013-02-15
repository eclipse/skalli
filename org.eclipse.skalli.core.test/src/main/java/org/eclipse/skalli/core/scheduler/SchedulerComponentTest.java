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
package org.eclipse.skalli.core.scheduler;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.services.scheduler.RunnableSchedule;
import org.eclipse.skalli.services.scheduler.Schedule;
import org.eclipse.skalli.services.scheduler.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class SchedulerComponentTest {

    private static final int TIMEOUT = 10 * 1000;
    private SchedulerComponent instance;

    @Before
    public void setup() {
        instance = new SchedulerComponent();
    }

    @After
    public void tearDown() {
        if (instance != null) {
            instance.deactivate();
        }
    }

    private static class TestRunnable implements Runnable {
        int count;

        @Override
        public void run() {
            ++count;
        }
    }

    private static class TestRunnableSchedule extends RunnableSchedule {
        TestRunnable runnable = new TestRunnable();

        public TestRunnableSchedule(Schedule schedule, long now) {
            super(schedule, "Test");
            setLastCompleted(now);
        }

        @Override
        public boolean isDue(Calendar now) {
            return true; // always claim to be due
        }

        @Override
        public void run() {
            try {
                setLastStarted(getLastCompleted());
                runnable.run();
            } finally {
                setLastCompleted(getLastCompleted() + 1L);
            }
        }
    }

    private void waitForExecution(int expectedCountAtLeast, TestRunnable runnable) {
        long start = System.currentTimeMillis();
        while (runnable.count < expectedCountAtLeast) {
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
            if (System.currentTimeMillis() > (start + TIMEOUT)) {
                Assert.fail("Task was not run, timeout (" + TIMEOUT + "ms) reached");
            }
        }
    }

    @Test
    public void testRegisterSingleShot() {
        TestRunnable testRunnable = new TestRunnable();
        Task task = new Task(testRunnable, 0, -1L);
        UUID taskId = instance.registerTask(task);
        Assert.assertTrue(instance.isRegistered(taskId));

        waitForExecution(1, testRunnable);
        Assert.assertEquals(1, testRunnable.count); // ensure that the runnable has been called once
        Assert.assertTrue(instance.isDone(taskId));
        Assert.assertFalse(instance.cancel(taskId, true));
        instance.unregisterTask(taskId);
        Assert.assertFalse(instance.isRegistered(taskId));
    }

    @Test
    public void testRegisterUnregisterPeriodic() {
        TestRunnable testRunnable = new TestRunnable();
        Task task = new Task(testRunnable, 0, 10L);
        UUID taskId = instance.registerTask(task);
        Assert.assertTrue(instance.isRegistered(taskId));

        waitForExecution(2, testRunnable);
        Assert.assertTrue(testRunnable.count > 1); // ensure that the runnable has been called multiple times
        Assert.assertFalse(instance.isDone(taskId));
        Assert.assertTrue(instance.cancel(taskId, true));
        Assert.assertTrue(instance.isDone(taskId));

        instance.unregisterTask(taskId);
        Assert.assertFalse(instance.isRegistered(taskId));
    }

    @Test
    public void testRegisterUnregisterSchedule() {
        Schedule schedule = new Schedule();
        long now = System.currentTimeMillis();
        TestRunnableSchedule runnableSchedule = new TestRunnableSchedule(schedule, now);
        instance.registerCron(100, TimeUnit.MILLISECONDS); // set the cron period to 10ms
        UUID scheduleId = instance.registerSchedule(runnableSchedule);

        waitForExecution(2, runnableSchedule.runnable);
        int count = runnableSchedule.runnable.count;
        Assert.assertTrue(count > 1); // ensure that the runnable has been called multiple times
        Assert.assertTrue(instance.isDone(scheduleId)); // ensure that it is now done
        Assert.assertEquals(now + count - 1, instance.getLastStarted(scheduleId));
        Assert.assertEquals(now + count, instance.getLastCompleted(scheduleId));

        instance.unregisterSchedule(scheduleId);
        Assert.assertEquals(count, runnableSchedule.runnable.count); // ensure that the runnable has not been called further
    }
}
