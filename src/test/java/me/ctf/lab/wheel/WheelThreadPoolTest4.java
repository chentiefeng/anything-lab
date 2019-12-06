package me.ctf.lab.wheel;

import me.ctf.lab.wheel.threadpool.WheelThreadPool4;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chentiefeng[chentiefeng@linzikg.com]
 * @date 2019/12/04 09:47
 */
public class WheelThreadPoolTest4 {
    public static void main(String[] args) throws InterruptedException {
        WheelThreadPool4 threadPool = new WheelThreadPool4(2, 5, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5), "WheelThreadPool");
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            threadPool.execute(() -> System.out.println(Thread.currentThread().getName() + "：线程运行结束：" + finalI));
        }
        System.out.println("线程池开始");
        //睡眠1秒让全部线程跑完
        TimeUnit.MILLISECONDS.sleep(1000);
        System.out.println("睡眠1秒，让所有线程运行完成");
        threadPool.stop();
        System.out.println("线程池优雅结束第2种情况");
        //睡眠1秒让全部线程跑完
        TimeUnit.MILLISECONDS.sleep(1000);
        //查看当前还存在的线程
        Thread.currentThread().getThreadGroup().list();
    }
}
