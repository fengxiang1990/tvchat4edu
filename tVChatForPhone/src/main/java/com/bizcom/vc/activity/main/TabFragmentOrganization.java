package com.bizcom.vc.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.UserStatusObject;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.V2Log;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabFragmentOrganization extends Fragment {
	public static final String TAG = TabFragmentOrganization.class.getSimpleName();

	private static final int ITEM_TYPE_USER = 0;
	private static final int ITEM_TYPE_TAB = 1;

	private static final int TAB_GROUP = 100;
	private static final int TAB_ORG = 101;
	private static final int TAB_CONTACT = 102;

	private static final int UPDATE_USER_SIGN = 8;

	private Context mContext;

	private View rootView;
	private LocalHandler mHandler = new LocalHandler(this);
	private LocalReceiver receiver = new LocalReceiver();

    private MyAdapter mGroupLVA;
	private List<ListItem> mItemList = new ArrayList<>();

	private BitmapManager.BitmapChangedListener mUserAvatarChangedListener = new UserAvatarChangedListener();
	private OnTouchListener mGroupTabItemTouchListener = new GroupTabItemTouchListener();
	private OnItemClickListener mGroupLVItemClickListener = new GroupLVItemClickListener();

	private IntentFilter intentFilter;
	private boolean mContactLoading;

	private Drawable mUserOnlineStatusDrawable;
	private Drawable mUserLeaveStatusDrawable;
	private Drawable mUserBusyStatusDrawable;
	private Drawable mUserDisturbStatusDrawable;
	private Drawable mUserPhoneStatusDrawable;

	private Drawable mNormalPressDrawable;
	private Drawable mNormalDrawable;

	public boolean mIsStopUpdate = false;
	private boolean mIsNeedNewData = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		V2Log.i(TAG, "TabFragmentOrganization onCreate()");
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		initReceiver();
		BitmapManager.getInstance().registerBitmapChangedListener(mUserAvatarChangedListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		V2Log.i(TAG, "TabFragmentOrganization onCreateView()");
		if (rootView != null) {
			return rootView;
		}

		Resources resources = mContext.getResources();
		mUserOnlineStatusDrawable = resources.getDrawable(R.drawable.online);
		mUserLeaveStatusDrawable = resources.getDrawable(R.drawable.leave);
		mUserBusyStatusDrawable = resources.getDrawable(R.drawable.busy);
		mUserDisturbStatusDrawable = resources.getDrawable(R.drawable.do_not_distrub);
		mUserPhoneStatusDrawable = resources.getDrawable(R.drawable.cell_phone_user);

		mNormalPressDrawable = resources.getDrawable(R.drawable.base_list_selector_pressed);
		mNormalDrawable = resources.getDrawable(R.color.common_activity_top_backgroud);

		rootView = inflater.inflate(R.layout.tab_fragment_main, container, false);
        ListView mContactsMLV = (ListView) rootView.findViewById(R.id.ws_main_fragment_main_list);

		mGroupLVA = new MyAdapter(mContext);
		mGroupLVA.AddType(R.layout.multilevel_adapter_item_user);
		mGroupLVA.AddType(R.layout.tab_fragment_main_tabitem);

		addNewItem(new ListItem(ITEM_TYPE_TAB, getGroupTabItem(), TAB_GROUP));
		addNewItem(new ListItem(ITEM_TYPE_TAB, getOrgTabItem(), TAB_ORG));
		addNewItem(new ListItem(ITEM_TYPE_TAB, getContactTabItem(), TAB_CONTACT));

		mContactsMLV.setAdapter(mGroupLVA);
		mContactsMLV.setDivider(null);
		mContactsMLV.setOnItemClickListener(mGroupLVItemClickListener);
		return rootView;
	}

	@Override
	public void onStart() {
		V2Log.i(TAG, "TabFragmentOrganization onStart()");
		super.onStart();
	}

	@Override
	public void onStop() {
		V2Log.i(TAG, "TabFragmentOrganization onStop()");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		V2Log.i(TAG, "TabFragmentOrganization onDestroyView()");
		super.onDestroyView();
		((ViewGroup) rootView.getParent()).removeView(rootView);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		V2Log.i(TAG, "TabFragmentOrganization onDestroy()");
		intentFilter = null;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}

		try {
			if (receiver != null)
				getActivity().unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BitmapManager.getInstance().unRegisterBitmapChangedListener(mUserAvatarChangedListener);
	}

	private void initReceiver() {
		if (intentFilter == null) {
			intentFilter = new IntentFilter();
			intentFilter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
			intentFilter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
			intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
			intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
			intentFilter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
			intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
			intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
			intentFilter.addAction(JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION);
			intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
			intentFilter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);
			intentFilter.addAction(PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP);
		}
		getActivity().registerReceiver(receiver, intentFilter);
	}

	public void receiveMessage(Message msg) {
		switch (msg.what) {
		case UPDATE_USER_SIGN:
			mIsNeedNewData = true;
			mGroupLVA.notifyDataSetChanged();
			mIsNeedNewData = false;
			break;
		}
	}

	private void fillContactsMultilevelGroup() {
		if (mContactLoading) {
			return;
		}
		new ContactsAsyncTaskLoader().execute();
	}

	private String updateContactsNumber() {
		int count = mItemList.size();
		int onlineCount = 0;
		for (ListItem temp : mItemList) {
			if (temp.mType == ITEM_TYPE_TAB) {
				count--;
			} else {
				User user = GlobalHolder.getInstance().getUser(temp.mUserID);
				User.Status status = user.getmStatus();
				if ((status == User.Status.ONLINE || status == User.Status.BUSY || status == User.Status.DO_NOT_DISTURB
						|| status == User.Status.LEAVE)) {
					onlineCount++;
				}
			}
		}
		return (String) TextUtils.concat(String.valueOf(onlineCount) , " / " , String.valueOf(count));
	}

	private void updateUserStatus(TextView mNameTV, CustomAvatarImageView mUserIcon, ImageView mStatusIV, User u) {
		User.DeviceType dType = u.getDeviceType();
		User.Status st = u.getmStatus();
		if (dType == User.DeviceType.CELL_PHONE) {
			mStatusIV.setImageDrawable(mUserPhoneStatusDrawable);
		} else {
			switch (st) {
			case ONLINE:
				mStatusIV.setImageDrawable(mUserOnlineStatusDrawable);
				break;
			case LEAVE:
				mStatusIV.setImageDrawable(mUserLeaveStatusDrawable);
				break;
			case BUSY:
				mStatusIV.setImageDrawable(mUserBusyStatusDrawable);
				break;
			case DO_NOT_DISTURB:
				mStatusIV.setImageDrawable(mUserDisturbStatusDrawable);
				break;
			default:
				break;
			}
		}

		if (st == User.Status.OFFLINE || st == User.Status.HIDDEN) {
			mStatusIV.setVisibility(View.GONE);
			mNameTV.setTextColor(mContext.getResources().getColor(R.color.contacts_user_view_item_color_offline));
		} else {
			mStatusIV.setVisibility(View.VISIBLE);
			mNameTV.setTextColor(mContext.getResources().getColor(R.color.conf_create_contacts_user_view_item_color));

		}
		mUserIcon.setImageBitmap(u.getOrgAvatarBitmap());
		mUserIcon.mUserStatus = st;
		mUserIcon.invalidate();
	}

	private Drawable getUserStatusDrawable(User u) {
		User.DeviceType dType = u.getDeviceType();
		User.Status st = u.getmStatus();
		if (dType == User.DeviceType.CELL_PHONE) {
			return mUserPhoneStatusDrawable;
		} else {
			switch (st) {
			case ONLINE:
				return mUserOnlineStatusDrawable;
			case LEAVE:
				return mUserLeaveStatusDrawable;
			case BUSY:
				return mUserBusyStatusDrawable;
			case DO_NOT_DISTURB:
				return mUserDisturbStatusDrawable;
			default:
			}
		}
		return null;
	}

	public SparseArray<Object> getGroupTabItem() {
		SparseArray<Object> mMap = new SparseArray<>();
		mMap.put(R.id.ws_main_fragment_main_tabitem_arrow, R.drawable.arrow_right_gray);
		mMap.put(R.id.ws_main_fragment_main_tabitem_name,
				getResources().getString(R.string.conversation_crowd_tab_title));
		mMap.put(R.id.ws_main_fragment_main_tabitem_number, 0);
		mMap.put(R.id.ws_main_fragment_main_tabitem_divider1, null);
		mMap.put(R.id.ws_main_fragment_main_tabitem_divider2, null);
		mMap.put(R.id.ws_main_fragment_main_tabitem_ly, mUserPhoneStatusDrawable);
		return mMap;
	}

	public SparseArray<Object> getOrgTabItem() {
		SparseArray<Object> mMap = new SparseArray<>();
		mMap.put(R.id.ws_main_fragment_main_tabitem_arrow, R.drawable.arrow_right_gray);
		mMap.put(R.id.ws_main_fragment_main_tabitem_name,
				getResources().getString(R.string.conversation_org_tab_title));
		mMap.put(R.id.ws_main_fragment_main_tabitem_number, 0);
		mMap.put(R.id.ws_main_fragment_main_tabitem_divider1, null);
		mMap.put(R.id.ws_main_fragment_main_tabitem_divider2, null);
		mMap.put(R.id.ws_main_fragment_main_tabitem_ly, mUserPhoneStatusDrawable);
		return mMap;
	}

	public SparseArray<Object> getContactTabItem() {
		SparseArray<Object> mMap = new SparseArray<>();
		mMap.put(R.id.ws_main_fragment_main_tabitem_arrow, R.drawable.arrow_right_gray);
		mMap.put(R.id.ws_main_fragment_main_tabitem_name,
				getResources().getString(R.string.contacts_default_group_name));
		mMap.put(R.id.ws_main_fragment_main_tabitem_number, 0);
		mMap.put(R.id.ws_main_fragment_main_tabitem_divider1, null);
		mMap.put(R.id.ws_main_fragment_main_tabitem_divider2, null);
		mMap.put(R.id.ws_main_fragment_main_tabitem_ly, mUserPhoneStatusDrawable);
		return mMap;
	}

	public SparseArray<Object> getUserItem(User user) {
		SparseArray<Object> map1 = new SparseArray<>();
		return getUserItem(map1, user);
	}

	public SparseArray<Object> getUserItem(SparseArray<Object> mMap, User user) {
		mMap.put(R.id.ws_common_avatar, user.getOrgAvatarBitmap());
		mMap.put(R.id.user_status_iv, getUserStatusDrawable(user));
		mMap.put(R.id.user_name, user.getDisplayName());
		mMap.put(R.id.user_signature, user.getSignature());
		mMap.put(R.id.user_name, user.getDisplayName());
		mMap.put(R.id.user_buttom_divider, null);
		return mMap;
	}

	public void setIsUpdate(boolean isUpdate) {
		if (isUpdate) {
			mIsStopUpdate = false;
			Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
		} else {
			mIsStopUpdate = true;
		}
	}

	public void GroupTabItemUpEvent(int action) {
		if (MotionEvent.ACTION_UP == action) {
			if (currentClickTabView != null) {
				long tag = (long) currentClickTabView.getTag();
				if (tag != TAB_CONTACT) {
					currentClickTabView.setBackgroundDrawable(mNormalDrawable);
				}
				currentClickTabView = null;
			}
		}
	}

	public void addNewItem(ListItem item) {
		boolean isAdd = true;
		for (int i = 0; i < mItemList.size(); i++) {
			if (mItemList.get(i).mUserID == item.mUserID) {
				isAdd = false;
				break;
			}
		}

		if (isAdd) {
			mItemList.add(item);
		}
	}

	private static class LocalHandler extends Handler {
		private final WeakReference<TabFragmentOrganization> mActivity;

		public LocalHandler(TabFragmentOrganization fragment) {
			mActivity = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mActivity.get() == null) {
				return;
			}
			mActivity.get().receiveMessage(msg);
		}
	}

	private class LocalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION)) {
				int groupType = intent.getIntExtra("gtype", -1);
				if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
					fillContactsMultilevelGroup();
				} else if(groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT){
					Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
				}
			} else if (JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION.equals(intent.getAction())) {
				UserStatusObject uso = (UserStatusObject) intent.getExtras().get("status");
				if (uso != null) {
					boolean friend = GlobalHolder.getInstance().isFriend(uso.getUid());
					if (friend) {
						Collections.sort(mItemList);
						Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
					}
				}
			} else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {
				int groupType = intent.getIntExtra("gtype", -1);
				if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
					Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
				}
			} else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
				GroupUserObject obj = (GroupUserObject) intent.getExtras().get("obj");
				if (obj == null) {
					V2Log.e(TAG,
							"JNI_BROADCAST_GROUP_USER_REMOVED --> Update Conversation failed that the user removed ... given GroupUserObject is null");
					return;
				}

				if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT
                        || obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
					for (int i = 0; i < mItemList.size(); i++) {
						ListItem temp = mItemList.get(i);
						if (temp.mType == ITEM_TYPE_USER && temp.mUserID == obj.getmUserId()) {
							mItemList.remove(i);
							break;
						}
					}
				}
                Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_ADDED.equals(intent.getAction())) {
				V2Log.d(TAG, "JNI_BROADCAST_GROUP_USER_ADDED --> The New User Coming !");
				GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
				if (guo == null) {
					V2Log.e(TAG, "JNI_BROADCAST_GROUP_USER_ADDED --> Add New User Failed ! Because"
							+ "Given GroupUserObject is null!");
					return;
				}

				if (guo.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
					User user = GlobalHolder.getInstance().getUser(guo.getmUserId());
					addNewItem(new ListItem(ITEM_TYPE_USER, getUserItem(user), guo.getmUserId()));
					Collections.sort(mItemList);
					Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
				}
				// Contacts group is updated
			} else if (JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO.equals(intent.getAction())) {
				long uid = intent.getLongExtra("uid", -1);
				if (uid == -1)
					return;
				Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
			} else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
				Long uid = intent.getLongExtra("modifiedUser", -1);
				if (uid == -1) {
					return;
				}

				Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
			} else if (PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP.equals(intent.getAction())) {
				Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
			}
		}
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<Integer> TypeList = new ArrayList<>();

		public void AddType(int mResource) {
			TypeList.add(mResource);
		}

		public MyAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getItemViewType(int position) {
			return mItemList.get(position).mType;
		}

		@Override
		public int getViewTypeCount() {
			if (TypeList.size() == 0)
				return 1;
			else
				return TypeList.size();
		}

		public int getCount() {
			return mItemList.size();
		}

		public ListItem getItem(int position) {
			return mItemList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (mIsStopUpdate && convertView != null) {
				return convertView;
			}
			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				ListItem item = getItem(position);
				convertView = mInflater.inflate(TypeList.get(type), null);

				for (int i = 0; i < item.mMap.size(); i++) {
					int id = item.mMap.keyAt(i);
					Object obj = convertView.findViewById(id);
					if (obj != null) {
						if (obj.getClass().equals(RelativeLayout.class)) {
							View temp = (View) obj;
							temp.setTag(item.mUserID);
							temp.setOnTouchListener(mGroupTabItemTouchListener);
						}
						holder.List_Object.add(obj);
						holder.List_id.add(id);
						holder.List_Map_Onject.append(id, obj);
					}
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ListItem listItem = mItemList.get(position);
			if (type == ITEM_TYPE_USER) {
				// if(mIsNeedNewData){
				User user = GlobalHolder.getInstance().getUser(listItem.mUserID);
				listItem.updateData(user);
				updateUserStatus((TextView) holder.List_Map_Onject.get(R.id.user_name),
						(CustomAvatarImageView) holder.List_Map_Onject.get(R.id.ws_common_avatar),
						(ImageView) holder.List_Map_Onject.get(R.id.user_status_iv), user);
				// }
                View mButtomDivider = (View) holder.List_Map_Onject.get(R.id.user_buttom_divider);
                if(position != mItemList.size() - 1){
                    mButtomDivider.setVisibility(View.VISIBLE);
                } else {
                    mButtomDivider.setVisibility(View.GONE);
                }
				holder.SetValue(listItem);
			} else {
				holder.SetValue(listItem);
				TextView name = (TextView) holder.List_Map_Onject.get(R.id.ws_main_fragment_main_tabitem_name);
				TextView number = (TextView) holder.List_Map_Onject.get(R.id.ws_main_fragment_main_tabitem_number);
				View arrow = (View) holder.List_Map_Onject.get(R.id.ws_main_fragment_main_tabitem_arrow);
				View divider1 = (View) holder.List_Map_Onject.get(R.id.ws_main_fragment_main_tabitem_divider1);
				View divider2 = (View) holder.List_Map_Onject.get(R.id.ws_main_fragment_main_tabitem_divider2);
				if (listItem.mUserID == TAB_GROUP) {
					name.setTextColor(getResources().getColor(R.color.common_item_text_color_black));
					number.setVisibility(View.GONE);
					arrow.setVisibility(View.VISIBLE);
					divider1.setVisibility(View.VISIBLE);
					divider2.setVisibility(View.VISIBLE);
				} else if (listItem.mUserID == TAB_ORG) {
					name.setTextColor(getResources().getColor(R.color.common_item_text_color_black));
					number.setVisibility(View.GONE);
					List<Group> orgList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DEPARTMENT);
					if (orgList != null && orgList.size() > 0) {
						Group mOrgRootGroup = orgList.get(0);
						int userCount = mOrgRootGroup.getUserCount();
						int onlineUserCount = mOrgRootGroup.getOnlineUserCount();
                        String personStatus = (String) TextUtils.concat(String.valueOf(onlineUserCount) , " / " , String.valueOf(userCount));
						number.setText(personStatus);
					}
					arrow.setVisibility(View.VISIBLE);
					divider1.setVisibility(View.VISIBLE);
					divider2.setVisibility(View.VISIBLE);
				} else if (listItem.mUserID == TAB_CONTACT) {
					name.setTextColor(getResources().getColor(R.color.common_item_text_color_blue));
					String contactsNum = updateContactsNumber();
					number.setText(contactsNum);
					number.setVisibility(View.VISIBLE);
					arrow.setVisibility(View.GONE);
					divider1.setVisibility(View.GONE);
					divider2.setVisibility(View.GONE);
				}
			}
			return convertView;
		}
	}

	class ViewHolder {
		ArrayList<Object> List_Object = new ArrayList<>();
		ArrayList<Integer> List_id = new ArrayList<>();
		SparseArray<Object> List_Map_Onject = new SparseArray<>();

		public boolean SetValue(ListItem item) {
			int i = 0;
			Object oV;
			for (Object obj : List_Object) {
				int id = List_id.get(i);
				oV = item.mMap.get(id);

				if (obj.getClass().equals(TextView.class)) {
					TextView temp = (TextView) obj;
					if (oV == null) {
						temp.setText("");
					} else {
						temp.setText(oV.toString());
					}
				}

				// if (obj.getClass().equals(ImageView.class)) {
				// ImageView temp = (ImageView) obj;
				// if (oV.getClass().equals(Integer.class)) {
				// temp.setImageResource((Integer) oV);
				// } else if (oV.getClass().equals(Bitmap.class)) {
				// temp.setImageBitmap((Bitmap) oV);
				// } else if (oV.getClass().equals(Drawable.class)) {
				// temp.setImageDrawable((Drawable) oV);
				// }
				// }

				// if (obj.getClass().equals(ImageButton.class)) {
				// if (oV.getClass().equals(Integer.class)) {
				// ((ImageButton) obj).setImageResource((Integer) oV);
				// }
				//
				// if (oV.getClass().equals(View.OnClickListener.class)) {
				// ((ImageButton) obj).setOnClickListener((View.OnClickListener)
				// oV);
				// }
				// }
				i++;
			}
			return false;
		}
	}

	class ListItem implements Comparable<ListItem> {
		/** 类型 */
		public int mType;
		/** 键值对应Map */
		public SparseArray<Object> mMap;
		public long mUserID;

		public ListItem(int type, SparseArray<Object> map, long mUserID) {
			mType = type;
			mMap = map;
			this.mUserID = mUserID;
		}

		public void updateData(User user) {
			getUserItem(mMap, user);
		}

		@Override
		public int compareTo(ListItem another) {
			if (mType == ITEM_TYPE_TAB && another.mType == ITEM_TYPE_TAB) {
				return 0;
			} else if (mType == ITEM_TYPE_TAB) {
				return -1;
			} else if (another.mType == ITEM_TYPE_TAB) {
				return 1;
			} else {
				User local = GlobalHolder.getInstance().getUser(mUserID);
				User remote = GlobalHolder.getInstance().getUser(another.mUserID);
				return local.compareTo(remote);
			}
		}
	}

	private class UserAvatarChangedListener implements BitmapManager.BitmapChangedListener {

		@Override
		public void notifyAvatarChanged(User user, Bitmap bm) {
			Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
		}
	}

	private View currentClickTabView;

	private class GroupTabItemTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			currentClickTabView = v;
			long tag = (long) v.getTag();
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (tag != TAB_CONTACT) {
					v.setBackgroundDrawable(mNormalPressDrawable);
				}
			}
			return false;
		}
	}

	private class GroupLVItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
			if (AlgorithmUtil.isFastClick()) {
				return;
			}

			ListItem listItem = mItemList.get(pos);
			if (listItem.mType == ITEM_TYPE_USER) {
				// 开启会话界面
				Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				i.putExtra("obj",
						new ConversationNotificationObject(V2GlobalConstants.GROUP_TYPE_USER, listItem.mUserID));
				startActivity(i);
			} else if (listItem.mUserID == TAB_GROUP) {
				Intent i = new Intent();
				i.setClass(mContext, GroupListActivity.class);
				startActivity(i);
			} else if (listItem.mUserID == TAB_ORG) {
				Intent i = new Intent();
				i.setClass(mContext, OrgListActivity.class);
				startActivity(i);
			}
		}
	}

	private class ContactsAsyncTaskLoader extends AsyncTask<Void, Void, ArrayList<ListItem>> {

		@Override
		protected ArrayList<ListItem> doInBackground(Void... params) {
			if (mContactLoading) {
				return null;
			}

			mContactLoading = true;
			ArrayList<ListItem> temp = null;
            List<Group> mContactGroupList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
			if (mContactGroupList != null && mContactGroupList.size() > 0) {
				temp = new ArrayList<>();
				List<User> users = mContactGroupList.get(0).getUsers();
				for (int i = 0; i < users.size(); i++) {
					User user = users.get(i);
					ListItem it = new ListItem(ITEM_TYPE_USER, getUserItem(user), user.getmUserId());
					temp.add(it);
				}
			}
			return temp;
		}

		@Override
		protected void onPostExecute(ArrayList<ListItem> result) {
			if (result != null && result.size() > 0) {
				mItemList.clear();
				addNewItem(new ListItem(ITEM_TYPE_TAB, getGroupTabItem(), TAB_GROUP));
				addNewItem(new ListItem(ITEM_TYPE_TAB, getOrgTabItem(), TAB_ORG));
				addNewItem(new ListItem(ITEM_TYPE_TAB, getContactTabItem(), TAB_CONTACT));
				for (int i = 0; i < result.size(); i++) {
					addNewItem(result.get(i));
				}
			}
			Collections.sort(mItemList);
			Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
			mContactLoading = false;
		}
	}
}
