package com.bizcom.vo;

import com.config.V2GlobalConstants;

import v2av.VideoPlayer;

public class UserChattingObject {

	public static final int VOICE_CALL = 0x01;
	public static final int VIDEO_CALL = 0x02;
	public static final int INCOMING_CALL = 0x10;
	public static final int OUTING_CALL = 0x00;
	public static final int SPEAKING = 0x100;
	public static final int CONNECTED = 0x200;
	private String szSessionID;
	private int flag;
	private User mUser;
	private long groupdId;

	private boolean sipCall;
	private String sipNumber;
	private UserDeviceConfig udc;
	private String data ="";

	public UserChattingObject(User user, int flag, String deviceId) {
		this(null, user, flag, deviceId, null);
	}

	public UserChattingObject(String szSessionID, User user, int flag, String deviceId, VideoPlayer vp) {
		this.szSessionID = szSessionID;
		this.flag = flag;
		this.flag |= SPEAKING;
		this.mUser = user;
		this.udc = new UserDeviceConfig(0, 0, user.getmUserId(), deviceId, vp, V2GlobalConstants.EVIDEODEVTYPE_CAMERA);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public User getUser() {
		return this.mUser;
	}

	public String getSzSessionID() {
		return szSessionID;
	}

	public void setSzSessionID(String szSessionID) {
		this.szSessionID = szSessionID;
	}

	public long getGroupdId() {
		return groupdId;
	}

	public String getDeviceId() {
		return udc.getDeviceID();
	}

	public void setDeviceId(String devId) {
		this.udc.setDeviceID(devId);
	}

	public VideoPlayer getVp() {
		return udc.getVp();
	}

	public void setVp(VideoPlayer vp) {
		this.udc.setVp(vp);
	}

	public UserDeviceConfig getUdc() {
		return this.udc;
	}

	public boolean isSipCall() {
		return sipCall;
	}

	public void setSipCall(boolean sipCall) {
		this.sipCall = sipCall;
	}

	public String getSipNumber() {
		return sipNumber;
	}

	public void setSipNumber(String sipNumber) {
		this.sipNumber = sipNumber;
	}

	public void setMute(boolean b) {
		if (b) {
			this.flag &= (~SPEAKING);
		} else {
			this.flag |= SPEAKING;
		}
	}

	public boolean isMute() {
		return !((this.flag & SPEAKING) == SPEAKING);
	}

	public boolean isAudioType() {
		return (flag & VOICE_CALL) == VOICE_CALL;
	}

	public boolean isVideoType() {
		return (flag & VIDEO_CALL) == VIDEO_CALL;
	}

	public boolean isIncoming() {
		return (flag & INCOMING_CALL) == INCOMING_CALL;
	}

	public void updateAudioType() {
		// Clear video call flag
		flag &= ~VIDEO_CALL;
		flag |= VOICE_CALL;
	}

	public boolean isConnected() {
		return (flag & CONNECTED) == CONNECTED ? true : false;
	}

	public void setConnected(boolean cFlag) {
		if (cFlag) {
			flag |= CONNECTED;
		} else {
			flag &= (~CONNECTED);
		}
	}
}
