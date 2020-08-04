package me.ctf.lab.cache;

import cn.hutool.core.util.IdUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 代理因为在云端直接用redis
 *
 * @author chentiefeng
 * @date 2020-07-06 11:22
 */
public class ClusterCacheSupport implements CacheSupport {
    private StringRedisTemplate redisTemplate;
    private final static RedisScript<Long> LUA_INCRBY = new DefaultRedisScript<>(
            "local current = redis.call(\"INCRBY\",KEYS[1],ARGV[1]) " +
                    "redis.call(\"EXPIRE\",KEYS[1],ARGV[2]) " +
                    "return current", Long.class);
    private final static RedisScript<Long> LUA_DECRBY = new DefaultRedisScript<>(
            "local current = redis.call(\"DECRBY\",KEYS[1],ARGV[1]) " +
                    "redis.call(\"EXPIRE\",KEYS[1],ARGV[2]) " +
                    "return current", Long.class);
    private final static RedisScript<Boolean> LUA_ZADD = new DefaultRedisScript<>(
            "local current = redis.call(\"ZADD\",KEYS[1],ARGV[1],ARGV[2]) " +
                    "redis.call(\"EXPIRE\",KEYS[1],ARGV[3]) " +
                    "return current > 0", Boolean.class);
    private final static RedisScript<Long> LUA_ZADD_ZCOUNT = new DefaultRedisScript<>(
            "redis.call(\"ZADD\",KEYS[1],ARGV[1],ARGV[2]) " +
                    "redis.call(\"EXPIRE\",KEYS[1],ARGV[3]) " +
                    "local cnt = redis.call(\"ZCOUNT\",KEYS[1],ARGV[4],ARGV[5]) " +
                    "return cnt", Long.class);
    private final static RedisScript<Long> LUA_SET_TRY_ADD = new DefaultRedisScript<>(
            "local ss = redis.call(\"SCARD\",KEYS[1]) " +
                    "if ss >= tonumber(ARGV[1]) then " +
                    "return 0 " +
                    "else " +
                    "redis.call(\"SADD\",KEYS[1],ARGV[2]) " +
                    "redis.call(\"EXPIRE\",KEYS[1],ARGV[3]) " +
                    "return 1 " +
                    "end", Long.class);

    public ClusterCacheSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long incrementAndGet(String key, long val, Duration expire) {
        List<String> keys = new ArrayList<>(1);
        keys.add(key);
        Long ret = redisTemplate.execute(LUA_INCRBY, keys, val + "", expire.getSeconds() + "");
        return ret == null ? 0L : ret;
    }

    @Override
    public long decrementAndGet(String key, long val, Duration expire) {
        List<String> keys = new ArrayList<>(1);
        keys.add(key);
        Long ret = redisTemplate.execute(LUA_DECRBY, keys, val + "", expire.getSeconds() + "");
        return ret == null ? 0L : ret;
    }


    @Override
    public void put(String key, String val, Duration expire) {
        redisTemplate.opsForValue().set(key, val, expire);
    }

    @Override
    public boolean putIfAbsent(String key, String val, Duration expire) {
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, val, expire);
        return aBoolean != null && aBoolean;
    }

    @Override
    public String putIfAbsentAndGet(String key, Supplier<String> supplier, Duration expire, boolean updateLastAccess) {
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            if (updateLastAccess) {
                redisTemplate.expire(key, expire.toNanos(), TimeUnit.NANOSECONDS);
            }
            return val;
        }
        redisTemplate.opsForValue().setIfAbsent(key, supplier.get(), expire);
        if (updateLastAccess) {
            redisTemplate.expire(key, expire.toNanos(), TimeUnit.NANOSECONDS);
        }
        val = redisTemplate.opsForValue().get(key);
        return val;
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public String getAndUpdateAccess(String key, Duration expire) {
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            redisTemplate.expire(key, expire.toNanos(), TimeUnit.NANOSECONDS);
        }
        return val;
    }


    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean putSet(String key, String val, Duration expire) {
        if (val == null) {
            return false;
        }
        List<String> keys = new ArrayList<>(1);
        keys.add(key);
        Boolean add = redisTemplate.execute(LUA_ZADD, keys, (System.nanoTime() + expire.toNanos()) + "", val, expire.getSeconds() + "");
        return add != null && add;
    }

    @Override
    public Set<String> getSet(String key) {
        Set<String> members = redisTemplate.opsForZSet().rangeByScore(key, System.nanoTime(), Long.MAX_VALUE);
        if (members != null) {
            return members;
        }
        return null;
    }

    @Override
    public String popSetItem(String key) {
        Set<String> set = redisTemplate.opsForZSet().rangeByScore(key, System.nanoTime(), Long.MAX_VALUE, 0, 1);
        if (set != null) {
            return set.stream().findAny().orElse(null);
        }
        return null;
    }

    @Override
    public void removeSetItem(String key, String val) {
        redisTemplate.opsForZSet().remove(key, val);
    }

    @Override
    public int putList(String key, String val, Duration expire) {
        List<String> keys = new ArrayList<>(1);
        keys.add(key);
        Long count = redisTemplate.execute(LUA_ZADD_ZCOUNT, keys,
                (System.nanoTime() + expire.toNanos()) + "",
                val + "@" + IdUtil.fastSimpleUUID(),
                expire.getSeconds() + "",
                System.nanoTime() + "",
                Long.MAX_VALUE + "");
        return count == null ? 0 : count.intValue();
    }

    @Override
    public String signalTryAdd(String key, int max, Duration expire) {
        List<String> keys = new ArrayList<>(1);
        keys.add(key);
        String signal = IdUtil.fastSimpleUUID();
        Long ret = redisTemplate.execute(LUA_SET_TRY_ADD, keys,
                max + "",
                signal,
                expire.getSeconds() + "");
        return ret != null && ret > 0 ? signal : null;
    }

    @Override
    public boolean signalDel(String key, String signal) {
        Long remove = redisTemplate.opsForSet().remove(key, signal);
        return remove != null && remove > 0;
    }
}
