package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.cgs.utils.ToastUtil;
import com.flyco.tablayout.SlidingTabLayout;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTab2 extends Fragment {
    private static final String TAG = HTab2.class.getSimpleName();

    public static final int SCANNIN_USER_CODE = 97;

    private String mTabStr[] = new String[]{"我的好友", "最近通话", "手机通讯录"};

    private SlidingTabLayout mSlidingTab;
    private ViewPager mPager;//页卡内容
    private ArrayList<Fragment> listViews; //Tab页面列表
    private int currIndex = 0;//当前页卡编号

    private HomeActivity mContext;

    private Resources res;

    private View rootView;

    private Button b1;

    private Button b2;

    private Button b3;

    class TabWrap {
        String mTabName;
        String mFragmentClassName;

        public TabWrap(String mTabName, String mFragmentClassName) {
            super();
            this.mTabName = mTabName;
            this.mFragmentClassName = mFragmentClassName;
        }

    }

    private TabWrap[] mTabClasses = new TabWrap[]{
            new TabWrap("mSecondTab1", SecondTab1.class.getName()),
            new TabWrap("mSecondTab2", SecondTab2.class.getName()),
            new TabWrap("mSecondTab3", SecondTab3.class.getName())
    };

    private void intiFragments() {
        listViews = new ArrayList<Fragment>();
        for (int i = 0; i < mTabClasses.length; i++) {
            TabWrap tabWrap = mTabClasses[i];
            Bundle bundle = new Bundle();
            bundle.putString("tag", tabWrap.mTabName);
            Fragment fragment = Fragment.instantiate(mContext, tabWrap.mFragmentClassName, bundle);
            listViews.add(fragment);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (HomeActivity) getActivity();
        res = getResources();
        intiFragments();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.htab2, container, false);
        }
        InitTextView();
        InitViewPager();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void InitTextView() {
        if (mContext.getHeadLayouManager() != null) {
//            mContext.getHeadLayouManager().updateTitle(SecondTab1.titleTextStr);
        }
        b1 = (Button) rootView.findViewById(R.id.b1);
        b2 = (Button) rootView.findViewById(R.id.b2);
        b3 = (Button) rootView.findViewById(R.id.b3);

        b1.setTextColor(res.getColor(R.color.white));

        b1.setBackgroundResource(R.drawable.left_bg);
        b2.setBackgroundResource(R.drawable.mid);
        b3.setBackgroundResource(R.drawable.right);
        b1.setOnClickListener(new MyOnClickListener(0));
        b2.setOnClickListener(new MyOnClickListener(1));
        b3.setOnClickListener(new MyOnClickListener(2));
    }

    /**
     * 头标点击监听
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }


    private void InitViewPager() {
        mSlidingTab = (SlidingTabLayout) rootView.findViewById(R.id.slidingTab);
        mPager = (ViewPager) rootView.findViewById(R.id.mPager);
        FragmentManager fragmentManager = getChildFragmentManager();
        mPager.setAdapter(new MainFragmentPagerAdapter(fragmentManager, listViews));
        mPager.setCurrentItem(0);
        b1.setTextColor(res.getColor(R.color.white));
        mPager.setOffscreenPageLimit(3);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mSlidingTab.setViewPager(mPager, mTabStr);
    }

    /**
     * ViewPager适配器
     */
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {
        @SuppressWarnings("deprecation")
        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
//                    mContext.getHeadLayouManager().updateTitle(SecondTab1.titleTextStr);
                    resetTextColor();
                    b1.setTextColor(res.getColor(R.color.white));
                    b1.setBackgroundResource(R.drawable.left_bg);
                    break;
                case 1:
//                    mContext.getHeadLayouManager().updateTitle(SecondTab2.titleTextStr);
                    resetTextColor();

                    b2.setTextColor(res.getColor(R.color.white));
                    b2.setBackgroundResource(R.drawable.recent_bg);
                    break;
                case 2:
//                    mContext.getHeadLayouManager().updateTitle(SecondTab3.titleTextStr);
                    resetTextColor();
                    b3.setTextColor(res.getColor(R.color.white));
                    b3.setBackgroundResource(R.drawable.right_bg);
                    break;
            }
            currIndex = arg0;
            if (mContext == null) {
                return;
            }
            mContext.mHeadLayoutManager.setSearchAction(View.VISIBLE, arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public void resetTextColor() {
        b1.setTextColor(Color.BLACK);
        b2.setTextColor(Color.BLACK);
        b3.setTextColor(Color.BLACK);
        b1.setBackgroundResource(R.drawable.left);
        b2.setBackgroundResource(R.drawable.mid);
        b3.setBackgroundResource(R.drawable.right);
    }

    public SecondTab1 getSecondTab1() {
        if (listViews == null) {
            intiFragments();
        }
        return (SecondTab1) listViews.get(0);
    }

    public SecondTab2 getSecondTab2() {
        if (listViews == null) {
            intiFragments();
        }

        return (SecondTab2) listViews.get(1);

    }

    public SecondTab3 getSecondTab3() {
        if (listViews == null) {
            intiFragments();
        }
        return (SecondTab3) listViews.get(2);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HTab2.SCANNIN_USER_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String text = bundle.getString("result");
                    Log.i("tvliao", "text-" + text);
                    try {
                        if (text.contains("abcd=")) {
                            // tv绑定二维码
                            int index = text.indexOf("abcd=");
                            String tvid = text.substring(index + 5, text.length());
                            checkTVInfo(tvid);
                        } else {
                            // 好友二维码
                            long userId = Long.parseLong(text);
                            openUserDetail(userId);
                        }

                    } catch (Exception e) {
                        Toast.makeText(mContext, "查询好友失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void openUserDetail(long uid) {
        Intent it = new Intent(mContext, UserDetailActivity.class);
        it.putExtra(UserDetailActivity.EXTRA_TYPE, UserDetailActivity.TYPE_ADD_FRIEND);
        it.putExtra(UserDetailActivity.EXTRA_USER, uid);
        startActivity(it);
    }

    private void checkTVInfo(String tvId) {
        WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.loding_progress));
        BussinessManger.getInstance(mContext).queryTvByTvId(new IBussinessManager.OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                WaitDialogBuilder.dismissDialog();
                if (isSuccess) {
                    Map<String, Object> data = (Map<String, Object>) obj;
                    int uid = (int) data.get("uid");
                    long userId = Long.parseLong("11" + uid);
                    Log.i("tvliao", "userId-" + userId);
                    openUserDetail(userId);

                } else {
                    ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                }

            }
        }, tvId, BussinessManger.CHANNEL);

    }

}
