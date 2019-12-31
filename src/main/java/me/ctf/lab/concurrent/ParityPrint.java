package me.ctf.lab.concurrent;

import java.util.Scanner;

/**
 * 奇偶打印，控制台随机输入一个正整数n，通过两个线程分别打印n+1、n+2、n+3、n+4、n+5、n+6，要求线程交替打印（即一个奇数线程，一个偶数线程），n的初始指为-1
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-30 17:18
 */
public class ParityPrint {

    private volatile int i = -1;
    
    public static void main(String[] args) throws InterruptedException {
        ParityPrint parityPrint = new ParityPrint();
        Thread oddThread = buildOddThread(parityPrint);
        Thread evenThread = buildEvenThread(parityPrint);
        evenThread.start();
        oddThread.start();
        //暂停1秒，等待两个线程进入wait状态
        Thread.sleep(1000);
        System.out.print("请输入一个随机正整数：");
        Scanner input = new Scanner(System.in);
        int num = input.nextInt();
        synchronized (ParityPrint.class) {
            parityPrint.i = num;
            ParityPrint.class.notifyAll();
            System.out.println("主线程唤醒所有线程");
        }
        input.close();
    }

    /**
     * 偶数线程创建
     * 
     * @param parityPrint
     * @return
     */
    private static Thread buildEvenThread(ParityPrint parityPrint) {
        return new Thread(()->{
            for (int j = 0; j < 3; j++) {
                synchronized (ParityPrint.class) {
                    //小于0或者当前是偶数都等待
                    while (parityPrint.i < 0 || (parityPrint.i & 1) == 0) {
                        try {
                            System.out.println("偶数线程等待中");
                            ParityPrint.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("偶数线程获得锁");
                    }
                    //奇数的话+1打印
                    System.out.println("偶数线程打印：" + (++parityPrint.i));
                    ParityPrint.class.notify();
                }
            }
        });
    }

    /**
     * 奇数线程创建
     * 
     * @param parityPrint
     * @return
     */
    private static Thread buildOddThread(ParityPrint parityPrint) {
        return new Thread(()->{
            for (int j = 0; j < 3; j++) {
                synchronized (ParityPrint.class) {
                    //小于0或者当前是奇数都等待
                    while (parityPrint.i < 0 || (parityPrint.i & 1) == 1) {
                        try {
                            System.out.println("奇数线程等待中");
                            ParityPrint.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("奇数线程获得锁");
                    }
                    //偶数的话+1打印
                    System.out.println("奇数线程打印：" + (++parityPrint.i));
                    ParityPrint.class.notify();
                }
            }
        });
    }
}
