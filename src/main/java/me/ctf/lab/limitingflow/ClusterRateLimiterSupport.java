package me.ctf.lab.limitingflow;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import me.ctf.lab.cache.CacheSupport;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author chentiefeng
 * @date 2020-07-06 11:18
 */
@Slf4j
public class ClusterRateLimiterSupport implements RateLimiterSupport {
    private final CacheSupport cacheSupport;

    public ClusterRateLimiterSupport(CacheSupport cacheSupport) {
        this.cacheSupport = cacheSupport;
    }

    @Override
    public boolean qps(String resource, int max, Duration duration, Execute execute, Execute failed) {
        int i;
        try {
            i = cacheSupport.putList(resource, "1", duration);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("限流异常，放行");
            if (execute != null) {
                execute.run();
            }
            return true;
        }
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
        String signal;
        try {
            signal = cacheSupport.signalTryAdd(resource, max, Duration.ofMinutes(30));
        } catch (Exception e) {
            //ignore
            log.error(e.getMessage(), e);
            log.error("限流异常，放行");
            execute.run();
            return true;
        }
        try {
            if (signal == null) {
                if (failed != null) {
                    failed.run();
                }
                return false;
            } else {
                execute.run();
                return true;
            }
        } finally {
            if (signal != null) {
                try {
                    cacheSupport.signalDel(resource, signal);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
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
        int num = 0;
        while ((++num) <= sleepNum) {
            String signal;
            try {
                signal = cacheSupport.signalTryAdd(resource, max, Duration.ofMinutes(30));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                log.error("限流异常，放行");
                bizExec.run();
                return;
            }
            try {
                if (signal != null) {
                    bizExec.run();
                    return;
                }
            } finally {
                if (signal != null) {
                    try {
                        cacheSupport.signalDel(resource, signal);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            if (sleepConsumer != null) {
                sleepConsumer.accept(num);
            }
            ThreadUtil.sleep(sleep.toMillis());
        }
    }
}
