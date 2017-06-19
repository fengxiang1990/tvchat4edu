package com.bizcom.request;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.V2.jni.AudioRequest;
import com.V2.jni.ChatRequest;
import com.V2.jni.FileRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.SipRequest;
import com.V2.jni.VideoRequest;
import com.V2.jni.callbacAdapter.AudioRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.FileRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.VideoRequestCallbackAdapter;
import com.V2.jni.callback.AudioRequestCallback;
import com.V2.jni.callback.VideoRequestCallback;
import com.V2.jni.ind.VideoJNIObjectInd;
import com.bizcom.request.jni.FileTransStatusIndication;
import com.bizcom.request.jni.FileTransStatusIndication.FileTransProgressStatusIndication;
import com.bizcom.request.jni.RequestChatServiceResponse;
import com.bizcom.request.util.DeviceRequest;
import com.bizcom.request.util.FileOperationEnum;
import com.bizcom.util.V2Log;
import com.bizcom.vo.UserChattingObject;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.config.GlobalConfig;
import com.config.V2GlobalConstants;

import java.util.List;

/**
 * @author 28851274
 */
public class V2ChatRequest extends DeviceRequest {

    private static final String TAG = V2ChatRequest.class.getSimpleName();

    private AudioRequestCallback mAudioCallBack;
    private VideoRequestCallback mVideoCallBack;
    private FileRequestCB mFileCallBack;

    private Handler mMessageQueueHandler;
    private HandlerThread mMessaeQueueHandlerTH;

    private static final int KEY_CANCELLED_LISTNER = 1;
    private static final int KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER = 2;
    private static final int KEY_VIDEO_CONNECTED = 3;
    private static final int KEY_P2P_CALL_RESPONSE = 4;
    private static final int KEY_P2P_RECORD_CALL_RESPONSE = 5;
    private static final int KEY_P2P_RECORD_MIC_CALL_RESPONSE = 6;

    // 调用jni nativie接口函数名的标记，降低耦合性，即使底层修改接口名字上层也不用改
    public static final int NATIVE_INVITE = 0x001;
    public static final int NATIVE_ACCEPT = 0x002;
    public static final int NATIVE_REFUSE = 0x003;
    public static final int NATIVE_CANNEL = 0x004;
    public static final int NATIVE_CLOSE = 0x005;
    public static final int NATIVE_MUTE = 0x006;
    public static final int NATIVE_RECORD_START = 0x007;
    public static final int NATIVE_RECORD_STOP = 0x008;

    public V2ChatRequest() {
        mAudioCallBack = new AudioRequestCallbackImpl();
        AudioRequest.getInstance().addCallback(mAudioCallBack);

        mVideoCallBack = new VideoRequestCallbackImpl();
        VideoRequest.getInstance().addCallback(mVideoCallBack);

        mFileCallBack = new FileRequestCB();
        FileRequest.getInstance().addCallback(mFileCallBack);

        mMessaeQueueHandlerTH = new HandlerThread("back-end");
        mMessaeQueueHandlerTH.start();
        mMessageQueueHandler = new Handler(mMessaeQueueHandlerTH.getLooper());
    }

    @Override
    public void clearCalledBack() {
        AudioRequest.getInstance().removeCallback(mAudioCallBack);
        VideoRequest.getInstance().removeCallback(mVideoCallBack);
        FileRequest.getInstance().removeCallback(mFileCallBack);
        mMessaeQueueHandlerTH.quit();
    }

