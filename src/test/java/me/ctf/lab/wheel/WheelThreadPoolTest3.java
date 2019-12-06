package me.ctf.lab.wheel;

import me.ctf.lab.wheel.threadpool.WheelThreadPool3;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chentiefeng[chentiefeng@linzikg.com]
 * @date 2019/12/04 09:47
 */
public class WheelThreadPoolTest3 {
    public static void main(String[] args) throws InterruptedException {
        WheelThreadPool3 threadPool = new WheelThreadPool3(2, 5, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5), "WheelThreadPool");
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            threadPool.execute(() -> System.out.println(Thread.currentThread().getName() + "：线程运行结束：" + finalI));
        }
        System.out.println("线程池开始");
        //睡眠1秒让全部线程跑完
        TimeUnit.MILLISECONDS.sleep(1000);
        //查看当前还存在的线程
        Thread.currentThread().getThreadGroup().list();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            if(!threadEntry.getKey().getName().startsWith("WheelThreadPool")){
                continue;
            }
            System.out.println("第一批，线程："+threadEntry.getKey().getName() + "，状态：" + threadEntry.getKey().getState());
            for (StackTraceElement stackTraceElement : threadEntry.getValue()) {
                System.out.println(stackTraceElement);
            }
        }
        //再来一次
        System.out.println("再来一次");
        for (int i = 10; i < 15; i++) {
            int finalI = i;
            threadPool.execute(() -> System.out.println(Thread.currentThread().getName() + "：线程运行结束：" + finalI));
        }
        System.out.println("查看执行完第二批5个线程情况");
        TimeUnit.MILLISECONDS.sleep(1000);
        Thread.currentThread().getThreadGroup().list();

        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            if(!threadEntry.getKey().getName().startsWith("WheelThreadPool")){
                continue;
            }
            System.out.println("第二批，线程："+threadEntry.getKey().getName() + "，状态：" + threadEntry.getKey().getState());
            for (StackTraceElement stackTraceElement : threadEntry.getValue()) {
                System.out.println(stackTraceElement);
            }
        }

        System.out.println("6秒后再查看一次线程情况");
        TimeUnit.MILLISECONDS.sleep(6000);
        Thread.currentThread().getThreadGroup().list();
    }
}
