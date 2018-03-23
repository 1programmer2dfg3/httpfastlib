package com.fastlib.app.task;

/**
 * Created by sgfb on 17/9/7.
 * 没有返回的事件
 */
public abstract class NoReturnAction<P> extends Action<P,Object>{

    /**
     * 适配没有返回的调起事件
     * @param param 参数
     */
    public abstract void executeAdapt(P param);

    @Override
    protected Object execute(P param){
        executeAdapt(param);
        return null;
    }
}