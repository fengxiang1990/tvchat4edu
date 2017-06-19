package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.ui.edu.ConferenceActivity2;
import com.bizcom.vc.hg.ui.edu.ConferenceHelper;
import com.bizcom.vc.hg.ui.edu.CourseInfoSingleton;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * 老师中途加入会议
 * Created by admin on 2017/1/18.
 */

public class JoinConferenceHandler implements BridgeHandler {

    String tag = "JoinConferenceHandler";

    List<Conversation> mItemList = new ArrayList<>();

    Handler handler;
    Context context;

    //public String carrange_id;
    //public String course_id;
    public JoinConferenceHandler(Context context, Handler handler) {
        this.handler = handler;
        this.context = context;
    }

    /**
     * teacherUid  type java.lang.Long
     * 会议id confId type java.lang.Long
     * teacherName type java.lang.String
     *
     * @param data     {"teacherUid":xxxx,"teacherName":xxxx,"confId":xxxxxx}
     * @param function
     */
    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag, "response from js-->" + data);
        if (!cameraIsCanUse()) {
            Toast.makeText(context, "相机故障,请检查您的摄像头权限是否已打开", Toast.LENGTH_LONG).show();
            return;
        }
        WaitDialogBuilder.showNormalWithHintProgress(context);
        long confId = 0;
        if (!TextUtils.isEmpty(data)) {
            Node node = new Gson().fromJson(data, Node.class);
            Log.e(tag, "node-->" + node.toString());
            //carrange_id = node.carrange_id;
            //course_id = node.course_id;
            CourseInfoSingleton.getCourseInfo().setEndTime(node.endTime);
            CourseInfoSingleton.getCourseInfo().setTeacher_name(node.teacherName);
            CourseInfoSingleton.getCourseInfo().setCourse_id(node.course_id);
            CourseInfoSingleton.getCourseInfo().setCarrange_id(node.carrange_id);
            if (TextUtils.isEmpty(node.confId)) {
                confId = 0;
            } else {
                confId = Long.parseLong(node.confId);
            }
        }
        initConference();
        ConferenceHelper.getConferenceHelper().requestExitConference(confId, handler);
//        for (Conversation conversation : mItemList) {
//            if (conversation.getExtId() == confId) {
//                ConferenceHelper.getConferenceHelper().requestExitConference(conversation.getExtId(), handler);
//                break;
//            }
//        }
    }


//    public void checkOrAddGroup(long confId){
//        for( Conversation cov:mItemList){
//            if(cov.getExtId()){
//
//            }
//        }
//    }

    void initConference() {
        List<Group> gl = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONFERENCE);
        if (gl != null && gl.size() > 0) {
            populateConversation(gl);
        }
    }

    /**
     * According populateType to fill the List Data. The data from server!
     *
     * @param list
     */
    private void populateConversation(List<Group> list) {
        mItemList.clear();
        for (int i = list.size() - 1; i >= 0; i--) {
            Group g = list.get(i);
            if (TextUtils.isEmpty(g.getName())) {
                Log.e(tag, "Recv bad conference , no name , id : " + g.getGroupID());
                continue;
            } else {
                Log.e(tag, "Recv new conference, name: " + g.getName() + " | id : " + g.getGroupID());
            }
            Conversation cov = new ConferenceConversation(g, false);
            mItemList.add(cov);
        }
    }


    class Node {
        public long teacherUid;
        public String confId;
        public String teacherName;
        public String carrange_id;
        public String course_id;
        public String endTime;

        @Override
        public String toString() {
            return "Node{" +
                    "teacherUid=" + teacherUid +
                    ", confId='" + confId + '\'' +
                    ", teacherName='" + teacherName + '\'' +
                    ", carrange_id='" + carrange_id + '\'' +
                    ", course_id='" + course_id + '\'' +
                    ", endTime='" + endTime + '\'' +
                    '}';
        }
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
