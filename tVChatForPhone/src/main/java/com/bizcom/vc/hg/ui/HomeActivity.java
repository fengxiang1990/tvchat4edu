package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.request.V2ImRequest;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.DialogManager;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.ui.edu.CourseConfig;
import com.bizcom.vc.hg.ui.edu.WebCourseNoToolbarActivity;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.CheckUserRoleResponse;
import com.bizcom.vc.hg.util.DownloadService;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vc.hg.web.interf.BaseResponse;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.ErrorResponse;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vc.hg.web.interf.SimpleResponseListener;
import com.bizcom.vc.listener.NotificationListener;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.User;
import com.bizcom.vo.enums.NetworkStateCode;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeActivity extends FragmentActivity implements NotificationListener, UncaughtExceptionHandler {
    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final int HomeRequestCode = 99;
    protected HeadLayoutManagerHG mHeadLayoutManager;
    //private TabHostOnTabChangeListener mOnTabChangeListener = new TabHostOnTabChangeListener();

    private Context mContext;
    private ArrayList<User> data;

    private boolean exitedFlag = false;
    private LocalReceiver receiver = new LocalReceiver();

    private Dialog mDlg1;

    private SimpleDraweeView mUserAvatar;


    TabWrap[] mTabClasses = new TabWrap[]{
            new TabWrap(V2GlobalConstants.TAG_HTAB0, R.string.htab0, R.string.htab0,
                    R.drawable.buttomtab_course_selector, HTab0.class.getName()),
            new TabWrap(V2GlobalConstants.TAG_HTAB1, R.string.htab1, R.string.htab1,
                    R.drawable.buttomtab_message_selector, HTab1.class.getName()),
            new TabWrap(V2GlobalConstants.TAG_HTAB2, R.string.htab2, R.string.htab2, R.drawable.buttomtab_org_selector,
                    HTab2.class.getName()),
            new TabWrap(V2GlobalConstants.TAG_HTAB3, R.string.htab3, R.string.htab3,
                    R.drawable.buttomtab_conference_selector, HTab3.class.getName())};


    List<Fragment> fragments;

    @BindView(R.id.rb0)
    RadioButton rb0;

    @BindView(R.id.rb1)
    RadioButton rb1;

    @BindView(R.id.rb2)
    RadioButton rb2;

    @BindView(R.id.rb3)
    RadioButton rb3;

    @BindView(R.id.content)
    FrameLayout content;

    Unbinder unbinder;

    HTab0 hTab0;
    HTab1 hTab1;
    HTab2 hTab2;
    HTab3 hTab3;

    FragmentManager fragmentManager;
    IBussinessManager manager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        unbinder = ButterKnife.bind(this);
        mContext = this;
        manager = BussinessManger.getInstance(this);
        if (savedInstanceState != null) {
            finish();
            return;
        }

        mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout), true);
        mHeadLayoutManager.updateTitle(mTabClasses[2].mTabTitle);
        mUserAvatar = (SimpleDraweeView) findViewById(R.id.titlePerson);
        initBroadcast();
        MainApplication.isAlreadLogin = true;

        initFragments();
        // 判断是否是因为程序第一次启动将服务禁止后,登录后还没有恢复
