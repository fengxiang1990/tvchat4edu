package com.bizcom.vc.activity.crow;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.DialogManager;
import com.bizcom.util.V2Log;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.enums.NetworkStateCode;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class CrowdDetailActivity extends Activity {

	private final static String RULE_ALLOW_ALL = "0";
	private final static String RULE_QUALIFICATION = "1";
	private final static String RULE_NEVER = "2";

	private final static int TYPE_BRIEF = 1;
	private final static int TYPE_ANNOUNCE = 2;
	private final static int TYPE_UPDATE_MEMBERS = 3;

	private final static int REQUEST_UPDATE_CROWD_DONE = 1;
	private final static int REQUEST_QUIT_CROWD_DONE = 2;

	private TextView mNoTV;
	private TextView mNameTV;
	private TextView mCreatorTV;
	private TextView mBriefTV;
	private TextView mAnouncementTV;
	private TextView mMembersCountsTV;

	private View mAdminBox;
	private View mQuitButton;
	private View mShowBriefButton;
	private View mShowAnnounceButton;
	private View mShowMembersButton;
	private View mSHowFilesButton;
	private TextView mReturnButton;
	private TextView mButtonText;
	private RadioGroup mRulesRD;
	private View mRulesLayout;
	private View mNewFileNotificator;

	private Dialog mDialog;

	private CrowdGroup crowd;
	private V2CrowdGroupRequest service = new V2CrowdGroupRequest();
	private State mState = State.NONE;
	private LocalReceiver localReceiver;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crowd_detail_activity);
		mContext = this;

		mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		mReturnButton.setText(getResources().getString(R.string.common_back));
		mReturnButton.setOnClickListener(mReturnButtonListener);
		TextView contentTitle = (TextView) findViewById(R.id.ws_common_activity_title_content);
		contentTitle.setText(getResources().getString(R.string.crowd_detail_title));
		findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);

		CustomAvatarImageView mCrowdIcon = (CustomAvatarImageView) findViewById(R.id.ws_common_avatar);
		mCrowdIcon.setImageResource(R.drawable.chat_group_icon);
		mNoTV = (TextView) findViewById(R.id.crowd_detail_no);
		mNameTV = (TextView) findViewById(R.id.ws_common_contact_conversation_topContent);
		mCreatorTV = (TextView) findViewById(R.id.crowd_detail_creator);
		mBriefTV = (TextView) findViewById(R.id.crowd_detail_brief);
		mAnouncementTV = (TextView) findViewById(R.id.crowd_detail_announcement);
		mMembersCountsTV = (TextView) findViewById(R.id.crowd_detail_members);
		mAdminBox = findViewById(R.id.crowd_detail_admistrator_box);
		mButtonText = (TextView) findViewById(R.id.crowd_detail_button_text);
		mRulesRD = (RadioGroup) findViewById(R.id.crowd_detail_radio_group);
		mRulesLayout = findViewById(R.id.crowd_detail_radio_group_layout);
		mQuitButton = findViewById(R.id.crowd_detail_button);
		mQuitButton.setOnClickListener(mQuitButtonListener);

		mShowBriefButton = findViewById(R.id.crowd_detail_brief_button);
		mShowBriefButton.setOnClickListener(mContentButtonListener);
		mShowAnnounceButton = findViewById(R.id.crowd_detail_announcement_button);
		mShowAnnounceButton.setOnClickListener(mContentButtonListener);
		mShowMembersButton = findViewById(R.id.crowd_detail_invitation_members_button);
		mShowMembersButton.setOnClickListener(mShowMembersButtonListener);
		mSHowFilesButton = findViewById(R.id.crowd_detail_files_button);
		mSHowFilesButton.setOnClickListener(mShowFilesButtonListener);

		mNewFileNotificator = findViewById(R.id.crowd_detail_new_file_notificator);

		crowd = (CrowdGroup) GlobalHolder.getInstance().getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD,
				getIntent().getLongExtra("cid", 0));

		if (crowd == null) {
			super.onDestroy();
			return;
		}
		String cid = String.valueOf(crowd.getGroupID());
		mNoTV.setText(cid);

		mCreatorTV.setText(crowd.getOwnerUser().getDisplayName());
		mCreatorTV.setSingleLine();

		mNameTV.setText(crowd.getName());
		mBriefTV.setText(crowd.getBrief());
		mAnouncementTV.setText(crowd.getAnnouncement());
		if (crowd.getOwnerUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			mAdminBox.setVisibility(View.VISIBLE);
			mButtonText.setText(R.string.crowd_detail_qulification_dismiss_button);
		} else {
			mAdminBox.setVisibility(View.GONE);
			mButtonText.setText(R.string.crowd_detail_qulification_quit_button);
		}

		mMembersCountsTV.setText(String.valueOf(crowd.getUsers().size()));
		initRules();
		mRulesRD.setOnCheckedChangeListener(mRulesChangedListener);
		mRulesLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!GlobalHolder.getInstance().isServerConnected()) {
					Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});
		initReceiver();
		updateGroupFileNotificator();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TYPE_BRIEF) {
			mBriefTV.setText(crowd.getBrief());
		} else if (requestCode == TYPE_ANNOUNCE) {
			mAnouncementTV.setText(crowd.getAnnouncement());
		} else if (requestCode == TYPE_UPDATE_MEMBERS) {
			mMembersCountsTV.setText(crowd.getUsers().size() + "");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			this.unregisterReceiver(localReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		service.clearCalledBack();
	}

	private void updateGroupFileNotificator() {
		if (crowd.getNewFileCount() > 0) {
			mNewFileNotificator.setVisibility(View.VISIBLE);
		} else {
			mNewFileNotificator.setVisibility(View.GONE);
		}
	}

	private void initReceiver() {
		localReceiver = new LocalReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
		filter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_UPDATED);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
		filter.addAction(JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION);
		filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
		filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		this.registerReceiver(localReceiver, filter);
	}

	private void initRules() {
		for (int i = 0; i < mRulesRD.getChildCount(); i++) {
			if (mRulesRD.getChildAt(i) instanceof RadioButton) {
				RadioButton rb = (RadioButton) mRulesRD.getChildAt(i);
				if (rb.getTag().equals(RULE_ALLOW_ALL) && crowd.getAuthType() == CrowdGroup.AuthType.ALLOW_ALL) {
					rb.setChecked(true);
				} else if (rb.getTag().equals(RULE_QUALIFICATION)
						&& crowd.getAuthType() == CrowdGroup.AuthType.QULIFICATION) {
					rb.setChecked(true);
				} else if (rb.getTag().equals(RULE_NEVER) && crowd.getAuthType() == CrowdGroup.AuthType.NEVER) {
					rb.setChecked(true);
				}
			}
		}

	}

	private void showDialog() {
		mDialog = DialogManager.getInstance()
				.showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mContext, null,
						mContext.getText(R.string.crowd_detail_quit_confirm_title),
						mContext.getText(R.string.activiy_contact_group_button_confirm),
						mContext.getText(R.string.activiy_contact_group_button_cancel)) {

					@Override
					public void confirmCallBack() {
						service.quitCrowd(crowd, new HandlerWrap(mLocalHandler, REQUEST_QUIT_CROWD_DONE, null));
					}

					@Override
					public void cannelCallBack() {
						mDialog.dismiss();
					}
				});

		if (crowd.getOwnerUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			DialogManager.getInstance().setDialogContent(mContext.getText(R.string.crowd_detail_dismiss_confirm_title));
		} else {
			DialogManager.getInstance().setDialogContent(mContext.getText(R.string.crowd_detail_quit_confirm_title));
		}
		mDialog.show();
	}

	private void handleQuitDone() {
		// Remove cache crowd
		GlobalHolder.getInstance().removeGroup(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID());
		finish();
	}

	private OnClickListener mQuitButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			showDialog();
		}

	};

	private OnClickListener mReturnButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			onBackPressed();
		}
	};

	private OnCheckedChangeListener mRulesChangedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup rg, int id) {
			RadioButton rb = (RadioButton) mRulesRD.findViewById(id);
			synchronized (mState) {
				if (mState == State.PENDING) {
					return;
				}
				mState = State.PENDING;
			}
			CrowdGroup.AuthType at = null;
			if (RULE_ALLOW_ALL.equals(rb.getTag())) {
				at = CrowdGroup.AuthType.ALLOW_ALL;
			} else if (RULE_QUALIFICATION.equals(rb.getTag())) {
				at = CrowdGroup.AuthType.QULIFICATION;
			} else if (RULE_NEVER.equals(rb.getTag())) {
				at = CrowdGroup.AuthType.NEVER;
			} else {
				V2Log.e(" unkonw type");
				mState = State.NONE;
			}

			if (at != crowd.getAuthType()) {
				crowd.setAuthType(at);
				service.updateCrowd(crowd, new HandlerWrap(mLocalHandler, REQUEST_UPDATE_CROWD_DONE, null));
			}
		}
	};

	private OnClickListener mContentButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int type = 0;
			// type value must match with
			// CrowdContentUpdateActivity.UPDATE_TYPE_BRIEF or
			// or CrowdContentUpdateActivity.UPDATE_TYPE_ANNOUNCEMENT
			if (view == mShowBriefButton) {
				type = TYPE_BRIEF;
			} else if (view == mShowAnnounceButton) {
				type = TYPE_ANNOUNCE;
			}

			Intent i = new Intent(PublicIntent.SHOW_CROWD_CONTENT_ACTIVITY);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			i.putExtra("type", type);
			i.putExtra("cid", crowd.getGroupID());
			startActivityForResult(i, type);
		}

	};

	private OnClickListener mShowMembersButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent i = new Intent(mContext, GroupMemberActivity.class);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			i.putExtra("cid", crowd.getGroupID());
			i.putExtra("memberType", GroupMemberActivity.GROUP_MEMBER_TYPE_CROWD);
			startActivityForResult(i, TYPE_UPDATE_MEMBERS);
		}

	};

	private OnClickListener mShowFilesButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {

			Intent i = new Intent(PublicIntent.START_CROWD_FILES_ACTIVITY);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			i.putExtra("cid", crowd.getGroupID());
			startActivity(i);

			crowd.resetNewFileCount();
			updateGroupFileNotificator();
			finish();
		}

	};

	private Handler mLocalHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REQUEST_UPDATE_CROWD_DONE:
				synchronized (mState) {
					mState = State.NONE;
				}
				break;
			case REQUEST_QUIT_CROWD_DONE:
				handleQuitDone();
				if (mDialog != null) {
					mDialog.dismiss();
				}
				break;

			}
		}

	};

	enum State {
		NONE, PENDING;
	}

	class LocalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION.equals(intent.getAction())
					|| intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
				GroupUserObject obj = intent.getParcelableExtra("group");
				if (obj == null) {
					V2Log.e("CrowdDetailActivity",
							"Received the broadcast to quit the crowd group , but crowd id is wroing... ");
					return;
				}
				if (obj.getmGroupId() == crowd.getGroupID()) {
					finish();
				}
			} else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_UPDATED)) {
				long crowdId = intent.getLongExtra("gid", 0);
				// Update content
				if (crowdId == crowd.getGroupID()) {
					crowd = (CrowdGroup) GlobalHolder.getInstance().getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD,
							crowdId);
					if (crowd != null) {
						initRules();
						mBriefTV.setText(crowd.getBrief());
						mAnouncementTV.setText(crowd.getAnnouncement());
						mMembersCountsTV.setText(crowd.getUsers().size() + "");
						mNameTV.setText(crowd.getName());
					}
				}
			} else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_USER_ADDED)) {
				GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
				if (guo.getmGroupId() == crowd.getGroupID()) {
					CrowdGroup newGroup = (CrowdGroup) GlobalHolder.getInstance()
							.getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID());
					if (newGroup != null) {
						crowd = newGroup;
						mMembersCountsTV.setText(String.valueOf(crowd.getUsers().size()));
					}
				}
			} else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED)) {
				GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
				long ownerUserID = crowd.getOwnerUser().getmUserId();
				if (guo.getmUserId() == ownerUserID) {
					finish();
				} else {
					CrowdGroup newGroup = (CrowdGroup) GlobalHolder.getInstance()
							.getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID());
					if (newGroup != null) {
						crowd = newGroup;
						mMembersCountsTV.setText(String.valueOf(crowd.getUsers().size()));
					}
				}
			} else if (intent.getAction().equals(JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION)) {
				long crowdId = intent.getLongExtra("groupID", 0);
				if (crowdId == crowd.getGroupID()) {
					updateGroupFileNotificator();
				}
			} else if (intent.getAction().equals(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION)) {
				NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
				if (code == NetworkStateCode.CONNECTED_ERROR) {
					for (int i = 0; i < mRulesRD.getChildCount(); i++) {
						if (mRulesRD.getChildAt(i) instanceof RadioButton) {
							mRulesRD.getChildAt(i).setClickable(false);
						}
					}
				} else {
					for (int i = 0; i < mRulesRD.getChildCount(); i++) {
						if (mRulesRD.getChildAt(i) instanceof RadioButton) {
							mRulesRD.getChildAt(i).setClickable(true);
						}
					}
				}
			}
		}

	}
}
