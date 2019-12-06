package me.ctf.lab.wheel.threadpool;

import com.sun.corba.se.spi.orbutil.threadpool.Work;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
public class WheelThreadPool2 {
    /** 核心线程数 */
    private int coreCount;
    /** 最大线程数 */
    private int maxCount;
    /** 阻塞队列 */
    private BlockingQueue<Runnable> queue;
    /** 线程名字前缀 */
    private String prefix;
    /** 线程运行完成后存活时长 */
    private long keepAliveTime;
    /** 线程运行完成后存活时长单位 */
    private TimeUnit keepAliveTimeUnit;

    /** 当前活动线程数 */
    private AtomicInteger activeCount = new AtomicInteger(0);

    public WheelThreadPool2(int coreCount, int maxCount, long keepAliveTime, TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> queue, String prefix) {
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
    /** 线程编号 */
    private AtomicInteger threadNum = new AtomicInteger(0);

    public class Worker extends Thread {
        /** 用户传的参数线程实现 */
        private Runnable runnable;

        public Worker(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setName(String.format(prefix + "-%s", threadNum.getAndIncrement()));
                runnable.run();
            } finally {
                // 当前活动线程数-1
                activeCount.decrementAndGet();
            }
        }
    }
}
