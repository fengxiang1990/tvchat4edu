package com.V2.jni.ind;


public class VideoJNIObjectInd extends JNIObjectInd {
	
	private long mGroupId;
	private String szSessionID;
	private long mFromUserId;
	private String mDeviceId;
	private String data;

	public VideoJNIObjectInd(long mGroupId, String szSessionID, long mFromUserId, String mDeviceId, String data) {
		this.mGroupId = mGroupId;
		this.szSessionID = szSessionID;
		this.mFromUserId = mFromUserId;
		this.mDeviceId = mDeviceId;
		this.data = data;
	}

	public VideoJNIObjectInd(long groupId, long fromUserId, String deviceId, int requestType) {
		this.mGroupId = groupId;
		this.mFromUserId = fromUserId;
		this.mRequestType = requestType;
		this.mType = JNIIndType.VIDEO;
		this.mDeviceId = deviceId;
	}
	
	public VideoJNIObjectInd(String szSessionID, long fromUserId, String deviceId, int requestType) {
		this.szSessionID = szSessionID;
		this.mFromUserId = fromUserId;
		this.mRequestType = requestType;
		this.mType = JNIIndType.VIDEO;
		this.mDeviceId = deviceId;
	}


	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getGroupId() {
		return mGroupId;
	}

	public void setGroupId(long groupId) {
		this.mGroupId = groupId;
	}

	public long getFromUserId() {
		return mFromUserId;
	}

	public void setFromUserId(long fromUserId) {
		this.mFromUserId = fromUserId;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public void setDeviceId(String deviceId) {
		this.mDeviceId = deviceId;
	}
	
	public String getSzSessionID() {
		return szSessionID;
	}

	public void setSzSessionID(String szSessionID) {
		this.szSessionID = szSessionID;
	}
	

}
