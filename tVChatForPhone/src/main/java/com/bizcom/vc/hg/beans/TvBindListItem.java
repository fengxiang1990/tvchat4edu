package com.bizcom.vc.hg.beans;

import java.io.Serializable;

public class TvBindListItem implements Serializable{

	private static final long serialVersionUID = 1602995366795490088L;
	
	private String uid;
	private String nickName;
	private String userName;
	private String picurl;
	private int id;
	public TvBindListItem(String uid, String nickName, String userName, String picurl) {
		super();
		this.uid = uid;
		this.nickName = nickName;
		this.userName = userName;
		this.picurl = picurl;
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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

}
