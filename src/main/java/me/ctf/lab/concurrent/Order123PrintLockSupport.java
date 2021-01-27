package me.ctf.lab.concurrent;

import java.util.concurrent.locks.LockSupport;

/**
 * @author chentiefeng
 * @date 2021/1/27 10:53
 */
public class Order123PrintLockSupport {

    static class Print implements Runnable {
        private String out;
        private Thread next;

        public Print(String out) {
            this.out = out;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                LockSupport.park();
                System.out.print(out);
                LockSupport.unpark(next);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Print target1 = new Print("1");
        Print target2 = new Print("2");
        Print target3 = new Print("3");
        Thread t1 = new Thread(target1);
        Thread t2 = new Thread(target2);
        Thread t3 = new Thread(target3);
        target1.next = t2;
        target2.next = t3;
        target3.next = t1;
        t1.start();
        t2.start();
        t3.start();
        Thread.sleep(10);
        LockSupport.unpark(t1);
    }
}
