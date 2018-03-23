/*
package com.fastlib.utils.json;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.fastlib.db.SaveUtil;
import com.fastlib.net.Listener;
import com.fastlib.net.Request;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

*/
/**
 * Created by sgfb on 17/3/21.
 * 接口返回的json转换成实体类
 *//*

public class ResponseJsonConvert{
    private File mSaveFolder; //存储转换好的实体类的文件夹，如果不存在会尝试创建
    private String mPackageName;
    private String mEntityBody; //相对于模版类的实体块
    private String mUrl;
    private String mData;
    private List<Request> mRequests;

    public ResponseJsonConvert(List<Request> requests) {
        mRequests = requests;
        mPackageName="com.bean";
        mSaveFolder= Environment.getExternalStorageDirectory();
    }

    public ResponseJsonConvert(String fileName,String data){
        mUrl =fileName;
        mData=data;
        mPackageName="com.bean";
        mSaveFolder= Environment.getExternalStorageDirectory();
    }

    public ResponseJsonConvert setSaveFolder(File saveFolder){
        mSaveFolder=saveFolder;
        return this;
    }

    public ResponseJsonConvert setPackageName(String packageName){
        mPackageName=packageName;
        return this;
    }

    public ResponseJsonConvert setEntityBody(String entityBody){
        mEntityBody=entityBody;
        return this;
    }

    */
/**
     * 拼接文件名
     * @param uri
     * @return
     *//*

    private String joinFileName(Uri uri){
        List<String> segment=uri.getPathSegments();
        if(segment==null||segment.isEmpty()) return "empty";
        StringBuilder sb=new StringBuilder();
        for(String s:segment)
            sb.append(s.substring(0,1).toUpperCase()+s.substring(1));
        return sb.toString()+".java";
    }

    */
/**
     * 准备生成Json实体。生成文件，写包名，将实体写入文件等等
     * @param url
     * @param entity
     *//*

    private void prepareGenerateJsonEntity(String URL, JsonObject entity){
        Uri uri=Uri.parse(URL);
        String fileName=joinFileName(uri);
        File file=new File(mSaveFolder,fileName.substring(0,1).toUpperCase()+fileName.substring(1));
        StringBuilder sb=new StringBuilder();

        try {
            Field fieldKey=JsonObject.class.getDeclaredField("mKey");
            file.createNewFile();

            fieldKey.setAccessible(true);
            fieldKey.set(entity,uri.getLastPathSegment()); //反射使第一层json对象有key值
            sb.append("package ").append(mPackageName).append("\n\n");
            firstLayerParse(sb,entity);
            SaveUtil.saveToFile(file,sb.toString(),false);
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    */
/**
     * 解析第一层json（正常json第一层只可能是{}或者[]）
     * @param sb
     * @param entity
     *//*

    private void firstLayerParse(StringBuilder sb, JsonObject entity){
        if(entity.getValue() instanceof Map)
            sb.append(generateNestingClass(entity));
        else if(entity.getValue() instanceof List){
            List<JsonObject> list=entity.getValue();
            if(!list.isEmpty()){
                JsonObject jo=list.get(0);
                if(jo.getValue() instanceof Map)
                    sb.append(generateNestingClass(jo));
            }
        }
    }

    private String basicTypeName(JsonObject jo){
        String type;

        //只有double、long、string、boolean4种类型
        if(jo.getValue() instanceof Integer||jo.getValue() instanceof Long)
            type="long";
        else if(jo.getValue() instanceof Float||jo.getValue() instanceof Double)
            type="double";
        else if(jo.getValue() instanceof Boolean)
            type="boolean";
        else
            type="String";
        return type;
    }



    */
/**
     * 生成内部类.前条件 jsonObject的value一定是map
     * @param jo
     * @return
     *//*

    private String generateNestingClass(JsonObject jo){
        StringBuilder sb=new StringBuilder();
        Map<String,JsonObject> map=jo.getValue();
        List<JsonObject> nestingJsonObject=new ArrayList<>(); //嵌套类临时保存，到最后生成
        Iterator<String> keyIter=map.keySet().iterator();

        sb.append("public class "+jo.getKey().substring(0,1).toUpperCase()+jo.getKey().substring(1)).append("{").append("\n");
        while(keyIter.hasNext()){
            String key=keyIter.next();
            JsonObject element=map.get(key);

            try{
                if(element.getValue() instanceof List){
                    List<JsonObject> list=element.getValue();
                    String genericType="String";

                    if(!list.isEmpty()){
                        JsonObject listElement=list.get(0);
                        
                        if(listElement.getValue() instanceof Map){
                            Field elementField=JsonObject.class.getDeclaredField("mKey");
                            
                            genericType=element.getKey().substring(0,1).toUpperCase()+element.getKey().substring(1);
                            elementField.setAccessible(true);
                            elementField.set(listElement,genericType);
                            nestingJsonObject.add(listElement);
                        }
                        else
                            genericType=basicTypeName(listElement);
                        genericType=genericType.substring(0,1).toUpperCase()+genericType.substring(1);
                    }
                    sb.append("\t\t")
                            .append("public List<")
                            .append(genericType)
                            .append("> ")
                            .append(key)
                            .append(";")
                            .append("\n");
                }
                else if(element.getValue() instanceof Map){
                    sb.append("\t\t")
                            .append("public ")
                            .append(element.getKey().substring(0,1).toUpperCase()+element.getKey().substring(1))
                            .append(" ")
                            .append(element.getKey())
                            .append(";")
                            .append("\n");
                    nestingJsonObject.add(element); //将嵌套类临时保存，到最后生成嵌套类字符串
                }
                else{
                    String type=basicTypeName(element);
                    sb.append("\t\t")
                            .append("public ")
                            .append(type+" ")
                            .append(key)
                            .append(";")
                            .append("\n");
                }
            }catch(ClassCastException e){
                //如果是null，会抛出ClassCastException，我们默认这个字段为String
                sb.append("\t\t")
                        .append("public String ")
                        .append(key)
                        .append(";")
                        .append("\n");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        sb.append("}").append("\n");
        sortFields(sb);
        for(JsonObject nestingClass:nestingJsonObject)
            sb.append(generateNestingClass(nestingClass));
        return sb.toString();
    }

    */
