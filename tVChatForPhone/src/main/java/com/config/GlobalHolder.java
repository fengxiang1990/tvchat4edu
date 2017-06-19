package com.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.Toast;

import com.V2.jni.ImRequest;
import com.V2.jni.ind.BoUserInfoBase;
import com.V2.jni.ind.BoUserInfoGroup;
import com.V2.jni.ind.BoUserInfoShort;
import com.V2.jni.ind.V2Group;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.util.BitmapLruCache;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.V2Log;
import com.bizcom.vo.AddFriendHistorieNode;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.ContactGroup;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.CrowdGroup.AuthType;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.FileDownLoadBean;
import com.bizcom.vo.Group;
import com.bizcom.vo.OrgGroup;
import com.bizcom.vo.User;
import com.bizcom.vo.User.DeviceType;
import com.bizcom.vo.User.Status;
import com.bizcom.vo.UserDeviceConfig;
import com.bizcom.vo.meesage.VMessage;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalHolder {

	private static GlobalHolder holder;
	public long mCurrentUserId;
	private User mCurrentUser;

	private LongSparseArray<User> mUserHolder = new LongSparseArray<>();
	private LongSparseArray<Group> mGroupHolder = new LongSparseArray<>();

	private List<Group> mOrgGroup = new ArrayList<>();
	public List<Group> mConfGroup = new ArrayList<>();
	private List<Group> mContactsGroup = new ArrayList<>();
	private List<Group> mCrowdGroup = new ArrayList<>();
	private List<Group> mDiscussionBoardGroup = new ArrayList<>();

	private LongSparseArray<List<UserDeviceConfig>> mUserDeviceList = new LongSparseArray<>();
	private GlobalState mState = new GlobalState();
	private List<String> mDataBaseTableCacheName = new ArrayList<>();
	public List<AddFriendHistorieNode> mAddFriendHistorieList = new ArrayList<>();
	public LruCache<Long, Bitmap> mAvatarBmHolder = new BitmapLruCache<>(GlobalConfig.getLruCacheMaxSize());
	public LruCache<Long, Bitmap> mOrgAvatarBmHolder = new BitmapLruCache<>(GlobalConfig.getLruCacheMaxSize());
	public Map<String, FileDownLoadBean> mGlobleFileProgress = new HashMap<>();
	public Map<String, String> mTransingLockFiles = new HashMap<>();
	// 保存文档的ID，暂时用来区分该文档是图片文档还是白板
	public Map<String, Integer> mShareDocIds = new HashMap<>();
	// 限制同时发送或下载文件的数量
	public Map<Long, Integer> mTransingFiles = new HashMap<>();
	public List<Long> mTransingTag = new ArrayList<>();
	// public static Map<Long, Integer> mDownLoadingFiles = new
	// HashMap<Long,Integer>();

	private volatile boolean p2pAVNeedStickyBraodcast = false;
	private long mCurrentMeetingID = -1;
	private Lock mLock;

	public static GlobalHolder getInstance() {
		if (holder == null) {
			synchronized (GlobalHolder.class) {
				if (holder == null) {
					holder = new GlobalHolder();
				}
			}
		}
		return holder;
	}

	public void clearAll() {
		mUserHolder.clear();
		mUserHolder = null;
		mGroupHolder.clear();
		mGroupHolder = null;

		mOrgGroup.clear();
		mOrgGroup = null;
		mConfGroup.clear();
		mConfGroup = null;
		mContactsGroup.clear();
		mContactsGroup = null;
		mCrowdGroup.clear();
		mCrowdGroup = null;
		mDiscussionBoardGroup.clear();
		mDiscussionBoardGroup = null;

		mAvatarBmHolder.evictAll();
		mAvatarBmHolder = null;
		mOrgAvatarBmHolder.evictAll();
		mOrgAvatarBmHolder = null;
		mUserDeviceList.clear();
		mUserDeviceList = null;
		mAddFriendHistorieList.clear();
		mAddFriendHistorieList = null;
		mDataBaseTableCacheName.clear();
		mDataBaseTableCacheName = null;
		mGlobleFileProgress.clear();
		mGlobleFileProgress = null;
		mShareDocIds.clear();
		mShareDocIds = null;
		mTransingFiles.clear();
		mTransingFiles = null;
		mTransingTag.clear();
		mTransingTag = null;

		BitmapManager.getInstance().unRegisterBitmapChangedListener(bitmapChangedListener);
		mState = null;
		holder = null;
	}

	private GlobalHolder() {
		BitmapManager.getInstance().registerLastBitmapChangedListener(bitmapChangedListener);
//		// 初始化好友默认tab
//		Group defGroup = new ContactGroup(-1, GlobalConfig.Resource.CONTACT_DEFAULT_TAB_NAME);
//		mContactsGroup.sendFriendToTv(defGroup);
		mLock = new ReentrantLock();
	}

	public User getCurrentUser() {
		if (mCurrentUser == null)
			mCurrentUser = new User(mCurrentUserId);
		return mCurrentUser;
	}

	public long getCurrentUserId() {
		if (mCurrentUser == null) {
			return 0;
		} else {
			return mCurrentUser.getmUserId();
		}
	}

	/**
	 * Get user object according user ID<br>
	 * If id is negative, will return null.<br>
	 * Otherwise user never return null. If application doesn't receive user
	 * information from server.<br>
	 * User property is dirty {@link User#isFromService()}
	 *
	 * @param userID
	 * @return
	 */
	public User getUser(long userID) {
		if (userID <= 0) {
			return null;
		}

		mLock.lock();
		try {
			User tmp = mUserHolder.get(userID);
			if (tmp == null) {
				tmp = new User(userID);
				mUserHolder.put(userID, tmp);
				if (GlobalHolder.getInstance().getGlobalState().isGroupLoaded()) {
					// if receive this callback , the dirty change false;
					V2Log.i("user is null , invoke!");
					V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, userID);
				}
			}
			return tmp;
		} finally {
			mLock.unlock();
		}

	}

	public User getExistUser(long userID) {
		mLock.lock();
		try {
			User tmp = mUserHolder.get(userID);
			if (tmp == null) {
				return null;
			} else {
				return tmp;
			}
		} finally {
			mLock.unlock();
		}
	}

	public void setCurrentUser(User u) {
		User mCurrentUser = getUser(u.getmUserId());
		this.mCurrentUser = mCurrentUser;
		this.mCurrentUser.setCurrentLoggedInUser(true);
		this.mCurrentUser.updateStatus(User.Status.ONLINE);
		mCurrentUser.updateStatus(User.Status.ONLINE);
	}

	public boolean putUser(User user) {
		if (user == null) {
			return false;
		}

		boolean ret;
		Long key = user.getmUserId();
		User cu = mUserHolder.get(key);
		if (cu == null) {
			mUserHolder.put(key, user);
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	public User putOrUpdateUser(BoUserInfoGroup boGroupUserInfo) {
		if (boGroupUserInfo == null || boGroupUserInfo.mId <= 0) {
			return null;
		}

		User user;
		boolean isContained = true;
		user = mUserHolder.get(boGroupUserInfo.mId);
		if (user == null) {
			isContained = false;
			user = new User(boGroupUserInfo.mId);
			user.isContain = false;
		} else {
			user.isContain = true;
		}

		user.setFromService(true);

		if (boGroupUserInfo.mAccount != null) {
			user.setAccount(boGroupUserInfo.mAccount);
		}
		if (boGroupUserInfo.mAccountType != null) {
			int mAccountType = Integer.valueOf(boGroupUserInfo.mAccountType);
			user.setAccountType(mAccountType);
			if (mAccountType == V2GlobalConstants.ACCOUNT_TYPE_PHONE_FRIEND) {
				user.updateStatus(Status.ONLINE);
			}
		}
		if (boGroupUserInfo.mAvatarLocation != null) {
			user.setmAvatarLocation(boGroupUserInfo.mAvatarLocation);
		}
		if (boGroupUserInfo.mNickName != null) {
			user.setNickName(boGroupUserInfo.mNickName);
		}
		if (boGroupUserInfo.mCommentName != null) {
			user.setCommentName(boGroupUserInfo.mCommentName);
		}
		if (boGroupUserInfo.mSign != null) {
			user.setSignature(boGroupUserInfo.mSign);
		}

		if (boGroupUserInfo.mAuthtype != null) {
			try {
				int authtype = Integer.valueOf(boGroupUserInfo.mAuthtype);
				user.setAuthtype(authtype);
			} catch (NumberFormatException e) {
				V2Log.e("CLASS = GlobalHolder MOTHERD = putOrUpdateUser(BoUserInfoGroup boGroupUserInfo) mAuthtype 转整数失败 ");
			}
		}
		if (boGroupUserInfo.mSex != null) {
			user.setSex(boGroupUserInfo.mSex);
		}
		if (boGroupUserInfo.mStringBirthday != null) {
			user.setmStringBirthday(boGroupUserInfo.mStringBirthday);
		}
		if (boGroupUserInfo.mMobile != null) {
			user.setMobile(boGroupUserInfo.mMobile);
		}
		if (boGroupUserInfo.mTelephone != null) {
			user.setTelephone(boGroupUserInfo.mTelephone);
		}
		if (boGroupUserInfo.mEmail != null) {
			user.setEmail(boGroupUserInfo.mEmail);
		}
		if (boGroupUserInfo.mFax != null) {
			user.setFax(boGroupUserInfo.mFax);
		}
		if (boGroupUserInfo.mJob != null) {
			user.setJob(boGroupUserInfo.mJob);
		}

		if (boGroupUserInfo.mAddress != null) {
			user.setAddress(boGroupUserInfo.mAddress);
		}

		if (boGroupUserInfo.mBirthday != null) {
			user.setBirthday(boGroupUserInfo.mBirthday);
		}

		if (!isContained) {
			mUserHolder.put(user.getmUserId(), user);
		}
		return user;
	}

	public User putOrUpdateUser(BoUserInfoShort boUserInfoShort) {
		if (boUserInfoShort == null || boUserInfoShort.mId <= 0) {
			return null;
		}

		User user;
		boolean isContained = true;
		user = mUserHolder.get(boUserInfoShort.mId);
		if (user == null) {
			isContained = false;
			user = new User(boUserInfoShort.mId);
		}

		user.setFromService(true);

		if (boUserInfoShort.mAccount != null) {
			user.setAccount(boUserInfoShort.mAccount);
		}
		if (boUserInfoShort.mNickName != null) {
			user.setNickName(boUserInfoShort.mNickName);
		}
		if (boUserInfoShort.mCommentName != null) {
			user.setCommentName(boUserInfoShort.mCommentName);
		}

		if (boUserInfoShort.mUeType != null) {
			try {
				int deviceType = Integer.valueOf(boUserInfoShort.mUeType);
				user.setDeviceType(DeviceType.fromInt(deviceType));
			} catch (NumberFormatException e) {
				V2Log.e("CLASS = GlobalHolder MOTHERD = putOrUpdateUser(BoUserInfoShort boUserInfoShort) mUeType 转整数失败 ");
			}
		}

		if (boUserInfoShort.mAccountType != null) {
			try {
				int accountType = Integer.valueOf(boUserInfoShort.mAccountType);

				if (accountType == V2GlobalConstants.ACCOUNT_TYPE_NON_REGISTERED) {
					user.setRapidInitiation(true);
				} else {
					user.setRapidInitiation(false);
				}
			} catch (NumberFormatException e) {
				V2Log.e("CLASS = GlobalHolder MOTHERD = putOrUpdateUser(BoUserInfoShort boUserInfoShort) mAccountType 转整数失败 ");
			}
		}

		if (!isContained) {
			mUserHolder.put(user.getmUserId(), user);
		}
		return user;
	}

	public User putOrUpdateUser(BoUserInfoBase boUserBaseInfo) {
		if (boUserBaseInfo == null || boUserBaseInfo.mId <= 0) {
			return null;
		}

		User user;
		boolean isContained = true;
		user = mUserHolder.get(boUserBaseInfo.mId);
		if (user == null) {
			isContained = false;
			user = new User(boUserBaseInfo.mId);
		}

		user.setFromService(true);

		if (boUserBaseInfo.mAccount != null) {
			user.setAccount(boUserBaseInfo.mAccount);
		}
		if (boUserBaseInfo.mNickName != null) {
			user.setNickName(boUserBaseInfo.mNickName);
		}
		if (boUserBaseInfo.mCommentName != null) {
			user.setCommentName(boUserBaseInfo.mCommentName);
		}
		if (boUserBaseInfo.mSign != null) {
			user.setSignature(boUserBaseInfo.mSign);
		}

		if (boUserBaseInfo.mAuthtype != null) {
			try {
				int authtype = Integer.valueOf(boUserBaseInfo.mAuthtype);
				user.setAuthtype(authtype);
			} catch (NumberFormatException e) {
				V2Log.e("CLASS = GlobalHolder MOTHERD = putOrUpdateUser(BoUserInfoGroup boGroupUserInfo) mAuthtype 转整数失败 ");
			}
		}
		if (boUserBaseInfo.mSex != null) {
			user.setSex(boUserBaseInfo.mSex);
		}
		if (boUserBaseInfo.mStringBirthday != null) {
			user.setmStringBirthday(boUserBaseInfo.mStringBirthday);
		}
		if (boUserBaseInfo.mMobile != null) {
			user.setMobile(boUserBaseInfo.mMobile);
		}
		if (boUserBaseInfo.mTelephone != null) {
			user.setTelephone(boUserBaseInfo.mTelephone);
		}
		if (boUserBaseInfo.mEmail != null) {
			user.setEmail(boUserBaseInfo.mEmail);
		}
		if (boUserBaseInfo.mFax != null) {
			user.setFax(boUserBaseInfo.mFax);
		}
		if (boUserBaseInfo.mJob != null) {
			user.setJob(boUserBaseInfo.mJob);
		}

		if (boUserBaseInfo.mAddress != null) {
			user.setAddress(boUserBaseInfo.mAddress);
		}

		if (boUserBaseInfo.mBirthday != null) {
			user.setBirthday(boUserBaseInfo.mBirthday);
		}

		if (!isContained) {
			mUserHolder.put(user.getmUserId(), user);
		}
		return user;
	}

	/**
	 * Group information is server active call, we can't request from server
	 * directly.<br>
	 * Only way to get group information is waiting for server call.<br>
	 * So if this function return null, means service doesn't receive any call
	 * from server. otherwise server already sent group information to service.
	 * <br>
	 * Notice: maybe you didn't receive broadcast forever, because this
	 * broadcast is sent before you register
     * FIXME 好友组需要处理同步问题
	 *
	 * @param groupType
	 * @return return null means server didn't send group information to
	 *         service.
	 */
	public List<Group> getGroup(int groupType) {
        List<Group> ct = new CopyOnWriteArrayList<>();
		switch (groupType) {
		case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
            ct.addAll(this.mOrgGroup);
            break;
		case V2GlobalConstants.GROUP_TYPE_CONTACT:
//            ct.addAll(this.mContactsGroup);
            return this.mContactsGroup;
		case V2GlobalConstants.GROUP_TYPE_CROWD:
			ct.addAll(this.mCrowdGroup);
            break;
		case V2GlobalConstants.GROUP_TYPE_CONFERENCE:
			List<Group> confL = new ArrayList<>();
			confL.addAll(this.mConfGroup);
			Collections.sort(confL);
            ct.addAll(confL);
            break;
		case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
            ct.addAll(this.mDiscussionBoardGroup);
            break;
		default:
			throw new RuntimeException("Unkonw type");
		}
        return ct;
	}

	/**
	 * @param gId
	 * @return {@see com.V2.jni.V2GlobalConstants}
	 */
	public Group getGroupById(long gId) {
		return mGroupHolder.get(gId);
	}

	public Group getGroupById(int groupType, long gId) {
		return mGroupHolder.get(gId);
	}

	public Group getRootGroup(int groupType) {
		if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
			return mOrgGroup.get(0);
		} else if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
			return mContactsGroup.get(0);
		}
		return null;
	}

	private void populateGroup(int groupType, Group parent, Set<V2Group> list) {
		for (V2Group vg : list) {
			Group cache = mGroupHolder.get(vg.id);
			Group g;
			if (cache != null) {
				g = cache;
				// Update new name
				cache.setName(vg.getName());
			} else {
				if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
					User owner = GlobalHolder.getInstance().getUser(vg.owner.mId);
					g = new CrowdGroup(vg.id, vg.getName(), owner);
				} else if (groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
					User owner = GlobalHolder.getInstance().getUser(vg.owner.mId);
					User chairMan = GlobalHolder.getInstance().getUser(vg.chairMan.mId);
					g = new ConferenceGroup(vg.id, vg.getName(), owner, vg.createTime, chairMan);
				} else if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
					g = new OrgGroup(vg.id, vg.getName());
				} else if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
					g = new ContactGroup(vg.id, vg.getName());
				} else {
					throw new RuntimeException(" Can not support this type");
				}
			}

			parent.addGroupToGroup(g);
			mGroupHolder.put(g.getGroupID(), g);
			populateGroup(groupType, g, vg.childs);
		}
	}

	public void addGroupToList(int groupType, Group g) {
		if (groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
			mConfGroup.add(g);
		} else if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
			this.mCrowdGroup.add(g);
		} else if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
			mContactsGroup.get(0).addGroupToGroup(g);
			this.mContactsGroup.add(g);
		} else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
			this.mDiscussionBoardGroup.add(g);
		}
		mGroupHolder.put(g.getGroupID(), g);
	}

	/**
	 * Add user collections to group collections
	 *
	 * @param gList
	 * @param uList
	 * @param belongGID
	 */
	public void addUserToGroup(List<Group> gList, List<User> uList, long belongGID) {
		for (Group g : gList) {
			if (belongGID == g.getGroupID()) {
				g.addUserToGroup(uList);
				return;
			}
			addUserToGroup(g.getChildGroup(), uList, belongGID);
		}
	}

	/**
	 * Update group information according server's side push data
	 *
	 * @param gType
	 * @param list
	 *
	 */
	public void updateGroupList(int gType, List<V2Group> list) {
		for (V2Group vg : list) {
			Group cache = mGroupHolder.get(vg.id);
			if (cache != null) {
				continue;
			}

			if (vg.getName() == null)
				V2Log.e("parse the group name is wroing...the group is :" + vg.id);

			Group g = null;
			if (gType == V2GlobalConstants.GROUP_TYPE_CROWD) {
				boolean flag = true;
				for (Group group : mCrowdGroup) {
					if (group.getGroupID() == vg.id) {
						flag = false;
					}
				}

				if (flag) {
					User owner = GlobalHolder.getInstance().getUser(vg.owner.mId);
					g = new CrowdGroup(vg.id, vg.getName(), owner);
					CrowdGroup crowd = (CrowdGroup) g;
					crowd.setBrief(vg.getBrief());
					crowd.setAnnouncement(vg.getAnnounce());
					crowd.setAuthType(AuthType.fromInt(vg.authType));
					crowd.setCreateDate(new Date(GlobalConfig.getGlobalServerTime()));
					mCrowdGroup.add(g);
				}
			} else if (gType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
				User owner = GlobalHolder.getInstance().getUser(vg.owner.mId);
				User chairMan = GlobalHolder.getInstance().getUser(vg.chairMan.mId);
				g = new ConferenceGroup(vg.id, vg.getName(), owner, vg.createTime, chairMan);
				mConfGroup.add(g);
			} else if (gType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				g = new OrgGroup(vg.id, vg.getName());
				mOrgGroup.add(g);
			} else if (gType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
				g = new ContactGroup(vg.id, vg.getName());
				if (vg.isDefault) {
					((ContactGroup) g).setDefault(true);
					g.setName(GlobalConfig.Resource.CONTACT_DEFAULT_GROUP_NAME);
				}
				mContactsGroup.add(g);
			} else if (gType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
				User owner = GlobalHolder.getInstance().getUser(vg.owner.mId);
				g = new DiscussionGroup(vg.id, vg.getName(), owner, new Date(GlobalConfig.getGlobalServerTime()));
				mDiscussionBoardGroup.add(g);
			} else {
				throw new RuntimeException(" Can not support this type");
			}

            if(g != null){
                mGroupHolder.put(g.getGroupID(), g);
                populateGroup(gType, g, vg.childs);
            }
		}
	}

	/**
	 * Find all types of group information according to group ID
	 *
	 * @param gid
	 * @return null if doesn't find group, otherwise return Group information
	 *
	 * @see Group
	 */
	public Group findGroupById(long gid) {
		return mGroupHolder.get(gid);
	}

	public boolean removeGroup(int gType, long gid) {
		List<Group> list = null;
		boolean isContact = false;
		if (gType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
			list = mConfGroup;
		} else if (gType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
			isContact = true;
			list = mContactsGroup;
		} else if (gType == V2GlobalConstants.GROUP_TYPE_CROWD) {
			list = mCrowdGroup;
		} else if (gType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
			list = mOrgGroup;
		} else if (gType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
			list = mDiscussionBoardGroup;
		}
		mGroupHolder.remove(gid);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				Group g = list.get(i);
				if (g.getGroupID() == gid) {
					list.remove(g);
					if (isContact) {
						list.get(0).removeGroupFromGroup(g);
					}
					return true;
				}
			}
		}
		return false;
	}

	public void removeGroupUser(long gid, long uid) {
		Group g = this.findGroupById(gid);
		if (g != null) {
			g.removeUserFromGroup(uid);
		} else {
			V2Log.e("GlobalHolder removeGroupUser",
					" Remove user failed ! get group is null " + " group id is : " + gid + " user id is : " + uid);
		}
	}

	/**
	 * Add user collections to group collections
	 *
	 * @param uList
	 * @param belongGID
	 */
	public void addUserToGroup(List<User> uList, long belongGID) {
		Group g = findGroupById(belongGID);
		if (g == null) {
			V2Log.e("Doesn't receive group<" + belongGID + "> information yet!");
			return;
		}
		g.addUserToGroup(uList);
	}

	public void addUserToGroup(User u, long belongGID) {
		Group g = findGroupById(belongGID);
		if (g == null) {
			V2Log.e("Doesn't receive group<" + belongGID + "> information yet!");
			return;
		}
		g.addUserToGroup(u);
	}

	/**
	 * Get user's video device according to user id.<br>
	 * This function never return null, even through we don't receive video
	 * device data from server.
	 *
	 * @param uid
	 *            user's id
	 * @return list of user device
	 */
	public List<UserDeviceConfig> getAttendeeDevice(long uid) {
		List<UserDeviceConfig> list = mUserDeviceList.get(uid);
		if (list == null) {
			return new ArrayList<>();
		}

		return list;
	}

	public UserDeviceConfig getUserDefaultDevice(long uid) {
		List<UserDeviceConfig> list = mUserDeviceList.get(uid);
		if (list == null) {
			return null;
		}
		for (UserDeviceConfig udc : list) {
			if (udc.isDefault()) {
				return udc;
			}
		}

		if (list.size() > 0) {
			V2Log.e("Not found default device, use first device !");
			return list.iterator().next();
		}
		return null;
	}

	/**
	 * Update user video device and clear existed user device first
	 *
	 * @param key
	 * @param udcList
	 */
	public void updateUserDevice(long key, List<UserDeviceConfig> udcList) {
		List<UserDeviceConfig> list = mUserDeviceList.get(key);
		if (list != null) {
			list.clear();
		} else {
			list = new ArrayList<>();
			mUserDeviceList.put(key, list);
		}

		list.addAll(udcList);
	}

	public void setChatState(boolean flag, long remoteID) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (flag) {
				st |= GlobalState.STATE_CHAT_INTERFACE_OPEN;
			} else {
				st &= (~GlobalState.STATE_CHAT_INTERFACE_OPEN);
			}
			mState.setState(st);
			mState.setRemoteChatUid(remoteID);
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * Set current app audio state, also set voice connected state
	 *
	 * @param flag
	 * @param uid
	 */
	public void setAudioState(boolean flag, long uid) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (flag) {
				st |= GlobalState.STATE_IN_AUDIO_CONVERSATION;
			} else {
				st &= (~GlobalState.STATE_IN_AUDIO_CONVERSATION);
			}
			mState.setState(st);
			mState.setUid(uid);
			setVoiceConnectedState(flag);
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * set cuurent app video state
	 *
	 * @param flag
	 * @param uid
	 */
	public void setVideoState(boolean flag, long uid) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (flag) {
				st |= GlobalState.STATE_IN_VIDEO_CONVERSATION;
			} else {
				st &= (~GlobalState.STATE_IN_VIDEO_CONVERSATION);
			}
			mState.setState(st);
			mState.setUid(uid);
		} finally {
			mLock.unlock();
		}
	}

	public void setMeetingState(boolean flag, long gid) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (flag) {
				st |= GlobalState.STATE_IN_MEETING_CONVERSATION;
			} else {
				st &= (~GlobalState.STATE_IN_MEETING_CONVERSATION);
			}
			mState.setState(st);
			mState.setGid(gid);
		} finally {
			mLock.unlock();
		}
	}

	public void setVoiceConnectedState(boolean flag) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (flag) {
				st |= GlobalState.STATE_IN_VOICE_CONNECTED;
			} else {
				st &= (~GlobalState.STATE_IN_VOICE_CONNECTED);
			}
			this.mState.setState(st);
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * Set wired headset state
	 *
	 * @param flag
	 */
	public void setWiredHeadsetState(boolean flag) {
		mLock.lock();
		try {
			mState.setWiredHeadsetState(flag);
		} finally {
			mLock.unlock();
		}
	}

	public void setServerConnection(boolean connected) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (connected) {
				st |= GlobalState.STATE_SERVER_CONNECTED;
			} else {
				st &= (~GlobalState.STATE_SERVER_CONNECTED);
			}
			mState.setState(st);
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * Set bluetooth headset matched or not
	 *
	 * @param flag
	 */
	public void setBluetoothHeadset(boolean flag) {
		mLock.lock();
		try {
			mState.setBluetoothHeadset(flag);
		} finally {
			mLock.unlock();
		}
	}

	public void setGroupLoaded() {
		mLock.lock();
		try {
			int st = mState.getState();
			mState.setState(st | GlobalState.STATE_SERVER_GROUPS_LOADED);
		} finally {
			mLock.unlock();
		}
	}

	public void setOfflineLoaded(boolean isLoad) {
		mLock.lock();
		try {
			int st = this.mState.getState();
			if (isLoad) {
				st |= GlobalState.STATE_SERVER_OFFLINE_MESSAGE_LOADED;
			} else {
				st &= (~GlobalState.STATE_SERVER_OFFLINE_MESSAGE_LOADED);
			}
			this.mState.setState(st);
		} finally {
			mLock.unlock();
		}
	}

	public void setP2pAVNeedStickyBraodcast(boolean p2pAVNeedStickyBraodcast) {
		this.p2pAVNeedStickyBraodcast = p2pAVNeedStickyBraodcast;
	}

	public void setMessageShowTime(Context mContext, int groupType, long groupID, long remoteUserID, VMessage msg) {
		VMessage lastMsg = ChatMessageProvider.getNewestShowTimeMessage(groupType, groupID, remoteUserID);
		if (lastMsg == null) {
			msg.setShowTime(true);
		} else {
			long lastDateLong = lastMsg.getmDateLong();
			if (msg.getmDateLong() - lastDateLong > 60000) {
				msg.setShowTime(true);
			} else {
				msg.setShowTime(false);
			}
		}
	}

	public void setCurrentMeetingID(long mCurrentMeetingID) {
		this.mCurrentMeetingID = mCurrentMeetingID;
	}

	public void setDataBaseTableCacheName(List<String> dataBaseTableCacheName) {
		this.mDataBaseTableCacheName = dataBaseTableCacheName;
	}

	public boolean checkServerConnected(Context mContext) {
		mLock.lock();
		try {
//			if (LocalSharedPreferencesStorage.checkCurrentAviNetwork(mContext)) {
				if (!mState.isConnectedServer()) {
					Toast.makeText(mContext, R.string.error_connect_to_server, Toast.LENGTH_SHORT).show();
					return true;
				}
				return false;
//			} else {
//				Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
//				return true;
//			}
		} finally {
			mLock.unlock();
		}
	}

	public void checkUserName(long userID) {
		mLock.lock();
		try {
			User user = getUser(userID);
			if (TextUtils.isEmpty(user.getNickName())) {
				if (GlobalHolder.getInstance().getGlobalState().isGroupLoaded()) {
					// if receive this callback , the dirty change false;
					V2Log.e("user display name is null , invoke");
					ImRequest.getInstance().ImGetUserBaseInfo(userID);
				}
			}
		} finally {
			mLock.unlock();
		}
	}

	public boolean isChatInterfaceOpen(long remoteID) {
		mLock.lock();
		try {
			return mState.isChatInterfaceOpen() && this.mState.getRemoteChatUid() == remoteID;
		} finally {
			mLock.unlock();
		}
	}

	public boolean isVoiceConnected() {
		mLock.lock();
		try {
			return this.mState.isVoiceConnected();
		} finally {
			mLock.unlock();
		}
	}

	public boolean isInAudioCall() {
		mLock.lock();
		try {
			return this.mState.isInAudioCall();
		} finally {
			mLock.unlock();
		}
	}

	public boolean isInVideoCall() {
		mLock.lock();
		try {
			return mState.isInVideoCall();
		} finally {
			mLock.unlock();
		}
	}

	public boolean isInMeeting() {
		mLock.lock();
		try {
			return mState.isInMeeting();
		} finally {
			mLock.unlock();
		}
	}

	public boolean isServerConnected() {
		mLock.lock();
		try {
			return mState.isConnectedServer();
		} finally {
			mLock.unlock();
		}
	}

	public boolean isFriend(User user) {
		if (user == null) {
			V2Log.e("GlobalHolder isFriend ---> get user is null , please check conversation user is exist");
			return false;
		}
		return isFriend(user.getmUserId());
	}

	public boolean isFriend(long nUserId) {
		mLock.lock();
		try {
			List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
			if (friendGroup!=null && friendGroup.size() >= 0) {
				for (Group friend : friendGroup) {
					List<User> users = friend.getUsers();
					for (User friendUser : users) {
						if (nUserId == friendUser.getmUserId()) {
							return true;
						}
					}
				}
			}
			return false;
		} finally {
			mLock.unlock();
		}
	}

	public boolean isOfflineLoaded() {
		mLock.lock();
		try {
			return mState.isOfflineLoaded();
		} finally {
			mLock.unlock();
		}
	}

	public boolean isIllegalState() {
		if (mGroupHolder.size() > 0)
			return true;
		else
			return false;
	}

	public boolean isP2pAVNeedStickyBraodcast() {
		return p2pAVNeedStickyBraodcast;
	}

	/**
	 * Get current application state copy
	 *
	 * @return
	 */
	public GlobalState getGlobalState() {
		return new GlobalState(this.mState);
	}

	public long getCurrentMeetingID() {
		return mCurrentMeetingID;
	}

	public Bitmap getUserAvatar(long key) {
		return mAvatarBmHolder.get(key);
	}

	public Bitmap getOrgUserAvatar(long key){
		return mOrgAvatarBmHolder.get(key);
	}

	public List<String> getDataBaseTableCacheName() {
		return mDataBaseTableCacheName;
	}

	public void changeGlobleTransSizeToZero(long key) {
		Integer transing = mTransingFiles.get(GlobalHolder.getInstance().getCurrentUserId());
		if (transing != null) {
			for (int i = 0; i < mTransingTag.size(); i++) {
				long temp = mTransingTag.get(i);
				if (temp == key) {
					mTransingTag.remove(temp);
					transing--;
				}
			}
			mTransingFiles.put(GlobalHolder.getInstance().getCurrentUserId(), transing);
		}
	}

	public boolean changeGlobleTransFileMember(final int transType, final Context mContext, boolean isAdd, Long key,
			String tag) {
		long targetKey = GlobalHolder.getInstance().getCurrentUserId();
		Map<Long, Integer> transingCollection = getFileTypeColl(transType);
		Integer transing = transingCollection.get(targetKey);
		String typeString;
		if (transType == V2GlobalConstants.FILE_TRANS_SENDING)
			typeString = mContext.getResources().getString(R.string.application_global_holder_send_or_upload);
		else
			typeString = mContext.getResources().getString(R.string.application_global_holder_download);
		if (transing == null) {
			if (isAdd) {
				V2Log.d("TRANSING_FILE_SIZE",
						tag + " --> ID为- " + targetKey + " -的用户或群 , " + "传输类型 : " + typeString + " 正在传输文件加1 , 当前数量为1");
				transing = 1;
				transingCollection.put(targetKey, transing);
				mTransingTag.add(key);
			}
			return true;
		} else {
			if (isAdd) {
				if (transing >= GlobalConfig.MAX_TRANS_FILE_SIZE) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							Looper.prepare();
							// if(transType ==
							// V2GlobalConstants.FILE_TRANS_SENDING)
							// Toast.makeText(mContext,
							// "发送文件个数已达上限，当前正在传输的文件数量已达5个",
							// Toast.LENGTH_LONG).show();
							// else
							// Toast.makeText(mContext,
							// "下载文件个数已达上限，当前正在下载的文件数量已达5个",
							// Toast.LENGTH_LONG).show();
							Toast.makeText(mContext, R.string.application_global_holder_limit_number, Toast.LENGTH_LONG)
									.show();
							Looper.loop();
						}
					}).start();
					return false;
				} else {
					transing = transing + 1;
					V2Log.d("TRANSING_FILE_SIZE", tag + " --> ID为- " + targetKey + " -的用户或群 , " + "传输类型 : " + typeString
							+ " 正在传输文件加1 , 当前数量为: " + transing);
					transingCollection.put(targetKey, transing);
					mTransingTag.add(key);
					return true;
				}
			} else {
				if (transing == 0)
					return false;
				transing = transing - 1;
				V2Log.d("TRANSING_FILE_SIZE", tag + " --> ID为- " + targetKey + " -的用户或群 , " + "传输类型 : " + typeString
						+ " 正在传输文件减1 , 当前数量为: " + transing);
				transingCollection.put(targetKey, transing);
				mTransingTag.remove(key);
				return true;
			}
		}

	}

	private Map<Long, Integer> getFileTypeColl(int transType) {
		return mTransingFiles;
		// if(transType == V2GlobalConstants.FILE_TRANS_SENDING)
		// return GlobalConfig.mTransingFiles;
		// else
		// return GlobalConfig.mDownLoadingFiles;
	}

	/**
	 * Use to update cache avatar
	 */
	private BitmapManager.BitmapChangedListener bitmapChangedListener = new BitmapManager.BitmapChangedListener() {

		@Override
		public void notifyAvatarChanged(User user, Bitmap newAvatar) {
			Long key = user.getmUserId();
			if (mAvatarBmHolder != null) {
				Bitmap cache = mAvatarBmHolder.get(key);
				if (cache != null && !cache.isRecycled()) {
					cache.recycle();
				}
				mAvatarBmHolder.put(key, newAvatar);
			}

			if (mOrgAvatarBmHolder != null) {
				Bitmap cache = mOrgAvatarBmHolder.get(key);
				if (cache != null && !cache.isRecycled()) {
					cache.recycle();
				}
				Bitmap compressdAvatar = BitmapUtil.
						getCompressdAvatar(V2GlobalConstants.AVATAR_ORG, newAvatar);
				mOrgAvatarBmHolder.put(key, compressdAvatar);
			}


		}

	};

}
