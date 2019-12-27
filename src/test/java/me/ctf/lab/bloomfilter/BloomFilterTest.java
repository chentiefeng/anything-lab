package me.ctf.lab.bloomfilter;

import com.google.common.hash.Funnel;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * 应用场景：复杂网络数据初始化，防止电话重复 / 指标服务结果表数据迁移一致性校验
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-27 15:38
 */
public class BloomFilterTest {
    public static void main(String[] args) {
        BloomFilter<String> bloomFilter = new BloomFilter.BloomFilterBuilder<String>()
                .expectedInsertions(500000)
                .fpp(0.01d)
                .funnel((Funnel<String>) (s, primitiveSink) -> primitiveSink.putString(s, StandardCharsets.UTF_8))
                .build();
        System.out.println("开始初始化bloomFilter");
        for (int i = 0; i < 500000; i++) {
            bloomFilter.put(i + "");
        }
        System.out.println("完成初始化bloomFilter");
        for (int i = 0; i < 10; i++) {
            Random r = new Random();
            int asInt = r.ints(300000, (800000 + 1)).findFirst().getAsInt();
            boolean contain = bloomFilter.contain(asInt + "");
            System.out.println("判断" + asInt + "是否存在：" + contain);
        }
    }
}
