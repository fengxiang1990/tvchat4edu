package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.MainApplication;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.adapter.FriendAdapter;
import com.bizcom.vc.hg.view.GridViewWithHeaderAndFooter;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.lidroid.xutils.exception.DbException;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFriendTab1 extends Fragment {

    @BindView(R.id.text_empty)
    TextView textEmpty;
    private GridViewWithHeaderAndFooter gd;
    private List<User> data = new ArrayList<User>();
    private List<User> dataAll = new ArrayList<User>();
    private FriendAdapter mAdapter;

    private ImageView emptyView;
    private Activity mContext;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hg_2nd_tab_one, container, false);
        ButterKnife.bind(this, v);

        initGridView(v);
        initData();
        updateEmptyView("");
        return v;
    }

    private void initGridView(View v) {
        emptyView = (ImageView) v.findViewById(R.id.emptyView);
        emptyView.setVisibility(View.VISIBLE);

        mAdapter = new FriendAdapter(data, this.getActivity());
        gd = (GridViewWithHeaderAndFooter) v.findViewById(R.id.gd);
        gd.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gd.setAdapter(mAdapter);
        gd.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= data.size())
                    return;
                User u = data.get(position);
                startVideoCall(u.getmUserId());

            }
        });
        gd.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position >= data.size())
                    return true;

                Intent it = new Intent(mContext, UserDetailActivity.class);
                it.putExtra(UserDetailActivity.EXTRA_USER, data.get(position).getmUserId());
                startActivity(it);
                return true;
            }
        });
    }

    private void initData() {
        try {
            ArrayList<User> users = (ArrayList<User>) MainApplication.getDbUtils().findAll(User.class);
            dataAll.clear();
            dataAll.addAll(users);

            if (users == null || users.isEmpty()) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                }, 1000);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void onSearch(String search) {
        data.clear();
        if (!TextUtils.isEmpty(search)) {
            for (User u : dataAll) {
                if (u.getAccount().contains(search) || u.getNickName().contains(search) || u.getCommentName().contains(search)) {
                    if(!data.contains(u)){
                        data.add(u);
                    }
                }
            }
        }
        notifyAdapter(search);
    }

    private void notifyAdapter(String search) {
        for (User u : data) {
            u.updateStatus(User.Status.fromInt(u.getmStatusToIntValue()));
        }

        Collections.sort(data);
        mAdapter.notifyDataSetChanged();
        updateEmptyView(search);
    }

    private void updateEmptyView(String search) {
        if (data.size() - 1 < 0) {
            emptyView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(search)) {
                emptyView.setImageResource(R.mipmap.ic_add_friend_search);
                textEmpty.setVisibility(View.GONE);
            } else {
                emptyView.setImageResource(R.mipmap.ic_add_friend_empty);
                textEmpty.setVisibility(View.VISIBLE);
            }

        } else {
            textEmpty.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * 发起视频聊天
     *
     * @param remoteUserId 对方的id
     */
    private void startVideoCall(long remoteUserId) {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            ToastUtil.ShowToast_long(mContext, "服务器连接异常,请稍后再试");
            return;
        }
        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId, false,
                null, null);
    }
}
