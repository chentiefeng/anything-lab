package me.ctf.lab.cache;

import cn.hutool.cache.GlobalPruneTimer;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author chentiefeng
 * @date 2020-07-05 22:28
 */
public class LocalCacheSupport implements CacheSupport {
    private final ConcurrentMap<String, CopyOnWriteArrayList<LocalCache>> LOCAL_CACHE = Maps.newConcurrentMap();

    public LocalCacheSupport() {
        /**
         * 每30分钟清理一次过期数据
         */
        GlobalPruneTimer.INSTANCE.schedule(this::pruneCache, TimeUnit.MINUTES.toMillis(30));
    }

    private static CopyOnWriteArrayList<LocalCache> getList(String val, long expire) {
        CopyOnWriteArrayList<LocalCache> localCaches = Lists.newCopyOnWriteArrayList();
        localCaches.add(new LocalCache(val, expire));
        return localCaches;
    }

    @Override
    public long incrementAndGet(String key, long val, Duration expire) {
        AtomicLong atomicLong = new AtomicLong();
        LOCAL_CACHE.compute(key, (s, list) -> {
            if (list == null) {
                atomicLong.set(val);
                return getList(Long.toString(val), expire.toNanos());
            }
            if (list.get(0).isExpired()) {
                list.get(0).val = Long.toString(val);
                atomicLong.set(val);
                list.get(0).updateLastAccess();
                return list;
            }
            long v = Long.parseLong(Objects.toString(list.get(0).val)) + val;
            list.get(0).val = String.valueOf(v);
            atomicLong.set(v);
            list.get(0).updateLastAccess();
            return list;
        });
        return atomicLong.get();
    }

    @Override
    public long decrementAndGet(String key, long val, Duration expire) {
        AtomicLong atomicLong = new AtomicLong();
        LOCAL_CACHE.compute(key, (s, list) -> {
            if (list == null) {
                atomicLong.set(-val);
                return getList(Long.toString(-val), expire.toNanos());
            }
            if (list.get(0).isExpired()) {
                list.get(0).val = Long.toString(-val);
                atomicLong.set(-val);
                list.get(0).updateLastAccess();
                return list;
            }
            long v = Long.parseLong(Objects.toString(list.get(0).val)) - val;
            list.get(0).val = String.valueOf(v);
            atomicLong.set(v);
            list.get(0).updateLastAccess();
            return list;
        });
        return atomicLong.get();
    }

    @Override
    public void put(String key, String val, Duration expire) {
        LOCAL_CACHE.put(key, getList(val, expire.toNanos()));
    }

    @Override
    public boolean putIfAbsent(String key, String val, Duration expire) {
        return LOCAL_CACHE.putIfAbsent(key, getList(val, expire.toNanos())) == null;
    }

    @Override
    public String putIfAbsentAndGet(String key, Supplier<String> supplier, Duration expire, boolean updateLastAccess) {
        AtomicReference<String> atomicReference = new AtomicReference<>();
        LOCAL_CACHE.compute(key, (s, list) -> {
            if (list == null) {
                atomicReference.set(supplier.get());
                return getList(atomicReference.get(), expire.toNanos());
            }
            if (list.get(0).isExpired()) {
                atomicReference.set(supplier.get());
                list.get(0).val = atomicReference.get();
            }
            if (updateLastAccess) {
                list.get(0).updateLastAccess();
            }
            atomicReference.set(list.get(0).val);
            return list;
        });
        return atomicReference.get();
    }

    @Override
    public String get(String key) {
        AtomicReference<String> ret = new AtomicReference<>();
        forEach(key, localCache -> {
            if (ret.get() == null) {
                ret.set(localCache.val);
            }
        });
        return ret.get();
    }

    @Override
    public String getAndUpdateAccess(String key, Duration expire) {
        AtomicReference<String> ret = new AtomicReference<>();
        forEach(key, localCache -> {
            if (ret.get() == null) {
                ret.set(localCache.val);
            }
            localCache.updateLastAccess();
            localCache.ttl = expire.toNanos();
        });
        return ret.get();
    }

    private void forEach(String key, Consumer<LocalCache> consumer) {
        List<LocalCache> localCaches = LOCAL_CACHE.get(key);
        if (localCaches == null || localCaches.size() == 0) {
            return;
        }
        localCaches.removeIf(LocalCache::isExpired);
        Iterator<LocalCache> iterator = localCaches.iterator();
        LocalCache localCache;
        while (iterator.hasNext()) {
            localCache = iterator.next();
            consumer.accept(localCache);
        }
        localCaches.removeIf(LocalCache::isExpired);
        if (localCaches.size() == 0) {
            remove(key);
        }
    }

