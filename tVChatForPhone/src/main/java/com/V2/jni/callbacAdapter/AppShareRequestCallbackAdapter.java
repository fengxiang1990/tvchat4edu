package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.AppShareRequestCallBack;
import com.bizcom.util.V2Log;

public abstract class AppShareRequestCallbackAdapter implements AppShareRequestCallBack {

	@Override
	public void OnAppShareCreated(int nGroupType, long nGroupID, long nHostUserID, String szVideoDeviceID) {
		V2Log.jniCall("OnAppShareCreated", " nGroupType: " + nGroupType + "| nGroupID: " + nGroupID + "| nHostUserID: "
				+ nHostUserID + "| szVideoDeviceID: " + szVideoDeviceID);
	}

	@Override
	public void OnAppShareDestroyed(String szVideoDeviceID) {
		V2Log.jniCall("OnAppShareDestroyed", " szVideoDeviceID: " + szVideoDeviceID);
	}
}
