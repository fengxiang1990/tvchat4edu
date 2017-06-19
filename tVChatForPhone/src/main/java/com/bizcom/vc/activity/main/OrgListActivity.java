package com.bizcom.vc.activity.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.UserStatusObject;
import com.bizcom.service.JNIService;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.widget.cus.MultilevelListView;
import com.bizcom.vc.widget.cus.MultilevelListView.ItemData;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.List;

public class OrgListActivity extends BaseActivity {

	private static final int UPDATE_GROUP_STATUS = 4;
	private static final String TAG = OrgListActivity.class.getSimpleName();
	private MultilevelListView mOrganizationMLV;
	private boolean mOrgLoading;
    private MultilevelListView.MultilevelListViewListener mMultilevelListItemClickListener = new MultilevelListItemClickListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setNeedAvatar(true);
		super.setNeedBroadcast(true);
		super.setNeedHandler(true);
		setContentView(R.layout.activity_org_main);
		super.onCreate(savedInstanceState);
		fillOrgMultilevelGroup();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mOrganizationMLV.clearAll();
	}

	@Override
	public void addBroadcast(IntentFilter intentFilter) {
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
		intentFilter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
		intentFilter.addAction(JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION)) {
			int groupType = intent.getIntExtra("gtype", -1);
			if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				fillOrgMultilevelGroup();
			}
		} else if (JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION.equals(intent.getAction())) {
			UserStatusObject uso = (UserStatusObject) intent.getExtras().get("status");
			User.Status us;
			if (uso != null) {
				us = User.Status.fromInt(uso.getStatus());
				User orgUser = GlobalHolder.getInstance().getUser(uso.getUid());
				mOrganizationMLV.updateUserStatus(orgUser, us);
			}
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {
			int groupType = intent.getIntExtra("gtype", -1);
			if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				Message.obtain(mHandler, UPDATE_GROUP_STATUS).sendToTarget();
			}
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
			GroupUserObject obj = (GroupUserObject) intent.getExtras().get("obj");
			if (obj == null) {
				V2Log.e(TAG,
						"JNI_BROADCAST_GROUP_USER_REMOVED --> Update Conversation failed that the user removed ... given GroupUserObject is null");
				return;
			}

			if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				// 组织
				User user = GlobalHolder.getInstance().getUser(obj.getmUserId());
				if (user != null) {
					mOrganizationMLV.removeItem(user);
				}
			}
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_ADDED.equals(intent.getAction())) {
			V2Log.d(TAG, "JNI_BROADCAST_GROUP_USER_ADDED --> The New User Coming !");
			GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
			if (guo == null) {
				V2Log.e(TAG, "JNI_BROADCAST_GROUP_USER_ADDED --> Add New User Failed ! Because"
						+ "Given GroupUserObject is null!");
				return;
			}

			if (guo.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				mOrganizationMLV.addUser(GlobalHolder.getInstance().getGroupById(guo.getmGroupId()),
						GlobalHolder.getInstance().getUser(guo.getmUserId()));
			}
			// Contacts group is updated
		} else if (JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO.equals(intent.getAction())) {
			long uid = intent.getLongExtra("uid", -1);
			if (uid == -1)
				return;
			Message.obtain(mHandler, UPDATE_GROUP_STATUS, uid).sendToTarget();
		} else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
			Long uid = intent.getLongExtra("modifiedUser", -1);
			if (uid == -1l) {
				return;
			}
			Message.obtain(mHandler, UPDATE_GROUP_STATUS, uid).sendToTarget();
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		switch (msg.what) {
		case UPDATE_GROUP_STATUS:
			mOrganizationMLV.updateAdapter();
			break;
		}
	}

	@Override
	public void initViewAndListener() {
        setComRightStyle(getResources().getString(R.string.conversation_org_tab_title) , false);
        // 组织
        mOrganizationMLV = (MultilevelListView) findViewById(R.id.ws_org_main_list);
        mOrganizationMLV.setMultilevelType(MultilevelListView.MULTILEVEL_TYPE_ORG);
        mOrganizationMLV.setListener(mMultilevelListItemClickListener);
        mOrganizationMLV.setTextFilterEnabled(true);
        mOrganizationMLV.setDivider(null);
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {
		Message.obtain(mHandler, UPDATE_GROUP_STATUS).sendToTarget();
	}

	private void fillOrgMultilevelGroup() {
		if (mOrgLoading) {
			return;
		}
		new OrgAsyncTaskLoader().execute();
	}

	private class MultilevelListItemClickListener implements MultilevelListView.MultilevelListViewListener {

		@Override
		public void onItemClicked(AdapterView<?> parent, View view, int position, long id, ItemData item) {
			if (item.getItemDataType() == MultilevelListView.MULTILEVEL_ITEM_TYPE_USER) {
				User target = (User) item.getObject();
				if (target.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
					Intent i = new Intent(mContext , ContactDetail2.class);
					i.putExtra("uid", target.getmUserId());
					startActivity(i);
				} else {
					Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
					i.addCategory(PublicIntent.DEFAULT_CATEGORY);
					i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					i.putExtra("obj", new ConversationNotificationObject(V2GlobalConstants.GROUP_TYPE_USER,
							((User) item.getObject()).getmUserId()));
					startActivity(i);
				}
			}
		}

		public void onCheckboxClicked(View view, ItemData item) {

		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, ItemData item) {
			return false;
		}
	}

	private class OrgAsyncTaskLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mOrgLoading) {
				return null;
			}
			mOrgLoading = true;
            List<Group> mOrgGroupList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DEPARTMENT);
			if (mOrgGroupList != null && mOrgGroupList.size() > 0) {
				mOrganizationMLV.setOrgGroupList(mOrgGroupList.get(0));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mOrganizationMLV.updateAdapter();
			mOrgLoading = false;
		}
	}
}
