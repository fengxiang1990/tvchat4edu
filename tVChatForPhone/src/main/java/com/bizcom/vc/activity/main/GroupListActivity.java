package com.bizcom.vc.activity.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.Notificator;
import com.bizcom.util.SearchUtils.ScrollItem;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.conference.GroupLayout;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.CrowdConversation;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DiscussionConversation;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GroupListActivity extends BaseActivity {

	private static final String TAG = GroupListActivity.class.getSimpleName();

	private static final int QUIT_DISCUSSION_BOARD_DONE = 0;
	private static final int QUIT_CROWD_GROUP_DONE = 1;

	private ListView mGroupLV;
	private GroupConversationAdapter mGroupLVA = new GroupConversationAdapter();
	private V2CrowdGroupRequest chatService;

	private TreeSet<ScrollItem> mGroupItemList = new TreeSet<>();
	private List<Group> mGroupLeaveOut = new ArrayList<>();
	private Conversation currentClickConversation;

	private OnItemClickListener mGroupLVItemClickListener = new GroupLVItemClickListener();
	private OnItemLongClickListener mGroupLVItemLongClickListener = new GroupLVItemLongClickListener();
	private OnClickListener mPopupWindowGroupItemClickListener = new PopupWindowGroupItemClickListener();

	private PopupWindow mPopup;
	private TextView mGroupQuitPopTV;
	private TextView mGroupDissolutionPopTV;
	private TextView mGroupQuitDisPopTV;

	private boolean mGroupLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setNeedAvatar(false);
		super.setNeedBroadcast(true);
		super.setNeedHandler(true);
		setContentView(R.layout.activity_group_main);
		super.onCreate(savedInstanceState);
		chatService = new V2CrowdGroupRequest();
		loadGroups(V2GlobalConstants.GROUP_TYPE_CROWD);
		loadGroups(V2GlobalConstants.GROUP_TYPE_DISCUSSION);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		chatService.clearCalledBack();
	}

	@Override
	public void addBroadcast(IntentFilter intentFilter) {
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
		intentFilter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
		intentFilter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_UPDATED);
		intentFilter.addAction(PublicIntent.BROADCAST_NEW_CROWD_NOTIFICATION);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION)) {
			int groupType = intent.getIntExtra("gtype", -1);
			loadGroups(groupType);
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {
			int groupType = intent.getIntExtra("gtype", -1);
			if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD || groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION
					|| groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				mGroupLVA.notifyDataSetChanged();
			}
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
			GroupUserObject obj = (GroupUserObject) intent.getExtras().get("obj");
			if (obj == null) {
				V2Log.e(TAG,
						"JNI_BROADCAST_GROUP_USER_REMOVED --> Update Conversation failed that the user removed ... given GroupUserObject is null");
				return;
			}

			if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				// 群组
				List<ScrollItem> removed = new ArrayList<>();
				Iterator<ScrollItem> iterator = mGroupItemList.iterator();
				while (iterator.hasNext()) {
					ScrollItem scrollItem = iterator.next();
					Conversation cov = scrollItem.cov;
					if (cov.getType() == Conversation.TYPE_DISCUSSION) {
						DiscussionConversation dis = (DiscussionConversation) cov;
						if (dis.getGroup().getOwnerUser().getmUserId() == obj.getmUserId()) {
							removed.add(scrollItem);
						}
					} else {
						CrowdConversation crowdConv = (CrowdConversation) cov;
						if (crowdConv.getGroup().getOwnerUser().getmUserId() == obj.getmUserId()) {
							removed.add(scrollItem);
						}
					}
				}

				for (int i = 0; i < removed.size(); i++) {
					mGroupItemList.remove(removed.get(i));
				}
			}
			mGroupLVA.notifyDataSetChanged();
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_ADDED.equals(intent.getAction())) {
			GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
			if (guo == null) {
				V2Log.e(TAG, "JNI_BROADCAST_GROUP_USER_ADDED --> Add New User Failed ! Because"
						+ "Given GroupUserObject is null!");
				return;
			}

			mGroupLVA.notifyDataSetChanged();
			// Contacts group is updated
		} else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
			Long uid = intent.getLongExtra("modifiedUser", -1);
			if (uid == -1l) {
				return;
			}

			mGroupLVA.notifyDataSetChanged();
		} else if (PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION.equals(intent.getAction())
				|| intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
			GroupUserObject obj = intent.getParcelableExtra("group");
			if (obj == null) {
				V2Log.e(TAG, "Received the broadcast to quit the crowd group , but crowd id is wroing... ");
				return;
			}

			if (V2GlobalConstants.GROUP_TYPE_DISCUSSION == obj.getmType()
					&& obj.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
				return;
			}

			if (V2GlobalConstants.GROUP_TYPE_DISCUSSION == obj.getmType()
					|| V2GlobalConstants.GROUP_TYPE_CROWD == obj.getmType()) {
				groupRemoveConversation(obj.getmGroupId());
			}
		} else if (JNIService.JNI_BROADCAST_GROUP_UPDATED.equals(intent.getAction())) {
			long gid = intent.getLongExtra("gid", 0);
			Group g = GlobalHolder.getInstance().getGroupById(gid);
			if (g == null) {
				V2Log.e(TAG, "Update Group Infos Failed... Because get null goup , id is : " + gid);
				return;
			}

			if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD
					|| g.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
				mGroupLVA.notifyDataSetChanged();
			}
		} else if (PublicIntent.BROADCAST_NEW_CROWD_NOTIFICATION.equals(intent.getAction())) {
			GroupUserObject obj = intent.getParcelableExtra("group");
			if (obj == null) {
				V2Log.e(TAG, "Received the broadcast to quit the crowd group , but crowd id is wroing... ");
				return;
			} else {
				V2Log.d(TAG, "Received the new group broadcast !");
			}

			if (V2GlobalConstants.GROUP_TYPE_CROWD == obj.getmType()) {
				Group crowd = GlobalHolder.getInstance().getGroupById(obj.getmGroupId());
				if (crowd != null) {
					if (mGroupLoading) {
						mGroupLeaveOut.add(crowd);
					} else {
						groupAddNewConversation(crowd);
					}
				} else
					V2Log.e(TAG, "Can not get crowd group :" + obj.getmGroupId());
			} else if (V2GlobalConstants.GROUP_TYPE_DISCUSSION == obj.getmType()) {
				Group discussion = GlobalHolder.getInstance().getGroupById(obj.getmGroupId());
				if (discussion != null) {
					if (mGroupLoading) {
						mGroupLeaveOut.add(discussion);
					} else {
						groupAddNewConversation(discussion);
					}
				} else {
					V2Log.e(TAG, "Can not get discussion group from mGroupHolder! id is :" + obj.getmGroupId());
					List<Group> disGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DISCUSSION);
					boolean isLook = false;
					for (int i = 0; i < disGroup.size(); i++) {
						Group temp = disGroup.get(i);
						if (temp.getGroupID() == obj.getmGroupId()) {
							V2Log.e(TAG, "Search it from mDiscussionBoardGroup ! id is :" + obj.getmGroupId());
							isLook = true;
							if (mGroupLoading) {
								mGroupLeaveOut.add(temp);
							} else {
								groupAddNewConversation(temp);
							}
							break;
						}
					}

					if (!isLook)
						V2Log.e(TAG, "Can not get discussion group :" + obj.getmGroupId());
				}
			}
		}
	}

	private void loadGroups(int groupType) {
		if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
			List<Group> chatGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CROWD);
			if (chatGroup.size() > 0) {
				groupPopulateConversation(chatGroup, false);
			}
		} else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
			List<Group> disGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DISCUSSION);
			if (disGroup.size() > 0) {
				groupPopulateConversation(disGroup, true);
			}
		}
	}

	@Override
	public void receiveMessage(Message msg) {

		switch (msg.what) {
		case QUIT_DISCUSSION_BOARD_DONE:
		case QUIT_CROWD_GROUP_DONE:
			WaitDialogBuilder.dismissDialog();
			JNIResponse res = (JNIResponse) msg.obj;
			if (res.getResult() == JNIResponse.Result.TIME_OUT) {
				Toast.makeText(mContext, getText(R.string.error_time_out), Toast.LENGTH_SHORT).show();
			} else if (res.getResult() == JNIResponse.Result.SUCCESS) {
				Group group = (Group) res.callerObject;
				groupRemoveConversation(group.getGroupID());
				V2Log.d(TAG, "成功收到退出群或讨论组的消息 , id : " + group.getGroupID() + " | name : " + group.getName());
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void initViewAndListener() {
		setComRightStyle(getResources().getString(R.string.conversation_crowd_tab_title) , false);
		mGroupLV = (ListView) findViewById(R.id.ws_group_main_list);
		mGroupLV.setDivider(null);
		mGroupLV.setOnItemClickListener(mGroupLVItemClickListener);
		mGroupLV.setOnItemLongClickListener(mGroupLVItemLongClickListener);
		mGroupLV.setAdapter(mGroupLVA);
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

	}

	private void initPopupWindow() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout popWindow = (RelativeLayout) inflater.inflate(R.layout.pop_up_window_group_list_view, null);
		mPopup = new PopupWindow(popWindow, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopup.setFocusable(true);
		mPopup.setTouchable(true);
		mPopup.setOutsideTouchable(true);
		mGroupQuitPopTV = (TextView) popWindow.findViewById(R.id.pop_up_window_quit_crowd_item);
		mGroupDissolutionPopTV = (TextView) popWindow.findViewById(R.id.pop_up_window_dissolution_crowd_item);
		mGroupQuitDisPopTV = (TextView) popWindow.findViewById(R.id.pop_up_window_quit_discussion);

		mGroupQuitPopTV.setOnClickListener(mPopupWindowGroupItemClickListener);
		mGroupDissolutionPopTV.setOnClickListener(mPopupWindowGroupItemClickListener);
		mGroupQuitDisPopTV.setOnClickListener(mPopupWindowGroupItemClickListener);
	}

	private void showPopupWindow(final View anchor, boolean isContactItem) {
		if (!anchor.isShown()) {
			return;
		}

		if (this.mPopup == null) {
			initPopupWindow();
		}

		if (isContactItem) {
			mGroupQuitPopTV.setVisibility(View.GONE);
			mGroupDissolutionPopTV.setVisibility(View.GONE);
			mGroupQuitDisPopTV.setVisibility(View.GONE);
		} else {
			if (currentClickConversation.getType() == Conversation.TYPE_DISCUSSION) {
				mGroupQuitDisPopTV.setVisibility(View.VISIBLE);
				mGroupQuitPopTV.setVisibility(View.GONE);
				mGroupDissolutionPopTV.setVisibility(View.GONE);
			} else {
				Group crowd = GlobalHolder.getInstance().getGroupById(currentClickConversation.getExtId());
				if (crowd.getOwnerUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
					mGroupDissolutionPopTV.setVisibility(View.VISIBLE);
					mGroupQuitDisPopTV.setVisibility(View.GONE);
					mGroupQuitPopTV.setVisibility(View.GONE);
				} else {
					mGroupQuitPopTV.setVisibility(View.VISIBLE);
					mGroupQuitDisPopTV.setVisibility(View.GONE);
					mGroupDissolutionPopTV.setVisibility(View.GONE);
				}
			}
		}

		if (mPopup.getContentView().getWidth() <= 0) {
			mPopup.getContentView().measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
		}
		int popupWindowWidth = mPopup.getContentView().getMeasuredWidth();
		int popupWindowHeight = mPopup.getContentView().getMeasuredHeight();

		mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		int viewWidth = anchor.getMeasuredWidth();
		int viewHeight = anchor.getMeasuredHeight();
		int offsetX = (viewWidth - popupWindowWidth) / 2;
		int offsetY = (viewHeight + popupWindowHeight);

		int[] location = new int[2];
		anchor.getLocationInWindow(location);
		// if (location[1] <= 0) {
		Rect r = new Rect();
		anchor.getDrawingRect(r);
		Rect r1 = new Rect();
		anchor.getGlobalVisibleRect(r1);
		int offsetXLocation = r1.left + offsetX;
		int offsetYLocation = r1.top - (offsetY / 2);
		mPopup.showAtLocation((View) anchor.getParent(), Gravity.NO_GRAVITY, offsetXLocation, offsetYLocation);
	}

	/**
	 * According populateType to fill the List Data. The data from server!
	 *
	 * @param list
	 */
	private void groupPopulateConversation(List<Group> list, boolean isDiscussion) {
		if (mGroupLoading) {
			return;
		}
		mGroupLoading = true;
		List<ScrollItem> deleteList = new ArrayList<>();
		Iterator<ScrollItem> iterator = mGroupItemList.iterator();
		while (iterator.hasNext()) {
			ScrollItem item = iterator.next();
			Conversation cov = item.cov;
			if (cov.getType() == Conversation.TYPE_GROUP && !isDiscussion) {
				deleteList.add(item);
			} else if (cov.getType() == Conversation.TYPE_DISCUSSION && isDiscussion) {
				deleteList.add(item);
			}
		}

		for (int i = 0; i < deleteList.size(); i++) {
			ScrollItem scrollItem = deleteList.get(i);
			mGroupItemList.remove(scrollItem);
		}
		mGroupLVA.notifyDataSetChanged();

		List<Conversation> tempList = new ArrayList<>();
		for (int i = list.size() - 1; i >= 0; i--) {
			Group g = list.get(i);
			Conversation cov;
			if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
				cov = new CrowdConversation(g);
			} else if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
				cov = new DiscussionConversation(g);
			} else {
				continue;
			}
			tempList.add(cov);
		}
		// 将Conversation转化为ScrollItem
		for (int i = 0; i < tempList.size(); i++) {
			Conversation cov = tempList.get(i);
			if (cov == null) {
				V2Log.e(TAG, "when fillAdapter , get null Conversation , index :" + i);
				continue;
			}

			GroupLayout layout = new GroupLayout(mContext, cov);
			// 需要调用updateGroupContent
			if (cov.getType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
				Group fillGroup = ((CrowdConversation) cov).getGroup();
				if (fillGroup != null) {
					if (i == tempList.size() - 1) {
						layout.updateGroupContent(fillGroup, cov.getReadFlag(), false);
					} else {
						layout.updateGroupContent(fillGroup, cov.getReadFlag(), true);
					}
				}
			}

			ScrollItem newItem = new ScrollItem(cov, layout, true);
			mGroupItemList.add(newItem);
		}

		mGroupLVA.notifyDataSetChanged();
		V2Log.w(TAG, "The ListView already fill over !  , type is CROWD");
		mGroupLoading = false;
		groupCheckLeaveOut();
	}

	private void groupCheckLeaveOut() {
		for (Group temp : mGroupLeaveOut) {
			V2Log.e(TAG, "pick up a leaved crowd group , name is : " + temp.getName());
			groupAddNewConversation(temp);
		}
		mGroupLeaveOut.clear();
	}

	/**
	 * Add a new conversation to current list.
	 * 
	 * @param g
	 */
	private void groupAddNewConversation(Group g) {
		if (g == null) {
			V2Log.e(TAG, "addConversation --> Add new conversation failed ! Given Group is null");
			return;
		}

		Conversation cov;
		ScrollItem currentItem;
		if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
			cov = new CrowdConversation(g);
		} else if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
			cov = new DiscussionConversation(g);
		} else {
			V2Log.e(TAG, "addConversation --> Add new group conversation failed ... " + "the group type is : "
					+ g.getGroupType());
			return;
		}

		V2Log.d(TAG, "addConversation -- Successfully sendFriendToTv a new conversation , type is : " + cov.getType()
				+ " and id is : " + cov.getExtId() + " and name is : " + cov.getName());
		GroupLayout gp = new GroupLayout(mContext, cov);
		gp.updateGroupContent(g, cov.getReadFlag(), false);
		currentItem = new ScrollItem(cov, gp, true);
		mGroupItemList.add(currentItem);
		mGroupLVA.notifyDataSetChanged();
		mGroupLV.post(new Runnable() {

			@Override
			public void run() {
				mGroupLV.setSelection(0);
			}
		});
	}

	/**
	 * Remove conversation from mConvList by id.
	 * 
	 * @param conversationID
	 */
	private void groupRemoveConversation(long conversationID) {
		ScrollItem removed = null;
		Iterator<ScrollItem> iterator = mGroupItemList.iterator();
		while (iterator.hasNext()) {
			ScrollItem temp = iterator.next();
			if (temp.cov.getExtId() == conversationID) {
				removed = temp;
				// clear all system notification
				Notificator.cancelSystemNotification(this, V2GlobalConstants.MESSAGE_NOTIFICATION_ID);
				break;
			}
		}

		if (removed != null) {
			// remove item
			boolean result = mGroupItemList.remove(removed);
			if (!result) {
				ScrollItem[] items = new ScrollItem[mGroupItemList.size()];
				mGroupItemList.toArray(items);
				mGroupItemList.clear();
				for (ScrollItem scrollItem : items) {
					if (removed.cov.getExtId() != scrollItem.cov.getExtId()) {
						mGroupItemList.add(scrollItem);
					}
				}
			}
		}
		mGroupLVA.notifyDataSetChanged();
	}

	class GroupConversationAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGroupItemList.size();
		}

		@Override
		public Object getItem(int position) {
			return mGroupItemList.toArray()[position];
		}

		@Override
		public long getItemId(int position) {
			return ((ScrollItem) mGroupItemList.toArray()[position]).cov.getExtId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ScrollItem item = (ScrollItem) mGroupItemList.toArray()[position];
			GroupLayout view = (GroupLayout) item.gp;
			Conversation cov = item.cov;
			if (cov.getType() == Conversation.TYPE_GROUP) {
				CrowdConversation crowdConversation = (CrowdConversation) item.cov;
				Group crowd = GlobalHolder.getInstance().getGroupById(crowdConversation.getExtId());
				crowdConversation.setGroup(crowd);
				// view.updateGroupContent(crowd,
				// crowdConversation.getReadFlag());
			} else {
				DiscussionConversation disConversation = (DiscussionConversation) item.cov;
				Group dis = GlobalHolder.getInstance().getGroupById(disConversation.getExtId());
				disConversation.setGroup(dis);
			}

			if (position == mGroupItemList.size() - 1) {
				view.update(cov, true, false);
			} else {
				view.update(cov, true, true);
			}
			return view;
		}
	}

	private class GroupLVItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
			if (AlgorithmUtil.isFastClick()) {
				return;
			}

			ScrollItem item = ((ScrollItem) mGroupItemList.toArray()[pos]);
			Conversation cov = item.cov;
			currentClickConversation = cov;
			// 开启会话界面
			Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra("obj", new ConversationNotificationObject(cov.getType(), cov.getExtId()));
			startActivity(i);
		}
	}

	private class PopupWindowGroupItemClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!GlobalHolder.getInstance().isServerConnected()) {
				Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
				return;
			}

			WaitDialogBuilder.showNormalWithHintProgress(mContext);
			Group crowd = GlobalHolder.getInstance().getGroupById(currentClickConversation.getExtId());
			// If group is null, means we have removed this conversaion
			if (crowd != null) {
				if (crowd.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
					chatService.quitCrowd((CrowdGroup) crowd, new HandlerWrap(mHandler, QUIT_CROWD_GROUP_DONE, crowd));
				} else if (crowd.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
					chatService.quitDiscussionBoard((DiscussionGroup) crowd,
							new HandlerWrap(mHandler, QUIT_DISCUSSION_BOARD_DONE, crowd));
				}
			} else
				V2Log.e(TAG, "quit crowd group failed .. id is :" + currentClickConversation.getExtId());
			mPopup.dismiss();
		}
	}

	private class GroupLVItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) {
			currentClickConversation = ((ScrollItem) mGroupItemList.toArray()[pos]).cov;
			showPopupWindow(v, false);
			return true;
		}
	}
}
