package com.bizcom.vc.hg.web.models;

/**
 * Created by admin on 2016/12/17.
 */

public class UserCenterCallLong {

    public String touch;  //常联系
    public String conversation;//通话时长

    @Override
    public String toString() {
        return "UserCenterCallLong{" +
                "touch='" + touch + '\'' +
                ", conversation='" + conversation + '\'' +
                '}';
    }
}
