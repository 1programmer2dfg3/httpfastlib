package com.fastlib.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fastlib.R;

/**
 * Created by sgfb on 16/9/20.
 * 进度提示,默认居中
 */
public class LoadingDialog extends DialogFragment{
    private TextView mHint;
    private String mHintStr;

    public LoadingDialog(){
        setStyle(STYLE_NO_TITLE,0);
    }

    @Override
    public void onStart(){
        super.onStart();
        Window window = getDialog().getWindow();
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,getDialog().getWindow().getAttributes().height);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setGravity(Gravity.CENTER);
    }

    public void show(FragmentManager fm){
        show(fm,false);
    }

    public void show(FragmentManager fm,boolean cancelable){
        setCancelable(cancelable);
        show(fm,"loading dialog");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.dialog_loading,null);
        mHint= (TextView) view.findViewById(R.id.text);
        if(!TextUtils.isEmpty(mHintStr))
            mHint.setText(mHintStr);
        return view;
    }

    /**
     * 设置提示文字
     * @param hint 提示文字
     */
    public void setHint(String hint){
        mHintStr=hint;
        if(mHint!=null) {
            if(TextUtils.isEmpty(mHintStr))
                mHint.setVisibility(View.GONE);
            else{
                mHint.setVisibility(View.VISIBLE);
                mHint.setText(mHintStr);
            }
        }
    }
}
