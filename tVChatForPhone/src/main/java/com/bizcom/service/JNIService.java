package com.bizcom.service;

import android.app.Dialog;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.widget.Toast;

import com.MainApplication;
import com.V2.jni.AppShareRequest;
import com.V2.jni.AudioRequest;
import com.V2.jni.ChatRequest;
import com.V2.jni.ConfRequest;
import com.V2.jni.FileRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.ImRequest;
import com.V2.jni.SipRequest;
import com.V2.jni.VideoRequest;
import com.V2.jni.WebManagerRequest;
import com.V2.jni.callbacAdapter.AppShareRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.AudioRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.ChatRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.ConfRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.FileRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.GroupRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.ImRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.SipRequestCallBackAdapter;
import com.V2.jni.callbacAdapter.VideoRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.WebManagerRequestCallbackAdapter;
import com.V2.jni.callback.ImRequestCallback;
import com.V2.jni.ind.BoUserInfoBase;
import com.V2.jni.ind.BoUserInfoGroup;
import com.V2.jni.ind.BoUserInfoShort;
import com.V2.jni.ind.FileJNIObject;
import com.V2.jni.ind.GroupJoinErrorJNIObject;
import com.V2.jni.ind.GroupQualicationJNIObject;
import com.V2.jni.ind.JNIObjectInd;
import com.V2.jni.ind.JNIObjectInd.JNIIndType;
import com.V2.jni.ind.V2ConfSyncVideoJNIObject;
import com.V2.jni.ind.V2Conference;
import com.V2.jni.ind.V2Group;
import com.V2.jni.ind.VideoJNIObjectInd;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.bo.UserAvatarObject;
import com.bizcom.bo.UserStatusObject;
import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.db.provider.VerificationProvider;
import com.bizcom.request.V2ChatRequest;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.FileDownLoadErrorIndication;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.DialogManager;
import com.bizcom.util.FileUtils;
import com.bizcom.util.Notificator;
import com.bizcom.util.V2Log;
import com.bizcom.util.XmlAttributeExtractor;
import com.bizcom.util.XmlParser;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.activity.contacts.AddFriendHistroysHandler;
import com.bizcom.vc.activity.main.MainActivity;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.listener.CommonCallBack.CommonCrowdRequestNotifyJniService;
import com.bizcom.vc.listener.CommonCallBack.CommonUpdateConversationStateInterface;
import com.bizcom.vo.AddFriendHistorieNode;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.FileDownLoadBean;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.UserDeviceConfig;
import com.bizcom.vo.VideoBean;
import com.bizcom.vo.enums.GroupQualicationState;
import com.bizcom.vo.enums.NetworkStateCode;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageFileItem.FileType;
import com.bizcom.vo.meesage.VMessageFileRecvItem;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageQualification;
import com.bizcom.vo.meesage.VMessageQualification.QualificationState;
import com.bizcom.vo.meesage.VMessageQualification.ReadState;
import com.bizcom.vo.meesage.VMessageQualification.Type;
import com.bizcom.vo.meesage.VMessageQualificationApplicationCrowd;
import com.bizcom.vo.meesage.VMessageQualificationInvitationCrowd;
import com.bizcom.vo.meesage.VMessageTextItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.GlobalState;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * This service is used to wrap JNI call.<br>
 * JNI calls are asynchronous, we don't expect activity involve JNI.<br>
 *
 * @author 28851274
 */
