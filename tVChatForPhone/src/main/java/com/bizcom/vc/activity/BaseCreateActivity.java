package com.bizcom.vc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bizcom.service.JNIService;
import com.bizcom.vc.adapter.BaseCreateAdapter;
import com.bizcom.vc.widget.cus.MultilevelListView;
import com.bizcom.vc.widget.cus.MultilevelListView.ItemData;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.enums.NetworkStateCode;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 需要调用 initTitle 函数，初始化标题栏
 * 
 * @author Administrator
 * 
 */
public abstract class BaseCreateActivity extends BaseActivity {

	protected static final String TAG = "BaseCreateActivity";
	protected static final int CREATE_LAYOUT_TYPE_CONFERENCE = 0x001;
	protected static final int CREATE_LAYOUT_TYPE_CROWD = 0x002;
	protected static final int CREATE_LAYOUT_TYPE_DISCUSSION = 0x004;
	protected static final int SELECT_GROUP_END = 10;

	protected static final int PAD_LAYOUT = 1;
	protected static final int PHONE_LAYOUT = 0;

	protected Context mContext;

	protected int createType;

	protected State mState = State.DONE;

	protected int landLayout = PAD_LAYOUT;

	protected TextView titleContentTV;
	protected TextView leftButtonTV;
	protected TextView rightButtonTV;
	protected ClearEditText searchedTextET;
	protected TextView mErrorNotification;
	protected View customLayout;

	protected AdapterView<ListAdapter> mAttendeeContainer;
	protected MultilevelListView mGroupListView;
	protected BaseCreateAdapter mAdapter;

	protected EtFocusChangeListener mEtFocusChangeListener = new EtFocusChangeListener();

	// Used to save current selected user
	protected Set<User> mAttendeeList = new HashSet<>();
	protected List<User> mAttendeeArrayList = new ArrayList<>();

	private ProgressDialog mWaitingDialog;

	protected void initCreateType(int createType) {
		this.createType = createType;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setNeedAvatar(false);
		super.setNeedBroadcast(true);
		super.setNeedHandler(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_create_group_layout);
		View functionLy = findViewById(R.id.ws_activity_main_title_functionLy);
		if(functionLy != null){
			functionLy.setVisibility(View.INVISIBLE);
		}
		mContext = this;
		initBase();
		init();
		setListener();
	}

	@SuppressWarnings("unchecked")
	private void initBase() {
		mGroupListView = (MultilevelListView) findViewById(R.id.ws_common_create_group_list_view);
		mGroupListView.initCreateMode();
		mGroupListView
				.setMultilevelType(MultilevelListView.MULTILEVEL_TYPE_CREATE);
		mGroupListView.setListener(listViewListener);

		mAttendeeContainer = (AdapterView<ListAdapter>) findViewById(R.id.ws_common_create_select_layout);
		mAttendeeContainer.setOnItemClickListener(mItemClickedListener);
		landLayout = mAttendeeContainer.getTag().equals("vertical") ? PAD_LAYOUT
				: PHONE_LAYOUT;
		mAdapter = new BaseCreateAdapter(this, mAttendeeArrayList, landLayout);
		mAttendeeContainer.setAdapter(mAdapter);

		mErrorNotification = (TextView) findViewById(R.id.ws_common_error_connect);
		boolean connect = GlobalHolder.getInstance().isServerConnected();
		if (connect) {
			mErrorNotification.setVisibility(View.GONE);
		} else {
			mErrorNotification.setVisibility(View.VISIBLE);
			mErrorNotification
					.setText(R.string.error_create_conference_failed_no_network);
		}

		searchedTextET = (ClearEditText) findViewById(R.id.ws_common_create_search);
		searchedTextET.addTextListener(mGroupListView);

		customLayout = findViewById(R.id.ws_common_create_custom_content_ly);
        View mEditBelowContent = findViewById(R.id.ws_common_create_edit_below_content);
		TextView editNameHint = (TextView) findViewById(R.id.ws_common_create_edit_name_hint);
		TextView editContentHint = (TextView) findViewById(R.id.ws_common_create_edit_content_hint);
		View confStartTime = findViewById(R.id.conference_create_conf_start_time);
		View crowdSpiner = findViewById(R.id.group_create_group_rule);
		switch (createType) {
		case CREATE_LAYOUT_TYPE_CONFERENCE:
			initTitle(R.string.conference_create_title,
					R.string.conference_create_cancel,
					R.string.conference_create_confirm);
			editNameHint.setText(getResources().getString(R.string.conference_create_conf_name) + " :");
			editContentHint.setText(R.string.conference_create_conf_start_time);
			customLayout.setVisibility(View.VISIBLE);
			crowdSpiner.setVisibility(View.GONE);
            mEditBelowContent.setVisibility(View.GONE);
			break;
		case CREATE_LAYOUT_TYPE_CROWD:
			initTitle(R.string.crowd_create_activity_title,
					R.string.common_return_name, R.string.common_confirm_name);
			editNameHint.setText(R.string.group_create_group_name);
			editContentHint.setText(R.string.group_create_group_qualification);
			customLayout.setVisibility(View.VISIBLE);
			confStartTime.setVisibility(View.GONE);
			crowdSpiner.setVisibility(View.VISIBLE);
			break;
		case CREATE_LAYOUT_TYPE_DISCUSSION:
			initTitle(R.string.discussion_create_activity_title,
					R.string.common_return_name, R.string.common_confirm_name);
			customLayout.setVisibility(View.GONE);
//			View devider = findViewById(R.id.divider2);
//			if(devider != null){
//				devider.setVisibility(View.GONE);
//			}
			break;
		default:
			break;
		}
	}

