package com.fastlib.utils.json;

import android.content.Context;
import android.support.annotation.IdRes;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sgfb on 16/11/13.
 * json转换时留存的最小单元.json值默认保存为string，使用的时候进行转换，基本类型尝试遍历类型返回，引用类型使用额外方法getValue(Type type)和getValue(Class<?> cla)
 */
@SuppressWarnings("unchecked")
public final class JsonObject{
    private String mJsonRaw; //本单元的原始json字符串，可以进行二次json解析
    private String mKey; //json名
    private Object mValue; //json值.可能是List<JsonObject>,Map<String,JsonObject>,基本类型字符串

    public JsonObject(String raw,String key,Object value){
        mJsonRaw=raw;
        mKey=key;
        mValue=value;
    }

    /**
     * 根据键名返回值,仅搜索第一层
     * @param key
     * @param <T>
     * @return
     * @throws ClassCastException
     */
    public <T> T findValue(String key,T defaultValue)throws ClassCastException{
        return findValue(key,false,defaultValue);
    }

    /**
     * 根据视图id返回值
     * @param context
     * @param id
     * @param <T>
     * @throws ClassCastException
     * @return
     */
    public <T> T findValue(Context context, @IdRes int id) throws ClassCastException{
        return findValue(context.getResources().getResourceEntryName(id),true,null);
    }

    /**
     * 根据键名返回值
     * @param key 键
     * @param deep 是否逐层寻找
     * @param <T> 一般泛型
     * @return
     * @throws ClassCastException
     */
    public <T> T findValue(String key,boolean deep,T defaultValue)throws ClassCastException{
        if(TextUtils.equals(mKey,key))
            return getValue();
        else{
            if(mValue instanceof Map){
                Map<String,JsonObject> joMap= (Map<String, JsonObject>) mValue;
                JsonObject jo=joMap.get(key);
                if(jo!=null)
                    return jo.findValue(key,null);
                else{
                    if(deep){
                        Iterator<String> iter = joMap.keySet().iterator();
                        while (iter.hasNext())
                            return joMap.get(iter.next()).findValue(key,null);
                    }
                }
            }
        }
        return defaultValue;
    }

    /**
     * 获取值
     * @param <T>
     * @return
     * @throws ClassCastException
     */
    public <T> T getValue()throws ClassCastException{
        if(mValue==null)
            throw new ClassCastException();
        return (T) mValue;
    }

    /**
     * 类类型转换
     * @param cla
     * @param <T>
     * @return
     */
    public <T> T getValue(Class<?> cla){
        Gson gson=new Gson();
        try{
            return (T) gson.fromJson(mJsonRaw,cla);
        }catch (JsonParseException e){
            return null;
        }
    }

    /**
     * 特定类型转换
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getValue(Type type){
        Gson gson=new Gson();
        try{
            return gson.fromJson(mJsonRaw,type);
        }catch (JsonParseException e){
            return null;
        }
    }

    /**
     * 获取键
     * @return
     */
    public String getKey(){
        return mKey;
    }
}