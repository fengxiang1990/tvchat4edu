package com.bizcom.vc.activity.conference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bizcom.request.V2ConferenceRequest;
import com.bizcom.vo.User;
import com.bizcom.vo.enums.ConferencePermission;
import com.bizcom.vo.enums.PermissionState;
import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class ConferenceMsgDialog extends Dialog {

	private Set<User> users;
	private List<listViewItemData> mList;
	private ListView lv;
	private LayoutInflater inflater;
	private V2ConferenceRequest service;
	private BaseAdapter mAdapter = new ListAdapter();

	public ConferenceMsgDialog(Context context, Set<User> list, V2ConferenceRequest service) {
		super(context, R.style.DialogStyle1);
		setCanceledOnTouchOutside(true);
		users = list;
		mList = new ArrayList<listViewItemData>();
		Iterator<User> iterator = users.iterator();
		while (iterator.hasNext()) {
			listViewItemData itemData = new listViewItemData();
			itemData.user = iterator.next();
			itemData.state = listViewItemData.STATE_UNDISPOSED;
			mList.add(itemData);
		}

		this.service = service;
		initLayout(context);
	}

	private void initLayout(Context context) {
		inflater = LayoutInflater.from(context);
		View root = inflater.inflate(R.layout.conference_request_host_list, null);
		lv = (ListView) root.findViewById(R.id.conference_request_host_list_view);
		lv.setAdapter(mAdapter);
		setContentView(root, new LayoutParams(LayoutParams.WRAP_CONTENT, (int) (GlobalConfig.getAdapterScreenHeight() * 2 / 3 + 0.5f)));
	}

	public void resetList(Set<User> list) {
		users = list;
		mList.clear();
		Iterator<User> iterator = users.iterator();
		while (iterator.hasNext()) {
			listViewItemData itemData = new listViewItemData();
			itemData.user = iterator.next();
			itemData.state = listViewItemData.STATE_UNDISPOSED;
			mList.add(itemData);
		}

		mAdapter.notifyDataSetChanged();
	}

	public void addToList(User user) {
		if (user == null) {
			return;
		}
		boolean ret = false;
		Iterator<listViewItemData> iterator = mList.iterator();
		while (iterator.hasNext()) {
			listViewItemData itemData = new listViewItemData();
			itemData = iterator.next();
			if (user.getmUserId() == itemData.user.getmUserId()) {
				itemData.state = listViewItemData.STATE_UNDISPOSED;
				ret = true;
			}
		}

		if (!ret) {
			listViewItemData itemData = new listViewItemData();
			itemData.user = user;
			itemData.state = listViewItemData.STATE_UNDISPOSED;
			mList.add(itemData);
		}

		mAdapter.notifyDataSetChanged();
	}

	public void deleteFromList(User user) {
		if (user == null) {
			return;
		}
		boolean ret = false;
		Iterator<listViewItemData> iterator = mList.iterator();
		while (iterator.hasNext()) {
			listViewItemData itemData = new listViewItemData();
			itemData = iterator.next();
			if (user.getmUserId() == itemData.user.getmUserId()) {
				iterator.remove();
				ret = true;
			}
		}

		if (mList.size() == 0) {
			this.dismiss();
			return;
		}

		if (ret) {
			mAdapter.notifyDataSetChanged();
		}

	}

	class listViewItemData {
		private static final int STATE_UNDISPOSED = 0;
		private static final int STATE_ACCESS = 1;
		private static final int STATE_REJECT = 2;

		public User user;
		public int state = STATE_UNDISPOSED;
	}

	class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList == null ? 0 : mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mList.get(position).user.getmUserId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.conference_request_host_list_item, null);
			}
			listViewItemData itemData = mList.get(position);
			((TextView) convertView.findViewById(R.id.conference_request_host_user_name))
					.setText(itemData.user.getDisplayName());
			View acceptButton = convertView.findViewById(R.id.conference_request_host_button_accept);
			View rejectButton = convertView.findViewById(R.id.conference_request_host_button_reject);

			TextView state = (TextView) convertView.findViewById(R.id.state_text);

			switch (itemData.state) {
			case listViewItemData.STATE_UNDISPOSED:
				state.setVisibility(View.GONE);
				acceptButton.setVisibility(View.VISIBLE);
				acceptButton.setTag(itemData);
				acceptButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onAcceptButtonClick(v);
					}
				});

				rejectButton.setVisibility(View.VISIBLE);
				rejectButton.setTag(itemData);
				rejectButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onRejectButtonClick(v);
					}
				});
				break;
			case listViewItemData.STATE_ACCESS:
				acceptButton.setVisibility(View.GONE);
				rejectButton.setVisibility(View.GONE);
				state.setVisibility(View.VISIBLE);
				state.setText(R.string.conference_state_access);
				break;
			case listViewItemData.STATE_REJECT:
				acceptButton.setVisibility(View.GONE);
				rejectButton.setVisibility(View.GONE);
				state.setVisibility(View.VISIBLE);
				state.setText(R.string.conference_state_reject);
				break;
			}

			return convertView;
		}

		private void onAcceptButtonClick(View v) {
			listViewItemData itemData = (listViewItemData) v.getTag();
			service.grantPermission(itemData.user, ConferencePermission.CONTROL, PermissionState.GRANTED, null);
			itemData.state = listViewItemData.STATE_ACCESS;
			users.remove(itemData.user);
			mAdapter.notifyDataSetChanged();
		}

		private void onRejectButtonClick(View v) {
			listViewItemData itemData = (listViewItemData) v.getTag();
			service.grantPermission(itemData.user, ConferencePermission.CONTROL, PermissionState.NORMAL, null);
			itemData.state = listViewItemData.STATE_REJECT;
			users.remove(itemData.user);
			mAdapter.notifyDataSetChanged();
		}
	}

}
