package me.ctf.lab.bloomfilter;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;

import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 布隆过滤器
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-27 14:24
 */
public class BloomFilter<T> {
    /**
     * hash
     */
    private Funnel<T> funnel;
    /**
     * bit数组长度
     */
    private int numBits;
    /**
     * hash方法个数
     */
    private int numHashFunctions;
    /**
     * 是否本地
     */
    private boolean local;
    /**
     * bit array
     */
    private BitSet bitSet;
    /**
     * put
     */
    private Consumer<Integer> putConsumer;
    /**
     * contain
     */
    private Predicate<Integer> containPredicate;

    private BloomFilter(int expectedInsertions, double fpp, Funnel<T> funnel, boolean local) {
        extracted(expectedInsertions, fpp, funnel, local);
    }
    private BloomFilter(int expectedInsertions, double fpp, Funnel<T> funnel, boolean local, Consumer<Integer> putConsumer,Predicate<Integer> containPredicate ) {
        extracted(expectedInsertions, fpp, funnel, local);
        this.putConsumer = putConsumer;
        this.containPredicate = containPredicate;
        
    }
    private void extracted(int expectedInsertions, double fpp, Funnel<T> funnel, boolean local) {
        fpp = fpp == 0 ? Double.MIN_VALUE : fpp;
        this.funnel = funnel;
        this.numBits = (int) (-expectedInsertions * Math.log(fpp) / (Math.log(2) * Math.log(2)));
        this.numHashFunctions = Math.max(1, (int) Math.round((double) numBits / expectedInsertions * Math.log(2)));
        this.local = local;
    }
    /**
     * 根据key获取bitmap下标 方法来自guava
     *
     * @param key
     * @return
     */
    private int[] getIndexs(T key) {
        long hash1 = Hashing.murmur3_128().hashObject(key, funnel).asLong();
        long hash2 = hash1 >>> 16;
        int[] result = new int[numHashFunctions];
        for (int i = 0; i < numHashFunctions; i++) {
            long combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            result[i] = (int) (combinedHash % numBits);
        }
        return result;
    }

    /**
     * put
     *
     * @param key
     */
    public void put(T key) {
        if (putConsumer == null) {
            if (local) {
                if (bitSet == null) {
                    bitSet = new BitSet(numBits);
                }
                putConsumer = idx -> bitSet.set(idx);
            } else {
                throw new IllegalArgumentException("Consumer must not be null if `local` is false");
            }
        }
        int[] indexs = getIndexs(key);
        Arrays.stream(indexs).forEach(putConsumer::accept);
    }


    public boolean contain(T key) {
        if (containPredicate == null) {
            if (local) {
                containPredicate = idx -> bitSet.get(idx);
            } else {
                throw new IllegalArgumentException("containPredicate must not be null if `local` is false");
            }
        }
        int[] indexs = getIndexs(key);
        for (int index : indexs) {
            if (!containPredicate.test(index)) {
                return false;
            }
        }
        return true;
    }


    static class BloomFilterBuilder<T> {
        /**
         * 期望数量
         */
        private int expectedInsertions;
        /**
         * 准确率
         */
        private double fpp;
        /**
         * hash funnel
         */
        private Funnel<T> funnel;


        public BloomFilterBuilder<T> expectedInsertions(int expectedInsertions) {
            this.expectedInsertions = expectedInsertions;
            return this;
        }

        public BloomFilterBuilder<T> fpp(double fpp) {
            this.fpp = fpp;
            return this;
        }

        public BloomFilterBuilder<T> funnel(Funnel<T> funnel) {
            this.funnel = funnel;
            return this;
        }


        public BloomFilter<T> build() {
            return new BloomFilter<>(expectedInsertions, fpp, funnel, true);
        }
        public BloomFilter<T> build(Consumer<Integer> putConsumer, Predicate<Integer> containPredicate) {
            return new BloomFilter<>(expectedInsertions, fpp, funnel, false, putConsumer, containPredicate);
        }
    }
}
