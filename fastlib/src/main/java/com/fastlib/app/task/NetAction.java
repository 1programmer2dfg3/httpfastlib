package com.fastlib.app.task;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fastlib.net.GlobalListener;
import com.fastlib.net.NetManager;
import com.fastlib.net.Request;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/9/5.
 * 网络事件.因为网络请求不能在主线程中，所以execute中的行为必须置与子线程中。而executeAdapt置与指定的线程中
 */
public abstract class NetAction<R> extends Action<Request,R> {
    private ThreadPoolExecutor mExecutor; //可选的运行线程池

    protected abstract void executeAdapt(R r,Request request);

    @Override
    protected R execute(final Request param) throws IOException {
        Object responseObj=null;
        byte[] response= NetManager.getInstance().netRequestPromptlyBack(param);
        Method[] methods=getClass().getDeclaredMethods();
        Type rType=Object.class;
        final GlobalListener globalListener=NetManager.getInstance().getGlobalListener();

        for(Method m:methods){
            if("executeAdapt".equals(m.getName())){
                Type[] paramsType=m.getGenericParameterTypes();
                for(Type paramType:paramsType)
                    if(paramType!=Object.class){
                        rType=paramType;
                        break;
                    }
            }
            if(rType!=Object.class) break;
        }
        if(globalListener!=null)
            globalListener.onRawData(param,response);
        if(response!=null){
            if(rType==Object.class||rType==byte[].class||rType==byte.class)
                responseObj=response;
            else if(rType==String.class) {
                responseObj = new String(response);
                if(globalListener!=null)
                    globalListener.onTranslateJson(param, (String) responseObj);
            }
            else {
                Gson gson=new Gson();
                try{
                    String json=new String(response);
                    if(globalListener!=null)
                        globalListener.onTranslateJson(param,json);
                    responseObj=gson.fromJson(json,rType);
                }catch (JsonParseException e){
                    if(globalListener!=null) globalListener.onErrorListener(param,"json解析异常");
                    e.printStackTrace();
                    stopTask(); //如果异常了，停止任务链
                }
            }
        }
        checkNetBackStatus(param);
        if(mThreadType== ThreadType.MAIN){
            final Object fResponseObj=responseObj;
            Handler mainHandle=new Handler(Looper.getMainLooper());
            mainHandle.post(new Runnable() {
                @Override
                public void run(){
                    if(globalListener!=null)
                        globalListener.onResponseListener(param,fResponseObj,null);
                    executeAdapt((R) fResponseObj,param);
                }
            });
        }
        else {
            if(globalListener!=null)
                globalListener.onResponseListener(param,responseObj,null);
            executeAdapt((R) responseObj,param);
        }
        return (R) responseObj;
    }

    private void checkNetBackStatus(Request request){
        if(request.getResponseStatus().code!=200) {
            stopTask();
            Log.d(NetAction.class.getSimpleName(),"net action error:"+request.getResponseStatus().message);
        }
    }

    public ThreadPoolExecutor getExecutor() {
        return mExecutor;
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        mExecutor = executor;
    }

    /**
     * 强制返回运行在子线程中
     * @return 线程类型（固定子线程）
     */
    @Override
    public ThreadType getThreadType() {
        return ThreadType.WORK;
    }
}