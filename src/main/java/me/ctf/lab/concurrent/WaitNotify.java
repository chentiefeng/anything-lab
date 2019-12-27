package me.ctf.lab.concurrent;

import java.util.Random;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-27 09:30
 */
public class WaitNotify {
    private static int STORE = 0;
    private static int MAX = 20;

    public static void main(String[] args) {
        new Thread(new Producer("Producer-1")).start();
        new Thread(new Producer("Producer-2")).start();

        new Thread(new Consumer("Consumer-1")).start();
        new Thread(new Consumer("Consumer-2")).start();
        new Thread(new Consumer("Consumer-3")).start();
        new Thread(new Consumer("Consumer-4")).start();
        new Thread(new Consumer("Consumer-5")).start();

    }

    static class Producer implements Runnable {
        private String name;

        Producer(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(name);
            while (true) {
                try {
                    Thread.sleep(new Random().nextInt(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (WaitNotify.class) {
                    if (STORE >= MAX) {
                        try {
                            System.out.println(Thread.currentThread().getName() + ",库存已满，等待中");
                            WaitNotify.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (STORE < MAX) {
                        STORE++;
                        System.out.println(Thread.currentThread().getName() + ",生产一个库存，现有数量:" + STORE);
                    }
                    WaitNotify.class.notifyAll();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        private String name;

        Consumer(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(name);
            while (true) {
                try {
                    Thread.sleep(new Random().nextInt(300));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (WaitNotify.class) {
                    if (STORE == 0) {
                        try {
                            System.out.println(Thread.currentThread().getName() + ",库存已空，等待中");
                            WaitNotify.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (STORE > 0) {
                        STORE--;
                        System.out.println(Thread.currentThread().getName() + ",消费第一个库存，现有数量:" + STORE);
                    }
                    WaitNotify.class.notifyAll();
                }
            }
        }
    }
}
