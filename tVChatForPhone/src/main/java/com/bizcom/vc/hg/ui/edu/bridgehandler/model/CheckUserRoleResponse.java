package com.bizcom.vc.hg.ui.edu.bridgehandler.model;

/**
 * Created by admin on 2017/2/13.
 */

public class CheckUserRoleResponse{

    public String id;
    public String account;
    public String user_type;
    public String user_name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    @Override
    public String toString() {
        return "CheckUserRoleResponse{" +
                "id='" + id + '\'' +
                ", account='" + account + '\'' +
                ", user_type='" + user_type + '\'' +
                ", user_name='" + user_name + '\'' +
                '}';
    }
}
