package com.fastlib.annotation;

import android.support.v4.util.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sgfb on 17/3/5.
 * 如果该注解存在，说明这个Activity有动态的元素共享动画.可以与Bind配合使用（当前仅支持Activity，不支持Fragment）
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransitionAnimation{
    String value() default ""; //与Bind配合使用，视图对应TransitionName Activity参数来自Intent.getString,Fragment参数来自Bundle.getString
}