public class JNIService extends Service implements
        CommonUpdateConversationStateInterface,
        CommonCrowdRequestNotifyJniService {
    private static final String TAG = JNIService.class.getSimpleName();

    public static final int BINARY_TYPE_AUDIO = 3;
    public static final int BINARY_TYPE_IMAGE = 2;

    public static final String JNI_ACTIVITY_CATEGROY = "com.v2tech";
    public static final String JNI_BROADCAST_CATEGROY = "com.v2tech.jni.broadcast";

    public static final String JNI_BROADCAST_CONNECT_STATE_NOTIFICATION = "com.v2tech.jni.broadcast.connect_state_notification";
    public static final String JNI_BROADCAST_USER_STATUS_NOTIFICATION = "com.v2tech.jni.broadcast.user_stauts_notification";
    public static final String JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION = "com.v2tech.jni.broadcast.user_log_out_notification";
    public static final String JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION = "com.v2tech.jni.broadcast.file_stauts_error_notification";
    /**
     * Notify user avatar changed, notice please do not listen this broadcast if
     * you are UI. Use
     * {@link BitmapManager#registerBitmapChangedListener(com.bizcom.request.util.BitmapManager.BitmapChangedListener)}
     * to listener bitmap change if you are UI.<br>
     * key avatar : #UserAvatarObject
     */
    public static final String JNI_BROADCAST_USER_AVATAR_CHANGED_NOTIFICATION = "com.v2tech.jni.broadcast.user_avatar_notification";
    public static final String JNI_BROADCAST_USER_UPDATE_BASE_INFO = "com.v2tech.jni.broadcast.user_update_base_info";
    public static final String JNI_BROADCAST_GROUP_NOTIFICATION = "com.v2tech.jni.broadcast.group_geted";
    public static final String JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION = "com.v2tech.jni.broadcast.group_user_updated";
    public static final String JNI_BROADCAST_GROUP_UPDATED = "com.v2tech.jni.broadcast.group_updated";
    public static final String JNI_BROADCAST_GROUP_JOIN_FAILED = "com.v2tech.jni.broadcast.group_join_failed";
    public static final String JNI_BROADCAST_NEW_MESSAGE = "com.v2tech.jni.broadcast.new.message";
    public static final String JNI_BROADCAST_MESSAGE_SENT_RESULT = "com.v2tech.jni.broadcast.message_sent_result";

    // 会议相关的广播
    public static final String JNI_BROADCAST_NEW_CONF_MESSAGE = "com.v2tech.jni.broadcast.new.conf.message";
    public static final String JNI_BROADCAST_CONFERENCE_INVATITION = "com.v2tech.jni.broadcast.conference_invatition_new";
    public static final String JNI_BROADCAST_CONFERENCE_REMOVED = "com.v2tech.jni.broadcast.conference_removed";
    public static final String JNI_BROADCAST_CONFERENCE_REMOVED_SIP_CALL = "com.v2tech.jni.broadcast.conference_removed_sip_call";
    public static final String JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO = "com.v2tech.jni.broadcast.conference_confSyncOpenVideo";
    public static final String JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO = "com.v2tech.jni.broadcast.conference_confSyncCloseVideo";
    public static final String JNI_BROADCAST_CONFERENCE_CONF_VOD_OPEN_VIDEO = "com.v2tech.jni.broadcast.conference_vodOpenVideo";
    public static final String JNI_BROADCAST_CONFERENCE_APPSHARE_CREATE = "com.v2tech.jni.broadcast.conference_appShareCreate";
    public static final String JNI_BROADCAST_CONFERENCE_APPSHARE_DESTORY = "com.v2tech.jni.broadcast.conference_appShareDestory";

    public static final String JNI_BROADCAST_GROUP_USER_REMOVED = "com.v2tech.jni.broadcast.group_user_removed";
    public static final String JNI_BROADCAST_GROUP_USER_ADDED = "com.v2tech.jni.broadcast.group_user_added";
    public static final String JNI_BROADCAST_VIDEO_CALL_CLOSED = "com.v2tech.jni.broadcast.video_call_closed";
    public static final String JNI_BROADCAST_CONTACTS_AUTHENTICATION = "com.v2tech.jni.broadcast.friend_authentication";
    public static final String JNI_BROADCAST_NEW_QUALIFICATION_MESSAGE = "com.v2tech.jni.broadcast.new.qualification_message";
    public static final String JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO_TO_MOBILE = "com.v2tech.jni.broadcast.conference_OnConfSyncCloseVideoToMobile";
    public static final String JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO_TO_MOBILE = "com.v2tech.jni.broadcast.conference_OnConfSyncOpenVideoToMobile";
    public static final String JNI_BROADCAST_GROUPS_LOADED = "com.v2tech.jni.broadcast.groups_loaded";
    public static final String JNI_BROADCAST_OFFLINE_MESSAGE_END = "com.v2tech.jni.broadcast.offline_message_end";
    // Current user kicked by crowd master key crowd : crowdId
    public static final String JNI_BROADCAST_KICED_CROWD = "com.v2tech.jni.broadcast.kick_crowd";
    // Crowd invitation with key crowd
    public static final String JNI_BROADCAST_CROWD_INVATITION = "com.v2tech.jni.broadcast.crowd_invatition";
    // Broadcast for joined new discussion key gid
    public static final String BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION = "com.v2tech.jni.broadcast.new.upload_crowd_file_message";

    // SipActivity
    public static final String JNI_BROADCAST_SIP_ACCEPT_INVITE = "com.v2tech.jni.broadcast.sipAcceptInvite";
    // SettingActivity
    public static final String JNI_BROADCAST_SETTING_UPDATE_PASSWORD = "com.v2tech.jni.broadcast.settingUpdatePassword";
    // //////////////////////////////////////////////////////////
    // Internal message definition //
    // //////////////////////////////////////////////////////////
    private static final int JNI_UPDATE_USER_INFO = 24;
    private static final int JNI_UPDATE_USER_STATE = 25;
    private static final int JNI_LOG_OUT = 26;
    private static final int JNI_LOG_USER_DELETE = 27;
    private static final int JNI_GROUP_NOTIFY = 35;
    private static final int JNI_GROUP_USER_INFO_NOTIFICATION = 60;
    private static final int JNI_GROUP_LOADED = 63;
    private static final int JNI_OFFLINE_LOADED = 64;
    private static final int JNI_CONFERENCE_INVITATION = 61;
    private static final int JNI_RECEIVED_MESSAGE = 91;
    private static final int JNI_RECEIVED_MESSAGE_BINARY_DATA = 93;
    private static final int JNI_RECEIVED_VIDEO_INVITION = 92;
    private static final int JNI_RECEIVED_SIP_COMING = 94;

    // ////////////////////////////////////////
    // JNI call back definitions
    private ImRequestCallback mImRequestCB;
    private GroupRequestCB mGroupRequestCB;
    private VideoRequestCB mVideoRequestCB;
    private ChatRequestCB mChatRequestCB;
    private ConfRequestCB mConfRequestCB;
    private AudioRequestCB mAudioRequestCB;
    private FileRequestCB mFileRequestCB;
    private SipRequestCB mSipRequestCB;
    private AppShareRequestCB mAppShareRequestCB;
    private WebManagerRequestCB mWebManagerRequestCB;

    private HandlerThread mLocalHandlerThread;
    private LocalHandlerThreadHandler mLocalHandlerThreadHandler;

    private Context mContext;
    private List<Integer> delayBroadcast = new ArrayList<>();
    private List<GroupUserInfoOrig> delayUserBroadcast = new ArrayList<>();
    private List<MessageObject> delayMessageBroadcast = new ArrayList<>();

    private HashMap<String, VMessage> mBinaryCache = new HashMap<>();
    private HashMap<String, VMessage> mSendBinaryCache = new HashMap<>();
    private HashMap<String, Integer> mSendBinaryCacheKey = new HashMap<>();
    private LongSparseArray<UserStatusObject> onLineUsers = new LongSparseArray<>();

    private final LocalBinder mBinder = new LocalBinder();

    private static long lastNotificatorTime = 0;
    private Integer mBinderRef = 0;
    private boolean noNeedBroadcast;
    private boolean isAcceptApply;
    private int mConversationState = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mLocalHandlerThread = new HandlerThread("JNI-Callbck");
        mLocalHandlerThread.start();
        // Ensure that the thread has been started
        while (!mLocalHandlerThread.isAlive()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mLocalHandlerThreadHandler = new LocalHandlerThreadHandler(
                mLocalHandlerThread.getLooper());
        initJNICallback();
        CommonCallBack.getInstance().setConversationStateInterface(this);
        CommonCallBack.getInstance().setCrowdRequestNotifyJniService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        Notification notification = new Notification();
        // 让该service前台运行，避免手机休眠时系统自动杀掉该服务
        // 如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(0, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        V2Log.i(TAG, "onBind...");
        mBinderRef++;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        V2Log.i(TAG, "onUnbind...");
        mBinderRef--;
        // if mBinderRef equals 0 means no activity
        if (mBinderRef == 0) {
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        V2Log.i(TAG, "onDestroy...");
        stopForeground(true);
        ImRequest.getInstance().removeCallback(mImRequestCB);
        GroupRequest.getInstance().removeCallback(mGroupRequestCB);
        VideoRequest.getInstance().removeCallback(mVideoRequestCB);
        ConfRequest.getInstance().removeCallback(mConfRequestCB);
        AudioRequest.getInstance().removeCallback(mAudioRequestCB);
        FileRequest.getInstance().removeCallback(mFileRequestCB);
        SipRequest.getInstance().removeCallback(mSipRequestCB);
        AppShareRequest.getInstance().removeCallback(mAppShareRequestCB);
        WebManagerRequest.getInstance().removeCallback(mWebManagerRequestCB);

        mLocalHandlerThread.quit();
        super.onDestroy();
    }

    @Override
    public void updateConversationState() {
        mConversationState++;
        V2Log.d(TAG, "mConversationState : " + mConversationState);
        if (mConversationState != 2) {
            return;
        }
        V2Log.d(TAG,
                "ALL tabs already builed successfully , send broadcast now!");
        if (delayBroadcast.size() <= 0) {
            V2Log.d(TAG,
                    "There is no broadcast in delayBroadcast collections , mean no callback!");
        } else {
            for (int i = 0; i < delayBroadcast.size(); i++) {
                int type = delayBroadcast.get(i);
                V2Log.d(TAG, "The delay broadcast was sending now , type is : "
                        + type);
                if (type == JNI_GROUP_LOADED) {
                    Intent loaded = new Intent();
                    loaded.addCategory(JNI_BROADCAST_CATEGROY);
                    loaded.setAction(JNI_BROADCAST_GROUPS_LOADED);
                    sendBroadcast(loaded);
                } else if (type == JNI_OFFLINE_LOADED) {
                    Intent offline = new Intent();
                    offline.addCategory(JNI_BROADCAST_CATEGROY);
                    offline.setAction(JNI_BROADCAST_OFFLINE_MESSAGE_END);
                    sendBroadcast(offline);
                } else {
                    Intent gi = new Intent(JNI_BROADCAST_GROUP_NOTIFICATION);
                    gi.putExtra("gtype", type);
                    gi.addCategory(JNI_BROADCAST_CATEGROY);
                    mContext.sendBroadcast(gi);
                }
            }

            for (int i = 0; i < delayUserBroadcast.size(); i++) {
                GroupUserInfoOrig go = delayUserBroadcast.get(i);
                V2Log.d(TAG,
                        "The delay user broadcast was sending now , type is : "
                                + go.gType + "-------");
                Intent intent = new Intent(
                        JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
                intent.addCategory(JNI_BROADCAST_CATEGROY);
                intent.putExtra("gid", go.gId);
                intent.putExtra("gtype", go.gType);
                mContext.sendBroadcast(intent);
            }

            for (int i = 0; i < delayMessageBroadcast.size(); i++) {
                MessageObject msgObj = delayMessageBroadcast.get(i);
                if (msgObj.groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                    Intent intent = new Intent();
                    intent.setAction(JNIService.JNI_BROADCAST_CONFERENCE_INVATITION);
                    intent.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                    intent.putExtra("gid", msgObj.remoteGroupID);
                    intent.putExtra("invite", msgObj.rempteUserID);
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(JNI_BROADCAST_NEW_MESSAGE);
                    intent.addCategory(JNI_BROADCAST_CATEGROY);
                    intent.putExtra("msgObj", msgObj);
                    mContext.sendBroadcast(intent);
                }
            }

            delayBroadcast.clear();
            delayUserBroadcast.clear();
            delayMessageBroadcast.clear();
        }
        noNeedBroadcast = true;
    }

    @Override
    public void crowdRequestNotifyJniService(long msgID) {
        Intent i = new Intent(JNI_BROADCAST_NEW_QUALIFICATION_MESSAGE);
        i.addCategory(JNI_BROADCAST_CATEGROY);
        i.putExtra("msgId", msgID);
        i.putExtra("isNotifyVoice", false);
        mContext.sendBroadcast(i);
    }

    private void initJNICallback() {
        mImRequestCB = new ImRequestCB();
        mGroupRequestCB = new GroupRequestCB();
        mVideoRequestCB = new VideoRequestCB();
        mConfRequestCB = new ConfRequestCB();
        mChatRequestCB = new ChatRequestCB();
        mAudioRequestCB = new AudioRequestCB();
        mFileRequestCB = new FileRequestCB();
        mSipRequestCB = new SipRequestCB();
        mAppShareRequestCB = new AppShareRequestCB();
        mWebManagerRequestCB = new WebManagerRequestCB();

        ImRequest.getInstance().addCallback(mImRequestCB);
        GroupRequest.getInstance().addCallback(mGroupRequestCB);
        VideoRequest.getInstance().addCallback(mVideoRequestCB);
        ConfRequest.getInstance().addCallback(mConfRequestCB);
        ChatRequest.getInstance().setChatRequestCallback(mChatRequestCB);
        AudioRequest.getInstance().addCallback(mAudioRequestCB);
        FileRequest.getInstance().addCallback(mFileRequestCB);
        SipRequest.getInstance().addCallback(mSipRequestCB);
        AppShareRequest.getInstance().addCallbacks(mAppShareRequestCB);
        WebManagerRequest.getInstance().addCallback(mWebManagerRequestCB);
    }

    private void updateFileState(int transType, VMessageFileItem fileItem,
                                 String tag, boolean isAdd) {
        long remoteID;
        VMessage vm = fileItem.getVm();
        if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER)
            remoteID = vm.getToUser().getmUserId();
        else
            remoteID = vm.getGroupId();
        GlobalHolder.getInstance().changeGlobleTransFileMember(transType,
                mContext, isAdd, remoteID, tag);
    }

    private VMessage changekBinaryCache(String szFileID, boolean isFailed,
                                        String filePath) {
        boolean isTrueLocation = false;
        VMessage vm = mBinaryCache.get(szFileID);
        if (vm == null) {
            return null;
        }

        if (vm.getImageItems().size() > 0) {
            int receivedCount = 0;
            List<VMessageImageItem> imageItems = vm.getImageItems();
            for (int i = 0; i < imageItems.size(); i++) {
                VMessageImageItem image = imageItems.get(i);

                if (image.isReceived()) {
                    receivedCount++;
                    continue;
                }

                if (image.getUuid().equals(szFileID)) {
                    if (isFailed) {
                        image.setFilePath("error");
                        image.setState(VMessageAbstractItem.TRANS_SENT_FALIED);
                        V2Log.e(TAG, "接收图片  -" + szFileID + "- 失败或超时!");
                    } else {
                        image.setFilePath(filePath);
                        image.setState(VMessageAbstractItem.TRANS_SENT_SUCCESS);
                    }
                    isTrueLocation = true;
                    image.setReceived(true);
                    receivedCount++;
                }
            }

            if (isTrueLocation) {
                vm.setmXmlDatas(vm.getmXmlDatas());
                vm.currentReplaceImageID = szFileID;
                Message msg = Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE_BINARY_DATA, vm);
                msg.arg1 = 2;
                msg.sendToTarget();
                if (receivedCount == imageItems.size()) {
                    removeImageCache(szFileID, vm);
                }
            }
        } else if (vm.getAudioItems().size() > 0) {
            for (int j = 0; j < vm.getAudioItems().size(); j++) {
                VMessageAudioItem audio = vm.getAudioItems().get(j);
                if (audio.getUuid().equals(szFileID)) {
                    if (isFailed) {
                        audio.setState(VMessageAbstractItem.TRANS_SENT_FALIED);
                        V2Log.e(TAG, "接收留言  -" + szFileID + "- 失败或超时!");
                    } else {
                        audio.setState(VMessageAbstractItem.TRANS_SENT_SUCCESS);
                        audio.setAudioFilePath(filePath);
                    }
                    isTrueLocation = true;
                    break;
                }
            }

            if (isTrueLocation) {
                mBinaryCache.remove(szFileID);
                Message msg = Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE_BINARY_DATA, vm);
                msg.arg1 = 1;
                msg.sendToTarget();
            }
        }
        return vm;
    }

    private void removeImageCache(String messageId, VMessage vMessage) {
        mBinaryCache.remove(messageId);
        Set<Entry<String, VMessage>> entrySet = mBinaryCache.entrySet();
        Iterator<Entry<String, VMessage>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Entry<String, VMessage> next = iterator.next();
            VMessage temp = next.getValue();
            if (temp.getUUID().equals(vMessage.getUUID()))
                mBinaryCache.remove(next);
        }
    }

    public class LocalBinder extends Binder {
        public JNIService getService() {
            return JNIService.this;
        }
    }

    class GroupUserInfoOrig {
        int gType;
        long gId;
        String xml;

        public GroupUserInfoOrig(int gType, long gId, String xml) {
            super();
            this.gType = gType;
            this.gId = gId;
            this.xml = xml;
        }

    }

    private class LocalHandlerThreadHandler extends Handler {

        public LocalHandlerThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case JNI_UPDATE_USER_INFO:
                    BoUserInfoBase boUserBaseInfo = (BoUserInfoBase) msg.obj;
                    GlobalHolder.getInstance().putOrUpdateUser(boUserBaseInfo);
                    // 更新全局群组中用户信息
                    User updateUser = GlobalHolder.getInstance().getUser(
                            boUserBaseInfo.mId);
                    if (updateUser != null) {
                        Set<Group> updateBelongsGorup = updateUser.getBelongsGroup();
                        Iterator<Group> updateIter = updateBelongsGorup.iterator();
                        while (updateIter.hasNext()) {
                            Group iterGroup = updateIter.next();
                            User searchUser = iterGroup.getUser(boUserBaseInfo.mId);
                            if (searchUser != null) {
                                searchUser.setNickName(updateUser.getNickName());
                                searchUser.setCommentName(updateUser.getCommentName());
                            }
                        }

                        Intent intent = new Intent(JNI_BROADCAST_USER_UPDATE_BASE_INFO);
                        intent.addCategory(JNI_BROADCAST_CATEGROY);
                        intent.putExtra("uid", boUserBaseInfo.mId);
                        mContext.sendBroadcast(intent);
                        // 如果nickName不是null,则说明用户昵称被修改，需要通知界面更新
                        if (boUserBaseInfo.mNickName != null) {
                            Intent comment = new Intent();
                            comment.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                            comment.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            comment.putExtra("modifiedUser", boUserBaseInfo.mId);
                            mContext.sendBroadcast(comment);
                        }
                    }
                    break;
                case JNI_UPDATE_USER_STATE:
                    UserStatusObject uso = (UserStatusObject) msg.obj;
                    Intent intent1 = new Intent(
                            JNI_BROADCAST_USER_STATUS_NOTIFICATION);
                    intent1.addCategory(JNI_BROADCAST_CATEGROY);
                    intent1.putExtra("status", uso);
                    mContext.sendBroadcast(intent1);
                    break;
                case JNI_LOG_OUT:
                    int type = msg.arg1;
                    switch (type) {
                        case V2GlobalConstants.ERR_LOGIN_OTHER_PLACE_LOGIN:
                            Toast.makeText(mContext,
                                    R.string.user_logged_with_other_device,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case V2GlobalConstants.ERR_LOGIN_PASSWORD_CHANGE:
                            Toast.makeText(mContext,
                                    R.string.user_logged_with_password_change,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case V2GlobalConstants.ERR_LOGIN_RESOURCE_CHANGE:
                            Toast.makeText(mContext,
                                    R.string.user_logged_with_resource_change,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case V2GlobalConstants.ERR_LOGIN_DELETE:
                            Toast.makeText(mContext,
                                    R.string.user_logged_with_resource_change,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case V2GlobalConstants.ERR_LOGIN_ORGDISABLED:
                            Toast.makeText(mContext,
                                    R.string.user_logged_with_org_disable,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case V2GlobalConstants.ERR_LOGIN_OUT:
                            MainApplication.isLogout = true;
                            noNeedBroadcast = false;
                            isAcceptApply = false;
                            mConversationState = 0;
                            V2Log.d("noNeedBroadcast : " + noNeedBroadcast
                                    + " | isAcceptApply : " + isAcceptApply);
                            delayBroadcast.clear();
                            delayUserBroadcast.clear();
                            delayMessageBroadcast.clear();

                            mBinaryCache.clear();
                            mSendBinaryCache.clear();
                            mSendBinaryCacheKey.clear();
                            onLineUsers.clear();
                            GlobalHolder.getInstance().clearAll();

                            Intent logOut = new Intent(
                                    JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION);
                            logOut.addCategory(JNI_BROADCAST_CATEGROY);
                            mContext.sendBroadcast(logOut);
                            break;
                    }

                    Notificator.cancelAllSystemNotification(mContext);
                    // Send broadcast PREPARE_FINISH_APPLICATION first to let all
                    // activity quit and release resource
                    // Notice: if any activity doesn't release resource, android
                    // will
                    // automatically restart main activity
                    Intent prepareFinish = new Intent();
                    prepareFinish
                            .setAction(PublicIntent.PREPARE_FINISH_APPLICATION);
                    prepareFinish.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    mContext.sendBroadcast(prepareFinish);

                    mLocalHandlerThreadHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GlobalConfig.saveLogoutFlag(mContext);

                            Intent i = new Intent();
                            i.setAction(PublicIntent.FINISH_APPLICATION);
                            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            mContext.sendBroadcast(i);
                        }

                    }, 2000);
                    break;
                case JNI_GROUP_NOTIFY:
                    @SuppressWarnings("unchecked")
                    List<V2Group> gl = (List<V2Group>) msg.obj;
                    if (gl != null && gl.size() > 0) {
                        // 此操作是因为，服务器卸载后会重新加载数据，这是移动端断线重连应该刷新数据
                        if (msg.arg1 == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                            List<Group> conferences = GlobalHolder
                                    .getInstance()
                                    .getGroup(
                                            V2GlobalConstants.GROUP_TYPE_CONFERENCE);
                            if (conferences.size() > gl.size()) {
                                for (int i = 0; i < conferences.size(); i++) {
                                    Group group = conferences.get(i);
                                    boolean isExist = false;
                                    for (int j = 0; j < gl.size(); j++) {
                                        V2Group v2Group = gl.get(j);
                                        if (v2Group.id == group.getGroupID()) {
                                            isExist = true;
                                            break;
                                        }
                                    }

                                    if (!isExist) {
                                        GlobalHolder.getInstance().removeGroup(
                                                group.getGroupType(),
                                                group.getGroupID());
                                    }
                                }
                            }
                        }

                        GlobalHolder.getInstance().updateGroupList(msg.arg1, gl);
                        if (!noNeedBroadcast) {
                            V2Log.d(TAG,
                                    "TabFragmentMessage no builed successfully! Need to delay 'JNI_BROADCAST_GROUP_NOTIFICATION' sending , type is : "
                                            + msg.arg1);
                            delayBroadcast.add(msg.arg1);
                        } else {
                            V2Log.d(TAG,
                                    "TabFragmentMessage already builed successfully! 'JNI_BROADCAST_GROUP_NOTIFICATION' sending , type is : "
                                            + msg.arg1);
                            Intent intent2 = new Intent(
                                    JNI_BROADCAST_GROUP_NOTIFICATION);
                            intent2.putExtra("gtype", msg.arg1);
                            intent2.addCategory(JNI_BROADCAST_CATEGROY);
                            mContext.sendBroadcast(intent2);
                        }
                    }
                    break;
                case JNI_GROUP_USER_INFO_NOTIFICATION:
                    GroupUserInfoOrig go = (GroupUserInfoOrig) msg.obj;
                    if (go != null && go.xml != null) {
                        List<BoUserInfoGroup> boGroupUserInfoList = BoUserInfoGroup
                                .paserXml(go.xml);
                        Group group = GlobalHolder.getInstance().findGroupById(
                                go.gId);
                        if (group == null) {
                            V2Log.e(TAG,
                                    "Load Group users , didn't find group information , group id is : "
                                            + go.gId);
                            return;
                        } else {
                            // FIXME
                            // 此操作是因为，服务器卸载后会重新加载数据或者数据有更改，移动端断线重连应该刷新数据，但还没考虑到组织外用户的情况
                            if (group.getGroupType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                                List<User> users = group.getUsers();
                                if (users != null && users.size() > 0) {
                                    for (int i = 0; i < users.size(); i++) {
                                        long nUserID = users.get(i).getmUserId();
                                        boolean isExist = false;
                                        for (int j = 0; j < boGroupUserInfoList
                                                .size(); j++) {
                                            if (boGroupUserInfoList.get(j).mId == nUserID) {
                                                isExist = true;
                                                break;
                                            }
                                        }

                                        if (!isExist) {
                                            // 删除该用户所有的群组信息
                                            User removed = GlobalHolder
                                                    .getInstance().getUser(nUserID);
                                            Set<Group> belongsGroup = removed
                                                    .getBelongsGroup();
                                            Iterator<Group> iterator = belongsGroup
                                                    .iterator();
                                            while (iterator.hasNext()) {
                                                Group temp = iterator.next();
                                                if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD
                                                        || temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION
                                                        || temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                                                    if (temp.getOwnerUser()
                                                            .getmUserId() == nUserID) {
                                                        GlobalHolder
                                                                .getInstance()
                                                                .removeGroup(
                                                                        temp.getGroupType(),
                                                                        temp.getGroupID());
                                                    } else {
                                                        temp.removeUserFromGroup(nUserID);
                                                    }
                                                }
                                            }

                                            GlobalHolder.getInstance()
                                                    .removeGroupUser(
                                                            group.getGroupID(),
                                                            nUserID);
                                            GroupUserObject obj = new GroupUserObject(
                                                    group.getGroupType(),
                                                    group.getGroupID(), nUserID);
                                            Intent delete = new Intent();
                                            delete.setAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
                                            delete.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                                            delete.putExtra("obj", obj);
                                            sendBroadcast(delete);
                                        }
                                    }
                                }
                            }
                        }

                        // 获取当前更新群组的创建者ID.
                        long ownerUserId = -1;
                        User ownerUser = group.getOwnerUser();
                        if (ownerUser != null)
                            ownerUserId = ownerUser.getmUserId();
                        boolean isDisCreaterExist = false;
                        for (BoUserInfoGroup tempBoGroupUserInfo : boGroupUserInfoList) {
                            // 屏蔽电话联系人
                            if (go.gType == V2GlobalConstants.GROUP_TYPE_CONTACT
                                    && tempBoGroupUserInfo.mAccountType != null) {
                                int accountType = Integer
                                        .valueOf(tempBoGroupUserInfo.mAccountType);
                                if (accountType == V2GlobalConstants.ACCOUNT_TYPE_PHONE_FRIEND)
                                    continue;
                            }

                            boolean isFriend = GlobalHolder.getInstance().isFriend(
                                    tempBoGroupUserInfo.mId);
                            if (isFriend) {
                                User existUser = GlobalHolder.getInstance()
                                        .getExistUser(tempBoGroupUserInfo.mId);
                                if (existUser != null) {
                                    String commentName = existUser.getCommentName();
                                    if (!TextUtils.isEmpty(commentName)) {
                                        tempBoGroupUserInfo.mCommentName = commentName;
                                    }
                                }
                            }

                            // 更新全局集合中用户的信息
                            User existUser = GlobalHolder.getInstance()
                                    .putOrUpdateUser(tempBoGroupUserInfo);
                            // 判断是否是组织外的用户
                            if (go.gType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                                boolean isHas = false;
                                Set<Group> belongs = existUser.getBelongsGroup();
                                Iterator<Group> iterator = belongs.iterator();
                                while (iterator.hasNext()) {
                                    if (iterator.next().getGroupType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                                        isHas = true;
                                        break;
                                    }
                                }

                                if (!isHas) {
                                    existUser.isOutOrg = true;
                                }
                            }

                            if (existUser.getmUserId() == GlobalHolder
                                    .getInstance().getCurrentUserId()) {
                                GlobalHolder.getInstance()
                                        .setCurrentUser(existUser);
                            }

                            long currentUserID = existUser.getmUserId();
                            if (!existUser.isContain
                                    && GlobalHolder.getInstance().getGlobalState()
                                    .isGroupLoaded()) {
                                V2Log.e(TAG,
                                        "The User that id is : "
                                                + existUser.getmUserId()
                                                + " dirty!"
                                                + " Need to get user base infos");

                                V2ImRequest.invokeNative(
                                        V2ImRequest.NATIVE_GET_USER_INFO,
                                        currentUserID);
                            }

                            UserStatusObject userStatusObject = onLineUsers
                                    .get(tempBoGroupUserInfo.mId);
                            if (userStatusObject != null) {
                                existUser.updateStatus(User.Status
                                        .fromInt(userStatusObject.getStatus()));
                                existUser.setDeviceType(User.DeviceType
                                        .fromInt(userStatusObject.getDeviceType()));
                            }

                            // 更新当前群组中创建者的信息
                            if (ownerUserId != -1 && ownerUserId == currentUserID) {
                                group.setOwnerUser(existUser);
                            }

                            // 寻找讨论组的群组是否还在
                            if (go.gType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                                if (currentUserID == ownerUserId)
                                    isDisCreaterExist = true;
                            }
                            group.addUserToGroup(existUser);
                        }

                        // 寻找讨论组的群组是否还在
                        if (go.gType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                            DiscussionGroup dis = (DiscussionGroup) group;
                            if (isDisCreaterExist)
                                dis.setCreatorExist(true);
                            else
                                dis.setCreatorExist(false);
                        }

                        V2Log.w(TAG, "The Group -" + go.gId
                                + "- users info have update over! " + " type is : "
                                + go.gType + "- user size is : "
                                + boGroupUserInfoList.size());
                        if (!noNeedBroadcast) {
                            V2Log.d(TAG,
                                    "TabFragmentMessage no builed successfully! Need to delay sending , type is "
                                            + msg.arg1);
                            delayUserBroadcast.add(go);
                        } else {
                            Intent intent3 = new Intent(
                                    JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
                            intent3.addCategory(JNI_BROADCAST_CATEGROY);
                            intent3.putExtra("gid", go.gId);
                            intent3.putExtra("gtype", go.gType);
                            mContext.sendBroadcast(intent3);
                            // 该操作是如果好友组在离线状态下有增或删，需要重新刷新组
                            if (go.gType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                                Intent intent2 = new Intent(
                                        JNI_BROADCAST_GROUP_NOTIFICATION);
                                intent2.putExtra("gtype",
                                        V2GlobalConstants.GROUP_TYPE_CONTACT);
                                intent2.addCategory(JNI_BROADCAST_CATEGROY);
                                mContext.sendBroadcast(intent2);

                            }
                        }
                    } else {
                        V2Log.e("Invalid group user data");
                    }
                    break;
                case JNI_GROUP_LOADED:
                    V2Log.e(TAG, "The All Group Infos Loaded !");
                    if (!noNeedBroadcast) {
                        V2Log.d(TAG,
                                "TabFragmentMessage no builed successfully! Need to delay sending , type is 锛欽NI_GROUP_LOADED");
                        delayBroadcast.add(JNI_GROUP_LOADED);
                    } else {
                        Intent intent4 = new Intent();
                        intent4.addCategory(JNI_BROADCAST_CATEGROY);
                        intent4.setAction(JNI_BROADCAST_GROUPS_LOADED);
                        sendBroadcast(intent4);
                    }
                    break;
                case JNI_OFFLINE_LOADED:
                    boolean isOfflineEnd = (Boolean) msg.obj;
                    V2Log.e(TAG, "OFFLINE MESSAGE LOAD : " + isOfflineEnd);
                    if (!noNeedBroadcast) {
                        V2Log.d(TAG,
                                "TabFragmentMessage no builed successfully! Need to delay sending , type is 锛欽NI_OFFLINE_LOADED");
                        delayBroadcast.add(JNI_OFFLINE_LOADED);
                    } else {
                        Intent i = new Intent();
                        i.addCategory(JNI_BROADCAST_CATEGROY);
                        i.setAction(JNI_BROADCAST_OFFLINE_MESSAGE_END);
                        sendBroadcast(i);
                    }
                    break;
                case JNI_CONFERENCE_INVITATION:
                    ConferenceGroup g = (ConferenceGroup) msg.obj;
                    long mInviteID = msg.arg1;
                    Group cache = GlobalHolder.getInstance().getGroupById(
                            g.getGroupID());
                    if (cache != null && g.getGroupID() != 0) {
                        // conference already in cache list
                        V2Log.d(TAG,
                                "Current user conference in group:"
                                        + cache.getName() + "  "
                                        + cache.getGroupID()
                                        + " only send Broadcast!");
                        if (GlobalHolder.getInstance().isInMeeting()
                                && GlobalHolder.getInstance().getCurrentMeetingID() == g
                                .getGroupID()) {
                            V2Log.d(TAG,
                                    "refuse new conference , already in meeting !");
                            return;
                        }
                    } else {
                        GlobalHolder.getInstance().addGroupToList(
                                V2GlobalConstants.GROUP_TYPE_CONFERENCE, g);
                    }

                    if (!noNeedBroadcast) {
                        V2Log.d(TAG,
                                "TabFragmentMessage no builed successfully! Need to delay sending , "
                                        + "type is JNI_CONFERENCE_INVITATION");
                        MessageObject msgObj = new MessageObject(
                                V2GlobalConstants.GROUP_TYPE_CONFERENCE,
                                g.getGroupID(), mInviteID, -1);
                        delayMessageBroadcast.add(msgObj);
                    } else {
                        Intent i = new Intent();
                        i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_INVATITION);
                        i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                        i.putExtra("gid", g.getGroupID());
                        i.putExtra("invite", mInviteID);
                        sendBroadcast(i);
                    }
                    break;
                case JNI_RECEIVED_MESSAGE:
                    VMessage vm = (VMessage) msg.obj;
                    if (vm != null) {
                        String action;
                        boolean isDelay = false;
                        if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                            action = JNI_BROADCAST_NEW_CONF_MESSAGE;
                        } else {
                            action = JNI_BROADCAST_NEW_MESSAGE;
                            isDelay = true;
                        }

                        MessageObject msgObj = new MessageObject(vm.getMsgCode(),
                                vm.getGroupId(), vm.getFromUser().getmUserId(),
                                vm.getId());
                        V2Log.d(TAG,
                                "three step , prerare to send new message! isDelay : "
                                        + isDelay + " | noNeedBroadcast : "
                                        + noNeedBroadcast);
                        if (isDelay) {
                            if (!noNeedBroadcast) {
                                V2Log.d(TAG,
                                        "TabFragmentMessage no builed successfully! Need to delay sending , type is JNI_RECEIVED_MESSAGE");
                                delayMessageBroadcast.add(msgObj);
                            } else {
                                Intent msgIntent = new Intent(action);
                                msgIntent
                                        .addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                                msgIntent.putExtra("msgObj", msgObj);
                                mContext.sendBroadcast(msgIntent, null);
                            }
                        } else {
                            Intent msgIntent = new Intent(action);
                            msgIntent
                                    .addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                            msgIntent.putExtra("msgObj", msgObj);
                            mContext.sendBroadcast(msgIntent, null);
                        }
                    }
                    break;
                case JNI_RECEIVED_MESSAGE_BINARY_DATA:
                    VMessage binaryVM = (VMessage) msg.obj;
                    int receiveState = msg.arg1;
                    if (receiveState == 1) {
                        if (binaryVM.getAudioItems() != null) {
                            ChatMessageProvider.updateBinaryAudioItem(binaryVM
                                    .getAudioItems().get(0));
                            CommonCallBack.getInstance()
                                    .executeNotifyChatInterToReplace(binaryVM);
                        }
                    } else {
                        if (binaryVM.getImageItems().size() > 0) {
                            List<VMessageImageItem> imageItems = binaryVM
                                    .getImageItems();
                            V2Log.d("binaryReplace",
                                    "开始替换沙漏  : " + binaryVM.getUUID());
                            for (int i = 0; i < imageItems.size(); i++) {
                                VMessageImageItem vMessageImageItem = imageItems
                                        .get(i);
                                if (vMessageImageItem.getUuid().equals(
                                        binaryVM.currentReplaceImageID)) {
                                    ChatMessageProvider
                                            .updateBinaryImageItem(imageItems
                                                    .get(i));
                                    V2Log.d("binaryReplace", "成功替换了沙漏图片 ，通知界面更新 : "
                                            + imageItems.get(i).getUuid());
                                }
                            }
                        }
                        CommonCallBack.getInstance()
                                .executeNotifyChatInterToReplace(binaryVM);
                    }
                    break;
                case JNI_RECEIVED_VIDEO_INVITION:
                    VideoJNIObjectInd vjoi = (VideoJNIObjectInd) msg.obj;
                    GlobalConfig.startP2PConnectChatByJNI(mContext,
                            ConversationP2PAVActivity.P2P_CONNECT_VIDEO,
                            vjoi.getFromUserId(), true, vjoi.getSzSessionID(),
                            vjoi.getDeviceId(), null, vjoi.getData());
                    break;
                case JNI_LOG_USER_DELETE:
                    Dialog dialog = DialogManager
                            .getInstance()
                            .showSingleNoTitleDialog(
                                    DialogManager.getInstance().new DialogInterface(
                                            mContext,
                                            null,
                                            mContext.getText(R.string.user_logged_with_failed),
                                            mContext.getText(R.string.conversation_quit_dialog_confirm_text),
                                            null) {
                                        @Override
                                        public void confirmCallBack() {
                                            GlobalConfig.saveLogoutFlag(mContext);
                                            Intent i = new Intent();
                                            i.setAction(PublicIntent.FINISH_APPLICATION);
                                            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                                            mContext.sendBroadcast(i);
                                        }

                                        @Override
                                        public void cannelCallBack() {

                                        }
                                    });
                    DialogManager.getInstance().changeDialogGlobal();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    break;
                case JNI_RECEIVED_SIP_COMING:
                    String sipNumber = (String) msg.obj;
                    if (GlobalHolder.getInstance().isInMeeting()
                            || GlobalHolder.getInstance().isInAudioCall()
                            || GlobalHolder.getInstance().isInVideoCall()) {
                        SipRequest.getInstance().CloseSipCall(sipNumber);
                        // Intent i = new Intent();
                        // i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_REMOVED_SIP_CALL);
                        // i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                        // sendBroadcast(i);
                        return;
                    }

                    GlobalConfig.startP2PConnectChat(mContext,
                            ConversationP2PAVActivity.P2P_CONNECT_SIP, -100, false,
                            null, sipNumber);
                    break;
            }

        }
    }

    private class ImRequestCB extends ImRequestCallbackAdapter {

        @Override
        public void OnLoginCallback(long nUserID, int nStatus, int nResult,
                                    long serverTime, String sDBID) {
            super.OnLoginCallback(nUserID, nStatus, nResult, serverTime, sDBID);
            if (GlobalConfig.isLogined) {
                if (nResult != 0) {
                    Message.obtain(mLocalHandlerThreadHandler,
                            JNI_LOG_USER_DELETE).sendToTarget();
                }
            }
        }

        @Override
        public void OnLogoutCallback(int type) {
            super.OnLogoutCallback(type);
            Message.obtain(mLocalHandlerThreadHandler, JNI_LOG_OUT, type, 0)
                    .sendToTarget();
        }

        @Override
        public void OnConnectResponseCallback(int nResult) {
            super.OnConnectResponseCallback(nResult);
            V2Log.d("CONNECT",
                    "--------------------------------------------------------------------");
            V2Log.d("CONNECT", "Receive Connection State is : " + nResult
                    + " -- name is : "
                    + NetworkStateCode.fromInt(nResult).name());
            GlobalHolder
                    .getInstance()
                    .setServerConnection(
                            NetworkStateCode.fromInt(nResult) == NetworkStateCode.CONNECTED);
            V2Log.d("CONNECT", "GlobleHolder Connection State is : "
                    + GlobalHolder.getInstance().isServerConnected());

            NetworkStateCode code = NetworkStateCode.fromInt(nResult);
            if (code != NetworkStateCode.CONNECTED) {
                for (int i = 0; i < onLineUsers.size(); i++) {
                    UserStatusObject uso = onLineUsers.valueAt(i);
                    uso.setStatus(User.Status.OFFLINE.ordinal());
                }
            }

            Intent i = new Intent();
            i.setAction(JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
            i.addCategory(JNI_BROADCAST_CATEGROY);
            i.putExtra("state", (Parcelable) code);
            sendBroadcast(i);
        }

        @Override
        public void OnUpdateBaseInfoCallback(long nUserID, String updatexml) {
            super.OnUpdateBaseInfoCallback(nUserID, updatexml);
            BoUserInfoBase boUserBaseInfo;
            try {
                boUserBaseInfo = BoUserInfoBase.parserXml(updatexml);
            } catch (Exception e) {
                V2Log.e("ImRequest OnUpdateBaseInfo --> Parsed the xml convert to a V2User Object failed... userID is : "
                        + "" + nUserID + " and xml is : " + updatexml);
                return;
            }

            if (boUserBaseInfo == null) {
                V2Log.e("ImRequest OnUpdateBaseInfo --> Parsed the xml convert to a V2User Object failed... userID is : "
                        + "" + nUserID + " and xml is : " + updatexml);
                return;
            }

            boUserBaseInfo.mId = nUserID;
            Message.obtain(mLocalHandlerThreadHandler, JNI_UPDATE_USER_INFO,
                    boUserBaseInfo).sendToTarget();
        }

        @Override
        public void OnUserStatusUpdatedCallback(long nUserID, int type,
                                                int nStatus, String szStatusDesc) {
            super.OnUserStatusUpdatedCallback(nUserID, type, nStatus,
                    szStatusDesc);
            UserStatusObject uso = new UserStatusObject(nUserID, type, nStatus);
            User u = GlobalHolder.getInstance().getUser(nUserID);
            V2Log.d(TAG, "OnUserStatusUpdatedCallback --> " + " | USER : "
                    + nUserID + " : | NAME: " + u.getDisplayName()
                    + " | STATUS :" + nStatus);
            u.updateStatus(User.Status.fromInt(nStatus));
            u.setDeviceType(User.DeviceType.fromInt(type));
            onLineUsers.put(nUserID, uso);

            Message.obtain(mLocalHandlerThreadHandler, JNI_UPDATE_USER_STATE,
                    uso).sendToTarget();
        }

        @Override
        public void OnChangeAvatarCallback(int nAvatarType, long nUserID,
                                           String AvatarName) {
            super.OnChangeAvatarCallback(nAvatarType, nUserID, AvatarName);
            File f = new File(AvatarName);
            if (f.isDirectory()) {
                // Do not notify if is not file;
                return;
            }
            // System default icon
            if (AvatarName.equals("Default.png")) {
                AvatarName = null;
            }

            GlobalHolder.getInstance().getUser(nUserID)
                    .setAvatarPath(AvatarName);
            GlobalHolder.getInstance().getUser(nUserID)
                    .setmAvatarLocation(AvatarName);
            Intent i = new Intent();
            i.addCategory(JNI_BROADCAST_CATEGROY);
            i.setAction(JNI_BROADCAST_USER_AVATAR_CHANGED_NOTIFICATION);
            i.putExtra("avatar", new UserAvatarObject(nUserID, AvatarName));
            sendBroadcast(i);
        }

        @Override
        public void OnGroupsLoaded() {
            super.OnGroupsLoaded();
            Message.obtain(mLocalHandlerThreadHandler, JNI_GROUP_LOADED)
                    .sendToTarget();
        }

        @Override
        public void OnOfflineStart() {
            super.OnOfflineStart();
            GlobalHolder.getInstance().setOfflineLoaded(false);
        }

        @Override
        public void OnOfflineEnd() {
            super.OnOfflineEnd();
            Message.obtain(mLocalHandlerThreadHandler, JNI_OFFLINE_LOADED, true)
                    .sendToTarget();
        }

        @Override
        public void OnImUpdateUserPwd(int ret) {
            super.OnImUpdateUserPwd(ret);
            Intent i = new Intent();
            i.setAction(JNI_BROADCAST_SETTING_UPDATE_PASSWORD);
            i.addCategory(JNI_BROADCAST_CATEGROY);
            i.putExtra("result", ret);
            sendBroadcast(i);
        }
    }

    private class GroupRequestCB extends GroupRequestCallbackAdapter {

        @Override
        public void OnGetGroupInfo(int groupType, String sXml) {
            super.OnGetGroupInfo(groupType, sXml);
            // 屏蔽群组
            if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                return;
            }

            List<V2Group> list = null;
            if (groupType == V2Group.TYPE_CONF) {
                list = XmlAttributeExtractor.parseConference(sXml);
            } else if (groupType == V2Group.TYPE_CONTACTS_GROUP) {
                List<Group> contacts = GlobalHolder.getInstance().getGroup(
                        V2GlobalConstants.GROUP_TYPE_CONTACT);
                // FIXME ...应该留一个人，防止组织架构闭合
                for (int i = 0; i < contacts.size(); i++) {
                    Group group = contacts.get(i);
                    group.clearUsers();
                }
                list = XmlAttributeExtractor.parseContactsGroup(sXml);
            } else if (groupType == V2Group.TYPE_ORG) {
                list = XmlAttributeExtractor.parseOrgGroup(sXml);
            } else if (groupType == V2Group.TYPE_DISCUSSION_BOARD) {
                list = XmlAttributeExtractor.parseDiscussionGroup(sXml);
            }
            Message.obtain(mLocalHandlerThreadHandler, JNI_GROUP_NOTIFY,
                    groupType, 0, list).sendToTarget();
        }

        @Override
        public void OnGetGroupUserInfoCallback(int groupType, long nGroupID,
                                               String sXml) {
            super.OnGetGroupUserInfoCallback(groupType, nGroupID, sXml);
            Message.obtain(mLocalHandlerThreadHandler,
                    JNI_GROUP_USER_INFO_NOTIFICATION,
                    new GroupUserInfoOrig(groupType, nGroupID, sXml))
                    .sendToTarget();
        }

        @Override
        public void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
            super.OnModifyGroupInfo(groupType, nGroupID, sXml);
            V2Group group = new V2Group(nGroupID, groupType);
            if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                String name = XmlAttributeExtractor.extractAttribute(sXml,
                        "name");
                String announcement = XmlAttributeExtractor.extractAttribute(
                        sXml, "announcement");
                String summary = XmlAttributeExtractor.extractAttribute(sXml,
                        "summary");
                String authtype = XmlAttributeExtractor.extractAttribute(sXml,
                        "authtype");
                group.setName(name);
                group.setAnnounce(announcement);
                group.setBrief(summary);
                if (authtype != null) {
                    group.authType = Integer.parseInt(authtype);
                } else {
                    V2Log.e("No found authtype attrbitue, use 0 as default");
                }

            } else if (groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                // 会议室
                group.xml = sXml;
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION
                    || groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                String name = XmlAttributeExtractor.extractAttribute(sXml,
                        "name");
                if (!TextUtils.isEmpty(name)) {
                    String newName = name.replace("&#x0D;", "")
                            .replace("&#x0A;", "").replace("&#x09;", "");
                    group.setName(newName);
                } else {
                    group.setName(name);
                }
            }

            if (group.type == V2GlobalConstants.GROUP_TYPE_CROWD) {
                CrowdGroup cg = (CrowdGroup) GlobalHolder.getInstance()
                        .getGroupById(group.id);
                cg.setAnnouncement(group.getAnnounce());
                cg.setBrief(group.getBrief());
                cg.setAuthType(CrowdGroup.AuthType.fromInt(group.authType));
                cg.setName(group.getName());
            } else if (group.type == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                DiscussionGroup cg = (DiscussionGroup) GlobalHolder
                        .getInstance().getGroupById(group.id);
                cg.setName(group.getName());
            } else if (group.type == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                /**
                 * @see V2ConferenceRequest#OnModifyGroupInfoCallback
                 */
                return;
            }

            // Send broadcast
            Intent i = new Intent(JNI_BROADCAST_GROUP_UPDATED);
            i.addCategory(JNI_BROADCAST_CATEGROY);
            i.putExtra("gid", group.id);
            mContext.sendBroadcast(i);
        }

        @Override
        public void OnApplyJoinGroup(int groupType, long nGroupID,
                                     String userInfo, String reason) {
            super.OnApplyJoinGroup(groupType, nGroupID, userInfo, reason);
            BoUserInfoShort boUserInfoShort;
            try {
                boUserInfoShort = BoUserInfoShort.parserXml(userInfo);
            } catch (Exception e) {
                V2Log.d("CLASS = GroupRequest METHOD = OnApplyJoinGroup() xml 解析错误");
                e.printStackTrace();
                return;
            }

            if (boUserInfoShort == null) {
                V2Log.d("CLASS = GroupRequest METHOD = OnApplyJoinGroup() xml 解析错误");
                return;
            }
            V2Group group = new V2Group(nGroupID, groupType);
            VMessageQualification qualication = VerificationProvider
                    .queryCrowdQualMessageByCrowdId(boUserInfoShort.mId,
                            group.id);
            if (qualication != null) {
                V2Log.d(TAG, "OnApplyJoinGroup --> qualication : "
                        + qualication.getReadState().name()
                        + " offline state : "
                        + GlobalHolder.getInstance().isOfflineLoaded());
                if (qualication.getReadState() == ReadState.READ
                        && !GlobalHolder.getInstance().isOfflineLoaded()) {
                    return;
                }
            }

            CrowdGroup crowd = (CrowdGroup) GlobalHolder.getInstance()
                    .getGroupById(group.id);
            if (crowd == null) {
                V2Log.d(TAG,
                        "OnApplyJoinGroup --> Parse failed! Because get CrowdGroup is null from GlobleHolder!"
                                + "ID is : " + group.id);
                return;
            }

            User remoteUser = GlobalHolder.getInstance().getUser(
                    boUserInfoShort.mId);
            if (!remoteUser.isFromService()) {
                // remoteUser = boUserToUser(boUserBaseInfo);
                // GlobalHolder.getInstance().putUser(boUserBaseInfo.mId,
                // remoteUser);
                remoteUser = GlobalHolder.getInstance().putOrUpdateUser(
                        boUserInfoShort);
            }

            checkMessageAndSendBroadcast(
                    VMessageQualification.Type.CROWD_APPLICATION, crowd,
                    remoteUser, reason);
        }

        @Override
        public void OnJoinGroupError(int eGroupType, long nGroupID, int nErrorNo) {
            // Send broadcast
            Intent i = new Intent(JNI_BROADCAST_GROUP_JOIN_FAILED);
            i.addCategory(JNI_BROADCAST_CATEGROY);
            i.putExtra("joinCode", new GroupJoinErrorJNIObject(eGroupType,
                    nGroupID, nErrorNo));
            mContext.sendBroadcast(i);
        }

        @Override
        public void OnInviteJoinGroup(int groupType, String groupInfo,
                                      String userInfo, String additInfo) {
            super.OnInviteJoinGroup(groupType, groupInfo, userInfo, additInfo);
            // 屏蔽群组
            if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                return;
            }

            V2Group group = null;
            BoUserInfoBase boUserInfoBase = null;
            if (groupType == V2Group.TYPE_CONF) {
                String id = XmlAttributeExtractor.extract(groupInfo, " id='",
                        "'");
                if (id == null || id.isEmpty()) {
                    V2Log.e(" Unknow group information:" + groupInfo);
                    return;
                }
                group = new V2Group(Long.parseLong(id), groupType);

                String name = XmlAttributeExtractor.extract(groupInfo,
                        " subject='", "'");
                String starttime = XmlAttributeExtractor.extract(groupInfo,
                        " starttime='", "'");
                String createuserid = XmlAttributeExtractor.extract(groupInfo,
                        " createuserid='", "'");

                group.setName(name);
                group.createTime = new Date(Long.parseLong(starttime) * 1000);
                group.chairMan = new BoUserInfoBase(Long.valueOf(createuserid));
                group.owner = new BoUserInfoBase(Long.valueOf(createuserid));
                group.inviateUserID = Long.valueOf(XmlAttributeExtractor
                        .extractAttribute(userInfo, "id"));
            } else if (groupType == V2Group.TYPE_CROWD) {
                group = XmlAttributeExtractor.parseSingleCrowd(groupInfo,
                        userInfo);
            } else if (groupType == V2Group.TYPE_CONTACTS_GROUP) {
                try {
                    boUserInfoBase = BoUserInfoBase.parserXml(userInfo);
                } catch (Exception e) {
                    V2Log.d("CLASS = GroupRequest METHOD = OnInviteJoinGroup() xml解析失败");
                    e.printStackTrace();
                    return;
                }

                if (boUserInfoBase == null) {
                    V2Log.d("CLASS = GroupRequest METHOD = OnInviteJoinGroup() xml解析失败");
                    return;
                }

            } else if (groupType == V2Group.TYPE_DISCUSSION_BOARD) {
                String id = XmlAttributeExtractor.extractAttribute(groupInfo,
                        "id");
                if (id == null || id.isEmpty()) {
                    V2Log.e(" Unknow disucssion information:" + groupInfo);
                    return;
                }
                String name = XmlAttributeExtractor.extractAttribute(groupInfo,
                        "name");
                group = new V2Group(Long.parseLong(id), groupType);
                group.setName(name);
            }

            if (group == null && groupType != V2Group.TYPE_CONTACTS_GROUP) {
                V2Log.e(TAG, "OnInviteJoinGroup --> parse xml failed!");
                return;
            }

            if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                AddFriendHistorieNode node = VerificationProvider
                        .queryFriendQualMessageByUserId(boUserInfoBase.mId);
                if (node != null && node.addState == 0
                        && node.readState == ReadState.READ.intValue()) {
                    V2Log.d(TAG,
                            "OnRequestCreateRelationCallback --> Node readState : "
                                    + node.readState
                                    + " offlineLoad"
                                    + ": "
                                    + GlobalHolder.getInstance()
                                    .isOfflineLoaded());
                    if (node.readState == ReadState.READ.intValue()
                            && !GlobalHolder.getInstance().isOfflineLoaded()) {
                        return;
                    }
                } else {
                    V2Log.d(TAG,
                            "OnRequestCreateRelationCallback --> user id is : "
                                    + boUserInfoBase.mId);
                }

                boolean isOutORG = false;
                User vUser = GlobalHolder.getInstance().getUser(
                        boUserInfoBase.mId);
                if (vUser.isFromService()) {
                    if (TextUtils.isEmpty(vUser.getNickName()))
                        vUser.setNickName(boUserInfoBase.mNickName);
                    AddFriendHistroysHandler.addMeNeedAuthentication(
                            getApplicationContext(), vUser, additInfo);
                } else {
                    isOutORG = true;
                    User newUser = GlobalHolder.getInstance().putOrUpdateUser(
                            boUserInfoBase);
                    AddFriendHistroysHandler.addMeNeedAuthentication(
                            getApplicationContext(), newUser, additInfo);
                }
                Intent intent = new Intent();
                intent.putExtra("isOutORG", isOutORG);
                intent.putExtra("v2User", boUserInfoBase);
                intent.putExtra("uid", boUserInfoBase.mId);
                intent.setAction(JNI_BROADCAST_CONTACTS_AUTHENTICATION);
                intent.addCategory(JNI_BROADCAST_CATEGROY);
                sendBroadcast(intent);
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                User owner = GlobalHolder.getInstance()
                        .getUser(group.owner.mId);
                User chairMan = null;
                if (group.owner.mId != group.chairMan.mId) {
                    chairMan = GlobalHolder.getInstance().getUser(
                            group.chairMan.mId);
                } else {
                    chairMan = owner;
                }

                ConferenceGroup g = new ConferenceGroup(group.id,
                        group.getName(), owner, group.createTime, chairMan);
                Message obtain = Message.obtain(mLocalHandlerThreadHandler,
                        JNI_CONFERENCE_INVITATION, g);
                obtain.arg1 = (int) group.inviateUserID;
                obtain.sendToTarget();
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                // 判断是否已经在群中
                Group checkExist = GlobalHolder.getInstance().getGroupById(
                        group.id);

                if (checkExist != null) {
                    return;
                }

                VMessageQualification qualication = VerificationProvider
                        .queryCrowdQualMessageByCrowdId(group.creator.mId,
                                group.id);
                if (qualication != null) {
                    V2Log.d(TAG, "OnInviteJoinGroupCallback --> qualication : "
                            + qualication.getReadState().name()
                            + " offline state : "
                            + GlobalHolder.getInstance().isOfflineLoaded());
                    if (qualication.getReadState() == ReadState.READ
                            && !GlobalHolder.getInstance().isOfflineLoaded()
                            && qualication.getQualState() == QualificationState.WAITING) {
                        return;
                    }
                } else {
                    V2Log.d(TAG, "OnInviteJoinGroupCallback --> group id : "
                            + group.id + " group name : " + group.getName()
                            + " group user id : " + group.creator.mId);
                }
                User owner = GlobalHolder.getInstance().getUser(
                        group.creator.mId);
                if (owner.isFromService()) {
                    if (!TextUtils.isEmpty(owner.getDisplayName())
                            && TextUtils.isEmpty(group.creator.getNickName())) {
                        V2Log.e(TAG,
                                "OnInviteJoinGroupCallback --> Get Create User Name is empty and older user"
                                        + "name not empty , dirty is mistake");
                    } else
                        owner.setNickName(group.creator.getNickName());
                }

                CrowdGroup cg = new CrowdGroup(group.id, group.getName(), owner);
                cg.setAuthType(CrowdGroup.AuthType.fromInt(group.authType));

                checkMessageAndSendBroadcast(
                        VMessageQualification.Type.CROWD_INVITATION, cg, owner,
                        null);

            } else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                Group cache = GlobalHolder.getInstance().getGroupById(group.id);
                if (cache != null) {
                    V2Log.w("Discussion exists  id " + cache.getGroupID()
                            + "  name: " + cache.getName());
                    return;
                }

                User owner = GlobalHolder.getInstance().getUser(
                        group.creator.mId);
                DiscussionGroup cg = new DiscussionGroup(group.id,
                        group.getName(), owner);
                GlobalHolder.getInstance().addGroupToList(
                        V2GlobalConstants.GROUP_TYPE_DISCUSSION, cg);
                Intent i = new Intent();
                i.setAction(PublicIntent.BROADCAST_NEW_CROWD_NOTIFICATION);
                i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                i.putExtra(
                        "group",
                        new GroupUserObject(
                                V2GlobalConstants.GROUP_TYPE_DISCUSSION, cg
                                .getGroupID(), -1));
                sendBroadcast(i);
            }
        }

        @Override
        public void OnDelGroupCallback(int groupType, long nGroupID,
                                       boolean bMovetoRoot) {
            super.OnDelGroupCallback(groupType, nGroupID, bMovetoRoot);
            if (groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                String gName;
                Group rG = GlobalHolder.getInstance().getGroupById(nGroupID);
                if (rG != null) {
                    gName = rG.getName();
                } else {
                    gName = nGroupID + "";
                }
                boolean flag = GlobalHolder.getInstance().removeGroup(
                        V2GlobalConstants.GROUP_TYPE_CONFERENCE, nGroupID);
                // If flag is true, mean current user dosn't remove this group
                // should notify
                // Otherwise this user removed this group should not notify
                if (flag) {
                    Intent i = new Intent();
                    i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_REMOVED);
                    i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                    i.putExtra("gid", nGroupID);
                    sendBroadcast(i);

                    if (((MainApplication) mContext.getApplicationContext())
                            .isRunningBackgound()) {
                        if (!GlobalHolder.getInstance().isInAudioCall()
                                || !GlobalHolder.getInstance().isInVideoCall()) {
                            Intent intent = new Intent(mContext,
                                    MainActivity.class);
                            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            intent.putExtra("initFragment", 3);
                            Notificator
                                    .updateSystemNotification(
                                            mContext,
                                            mContext.getText(
                                                    R.string.requesting_delete_conference)
                                                    .toString(),
                                            gName
                                                    + mContext
                                                    .getText(R.string.confs_is_deleted_notification),
                                            false,
                                            intent,
                                            V2GlobalConstants.VIDEO_NOTIFICATION_ID,
                                            nGroupID);
                        }
                    }
                }
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                GlobalHolder.getInstance().removeGroup(
                        V2GlobalConstants.GROUP_TYPE_CROWD, nGroupID);
                Intent i = new Intent();
                i.setAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
                i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                i.putExtra("group", new GroupUserObject(
                        V2GlobalConstants.GROUP_TYPE_CROWD, nGroupID, -1));
                sendBroadcast(i, null);
                GlobalHolder.getInstance()
                        .changeGlobleTransSizeToZero(nGroupID);
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                GlobalHolder.getInstance().removeGroup(
                        V2GlobalConstants.GROUP_TYPE_DISCUSSION, nGroupID);
                Intent i = new Intent();
                i.setAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
                i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                i.putExtra("group", new GroupUserObject(
                        V2GlobalConstants.GROUP_TYPE_DISCUSSION, nGroupID, -1));
                sendBroadcast(i);
            }
        }

        @Override
        public void OnDelGroupUserCallback(int groupType, long nGroupID,
                                           long nUserID) {
            super.OnDelGroupUserCallback(groupType, nGroupID, nUserID);
            if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                if (nUserID == GlobalHolder.getInstance().getCurrentUserId()) {
                    GlobalConfig.saveLogoutFlag(mContext);
                    Intent i = new Intent();
                    i.setAction(PublicIntent.FINISH_APPLICATION);
                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    i.putExtra("delay", true);
                    mContext.sendBroadcast(i);
                    File file = new File(GlobalConfig.getGlobalUserPath());
                    if (file.exists()) {
                        GlobalConfig.recursionDeleteOlderFiles(file);
                        GlobalConfig.initConfigFile(false);
                    }
                    return;
                } else {
                    // 删除该用户所有的群组信息，除了讨论组
                    User removed = GlobalHolder.getInstance().getUser(nUserID);
                    // 将全局中控制上传文件的数量变0
                    Set<Group> belongsGroup = removed.getBelongsGroup();
                    Iterator<Group> iterator = belongsGroup.iterator();
                    while (iterator.hasNext()) {
                        Group temp = iterator.next();
                        if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD
                                || temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                            if (temp.getOwnerUser().getmUserId() == nUserID) {
                                // 将全局中控制上传文件的数量变0
                                GlobalHolder.getInstance().removeGroup(
                                        temp.getGroupType(), temp.getGroupID());
                                continue;
                            }
                        } else if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                            if (temp.getOwnerUser().getmUserId() == nUserID) {
                                ((DiscussionGroup) temp).setCreatorExist(false);
                            }

                            Group group = GlobalHolder.getInstance()
                                    .getGroupById(temp.getGroupID());
                            if (group.getOwnerUser().getmUserId() == nUserID) {
                                ((DiscussionGroup) group)
                                        .setCreatorExist(false);
                            }
                        }
                        temp.removeUserFromGroup(nUserID);
                        Group group = GlobalHolder.getInstance().getGroupById(
                                temp.getGroupID());
                        group.removeUserFromGroup(nUserID);
                    }
                    GlobalHolder.getInstance().removeGroupUser(nGroupID,
                            nUserID);
                }
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                // 好友删除：被删除的时候，nGroupID为 0
                Set<Group> groupSet = GlobalHolder.getInstance()
                        .getUser(nUserID).getBelongsGroup();
                Iterator<Group> iterator = groupSet.iterator();
                while (iterator.hasNext()) {
                    Group temp = iterator.next();
                    if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                        nGroupID = temp.getGroupID();
                        V2Log.d(TAG,
                                "OnDelGroupUserCallback --> delete group id is : "
                                        + nGroupID);
                        // 清空备注
                        User removedUser = GlobalHolder.getInstance().getUser(
                                nUserID);
                        removedUser.setCommentName("");
                        if (Notificator.currentRmoteID == removedUser
                                .getmUserId())
                            Notificator.cancelAllSystemNotification(mContext);
                        break;
                    }
                }

                Intent intent = new Intent();
                intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                intent.putExtra("modifiedUser", nUserID);
                intent.putExtra("fromPlace", "delGroupUser");
                sendBroadcast(intent);
                GlobalHolder.getInstance().removeGroupUser(nGroupID, nUserID);
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                GlobalHolder.getInstance().removeGroupUser(nGroupID, nUserID);
                if (GlobalHolder.getInstance().getCurrentUserId() == nUserID) {
                    GlobalHolder.getInstance().removeGroup(
                            V2GlobalConstants.GROUP_TYPE_CROWD, nGroupID);
                    Intent i = new Intent();
                    i.setAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    i.putExtra("group", new GroupUserObject(
                            V2GlobalConstants.GROUP_TYPE_CROWD, nGroupID,
                            nUserID));
                    sendBroadcast(i);
                } else {
                    GroupUserObject obj = new GroupUserObject(groupType,
                            nGroupID, nUserID);
                    Intent i = new Intent();
                    i.setAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
                    i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                    i.putExtra("obj", obj);
                    sendBroadcast(i);
                }
                return;
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                DiscussionGroup dis = (DiscussionGroup) GlobalHolder
                        .getInstance().getGroupById(nGroupID);
                if (dis != null) {
                    if (dis.getOwnerUser().getmUserId() == nUserID) {
                        dis.setCreatorExist(false);
                    }
                }

                GlobalHolder.getInstance().removeGroupUser(nGroupID, nUserID);
                if (GlobalHolder.getInstance().getCurrentUserId() == nUserID) {
                    GlobalHolder.getInstance().removeGroup(
                            V2GlobalConstants.GROUP_TYPE_DISCUSSION, nGroupID);
                    Intent i = new Intent();
                    i.setAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    i.putExtra("group", new GroupUserObject(
                            V2GlobalConstants.GROUP_TYPE_DISCUSSION, nGroupID,
                            -1));
                    sendBroadcast(i);
                } else {
                    GroupUserObject obj = new GroupUserObject(groupType,
                            nGroupID, nUserID);
                    Intent i = new Intent();
                    i.setAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
                    i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                    i.putExtra("obj", obj);
                    sendBroadcast(i);
                }
                return;
            }
            GroupUserObject obj = new GroupUserObject(groupType, nGroupID,
                    nUserID);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("obj", obj);
            sendBroadcast(i);
        }

        @Override
        public void OnAddGroupUserInfoCallback(int groupType, long nGroupID,
                                               String sXml) {
            super.OnAddGroupUserInfoCallback(groupType, nGroupID, sXml);
            BoUserInfoShort boUserInfoShort;
            try {
                boUserInfoShort = BoUserInfoShort.parserXml(sXml);
            } catch (Exception e) {
                e.printStackTrace();
                V2Log.e("OnAddGroupUserInfo -> parse xml failed ...get null user : "
                        + sXml);
                return;
            }
            if (boUserInfoShort == null) {
                V2Log.e("OnAddGroupUserInfo -> parse xml failed ...get null user : "
                        + sXml);
                return;
            }
            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO,
                    boUserInfoShort.mId);

            User newUser = GlobalHolder.getInstance().putOrUpdateUser(
                    boUserInfoShort);
            GlobalHolder.getInstance().addUserToGroup(newUser, nGroupID);

            if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                boolean isHas = false;
                Set<Group> belongs = newUser.getBelongsGroup();
                Iterator<Group> iterator = belongs.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getGroupType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                        isHas = true;
                        break;
                    }
                }

                if (!isHas) {
                    newUser.isOutOrg = true;
                }
                OnAddContactsGroupUserInfoCallback(nGroupID, newUser);
            } else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                DiscussionGroup dis = (DiscussionGroup) GlobalHolder
                        .getInstance().getGroupById(nGroupID);
                if (dis != null) {
                    if (dis.getOwnerUser().getmUserId() == boUserInfoShort.mId) {
                        if (!dis.isCreatorExist()) {
                            dis.setCreatorExist(true);
                        }
                    }
                }
            }

            GroupUserObject object = new GroupUserObject(groupType, nGroupID,
                    newUser.getmUserId());
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("obj", object);
            sendBroadcast(i);
        }

        @Override
        public void OnAcceptApplyJoinGroup(int groupType, String sXml) {
            super.OnAcceptApplyJoinGroup(groupType, sXml);
            V2Group group = XmlAttributeExtractor.parseSingleCrowd(sXml, null);
            if (group == null)
                return;

            if (group.type == V2GlobalConstants.GROUP_TYPE_CROWD) {
                CrowdGroup g = (CrowdGroup) GlobalHolder.getInstance()
                        .getGroupById(group.id);
                if (g == null) {
                    if (group.createTime == null)
                        group.createTime = new Date(
                                GlobalConfig.getGlobalServerTime());
                    User user = GlobalHolder.getInstance().getUser(
                            group.creator.mId);
                    g = new CrowdGroup(group.id, group.getName(), user,
                            group.createTime);
                    g.setBrief(group.getBrief());
                    g.setAnnouncement(group.getAnnounce());
                    GlobalHolder.getInstance().addGroupToList(
                            V2GlobalConstants.GROUP_TYPE_CROWD, g);
                }

                long msgID = VerificationProvider
                        .updateCrowdQualicationMessageState(group,
                                new GroupQualicationState(
                                        Type.CROWD_INVITATION,
                                        QualificationState.BE_ACCEPTED, null,
                                        ReadState.UNREAD, false));
                if (msgID == -1) {
                    V2Log.e(TAG,
                            "OnAcceptApplyJoinGroup : Update Qualication Message to Database failed.. return -1 , group id is : "
                                    + group.id
                                    + " user id"
                                    + ": "
                                    + group.creator.mId);
                    return;
                }
                isAcceptApply = true;

                Intent i = new Intent();
                i.setAction(PublicIntent.BROADCAST_NEW_CROWD_NOTIFICATION);
                i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                i.putExtra("group", new GroupUserObject(
                        V2GlobalConstants.GROUP_TYPE_CROWD, group.id, -1));
                sendBroadcast(i);
                sendQualicationBroad(msgID);
            }
        }

        @Override
        public void OnAcceptInviteJoinGroup(int groupType, long groupId,
                                            long nUserID) {
            super.OnAcceptInviteJoinGroup(groupType, groupId, nUserID);
            if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                GroupQualicationState state = new GroupQualicationState(
                        Type.CROWD_APPLICATION, QualificationState.BE_ACCEPTED,
                        null, ReadState.UNREAD, false);
                state.isUpdateTime = false;
                long msgID = VerificationProvider
                        .updateCrowdQualicationMessageState(groupId, nUserID,
                                state);
                sendQualicationBroad(msgID);
            }
        }

        @Override
        public void OnRefuseApplyJoinGroup(int groupType, String sXml,
                                           String reason) {
            super.OnRefuseApplyJoinGroup(groupType, sXml, reason);
            V2Group parseSingleCrowd = XmlAttributeExtractor.parseSingleCrowd(
                    sXml, null);
            if (parseSingleCrowd == null)
                return;

            long msgID = VerificationProvider
                    .updateCrowdQualicationMessageState(parseSingleCrowd,
                            new GroupQualicationState(Type.CROWD_INVITATION,
                                    QualificationState.BE_REJECT, reason,
                                    ReadState.UNREAD, false));
            if (msgID == -1) {
                V2Log.e(TAG,
                        "OnRefuseApplyJoinGroup : Update Qualication Message to Database failed.. return -1 , group id is : "
                                + parseSingleCrowd.id
                                + " user id"
                                + ": "
                                + parseSingleCrowd.creator);
                return;
            }
            sendQualicationBroad(msgID);
        }

        @Override
        public void OnRefuseInviteJoinGroup(int groupType, long nGroupID,
                                            long nUserID, String reason) {
            super.OnRefuseInviteJoinGroup(groupType, nGroupID, nUserID, reason);
            GroupQualicationJNIObject obj = new GroupQualicationJNIObject(
                    groupType, nGroupID, nUserID, 1, 3, reason);
            if (obj.groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                AddFriendHistroysHandler.addOtherRefused(
                        getApplicationContext(), obj.userID, obj.reason);
                Intent intent = new Intent();
                intent.putExtra("uid", obj.userID);
                intent.putExtra("authType", "refuseInvite");
                intent.setAction(JNI_BROADCAST_CONTACTS_AUTHENTICATION);
                intent.addCategory(JNI_BROADCAST_CATEGROY);
                sendOrderedBroadcast(intent, null);
            } else if (obj.groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                Group isExist = GlobalHolder.getInstance().getGroupById(
                        obj.groupID);
                if (isExist == null) {
                    V2Log.e(TAG,
                            "The Crowd Group already no exist! group id is : "
                                    + obj.groupID);
                    return;
                }

                long msgID = VerificationProvider
                        .updateCrowdQualicationMessageState(
                                obj.groupID,
                                obj.userID,
                                new GroupQualicationState(
                                        com.bizcom.vo.meesage.VMessageQualification.Type
                                                .fromInt(obj.qualicationType),
                                        com.bizcom.vo.meesage.VMessageQualification.QualificationState
                                                .fromInt(obj.state),
                                        obj.reason, ReadState.UNREAD, false));
                if (msgID == -1) {
                    V2Log.e(TAG,
                            "OnRefuseInviteJoinGroup --> update refuse Invite join group failed... !");
                    return;
                }
                sendQualicationBroad(msgID);
            }
        }

        @Override
        public void onAddGroupInfo(int groupType, long nParentID,
                                   long nGroupID, String sXml) {
            super.onAddGroupInfo(groupType, nParentID, nGroupID, sXml);
            // 屏蔽群组
            if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                return;
            }

            String gid = XmlAttributeExtractor.extract(sXml, " id='", "'");
            String name = XmlAttributeExtractor.extract(sXml, " name='", "'");
            String createUesrID = XmlAttributeExtractor.extract(sXml,
                    " creatoruserid='", "'");
            // 群组的解析字段
            // String announcement = XmlAttributeExtractor.extract(sXml,
            // " announcement='", "'");
            // String brief = XmlAttributeExtractor.extract(sXml, " summary='",
            // "'");
            // String authType = XmlAttributeExtractor.extract(sXml,
            // " authtype='", "'");
            // String groupSize = XmlAttributeExtractor.extract(sXml, " size='",
            // "'");
            V2Group crowd = new V2Group(Long.parseLong(gid), name, groupType);
            if (gid != null && !gid.isEmpty() && createUesrID != null) {
                crowd.owner = new BoUserInfoBase(Long.valueOf(createUesrID));
                crowd.creator = crowd.owner;
                // if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                // crowd.setAnnounce(announcement);
                // crowd.setBrief(brief);
                // crowd.authType = Integer.valueOf(authType);
                // crowd.groupSize = Integer.valueOf(groupSize);
                // }
            } else {
                V2Log.e("OnAddGroupInfo:: parse xml failed , don't get group id or user id ...."
                        + sXml);
                return;
            }

            if (isAcceptApply) {
                isAcceptApply = false;
                return;
            }

            if (crowd.createTime == null)
                crowd.createTime = new Date(GlobalConfig.getGlobalServerTime());

            if (crowd.type == V2Group.TYPE_CROWD
                    && GlobalHolder.getInstance().getCurrentUserId() != crowd.owner.mId) {
                V2Log.e(TAG, "onAddGroupInfo--> sendFriendToTv a new group , id is : "
                        + crowd.id);
                User user = GlobalHolder.getInstance().getUser(
                        crowd.creator.mId);
                if (user == null) {
                    V2Log.e(TAG,
                            "onAddGroupInfo--> sendFriendToTv a new group failed , get user is null , id is : "
                                    + crowd.creator.mId);
                    return;
                }
                CrowdGroup g = new CrowdGroup(crowd.id, crowd.getName(), user,
                        crowd.createTime);
                g.setBrief(crowd.getBrief());
                g.setAnnouncement(crowd.getAnnounce());
                g.setAuthType(CrowdGroup.AuthType.fromInt(crowd.authType));
                GlobalHolder.getInstance().addGroupToList(
                        V2GlobalConstants.GROUP_TYPE_CROWD, g);

                GroupQualicationState state = new GroupQualicationState(
                        Type.CROWD_INVITATION, QualificationState.ACCEPTED,
                        null, ReadState.UNREAD, false);
                state.isUpdateTime = false;
                VerificationProvider.updateCrowdQualicationMessageState(crowd,
                        state);

                Intent i = new Intent();
                i.setAction(PublicIntent.BROADCAST_NEW_CROWD_NOTIFICATION);
                i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                i.putExtra("group", new GroupUserObject(
                        V2GlobalConstants.GROUP_TYPE_CROWD, crowd.id, -1));
                sendBroadcast(i);
            } else if (crowd.type == V2Group.TYPE_DISCUSSION_BOARD) {
                if (GlobalHolder.getInstance().getCurrentUserId() != crowd.creator.mId) {
                    Group existGroup = GlobalHolder.getInstance().getGroupById(
                            crowd.id);
                    if (existGroup != null)
                        return;

                    V2Log.e(TAG,
                            "onAddGroupInfo--> sendFriendToTv a new discussion group , id is : "
                                    + crowd.id);
                    User user = GlobalHolder.getInstance().getUser(
                            crowd.creator.mId);
                    if (user == null) {
                        V2Log.e(TAG,
                                "onAddGroupInfo--> sendFriendToTv a new group failed , get user is null , id is : "
                                        + crowd.creator.mId);
                        return;
                    }
                    DiscussionGroup g = new DiscussionGroup(crowd.id,
                            crowd.getName(), user, crowd.createTime);
                    GlobalHolder.getInstance().addGroupToList(
                            V2GlobalConstants.GROUP_TYPE_DISCUSSION, g);
                }

                Intent i = new Intent();
                i.setAction(PublicIntent.BROADCAST_NEW_CROWD_NOTIFICATION);
                i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                i.putExtra("group", new GroupUserObject(
                        V2GlobalConstants.GROUP_TYPE_DISCUSSION, crowd.id, -1));
                sendBroadcast(i);
            }
        }

        @Override
        public void OnAddGroupFile(int eGroupType, long nGroupId, String sXml) {
            super.OnAddGroupFile(eGroupType, nGroupId, sXml);
            List<FileJNIObject> list = XmlAttributeExtractor.parseFiles(sXml);
            V2Group group = new V2Group(nGroupId, eGroupType);
            if (list == null || list.size() <= 0) {
                V2Log.e("OnAddGroupFile : May receive new group files failed.. get empty collection");
                return;
            }

            if (group.type == V2GlobalConstants.GROUP_TYPE_CROWD) {
                CrowdGroup cg = (CrowdGroup) GlobalHolder.getInstance()
                        .getGroupById(group.id);
                if (cg != null) {
                    for (int i = 0; i < list.size(); i++) {
                        FileJNIObject newFile = list.get(i);
                        if (newFile.user.mId != GlobalHolder.getInstance().mCurrentUserId)
                            cg.addNewFileNum();
                    }
                }
            } else if (group.type == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                return;
            }

            // for (FileJNIObject fileJNIObject : list) {
            FileJNIObject fileJNIObject = list.get(0);
            long uploadUserID = fileJNIObject.user.mId;
            if (GlobalHolder.getInstance().getCurrentUserId() != uploadUserID) {
                User user = GlobalHolder.getInstance().getUser(uploadUserID);
                VMessage vm = new VMessage(V2GlobalConstants.GROUP_TYPE_CROWD,
                        group.id, user, null, new Date(
                        GlobalConfig.getGlobalServerTime()));
                VMessageFileItem item = new VMessageFileItem(vm,
                        fileJNIObject.fileName,
                        VMessageFileItem.STATE_FILE_SENT, fileJNIObject.fileId);
                item.setFileSize(fileJNIObject.fileSize);
                item.setFileType(FileType.fromInt(fileJNIObject.fileType));
                // save to database
                vm.setmXmlDatas(vm.toXml());
                ChatMessageProvider.saveChatMessage(vm);
                ChatMessageProvider.saveFileVMessage(vm);
                fileJNIObject.vMessageID = vm.getUUID();
            }
            // }

            Intent intent = new Intent();
            intent.setAction(BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION);
            intent.addCategory(JNI_BROADCAST_CATEGROY);
            intent.putExtra("groupID", group.id);
            intent.putExtra("uploader", uploadUserID);
            intent.putParcelableArrayListExtra("fileJniObjects",
                    new ArrayList<>(list));
            sendBroadcast(intent);
        }

        @Override
        public void OnKickGroupUser(int groupType, long groupId, long nUserId) {
            super.OnKickGroupUser(groupType, groupId, nUserId);
            GlobalHolder.getInstance().removeGroup(groupType, groupId);
            Intent kick = new Intent();
            kick.setAction(JNI_BROADCAST_KICED_CROWD);
            kick.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            kick.putExtra("group", new GroupUserObject(groupType, groupId,
                    nUserId));
            sendBroadcast(kick);
        }

        /**
         * @param type
         * @param g
         * @param user
         * @param reason
         * @return
         */
        private VMessageQualification checkMessageAndSendBroadcast(
                VMessageQualification.Type type, CrowdGroup g, User user,
                String reason) {
            VMessageQualification crowdMsg;
            if (type == Type.CROWD_APPLICATION) {
                crowdMsg = VerificationProvider
                        .queryCrowdApplyQualMessageByUserId(g.getGroupID(),
                                user.getmUserId());
            } else {
                crowdMsg = VerificationProvider.queryCrowdQualMessageByCrowdId(
                        user, g);
            }

            if (crowdMsg != null) {
                if (crowdMsg.getQualState() != VMessageQualification.QualificationState.WAITING) {
                    crowdMsg.setQualState(VMessageQualification.QualificationState.WAITING);
                }

                CrowdGroup olderGroup = crowdMsg.getmCrowdGroup();
                crowdMsg.setReadState(VMessageQualification.ReadState.UNREAD);
                crowdMsg.setmCrowdGroup(g);
                if (type == VMessageQualification.Type.CROWD_APPLICATION) {
                    ((VMessageQualificationApplicationCrowd) crowdMsg)
                            .setApplyReason(reason);
                } else if (type == VMessageQualification.Type.CROWD_INVITATION) {
                    crowdMsg.setRejectReason(reason);
                } else {
                    throw new RuntimeException(
                            "checkMessageAndSendBroadcast --> Unkown type");
                }

                if (olderGroup.getGroupID() == g.getGroupID())
                    VerificationProvider
                            .updateCrowdQualicationMessage(crowdMsg);
                else
                    VerificationProvider.updateCrowdQualicationMessage(
                            olderGroup, crowdMsg);
            } else {
                // Save message to database
                if (type == VMessageQualification.Type.CROWD_APPLICATION) {
                    crowdMsg = new VMessageQualificationApplicationCrowd(g,
                            user);
                    ((VMessageQualificationApplicationCrowd) crowdMsg)
                            .setApplyReason(reason);
                } else if (type == VMessageQualification.Type.CROWD_INVITATION) {
                    crowdMsg = new VMessageQualificationInvitationCrowd(g,
                            GlobalHolder.getInstance().getCurrentUser());
                } else {
                    throw new RuntimeException("Unkown type");
                }

                crowdMsg.setmTimestamp(new Date(GlobalConfig
                        .getGlobalServerTime()));
                crowdMsg.setReadState(VMessageQualification.ReadState.UNREAD);
                Uri uri = VerificationProvider.saveQualicationMessage(crowdMsg);
                if (uri != null) {
                    crowdMsg.setId(Long.parseLong(uri.getLastPathSegment()));
                }
            }

            if (crowdMsg != null && crowdMsg.getId() > 0) {
                // Send broadcast
                sendQualicationBroad(crowdMsg.getId());
            }
            return crowdMsg;
        }

        private void sendQualicationBroad(long msgID) {
            Intent i = new Intent(JNI_BROADCAST_NEW_QUALIFICATION_MESSAGE);
            i.addCategory(JNI_BROADCAST_CATEGROY);
            i.putExtra("msgId", msgID);
            mContext.sendBroadcast(i);

        }

        private void OnAddContactsGroupUserInfoCallback(long nGroupID,
                                                        User newUser) {
            AddFriendHistroysHandler.becomeFriendHanler(
                    getApplicationContext(), newUser);
            if (newUser.getCommentName() != null) {
                Intent intent = new Intent();
                intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                intent.putExtra("modifiedUser", newUser.getmUserId());
                sendBroadcast(intent);
            }
            Intent intent = new Intent();
            intent.setAction(JNI_BROADCAST_CONTACTS_AUTHENTICATION);
            intent.addCategory(JNI_BROADCAST_CATEGROY);
            intent.putExtra("uid", newUser.getmUserId());
            intent.putExtra("gid", nGroupID);
            sendBroadcast(intent);
        }
    }

    private class AudioRequestCB extends AudioRequestCallbackAdapter {

        @Override
        public void OnAudioChatInvite(final String szSessionID,
                                      final long nFromUserID) {
            super.OnAudioChatInvite(szSessionID, nFromUserID);
            if (GlobalHolder.getInstance().isInVideoCall()) {
                // 需要延迟2秒，防止远端刚挂断视频通话，就接着来音频通话
                GlobalState state = GlobalHolder.getInstance().getGlobalState();
                // if in video automatically accept audio and user never accept
                // audio call.
                // because audio and video use different message
                if (state.getUid() == nFromUserID && !state.isVoiceConnected()
                        && szSessionID.contains("ByVideo")) {
                    V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_ACCEPT,
                            szSessionID, nFromUserID);
                    // mark voice state to connected
                    GlobalHolder.getInstance().setVoiceConnectedState(true);
                } else {
                    V2Log.i("Ignore audio call for others: " + szSessionID);
                    updateAudioRecord(szSessionID, nFromUserID);
                    V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_REFUSE,
                            szSessionID, nFromUserID);
                }
                return;
            }

            if (GlobalHolder.getInstance().isInMeeting()
                    || GlobalHolder.getInstance().isInAudioCall()
                    || GlobalHolder.getInstance().isInVideoCall()) {
                V2Log.i("OnAudioChatInvite --> The audio chat invite coming ! Ignore audio call ");
                updateAudioRecord(szSessionID, nFromUserID);
                V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_REFUSE,
                        szSessionID, nFromUserID);
                return;
            }

            GlobalConfig.startP2PConnectChat(mContext,
                    ConversationP2PAVActivity.P2P_CONNECT_AUDIO, nFromUserID,
                    true, szSessionID, null);
        }

        @Override
        public void OnAudioChatClosed(String szSessionID, long nFromUserID) {
            super.OnAudioChatClosed(szSessionID, nFromUserID);
            Intent i = new Intent(JNI_BROADCAST_VIDEO_CALL_CLOSED);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("fromUserId", nFromUserID);
            i.putExtra("groupId", szSessionID);
            if (GlobalHolder.getInstance().isP2pAVNeedStickyBraodcast()) {
                // Send sticky broadcast, make sure activity receive
                mContext.sendStickyBroadcast(i);
            } else {
                mContext.sendBroadcast(i);
            }
        }

        private void updateAudioRecord(String szSessionID, long nFromUserID) {
            if ((System.currentTimeMillis() / 1000) - lastNotificatorTime > 2) {
                Uri notification = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager
                        .getRingtone(mContext, notification);
                if (r != null) {
                    r.play();
                }
                lastNotificatorTime = System.currentTimeMillis() / 1000;
            }
            // record in database
            VideoBean currentVideoBean = new VideoBean();
            currentVideoBean.readSatate = V2GlobalConstants.READ_STATE_UNREAD;
            currentVideoBean.formUserID = nFromUserID;
            currentVideoBean.remoteUserID = nFromUserID;
            currentVideoBean.toUserID = GlobalHolder.getInstance()
                    .getCurrentUserId();
            currentVideoBean.mediaChatID = szSessionID;
            currentVideoBean.mediaType = AudioVideoMessageBean.TYPE_AUDIO;
            currentVideoBean.startDate = GlobalConfig.getGlobalServerTime();
            currentVideoBean.mediaState = AudioVideoMessageBean.STATE_NO_ANSWER_CALL;
            currentVideoBean.endDate = System.currentTimeMillis();
            MediaRecordProvider.saveMediaChatHistories(currentVideoBean);

            Intent intent = new Intent();
            intent.setAction(ConversationP2PAVActivity.P2P_BROADCAST_MEDIA_UPDATE);
            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intent.putExtra("remoteID", currentVideoBean.remoteUserID);
            sendBroadcast(intent);
        }
    }

    private class VideoRequestCB extends VideoRequestCallbackAdapter {

        @Override
        public void OnRemoteUserVideoDevice(long uid, String szXmlData) {
            List<UserDeviceConfig> ll = UserDeviceConfig.parseFromXml(uid, szXmlData);
            GlobalHolder.getInstance().updateUserDevice(uid, ll);
        }

        @Override
        public void OnVideoChatInviteCallback(String szSessionID,
                                              long nFromUserID, String szDeviceID, String data) {
            super.OnVideoChatInviteCallback(szSessionID, nFromUserID,
                    szDeviceID, data);
            VideoJNIObjectInd ind = new VideoJNIObjectInd(szSessionID,
                    nFromUserID, szDeviceID, 0);
            if (GlobalHolder.getInstance().isInMeeting()
                    || GlobalHolder.getInstance().isInAudioCall()
                    || GlobalHolder.getInstance().isInVideoCall()) {
                V2Log.i(TAG,
                        "OnVideoChatInvite --> The video chat invite coming ! Ignore video call ");
                updateVideoRecord(ind);
                VideoRequest.getInstance().VideoRefuseChat(
                        ind.getSzSessionID(), ind.getFromUserId(),
                        ind.getDeviceId(), data);
                return;
            }
            ind.setData(data);

            Message.obtain(mLocalHandlerThreadHandler,
                    JNI_RECEIVED_VIDEO_INVITION, ind).sendToTarget();
        }

        @Override
        public void OnVideoChatClosed(String szSessionID, long nFromUserID,
                                      String szDeviceID) {
            super.OnVideoChatClosed(szSessionID, nFromUserID, szDeviceID);
            Intent i = new Intent(JNI_BROADCAST_VIDEO_CALL_CLOSED);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("fromUserId", nFromUserID);
            i.putExtra("groupId", szSessionID);
            if (GlobalHolder.getInstance().isP2pAVNeedStickyBraodcast()) {
                // Send sticky broadcast, make sure activity receive
                mContext.sendStickyBroadcast(i);
            } else {
                mContext.sendBroadcast(i);
            }
        }

        private void updateVideoRecord(VideoJNIObjectInd ind) {
            if ((System.currentTimeMillis() / 1000) - lastNotificatorTime > 2) {
                Uri notification = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager
                        .getRingtone(mContext, notification);
                if (r != null) {
                    r.play();
                }
                lastNotificatorTime = System.currentTimeMillis() / 1000;
            }
            // record in database
            VideoBean currentVideoBean = new VideoBean();
            currentVideoBean.readSatate = V2GlobalConstants.READ_STATE_UNREAD;
            currentVideoBean.formUserID = ind.getFromUserId();
            currentVideoBean.remoteUserID = ind.getFromUserId();
            currentVideoBean.toUserID = GlobalHolder.getInstance()
                    .getCurrentUserId();
            currentVideoBean.mediaChatID = ind.getSzSessionID();
            currentVideoBean.mediaType = AudioVideoMessageBean.TYPE_VIDEO;
            currentVideoBean.startDate = GlobalConfig.getGlobalServerTime();
            currentVideoBean.mediaState = AudioVideoMessageBean.STATE_NO_ANSWER_CALL;
            currentVideoBean.endDate = System.currentTimeMillis();
            MediaRecordProvider.saveMediaChatHistories(currentVideoBean);

            Intent intent = new Intent();
            intent.setAction(ConversationP2PAVActivity.P2P_BROADCAST_MEDIA_UPDATE);
            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intent.putExtra("remoteID", currentVideoBean.remoteUserID);
            sendBroadcast(intent);
        }
    }

    private class ConfRequestCB extends ConfRequestCallbackAdapter {

        @Override
        public void OnConfNotify(String confXml, String creatorXml) {
            super.OnConfNotify(confXml, creatorXml);
            if (confXml == null || confXml.isEmpty()) {
                V2Log.e(TAG, "OnConfNotify --> confXml is null ");
                return;
            }

            V2Conference conf = new V2Conference();
            String confId = XmlAttributeExtractor
                    .extract(confXml, " id='", "'");
            if (confId == null || confId.isEmpty()) {
                V2Log.e(TAG, "OnConfNotify --> confId is null  can not pasrse");
                return;
            }

            String startTime = XmlAttributeExtractor.extract(confXml,
                    " starttime='", "'");
            String subject = XmlAttributeExtractor.extract(confXml,
                    " subject='", "'");

            conf.cid = Long.parseLong(confId);
            conf.name = subject;
            if (!TextUtils.isEmpty(startTime))
                conf.startTime = new Date(Long.parseLong(startTime) * 1000);
            else {
                V2Log.e(TAG, "OnConfNotify --> get startTime is null...");
                conf.startTime = new Date(GlobalConfig.getGlobalServerTime());
            }

            BoUserInfoBase user = new BoUserInfoBase();
            String uid = XmlAttributeExtractor
                    .extract(creatorXml, " id='", "'");
            if (uid == null || uid.isEmpty()) {
                V2Log.e(TAG, "OnConfNotify --> uid is null  can not pasrse");
                return;
            }
            user.mId = Long.parseLong(uid);
            conf.creator = user;

            User owner = GlobalHolder.getInstance().getUser(user.mId);
            Group g = new ConferenceGroup(conf.cid, conf.name, owner,
                    conf.startTime, owner);
            GlobalHolder.getInstance().addGroupToList(
                    V2GlobalConstants.GROUP_TYPE_CONFERENCE, g);

            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_INVATITION);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("gid", g.getGroupID());
            i.putExtra("invite", Long.valueOf(uid));
            sendBroadcast(i);
        }

        @Override
        public void OnConfSyncOpenVideo(String xml) {
            super.OnConfSyncOpenVideo(xml);
            ArrayList<JNIObjectInd> objs = XmlParser.parseJNICallBackNormalXml(
                    xml, V2ConfSyncVideoJNIObject.class);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("syncOpen", objs);
            i.putExtra("syncOpen", objs);
            sendBroadcast(i);
        }

        @Override
        public void OnConfSyncCloseVideoToMobile(long nDstUserID,
                                                 String sDstMediaID) {
            super.OnConfSyncCloseVideoToMobile(nDstUserID, sDstMediaID);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO_TO_MOBILE);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("nDstUserID", nDstUserID);
            i.putExtra("sDstMediaID", sDstMediaID);
            sendBroadcast(i);
        }

        @Override
        public void OnConfSyncOpenVideoToMobile(String sSyncVideoMsgXML) {
            super.OnConfSyncOpenVideoToMobile(sSyncVideoMsgXML);
            ArrayList<JNIObjectInd> objs = XmlParser.parseJNICallBackNormalXml(
                    sSyncVideoMsgXML, V2ConfSyncVideoJNIObject.class);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO_TO_MOBILE);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("syncOpen", objs);
            i.putExtra("syncOpen", objs);
            sendBroadcast(i);
        }

        @Override
        public void OnConfSyncCloseVideo(long nDstUserID, String dstDeviceID) {
            super.OnConfSyncCloseVideo(nDstUserID, dstDeviceID);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("nDstUserID", nDstUserID);
            i.putExtra("dstDeviceID", dstDeviceID);
            sendBroadcast(i);
        }

        @Override
        public void OnChangeSyncConfOpenVideoPos(long nDstUserID,
                                                 String szDeviceID, String sPos) {
            super.OnChangeSyncConfOpenVideoPos(nDstUserID, szDeviceID, sPos);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_VOD_OPEN_VIDEO);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("deviceID", szDeviceID);
            sendBroadcast(i);
        }
    }

    private class ChatRequestCB extends ChatRequestCallbackAdapter {

        @Override
        public void OnRecvChatTextCallback(int eGroupType, long nGroupID,
                                           long nFromUserID, long nToUserID, long nTime, String szSeqID,
                                           String szXmlText) {
            super.OnRecvChatTextCallback(eGroupType, nGroupID, nFromUserID,
                    nToUserID, nTime, szSeqID, szXmlText);
            int messageItemType = XmlParser.getMessageItemType(szXmlText);
            if (messageItemType == VMessageAbstractItem.ITEM_TYPE_ALL) {
                V2Log.e(TAG, "Recv a new link message , ignore! szXmlText : "
                        + szXmlText);
                return;
            }

            V2Log.d(TAG, "one step , Recv a new message , type : "
                    + messageItemType + " | szSeqID : " + szSeqID);
            User toUser = GlobalHolder.getInstance().getUser(nToUserID);
            User fromUser = GlobalHolder.getInstance().getUser(nFromUserID);
            VMessage msg = new VMessage(eGroupType, nGroupID, fromUser, toUser,
                    szSeqID, new Date(nTime * 1000));
            msg.setmXmlDatas(szXmlText);
            msg.setState(VMessageAbstractItem.TRANS_WAIT_RECEIVE);
            if (messageItemType == VMessageAbstractItem.ITEM_TYPE_IMAGE) {
                XmlParser.extraImageMetaFrom(msg, szXmlText);
                // messageQueue.sendFriendToTv(vm);
                ChatMessageProvider.saveChatMessage(msg);
                List<VMessageImageItem> imageItems = msg.getImageItems();
                for (int i = 0; i < imageItems.size(); i++) {
                    VMessageImageItem vMessageImageItem = imageItems.get(i);
                    String key = vMessageImageItem.getUuid();
                    ChatRequest.getInstance().ChatMonitorRecvBinary(
                            msg.getMsgCode(), key);
                    mBinaryCache.put(key, msg);
                    V2Log.d(TAG, " 缓存二进制图片数据占位符 : " + key);
                }
                ChatMessageProvider.saveBinaryVMessage(msg);
                Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE, msg).sendToTarget();
            } else if (messageItemType == VMessageAbstractItem.ITEM_TYPE_AUDIO) {
                // Record audio data meta
                XmlParser.extraAudioMetaFrom(msg, szXmlText);
                List<VMessageAudioItem> audioItems = msg.getAudioItems();
                // messageQueue.sendFriendToTv(vm);
                // FIXME ..
                ChatMessageProvider.saveChatMessage(msg);
                ChatMessageProvider.saveBinaryVMessage(msg);
                for (int i = 0; i < audioItems.size(); i++) {
                    VMessageAudioItem vMessageAudioItem = audioItems.get(i);
                    String key = vMessageAudioItem.getUuid();
                    ChatRequest.getInstance().ChatMonitorRecvBinary(
                            msg.getMsgCode(), key);
                    mBinaryCache.put(key, msg);
                    V2Log.d(TAG, " 缓存二进制音频数据占位符 : " + key + " VMessage id : "
                            + msg.getUUID());
                }
                Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE, msg).sendToTarget();
            } else {
                msg.setState(VMessageAbstractItem.TRANS_WAIT_RECEIVE);
                // messageQueue.sendFriendToTv(vm);
                V2Log.d(TAG, "two step , save a new message , szSeqID : "
                        + szSeqID);
                ChatMessageProvider.saveChatMessage(msg);
                Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE, msg).sendToTarget();
            }
        }

        @Override
        public void OnRecvChatBinaryCallback(int eGroupType, long nGroupID,
                                             long nFromUserID, long nToUserID, long nTime, int binaryType,
                                             String messageId, String binaryPath) {
            super.OnRecvChatBinaryCallback(eGroupType, nGroupID, nFromUserID,
                    nToUserID, nTime, binaryType, messageId, binaryPath);
            switch (binaryType) {
                case BINARY_TYPE_IMAGE:
                    handlerChatPictureCallback(eGroupType, nGroupID, nFromUserID,
                            nToUserID, nTime, messageId, binaryPath);
                    break;
                case BINARY_TYPE_AUDIO:
                    handlerChatAudioCallback(eGroupType, nGroupID, nFromUserID,
                            nToUserID, nTime, messageId, binaryPath);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void OnSendChatResult(int eGroupType, long nGroupID,
                                     long nFromUserID, long nToUserID, int mediaType, String sSeqID,
                                     int nResult) {
            super.OnSendChatResult(eGroupType, nGroupID, nFromUserID,
                    nToUserID, mediaType, sSeqID, nResult);
            Result result = Result.fromInt(nResult);
            String msgUID = sSeqID;
            boolean isBinaryFirst = false;
            VMessage vm = null;
            if (mediaType == GlobalConfig.MESSAGE_RECV_BINARY_TYPE_TEXT) {
                boolean tableExist = ChatMessageProvider.isTableExist(
                        eGroupType, nGroupID, nToUserID);
                if (!tableExist) {
                    V2Log.d(TAG, "表创建失败...");
                    return;
                }
                List<VMessage> messages = ChatMessageProvider
                        .queryMessage(
                                ContentDescriptor.HistoriesMessage.Cols.HISTORY_MESSAGE_ID
                                        + "= ? ", new String[]{sSeqID}, null);
                if (messages != null && messages.size() > 0) {
                    vm = messages.get(0);
                    if (vm.getImageItems().size() > 0) {
                        String key = vm.getImageItems().get(0).getUuid();
                        String cacheKey = null;
                        int cacheValue = -1;
                        Set<Entry<String, Integer>> entries = mSendBinaryCacheKey
                                .entrySet();
                        while (entries.iterator().hasNext()) {
                            Entry<String, Integer> next = entries.iterator()
                                    .next();
                            String temp = next.getKey();
                            cacheValue = next.getValue();
                            if (temp.equals(key)) {
                                cacheKey = temp;
                                break;
                            }
                        }

                        if (cacheKey != null) {
                            mSendBinaryCacheKey.remove(cacheKey);
                            vm.mRecvState = cacheValue;
                            isBinaryFirst = true;
                        } else {
                            vm.mRecvState = nResult;
                            mSendBinaryCache.put(key, vm);
                            return;
                        }
                    } else if (vm.getAudioItems().size() > 0) {
                        String key = vm.getAudioItems().get(0).getUuid();
                        String cacheKey = null;
                        int cacheValue = -1;
                        Set<Entry<String, Integer>> entries = mSendBinaryCacheKey
                                .entrySet();
                        while (entries.iterator().hasNext()) {
                            Entry<String, Integer> next = entries.iterator()
                                    .next();
                            String temp = next.getKey();
                            cacheValue = next.getValue();
                            if (temp.equals(key)) {
                                cacheKey = temp;
                                V2Log.d("test", "bbbbbbbbbbbbbbbbbb cacheKey "
                                        + cacheKey);
                                break;
                            }
                        }

                        if (cacheKey != null) {
                            mSendBinaryCacheKey.remove(cacheKey);
                            vm.mRecvState = cacheValue;
                            isBinaryFirst = true;
                            V2Log.d("test",
                                    "cccccccccccccccccccccccc vm.mRecvState "
                                            + vm.mRecvState);
                        } else {
                            vm.mRecvState = nResult;
                            mSendBinaryCache.put(key, vm);
                            return;
                        }
                    }
                }
            } else {
                vm = mSendBinaryCache.remove(sSeqID);
                if (vm == null) {
                    V2Log.d("test", "aaaaaaaaaaaaaaaaaaaaaaaaa nResult "
                            + nResult);
                    mSendBinaryCacheKey.put(sSeqID, nResult);
                }
            }

            if (vm != null) {
                int state;
                int fileState;
                if (result != Result.SUCCESS) {
                    state = VMessageAbstractItem.TRANS_SENT_FALIED;
                    fileState = VMessageAbstractItem.STATE_FILE_SENT_FALIED;
                } else {
                    state = VMessageAbstractItem.TRANS_SENT_SUCCESS;
                    fileState = VMessageAbstractItem.STATE_FILE_SENT;
                }
                vm.setState(state);
                if (mediaType == GlobalConfig.MESSAGE_RECV_BINARY_TYPE_TEXT
                        && !isBinaryFirst) {
                    // 如果含有audio或image，则只要看onSendBinaryResult
                    ChatMessageProvider.updateChatMessageState(vm);
                    Intent i = new Intent();
                    i.setAction(JNIService.JNI_BROADCAST_MESSAGE_SENT_RESULT);
                    i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                    i.putExtra("MsgUID", msgUID);
                    i.putExtra("result", result);
                    i.putExtra("binaryType", mediaType);
                    sendBroadcast(i);
                } else {
                    V2Log.d("test", "dddddddddddddddddddddddd");
                    if (mediaType == JNIIndType.CHAT_BINARY.ordinal()) {
                        V2Log.d(TAG, " 接收到了二进制数据 : " + sSeqID + " 的最终结果 "
                                + " | error code : " + nResult);
                    }

                    msgUID = vm.getUUID();
                    List<VMessageAbstractItem> items = vm.getItems();
                    for (VMessageAbstractItem item : items) {
                        switch (item.getType()) {
                            case VMessageAbstractItem.ITEM_TYPE_IMAGE:
                            case VMessageAbstractItem.ITEM_TYPE_AUDIO:
                                V2Log.d("test", "eeeeeeeeeeeeeee state " + state
                                        + " vm.mRecvState " + vm.mRecvState);
                                if (state == VMessageAbstractItem.TRANS_SENT_FALIED
                                        || vm.mRecvState != 0) {
                                    result = Result.FAILED;
                                    mediaType = 1;
                                    item.setState(VMessageAbstractItem.TRANS_SENT_FALIED);
                                    vm.setState(VMessageAbstractItem.STATE_FILE_SENT_FALIED);
                                } else {
                                    item.setState(VMessageAbstractItem.TRANS_SENT_SUCCESS);
                                }
                                break;
                            case VMessageAbstractItem.ITEM_TYPE_FILE:
                                item.setState(fileState);
                                break;
                        }
                    }
                    ChatMessageProvider.updateChatMessageState(mContext, vm);
                    Intent i = new Intent();
                    i.setAction(JNIService.JNI_BROADCAST_MESSAGE_SENT_RESULT);
                    i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
                    i.putExtra("MsgUID", msgUID);
                    i.putExtra("result", result);
                    i.putExtra("binaryType", mediaType);
                    sendBroadcast(i);
                }
            }

        }

        @Override
        public void OnMonitorRecv(int eGroupType, String sSeqID, int nResult) {
            super.OnMonitorRecv(eGroupType, sSeqID, nResult);
            changekBinaryCache(sSeqID, true, null);
        }

        private void handlerChatPictureCallback(int eGroupType, long nGroupID,
                                                long nFromUserID, long nToUserID, long nTime, String messageId,
                                                String binaryPath) {

            VMessage vMessage = changekBinaryCache(messageId, false, binaryPath);
            if (vMessage != null) {
                vMessage.currentReplaceImageID = messageId;
                Message msg = Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE_BINARY_DATA, vMessage);
                msg.arg1 = 2;
                msg.sendToTarget();
            }
        }

        private void handlerChatAudioCallback(int eGroupType, long nGroupID,
                                              long nFromUserID, long nToUserID, long nTime, String messageId,
                                              String binaryPath) {
            VMessage vMessage = changekBinaryCache(messageId, false, binaryPath);
            if (vMessage != null) {
                Message msg = Message.obtain(mLocalHandlerThreadHandler,
                        JNI_RECEIVED_MESSAGE_BINARY_DATA, vMessage);
                msg.arg1 = 1;
                msg.sendToTarget();
            }
        }
    }

    private class FileRequestCB extends FileRequestCallbackAdapter {

        private Map<String, FileDownLoadBean> mark = new HashMap<String, FileDownLoadBean>();

        @Override
        public void OnFileTransBegin(String szFileID, int nTransType,
                                     long nFileSize) {
            super.OnFileTransBegin(szFileID, nTransType, nFileSize);
        }

        @Override
        public void OnFileTransProgress(String szFileID, long nBytesTransed,
                                        int nTransType) {
            super.OnFileTransProgress(szFileID, nBytesTransed, nTransType);
            if (!szFileID.contains("AVATAR")) {
                super.OnFileTransProgress(szFileID, nBytesTransed, nTransType);
                FileDownLoadBean lastBean = mark.get(szFileID);
                if (lastBean == null) {
                    lastBean = new FileDownLoadBean();
                    lastBean.lastLoadTime = System.currentTimeMillis();
                    lastBean.lastLoadSize = 0;
                    mark.put(szFileID, lastBean);
                } else {
                    FileDownLoadBean bean = GlobalHolder.getInstance().mGlobleFileProgress
                            .get(szFileID);
                    if (bean == null)
                        bean = new FileDownLoadBean();

                    bean.lastLoadTime = lastBean.lastLoadTime;
                    bean.lastLoadSize = lastBean.lastLoadSize;
                    long time = System.currentTimeMillis();
                    bean.currentLoadTime = time;
                    bean.currentLoadSize = nBytesTransed;
                    GlobalHolder.getInstance().mGlobleFileProgress.put(
                            szFileID, bean);
                }
            }
        }

        @Override
        public void OnFileTransInvite(long userid, String szFileID,
                                      String szFileName, long nFileBytes, String url, int linetype) {
            super.OnFileTransInvite(userid, szFileID, szFileName, nFileBytes,
                    url, linetype);
            User fromUser = GlobalHolder.getInstance().getUser(userid);
            // If doesn't receive user information from server side,
            // construct new user object
            if (fromUser == null) {
                fromUser = new User(userid);
            }

            FileType fileType = FileType.UNKNOW;
            if (szFileName != null && !szFileName.isEmpty()) {
                fileType = FileUtils.getFileType(szFileName);
            }

            VMessage vm = new VMessage(0, 0, fromUser, GlobalHolder
                    .getInstance().getCurrentUser(), new Date(
                    GlobalConfig.getGlobalServerTime()));
            new VMessageFileItem(vm, szFileID, nFileBytes,
                    VMessageFileItem.STATE_FILE_UNDOWNLOAD, szFileName,
                    fileType, url);
            vm.setmXmlDatas(vm.toXml());
            ChatMessageProvider.saveChatMessage(vm);
            ChatMessageProvider.saveFileVMessage(vm);
            Message.obtain(mLocalHandlerThreadHandler, JNI_RECEIVED_MESSAGE, vm)
                    .sendToTarget();
        }

        @Override
        public void OnFileTransEnd(String szFileID, String szFileName,
                                   long nFileSize, int nTransType) {
            super.OnFileTransEnd(szFileID, szFileName, nFileSize, nTransType);
            if (!szFileID.contains("AVATAR")) {

                mark.remove(szFileID);
                GlobalHolder.getInstance().mGlobleFileProgress.remove(szFileID);
                VMessageFileItem fileItem = ChatMessageProvider
                        .queryFileItemByID(szFileID);
                if (fileItem == null) {
                    V2Log.e(TAG, "File Trans End Record Failed! ID is : "
                            + szFileID);
                    return;
                }

                int transType;
                if (nTransType == FileDownLoadErrorIndication.TYPE_SEND) {
                    fileItem.setState(VMessageAbstractItem.STATE_FILE_SENT);
                    transType = V2GlobalConstants.FILE_TRANS_SENDING;
                } else {
                    fileItem.setState(VMessageAbstractItem.STATE_FILE_DOWNLOADED);
                    if (fileItem.getVm().getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER) {
                        // 向服务器发送一条接收到的消息
                        String hint = getResources().getString(
                                R.string.common_recv_file);
                        String text = hint + "\"" + fileItem.getFileName()
                                + "\"";
                        VMessage msg = new VMessage(0, 0, GlobalHolder
                                .getInstance().getCurrentUser(), fileItem
                                .getVm().getFromUser(), UUID.randomUUID()
                                .toString(), new Date(
                                GlobalConfig.getGlobalServerTime()));
                        msg.setAutoReply(true);
                        new VMessageFileRecvItem(msg, szFileID, 0);
                        new VMessageTextItem(msg, text);
                        ChatRequest.getInstance().ChatSendTextMessage(
                                msg.getMsgCode(),
                                msg.getGroupId(),
                                msg.getToUser() == null ? 0 : msg.getToUser()
                                        .getmUserId(), msg.getUUID(),
                                msg.toXml().getBytes(),
                                msg.toXml().getBytes().length);
                    }
                    transType = V2GlobalConstants.FILE_TRANS_DOWNLOADING;
                }
                updateFileState(transType, fileItem,
                        "JNIService OnFileTransEnd", false);
                int updates = ChatMessageProvider.updateVMessageItem(mContext,
                        fileItem);
                V2Log.d(TAG, "OnFileTransEnd updates success : " + updates);
                fileItem = null;
            }
        }

        @Override
        public void OnFileTransError(String szFileID, int errorCode,
                                     int nTransType) {
            super.OnFileTransError(szFileID, errorCode, nTransType);
            if (!szFileID.contains("AVATAR")) {
                changekBinaryCache(szFileID, true, null);
                mark.remove(szFileID);
                GlobalHolder.getInstance().mGlobleFileProgress.remove(szFileID);
                VMessageFileItem fileItem = ChatMessageProvider
                        .queryFileItemByID(szFileID);
                if (fileItem != null) {
                    int transType = -1;
                    if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_SENDING
                            || fileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING) {
                        transType = V2GlobalConstants.FILE_TRANS_SENDING;
                        fileItem.setState(VMessageAbstractItem.STATE_FILE_SENT_FALIED);
                    } else if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING
                            || fileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING) {
                        fileItem.setState(VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED);
                        transType = V2GlobalConstants.FILE_TRANS_DOWNLOADING;
                    }
                    ChatMessageProvider.updateVMessageItem(mContext, fileItem);
                    updateFileState(transType, fileItem,
                            "JNIService OnFileTransError", false);
                    Intent intent = new Intent();
                    intent.setAction(JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION);
                    intent.addCategory(JNI_BROADCAST_CATEGROY);
                    intent.putExtra("fileID", szFileID);
                    intent.putExtra("transType", transType);
                    sendBroadcast(intent);
                } else {
                    V2Log.w(TAG, "OnFileTransError updates miss , id : "
                            + szFileID);
                    Intent intent = new Intent();
                    intent.setAction(JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION);
                    intent.addCategory(JNI_BROADCAST_CATEGROY);
                    intent.putExtra("fileID", szFileID);
                    sendBroadcast(intent);
                }
            } else {
                long userID = Long.valueOf(szFileID.split("_")[1]);
                User user = GlobalHolder.getInstance().getUser(userID);
                user.isAvatarLoaded = true;
                BitmapManager.getInstance().notifiyAvatarChanged(
                        user,
                        BitmapUtil.loadAvatarFromPath(
                                V2GlobalConstants.AVATAR_NORMAL, null));
            }
        }
    }

    private class SipRequestCB extends SipRequestCallBackAdapter {

        @Override
        public void OnAcceptSipCall(String szURI, boolean isVideoCall) {
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_SIP_ACCEPT_INVITE);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("result", Result.SUCCESS);
            i.putExtra("sipNumber", szURI);
            i.putExtra("callType", isVideoCall);
            sendBroadcast(i);
        }

        @Override
        public void OnInviteSipCall(String szURI) {
            Message.obtain(mLocalHandlerThreadHandler, JNI_RECEIVED_SIP_COMING,
                    szURI).sendToTarget();
        }

        @Override
        public void OnFailureSipCall(String szURI, int nErrorCode) {
            super.OnFailureSipCall(szURI, nErrorCode);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_SIP_ACCEPT_INVITE);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("result", Result.FAILED);
            i.putExtra("errorCode", nErrorCode);
            i.putExtra("sipNumber", szURI);
            sendBroadcast(i);
        }

        @Override
        public void OnCloseSipCall(String szURI) {
            super.OnCloseSipCall(szURI);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_SIP_ACCEPT_INVITE);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("result", Result.FAILED);
            i.putExtra("sipNumber", szURI);
            sendBroadcast(i);
        }

    }

    private class AppShareRequestCB extends AppShareRequestCallbackAdapter {

        @Override
        public void OnAppShareCreated(int nGroupType, long nGroupID,
                                      long nHostUserID, String szVideoDeviceID) {
            super.OnAppShareCreated(nGroupType, nGroupID, nHostUserID,
                    szVideoDeviceID);
            // Intent i = new Intent();
            // i.setAction(JNI_BROADCAST_CONFERENCE_APPSHARE_CREATE);
            // i.addCategory(JNI_BROADCAST_CATEGROY);
            // i.putExtra("videoDeviceID", szVideoDeviceID);
            // sendBroadcast(i);
        }

        @Override
        public void OnAppShareDestroyed(String szVideoDeviceID) {
            super.OnAppShareDestroyed(szVideoDeviceID);
            // Intent i = new Intent();
            // i.setAction(JNI_BROADCAST_CONFERENCE_APPSHARE_DESTORY);
            // i.addCategory(JNI_BROADCAST_CATEGROY);
            // sendBroadcast(i);
        }
    }

    private class WebManagerRequestCB extends WebManagerRequestCallbackAdapter {

        @Override
        public void OnWebManagerDelUser(long nUserID) {
            super.OnWebManagerDelUser(nUserID);
            // 删除该用户所有的群组信息，除了讨论组
            User removed = GlobalHolder.getInstance().getUser(nUserID);
            // 将全局中控制上传文件的数量变0
            Set<Group> belongsGroup = removed.getBelongsGroup();
            Iterator<Group> iterator = belongsGroup.iterator();
            long nGroupID = 0;
            while (iterator.hasNext()) {
                Group temp = iterator.next();
                if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD
                        || temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                    if (temp.getOwnerUser().getmUserId() == nUserID) {
                        // 将全局中控制上传文件的数量变0
                        GlobalHolder.getInstance().removeGroup(
                                temp.getGroupType(), temp.getGroupID());
                        continue;
                    }
                } else if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                    if (temp.getOwnerUser().getmUserId() == nUserID) {
                        ((DiscussionGroup) temp).setCreatorExist(false);
                    }

                    Group group = GlobalHolder.getInstance().getGroupById(
                            temp.getGroupID());
                    if (group.getOwnerUser().getmUserId() == nUserID) {
                        ((DiscussionGroup) group).setCreatorExist(false);
                    }
                } else if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                    nGroupID = temp.getGroupID();
                }
                temp.removeUserFromGroup(nUserID);
                Group group = GlobalHolder.getInstance().getGroupById(
                        temp.getGroupID());
                group.removeUserFromGroup(nUserID);
            }
            GlobalHolder.getInstance().removeGroupUser(nGroupID, nUserID);

            GroupUserObject obj = new GroupUserObject(
                    V2GlobalConstants.GROUP_TYPE_CONTACT, nGroupID, nUserID);
            Intent i = new Intent();
            i.setAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
            i.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            i.putExtra("obj", obj);
            sendBroadcast(i);
        }
    }
}
