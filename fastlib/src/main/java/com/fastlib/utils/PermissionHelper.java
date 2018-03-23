package com.fastlib.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.SparseArray;

import com.fastlib.annotation.Permission;
import com.fastlib.annotation.PermissionInterface;
import com.fastlib.bean.PermissionRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sgfb on 17/8/10.
 * 6.0权限获取辅助类.需要在Activity中的onRequestPermissionsResult来回调permissionResult方法.
 * 有两种方法来回去权限并且回调成功或者失败的处理
 * 方案一 直接调用requestPermission(Activity 上下文,String[] 权限,Runnable 成功回调,Runnable 失败回调)
 * 方案二 1创建注解PermissionInterface接口，在接口需要权限的方法上注解Permission.2调用permissionGuard来实现一个权限保护对象来使用
 */
public class PermissionHelper{
    private SparseArray<PermissionRequest> mPermissionMap = new SparseArray<>();     //请求码->权限请求

    /**
     * 6.0后发起请求权限
     * @param permission 权限名
     * @param grantedAfterProcess 成功后回调
     * @param deniedAfterProcess 失败后回调
     */
    public void requestPermission(Activity activity, String[] permission, Runnable grantedAfterProcess, Runnable deniedAfterProcess) {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M||checkPermissionGranted(activity,permission))   //小于6.0或之前已获取过权限，直接运行
            grantedAfterProcess.run();
        else {
            int requestCode = mPermissionMap.size() + 1;
            mPermissionMap.put(requestCode, new PermissionRequest(requestCode, grantedAfterProcess, deniedAfterProcess));
            ActivityCompat.requestPermissions(activity,permission, requestCode);
        }
    }

    /**
     * 6.0请求权限返回
     * @param requestCode 请求码
     * @param permissions 权限组
     * @param grantResults 返回结果
     */
    public void permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        boolean success=true;
        for (int i = 0; i < grantResults.length; i++){
            if(grantResults[i]==PackageManager.PERMISSION_DENIED){  //有一个权限被拒绝就运行请求权限被拒绝回调
                success=false;
                break;
            }
        }

        PermissionRequest pr = mPermissionMap.get(requestCode);
        if (pr != null){
            mPermissionMap.remove(requestCode);
            if (success) pr.hadPermissionProcess.run();
            else pr.deniedPermissionProcess.run();
        }
    }

    /**
     * 检查之前是否获取成功过
     * @param activity 上下文
     * @param permissions 权限
     * @return 如果获取成功过返回true，否则false
     */
    private boolean checkPermissionGranted(Activity activity,String[] permissions){
        for(String permission:permissions){
            if(ContextCompat.checkSelfPermission(activity,permission) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    /**
     * 动态代理权限自动申请，仅仅对第一个代理接口有效d
     * @param activity 上下文
     * @param obj 有实现有PermissionInterface注解的接口的对象
     * @return 小于6.0返回obj对象本体否则返回代理类
     */
    public Object permissionGuard(final Activity activity,final Object obj){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) return obj;
        Class<?>[] interfaces=obj.getClass().getInterfaces();

        for(Class<?> inter:interfaces){
            PermissionInterface piInject=inter.getAnnotation(PermissionInterface.class);
            if(piInject!=null){
                return Proxy.newProxyInstance(inter.getClassLoader(), new Class<?>[]{inter}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable{
                        final Permission permissionInject=method.getAnnotation(Permission.class);
                        if(permissionInject!=null)
                            requestPermission(activity, permissionInject.value(), new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        method.invoke(obj,args);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    if(!TextUtils.isEmpty(permissionInject.print())){
                                        N.showShort(activity,permissionInject.print());
                                    }
                                }
                            });
                        else return method.invoke(obj,args);
                        return null;
                    }
                });
            }
        }
        throw new IllegalArgumentException("对象没有实现有PermissionInterface注解的接口");
    }
}