    protected int pruneCache() {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, CopyOnWriteArrayList<LocalCache>> entry : LOCAL_CACHE.entrySet()) {
            entry.getValue().removeIf(LocalCache::isExpired);
            if (entry.getValue().size() == 0) {
                keys.add(entry.getKey());
            }
        }
        keys.forEach(LOCAL_CACHE::remove);
        return keys.size();
    }

    @Override
    public void remove(String key) {
        LOCAL_CACHE.remove(key);
    }

    @Override
    public boolean putSet(String key, String val, Duration expire) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LOCAL_CACHE.compute(key, (s, localCaches) -> {
            if (localCaches == null) {
                atomicBoolean.set(true);
                return getList(val, expire.toNanos());
            }
            LocalCache localCache = new LocalCache(val, expire.toNanos());
            if (localCaches.contains(localCache)) {
                Optional<LocalCache> first = localCaches.stream().filter(l -> l.equals(localCache)).findFirst();
                if (first.isPresent()) {
                    if (first.get().isExpired()) {
                        atomicBoolean.set(true);
                    }
                    first.get().updateLastAccess();
                }
            } else {
                atomicBoolean.set(true);
                localCaches.add(localCache);
            }
            return localCaches;
        });
        return atomicBoolean.get();
    }

    @Override
    public Set<String> getSet(String key) {
        Set<String> ret = new HashSet<>();
        forEach(key, localCache -> ret.add(localCache.val));
        return ret.size() == 0 ? null : ret;
    }

    @Override
    public String popSetItem(String key) {
        AtomicReference<String> ret = new AtomicReference<>();
        forEach(key, localCache -> {
            if (ret.get() == null) {
                ret.set(localCache.val);
                //超时时间改为0，则下次取得时候自动删除
                localCache.ttl = 0;
            }
        });
        return ret.get();
    }

    @Override
    public void removeSetItem(String key, String val) {
        forEach(key, localCache -> {
            if (localCache.val.equals(val)) {
                //超时时间改为0，则下次取得时候自动删除
                localCache.ttl = 0;
            }
        });
    }

    @Override
    public int putList(String key, String val, Duration expire) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        LOCAL_CACHE.compute(key, (s, localCaches) -> {
            if (localCaches == null) {
                atomicInteger.set(1);
                return getList(val, expire.toNanos());
            }
            LocalCache localCache = new LocalCache(val, expire.toNanos());
            localCaches.add(localCache);
            atomicInteger.set(Long.valueOf(localCaches.stream().filter(l -> !l.isExpired()).count()).intValue());
            return localCaches;
        });
        return atomicInteger.get();
    }

    @Override
    public String signalTryAdd(String key, int max, Duration expire) {
        if (max < 1) {
            throw new IllegalArgumentException("max must gte 1");
        }
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        String signal = IdUtil.fastSimpleUUID();
        LOCAL_CACHE.compute(key, (s, localCaches) -> {
            if (localCaches == null) {
                return getList(signal, expire.toNanos());
            }
            if (localCaches.size() < max) {
                LocalCache localCache = new LocalCache(signal, expire.toNanos());
                localCaches.add(localCache);
            } else {
                atomicBoolean.set(false);
            }
            return localCaches;
        });
        return atomicBoolean.get() ? signal : null;
    }

    @Override
    public boolean signalDel(String key, String signal) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        forEach(key, localCache -> {
            if (localCache.val.equals(signal)) {
                localCache.ttl = 0L;
                atomicBoolean.set(true);
            }
        });
        return atomicBoolean.get();
    }

    static class LocalCache {
        String val;
        long lastAccess;
        long ttl;

        LocalCache(String val, long ttl) {
            this.val = val;
            this.ttl = ttl;
            this.lastAccess = System.nanoTime();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LocalCache that = (LocalCache) o;
            return Objects.equals(val, that.val);
        }

        @Override
        public int hashCode() {
            return Objects.hash(val);
        }

        /**
         * 判断是否过期
         *
         * @return 是否过期
         */
        boolean isExpired() {
            if (this.ttl >= 0) {
                final long expiredTime = this.lastAccess + this.ttl;
                return expiredTime > 0 && expiredTime < System.nanoTime();
            }
            return false;
        }

        /**
         * 更新当前操作时间
         */
        void updateLastAccess() {
            this.lastAccess = System.nanoTime();
        }
    }
}
