package com.fastlib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 多页滚动时可以使用的底部圆点
 */
public class Indicator extends View{
	//总共页数（圆点数）
	private int mPageCount=0;
	//默认除了mIndex的圆点其他圆点都是半透明
	private int mIndex=0;
	private Paint mSelectPaint;
	private Paint mUnSelectPaint;
	
	public Indicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSelectPaint =new Paint();
		mUnSelectPaint=new Paint();
		//灰色，抗锯齿
		mSelectPaint.setColor(Color.GRAY);
		mSelectPaint.setAntiAlias(true);
		mUnSelectPaint.setColor(Color.GRAY);
		mUnSelectPaint.setAntiAlias(true);
		mUnSelectPaint.setAlpha(120);
	}
	
	public Indicator(Context context, int pageCount, int index){
		super(context);
		mIndex=index;
		mPageCount=pageCount;
	}
	
	@Override
	public void onDraw(Canvas canvas){
		int width=canvas.getWidth();
		int height=canvas.getHeight();
		int circleDiameter=width/480*8;
		int circleSpacing=circleDiameter*3;
		//两边空隙,其中一边的长度
		int doubleSpacing=(width-(mPageCount-1)*(circleDiameter+circleSpacing))/2;
		Paint currentPaint;

		for(int i=0;i<mPageCount;i++){
			if(mPageCount-i-1!=mIndex)
				currentPaint=mUnSelectPaint;
			else
				currentPaint=mSelectPaint;
			canvas.drawCircle(width-doubleSpacing-(circleDiameter+circleSpacing)*i,height/2,circleDiameter,currentPaint);
		}
	}
	
	public void setCurrentItem(int index){
		mIndex=index;
		invalidate();
	}
	
	public void setItemCount(int count){
		mPageCount=count;
		invalidate();
	}

	public void setSelectPaint(Paint paint){
		mSelectPaint=paint;
	}

	public void setUnSelectPaint(Paint paint){
		mUnSelectPaint=paint;
	}

	public void setSelectPaint(int color){
		mSelectPaint.setColor(color);
	}

	public void setUnSelectPaint(int color){
		mUnSelectPaint.setColor(color);
	}
}