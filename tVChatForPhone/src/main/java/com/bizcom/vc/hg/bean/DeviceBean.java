package com.bizcom.vc.hg.bean;

public class DeviceBean {

	/**
	 * 用户id
	 */
	private long uid;
	/**
	 * 昵称
	 */
	private String nickName;
	/**
	 * 账号
	 */
	private String userName;
	/**
	 * 头像地址
	 */
	private String picurl;
	
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPicurl() {
		return picurl;
	}
	public void setPicurl(String picurl) {
		this.picurl = picurl;
	}
	@Override
	public String toString() {
		return "DeviceBean [uid=" + uid + ", nickName=" + nickName
				+ ", userName=" + userName + ", picurl=" + picurl + "]";
	}
}
