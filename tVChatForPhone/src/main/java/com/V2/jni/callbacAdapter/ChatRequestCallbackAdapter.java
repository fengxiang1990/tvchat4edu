package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.ChatRequestCallback;
import com.bizcom.util.V2Log;

public abstract class ChatRequestCallbackAdapter implements ChatRequestCallback {
	@Override
	public void OnRecvChatTextCallback(int eGroupType, long nGroupID, long nFromUserID, long nToUserID, long nTime,
			String szSeqID, String szXmlText) {
		V2Log.jniCall("OnRecvText",
				" eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + " | nFromUserID: " + nFromUserID
						+ " | nToUserID: " + nToUserID + " | nTime: " + nTime + " | szSeqID: " + szSeqID
						+ " | szXmlText: " + szXmlText);
	}

	@Override
	public void OnRecvChatBinaryCallback(int eGroupType, long nGroupID, long nFromUserID, long nToUserID, long nTime,
			int binaryType, String messageId, String binaryPath) {
		V2Log.jniCall("OnRecvBinary",
				" eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + " | nFromUserID: " + nFromUserID
						+ " | nToUserID: " + nToUserID + " | nTime: " + nTime + " | binaryType: " + binaryType
						+ " | messageId: " + messageId + " | binaryPath: " + binaryPath);
	}

	@Override
	public void OnSendTextResultCallback(int eGroupType, long nGroupID, long nFromUserID, long nToUserID, String sSeqID,
			int nResult) {
		V2Log.jniCall("OnSendTextResult", " eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + " | nFromUserID: "
				+ nFromUserID + " | nToUserID: " + nToUserID + " | sSeqID: " + sSeqID + " | nResult: " + nResult);
		OnSendChatResult(eGroupType, nGroupID, nFromUserID, nToUserID, 0, sSeqID, nResult);
	}

	@Override
	public void OnSendBinaryResultCallback(int eGroupType, long nGroupID, long nFromUserID, long nToUserID,
			int mediaType, String sSeqID, int nResult) {
		V2Log.jniCall("OnSendBinaryResult",
				" eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + " | nFromUserID: " + nFromUserID
				+ " | nToUserID: " + nToUserID + " | mediaType: " + mediaType + " | sSeqID: " + sSeqID
				+ " | nResult: " + nResult);
		OnSendChatResult(eGroupType, nGroupID, nFromUserID, nToUserID, mediaType, sSeqID, nResult);
	}

	@Override
	public void OnMonitorRecv(int eGroupType, String sSeqID, int nResult) {
		V2Log.jniCall("OnMonitorRecv",
				" eGroupType: " + eGroupType + " | sSeqID: " + sSeqID + " | nResult: " + nResult);
	}

	public void OnSendChatResult(int eGroupType, long nGroupID, long nFromUserID, long nToUserID, int mediaType,
			String sSeqID, int nResult) {
	}
}
