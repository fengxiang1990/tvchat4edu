package com.V2.jni.callback;

public interface ImRequestCallback {

    /**
     * 我登录的回调
     *
     * @param nUserID    if succeed means user ID, otherwise 0
     * @param nStatus
     * @param nResult    0: succeed, 1: failed
     * @param serverTime
     * @param sDBID      : Server id
     */
    void OnLoginCallback(long nUserID, int nStatus, int nResult, long serverTime, String sDBID);

    /**
     * 用户在其他设备上登录 called
     *
     * @param nType device type of logged
     */
    void OnLogoutCallback(int nType);

    /**
     * 与服务器连接状态改变的回调
     *
     * @param nResult 301 can't not connect server; 0: succeed
     */
    void OnConnectResponseCallback(int nResult);

    /**
     * 更新基本信息
     *
     * @param nUserID
     * @param updatexml
     */
    void OnUpdateBaseInfoCallback(long nUserID, String updatexml);

    /**
     * 用户状态更新的回调
     *
     * @param nUserID
     * @param nType        1 PC 2 cell phone
     * @param nStatus      1 is online, 0 is offline
     * @param szStatusDesc
     */
    void OnUserStatusUpdatedCallback(long nUserID, int nType, int nStatus, String szStatusDesc);

    /**
     * 好友头像更新
     *
     * @param bSystemAvatar 是系统头像还是自定义头像
     * @param nUserID       用户ID
     * @param Filename      头像本地路径
     */
    void OnChangeAvatarCallback(int bSystemAvatar, long nUserID, String Filename);

    /**
     * 修改备注姓名
     *
     * @param nUserId
     * @param sCommmentName
     */
    void OnModifyCommentNameCallback(long nUserId, String sCommmentName);

    /**
     * 检测到客户端有更新
     *
     * @param updatefilepath
     * @param updatetext
     */
    void OnHaveUpdateNotify(String updatefilepath, String updatetext);

    /**
     * 开始下载更新文件
     *
     * @param filesize
     */
    void OnUpdateDownloadBegin(long filesize);

    /**
     * 正在下载更新
     *
     * @param size
     */
    void OnUpdateDownloading(long size);

    /**
     * 下载更新完成
     *
     * @param error
     */
    void OnUpdateDownloadEnd(boolean error);

    /**
     * 开始获得组, 组成员信息
     */
    void OnGetGroupsInfoBegin();

    /**
     * 获得所有组, 组成员信息结束
     */
    void OnGroupsLoaded();

    /**
     * 开始获得离线消息
     */
    void OnOfflineStart();

    /**
     * 获得离线消息结束
     */
    void OnOfflineEnd();

    /**
     * 连接成功的情况下和服务器连接断开
     */
    void OnSignalDisconnected();

    /**
     * 在服务器上查找用户的回调函数
     *
     * @param xmlinfo 返回的用户列表的XML
     */
    void OnSearchUserCallback(String xmlinfo);

    /**
     * 创建验证码回调
     *
     * @param ret 返回值： 0=成功 ERR_IM_CREATE_VALIDATECODE=生成验证码失败
     *            ERR_IM_SEND_VALIDATECODE=发送短信失败 ERR_IM_ALREADY_REGISTED=账号已被注册
     *            ERR_IM_WRONG_PHONENUMBER=手机号格式错误
     *            ERR_IM_CON_REGSERVER_FAIL=连接服务器失败
     */
    void OnImUserCreateValidateCode(int ret);

    /**
     * 手机用户注册回调
     *
     * @param ret 返回值： 0=成功 ERR_IM_REG_FAIL=注册失败 ERR_IM_INVALIDATECODE=无效验证码
     *            ERR_IM_WRONG_PHONENUMBER=手机号格式错误
     *            ERR_IM_CON_REGSERVER_FAIL=连接服务器失败
     *            ERR_IM_ALREADY_REGISTED=账号已被注册
     */
    void OnImRegisterPhoneUser(int ret);

    /**
     * 更新密码回调
     *
     * @param ret 0=成功 其它=失败
     */
    void OnImUpdateUserPwd(int ret);

    /**
     * 游客注册回调
     *
     * @param sAccount 注册的账号
     * @param sPwd     该帐号密码
     * @param ret      结果 0代表成功,其它失败
     */
    void OnImRegisterGuest(String sAccount, String sPwd, int ret);
}
