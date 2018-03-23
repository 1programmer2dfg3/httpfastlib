package com.fastlib.app.task;

/**
 * Created by sgfb on 17/9/7.
 * 没有参数和返回的事件
 */
public abstract class  EmptyAction extends Action{

    /**
     * 适配方法，去掉了参数
     */
    protected abstract void executeAdapt();

    @Override
    protected Object execute(Object param){
        executeAdapt();
        return null;
    }
}