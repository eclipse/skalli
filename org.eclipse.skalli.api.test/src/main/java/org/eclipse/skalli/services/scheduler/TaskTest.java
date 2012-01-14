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

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class TaskTest {

    private static class TestRunnable implements Runnable {
        @Override
        public void run() {
        }
    }

    @Test
    public void testBasics() {
        Task task = new Task(new TestRunnable());
        Assert.assertNotNull(task.getRunnable());
        Assert.assertEquals(0, task.getInitialDelay());
        Assert.assertEquals(-1L, task.getPeriod());
        Assert.assertTrue(task.isOneShot());
        Assert.assertNotNull(task.toString());

        Task anotherTask = new Task(new TestRunnable(), 4711L, 1234L);
        Assert.assertNotNull(anotherTask.getRunnable());
        Assert.assertEquals(4711L, anotherTask.getInitialDelay());
        Assert.assertEquals(1234L, anotherTask.getPeriod());
        Assert.assertFalse(anotherTask.isOneShot());
        Assert.assertNotNull(anotherTask.toString());

        task = new Task(new TestRunnable(), -1234L, 5L);
        Assert.assertNotNull(task.getRunnable());
        Assert.assertEquals(0, task.getInitialDelay()); // initial delay in the past! => initial delay = 0
        Assert.assertEquals(10L, task.getPeriod()); // period<10ms! => period=10ms
        Assert.assertFalse(task.isOneShot());
        Assert.assertNotNull(task.toString());

        try {
            task = new Task(null);
            Assert.fail("Task(null)");
        } catch (IllegalArgumentException e) {
        }
        try {
            task = new Task(null, 0, 0);
            Assert.fail("Task(null,0,0)");
        } catch (IllegalArgumentException e) {
        }
    }

}
