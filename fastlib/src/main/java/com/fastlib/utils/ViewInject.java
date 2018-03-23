package com.fastlib.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;

import com.fastlib.BuildConfig;
import com.fastlib.annotation.Bind;
import com.fastlib.annotation.LocalData;
import com.fastlib.annotation.TransitionAnimation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/1/12.
 * 视图注入到字段和方法中
 */
public class ViewInject{
    private ThreadPoolExecutor mThreadPool;
    private Object mHost; //host仅为Activity或Fragment
    private View mRoot;

    private ViewInject(Object host, @NonNull View root, ThreadPoolExecutor threadPool){
        if(!(host instanceof Activity)&&!(host instanceof Fragment))
            throw new IllegalArgumentException("host 必须是Activity或者Fragment");
        mThreadPool = threadPool;
        mHost=host;
        mRoot=root;
        injectViewEvent();
    }

    public static void inject(Object host,@NonNull View root){
        inject(host,root,null);
    }

    public static void inject(Object host,@NonNull View root,ThreadPoolExecutor threadPool){
        new ViewInject(host,root,threadPool);
    }

    /**
     * 绑定视图事件到方法上，运行一段代码如果有异常自行处理
     * @param runOnWorkThread
     * @param m
     * @param objs
     */
    private boolean invokeWithoutError(boolean runOnWorkThread, final Method m, final Object... objs){
        if(runOnWorkThread&&mThreadPool!=null)
            mThreadPool.execute(new Runnable() {
                @Override
                public void run(){
                    try {
                        m.invoke(mHost);  //先尝试绑定无参方法
                    } catch (InvocationTargetException e){ //这个异常是非方法参数异常所以直接显示或抛出
                        if(BuildConfig.isShowLog){
                            System.out.println("toggle exception");
                            e.printStackTrace();
                        }
                    } catch (IllegalAccessException e) {
                        try {
                            m.invoke(mHost,objs);
                        } catch (IllegalAccessException e1) {
                            if(BuildConfig.isShowLog){
                                System.out.println("toggle exception");
                                e1.printStackTrace();
                            }
                        } catch (InvocationTargetException e2) {
                            if(BuildConfig.isShowLog){
                                System.out.println("toggle exception");
                                e2.printStackTrace();
                            }
                        }
                    }
                }
            });
        else
            try {
                Object result=m.invoke(mHost);  //先尝试绑定无参方法
                if(result instanceof Boolean)
                    return (boolean)result;
            } catch (IllegalAccessException|IllegalArgumentException e){
                try {
                    Object result = m.invoke(mHost,objs);
                    if(result instanceof Boolean)
                        return (Boolean)result;
                } catch (IllegalAccessException|IllegalArgumentException e1) {
                    if(BuildConfig.isShowLog){
                        System.out.println("toggle exception");
                        e1.printStackTrace();
                    }
                } catch (InvocationTargetException e2){
                    if(BuildConfig.isShowLog){
                        System.out.println("toggle exception");
                        e2.printStackTrace();
                    }
                }
                return false;
            } catch (InvocationTargetException e){
                if(BuildConfig.isShowLog){
                    System.out.println("toggle exception");
                    e.printStackTrace();
                }
                return false;
            }
        return false;
    }

    /**
     * 绑定方法到事件监听中
     * @param m
     * @param v
     * @param vi
     */
    private void bindListener(final Method m, View v, final Bind vi){
        switch(vi.bindType()){
            case CLICK:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v){
                        invokeWithoutError(vi.runOnWorkThread(),m,v);
                    }
                });
                break;
            case LONG_CLICK:
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v){
                        return invokeWithoutError(vi.runOnWorkThread(),m,v);
                    }
                });
                break;
            case ITEM_CLICK:
                ((AdapterView)v).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        invokeWithoutError(vi.runOnWorkThread(),m,parent,view,position,id);
                    }
                });
                break;
            case ITEM_LONG_CLICK:
                ((AdapterView)v).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        return invokeWithoutError(vi.runOnWorkThread(),m,parent,view,position,id);
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * 绑定控件到方法和属性中
     */
    private void injectViewEvent(){
        //绑定控件方法
        Method[] methods=mHost.getClass().getDeclaredMethods();
        if(methods!=null&&methods.length>0)
            for(final Method m:methods){
                m.setAccessible(true);
                Bind vi=m.getAnnotation(Bind.class);
                LocalData ld=m.getAnnotation(LocalData.class);
                if(vi!=null&&ld==null){
                    int[] ids=vi.value();
                    if(ids.length>0){
                        for(int id:ids){
                            View v=mRoot.findViewById(id);
                            if(v!=null)
                                bindListener(m,v,vi);
                        }
                    }
                }
            }

        //绑定视图到属性
        Field[] fields=mHost.getClass().getDeclaredFields();
        if(fields!=null&&fields.length>0)
            for(Field field:fields){
                Bind vi=field.getAnnotation(Bind.class);
                if(vi!=null){
                    int[] ids=vi.value();
                    if(ids.length>0){
                        try{
                            View view=mRoot.findViewById(ids[0]);
                            field.setAccessible(true);
                            field.set(mHost,view);
                            checkTransitionAnimationInject(view,field);
                        } catch (IllegalAccessException e) {
                            if(BuildConfig.isShowLog)
                            System.out.println(e.getMessage());
                        }
                    }
//                    else{
//                        //如果视图id是空的，尝试根据属性名绑定对应视图
//                        String name=field.getName();
//                        int viewId;
//                        try{
//                            if((viewId=mRoot.getResources().getIdentifier(name,"id",mRoot.getContext().getPackageName()))!=0){
//                                View view=mRoot.findViewById(viewId);
//                                field.setAccessible(true);
//                                field.set(mHost,view);
//                            }
//                        }catch(IllegalAccessException e){
//                            if(Fastlib.isShowLog())
//                                System.out.println(e.getMessage());
//                        }
//                    }
                }
            }
    }

    /**
     * 检查是否有元素分享动画注解，如果有将分享名注入
     */
    @TargetApi(21)
    private void checkTransitionAnimationInject(View view, Field field){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP) //小于5.0不检查
            return;
        if(view==null){
            if(BuildConfig.isShowLog)
                System.out.println("视图为空，不可使用共享动画名注入");
            return;
        }
        TransitionAnimation ta=field.getAnnotation(TransitionAnimation.class);
        if(ta!=null)
            view.setTransitionName(getHostParams(ta.value()));
    }

    /**
     * 获取宿主字符串参数，Activity为Intent.getString,Fragment为Bundle.getString
     * @param key
     * @return
     */
    private String getHostParams(String key){
        if(mHost instanceof Activity)
            return ((Activity)mHost).getIntent().getStringExtra(key);
        else
            return ((Fragment)mHost).getArguments().getString(key);
    }
}