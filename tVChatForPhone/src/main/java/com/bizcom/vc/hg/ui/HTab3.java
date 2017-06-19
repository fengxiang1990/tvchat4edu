package com.bizcom.vc.hg.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MainApplication;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.util.V2Log;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.WebConfig;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HTab3 extends Fragment {
    private static final String tag = HTab3.class.getSimpleName();
    public static String EXTRA_SEND_TO_TV = "extra_tv_to_tv";

    private LocalReceiver receiver = new LocalReceiver();
    private IntentFilter intentFilter;

    private FragmentActivity mContext;
    TabWrap[] mTabClasses = new TabWrap[]{new TabWrap("mtab3_unbind", ThirdTabUnBind.class.getName()),
            new TabWrap("mtab3_binded", ThirdTabBinded.class.getName())};

    private Fragment[] fragments;

    Unbinder unbinder;

    class TabWrap {
        String mTabName;
        String mFragmentClassName;

        public TabWrap(String mTabName, String mFragmentClassName) {
            super();
            this.mTabName = mTabName;
            this.mFragmentClassName = mFragmentClassName;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.htab3, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        runnable = new MRunnable(GlobalHolder.getInstance().getCurrentUser());
        handler.post(runnable);
        return rootView;
    }

    private void intiFragments() {
        fragments = new Fragment[mTabClasses.length];
        for (int i = 0; i < mTabClasses.length; i++) {
            TabWrap tabWrap = mTabClasses[i];
            Bundle bundle = new Bundle();
            bundle.putString("tag", tabWrap.mTabName);
            Fragment fragment = Fragment.instantiate(mContext, tabWrap.mFragmentClassName, bundle);
            fragments[i] = fragment;
        }
    }

    public void translateFragment(Fragment mf) {
        try {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.container, mf);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ;

    private FragmentManager mFragmentManager;
    IBussinessManager service;

    class UnBindTVReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handler.post(runnable);
        }
    }


    UnBindTVReceiver unbindTVReceiver;
    MRunnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        V2Log.i(tag, "TabFragmentConference onCreate()");
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        service = BussinessManger.getInstance(mContext);
        mFragmentManager = getChildFragmentManager();
        intiFragments();
        initReceiver();
    }

    Handler handler = new Handler();

    private void initReceiver() {
        unbindTVReceiver = new UnBindTVReceiver();
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction(SecondTab1.NEW_MSG);
            intentFilter.addAction(EXTRA_SEND_TO_TV);
        }
        getActivity().registerReceiver(receiver, intentFilter);
        getActivity().registerReceiver(unbindTVReceiver, new IntentFilter(UpdateTvInfoActivity.UNBIND_SUCCESS_RECEIVER));
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
        if (unbindTVReceiver != null) {
            getActivity().unregisterReceiver(unbindTVReceiver);
        }
    }

    class MRunnable implements Runnable {
        User user;

        public MRunnable(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(user.getAccount())) {
                handler.postDelayed(this, 1000);
            } else {
                doooo(user);
            }

        }
    }

    public void doooo() {
        handler.post(runnable);
    }

    private void doooo(User user) {
        Log.e(tag, "doooo...");
        String uid = user.getTvlUid();
        service.queryTvByUid(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                Bundle bundle = new Bundle();
                if (isSuccess) {
                    TvInfoBeans mTvInfoBeans = JSON.parseObject(String.valueOf(obj), TvInfoBeans.class);
                    MainApplication.mTvInfoBean = mTvInfoBeans;
                    TabWrap tabWrap = mTabClasses[1];
                    bundle.putString("tag", tabWrap.mTabName);
                    bundle.putSerializable("beans", mTvInfoBeans);
                    Fragment fragment = Fragment.instantiate(mContext, tabWrap.mFragmentClassName, bundle);
                    fragments[1] = fragment;
                    translateFragment(fragments[1]);
                } else {
                    TabWrap tabWrap = mTabClasses[0];
                    bundle.putString("tag", tabWrap.mTabName);
                    Fragment fragment = Fragment.instantiate(mContext, tabWrap.mFragmentClassName, bundle);
                    fragments[0] = fragment;
                    translateFragment(fragments[0]);
                }

            }
        }, uid, WebConfig.channel);

    }

    public ThirdTabBinded getThirdTabBinded() {
        if (fragments == null) {
            intiFragments();
        }
        return (ThirdTabBinded) fragments[1];


    }

    public ThirdTabUnBind getThirdTabUnBind() {
        if (fragments == null) {
            intiFragments();
        }
        return (ThirdTabUnBind) fragments[0];
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SecondTab1.NEW_MSG)) {
                MessageObject msgObj = intent.getParcelableExtra("msgObj");
                long remoteID = msgObj.rempteUserID;
                long msgID = msgObj.messageColsID;
                VMessage m = ChatMessageProvider.loadUserMessageById(remoteID, msgID);
                String getInfo = m.getPlainText();
                JSONObject json = JSON.parseObject(getInfo);
                int type = json.getIntValue("type");

                if (type == ConstantParams.MESSAGE_TYPE_UNBIND_TV) {
                    handler.post(runnable);
                }
            } else if (intent.getAction().equals(EXTRA_SEND_TO_TV)) {
                handler.post(runnable);
            }
        }
    }
}
