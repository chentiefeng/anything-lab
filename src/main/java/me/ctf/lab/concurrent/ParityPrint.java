package me.ctf.lab.concurrent;

/**
 * 奇偶打印，随机传入一个正整数，通过两个线程交替+1打印奇偶数，每个线程打印3次
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-30 17:18
 */
public class ParityPrint {
    private static int i = -1;

    public static void main(String[] args) throws InterruptedException {
        Thread oddThread = new Thread(new OddNumber());
        Thread evenThread = new Thread(new EvenNumber());
        evenThread.start();
        oddThread.start();
        System.out.println("主线程开始等待1秒");
        Thread.sleep(1000);
        System.out.println("主线程开始等待1秒结束");
        synchronized (ParityPrint.class) {
            i = 45;
            ParityPrint.class.notifyAll();
            System.out.println("主线程唤醒所有线程");
        }
    }

    /**
     * 奇数线程
     */
    static class OddNumber implements Runnable {
        @Override
        public void run() {
            for (int j = 0; j < 3; j++) {
                synchronized (ParityPrint.class) {
                    //小于0或者当前是奇数都等待
                    while (i < 0 || (i & 1) == 1) {
                        try {
                            System.out.println("奇数线程等待中");
                            ParityPrint.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("奇数线程获得锁");
                    }
                    //偶数的话+1打印
                    System.out.println("奇数线程打印：" + (++i));
                    ParityPrint.class.notify();
                }
            }
        }
    }

    /**
     * 偶数线程
     */
    static class EvenNumber implements Runnable {
        @Override
        public void run() {
            for (int j = 0; j < 3; j++) {
                synchronized (ParityPrint.class) {
                    //小于0或者当前是偶数都等待
                    while (i < 0 || (i & 1) == 0) {
                        try {
                            System.out.println("偶数线程等待中");
                            ParityPrint.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("偶数线程获得锁");
                    }
                    //奇数的话+1打印
                    System.out.println("偶数线程打印：" + (++i));
                    ParityPrint.class.notify();
                }
            }
        }
    }
}