//		if (JPushInterface.isPushStopped(mContext)) {
//			JPushInterface.resumePush(mContext);
//		}
        getAppUpdateInfo();

        initListener();

        loadMineRunnable = new LoadMineRunnable();
        handler.post(loadMineRunnable);
    }

    void initFragments() {
        fragments = new ArrayList<>();
        fragmentManager = getSupportFragmentManager();
        hTab0 = (HTab0) fragmentManager.findFragmentByTag("htab0");
        hTab1 = (HTab1) fragmentManager.findFragmentByTag("htab1");
        hTab2 = (HTab2) fragmentManager.findFragmentByTag("htab2");
        hTab3 = (HTab3) fragmentManager.findFragmentByTag("htab3");
        fragments.add(hTab0);
        fragments.add(hTab1);
        fragments.add(hTab2);
        fragments.add(hTab3);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(hTab0);
        transaction.commit();

        transaction = fragmentManager.beginTransaction();
        transaction.hide(hTab1);
        transaction.commit();

        transaction = fragmentManager.beginTransaction();
        transaction.hide(hTab3);
        transaction.commit();

        setHeader(2);
    }

    public void switchFragment(FragmentTransaction fragmentTransaction,
                               Fragment fragment) {
        for (Fragment objFragment : fragments) {
            if (objFragment.isAdded()) {
                fragmentTransaction.hide(objFragment);
            }
        }
        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.content, fragment);
        } else {
            fragmentTransaction.show(fragment);
        }
        fragmentTransaction.commitAllowingStateLoss ();
    }

    private void checkUserRole() {
        Log.e(TAG, "checkUserRole");
        User user = GlobalHolder.getInstance().getCurrentUser();
        Log.e(TAG, "user-->" + user.toString());
        Log.e(TAG, "checkUserRole account-->" + user.getAccount());
        manager.checkUserRole(user.getAccount(), new SimpleResponseListener<BaseResponse<CheckUserRoleResponse>>() {
            @Override
            protected void onSuccess(BaseResponse<CheckUserRoleResponse> t) {
                Log.e(TAG, "checkUserRole onSuccess-->" + t.toString());
                if (t.code.equals("0000")) {
                    CheckUserRoleResponse checkUserRoleResponse = t.data;
                    if (checkUserRoleResponse != null) {
                        if (TextUtils.isEmpty(checkUserRoleResponse.user_type)) {
                            return;
                        }
                        if (checkUserRoleResponse.user_type.equals("TEACHER")) {
                            rb0.setVisibility(View.VISIBLE);
                            rb0.setChecked(true);
                            setHeader(0);

                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.show(hTab0);
                            transaction.commit();

//                            transaction = fragmentManager.beginTransaction();
//                            transaction.hide(hTab1);
//                            transaction.commit();

                            transaction = fragmentManager.beginTransaction();
                            transaction.hide(hTab2);
                            transaction.commit();

//                            transaction = fragmentManager.beginTransaction();
//                            transaction.hide(hTab3);
//                            transaction.commit();
                        } else {
                            rb0.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            protected void onError(ErrorResponse response) {
                Log.e(TAG, "checkUserRole onError-->" + response.toString());
            }
        });
    }

    void initListener() {
        rb0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb0.setChecked(true);
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(false);
                switchFragment(fragmentManager.beginTransaction(), hTab0);
                setHeader(0);

            }
        });
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb0.setChecked(false);
                rb1.setChecked(true);
                rb2.setChecked(false);
                rb3.setChecked(false);
                switchFragment(fragmentManager.beginTransaction(), hTab1);
                setHeader(1);
            }
        });

        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb0.setChecked(false);
                rb1.setChecked(false);
                rb2.setChecked(true);
                rb3.setChecked(false);
                switchFragment(fragmentManager.beginTransaction(), hTab2);
                setHeader(2);
            }
        });

        rb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb0.setChecked(false);
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(true);
                switchFragment(fragmentManager.beginTransaction(), hTab3);
                setHeader(3);
            }
        });
    }

    class TabWrap {
        String mTabName;
        int mTabTitle;
        int mShowText;
        int mDrawableId;
        String mFragmentClassName;
        View mViewNotificator;

        public TabWrap(String tabName, int tabTitle, int showText, int drawableId, String clsName,
                       View viewNotificator) {
            super();
            this.mTabName = tabName;
            this.mTabTitle = tabTitle;
            this.mShowText = showText;
            this.mDrawableId = drawableId;
            this.mFragmentClassName = clsName;
            this.mViewNotificator = viewNotificator;
        }

        public TabWrap(String tabName, int tabTitle, int showText, int drawableId, String clsName) {
            this(tabName, tabTitle, showText, drawableId, clsName, null);
        }

    }


    public void setHeader(int pos) {
        if (pos == 0) {
            mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
            mHeadLayoutManager.setSearchVisibility(View.GONE);
            mHeadLayoutManager.setTVHelpVisibility(View.GONE);
            mHeadLayoutManager.setAddVisibility(View.GONE);
            mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
            TextView text_right_btn1 = (TextView) mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1);
            text_right_btn1.setVisibility(View.VISIBLE);
            text_right_btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, WebCourseNoToolbarActivity.class);
                    intent.putExtra(CourseConfig.WEB_TYPE, CourseConfig.ADD_COURSE);
                    startActivity(intent);
                }
            });

        }
        if (pos == 1) {
            mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1).setVisibility(View.GONE);
            mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
            mHeadLayoutManager.setSearchVisibility(View.INVISIBLE);
            mHeadLayoutManager.setTVHelpVisibility(View.INVISIBLE);
            mHeadLayoutManager.setAddVisibility(View.VISIBLE);
            mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
        }
        if (pos == 2) {
            mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1).setVisibility(View.GONE);
            mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
            mHeadLayoutManager.setSearchVisibility(View.VISIBLE);
            mHeadLayoutManager.setAddVisibility(View.VISIBLE);
            mHeadLayoutManager.setTVHelpVisibility(View.INVISIBLE);
            mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
        }
        if (pos == 3) {
            mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1).setVisibility(View.GONE);
            mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
            mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
            mHeadLayoutManager.setSearchVisibility(View.INVISIBLE);
            mHeadLayoutManager.setAddVisibility(View.INVISIBLE);
            mHeadLayoutManager.setTVHelpVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) mHeadLayoutManager.rootContainer.findViewById(R.id.img_tv_help);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, WebViewActivity.class);
                    intent.putExtra("url", "http://tvl.hongguaninfo.com/tvAssist/index.html");
                    startActivity(intent);
                }
            });
        }
    }

    // 检查更新
    private void getAppUpdateInfo() {

        IBussinessManager manager = BussinessManger.getInstance(mContext);
        manager.versionUpInfo(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {

                if (isSuccess) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    int appVer = Integer.parseInt(String.valueOf(map.get("appVer")));
                    int minVer = Integer.parseInt(String.valueOf(map.get("minVer")));
                    String appUrl = String.valueOf(map.get("appUrl"));
                    String verName = String.valueOf(map.get("dispaly_version"));

                    checkVersion(minVer, appVer, appUrl, verName);
                }
            }
        });

    }


    @Override
    protected void onResume() {
//		JPushInterface.onResume(this);
        getTvlChatPwdFromClipBroad();
        super.onResume();
    }


    void getTvlChatPwdFromClipBroad() {
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (myClipboard != null) {
            ClipData clip = myClipboard.getPrimaryClip();
            if (clip != null) {
                if (clip.getItemCount() > 0) {
                    ClipData.Item item = clip.getItemAt(0);
                    String text = item.getText().toString();
                    String tvl_chat_hint = getResources().getString(R.string.tvl_chat_pwd_hint);
                    if (!TextUtils.isEmpty(text) && text.contains(tvl_chat_hint)) {
                        String pwd = text.replace(tvl_chat_hint, "");
                        Log.e(TAG, "pwd-->" + pwd);
                        decodeChatPwd(pwd);
                        myClipboard.setText("###");
                    }
                }

            }
        }


    }

    Handler handler = new Handler();
    MRunnable mRunnable;
    LoadMineRunnable loadMineRunnable;

    class LoadMineRunnable implements Runnable {

        @Override
        public void run() {
            User user = GlobalHolder.getInstance().getCurrentUser();
            Log.d(TAG, "load current user...");
            Log.d(TAG, "current user-->" + user);
            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, user.getmUserId());
            if (TextUtils.isEmpty(user.getAccount())) {
                handler.postDelayed(this, 200);
            } else {
                checkUserRole();
            }
        }
    }

    class MRunnable implements Runnable {

        User user;

        public MRunnable(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            Log.d(TAG, "load user...");
            Log.d(TAG, "user-->" + user);
            if (TextUtils.isEmpty(user.getAccount())) {
                handler.postDelayed(this, 1000);
            } else {
                showAddByChatPwdDialog(user);
            }
        }
    }


    void showAddByChatPwdDialog(final User user) {
        final boolean isfriend = GlobalHolder.getInstance().isFriend(user);
        AlertMsgUtils.showAddFrendByChatPwdConfirm(HomeActivity.this, user, new AlertMsgUtils.OnDialogBtnClickListener() {
            @Override
            public void onConfirm(final Dialog dialog) {
                if (isfriend) {
                    Intent intent = new Intent(HomeActivity.this, UserDetailActivity.class);
                    intent.putExtra(UserDetailActivity.EXTRA_USER, user.getmUserId());
                    startActivity(intent);
                    dialog.dismiss();
                } else {
                    FriendUtil.addConstant(HomeActivity.this, user.getmUserId(), new FriendUtil.AddFriendCallback() {
                        @Override
                        public void onResult(boolean isSuccess) {
                            if (isSuccess) {
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    void decodeChatPwd(String pwd) {
        BussinessManger.getInstance(this).generateChatPassword("1", "", pwd, new OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                Log.d(TAG, "decodeChatPwd-->" + what + " " + String.valueOf(obj));
                if (isSuccess) {
                    String uid = String.valueOf(obj);
                    uid = "11" + uid;
                    //不是自己
                    if (!uid.equals(String.valueOf(GlobalHolder.getInstance().getCurrentUserId()))) {
                        final long luid = Long.parseLong(uid);
                        V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, luid);
                        final User user = GlobalHolder.getInstance().getUser(luid);
                        mRunnable = new MRunnable(user);
                        handler.postDelayed(mRunnable, 1000);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, String.valueOf(obj), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Fragment mCurrentFragment = fragments[mViewPager.getCurrentItem()];
//        if (keyCode == event.KEYCODE_BACK && mCurrentFragment instanceof HTab1) {
//            return getHTab1().onKeyDown(keyCode, event);
//        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean KeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    protected void checkVersion(int minVer, int appVer, String appUrl, String verName) {
        if (getLocalVersion() >= minVer && getLocalVersion() < appVer) {// 可更新
            showUpdate1(appUrl, verName);
        } else if (getLocalVersion() < minVer) {// 强制更新
            showUpdate2(appUrl, verName);
        }
    }

    private void showUpdate1(final String appUrl, String verName) {

        mDlg1 = DialogManager.getInstance().showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mContext,
                null, "亲爱的用户，TV聊新版客户端已发布，全面优化使用体验，建议您立即更新升级。", "立即更新", "忽略更新") {

            @Override
            public void confirmCallBack() {
                mDlg1.dismiss();

                Intent mIntent = new Intent(mContext, DownloadService.class);
                mIntent.putExtra("url", appUrl);
                mContext.startService(mIntent);

                WaitDialogBuilder.showNormalWithHintProgress(HomeActivity.this, "正在更新,请稍候...", false);

            }

            @Override
            public void cannelCallBack() {
                mDlg1.dismiss();
            }
        });

        mDlg1.show();

    }

    private void showUpdate2(final String appUrl, String verName) {

        mDlg1 = DialogManager.getInstance().showNoTitleDialog2(
                DialogManager.getInstance().new DialogInterface(mContext, null, "当前版本已失效，请更新至新版本！", "立即更新", "立即更新") {

                    @Override
                    public void confirmCallBack() {
                        mDlg1.dismiss();

                        Intent mIntent = new Intent(mContext, DownloadService.class);
                        mIntent.putExtra("url", appUrl);
                        mContext.startService(mIntent);
                    }

                    @Override
                    public void cannelCallBack() {
                        mDlg1.dismiss();

                        Intent mIntent = new Intent(mContext, DownloadService.class);
                        mIntent.putExtra("url", appUrl);
                        mContext.startService(mIntent);

                        WaitDialogBuilder.showNormalWithHintProgress(HomeActivity.this, "正在更新,请稍候...", false);
                    }
                });

        mDlg1.show();

    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public int getLocalVersion() {
        int version = -1;
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    @Override
    protected void onPause() {
//		JPushInterface.onPause(this);
        super.onPause();
    }

    // @Override
    // protected void onNewIntent(Intent intent) {
    // super.onNewIntent(intent);
    // int index = intent.getIntExtra("initFragment", 0);
    // // mViewPager.setCurrentItem(index);
    // mViewPager.setCurrentItem(index, true);
    // if (intent.getExtras() != null) {
    // conf = (Conference) intent.getExtras().get("conf");
    // }
    // }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(receiver);

            if (exitedFlag) {
//                MainApplication.getDbUtils().deleteAll(User.class);
                ((MainApplication) getApplication()).uninitForExitProcess();

            }
        } catch (Exception e) {
        }
    }


//    /**
//     * 初始化ViewPager内容，并指定初始的显示位置
//     *
//     * @param index
//     */
//    private void intialiseViewPager(int index) {
//        fragments = new Fragment[mTabClasses.length];
//        for (int i = 0; i < mTabClasses.length; i++) {
//            TabWrap tabWrap = mTabClasses[i];
//            Bundle bundle = new Bundle();
//            bundle.putString("tag", tabWrap.mTabName);
//            Fragment fragment = Fragment.instantiate(this, tabWrap.mFragmentClassName, bundle);
//            fragments[i] = fragment;
//        }
//
//        mViewPager = (com.bizcom.vc.hg.view.MyViewPager) super.findViewById(R.id.viewpager);
//        mViewPager.setNoScroll(true);// 设置 滑动切换
//
//        MyFragmentPagerAdapter mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
//        mViewPager.setAdapter(mPagerAdapter);
//
//        mViewPager.setOnPageChangeListener(listenerOfPageChange);
//        // 保留子视图的个数，防止频繁创建和销毁
//        mViewPager.setOffscreenPageLimit(4);
//        mViewPager.setCurrentItem(index);
//    }

//    /**
//     * 获取底部导航页的View对象
//     *
//     * @param tabWrap
//     * @param i
//     * @return
//     */
//    private View getTabView(TabWrap tabWrap, int i) {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View v = null;
//        // if(i==1){
//        // v =inflater.inflate(R.layout.tab_widget_view_max, null, false);
//        // }else{
//        v = inflater.inflate(R.layout.tab_widget_view, null, false);
//        // }
//
//        ImageView iv = (ImageView) v.findViewById(R.id.tab_image);
//        if (iv != null) {
//            iv.setImageDrawable(this.getResources().getDrawable(tabWrap.mDrawableId));
//            iv.bringToFront();
//        }
//
//        TextView tv = (TextView) v.findViewById(R.id.tab_name);
//        if (tv != null) {
//            tv.setText(this.getResources().getText(tabWrap.mShowText));
//            tv.bringToFront();
//        }
//
//        tabWrap.mViewNotificator = v.findViewById(R.id.tab_notificator);
//        tabWrap.mViewNotificator.setVisibility(View.INVISIBLE);
//        return v;
//    }

    public void updateNotificator(final int tabType, final boolean whetherDisplay) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                View noticator;
                if (tabType == Conversation.TYPE_CONTACT) {
                    //  noticator = mTabClasses[0].mViewNotificator;
                } else {
                    V2Log.e(TAG, "Error TabFragment Type Value : " + tabType);
                    return;
                }

                if (whetherDisplay) {
                    // noticator.setVisibility(View.VISIBLE);
                } else {
                    // noticator.setVisibility(View.GONE);
                }
            }
        });
    }
//
//    private TextWatcher listenerOfSearchTextWatcher = new TextWatcher() {
//
//        @Override
//        public void afterTextChanged(Editable edit) {
//
//            ((TextWatcher) ((FragmentPagerAdapter) mViewPager.getAdapter()).getItem(mTabHost.getCurrentTab()))
//                    .afterTextChanged(edit);
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//            ((TextWatcher) ((FragmentPagerAdapter) mViewPager.getAdapter()).getItem(mTabHost.getCurrentTab()))
//                    .beforeTextChanged(arg0, arg1, arg2, arg3);
//        }
//
//        @Override
//        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//            ((TextWatcher) ((FragmentPagerAdapter) mViewPager.getAdapter()).getItem(mTabHost.getCurrentTab()))
//                    .onTextChanged(arg0, arg1, arg2, arg3);
//        }
//
//    };

//    private class TabHostOnTabChangeListener implements TabHost.OnTabChangeListener {
//        public void onTabChanged(String tag) {
//            int pos = mTabHost.getCurrentTab();
//            if (mViewPager == null) {
//                V2Log.e(" MainActivity state is illegal");
//                return;
//            }
//            // 恢复搜索状态
//            TabWrap tab = mTabClasses[pos];
//            Fragment fragment = fragments[pos];
//
//            mViewPager.setCurrentItem(pos);
//            mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
//
//        }
//
//    }

//    private ViewPager.OnPageChangeListener listenerOfPageChange = new ViewPager.OnPageChangeListener() {
//
//        @Override
//        public void onPageScrollStateChanged(int arg0) {
//        }
//
//        @Override
//        public void onPageScrolled(int arg0, float arg1, int arg2) {
//        }
//
//        @Override
//        public void onPageSelected(int pos) {
//            if (pos == 0) {
//                mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
//                mHeadLayoutManager.setSearchVisibility(View.GONE);
//                mHeadLayoutManager.setTVHelpVisibility(View.GONE);
//                mHeadLayoutManager.setAddVisibility(View.GONE);
//                mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
//                TextView text_right_btn1 = (TextView) mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1);
//                text_right_btn1.setVisibility(View.VISIBLE);
//                text_right_btn1.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(HomeActivity.this, WebCourseNoToolbarActivity.class);
//                        intent.putExtra(CourseConfig.WEB_TYPE, CourseConfig.ADD_COURSE);
//                        startActivity(intent);
//                    }
//                });
//
//            }
//            if (pos == 1) {
//                mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1).setVisibility(View.GONE);
//                mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
//                mHeadLayoutManager.setSearchVisibility(View.INVISIBLE);
//                mHeadLayoutManager.setTVHelpVisibility(View.INVISIBLE);
//                mHeadLayoutManager.setAddVisibility(View.VISIBLE);
//                mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
//            }
//            if (pos == 2) {
//                mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1).setVisibility(View.GONE);
//                mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
//                mHeadLayoutManager.setSearchVisibility(View.VISIBLE);
//                mHeadLayoutManager.setAddVisibility(View.VISIBLE);
//                mHeadLayoutManager.setTVHelpVisibility(View.INVISIBLE);
//            }
//            if (pos == 3) {
//                mHeadLayoutManager.rootContainer.findViewById(R.id.text_right_btn1).setVisibility(View.GONE);
//                mHeadLayoutManager.rootContainer.setVisibility(View.VISIBLE);
//                mHeadLayoutManager.updateTitle("TV协助");
//                mHeadLayoutManager.setSearchVisibility(View.INVISIBLE);
//                mHeadLayoutManager.setAddVisibility(View.INVISIBLE);
//                mHeadLayoutManager.setTVHelpVisibility(View.VISIBLE);
//                ImageView imageView = (ImageView) mHeadLayoutManager.rootContainer.findViewById(R.id.img_tv_help);
//                imageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(HomeActivity.this, WebViewActivity.class);
//                        intent.putExtra("url", "http://tvl.hongguaninfo.com/tvAssist/index.html");
//                        startActivity(intent);
//                    }
//                });
//            }
//
//        }
//
//    };
//
//    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
//
//        public MyFragmentPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return fragments[position];
//        }
//
//        @Override
//        public int getCount() {
//            return fragments.length;
//        }
//    }
//
//    class TabWrap {
//        String mTabName;
//        int mTabTitle;
//        int mShowText;
//        int mDrawableId;
//        String mFragmentClassName;
//        View mViewNotificator;
//
//        public TabWrap(String tabName, int tabTitle, int showText, int drawableId, String clsName,
//                       View viewNotificator) {
//            super();
//            this.mTabName = tabName;
//            this.mTabTitle = tabTitle;
//            this.mShowText = showText;
//            this.mDrawableId = drawableId;
//            this.mFragmentClassName = clsName;
//            this.mViewNotificator = viewNotificator;
//        }
//
//        public TabWrap(String tabName, int tabTitle, int showText, int drawableId, String clsName) {
//            this(tabName, tabTitle, showText, drawableId, clsName, null);
//        }
//
//    }
//
//    class TabFactory implements TabContentFactory {
//
//        private final Context mContext;
//
//        /**
//         * @param context
//         */
//        public TabFactory(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        public View createTabContent(String tag) {
//            View v = new View(mContext);
//            v.setMinimumWidth(0);
//            v.setMinimumHeight(0);
//            return v;
//        }
//    }

    public HTab1 getHTab0() {
        return (HTab1) fragments.get(0);

    }

    public HTab1 getHTab1() {
        return (HTab1) fragments.get(1);

    }

    public HTab2 getHTab2() {
        return (HTab2) fragments.get(2);

    }

    public HTab3 getHTab3() {
        return (HTab3) fragments.get(3);
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
        filter.addAction(PublicIntent.FINISH_APPLICATION);
        filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
        mContext.registerReceiver(receiver, filter);
    }

    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PublicIntent.FINISH_APPLICATION.equals(action)) {
                exitedFlag = true;
                boolean isDelay = intent.getBooleanExtra("delay", false);
                if (isDelay) {
                    Toast.makeText(mContext, mContext.getText(R.string.user_logged_with_deleted), Toast.LENGTH_LONG)
                            .show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            requestQuit();
                        }
                    }, 3000);
                } else {
                    requestQuit();
                }
            } else if (JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION.equals(action)) {
                V2Log.d("CONNECT", "MainActivity Receiver Broadcast ! Globle Connection State is : "
                        + GlobalHolder.getInstance().isServerConnected());
                NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
                if (code != null) {
                    V2Log.d("CONNECT",
                            "MainActivity Receiver Broadcast ! receiver Connection State is : " + code.name());
                }

                V2Log.d("CONNECT", "--------------------------------------------------------------------");
                if (mHeadLayoutManager != null) {
                    mHeadLayoutManager.updateConnectState(code);
                } else {
                    V2Log.d("CONNECT", "TitleBar is null !");
                }
                V2Log.d("CONNECT", "--------------------------------------------------------------------");
            }
        }
    }

    private void requestQuit() {
        if (exitedFlag) {
            ((MainApplication) this.getApplicationContext()).requestQuit();
        } else {
            exitedFlag = true;
            Toast.makeText(this, R.string.quit_promption, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    exitedFlag = false;
                }

            }, 2500);
        }
    }

    //更新个人信息
    public void displayIcon() {

        SimpleDraweeViewUtils.display(mUserAvatar, GlobalHolder.getInstance().getCurrentUser().getmAvatarLocation());
//            UserHeaderImgHelper.display(mUserAvatar, GlobalHolder.getInstance().getCurrentUser());

    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ToastUtil.ShowToast_long(mContext, "系统错误");
        sendErrorMessage("" + ex.getLocalizedMessage() + " " + ex.getMessage());
        finish();
    }


    /**
     * 上传错误
     *
     * @param error_message
     */
    private void sendErrorMessage(String error_message) {

        BussinessManger.getInstance(this).clientErrorLog(
                GlobalHolder.getInstance().getCurrentUser().getAccount() + "",
                error_message,
                String.valueOf(System.currentTimeMillis()));
    }


    public HeadLayoutManagerHG getHeadLayouManager() {
        return mHeadLayoutManager;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HTab2.SCANNIN_USER_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    getHTab2().onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!MainApplication.IsInitDataLoadingFinish) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }
}
