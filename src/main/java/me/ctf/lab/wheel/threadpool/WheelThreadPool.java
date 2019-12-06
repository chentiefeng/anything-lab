package me.ctf.lab.wheel.threadpool;

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
public class WheelThreadPool {
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
     * 线程编号
     */
    private AtomicInteger threadNum = new AtomicInteger(0);
    /**
     * 线程锁
     */
    private Lock lock = new ReentrantLock();
    /**
     * 剩余线程数
     */
    private AtomicInteger remainingCount = new AtomicInteger(0);
    /**
     * 线程池状态，-1：正在运行，0：暴力关闭，1：优雅关闭
     */
    private volatile int status = -1;

    public WheelThreadPool(int coreCount, int maxCount, long keepAliveTime, TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> queue, String prefix) {
        this.coreCount = coreCount;
        this.maxCount = maxCount;
        this.queue = queue;
        this.prefix = prefix;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }

    /**
     * 提交线程
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException("线程不能为空");
        }
        if (status >= 0) {
            throw new RuntimeException("线程池已经结束");
        }
        remainingCount.incrementAndGet();
        // 当前运行线程数小于核心线程数
        if (workers.size() < coreCount) {
            addWorker(runnable);
            return;
        }
        boolean offer = queue.offer(runnable);
        if (!offer) {
            if (workers.size() < maxCount) {
                addWorker(runnable);
                return;
            }
            //这里简单处理直接抛异常了
            throw new RuntimeException("线程池已经满了");
        }
    }


    /**
     * 工作线程
     */
    class Worker extends Thread {
        /**
         * 用户传的参数线程实现
         */
        private Runnable runnable;

        public Worker(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            if (status == 0) {
                return;
            }
            //线程是否执行成功标记，默认成功
            boolean isSuccess = true;
            try {
                Thread.currentThread().setName(String.format(prefix + "-%s", threadNum.getAndIncrement()));
                while (runnable != null || (runnable = getQueueTask()) != null) {
                    try {
                        runnable.run();
                    } finally {
                        remainingCount.decrementAndGet();
                        runnable = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 异常情况把标记置为false
                isSuccess = false;
            } finally {
                if (status != 0) {
                    // 当前活动线程数-1
                    workers.remove(this);
                    if (!isSuccess) {
                        // 如果异常则重新创建一个Worker线程
                        addWorker(null);
                    }
                    interruptWorkers(status == 1 && remainingCount.get() == 0);
                }
            }
        }
    }

    /**
     * 获取队列任务
     * @return
     */
    private Runnable getQueueTask() {
        try {
            Runnable r = queue.poll(keepAliveTime, keepAliveTimeUnit);
            if (r == null) {
                lock.lock();
                try {
                    if (workers.size() > coreCount) {
                        return null;
                    }
                } finally {
                    lock.unlock();
                }
                r = queue.take();
            }
            return r;
        } catch (InterruptedException ignore) {
        }
        return null;
    }

    private void interruptWorkers(boolean b) {
        if (b) {
            for (Worker worker : workers) {
                if (!worker.isInterrupted()) {
                    worker.interrupt();
                }
            }
        }
    }

    /**
     * 新增线程到workers
     *
     * @param o
     */
    private void addWorker(Runnable o) {
        Worker w = new Worker(o);
        workers.add(w);
        w.start();
    }

    /**
     * 保存正在运行的线程
     */
    private Set<Worker> workers = ConcurrentHashMap.newKeySet();

    /**
     * 暴力关闭线程池
     */
    public void stopNow() {
        status = 0;
        interruptWorkers(true);
    }


    /**
     * 优雅关闭线程池
     */
    public void stop() {
        status = 1;
        interruptWorkers(remainingCount.get() == 0);
    }
}
