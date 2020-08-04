package me.ctf.lab.limitingflow;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import me.ctf.lab.cache.CacheSupport;
import me.ctf.lab.cache.LocalCacheSupport;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 单机限流
 *
 * @author chentiefeng
 * @date 2020-07-04 15:10
 */
public class LocalRateLimiterSupport implements RateLimiterSupport {
    private final ConcurrentMap<String, Semaphore> SEMAPHORE_MAP = new ConcurrentHashMap<>(8);

    private final CacheSupport cacheSupport;

    public LocalRateLimiterSupport(CacheSupport cacheSupport) {
        this.cacheSupport = cacheSupport;
    }

    public LocalRateLimiterSupport() {
        this.cacheSupport = new LocalCacheSupport();
    }

    @Override
    public boolean qps(String resource, int max, Duration duration, Execute execute, Execute failed) {
        int i = cacheSupport.putList(resource, "1", duration);
        if (i <= max) {
            if (execute != null) {
                execute.run();
            }
            return true;
        }
        if (failed != null) {
            failed.run();
        }
        return false;
    }

    @Override
    public boolean concurrent(String resource, int max, Execute execute, Execute failed) {
        SEMAPHORE_MAP.putIfAbsent(resource, new Semaphore(max));
        Semaphore semaphore = SEMAPHORE_MAP.get(resource);
        if (!semaphore.tryAcquire()) {
            failed.run();
            return false;
        }
        try {
            execute.run();
            return true;
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void qpsQueue(String resource, int max, Duration duration, Execute bizExec, int sleepNum, Duration sleep, Consumer<Integer> sleepConsumer) {
        if (bizExec == null) {
            throw new IllegalArgumentException("业务处理逻辑不能为空");
        }
        int num = 0;
        while (!qps(resource, max, duration, bizExec, null) && (++num) <= sleepNum) {
            if (sleepConsumer != null) {
                sleepConsumer.accept(num);
            }
            ThreadUtil.sleep(sleep.toMillis());
        }
    }

    @Override
    public void concurrentQueue(String resource, int max, Execute bizExec, int sleepNum, Duration sleep, Consumer<Integer> sleepConsumer) {
        if (bizExec == null) {
            throw new IllegalArgumentException("业务处理逻辑不能为空");
        }
        SEMAPHORE_MAP.putIfAbsent(resource, new Semaphore(max));
        Semaphore semaphore = SEMAPHORE_MAP.get(resource);
        int num = 0;
        try {
            while (!semaphore.tryAcquire(sleep.toMillis(), TimeUnit.MILLISECONDS) && (++num) <= sleepNum) {
                if (sleepConsumer != null) {
                    sleepConsumer.accept(num);
                }
            }
        } catch (InterruptedException e) {
            //ignore
        }
        try {
            bizExec.run();
        } finally {
            semaphore.release();
            if (!semaphore.hasQueuedThreads()) {
                SEMAPHORE_MAP.remove(resource);
            }
        }
    }

    public static void main(String[] args) {
        RateLimiterSupport rateLimiterSupport = new LocalRateLimiterSupport(new LocalCacheSupport());
        String resource = "Test";
        rateLimiterSupport.qps(resource, 2, Duration.ofSeconds(5), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":1秒执行"), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":1秒失败"));
        System.out.println(DateUtil.formatDateTime(new Date()) + ":睡眠4秒");
        ThreadUtil.sleep(4000);
        rateLimiterSupport.qps(resource, 2, Duration.ofSeconds(5), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":4秒执行"), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":4秒失败"));
        ThreadUtil.sleep(2000);
        rateLimiterSupport.qps(resource, 2, Duration.ofSeconds(5), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":6秒执行"), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":6秒失败"));
        ThreadUtil.sleep(1000);
        rateLimiterSupport.qps(resource, 2, Duration.ofSeconds(5), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":7秒执行"), () -> System.out.println(DateUtil.formatDateTime(new Date()) + ":7秒失败"));
    }
}