/**
     * 对属性进行排序 1.boolean 2.long 3.double 4.String 5.other
     * @param unSortString
     *//*

    private void sortFields(StringBuilder unSortString){
        int start=0;
        int afterIndex; //接到某个位置之后

        afterIndex=unSortString.indexOf("{\n")+2; //＋2是为了放在{\n两个字符之后
        //调整boolean型到最前面
        while(true){
            start=unSortString.indexOf("\t\tpublic boolean",start+1);
            if(start!=-1){
                int end=unSortString.indexOf("\n",start)+1; //end一定不等于－1，略过判断.＋1是为了替换时把换行带上
                String temp=unSortString.substring(start,end);
                unSortString.replace(start,end,"");
                unSortString.insert(afterIndex,temp);
            }
            else break;
        }
        start=0; //清空起始点索引
        int attemptGetBooleanAfter=unSortString.lastIndexOf("public boolean");
        if(attemptGetBooleanAfter!=-1)
            afterIndex=unSortString.indexOf("\n",attemptGetBooleanAfter)+1;
        //调整long型到boolean型后面或者最前面
        while(true){
            start=unSortString.indexOf("\t\tpublic long",start+1);
            if(start!=-1){
                int end=unSortString.indexOf("\n",start)+1;
                String temp=unSortString.substring(start,end);
                unSortString.replace(start,end,"");
                unSortString.insert(afterIndex,temp);
            }
            else break;
        }
        start=0;
        int attemptGetLongAfter=unSortString.lastIndexOf("public long");
        if(attemptGetLongAfter!=-1)
            afterIndex=unSortString.indexOf("\n",attemptGetLongAfter)+1;
        //调整double到boolean和long型后面或者最前面
        while(true){
            start=unSortString.indexOf("\t\tpublic double",start+1);
            if(start!=-1){
                int end=unSortString.indexOf("\n",start)+1;
                String temp=unSortString.substring(start,end);
                unSortString.replace(start,end,"");
                unSortString.insert(afterIndex,temp);
            }
            else break;
        }
        start=0;
        int attemptGetDoubleAfter=unSortString.lastIndexOf("public double");
        if(attemptGetDoubleAfter!=-1)
            afterIndex=unSortString.indexOf("\n",attemptGetDoubleAfter)+1;
        //调整String到long型和boolean和double型后面或者最前面
        while(true){
            start=unSortString.indexOf("\t\tpublic String",start+1);
            if(start!=-1){
                int end=unSortString.indexOf("\n",start)+1;
                String temp=unSortString.substring(start,end);
                unSortString.replace(start,end,"");
                unSortString.insert(afterIndex,temp);
            }
            else break;
        }
    }

    */
/**
     * 开始调用接口,生成json实体类
     *//*

    public void start(){
        if(mData!=null){
            try {
                prepareGenerateJsonEntity(mUrl,FastJson.fromJson(mData));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            for(Request request:mRequests){
                request.setListener(new Listener<String,Object,Object>(){

                    @Override
                    public void onRawData(Request r, byte[] data){

                    }

                    @Override
                    public void onTranslateJson(Request r, String json) {

                    }

                    @Override
                    public void onResponseListener(Request r, String result,Object none1,Object none2){
                        try {
                            JsonObject jo=FastJson.fromJson(result);
                            Object entity;

                            if(!TextUtils.isEmpty(mEntityBody)) entity=jo.findValue(mEntityBody,null);
                            else entity=jo.getValue();
                            if(entity!=null){
                                if(!mSaveFolder.exists())
                                    mSaveFolder.mkdirs();
                                prepareGenerateJsonEntity(r.getUrl(),jo);
                            }
                        } catch (IOException e) {
                            System.out.println("实体类生成失败:"+e);
                        }
                        r.clear();
                    }

                    @Override
                    public void onErrorListener(Request r, String error) {
                        System.out.println("实体类生成失败:"+error);
                    }
                });
                request.start();
            }
        }
    }
}*/
