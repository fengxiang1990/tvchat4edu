package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.ConfRequestCallback;
import com.bizcom.util.V2Log;

public abstract class ConfRequestCallbackAdapter implements ConfRequestCallback {
	@Override
	public void OnEnterConfCallback(long nConfID, long nTime, String szConfData, int nJoinResult) {
		V2Log.jniCall("OnEnterConf", " nConfID = " + nConfID + " | nTime = " + nTime + " | szConfData = " + szConfData
				+ " | nJoinResult = " + nJoinResult);
	}

	@Override
	public void OnConfMemberEnter(long nConfID, long nUserID, long nTime, String szUserInfos) {
		V2Log.jniCall("OnConfMemberEnter", " nConfID = " + nConfID + " | nUserID = " + nUserID + " | nTime = " + nTime
				+ " | szUserInfos = " + szUserInfos);
	}

	@Override
	public void OnConfMemberExitCallback(long nConfID, long nTime, long nUserID) {
		V2Log.jniCall("OnConfMemberExit", " nConfID = " + nConfID + " | nTime = " + nTime + " | nUserID = " + nUserID);
	}

	@Override
	public void OnKickConfCallback(int nReason) {
		V2Log.jniCall("OnKickConf", " nReason = " + nReason);
	}

	@Override
	public void OnConfNotify(String confXml, String creatorXml) {
		V2Log.jniCall("OnConfNotify", "confXml : " + confXml + " | creatorXml: " + creatorXml);
	}

	@Override
	public void OnNotifyChair(long userid, int type) {
		V2Log.jniCall("OnNotifyChair", "userid : " + userid + " | type: " + type);
	}

	@Override
	public void OnGrantPermissionCallback(long userid, int type, int status) {
		V2Log.jniCall("OnGrantPermission", "userid : " + userid + " | type: " + type + " | status: " + status);
	}

	@Override
	public void OnConfSyncOpenVideo(String sSyncVideoMsgXML) {
		V2Log.jniCall("OnConfSyncOpenVideo", "sSyncVideoMsgXML : " + sSyncVideoMsgXML);
	}

	@Override
	public void OnConfSyncCloseVideo(long gid, String sSyncVideoMsgXML) {
		V2Log.jniCall("OnConfSyncCloseVideo", "gid : " + gid + " | sSyncVideoMsgXML: " + sSyncVideoMsgXML);
	}

	@Override
	public void OnConfSyncCloseVideoToMobile(long nDstUserID, String sDstMediaID) {
		V2Log.jniCall("OnConfSyncCloseVideoToMobile", "nDstUserID : " + nDstUserID + " | sDstMediaID: " + sDstMediaID);
	}

	@Override
	public void OnConfSyncOpenVideoToMobile(String sSyncVideoMsgXML) {
		V2Log.jniCall("OnConfSyncOpenVideoToMobile", "sSyncVideoMsgXML : " + sSyncVideoMsgXML);
	}

	@Override
	public void OnConfChairChanged(long nConfID, long nChairID) {
		V2Log.jniCall("OnConfChairChanged", "nConfID : " + nConfID + " | nChairID : " + nChairID);
	}

	@Override
	public void OnChangeSyncConfOpenVideoPos(long nDstUserID, String szDeviceID, String sPos) {
		V2Log.jniCall("OnChangeSyncConfOpenVideoPos",
				"nDstUserID : " + nDstUserID + " | szDeviceID: " + szDeviceID + " | sPos: " + sPos);
	}

	@Override
	public void OnConfMute() {
		V2Log.jniCall("OnConfMute", "----");
	}

	@Override
	public void OnGetConfVodList(long nGroupID, String sVodXmlList) {
		V2Log.jniCall("OnGetConfVodList", "nGroupID : " + nGroupID + " | sVodXmlList: " + sVodXmlList);
	}

	@Override
	public void OnConfNotify(long nSrcUserID, String srcNickName, long nConfID, String subject, long nTime) {
		V2Log.jniCall("OnConfNotify", "nSrcUserID : " + nSrcUserID + " | srcNickName: " + srcNickName + " | nConfID: "
				+ nConfID + " | subject: " + subject + " | nTime: " + nTime);
	}

	@Override
	public void OnConfNotifyEnd(long nConfID) {
		V2Log.jniCall("OnGetConfVodList", "nConfID : " + nConfID);
	}

	@Override
	public void OnConfSyncOpenVideo(long nDstUserID, String sDstMediaID, int nPos) {
		V2Log.jniCall("OnConfSyncOpenVideo",
				"nDstUserID : " + nDstUserID + " | sDstMediaID: " + sDstMediaID + " | nPos: " + nPos);
	}
}
