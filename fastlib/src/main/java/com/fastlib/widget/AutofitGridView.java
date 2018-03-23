package com.fastlib.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fastlib.utils.DensityUtils;
import com.fastlib.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 16/6/18.
 */
public class AutofitGridView extends LinearLayout implements View.OnClickListener{
    private AdapterView.OnItemClickListener mListener;
    private int mPaddingAndMargin; //外边距和内边距,px单位
    private int index;

    public AutofitGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp= (LayoutParams) getLayoutParams();
        if(lp!=null)
            mPaddingAndMargin+=(lp.leftMargin+lp.rightMargin);
        mPaddingAndMargin=getPaddingLeft()+getPaddingRight();
    }

    public void addString(@LayoutRes final int resId,final String... ss){
        List<String> list=new ArrayList<>();
        for(int i=0;i<ss.length;i++)
            list.add(ss[i]);
        index=0;
        addString(resId, list);
    }

    public void addString(@LayoutRes final int resId,final List<String> list){
        index=0;
        addStringByCircle(resId, list);
    }

    private void addStringByCircle(@LayoutRes final int resId,final List<String> list){
        TextView tv= (TextView) LayoutInflater.from(getContext()).inflate(resId,null);
        LayoutParams lp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        LayoutParams lp2=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

        if(list==null||list.size()<=0){
            invalidate();
            return;
        }
        List<String> temp=new ArrayList<>(list);
        String str=temp.remove(0);
        lp.setMargins(0, 0, DensityUtils.dp2px(getContext(), 15), 0);
        lp2.setMargins(0, 0, 0, DensityUtils.dp2px(getContext(), 20));
        tv.setLayoutParams(lp);
        tv.setText(str);
        tv.setTag(index++);
        tv.setOnClickListener(this);

        if(getChildCount()>0){
            LinearLayout ll=(LinearLayout)getChildAt(getChildCount()-1);
            int width = (int) (tv.getText().toString().length()*tv.getTextSize())+DensityUtils.dp2px(ll.getContext(),15);
            int screenWidth=ScreenUtils.getScreenWidth();
            int textRight=getSelfRight(ll)+width+mPaddingAndMargin;
            System.out.println(screenWidth+"?"+textRight);
            if (screenWidth<textRight){
                LinearLayout ll2 = new LinearLayout(getContext());

                ll2.setLayoutParams(lp2);
                ll2.setOrientation(LinearLayout.HORIZONTAL);
                ll2.addView(tv);
                addView(ll2);
            } else
                ll.addView(tv);
            addStringByCircle(resId, temp);
        }
        else{
            LinearLayout ll=new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(lp2);
            ll.addView(tv);
            addView(ll);
            addStringByCircle(resId, temp);
        }
    }

    public void setOnItemListener(AdapterView.OnItemClickListener l){
        mListener=l;
    }

    private int getSelfRight(ViewGroup v){
        int right=0;
        for(int i=0;i<v.getChildCount();i++){
            TextView tv= (TextView) v.getChildAt(i);
            right+=(tv.getText().toString().length()*tv.getTextSize());
        }
        return right;
    }

    public View findViewByPosition(int position){
        int childCount=getChildCount();
        int grandsonCount=0;
        for(int i=0;i<childCount;i++){
            ViewGroup gv= (ViewGroup) getChildAt(i);
            grandsonCount+=gv.getChildCount();
            if(grandsonCount>position)
                return gv.getChildAt(position-(grandsonCount-gv.getChildCount()));
        }
        return null;
    }

    @Override
    public void onClick(View v){
        if(mListener!=null){
            int index= (int) v.getTag();
            mListener.onItemClick(null,v,index,index);
        }
    }
}
