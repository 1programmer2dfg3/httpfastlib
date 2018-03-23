package com.fastlib.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fastlib.app.FastActivity;
import com.fastlib.base.AdapterViewState;
import com.fastlib.base.OldViewHolder;
import com.fastlib.base.Refreshable;
import com.fastlib.net.Listener;
import com.fastlib.net.Request;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 单类型绑定适配器,将视图与服务器中的数据捆绑
 * @param <T> 数据类型
 * @param <R> 返回类型
 */
public abstract class SingleAdapter<T,R> extends BaseAdapter implements Listener<R,Object,Object>{
	public static final String TAG=SingleAdapter.class.getSimpleName();

	protected Context mContext;
	protected List<T> mData;
	private AdapterViewState mViewState;
	protected Request mRequest;
	private Refreshable mRefreshLayout;
	private ThreadPoolExecutor mThreadPool;
	private int mItemLayoutId;
	private int mPerCount; //每次读取条数，默认为1
	protected boolean isRefresh,isMore,isLoading;

	public abstract Request generateRequest();

	/**
	 * 数据绑定视图
	 * @param position
	 * @param data
	 * @param holder
	 */
	public abstract void binding(int position, T data, OldViewHolder holder);

	/**
	 * 返回的数据转换
	 * @param result
	 * @return
	 */
	public abstract List<T> translate(R result);

	/**
	 * 请求更多数据时的请求
	 * @param request
	 */
	public abstract void getMoreDataRequest(Request request);

	/**
	 * 刷新数据时的请求
	 * @param request
	 */
	public abstract void getRefreshDataRequest(Request request);

	public SingleAdapter(Context context, @LayoutRes int resId){
		this(context,resId,true);
	}

	public SingleAdapter(Context context, @LayoutRes int resId, boolean startNow){
		mContext=context;
		mRequest= generateRequest();
		mPerCount=1;
		mItemLayoutId=resId;
		isRefresh=true;
		isMore=true;
		isLoading=false;
		mRequest.setGenericType(new Type[]{getResponseType()});
		mRequest.setListener(this);
		if(mContext instanceof FastActivity)
			((FastActivity)mContext).addRequest(mRequest);
		if(startNow)
		    refresh();
	}

	private Type getResponseType(){
		Method[] methods=getClass().getDeclaredMethods();
		for(Method m:methods)
		    if(m.getName().equals("translate")){
				Type type=m.getGenericParameterTypes()[0];
				if(type!=Object.class)
					return type;
			}
		return null;
	}

	@Override
	public int getCount() {
		return mData==null?0:mData.size();
	}

	@Override
	public T getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final OldViewHolder viewHolder = OldViewHolder.get(mContext, convertView, parent, mItemLayoutId);
		if(position>=getCount()-1&&isMore&&!isLoading)
			loadMoreData();
		binding(position,getItem(position),viewHolder);
		return viewHolder.getConvertView();
	}

	/**
	 * 向服务器请求的参数
	 */
	private void loadMoreData(){
		isLoading=true;
		isRefresh=false;
		if(mViewState!=null)
			mViewState.onStateChanged(AdapterViewState.STATE_LOADING);
		getMoreDataRequest(mRequest);
		mRequest.setExecutor(mThreadPool).start(false);
	}

	public void refresh(){
		isLoading=true;
		isRefresh=true;
		//刷新之后也许有更多数据？
		isMore=true;
		getRefreshDataRequest(mRequest);
		mRequest.setExecutor(mThreadPool).start(true);
	}

	public void setViewStateListener(AdapterViewState state){
		mViewState=state;
	}

	public void setLoadCount(int count){
		mPerCount=count;
	}

	public void setRefreshLayout(Refreshable refreshLayout){
		mRefreshLayout=refreshLayout;
	}

	public ThreadPoolExecutor getThreadPool(){
		return mThreadPool;
	}

	public void setThreadPool(ThreadPoolExecutor threadPool) {
		mThreadPool = threadPool;
	}

	/**
	 * 设置是否仅请求一次,需要在请求之前设置
	 * @param requestOnce
	 */
	public void setRequestOnce(boolean requestOnce){
		isMore=!requestOnce;
	}

	/**
	 * 如何对待新数据
	 * @param list
	 */
	private void dataRefresh(List<T> list){
		if(isRefresh) mData = list;
		else{
			if(list!=null)
			    mData.addAll(list);
		}
	}

	@Override
	public void onResponseListener(Request request,R result,Object none1,Object none2){
		if(mRefreshLayout!=null)
			mRefreshLayout.setRefreshStatus(false);
		List<T> list=translate(result);

		isLoading=false;
		if(list==null||list.size()<mPerCount){
			isMore=false;
			if(mViewState!=null)
				mViewState.onStateChanged(AdapterViewState.STATE_NO_MORE);
		}
		dataRefresh(list);
		notifyDataSetChanged();
	}

	@Override
	public void onErrorListener(Request r,String error){
		if(mRefreshLayout!=null)
			mRefreshLayout.setRefreshStatus(false);
		isLoading=false;
	}

	@Override
	public void onRawData(Request r, byte[] data){
		//被适配
	}

	@Override
	public void onTranslateJson(Request r, String json) {
		//被适配
	}

	/**
	 * 获取适配器中所有数据
	 * @return 适配器中所有数据
     	*/
	public List<T> getData() {
		return mData;
	}

	/**
	 * 适配器中的网络请求
	 * @return 网络请求
     	*/
	public Request getRequest() {
		return mRequest;
	}
}
