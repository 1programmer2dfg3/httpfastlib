package com.fastlib.bean;

import android.os.Build;

import com.fastlib.BuildConfig;
import com.fastlib.annotation.Database;

import java.io.Serializable;

/**
 * Created by sgfb on 17/2/16.
 * 默认的奔溃异常数据实体类
 */
public class CrashExceptionBean{
    public final int systemVersion=Build.VERSION.SDK_INT; //android版本
    public final  String phoneModel= Build.MODEL; //手机具体型号

    public int crashLevel=1; //奔溃严重级别,越高越严重
    public int appVersion=BuildConfig.VERSION_CODE; //app版本
    public int totalMemory; //系统内存总量，单位MB
    public int useMemory; //已使用内存，单位MB
    public String message; //奔溃原因
    public String causePosition; //异常发生点
    public String netStatus; //网络状态
    public String projectName= BuildConfig.APPLICATION_ID;
    public String extra;

    @Override
    public String toString() {
        return "CrashExceptionBean{" +
                "systemVersion=" + systemVersion +
                ", phoneModel='" + phoneModel + '\'' +
                ", crashLevel=" + crashLevel +
                ", appVersion=" + appVersion +
                ", totalMemory=" + totalMemory +
                ", useMemory=" + useMemory +
                ", message='" + message + '\'' +
                ", causePosition='" + causePosition + '\'' +
                ", netStatus='" + netStatus + '\'' +
                ", projectName='" + projectName + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}