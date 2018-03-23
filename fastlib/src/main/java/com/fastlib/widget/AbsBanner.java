package com.fastlib.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by sgfb on 16/3/23.
 * 通用轮播.指示器需要额外加装
 */
public abstract class AbsBanner<T> extends ViewPager{
    private BannerAdapter mAdapter;
    private List<T> mData;
    private boolean mAutoScroll = false; //初始化设置false才能自动轮播
    private long mScrollInterval = 5000; //轮播间隔时间

    protected abstract HandlePage<T> getHandleImageWithEvent();

    protected abstract int getItemLayoutId();

    public AbsBanner(Context context) {
        super(context);
        init();
    }

    public AbsBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mAdapter = new BannerAdapter();
        mAdapter.mImageWithEvent = getHandleImageWithEvent();
        setAdapter(mAdapter);
    }

    private void startAutoScroll() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAutoScroll) {
                    setCurrentItem((getCurrentItem() + 1) % mAdapter.getCount(), true);
                    startAutoScroll();
                }
            }
        }, mScrollInterval);
    }

    public void setAutoScroll(boolean autoScroll) {
        if (!this.mAutoScroll && autoScroll) {
            this.mAutoScroll = true;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startAutoScroll();
                }
            }, mScrollInterval);
        }
    }

    public void setData(List<T> data) {
        if (data == null || data.size() == 0)
            return;
        mData = data;
        mAdapter.notifyDataSetChanged();
        if (!mAutoScroll) { //如果没有自动轮播,延迟一个轮播间隔启动自动轮播
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAutoScroll = true;
                    startAutoScroll();
                }
            }, mScrollInterval);
        }
    }

    public void setInterval(long interval) {
        mScrollInterval = interval;
    }

    private class BannerAdapter extends PagerAdapter{
        private HandlePage<T> mImageWithEvent;

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            int layoutId = getItemLayoutId();
            View v = layoutId > 0 ? LayoutInflater.from(getContext()).inflate(layoutId, null) : new ImageView(getContext());
            if (mImageWithEvent != null)
                mImageWithEvent.handle(v, mData.get(position));
            container.addView(v);
            return v;
        }
    }

    public interface HandlePage<T>{
        void handle(View v,T element);
    }
}