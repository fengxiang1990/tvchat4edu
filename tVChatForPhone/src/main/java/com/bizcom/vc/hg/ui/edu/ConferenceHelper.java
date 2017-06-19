package com.bizcom.vc.hg.ui.edu;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.bizcom.request.V2ConferenceRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.UserDeviceConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.util.Date;
import java.util.List;

/**
 * 多人视频帮助类
 * Created by fengxiang on 2017/1/9.
 */

public class ConferenceHelper {

    String tag = "ConferenceHelper";
    public static final int QUIT_CONFERENC_RESP = 6;
    public static final int CREATE_CONFERENC_RESP = 4;
    public static final int FILL_CONFS_LIST = 1;
    public static final int REQUEST_ENTER_CONF = 2;
    public static final int REQUEST_ENTER_CONF_RESPONSE = 3;
    public static final int REQUEST_UPDATE_ADAPTER = 5;
    public static final int REQUEST_EXIT_CONF = 7;
    public static final int REQUEST_CLOSE_VIDEO_DEVICE = 8;

    private static int POST_DELAY_ENTER_CONF = 3000;
    private static V2ConferenceRequest conferenceRequest = new V2ConferenceRequest();

    private static ConferenceHelper conferenceHelper = new ConferenceHelper();

    private ConferenceHelper() {

    }

    public static synchronized ConferenceHelper getConferenceHelper() {
        return conferenceHelper;
    }

    /**
     * 生成会议对象
     *
     * @param title 会议标题
     * @param date  开始日期
     * @param users 参会人
     * @return
     */
    public Conference getConference(String title, Date date, List<User> users) {
        return new Conference(title, date, null, users);
    }

    /**
     * 创建会议
     *
     * @param conference
     * @param handler
     */
    public void createConference(Conference conference, Handler handler) {
      //  reset(handler);
        conferenceRequest.createConference(conference, new HandlerWrap(handler, CREATE_CONFERENC_RESP, conference));
    }


    /**
     * 请求进入会议
     *
     * @param confID
     * @param handler
     */
    public void requestEnterConference(long confID, Handler handler) {
        Conference conference = new Conference(confID);
        conferenceRequest.requestEnterConference(conference,
                new HandlerWrap(handler, REQUEST_ENTER_CONF_RESPONSE, conference));
    }


    /**
     * 请求退出会议
     *
     * @param confID
     * @param handler
     */
    public void requestExitConference(long confID, Handler handler) {
        Conference conference = new Conference(confID);
        conferenceRequest.requestExitConference(conference,
                new HandlerWrap(handler, REQUEST_EXIT_CONF, conference));
    }


    /**
     * 关闭视频
     * @param udc
     * @param handler
     */
    public void requestCloseVideoDevice(UserDeviceConfig udc,Handler handler){
        Log.e(tag,"requestCloseVideoDevice-->"+udc.getDeviceID());
        conferenceRequest.requestCloseVideoDevice(udc,new HandlerWrap(handler, REQUEST_CLOSE_VIDEO_DEVICE,udc));
    }

    void reset(Handler handler) {
        List<Group> gl = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONFERENCE);
            if (gl != null && gl.size() > 0) {
            populateConversation(gl, handler);
        }
    }

    /**
     * According populateType to fill the List Data. The data from server!
     *
     * @param list
     */
    private void populateConversation(List<Group> list, Handler handler) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Group g = list.get(i);
            if (TextUtils.isEmpty(g.getName())) {
                Log.e(tag, "Recv bad conference , no name , id : " + g.getGroupID());
                continue;
            } else {
                Log.e(tag, "Recv new conference, name: " + g.getName() + " | id : " + g.getGroupID());
            }
            Conversation cov = new ConferenceConversation(g, false);
            long confId = cov.getExtId();
            Conference conference = new Conference(confId);
            conferenceRequest.requestExitConference(conference, new HandlerWrap(handler, QUIT_CONFERENC_RESP, conference));
        }
    }

}
