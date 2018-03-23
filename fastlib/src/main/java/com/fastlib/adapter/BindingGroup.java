package com.fastlib.adapter;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import com.fastlib.adapter.MultiTypeAdapter;
import com.fastlib.base.CommonViewHolder;
import com.fastlib.net.Listener;
import com.fastlib.net.Request;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author sgfb on 18/2/6.
 * 多类型适配器中的分页支持群
 * @param <T> 适配器实体类
 * @param <R> 接口返回类型
 */
public abstract class BindingGroup<T,R> extends MultiTypeAdapter.RecyclerGroup<T> implements Listener<R,Object,Object>{
    protected boolean isRefresh,isMore,isLoading;
    protected Request mRequest;
    protected SwipeRefreshLayout mRefreshLayout;

    public BindingGroup(){
        this(true);
    }

    public BindingGroup(boolean startNow){
        isRefresh=true;
        isMore=true;
        isLoading=false;
        mRequest=initRequest();
        mRequest.setGenericType(new Type[]{getResponseType()});
        mRequest.setListener(this);
        if(startNow) refresh();
    }

    /**
     * 初始化网络请求
     * @return 基础网络请求
     */
    protected abstract @NonNull Request initRequest();

    /**
     * 刷新之前回调
     * @param request 当前适配器的请求
     */
    protected abstract void requestRefreshBefore(Request request);

    /**
     * 读取下一页回调
     * @param request 当前适配器的网络请求
     */
    protected abstract void requestMoreBefore(Request request);

    /**
     * 通过解释来适配接口与适配器不同实体类
     * @return 适配器实体类列表
     */
    protected abstract List<T> translate(R responseData);

    /**
     * 自动分页后绑定数据
     * @param positionOfRecyclerView 在整个RecyclerView中的索引位置
     * @param positionOfGroup 在当前组中的索引位置
     * @param data 数据
     * @param holder 视图持有者
     */
    protected abstract void rebinding(int positionOfRecyclerView, int positionOfGroup, T data, CommonViewHolder holder);

    /**
     * 刷新数据
     */
    public void refresh(){
        isRefresh=true;
        isMore=true;
        isLoading=true;
        requestRefreshBefore(mRequest);
        mRequest.start();
    }

    /**
     * 加载更多
     */
    public void loadMore(){
        isRefresh=false;
        isLoading=true;
        requestMoreBefore(mRequest);
        mRequest.start();
    }

    @Override
    protected void binding(int positionOfRecyclerView, int positionOfGroup, T data, CommonViewHolder holder) {
        rebinding(positionOfRecyclerView,positionOfGroup,data,holder);
        if(positionOfGroup>=getCount()-1&&isMore&&!isLoading)
            loadMore();
    }

    @Override
    public void onResponseListener(Request r, R result, Object result2, Object cookedResult){
        List<T> data=translate(result);

        if(data==null||data.isEmpty())
            isMore=false;
        else{
            if(isRefresh)
                setData(data);
            else addAllData(data);
        }
        if(mRefreshLayout!=null) mRefreshLayout.setRefreshing(false);
        isLoading=false;
    }

    @Override
    public void onErrorListener(Request r, String error){
        if(mRefreshLayout!=null) mRefreshLayout.setRefreshing(false);
        isLoading=false;
    }

    @Override
    public void onRawData(Request r, byte[] data) {
        //被适配
    }

    @Override
    public void onTranslateJson(Request r, String json) {
        //被适配
    }

    /**
     * 生成返回实体类型
     * @return 返回实体类型
     */
    private Type getResponseType(){
        Method[] methods=getClass().getDeclaredMethods();
        for(Method m:methods)
            if(m.getName().equals("translate")){
                Type type=m.getGenericParameterTypes()[0];
                if(type!=Object.class)
                    return type;
            }
        return null;
    }

    public void setRefreshLayout(SwipeRefreshLayout refreshLayout){
        mRefreshLayout=refreshLayout;
    }

    public SwipeRefreshLayout getRefreshLayout(){
        return mRefreshLayout;
    }

    public Request getRequest(){
        return mRequest;
    }
}