package com.bizcom.vc.hg.beans;

import java.io.Serializable;

public class TvInfoBeans implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7313253497548020027L;
	
//	{"nickName":"2112433","tvId":50,"uid":31,"userName":"2112433"}
	private String uid;
	private String tvId;
	private String nickName;
	private String userName;
	private String picurl;
	public TvInfoBeans(String uid, String tvId, String nickName, String userName) {
		super();
		this.uid = uid;
		this.tvId = tvId;
		this.nickName = nickName;
		this.userName = userName;
	}
	
	public TvInfoBeans() {
		super();
	}

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getTvId() {
		return tvId;
	}
	public void setTvId(String tvId) {
		this.tvId = tvId;
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

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getPicurl() {
		return picurl;
	}

	public void setPicurl(String picurl) {
		this.picurl = picurl;
	}
}
