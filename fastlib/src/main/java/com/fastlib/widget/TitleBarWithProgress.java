package com.fastlib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.fastlib.utils.DensityUtils;

/**
 * Created by sgfb on 17/5/15.
 * TitleBar复合加上进度条
 */
public class TitleBarWithProgress extends LinearLayout{
    private TitleBar mTitleBar;
    private ProgressBar mProgressBar;

    public TitleBarWithProgress(Context context) {
        super(context);
        init();
    }

    public TitleBarWithProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setOrientation(VERTICAL);
        mTitleBar=new TitleBar(getContext());
        mProgressBar=new ProgressBar(getContext(),null,android.R.attr.progressBarStyleHorizontal);
        TypedValue tv=new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize,tv,true);

        LayoutParams titleBarLp=new LayoutParams(LayoutParams.MATCH_PARENT,(int)tv.getDimension(getResources().getDisplayMetrics()));
        LayoutParams progressLp=new LayoutParams(LayoutParams.MATCH_PARENT,DensityUtils.dp2px(getContext(),5));
        mTitleBar.setLayoutParams(titleBarLp);
        mProgressBar.setLayoutParams(progressLp);
        mProgressBar.setVisibility(View.GONE);
        addView(mTitleBar);
        addView(mProgressBar);
    }

    public void setProgress(int progress){
        mProgressBar.setProgress(progress);
        checkProgress();
    }

    public void increProgress(int diff){
        mProgressBar.incrementProgressBy(diff);
        checkProgress();
    }

    private void checkProgress(){
        if(mProgressBar.getProgress()>=mProgressBar.getMax())
            mProgressBar.setVisibility(View.GONE);
        else
            mProgressBar.setVisibility(View.VISIBLE);
    }
}