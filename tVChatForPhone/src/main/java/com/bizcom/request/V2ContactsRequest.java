package com.bizcom.request;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.V2.jni.GroupRequest;
import com.V2.jni.callbacAdapter.GroupRequestCallbackAdapter;
import com.V2.jni.ind.BoUserInfoShort;
import com.V2.jni.ind.V2Group;
import com.bizcom.request.jni.GroupServiceJNIResponse;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.request.jni.RequestConfCreateResponse;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.V2Log;
import com.bizcom.util.XmlAttributeExtractor;
import com.bizcom.vo.ContactGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author jiangzhen
 * 
 */
public class V2ContactsRequest extends V2AbstractHandler {

    private static final String TAG = "V2ContactsRequest";

    private static final int CREATE_CONTACTS_GROUP = 10;
    private static final int UPDATE_CONTACTS_GROUP = 11;
    private static final int DELETE_CONTACTS_GROUP = 12;
    private static final int UPDATE_CONTACT_BELONGS_GROUP = 13;
    private static final int DELETE_CONTACT_USER = 14;
    private static final int ADD_CONTACT_USER = 15;

    private long mWatingGid = 0;
	private long mWatingUserID = 0;

	private GroupRequestCB crCB;

	public V2ContactsRequest() {
		super();
		crCB = new GroupRequestCB(this);
		GroupRequest.getInstance().addCallback(crCB);
	}

	/**
	 * @comment-user:wenzl 2014年9月19日
	 * @overview:
	 * 
	 * @param contactGroup
	 * @param user
	 * @param additInfo
	 * @param commentName
	 * @return:
	 */
	public void addContact(Group contactGroup, User user, String additInfo, String commentName, HandlerWrap caller) {
		if (TextUtils.isEmpty(additInfo))
			additInfo = "";
		else
			additInfo = EscapedcharactersProcessing.convert(additInfo);
		commentName = EscapedcharactersProcessing.convert(commentName);

		String groupInfo = "<friendgroup" + " id='" + contactGroup.getGroupID() + "'/>";
		String userInfo = "<userlist>" + "<user id='" + user.getmUserId() + "'" + " commentname='" + commentName
				+ "'></user>" + "</userlist>";
        GroupRequest.getInstance().GroupInviteUsers(V2GlobalConstants.GROUP_TYPE_CONTACT, groupInfo, userInfo, additInfo);
        if(caller != null){
            mWatingUserID = user.getmUserId();
            initTimeoutMessage(ADD_CONTACT_USER, DEFAULT_TIME_OUT_SECS, caller);
        }
	}

	/**
	 * @comment-user:wenzl 2014年9月19日
	 * @overview:删除联系人
	 * 
	 * @param user
	 * @return:
	 */
	public void delContact(User user, HandlerWrap caller) {
		if (user == null)
			return;

		mWatingUserID = user.getmUserId();

		boolean isExist = false;
		User remoed = GlobalHolder.getInstance().getUser(mWatingUserID);
		Iterator<Group> iterator = remoed.getBelongsGroup().iterator();
		while (iterator.hasNext()) {
			Group temp = iterator.next();
			if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
				isExist = true;
				break;
			}
		}

		if (!isExist) {
			JNIResponse jniRes = new GroupServiceJNIResponse(GroupServiceJNIResponse.Result.SUCCESS);
			callerSendMessage(caller, jniRes);
			return;
		}

