package com.fastlib.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.fastlib.R;
import com.fastlib.annotation.ContentView;
import com.fastlib.annotation.TransitionAnimation;
import com.fastlib.app.task.EmptyAction;
import com.fastlib.app.task.NoReturnAction;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sgfb on 16/9/5.
 * Activity基本封装.
 * 1.ContentView注解，Bind视图注解.
 * 2.全局事件注册和解注册(EventObserver)
 * 3.线程池及顺序任务列辅助方法(mThreadPool和startTasks(Task))
 * 4.本地数据辅助（LocalData）
 * 5.相机相册调取（openAlbum(PhotoResultListener)和openCamera(PhotoResultListener))
 * 6.6.0权限获取辅助(mPermissionHelper)
 * 7.延时启动优化
 */
public abstract class FastActivity extends AppCompatActivity implements Deferrable {
    private static final int THREAD_POOL_SIZE =Runtime.getRuntime().availableProcessors()/2+1;

    protected ThreadPoolExecutor mThreadPool;
    protected PermissionHelper mPermissionHelper;
    public volatile int mPreparedTaskRemain=4; //剩余初始化异步任务，当初始化异步任务全部结束时调用alreadyPrepared

    private boolean isFirstLoaded=false;
    private boolean isGatingPhoto; //是否正在获取图像
    private boolean isHadTransitionAnimation=false;
    private LocalDataInject mLocalDataInject;
    private PhotoResultListener mPhotoResultListener;
    private TaskLauncher mTaskLauncher;
    private LoadingDialog mLoading;
    private List<Request> mRequests = new ArrayList<>();
    private ViewStub mViewStub;
    private View mDeferView;

