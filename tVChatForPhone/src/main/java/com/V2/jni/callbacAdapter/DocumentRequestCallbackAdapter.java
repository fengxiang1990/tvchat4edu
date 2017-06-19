package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.WBRequestCallback;
import com.bizcom.util.V2Log;

public class DocumentRequestCallbackAdapter implements WBRequestCallback {

	@Override
	public void OnWBoardPageListCallback(String szWBoardID, String szPageData, int nPageID) {
		V2Log.jniCall("OnWBoardPageListCallback",
				" szWBoardID = " + szWBoardID + " | szPageData = " + szPageData + " | nPageID = " + nPageID);
	}

	@Override
	public void OnWBoardActivePageCallback(long nUserID, String szWBoardID, int nPageID) {
		V2Log.jniCall("OnWBoardActivePageCallback",
				" nUserID = " + nUserID + " | szWBoardID = " + szWBoardID + " | nPageID = " + nPageID);
	}

	@Override
	public void OnWBoardDocDisplayCallback(String szWBoardID, int nPageID, String szFileName, int result) {
		V2Log.jniCall("OnWBoardDocDisplayCallback", " szWBoardID = " + szWBoardID + " | nPageID = " + nPageID
				+ " | szFileName = " + szFileName + " | result = " + result);
	}

	@Override
	public void OnWBoardAddPageCallback(String szWBoardID, int nPageID) {
		V2Log.jniCall("OnWBoardAddPageCallback", " szWBoardID = " + szWBoardID + " | nPageID = " + nPageID);
	}

	@Override
	public void OnRecvAddWBoardDataCallback(String szWBoardID, int nPageID, String szDataID, String szData) {
		V2Log.jniCall("OnRecvAddWBoardDataCallback", " szWBoardID = " + szWBoardID + " | nPageID = " + nPageID
				+ " | szDataID = " + szDataID + " | szData = " + szData);
	}

	@Override
	public void OnRecvChangeWBoardData(String szWBoardID, int nPageID, String szDataID, String szData) {

	}

	@Override
	public void OnWBoardDataRemoved(String szWBoardID, int nPageID, String szDataID) {

	}

	@Override
	public void OnGetPersonalSpaceDocDesc(long id, String xml) {

	}

	@Override
	public void OnDataBegin(String szWBoardID) {

	}

	@Override
	public void OnDataEnd(String szWBoardID) {

	}

	@Override
	public void OnRecvAppendWBoardDataCallback(String szWBoardID, int nPageID, String szDataID, String szData) {
		V2Log.jniCall("OnRecvAppendWBoardDataCallback", " szWBoardID = " + szWBoardID + " | nPageID = " + nPageID
				+ " | szDataID = " + szDataID + " | szData = " + szData);
	}

	@Override
	public void OnWBoardDeletePage(String szWBoardID, int nPageID) {

	}

}
