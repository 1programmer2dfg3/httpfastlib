package com.fastlib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

/**
 * Created by sgfb on 16/6/12.
 * 百分比视图
 */
public class PercentView extends RelativeLayout{

    public PercentView(Context context){
        super(context);
    }

    public PercentView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        int count=getChildCount();
        for(int i=0;i<count;i++){
            View child=getChildAt(i);
            child.getLeft();
            if (child.getVisibility() != GONE){
                RelativeLayout.LayoutParams st =
                        (RelativeLayout.LayoutParams) child.getLayoutParams();
                int left=0,right=0,top=0,bottom=0;
                try {
                    Field leftField=st.getClass().getDeclaredField("mLeft");
                    Field rightField=st.getClass().getDeclaredField("mRight");
                    Field topField=st.getClass().getDeclaredField("mTop");
                    Field bottomField=st.getClass().getDeclaredField("mBottom");
                    leftField.setAccessible(true);
                    rightField.setAccessible(true);
                    topField.setAccessible(true);
                    bottomField.setAccessible(true);
                    left=leftField.getInt(st);
                    right=rightField.getInt(st);
                    top=topField.getInt(st);
                    bottom=bottomField.getInt(st);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                child.layout(left,top,right,bottom);
            }
        }
    }
}