    protected abstract void alreadyPrepared(); //所有初始化任务结束

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDeferView= generateDeferLoadingView();
        if(mDeferView==null)
            init();
        checkContentViewInject();
    }

    private void init(){
        mPermissionHelper=new PermissionHelper();
        mLocalDataInject=new LocalDataInject(this);
        mThreadPool=generateThreadPool();
        mTaskLauncher=new TaskLauncher(this,mThreadPool);
        checkTransitionInject();
        mThreadPool.execute(new Runnable(){
            @Override
            public void run() {
                EventObserver.getInstance().subscribe(FastActivity.this,FastActivity.this);
                prepareTask();
            }
        });
        startInternalPrepareTask();
    }

    /**
     * 后期绑定mThreadPool增加灵活性
     * @return 线程池
     */
    protected ThreadPoolExecutor generateThreadPool(){
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * 查看是否有共享元素动画
     */
    private void checkTransitionInject(){
        TransitionAnimation ta=getClass().getAnnotation(TransitionAnimation.class);
        if(ta!=null){
            supportPostponeEnterTransition(); //暂停共享元素动画
            isHadTransitionAnimation=true;
        }
    }

    /**
     * ContentView注入，如果存在的话
     */
    private void checkContentViewInject(){
        ContentView cv=getClass().getAnnotation(ContentView.class);
        if(cv!=null){
            if(mDeferView==null) setContentView(cv.value());
            else{
                FrameLayout frameLayout=new FrameLayout(this);
                mViewStub=new ViewStub(this,cv.value());
                frameLayout.addView(mViewStub);
                frameLayout.addView(mDeferView);
                setContentView(frameLayout);
            }
        }
    }

    /**
     * 启动网络请求
     * @param request 网络请求
     */
    protected void net(Request request) {
        if (!mRequests.contains(request))
            mRequests.add(request);
        request.setHost(this).setExecutor(mThreadPool).start(false);
    }

    /**
     * 增加网络请求到列表中，但是不立即请求
     * @param request 网络请求
     */
    public void addRequest(Request request) {
        if (!mRequests.contains(request))
            mRequests.add(request);
    }

    /**
     * 开始线性任务
     * @param task 任务
     */
    public void startTask(Task task){
        mTaskLauncher.startTask(task);
    }

    /**
     * 开始线性任务，并且有异常处理和尾回调
     * @param task 任务
     * @param exceptionHandler 异常处理
     * @param lastAction 尾回调
     */
    public void startTask(Task task, NoReturnAction<Throwable> exceptionHandler, EmptyAction lastAction){
        mTaskLauncher
                .setExceptionHandler(exceptionHandler)
                .setLastTask(lastAction)
                .startTask(task);
    }

    /**
     * 开启获取相册照片
     * @param photoResultListener 取相册中相片回调
     */
    public void openAlbum(final PhotoResultListener photoResultListener) {
        mPermissionHelper.requestPermission(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new Runnable() {
            @Override
            public void run() {
                isGatingPhoto = true;
                mPhotoResultListener = photoResultListener;
                ImageUtil.openAlbum(FastActivity.this);
            }
        }, new Runnable() {
            @Override
            public void run() {
                N.showShort(FastActivity.this, "请开启读存储卡权限");
            }
        });
    }

    /**
     * 开启相机获取照片并且指定存储位置
     * @param photoResultListener 照相成功后回调
     * @param path 指定路径,这个路径的文件不能已被创建
     */
    public void openCamera(final PhotoResultListener photoResultListener, final String path) {
        mPermissionHelper.requestPermission(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new Runnable() {
            @Override
            public void run() {
                mPermissionHelper.requestPermission(FastActivity.this,new String[]{Manifest.permission.CAMERA}, new Runnable() {
                    @Override
                    public void run() {
                        isGatingPhoto = true;
                        mPhotoResultListener = photoResultListener;
                        if (TextUtils.isEmpty(path))
                            ImageUtil.openCamera(FastActivity.this);
                        else
                            ImageUtil.openCamera(FastActivity.this,Uri.fromFile(new File(path)));
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        N.showShort(FastActivity.this, "请开启使用照相机权限");
                    }
                });
            }
        }, new Runnable() {
            @Override
            public void run() {
                N.showShort(FastActivity.this, "请开启写存储卡权限");
            }
        });
    }

    /**
     * 开启相机获取照片
     * @param photoResultListener 拍照成功回调
     */
    public void openCamera(PhotoResultListener photoResultListener){
        File directory=new File(getExternalCacheDir(), Environment.DIRECTORY_PICTURES);

        directory.mkdirs();
        openCamera(photoResultListener,new File(directory,System.currentTimeMillis()+".jpg").getAbsolutePath());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLocalDataInject.injectChildBack(data);
        if (isGatingPhoto) {
            isGatingPhoto = false;
            if (resultCode != Activity.RESULT_OK)
                return;
            Uri photoUri = ImageUtil.getImageFromActive(this, requestCode, resultCode, data);
            if (photoUri != null) {
                String photoPath = ImageUtil.getImagePath(this, photoUri);
                if (mPhotoResultListener != null)
                    mPhotoResultListener.onPhotoResult(photoPath);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHelper.permissionResult(requestCode,permissions,grantResults);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        afterSetContentView(true);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        afterSetContentView(true);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        afterSetContentView(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventObserver.getInstance().unsubscribe(this,this);
        if(mThreadPool!=null){
            mThreadPool.shutdownNow();
            mThreadPool.purge();
            mThreadPool=null;
        }
        if(mRequests!=null){
            for (Request request : mRequests)
                request.clear();
            mRequests.clear();
            mRequests=null;
        }
    }

    /**
     * 在设置布局后视图注解和局部数据注解
     * @param waitDeferTask 是否等待延迟任务
     */
    protected void afterSetContentView(boolean waitDeferTask){
        if(mDeferView==null||!waitDeferTask){
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ViewInject.inject(FastActivity.this,findViewById(android.R.id.content),mThreadPool);
                    prepareTask();
                }
            });
            mThreadPool.execute(new Runnable() {
                @Override
                public void run(){
                    mLocalDataInject.localDataInject();
                    prepareTask();
                }
            });
        }
    }

    /**
     * 快捷启动一个Activity
     * @param cla 启动的Activity类
     */
    public void startActivity(Class<? extends Activity> cla) {
        startActivity(new Intent(this, cla));
    }

    /**
     * 快捷启动一个Activity并且获取返回
     * @param cla 启动的Activity类
     * @param requestCode 请求码
     */
    public void startActivityForResult(Class<? extends Activity> cla,int requestCode){
        startActivityForResult(new Intent(this,cla),requestCode);
    }

    /**
     * 获取权限辅助工具
     * @return 权限辅助工具
     */
    public PermissionHelper getPermissionHelper(){
        return mPermissionHelper;
    }

    /**
     * 显示进度条
     */
    public void loading(){
        loading(getString(R.string.loading));
    }

    boolean isLoadingShowing=false;

    /**
     * 显示无限进度
     * @param hint 进度提示
     */
    public void loading(final String hint){
        if(mLoading==null)
            mLoading=new LoadingDialog();
        if(!isLoadingShowing){
            isLoadingShowing=true;
            mLoading.show(getSupportFragmentManager());
        }
        if(Looper.getMainLooper()!=Looper.myLooper()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoading.setHint(hint);
                }
            });
        }
        else mLoading.setHint(hint);
    }

    /**
     * 关闭进度条
     */
    public void dismissLoading(){
        if(mLoading!=null) {
            if(Thread.currentThread()!=Looper.getMainLooper().getThread())
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.dismiss();
                    }
                });
            else
            mLoading.dismiss();
        }
        isLoadingShowing=false;
    }

    /**
     * 其中一个异步任务完成
     */
    protected synchronized void prepareTask(){
        if(--mPreparedTaskRemain<=0)
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    if(mDeferView!=null) {
                        if(mDeferView.getParent() instanceof ViewGroup){
                            ViewGroup parent= (ViewGroup) mDeferView.getParent();
                            parent.removeView(mDeferView);
                        }
                    }
                    alreadyPrepared();
                    if(isHadTransitionAnimation)
                        supportStartPostponedEnterTransition();
                }
            });
    }

    /**
     * 延迟加载视图，如果不为空，使用延迟加载策略
     * @return  延迟加载视图
     */
    protected View generateDeferLoadingView(){
        return null;
    }

    /**
     * 内部预任务
     * @return 额外的预任务
     */
    protected Task internalPrepare(){
        return null;
    }

    @Override
    public void firstLoad(){
        if(!isFirstLoaded&&mViewStub!=null){
            isFirstLoaded=true;
            mViewStub.inflate();
            init();
            afterSetContentView(false);
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
            startTask(task,null,null);
        }
    }
}