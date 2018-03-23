package com.fastlib.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fastlib.R;
import com.fastlib.utils.ScreenUtils;

/**
 * Created by sgfb on 16/9/20.
 * 底部dialog，带动画
 */
public abstract class BottomDialog extends Fragment{
    public static final String ARG_INT_LAYOUT_ID ="layoutId"; //必传的布局id
    public static final String ARG_INT_COLOR ="colorId"; //背景颜色代码

    private ObjectAnimator mStartAnimator,mBgAnimation;
    private View mView;
    private View mBg;

    /**
     * 绑定视图
     * @param v
     */
    protected abstract void bindView(View v);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        ViewGroup parent= (ViewGroup) inflater.inflate(R.layout.dialog_bottom,null);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        int bgColor=getArguments().getInt(ARG_INT_COLOR,-1);
        mBg=parent.findViewById(R.id.bg);
        mView=inflater.inflate(getArguments().getInt(ARG_INT_LAYOUT_ID),null);

        if(bgColor!=-1)
            mBg.setBackgroundColor(bgColor);
        mView.setLayoutParams(lp);
        mView.setTranslationY(ScreenUtils.getScreenHeight()-ScreenUtils.getStatusHeight(getContext()));
        bindView(mView);
        parent.addView(mView);
        mBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //nothing for defense touch event dispatch
            }
        });
        return parent;
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.post(new Runnable() {
            @Override
            public void run(){
                mStartAnimator=ObjectAnimator.ofFloat(mView,"translationY",mView.getTranslationY(),ScreenUtils.getScreenHeight()-ScreenUtils.getStatusHeight(getContext())-mView.getHeight()).setDuration(220);
                mBgAnimation=ObjectAnimator.ofFloat(mBg,"alpha",0,1,220);
                mStartAnimator.start();
                mBgAnimation.start();
            }
        });
    }

    public void dismiss(){
        if(mStartAnimator!=null){
            mStartAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    getFragmentManager().beginTransaction().remove(BottomDialog.this).commit();
                }
            });
            mBgAnimation.reverse();
            mStartAnimator.reverse();
        }
        else getFragmentManager().beginTransaction().remove(this).commit();
    }
}