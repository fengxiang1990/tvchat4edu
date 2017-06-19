package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.AudioRequestCallback;
import com.bizcom.util.V2Log;

public abstract class AudioRequestCallbackAdapter implements AudioRequestCallback {
	@Override
	public void OnAudioChatInvite(String szSessionID, long nFromUserID) {
		V2Log.jniCall("OnAudioChatInvite", " szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID);
	}

	@Override
	public void OnAudioChatAccepted(String szSessionID, long nFromUserID) {
		V2Log.jniCall("OnAudioChatAccepted", " szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID);
	}

	@Override
	public void OnAudioChatRefused(String szSessionID, long nFromUserID) {
		V2Log.jniCall("OnAudioChatRefused", " szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID);
	}

	@Override
	public void OnAudioChatClosed(String szSessionID, long nFromUserID) {
		V2Log.jniCall("OnAudioChatClosed", " szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID);
	}

	@Override
	public void OnRecordStart(String fileID, int result) {
		V2Log.jniCall("OnRecordStart", " fileID: " + fileID + "| result: " + result);
	}

	@Override
	public void OnRecordStop(String fileID, String filePath, int result) {
		V2Log.jniCall("OnRecordStop", " fileID: " + fileID + "| filePath: " + filePath + "| result: " + result);
	}

	@Override
	public void OnAudioMicCurrentLevel(int nValue) {
		V2Log.jniCall("OnAudioMicCurrentLevel", " level: " + nValue);
	}

	@Override
	public void OnAudioChating(String szSessionID, long nFromUserID) {
		V2Log.jniCall("OnAudioChating", " szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID);
	}

	@Override
	public void OnAudioGroupEnableAudio(int eGroupType, long nGroupID) {
		V2Log.jniCall("OnAudioGroupEnableAudio", " eGroupType: " + eGroupType + "| nGroupID: " + nGroupID);
	}

	@Override
	public void OnAudioGroupOpenAudio(int eGroupType, long nGroupID, long nUserID, boolean bSpeaker) {
		V2Log.jniCall("OnAudioGroupOpenAudio", " eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + "| nUserID: "
				+ nUserID + "| bSpeaker: " + bSpeaker);
	}

	@Override
	public void OnAudioGroupCloseAudio(int eGroupType, long nGroupID, long nUserID) {
		V2Log.jniCall("OnAudioGroupCloseAudio",
				" eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + "| nUserID: " + nUserID);
	}

	@Override
	public void OnAudioGroupUserSpeaker(int eGroupType, long nGroupID, long nUserID, boolean bSpeaker) {
		V2Log.jniCall("OnAudioGroupEnableAudio",
				" eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + "| bSpeaker: " + bSpeaker);
	}

	@Override
	public void OnAudioGroupMuteSpeaker(int eGroupType, long nGroupID, String sExecptUserIDXml) {
		V2Log.jniCall("OnAudioGroupEnableAudio",
				" eGroupType: " + eGroupType + "| nGroupID: " + nGroupID + "| sExecptUserIDXml: " + sExecptUserIDXml);
	}

}
