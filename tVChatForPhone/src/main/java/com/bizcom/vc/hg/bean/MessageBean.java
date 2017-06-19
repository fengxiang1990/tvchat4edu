package com.bizcom.vc.hg.bean;

import android.graphics.Bitmap;

public class MessageBean {
	public int isCancelByMine = 0;
	public static final int MESSAGE_TYPE_FRIEND = 1;// 好友认证消息
	public static final int MESSAGE_TYPE_VIDEO = 2;// 视频通话消息
	private long remoteUserID; // 联系人的id
	private long id; // 消息对象操作的id[删除等]
	private String name;// 联系人名称
	private int callType;// 呼叫类型【呼入/呼出】
	private int callHandle;// 呼出被叫是否处理
	private long callDuration;// 呼叫时长
	private long handleDate;// 消息的操作时间
	private String dheadFilePath;// 照片路径
	private String info;// 认证消息，描述信息
	// 认证消息，状态【别人加我：允许任何人：0已添加您为好友，需要验证：1未处理，2已同意，3已拒绝】
	// 【我加别人：允许认识人：4被同意（成为好友），需要验证：5等待验证，6被拒绝】
	private int state;
	private int messageType;

	public long getRemoteUserID() {
		return remoteUserID;
	}

	public void setRemoteUserID(long remoteUserID) {
		this.remoteUserID = remoteUserID;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getCallType() {
		return callType;
	}

	public void setCallType(int callType) {
		this.callType = callType;
	}

	public long getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(long callDuration) {
		this.callDuration = callDuration;
	}

	public long getHandleDate() {
		return handleDate;
	}

	public void setHandleDate(long handleDate) {
		this.handleDate = handleDate;
	}

	public int getCallHandle() {
		return callHandle;
	}

	public void setCallHandle(int callHandle) {
		this.callHandle = callHandle;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getDheadFilePath() {
		return dheadFilePath;
	}

	public void setDheadFilePath(String dheadFilePath) {
		this.dheadFilePath = dheadFilePath;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
}
