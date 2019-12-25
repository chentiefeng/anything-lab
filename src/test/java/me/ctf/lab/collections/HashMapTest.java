package me.ctf.lab.collections;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-25 08:57
 */
public class HashMapTest {
    public static void main(String[] args) {
//        Class1 c = new Class1(1);
//        Class1 c2 = new Class1(2);
//        Map<Class1,String> map = new HashMap<>();
//        map.put(c,"1");
//        map.put(c2,"2");
//        System.out.println(map);
//        System.out.println(map.get(c2));
//        System.out.println(map.get(c));
        System.out.println(Integer.toBinaryString(21));
        System.out.println(Integer.toBinaryString(16));
        System.out.println(21&16);
    }
    static class Class1{
        int i;
        @Override
        public boolean equals(Object obj) {
            return true;
        }

        @Override
        public int hashCode() {
            return i;
        }

        public Class1(int i) {
            this.i = i;
        }
    }
}
