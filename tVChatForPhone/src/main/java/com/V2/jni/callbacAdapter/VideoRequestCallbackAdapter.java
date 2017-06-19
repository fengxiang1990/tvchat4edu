package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.VideoRequestCallback;
import com.bizcom.util.V2Log;

public abstract class VideoRequestCallbackAdapter implements VideoRequestCallback {
	@Override
	public void OnRemoteUserVideoDevice(long uid, String szXmlData) {
		V2Log.jniCall("OnRemoteUserVideoDevice", " uid: " + uid + "| szXmlData: " + szXmlData);
	}

	@Override
	public void OnVideoChatInviteCallback(String szSessionID, long nFromUserID, String szDeviceID,String data) {
		V2Log.jniCall("OnVideoChatInviteCallback",
				" szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID + "| szDeviceID: " + szDeviceID);
	}

	@Override
	public void OnSetCapParamDone(String szDevID, int nSizeIndex, int nFrameRate, int nBitRate) {
		V2Log.jniCall("OnSetCapParamDone", " szDevID: " + szDevID + "| nSizeIndex: " + nSizeIndex + "| nFrameRate: "
				+ nFrameRate + "| nBitRate: " + nBitRate);
	}

	@Override
	public void OnVideoChatAccepted(String szSessionID, long nFromUserID, String szDeviceID) {
		V2Log.jniCall("OnVideoChatAccepted",
				" szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID + "| szDeviceID: " + szDeviceID);
	}

	@Override
	public void OnVideoChatRefused(String szSessionID, long nFromUserID, String szDeviceID,String data) {
		V2Log.jniCall("OnVideoChatRefused",
				" szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID + "| szDeviceID: " + szDeviceID+"| data: " + data);
	}

	@Override
	public void OnVideoChatClosed(String szSessionID, long nFromUserID, String szDeviceID) {
		V2Log.jniCall("OnVideoChatClosed",
				" szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID + "| szDeviceID: " + szDeviceID);
	}

	@Override
	public void OnVideoChating(String szSessionID, long nFromUserID, String szDeviceID) {
		V2Log.jniCall("OnVideoChating",
				" szSessionID: " + szSessionID + "| nFromUserID: " + nFromUserID + "| szDeviceID: " + szDeviceID);
	}

	@Override
	public void OnVideoBitRate(Object hwnd, int bps) {
		V2Log.jniCall("OnVideoBitRate", " hwnd: " + hwnd + "| bps: " + bps);
	}

	@Override
	public void OnGetVideoDevice(String xml, long l) {
		V2Log.jniCall("OnGetVideoDevice", " xml: " + xml + "| l: " + l);
	}

	@Override
	public void OnVideoCaptureError(String szDevID, int nErr) {
		V2Log.jniCall("OnVideoCaptureError", " szDevID: " + szDevID + "| nErr: " + nErr);
	}
}
