package me.ctf.lab.collections;

import me.ctf.lab.wheel.collections.WheelHashMap;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-25 14:02
 */
public class WheelHashMapTest {
    public static void main(String[] args) {
//        solution1();
//        solution2();
//        solution3();
        solution4();
    }

    private static void solution1() {
        WheelHashMap<Integer, Integer> hashMap = new WheelHashMap<>();
        for (int i = 0; i < 16; i++) {
            hashMap.put(i, i);
        }
        for (int i = 0; i < 16; i++) {
            System.out.println(hashMap.get(i));
        }
    }

    private static void solution2() {
        WheelHashMap<Class1, Integer> hashMap = new WheelHashMap<>();
        Class1 class1 = new Class1(1);
        Class1 class2 = new Class1(2);
        hashMap.put(class1, 1);
        hashMap.put(class2, 2);
        System.out.println(hashMap.get(class2));
        System.out.println(hashMap.get(class1));
    }

    private static void solution3() {
        //5--->21--->37--->53---->null
        WheelHashMap<Class2, Integer> hashMap = new WheelHashMap<>();
        Class2 class1 = new Class2(5);
        Class2 class2 = new Class2(21);
        Class2 class3 = new Class2(37);
        Class2 class4 = new Class2(53);
        hashMap.put(class1, 5);
        hashMap.put(class2, 21);
        hashMap.put(class3, 37);
        hashMap.put(class4, 53);
        for (int i = 1; i <= 10; i++) {
            Class2 class5 = new Class2(i * 100);
            hashMap.put(class5, i * 100);
        }
        System.out.println(hashMap.get(class4));
        System.out.println(hashMap.get(class3));
        System.out.println(hashMap.get(class2));
        System.out.println(hashMap.get(class1));
    }

    private static void solution4() {
        WheelHashMap<String, String> hashMap = new WheelHashMap<>();
        String s = "Alabama：阿拉巴马州,Alaska：阿拉斯加,Arizona：亚利桑那州,Arkansas：阿肯色州," +
                "California：加利福尼亚州,Colorado：科罗拉多州,Connecticut：康涅狄格州,Delaware：特拉华州," +
                "Florida：佛罗里达州,Georgia：乔治亚州,Hawaii：夏威夷,Idaho：爱达荷州," +
                "Illinois：伊利诺伊州,Indiana：印第安纳州,Iowa：爱荷华州,Kansas：堪萨斯州";
        String[] split = s.split(",");
        for (String s1 : split) {
            System.out.println(s1);
            hashMap.put(s1.split("：")[0], s1.split("：")[1]);
        }
        System.out.println("==============================");
        for (String s1 : split) {
            System.out.println(s1.split("：")[0] + "：" + hashMap.get(s1.split("：")[0]));
        }
    }

    static class Class1 {
        int i;

        @Override
        public int hashCode() {
            return 5;
        }

        public Class1(int i) {
            this.i = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Class1 class1 = (Class1) o;
            return i == class1.i;
        }

        @Override
        public String toString() {
            return "Class1{" +
                    "i=" + i +
                    '}';
        }
    }

    static class Class2 {
        int i;

        @Override
        public int hashCode() {
            return i;
        }

        public Class2(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return "Class2{" +
                    "i=" + i +
                    '}';
        }
    }
}
