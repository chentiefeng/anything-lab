package me.ctf.lab.wheel;

import me.ctf.lab.wheel.threadpool.WheelThreadPool2;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chentiefeng[chentiefeng@linzikg.com]
 * @date 2019/12/04 09:47
 */
public class WheelThreadPoolTest2 {
    public static void main(String[] args) {
        WheelThreadPool2 threadPool = new WheelThreadPool2(2, 5, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5), "WheelThreadPool");
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                System.out.println(Thread.currentThread().getName() + "：线程运行结束：" + finalI);
            });
        }
    }
}
