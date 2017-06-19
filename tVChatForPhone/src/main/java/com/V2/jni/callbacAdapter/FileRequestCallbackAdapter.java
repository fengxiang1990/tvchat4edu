package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.FileRequestCallback;
import com.bizcom.util.V2Log;

public abstract class FileRequestCallbackAdapter implements FileRequestCallback {
	@Override
	public void OnFileTransInvite(long userid, String szFileID, String szFileName, long nFileBytes, String url,
			int linetype) {
		V2Log.jniCall("OnFileTransInvite", " userid: " + userid + "| szFileID: " + szFileID + "| szFileName: "
				+ szFileName + "| nFileBytes: " + nFileBytes + "| url: " + url);
	}

	@Override
	public void OnFileTransAccepted(String szFileID) {
		V2Log.jniCall("OnFileTransAccepted", " szFileID: " + szFileID);
	}

	@Override
	public void OnFileTransRefuse(String szFileID) {
		V2Log.jniCall("OnFileTransRefuse", " szFileID: " + szFileID);
	}

	@Override
	public void OnFileTransBegin(String szFileID, int nTransType, long nFileSize) {
		V2Log.jniCall("OnFileTransBegin",
				" szFileID: " + szFileID + "| nTransType: " + nTransType + "| nFileSize: " + nFileSize);
	}

	@Override
	public void OnFileTransProgress(String szFileID, long nBytesTransed, int nTransType) {
		V2Log.jniCall("OnFileTransProgress",
				" szFileID: " + szFileID + "| nBytesTransed: " + nBytesTransed + "| nTransType: " + nTransType);
	}

	@Override
	public void OnFileTransError(String szFileID, int errorCode, int nTransType) {
		V2Log.jniCall("OnFileTransError",
				" szFileID: " + szFileID + "| errorCode: " + errorCode + "| nTransType: " + nTransType);
	}

	@Override
	public void OnFileTransEnd(String szFileID, String szFileName, long nFileSize, int nTransType) {
		V2Log.jniCall("OnFileTransEnd", " szFileID: " + szFileID + "| szFileName: " + szFileName + "| nFileSize: "
				+ nFileSize + "| nTransType: " + nTransType);
	}

	@Override
	public void OnFileTransCancel(String szFileID) {
		V2Log.jniCall("OnFileTransCancel", " szFileID: " + szFileID);
	}
}
