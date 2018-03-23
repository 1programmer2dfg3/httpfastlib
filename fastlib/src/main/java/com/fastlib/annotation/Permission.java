package com.fastlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sgfb on 17/8/2.
 * 6.0运行权限注解.被注解的对象可以返回一个Runnable代表权限请求失败时执行的回调
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission{
    String[] value(); //请求的权限
    String print() default  ""; //如果没有指定请求失败回调（被注解对象返回Runnable），则显示简便的打印信息
}