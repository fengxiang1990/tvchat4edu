package com.bizcom.vc.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.V2.jni.AudioRequest;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.bo.UserStatusObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.V2ChatRequest;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.RequestChatServiceResponse;
import com.bizcom.request.util.AsyncResult;
import com.bizcom.service.JNIService;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.DateUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.conference.ConferenceSurfaceView;
import com.bizcom.vc.activity.conversation.ConversationSelectImageActivity;
import com.bizcom.vc.hg.beans.queryPhoneInfoBean;
import com.bizcom.vc.hg.beans.setPBeans;
import com.bizcom.vc.hg.ui.SecondTab1;
import com.bizcom.vc.hg.util.DataUtil;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.CameraConfiguration;
import com.bizcom.vo.User;
import com.bizcom.vo.User.Status;
import com.bizcom.vo.UserChattingObject;
import com.bizcom.vo.VideoBean;
import com.bizcom.vo.enums.NetworkStateCode;
import com.bizcom.vo.meesage.VMessage;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.shdx.tvchat.phone.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;
import java.util.UUID;

import v2av.VideoCaptureDevInfo;
import v2av.VideoPlayer;
import v2av.VideoPlayer.RemoteVideoPlayCallBack;
import v2av.VideoRecorder;

/**
 * 视频呼入呼出通话
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ConversationP2PAVActivity extends BaseActivity implements UncaughtExceptionHandler {
    public static final String TAG = "ConversationP2PAVActivity";

    // 判断对方横竖屏
    private boolean isRomoteVideoLandScape;
    private static final int UPDATE_TIME = 1;
    private static final int UPDATE_VIDEO_BYOWNER = 7;
    private static final int CHAT_CLOSE_LISTNER = 4;
    private static final int CHAT_CALL_RESPONSE = 5;
    private static final int KEY_VIDEO_CONNECTED = 6;

    public static final int P2P_CONNECT_AUDIO = 10;
    public static final int P2P_CONNECT_VIDEO = 11;
    public static final int P2P_CONNECT_SIP = 12;

    private static final String SURFACE_HOLDER_TAG_LOCAL = "local";
    private static final String SURFACE_HOLDER_TAG_REMOTE = "remote";

    public static final String P2P_BROADCAST_MEDIA_UPDATE = "com.v2tech.p2p.broadcast.media_update";

    private Context mContext;
    private UserChattingObject uad;
    private V2ChatRequest chatService = new V2ChatRequest();

    private AudioManager audioManager;
    private BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
    public RelativeLayout smallWindowVideoLayout;
    private RelativeLayout bigWindowVideoLayout;

    private ConferenceSurfaceView mLocalSurface;
    private ConferenceSurfaceView mRemoteSurface;

    private MediaPlayer mPlayer;
    private VideoPlayer mRmoteVideoPlayer;

    private boolean isOpenedRemote;
    private boolean isStoped;
    private boolean isOpenedLocal;
    private VideoBean currentVideoBean;
    private long startTime;

    // private boolean displayWidthIsLonger;
    private boolean isRejected;
    private boolean isAccepted;
    private boolean isBluetoothHeadsetConnected;
    private boolean isSmallLayoutClick;

    /**
     * 本地视频与远端视频相互切换时的标记
     */
    private boolean testFlag = true;
    /**
     * 以下标记主要是防止退出动作的重复执行
     */
    private boolean isHangUping;
    private boolean isNeedToQuit;
    private boolean isAlreadyDestory = false;
    private int mFixSurfaceWidth = 0;
    private int mFixSurfaceHeight = 0;

    /* listener */
    private SurfaceHolder.Callback mRemoteVideoHolder = new RemoteVideoHolderSHCallback();
    private SurfaceHolder.Callback mLocalCameraSHCallback = new LocalCameraSHCallback();
    private Dialog mQuitDialog;
    private PowerManager.WakeLock mWakeLock;

    private int mRemoteVideoWidth = mFixSurfaceWidth;
    private int mRemoteVideoHeight = mFixSurfaceHeight;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mScreenContentHeight = 0;

    private int lastX;
    private int lastY;

    private View videolHangup;


    //    private boolean isLanscape;//当前是否横屏;
    public static final int ACTIVITY_PICK_PHOTO = 0x003;

    private ViewGroup mViewSending;
    private ViewGroup mViewSendFinish;
    private ViewGroup mViewProgress;
    private ProgressBar mPb;
    private TextView mTextResult;
    private TextView mTextCancel;
    private ImageView mImageIcon;
    private int mProgress = 0;
    private int mProgressResult = 0;

    // 通话时长
    private long mTimeLine = 0;
    private TextView mTimerTV;
    private ImageView mVideoExpression, mVideoPhoto, mVideoMute, mVideoHangUp, mVideoCamera, mVideoSpeak, mVideoTheme;
    private TextView mVideoMuteTv, mVideoCameraTv, mVideoSpeakTv;
    private boolean isMuteClose, isVideoLoading;
    private View btLayout, bqLayout, ztLayout, gifText;
    private SimpleDraweeView sdvGif;
    private TextView videoClose;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setNeedAvatar(false);
        super.setNeedBroadcast(true);
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        mContext = this;

        // 加如下设置锁屏状态下一样能跳出此activity
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Gank");
            mWakeLock.acquire();
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        // Update global state
        GlobalHolder.getInstance().setP2pAVNeedStickyBraodcast(false);
        AudioRequest.getInstance().AudioSetAecm(true);
        AudioRequest.getInstance().AudioSetAecmMode(3);
        AudioRequest.getInstance().AudioSetAgc(false);
