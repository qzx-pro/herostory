package com.qzx.herostory.async;

/**
 * @author: qzx
 * @date: 2021/2/8 - 02 - 08 - 16:24
 * @description: 异步操作接口
 * @version: 1.0
 */
public interface IAsyncOperation {
    /**
     * 获取当前线程绑定的id
     *
     * @return id
     */
    default int getBindId() {
        return 0;
    }

    /**
     * 异步执行逻辑
     */
    void doAsync();

    /**
     * 异步执行结束后的逻辑处理
     */
    default void doFinish() {

    }
}
