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
    /** 总共完成线程数 */
    private AtomicInteger completeCount = new AtomicInteger(0);
    /** 线程编号 */
    private AtomicInteger threadNum = new AtomicInteger(0);
    /** 线程锁 */
    private Lock lock = new ReentrantLock();
    /** 线程池状态：-1 running,0 shutdown,1 shutdown now */
    private volatile int status = -1;
    /** 工作线程 */
    private Set<Worker> workers = ConcurrentHashMap.newKeySet();


    public WheelThreadPool(int coreCount, int maxCount, long keepAliveTime, TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> queue, String prefix) {
        this.coreCount = coreCount;
        this.maxCount = maxCount;
        this.queue = queue;
        this.prefix = prefix;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }

    public void execute(Runnable runnable) {
        if (status >= 0) {
            throw new RuntimeException("线程池已经关闭");
        }
        // 当前运行线程数小于核心线程数
        if (activeCount.get() < coreCount) {
            activeCount.incrementAndGet();
            Worker w = new Worker(runnable);
            workers.add(w);
            w.start();
            return;
        }
        boolean offer = queue.offer(runnable);
        if (!offer) {
            if (activeCount.get() < maxCount) {
                activeCount.incrementAndGet();
                Worker w = new Worker(runnable);
                workers.add(w);
                w.start();
                return;
            }
            throw new RuntimeException("线程池已经满了");
        }
    }

    public void stop() {
        status = 0;
    }

    public void stopNow() {
        status = 1;
        for (Worker worker : workers) {
            if (!worker.isInterrupted()) {
                worker.interrupt();
            }
        }
    }

    public class Worker extends Thread {
        /** 用户传的参数线程实现 */
        private Runnable runnable;

        public Worker(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            Worker work = null;
            try {
                Thread.currentThread().setName(String.format(prefix + "-%s", threadNum.getAndIncrement()));
                while (runnable != null || (work = getTask()) != null) {
                    try {
                        if (runnable != null) {
                            runnable.run();
                        } else {
                            work.runnable.run();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    } finally {
                        runnable = null;
                        completeCount.getAndIncrement();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                activeCount.incrementAndGet();
                Worker w = new Worker(null);
                workers.add(w);
                w.start();
            } finally {
                activeCount.decrementAndGet();
                workers.remove(this);
                if (status >= 0 && queue.size() == 0) {
                    stopNow();
                }
            }
        }
    }

    private Worker getTask() {
        if (status >= 0 && queue.size() == 0) {
            return null;
        }
        try {
            lock.lock();
            if (activeCount.get() <= coreCount) {
                Runnable take = queue.take();
                return new Worker(take);
            } else {
                Runnable poll = queue.poll(keepAliveTime, keepAliveTimeUnit);
                if (poll == null) {
                    return null;
                } else {
                    return new Worker(poll);
                }
            }
        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
        return null;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public int getActiveCount() {
        return activeCount.get();
    }

    public int getCompleteCount() {
        return completeCount.get();
    }

    public int getMaxCount() {
        return maxCount;
    }

    public BlockingQueue<Runnable> getQueue() {
        return queue;
    }

    public Set<Worker> getWorkers() {
        return workers;
    }
}
