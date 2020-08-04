package me.ctf.lab.cache;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 缓存操作
 *
 * @author chentiefeng
 * @date 2020-07-05 22:21
 */
public interface CacheSupport {
    /**
     * 新增val，并返回新增后的数
     *
     * @param key
     * @param val
     * @param expire
     * @return
     */
    long incrementAndGet(String key, long val, Duration expire);

    /**
     * 相减val，并返回相减后的数
     *
     * @param key
     * @param val
     * @param expire
     * @return
     */
    long decrementAndGet(String key, long val, Duration expire);

    /**
     * put
     *
     * @param key
     * @param val
     * @param expire
     */
    void put(String key, String val, Duration expire);

    /**
     * putIfAbsent
     *
     * @param key
     * @param val
     * @param expire
     * @return true put ,false exists
     */
    boolean putIfAbsent(String key, String val, Duration expire);


    /**
     * 不存在则执行supplier，返回已有的数据
     *
     * @param key
     * @param supplier
     * @param expire
     * @param updateLastAccess
     * @return
     */
    String putIfAbsentAndGet(String key, Supplier<String> supplier, Duration expire, boolean updateLastAccess);


    /**
     * get
     *
     * @param key
     * @return
     */
    String get(String key);


    /**
     * 获取数据并刷新过期时间
     *
     * @param key
     * @param expire
     * @return
     */
    String getAndUpdateAccess(String key, Duration expire);


    /**
     * remove
     *
     * @param key
     */
    void remove(String key);

    /**
     * 放入set
     *
     * @param key
     * @param val
     * @param expire
     * @return
     */
    boolean putSet(String key, String val, Duration expire);

    /**
     * 获取Set全部列表
     *
     * @param key
     * @return
     */
    Set<String> getSet(String key);

    /**
     * 弹出第一个
     *
     * @param key
     * @return
     */
    String popSetItem(String key);

    /**
     * remove set item
     *
     * @param key
     * @param val
     */
    void removeSetItem(String key, String val);

    /**
     * 放入List
     *
     * @param key
     * @param val
     * @param expire
     * @return list size
     */
    int putList(String key, String val, Duration expire);

    /**
     * 尝试增加，用于并发控制
     * @param key
     * @param max
     * @param expire
     * @return 非null，标识添加成功
     */
    String signalTryAdd(String key, int max, Duration expire);

    /**
     * 删除信号量
     * @param key
     * @param signal
     * @return
     */
    boolean signalDel(String key, String signal);
}
