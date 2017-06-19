package com.bizcom.vc.hg.beans;

import java.io.Serializable;

import com.bizcom.vc.hg.view.CharacterParser;

public class PhoneFriendItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -653201803171676508L;
	private Long userId;
	private boolean hasRegsited;//是否注册
	private String sortLetters; // 显示数据拼音的首字母
	private String name;
	private String firstName;
	private String phoneNum;
	private boolean  status;//在线状态 
	private boolean ifFriends;//是否是好友
	private String picUrl;// 头像地址
	
	public boolean isIfFriends() {
		return ifFriends;
	}
	public void setIfFriends(boolean ifFriends) {
		this.ifFriends = ifFriends;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
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
	public boolean isHasRegsited() {
		return hasRegsited;
	}
	public void setHasRegsited(boolean hasRegsited) {
		this.hasRegsited = hasRegsited;
	}
	
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public PhoneFriendItem(boolean hasRegsited, String name, String phoneNum, Long uid, boolean status, boolean ifFriends,String picUrl) {
		super();
		this.ifFriends = ifFriends;
		this.hasRegsited = hasRegsited;
		this.userId = uid;
		this.name = name;
		this.phoneNum = phoneNum;
		this.status = status;
		this.firstName = name.substring(0,1);
		this.sortLetters=getSortLetterstxt(name);
		this.picUrl = picUrl;
	}
	private String getSortLetterstxt( String s) {
		// 汉字转换成拼音
		String pinyin =CharacterParser.getInstance().getSelling(s);
		String sortString = pinyin.substring(0, 1).toUpperCase();

		// 正则表达式，判断首字母是否是英文字母
		if (sortString.matches("[A-Z]")) {
			return sortString.toUpperCase();
		} else {
			return "#";
		}
		
	}
	
	

}
