package com.fastlib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.fastlib.R;
import com.fastlib.utils.DensityUtils;

/**
 * Created by sgfb on 16/5/10.
 * 这是一个与父视图同长的指示器
 */
public class PercentIndicator extends View{
    public static final String TAG=PercentIndicator.class.getSimpleName();

    private Paint mPaint;
    private int mHeight;  //默认5dp高度
    private int mItemCount;
    private int mSelectColor;
    private int mOtherColor;
    private int mCurrentItem;
    private float mPercent; //值域－1~1

    public PercentIndicator(Context context){
        super(context);
        init();
    }

    public PercentIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mHeight= DensityUtils.dp2px(getContext(),5);
        mPaint=new Paint();
        mSelectColor=Color.RED;
        mOtherColor=getResources().getColor(R.color.grey_400);
    }

    public void setItemCount(int count){
        mItemCount=count;
        invalidate();
    }

    public void setCurrentItem(int current){
        if(current>mItemCount){
            Log.w(TAG,"设置的item大于了item总数");
            mCurrentItem=mItemCount;
            return;
        }
        mCurrentItem=current;
        invalidate();
    }

    public int getCurrentItem(){
        return mCurrentItem;
    }

    public void setPercent(int position,float percent){
        mCurrentItem=position;
        mPercent=percent;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        if(mItemCount==0)
            return;
        int itemWidth=canvas.getWidth()/mItemCount;  //等于item数量分之1的长度
        float offset=mPercent*itemWidth;

        mPaint.setColor(mOtherColor);
        canvas.drawRect(0,0,canvas.getWidth(),mHeight,mPaint);
        mPaint.setColor(mSelectColor);
        canvas.drawRect(mCurrentItem*itemWidth+offset,0,mCurrentItem*itemWidth+itemWidth+offset,mHeight,mPaint);
    }
}