package org.eclipse.skalli.commons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void submit(Runnable task) {
        threadPool.submit(task);
    }

}