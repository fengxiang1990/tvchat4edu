package com.bizcom.vc.hg.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bizcom.vc.hg.ui.edu.CourseFragment;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HTab0 extends Fragment {

    String tag = "HTab0";

    String mTabStr[] = new String[]{"正在上课", "未开始", "已结束"};

    Unbinder unbinder;

    @BindView(R.id.slidingTab)
    SlidingTabLayout mSlidingTab;

    @BindView(R.id.mPager)
    ViewPager mPager;


    CourseFragment tab1;
    CourseFragment tab2;
    CourseFragment tab3;

    public static String ON_PAGE_REFRESH_ACTION = "com.edu.page.refresh";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edu, container, false);
        unbinder = ButterKnife.bind(this, view);
        tab1 = new CourseFragment();
        tab2 = new CourseFragment();
        tab3 = new CourseFragment();
        tab1.tag = "tab1";
        tab2.tag = "tab2";
        tab3.tag = "tab3";
        initViewPager();
        IntentFilter intentFilter = new IntentFilter(ON_PAGE_REFRESH_ACTION);
        getActivity().registerReceiver(onPageRereshReciver, intentFilter);
        return view;
    }

    OnPageRereshReciver onPageRereshReciver = new OnPageRereshReciver();

    class OnPageRereshReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(tag, "OnPageRereshReciver onReceive");
            tab1.reload();
            tab2.reload();
            tab3.reload();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(onPageRereshReciver);
    }

    private void initViewPager() {
        FragmentManager fragmentManager = getChildFragmentManager();
        mPager.setAdapter(new MyPagerAdapter(fragmentManager));
        mSlidingTab.setViewPager(mPager, mTabStr);
        mPager.setOffscreenPageLimit(3);
        mSlidingTab.setCurrentTab(0);
        mSlidingTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                Log.e(tag, "onTabSelect-->" + position);
                switch (position) {
                    case 0:
                        tab1.reload();
                        break;
                    case 1:
                        tab2.reload();
                        break;
                    case 2:
                        tab3.reload();
                        break;
                }
            }

            @Override
            public void onTabReselect(int position) {
                Log.e(tag, "onTabReselect-->" + position);
                switch (position) {
                    case 0:
                        tab1.reload();
                        break;
                    case 1:
                        tab2.reload();
                        break;
                    case 2:
                        tab3.reload();
                        break;
                }
            }
        });


        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e(tag, "onPageSelected-->" + position);
                switch (position) {
                    case 0:
                        if (!isTab1Load) {
                            tab1.loadWeb();
                            isTab1Load = true;
                        }
                        break;
                    case 1:
                        if (!isTab2Load) {
                            tab2.loadWeb();
                            isTab2Load = true;
                        }
                        break;
                    case 2:
                        if (!isTab3Load) {
                            tab3.loadWeb();
                            isTab3Load = true;
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    boolean isTab1Load = false;
    boolean isTab2Load = false;
    boolean isTab3Load = false;

    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return tab1;
                case 1:
                    return tab2;
                case 2:
                    return tab3;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
