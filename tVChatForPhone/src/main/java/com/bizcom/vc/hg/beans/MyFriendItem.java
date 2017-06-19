package com.bizcom.vc.hg.beans;

import java.io.Serializable;

public class MyFriendItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -653201803171676508L;
	
	private String name;
	private String firstName;
	private String phoneNum;
	private boolean  status;//在线状态 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public MyFriendItem(String name,  String phoneNum, boolean status) {
		super();
		this.name = name;
		this.firstName = name.substring(0,1);
		this.phoneNum = phoneNum;
		this.status = status;
	}
	
	

}
