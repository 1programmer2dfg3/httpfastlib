package com.fastlib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by sgfb on 16/8/23.
 * RecyclerView间隔
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration{
    private final int[] ATTRS=new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST= LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST=LinearLayoutManager.VERTICAL;

    private Drawable mDivider;
    private int mOrientation;
    private int mDividerHeight=-1;
    private int mDividerWidth=-1;

    public DividerItemDecoration(Context context,int orientation){
        final TypedArray a=context.obtainStyledAttributes(ATTRS);
        mDivider=a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation){
        if(orientation!=HORIZONTAL_LIST&&orientation!=VERTICAL_LIST)
            throw new IllegalArgumentException("invalid orientation");
        mOrientation=orientation;
    }

    @Override
    public void onDraw(Canvas c,RecyclerView parent){
        if(mOrientation==VERTICAL_LIST)
            drawVertical(c,parent);
        else
            drawHorizontal(c,parent);
    }

    public void drawVertical(Canvas c,RecyclerView parent){
        final int left=parent.getPaddingLeft();
        final int right=parent.getWidth()-parent.getPaddingRight();

        final int childCount=parent.getChildCount();
        for(int i=0;i<childCount;i++){
            final View child=parent.getChildAt(i);
            RecyclerView.LayoutParams params=(RecyclerView.LayoutParams) child.getLayoutParams();
            final int top=child.getBottom()+params.bottomMargin;
            int bottom;
            if(mDividerHeight!=-1) bottom=top+mDividerHeight;
            else bottom=top+mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public void drawHorizontal(Canvas c,RecyclerView parent){
        final int top=parent.getPaddingTop();
        final int bottom=parent.getHeight()-parent.getPaddingBottom();

        final int childCount=parent.getChildCount();
        for(int i=0;i<childCount;i++){
            final View child=parent.getChildAt(i);
            final RecyclerView.LayoutParams params=(RecyclerView.LayoutParams)child.getLayoutParams();
            final int left=child.getRight()+params.rightMargin;
            int right;
            if(mDividerWidth!=-1) right=left+mDividerWidth;
            else right=left+mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect,int itemPosition,RecyclerView parent){
        if(mOrientation==VERTICAL_LIST)
            outRect.set(0,0,0,mDivider.getIntrinsicHeight());
        else
            outRect.set(0,0,mDivider.getIntrinsicWidth(),0);
    }

    public int getOrientation(){
        return mOrientation;
    }

    public int getDividerHeight(){
        return mDivider.getIntrinsicHeight();
    }

    public int getDividerWidth(){
        return mDivider.getIntrinsicWidth();
    }

    public void setDividerHeight(int height){
        mDividerHeight=height;
    }

    public void setDividerWidth(int width){
        mDividerWidth=width;
    }

    public Drawable getDivider() {
        return mDivider;
    }

    public void setDivider(Drawable divider) {
        mDivider = divider;
    }
}