//     AudioRequest.getInstance().AudioSetMicGain(2);
        AudioRequest.getInstance().AudioSetNs(true, 3);
        // This variable is used to determine the current device width is
        // greater than height
        buildIntentObject();
        fixSurfaceViewSize();
        initCallRecord();
        init();
        // initVideoConnect();
        // messageSendUtil = new MessageSendUtil(this);// 消息发送类

        V2Log.i(TAG, "onCreate finished! : " + GlobalHolder.getInstance().isP2pAVNeedStickyBraodcast());
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i("tvliao", "onStart");
        isStoped = false;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("tvliao", "onResume");
        if (isNeedToQuit) {
            V2Log.d(TAG, "onResume invoke hangUp()");
            hangUp();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("tvliao", "onStop");
        V2Log.d(TAG, "onStop....");
        isStoped = true;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        V2Log.d(TAG, "onNewIntent invokeing....");
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("tvliao", "onDestroy");
        SecondTab1.RemoteUser = null;
        SecondTab1.setConnectedMessageHandle(null);
        V2Log.d(TAG, "ondestory invokeing....");
        if (isAlreadyDestory) {
            return;
        }
        isAlreadyDestory = true;
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        closeLocalVideo();
        chatService.removeRegisterCancelledListener(mHandler, CHAT_CLOSE_LISTNER, null);
        chatService.removeVideoChatConnectedistener(mHandler, KEY_VIDEO_CONNECTED, null);
        chatService.removeP2PCallResponseListener(mHandler, CHAT_CALL_RESPONSE, null);
        chatService.clearCalledBack();


        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        GlobalHolder.getInstance().setP2pAVNeedStickyBraodcast(true);
        setGlobalState(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!uad.isConnected())
            return;

        fixSurfaceViewSize();
        openLocalVideo();
        Log.i("tvliao", "onConfigurationChanged");
        setSurfaceLayoutParams(newConfig);
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(PublicIntent.BROADCAST_JOINED_CONFERENCE_NOTIFICATION);
        filter.addAction(JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION);
        filter.addAction(JNIService.JNI_BROADCAST_SIP_ACCEPT_INVITE);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
        filter.addAction(JNIService.JNI_BROADCAST_VIDEO_CALL_CLOSED);
        filter.addAction(SecondTab1.NEW_MSG);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION:
                NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
                if (code != NetworkStateCode.CONNECTED) {
                    if (!isStoped) {
                        V2Log.d(TAG, "JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION 调用了 HANG_UP_NOTIFICATION");
                        hangUp();
                    } else {
                        isNeedToQuit = true;
                    }
                }
                break;
            case Intent.ACTION_HEADSET_PLUG:
                if (intent.hasExtra("state")) {
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1) {
                        V2Log.i(TAG, "插入耳机");
                        headsetAndBluetoothHeadsetHandle(false);
                    } else if (state == 0) {
                        V2Log.i(TAG, "拔出耳机");
                        headsetAndBluetoothHeadsetHandle(false);
                    }
                }
                break;
            case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);

                if (state == BluetoothProfile.STATE_CONNECTED) {
                    isBluetoothHeadsetConnected = true;
                    V2Log.i(TAG, "蓝牙耳机已连接");
                    headsetAndBluetoothHeadsetHandle(false);
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    V2Log.i("TAG_THIS_FILE", "蓝牙耳机已断开");
                    isBluetoothHeadsetConnected = false;
                    headsetAndBluetoothHeadsetHandle(false);
                }
                break;
            case Intent.ACTION_USER_PRESENT:
                if (uad.isVideoType() && (uad.isConnected() && uad.isIncoming())) {
                    openLocalVideo();
                    Log.i("tvliao", "ACTION_USER_PRESENT");
                    if (uad.isConnected()) {
                        openRemoteVideo();
                    }
                }
                break;
            case PublicIntent.BROADCAST_JOINED_CONFERENCE_NOTIFICATION:
                V2Log.i(TAG, "BROADCAST_JOINED_CONFERENCE_NOTIFICATION was call hangUp!!");
                if (!uad.isIncoming()) {
                    mHandler.removeCallbacks(timeOutRun);
                }
                hangUp();
                break;
            case JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION:
                UserStatusObject uso = (UserStatusObject) intent.getExtras().get("status");
                if (uso != null && uso.getUid() == uad.getUser().getmUserId()) {
                    if (uso.getStatus() == Status.OFFLINE.toIntValue()) {
                        V2Log.d(TAG, "JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION invoke hangUp()");
                        hangUp();
                    }
                }
                break;
            case JNIService.JNI_BROADCAST_GROUP_USER_REMOVED:
                GroupUserObject obj = intent.getParcelableExtra("obj");
                if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    if (obj.getmUserId() == uad.getUser().getmUserId()) {
                        Message.obtain(mHandler, CHAT_CLOSE_LISTNER, uad.getSipNumber()).sendToTarget();
                    }
                }
                break;
            case JNIService.JNI_BROADCAST_VIDEO_CALL_CLOSED:
                String szSessionID = intent.getStringExtra("groupId");
                if (szSessionID != null && szSessionID.equals(uad.getSzSessionID())) {
                    V2Log.d(TAG, "initReceiver invoking ..hangUp() ");
                    hangUp();
                }
                break;
            case SecondTab1.NEW_MSG:
                MessageObject msgObj = intent.getParcelableExtra("msgObj");
                long remoteID = msgObj.rempteUserID;
                long msgID = msgObj.messageColsID;
                VMessage m = ChatMessageProvider.loadUserMessageById(remoteID, msgID);
                String getInfo = m.getPlainText();
                JSONObject json = null;
                try {
                    json = new JSONObject(getInfo);
                    int type = json.getInt("type");
                    // 表情播放
                    switch (type) {
                        case ConstantParams.MESSAGE_TYPE_MULTI:// 发送过来的表情
                            if (isStartAnimIng) {
                                return;
                            }
                            int imgNum = json.getInt("imgeNum");
                            switch (imgNum) {
                                case 0:
                                    playGif(R.drawable.gif_dg);
                                    break;
                                case 1:
                                    playGif(R.drawable.gif_xh);
                                    break;
                                case 2:
                                    playGif(R.drawable.gif_qq);
                                    break;
                                case 3:
                                    playGif(R.drawable.gif_xk);
                                    break;
                                case 4:
                                    playGif(R.drawable.gif_dz);
                                    break;
                            }
                            break;
                        case ConstantParams.MESSAGE_TYPE_VIDEO_CLOSE:
                            //摄像头控制，0关闭，1打开
                            int dt = json.getInt("data");
                            if (videoClose != null) {
                                if (dt == 0) {//提示
                                    videoClose.setVisibility(View.VISIBLE);
                                    videoClose.setText("对方关闭了摄像头");
                                } else {//隐藏
                                    videoClose.setVisibility(View.GONE);
                                }
                            }
                            break;
                        case ConstantParams.MESSAGE_TYPE_CAMERA_ERROR:
                            isVideoLoading = false;
                            mHandler.postDelayed(gifStop, 0);
                            if (videoClose != null) {
                                videoClose.setVisibility(View.VISIBLE);
                                videoClose.setText("对方未打开摄像头");
                            }
                            break;
                    }
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_TIME:
                if (mTimeLine != -1 && mTimerTV != null) {
                    mTimeLine++;
                    mTimerTV.setText(DateUtil.calculateFixedTime(mTimeLine));
                    Message m = Message.obtain(mHandler, UPDATE_TIME);
                    mHandler.sendMessageDelayed(m, 1000);
                }
                break;
            case CHAT_CLOSE_LISTNER:
                chatService.requestAV2Close(uad);
                long fromUserID = msg.arg1;
                if (fromUserID != 0 && fromUserID != uad.getUser().getmUserId()) {
                    return;
                }

                if (isHangUping) {
                    break;
                }

                if (!isHangupFlag && !uad.isConnected()) {
                    ToastUtil.ShowToast_long(mContext, "对方已挂断");
                }

                V2Log.d(TAG, "the CHAT_CLOSE_LISTNER was called ....");
                isHangUping = true;
                // 更新音视频聊天记录
                if (currentVideoBean.startDate > 0) {
                    currentVideoBean.endDate = GlobalConfig.getGlobalServerTime();
                } else {
                    currentVideoBean.startDate = startTime;
                }

                if (currentVideoBean.mediaState != AudioVideoMessageBean.STATE_ANSWER_CALL)
                    currentVideoBean.mediaState = AudioVideoMessageBean.STATE_NO_ANSWER_CALL;

                if (uad.isIncoming()) {
                    if (isRejected || isAccepted)
                        currentVideoBean.readSatate = V2GlobalConstants.READ_STATE_READ;
                    else
                        currentVideoBean.readSatate = V2GlobalConstants.READ_STATE_UNREAD;
                } else {
                    currentVideoBean.readSatate = V2GlobalConstants.READ_STATE_READ;
                }

                currentVideoBean.account = uad.getUser().getAccount();
                currentVideoBean.nickName = uad.getUser().getDisplayName();
                currentVideoBean.avatarUrl = uad.getUser().getmAvatarLocation();

                MediaRecordProvider.saveMediaChatHistories(currentVideoBean);
                // send broadcast to update voice message
                Intent intent = new Intent();
                intent.setAction(P2P_BROADCAST_MEDIA_UPDATE);
                intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                intent.putExtra("remoteID", currentVideoBean.remoteUserID);
                sendBroadcast(intent);
                // 禁用按钮
                disableAllButtons();
                // 关闭视频
                if (uad.isVideoType()) {
                    closeRemoteVideo();
                    closeLocalVideo();
                }
                stopRingTone();
                // Delay Exit Interface , Because need update UI
                mHandler.postAtTime(new Runnable() {

                    @Override
                    public void run() {//未接通  zhan xian
                        finish();
                    }
                }, 2000);
                break;
            case CHAT_CALL_RESPONSE:// 视频被接通
                if (isHangUping) {
                    V2Log.d(TAG, "当前已处在挂断过程...");
                    return;
                }
                stopRingToneOuting();
                notifyTv(ConstantParams.MESSAGE_TYPE_VIDEO_CONNECTED);
                RequestChatServiceResponse rcsr = (RequestChatServiceResponse) ((AsyncResult) msg.obj).getResult();

                if ((rcsr.getP2PChatType() == RequestChatServiceResponse.P2P_TYPE_AUDIO && !uad.isAudioType())
                        || (rcsr.getP2PChatType() == RequestChatServiceResponse.P2P_TYPE_VIDEO && !uad.isVideoType())) {
                    V2Log.d(TAG, "收到了远端音视频的反馈... TYPE ERROR , RECEIVE TYPE : " + rcsr.getP2PChatType() + " | sessionID : "
                            + rcsr.getSzSessionID());
                }

                String sessionID = rcsr.getSzSessionID();
                V2Log.d(TAG, "收到了远端音视频的反馈ID... " + sessionID + " 当前音视频ID... " + uad.getSzSessionID());
                currentVideoBean.mediaState = AudioVideoMessageBean.STATE_ANSWER_CALL;
                if (rcsr.getCode() == RequestChatServiceResponse.ACCEPTED) {
                    if (sessionID == null || sessionID.contains("ByVideo")) {
                        return;
                    }

                    if (!sessionID.equals(uad.getSzSessionID())) {
                        V2Log.d(TAG, "ACCEPTED -- 接收的音视频返回信令与当前正在等待的信令不匹配!");
                        return;
                    }

                    uad.setConnected(true);
                    if (uad.isVideoType()) {
                        currentVideoBean.startDate =
                                GlobalConfig.getGlobalServerTime();
                        headsetAndBluetoothHeadsetHandle(true);
                        chatService.requestAV2Invite(uad);
                        if (!uad.isIncoming()) {
                            // 呼出视频通话被接通
                            initVideoConnect();
                        }
                        uad.setDeviceId(rcsr.getSzDeviceID());
                        // FIXME 修改的地方
                        openRemoteVideo();
                    }
                } else if (rcsr.getCode() == RequestChatServiceResponse.REJCTED) {
                    if (!rcsr.getSzSessionID().equals(uad.getSzSessionID())) {
                        V2Log.e(TAG, "REJCTED -- 接收的音视频返回信令与当前正在等待的信令不匹配!");
                        return;
                    }
                    Message.obtain(mHandler, CHAT_CLOSE_LISTNER, uad.getSzSessionID()).sendToTarget();
                }
                // Remove timer
                mHandler.removeCallbacks(timeOutRun);
                currentVideoBean.readSatate = V2GlobalConstants.READ_STATE_READ;
                break;
            case KEY_VIDEO_CONNECTED:
                if (uad.isVideoType()) {
                    // FIXME 新添加的一行，用于接收视频邀请的情况，当收到OnVideoChating表明通话正常的时候才开始打开远端视频
                    openRemoteVideo();
                }
                break;
        }
    }

    @Override
    public void initViewAndListener() {

    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        ToastUtil.ShowToast_long(mContext, "视频连接中断，请重新连接");
        sendErrorMessage("" + ex.getLocalizedMessage() + " " + ex.getMessage());
        finish();
    }

    private void setSurfaceLayoutParams(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            adjustLyoutToSmall(smallWindowVideoLayout);
            if (!isRomoteVideoLandScape) {
                changeSurfaceLayoutParams(mScreenHeight, mScreenWidth, bigWindowVideoLayout);
            } else {
                changeSurfaceLayoutParams(mScreenWidth, mScreenHeight, bigWindowVideoLayout);
            }

        } else {
            adjustLyoutToSmall(smallWindowVideoLayout);
            if (isRomoteVideoLandScape) {
                changeSurfaceLayoutParams(mScreenHeight, mScreenWidth, bigWindowVideoLayout);
            } else {
                changeSurfaceLayoutParams(mScreenWidth, mScreenHeight, bigWindowVideoLayout);
            }
        }
    }

    private void fixSurfaceViewSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        // FIXME 获取到屏幕的宽和高
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mFixSurfaceWidth = mScreenWidth;
        mFixSurfaceHeight = mScreenWidth;
        setBigSurfaceviewWH();
        // 得到标题栏和状态栏的高度
        int statusBarHeight1 = -1;
        // 获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            // 根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        mScreenContentHeight = mScreenHeight - statusBarHeight1;
        Log.i("tv", "statusBarHeight1-" + statusBarHeight1);
        try {
            setVideoConfig();
        } catch (Exception e) {
//            ToastUtil.ShowToast_long(mContext, "视频参数错误");
        }
    }

    public void fixBigSurfaceViewSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        mFixSurfaceWidth = metrics.widthPixels;
        mFixSurfaceHeight = metrics.heightPixels;

        setBigSurfaceviewWH();
    }

    /**
     * 修复旋转摄像头拉伸的问题
     */
    public void setBigSurfaceviewWH() {
        int width, height = 0;
        setPBeans beans = MainApplication.getP();
        if (beans != null) {
            width = beans.getWid();
            height = beans.getHei();
        } else {
            width = 1280;
            height = 720;
        }
        fixLocalCameraViewSize(width, height);
    }

    /**
     * 动态设置视频参数
     */
    private void setVideoConfig() {
        setPBeans beans = null;
        boolean f = DataUtil.getData("MediaEncodeType", mContext) == null;
        if (f) {//服务器上不存在就配置默认值
            beans = setDefultType();

        } else {//取服务器数据
            queryPhoneInfoBean mqueryPhoneInfoBean = (queryPhoneInfoBean) DataUtil.getData("MediaEncodeType", mContext);
            beans = setVideoType(mqueryPhoneInfoBean);
        }
        MainApplication.setP(beans);
        GlobalConfig.setGlobalVideoLevel(mContext, V2GlobalConstants.CONF_CAMERA_CUSTOMER);

    }

    /**
     * 设置默认视频参数
     */
    private setPBeans setDefultType() {
        setPBeans beans = null;
        if (uad.getUser().getAccount().length() != 11) {// 手机对电视
            beans = new setPBeans(1024, 20, 768, 432, 2, 2);
        } else {
            beans = new setPBeans(512, 15, 768, 432, 2, 2);
        }
        return beans;

    }

    /**
     * 设置动态获取的视频参数
     */
    private setPBeans setVideoType(queryPhoneInfoBean mqueryPhoneInfoBean) {
        setPBeans beans = null;
        int malv;
        int zhenlv;
        String resolving;
        if (uad.getUser() == null || TextUtils.isEmpty(uad.getUser().getAccount())) {
            malv = Integer.parseInt(mqueryPhoneInfoBean.getP_p_rate());
            zhenlv = Integer.parseInt(mqueryPhoneInfoBean.getP_p_frame());
            resolving = mqueryPhoneInfoBean.getP_p_resolving();
        } else {
            if (uad.getUser().getAccount().length() != 11) {// 手机对电视
                malv = Integer.parseInt(mqueryPhoneInfoBean.getP_t_rate());
                zhenlv = Integer.parseInt(mqueryPhoneInfoBean.getP_t_frame());
                resolving = mqueryPhoneInfoBean.getP_t_resolving();
            } else {
                malv = Integer.parseInt(mqueryPhoneInfoBean.getP_p_rate());
                zhenlv = Integer.parseInt(mqueryPhoneInfoBean.getP_p_frame());
                resolving = mqueryPhoneInfoBean.getP_p_resolving();
            }
        }

        String[] resolvingArr = resolving.split("\\*");//格式1280*720 要拆分

        beans = new setPBeans(
                malv,
                zhenlv,
                Integer.parseInt(resolvingArr[0]),
                Integer.parseInt(resolvingArr[1]),
                Integer.parseInt(mqueryPhoneInfoBean.getIsYB()) == 1 ? 1 : 2,
                Integer.parseInt(mqueryPhoneInfoBean.getIsYJ()) == 1 ? 1 : 2);
        return beans;

    }

    private void setGlobalState(boolean flag) {
        long uid = flag ? uad.getUser().getmUserId() : 0;
        if (uad.isAudioType()) {
            GlobalHolder.getInstance().setAudioState(flag, uid);
        } else if (uad.isVideoType()) {
            GlobalHolder.getInstance().setVideoState(flag, uid);
            // If clear video state, we must clear voice connect state too.
            if (!flag) {
                GlobalHolder.getInstance().setVoiceConnectedState(false);
            }
        }
    }

    long uid;

    private void buildIntentObject() {
        Intent comingIntent = getIntent();
        boolean mIsInComingCall = comingIntent.getBooleanExtra("is_coming_call", false);
        uid = 0;
        boolean mIsVoiceCall;
        String deviceId = null;
        String sessionID = null;
        uid = comingIntent.getLongExtra("uid", -1);
        mIsVoiceCall = comingIntent.getBooleanExtra("voice", false);
        deviceId = comingIntent.getStringExtra("device");
        // 主叫自动填写一个id，被叫可以直接获得主叫放的id。
        if (mIsInComingCall) {
            sessionID = comingIntent.getStringExtra("sessionID");
        } else {
            sessionID = UUID.randomUUID().toString();
        }
        V2Log.d(TAG, "buildIntentObject uid : " + uid);
        V2Log.d(TAG, "buildIntentObject deviceId : " + deviceId);
        V2Log.d(TAG, "buildIntentObject sessionID : " + sessionID);

        if (!GlobalHolder.getInstance().isFriend(uid)) {
            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, uid);
        }
        User u = GlobalHolder.getInstance().getUser(uid);

        if (u != null) {
            int callType = mIsVoiceCall ? UserChattingObject.VOICE_CALL : UserChattingObject.VIDEO_CALL;
            if (mIsInComingCall) {
                callType |= UserChattingObject.INCOMING_CALL;
            } else {
                callType |= UserChattingObject.OUTING_CALL;
            }
            uad = new UserChattingObject(u, callType, deviceId);
            uad.setSzSessionID(sessionID);
        } else {//u 对象是null 需上传错误日志
            sendErrorMessage("user==null; Line:631" + "---" + ConversationP2PAVActivity.class.getName());
        }
    }


    /**
     * 上传错误
     *
     * @param error_message
     */
    private void sendErrorMessage(String error_message) {

        BussinessManger.getInstance(ConversationP2PAVActivity.this).clientErrorLog(
                GlobalHolder.getInstance().getCurrentUser().getAccount() + "",
                error_message,
                String.valueOf(System.currentTimeMillis()));
    }

    Runnable mUiRunnable = new Runnable() {
        @Override
        public void run() {
            User u = GlobalHolder.getInstance().getUser(uid);
            if (TextUtils.isEmpty(u.getAccount())) {
                mHandler.postDelayed(this, 500);
            } else {
                ((TextView) findViewById(R.id.incall_name)).setText(u.getDisplayName());
                userPortrait(u);
            }
        }
    };

    private void initCallRecord() {
        if (uad == null) {
            ToastUtil.ShowToast_short(mContext, "获取对方信息失败");
            finish();
        }
        // 记录开始通话时间
        startTime = GlobalConfig.getGlobalServerTime();
        currentVideoBean = new VideoBean();
        if (uad.isIncoming()) {
            currentVideoBean.toUserID = GlobalHolder.getInstance().getCurrentUserId();
            if (uad.isSipCall()) {
                currentVideoBean.formUserID = Long.valueOf(uad.getSipNumber());
                currentVideoBean.remoteUserID = Long.valueOf(uad.getSipNumber());
                currentVideoBean.mediaType = AudioVideoMessageBean.TYPE_SIP;
                currentVideoBean.mediaChatID = uad.getSipNumber();
            } else {
                currentVideoBean.formUserID = uad.getUser().getmUserId();
                currentVideoBean.remoteUserID = uad.getUser().getmUserId();
                currentVideoBean.mediaChatID = uad.getSzSessionID();
                if (uad.isAudioType()) {
                    currentVideoBean.mediaType = AudioVideoMessageBean.TYPE_AUDIO;
                } else if (uad.isVideoType()) {
                    currentVideoBean.mediaType = AudioVideoMessageBean.TYPE_VIDEO;
                }
            }
        } else {
            currentVideoBean.formUserID = GlobalHolder.getInstance().getCurrentUserId();
            currentVideoBean.toUserID = uad.getUser().getmUserId();
            currentVideoBean.remoteUserID = uad.getUser().getmUserId();
            currentVideoBean.mediaChatID = uad.getSzSessionID();
            currentVideoBean.mediaType = AudioVideoMessageBean.TYPE_VIDEO;
            uad.setSzSessionID(currentVideoBean.mediaChatID);
        }
    }

    private void init() {
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        chatService.registerCancelledListener(mHandler, CHAT_CLOSE_LISTNER, new Object());
        chatService.registerVideoChatConnectedListener(mHandler, KEY_VIDEO_CONNECTED, null);
        chatService.registerP2PCallResponseListener(mHandler, CHAT_CALL_RESPONSE, null);
        // 检测蓝牙耳机
        if (blueadapter != null && BluetoothProfile.STATE_CONNECTED == blueadapter
                .getProfileConnectionState(BluetoothProfile.HEADSET)) {
            isBluetoothHeadsetConnected = true;
        }
        // initialize phone state listener
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                    V2Log.d(TAG, " the initTelephonyManagerListener onCallStateChanged --> was called hangUp()");
                    hangUp();
                }
            }

        }, PhoneStateListener.LISTEN_CALL_STATE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (uad.isIncoming()) {// 被叫
            setContentView(R.layout.fragment_conversation_incall_video);
            User u;
            if (SecondTab1.RemoteUser == null) {
                mHandler.post(mUiRunnable);
                userPortrait(uad.getUser());
            } else {
                ((TextView) findViewById(R.id.incall_name)).setText(SecondTab1.RemoteUser.getDisplayName());
                u = SecondTab1.RemoteUser;
                userPortrait(u);
            }

            videolHangup = findViewById(R.id.incall_hangup);// 取消通话/挂断
            playRingToneIncoming();
            // 同意接听
            findViewById(R.id.incall_accept).setOnClickListener(videoClickListener);
            videolHangup.setOnClickListener(videoClickListener);
        } else {//主叫
            setContentView(R.layout.fragment_conversation_outcall_video);
            //视频surface布局
            mLocalSurface = (ConferenceSurfaceView) findViewById(
                    R.id.fragment_conversation_outcall_video_local_surface);
            mLocalSurface.setTag(SURFACE_HOLDER_TAG_LOCAL);
            mLocalSurface.setZOrderMediaOverlay(true);
            mLocalSurface.getHolder().addCallback(mLocalCameraSHCallback);
            RelativeLayout bigLayout = (RelativeLayout) findViewById(R.id.rl_big);
            changeSurfaceLayoutParams(bigLayout);
//            openLocalVideo();
//            Log.i("tvliao","主叫init");
            findViewById(R.id.outcall_camera_turn).setOnClickListener(videoClickListener);
            TextView outCallName = (TextView) findViewById(R.id.outcall_name);
            outCallName.setText(uad.getUser().getDisplayName());
            mHandler.postDelayed(timeOutRun, 1000 * 58);//超时提示用了2s
            playRingToneOuting();
            V2Log.d(TAG, "发起一个新的视频邀请 , 等待回应中.... 此次通信的uuid ：" + currentVideoBean.mediaChatID);
            chatService.requestAV2Invite(uad);
            findViewById(R.id.outcall_cancel).setOnClickListener(videoClickListener);
        }
    }

    /**
     * 展示通话对方头像
     */
    private void userPortrait(User connUser) {
        SimpleDraweeView sdvPortrait = (SimpleDraweeView) findViewById(R.id.incall_portrait_sdv);
        String portraitPath = connUser.getmAvatarLocation();
        if (TextUtils.isEmpty(portraitPath) || portraitPath.toLowerCase().equals("null")) {// 服务器图片是否存在【不存则查找本地】
            portraitPath = "res://drawable/" + R.drawable.avatar;// 显示默认头像
        }
        Uri uri = Uri.parse(portraitPath);
        DraweeController incallBkController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(ImageRequestBuilder.newBuilderWithSource(uri).setPostprocessor(postprocessor).build())
                .build();
        SimpleDraweeView incallBk = (SimpleDraweeView) findViewById(R.id.incall_bk_sdv);
        incallBk.setController(incallBkController);
        DraweeController dc = Fresco.newDraweeControllerBuilder().setUri(uri).build();
        GenericDraweeHierarchy gdh = new GenericDraweeHierarchyBuilder(getResources())
                .setPlaceholderImage(R.drawable.avatar)
                .setPlaceholderImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setFailureImage(R.drawable.avatar)
                .setFailureImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .build();
        gdh.setRoundingParams(RoundingParams.asCircle());
        sdvPortrait.setHierarchy(gdh);
        sdvPortrait.setController(dc);
    }

    /**
     * 建立视频连接，初始化控件
     */
    private void initVideoConnect() {
        setContentView(R.layout.fragment_conversation_connected_video);
        // 视频surface布局
        smallWindowVideoLayout = (RelativeLayout) findViewById(R.id.small_window_video_layout);
        smallWindowVideoLayout.setOnTouchListener(sTouch);
        smallWindowVideoLayout.setOnClickListener(videoClickListener);
        mLocalSurface = (ConferenceSurfaceView) findViewById(
                R.id.fragment_conversation_connected_video_local_surface);
        mLocalSurface.setTag(SURFACE_HOLDER_TAG_LOCAL);
        mLocalSurface.setZOrderMediaOverlay(true);
        mLocalSurface.getHolder().addCallback(mLocalCameraSHCallback);
        mRemoteSurface = (ConferenceSurfaceView) findViewById(
                R.id.fragment_conversation_connected_video_remote_surface);
        mRemoteSurface.setTag(SURFACE_HOLDER_TAG_REMOTE);
        mRemoteSurface.getHolder().addCallback(mRemoteVideoHolder);
        bigWindowVideoLayout = (RelativeLayout) findViewById(R.id.big_window_video_layout);
        User u;
        if (SecondTab1.RemoteUser == null) {
            ((TextView) findViewById(R.id.connected_call_name)).setText(uad.getUser().getDisplayName());
            u = uad.getUser();
        } else {
            ((TextView) findViewById(R.id.connected_call_name)).setText(SecondTab1.RemoteUser.getDisplayName());
            u = SecondTab1.RemoteUser;

        }
        ((TextView) findViewById(R.id.connected_call_name)).setText(u.getDisplayName());
        closeLocalVideo();
        openLocalVideo();
        Log.i("tvliao", "建立视频连接init");
        // 计时器
        mTimerTV = (TextView) findViewById(R.id.connected_call_duration);
        Message.obtain(mHandler, UPDATE_TIME).sendToTarget();
        sdvGif = (SimpleDraweeView) findViewById(R.id.connected_gif);
        videoClose = (TextView) findViewById(R.id.connected_video_close);
        gifText = findViewById(R.id.connected_loading_tv);
        isVideoLoading = true;
        playGif(R.drawable.loading_video_gif);
        initConnectedHandleBt();
        initConnectedHandleBt2();
        getScreenWidthAndHeight();
        adjustLyoutToSmall(smallWindowVideoLayout);
        VideoPlayer.setPlayerHandle(new VideoPlayer.PlayerHandle() {
            @Override
            public void playerHandle(boolean startPlayer) {
                if (isVideoLoading) {
                    isVideoLoading = false;
                    mHandler.postDelayed(gifStop, 0);
                }
            }
        });

        try {
            if (!VideoCaptureDevInfo.CreateVideoCaptureDevInfo().isHasCamera()) {
                Toast.makeText(ConversationP2PAVActivity.this, "您的摄像头未打开，对方将看不到您的画面，听不到您的声音", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(ConversationP2PAVActivity.this, "您的摄像头未打开，对方将看不到您的画面，听不到您的声音", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * 初始化视频界通话中操作按钮
     */
    private void initConnectedHandleBt() {
        View CameraTurn = findViewById(R.id.connected_camera_turn);
        CameraTurn.setOnClickListener(videoClickListener);
        btLayout = findViewById(R.id.connected_bt_layout);
        mVideoExpression = (ImageView) findViewById(R.id.connected_button_bq_iv);
        mVideoExpression.setOnClickListener(videoClickListener);
        mVideoPhoto = (ImageView) findViewById(R.id.connected_button_tp_iv);
        mVideoPhoto.setOnClickListener(videoClickListener);
        mVideoMute = (ImageView) findViewById(R.id.connected_button_jy_iv);
        mVideoMuteTv = (TextView) findViewById(R.id.connected_button_jy_tv);
        mVideoMute.setOnClickListener(videoClickListener);
        mVideoHangUp = (ImageView) findViewById(R.id.connected_button_gd_iv);
        mVideoHangUp.setOnClickListener(videoClickListener);
        mVideoCamera = (ImageView) findViewById(R.id.connected_button_sxt_iv);
        mVideoCameraTv = (TextView) findViewById(R.id.connected_button_sxt_tv);
        mVideoCamera.setOnClickListener(videoClickListener);
        mVideoSpeak = (ImageView) findViewById(R.id.connected_button_ysq_iv);
        mVideoSpeakTv = (TextView) findViewById(R.id.connected_button_ysq_tv);
        mVideoSpeak.setOnClickListener(videoClickListener);
        mVideoTheme = (ImageView) findViewById(R.id.connected_button_zt_iv);
        mVideoTheme.setOnClickListener(videoClickListener);
    }

    /**
     * 表情/主题换肤列表/发送图片进度条
     */
    private void initConnectedHandleBt2() {
        //表情
        bqLayout = findViewById(R.id.connected_bq_layout);
        View bqBack = findViewById(R.id.connected_bq_back);
        bqBack.setOnClickListener(videoClickListener);
        View bqqq = findViewById(R.id.connected_bq_qq);
        bqqq.setOnClickListener(gifClickListener);
        View bqdz = findViewById(R.id.connected_bq_dz);
        bqdz.setOnClickListener(gifClickListener);
        View bqxk = findViewById(R.id.connected_bq_xk);
        bqxk.setOnClickListener(gifClickListener);
        View bqdg = findViewById(R.id.connected_bq_dg);
        bqdg.setOnClickListener(gifClickListener);
        View bqxh = findViewById(R.id.connected_bq_xh);
        bqxh.setOnClickListener(gifClickListener);
        //主题换肤
        ztLayout = findViewById(R.id.connected_zt_layout);
        View ztBack = findViewById(R.id.connected_zt_back);
        ztBack.setOnClickListener(videoClickListener);
        View ztJc = findViewById(R.id.connected_zt_jc);
        ztJc.setOnClickListener(themeClickListener);
        View ztHy = findViewById(R.id.connected_zt_hy);
        ztHy.setOnClickListener(themeClickListener);
        View ztSl = findViewById(R.id.connected_zt_sl);
        ztSl.setOnClickListener(themeClickListener);
        // 发送图片进度条
        mViewSending = (ViewGroup) findViewById(R.id.view_sending);
        mViewSendFinish = (ViewGroup) findViewById(R.id.view_send_finish);
        mViewProgress = (ViewGroup) findViewById(R.id.view_pb);
        mImageIcon = (ImageView) findViewById(R.id.image_icon);
        mTextResult = (TextView) findViewById(R.id.text_result);
        mTextCancel = (TextView) findViewById(R.id.text_cancel);
        mTextCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressResult = 3;
                mHandler.post(mProgressGoneRunnable);
            }
        });
        mPb = (ProgressBar) findViewById(R.id.progressBar);
    }

    private boolean reverseIng;
    /**
     * 视频操作各个点击事件
     */
    private OnClickListener videoClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.outcall_cancel://呼出(主叫)取消
                    V2Log.i("TAG", "HangUpButtonOnClickListener was click hangUp!");
                    isRejected = true;
                    isHangupFlag = true;
                    mHandler.removeCallbacks(timeOutRun);
                    hangUp();
                    break;
                case R.id.outcall_camera_turn://切换摄像头
                case R.id.connected_camera_turn:
                    if (reverseIng) {
                        Toast.makeText(mContext, "切换时间间隔不能小于3秒~", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reverseIng = true;
                    reverseLocalCamera();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reverseIng = false;
                        }
                    }, 3000);
                    break;
                case R.id.incall_accept://呼入(被叫)同意
                    isAccepted = true;
                    // Stop ring tone
                    stopRingTone();
                    // set state to connected
                    uad.setConnected(true);
                    headsetAndBluetoothHeadsetHandle(true);
                    chatService.reqeustAV2Accept(uad);
                    if (uad.isVideoType()) {
                        initVideoConnect();
                    }
                    currentVideoBean.startDate = GlobalConfig.getGlobalServerTime();
                    currentVideoBean.mediaState = AudioVideoMessageBean.STATE_ANSWER_CALL;
                    break;
                case R.id.incall_hangup://呼入(被叫)拒接
                    V2Log.i(TAG, "RejectButton was called hangUp!");
                    isRejected = true;
                    isHangupFlag = true;
                    currentVideoBean.mediaState = AudioVideoMessageBean.STATE_ANSWER_CALL;
                    hangUp();
                    break;
                case R.id.small_window_video_layout:
                    if (!uad.isConnected()) {
                        return;
                    }
                    if (isSmallLayoutClick) {
//                        Toast.makeText(ConversationP2PAVActivity.this, "切换时间间隔不能小于3秒", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isSmallLayoutClick = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isSmallLayoutClick = false;
                        }
                    }, 3000);
                    exchangeRemoteVideoAndLocalVideo();
                    break;
                case R.id.connected_button_bq_iv://弹出表情列表
                    btLayout.setVisibility(View.GONE);
                    bqLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.connected_bq_back://收起表情列表
                    bqLayout.setVisibility(View.GONE);
                    btLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.connected_button_tp_iv://通话中发图片
                    if (uad.getUser().getAccount().length() == 11) {
                        ToastUtil.ShowToast_short(ConversationP2PAVActivity.this, "该功能仅支持与电视聊天使用！");
                        return;
                    }
                    Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
                    startActivityForResult(intent, ACTIVITY_PICK_PHOTO);
                    break;
                case R.id.connected_button_jy_iv://通话中静音
                    if (isMuteClose) {
                        uad.setMute(false);
                        isMuteClose = false;
                        mVideoMute.setImageResource(R.drawable.n_mute);
                        mVideoMuteTv.setTextColor(Color.WHITE);
                    } else {
                        uad.setMute(true);
                        isMuteClose = true;
                        mVideoMute.setImageResource(R.drawable.n_mute_off);
                        mVideoMuteTv.setTextColor(Color.parseColor("#F56450"));
                    }
                    chatService.requestAV2UpdateMuteState(uad);
                    break;
                case R.id.connected_button_gd_iv:// 通话中挂断
                    V2Log.i("TAG", "HangUpButtonOnClickListener was click hangUp!");
                    isRejected = true;
                    isHangupFlag = true;
                    mHandler.removeCallbacks(timeOutRun);
                    hangUp();
                    break;
                case R.id.connected_button_sxt_iv://通话中摄像头
                    int videoClose = 1; //摄像头控制，0关闭，1打开
                    if (isOpenedLocal) {
                        closeLocalVideo();
                        mVideoCamera.setImageResource(R.drawable.n_camera_off);
                        mVideoCameraTv.setTextColor(Color.parseColor("#F56450"));
                        mLocalSurface.setVisibility(View.GONE);
                        videoClose = 0;
                    } else {
                        mLocalSurface.setVisibility(View.VISIBLE);
                        openLocalVideo();
                        Log.i("tvliao", "connected_button_sxt_iv");
                        mVideoCamera.setImageResource(R.drawable.n_camera);
                        mVideoCameraTv.setTextColor(Color.WHITE);
                    }
                    JSONObject js = new JSONObject();
                    try {
                        js.put("type", ConstantParams.MESSAGE_TYPE_VIDEO_CLOSE);
                        js.put("data", videoClose);
                        js.put("timeStamp", System.currentTimeMillis());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new MessageSendUtil(mContext).sendMessageToRemote(js.toString(), uad.getUser());
                    break;
                case R.id.connected_button_ysq_iv://通话中扬声器
                    if (v.getTag() == null || v.getTag().equals("speakerphone")) {
                        if (isBluetoothHeadsetConnected) {
                            audioManager.stopBluetoothSco();
                            audioManager.setBluetoothScoOn(false);
                        } else {
                            audioManager.setSpeakerphoneOn(false);
                        }
                        mVideoSpeak.setImageResource(R.drawable.n_speaker_off);
                        mVideoSpeakTv.setTextColor(Color.parseColor("#F56450"));
                        v.setTag("earphone");
                    } else {
                        if (isBluetoothHeadsetConnected) {
                            audioManager.startBluetoothSco();
                            audioManager.setBluetoothScoOn(true);
                        } else {
                            audioManager.setSpeakerphoneOn(true);
                        }
                        mVideoSpeak.setImageResource(R.drawable.n_speaker);
                        mVideoSpeakTv.setTextColor(Color.WHITE);
                        v.setTag("speakerphone");
                    }
                    break;
                case R.id.connected_button_zt_iv://弹出主题换肤列表
                    if (uad.getUser().getAccount().length() == 11) {
                        ToastUtil.ShowToast_short(ConversationP2PAVActivity.this, "该功能仅支持与电视聊天使用！");
                        return;
                    }
                    btLayout.setVisibility(View.GONE);
                    ztLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.connected_zt_back://收起主题换肤列表
                    ztLayout.setVisibility(View.GONE);
                    btLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    /**
     * 表情点击事件
     */
    private OnClickListener gifClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isVideoLoading) {//远端视频正在加载，播放加载中gif
                return;
            }
            if (isStartAnimIng) {
                Toast.makeText(mContext, "两次发送时间间隔不能小于3秒~", Toast.LENGTH_LONG).show();
                return;
            }
            int sendGifNum = 0;
            switch (view.getId()) {
                case R.id.connected_bq_dg:
                    playGif(R.drawable.gif_dg);
                    sendGifNum = 0;
                    break;
                case R.id.connected_bq_xh:
                    playGif(R.drawable.gif_xh);
                    sendGifNum = 1;
                    break;
                case R.id.connected_bq_qq:
                    playGif(R.drawable.gif_qq);
                    sendGifNum = 2;
                    break;
                case R.id.connected_bq_xk:
                    playGif(R.drawable.gif_xk);
                    sendGifNum = 3;
                    break;
                case R.id.connected_bq_dz:
                    playGif(R.drawable.gif_dz);
                    sendGifNum = 4;
                    break;
            }
            BussinessManger.getInstance(mContext)
                    .notifyTv(ConstantParams.MESSAGE_TYPE_MULTI,
                            sendGifNum, -1, -1l, uad.getUser().getmUserId());
        }
    };

    /**
     * 主题点击事件
     */
    private OnClickListener themeClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (themeIng) {
                Toast.makeText(ConversationP2PAVActivity.this, "两次发送时间间隔不能小于3秒~", Toast.LENGTH_LONG).show();
                return;
            }
            themeIng = true;
            int sendThemeNum = 0;
            switch (v.getId()) {
                case R.id.connected_zt_jc://剧场
                    sendThemeNum = 0;
                    break;
                case R.id.connected_zt_hy://海洋
                    sendThemeNum = 1;
                    break;
                case R.id.connected_zt_sl://森林
                    sendThemeNum = 2;
                    break;
            }
            BussinessManger.getInstance(mContext)
                    .notifyTv(ConstantParams.MESSAGE_TYPE_THEME,
                            -1, sendThemeNum, -1l, uad.getUser().getmUserId());
            mHandler.postDelayed(themeRun, 3000);
        }
    };
    private Runnable themeRun = new Runnable() {
        @Override
        public void run() {
            themeIng = false;
        }
    };

    private boolean themeIng;
    private boolean isStartAnimIng;

    private void playGif(int resId) {
        if (sdvGif == null) {
            return;
        }
        isStartAnimIng = true;
        Uri uri = Uri.parse("res://drawable/" + resId);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        sdvGif.setController(controller);
        sdvGif.setVisibility(View.VISIBLE);
        if (isVideoLoading) return;//如果是视频加载播放gif不执行播放3s停止
        mHandler.postDelayed(gifStop, 3000);
    }

    private Runnable gifStop = new Runnable() {

        @Override
        public void run() {
            Animatable am = sdvGif.getController().getAnimatable();
            if (am != null && am.isRunning()) {
                am.stop();
            }
            if (gifText.getVisibility() == View.VISIBLE) {
                gifText.setVisibility(View.GONE);
            }
            isStartAnimIng = false;
            sdvGif.setVisibility(View.GONE);
        }
    };

    /**
     * FIXME optimze code
     */
    private void disableAllButtons() {

        if (mVideoHangUp != null) {
            mVideoHangUp.setEnabled(false);
        }

        if (mVideoPhoto != null) {
            mVideoPhoto.setEnabled(false);
        }

        if (mVideoMute != null) {
            mVideoMute.setEnabled(false);
        }
        if (mVideoCamera != null) {
            mVideoCamera.setEnabled(false);
        }
        if (mVideoSpeak != null) {
            mVideoSpeak.setEnabled(false);
        }
        TextView incomingVideoCallTitle = (TextView) findViewById(R.id.fragment_conversation_video_title);
        if (incomingVideoCallTitle != null) {
            incomingVideoCallTitle.setText(R.string.conversation_end);
        }

        // If is incoming layout, no mTimerTV view
        if (mTimerTV != null) {
            mTimerTV.setText(R.string.conversation_end);
        }
        mTimeLine = -1;
    }

    private void openRemoteVideo() {
        SurfaceHolder holder = mRemoteSurface.getHolder();
        Log.e("remoteHolder", holder.toString());
        openRemoteVideo(holder);
    }

    private void openRemoteVideo(SurfaceHolder holder) {
        if (isStoped) {
            return;
        }

        if (isOpenedRemote) {
            return;
        }

        if (!isOpenedLocal) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    openRemoteVideo();
                }
            }, 1000);
        }
        // FIXME 修改的地方 这里给VideoPlayer对象添加底层每一次开始重新创建解码器并开始绘制的监听器
        // 具体监听的是OnPlayVideo(int datalen)函数
        VideoPlayer.DisplayRotation = GlobalConfig.getDisplayRotation(this);
        mRmoteVideoPlayer = uad.getVp();
        if (mRmoteVideoPlayer == null) {
            mRmoteVideoPlayer = new VideoPlayer();
            mRmoteVideoPlayer.setRemoteVideoPlayCallBack(new RemoteVideoPlayCallBack() {

                @Override
                public void receiveNewPlayParams(final int width,
                                                 final int height, boolean isCallByJNI) {
                    V2Log.d(TAG, "receiveNewPlayParams " + width + " "
                            + height);
                    mRemoteVideoWidth = width;
                    mRemoteVideoHeight = height;
                    if (VideoPlayer.GetMediaCodecEnabled()) {
                        if (isCallByJNI) {
                            mRmoteVideoPlayer.isPausePlay = true;
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            changeSurfaceLayoutParams(width, height, bigWindowVideoLayout, true);
                        }
                    });
                }

            });
            uad.setVp(mRmoteVideoPlayer);
        }
        mRmoteVideoPlayer.setSuspended(false);
        if (uad.getDeviceId() == null || uad.getDeviceId().isEmpty()) {
            V2Log.e(TAG, "openRemoteVideo --> No P2P remote device Id");
            return;
        }
        isOpenedRemote = true;
        V2Log.i(TAG, "openRemoteVideo --> Successfully opend remote video!");
        mRmoteVideoPlayer.setSurfaceHolder(holder);
        chatService.requestOpenVideoDevice(uad.getUdc(), null);
    }

    private void closeRemoteVideo() {
        if (uad.getVp() != null && uad.getDeviceId() != null) {
            chatService.requestCloseVideoDevice(uad.getUdc(), null);
        }
        isOpenedRemote = false;
    }

    /**
     * 本地视频布局与远端视频布局大小切换 (硬解)
     *
     * @param width
     * @param height
     * @param bigLayout
     * @param isChangePause
     */
    private void changeSurfaceLayoutParams(int width, int height, ViewGroup bigLayout, boolean isChangePause) {
        // FIXME 每当远端旋转了角度，通过该函数来调整surfaceview的大小,但如果现在大屏是本地，则不用
        if (!testFlag || width == 0 || height == 0) {
            if (isChangePause) {
                mRmoteVideoPlayer.isPausePlay = false;
            }
            return;

        }
        changeSurfaceLayoutParams(width, height, bigLayout);
        if (isChangePause) {
            mRmoteVideoPlayer.isPausePlay = false;
        }
    }

    private void changeSurfaceLayoutParams(ViewGroup bigLayout) {
        RelativeLayout.LayoutParams surfaceLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bigLayout.getChildAt(0).setLayoutParams(surfaceLP);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mFixSurfaceWidth,
                mFixSurfaceHeight);
        params.gravity = Gravity.CENTER;
        bigLayout.setLayoutParams(params);
        bigLayout.setTag(null);
    }

    private void changeSurfaceLayoutParams(int width, int height, ViewGroup bigLayout) {
        boolean flagOreation = false;// 横屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            flagOreation = false;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            flagOreation = true;
        }
        if (bigLayout != null) {
            int targetWidth;
            int targetHeight;
            if (flagOreation) {// 手机竖屏
                double widthRatio = mScreenWidth / width;
                double heightRatio = mScreenHeight / height;
                double ratio;
                ratio = (double) width / height;
                if (widthRatio < heightRatio) {// 视频宽大于高
                    targetWidth = mScreenWidth;
                    targetHeight = (int) (targetWidth / ratio);
                } else {// 视频高大于宽
                    targetHeight = mScreenHeight;
                    targetWidth = (int) (targetHeight * ratio);
                }
            } else {// 手机横屏
                double widthRatio = mScreenHeight / width;//横屏手机的宽高不变，但视频宽高倒置
//				double heightRatio = mScreenHeight / height;
                double heightRatio = mScreenWidth / height;
                double ratio;
                ratio = (double) width / height;
                if (widthRatio < heightRatio) {
                    targetWidth = mScreenWidth;
                    targetHeight = (int) (targetWidth / ratio);
                } else {
                    targetHeight = mScreenHeight;
                    targetWidth = (int) (targetHeight * ratio);
                }
            }

            RelativeLayout.LayoutParams surfaceLP = (RelativeLayout.LayoutParams) bigLayout.getChildAt(0)
                    .getLayoutParams();

            try {
                if (surfaceLP != null) {
                    if (flagOreation && !isRomoteVideoLandScape && targetWidth < mScreenWidth) {
                        surfaceLP.width = mScreenWidth + 100;
                        surfaceLP.height = targetHeight;
                    } else {
                        surfaceLP.width = targetWidth;
                        surfaceLP.height = targetHeight;
                    }

                    surfaceLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    bigLayout.getChildAt(0).setLayoutParams(surfaceLP);

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER;
                    bigLayout.setLayoutParams(params);
                    bigLayout.setTag(null);
                }
            } catch (Exception e) {
                System.out.println("");
            }
        }
    }

    private void adjustLyoutToSmall(ViewGroup smallLayout) {
        android.widget.FrameLayout.LayoutParams localLp = (android.widget.FrameLayout.LayoutParams) smallLayout
                .getLayoutParams();
        if (localLp != null) {
            localLp.width = screenWidth / 5;
            localLp.height = screenHeight / 5;
            smallLayout.setLayoutParams(localLp);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);

            smallLayout.getChildAt(0).setLayoutParams(params);
        }

    }

    /**
     * 挂断通话
     */
    private void hangUp() {
        if (isHangUping) {
            return;
        }
        V2Log.d(TAG, "the hangUp() was invoking ! , and change to HANG_UP_NOTIFICATION");
        // Stop ring tone

        notifyTv(ConstantParams.MESSAGE_TYPE_HANGHP);
        if (uad.isSipCall()) {
            Message.obtain(mHandler, CHAT_CLOSE_LISTNER, uad.getSipNumber()).sendToTarget();
        } else {
            Message.obtain(mHandler, CHAT_CLOSE_LISTNER, uad.getSzSessionID()).sendToTarget();
        }
    }

    // 通知第三方挂断
    private void notifyTv(int type) {

        if (SecondTab1.RemoteUser == null)
            return;
        JSONObject json = new JSONObject();

        String fromId = String.valueOf(SecondTab1.RemoteUser.getmUserId());
        fromId = fromId.substring(2, fromId.length());
        try {
            json.put("type", type);
            json.put("result", 0);
            json.put("fromID", fromId);
            json.put("toID", SecondTab1.RemoteUser.getmUserId());
            json.put("timeStamp", System.currentTimeMillis() + "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        new MessageSendUtil(mContext).sendMessageToRemote(json.toString(), SecondTab1.RemoteUser);
    }

    @SuppressWarnings("deprecation")
    private void headsetAndBluetoothHeadsetHandle(boolean isInit) {
        if (uad == null || audioManager == null) {
            return;
        }

        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (audioManager.isWiredHeadsetOn()) {
            audioManager.setSpeakerphoneOn(false);
            V2Log.i(TAG, "切换到了有线耳机");

        } else if (isBluetoothHeadsetConnected && !audioManager.isBluetoothA2dpOn()) {
            SystemClock.sleep(500);
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(false);
                V2Log.i(TAG, "切换到SCO链路蓝牙耳机 ， 扬声器关闭");
            } else {
                V2Log.i(TAG, "切换到SCO链路蓝牙耳机 ， 扬声器已经关闭");
            }
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);

        } else if (isBluetoothHeadsetConnected && audioManager.isBluetoothA2dpOn()) {
            SystemClock.sleep(500);
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(false);
                V2Log.i(TAG, "切换到了ACL链路的A2DP蓝牙耳机 ， 扬声器关闭");
            } else {
                V2Log.i(TAG, "切换到了ACL链路的A2DP蓝牙耳机 ， 扬声器已经关闭");
            }

            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();

        } else {
            if (uad.isVideoType()) {
                audioManager.setSpeakerphoneOn(true);
                V2Log.i(TAG, "VIDEO 切换到了外放");
            }
        }
    }

    private void playRingToneOuting() {
        if (uad.isSipCall()) {
            return;
        }

        if (mPlayer == null)
            mPlayer = MediaPlayer.create(mContext, R.raw.outing_ring_tone_1);
        mPlayer.setLooping(true);
        if (!mPlayer.isPlaying())
            mPlayer.start();
    }

    private void stopRingToneOuting() {
        if (uad.isSipCall()) {
            return;
        }

        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    private void playRingToneIncoming() {
        if (mPlayer == null)
            mPlayer = MediaPlayer.create(mContext, R.raw.outing_ring_tone_1);
        mPlayer.setLooping(true);
        if (!mPlayer.isPlaying())
            mPlayer.start();
    }

    private void stopRingTone() {
        if (uad.isSipCall()) {
            return;
        }

        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    private boolean isHangupFlag;

    private Runnable timeOutRun = new Runnable() {
        @Override
        public void run() {
            V2Log.d(TAG, "the mTimeOutMonitor invoking hangUp()");
            Toast.makeText(mContext, "无人接听，请稍后再拨", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isHangupFlag = true;
                    hangUp();
                }
            }, 2000);
        }
    };

    private class RemoteVideoHolderSHCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int flag, int width,
                                   int height) {
            V2Log.d(TAG, " RemoteVideo surfaceChanged -> isStoped : "
                    + isStoped + " | isOpenedRemote : " + isOpenedRemote);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            V2Log.d(TAG, "RemoteVideo surfaceCreated -> isStoped : " + isStoped
                    + " | isOpenedRemote : " + isOpenedRemote);
            if (VideoPlayer.GetMediaCodecEnabled()) {
                //if (uad.getVp() != null && uad.getVp().isPausePlay) {
                //    V2Log.d(TAG, "surfaceCreated flag false holder : " + holder);
                //    uad.getVp().setSurfaceHolder(holder);
                //    uad.getVp().startVideoPlay(mRemoteVideoWidth,
                //            mRemoteVideoHeight);
                //    uad.getVp().isPausePlay = false;
                //}
            } else {
                if (uad.getVp() != null) {
                    uad.getVp().setSuspended(false);
                }
            }

            if (uad.isConnected() && !isOpenedRemote) {
                openRemoteVideo(holder);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            V2Log.d(TAG, "RemoteVideo surfaceDestroyed -> isStoped : "
                    + isStoped + " | isOpenedRemote : " + isOpenedRemote);
            V2Log.d(TAG, "surfaceDestroyed flag false holder : " + holder);
            if (VideoPlayer.GetMediaCodecEnabled()) {
                //if (uad.getVp() != null) {
                //    uad.getVp().isPausePlay = true;
                //    uad.getVp().StopVideoPlay();
                //}
            } else {
                uad.getVp().setSuspended(true);
            }

            if (uad.isConnected() && isOpenedRemote) {
                closeRemoteVideo();
            }
        }

    }

    private class LocalCameraSHCallback implements SurfaceHolder.Callback {
        @Override

        public void surfaceChanged(SurfaceHolder holder, int flag, int width,
                                   int height) {
            V2Log.d(TAG, "LocalCamera surfaceChanged -> isStoped : " + isStoped
                    + " | isOpenedLocal : " + isOpenedLocal);
            if (!isStoped && !isOpenedLocal) {
                openLocalVideo(holder);
                Log.i("tvliao", "surfaceChanged-openLocalVideo");
            }
        }


        @SuppressWarnings("deprecation")
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            V2Log.d(TAG, "LocalCamera surfaceCreated -> isStoped : " + isStoped
                    + " | isOpenedLocal : " + isOpenedLocal);
            if (isStoped) {
                return;
            }

            if (isOpenedLocal) {
                closeLocalVideo();
            }
// when conversation is connected or during outing call
            if (uad.isConnected() || !uad.isIncoming()) {
                V2Log.d(TAG, "Create new holder " + holder);
//                openLocalVideo(holder);
                Log.i("tvliao", "surfaceCreated-openLocalVideo");
            }
        }


        @Override

        public void surfaceDestroyed(SurfaceHolder holder) {
            V2Log.d(TAG, "LocalCamera surfaceDestroyed -> isStoped : "
                    + isStoped + " | isOpenedLocal : " + isOpenedLocal);
        }

    }

    //获取屏幕的高度和宽度以及图片的高度和宽度
    private int screenWidth, screenHeight, ImWidth, ImHeight;

    //得到原图的高度和宽度
    private void getImageWidthAndHeight(String path) {
        try {
            ExifInterface exifInterfece = new ExifInterface(path);
            ImWidth = exifInterfece.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            ImHeight = exifInterfece.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据SD卡路径加载图片的大小比例压缩
     */
    public Bitmap getImageByScaleSize(String path) {
        int scaleSize = 1;//1就表示不压缩
        BitmapFactory.Options options = new BitmapFactory.Options();
     /*  options.inJustDecodeBounds=true;//只读取图片的信息，不读取图片的具体数据
            ImWidth = options.outWidth;
            ImHeight = options.outHeight;*/
        getImageWidthAndHeight(path);//得到图片的高度和宽度
        if (ImWidth > ImHeight && ImWidth > screenWidth) {
            scaleSize = (int) (ImWidth * 1.0f / screenWidth + 0.5f);//加0.5是为了四舍五入，取一个很好的精度
        } else if (ImHeight > ImWidth && ImHeight > screenHeight) {
            scaleSize = (int) (ImHeight * 1.0f / screenHeight + 0.5f);
        } else {//其他情况表示，就是当是横向或者纵向图片时，它的长度和宽度都大于屏幕
            scaleSize = (int) (ImWidth * 1.0f / screenWidth + ImHeight * 1.0f / screenHeight + 0.5f) / 2;
        }
        //设置图片的采样率
        options.inSampleSize = scaleSize;//针对不同的手机分辨率，设置的缩放比也不一样，这里的值可能是不一样的
        return BitmapFactory.decodeFile(path, options);
    }

    private void getScreenWidthAndHeight() {
//        DisplayMetrics metrics = new DisplayMetrics();
//        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        manager.getDefaultDisplay().getMetrics(metrics);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

//        screenWidth = metrics.widthPixels;
//        screenHeight = metrics.heightPixels;
//        if (screenHeight > screenWidth) {
//            int temp = screenHeight;
//            screenHeight = screenWidth;
//            screenWidth = temp;
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_PICK_PHOTO:
                if (data != null) {
                    final String filePath = data.getStringExtra("checkedImage");
                    if (filePath == null) {
                        V2Toast.makeText(mContext, R.string.error_contact_messag_invalid_image_path, V2Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    WaitDialogBuilder.showNormalWithHintProgress(mContext, "图片加载中...");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap finalBm = getImageByScaleSize(filePath);
                            mHandler.post(new Runnable() {
                                public void run() {
                                    WaitDialogBuilder.dismissDialog();

                                    BussinessManger.getInstance(mContext).mFileUpload(finalBm, "", new IBussinessManager.FileUploadCallBack() {

                                        @Override
                                        public void onStart(final int max) {
                                            mProgress = 0;
                                            mProgressResult = 0;
                                            mHandler.post(mProgressBarRunnable);
                                        }

                                        @Override
                                        public void onSuccess(final String dataString) {
                                            mProgress = 100;
                                            if (mProgressResult != 3) {
                                                ConversationP2PAVActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        BussinessManger.getInstance(ConversationP2PAVActivity.this)
                                                                .notifyTv(ConstantParams.MESSAGE_TYPE_PICTURE,
                                                                        -1, -1, -1l, uad.getUser().getmUserId(), dataString);
                                                    }
                                                });
                                            }
                                            BitmapUtil.recycled(finalBm);
                                        }

                                        @Override
                                        public void onFailed(String msg) {
                                            mProgressResult = 2;
                                            BitmapUtil.recycled(finalBm);
                                        }

                                        @Override
                                        public void onUpload(final int progress) {
                                        }
                                    });
                                }
                            });
                        }
                    }).start();

                }
                break;
        }
    }

    Runnable mProgressBarRunnable = new Runnable() {
        @Override
        public void run() {

            if (mProgress == 100) {
                mPb.setProgress(mProgress);
                mProgressResult = 1;
                mHandler.postDelayed(mProgressResultRunnable, 1000);

            } else if (mProgress < 85) {
                if (mProgress == 0) {
                    mViewProgress.setVisibility(View.VISIBLE);
                    mViewSendFinish.setVisibility(View.GONE);
                    mViewSending.setVisibility(View.VISIBLE);
                }

                mProgress += new Random(System.currentTimeMillis()).nextInt(10);
                mPb.setProgress(mProgress);
                mHandler.postDelayed(mProgressBarRunnable, 1000);
            }
        }
    };

    Runnable mProgressResultRunnable = new Runnable() {
        @Override
        public void run() {
            if (mProgressResult == 2) {
                mViewSending.setVisibility(View.GONE);
                mViewSendFinish.setVisibility(View.VISIBLE);

                mTextResult.setText("发送失败");
                mImageIcon.setImageResource(R.drawable.ic_send_fail);

                mHandler.postDelayed(mProgressGoneRunnable, 2000);
            } else if (mProgressResult == 1) {
                mViewSending.setVisibility(View.GONE);
                mViewSendFinish.setVisibility(View.VISIBLE);

                mTextResult.setText("发送成功");
                mImageIcon.setImageResource(R.drawable.ic_send_succ);

                mHandler.postDelayed(mProgressGoneRunnable, 2000);
            }
        }
    };

    Runnable mProgressGoneRunnable = new Runnable() {
        @Override
        public void run() {
            mViewProgress.setVisibility(View.GONE);
        }
    };

    public void reverseLocalCamera() {
        if (!VideoCaptureDevInfo.CreateVideoCaptureDevInfo().isHasCamera()) {
            Toast.makeText(ConversationP2PAVActivity.this, "您的摄像头权限未打开", Toast.LENGTH_LONG).show();
            return;
        }
        closeLocalVideo();
        if (VideoCaptureDevInfo.CreateVideoCaptureDevInfo().reverseCamera()) {
            chatService.updateCameraParameters(new CameraConfiguration(new VideoPlayer()), null);
        }
        openLocalVideo();
        Log.i("tvliao", "reverseLocalCamera");
    }

    public void openLocalVideo() {
        openLocalVideo(mLocalSurface.getHolder());
    }

    public void openLocalVideo(SurfaceHolder holder) {
        if (isStoped) {
            return;
        }
        if (mLocalSurface == null) {
            return;
        }
        if (isOpenedLocal) {
            return;
        }
        isOpenedLocal = true;
        VideoRecorder.mDisplay = getWindow().getWindowManager().getDefaultDisplay();
        GlobalConfig.setVideoRecordParams(this, holder);
        UserChattingObject selfUCD = new UserChattingObject(GlobalHolder.getInstance().getCurrentUser(), 0, "");
        chatService.requestOpenVideoDevice(selfUCD.getUdc(), null);
    }

    public void closeLocalVideo() {
        UserChattingObject selfUCD = new UserChattingObject(GlobalHolder.getInstance().getCurrentUser(), 0, "");
        chatService.requestCloseVideoDevice(selfUCD.getUdc(), null);
        isOpenedLocal = false;
    }

    float x;

    /**
     * 小窗口拖拽
     */
    private OnTouchListener sTouch = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            Log.i("tv", "action-" + action);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getRawX();
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                /**
                 * layout(l,t,r,b) l Left position, relative to parent t Top
                 * position, relative to parent r Right position, relative to
                 * parent b Bottom position, relative to parent
                 */
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;

                    int left = v.getLeft() + dx;
                    int top = v.getTop() + dy;
                    int right = v.getRight() + dx;
                    int bottom = v.getBottom() + dy;
                    if (left < 0) {
                        left = 0;
                        right = left + v.getWidth();
                    }
                    if (right > screenWidth) {
                        right = screenWidth;
                        left = right - v.getWidth();
                    }
                    if (top < 0) {
                        top = 0;
                        bottom = top + v.getHeight();
                    }
                    if (bottom > mScreenContentHeight) {
                        bottom = mScreenContentHeight;
                        top = bottom - v.getHeight();
                    }
                    v.layout(left, top, right, bottom);
                    Log.i("tv", left + ", " + top + ", " + right + ", " + bottom);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float x2 = event.getRawX();
                    if (Math.abs(x - x2) < 10) {
                        return false;// 距离较小，当作click事件来处理
                    }
                    if (Math.abs(x - x2) > 60) {
                        return true;
                    }
                    break;
            }
            return false;
        }

    };

    /**
     * 图片虚化处理
     */
    Postprocessor postprocessor = new BasePostprocessor() {
        @Override
        public String getName() {
            return "blurPostprocessor";
        }

        @Override
        public void process(Bitmap bitmap) {

            int radius = 10;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int temp = 256 * divsum;
            int dv[] = new int[temp];
            for (i = 0; i < temp; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                            | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        }
    };
    //    private void themeUpdate(int themeType) {
//        LocalSharedPreferencesStorage.putIntValue(mContext, "p2p_theme", themeType);
//        switch (themeType) {
//            case ConstantParamsTheme.MESSAGE_CHANGE_THEME_0:
//                ivZtLeft.setImageResource(R.drawable.n_zt_1_left);
//                ivZtRight.setImageResource(R.drawable.n_zt_1_right);
//                break;
//            case ConstantParamsTheme.MESSAGE_CHANGE_THEME_1:
//                ivZtLeft.setImageResource(R.drawable.n_zt_2_left);
//                ivZtRight.setImageResource(R.drawable.n_zt_2_right);
//                break;
//            case ConstantParamsTheme.MESSAGE_CHANGE_THEME_2:
//                ivZtLeft.setImageResource(R.drawable.n_zt_3_left);
//                ivZtRight.setImageResource(R.drawable.n_zt_3_right);
//                break;
//        }
//    }

    /**
     * 本地视频窗口与远端视频窗口相互切换
     */
    private void exchangeRemoteVideoAndLocalVideo() {
        //if (VideoPlayer.GetMediaCodecEnabled()) {
        //    exchangeRemoteAndLocalVideoHardware();
        //} else {
        exchangeRemoteAndLocalVideoSoft();
        //}
    }


    /**
     * Soft decoder , this function is used to switching remote and local
     * SurfaceView dispaly location.
     */
    private void exchangeRemoteAndLocalVideoSoft() {
        ViewGroup remoteSurfaceParent = (ViewGroup) mRemoteSurface.getParent();
        if (remoteSurfaceParent != null) {
            remoteSurfaceParent.removeViewInLayout(mRemoteSurface);
        }

        ViewGroup localSurfaceParent = (ViewGroup) mLocalSurface.getParent();
        if (localSurfaceParent != null) {
            localSurfaceParent.removeViewInLayout(mLocalSurface);
        }
        if (testFlag) {
            changeSurfaceSizeParams(mRemoteSurface, mLocalSurface);
            testFlag = false;
            mLocalSurface.setZOrderMediaOverlay(false);
            bigWindowVideoLayout.addView(mLocalSurface);

            mRemoteSurface.setZOrderMediaOverlay(true);
            smallWindowVideoLayout.addView(mRemoteSurface);
            mHandler.sendEmptyMessage(UPDATE_VIDEO_BYOWNER);
        } else {
            changeSurfaceSizeParams(mLocalSurface, mRemoteSurface);
            testFlag = true;
            mRemoteSurface.setZOrderMediaOverlay(false);
            bigWindowVideoLayout.addView(mRemoteSurface);

            mLocalSurface.setZOrderMediaOverlay(true);
            smallWindowVideoLayout.addView(mLocalSurface);
        }
    }

    /**
     * Hard decoder , this function is used to switching remote and local
     * SurfaceView dispaly location.
     */
    /**
     * Hard decoder , this function is used to switching remote and local
     * SurfaceView dispaly location.
     */
    private void exchangeRemoteAndLocalVideoHardware() {
        ViewGroup remoteSurfaceParent = (ViewGroup) mRemoteSurface.getParent();
        if (remoteSurfaceParent != null) {
            remoteSurfaceParent.removeViewInLayout(mRemoteSurface);
        }
        ViewGroup localSurfaceParent = (ViewGroup) mLocalSurface.getParent();
        if (localSurfaceParent != null) {
            localSurfaceParent.removeViewInLayout(mLocalSurface);
        }
        mLocalSurface = new ConferenceSurfaceView(mContext);
        mLocalSurface.setTag(SURFACE_HOLDER_TAG_LOCAL);
        mLocalSurface.setZOrderMediaOverlay(true);

        mRemoteSurface = new ConferenceSurfaceView(mContext);
        mRemoteSurface.setTag(SURFACE_HOLDER_TAG_REMOTE);
        if (testFlag) {
            fixBigSurfaceViewSize();
            Log.i("tvliao", "mFixSurfaceHeight-" + mFixSurfaceHeight + "--" + mFixSurfaceWidth);
            testFlag = false;
            RelativeLayout.LayoutParams mRemoteParams = new RelativeLayout.LayoutParams(mFixSurfaceWidth,
                    mFixSurfaceHeight);
            mRemoteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mRemoteSurface.setLayoutParams(mRemoteParams);

            mLocalSurface.getHolder().addCallback(mRemoteVideoHolder);
            mRemoteSurface.getHolder().addCallback(mLocalCameraSHCallback);

            smallWindowVideoLayout.addView(mLocalSurface);
            bigWindowVideoLayout.addView(mRemoteSurface);
        } else {
            testFlag = true;
            mLocalSurface.getHolder().addCallback(mLocalCameraSHCallback);
            mRemoteSurface.getHolder().addCallback(mRemoteVideoHolder);

            smallWindowVideoLayout.addView(mLocalSurface);
            bigWindowVideoLayout.addView(mRemoteSurface);
            changeSurfaceLayoutParams(mRemoteVideoWidth, mRemoteVideoHeight, bigWindowVideoLayout, false);
        }
    }

    private void changeSurfaceSizeParams(View smallSurface, View bigSurface) {
        int smallWidth;
        int smallHeight;
        RelativeLayout.LayoutParams smallSurfaceLP = (RelativeLayout.LayoutParams) smallSurface.getLayoutParams();
        if (smallSurfaceLP != null) {
            if (GlobalConfig.PROGRAM_IS_PAD) {
                smallWidth = (int) getResources()
                        .getDimension(R.dimen.fragment_conversation_connected_local_video_layout_width);
                smallHeight = (int) getResources()
                        .getDimension(R.dimen.fragment_conversation_connected_local_video_layout_height);
            } else {
                smallHeight = (int) getResources()
                        .getDimension(R.dimen.fragment_conversation_connected_local_video_layout_width);
                smallWidth = (int) getResources()
                        .getDimension(R.dimen.fragment_conversation_connected_local_video_layout_height);
            }
            smallSurfaceLP.width = smallWidth;
            smallSurfaceLP.height = smallHeight;
            smallSurface.setLayoutParams(smallSurfaceLP);
        }

        RelativeLayout.LayoutParams surfaceLP = (RelativeLayout.LayoutParams) bigSurface.getLayoutParams();
        if (surfaceLP != null) {
            surfaceLP.width = mFixSurfaceWidth;
            surfaceLP.height = mFixSurfaceHeight;
            surfaceLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            bigSurface.setLayoutParams(surfaceLP);
        }
    }

    public void fixLocalCameraViewSize(int videoWidth, int videoHeight) {
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_PORTRAIT) {
            int temp = mScreenWidth;
            mScreenWidth = mScreenHeight;
            mScreenHeight = temp;
        }
        V2Log.i(TAG, "screen width : " + mScreenWidth + " | screen height : " + mScreenHeight);
        V2Log.i(TAG, "videoWidth : " + videoWidth + " | videoHeight : " + videoHeight);
        float bit = (float) videoWidth / (float) videoHeight;
        float targetHeight = mScreenWidth / bit;
        float targetWidth = mScreenHeight * bit;
        if (targetHeight >= mScreenHeight) {
            mFixSurfaceWidth = mScreenWidth;
            mFixSurfaceHeight = (int) targetHeight;
        }

        if (targetWidth >= mScreenWidth) {
            mFixSurfaceWidth = (int) targetWidth;
            mFixSurfaceHeight = mScreenHeight;
        }

        if (ori == Configuration.ORIENTATION_PORTRAIT) {
            int temp = mFixSurfaceWidth;
            mFixSurfaceWidth = mFixSurfaceHeight;
            mFixSurfaceHeight = temp;
        }
        V2Log.i(TAG, "mFixSurfaceWidth : " + mFixSurfaceWidth + " | mFixSurfaceHeight : " + mFixSurfaceHeight);
    }

//    private void fixSurfaceView(final int width, final int height) {
//
//        //		if (width < height) {
//        //			isRomoteVideoLandScape = true;
//        //			hasSizeChange1=true;
//        //		} else {
//        //			isRomoteVideoLandScape = false;
//        //			hasSizeChange1=false;
//        //		}
//        //防止软解重复调用创建视频窗口的函数
//        //		if(hasSizeChange2==hasSizeChange1&&MainApplication.getP().getF2()==2)  return;
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                changeSurfaceLayoutParams(width, height, bigWindowVideoLayout, true);
//                //				hasSizeChange2=hasSizeChange1;
//
//            }
//        });
//    }
}