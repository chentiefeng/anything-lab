package me.ctf.lab.wheel;

import me.ctf.lab.wheel.threadpool.WheelThreadPool3;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-05 16:32
 */
public class ThreadPoolExecutorTest {
    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 5, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5));
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            threadPool.execute(() -> System.out.println(Thread.currentThread().getName() + "：线程运行：" + finalI));
        }
        System.out.println("线程池开始");
        //睡眠1秒让全部线程跑完
        TimeUnit.MILLISECONDS.sleep(1000);
        //查看当前还存在的线程
        Thread.currentThread().getThreadGroup().list();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            if(!threadEntry.getKey().getName().startsWith("pool-1")){
                continue;
            }
            System.out.println("第一批，线程："+threadEntry.getKey().getName() + "，状态：" + threadEntry.getKey().getState());
            for (StackTraceElement stackTraceElement : threadEntry.getValue()) {
                System.out.println(stackTraceElement);
            }
        }
        threadPool.shutdown();
    }
}
