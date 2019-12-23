package me.ctf.lab.gc;

/**
 * -Xms60m -Xmx60m -Xmn20m -XX:NewRatio=2 -XX:SurvivorRatio=8 -XX:+PrintGCDetails
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-16 10:18
 */
public class TestVm {
    public void doTest(){



        Integer M = new Integer(1024 * 1024 * 1); //单位, 兆(M)
        byte[] bytes = new byte[1 * M]; //申请 1M 大小的内存空间
        bytes = null; //断开引用链
        System.gc(); //通知 GC 收集垃圾
        System.out.println(); //
        bytes = new byte[1 * M]; //重新申请 1M 大小的内存空间
        bytes = new byte[1 * M]; //再次申请 1M 大小的内存空间
        System.gc();
        System.out.println();
    }
    public static void main(String[] args) {
        new TestVm().doTest();
    }
}
