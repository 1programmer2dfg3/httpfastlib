package com.fastlib.base;

/**
 * Created by sgfb on 16/9/21.
 * 实现此接口表示实现类支持刷新功能
 */
public interface Refreshable{

    /**
     * 设置刷新状态
     * @param status
     */
    void setRefreshStatus(boolean status);

    /**
     * 设置开始刷新回调
     * @param callback
     */
    void setRefreshCallback(RefreshCallback callback);

    interface RefreshCallback{
        void startRefresh();
    }
}