	protected void addAttendee(User u) {
		if (u.getAccountType() == V2GlobalConstants.ACCOUNT_TYPE_PHONE_FRIEND) {
			return;
		}

		if (!mAttendeeList.contains(u))
			mAttendeeList.add(u);
		if (!mAttendeeArrayList.contains(u))
			mAttendeeArrayList.add(u);
		mAdapter.notifyDataSetChanged();
	}

	protected void removeAttendee(User u) {
		mAttendeeList.remove(u);
		mAttendeeArrayList.remove(u);
		mAdapter.notifyDataSetChanged();
	}

	protected void selectGroup(Group selectGroup, boolean addOrRemove) {
		List<Group> subGroups = selectGroup.getChildGroup();
		for (int i = 0; i < subGroups.size(); i++) {
			selectGroup(subGroups.get(i), addOrRemove);
		}
		List<User> list = selectGroup.getUsers();
		for (int i = 0; i < list.size(); i++) {
			User u = list.get(i);
			if (u.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
				continue;
			}

			if (addOrRemove) {
				addAttendee(u);
			} else {
				removeAttendee(u);
			}
		}
	}

	protected void startSelectGroup(final Handler mLocalHandler,
			final CheckBox cb, final Group selectedGroup) {
		mWaitingDialog = ProgressDialog.show(mContext, "", mContext
				.getResources()
				.getString(R.string.notification_watiing_process), true);
		mLocalHandler.post(new Runnable() {

			@Override
			public void run() {
				selectGroup(selectedGroup, cb.isChecked());
				mGroupListView.updateCheckItem(selectedGroup, cb.isChecked());
				mWaitingDialog.dismiss();
				mLocalHandler.sendEmptyMessage(SELECT_GROUP_END);
			}
		});
	}

	@Override
	public void addBroadcast(IntentFilter filter) {
		filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		if (JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION.equals(intent
				.getAction())) {
			NetworkStateCode code = (NetworkStateCode) intent.getExtras().get(
					"state");
			if (code != NetworkStateCode.CONNECTED) {
				mErrorNotification.setVisibility(View.VISIBLE);
				mErrorNotification
						.setText(R.string.common_networkIsDisconnection_failed_no_network);
			} else {
				mErrorNotification.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void receiveMessage(Message msg) {

	}

	@Override
	public void initViewAndListener() {

	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

	}

	protected abstract void init();

	protected abstract void setListener();

	protected abstract void leftButtonClickListener(View v);

	protected abstract void rightButtonClickListener(View v);

	protected abstract void mAttendeeContainerItemClick(AdapterView<?> parent,
			View view, int position, long id);

	protected abstract void mGroupListViewItemClick(AdapterView<?> parent,
			View view, int position, long id, ItemData item);

	protected abstract void mGroupListViewlongItemClick(AdapterView<?> parent,
			View view, int position, long id, ItemData item);

	protected abstract void mGroupListViewCheckBoxChecked(View view,
			ItemData item);
	
	protected abstract void focusChangeListener(View view, boolean focus);

	private void initTitle(int titleContent, int leftButton, int rightButton) {
		titleContentTV = (TextView) findViewById(R.id.ws_common_activity_title_content);
		titleContentTV.setText(titleContent);

		leftButtonTV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		setComBackImageTV(leftButtonTV);
		leftButtonTV.setOnClickListener(leftButtonClickListener);

		rightButtonTV = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
		rightButtonTV.setText(rightButton);
		rightButtonTV.setOnClickListener(rightButtonClickListener);
	}

	private OnClickListener leftButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			leftButtonClickListener(v);
		}

	};

	private OnClickListener rightButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mErrorNotification.setVisibility(View.GONE);
			rightButtonClickListener(v);
		}

	};

	private OnItemClickListener mItemClickedListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mAttendeeContainerItemClick(parent, view, position, id);
		}
	};

	private class EtFocusChangeListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View arg0, boolean focus) {
			focusChangeListener(arg0 , focus);
		}
	}

	private MultilevelListView.MultilevelListViewListener listViewListener = new MultilevelListView.MultilevelListViewListener() {

		@Override
		public void onItemClicked(AdapterView<?> parent, View view,
				int position, long id, ItemData item) {
			mGroupListViewItemClick(parent, view, position, id, item);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id, ItemData item) {
			mGroupListViewlongItemClick(parent, view, position, id, item);
			return true;
		}

		public void onCheckboxClicked(View view, ItemData item) {
			mGroupListViewCheckBoxChecked(view, item);
		}

	};

	public enum State {
		DONE, CREATEING
	}
}
