package com.fastlib.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fastlib.base.CommonViewHolder;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 17/10/5.
 * 多类型RecyclerView适配器
 */
public abstract class MultiTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private int mIncreaseType; //递加group类型值
    protected Context mContext;
    protected List<RecyclerGroup> mRecyclerGroup; //索引值对应类型,一个类型一个group
    protected List<RecyclerItem> mRecyclerItem;

    public MultiTypeAdapter(Context context){
        mIncreaseType =0;
        mContext=context;
        mRecyclerGroup=new ArrayList<>();
        mRecyclerItem =new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return mRecyclerItem.get(position).mGroup.mType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(mRecyclerGroup==null) return null;
        for(RecyclerGroup group:mRecyclerGroup){
            if(group.mType==viewType) return new CommonViewHolder(LayoutInflater.from(mContext).inflate(group.mLayoutId,parent,false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecyclerItem item= mRecyclerItem.get(position);
        item.mGroup.binding(position,item.mGroup.mItem.indexOf(item),item.mData,(CommonViewHolder) holder);
    }

    @Override
    public int getItemCount(){
        return mRecyclerItem ==null?0: mRecyclerItem.size();
    }

    /**
     * 增加适配器类型组到尾部
     * @param group 类型组
     */
    public void addGroup(RecyclerGroup group){
        if(mRecyclerGroup.contains(group)) return; //如果存在，跳过
        if(group.mType==0)
            group.mType=++mIncreaseType;
        if(group.getClass().getGenericSuperclass() instanceof ParameterizedType){
            ParameterizedType pt= (ParameterizedType) group.getClass().getGenericSuperclass();
            if(group.getCount()<=0&&pt.getActualTypeArguments().length>0&&(pt.getActualTypeArguments()[0]==Object.class||pt.getActualTypeArguments()[0]==Void.class))
                group.addData(null);
        }
        mRecyclerGroup.add(group);
        group.setMultiAdapter(this);
        notifyDataSetChanged();
    }

    /**
     * 增加适配器类型组在具体位置
     * @param group 类型组
     * @param index 插入具体位置
     */
    public void addGroup(RecyclerGroup group,int index){
        if(mRecyclerGroup.contains(group)) return; //如果存在，跳过
        if(group.mType==0)
            group.mType=++mIncreaseType;
        //特殊类型自动添加一个占位
        if(group.getClass().getGenericSuperclass() instanceof ParameterizedType){
            ParameterizedType pt= (ParameterizedType) group.getClass().getGenericSuperclass();
            if(group.getCount()<=0&&pt.getActualTypeArguments().length>0&&(pt.getActualTypeArguments()[0]==Object.class||pt.getActualTypeArguments()[0]==Void.class))
                group.addData(null);
        }
        mRecyclerGroup.add(index,group);
        group.setMultiAdapter(this,index);
        notifyDataSetChanged();
    }

    public List<RecyclerItem> getRecyclerItem(){
        return mRecyclerItem;
    }

    /**
     * 删除适配器类型组
     * @param group
     */
    public void removeGroup(RecyclerGroup group){
        mRecyclerGroup.remove(group);
        group.setMultiAdapter(null);
        notifyDataSetChanged();
    }

    /**
     * 根据类型获取组
     * @param type
     * @return
     */
    public RecyclerGroup getGroupByType(int type){
        if(mRecyclerGroup==null||mRecyclerGroup.isEmpty()) throw new IllegalArgumentException("Group不存在");
        if(mRecyclerGroup.size()<=type) throw new IllegalArgumentException("type值不存在");
        return mRecyclerGroup.get(type);
    }

    /**
     * 清理所有数据
     */
    public void clear(){
        for(RecyclerGroup group:mRecyclerGroup)
            group.clear();
    }

    /**
     * 关联RecyclerView内部类
     * 控制一个类型的集群和特殊属性（可折叠和折叠状态等）
     * @param <T> 对应类型组的数据实体类型。泛型如果是Object或者Void，则默认是空数据视图填充一个null数据
     */
    public static abstract class RecyclerGroup<T>{
        private boolean mCanSuspend; //是否可折叠
        private boolean isSuspend; //折叠状态
        private int mType;
        private int mLayoutId;
        private int mSuspendIndex=-1; //保留折叠位置
        private MultiTypeAdapter mMultiAdapter;
        private List<RecyclerItem<T>> mItem;

        /**
         * 绑定视图
         * @param positionOfRecyclerView 列表中绝对位置
         * @param positionOfGroup 相对Group中的位置
         * @param holder 视图持有者
         */
        protected abstract void binding(int positionOfRecyclerView, int positionOfGroup,T data,CommonViewHolder holder);
        protected abstract @LayoutRes int getLayoutId();

        public RecyclerGroup(){
            this(null);
        }

        public RecyclerGroup(MultiTypeAdapter adapter){
            mItem =new ArrayList<>();
            mMultiAdapter=adapter;
            mLayoutId =getLayoutId();
        }

        /**
         * 是否开启折叠
         * @param enable 如果是true支持展开，否则不支持展开
         */
        public void setSuspandEnable(boolean enable){
            if(enable==mCanSuspend) return;
            mCanSuspend=enable;
            if(mCanSuspend){ //如果改变为支持折叠，重排序成此类型的item连在一起
                if(mMultiAdapter!=null&&!mItem.isEmpty()&&mItem.size()>1){
                    List<RecyclerItem> allItem=mMultiAdapter.getRecyclerItem();
                    int firstItemIndex= allItem.indexOf(mItem.get(0));
                    for(int i=1;i<mItem.size();i++){
                        RecyclerItem item=mItem.get(i);
                        allItem.remove(item);
                        allItem.add(firstItemIndex+1,item);
                    }
                    mMultiAdapter.notifyDataSetChanged();
                }
            }
        }

        /**
         * 如果收拢则展开否则收拢
         */
        public void suspend(){
            suspend(!isSuspend);
        }

        /**
         * 收拢或展开
         * @param suspend 如果是true则收拢否则展开
         */
        public void suspend(boolean suspend){
            if(isSuspend==suspend) return;
            isSuspend=suspend;
            if(mMultiAdapter!=null){
                List<RecyclerItem> allItem=mMultiAdapter.getRecyclerItem();
                if(isSuspend){
                    if(!mItem.isEmpty()){
                        mSuspendIndex= allItem.indexOf(mItem.get(0));
                        allItem.removeAll(mItem);
                    }
                }
                else{
                    if(!mItem.isEmpty()){
                        if(mSuspendIndex==-1) allItem.addAll(mItem);
                        else allItem.addAll(mSuspendIndex, mItem);
                    }
                }
                mMultiAdapter.notifyDataSetChanged();
            }
        }

        /**
         * 增加一行
         * @param data
         */
        public void addData(T data){
            RecyclerItem<T> item=new RecyclerItem<>(data,this);
            mItem.add(item);
            if(mMultiAdapter!=null){
                List<RecyclerItem> allItem=mMultiAdapter.getRecyclerItem();
                if(mCanSuspend&&!mItem.isEmpty())
                    allItem.add(allItem.indexOf(mItem.get(allItem.size()))+1,item);
                else allItem.add(item);
                if(!isSuspend)
                    mMultiAdapter.notifyDataSetChanged();
            }
        }

        public void setData(int position,T data){
            mItem.get(position).mData=data;
            if(mMultiAdapter!=null) mMultiAdapter.notifyDataSetChanged();
        }

        /**
         * 增加多行
         * @param list
         */
        public void addAllData(List<T> list){
            if(list==null||list.isEmpty()) return;
            for(T t:list){
                RecyclerItem<T> item=new RecyclerItem<>(t,this);
                mItem.add(item);
                if(mMultiAdapter!=null){
                    List<RecyclerItem> allItem=mMultiAdapter.getRecyclerItem();
                    //如果支持折叠，item以组分割
                    if(mCanSuspend){
                        //插入到下个组的前面，如果没有下个组，或者下个组的item列表为空，则插入到总item列表的最后一个
                        int nextGroupIndex=mMultiAdapter.mRecyclerGroup.indexOf(this)+1;
                        if(nextGroupIndex<mMultiAdapter.mRecyclerGroup.size()){
                            RecyclerGroup nextGroup=mMultiAdapter.mRecyclerGroup.get(nextGroupIndex);
                            if(nextGroup.mItem!=null&&!nextGroup.mItem.isEmpty()){
                                int firstItem=allItem.indexOf(nextGroup.mItem.get(0));
                                allItem.add(firstItem,item);
                            }
                            else allItem.add(item);
                        }
                        else allItem.add(item);
                    }
                    else allItem.add(item);
                }
            }
            if(mMultiAdapter!=null)
                mMultiAdapter.notifyDataSetChanged();
        }

        public void setData(List<T> list){
            if(mMultiAdapter!=null){
                mMultiAdapter.getRecyclerItem().removeAll(mItem);
                mItem.clear();
            }
            if(list!=null&&!list.isEmpty())
                addAllData(list);
            else{
                if(mMultiAdapter!=null)
                    mMultiAdapter.notifyDataSetChanged();
            }
        }

        /**
         * 获取行总数
         * @return 行总数
         */
        public int getCount(){
            return mItem.size();
        }

        /**
         * 获取类型
         * @return 类型
         */
        public int getType(){
            return mType;
        }

        /**
         * 获取所有映射数据
         * @return 所有映射数据
         */
        public List<T> getCopyData(){
            List<T> list=new ArrayList<>();
            for(RecyclerItem<T> item:mItem)
                list.add(item.mData);
            return list;
        }

        public void setMultiAdapter(MultiTypeAdapter adapter){
            setMultiAdapter(adapter,adapter==null?0:adapter.getRecyclerItem().size());
        }

        public void setMultiAdapter(MultiTypeAdapter adapter,int index){
            if(mMultiAdapter!=null)
                mMultiAdapter.getRecyclerItem().removeAll(mItem);
            if(adapter!=null){
                mMultiAdapter=adapter;
                mMultiAdapter.getRecyclerItem().addAll(index,mItem);
            }
        }

        public void clear(){
            if(mMultiAdapter!=null)
                mMultiAdapter.getRecyclerItem().removeAll(mItem);
            mItem.clear();
        }
    }

    /**
     * 列表元素最小单位
     * @param <T>
     */
    public static class RecyclerItem<T> {
        T mData;
        RecyclerGroup mGroup;

        public RecyclerItem(T data, RecyclerGroup group) {
            mData = data;
            mGroup = group;
        }
    }
}
