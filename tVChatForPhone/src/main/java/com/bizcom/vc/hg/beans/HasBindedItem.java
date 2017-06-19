package com.bizcom.vc.hg.beans;

import java.io.Serializable;

public class HasBindedItem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6372408845676362303L;
    public boolean isSelected = false;
    private String UserName;
    private String UserId;
    private String phoneNum;
    private String imageUrl;
    //是否离线
    private int status;
    /**
     * 0：未发送 1：自己发送 2：别人发送
     */
    private int type;

    public HasBindedItem(String userName, String userId, String phoneNum, String imageUrl, int status, int type) {
        super();
        UserName = userName;
        UserId = userId;
        this.phoneNum = phoneNum;
        this.imageUrl = imageUrl;
        this.status = status;
        this.type = type;
    }

    public HasBindedItem() {
        super();

    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "HasBindedItem{" +
                "isSelected=" + isSelected +
                ", UserName='" + UserName + '\'' +
                ", UserId='" + UserId + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", status=" + status +
                ", type=" + type +
                '}';
    }
}
