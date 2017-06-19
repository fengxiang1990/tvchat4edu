package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.ImRequestCallback;
import com.bizcom.util.V2Log;

public abstract class ImRequestCallbackAdapter implements ImRequestCallback {
	@Override
	public void OnLoginCallback(long nUserID, int nStatus, int nResult, long serverTime, String sDBID) {
		V2Log.jniCall("OnLoginCallback", " nUserID = " + nUserID + " nStatus = " + nStatus + " serverTime = "
				+ serverTime + " sDBID = " + sDBID + " nResult = " + nResult);
	}

	@Override
	public void OnLogoutCallback(int nType) {
		V2Log.jniCall("OnLogoutCallback", " nType = " + nType);
	}

	@Override
	public void OnConnectResponseCallback(int nResult) {
		V2Log.jniCall("OnConnectResponse", " nResult = " + nResult);
	}

	@Override
	public void OnUpdateBaseInfoCallback(long nUserID, String updatexml) {
		V2Log.jniCall("OnUpdateBaseInfo", " nUserID = " + nUserID + " updatexml = " + updatexml);
	}

	@Override
	public void OnUserStatusUpdatedCallback(long nUserID, int nType, int nStatus, String szStatusDesc) {
		V2Log.jniCall("OnUserStatusUpdated", " nUserID = " + nUserID + " nType = " + nType + " nStatus = " + nStatus
				+ " szStatusDesc = " + szStatusDesc);
	}

	@Override
	public void OnChangeAvatarCallback(int nAvatarType, long nUserID, String AvatarName) {
		V2Log.jniCall("OnChangeAvatar",
				" nAvatarType = " + nAvatarType + " nUserID = " + nUserID + " AvatarName = " + AvatarName);
	}

	@Override
	public void OnModifyCommentNameCallback(long nUserId, String sCommmentName) {
		V2Log.jniCall("OnModifyCommentName", " nUserId = " + nUserId + " sCommmentName = " + sCommmentName);
	}

	@Override
	public void OnHaveUpdateNotify(String updatefilepath, String updatetext) {
		V2Log.jniCall("OnHaveUpdateNotify", " updatefilepath = " + updatefilepath + " updatetext = " + updatetext);
	}

	@Override
	public void OnUpdateDownloadBegin(long filesize) {
		V2Log.jniCall("OnUpdateDownloadBegin", " filesize = " + filesize);
	}

	@Override
	public void OnUpdateDownloading(long size) {
		V2Log.jniCall("OnUpdateDownloading", " size = " + size);
	}

	@Override
	public void OnUpdateDownloadEnd(boolean error) {
		V2Log.jniCall("OnUpdateDownloadEnd", " error = " + error);
	}

	@Override
	public void OnGetGroupsInfoBegin() {
		V2Log.jniCall("OnGetGroupsInfoBegin", "----- ");
	}

	@Override
	public void OnGroupsLoaded() {
		V2Log.jniCall("OnGetGroupsInfoEnd", "");
	}

	@Override
	public void OnOfflineStart() {
		V2Log.jniCall("OnOfflineStart", "");
	}

	@Override
	public void OnOfflineEnd() {
		V2Log.jniCall("OnOfflineEnd", "");
	}

	@Override
	public void OnSignalDisconnected() {
		V2Log.jniCall("OnSignalDisconnected", "");
	}

	@Override
	public void OnSearchUserCallback(String xmlinfo) {
		V2Log.jniCall("OnSearchUserCallback", " xmlinfo = " + xmlinfo);
	}

	@Override
	public void OnImUserCreateValidateCode(int ret) {
		V2Log.jniCall("OnImUserCreateValidateCode", " ret = " + ret);
	}

	@Override
	public void OnImRegisterPhoneUser(int ret) {
		V2Log.jniCall("OnImRegisterPhoneUser", " ret = " + ret);
	}

	@Override
	public void OnImUpdateUserPwd(int ret) {
		V2Log.jniCall("OnImUpdateUserPwd", " ret = " + ret);
	}


    @Override
    public void OnImRegisterGuest(String sAccount, String sPwd, int ret) {
        V2Log.jniCall("OnImRegisterGuest", " sAccount = " + sAccount + " sPwd = " + sPwd + " ret = " + ret);
    }
}
