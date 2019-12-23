package me.ctf.lab.gc;

import java.util.ArrayList;
import java.util.List;

/**
 * -Xms41m -Xmx41m -Xmn10m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-16 09:39
 */
public class GcTest {
    private static final int _1MB = 1024 * 1024;
    public static void main(String[] args) {
        System.out.println("0.---");
        List caches = new ArrayList();
        for (int i = 0; i < 11; i++){
            System.out.println("0.add="+i);
            caches.add(new byte[1*_1MB]);
        }
//        System.out.println("1.---");
//        caches.add(new byte[3 * _1MB]);
//        caches.remove(0);
//        caches.add(new byte[3 * _1MB]);
//        for (int i = 0; i < 8; i++) {
//            caches.remove(0);
//        }
//        caches.add(new byte[3 * _1MB]);
//        System.out.println("2.---");
//        for (int i = 0; i < 7; i++){
//            caches.add(new byte[3 * _1MB]);
//        }
    }
}
