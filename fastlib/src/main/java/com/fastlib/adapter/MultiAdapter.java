package com.fastlib.adapter;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fastlib.base.OldViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 17/5/20.
 * 根据type来决定位置的多类型item适配器
 */
public abstract class MultiAdapter extends BaseAdapter{
    protected Context mContext;
    protected List<ItemGroup> mData;  //类型与索引对齐 类型0对于List(0)

    public MultiAdapter(Context context){
        mContext = context;
        mData =new ArrayList<>();
    }

    @Override
    public int getCount(){
        if(mData==null) return 0;
        int count=0;

        for(ItemGroup group:mData)
            if(!group.isSuspend)
                count+=group.mData.size();
        return count;
    }

    @Override
    public ItemMVC getItem(int position){
        return getItemByListPosition(position);
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public int getItemViewType(int position){
        ItemMVC item=getItemByListPosition(position);
        if(item==null) return -1;
        return item.getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ItemMVC item= getItem(position);
        OldViewHolder oldViewHolder=OldViewHolder.get(mContext,convertView,parent,item.getLayoutId());
        item.controlDataToView(position,item.getType(),oldViewHolder);
        return oldViewHolder.getConvertView();
    }

    /**
     * 获取指定列表位置的数据，这个方法不检查数组越界，所以要对传入的值做检查
     * @param position 列表绝对位置
     * @return 列表绝对位置的数据
     */
    private ItemMVC getItemByListPosition(int position){
        if(mData==null) return null;
        int count=0;

        for(ItemGroup group:mData){
            if(group.isSuspend) //如果item组隐藏，跳过这组
                continue;
            count+=group.mData.size();
            if(count>position)
                return (ItemMVC) group.mData.get(group.mData.size()-count+position);
        }
        return null;
    }

    /**
     * 检查item群类型是否存在，不存在建立当前和更小类型
     * @param type
     */
    private void checkGroup(int type){
        for(int i=mData.size();i<=type;i++)
            mData.add(new ItemGroup(i));
    }

    /**
     * 根据类型寻找列组
     * @param type
     * @return
     */
    public ItemGroup findGroupByType(int type){
        if(mData==null) return null;
        checkGroup(type);
        for(ItemGroup group:mData)
            if(type==group.type)
                return group;
        return null;
    }

    /**
     * 增加一个数据到列表中
     * @param dv
     */
    public void addData(ItemMVC dv){
        ItemGroup group=findGroupByType(dv.getType());
        if(group!=null)
            group.addData(dv);
        notifyDataSetChanged();
    }

    /**
     * 单类型列组
     * @param <T>
     */
    public class ItemGroup<T>{
        private boolean isSuspend;
        private int type;
        private List<ItemMVC<T>> mData;

        public ItemGroup(int type){
            mData=new ArrayList<>();
            this.type=type;
            isSuspend=false;
        }

        public boolean isEmpty(){
            return mData==null||mData.isEmpty();
        }

        public void addData(ItemMVC<T> t){
            mData.add(t);
        }

        public void remove(int position){
            mData.remove(position);
            notifyDataSetChanged();
        }

        public void remove(ItemMVC<T> t){
            if(mData.remove(t))
                notifyDataSetChanged();
        }

        public List<ItemMVC<T>> getItems(){
            return mData;
        }

        public void setIsSuspend(boolean suspend){
            isSuspend=suspend;
            notifyDataSetChanged();
        }
    }

    /**
     * 单类型列
     * @param <T>
     */
    public abstract class ItemMVC<T>{
        protected T mData;

        public ItemMVC(T data) {
            mData = data;
        }

        public abstract int getType();
        protected abstract int getLayoutId();

        public T getData(){
            return mData;
        }

        protected abstract void controlDataToView(int position,int type,OldViewHolder holder);
    }
}