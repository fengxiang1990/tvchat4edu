package com.bizcom.vc.hg.web;

public class ConstantParams {
    /**
     * 服务器固定加的id前缀
     */
    public static final String WEB_PREFIX = "11";

    /**
     * 消息执行结果-成功
     */
    public static final int MESSAGE_RESULT_SUCCESS = 0;
    /**
     * 消息执行结果-失败
     */
    public static final int MESSAGE_RESULT_FAIL = 1;

    /**
     * 手机绑定成功时，手机发送到TV的提醒
     */
    public static final int MESSAGE_TYPE_BIND = 0;

    /**
     * 手机解绑TV时，手机发送到TV的提醒
     */
    public static final int MESSAGE_TYPE_UNBIND = 1;

    /**
     * 手机发送好友到TV
     */
    public static final int MESSAGE_TYPE_FRIEND_ADD = 2;

    /**
     * 手机好友从TV移除
     */
    public static final int MESSAGE_TYPE_FRIEND_DEL = 3;

    /**
     * 手机发起通过TV与某好友进行视频通话,如果该消息为实时消息，则返回手机消息，可正常操作。
     */
    public static final int MESSAGE_TYPE_VIDEO_START = 4;

    /**
     * 手机发送静音操作
     */
    public static final int MESSAGE_TYPE_MUTE = 5;

    /**
     * 手机发送挂断操作
     */
    public static final int MESSAGE_TYPE_HANGHP = 6;

    /**
     * 手机发送切换主副屏
     */
    public static final int MESSAGE_TYPE_CHANGVIDEO = 7;

    /**
     * TV解绑手机成功，发送到手机的提醒
     */
    public static final int MESSAGE_TYPE_UNBIND_TV = 8;

    /**
     * 手机通过TV呼叫第三方，TV发送消息告诉第三方，当前来电是谁。
     */
    public static final int MESSAGE_TYPE_OTHER_CHANGE_NAME = 9;

    /**
     * 手机通过TV呼叫第三方，成功通话，发送消息告诉手机
     */
    public static final int MESSAGE_TYPE_VIDEO_CONNECTED = 10;

    /**
     * 手机通过TV呼叫第三方，未接听，发送消息告诉手机
     */
    public static final int MESSAGE_TYPE_VIDEO_NO_ANSWER = 11;

    /**
     * 手机通过TV呼叫第三方，拒接，发送消息告诉手机
     */
    public static final int MESSAGE_TYPE_VIDEO_REFUSE = 12;

    /**
     * 表情类型 --{"tyep":MESSAGE_TYPE_MULTI,"imgeNum":0}
     */
    public static final int MESSAGE_TYPE_MULTI = 13;

    /**
     * 表情类型 --{"tyep":MESSAGE_TYPE_THEME,"themeNum":0}
     */
    public static final int MESSAGE_TYPE_THEME = 14;


    /**
     * 手机通过TV呼叫第三方，挂断（无论是TV挂断还是第三方挂断），第三方发送消息告诉手机挂断
     */
    public static final int MESSAGE_TYPE_VIDEO_HangUp = 15;

    /**
     * 手机通过TV协助中修改TV昵称消息
     */
    public static final int MESSAGE_TYPE_ChangeFriend_Name = 16;


    /**
     * 发送图片
     */
    public static final int MESSAGE_TYPE_PICTURE = 17;
    public static long callRemoteUserId = -1l;


    /**
     * 手机批量发送好友到TV
     */
    public static final int MESSAGE_TYPE_FRIEND_BATCH_ADD = 18;


    /**
     * 手机批量发送好友删除到TV
     */
    public static final int MESSAGE_TYPE_FRIEND_BATCH_DEL = 19;

    /**
     * 本地视频关闭通知视频对端
     */
    public static final int MESSAGE_TYPE_VIDEO_CLOSE = 20;

    /**
     * TV本地摄像头异常[未插入、摄像头损坏....]
     */
    public static final int MESSAGE_TYPE_CAMERA_ERROR = 21;


    /**
     * 同意对方发言
     */
    public static final int MESSAGE_TYPE_AGREE_SPEAK = 22;


    /**
     * 不同意对方发言
     */
    public static final int MESSAGE_TYPE_NOT_AGREE_SPEAK = 23;

    /**
     * 申请发言
     */
    public static final int MESSAGE_TYPE_APPLY_SPEAK = 24;

    /**
     * 移除学生
     */
    public static final int MESSAGE_TYPE_REMOVE_BY_TEACHER = 25;


    /**
     * 通知学生老师离开
     */
    public static final int MESSAGE_NOTIFY_TEACHER_LEAVE = 26;


    /**
     * 通知学生课程结束
     */
    public static final int MESSAGE_NOTIFY_CLOSE_CLASS = 27;


    /**
     * 通知学生老师回来了
     */
    public static final int MESSAGE_TEACHER_ONLINE = 28;

    /**
     * 一键禁言
     */
    public static final int MESSAGE_TYPE_JINYAN = 29;


    /**
     * 老师进入后台
     */
    public static final int MESSAGE_TYPE_TEACHER_IN_BACKGROUND = 30;

}

