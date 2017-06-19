package com.bizcom.vc.hg.ui.edu.bridgehandler.model;

/**
 * Created by admin on 2017/2/14.
 */

public class StudentInfo {

    public String result;
    public String info;
    public String userId;
    public String userName;
    public String videoId;
    public String isVip;
    public String courseName;
    public String startTime;
    public String teacherName;
    public String teacherId;

    @Override
    public String toString() {
        return "StudentInfo{" +
                "result='" + result + '\'' +
                ", info='" + info + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", videoId='" + videoId + '\'' +
                ", isVip='" + isVip + '\'' +
                ", courseName='" + courseName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", teacherId='" + teacherId + '\'' +
                '}';
    }
}
