package com.bizcom.vc.hg.web.interf;

import android.graphics.Bitmap;

import com.bizcom.vc.hg.ui.edu.bridgehandler.model.CheckUserRoleResponse;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.StudentInfo;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.StudentInfo2;
import com.bizcom.vc.hg.web.models.NickName;
import com.bizcom.vc.hg.web.models.UserCenterCallLong;

import java.util.List;

public interface IBussinessManager {
    public interface OnResponseListener {
        public void onResponse(boolean isSuccess, int what, Object obj);

        public interface BooleanCallBack {
            public void onResponse(boolean isSuccess);
        }
    }

    public interface FileUploadCallBack {
        public void onStart(int max);

        public void onSuccess(String s);

        public void onFailed(String s);

        public void onUpload(int progress);

    }

    /**
     * 获取服务器地址
     */
    public void serviceAddr(OnResponseListener lis, String channel);

    /**
     * sn	String	Y	TV唯一标识
     * channel	String	Y	渠道：
     * TV与MOBILE
     * TV为电视端
     * MOBILE为手机
     */
    public void tvAdd(OnResponseListener lis, String sn, String channel);

    /**
     * 验证tv与用户是否绑定
     * uid	String	Y	用户ID
     * tvId	String	Y	Tv电视ID
     * channel	String	Y	渠道：
     * TV与MOBILE
     * TV为电视端
     * MOBILE为手机
     */
    public void credentials(OnResponseListener lis, String uid, String tvId, String channel);

    /**
     * tv与用户绑定
     * uid	String	Y	用户ID
     * tvId	String	Y	Tv电视ID
     * channel	String	Y	渠道：
     * TV与MOBILE
     * TV为电视端
     * MOBILE为手机
     */
    public void binding(OnResponseListener lis, String uid, String tvId, String channel);

    /**
     * 解除绑定
     * uid	String	Y	用户ID
     * tvId	String	Y	Tv电视ID
     * channel	String	Y	渠道：
     * TV与MOBILE
     * TV为电视端
     * MOBILE为手机
     */
    public void removeBingding(OnResponseListener lis, String uid, String tvId, String channel);

    /**
     * 查询TV与用户是否为好友关系
     * friendId	String	Y	好友ID
     * tvId	String	Y	Tv电视ID
     * channel	String	Y	渠道：
     * TV与MOBILE
     * TV为电视端
     * MOBILE为手机
     */
    public void queryTvAndUserRel(OnResponseListener lis, String friendId, String tvId, String channel);

    /**
     * TV添加好友
     * friendId
     * tvId
     * uid
     * channel
     */
    public void addTvFriend(OnResponseListener lis, String friendId, String tvId, String uid, String channel);

    /**
     * 删除好友
     * friendId
     * tvId
     * uid
     * channel
     */
    public void delTvFriend(OnResponseListener lis, String friendId, String tvId, String uid, String channel);

    /**
     * 查询TV与用户的好友
     * tvId
     * uid
     * channel
     */
    public void queryTvUserFriend(OnResponseListener lis, String tvId, String firendId, String uid, String channel);


    /**
     * 查询TV与用户的好友
     * tvId
     * uid
     * channel
     */
    public void queryTvUserFriend(OnResponseListener lis, String tvId, String uid, String channel);

    /**
     * 查询TV绑定设备列表
     * tvId
     * channel
     */
    public void queryTvUserFriend(OnResponseListener lis, String tvId, String channel);

    /**
     * 查询手机绑定的TV信息
     * tvId
     * channel
     */
    public void queryTvByUid(OnResponseListener lis, String tvId, String channel);

    /**
     * 查询TVID查询TV信息
     * tvId
     * channel
     */
    public void queryTvByTvId(OnResponseListener lis, String tvId, String channel);

    /**
     * 注册
     * phone	String	Y	注册手机号
     * passWord	String	Y	登录密码（需要MD5加密）
     * accountNo	String	N	登录账号（选填：如不填，默认为用户注册手机号）
     * userName	String	N	用户昵称（选填：如不填，默认为用户注册手机号）
     */
    public void registerUser(OnResponseListener lis, String phone, String accountNo, String userName, String passWord);

