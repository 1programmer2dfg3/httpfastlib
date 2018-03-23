package com.fastlib.test.UrlImage;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 17/2/27.
 * 来自服务器上的图像实体
 */
public class UrlImage implements Application.ActivityLifecycleCallbacks{
    public boolean isDownloading; //是否在下载任务队列中
    public String mUrl;
    public String mDiskPath; //根目录＋url地址md5 32位
    public Bitmap mBitmap;
    public UrlImageRuntime mRuntime;
    public List<ImageView> mReferImage=new ArrayList<>();  //当引用数到0时这个图像可能会被清理

    public UrlImage(String url){
        mRuntime = new UrlImageRuntime(this);
        mUrl=url;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity){
        List<ImageView> needRemove=new ArrayList<>();
        for(ImageView iv:mReferImage)
            if(iv.getContext()==activity)
                needRemove.add(iv);
        mReferImage.removeAll(needRemove);
    }
}