package me.ctf.lab.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chentiefeng
 * @date 2021/1/27 10:53
 */
public class Order123PrintLock {


    public static void main(String[] args) throws InterruptedException {
        Lock lock = new ReentrantLock();
        AtomicInteger state = new AtomicInteger(0);
        new Thread(() -> {
            for (int i = 0; i < 10; ) {
                lock.lock();
                try {
                    while (state.get() % 3 == 0) {
                        System.out.println("1");
                        state.incrementAndGet();
                        i++;
                    }
                } finally {
                    lock.unlock();
                }

            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 10; ) {
                lock.lock();
                try {
                    while (state.get() % 3 == 1) {
                        System.out.println("2");
                        state.incrementAndGet();
                        i++;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 10; ) {
                lock.lock();
                try {
                    while (state.get() % 3 == 2) {
                        System.out.println("3");
                        state.incrementAndGet();
                        i++;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }).start();
    }
}
