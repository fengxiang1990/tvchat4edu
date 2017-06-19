package com.bizcom.vc.hg.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bizcom.util.MessageUtil;
import com.flyco.tablayout.SlidingTabLayout;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by HG-MALONG on 2016/12/19.
 */

public class SearchFriendActivity extends FragmentActivity {

    public static final String EXTRA_CURRENT_INDEX = "extra_currentIndex";
    private int mCurrentIndex = 0;

    @BindView(R.id.et)
    EditText mInputSearch;
    @BindView(R.id.slidingTab)
    SlidingTabLayout slidingTab;
    @BindView(R.id.mPager)
    ViewPager mPager;

    private String mTabStr[] = new String[]{"我的好友", "最近通话", "手机通讯录"};

    private ArrayList<Fragment> listViews;

    private SearchFriendActivity.TabWrap[] mTabClasses = new SearchFriendActivity.TabWrap[]{
            new SearchFriendActivity.TabWrap("SearchFriendTab1", SearchFriendTab1.class.getName()),
            new SearchFriendActivity.TabWrap("SearchFriendTab2", SearchFriendTab2.class.getName()),
            new SearchFriendActivity.TabWrap("SearchFriendTab3", SearchFriendTab3.class.getName())
    };

    class TabWrap {
        String mTabName;
        String mFragmentClassName;

        public TabWrap(String mTabName, String mFragmentClassName) {
            super();
            this.mTabName = mTabName;
            this.mFragmentClassName = mFragmentClassName;
        }
    }

    public SearchFriendTab1 getTab1() {
        return (SearchFriendTab1) listViews.get(0);
    }

    public SearchFriendTab2 getTab2() {
        return (SearchFriendTab2) listViews.get(1);
    }

    public SearchFriendTab3 getTab3() {
        return (SearchFriendTab3) listViews.get(2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_frends_totv);
        mCurrentIndex = getIntent().getIntExtra(EXTRA_CURRENT_INDEX, 0);
        ButterKnife.bind(this);

        intiFragments();
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MessageUtil.showKeyBoard(mInputSearch);
    }

    private void intiFragments() {
        listViews = new ArrayList<Fragment>();
        for (int i = 0; i < mTabClasses.length; i++) {
            SearchFriendActivity.TabWrap tabWrap = mTabClasses[i];
            Bundle bundle = new Bundle();
            bundle.putString("tag", tabWrap.mTabName);
            Fragment fragment = Fragment.instantiate(SearchFriendActivity.this, tabWrap.mFragmentClassName, bundle);
            listViews.add(fragment);
        }
    }

    private void initViewPager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPager.setAdapter(new MainFragmentPagerAdapter(fragmentManager, listViews));
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(3);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        slidingTab.setViewPager(mPager, mTabStr);
        mPager.setCurrentItem(mCurrentIndex);
//        slidingTab.setCurrentTab(mCurrentIndex);

        mInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int index = mPager.getCurrentItem();
                onSearch(index);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInputSearch.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(
                                    SearchFriendActivity.this
                                            .getCurrentFocus()
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);

                    int index = mPager.getCurrentItem();
                    onSearch(index);
                }
                return false;
            }
        });
    }

    public String getSearchText() {
        return mInputSearch.getText().toString().trim();
    }

    public void onSearch(int index) {
        switch (index) {
            case 0:
                getTab1().onSearch(getSearchText());
                break;
            case 1:
                getTab2().onSearch(getSearchText());
                break;
            case 2:
                getTab3().onSearch(getSearchText());
                break;
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @SuppressWarnings("deprecation")
        @Override
        public void onPageSelected(int arg0) {
            onSearch(arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @OnClick({R.id.img_clear, R.id.text_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_clear:
                mInputSearch.setText("");
                break;
            case R.id.text_cancel:
                MessageUtil.hideKeyBoard(SearchFriendActivity.this, mInputSearch.getWindowToken());
                finish();
                break;
        }
    }
}
