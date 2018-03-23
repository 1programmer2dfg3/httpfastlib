package com.fastlib.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.fastlib.base.Refreshable;

/**
 * Created by sgfb on 16/9/21.
 * 实现Refreshable接口的SwipeRefreshLayout
 */
public class RefreshLayout extends SwipeRefreshLayout implements Refreshable{

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setRefreshStatus(boolean status){
        setRefreshing(status);
    }

    @Override
    public void setRefreshCallback(final RefreshCallback callback) {
        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                callback.startRefresh();
            }
        });
    }
}
