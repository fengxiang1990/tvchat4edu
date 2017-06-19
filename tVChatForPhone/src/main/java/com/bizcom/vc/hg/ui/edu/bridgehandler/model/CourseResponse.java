package com.bizcom.vc.hg.ui.edu.bridgehandler.model;

import java.util.List;

/**
 * Created by admin on 2017/1/20.
 */

public class CourseResponse {
    public String course_id;//课程id
    public String courseName;
    public String startTime;
    public String nickName;
    public String usrId;//老师 UID
    public String teacherName;
    public String endTime;
    public String carrange_id;  //课时ID;
    public List<CourseStudent> users;




    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getCarrange_id() {
        return carrange_id;
    }

    public void setCarrange_id(String carrange_id) {
        this.carrange_id = carrange_id;
    }

    public List<CourseStudent> getUsers() {
        return users;
    }

    public void setUsers(List<CourseStudent> users) {
        this.users = users;
    }


    @Override
    public String toString() {
        return "CourseResponse{" +
                "course_id='" + course_id + '\'' +
                ", courseName='" + courseName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", nickName='" + nickName + '\'' +
                ", usrId='" + usrId + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", endTime='" + endTime + '\'' +
                ", carrange_id='" + carrange_id + '\'' +
                ", users=" + users +
                '}';
    }
}
