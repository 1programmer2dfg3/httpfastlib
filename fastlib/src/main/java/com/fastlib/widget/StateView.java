package com.fastlib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import com.fastlib.BuildConfig;
import com.fastlib.base.AdapterViewState;

/**
 * Created by sgfb on 16/4/21.
 * 具有状态的视图
 */
public class StateView extends FrameLayout implements AdapterViewState{
    public static final String TAG=StateView.class.getSimpleName();

    private SparseArray<View> mViews=new SparseArray<>();
    private View mCurrentView;

    public StateView(Context context){
        super(context,null);
    }

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onStateChanged(int state){
        View v=mViews.get(state);
        if(v==null){
            if(BuildConfig.isShowLog)
            Log.d(TAG,"StateView改变成一个非预期的状态");
            return;
        }
        if(mCurrentView!=null)
            removeView(mCurrentView);
        mCurrentView=v;
        addView(mCurrentView);
    }

    public void addStateView(int state,View view){
        addStateView(state,view,-1);
    }

    /**
     * 增加状态视图,每个状态只保存一个
     * @param state 状态
     * @param view 视图
     * @param location 何种形式生成位置 miss
     */
    @Override
    public void addStateView(int state,View view, int location) {
        mViews.put(state,view);
    }
}
