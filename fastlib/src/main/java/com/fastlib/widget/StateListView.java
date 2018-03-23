package com.fastlib.widget;

import com.fastlib.bean.StateLocationView;
import com.fastlib.base.AdapterViewState;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 具有状态的列表,状态显示在底部
 * 
 * @author sgfb,shenhaofeng
 *
 */
public class StateListView extends ListView implements AdapterViewState{
	private int mCurrState;
	private Map<Integer,StateLocationView> mViews;
	
	public StateListView(Context context){
		this(context,null);
	}

	public StateListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCurrState=-1;
		mViews=new HashMap<>();
	}

	@Override
	public void onStateChanged(int flag) {
		if(mCurrState==AdapterViewState.STATE_NO_MORE)
			return;
		mCurrState=flag;
               if(flag==AdapterViewState.STATE_NO_MORE) {
			removeFootView();
			removeHeadView();
		}
		StateLocationView lv=mViews.get(flag);
		if(lv==null||lv.view==null)
			return;
		showStateView(mViews.get(flag));
	}

	@Override
	public void addStateView(int state, View view, int location) {
		StateLocationView lv=new StateLocationView();
		lv.location=location;
		lv.view=view;
		mViews.put(state,lv);
	}

	private void showStateView(StateLocationView locationView){
		View view=locationView.view;
		switch(locationView.location){
			case AdapterViewState.LOCATION_FOOT:
				removeFootView();
				addFooterView(view,null,false);
				break;
			case AdapterViewState.LOCATION_HEAD:
				removeHeadView();
				addHeaderView(view,null,false);
				break;
			case AdapterViewState.LOCATION_MIDDLE_CLEAR:
				break;
			case AdapterViewState.LOCATION_MIDDLE_COVER:
				break;
			default:
				break;
		}
	}

	private void removeHeadView(){
		Iterator<Integer> iter=mViews.keySet().iterator();

		while(iter.hasNext()){
			int state=iter.next();
			StateLocationView lv=mViews.get(state);
			if(lv.location==AdapterViewState.LOCATION_HEAD)
				removeFooterView(lv.view);
		}
	}
	
	private void removeFootView(){
		Iterator<Integer> iter=mViews.keySet().iterator();

		while(iter.hasNext()){
			int state=iter.next();
			StateLocationView lv=mViews.get(state);
			if(lv.location==AdapterViewState.LOCATION_FOOT)
			    removeFooterView(lv.view);
		}
	}
}
