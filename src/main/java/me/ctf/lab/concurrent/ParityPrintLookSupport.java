package me.ctf.lab.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 奇偶打印，控制台随机输入一个正整数n，通过两个线程分别打印n+1、n+2、n+3、n+4、n+5、n+6，要求线程交替打印（即一个奇数线程，一个偶数线程），n的初始指为-1
 * @author chentiefeng
 * @date 2021/4/1 9:24
 */
public class ParityPrintLookSupport {
    static AtomicInteger num = new AtomicInteger(-1);
    static class Print extends Thread{
        public Thread next;
        @Override
        public void run(){
            for (int i = 0; i < 3; i++) {
                LockSupport.park();
                System.out.println(num.incrementAndGet());
                LockSupport.unpark(next);
            }
        }
    }

    public static void main(String[] args) throws Exception{
        Print js = new Print();
        Print os = new Print();
        js.next = os;
        os.next = js;
        js.start();
        os.start();
        Thread.sleep(10);
        num.set(5);
        LockSupport.unpark(os);
    }
}
