package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.GroupRequestCallback;
import com.bizcom.util.V2Log;

public abstract class GroupRequestCallbackAdapter implements GroupRequestCallback {
	@Override
	public void OnGetGroupUserInfoCallback(int groupType, long nGroupID, String sXml) {
		V2Log.jniCall("OnGetGroupUserInfo",
				" groupType = " + groupType + " nGroupID = " + nGroupID + " sXml = " + sXml);
	}

	@Override
	public void OnDelGroupCallback(int groupType, long nGroupID, boolean bMovetoRoot) {
		V2Log.jniCall("OnDelGroup",
				" groupType = " + groupType + " nGroupID = " + nGroupID + " bMovetoRoot = " + bMovetoRoot);
	}

	@Override
	public void OnDelGroupUserCallback(int groupType, long nGroupID, long nUserID) {
		V2Log.jniCall("OnDelGroupUser",
				" groupType = " + groupType + " nGroupID = " + nGroupID + " nUserID = " + nUserID);
	}

	@Override
	public void OnAddGroupUserInfoCallback(int groupType, long nGroupID, String sXml) {
		V2Log.jniCall("OnAddGroupUserInfo",
				" groupType = " + groupType + " nGroupID = " + nGroupID + " sXml = " + sXml);
	}

	@Override
	public void onAddGroupInfo(int groupType, long nParentID, long nGroupID, String sXml) {
		V2Log.jniCall("OnAddGroupInfo", " groupType = " + groupType + " nParentID = " + nParentID + " nGroupID = "
				+ nGroupID + " sXml = " + sXml);
	}

	@Override
	public void OnAcceptInviteJoinGroup(int groupType, long groupId, long nUserID) {
		V2Log.jniCall("OnAcceptInviteJoinGroup",
				" groupType = " + groupType + " groupId = " + groupId + " nUserID = " + nUserID);
	}

	@Override
	public void OnKickGroupUser(int groupType, long groupId, long nUserId) {
		V2Log.jniCall("OnKickGroupUser",
				" groupType = " + groupType + " groupId = " + groupId + " nUserId = " + nUserId);
	}

	@Override
	public void OnJoinGroupError(int eGroupType, long nGroupID, int nErrorNo) {
		V2Log.jniCall("OnJoinGroupError",
				" eGroupType = " + eGroupType + " nGroupID = " + nGroupID + " nErrorNo = " + nErrorNo);
	}

	@Override
	public void OnInviteJoinGroup(int groupType, String groupInfo, String userInfo, String additInfo) {
		V2Log.jniCall("OnInviteJoinGroup", " groupType = " + groupType + " groupInfo = " + groupInfo + " userInfo = "
				+ userInfo + " additInfo = " + additInfo);
	}

	@Override
	public void OnRefuseInviteJoinGroup(int groupType, long nGroupID, long nUserID, String reason) {
		V2Log.jniCall("OnRefuseInviteJoinGroup", " groupType = " + groupType + " nGroupID = " + nGroupID + " nUserID = "
				+ nUserID + " reason = " + reason);
	}

	@Override
	public void OnMoveUserToGroup(int groupType, long srcGroupID, long dstGroupID, long nUserID) {
		V2Log.jniCall("OnMoveUserToGroup", " groupType = " + groupType + " srcGroupID = " + srcGroupID
				+ " dstGroupID = " + dstGroupID + " nUserID = " + nUserID);
	}

	@Override
	public void OnApplyJoinGroup(int groupType, long nGroupID, String userInfo, String reason) {
		V2Log.jniCall("OnApplyJoinGroup", " groupType = " + groupType + " nGroupID = " + nGroupID + " userInfo = "
				+ userInfo + " reason = " + reason);
	}

	@Override
	public void OnAcceptApplyJoinGroup(int groupType, String sXml) {
		V2Log.jniCall("OnAcceptApplyJoinGroup", " groupType = " + groupType + " sXml = " + sXml);
	}

	@Override
	public void OnRefuseApplyJoinGroup(int groupType, String sXml, String reason) {
		V2Log.jniCall("OnRefuseApplyJoinGroup",
				" groupType = " + groupType + " sXml = " + sXml + " reason = " + reason);
	}

	@Override
	public void OnGroupCreateWBoard(int eGroupType, long nGroupID, String szWBoardID, int nWhiteIndex) {
		V2Log.jniCall("OnGroupCreateWBoard", " eGroupType = " + eGroupType + " nGroupID = " + nGroupID
				+ " szWBoardID = " + szWBoardID + " nWhiteIndex = " + nWhiteIndex);
	}

	@Override
	public void OnRenameGroupFile(int eGroupType, long nGroupID, String sFileID, String sNewName) {
		V2Log.jniCall("OnRenameGroupFile", " eGroupType = " + eGroupType + " nGroupID = " + nGroupID + " sFileID = "
				+ sFileID + " sNewName = " + sNewName);
	}

	@Override
	public void OnWBoardDestroy(int eGroupType, long nGroupID, String szWBoardID) {
		V2Log.jniCall("OnWBoardDestroy",
				" eGroupType = " + eGroupType + " nGroupID = " + nGroupID + " szWBoardID = " + szWBoardID);
	}

	@Override
	public void OnGroupCreateDocShare(int eGroupType, long nGroupID, String szWBoardID, String szFileName) {
		V2Log.jniCall("OnGroupCreateDocShare",
				" eGroupType = " + eGroupType + " nGroupID = " + nGroupID + " szWBoardID = " + szWBoardID
						+ " szFileName = " + szFileName);
	}

	@Override
	public void OnSearchGroup(int eGroupType, String infoXml) {
		V2Log.jniCall("OnSearchGroup", " eGroupType = " + eGroupType + " InfoXml = " + infoXml);
	}

	@Override
	public void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
		V2Log.jniCall("OnModifyGroupInfo", " groupType = " + groupType + " nGroupID = " + nGroupID + " sXml = " + sXml);
	}

	@Override
	public void OnGetGroupInfo(int groupType, String sXml) {
		V2Log.jniCall("OnGetGroupInfo", " groupType = " + groupType + " sXml = " + sXml);
	}

	@Override
	public void OnDelGroupFile(int type, long nGroupId, String fileId) {
		V2Log.jniCall("OnDelGroupFile", " type = " + type + " nGroupId = " + nGroupId + " fileId = " + fileId);
	}

	@Override
	public void OnAddGroupFile(int eGroupType, long nGroupId, String sXml) {
		V2Log.jniCall("OnAddGroupFile", " eGroupType = " + eGroupType + " nGroupId = " + nGroupId + " sXml = " + sXml);
	}

	@Override
	public void OnGetGroupFileInfo(int groupType, long nGroupId, String sXml) {
		V2Log.jniCall("OnGetGroupFileInfo",
				" groupType = " + groupType + " nGroupId = " + nGroupId + " sXml = " + sXml);
	}
}
