package com.bizcom.vc.hg.adapter;

import java.util.List;

import com.bizcom.util.xlog.XLog.Log;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.ui.HgAddFriendsConfirmActivity;
import com.bizcom.vo.User;
import com.bizcom.vo.User.Status;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AddFriendsAdapter extends BaseAdapter {
	private List<User> data;
	private Activity mCon;
	private LayoutInflater mInflater;

	public AddFriendsAdapter(List<User> data, Context mCon) {
		super();
		this.data = data;
		this.mCon = (Activity) mCon;

		mInflater = LayoutInflater.from(mCon);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final User user = data.get(position);
		ViewTag tag;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.addfriends_list_item, null);
			tag = new ViewTag();
			tag.tv1 = (TextView) convertView.findViewById(R.id.tv_firstNameTxet);
			tag.tvStatus = (TextView) convertView.findViewById(R.id.tvStatus);
			tag.tv2 = (TextView) convertView.findViewById(R.id.tv_nameTxet);
			tag.tv3 = (TextView) convertView.findViewById(R.id.tv_numberText);
			tag.ll_delete = convertView.findViewById(R.id.ll_delete);
			tag.ll_startCom = convertView.findViewById(R.id.ll_startCom);
			tag.llp = convertView.findViewById(R.id.ll2);
			tag.view = convertView.findViewById(R.id.view);

			convertView.setTag(tag);
		} else {
			tag = (ViewTag) convertView.getTag();
		}

		if (user.getmStatus() == Status.ONLINE) {
			tag.tvStatus.setText("在线");
			tag.tvStatus.setBackgroundResource(R.drawable.online_bg);
			tag.tv1.setBackgroundResource(R.drawable.root_main_tongxunlu_nor);
		} else {
			tag.tvStatus.setText("离线");
			tag.tvStatus.setBackgroundResource(R.drawable.outline_bg);
			tag.tv1.setBackgroundResource(R.drawable.root_main_tongxunlu_unnuor);
			tag.tv1.setText("");
		}

		String displayName = user.getCommentName();
		if (TextUtils.isEmpty(displayName)) {
			displayName = user.getNickName();
		}
		if (displayName != null && displayName.length() > 0) {

			String s=displayName.substring(0, 1);
			if(!checkIfNum(s)){
				tag.tv1.setText(s);
			}else{
				tag.tv1.setBackgroundResource(R.drawable.root_main_tongxunlu_unnuor);
				tag.tv1.setText("");
			}
			
		}
		tag.tv2.setText(displayName);
		tag.tv3.setText(user.getMobile());
		if (user.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			tag.ll_delete.setVisibility(View.GONE);
		} else {
			tag.ll_delete.setVisibility(View.VISIBLE);
		}
		if (data.size() - 1 == position) {
			tag.view.setVisibility(View.VISIBLE);
		} else {
			tag.view.setVisibility(View.GONE);
		}

		tag.ll_startCom.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (user.getmStatus() != Status.ONLINE) {

					ToastUtil.ShowToast_long(mCon, "当前用户不在线");
					return;
				}

				try {
					if (GlobalHolder.getInstance().getCurrentUserId() == user.getmUserId()) {
						ToastUtil.ShowToast_long(mCon, "不能和当前用户视频聊天");
					} else {
						startVideoCall(user.getmUserId());
					}

				} catch (ClassCastException e) {
					ToastUtil.ShowToast_long(mCon, "获取用户信息失败");
				}

			}
		});

		tag.ll_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent in = new Intent(mCon, HgAddFriendsConfirmActivity.class);
				in.putExtra("userId", user.getmUserId());
				in.putExtra("titleText", "添加好友");
				mCon.startActivityForResult(in, 0);
			}
		});
		return convertView;
	}

	private boolean checkIfNum(String s) {
		try {
			int i=Integer.parseInt(s);
			
			return true;
		} catch (Exception e) {
			return false;
		}
	
		
		
	}

	class ViewTag {
		TextView tv1;
		TextView tv2;
		TextView tv3;
		TextView tvStatus;
		View ll_startCom;
		View ll_delete;
		View llp;
		View view;
	}

	/**
	 * 发起视频聊天
	 * 
	 * @param remoteUserId
	 *            对方的id
	 */
	private void startVideoCall(long remoteUserId) {
		if (GlobalHolder.getInstance().checkServerConnected(mCon)) {
			ToastUtil.ShowToast_long(mCon, "服务器连接异常,请稍后再试");
			return;
		}
		GlobalConfig.startP2PConnectChat(mCon, ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId, false, null,
				null);
	}

}
