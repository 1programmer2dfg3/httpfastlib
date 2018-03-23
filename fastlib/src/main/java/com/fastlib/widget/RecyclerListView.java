package com.fastlib.widget;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.fastlib.widget.DividerItemDecoration;

/**
 * Created by sgfb on 16/8/22.
 * 类似与ListView的Recycler
 */
public class RecyclerListView extends RecyclerView{
    private LinearLayoutManager mLayoutManager;
    private DividerItemDecoration mDivider;
    private View mEmptyView;
    private AdapterDataObserver emptyObserver=new AdapterDataObserver(){

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            onChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            onChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            onChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            onChanged();
        }

        @Override
        public void onChanged() {
            Adapter<?> adapter =  getAdapter();
            if(mEmptyView!=null){
                if(adapter==null||adapter.getItemCount()<=0){
                    mEmptyView.setVisibility(View.VISIBLE);
                    setVisibility(View.GONE);
                }
                else{
                    mEmptyView.setVisibility(View.GONE);
                    setVisibility(View.VISIBLE);
                }
            }
        }
    };


    public RecyclerListView(Context context){
        super(context);
        init();
    }

    public RecyclerListView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){
        setLayoutManager(mLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        setItemAnimator(new DefaultItemAnimator());
        addItemDecoration(mDivider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
    }

    public void setEmptyView(View emptyView){
        mEmptyView=emptyView;
        if(getAdapter()!=null)
            getAdapter().registerAdapterDataObserver(emptyObserver);
        emptyObserver.onChanged();
    }

    @Override
    public void setAdapter(Adapter adapter){
        super.setAdapter(adapter);
        if(adapter!=null)
            adapter.registerAdapterDataObserver(emptyObserver);
        emptyObserver.onChanged();
    }

    public DividerItemDecoration getDivider(){
        return mDivider;
    }
}