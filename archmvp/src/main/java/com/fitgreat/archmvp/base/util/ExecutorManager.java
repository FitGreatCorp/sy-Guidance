package com.fitgreat.archmvp.base.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理器<p>
 *
 * @author zixuefei
 * @since 2019/11/29 16:14
 */
public class ExecutorManager {
    private final String TAG = ExecutorManager.class.getSimpleName();
    private final ExecutorService executor = new ThreadPoolExecutor(1, 10,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(20),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    public static ExecutorManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExecutorManager() {
    }

    private static class Holder {
        private static ExecutorManager INSTANCE = new ExecutorManager();
    }

    public void executeTask(Runnable task) {
        if (task == null || executor.isShutdown()) {
            return;
        }
        executor.execute(task);
    }

    public Future executeScheduledTask(Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (task == null || scheduledExecutorService.isShutdown()) {
            return null;
        }
        return scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public void cancelScheduledTask(Future future) {
        if (scheduledExecutorService == null || future == null) {
            return;
        }
        if (!future.isCancelled()) {
            future.cancel(true);
        }
    }

    private void shutDownAllScheduled() {
        if (scheduledExecutorService == null) {
            return;
        }
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdownNow();
        }
    }

    private void shutDownExecutor() {
        if (executor == null) {
            return;
        }
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public void shutDown() {
        shutDownExecutor();
        shutDownAllScheduled();
    }
}
