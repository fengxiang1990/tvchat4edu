package com.bizcom.vc.activity.contacts;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.jni.GroupServiceJNIResponse;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.DensityUtils;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vo.ContactGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.enums.NetworkStateCode;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.List;

public class SelectJionGroupActivity extends Activity {

	private static final int UPDATE_USER_GROUP_DONE = 1;
	public static final int SELECT_GROUP_RESPONSE_CODE_DONE = 0;
	public static final int SELECT_GROUP_RESPONSE_CODE_CANCEL = 1;

	private Context mContext;
	private RadioGroup mGroupListLy;
	private View mRadioGroupLy;
	private V2ContactsRequest contactService = new V2ContactsRequest();

	private STATE state = STATE.NONE;
	private boolean changed;
	private long originGroupId;
	private long userId;
	private Toast mToast;
	// 值为"addFriend"时是从加好友跳转而来，其他值为更改分组跳转而来。
	private String from;
	private TextView mReturnButton;
	private LocalReceiver localReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		originGroupId = getIntent().getLongExtra("gid", 0);
		userId = getIntent().getLongExtra("uid", 0);
		from = getIntent().getStringExtra("from");

		setContentView(R.layout.activity_contacts_update_group);

		TextView titleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
		titleContent.setText(getResources().getString(R.string.activiy_contact_group_title));
		mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		mReturnButton.setText(getResources().getString(R.string.crowd_files_return_button));
		mReturnButton.setOnClickListener(mReturnButtonListener);
		findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.GONE);

		mGroupListLy = (RadioGroup) findViewById(R.id.contact_update_group_list);
		mRadioGroupLy = findViewById(R.id.contact_update_group_list_layout);
		mRadioGroupLy.setOnTouchListener(mRadioGroupLyListener);
		if (from != null && from.equals("addFriend")) {
			((TextView) mReturnButton).setText(R.string.contacts_update_group_add_friends);
		} else {
			((TextView) mReturnButton).setText(R.string.contacts_update_group_back);
		}
		initReceiver();
		// build radio button first
		buildList();
		mGroupListLy.setOnCheckedChangeListener(mGroupChangedListener);
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initRadioGroup();
	}

	private void initRadioGroup() {
		if (!GlobalHolder.getInstance().isServerConnected()) {
			for (int i = 0; i < mGroupListLy.getChildCount(); i++) {
				if (mGroupListLy.getChildAt(i) instanceof RadioButton) {
					mGroupListLy.getChildAt(i).setClickable(false);
				}
			}
		}
	}

	private void initReceiver() {
		localReceiver = new LocalReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
		filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		this.registerReceiver(localReceiver, filter);
	}

	private void buildList() {

		List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
		for (int i = 1; i < friendGroup.size(); i++) {
			Group g = friendGroup.get(i);

			RadioButton rb = (RadioButton) LayoutInflater.from(mContext).inflate(R.layout.common_radio_right, null);
			rb.setText(g.getName());
			rb.setTag(g);
			int leftPadding = (int) mContext.getResources().getDimension(R.dimen.common_radio_button_margin_left);
			int rightPadding = (int) mContext.getResources().getDimension(R.dimen.common_radio_button_margin_right);
			int topPadding = (int) mContext.getResources().getDimension(R.dimen.common_radio_button_margin_top);
			int buttomPadding = (int) mContext.getResources().getDimension(R.dimen.common_radio_button_margin_buttom);
			rb.setPadding(leftPadding, rightPadding, topPadding, buttomPadding);
			rb.setId((int) g.getGroupID());

			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			mGroupListLy.addView(rb, ll);

			LinearLayout line = new LinearLayout(mContext);
			line.setBackgroundColor(getResources().getColor(R.color.common_divider_color_gray));
			mGroupListLy.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					DensityUtils.dip2px(mContext, 1)));
			if (g.getGroupID() == originGroupId) {
				rb.toggle();
			}
		}

		if ((from != null) && from.equals("addFriend")) {
			int i1 = (int) getIntent().getLongExtra("groupID", -1);
			if (i1 != -1) {
				mGroupListLy.check(i1);
			}
		}
	}

	@Override
	public void finish() {
		if ((from != null) && from.equals("addFriend")) {
		} else {
			if (changed) {
				Intent i = new Intent(PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP);
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				mContext.sendBroadcast(i);
			}
		}
		super.finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(localReceiver);
		contactService.clearCalledBack();
	}

	@Override
	public void onBackPressed() {
		if ((from != null) && from.equals("addFriend")) {
			setResult(SELECT_GROUP_RESPONSE_CODE_CANCEL, null);
		}
		super.onBackPressed();
	}

	private OnClickListener mReturnButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}

	};

	private OnTouchListener mRadioGroupLyListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (!GlobalHolder.getInstance().isServerConnected()) {
				Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
			}
			return false;
		}

	};

	private RadioGroup.OnCheckedChangeListener mGroupChangedListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup rg, int id) {
			if ((from != null) && from.equals("addFriend")) {
				if (!GlobalHolder.getInstance().checkServerConnected(mContext)) {
                    RadioButton rb = (RadioButton) rg.findViewById(id);
                    Group group = (Group) rb.getTag();
                    Intent intent = new Intent();
                    intent.putExtra("groupName", group.getName());
                    intent.putExtra("groupID", group.getGroupID());
                    setResult(SELECT_GROUP_RESPONSE_CODE_DONE, intent);
				}
				finish();
			} else {
				synchronized (state) {
					if (GlobalHolder.getInstance().checkServerConnected(mContext))

					if (state == STATE.UPDATING) {
						if (mToast == null) {
							mToast = Toast.makeText(mContext,
									R.string.activiy_contact_update_group_error_msg_in_progess, Toast.LENGTH_SHORT);
						}
						mToast.cancel();
						mToast.show();
						return;
					}
					state = STATE.UPDATING;
				}
				Group srcGroup = GlobalHolder.getInstance().getGroupById(originGroupId);
				// update group id to new group
				originGroupId = ((Group) rg.findViewById(id).getTag()).getGroupID();
				Group desGroup = GlobalHolder.getInstance().getGroupById(originGroupId);

				User user = GlobalHolder.getInstance().getUser(userId);
				contactService.updateUserGroup((ContactGroup) desGroup, (ContactGroup) srcGroup, user, new HandlerWrap(
						mLocalHandler, UPDATE_USER_GROUP_DONE, new LocalObject(user, srcGroup, desGroup)));
				WaitDialogBuilder.showNormalWithHintProgress(mContext);
			}
		}
	};

	class LocalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION)) {
				NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
				if (code == NetworkStateCode.CONNECTED_ERROR) {
					for (int i = 0; i < mGroupListLy.getChildCount(); i++) {
						if (mGroupListLy.getChildAt(i) instanceof RadioButton) {
							mGroupListLy.getChildAt(i).setClickable(false);
						}
					}
				} else {
					for (int i = 0; i < mGroupListLy.getChildCount(); i++) {
						if (mGroupListLy.getChildAt(i) instanceof RadioButton) {
							mGroupListLy.getChildAt(i).setClickable(true);
						}
					}
				}
			}
		}
	}

	private Handler mLocalHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if ((from != null) && from.equals("addFriend")) {
			} else {
				switch (msg.what) {
				case UPDATE_USER_GROUP_DONE:
					WaitDialogBuilder.dismissDialog();
					synchronized (state) {
						state = STATE.NONE;
					}
					JNIResponse response = (JNIResponse) msg.obj;
					if (response.getResult() == Result.SUCCESS) {
						LocalObject lo = (LocalObject) ((GroupServiceJNIResponse) msg.obj).callerObject;
						// Send broadcast for indicate contact group update
						Intent i = new Intent(PublicIntent.BROADCAST_CONTACT_GROUP_UPDATED_NOTIFICATION);
						i.addCategory(PublicIntent.DEFAULT_CATEGORY);
						i.putExtra("userId", lo.user.getmUserId());
						i.putExtra("srcGroupId", lo.src.getGroupID());
						i.putExtra("destGroupId", lo.dest.getGroupID());

						mContext.sendBroadcast(i);

						// Set result to parent
						Intent intent = new Intent();
						Group g = GlobalHolder.getInstance().getGroupById(lo.dest.getGroupID());
						intent.putExtra("groupName", g.getName());
						intent.putExtra("groupID", lo.dest.getGroupID());
						setResult(SELECT_GROUP_RESPONSE_CODE_DONE, intent);

						finish();
					}
					break;
				}
			}
		}

	};

	class LocalObject {
		User user;
		Group src;
		Group dest;

		public LocalObject(User user, Group src, Group dest) {
			super();
			this.user = user;
			this.src = src;
			this.dest = dest;
		}

	}

	enum STATE {
		NONE, UPDATING
	}

}
