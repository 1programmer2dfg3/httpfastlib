package com.fastlib.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fastlib.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 17/2/22.
 * Recycler单类型列表适配器
 */
public abstract class FastAdapterForRecycler<T> extends RecyclerView.Adapter<CommonViewHolder>{
    private int mItemId;
    private List<T> mData;
    protected Context mContext;

    public abstract void binding(int position,T data,CommonViewHolder holder);

    public FastAdapterForRecycler(Context context,int itemId){
        this(context,itemId,null);
    }

    public FastAdapterForRecycler(Context context,int itemId,List<T> data){
        mItemId = itemId;
        mData = data;
        mContext = context;
        if(mData==null)
            mData=new ArrayList<>();
    }

    public T getItemAtPosition(int position){
        return mData.get(position);
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        binding(position,getItemAtPosition(position),holder);
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommonViewHolder(LayoutInflater.from(mContext).inflate(mItemId,parent,false));
    }

    @Override
    public int getItemCount() {
        return mData==null?0:mData.size();
    }

    public void setData(List<T> list){
        mData=list;
        notifyDataSetChanged();
    }

    /**
     * 插入数据
     * @param data 数据
     * @param position 插入位置
     * @param anim 是否显示插入动画效果
     */
    public void addData(T data,int position,boolean anim){
        mData.add(position,data);
        if(anim) notifyItemInserted(position);
        else notifyDataSetChanged();
    }

    /**
     * 插入到尾部，不开动画效果
     * @param data
     */
    public void addData(T data){
        mData.add(data);
        notifyDataSetChanged();
    }

    /**
     * 插入列表数据
     * @param data 数据
     * @param position 位置
     * @param anim 是否显示插入动画效果
     */
    public void addAllData(List<T> data,int position,boolean anim){
        mData.addAll(position,data);
        if(anim) notifyItemRangeInserted(position,data.size());
        else notifyDataSetChanged();
    }

    /**
     * 插入数据到尾部，不显示动画效果
     * @param data
     */
    public void addData(List<T> data){
        mData.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 删除某个数据，不显示动画效果
     * @param data
     */
    public void remove(T data){
        mData.remove(data);
        notifyDataSetChanged();
    }

    /**
     * 删除某个位置的数据，不显示动画效果
     * @param position
     */
    public void remove(int position){
        remove(position,false);
    }

    /**
     * 删除某个位置数据
     * @param position 位置
     * @param anim 是否显示动画
     */
    public void remove(int position,boolean anim){
        mData.remove(position);
        if(anim) notifyItemRemoved(position);
        else notifyDataSetChanged();
    }

    /**
     * 删除多个数据
     * @param position 起始位置
     * @param count 总数
     * @param anim 是否显示动画
     */
    public void remove(int position,int count,boolean anim){
        for(int i=0;i<count;i++)
            mData.remove(position);
        if(anim) notifyItemRangeRemoved(position,count);
        else notifyDataSetChanged();
    }

    public void clear(){
        if(mData!=null) {
            mData.clear();
            notifyDataSetChanged();
        }
    }

    public List<T> getData(){
        return mData;
    }
}