    public static void invokeNative(int nativeMethod, Object... obj) {
        switch (nativeMethod) {
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_INVITE ,
            case NATIVE_INVITE:
                AudioRequest.getInstance().AudioInviteChat((String) obj[0], (Long) obj[1]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_ACCEPT ,
            case NATIVE_ACCEPT:
                AudioRequest.getInstance().AudioAcceptChat((String) obj[0], (Long) obj[1]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_REFUSE ,
            case NATIVE_REFUSE:
                AudioRequest.getInstance().AudioRefuseChat((String) obj[0], (Long) obj[1]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_CANNEL ,
            case NATIVE_CANNEL:
                AudioRequest.getInstance().AudioCancelChat((String) obj[0], (Long) obj[1]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_CLOSE ,
            case NATIVE_CLOSE:
                AudioRequest.getInstance().AudioCloseChat((String) obj[0], (Long) obj[1]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_MUTE ,
            case NATIVE_MUTE:
                AudioRequest.getInstance().AudioMuteMic((Long) obj[0], (Long) obj[1], (Boolean) obj[2]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_RECORD_START ,
            case NATIVE_RECORD_START:
                AudioRequest.getInstance().AudioStartRecord((String) obj[0]);
                break;
            // V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_RECORD_STOP ,
            case NATIVE_RECORD_STOP:
                AudioRequest.getInstance().AudioStopRecord((String) obj[0]);
                break;
            default:
                break;
        }
    }

    /**
     * Register listener for out conference by kick.
     *
     * @param h
     * @param what
     * @param obj
     */
    public void registerCancelledListener(Handler h, int what, Object obj) {
        registerListener(KEY_CANCELLED_LISTNER, h, what, obj);
    }

    /**
     * Register listener for video connected, after this notification. can open
     * remote video device
     *
     * @param h
     * @param what
     * @param obj
     */
    public void registerVideoChatConnectedListener(Handler h, int what, Object obj) {
        registerListener(KEY_VIDEO_CONNECTED, h, what, obj);
    }

    /**
     * Register listener for out conference by kick.
     *
     * @param h
     * @param what
     * @param obj
     */
    public void registerFileTransStatusListener(Handler h, int what, Object obj) {
        registerListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, h, what, obj);
    }

    public void registerP2PCallResponseListener(Handler h, int what, Object obj) {
        registerListener(KEY_P2P_CALL_RESPONSE, h, what, obj);
    }

    public void registerP2PRecordResponseListener(Handler h, int what, Object obj) {
        registerListener(KEY_P2P_RECORD_CALL_RESPONSE, h, what, obj);
    }

    public void registerP2PRecordMicResponseListener(Handler h, int what, Object obj) {
        registerListener(KEY_P2P_RECORD_MIC_CALL_RESPONSE, h, what, obj);
    }

    public void removeRegisterCancelledListener(Handler h, int what, Object obj) {
        unRegisterListener(KEY_CANCELLED_LISTNER, h, what, obj);
    }

    public void removeVideoChatConnectedistener(Handler h, int what, Object obj) {
        unRegisterListener(KEY_VIDEO_CONNECTED, h, what, obj);
    }

    public void removeRegisterFileTransStatusListener(Handler h, int what, Object obj) {
        unRegisterListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, h, what, obj);
    }

    public void removeP2PCallResponseListener(Handler h, int what, Object obj) {
        unRegisterListener(KEY_P2P_CALL_RESPONSE, h, what, obj);
    }

    public void removeP2PRecordResponseListener(Handler h, int what, Object obj) {
        unRegisterListener(KEY_P2P_RECORD_CALL_RESPONSE, h, what, obj);
    }

    public void removeP2PRecordMicResponseListener(Handler h, int what, Object obj) {
        unRegisterListener(KEY_P2P_RECORD_MIC_CALL_RESPONSE, h, what, obj);
    }

    /**
     * send message
     *
     * @param msg
     */
    public void requestSendChatMessage(final VMessage msg) {
        if (msg == null) {
            V2Log.e(TAG, "Send Message fail ! Because VMessage Object is null ! please check!");
            return;
        }

        mMessageQueueHandler.post(new Runnable() {
            @Override
            public void run() {

                // Send file
                if (msg.getFileItems().size() > 0) {
                    requestFile2SendMessage(msg);
                    return;
                }
                // If message items do not only contain audio message item
                // then send text message
                String xml = msg.toXml();
                V2Log.e(xml);
                byte[] bytes = xml.getBytes();
                ChatRequest.getInstance().ChatSendTextMessage(msg.getMsgCode(), msg.getGroupId(),
                        msg.getToUser() == null ? 0 : msg.getToUser().getmUserId(), msg.getUUID(), bytes, bytes.length);
                V2Log.d(TAG, "sendTextMessage---> eGroupType :" + msg.getMsgCode() + " | nGroupID: " + msg.getGroupId()
                        + " | sSeqID: " + msg.getUUID() + " | sendContent: " + msg.getTextContent());
                // send image message
                List<VMessageImageItem> imageItems = msg.getImageItems();
                for (int i = 0; imageItems != null && i < imageItems.size(); i++) {
                    VMessageImageItem item = imageItems.get(i);
                    // byte[] data = item.loadImageData();
                    ChatRequest.getInstance().ChatSendBinaryMessage(msg.getMsgCode(), msg.getGroupId(),
                            msg.getToUser() == null ? 0 : msg.getToUser().getmUserId(), 2, item.getUuid(),
                            item.getFilePath());
                    V2Log.d(TAG, "sendBinaryMessage---> eGroupType :" + msg.getMsgCode() + " | nGroupID: "
                            + msg.getGroupId() + " | MessageID: " + msg.getUUID() + " | sSeqID: " + item.getUuid());
                }

                // send aduio message
                List<VMessageAudioItem> audioList = msg.getAudioItems();
                for (int i = 0; audioList != null && i < audioList.size(); i++) {
                    ChatRequest.getInstance().ChatSendBinaryMessage(msg.getMsgCode(), msg.getGroupId(),
                            msg.getToUser() == null ? 0 : msg.getToUser().getmUserId(), 3, audioList.get(i).getUuid(),
                            audioList.get(i).getAudioFilePath());
                    V2Log.d(TAG,
                            "sendBinaryMessage---> eGroupType :" + msg.getMsgCode() + " | nGroupID: " + msg.getGroupId()
                                    + " | MessageID: " + msg.getUUID() + " | sSeqID: " + audioList.get(i).getUuid());
                }
            }
        });
    }

    public void requestFile2SendMessage(VMessage vm) {
        List<VMessageFileItem> items = vm.getFileItems();
        for (VMessageFileItem item : items) {
            if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_CROWD) {
                V2Log.d(TAG, "uploadFile --> uuid is : " + item.getUuid() + " name is : " + item.toXmlItem());
                GroupRequest.getInstance().FileTransUploadGroupFile(vm.getMsgCode(), vm.getGroupId(), item.toXmlItem());
            } else {
                V2Log.d(TAG, "sendFile --> uuid is : " + item.getUuid() + " name is : " + item.toXmlItem());
                FileRequest.getInstance().FileTransInviteImFile(vm.getToUser().getmUserId(), item.toXmlItem(),
                        V2GlobalConstants.FILE_TYPE_OFFLINE);
            }
        }
    }

    /**
     * Update file operation, like pause transport; resume transport; cancel
     * transport
     *
     * @param vfi file item Object
     * @param opt operation of file
     * @see FileOperationEnum
     */
    public void requestFile2UpdateStateOperation(VMessageFileItem vfi, FileOperationEnum opt) {
        if (vfi == null || (opt == null)) {
            return;
        }

        switch (opt) {
            case OPERATION_PAUSE_SENDING:
                FileRequest.getInstance().FileTransPauseUploadFile(vfi.getUuid());
                break;
            case OPERATION_RESUME_SEND:
                FileRequest.getInstance().FileTransResumeUploadFile(vfi.getUuid());
                break;
            case OPERATION_PAUSE_DOWNLOADING:
                FileRequest.getInstance().FileTransPauseDownloadFile(vfi.getUuid());
                break;
            case OPERATION_RESUME_DOWNLOAD:
                FileRequest.getInstance().FileTransResumeDownloadFile(vfi.getUuid());
                break;
            case OPERATION_CANCEL_SENDING:
                FileRequest.getInstance().FileTransCloseSendFile(vfi.getUuid());
                break;
            case OPERATION_CANCEL_DOWNLOADING:
                FileRequest.getInstance().FileTransCloseRecvFile(vfi.getUuid());
                break;
            case OPERATION_START_DOWNLOAD:
                String path = GlobalConfig.getGlobalFilePath() + "/" + vfi.getFileName();
                V2Log.d(TAG, "start download file! id is : " + vfi.getUuid() + " and path is : " + path + " and url is : "
                        + vfi.getUrl());
                FileRequest.getInstance().FileTransDownloadFile(vfi.getUrl(), vfi.getUuid(), path,
                        V2GlobalConstants.FILE_ENCRYPT_TYPE);
            default:
                break;

        }
    }

    /**
     * Invite contact for chat
     *
     * @param ud
     */
    public void requestAV2Invite(UserChattingObject ud) {
        if (ud.isSipCall()) {
            SipRequest.getInstance().InviteSipCall(ud.getSipNumber(), ud.isVideoType());
        } else {
            if (ud.isAudioType()) {
                V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_INVITE, ud.getSzSessionID(), ud.getUser().getmUserId());
            } else if (ud.isVideoType()) {

                // If connected, send audio message
                if (ud.isConnected()) {
                    String szSessionID = ud.getSzSessionID();
                    szSessionID = "ByVideo" + szSessionID;
                    V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_INVITE, szSessionID, ud.getUser().getmUserId());
                } else {
                    if (TextUtils.isEmpty(ud.getDeviceId())) {
                        ud.setDeviceId(ud.getUser().getmUserId() + ":Camera");
                    }
                    VideoRequest.getInstance().VideoInviteChat(ud.getSzSessionID(), ud.getUser().getmUserId(),
                            ud.getDeviceId(), ud.getData());
                }
            }
        }
    }

    /**
     * Cancel current video or audio conversation.
     *
     * @param ud
     */
    public void requestAV2Close(UserChattingObject ud) {
        if (ud.isSipCall()) {
            SipRequest.getInstance().CloseSipCall(ud.getSipNumber());
        } else {
            if (ud.isAudioType()) {
                if (ud.isConnected() || !ud.isIncoming()) {
                    V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_CLOSE, ud.getSzSessionID(),
                            ud.getUser().getmUserId());
                } else {
                    V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_REFUSE, ud.getSzSessionID(),
                            ud.getUser().getmUserId());
                }

            } else if (ud.isVideoType()) {
                if (ud.isConnected() || !ud.isIncoming()) {
                    VideoRequest.getInstance().VideoCloseChat(ud.getSzSessionID(), ud.getUser().getmUserId(),
                            ud.getDeviceId());

                    V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_CLOSE, ud.getSzSessionID(),
                            ud.getUser().getmUserId());
                } else {
                    VideoRequest.getInstance().VideoRefuseChat(ud.getSzSessionID(), ud.getUser().getmUserId(),
                            ud.getDeviceId(), ud.getData());
                }
            }
        }
    }

    /**
     * accept incoming call
     *
     * @param ud
     */
    public void reqeustAV2Accept(UserChattingObject ud) {
        if (ud.isSipCall()) {
            SipRequest.getInstance().AcceptSipCall(ud.getSipNumber(), ud.isVideoType());
        } else {
            if (ud.isAudioType()) {
                V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_ACCEPT, ud.getSzSessionID(), ud.getUser().getmUserId());
            } else if (ud.isVideoType()) {
                VideoRequest.getInstance().VideoAcceptChat(ud.getSzSessionID(), ud.getUser().getmUserId(),
                        ud.getDeviceId());
            }
        }
    }

    /**
     * Mute microphone when in conversation
     *
     * @param ud
     */
    public void requestAV2UpdateMuteState(UserChattingObject ud) {
        if (ud.isSipCall()) {
            SipRequest.getInstance().SetMicMuted(ud.isMute());
        } else {
            V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_MUTE, ud.getGroupdId(), ud.getUser().getmUserId(),
                    ud.isMute());
        }
    }

    class VideoRequestCallbackImpl extends VideoRequestCallbackAdapter {

        @Override
        public void OnVideoChatAccepted(String szSessionID, long nFromUserID, String szDeviceID) {
            super.OnVideoChatAccepted(szSessionID, nFromUserID, szDeviceID);
            RequestChatServiceResponse resp = new RequestChatServiceResponse(
                    RequestChatServiceResponse.P2P_TYPE_VIDEO,
                    RequestChatServiceResponse.ACCEPTED,
                    nFromUserID, szSessionID, szDeviceID, RequestChatServiceResponse.Result.SUCCESS);
            notifyListener(KEY_P2P_CALL_RESPONSE, 0, 0, resp);
        }

        @Override
        public void OnVideoChatRefused(String szSessionID, long nFromUserID, String szDeviceID,String data) {
            super.OnVideoChatRefused(szSessionID, nFromUserID, szDeviceID,data);
            RequestChatServiceResponse resp = new RequestChatServiceResponse(
                    RequestChatServiceResponse.P2P_TYPE_VIDEO,
                    RequestChatServiceResponse.REJCTED,
                    nFromUserID, szSessionID, szDeviceID, RequestChatServiceResponse.Result.SUCCESS);
            resp.setData(data);
            notifyListener(KEY_P2P_CALL_RESPONSE, 0, 0, resp);
        }

        @Override
        public void OnVideoChating(String szSessionID, long nFromUserID, String szDeviceID) {
            super.OnVideoChating(szSessionID, nFromUserID, szDeviceID);
            VideoJNIObjectInd ind = new VideoJNIObjectInd(szSessionID, nFromUserID, szDeviceID, 0);
            notifyListener(KEY_VIDEO_CONNECTED, 0, 0, ind);
        }
    }

    class AudioRequestCallbackImpl extends AudioRequestCallbackAdapter {

        @Override
        public void OnAudioChatAccepted(String szSessionID, long nFromUserID) {
            super.OnAudioChatAccepted(szSessionID, nFromUserID);
            RequestChatServiceResponse resp = new RequestChatServiceResponse(
                    RequestChatServiceResponse.P2P_TYPE_AUDIO,
                    RequestChatServiceResponse.ACCEPTED,
                    nFromUserID, szSessionID, null, RequestChatServiceResponse.Result.SUCCESS);
            notifyListener(KEY_P2P_CALL_RESPONSE, 0, 0, resp);
        }

        @Override
        public void OnAudioChatRefused(String szSessionID, long nFromUserID) {
            super.OnAudioChatRefused(szSessionID, nFromUserID);
            RequestChatServiceResponse resp = new RequestChatServiceResponse(
                    RequestChatServiceResponse.P2P_TYPE_AUDIO,
                    RequestChatServiceResponse.REJCTED,
                    nFromUserID, szSessionID, null, RequestChatServiceResponse.Result.SUCCESS);
            notifyListener(KEY_P2P_CALL_RESPONSE, 0, 0, resp);
        }

        @Override
        public void OnRecordStart(String fileID, int result) {
            super.OnRecordStart(fileID, result);
            notifyListener(KEY_P2P_RECORD_CALL_RESPONSE, result, V2GlobalConstants.RECORD_TYPE_START, fileID);
        }

        @Override
        public void OnRecordStop(String fileID, String filePath, int result) {
            super.OnRecordStop(fileID, filePath, result);
            notifyListener(KEY_P2P_RECORD_CALL_RESPONSE, result, V2GlobalConstants.RECORD_TYPE_STOP, fileID);
        }

        @Override
        public void OnAudioMicCurrentLevel(int nValue) {
            super.OnAudioMicCurrentLevel(nValue);
            notifyListener(KEY_P2P_RECORD_MIC_CALL_RESPONSE, nValue, 0, null);
        }
    }

    class FileRequestCB extends FileRequestCallbackAdapter {

        @Override
        public void OnFileTransProgress(String szFileID, long nBytesTransed, int nTransType) {
            notifyListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, 0, 0, new FileTransProgressStatusIndication(
                    nTransType, szFileID, nBytesTransed, FileTransStatusIndication.IND_TYPE_PROGRESS_TRANSING));

        }

        @Override
        public void OnFileTransEnd(String szFileID, String szFileName, long nFileSize, int nTransType) {
            notifyListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, 0, 0, new FileTransProgressStatusIndication(
                    nTransType, szFileID, nFileSize, FileTransStatusIndication.IND_TYPE_PROGRESS_END));
        }
    }
}
