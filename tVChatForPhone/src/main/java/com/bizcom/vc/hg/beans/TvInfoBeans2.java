package com.bizcom.vc.hg.beans;

import java.io.Serializable;
import java.util.HashMap;

public class TvInfoBeans2 implements Serializable{
	/**
	 * uid
nickName
userName
picurl
owner_uid
	 */
	private static final long serialVersionUID = 5174277091819713923L;
	private String uid;
	private String nickName;
	private String userName;
	private String picurl;
	private String owner_uid;
	public TvInfoBeans2(String uid, String nickName, String userName, String picurl,String owner_uid) {
		super();
		this.uid = uid;
		this.nickName = nickName;
		this.userName = userName;
		this.picurl = picurl;
		this.owner_uid = owner_uid;
	}
	
	
	public String getOwner_uid() {
		return owner_uid;
	}


	public void setOwner_uid(String owner_uid) {
		this.owner_uid = owner_uid;
	}


	public TvInfoBeans2() {
		super();
	}


	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
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
		return "TvInfoBeans2{" +
				"uid='" + uid + '\'' +
				", nickName='" + nickName + '\'' +
				", userName='" + userName + '\'' +
				", picurl='" + picurl + '\'' +
				", owner_uid='" + owner_uid + '\'' +
				'}';
	}
}
