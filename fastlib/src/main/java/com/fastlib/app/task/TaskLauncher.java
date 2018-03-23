package com.fastlib.app.task;

import android.app.Activity;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.Fragment;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/9/6.
 * 启动线性Task必要的启动器
 */
public class TaskLauncher{
    private Object mHost;
    private ThreadPoolExecutor mThreadPool;
    private volatile boolean mStopFlag;
    private NoReturnAction<Throwable> mExceptionHandler; //一个全局的异常处理器,如果Task有对应异常处理器则不调用此处理器
    private EmptyAction mCompleteAction;

    public TaskLauncher(Activity activity, ThreadPoolExecutor threadPool) {
        mHost = activity;
        mThreadPool = threadPool;
    }

    public TaskLauncher(Fragment fragment,ThreadPoolExecutor threadPool){
        mHost=fragment;
        mThreadPool=threadPool;
    }

    /**
     * 开始线性任务
     * @param task
     */
    public void startTask(Task task){
        Task firstTask=task;
        while(firstTask.getPrevious()!=null)
            firstTask=firstTask.getPrevious();
        threadDispatch(firstTask);
    }

    /**
     * 线性任务线程调度
     * @param task
     */
    private void threadDispatch(final Task task){
        if(task.getOnWhichThread()== ThreadType.MAIN){
            if(Looper.myLooper()==Looper.getMainLooper())
                processTask(task);
            else getHostActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    processTask(task);
                }
            });
        }
        else{
            if(Looper.myLooper()==Looper.getMainLooper())
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        processTask(task);
                    }
                });
            else
                processTask(task);
        }
    }

    /**
     * 线性任务处理具体任务
     * @param task
     */
    private void processTask(Task task){
        try{
            task.process(); //执行事件后才有返回
            if(checkStopStatus(task)){  //中断任务事件
                if(mCompleteAction!=null)
                    mCompleteAction.executeAdapt();
                return;
            }
            Object obj=task.getReturn();
            Task nextTask=task.getNext();
            //过滤任务处理
            if(task.isFilterTask()){
                if(obj!=null&&(obj instanceof Boolean)){
                    Boolean b= (Boolean) obj;
                    if(!b){
                        if(task.isCycleEnd()){
                            while(nextTask!=null&&!nextTask.isAgainTask())
                                nextTask=nextTask.getNext();
                        }
                        else
                            nextTask=task.getCycler();
                    }
                }
                obj=task.getParam(); //过滤任务的参数就是返回，递交给下一个任务
            }
            if(nextTask!=null){
                nextTask.setParam(obj);
                threadDispatch(nextTask);
            }
            else{
                if(mCompleteAction!=null)
                    mCompleteAction.execute(null);
            }
        }catch (Throwable throwable){
            //优先处理任务中存在的异常处理器，如果没有再尝试运行全局异常处理器
            boolean handled=false;
            if(task!=null){
                NoReturnAction<Throwable> taskExceptionHandler=task.getTaskExceptionHandler();
                if(taskExceptionHandler!=null){
                    taskExceptionHandler.execute(throwable);
                    handled=true;
                }
            }
            if(mExceptionHandler!=null&&!handled) mExceptionHandler.execute(throwable);
            if(mCompleteAction!=null) mCompleteAction.execute(null);
        }
    }

    public Activity getHostActivity(){
        if(mHost instanceof Activity) return (Activity) mHost;
        else return ((Fragment)mHost).getActivity();
    }

    private boolean checkStopStatus(Task task){
        return hostIsFinish()||task.isStopNow()||mStopFlag;
    }

    /**
     * 宿主是否已结束生命周期
     * @return true已结束（不继续任务事件）
     */
    private boolean hostIsFinish(){
        if(mHost instanceof Activity){
            Activity activity= (Activity) mHost;
            return activity.isFinishing()||(Build.VERSION.SDK_INT>=17&&activity.isDestroyed());
        }
        else {
            Fragment fragment= (Fragment) mHost;
            return fragment.isRemoving()||fragment.isDetached();
        }
    }

    public void stopNow(boolean stopFlag){
        mStopFlag = stopFlag;
    }

    /**
     * 这次线性任务全局异常处理
     * @param handler 异常处理器
     * @return 线性任务启动器
     */
    public TaskLauncher setExceptionHandler(NoReturnAction<Throwable> handler){
        mExceptionHandler=handler;
        return this;
    }

    /**
     * 无论是正常流程结束还是异常结束，最后都调用这个方法，如果存在的话
     * @param action 结尾任务
     * @return 线性任务启动器
     */
    public TaskLauncher setLastTask(EmptyAction action){
        mCompleteAction=action;
        return this;
    }
}