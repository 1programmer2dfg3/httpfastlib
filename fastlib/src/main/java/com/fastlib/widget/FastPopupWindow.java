package com.fastlib.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by sgfb on 16/2/24.
 * 一个简化的PopupWindow,支持多个Anchor
 */
public class FastPopupWindow{
    private PopupWindow mPopupWindow;
    private View mAnchor;
    private Orientation mOrientation=Orientation.BOTTOM;
    private Point mCustomPoint;
    private Context mContext;

    public FastPopupWindow(Context context){
        this(null,null);
        mContext=context;
    }

    public FastPopupWindow(View contentView){
        this(contentView,null);
    }

    public FastPopupWindow(View contentView, View anchor){
        mAnchor=anchor;
        mPopupWindow=new PopupWindow(contentView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mCustomPoint=new Point();
        if(contentView!=null)
            mContext=contentView.getContext();
    }

    public void toggle(){
        if(mPopupWindow.isShowing())
            mPopupWindow.dismiss();
        else {
            switch(mOrientation){
                case TOUCH:
                    mPopupWindow.showAsDropDown(mAnchor,mCustomPoint.x,mCustomPoint.y);
                    break;
                case LEFT:
                    mPopupWindow.showAsDropDown(mAnchor,mPopupWindow.getContentView().getWidth()*-1,mAnchor.getHeight()*-1);
                    break;
                case RIGHT:
                    mPopupWindow.showAsDropDown(mAnchor,mAnchor.getWidth(),mAnchor.getHeight()*-1);
                    break;
                case ABOVE:
                    mPopupWindow.showAsDropDown(mAnchor,0,mAnchor.getHeight()*-1-mPopupWindow.getContentView().getHeight());
                    break;
                case BOTTOM:
                    mPopupWindow.showAsDropDown(mAnchor);
//                    mPopupWindow.showAtLocation(mAnchor, Gravity.BOTTOM,0,0);
                    break;
                default:
                    break;
            }
        }
    }

    public void toggle(View newAnchor){
        mAnchor=newAnchor;
        toggle();
    }

    public void toggle(Point point){
        mCustomPoint.x=point.x;
        mCustomPoint.y=point.y;
        mOrientation=Orientation.TOUCH;
        toggle();
    }

    public void toggle(View newAnchor,Point point){
        mAnchor=newAnchor;
        toggle(point);
    }

    public void setListView(List<String> list, final AdapterView.OnItemClickListener l){
        LinearLayout mainView=new LinearLayout(mContext);

        mainView.setOrientation(LinearLayout.VERTICAL);
        mainView.setBackgroundColor(Color.WHITE);
        for(int i=0;i<list.size();i++){
            final int finalI = i;
            String s=list.get(i);
            LinearLayout ll=new LinearLayout(mContext);
            TextView tv=new TextView(mContext);
            tv.setTextColor(Color.BLACK);
            tv.setText(s);
            ll.setPadding(15, 15, 15, 15);
            ll.addView(tv);
            mainView.addView(ll);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    l.onItemClick(null,v, finalI,-1);
                }
            });
        }
        mPopupWindow.setContentView(mainView);
    }

    /**
     * 反射调用setTouchModal。如果为false，touch事件将会被传递到Popupwindow的下面
     * @param touchModal
     */
    public void setTouchModal(boolean touchModal){
        try {
            Method method=mPopupWindow.getClass().getDeclaredMethod("setTouchModal",boolean.class);
            method.setAccessible(true);
            method.invoke(mPopupWindow,touchModal);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置方向，默认是Bottom.必须已经设置了Anchor
     *
     * @param orientation
     */
    public void setOrientation(Orientation orientation){
        mOrientation=orientation;
    }

    public void setAnchor(View anchor){
        mAnchor=anchor;
    }

    public View getAnchor(){
        return mAnchor;
    }

    public PopupWindow getPopupWindow(){
        return mPopupWindow;
    }

    public enum Orientation{
        TOUCH,
        LEFT,
        RIGHT,
        ABOVE,
        BOTTOM;
    }
}
