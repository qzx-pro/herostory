package com.qzx.herostory.async;

import com.qzx.herostory.MainMsgHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: qzx
 * @date: 2021/2/8 - 02 - 08 - 15:08
 * @description: 异步处理器
 * @version: 1.0
 */
public class AsyncOperationProcessor {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);

    /**
     * 单例对象
     */
    private static final AsyncOperationProcessor ASYNC_OPERATION_PROCESSOR = new AsyncOperationProcessor();

    /**
     * 构建单线程线程数组
     */
    private final ExecutorService[] esArray = new ExecutorService[8];

    /**
     * 私有化构造方法，实例化单线程数组
     */
    private AsyncOperationProcessor() {
        for (int i = 0; i < esArray.length; ++i) {
            String threadName = "AsyncOperationProcessor [ " + i + " ]";
            esArray[i] = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    (runnable) -> {
                        Thread thread = new Thread(runnable);
                        // 设置线程的名字
                        thread.setName(threadName);
                        return thread;
                    });
        }
    }

    /**
     * 获取AsyncOperationProcessor单例对象
     *
     * @return AsyncOperationProcessor单例对象
     */
    public static AsyncOperationProcessor getInstance() {
        return ASYNC_OPERATION_PROCESSOR;
    }

    /**
     * 执行任务
     *
     * @param r 任务
     */
    public void process(IAsyncOperation r) {
        // 获取绑定的id
        int bindId = r.getBindId();
        // 使用绑定的线程执行
        int index = bindId % esArray.length;
        esArray[index].submit(() -> {
            // 执行异步逻辑
            r.doAsync();
            // 执行回调逻辑(业务逻辑)
            MainMsgHandler.process(r::doFinish);
        });
    }

}
