package com.fastlib.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.fastlib.R;
import com.fastlib.annotation.ContentView;
import com.fastlib.app.task.EmptyAction;
import com.fastlib.app.task.Task;
import com.fastlib.app.task.TaskLauncher;
import com.fastlib.base.Deferrable;
import com.fastlib.net.Request;
import com.fastlib.utils.ImageUtil;
import com.fastlib.utils.LocalDataInject;
import com.fastlib.utils.N;
import com.fastlib.utils.PermissionHelper;
import com.fastlib.utils.ViewInject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 17/1/31.
 * Fragment基本封装
 * 1.全局事件注册和解注册（EventObserver）
 * 2.视图属性和事件注解（Bind）
 * 3.线程池及顺序任务列辅助方法(mThreadPool和startTasks(Task))
 * 4.本地数据辅助（LocalData）
 * 5.相机相册调取（openAlbum(PhotoResultListener)和openCamera(PhotoResultListener))
 * 6.6.0权限获取辅助(mPermissionHelper)
 * 7.延时启动优化
 */
public abstract class FastFragment extends Fragment implements Deferrable{
    protected ThreadPoolExecutor mThreadPool;
    protected PermissionHelper mPermissionHelper;

    private boolean isFirstLoaded=false;
    private boolean isGatingPhoto; //是否正在获取图像
    private volatile int mPreparedTaskRemain=4; //剩余初始化异步任务，当初始化异步任务全部结束时调用alreadyPrepared
    private List<Request> mRequests=new ArrayList<>();
    private LocalDataInject mLocalDataInject;
    private TaskLauncher mTaskLauncher;
    private LoadingDialog mLoading;
    private PhotoResultListener mPhotoResultListener;
    private View mDeferView; //延迟加载预显示视图
    private ViewStub mStubView;

