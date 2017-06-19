package com.bizcom.vc.hg.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.MainApplication;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.User;
import com.shdx.tvchat.phone.R;

import java.util.Collections;

public class SearchFriendTab2 extends SecondTab2 {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onSearch(String search) {
        mCurrentUser.clear();
        if (!TextUtils.isEmpty(search)) {
            for (User u : MainApplication.mLatelyUserList) {
                if (u.getAccount().contains(search) || u.getNickName().contains(search) ||(!TextUtils.isEmpty(u.getCommentName())&&u.getCommentName().contains(search))) {
                    mCurrentUser.add(u);
                }
            }
        }
        if (mAdapter != null) {
            notifyAdapter(search);
        }
    }

    @Override
    public int getType() {
        return LatelyAdapter.TYPE_LATELY_SEARCH;
    }

    private void notifyAdapter(String search) {
        mAdapter.notifyDataSetChanged();
        updateEmptyView(search);
    }

    private void updateEmptyView(String search) {
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) emptyView.getLayoutParams();
//        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//        emptyView.setLayoutParams(lp);
        if (mCurrentUser.size() - 1 < 0) {
            emptyView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(search)) {
                emptyView.setImageResource(R.mipmap.ic_add_friend_search);
                textEmpty.setVisibility(View.GONE);
            } else {
                textEmpty.setText("没有搜索到这个人喔");
                emptyView.setImageResource(R.mipmap.ic_add_friend_empty);
                textEmpty.setVisibility(View.VISIBLE);
            }

        } else {
            textEmpty.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }


    public void getData() {
    }

    public void dispatch(AudioVideoMessageBean item) {

    }

    public void notifyAdapter() {

    }

    @Override
    public void OnCall(Object obj) {
    }
}
