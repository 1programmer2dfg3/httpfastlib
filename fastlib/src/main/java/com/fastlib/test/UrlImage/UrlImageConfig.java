package com.fastlib.test.UrlImage;

import android.graphics.Bitmap;

/**
 * Created by sgfb on 17/3/1.
 * 单张图像配置
 */
public class UrlImageConfig{
    public int mWidth,mHeight;
    public Bitmap.Config mConfig;

    public UrlImageConfig(){
        mConfig= Bitmap.Config.ARGB_8888;
    }

    public UrlImageConfig setWidth(int width) {
        mWidth = width;
        return this;
    }

    public UrlImageConfig setHeight(int height) {
        mHeight = height;
        return this;
    }

    public UrlImageConfig setConfig(Bitmap.Config config) {
        mConfig = config;
        return this;
    }
}