    protected abstract void alreadyPrepared(); //所有初始化任务结束

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //如果延迟加载视图为空，开始初始化等正常流程
        mDeferView=deferLoadingView();
        if(mDeferView==null)
            init();
    }

    private void init() {
        mLocalDataInject = new LocalDataInject(this);
        mPermissionHelper = new PermissionHelper();
        mThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        mTaskLauncher = new TaskLauncher(this, mThreadPool);
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                EventObserver.getInstance().subscribe(getContext(), FastFragment.this);
                prepareTask();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final ContentView cv=getClass().getAnnotation(ContentView.class);
        //如果有延迟视图，把实际视图用stubView存根，等firstLoad后再加载
        if(mDeferView!=null){
            mStubView=new ViewStub(getContext(),cv==null?0:cv.value());
            FrameLayout root=new FrameLayout(getContext());
            root.addView(mStubView);
            root.addView(mDeferView);
            return root;
        }
        if(cv!=null){
            final View root=inflater.inflate(cv.value(),null);
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ViewInject.inject(FastFragment.this,root,mThreadPool);
                    prepareTask();
                }
            });
            return root;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mDeferView==null){
            mThreadPool.execute(new Runnable(){
                @Override
                public void run() {
                    mLocalDataInject.localDataInject();
                    prepareTask();
                }
            });
            startInternalPrepareTask();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        mLocalDataInject.injectChildBack(data); //尝试获取有注解的子Activity返回数据
        if (isGatingPhoto) {
            isGatingPhoto = false;
            if (resultCode != Activity.RESULT_OK)
                return;
            Uri photoUri = ImageUtil.getImageFromActive(getContext(), requestCode, resultCode, data);
            if (photoUri != null) {
                String photoPath = ImageUtil.getImagePath(getContext(), photoUri);
                if (mPhotoResultListener != null)
                    mPhotoResultListener.onPhotoResult(photoPath);
            }
        }
    }

    /**
     * 开启获取相册照片
     * @param photoResultListener 图像获取回调
     */
    public void openAlbum(final PhotoResultListener photoResultListener) {
        mPermissionHelper.requestPermission(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new Runnable() {
            @Override
            public void run() {
                isGatingPhoto = true;
                mPhotoResultListener = photoResultListener;
                ImageUtil.openAlbum(FastFragment.this);
            }
        }, new Runnable() {
            @Override
            public void run() {
                N.showShort(getContext(),"请开启读存储卡权限");
            }
        });
    }

    /**
     * 开启相机获取照片
     * @param photoResultListener 图像获取回调
     */
    public void openCamera(PhotoResultListener photoResultListener) {
        File file=new File(getContext().getExternalCacheDir(), Environment.DIRECTORY_PICTURES);
        file.mkdir();
        openCamera(photoResultListener,ImageUtil.getTempFile("",".jpg",file).getAbsolutePath());
    }

    /**
     * 开启相机获取照片并且指定存储位置
     * @param photoResultListener 图像获取回调
     * @param path 指定照片存储路径
     */
    public void openCamera(final PhotoResultListener photoResultListener,final String path) {
        mPermissionHelper.requestPermission(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new Runnable() {
            @Override
            public void run() {
                mPermissionHelper.requestPermission(getActivity(),new String[]{Manifest.permission.CAMERA}, new Runnable() {
                    @Override
                    public void run() {
                        isGatingPhoto = true;
                        mPhotoResultListener = photoResultListener;
                        if (TextUtils.isEmpty(path))
                            ImageUtil.openCamera(FastFragment.this);
                        else
                            ImageUtil.openCamera(FastFragment.this,Uri.fromFile(new File(path)));
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        N.showShort(getContext(), "请开启使用照相机权限");
                    }
                });
            }
        }, new Runnable() {
            @Override
            public void run() {
                N.showShort(getContext(), "请开启写存储卡权限");
            }
        });
    }

    /**
     * 启动网络请求
     * @param request
     */
    protected void net(Request request){
        if(!mRequests.contains(request))
            mRequests.add(request);
        request.setHost(this).setExecutor(mThreadPool).start(false);
    }

    public void addRequest(Request request){
        if(!mRequests.contains(request))
            mRequests.add(request);
    }

    /**
     * 启动Activity
     * @param cla
     */
    protected void startActivity(Class<? extends Activity> cla){
        startActivity(new Intent(getContext(),cla));
    }

    /**
     * 启动线性任务
     * @param task 任务
     */
    protected void startTask(Task task){
        mTaskLauncher.startTask(task);
    }

    /**
     * 延迟启动替换视图，如果不为空，使用延迟启动策略
     * @return 延迟启动视图
     */
    protected View deferLoadingView(){
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPreparedTaskRemain=2;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventObserver.getInstance().unsubscribe(getContext(),this);
        if(mThreadPool!=null){
            mThreadPool.shutdownNow();
            mThreadPool.purge();
        }
        if(mRequests!=null){
            for(Request request:mRequests)
                request.clear();
        }
    }

    /**
     * 显示进度条
     */
    public void loading(){
        loading(getString(R.string.loading));
    }

    /**
     * 显示无限进度
     * @param hint 进度提示
     */
    public void loading(final String hint){
        if(mLoading==null) {
            mLoading=new LoadingDialog();
            mLoading.show(getFragmentManager(),false);
        }
        if(Looper.getMainLooper()!=Looper.myLooper()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoading.setHint(hint);
                }
            });
        }
    }

    /**
     * 关闭进度条
     */
    public void dismissLoading(){
        if(mLoading!=null){
            mLoading.dismiss();
            mLoading=null;
        }
    }

    /**
     * 从宿主Activity中移除自身
     */
    protected void finish(){
        if(getFragmentManager()==null) return;
        getFragmentManager()
                .beginTransaction()
                .remove(this)
                .commit();
    }

    /**
     * 从宿主Activity中移除自身并且加入指定Fragment
     * @param fragment 替换自身位置的新Fragment
     */
    protected void replce(Fragment fragment){
        getFragmentManager()
                .beginTransaction()
                .replace(getId(),fragment)
                .commit();
    }

    /**
     * 完成单项预任务，如果所有预任务都完成将会回调alreadyPrepared方法
     */
    private synchronized void prepareTask(){
        if(--mPreparedTaskRemain<=0)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    if(mDeferView!=null) {
                        if(mDeferView.getParent() instanceof ViewGroup){
                            ViewGroup parent= (ViewGroup) mDeferView.getParent();
                            parent.removeView(mDeferView);
                        }
                    }
                    alreadyPrepared();
                }
            });
    }

    @Override
    public void firstLoad(){
        if(!isFirstLoaded&&mStubView!=null){
            isFirstLoaded=true;
            final View view=mStubView.inflate();
            init();
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ViewInject.inject(FastFragment.this,view,mThreadPool);
                    prepareTask();
                }
            });
            mThreadPool.execute(new Runnable(){
                @Override
                public void run() {
                    mLocalDataInject.localDataInject();
                    prepareTask();
                }
            });
            startInternalPrepareTask();
        }
    }

    /**
     * 开始内部任务加载
     */
    private void startInternalPrepareTask(){
        Task task=internalPrepare();
        if(task==null)
            prepareTask();
        else{
            task.again(new EmptyAction() {
                @Override
                protected void executeAdapt() {
                    prepareTask();
                }
            });
            startTask(task);
        }
    }

    /**
     * 内部预任务加载
     * @return 额外内部预任务顺序事件
     */
    protected Task internalPrepare(){
        return null;
    }
}