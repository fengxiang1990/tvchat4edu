package com.bizcom.vc.activity.crow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.service.JNIService;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.adapter.view.GroupMemberView;
import com.bizcom.vc.adapter.view.GroupMemberView.ClickListener;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupMemberActivity extends BaseActivity {

	public static final int GROUP_MEMBER_TYPE_CROWD = 0;
	public static final int GROUP_MEMBER_TYPE_DISCUSSION = 1;
	private static final String TAG = GroupMemberActivity.class.getSimpleName();

	private int activityType;

	private Group memberGroup;
	private List<User> mMembers;
	private List<User> deleteMemberList;
	private ListView mMembersContainer;
	private TextView mInvitationButton;
	private TextView mTitleTV;
	private TextView mReturnButton;
	private MembersAdapter adapter;

	private V2CrowdGroupRequest service;
	private boolean isInDeleteMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.crowd_members_activity);
		super.setNeedAvatar(true);
		super.setNeedBroadcast(true);
		super.setNeedHandler(false);
		super.onCreate(savedInstanceState);
		mContext = this;
		activityType = getIntent().getIntExtra("memberType", 0);
		V2Log.d(TAG, "open member activity type is : " + activityType);
		View functionLy = findViewById(R.id.ws_activity_main_title_functionLy);
		if(functionLy != null){
			functionLy.setVisibility(View.INVISIBLE);
		}
		setListener();
		init();
	}

	@Override
	protected void onDestroy() {
		deleteMemberList.clear();
		mMembers.clear();
		service.clearCalledBack();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (isInDeleteMode) {
			isInDeleteMode = false;
			mInvitationButton.setText(R.string.crowd_members_invitation);
			for (int i = 0; i < deleteMemberList.size(); i++) {
				User user = deleteMemberList.get(i);
				int index = mMembers.indexOf(user);
				if (index != -1) {
					User search = mMembers.get(index);
					search.isShowDelete = false;
				}
			}
			deleteMemberList.clear();
			adapter.notifyDataSetChanged();
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_CANCELED) {
			mMembers.clear();
			mMembers = memberGroup.getUsers();
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void addBroadcast(IntentFilter filter) {
		filter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
		filter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		if (intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
			GroupUserObject obj = intent.getParcelableExtra("group");
			if (obj == null) {
				V2Log.e("GroupMemberActivity",
						"Received the broadcast to quit the crowd group , but crowd id is wroing... ");
				return;
			}
			if (obj.getmGroupId() == memberGroup.getGroupID()) {
				finish();
			}
		} else if (intent.getAction().equals(
				JNIService.JNI_BROADCAST_GROUP_USER_REMOVED)) {
			GroupUserObject obj = intent.getParcelableExtra("obj");
			if(V2GlobalConstants.GROUP_TYPE_DEPARTMENT == obj.getmType()
					 && activityType == GROUP_MEMBER_TYPE_CROWD){
				long ownerUserID = memberGroup.getOwnerUser().getmUserId();
				if (obj.getmUserId() == ownerUserID) {
					finish();
				}
			} else {
				updateMembersChange(obj);
			}
		} else if (intent.getAction().equals(
				JNIService.JNI_BROADCAST_GROUP_USER_ADDED)) {
			GroupUserObject obj = intent.getParcelableExtra("obj");
			updateMembersChange(obj);
		}
	}

	@Override
	public void receiveMessage(Message msg) {

	}

	@Override
	public void initViewAndListener() {
		mInvitationButton = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
		mInvitationButton.setText(getResources().getString(
				R.string.crowd_members_invitation));
		mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		mReturnButton.setText(getResources().getString(R.string.common_back));
		mTitleTV = (TextView) findViewById(R.id.ws_common_activity_title_content);
		mTitleTV.setText(getResources().getString(R.string.crowd_members_title));

		mMembersContainer = (ListView) findViewById(R.id.crowd_members_list);
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {
		if (targetUser == null || bnewAvatarm == null
				|| bnewAvatarm.isRecycled()) {
			return;
		}

		adapter.notifyDataSetChanged();
	}

	private void init() {
		service = new V2CrowdGroupRequest();
		deleteMemberList = new ArrayList<User>();

		long cid = getIntent().getLongExtra("cid", 0);
		if (activityType == GROUP_MEMBER_TYPE_CROWD) {
			memberGroup = GlobalHolder.getInstance().getGroupById(
					V2GlobalConstants.GROUP_TYPE_CROWD, cid);
			if (memberGroup.getOwnerUser().getmUserId() != GlobalHolder
					.getInstance().getCurrentUserId()) {
				mInvitationButton.setVisibility(View.INVISIBLE);
			}
		} else {
			mTitleTV.setText(getResources().getString(
					R.string.discussion_board_detail_members));
			memberGroup = GlobalHolder.getInstance().getGroupById(
						V2GlobalConstants.GROUP_TYPE_DISCUSSION, cid);
		}

		mMembers = memberGroup.getUsers();
		V2Log.d(TAG, "Group name is : " + memberGroup.getName());
		V2Log.d(TAG, "Get members is : " + mMembers.size());
		sortMembers();
		adapter = new MembersAdapter();
		mMembersContainer.setAdapter(adapter);
	}

	private void setListener() {
		mMembersContainer.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

			}
		});
		mMembersContainer
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						if (memberGroup.getOwnerUser().getmUserId() != GlobalHolder
								.getInstance().getCurrentUserId()) {
							return false;
						}
						if (!isInDeleteMode) {
							isInDeleteMode = true;
							mInvitationButton
									.setText(R.string.crowd_members_deletion_mode_quit_button);
							adapter.notifyDataSetChanged();
							return true;
						}
						return false;
					}
				});

		mReturnButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mInvitationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isInDeleteMode) {
					onBackPressed();
				} else {
					if (!GlobalHolder.getInstance().isServerConnected()) {
						Toast.makeText(mContext,
								R.string.common_networkIsDisconnection_failed_no_network,
								Toast.LENGTH_SHORT).show();
					} else {
						Intent i = null;
						if (activityType == GROUP_MEMBER_TYPE_CROWD) {
							i = new Intent(
									PublicIntent.START_GROUP_CREATE_ACTIVITY);
							i.addCategory(PublicIntent.DEFAULT_CATEGORY);
							i.putExtra("cid", memberGroup.getGroupID());
							i.putExtra("mode", true);
							startActivity(i);
						} else {
							i = new Intent(
									PublicIntent.START_DISCUSSION_BOARD_CREATE_ACTIVITY);
							i.addCategory(PublicIntent.DEFAULT_CATEGORY);
							i.putExtra("cid", memberGroup.getGroupID());
							i.putExtra("mode", true);
							startActivityForResult(i, 100);
						}
					}
				}
			}
		});
	}

	private void updateMembersChange(GroupUserObject obj) {
		long cid = obj.getmGroupId();
		if (activityType == GROUP_MEMBER_TYPE_CROWD
				&& obj.getmType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
			memberGroup = (CrowdGroup) GlobalHolder.getInstance().getGroupById(
					V2GlobalConstants.GROUP_TYPE_CROWD, cid);
		} else if (activityType == GROUP_MEMBER_TYPE_DISCUSSION
				&& obj.getmType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
			memberGroup = (DiscussionGroup) GlobalHolder.getInstance()
					.getGroupById(	V2GlobalConstants.GROUP_TYPE_DISCUSSION, cid);
		} else {
			return;
		}
		mMembers = memberGroup.getUsers();
		sortMembers();
		adapter.notifyDataSetChanged();
	}

	private void sortMembers() {
		long ownerID = memberGroup.getOwnerUser().getmUserId();
		long loginUserID = GlobalHolder.getInstance().getCurrentUserId();
		User loginUser = GlobalHolder.getInstance().getCurrentUser();
		int ownerPos = -1;
		int loginPos = -1;
		boolean isExistCreater = false;


        Collections.sort(mMembers);
		for (int i = 0; i < mMembers.size(); i++) {
			if (ownerPos != -1 && loginPos != -1)
				break;

			if (ownerID == mMembers.get(i).getmUserId()) {
				ownerPos = i;
			} else if (loginUserID == mMembers.get(i).getmUserId()) {
				loginPos = i;
			}
		}

		if (ownerPos != -1) {
			isExistCreater = true;
			User user = mMembers.get(ownerPos);
			mMembers.remove(user);
			mMembers.add(0, user);
		}

		if (loginPos != -1) {
			mMembers.remove(loginUser);
			if (isExistCreater) {
				mMembers.add(1, loginUser);
			} else {
				mMembers.add(0, loginUser);
			}
		}
	}

	public ClickListener memberClick = new ClickListener() {

		@Override
		public void removeMember(User user) {
			service.removeMember(memberGroup, user, null);
			mMembers.remove(user);
			deleteMemberList.remove(user);
			user.isShowDelete = false;
			adapter.notifyDataSetChanged();
		}

		@Override
		public void changeDeletedMembers(boolean isAdd, User user) {
			if (isAdd) {
				deleteMemberList.add(user);
			} else {
				deleteMemberList.remove(user);
			}
		}
	};

	class MembersAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mMembers.size();
		}

		@Override
		public Object getItem(int position) {
			return mMembers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mMembers.get(position).getmUserId();
		}

		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			User temp = mMembers.get(position);
			if (convertView == null) {
				convertView = new GroupMemberView(mContext, temp, memberClick , isInDeleteMode , memberGroup.getOwnerUser());
			} else {
				((GroupMemberView) convertView).update(isInDeleteMode, temp,
						memberGroup.getOwnerUser());
			}
			return convertView;
		}
	}
}
