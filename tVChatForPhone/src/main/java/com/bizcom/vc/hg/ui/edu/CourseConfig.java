package com.bizcom.vc.hg.ui.edu;

import com.shdx.tvchat.phone.BuildConfig;

/**
 * Created by admin on 2017/1/16.
 */

public interface CourseConfig {


    String WEB_TYPE = "type";

    int ADD_COURSE = 1;

    String DEMO_URL = "file:///android_asset/web/demo.html";

    String COUSE_TAB1 = BuildConfig.EDU_HOST + "syscourse/classing";
    String COUSE_TAB2 = BuildConfig.EDU_HOST + "syscourse/classnobegin";
    String COUSE_TAB3 = BuildConfig.EDU_HOST + "syscourse/classend";
    String ADD_COURSE_URL = BuildConfig.EDU_HOST + "syscourse/pushCourse";
    String MANAGE_STUDENT_URL = BuildConfig.EDU_HOST + "syscourse/personseat";
    String GET_STUDENT_LIST = BuildConfig.EDU_HOST + "clecture/courseEntry";
//
//    String COUSE_TAB1 = "http://tvl.hongguaninfo.com/tvl_edu/syscourse/classing";
//
//    String COUSE_TAB2 = "http://tvl.hongguaninfo.com/tvl_edu/syscourse/classnobegin";
//
//    String COUSE_TAB3 = "http://tvl.hongguaninfo.com/tvl_edu/syscourse/classend";
//
//    String ADD_COURSE_URL = "http://tvl.hongguaninfo.com/tvl_edu/syscourse/pushCourse";
}
