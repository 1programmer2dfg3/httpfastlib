package com.fastlib.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

/**
 * 偷懒版SwipeRefreshLayout.可以包裹非AbsListView正常使用
 */
public class FastSwipeRefresh extends FrameLayout{
    private RefreshLayout mRefresh;
    private StateListView mDefaultListView;

    public FastSwipeRefresh(Context context, AttributeSet attrs) {
        super(context,attrs);
        mRefresh=new RefreshLayout(context,attrs);
        mDefaultListView=new StateListView(context);
        mRefresh.addView(mDefaultListView);
        addView(mRefresh);
        setOnHierarchyChangeListener(new OnHierarchyChangeListener(){ //当视图树变动时,使refresh始终显示在最上层
            @Override
            public void onChildViewAdded(View parent, View child) {
                if(child!=mRefresh){
                    removeView(mRefresh);
                    addView(mRefresh);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
    }

    public RefreshLayout getRefresh(){
        return mRefresh;
    }

    public StateListView getListView(){
        return mDefaultListView;
    }
}