		long nGroupID = -1;
		boolean ret = false;
		boolean isAdd = false;
		List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
		for (Group group : friendGroup) {
			if ((group.findUser(user)) != null) {
				nGroupID = group.getGroupID();
				Iterator<Group> itor = user.getBelongsGroup().iterator();
				while (itor.hasNext()) {
					Group temp = itor.next();
					if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
						nGroupID = temp.getGroupID();
						isAdd = true;
					}
				}

				if (!isAdd) {
					user.addUserToGroup(group);
				}
				ret = true;
				break;
			}
		}

		if (!ret) {
			V2Log.e("ContactsService delContact --> ",
					"Delete User Failed... Because The user isn't belong to CONTACT GROUP!");
			return;
		}

		initTimeoutMessage(DELETE_CONTACT_USER, DEFAULT_TIME_OUT_SECS, caller);
		long nUserID = user.getmUserId();
		GlobalHolder.getInstance().getUser(nUserID).setCommentName(null);
		GroupRequest.getInstance().GroupKickUser(V2GlobalConstants.GROUP_TYPE_CONTACT, nGroupID, nUserID);
	}

	/**
	 * @comment-user:wenzl 2014年9月19日
	 * @overview:同意被加为联系人
	 * 
	 * @param groupId
	 * @param nUserID
	 * @return:
	 */
	public void acceptAddedAsContact(long groupId, long nUserID) {
		GroupRequest.getInstance().GroupAcceptInvite(V2GlobalConstants.GROUP_TYPE_CONTACT, groupId, nUserID);
	}

	/**
	 * @comment-user:wenzl 2014年9月19日
	 * @overview:拒绝被加为联系人
	 * 
	 * @param nGroupID
	 * @param nUserID
	 * @param reason
	 * @return:
	 */
	public void refuseAddedAsContact(long nGroupID, long nUserID, String reason) {
		reason = EscapedcharactersProcessing.convert(reason);
		GroupRequest.getInstance().GroupRefuseInvite(V2GlobalConstants.GROUP_TYPE_CONTACT, nGroupID, nUserID, reason);
	}

	/**
	 * Create contacts group
	 * 
	 * @param group
	 * @param caller
	 */
	public void createGroup(ContactGroup group, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { group })) {
			return;
		}

		// Initialize time out message
		initTimeoutMessage(CREATE_CONTACTS_GROUP, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupCreate(group.getGroupType(), group.toXml(), "");
	}

	/**
	 * Update contacts group
	 * 
	 * @param group
	 * @param caller
	 */
	public void updateGroup(ContactGroup group, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { group })) {
			return;
		}

		mWatingGid = group.getGroupID();
		// Initialize time out message
		initTimeoutMessage(UPDATE_CONTACTS_GROUP, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupModify(group.getGroupType(), group.getGroupID(), group.toXml());
	}

	/**
	 * 
	 * @param group
	 * @param caller
	 */
	public void removeGroup(ContactGroup group, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { group })) {
			return;
		}

		mWatingGid = group.getGroupID();
		List<Group> list = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
		// Update all users which belongs this group to root group
		if (list.size() > 0) {
			ContactGroup defaultGroup = (ContactGroup) list.get(0).getChildGroup().get(0);
			List<User> userList = group.getUsers();
			for (int i = 0; i < userList.size(); i++) {
				User user = userList.get(i);
				GroupRequest.getInstance().GroupMoveUserTo(group.getGroupType(), group.getGroupID(),
						defaultGroup.getGroupID(), user.getmUserId());
				group.removeUserFromGroup(user);
				defaultGroup.addUserToGroup(user);
			}
		}
		// Initialize time out message
		initTimeoutMessage(DELETE_CONTACTS_GROUP, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupDestroy(group.getGroupType(), group.getGroupID());
	}

	/**
	 * Update User to specific group and remove user from origin group.<br>
	 * Add new contact if srcGroup is null.<br>
	 * Remove contact if desGroup is null.<br>
	 * 
	 * @param desGroup
	 *            if desGroup is null, remove contact from group
	 * @param srcGroup
	 *            if srcGroup is null, means sendFriendToTv new contact to group
	 * @param user
	 * @param caller
	 */
	public void updateUserGroup(ContactGroup desGroup, ContactGroup srcGroup, User user, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { user })) {
			return;
		}
		// If srcGroup is null, means sendFriendToTv new contact
		if (srcGroup == null && desGroup != null) {
			GroupRequest.getInstance().GroupInviteUsers(V2GlobalConstants.GROUP_TYPE_CONTACT,
					"<friendgroup id =\"" + desGroup.getGroupID() + "\" />",
					XmlAttributeExtractor.buildAttendeeUsersXml(user), "");
			// remove contact
		} else if (desGroup == null && srcGroup != null) {
			GroupRequest.getInstance().GroupKickUser(V2GlobalConstants.GROUP_TYPE_CONTACT, srcGroup.getGroupID(),
					user.getmUserId());
			// move contact to other group
		} else {
			// Initialize time out message
			initTimeoutMessage(UPDATE_CONTACT_BELONGS_GROUP, DEFAULT_TIME_OUT_SECS, caller);
			GroupRequest.getInstance().GroupMoveUserTo(V2GlobalConstants.GROUP_TYPE_CONTACT, srcGroup.getGroupID(),
					desGroup.getGroupID(), user.getmUserId());
			// Update cache
			srcGroup.removeUserFromGroup(user);
			desGroup.addUserToGroup(user);
		}
	}

	@Override
	public void clearCalledBack() {
		GroupRequest.getInstance().removeCallback(crCB);
	}

	class GroupRequestCB extends GroupRequestCallbackAdapter {

		private Handler mCallbackHandler;

		public GroupRequestCB(Handler callbackHandler) {
			mCallbackHandler = callbackHandler;
		}

		@Override
		public void OnMoveUserToGroup(int groupType, long srcGroupID, long dstGroupID, long nUserID) {
			if (groupType != V2GlobalConstants.GROUP_TYPE_CONTACT) {
				return;
			}
			GroupServiceJNIResponse jniRes = new GroupServiceJNIResponse(Result.SUCCESS);
			Message.obtain(mCallbackHandler, UPDATE_CONTACT_BELONGS_GROUP, jniRes).sendToTarget();
		}

		@Override
		public void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
			// If equals, means we are waiting for modified response
			if (nGroupID == mWatingGid) {
				ContactGroup conGroup = (ContactGroup) GlobalHolder.getInstance()
						.getGroupById(V2GlobalConstants.GROUP_TYPE_CONTACT, nGroupID);
				if (conGroup != null) {
					String name = XmlAttributeExtractor.extractAttribute(sXml, "name");
					conGroup.setName(name);
					JNIResponse jniRes = new GroupServiceJNIResponse(RequestConfCreateResponse.Result.SUCCESS);
					Message.obtain(mCallbackHandler, UPDATE_CONTACTS_GROUP, jniRes).sendToTarget();
				}
				mWatingGid = 0;
			}
		}

		@Override
		public void OnDelGroupCallback(int groupType, long nGroupID, boolean bMovetoRoot) {
			if (groupType != V2GlobalConstants.GROUP_TYPE_CONTACT) {
				return;
			}

			// If equals, means we are waiting for modified response
			if (nGroupID == mWatingGid) {
				JNIResponse jniRes = new GroupServiceJNIResponse(RequestConfCreateResponse.Result.SUCCESS,
						new ContactGroup(nGroupID, null));
				Message.obtain(mCallbackHandler, DELETE_CONTACTS_GROUP, jniRes).sendToTarget();
				mWatingGid = 0;
				GlobalHolder.getInstance().removeGroup(V2GlobalConstants.GROUP_TYPE_CONTACT, nGroupID);
			}
		}

		@Override
		public void OnDelGroupUserCallback(int groupType, long nGroupID, long nUserID) {
			if (groupType != V2GlobalConstants.GROUP_TYPE_CONTACT) {
				return;
			}

			if (mWatingUserID == nUserID) {
				JNIResponse jniRes = new GroupServiceJNIResponse(GroupServiceJNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, DELETE_CONTACT_USER, jniRes).sendToTarget();
			}
		}

		@Override
		public void onAddGroupInfo(int groupType, long nParentID, long nGroupID, String sXml) {
			if (TextUtils.isEmpty(sXml)) {
				return;
			}

			if (groupType == V2Group.TYPE_CONTACTS_GROUP) {
				String gid = XmlAttributeExtractor.extract(sXml, " id='", "'");
				String name = XmlAttributeExtractor.extract(sXml, " name='", "'");
				if (TextUtils.isEmpty(gid) || TextUtils.isEmpty(name)) {
					V2Log.e(V2Log.JNISERVICE_CALLBACK,
							"V2ContactsRequest onAddGroupInfo -> 解析xml失败，没有获取到creatoruserid, xml : " + sXml);
					return;
				}

				Group g = new ContactGroup(Long.valueOf(gid), name);
				GlobalHolder.getInstance().addGroupToList(g.getGroupType(), g);
				JNIResponse jniRes = new GroupServiceJNIResponse(GroupServiceJNIResponse.Result.SUCCESS, g);
				Message.obtain(mCallbackHandler, CREATE_CONTACTS_GROUP, jniRes).sendToTarget();
			}
		}

        @Override
        public void OnAddGroupUserInfoCallback(int groupType, long nGroupID, String sXml) {
            BoUserInfoShort boUserInfoShort;
            try {
                boUserInfoShort = BoUserInfoShort.parserXml(sXml);
            } catch (Exception e) {
                e.printStackTrace();
                V2Log.e(TAG , "OnAddGroupUserInfoCallback -> parse xml failed ...get null user : " + sXml);
                return;
            }
            if (boUserInfoShort == null) {
                V2Log.e(TAG , "OnAddGroupUserInfoCallback -> parse xml failed ...get null user : " + sXml);
                return;
            }

            if(boUserInfoShort.mId == mWatingUserID){
                mWatingUserID = 0;
                JNIResponse jniRes = new JNIResponse(Result.SUCCESS);
                Message.obtain(mCallbackHandler, ADD_CONTACT_USER, jniRes).sendToTarget();
            }
        }
    }
}
