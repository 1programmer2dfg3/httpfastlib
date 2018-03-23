package com.fastlib.app;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;

import com.fastlib.app.task.Action;
import com.fastlib.app.task.Task;
import com.fastlib.app.task.ThreadType;
import com.fastlib.base.OldViewHolder;
import com.fastlib.utils.json.JsonObject;
import com.fastlib.utils.json.JsonViewBinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sgfb on 16/9/21.
 * 数据填充来自 1.Intent数据传递 2.服务器 3.特殊
 */
public abstract class JsonActivity extends FastActivity{
    protected JsonViewBinder mJsonViewBinder;
    protected OldViewHolder mViewHolder;

    public JsonActivity(){
        mPreparedTaskRemain++;
    }

    public Pair<JsonViewBinder.ViewResolve,Class<? extends View>>[] generateViewResolves(){
        return null;
    }

    @Override
    protected void afterSetContentView(boolean flag){
        super.afterSetContentView(flag);
        mViewHolder=OldViewHolder.get(findViewById(android.R.id.content));
        startTask(Task.begin(0)
        .next(new Action<Integer,Integer>(){

            @Override
            protected Integer execute(Integer param){  //初始化有遍历View过程，放入工作线程来处理
                mJsonViewBinder=new JsonViewBinder(JsonActivity.this);
                Pair<JsonViewBinder.ViewResolve,Class<? extends View>>[] extraViewResolves=generateViewResolves();
                if(extraViewResolves!=null&&extraViewResolves.length>0)
                    for(Pair<JsonViewBinder.ViewResolve,Class<? extends View>> pair:extraViewResolves)
                        mJsonViewBinder.putResolve(pair.first,pair.second);
                return 0;
            }
        })
        .next(new Action<Integer,Integer>(){
            @Override
            protected Integer execute(Integer param){
                prepareTask();
                return 0;
            }
        }, ThreadType.MAIN)
        .next(new Action<Integer,Map<String,Object>>(){  //将Intent中的参数全部取出，尝试填充对应视图

            @Override
            protected Map<String, Object> execute(Integer param) {
                Map<String,Object> map=new HashMap<>();
                Bundle bundle=getIntent().getExtras();
                Set<String> keys=bundle.keySet();
                for(String key:keys)
                    map.put(key,bundle.get(key));
                return map;
            }
        })
        .next(new Action<Map<String,Object>,Integer>(){
            @Override
            protected Integer execute(Map<String, Object> param){
                mJsonViewBinder.bindDataToView(param);
                return 0;
            }
        },ThreadType.MAIN));
    }

    /**
     * 填充json数据到视图
     * @param json json字符串
     */
    protected void inflaterJsonToView(String json){
        try {
            mJsonViewBinder.bindDataToView(json);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    protected void inflaterJsonToView(JsonObject jo){
        mJsonViewBinder.bindDataToView(jo);
    }
}