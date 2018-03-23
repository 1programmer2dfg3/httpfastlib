package com.fastlib.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fastlib.app.UploadUncaughtException;
import com.fastlib.bean.CrashExceptionBean;
import com.fastlib.db.SaveUtil;
import com.fastlib.net.Request;
import com.fastlib.utils.NetUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sgfb on 17/9/6.
 */
public abstract class CrashHandler extends UploadUncaughtException{
    private Context mContext;

    /**
     * 上传异常类构造
     * @param thread 主线程
     * @param parent 存储父路径
     */
    public CrashHandler(Context context,Thread thread, File parent) {
        super(thread,parent);
        mContext=context;
    }

    @Override
    public abstract Request generateRequest();

    @Override
    public void writeExceptionToFile(@NonNull final OutputStream out, @NonNull Throwable ex){
        final CrashExceptionBean bean=new CrashExceptionBean();
        final Gson gson=new Gson();
        ActivityManager am= (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo=new ActivityManager.MemoryInfo();
        Debug.MemoryInfo appMemoryInfo=new Debug.MemoryInfo();
        Debug.getMemoryInfo(appMemoryInfo);

        am.getMemoryInfo(memoryInfo);
        bean.totalMemory=(int)(memoryInfo.totalMem/1024/1024);
        bean.useMemory=appMemoryInfo.getTotalPrivateDirty()/1024;
        bean.message=ex.toString();
        bean.causePosition=(ex.getStackTrace()!=null&&ex.getStackTrace().length>0)?ex.getStackTrace()[0].toString():"无法定位位置";
        bean.extra=getCupFramework();
        bean.netStatus= NetUtils.isConnected(mContext)?(NetUtils.isWifi(mContext)?"WIFI":"2G/3G/4G"):"未联网";
        try {
            out.write((gson.toJson(bean) + ",").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取cpu架构
     * @return cpu架构
     */
    private String getCupFramework(){
        if(Build.VERSION.SDK_INT<21) return "cpu0:"+Build.CPU_ABI+" cpu1:"+(TextUtils.isEmpty(Build.CPU_ABI2)?"":Build.CPU_ABI2);
        else{
            StringBuilder sb=new StringBuilder();
            String[] cpusFramework=Build.SUPPORTED_ABIS;
            int i=0;
            if(cpusFramework!=null&&cpusFramework.length>0) {
                for (String framework : cpusFramework)
                    sb.append("CPU").append(i++).append(" ").append(framework).append("\n");
                sb.deleteCharAt(sb.length()-1);
            }
            return sb.toString();
        }
    }
}
