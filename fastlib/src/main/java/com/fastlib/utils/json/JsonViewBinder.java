package com.fastlib.utils.json;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.fastlib.base.OldViewHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sgfb on 16/4/23.
 * Json键名对id自动绑定数据与视图
 */
public class JsonViewBinder{
    private Map<Class,ViewResolve> mResolves; //视图类型和对应解析器
    private Map<String,Integer> mNameWithId; //视图id值与视图名映射
    private Context mContext;
    private View mRootView;

    /**
     * 应在setContentView之后使用此构造
     * @param activity 主模块
     */
    public JsonViewBinder(Activity activity){
        this(activity,activity.findViewById(android.R.id.content));
    }

    /**
     * 指定上下文和相对根视图
     * @param context 上下文
     * @param convertView 相对根视图
     */
    public JsonViewBinder(Context context, View convertView){
        mContext=context;
        mResolves=new HashMap<>();
        mNameWithId =new HashMap<>();
        mRootView=convertView;
        TextViewResolve tvResolve=new TextViewResolve();
        CheckBoxResolve cbResolve=new CheckBoxResolve();
        mResolves.put(TextView.class,tvResolve);
        mResolves.put(AppCompatTextView.class,tvResolve);
        mResolves.put(CheckBox.class,cbResolve);
        mResolves.put(AppCompatCheckBox.class, cbResolve);
        if(convertView!=null)
            getAllChild(mNameWithId,convertView);
    }

    /**
     * 绑定json数据到视图,使用解析时的根视图
     * @param data 映射数据
     */
    public void bindDataToView(Map<String,Object> data){
        bindDataToView(mRootView,data);
    }

    /**
     * 绑定json数据到视图,使用自定义的根视图
     * @param root 指定根视图
     * @param data 映射数据
     */
    public void bindDataToView(View root, Map<String,Object> data){
        bindDataToView(root,data, mNameWithId);
    }

    /**
     * 绑定json数据到视图
     * @param json json字符串
     */
    public void bindDataToView(String json) throws IOException{
        bindDataToView(FastJson.fromJson(json));
    }

    /**
     * 绑定json数据到视图
     * @param jo json对象
     */
    public void bindDataToView(JsonObject jo){
        for(Map.Entry<String,Integer> entry:mNameWithId.entrySet()){
            Integer id=entry.getValue();
            if(id==null)
                continue;
            View view=mRootView.findViewById(id);
            if(view==null)
                continue;
            ViewResolve vr=mResolves.get(view.getClass());
            Object obj=jo.findValue(entry.getKey(),true,null);
            if(vr!=null&&obj!=null)
                vr.resolve(view,obj);
        }
    }

    /**
     * 绑定json数据到视图,使用自定义的根视图和视图名与id映射
     * @param root
     * @param data
     * @param views
     */
    public void bindDataToView(View root, Map<String,Object> data, Map<String,Integer> views){
        Iterator<String> iter=data.keySet().iterator();
        while(iter.hasNext()){
            String key=iter.next();
            Integer id=views.get(key);
            if(id==null)
                continue;
            View view=root.findViewById(id);
            ViewResolve vr=mResolves.get(view.getClass());
            Object obj=data.get(key);
            if(vr!=null)
                vr.resolve(view,obj);
        }
    }

    /**
     * 绑定到视图持有者中
     * @param item
     * @param holder
     */
    public void bindDataToView(JsonObject item, OldViewHolder holder){
        Iterator<String> iter= mNameWithId.keySet().iterator();
        while(iter.hasNext()){
            String key=iter.next();
            int value=mNameWithId.get(key);
            Object obj=item.findValue(mContext,value);
            View view=holder.getView(value);
            ViewResolve vr=mResolves.get(view.getClass());
            if(vr!=null)
                vr.resolve(view,obj);
        }
    }

    public void putResolve(ViewResolve resolve,Class<? extends View> ...cla){
        for(Class<? extends View> c:cla)
            mResolves.put(c,resolve);
    }

    /**
     * 获取所有视图的名字和id
     * @param map
     * @param v
     */
    private void getAllChild(Map<String,Integer> map,View v){
        int id=v.getId();
        if(id!=-1)
            map.put(mContext.getResources().getResourceEntryName(v.getId()),v.getId());
        if(v instanceof ViewGroup) {
            ViewGroup vg=(ViewGroup)v;
            for(int i=0;i<vg.getChildCount();i++)
                getAllChild(map,vg.getChildAt(i));
        }
    }

    /**
     * 检查是否包含这个视图
     * @param map
     * @param value
     * @return
     */
    private boolean checkContain(Map<String,Integer> map,String value){
        Iterator<String> iter=map.keySet().iterator();
        while(iter.hasNext()){
            String s=iter.next();
            if(s.equals(value))
                return true;
        }
        return false;
    }

    /**
     * 将数据解析成视图的桥梁
     */
    public interface ViewResolve{
        void resolve(View view,Object reader);
    }

    /**
     * 一个默认的TextView解析器
     */
    public final class TextViewResolve implements ViewResolve{

        @Override
        public void resolve(View view,Object obj){
            TextView tv=(TextView)view;
            if(obj instanceof JsonReader){
                JsonReader reader= (JsonReader) obj;
                try {
                    final JsonToken token=reader.peek();
                    switch (token){
                        case NUMBER:
                        case STRING:
                            tv.setText(reader.nextString());
                            break;
                        case BOOLEAN:
                            tv.setText(Boolean.toString(reader.nextBoolean()));
                            break;
                        case NULL:
                            tv.setText(null);
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                }catch(IOException e){
                    //do noting
                }
            }
            else tv.setText(obj.toString());
        }
    }

    /**
     * 一个默认的CheckBox解析器
     */
    public final class CheckBoxResolve implements ViewResolve{

        @Override
        public void resolve(View view,Object obj){
            CheckBox cb=(CheckBox)view;
            if(obj instanceof Boolean){
                boolean b= (boolean) obj;
                cb.setChecked(b);
            }
            else if(obj instanceof Integer){
                int i=(int)obj;
                cb.setChecked(i>0);
            }
            else if(obj instanceof String){
                String s= (String) obj;
                cb.setChecked(!s.equals("0"));
            }
            else if(obj instanceof JsonReader){
                JsonReader reader= (JsonReader) obj;
                try {
                    JsonToken token=reader.peek();
                    switch (token){
                        case BOOLEAN:
                            cb.setChecked(reader.nextBoolean());
                            break;
                        case STRING:
                            String value=reader.nextString().toLowerCase();
                            if(value.equals("true")||value.equals("false"))
                                cb.setChecked(value.equals("true"));
                            break;
                        case NUMBER:
                            double doubleValue=reader.nextDouble();
                            cb.setChecked(doubleValue>0);
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                } catch (IOException e) {
                    //do noting
                }
            }
        }
    }
}
