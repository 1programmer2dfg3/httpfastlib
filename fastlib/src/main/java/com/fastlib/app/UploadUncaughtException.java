package com.fastlib.app;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.fastlib.net.Listener;
import com.fastlib.net.Request;
import com.fastlib.net.SimpleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Created by sgfb on 17/2/16.
 * 上传异常封装类.异常发生时应该记录到本地.以天为单位生成日志
 * 这个方法里的异常应该都忽视掉
 */
public abstract class UploadUncaughtException implements Thread.UncaughtExceptionHandler{
    protected File mParent;

    public abstract Request generateRequest(); //生成上传请求

    /**
     * 写异常到异常文件中,这个方法是不保证回调的
     * @param file 存储异常的文件
     * @param ex 异常信息
     */
    public abstract void writeExceptionToFile(@NonNull OutputStream file, @NonNull Throwable ex);

    /**
     * 上传异常类构造
     * @param thread 主线程
     * @param parent 存储父路径
     */
    public UploadUncaughtException(Thread thread,File parent){
        mParent=parent;
        thread.setUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex){
        handleError(ex);
        if(Looper.getMainLooper()==Looper.myLooper())
            System.exit(0);
    }

    /**
     * 处理错误
     * @param ex
     */
    private void handleError(Throwable ex){
        File f=new File(mParent,generateErrorLogName());
        OutputStream fOut=null;

        try{
            if(!f.exists()){
                try {
                    if(f.createNewFile())
                        fOut=new FileOutputStream(f,true);
                } catch (IOException e) {
                    //因为已经奔溃了，奔溃里的错误什么事都不做
                }
            }
            else fOut=new FileOutputStream(f,true);
            if(fOut!=null){
                writeExceptionToFile(fOut,ex);
                fOut.close();
            }
        }catch (IOException e){
            //因为已经奔溃了，奔溃里的错误什么事都不做
        }
    }

    /**
     * 生成日志名 年.月.日
     * @return 日志名
     */
    private String generateErrorLogName(){
        Calendar calendar=Calendar.getInstance();
        return  "log" +
                calendar.get(Calendar.YEAR) +"_"+
                (calendar.get(Calendar.MONTH) + 1) +"_"+
                calendar.get(Calendar.DAY_OF_MONTH)+
                ".txt";
    }

    /**
     * 检查非今日的数据异常日志，如果存在则上传
     */
    public void uploadHistoryLog(){
        Request request=generateRequest();
        String todayErrorName=generateErrorLogName();
        final Listener listener=request.getListener();

        for(final File logFile:mParent.listFiles()){
            if(logFile.getName().equals(todayErrorName))
                continue;
            request.setListener(new SimpleListener<String>(){

                @Override
                public void onResponseListener(Request r, String result) {
                    logFile.delete();
                    if(listener!=null) listener.onResponseListener(r,result,null,null);
                    r.clear();
                }

                @Override
                public void onErrorListener(Request r, String error) {
                    super.onErrorListener(r, error);
                    if(listener!=null) listener.onErrorListener(r,error);
                    r.clear();
                }
            });
            request.start();
        }
    }
}