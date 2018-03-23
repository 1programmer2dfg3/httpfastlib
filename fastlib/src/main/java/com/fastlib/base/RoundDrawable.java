package com.fastlib.base;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

/**
 * Created by sgfb on 16/3/7.
 * 圆角Drawable
 */
public class RoundDrawable extends Drawable{
    private int mCorner;
    private Bitmap mBitmap;
    private Paint mPaint;
    private RectF mRectF;

    public RoundDrawable(Drawable drawable){
        int width=drawable.getIntrinsicWidth();
        int height=drawable.getIntrinsicHeight();

        if(width<=0)
            width=drawable.getBounds().width();
        if(height<=0)
            height=drawable.getBounds().height();
        mBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(mBitmap);
        drawable.draw(canvas);
        init();
    }

    public RoundDrawable(Bitmap bitmap){
        mBitmap=bitmap;
        init();
    }

    private void init(){
        mPaint=new Paint();
        mCorner=5;
        BitmapShader shader=new BitmapShader(mBitmap,TileMode.CLAMP,TileMode.CLAMP);
        mPaint.setAntiAlias(true);
        mPaint.setShader(shader);
        mRectF=new RectF(0,0,mBitmap.getWidth(),mBitmap.getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRectF,mCorner,mCorner,mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mRectF.set(left,top,right,bottom);
    }

    @Override
    public void setBounds(Rect bounds) {
        mRectF.set(bounds);
    }

    @Override
    public int getIntrinsicWidth(){
        return mBitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight(){
        return mBitmap.getHeight();
    }

    public void setCorner(int corner){
        mCorner=corner;
    }
}
