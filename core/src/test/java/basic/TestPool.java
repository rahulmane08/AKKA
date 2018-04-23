package basic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestPool {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService workerPool = Executors.newScheduledThreadPool(3);
        ScheduledExecutorService masterPool = Executors.newScheduledThreadPool(3);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            logStats(workerPool);
            Future<?> submit = masterPool.submit(() -> {
                CountDownLatch latch = new CountDownLatch(1);
                System.out.format("master [%s] submitting task %d%n", Thread.currentThread().getName(), finalI);

                workerPool.submit(() -> {
                    System.out.format("worker [%s] executing task %d%n", Thread.currentThread().getName(), finalI);
                    try {
                        Thread.sleep(10 * 1000);
                        int j = (finalI < 2) ? (1 / 0) : 1;
                        latch.countDown();
                    } catch (Exception e) {
                        System.out.format("worker [%s] threw exception on task %d%s%n", Thread.currentThread().getName(),
                                finalI, e.getMessage());
                    }
                    System.out.format("worker [%s] finished task %d%n", Thread.currentThread().getName(), finalI);
                });
                try {
                    latch.await();
                    System.out.format("master [%s] finished task %d%n", Thread.currentThread().getName(), finalI);
                } catch (InterruptedException e) {
                    System.out.format("master [%s] interrupted and unblocked while waiting on latch for task %d%n",
                            Thread.currentThread().getName(), finalI);
                }
            });
            futures.add(submit);
        }
        for (Future<?> submit : futures)
            try {
                logStats(workerPool);
                if (!submit.isDone())
                    submit.get(15, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println("main cancelling the future");
                submit.cancel(true);
            }
            logStats(workerPool);
    }

    private static void logStats(ScheduledExecutorService workerPool) {
        ScheduledThreadPoolExecutor threadPoolExecutor = (ScheduledThreadPoolExecutor) workerPool;
        System.out.format("getActiveCount(%d) getCorePoolSize(%d) getPoolSize(%d) getLargestPoolSize(%d) " +
                        "getMaximumPoolSize(%d) %n", threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getLargestPoolSize(),
                threadPoolExecutor.getMaximumPoolSize());
    }
}