    /**
     * 根据手机号查询用户账号是否已注册
     * phone	String	Y	注册手机号
     * accountNo	String	N	登录账号（选填：如不填，默认为用户注册手机号）
     */
    public void checkPhoneIsRegister(OnResponseListener lis, String phone, String accountNo);

    /**
     * 验证码
     * sign  md5加密
     *
     * @param lis
     * @param phone
     * @param sign
     */
    public void smsVerificationCode(OnResponseListener lis, String phone, String sign);

    /**
     * 忘记密码  uid是用户id，这个在点击发送验证码的时候会先调用用户是否注册，注册了会返回这个uid
     */
    public void updatePwd(OnResponseListener lis, String uid, String passWord);

    public void queryByTvSn(OnResponseListener lis, String userName);

    /**
     * 版本更新
     *
     * @param lis
     */
    public void versionUpInfo(OnResponseListener lis);


    /**
     * @param fromUserId
     * @param frends     [userid1,userid2]
     */
    public void notifyTvBatchAddFrends(long fromUserId, String frends, int tvid);


    /**
     * @param fromUserId
     * @param frends     [userid1,userid2]
     */
    public void notifyTvBatchDelFrends(long fromUserId, String frends, int tvid);

    /**
     * @param type    消息类型
     * @param imgeNum 表情编号
     * @param toID    接受者的id
     */
    public void notifyTv(int type, int imgeNum, int themeNum, long toID, long tvId);

    public void notifyTv(int type, int imgeNum, int themeNum, long toID, long tvId, String picUrl);

    /**
     * 获取视频支持的编解码
     *
     * @param modle 手机型号
     */
    public void getMediaEncodeType(OnResponseListener lis, String modle);

    /**
     * 错误日志上传
     * account
     * error_message
     * capture_time
     */
    public void clientErrorLog(String account, String error_message, String capture_time);


    /**
     * 修改昵称
     */
    public void updateNickNameByUid(OnResponseListener lis, String nickName, String userId);

    public void mFileUpload(final String filePath, final String fileName, FileUploadCallBack callBack);

    /**
     * @param bm
     * @param fileName
     * @param callBack
     */
    public void mFileUpload(Bitmap bm, final String fileName, FileUploadCallBack callBack);

    //生成聊口令
    public void generateChatPassword(String type, String uid, String pwd, OnResponseListener listener);


    //批量同步好友到TV
    public void syncFrendsBatchToTv(String friends, String tvId, String uid, String channel, String type, OnResponseListener listener);


    //批量同步好友到TV
    public void updateTvPhoto(String uid, String fileName, String data, String channel, OnResponseListener listener);

    /**
     * 获取用户中心通话时长 最近联系数量
     *
     * @param uid
     * @param channel
     * @param listener
     */
    public void getUserCenterCallLong(String uid, String channel, SimpleResponseListener<BaseResponse<UserCenterCallLong>> listener);

    //获取随机昵称
    public void getNickNameByRandom(String userId, SimpleResponseListener<BaseResponse<NickName>> listener);

    //获取通讯录好友
    public void getContactFriends(String accounts, long uid, OnResponseListener listener);


    /**
     * 结束课时（挂断）
     *
     * @param userid      教师用户id
     * @param carrange_id 课时id
     * @param vedioId     会议id
     */
    public void closeCourseTime(String userid,String carrange_id,String courseid, String vedioId, SimpleResponseListener<BaseResponse> listener);


    /**
     * 校验登陆人是否是老师
     *
     * @param account
     * @param listener
     */
    public void checkUserRole(String account, SimpleResponseListener<BaseResponse<CheckUserRoleResponse>> listener);


    /**
     * 获取学生信息
     *
     * @param uid
     * @param courseId
     * @param listener
     */
    public void getStudentInfo(String uid, String courseId, SimpleResponseListener<BaseResponse<StudentInfo>> listener);


    /**
     * 获取所有学生信息
     *
     * @param courseId
     * @param listener
     */
    public void getStudentList(String courseId, SimpleResponseListener<BaseResponse<List<StudentInfo2>>> listener);
}
