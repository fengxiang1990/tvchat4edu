package com.bizcom.vc.hg.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bizcom.vc.hg.adapter.TVbindedAdapter;
import com.bizcom.vc.hg.beans.HasBindedItem;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.web.WebConfig;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ThirdTabBinded extends Fragment implements updateDataCallback {

    String tag = "ThirdTabBinded";

    private TVbindedAdapter mAdapter;
    private List<HasBindedItem> mBindedListData;
    PullToRefreshListView bindListView;
    private TvInfoBeans mbeans;
    private Context mContext;
    private View emptyView;
    private String userId;

    Unbinder unbinder;

    @BindView(R.id.tvTVid)
    TextView textTvId;

    @BindView(R.id.text_tv_id)
    TextView text_tv_id;

    @BindView(R.id.img_tv_header)
    SimpleDraweeView img_tv_header;

    @BindView(R.id.text_sync)
    TextView text_sync;

    String uid;

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getActivity().getSharedPreferences("tvl", Context.MODE_PRIVATE);
        String sync_time = preferences.getString("sync_time", "");
        if (!TextUtils.isEmpty(sync_time)) {
            int sync_num = preferences.getInt("sync_num", 0);
            StringBuilder sb = new StringBuilder("上次同步时间");
            sb.append(sync_time);
            //sb.append(" ");
            //sb.append("已同步").append(sync_num)
            //       .append("位好友");
            text_sync.setText(sb.toString());
        }
    }

    OnTvInfoUpdateReceiver onTvInfoUpdateReceiver = new OnTvInfoUpdateReceiver();

    class OnTvInfoUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(tag, "OnTvInfoUpdateReceiver onReceive");
            String action = intent.getAction();
            switch (action) {
                case UpdateTvInfoActivity.UPDATE_SUCCESS_RECEIVER:
                    doooo();
                    break;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(onTvInfoUpdateReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(tag, "onCreateView");
        View v = inflater.inflate(R.layout.hg_third_tab_binded, container, false);
        unbinder = ButterKnife.bind(this, v);
        mbeans = (TvInfoBeans) getArguments().getSerializable("beans");
        IntentFilter intentFilter = new IntentFilter(UpdateTvInfoActivity.UPDATE_SUCCESS_RECEIVER);
        getActivity().registerReceiver(onTvInfoUpdateReceiver, intentFilter);
        mContext = getActivity();
        emptyView = inflater.inflate(R.layout.empty_view, null);
        text_tv_id.setVisibility(View.GONE);
        initView(v);
        initData();
        return v;
    }

    Handler handler = new Handler();

    public void doooo() {
        Log.d(tag, "doooo");
        BussinessManger.getInstance(getActivity()).queryTvByUid(new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    mbeans = JSON.parseObject(String.valueOf(obj), TvInfoBeans.class);
                    if (!TextUtils.isEmpty(mbeans.getPicurl())) {
                        img_tv_header.setImageURI(Uri.parse(mbeans.getPicurl()));
                        text_tv_id.setVisibility(View.GONE);
                    } else {
                        text_tv_id.setVisibility(View.VISIBLE);
                    }
                }

            }
        }, uid, WebConfig.channel);

    }

    @OnClick(R.id.btn_sync)
    void sync() {
        Intent intent = new Intent(getActivity(), SyncFrendsToTvActivity.class);
        intent.putExtra("beans", mbeans);
        startActivity(intent);
    }

    @OnClick(R.id.tv_detail)
    void bindTvDetail() {
        startActivity(new Intent(getActivity(), UpdateTvInfoActivity.class));
    }

    private void initData() {
        uid = GlobalHolder.getInstance().getCurrentUser().getmUserId() + "";
        if (uid.length() < 2) {
            return;
        }
        uid = uid.substring(2, uid.length());
        doooo();
    }

    private void initView(View v) {
        bindListView = (com.handmark.pulltorefresh.library.PullToRefreshListView) v.findViewById(R.id.mList);
        mBindedListData = new ArrayList<>();
        mAdapter = new TVbindedAdapter(mBindedListData, mContext, mbeans);
        bindListView.setEmptyView(emptyView);
        bindListView.setAdapter(mAdapter);
        userId = GlobalHolder.getInstance().getCurrentUserId() + "";
        if (userId.length() < 2) return;
        String tvAccount = mbeans.getUserName();
        if (tvAccount != null && tvAccount.length() >= 2) {
            text_tv_id.setText(tvAccount.substring(0, 2));
        }
        textTvId.setText(tvAccount);
        SharedPreferences preferences = getActivity().getSharedPreferences("tvl", Context.MODE_PRIVATE);
        String syncDate = preferences.getString("sync_date", null);
        int syncFrendsCount = preferences.getInt("sync_frends_count", 0);
        if (TextUtils.isEmpty(syncDate)) {
            text_sync.setText("");
        } else {
            StringBuilder sb = new StringBuilder("上次同步时间");
            sb.append(syncDate);
            //.append(" 已同步")
            //.append(syncFrendsCount).append("位好友");
            text_sync.setText(sb.toString());
        }
    }


    @Override
    public void OnCall(Object obj) {
    }


}
