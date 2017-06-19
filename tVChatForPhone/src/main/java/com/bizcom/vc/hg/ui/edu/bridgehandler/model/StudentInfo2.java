package com.bizcom.vc.hg.ui.edu.bridgehandler.model;

import java.io.Serializable;

/**
 * Created by admin on 2017/2/15.
 */

public class StudentInfo2 implements Serializable{

    public String account;
    public long course_id;
    public String course_name;
    public int is_vip;
    public long user_id;
    public String user_mobile;
    public String user_name;
    public String picurl;


    @Override
    public String toString() {
        return "StudentInfo2{" +
                "account='" + account + '\'' +
                ", course_id=" + course_id +
                ", course_name='" + course_name + '\'' +
                ", is_vip=" + is_vip +
                ", user_id=" + user_id +
                ", user_mobile='" + user_mobile + '\'' +
                ", user_name='" + user_name + '\'' +
                ", picurl='" + picurl + '\'' +
                '}';
    }
}
