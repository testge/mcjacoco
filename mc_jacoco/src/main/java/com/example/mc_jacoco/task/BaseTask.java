package com.example.mc_jacoco.task;

/**
 * @author luping
 * @date 2023/12/2 17:59
 */
public interface BaseTask extends Runnable {

    /**
     * 获取表达式
     * @return
     */
    String getCorn();

    /**
     * 执行逻辑
     */
    void execute();
}
