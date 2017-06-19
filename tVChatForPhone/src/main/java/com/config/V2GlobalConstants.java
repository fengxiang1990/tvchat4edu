package com.config;

public class V2GlobalConstants {

    /**
     * 用户登录错误信息：用户异地登陆
     */
    public static final int ERR_LOGIN_OTHER_PLACE_LOGIN = 1;

    /**
     * 用户登录错误信息：密码修改
     */
    public static final int ERR_LOGIN_PASSWORD_CHANGE= 2;

    /**
     * 用户登录错误信息：系统资源被修改
     */
    public static final int ERR_LOGIN_RESOURCE_CHANGE= 3;

    /**
     * 用户登录错误信息：组织被删除
     */
    public static final int ERR_LOGIN_DELETE = 4;

	/**
	 * 用户登录错误信息：组织不可用
	 */
	public static final int ERR_LOGIN_ORGDISABLED = 5;

	/**
	 * 用户注销登录
	 */
	public static final int ERR_LOGIN_OUT = 6;

    /**
     * 用户被禁用
     */
    public static final int ERR_LOGIN_USER_DISABLE= 7;

	/**
	 * 组的类型：个人
	 */
	public static final int GROUP_TYPE_USER = 0;

	/**
	 * 组的类型：组织
	 */
	public static final int GROUP_TYPE_DEPARTMENT = 1;

	/**
	 * 组的类型：好友
	 */
	public static final int GROUP_TYPE_CONTACT = 2;

	/**
	 * 组的类型：群组
	 */
	public static final int GROUP_TYPE_CROWD = 3;

	/**
	 * 组的类型：会议
	 */
	public static final int GROUP_TYPE_CONFERENCE = 4;

	/**
	 * 组的类型：讨论组
	 */
	public static final int GROUP_TYPE_DISCUSSION = 5;

	/**
	 * 未读标识，适用于各种场景。
	 */
	public static final int READ_STATE_UNREAD = 0;

	/**
	 * 已读标识，适用于各种场景。
	 */
	public static final int READ_STATE_READ = 1;

	/**
	 * 账户类型：未注册用户，用于快速入会
	 */
	public static final int ACCOUNT_TYPE_NON_REGISTERED = 2;

	/**
	 * 账户类型：电话联系人(仅PC能用，移动端屏蔽)
	 */
	public static final int ACCOUNT_TYPE_PHONE_FRIEND = 3;

	/**
	 * 用户状态:在线
	 */
	public static final int USER_STATUS_ONLINE = 1;

	/**
	 * 用户状态:离线
	 */
	public static final int USER_STATUS_OFFLINE = 0;

	/**
	 * 用户状态:离开
	 */
	public static final int USER_STATUS_LEAVING = 2;

	/**
	 * 用户状态:繁忙
	 */
	public static final int USER_STATUS_BUSY = 3;

	/**
	 * 用户状态:请勿打扰
	 */
	public static final int USER_STATUS_DO_NOT_DISTURB = 4;

	/**
	 * 用户状态:隐身
	 */
	public static final int USER_STATUS_HIDDEN = 5;

	/**
	 * 会议进入时错误信息：该会议已被删除
	 */
	public static final int CONF_ERROR_CONFOVER = 204;

	/**
	 * 会议进入时错误信息：与服务器断开连接
	 */
	public static final int CONF_ERROR_SERVERDISCONNECT = 206;

	/**
	 * 会议中语音激励状态：未激活
	 */
	public static final int CONF_VOICE_ACTIVATION_NO = 0;

	/**
	 * 会议中语音激励状态：已激活
	 */
	public static final int CONF_VOICE_ACTIVATION_YES = 1;

	/**
	 * 会议中文档的类型：正常文档
	 */
	public static final int DOC_TYPE_NORMAL = 3;

	/**
	 * 会议中文档的类型：白板
	 */
	public static final int DOC_TYPE_BLACK = 4;

	/**
	 * 文件的发送类型：在线发送
	 */
	public static final int FILE_TYPE_ONLINE = 1;

	/**
	 * 文件的发送类型：离线发送
	 */
	public static final int FILE_TYPE_OFFLINE = 2;

	/**
	 * 文件的发送类型：离线发送，未加密
	 */
	public static final int FILE_ENCRYPT_TYPE = 1;

	/**
	 * 文件传输中状态：正在发送
	 */
	public static final int FILE_TRANS_SENDING = 10;

	/**
	 * 文件传输中状态：正在下载
	 */
	public static final int FILE_TRANS_DOWNLOADING = 11;

	/**
	 * 文件传输中状态：传输错误
	 */
	public static final int FILE_TRANS_ERROR = 13;

	/**
	 * 点对点语音留言状态：开始录音
	 */
	public static final int RECORD_TYPE_START = 0x0001;

	/**
	 * 点对点语音留言状态：结束录音
	 */
	public static final int RECORD_TYPE_STOP = 0x0002;
	
	public static final String SETTINGS_VISIBILE = "1";
	public static final String SETTINGS_INVISIBILE = "0";

	public static final int MESSAGE_SHOW_TIME = 14;

	public static final int MESSAGE_NOT_SHOW_TIME = 15;

	public static final int UNKOWN = 0;
	public static final int EVIDEODEVTYPE_VIDEO = 1;
	public static final int EVIDEODEVTYPE_CAMERA = 2;
	public static final int EVIDEODEVTYPE_FILE = 3;
	public static final int EVIDEODEVTYPE_VIDEOMIXER = 4;
	
	public static final int CONF_CAMERA_MASS_LOW = 1;
	public static final int CONF_CAMERA_MASS_MIDDLE = 2;
	public static final int CONF_CAMERA_MASS_HIGH = 3;
	public static final int CONF_CAMERA_CUSTOMER = 0;//这里自定义

	/**
	 * ViewPager的Tab页类型
	 */
	public static final String TAG_CONTACT = "contacts";
	public static final String TAG_ORG = "org";
	public static final String TAG_COV = "conversation";
	public static final String TAG_CONF = "conference";
	public static final String TAG_GROUP = "group";
	/**
	 * ViewPager的Tab页类型
	 */
	public static final String TAG_HTAB0 = "htab0";
	public static final String TAG_HTAB1 = "htab1";
	public static final String TAG_HTAB2 = "htab2";
	public static final String TAG_HTAB3 = "htab3";

	public static final int MESSAGE_NOTIFICATION_ID = 1;
	public static final int VIDEO_NOTIFICATION_ID = 2;
	public static final int APPLICATION_STATUS_BAR_NOTIFICATION = 3;
	
	/**
	 * 搜索的类型
	 */
	public static final int SEARCH_REQUEST_TYPE_GROUP = 1;
	public static final int SEARCH_REQUEST_TYPE_USER = 2;
	
	public static final int AVATAR_ORG = 1;
	public static final int AVATAR_NORMAL = 2;

    /**
     * 服务器推送的消息类型
     */
    public static final String JPUSH_MESSAGE_CHAT = "EPUSHMSGTYPE_CHAT";
    public static final String JPUSH_VIDEO_CHAT = "EPUSHMSGTYPE_VIDEO";
    public static final String JPUSH_AUDIO_CHAT = "EPUSHMSGTYPE_AUDIO";
    public static final String JPUSH_CONF_CHAT = "EPUSHMSGTYPE_CONF";
}
