package com.fastlib.utils.json;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sgfb on 16/9/8.
 * 安卓原生json解析封装.这个解析类不负责将json解析为具体类，而是转换为Map<String,JsonObject>和List<JsonObject><br/>
 */
public class FastJson{

    private FastJson(){
        //no instance
    }

    public static JsonObject fromJson(String json) throws IOException {
        return fromJsonReader(new JsonReader(new StringReader(json)));
    }

    public static JsonObject fromJsonReader(JsonReader jsonReader) throws IOException {
        JsonObject jo=null;
        JsonToken jt=jsonReader.peek();
        StringBuilder jsonRaw=new StringBuilder();

        if(jt==JsonToken.BEGIN_ARRAY){
            List<JsonObject> list=readJsonArray(jsonReader,jsonRaw);
            jo=new JsonObject(jsonRaw.toString(),"",list); //根节点没有名字
        }
        else if(jt==JsonToken.BEGIN_OBJECT) {
            Map<String,JsonObject> map= readJsonObj(jsonReader,jsonRaw);
            jo=new JsonObject(jsonRaw.toString(),"",map); //根节点没有名字
        }
        jsonReader.close();
        return jo;
    }

    /**
     * 读取json对象
     * @param jsonReader
     * @param sb 记录源字符
     * @return
     * @throws IOException
     */
    private static Map<String,JsonObject> readJsonObj(JsonReader jsonReader,StringBuilder sb)throws IOException {
        jsonReader.beginObject();
        Map<String,JsonObject> map=new HashMap<>();
        JsonToken jt=jsonReader.peek();
        String name=null;

        if(jt==JsonToken.NULL){
            jsonReader.endObject();
            return null;
        }
        while(jsonReader.hasNext()){
            String divider=",";
            if(jt==JsonToken.NAME) {
                name = jsonReader.nextName();
                divider="";
            }
            else if(jt==JsonToken.BEGIN_OBJECT){
                StringBuilder childJsonRaw=new StringBuilder();
                Object value=readJsonObj(jsonReader,childJsonRaw);
                map.put(name,new JsonObject(childJsonRaw.toString(),name,value));
                sb.append("\""+name+"\":"+childJsonRaw.toString());
            }
            else if(jt==JsonToken.BEGIN_ARRAY){
                StringBuilder childJsonRaw=new StringBuilder();
                List<JsonObject> list=readJsonArray(jsonReader,childJsonRaw);
                map.put(name,new JsonObject(childJsonRaw.toString(),name,list));
                sb.append("\""+name+"\":"+childJsonRaw.toString());
            }
            else if(jt==JsonToken.BOOLEAN){
                boolean bool=jsonReader.nextBoolean();
                map.put(name,new JsonObject("{\""+name+"\":"+bool+"}",name,bool));
                sb.append("\""+name+"\":"+bool);
            }
            else if(jt==JsonToken.NUMBER){
                double temp=jsonReader.nextDouble();
                String record;
                if(Math.abs(temp-(long)temp)>0&&Math.abs(temp-(long)temp)<1)
                    map.put(name,new JsonObject(record=("{\""+name+"\":"+temp+"}"),name,temp));
                else if(Math.abs(temp)>Integer.MAX_VALUE)
                    map.put(name,new JsonObject(record=("{\""+name+"\":"+(long)temp+"}"),name,(long)temp));
                else
                    map.put(name,new JsonObject(record=("{\""+name+"\":"+(int)temp+"}"),name,(int)temp));
                sb.append(record.substring(1,record.length()-1));
            }
            else if(jt==JsonToken.STRING){
                String value=jsonReader.nextString();
                map.put(name, new JsonObject("{\"" + name + "\":" + "\"" +value+"\""+"}",name,value));
                sb.append("\""+name + "\":" + "\"" +value+"\"");
            }
            else if(jt==JsonToken.NULL){
                jsonReader.nextNull();
                map.put(name,new JsonObject("{}",name,null)); //是否有意义？
            }
            else jsonReader.skipValue();
            jt=jsonReader.peek();
            sb.append(divider);
        }
        jsonReader.endObject();
        //去掉最后的逗号,左右加上花扩号
        sb.deleteCharAt(sb.length()-1);
        sb.insert(0,"{");
        sb.append("}");
        return map;
    }

    /**
     * 读取json列表
     * @param jsonReader
     * @param sb 记录源字符
     * @return
     * @throws IOException
     */
    private static List<JsonObject> readJsonArray(JsonReader jsonReader,StringBuilder sb) throws IOException{
        jsonReader.beginArray();
        List<JsonObject> list=new ArrayList<>();
        JsonToken jt=jsonReader.peek();

        if(jt==JsonToken.NULL){
            jsonReader.endArray();
            return null;
        }
        while(jt!= JsonToken.END_ARRAY){
            if(jt==JsonToken.BEGIN_OBJECT){
                StringBuilder record=new StringBuilder();
                Map<String,JsonObject> map=readJsonObj(jsonReader,record);
                list.add(new JsonObject(record.toString(),"noname",map));
                sb.append(record.toString());
            }
            else if(jt==JsonToken.BEGIN_ARRAY){
                StringBuilder record=new StringBuilder();
                List<JsonObject> childList=readJsonArray(jsonReader,record);
                list.add(new JsonObject(record.toString(),"noname",childList));
                sb.append(record.toString());
            }
            else if(jt==JsonToken.BOOLEAN){
                boolean value=jsonReader.nextBoolean();
                list.add(new JsonObject("\"noname\":{"+Boolean.toString(value)+"}","noname",value));
                sb.append(Boolean.toString(value));
            }
            else if(jt==JsonToken.NUMBER){
                double temp=jsonReader.nextDouble();
                if(Math.abs(temp-(long)temp)>0&&Math.abs(temp-(long)temp)<1)
                    list.add(new JsonObject("{\"noname\":"+temp+"}","noname",temp));
                else if(Math.abs(temp)>Integer.MAX_VALUE)
                    list.add(new JsonObject("{\"noname\":"+(long)temp+"}","noname",(long)temp));
                else
                    list.add(new JsonObject("{\"noname\":"+(int)temp+"}","noname",(int)temp));
                sb.append(temp);
            }
            else if(jt==JsonToken.STRING){
                String value=jsonReader.nextString();
                list.add(new JsonObject("{\"noname\":\""+value+"\"}","noname",value));
                sb.append("\""+value+"\"");
            }
            else if(jt==JsonToken.NULL)
                list.add(null);
            else jsonReader.skipValue();
            jt=jsonReader.peek();
            sb.append(",");
        }
        //去掉最后的逗号,加上中扩号
        sb.deleteCharAt(sb.length()-1);
        sb.insert(0,"[");
        sb.append("]");
        jsonReader.endArray();
        return list;
    }
}