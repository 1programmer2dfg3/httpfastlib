package com.fastlib.test.UrlImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;

import com.fastlib.utils.ScreenUtils;
import com.fastlib.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 17/2/27.
 * 简化的网络图像缓存.根据网络地址缓存.缓存文件为url md5
 * 检查是否存在内存->外部存储->网络
 */
public class ImageLoader{
    private static ImageLoader mOwer;
    private static Config sConfig;
    private Map<String,UrlImage> mUrlMapDisk =new HashMap<>(); //url与网络图像实体引用

    public static void build(Context context){
        if(mOwer==null){
            mOwer=new ImageLoader();
            sConfig =new Config(context);
        }
    }

    public static synchronized ImageLoader getInstance(){
        if(mOwer==null)
            throw new IllegalArgumentException("ImageLoader未实例化");
        return mOwer;
    }

    /**
     * 加载网络图像起始点
     * @param url
     * @return
     */
    public UrlImageRuntime load(String url){
        UrlImage urlImage=mUrlMapDisk.get(url);
        if(urlImage==null){ //是否有临时数据
            urlImage=new UrlImage(url);
            urlImage.mDiskPath=sConfig.mRootDirectory.getAbsolutePath()+File.separator+Utils.getMd5(url,false);
            mUrlMapDisk.put(url,urlImage);
        }
        return urlImage.mRuntime;
    }

    /**
     * 清理没有引用的图像
     */
    public void clear(){
        Iterator<String> iter=mUrlMapDisk.keySet().iterator();
        List<String> needRemove=new ArrayList<>();

        while(iter.hasNext()){
            String key=iter.next();
            UrlImage image=mUrlMapDisk.get(key);
            if(image.mReferImage.isEmpty()){
                image.mBitmap.recycle();
                image.mBitmap=null;
                needRemove.add(key);
            }
        }
        for(String key:needRemove)
            mUrlMapDisk.remove(key);
    }

    /**
     * 占用的内存大小
     * @return 占用量
     */
    public int useMemory(){
        int count=0;
        Iterator<String> iter=mUrlMapDisk.keySet().iterator();

        while(iter.hasNext()){
            Bitmap bitmap=mUrlMapDisk.get(iter.next()).mBitmap;
            if(bitmap!=null)
                count+=bitmap.getByteCount();
        }
        return count;
    }

    public static void setConfig(Config config){
        sConfig =config;
    }

    public static Config getConfig(){
        return sConfig;
    }

    /**
     * 设置存放图像根目录，如果目录不存在则尝试创建.
     * 尽量不要随意多次的更改根目录，因为图像是以相对地址存储在根目录下，修改根目录后可能会重复下载相同的图像
     * @param rootDirectory 存储图像文件的根目录
     */
    public void setRootDirectory(File rootDirectory){
        sConfig.mRootDirectory = rootDirectory;
        if(!rootDirectory.exists()||!rootDirectory.isDirectory())
            rootDirectory.mkdirs();
    }

    public static class Config{
        public int mMaxCacheSize; //定义内存缓存使用大小,字节单位
        public File mRootDirectory=Environment.getExternalStorageDirectory(); //保存图像的根地址;
        public Point mDefaultSize=new Point(100,100); //没有读取到ImageView正确大小情况下默认保存图像大小

        public Config(Context context){
            mMaxCacheSize = ScreenUtils.getScreenHeight()*ScreenUtils.getScreenWidth()*2*4; //2屏幕ARGB_8888图像缓存大小
        }
    }
}