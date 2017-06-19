package com.bizcom.vc.hg.ui.edu;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/2/20.
 */

public class CourseInfoSingleton {

    String tag = "CourseInfoSingleton";


    private Map<Long,String> studentNamePars = new HashMap<>();
    private Map<Long,String> studentImgPars = new HashMap<>();
    private String course_id;

    private String carrange_id;

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    private String endTime;


    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    private String teacher_name;

    private static CourseInfoSingleton infoSingleton = new CourseInfoSingleton();

    private CourseInfoSingleton() {
    }

    public static synchronized CourseInfoSingleton getCourseInfo() {
        return infoSingleton;
    }

    public String getCourse_id() {
        Log.e(tag, "getCourse_id-->" + course_id);
        return course_id;
    }

    public void setCourse_id(String course_id) {
        Log.e(tag, "setCourse_id-->" + course_id);
        this.course_id = course_id;
    }

    public String getCarrange_id() {
        Log.e(tag, "getCarrange_id-->" + carrange_id);
        return carrange_id;
    }

    public void setCarrange_id(String carrange_id) {
        Log.e(tag, "setCarrange_id-->" + carrange_id);
        this.carrange_id = carrange_id;
    }

    public Map<Long, String> getStudentNamePars() {
        return studentNamePars;
    }

    public void setStudentNamePars(Map<Long, String> studentNamePars) {
        this.studentNamePars = studentNamePars;
    }

    public Map<Long, String> getStudentImgPars() {
        return studentImgPars;
    }

    public void setStudentImgPars(Map<Long, String> studentImgPars) {
        this.studentImgPars = studentImgPars;
    }
}
