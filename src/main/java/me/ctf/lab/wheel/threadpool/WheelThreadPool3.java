package me.ctf.lab.wheel.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程池轮子
 *
 * @author chentiefeng[chentiefeng@linzikg.com]
 * @date 2019/12/03 09:33
 */
public class WheelThreadPool3 {
    /**
     * 核心线程数
     */
    private int coreCount;
    /**
     * 最大线程数
     */
    private int maxCount;
    /**
     * 阻塞队列
     */
    private BlockingQueue<Runnable> queue;
    /**
     * 线程名字前缀
     */
    private String prefix;
    /**
     * 线程运行完成后存活时长
     */
    private long keepAliveTime;
    /**
     * 线程运行完成后存活时长单位
     */
    private TimeUnit keepAliveTimeUnit;

    /**
     * 当前活动线程数
     */
    private AtomicInteger activeCount = new AtomicInteger(0);

    public WheelThreadPool3(int coreCount, int maxCount, long keepAliveTime, TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> queue, String prefix) {
        this.coreCount = coreCount;
        this.maxCount = maxCount;
        this.queue = queue;
        this.prefix = prefix;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }

    public void execute(Runnable runnable) {
        // 当前运行线程数小于核心线程数
        if (activeCount.get() < coreCount) {
            activeCount.incrementAndGet();
            //新建线程直接开始，执行完后activeCount-1
            new Worker(runnable).start();
            return;
        }
        boolean offer = queue.offer(runnable);
        if (!offer) {
            if (activeCount.get() < maxCount) {
                activeCount.incrementAndGet();
                //新建线程直接开始，执行完后activeCount-1
                new Worker(runnable).start();
                return;
            }
            throw new RuntimeException("线程池已经满了");
        }
    }

    /**
     * 线程编号
     */
    private AtomicInteger threadNum = new AtomicInteger(0);
    /**
     * 线程锁
     */
    private Lock lock = new ReentrantLock();

    public class Worker extends Thread {
        /**
         * 用户传的参数线程实现
         */
        private Runnable runnable;

        public Worker(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            //线程是否执行成功标记，默认成功
            boolean isSuccess = true;
            try {
                Thread.currentThread().setName(String.format(prefix + "-%s", threadNum.getAndIncrement()));
                if (runnable != null) {
                    runnable.run();
                }
                Runnable r = queue.poll(keepAliveTime, keepAliveTimeUnit);
                while (true) {
                    if (r != null) {
                        r.run();
                        r = queue.poll(keepAliveTime, keepAliveTimeUnit);
                    } else {
                        lock.lock();
                        try {
                            if (activeCount.get() > coreCount) {
                                return;
                            }
                        } finally {
                            lock.unlock();
                        }
                        r = queue.take();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 异常情况把标记置为false
                isSuccess = false;
            } finally {
                // 当前活动线程数-1
                activeCount.decrementAndGet();
                if (!isSuccess) {
                    // 如果异常则重新创建一个Worker线程，
                    activeCount.incrementAndGet();
                    new Worker(null).start();
                }
            }
        }
    }
}
