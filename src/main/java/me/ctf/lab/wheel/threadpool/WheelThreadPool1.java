package me.ctf.lab.wheel.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池轮子
 *
 * @author chentiefeng[chentiefeng@linzikg.com]
 * @date 2019/12/03 09:33
 */
public class WheelThreadPool1 {
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

    public WheelThreadPool1(int coreCount, int maxCount, long keepAliveTime, TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> queue, String prefix) {
        this.coreCount = coreCount;
        this.maxCount = maxCount;
        this.queue = queue;
        this.prefix = prefix;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = keepAliveTimeUnit;
    }
}
