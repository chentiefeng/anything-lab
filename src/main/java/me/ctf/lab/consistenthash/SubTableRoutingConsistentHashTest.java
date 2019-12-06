package me.ctf.lab.consistenthash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-06 17:36
 */
public class SubTableRoutingConsistentHashTest {
    private static SortedMap<Long, String> virtualMap = new TreeMap<>();

    /**
     * 用了md5 方法
     *
     * @param value
     * @return
     */
    public static long hash(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes;
        try {
            keyBytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + value, e);
        }

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        return hashCode & 0xffffffffL;
    }

    /**
     * 根据id获取table
     *
     * @param i
     * @return
     */
    private static String getTable(int i) {
        long hash = hash(i + "");
        SortedMap<Long, String> longStringSortedMap = virtualMap.tailMap(hash);
        long nearHash;
        if (longStringSortedMap.isEmpty()) {
            nearHash = virtualMap.firstKey();
        } else {
            nearHash = longStringSortedMap.firstKey();
        }
        String virtualTable = virtualMap.get(nearHash);
        return "riskt_indicator_result_" + Integer.valueOf(virtualTable.split("&&")[0]);
    }

    /**
     * 一致性hash 分表id统计
     *
     * @param tableLen     总共多少张表
     * @param virtualNodes 虚拟节点
     * @param maxId        最大id
     * @return
     */
    private static Map<Integer, String> consistentHashStatMap(int tableLen, int virtualNodes, int maxId) {
        //初始化虚拟节点，测试方法每次都重新生成
        virtualMap.clear();
        for (int i = 0; i < tableLen; i++) {
            for (int j = 0; j < virtualNodes; j++) {
                String virtualTable = i + "&&" + j;
                long hash = hash(virtualTable);
                virtualMap.put(hash, virtualTable);
            }
        }
        Map<Integer, String> statMap = new TreeMap<>();
        for (int i = 1; i <= maxId; i++) {
            String realTable = getTable(i);
            statMap.put(i, realTable);
        }
        return statMap;
    }

    public static void main(String[] args) {
        System.out.println("======================原表统计开始==========================");
        Map<Integer, String> leftMap = consistentHashStatMap(32, 128, 100000);
        Map<String, Integer> leftStat = leftMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue,
                () -> new TreeMap<>(Comparator.comparingInt(s -> Integer.parseInt(s.substring(s.lastIndexOf("_") + 1)))),
                Collectors.summingInt(entry -> 1)));
        for (Map.Entry<String, Integer> map : leftStat.entrySet()) {
            System.out.println(String.format("表=%s,表数量=%s", map.getKey(), map.getValue()));
        }
        System.out.println("======================原表统计结束==========================");
        System.out.println("======================新表统计开始==========================");
        Map<Integer, String> rightMap = consistentHashStatMap(48, 128, 100000);
        Map<String, Integer> rightStat = rightMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue,
                () -> new TreeMap<>(Comparator.comparingInt(s -> Integer.parseInt(s.substring(s.lastIndexOf("_") + 1)))),
                Collectors.summingInt(entry -> 1)));
        for (Map.Entry<String, Integer> map : rightStat.entrySet()) {
            System.out.println(String.format("表=%s,表数量=%s", map.getKey(), map.getValue()));
        }
        System.out.println("======================新表统计结束==========================");

        Map<String, Integer> after = new HashMap<>(8);
        for (Map.Entry<Integer, String> map : leftMap.entrySet()) {
            String rightValue = rightMap.get(map.getKey());
            if (!map.getValue().equals(rightValue)) {
                after.put(map.getValue(), after.getOrDefault(map.getValue(), 0) + 1);
                //每个id从哪个表迁移到哪个表
//                System.out.println(String.format("id=%s,from=%s,to=%s", map.getKey(), map.getValue(), rightValue));
            }
        }
        System.out.println("======================迁移率统计开始==========================");
        for (Map.Entry<String, Integer> map : leftStat.entrySet()) {
            if (after.containsKey(map.getKey())) {
                System.out.println(String.format("表=%s,迁移率=%.2f", map.getKey(), (double) after.get(map.getKey()) / (double) map.getValue()));
            }
        }
        System.out.println("======================迁移率统计结束==========================");
    }
}
