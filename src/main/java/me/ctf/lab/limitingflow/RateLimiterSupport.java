package me.ctf.lab.limitingflow;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * 限流
 *
 * @author chentiefeng
 * @date 2020-07-05 14:09
 */
public interface RateLimiterSupport {
    @FunctionalInterface
    public interface Execute {
        /**
         * before
         */
        default void before() {

        }

        /**
         * run
         */
        void run();

        /**
         * after
         */
        default void after() {

        }

    }
    /**
     * qps限流:限制一段时间内发生的请求个数
     *
     * @param resource 资源
     * @param max      阀值
     * @param duration 时间间隔
     * @param execute  未达到阀值执行逻辑
     * @param failed   达到阀值执行逻辑
     * @return
     */
    boolean qps(String resource, int max, Duration duration, Execute execute, Execute failed);

    /**
     * 并发数限流:限制同一时刻的最大并发请求数量
     *
     * @param resource 资源
     * @param max      阀值
     * @param execute  未达到阀值执行逻辑
     * @param failed   达到阀值执行逻辑
     * @return
     */
    boolean concurrent(String resource, int max, Execute execute, Execute failed);

    /**
     * qps限流排队
     *
     * @param resource
     * @param max
     * @param duration
     * @param bizExec 业务逻辑
     * @param sleepNum
     * @param sleep
     * @param sleepConsumer
     */
    void qpsQueue(String resource, int max, Duration duration, Execute bizExec, int sleepNum, Duration sleep, Consumer<Integer> sleepConsumer);

    /**
     * 并发限流排队
     *
     * @param resource
     * @param max
     * @param bizExec       业务逻辑
     * @param sleepNum
     * @param sleep
     * @param sleepConsumer 等待逻辑
     */
    void concurrentQueue(String resource, int max, Execute bizExec, int sleepNum, Duration sleep, Consumer<Integer> sleepConsumer);

    enum Type {
        QPS,
        CONCURRENT
    }
}
