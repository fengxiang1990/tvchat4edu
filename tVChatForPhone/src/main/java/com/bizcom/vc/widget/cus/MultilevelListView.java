package com.bizcom.vc.widget.cus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bizcom.util.AnimationController;
import com.bizcom.util.SearchUtils;
import com.bizcom.util.V2Log;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MultilevelListView extends ListView {
	private static final String TAG = MultilevelListView.class.getSimpleName();

	public static final int MULTILEVEL_TYPE_CREATE = 0x0001;
	public static final int MULTILEVEL_TYPE_ORG = 0x0002;
	public static final int MULTILEVEL_TYPE_CONTACT = 0x0003;
	public static final int MULTILEVEL_TYPE_CONF = 0x0004;

	public static final int MULTILEVEL_ITEM_TYPE_GROUP = 1;
	public static final int MULTILEVEL_ITEM_TYPE_USER = 2;

	private MultilevelListViewAdapter adapter;
	private MultilevelListViewListener mListener;
	private OnItemClickListener mMyOnItemClickListener = new MyOnItemClickListener();
	private OnItemLongClickListener mMyOnItemLongClickListener = new MyOnItemLongClickListener();
	private OnScrollListener mMyOnScrollChangeListener = new MyOnScrollChangeListener();

	private SearchedFilter searchedFilter;

	private Context mContext;
	// This list hold all group data and use to search
	private List<Group> mGroupList;
	private List<ItemData> mBaseList;
	private List<ItemData> mShowItemDataList;
	private LongSparseArray<ItemData> mItemMap;
	private LongSparseArray<Set<ItemData>> mUserItemListMap;

	private List<List<ItemData>> mLastSearchResults = new ArrayList<>();
	private List<User> mSearchResults;
	private List<Long> mDeletedUsers;
	private SparseArray<Long> mExpandList;
	// 提供一个从User转换成UserItemData的缓存
	private LongSparseArray<LongSparseArray<ItemData>> mUserItemDataMapOfMap;

	// Flag to indicate show check box or not
	private boolean mCBFlag;
	// Flag to indicate doesn't show current logged in User
	private boolean mIgnoreCurrentUser;
	// Use to indicate current data set is filtered or not
	// 只有listView要显示的数据，可以是GroupItemData或是UserItemData
	private boolean mIsInFilter;
	private boolean mIsNeedShowSearch = true;
	private int multilevelType;
	private int mCurrentScrollState = 0;
	private SearchUtils mSearchUtils;

	private long currentClickGroup = -5;

	public MultilevelListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MultilevelListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MultilevelListView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mSearchUtils = new SearchUtils();
		mItemMap = new LongSparseArray<>();
		mUserItemListMap = new LongSparseArray<>();
		mUserItemDataMapOfMap = new LongSparseArray<>();
		mGroupList = new ArrayList<>();
		mBaseList = new ArrayList<>();
		mShowItemDataList = new ArrayList<>();
		adapter = new MultilevelListViewAdapter();
		mDeletedUsers = new ArrayList<>();
		mExpandList = new SparseArray<>();
		searchedFilter = new SearchedFilter();
		mContext = this.getContext();
		setAdapter(adapter);
		setOnItemClickListener(mMyOnItemClickListener);
		setOnItemLongClickListener(mMyOnItemLongClickListener);
		setOnScrollListener(mMyOnScrollChangeListener);
	}

	public void initCreateMode() {
		setShowedCheckedBox(true);
		setTextFilterEnabled(true);
		setIgnoreCurrentUser(true);
	}

	public void setGroupList(List<Group> list) {
		mGroupList.clear();
		mGroupList.addAll(list);
		mBaseList.clear();
		mShowItemDataList.clear();
		for (int i = 0; i < list.size(); i++) {
			mBaseList.add(getItem(list.get(i)));
		}
		mShowItemDataList.addAll(mBaseList);
		updateAdapter();
	}

	/**
	 * 适用于一个根组织结构(现在好友分组前要加一个tab页，所有分组作为该tab页的孩子存在。所以也用这个)
	 * 
	 * @param rootGroup
	 */
	public void setOrgGroupList(Group rootGroup) {
		if (mBaseList == null) {
			mBaseList = new ArrayList<>();
		}

		if (mBaseList.size() > 0) {
			// 记录当前组织展开状态
			GroupItemData mContactsRootItem = (GroupItemData) mBaseList.get(0);
			if (mContactsRootItem.isExpaned()) {
				mExpandList.put((int) mContactsRootItem.getId(), (long) getGroupItemPos(mContactsRootItem));
			}
			Group contactRootItem = mContactsRootItem.getGroup();
			List<Group> mOldChildGroup = contactRootItem.getChildGroup();
			for (int i = 0; i < mOldChildGroup.size(); i++) {
				ItemData itemData = getItem(mOldChildGroup.get(i));
				if (itemData.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
					GroupItemData item = (GroupItemData) itemData;
					if (item.isExpaned() && item.getId() != -1) {
						mExpandList.put((int) item.getId(), (long) getGroupItemPos(item));
					}
				}
			}
		}
		// 清空旧数据
		clearAllNotDelete();
		GroupItemData item = (GroupItemData) getItem(rootGroup);
		mBaseList.add(item);
		mGroupList.add(rootGroup);
		Long rootVal = mExpandList.get((int) item.getId());
		mShowItemDataList.add(item);
		if (rootVal != null) {
			item.setExpaned(true);
			expand(item, 0, false);
			// 重新刷新数据的时候,去还原之前已经展开的组织状态
			List<Group> mNewChildGroup = rootGroup.getChildGroup();
			for (int i = 0; i < mNewChildGroup.size(); i++) {
				ItemData itemData = getItem(mNewChildGroup.get(i));
				if (itemData.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
					GroupItemData tempGroupItem = (GroupItemData) itemData;
					long id = tempGroupItem.getId();
					Long value = mExpandList.get((int) id);
					if (value != null) {
						long pos = value;
						tempGroupItem.setExpaned(true);
						expand(tempGroupItem, (int) pos, false);
					}
				}
			}
		}
		mExpandList.clear();
	}

	public void setShowedCheckedBox(boolean flag) {
		mCBFlag = flag;
	}

	public void setIgnoreCurrentUser(boolean flag) {
		this.mIgnoreCurrentUser = flag;
	}

	public void setListener(MultilevelListViewListener listener) {
		this.mListener = listener;
	}

	public MultilevelListViewListener getListener() {
		return mListener;
	}

	public List<Group> getGroupList() {
		return mGroupList;
	}

	/**
	 * Update user's checked status of item
	 * 
	 * @param u
	 * @param flag
	 */
	public void updateCheckItem(User u, boolean flag) {
		if (u == null) {
			throw new NullPointerException("user is null");
		}

		updateCheckItemWithoutNotification(u, flag);
		updateAdapter();
	}

	/**
	 * Update all group's checked status of item
	 * 
	 * @param group
	 * @param flag
	 */
	public void updateCheckItem(Group group, boolean flag) {
		if (group == null) {
			throw new NullPointerException("Group is null");
		}
		updateCheckItemWithoutNotification(group, flag);
		GroupItemData targetItem = (GroupItemData) getItem(group);
		for (Group temp : mGroupList) {
			checkBelongGroupAllChecked(temp, temp.getUsers());
			if (!flag) {
				GroupItemData tempItem = (GroupItemData) getItem(temp);
				if (tempItem.getLevel() < targetItem.getLevel() && temp.getChildGroup().contains(group)) {
					tempItem.setChecked(false);
				}
			}
		}
		updateAdapter();
	}

	public void updateAllGroupItemCheck(Group group) {
		List<Group> childGroup = group.getChildGroup();
		if (childGroup.size() > 0) {
			for (int i = 0; i < childGroup.size(); i++) {
				updateAllGroupItemCheck(childGroup.get(i));
			}
		}
		updateCheckItemWithoutNotification(group, false);
		updateAdapter();
	}

	/**
	 * Update user's checked status of item
	 * 
	 * @param user
	 * @param flag
	 */
	public void updateUserItemcCheck(List<User> user, boolean flag) {
		for (int i = 0; i < user.size(); i++) {
			updateCheckItem(user.get(i), false);
		}

		for (Group group : mGroupList) {
			checkBelongGroupAllChecked(group, user);
		}
		updateAdapter();
	}

	/**
	 * if all users were checek in a group , the group item checkBox should was
	 * checked..
	 * 
	 * @param group
	 * @param list
	 * @return
	 */
	public ItemData checkBelongGroupAllChecked(Group group, List<User> list) {
		int count = 0;
		long loginUseID = GlobalHolder.getInstance().getCurrentUserId();
		for (User u : list) {
			if (u.getmUserId() == loginUseID)
				count = count + 1;
			else {
				ItemData item = getItem(group, u);
				if (item.isChecked()) {
					count = count + 1;
				}
			}
		}

		List<Group> childGroup = group.getChildGroup();
		for (Group child : childGroup) {
			ItemData childGroupItem = checkBelongGroupAllChecked(child, child.getUsers());
			if (childGroupItem != null && childGroupItem.isChecked()) {
				count = count + 1;
			}
		}

		ItemData item = mItemMap.get(group.getGroupID());
		if (item != null) {
			int allCount = list.size() + group.getChildGroup().size();
			if (count == allCount && allCount != 0)
				item.setChecked(true);
			else
				item.setChecked(false);
		}
		updateAdapter();
		return item;
	}

	/**
	 * Remote list view item according user
	 * 
	 * @param user
	 */
	public void removeItem(User user) {
		if (user == null) {
			return;
		}

		for (int i = 0; i < mShowItemDataList.size(); i++) {
			ItemData item = mShowItemDataList.get(i);
			if (item.getId() == user.getmUserId()) {
				removeItem(i);
				mDeletedUsers.add(user.getmUserId());
				i--;
			}

			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				GroupItemData groupItem = (GroupItemData) item;
				groupItem.mGroup.removeUserFromGroup(user);
			}
		}

		// 从所用用户列表中删除该user
		mUserItemListMap.remove(user.getmUserId());
		// 从该user所属的组中删除该user
		Set<Group> gSet = user.getBelongsGroup();
		for (Group g : gSet) {
			LongSparseArray<ItemData> userGroup = mUserItemDataMapOfMap.get(g.getGroupID());
			if (userGroup != null) {
				userGroup.remove(user.getmUserId());
			}
		}

		// 更新listView的显示
		updateAdapter();
	}

	/**
	 * Add new user to group
	 * 
	 * @param group
	 * @param user
	 */
	public void addUser(Group group, User user) {
		if (group == null || user == null) {
			V2Log.e(" incorrect group:" + group + " or user: " + user);
			return;
		}

		if (mDeletedUsers.contains(user.getmUserId()))
			mDeletedUsers.remove(user.getmUserId());

		// FIXME .以下代码添加有时会出错
		// for (int i = 0; i < mShowItemDataList.size(); i++) {
		// ItemData item = mShowItemDataList.get(i);
		// Object obj = item.getObject();
		// if (obj instanceof Group && ((Group) obj).getGroupType() ==
		// group.getGroupType()
		// && ((Group) obj).getmGId() == group.getmGId()) {
		// // obj == group 不能用地址比较，老是出错。
		// GroupItemData temp = (GroupItemData) item;
		// if (temp.isExpaned()) {
		// // Calculate group end position
		// int startPos = calculateAddGroupStartIndex(group);
		// int endPos = calculateAddGroupEndIndex(group, startPos);
		//
		// int pos = calculateIndex(startPos, endPos, user, user.getmStatus());
		// // 计算出的位置pos，是指的是被添加的item与该pos的item对比之后break的结果
		// int replaceItem = pos;
		// Log.i(TAG, "组名 = " + group.getName() + " 组开始位置 = " + startPos + "
		// ，组结束位置 = " + endPos + " 计算位置 = "
		// + pos);
		// if (pos != -1) {
		// ItemData userItem = this.getItem(group, user);
		// User insertUser = ((User) userItem.getObject());
		// insertUser.updateStatus(user.getmStatus());
		//
		// // 当只有一个默认好友分组，并且分组中没人成员，则第一次添加好友，会出现角标越界
		// if (replaceItem < mShowItemDataList.size()) {
		// ItemData itemData = mShowItemDataList.get(replaceItem);
		// if (itemData instanceof UserItemData) {
		// User replacedUser = ((User) itemData.getObject());
		// if (group.getUsers().contains(replacedUser)) {
		// int result = replacedUser.compareTo(insertUser);
		// if (result < 0)
		// pos++;
		// }
		// }
		// }
		//
		// Log.i(TAG, "组名 = " + group.getName() + " 组开始位置 = " + startPos + "
		// ，组结束位置 = " + endPos
		// + " ,插入位置 = " + pos);
		//
		// if (pos >= mShowItemDataList.size()) {
		// mShowItemDataList.sendFriendToTv(userItem);
		// } else {
		// if (pos == 0 && mShowItemDataList.size() == 1)
		// mShowItemDataList.sendFriendToTv(pos + 1, userItem);
		// else
		// mShowItemDataList.sendFriendToTv(pos, userItem);
		// }
		// break;
		// }
		// }
		// }
		// }
		updateMulListView();
	}

	private void updateMulListView() {
		if (mShowItemDataList.size() > 0) {
			GroupItemData root = (GroupItemData) mShowItemDataList.get(0);
			if (root.isExpaned()) {
				SparseIntArray expandList = new SparseIntArray();
				for (int i = 0; i < mShowItemDataList.size(); i++) {
					ItemData item = mShowItemDataList.get(i);
					if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
						GroupItemData temp = (GroupItemData) item;
						if (temp.isExpaned()) {
							expandList.append((int) temp.getId(), i);
						}
					}
				}

				collapse(root, 0);
				for (int i = 0; i < expandList.size(); i++) {
					Long groupID = (long) expandList.keyAt(i);
					ItemData itemData = mItemMap.get(groupID);
					GroupItemData temp = (GroupItemData) itemData;
					temp.setExpaned(true);
				}
				expand(root, 0);
			}
			updateAdapter();
		}
	}

	/**
	 * Update User online status and update user item position
	 * 
	 * @param user
	 * @param us
	 */
	public void updateUserStatus(User user, User.Status us) {
		if (mDeletedUsers.contains(user.getmUserId()))
			return;

		// boolean sort = false;
		// FIXME 用户状态刷新位置计算问题
		// for (int i = 0; i < mShowItemDataList.size(); i++) {
		// ItemData item = mShowItemDataList.get(i);
		// if (item instanceof GroupItemData && ((GroupItemData)
		// item).isExpaned()) {
		// Group temp = ((GroupItemData) item).mGroup;
		// Group belong = temp.findUser(user);
		// if (belong != null) {
		// int start = calculateGroupStartIndex(temp);
		// int end = calculateGroupEnd(temp, i);
		// int pos = updateUserPosition(((GroupItemData) item), start, end,
		// user, us);
		// V2Log.d(TAG,
		// " updateUserStatus --> user id : " + user.getmUserId() + " | user
		// name : "
		// + user.getDisplayName() + " | sendFriendToTv pos : " + pos + " | state is : " +
		// us.name()
		// + " | start : " + start + " | end : " + end);
		// }
		// }
		// }

		updateMulListView();
	}

	public void updateUserAddOrRemove(long groupID) {
		if (mShowItemDataList.size() > 0) {
			for (int i = 0; i < mShowItemDataList.size(); i++) {
				ItemData item = mShowItemDataList.get(i);
				if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
					GroupItemData temp = (GroupItemData) item;
					if (temp.isExpaned() && temp.mGroup.getGroupID() == groupID) {
						int groupItemPos = getGroupItemPos((GroupItemData) item);
						collapse(temp, groupItemPos);
						expand(temp, groupItemPos);
					}
				}
			}
		}
		updateAdapter();
	}

	public void updateUserGroup(User user, Group src, Group dest) {
		if (user == null) {
			V2Log.e("Incorrect paramters: user is null");
			return;
		}
		int removeIndex = -1;
		boolean found = false;
		boolean insert = false;
		for (int i = 0; i < mShowItemDataList.size(); i++) {
			ItemData item = mShowItemDataList.get(i);
			Object obj = item.getObject();
			if (!found && obj == src && ((GroupItemData) item).isExpaned()) {
				found = true;
			}

			if (dest == obj && ((GroupItemData) item).isExpaned()) {
				insert = true;
			}

			// If found source group and user, then remove from source group
			if (found && obj == user) {
				removeIndex = i;
			}

			if (removeIndex != -1 && insert) {
				break;
			}

		}

		if (removeIndex != -1) {
			removeItem(removeIndex);

		}
		if (insert) {
			addUser(dest, user);
		}
	}

	// /**
	// * Use to update user signature or avatar
	// */
	// public void updateOrgStatus() {
	// adapter.notifyDataSetChanged();
	// }

	public void updateAdapter() {
		if (mCurrentScrollState == 0) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * continue is false , break is true
	 * 
	 * @param comparedUser
	 * @param beComparedUser
	 * @param compUserStus
	 * @return
	 */
	private boolean compareUserSort(User comparedUser, User beComparedUser, User.Status compUserStus) {
		if (beComparedUser.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			return false;
		}

		if (compUserStus == User.Status.ONLINE || compUserStus == User.Status.BUSY
				|| compUserStus == User.Status.DO_NOT_DISTURB || compUserStus == User.Status.LEAVE) {
			if ((beComparedUser.getmStatus() == User.Status.ONLINE || beComparedUser.getmStatus() == User.Status.BUSY
					|| beComparedUser.getmStatus() == User.Status.DO_NOT_DISTURB
					|| beComparedUser.getmStatus() == User.Status.LEAVE)
					&& beComparedUser.compareTo(comparedUser) < 0) {
				return false;
			} else {
				return true;
			}
		} else if (compUserStus == User.Status.OFFLINE || compUserStus == User.Status.HIDDEN) {
			if ((beComparedUser.getmStatus() == User.Status.OFFLINE
					|| beComparedUser.getmStatus() == User.Status.HIDDEN)
					&& comparedUser.compareTo(beComparedUser) > 0) {
				return false;
			} else if (beComparedUser.getmStatus() != User.Status.OFFLINE
					&& beComparedUser.getmStatus() != User.Status.HIDDEN) {
				return false;
			} else {
				return true;
			}
		} else {
			if (beComparedUser.getmStatus() == User.Status.ONLINE) {
				return false;
			} else if (beComparedUser.getmStatus() == User.Status.OFFLINE
					|| beComparedUser.getmStatus() == User.Status.HIDDEN) {
				return true;
			} else
				return beComparedUser.compareTo(comparedUser) >= 0;
		}
	}

	/**
	 * Mark user as selected, and call {@link ItemData#isChecked()} will return
	 * true The function only called by {ConferenceCreateActivity#doPreSelect}
	 * 
	 * @param user
	 */
	public void selectUser(User user) {
		if (user == null) {
			return;
		}
		Set<Group> groupList = user.getBelongsGroup();
		for (Group g : groupList) {
			ItemData item = getItem(g, user);
			item.setChecked(true);

			GroupItemData groupItem = (GroupItemData) getItem(g);
			if (!groupItem.isExpaned()) {
				int pos = getGroupItemPos(groupItem);
				if (pos != -1) {
					groupItem.setExpaned(true);
					expand(groupItem, pos);
				}
			}
		}

		for (int i = 0; i < mGroupList.size(); i++) {
			Group group = mGroupList.get(i);
			checkBelongGroupAllChecked(group, group.getUsers());
		}
		updateAdapter();
	}

	/**
	 * Mark user list as selected, and call {@link ItemData#isChecked()} will
	 * return true The function only called by
	 * {ConferenceCreateActivity#doPreSelect}
	 * 
	 * @param userList
	 */
	public void selectUser(List<User> userList) {
		LongSparseArray<Group> temp = new LongSparseArray<Group>();
		for (User user : userList) {
			Set<Group> groupList = user.getBelongsGroup();
			for (Group g : groupList) {
				if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD
						|| g.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION
						|| g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE)
					continue;

				ItemData item = getItem(g, user);
				item.setChecked(true);

				Group group = temp.get(g.getGroupID());
				if (group == null)
					temp.put(g.getGroupID(), g);
			}
		}

		for (int i = 0; i < mGroupList.size(); i++) {
			Group group = mGroupList.get(i);
			if (temp.get(group.getGroupID()) != null) {
				temp.remove(group.getGroupID());
				GroupItemData groupItem = (GroupItemData) getItem(group);
				if (!groupItem.isExpaned()) {
					int pos = getGroupItemPos(groupItem);
					if (pos != -1) {
						groupItem.setExpaned(true);
						expand(groupItem, pos);
					}
				}
			}
		}

		for (int i = 0; i < temp.size(); i++) {
			Group group = temp.valueAt(i);
			GroupItemData groupItem = (GroupItemData) getItem(group);
			if (!groupItem.isExpaned()) {
				int pos = getGroupItemPos(groupItem);
				if (pos != -1) {
					groupItem.setExpaned(true);
					expand(groupItem, pos);
				}
			}
		}

		for (int i = 0; i < mGroupList.size(); i++) {
			Group group = mGroupList.get(i);
			checkBelongGroupAllChecked(group, group.getUsers());
		}

		updateAdapter();
	}

	/**
	 * Mark group as selected, and call {@link ItemData#isChecked()} will return
	 * true.<br>
	 * Users belong this group will mark, unless you expand this group.
	 * 
	 * @param group
	 */
	public void selectGroup(Group group) {
		if (group == null) {
			return;
		}
		ItemData item = getItem(group);
		item.setChecked(true);
		updateAdapter();
	}

	private void updateLoginUserNameColor(TextView nameTV, boolean isContainLoginUser) {
		if (MultilevelListView.MULTILEVEL_TYPE_ORG == multilevelType) {
			if (isContainLoginUser) {
				nameTV.setTextColor(Color.BLUE);
			} else {
				nameTV.setTextColor(
						getContext().getResources().getColor(R.color.conf_create_contacts_user_view_item_color));
			}
		} else {
			nameTV.setTextColor(
					getContext().getResources().getColor(R.color.conf_create_contacts_user_view_item_color));
		}
	}

	/**
	 * Update user item check status according to flag
	 * 
	 * @param u
	 * @param flag
	 */
	private void updateCheckItemWithoutNotification(User u, boolean flag) {
		Set<ItemData> itemDataSet = mUserItemListMap.get(u.getmUserId());
		if (itemDataSet == null || itemDataSet.size() <= 0) {
			if (itemDataSet == null)
				itemDataSet = new HashSet<>();
			Set<Group> belongsGroup = u.getBelongsGroup();
			Iterator<Group> iterator = belongsGroup.iterator();
			while (iterator.hasNext()) {
				Group tempGroup = iterator.next();
				ItemData item = getItem(tempGroup, u);
				itemDataSet.add(item);
			}
			return;
		}
		for (ItemData item : itemDataSet) {
			item.setChecked(flag);
		}
	}

	// Update group item check status according to flag
	private void updateCheckItemWithoutNotification(Group group, boolean flag) {
		ItemData item = mItemMap.get(group.getGroupID());
		if (item != null) {
			item.setChecked(flag);
		}
		List<User> list = group.getUsers();
		for (User u : list) {
			updateCheckItem(u, flag);
		}
		List<Group> subGroupList = group.getChildGroup();
		for (int i = 0; i < subGroupList.size(); i++) {
			updateCheckItemWithoutNotification(subGroupList.get(i), flag);
		}
	}

	/**
	 * Update user position according to new user status
	 * 
	 * @param gitem
	 *            group item which user belongs and expanded
	 * @param gstart
	 *            first child position of group
	 * @param gend
	 *            group end position
	 * @param user
	 * @param newSt
	 * @return
	 */
	private int updateUserPosition(GroupItemData gitem, int gstart, int gend, User user, User.Status newSt) {
		if (gend >= mShowItemDataList.size())
			gend = mShowItemDataList.size() - 1;
		int pos = -1;
		int start = gstart;
		int end = gend;

		while (start < end && end < mShowItemDataList.size() && mShowItemDataList.size() > start) {
			ItemData item = mShowItemDataList.get(start);
			ItemData endItem = mShowItemDataList.get(end);

			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_USER) {
				if (((User) ((UserItemData) item).getObject()).getmUserId() == user.getmUserId()) {
					pos = start;
				}
			} else {
				// If sub group is expended, we should update end position
				if (((GroupItemData) item).isExpaned()) {
					GroupItemData subGroupItem = (GroupItemData) item;

					int subGroupStartIndex = calculateGroupStartIndex(subGroupItem.mGroup);
					int subGroupEndIndex = calculateGroupEnd(subGroupItem.mGroup, start);
					updateUserPosition(subGroupItem, subGroupStartIndex, subGroupEndIndex, user, newSt);
					start += subGroupEndIndex;
				}
				start++;
			}

			if (endItem.getItemDataType() == MULTILEVEL_ITEM_TYPE_USER) {
				if (((User) ((UserItemData) endItem).getObject()).getmUserId() == user.getmUserId()) {
					pos = end;
				}
			} else {
				// If sub group is expended, we should update end position
				if (((GroupItemData) endItem).isExpaned()) {
					GroupItemData subGroupItem = (GroupItemData) endItem;
					int subGroupStartIndex = calculateGroupStartIndex(subGroupItem.mGroup);
					int subGroupEndIndex = calculateGroupEnd(subGroupItem.mGroup, start);
					updateUserPosition(subGroupItem, subGroupStartIndex, subGroupEndIndex, user, newSt);
					start += subGroupEndIndex;
				}
				start++;
			}

			if (pos != -1) {
				break;
			}
			start++;
			end--;
		}

		// 如果start与end相等而break，则说明目标user的位置就是start再加1的位置
		if (start == end)
			pos = start++;

		// Update user new position;
		if (pos != -1 && pos < mShowItemDataList.size()) {
			// Reset start and end position
			Group currentGroup = (Group) gitem.getObject();
			int startPos = calculateGroupStartIndex(currentGroup);
			int endPos = gend;
			// int startPos = gstart;
			// end = gend - 1;
			// int endPos = gend;

			// remove current status
			ItemData origin = removeItem(pos);
			pos = calculateIndex(startPos, endPos, user, newSt);
			if (pos != -1) {
				if (origin == null) {
					return pos;
				}

				if (pos == mShowItemDataList.size()) {
					mShowItemDataList.add(origin);
				} else {
					mShowItemDataList.add(pos, origin);
				}
			}
			return pos;
		}
		return pos;
	}

	/**
	 * Calculate end index of current group
	 * 
	 * @param group
	 * @param startIndex
	 *            current group index in list
	 * @return the last index of child position, if group child size is 0,
	 *         return current group index
	 */
	private int calculateGroupEnd(Group group, int startIndex) {
		if (startIndex >= mShowItemDataList.size()) {
			return -1;
		}

		int groupEnd = startIndex + calculateGroupEndIndex(group);
		while (startIndex < groupEnd && startIndex < mShowItemDataList.size()) {
			ItemData item = mShowItemDataList.get(startIndex);
			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				Object obj = item.getObject();
				// If current item is group and group level same and is not
				// self,
				// then group end is this item index - 1
				if ((group.getLevel() == ((Group) obj).getLevel()) && obj != group) {
					return getGroupItemPos((GroupItemData) item);
				}
				startIndex++;
			}
		}
		return startIndex;
	}

	private int calculateGroupEndIndex(Group group) {
		int groupEnd = group.getUsers().size();
		groupEnd += getExpandGroupSize(group.getChildGroup());
		return groupEnd;
	}

	private int calculateAddGroupEndIndex(Group group, int startIndex) {
		int groupEnd = startIndex + group.getUsers().size();
		return groupEnd - 1;
	}

	private int calculateGroupStartIndex(Group group) {
		GroupItemData item = (GroupItemData) getItem(group);
		return calculateGroupStartIndex(item, group);
	}

	private int calculateGroupStartIndex(GroupItemData item, Group group) {
		int itemStartPos = getGroupItemPos(item);
		int startPos = itemStartPos + getExpandGroupSize(group.getChildGroup());
		return startPos + 1;
	}

	private int calculateAddGroupStartIndex(Group group) {
		GroupItemData item = (GroupItemData) getItem(group);
		int itemStartPos = getGroupItemPos(item);
		int startPos = itemStartPos + getExpandGroupSize(group.getChildGroup());
		return startPos + 1;
	}

	private int getExpandGroupSize(List<Group> groups) {
		int groupLength = 0;
		for (int i = 0; i < groups.size(); i++) {
			groupLength += 1;
			Group child = groups.get(i);

			GroupItemData item = (GroupItemData) getItem(child);
			if (item.isExpaned()) {
				groupLength += child.getUsers().size();
			}

			if (child.getChildGroup().size() > 0) {
				groupLength += getExpandGroupSize(child.getChildGroup());
			}
		}
		return groupLength;
	}

	/**
	 * According ItemData , get pos in datas;
	 * 
	 * @param item
	 * @return
	 */
	private int getGroupItemPos(GroupItemData item) {
		for (int i = 0; i < mShowItemDataList.size(); i++) {
			ItemData itemData = mShowItemDataList.get(i);
			if (itemData.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				GroupItemData temp = (GroupItemData) itemData;
				if (temp.mGroup.getGroupID() == item.mGroup.getGroupID()) {
					return i;
				}
			}
		}
		return -1;
	}

	private int getUserItemPos(UserItemData item) {
		for (int i = 0; i < mShowItemDataList.size(); i++) {
			ItemData itemData = mShowItemDataList.get(i);
			if (itemData.getItemDataType() == MULTILEVEL_ITEM_TYPE_USER) {
				UserItemData temp = (UserItemData) itemData;
				if (temp.getId() == item.getId()) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Calculate user comfortable position which in list
	 * 
	 * @param start
	 *            start index which belongs group
	 * @param end
	 *            end index which belongs group
	 * @param targetUser
	 * @param ust
	 * @return
	 */
	private int calculateIndex(int start, int end, User targetUser, User.Status ust) {
		V2Log.d(TAG, "calculateIndex --> start : " + start + " | end : " + end + " | user name : "
				+ targetUser.getNickName() + " | Status : " + ust.name());
		int pos = -1;
		if (start < 0 || start > mShowItemDataList.size() || end > mShowItemDataList.size()) {
			return -1;
		}

		// If start equal list size, return position of end group
		if (start == mShowItemDataList.size() && end == mShowItemDataList.size()) {
			return start;
		} else if (start == end) {
			return end;
		}

		while (start <= end) {
			pos = start;
			if (start == mShowItemDataList.size()) {
				ItemData lastItem = mShowItemDataList.get(start - 1);
				User lastUser = (User) (lastItem).getObject();
				boolean result = compareUserSort(targetUser, lastUser, ust);
				if (result) {
					pos = pos - 1;
				}
				break;
			}

			ItemData item = mShowItemDataList.get(start++);
			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				continue;
			}

			User tempUser = (User) (item).getObject();
			// if item is current user, always sort after current user
			boolean result = compareUserSort(targetUser, tempUser, ust);
			if (result)
				break;
		}
		return pos;
	}

	private ItemData getItem(Group g) {
		ItemData item = mItemMap.get(g.getGroupID());
		if (item == null) {
			item = new GroupItemData(g);
			Group parent = g.getParent();
			if (parent != null) {
				ItemData itemParent = mItemMap.get(parent.getGroupID());
				if (itemParent != null)
					item.setChecked(itemParent.isChecked());
			}
			mItemMap.put(g.getGroupID(), item);
		}
		return item;
	}

	private ItemData getItem(Group g, User u) {
		LongSparseArray<ItemData> map = mUserItemDataMapOfMap.get(g.getGroupID());
		if (map == null) {
			map = new LongSparseArray<>();
			mUserItemDataMapOfMap.put(g.getGroupID(), map);
		}
		ItemData item = map.get(u.getmUserId());
		if (item == null) {
			item = new UserItemData(u, g.getLevel() + 1);
			// Initialize user item check status.
			// If exist one group checked, then user item should be checked
			boolean checked = false;
			Set<Group> parents = u.getBelongsGroup();
			for (Group parent : parents) {
				ItemData parentItem = getItem(parent);
				if (parentItem.isChecked()) {
					checked = true;
					break;
				}
			}
			// Update check status according group status.
			item.setChecked(checked);
			map.put(u.getmUserId(), item);
		}

		Set<ItemData> itemList = mUserItemListMap.get(u.getmUserId());
		if (itemList == null) {
			itemList = new HashSet<>();
			mUserItemListMap.put(u.getmUserId(), itemList);
		} else {
			Iterator<ItemData> iterator = itemList.iterator();
			ItemData next = iterator.next();
			if (next.isChecked()) {
				item.setChecked(true);
			} else {
				item.setChecked(false);
			}
		}
		itemList.add(item);

		return item;
	}

	@Override
	public void setFilterText(String filterText) {
		if (!TextUtils.isEmpty(filterText)) {
			if (this.isTextFilterEnabled()) {
				mIsInFilter = true;
				if (adapter != null) {
					Filter filter = adapter.getFilter();
					filter.filter(filterText);
				}
			}
		}
	}

	@Override
	public void clearTextFilter() {
		if (this.isTextFilterEnabled() && mIsInFilter) {
			mIsInFilter = false;
			if (adapter != null) {
				Filter filter = adapter.getFilter();
				filter.filter("");
			}
		}
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		if (listener != mMyOnItemClickListener) {
			throw new RuntimeException("Can not set others item listeners User setListener instead");
		}
		super.setOnItemClickListener(listener);
	}

	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		if (listener != mMyOnItemLongClickListener) {
			throw new RuntimeException("Can not set others item listeners Use setListener instead");
		}
		super.setOnItemLongClickListener(listener);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (this.adapter != adapter) {
			throw new RuntimeException("Do not permit set adatper");
		}
		super.setAdapter(adapter);
	}

	public void setMultilevelType(int multilevelType) {
		this.multilevelType = multilevelType;
	}

	public void clearAllNotDelete() {
		if (mGroupList != null) {
			mGroupList.clear();
		}

		if (mBaseList != null) {
			mBaseList.clear();
		}

		if (mShowItemDataList != null) {
			mShowItemDataList.clear();
		}

		if (mItemMap != null) {
			mItemMap.clear();
		}

		if (mDeletedUsers != null) {
			mDeletedUsers.clear();
		}

		if (mUserItemDataMapOfMap != null) {
			mUserItemDataMapOfMap.clear();
		}
	}

	public void clearAll() {
		if (mGroupList != null) {
			mGroupList.clear();
			mGroupList = null;
		}

		if (mBaseList != null) {
			mBaseList.clear();
			mBaseList = null;
		}

		if (mShowItemDataList != null) {
			mShowItemDataList.clear();
			mShowItemDataList = null;
		}

		if (mItemMap != null) {
			mItemMap.clear();
			mItemMap = null;
		}

		if (mDeletedUsers != null) {
			mDeletedUsers.clear();
			mDeletedUsers = null;
		}

		if (mUserItemDataMapOfMap != null) {
			mUserItemDataMapOfMap.clear();
			mUserItemDataMapOfMap = null;
		}

		if (mSearchResults != null) {
			mSearchResults.clear();
			mSearchResults = null;
		}

		if (mLastSearchResults != null) {
			mLastSearchResults.clear();
			mLastSearchResults = null;
		}
	}

	/**
	 * collapse current expanded group
	 * 
	 * @param item
	 * @param pos
	 */
	private void collapse(GroupItemData item, int pos) {
		int level = item.getLevel();
		int start = pos;
		int end = mShowItemDataList.size();
		while (++start < end) {
			ItemData it = mShowItemDataList.get(start);
			if (it.getLevel() > level && it.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				GroupItemData current = (GroupItemData) it;
				if (current.isExpaned()) {
					((GroupItemData) it).setExpaned(false);
					collapse(item, start);
				}
			}

			if (it.getLevel() > level) {
				removeItem(start--);
				end = mShowItemDataList.size();
			} else if (it.getLevel() == level) {
				break;
			}
		}
	}

	private int expand(GroupItemData item, int pos) {
		return expand(item, pos, true, false);
	}

	private int expand(GroupItemData item, int pos, boolean isFresh) {
		return expand(item, pos, isFresh, false);
	}

	/**
	 * Expand group item
	 * 
	 * @param item
	 * @param pos
	 *            Position of group item to expand.
	 * @param isFresh
	 * @param isExpandAll
	 *            This tag is used to search.
	 */
	private int expand(GroupItemData item, int pos, boolean isFresh, boolean isExpandAll) {
		// if current state in search , we should determine whether it should be
		// shown
		int groupItemPos = 0;
		Group g = (Group) item.getObject();
		List<Group> subGroupList = g.getChildGroup();
		for (int i = 0; i < subGroupList.size(); i++) {
			Group subG = subGroupList.get(i);
			GroupItemData groupItem = (GroupItemData) getItem(subG);
			if (!mIsInFilter) {
				if (pos + 1 >= mShowItemDataList.size()) {
					mShowItemDataList.add(groupItem);
				} else {
					mShowItemDataList.add(pos + 1, groupItem);
				}

				if (isFresh) {
					updateAdapter();
				}
			} else {
				if (pos + 1 >= mShowItemDataList.size()) {
					groupItemPos = mShowItemDataList.size();
				} else {
					groupItemPos = pos + 1;
				}
			}

			pos++;
			if (isExpandAll) {
				groupItem.setExpaned(true);
				pos = expand(groupItem, pos, isFresh, true);
			} else {
				if (groupItem.isExpaned()) {
					pos = expand(groupItem, pos, isFresh, false);
				}
			}
		}

		int mRealShowSize = 0;
		boolean isHasUserExist = false;
		List<User> list = g.getUsers();
		Collections.sort(list);
		for (int i = 0; i < list.size(); i++) {
			User u = list.get(i);
			// check ignore current logged use flag
			if (mIgnoreCurrentUser && u.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
				continue;
			}
			// 电话联系人不显示
			if (mIgnoreCurrentUser && u.getAccountType() == V2GlobalConstants.ACCOUNT_TYPE_PHONE_FRIEND) {
				continue;
			}

			if (u.getAccountType() == V2GlobalConstants.ACCOUNT_TYPE_PHONE_FRIEND
					&& multilevelType != MULTILEVEL_TYPE_CONTACT) {
				continue;
			}

			if (mIsInFilter && mSearchResults != null && !mSearchResults.contains(u)) {
				continue;
			}

			// If current state in search , Explain this group item should be
			// shown.
			isHasUserExist = true;
			if (pos + 1 >= mShowItemDataList.size()) {
				mShowItemDataList.add(getItem(g, u));
			} else {
				mShowItemDataList.add(pos + 1, getItem(g, u));
			}

			if (isFresh) {
				updateAdapter();
			}
			mRealShowSize++;
			pos++;
		}

		if (mIsInFilter) {
			if (isHasUserExist) {
				if (groupItemPos != 0)
					mShowItemDataList.add(groupItemPos, item);
				else {
					int target = pos - mRealShowSize - 1;
					if (target == -1) {
						target = 0;
					}
					mShowItemDataList.add(target, item);
				}
			} else {
				if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT && g.getGroupID() == -1
						&& mShowItemDataList.size() != 0) {
					mShowItemDataList.add(0, item);
				}
			}
		}
		return pos;
	}

	private ItemData removeItem(int pos) {
		if (pos > mShowItemDataList.size() || pos < 0) {
			return null;
		}
		ItemData remove = mShowItemDataList.remove(pos);
		updateAdapter();
		return remove;
	}

	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ItemData item = mShowItemDataList.get(position);
			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				GroupItemData groupItem = (GroupItemData) item;
				currentClickGroup = groupItem.getId();
				if (groupItem.isExpaned()) {
					collapse((GroupItemData) item, position);
					groupItem.setExpaned(false);
					groupItem.setVisiblieDivider(true);
				} else {
					expand(groupItem, position);
					groupItem.setExpaned(true);
					groupItem.setVisiblieDivider(false);
				}
			} else {
				UserItemData userItem = (UserItemData) item;
				User u = (User) userItem.getObject();
				if (u.getAccountType() == V2GlobalConstants.ACCOUNT_TYPE_PHONE_FRIEND) {
					return;
				}
			}

			if (mListener != null) {
				mListener.onItemClicked(parent, view, position, id, item);
				if (mCBFlag) {
					((MultilevelListViewItemView) view).updateCheckBox(item);
				}
			}
		}
	}

	private class MyOnScrollChangeListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mCurrentScrollState = scrollState;
			if (mCurrentScrollState == 0) {
				updateAdapter();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		}
	}

	private class MyOnItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			ItemData item = mShowItemDataList.get(position);

			boolean flag = false;
			if (mListener != null) {
				flag = mListener.onItemLongClick(parent, view, position, id, item);
				if (mCBFlag) {
					((MultilevelListViewItemView) view).updateCheckBox(item);
				}
			}
			return flag;
		}
	}

	private OnClickListener mCheckBoxListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ItemData item = (ItemData) v.getTag();
			if (mListener != null) {
				mListener.onCheckboxClicked(v, item);
			}
		}

	};

	class MultilevelListViewAdapter extends BaseAdapter implements Filterable {

		@Override
		public int getCount() {
			try {
				return mShowItemDataList.size();
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			try {
				return mShowItemDataList.get(position).getObject();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			try {
				return mShowItemDataList.get(position).getId();
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mShowItemDataList == null || mShowItemDataList.get(position) == null) {
				return convertView;
			}

			MultilevelListViewItemView view;
			if (convertView == null) {
				view = new MultilevelListViewItemView(mContext);
				convertView = view;
			} else {
				view = (MultilevelListViewItemView) convertView;
			}

			V2Log.w("test", "multilevel position : " + position + " convertView : " + convertView.toString());
			// view.update(mShowItemDataList.get(position), !mIsInFilter);
			// 搜索带组织结构
			view.update(mShowItemDataList.get(position), true);
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public Filter getFilter() {
			return searchedFilter;
		}

	}

	// Use to query item
	class SearchedFilter extends Filter {

		private boolean isFirstSearch = true;

		private String mLastSearchContent = null;
		private boolean isReverseSearch = false;

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults fr = new FilterResults();
			List<ItemData> list;
			// 每次重新搜索都要清空上一次的搜索结果
			if (mSearchResults != null) {
				mSearchResults.clear();
			}
			mSearchResults = null;
			// 判断是否是反向搜索
			isReverseSearch = mLastSearchContent != null
					&& constraint.toString().length() < mLastSearchContent.length();
			mLastSearchContent = constraint.toString();
			if (TextUtils.isEmpty(constraint.toString())) {
				list = new ArrayList<>();
				list.addAll(mLastSearchResults.get(0));
				mSearchUtils.clearAll();
				isFirstSearch = true;
				mLastSearchContent = null;
				mLastSearchResults.clear();
			} else {
				list = new ArrayList<>();
				if (isFirstSearch) {
					mSearchUtils.clearAll();
					List<Object> users = new ArrayList<>();
					for (Group group : mGroupList) {
						convertGroupToUser(users, group);
					}
					mSearchUtils.receiveList = users;
					// 第一次搜索时将当前组织结构的状态记录下来。
					for (int i = 0; i < mShowItemDataList.size(); i++) {
						ItemData itemData = mShowItemDataList.get(i);
						if (itemData.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
							GroupItemData groupItem = (GroupItemData) itemData;
							// groupItem.setTag(groupItem.isExpanded);
							collapseAllOrg(groupItem.getGroup(), groupItem.isExpanded, false);
						}
					}
					List<ItemData> temp = new ArrayList<>();
					temp.addAll(mShowItemDataList);
					mLastSearchResults.add(temp);
					isFirstSearch = false;
				}

				mSearchResults = mSearchUtils.receiveGroupUserFilterSearch(constraint.toString());
				if (mSearchResults != null) {
					if (mIsNeedShowSearch) {
						for (int i = 0; i < mSearchResults.size(); i++) {
							User user = mSearchResults.get(i);
							ItemData item;
							if (multilevelType == MultilevelListView.MULTILEVEL_TYPE_CONTACT) {
								item = getItem(user.getFirstBelongsContactGroup(), user);
							} else {
								item = getItem(user.getFirstBelongsGroup(), user);
							}
							list.add(item);
						}
					}
				}
			}

			fr.values = list;
			fr.count = list.size();
			return fr;
		}

		private void convertGroupToUser(List<Object> users, Group group) {
			users.addAll(group.getUsers());
			List<Group> gList = group.getChildGroup();
			for (Group subG : gList) {
				convertGroupToUser(users, subG);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (!isReverseSearch) {
				if (results.values != null && mSearchResults != null) {
					// 每次搜索都重新展开所有
					GroupItemData groupRootItem = (GroupItemData) mBaseList.get(0);
					groupRootItem.setExpaned(true);
					mShowItemDataList.clear();
					expand(groupRootItem, 0, false, true);

					List<ItemData> temp = new ArrayList<>();
					temp.addAll(mShowItemDataList);
					if (constraint.length() >= mLastSearchResults.size()) {
						mLastSearchResults.add(temp);
					} else {
						mLastSearchResults.add(constraint.length(), temp);
					}
				} else {
					mShowItemDataList = (List<ItemData>) results.values;
					if (mShowItemDataList != null) {
						// 每次搜索都重新展开所有
						GroupItemData groupRootItem = (GroupItemData) mBaseList.get(0);
						groupRootItem.setExpaned(true);
						mShowItemDataList.clear();
						expand(groupRootItem, 0, false, true);
					}
				}
			} else {
				if (TextUtils.isEmpty(constraint.toString())) {
					isReverseSearch = false;
					mShowItemDataList = (List<ItemData>) results.values;
					for (int i = 0; i < mShowItemDataList.size(); i++) {
						ItemData itemData = mShowItemDataList.get(i);
						if (itemData.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
							GroupItemData groupItem = (GroupItemData) itemData;
							// boolean isExpand = (boolean) groupItem.getTag();
							// V2Log.d("test", " publishResults GroupItemData :
							// " + groupItem.getGroup().getName() + " | expand :
							// " + groupItem.isExpaned());
							// groupItem.setExpaned(isExpand);
							collapseAllOrg(groupItem.getGroup(), null, true);
							// groupItem.setTag(null);
						}
					}
				} else {
					mShowItemDataList.clear();
					mShowItemDataList.addAll(mLastSearchResults.get(constraint.length()));
				}
			}
			updateAdapter();
		}

		@Override
		public CharSequence convertResultToString(Object resultValue) {
			return super.convertResultToString(resultValue);
		}

		void collapseAllOrg(Group group, Object obj, boolean isResult) {
			List<Group> childGroup = group.getChildGroup();
			if (childGroup.size() > 0) {
				for (int i = 0; i < childGroup.size(); i++) {
					collapseAllOrg(childGroup.get(i), obj, isResult);
				}
			}

			GroupItemData item = (GroupItemData) getItem(group);
			if (isResult) {
				Object tag2 = item.getTag();
				if (tag2 == null) {
					item.setExpaned(false);
				} else {
					boolean isExpand = (boolean) tag2;
					item.setExpaned(isExpand);
					item.setTag(null);
				}
			} else {
				item.setTag(obj);
			}

		}

		void search(List<ItemData> list, Group g, CharSequence constraint) {
			List<User> uList = g.getUsers();
			for (User u : uList) {
				if (u.getDisplayName().contains(constraint) || u.getArra().contains(constraint)) {
					list.add(getItem(g, u));
				}
			}
			List<Group> gList = g.getChildGroup();
			for (Group subG : gList) {
				search(list, subG, constraint);
			}
		}

	}

	// Item response Listener
	public interface MultilevelListViewListener {
		void onItemClicked(AdapterView<?> parent, View view, int position, long id, ItemData item);

		boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, ItemData item);

		void onCheckboxClicked(View view, ItemData item);
	}

	public interface ItemData extends Comparable<ItemData> {
		long getId();

		Object getObject();

		int getLevel();

		boolean isChecked();

		void setChecked(boolean flag);

		int getItemDataType();
	}

	class GroupItemData implements ItemData {

		private Group mGroup;
		private boolean isExpanded;
		private boolean isChecked;
		private boolean searchedCurrentUser;
		private boolean existCurrentUser;
		private boolean isVisiblieDivider = true;

		private Object mTag;

		public GroupItemData(Group group) {
			this.mGroup = group;
		}

		@Override
		public Object getObject() {
			return mGroup;
		}

		@Override
		public long getId() {
			return this.mGroup.getGroupID();
		}

		@Override
		public int compareTo(ItemData another) {
			return 0;
		}

		@Override
		public int getLevel() {
			return mGroup.getLevel();
		}

		@Override
		public boolean isChecked() {
			return isChecked;
		}

		@Override
		public void setChecked(boolean flag) {
			isChecked = flag;
		}

		@Override
		public int getItemDataType() {
			return MULTILEVEL_ITEM_TYPE_GROUP;
		}

		public Group getGroup() {
			return mGroup;
		}

		public Object getTag() {
			return mTag;
		}

		public boolean isExpaned() {
			return isExpanded;
		}

		public boolean isSearchedCurrentUser() {
			return searchedCurrentUser;
		}

		public boolean isExistCurrentUser() {
			return existCurrentUser;
		}

		public boolean isVisiblieDivider() {
			return isVisiblieDivider;
		}

		public void setGroup(Group mGroup) {
			this.mGroup = mGroup;
		}

		public void setExpaned(boolean isExpaned) {
			this.isExpanded = isExpaned;
		}

		public void setSearchedCurrentUser(boolean searchedCurrentUser) {
			this.searchedCurrentUser = searchedCurrentUser;
		}

		public void setExistCurrentUser(boolean existCurrentUser) {
			this.existCurrentUser = existCurrentUser;
		}

		public void setTag(Object mTag) {
			this.mTag = mTag;
		}

		public void setVisiblieDivider(boolean isVisiblieDivider) {
			this.isVisiblieDivider = isVisiblieDivider;
		}
	}

	class UserItemData implements ItemData {

		private User mUser;
		private int mLevel;
		private boolean isChecked;

		public UserItemData(User user, int level) {
			this.mUser = user;
			this.mLevel = level;
		}

		@Override
		public Object getObject() {
			return mUser;
		}

		@Override
		public long getId() {
			return mUser.getmUserId();
		}

		@Override
		public int getLevel() {
			return mLevel;
		}

		@Override
		public boolean isChecked() {
			return isChecked;
		}

		@Override
		public void setChecked(boolean flag) {
			isChecked = flag;
		}

		@Override
		public int compareTo(ItemData another) {
			if (another == null || another.getObject() == null)
				return -1;

			if (another.getItemDataType() == MULTILEVEL_ITEM_TYPE_USER) {
				User anotherUser = (User) another.getObject();
				boolean result = compareUserSort(anotherUser, mUser, anotherUser.getmStatus());
				if (result)
					return 1;
				else
					return -1;
			} else {
				return 1;
			}
		}

		@Override
		public int getItemDataType() {
			return MULTILEVEL_ITEM_TYPE_USER;
		}

		public User getUser() {
			return mUser;
		}

		public void setUser(User mUser) {
			this.mUser = mUser;
		}
	}

	/**
	 * Adapter item view
	 * 
	 * @author
	 *
	 */
	class MultilevelListViewItemView extends LinearLayout {

		private ItemData mItem;

		private View mGroupRootView;
		private TextView mGroupNameTV;
		private ImageView mGroupArrowIV;
		private TextView mUserOnlineStatusConuts;
		private CheckBox mGroupCheckBox;

		private View mUserRootView;
		private CustomAvatarImageView mPhotoIV;
		private TextView mUserNameTV;
		private TextView mUserSignatureTV;
		private V2ImageView mStatusIV;
		private CheckBox mUserCheckBox;

		private View mMulItemDivider;

		private static final int MULTILEVEL_LEFT_MARGIN = 35;

		private Drawable mUserOnlineStatusDrawable;
		private Drawable mUserLeaveStatusDrawable;
		private Drawable mUserBusyStatusDrawable;
		private Drawable mUserDisturbStatusDrawable;
		private Drawable mUserPhoneStatusDrawable;
		private Drawable mArrowRightDrawable;
		private Drawable mArrowDownDrawable;

		public MultilevelListViewItemView(Context context) {
			super(context);
			init(null);
		}

		private void init(ViewGroup root) {
			View mRoot = LayoutInflater.from(getContext()).inflate(R.layout.multilevel_adapter_item, root,
					root != null);
			addView(mRoot, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));

			mGroupRootView = mRoot.findViewById(R.id.group_view_root);
			mGroupNameTV = (TextView) mRoot.findViewById(R.id.group_name);
			mGroupArrowIV = (ImageView) mRoot.findViewById(R.id.group_arrow);
			mGroupCheckBox = (CheckBox) mRoot.findViewById(R.id.group_view_ck);
			mUserOnlineStatusConuts = ((TextView) mRoot.findViewById(R.id.group_online_statist));

			mUserRootView = mRoot.findViewById(R.id.user_view_root);
			mPhotoIV = (CustomAvatarImageView) mRoot.findViewById(R.id.ws_common_avatar);
			mUserNameTV = (TextView) mRoot.findViewById(R.id.user_name);
			mUserSignatureTV = (TextView) mRoot.findViewById(R.id.user_signature);
			mStatusIV = (V2ImageView) mRoot.findViewById(R.id.user_status_iv);
			mUserCheckBox = (CheckBox) mRoot.findViewById(R.id.user_check_view);

			mMulItemDivider = mRoot.findViewById(R.id.multilevel_adapter_item_divider);

			Resources resources = mContext.getResources();
			mUserOnlineStatusDrawable = resources.getDrawable(R.drawable.online);
			mUserLeaveStatusDrawable = resources.getDrawable(R.drawable.leave);
			mUserBusyStatusDrawable = resources.getDrawable(R.drawable.busy);
			mUserDisturbStatusDrawable = resources.getDrawable(R.drawable.do_not_distrub);
			mUserPhoneStatusDrawable = resources.getDrawable(R.drawable.cell_phone_user);

			mArrowRightDrawable = resources.getDrawable(R.drawable.ic_comlist_arrow_right);
			mArrowDownDrawable = resources.getDrawable(R.drawable.ic_comlist_arrow_down);
		}

		/**
		 * 
		 * @param item
		 * @param paddingFlag
		 *            For show search result
		 */
		public void update(ItemData item, boolean paddingFlag) {
			if (item == null) {
				return;
			}

			if (this.mItem == null || this.mItem != item) {
				this.mItem = item;
			}

			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				GroupItemData groupItem = (GroupItemData) item;
				mGroupRootView.setVisibility(View.VISIBLE);
				if (paddingFlag) {
					mGroupRootView.setPadding((item.getLevel() - 1) * MULTILEVEL_LEFT_MARGIN, 0, 0, 0);
				} else {
					mGroupRootView.setPadding(MULTILEVEL_LEFT_MARGIN, 0, 0, 0);
				}
				updateGroupItem();
				mUserRootView.setVisibility(View.GONE);
				if (groupItem.isVisiblieDivider()) {
					mMulItemDivider.setVisibility(View.VISIBLE);
				} else {
					mMulItemDivider.setVisibility(View.GONE);
				}
				// Update checkbox tag
				if (mGroupCheckBox != null) {
					mGroupCheckBox.setTag(item);
				}
			} else {
				mUserRootView.setVisibility(View.VISIBLE);
				if (paddingFlag) {
					mUserRootView.setPadding((item.getLevel() - 1) * MULTILEVEL_LEFT_MARGIN, 0, 0, 0);
				} else {
					mUserRootView.setPadding(MULTILEVEL_LEFT_MARGIN, 0, 0, 0);
				}
				updateUserItem();
				mGroupRootView.setVisibility(View.GONE);
				mMulItemDivider.setVisibility(View.GONE);
				// Update checkbox tag
				if (mUserCheckBox != null) {
					mUserCheckBox.setTag(item);
				}
			}
		}

		public void updateCheckBox(ItemData item) {
			if (!mCBFlag) {
				throw new RuntimeException(" Please set setShowedCheckedBox first");
			}

			if (item.getItemDataType() == MULTILEVEL_ITEM_TYPE_GROUP) {
				if (mGroupCheckBox != null) {
					mGroupCheckBox.setChecked(mItem.isChecked());
				}
			}

			if (mUserCheckBox != null) {
				mUserCheckBox.setChecked(mItem.isChecked());
			}
		}

		private void updateGroupItem() {
			Group g = ((Group) mItem.getObject());
			mGroupNameTV.setText(g.getName());
			boolean isContainLoginUser = updateGroupItemUserOnlineNumbers();
			updateLoginUserNameColor(mGroupNameTV, isContainLoginUser);
			V2Log.e("test", "mGroupArrowIV : " + mGroupArrowIV.toString());
			if (((GroupItemData) mItem).isExpaned()) {
				// 如果该组织处于打开状态并且没有子类了,就将其展开状态修改为闭合状态
				// if (g.getUserCount() <= 0) {
				// ((ImageView)
				// mRoot.findViewById(R.id.group_arrow)).setImageResource(R.drawable.arrow_right_gray);
				// ((GroupItemData) mItem).setExpaned(false);
				// } else {
				// }
				if (currentClickGroup == g.getGroupID()) {
					Drawable background = mGroupArrowIV.getDrawable();
					if (background.getIntrinsicWidth() == mArrowRightDrawable.getIntrinsicWidth()) {
						AnimationController.arrowRotateDown(mGroupArrowIV, 300, 0, 90);
					} else {
						AnimationController.arrowRotateDown(mGroupArrowIV, 300, -90, 0);
					}
					currentClickGroup = -5;
				} else {
					mGroupArrowIV.clearAnimation();
					mGroupArrowIV.setImageDrawable(mArrowDownDrawable);
				}
			} else {
				if (currentClickGroup == g.getGroupID()) {
					Drawable background = mGroupArrowIV.getDrawable();
					if (background.getIntrinsicWidth() == mArrowRightDrawable.getIntrinsicWidth()) {
						AnimationController.arrowRotateDown(mGroupArrowIV, 300, 90, 0);
					} else {
						AnimationController.arrowRotateDown(mGroupArrowIV, 300, 0, -90);
					}
					currentClickGroup = -5;
				} else {
					mGroupArrowIV.clearAnimation();
					mGroupArrowIV.setImageDrawable(mArrowRightDrawable);
				}
			}

			if (mCBFlag) {
				mGroupCheckBox.setVisibility(View.VISIBLE);
				mGroupCheckBox.setChecked(mItem.isChecked());
				mGroupCheckBox.setOnClickListener(mCheckBoxListener);
			} else {
				mGroupCheckBox.setVisibility(View.INVISIBLE);
			}
		}

		private boolean updateGroupItemUserOnlineNumbers() {
			boolean isContainLoginUser = false;
			GroupItemData gi = ((GroupItemData) mItem);
			Group g = ((Group) gi.getObject());
			int count = g.getUserCount();
			int onlineCount = 0;
			Set<User> sets = g.getOnlineUserSet();
			for (User u : sets) {
				User user = GlobalHolder.getInstance().getUser(u.getmUserId());
				User.Status status = user.getmStatus();
				if ((status == User.Status.ONLINE || status == User.Status.BUSY || status == User.Status.DO_NOT_DISTURB
						|| status == User.Status.LEAVE)
						&& ((!mIgnoreCurrentUser || (mIgnoreCurrentUser
								&& u.getmUserId() != GlobalHolder.getInstance().getCurrentUserId())))) {
					onlineCount++;
				}

				if (u.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
					isContainLoginUser = true;
				}
			}

			if (!gi.isSearchedCurrentUser() && mIgnoreCurrentUser) {
				if (g.findUser(GlobalHolder.getInstance().getCurrentUser()) != null) {
					gi.setExistCurrentUser(true);
				}
				gi.setSearchedCurrentUser(true);
			}
			mUserOnlineStatusConuts.setText(
					onlineCount + " / " + ((mIgnoreCurrentUser && gi.isExistCurrentUser()) ? count - 1 : count));
			mUserOnlineStatusConuts.invalidate();
			return isContainLoginUser;
		}

		private void updateUserItem() {
			User u = ((User) mItem.getObject());
			u = GlobalHolder.getInstance().getUser(u.getmUserId());

			mPhotoIV.setImageBitmap(u.getOrgAvatarBitmap());
			mUserNameTV.setText(u.getDisplayName());
			mUserSignatureTV.setText(u.getSignature() == null ? "" : u.getSignature());
			updateUserStatus(u);
			if (mCBFlag) {
				mUserCheckBox.setVisibility(View.VISIBLE);
				mUserCheckBox.setChecked(mItem.isChecked());
				mUserCheckBox.setOnClickListener(mCheckBoxListener);
			} else {
				mUserCheckBox.setVisibility(View.GONE);
			}
		}

		private void updateUserStatus(User u) {
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
				// 滤镜效果不好使,用于离线用户着色
				// mPhotoIV.setColorFilter(Color.GRAY,
				// PorterDuff.Mode.DST_OVER);
				mUserNameTV.setTextColor(
						getContext().getResources().getColor(R.color.contacts_user_view_item_color_offline));
			} else {
				mStatusIV.setVisibility(View.VISIBLE);
				// mPhotoIV.clearColorFilter();
				updateLoginUserNameColor(mUserNameTV, u.getmUserId() == GlobalHolder.getInstance().getCurrentUserId());
			}
			mPhotoIV.mUserStatus = st;
			mPhotoIV.invalidate();
			mStatusIV.invalidate();
		}
	}

}
