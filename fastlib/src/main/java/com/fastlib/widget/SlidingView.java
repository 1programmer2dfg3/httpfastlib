package com.fastlib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.fastlib.R;

/**
 * Created by sgfb on 16/2/26.
 */
public class SlidingView extends FrameLayout{
    private float mDownX;

    public SlidingView(Context context){
        this(context,null);
    }

    public SlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        //没有子视图时跳出
        if(getChildCount()<=0)
            return super.dispatchTouchEvent(ev);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX=ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                View v=getChildAt(0);
                float offset=ev.getX()-mDownX;
                if(v.getX()+offset<0)
                    v.setX(0);
                else
                    v.setTranslationX(offset);
                break;
            case MotionEvent.ACTION_UP:
                startAnimate(getWidth()/2>ev.getX()-mDownX,ev.getX()-mDownX);
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public void addView(View child){
        super.addView(child);
        if(getChildCount()>=2)
            throw new IllegalStateException("Can't add more than 2 views to a SlidingView");
    }

    private void startAnimate(final boolean toLeft,float offsetLeft){
        Animation anim;
        final View child=getChildAt(0);
        if(toLeft){
            anim=new TranslateAnimation(0,offsetLeft*-1,0,0);
            anim.setDuration(500);
        }
        else
            anim= AnimationUtils.loadAnimation(getContext(), R.anim.trans_left_to_right);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do noting
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(toLeft)
                    child.setX(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do noting
            }
        });
        child.startAnimation(anim);
    }
}
