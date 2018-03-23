package com.fastlib.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;

import java.util.List;

/**
 * Created by sgfb on 17/3/13.
 * 通用页面适配器,页面不可过多否则会增加内存压力
 */
public class CommonFragmentViewPagerAdapter extends FragmentPagerAdapter{
    private List<Pair<String,Fragment>> mFragments;

    public CommonFragmentViewPagerAdapter(FragmentManager fm, List<Pair<String, Fragment>> fragments){
        super(fm);
        mFragments=fragments;
    }

    @Override
    public Fragment getItem(int position){
        return mFragments.get(position).second;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).first;
    }

    @Override
    public int getCount() {
        return mFragments==null?0:mFragments.size();
    }

    public void addFragment(String title,Fragment fragment){
        mFragments.add(Pair.create(title,fragment));
        notifyDataSetChanged();
    }
}
