package com.fastlib.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fastlib.base.OldViewHolder;
import com.fastlib.base.Refreshable;
import com.fastlib.net.Listener;
import com.fastlib.net.Request;
import com.fastlib.utils.json.FastJson;
import com.fastlib.utils.json.JsonViewBinder;
import com.fastlib.utils.json.JsonObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by sgfb on 17/1/9.
 * json键名对视图id映射绑定
 */
public abstract class JsonAdapter extends BaseAdapter implements Listener<String,Object,Object>{
    public static final String TAG=JsonAdapter.class.getSimpleName();

    private boolean isRefresh,isLoading,isMore;
    private int mLayoutId;
    protected Context mContext;
    protected Request mRequest;
    protected Refreshable mRefreshable;
    protected JsonViewBinder mBinder;
    protected List<JsonObject> mData;

    public abstract List<JsonObject> translate(JsonObject raw);
    public abstract Request generateRequest();
    public abstract void getMoreRequest();
    public abstract void getRefreshRequest();

    public JsonAdapter(Context context, @LayoutRes int layoutId, boolean startNow){
        mContext=context;
        mLayoutId=layoutId;
        mBinder=new JsonViewBinder(mContext,LayoutInflater.from(context).inflate(layoutId,null));
        mRequest=generateRequest();
        if(mRequest!=null){
            mRequest.setListener(this);
            mRequest.setGenericType(new Type[]{String.class});
        }
        if(startNow)
            refresh();
    }

    /**
     * 可选的绑定视图,在json绑定之后调用
     * @param position
     * @param holder
     */
    protected void binding(int position,OldViewHolder holder){}

    /**
     * 手动刷新,强制使用新数据
     */
    public void refresh(){
        refresh(true);
    }

    protected void refresh(boolean force){
        isRefresh=true;
        isMore=true;
        isLoading=true;
        getRefreshRequest();
        if(mRequest!=null)
            mRequest.start(force);
    }

    private void loadMore(){
        isRefresh=false;
        isLoading=true;
        getMoreRequest();
        if(mRequest!=null)
            mRequest.start(true);
    }

    @Override
    public int getCount() {
        return mData==null?0:mData.size();
    }

    @Override
    public JsonObject getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(!isLoading&&isMore&&position>=getCount()-1)
            loadMore();
        OldViewHolder holder=OldViewHolder.get(mContext,convertView,parent,mLayoutId);
        mBinder.bindDataToView(getItem(position),holder);
        return holder.getConvertView();
    }

    @Override
    public void onResponseListener(Request r,String result,Object object,Object object2){
        isLoading=false;
        if(mRefreshable!=null)
            mRefreshable.setRefreshStatus(false);
        try {
            List<JsonObject> list=translate(FastJson.fromJson(result));
            if(list==null||list.isEmpty()){
                isMore=false;
                return;
            }
            if(isRefresh)
                mData=list;
            else{
                if(mData==null)
                    mData=list;
                else
                    mData.addAll(list);
            }
            notifyDataSetChanged();
        } catch (IOException e){
            Log.d(TAG,"解析json异常:"+e);
        }
    }

    @Override
    public void onErrorListener(Request r, String error){
        isLoading=false;
        if(mRefreshable!=null)
            mRefreshable.setRefreshStatus(false);
        Log.d(TAG,"网络请求响应异常:"+error);
    }

    public void addData(JsonObject jo){
        mData.add(jo);
        notifyDataSetChanged();
    }

    public void addData(List<JsonObject> list){
        mData.addAll(list);
        notifyDataSetChanged();
    }
}