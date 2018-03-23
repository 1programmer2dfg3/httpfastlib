package com.fastlib.widget;

import java.util.HashMap;
import java.util.Map;

import com.fastlib.R;
import com.fastlib.bean.StateLocationView;
import com.fastlib.base.AdapterViewState;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author sgfb
 * 与listview类似的recycleView.具有下拉刷新，有状态变化
 */
public class RecycleListView2 extends RelativeLayout implements AdapterViewState{
	private SwipeRefreshLayout mSwipe;
	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private DividerItemDecoration mDividerItemDecoration;
	private Map<Integer,StateLocationView> mStateView;
	private LinearLayout mHeadView;
	private LinearLayout mFootView;
	private boolean mAutofit;
	protected RecyclerView.OnScrollListener mAutoFitListener=new RecyclerView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			if (mAutofit&&newState == RecyclerView.SCROLL_STATE_IDLE){
				if (mDividerItemDecoration.getOrientation()==LinearLayoutManager.HORIZONTAL){
					int left = recyclerView.getChildAt(0).getLeft();
					int right = recyclerView.getChildAt(0).getRight();
					if (left < 0 && right > 1)
						mRecyclerView.smoothScrollBy(right-1,0);
				}else{
					View child=recyclerView.getChildAt(0);
					int top = child.getTop();
					int bottom = child.getBottom();
					int height=child.getHeight();
					if (top < 0 && bottom > 1){
						if(Math.abs(top)>height/2) //如果超过item一半，往下滚.否则往上
							mRecyclerView.smoothScrollBy(0, bottom);
						else
							mRecyclerView.smoothScrollBy(0,top);
					}
				}
			}
		}
	};

	public RecycleListView2(Context context){
		super(context);
		init();
	}

	public RecycleListView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init(){
		mSwipe=new SwipeRefreshLayout(getContext());
		mRecyclerView=new RecyclerView(getContext());
		mHeadView=new LinearLayout(getContext());
		mFootView=new LinearLayout(getContext());
		mStateView=new HashMap<>();

		mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(getContext()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.addItemDecoration(mDividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
		mSwipe.addView(mRecyclerView);
		mRecyclerView.addOnScrollListener(mAutoFitListener);
		mSwipe.setId(R.id.swipe);
		mHeadView.setId(R.id.headView);
		mFootView.setId(R.id.bottomView);
		LayoutParams swipeLp=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		LayoutParams footLp=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		swipeLp.addRule(RelativeLayout.ABOVE,mFootView.getId());
		swipeLp.addRule(RelativeLayout.BELOW, mHeadView.getId());
		footLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mFootView.setLayoutParams(footLp);
		mSwipe.setLayoutParams(swipeLp);
		addView(mHeadView);
		addView(mFootView);
		addView(mSwipe);
	}

	/**
	 * 当RecyclerView没有适配器或者适配器返回count小于等于0时，更改状态为empty
	 *
	 * @param state 状态
	 */
	@Override
	public void onStateChanged(int state){
		StateLocationView slv=mStateView.get(state);
		if(slv==null)
			return;
		changedState(slv.location, slv.view);
	}

	@Override
	public void addStateView(int state, View view, int location){
		StateLocationView slv=new StateLocationView();
		slv.location=location;
		slv.view=view;
		mStateView.put(state, slv);
	}

	private void changedState(int location,View view){
		mSwipe.setVisibility(View.VISIBLE);
		switch (location){
			case AdapterViewState.LOCATION_HEAD:
				if(mHeadView.getChildCount()>0)
					mHeadView.removeViewAt(0);
				mHeadView.addView(view);
				break;
			case AdapterViewState.LOCATION_FOOT:
				if(mFootView.getChildCount()>0)
					mFootView.removeViewAt(0);
				mFootView.addView(view);
				break;
			case AdapterViewState.LOCATION_MIDDLE_CLEAR:
				if(mHeadView.getChildCount()>0)
					mHeadView.removeViewAt(0);
				if(mFootView.getChildCount()>0)
					mFootView.removeViewAt(0);
				mSwipe.setVisibility(View.GONE);
				mHeadView.addView(view);
				break;
			case AdapterViewState.LOCATION_MIDDLE_COVER:
				//不知道怎么做
				break;
			default:
				break;
		}
	}

	public void setOrientation(int orientation){
		mLayoutManager.setOrientation(orientation);
		mDividerItemDecoration.setOrientation(orientation);
	}

	/**
	 * 如果滚动超过第一个item的宽或高的一半，自动贴合第二个item否则贴合第一个
	 * @param autofit
	 */
	public void enableAutofit(boolean autofit){
		mAutofit=autofit;
	}

	public void setAdapter(RecyclerView.Adapter<? extends ViewHolder> adapter){
		mRecyclerView.setAdapter(adapter);
	}

	public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener l){
		mSwipe.setOnRefreshListener(l);
	}
	
	public SwipeRefreshLayout getSwipe(){
		return mSwipe;
	}

	public RecyclerView getRecyclerView(){
		return mRecyclerView;
	}

	public ItemDecoration getDecoration(){
		return mDividerItemDecoration;
	}
}
