package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bizcom.util.DateUtil;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.ui.edu.ConferenceHelper;
import com.bizcom.vc.hg.ui.edu.CourseInfoSingleton;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.CourseResponse;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.CourseStudent;
import com.bizcom.vo.Conference;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 老师创建会议
 * Created by admin on 2017/1/18.
 */

public class CreateConferenceHandler implements BridgeHandler {

    String tag = "CreateConferenceHandler";
    Handler handler;

    Activity activity;
    public CallBackFunction function;
    //public String carrange_id;
    //public String course_id;
    Gson gson;

    public CreateConferenceHandler(Activity activity, Handler handler) {
        this.handler = handler;
        this.activity = activity;
        gson = new Gson();
    }


    @Override
    public void handler(String data, CallBackFunction function) {
        if (!cameraIsCanUse()) {
            Toast.makeText(activity, "相机故障,请检查您的摄像头权限是否已打开", Toast.LENGTH_LONG).show();
            return;
        }
        this.function = function;
        Log.e(tag, "response from js-->" + data);
        String courseName = null;
        String dateStr = null;
        Date date = null;
        List<User> userList = new ArrayList<>();
        if (!TextUtils.isEmpty(data)) {
            CourseResponse response = gson.fromJson(data, CourseResponse.class);
            if (response != null) {
                Log.e(tag, "response-->" + response.toString());
                courseName = response.courseName;
                dateStr = response.startTime;
                //carrange_id = response.carrange_id;
                // course_id = response.course_id;
                CourseInfoSingleton.getCourseInfo().setEndTime(response.endTime);
                CourseInfoSingleton.getCourseInfo().setTeacher_name(response.teacherName);
                CourseInfoSingleton.getCourseInfo().setCourse_id(response.course_id);
                CourseInfoSingleton.getCourseInfo().setCarrange_id(response.carrange_id);
                if (TextUtils.isEmpty(dateStr)) {
                    dateStr = DateUtil.getStandardDate(new Date(GlobalConfig.getGlobalServerTime() + 30000));
                    DateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
                    try {
                        date = sd.parse(dateStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    long datetimestamp = Long.parseLong(dateStr);
                    date = new Date(datetimestamp);
                }
                List<CourseStudent> students = response.users;
                if (students != null) {
                    for (CourseStudent courseStudent : students) {
                        long studengUid = courseStudent.uid;
                        User user = getUserByTvlUid(String.valueOf(studengUid));
                        if (user != null) {
                            userList.add(user);
                        }
                    }
                }
            } else {
                ToastUtil.ShowToast_short(activity, "课程信息不对,无法创建课堂");
                return;
            }
        }
        try {
            WaitDialogBuilder.showNormalWithHintProgress(activity);
            if (data == null) {
                ToastUtil.ShowToast_short(activity, "会议开始日期有误，无法进入课堂，请联系管理员");
                WaitDialogBuilder.dismissDialog();
                return;
            }
            Conference conference = ConferenceHelper.getConferenceHelper().getConference(courseName, date, userList);
            ConferenceHelper.getConferenceHelper().createConference(conference, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public User getUserByTvlUid(String tvlUid) {
        long uid = Long.parseLong("11" + tvlUid);
        User user = GlobalHolder.getInstance().getUser(uid);
        if (user != null) {
            return user;
        }
        return null;
    }

    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

}
