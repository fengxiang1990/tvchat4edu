package com.bizcom.vc.hg.ui.edu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.V2.jni.AppShareRequest;
import com.V2.jni.VideoMixerRequest;
import com.V2.jni.ind.V2ConfSyncVideoJNIObject;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.bo.UserStatusObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.V2ChatRequest;
import com.bizcom.request.V2ConferenceRequest;
import com.bizcom.request.V2DocumentRequest;
import com.bizcom.request.jni.PermissionRequestIndication;
import com.bizcom.request.jni.PermissionUpdateIndication;
import com.bizcom.request.jni.RequestCloseUserVideoDeviceResponse;
import com.bizcom.request.jni.RequestConfChairManChange;
import com.bizcom.request.util.AsyncResult;
import com.bizcom.request.util.ConferencMessageSyncService;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.AnimationHepler;
import com.bizcom.util.DensityUtils;
import com.bizcom.util.DialogManager;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.OrderedHashMap;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.conference.ConferenceMsgDialog;
import com.bizcom.vc.activity.conference.ConferenceSurfaceView;
import com.bizcom.vc.activity.conference.LeftAttendeeListLayout;
import com.bizcom.vc.activity.conference.LeftInvitionAttendeeLayout;
import com.bizcom.vc.activity.conference.LeftMessageChattingLayout;
import com.bizcom.vc.activity.conference.LeftShareDocLayout;
import com.bizcom.vc.activity.conference.SoftwareHelper;
import com.bizcom.vc.activity.conference.SurfaceHolderObserver;
import com.bizcom.vc.activity.conference.SurfaceViewConfig;
import com.bizcom.vc.activity.conversation.ConversationSelectImageActivity;
import com.bizcom.vc.hg.beans.queryPhoneInfoBean;
import com.bizcom.vc.hg.beans.setPBeans;
import com.bizcom.vc.hg.ui.HTab0;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.StudentInfo;
import com.bizcom.vc.hg.ui.edu.bridgehandler.model.StudentInfo2;
import com.bizcom.vc.hg.ui.edu.view.RaiseHandPopWindow;
import com.bizcom.vc.hg.ui.edu.view.StudentSurfaceLayout;
import com.bizcom.vc.hg.ui.edu.view.VipUserView;
import com.bizcom.vc.hg.util.DataUtil;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.interf.BaseResponse;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.ErrorResponse;
import com.bizcom.vc.hg.web.interf.SimpleResponseListener;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.listener.CommonCallBack.CommonNotifyChatInterToReplace;
import com.bizcom.vo.Attendee;
import com.bizcom.vo.AttendeeMixedDevice;
import com.bizcom.vo.CameraConfiguration;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.MixVideo;
import com.bizcom.vo.User;
import com.bizcom.vo.UserDeviceConfig;
import com.bizcom.vo.enums.ConferencePermission;
import com.bizcom.vo.enums.NetworkStateCode;
import com.bizcom.vo.enums.PermissionState;
import com.bizcom.vo.enums.SurfaceViewState;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.whiteboard.V2Doc;
import com.bizcom.vo.whiteboard.V2Doc.Page;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import v2av.VideoCaptureDevInfo;
import v2av.VideoPlayer;
import v2av.VideoRecorder;

@SuppressLint("InflateParams")
public class ConferenceActivity2 extends BaseActivity implements CommonNotifyChatInterToReplace {
    private static final String TAG = ConferenceActivity2.class.getSimpleName();
    // 左列表的显示模式标志
    private static final int TAG_SUB_WINDOW_STATE_FIXED = 0x1;
    private static final int TAG_SUB_WINDOW_STATE_FLOAT = 0x0;
    private static final int TAG_SUB_WINDOW_STATE_FULL_SCRREN = 0x10;
    private static final int TAG_SUB_WINDOW_STATE_RESTORED = 0x00;

    // 远程视频块SurfaceView是否被占用的标记
    private boolean SURFACE_VIEW_ONE = false;
    private boolean SURFACE_VIEW_TWO = false;
    private boolean SURFACE_VIEW_THREE = false;
    private boolean SURFACE_VIEW_FOUR = false;

    // 会议左侧菜单栏按钮的Tag标记
    private static final String LEFT_MENU_BUTTON_INVITE = "INVITE";
    private static final String LEFT_MENU_BUTTON_ATTENDEE = "ATTENDEE";
    private static final String LEFT_MENU_BUTTON_CHAT = "CHAT";
    private static final String LEFT_MENU_BUTTON_DOC = "DOC";
    private static final String LEFT_MENU_BUTTON_APPSHARE = "APPSHARE";

    // 会议文档的Handler标记
    private static final int NEW_DOC_NOTIFICATION = 50;
    private static final int DOC_PAGE_LIST_NOTIFICATION = 51;
    private static final int DOC_TURN_PAGE_NOTIFICATION = 52;
    private static final int DOC_ADDED_ONE_PAGE_NOTIFICATION = 53;
    private static final int DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION = 54;
    private static final int DOC_CLOSED_NOTIFICATION = 55;
    private static final int DOC_PAGE_CANVAS_NOTIFICATION = 56;

    private static final int ONLY_SHOW_LOCAL_VIDEO = 1;
    private static final int REQUEST_OPEN_DEVICE_RESPONSE = 4;
    private static final int REQUEST_CLOSE_DEVICE_RESPONSE = 5;
    private static final int REQUEST_OPEN_OR_CLOSE_DEVICE = 6;
    private static final int NOTIFICATION_KICKED = 7;

    /**
     * 用于会议中主讲权限的控制，当有人申请主讲会回调NOTIFY_HOST_PERMISSION_REQUESTED。
     * 取消则回调NOTIFY_USER_PERMISSION_UPDATED
     */
    private static final int NOTIFY_USER_PERMISSION_UPDATED = 11;
    private static final int NOTIFY_HOST_PERMISSION_REQUESTED = 12;

    private static final int ATTENDEE_DEVICE_LISTENER = 20;
    private static final int ATTENDEE_ENTER_OR_EXIT_LISTNER = 21;
    private static final int ATTENDEE_OPEN_VIDEO = 28;
    private static final int ATTENDEE_REMOVE_VIDEO = 29;
    private static final int CONF_USER_DEVICE_EVENT = 23;
    private static final int USER_DELETE_GROUP = 24;
    private static final int GROUP_ADD_USER = 25;
    private static final int CHAIR_MAN_CHANGE_LISTENER = 26;
    private static final int CHAIR_MAN_OPEN_LISTENER = 27;
    private static final int SYNC_MOBILE_VIDEO_LISTENER = 30;

    private static final int SYNC_STATE_NOTIFICATION = 57;
    private static final int VOICEACTIVATION_NOTIFICATION = 58;
    private static final int INVITATION_STATE_NOTIFICATION = 59;

    private static final int VIDEO_MIX_NOTIFICATION = 70;
    private static final int TAG_CLOSE_DEVICE = 0;
    private static final int TAG_OPEN_DEVICE = 1;

    private static final int SUB_ACTIVITY_CODE_SHARE_DOC = 100;

    Unbinder unbinder;

    @BindView(R.id.img_header)
    SimpleDraweeView img_header;

    @BindView(R.id.video_headers_layout)
    LinearLayout video_headers_layout;

    @BindView(R.id.text_jingyin)
    TextView text_jingyin;

    @BindView(R.id.text_touping)
    TextView text_text_touping;

    @BindView(R.id.text_manage_attendee)
    TextView text_manage_attendee;

    @BindView(R.id.text_gd)
    TextView text_gd;

    @BindView(R.id.text_jy)
    TextView text_jy; //禁言

    @BindView(R.id.text_close_camera)
    TextView text_close_camera;

    @BindView(R.id.text_voice_manager)
    TextView text_voice_manager;

//    @BindView(R.id.text_normal_place)
//    TextView text_normal_place;

    @BindView(R.id.text_username)
    TextView text_username;

    @BindView(R.id.text_course_name)
    TextView text_course_name;//课程名称

    @BindView(R.id.text_talk_minute)
    TextView text_talk_minute;//通话时长

    @BindView(R.id.ll_top)
    LinearLayout ll_top;

    @Nullable
    @BindView(R.id.ll_right_remotes)
    LinearLayout ll_right_remotes;


    private DisplayMetrics mDisMetr = new DisplayMetrics();
    private RelativeLayout mRootContainer;

    private FrameLayout mContentLayoutMain;
    private RelativeLayout mVideoLayout;
    private FrameLayout mSubWindowLayout;

    private TextView mGroupNameTV;
    private ImageView mChairmanControl;
    private View mMoreIV;
    private View mMsgNotification;
    private View mConfMsgRedDot;
    private View mChatMsgNotification;

    private ImageView mSpeakerIV;
    private ImageView mCameraIV;
    private PopupWindow mSettingWindow;
    private PopupWindow mChairControlWindow;
    private PopupWindow moreWindow;
    private TextView mRequestButtonName;
    private ImageView mRequestButtonImage;
    private ConferenceMsgDialog mConferenceMsgDialog;

    private Dialog mQuitDialog;
    private Dialog mCameraDialog;

    private LeftInvitionAttendeeLayout mInvitionContainer;
    private LeftMessageChattingLayout mMessageContainer;
    private LeftAttendeeListLayout mAttendeeContainer;
    private LeftShareDocLayout mDocContainer;

    private ImageView mMenuButton;
    private View mMenuButtonContainer;
    private View mMenuSparationLine;

    private View mMenuInviteAttendeeButton;
    private View mMenuMessageButton;
    private View mMenuAttendeeButton;
    private View mMenuDocButton;
    private View mConverseLocalCameraButton;
    private View mMenuButtonGroup[];

    private RelativeLayout mLocalSurfaceViewLy;
    private ConferenceSurfaceView mLocalSurface;

    private OnClickListener mMenuItemButtonOnClickListener = new LeftMenuItemButtonOnClickListener();
    private OnClickListener mSpeakIVOnClickListener = new SpeakIVOnClickListener();
    private OnClickListener onClickMCameraIV = new CameraIVOnClickListener();
    private OnClickListener mLectureButtonListener = new LectureButtonOnClickListener();
    private OnClickListener mMoreIVOnClickListener = new MoreIVOnClickListener();
    private OnClickListener mChairmanControlButtonOnClickListener = new ChairmanControlButtonOnClickListener();
    private OnClickListener mSettingButtonOnClickListener = new SettingButtonOnClickListener();
    private OnClickListener mConverseCameraOnClickListener = new ConverseCameraOnClickListener();
    private ServiceConnection mLocalServiceConnection = new LocalServiceConnection();
    private SurfaceHolder.Callback mLocalCameraSHCallback = new LocalCameraSHCallback();
    private OnTouchListener mLocalCameraOnTouchListener = new LocalCameraOnTouchListener();
    private OnTouchListener mContentLayoutMainTouchListener = new ContentLayoutMainTouchListener();
    private OnClickListener mQuitButtonOnClickListener = new QuitButtonOnClickListener();
    private OnClickListener mMenuButtonListener = new LeftMenuButtonOnClickListener();
    private LeftSubViewListener mLeftSubViewListener = new LeftSubViewListener();

    private ConferenceGroup cg;
    private Conference conf;
    private AudioManager audioManager;
    // 用于语音激励的时候，传递过来的参数，由于要延迟打开，所以用个变量存起来
    private V2ConfSyncVideoJNIObject mVoiceActivationObj;

    private V2ConferenceRequest v2ConferenceRequest;
    /**
     * 白版功能暂时不用，如果用的话把ds相关的代码取消注释
     */
    private V2DocumentRequest ds;
    private V2ChatRequest cs = new V2ChatRequest();

    private List<SurfaceViewConfig> mCurrentShowedSV;
    private List<UserDeviceConfig> remeberRemoteVideo = new ArrayList<>();
    private ArrayList<Attendee> mAttendeeList = new ArrayList<>();
    private SparseArray<Attendee> mAttendeeMap = new SparseArray<>();
    private List<PermissionUpdateIndication> mPendingPermissionUpdateList;
    private Set<User> mHostRequestUsers;
    private OrderedHashMap<String, V2Doc> mDocs = new OrderedHashMap<>();
    private HashMap<Integer, RelativeLayout> mSurfaceViewRootCache = new HashMap<>();

    // ----文档的一些标识----
    private String mCurrentLecturerActivateDocIdStr = null;
    private Page mCurrentLecturerActivateDocPage = null;
    // 用户是否自行做了切换文档和翻页动作，停止跟随
    private boolean isFreeMode;
    // ----文档的一些标识----

    // 蓝牙耳机检测
    private boolean isBluetoothHeadsetConnected;
    // 蓝牙耳机检测
    private BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();

    private int mVideoMaxRows = 2;
    private int arrowWidth = 0;
    private int mMixVideoChildCount = 0;

    private boolean mServiceBound;
    private boolean mLocalHolderIsCreate;
    public boolean isMoveTaskBack;
    private boolean isOpeningLocal;
    // 主席远程视频是否已经被打开
    private String isChairManVideoOpen;
    // 是否禁用本地视频的标识
    private boolean isMuteCamera;
    // 程序是否被切出去
    private boolean isWindowFocus;
    // 混合视频是否同步到移动端
    private boolean isMixVideoShow;
    // 用一个flag来标记当前activity中popupwindow是否正在显示。具体原因看记录
    private boolean isPopupWindowShow;
    /**
     * 当前是否处于同步状态
     */
    private boolean isSyn;
    private boolean isVoiceActivation;
    private boolean canInvitation;
    private boolean isFinish;
    private boolean hasUnreadChiremanControllMsg;
    private boolean isOpenLocalVideoFinish;

    private Attendee currentAttendee;
    private boolean isChangingSubLayout;

    // 调节本地视频质量的popupWindow上的一些变量
    private int popupWidth = -1;
    private int popupHeight = -1;
    private RelativeLayout popupRootView;
    private RadioGroup radios;
    private View mTitleBar;

    // ------AppSharevariable------
    private SurfaceViewState appShareSHState = SurfaceViewState.CLOSED;
    private AppShareDeleteBTOclick mAppShareDeleteBTOclick = new AppShareDeleteBTOclick();
    private String appShareDeviceID;
    private View mMenuAppShareButton;
    private View mAppShareButtonNotification;
    private RelativeLayout mAppShareSurfaceViewLy;
    private View mAppShareDeleteBT;
    private ConferenceSurfaceView mAppShareSurfaceView;
    private SurfaceHolder.Callback mAppShareSHCallback = new AppShareSHCallback();
    // ------AppSharevariable------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intentService = new Intent(this, MessageService.class);
        startService(intentService);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.setNeedAvatar(false);
        super.setNeedBroadcast(true);
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);
        if (!initConferenceDate()) {
            isFinish = true;
            // Toast.makeText(getApplicationContext(),
            // R.string.confs_is_deleted_notification,
            // Toast.LENGTH_LONG).show();
            super.finish();
            return;
        }

        ll_top.bringToFront();
        User user = GlobalHolder.getInstance().getCurrentUser();
        Log.e(TAG, "conf user-->" + user.toString());
        UserHeaderImgHelper.display(img_header, user);
        text_username.setText(CourseInfoSingleton.getCourseInfo().getTeacher_name());
        text_course_name.setText(conf.getName());

        currentActivityName = TAG;
        SoftwareHelper.getInstance().assistActivity(this);
        // Initialize broadcast receiver
        initService();
        init();
        // showMuteCameraDialog();
        GlobalHolder.getInstance().setCurrentMeetingID(conf.getId());
        SoftwareHelper.softwareShow = true;
        // Start animation
        overridePendingTransition(R.anim.nonam_scale_center_0_100, R.anim.nonam_scale_null);
        mHandler.post(openLocalTimeOut);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
        intentFilter.addAction(JNIService.JNI_BROADCAST_NEW_MESSAGE);

        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        //注册消息接收广播
        registerReceiver(msgReceiver, intentFilter);
        getAllStudents();
        setVideoConfig();
    }

    OpenRemoteCameraRunnable openRemoteCameraRunnable = new OpenRemoteCameraRunnable();


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        if (!cameraIsCanUse()) {
            Toast.makeText(ConferenceActivity2.this, "相机故障,请检查您的摄像头权限是否已打开", Toast.LENGTH_LONG).show();
            return;
        }
        isOpByMine = false;
        startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_TEACHER_ONLINE);
        //currentAttendee.setSpeakingState(true);
        if (v2ConferenceRequest != null) {
            doApplyOrReleaseSpeak(true);
        }

    }

    @OnClick(R.id.text_gd)
    void gd() {
        showQuitDialog(mContext.getText(R.string.in_meeting_quit_text).toString());
    }

    boolean isJingyin = false;

    @OnClick(R.id.text_jingyin)
    void text_jingyin() {
        Drawable drawableTop = null;
        if (!isJingyin) {
            isJingyin = true;
            drawableTop = getResources().getDrawable(R.drawable.n_mute_off);
            text_jingyin.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
        } else {
            isJingyin = false;
            drawableTop = getResources().getDrawable(R.drawable.n_mute);
            text_jingyin.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
        }
        doApplyOrReleaseSpeak(!currentAttendee.isSpeaking());
        currentAttendee.setSpeakingState(isJingyin);
    }


    @OnClick(R.id.text_manage_attendee)
    void text_manage_attendee() {
        isOpByMine = true;
        Intent intent = new Intent(ConferenceActivity2.this, WebCourseNoToolbarActivity.class);
        intent.putExtra("url", CourseConfig.MANAGE_STUDENT_URL + "/" + CourseInfoSingleton.getCourseInfo().getCarrange_id());
        Log.e(TAG, "url-->" + CourseConfig.MANAGE_STUDENT_URL + "/" + CourseInfoSingleton.getCourseInfo().getCarrange_id());
        startActivity(intent);
        startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_TYPE_TEACHER_IN_BACKGROUND);
    }

    @OnClick(R.id.text_touping)
    void text_touping() {
        ToastUtil.ShowToast_short(ConferenceActivity2.this, "功能正在开发中！");
    }


    //会场禁言
    boolean isJY = false;

    @OnClick(R.id.text_jy)
    void text_jy() {
        Drawable drawableTop = null;
        if (!isJY) {
            isJY = true;
            drawableTop = getResources().getDrawable(R.mipmap.all_mute_select);
            text_jy.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
            startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_TYPE_JINYAN);
        }
//        } else {
//            isJY = false;
//            drawableTop = getResources().getDrawable(R.mipmap.all_mute);
//            text_jy.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
//        }

        v2ConferenceRequest.muteConf();
    }


    //扬声器默认打开
    boolean isVoiceManagerOpen = true;

    @OnClick(R.id.text_voice_manager)
    void text_voice_manager() {
        Drawable drawableTop = null;
        if (!isVoiceManagerOpen) {
            isVoiceManagerOpen = true;
            drawableTop = getResources().getDrawable(R.drawable.n_speaker);
            text_voice_manager.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
        } else {
            isVoiceManagerOpen = false;
            drawableTop = getResources().getDrawable(R.drawable.n_speaker_off);
            text_voice_manager.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
        }

        if (isBluetoothHeadsetConnected) {
            if (isVoiceManagerOpen) {
                audioManager.startBluetoothSco();
                audioManager.setBluetoothScoOn(true);

            } else {
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
            }
        } else {
            audioManager.setSpeakerphoneOn(isVoiceManagerOpen);
        }
    }


    //默认打开本地视频
//    boolean isOpenLocalCamera = true;
//    @OnClick(R.id.text_close_camera)
//    void closeCamera(){
//        closeLocalCamera();
//        Drawable drawableTop = null;
//        if (!isOpenLocalCamera) {
//            isOpenLocalCamera = true;
//            drawableTop = getResources().getDrawable(R.drawable.n_camera);
//            text_voice_manager.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
//            openLocalCamera();
//        } else {
//            isOpenLocalCamera = false;
//            drawableTop = getResources().getDrawable(R.drawable.n_camera_off);
//            text_voice_manager.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
//            closeLocalCamera();
//        }
//    }

    @Override
    protected void onRestart() {
        // 某些设备，在会议中如果切到后台，则再返回时会自动弹出键盘
        View v = getCurrentFocus();
        if (v != null)
            v.clearFocus();
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        V2Log.i(TAG, "onStart ...");
        SoftwareHelper.isInStop = false;
        if (mServiceBound) {
            V2Log.i(TAG, "conference service bound successuflly!");
            suspendOrResume(true);
        }
        headsetAndBluetoothHeadsetHandle();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(V2GlobalConstants.VIDEO_NOTIFICATION_ID);
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //是否自己主动操作
    boolean isOpByMine = false;

    @Override
    protected void onStop() {
        super.onStop();
        V2Log.i(TAG, "onStop ...");
        SoftwareHelper.isInStop = true;
        MessageUtil.clearLruCache();
        if (mServiceBound) {
            suspendOrResume(false);
        }
        headsetAndBluetoothHeadsetHandle();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isOpByMine) {
            startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_NOTIFY_TEACHER_LEAVE);
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nonam_scale_null, R.anim.nonam_scale_center_100_0);
    }

    @Override
    public void onBackPressed() {
        isOpByMine = true;
        //  showQuitDialog(mContext.getText(R.string.in_meeting_quit_text).toString());
        WaitDialogBuilder.showNormalWithHintProgress(ConferenceActivity2.this);
        startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_NOTIFY_TEACHER_LEAVE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                WaitDialogBuilder.dismissDialog();
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalSurface.getHolder().getSurface().release();
        unregisterReceiver(msgReceiver);
        unregisterReceiver(mHomeKeyEventReceiver);
        if (isFinish) {
            return;
        }

        // 将主席视频的打开标记复位
        if (isChairManVideoOpen != null) {
            long chairman = conf.getChairman();
            Attendee chairMain = mAttendeeMap.get((int) chairman);
            if (chairMain != null) {
                List<UserDeviceConfig> chairMainDevice = getAttendeeDevices(chairMain);
                if (chairMainDevice != null) {
                    for (int k = 0; k < chairMainDevice.size(); k++) {
                        chairMainDevice.get(k).setShowing(false);
                    }
                }
            }
            isChairManVideoOpen = null;
        }
        SoftwareHelper.softwareShow = false;
        if (mServiceBound) {
            v2ConferenceRequest.requestExitConference(conf, null);
            v2ConferenceRequest.unRegisterPermissionUpdateListener(mHandler, NOTIFY_USER_PERMISSION_UPDATED, null);
            v2ConferenceRequest.unRegisterPermissionUpdateListener(this.mHandler, NOTIFY_HOST_PERMISSION_REQUESTED,
                    null);
            v2ConferenceRequest.removeRegisterOfKickedConfListener(mHandler, NOTIFICATION_KICKED, null);
            v2ConferenceRequest.removeAttendeeEnterOrExitListener(this.mHandler, ATTENDEE_ENTER_OR_EXIT_LISTNER, null);
            v2ConferenceRequest.unRegisterVideoMixerListener(mHandler, VIDEO_MIX_NOTIFICATION, null);
            v2ConferenceRequest.unregisterChairManChangeListener(mHandler, CHAIR_MAN_CHANGE_LISTENER, null);
            v2ConferenceRequest.removeSyncStateListener(mHandler, SYNC_STATE_NOTIFICATION, null);
            v2ConferenceRequest.removeInvitationStateListener(mHandler, INVITATION_STATE_NOTIFICATION, null);
            v2ConferenceRequest.removeVoiceActivationListener(mHandler, VOICEACTIVATION_NOTIFICATION, null);
            v2ConferenceRequest.removeAttendeeDeviceListener(mHandler, ATTENDEE_DEVICE_LISTENER, null);
            ds.unRegisterNewDocNotification(mHandler, NEW_DOC_NOTIFICATION, null);
            ds.unRegisterDocDisplayNotification(mHandler, DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION, null);
            ds.unRegisterDocClosedNotification(mHandler, DOC_CLOSED_NOTIFICATION, null);
            ds.unRegisterDocPageAddedNotification(mHandler, DOC_ADDED_ONE_PAGE_NOTIFICATION, null);
            ds.unRegisterPageCanvasUpdateNotification(mHandler, DOC_PAGE_CANVAS_NOTIFICATION, null);

            unbindService(mLocalServiceConnection);
        }
        // call clear function from all service
        cs.clearCalledBack();
        // 停止会议相关服务
        mContext.stopService(new Intent(mContext, ConferencMessageSyncService.class));
        // 清除全局中会议状态
        GlobalHolder.getInstance().setMeetingState(false, 0);
        // 清除数据库中会议聊天消息
        //ChatMessageProvider.deleteMessageByID(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(), -1, false);
        // 恢复控制的音频类型
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        if (mDocContainer != null) {
            // clean document bitmap cache
            mDocContainer.cleanAllResource();
            mDocContainer = null;
        }

        if (mInvitionContainer != null) {
            mInvitionContainer.cleanAllResource();
            mInvitionContainer = null;
        }

        if (mQuitDialog != null) {
            mQuitDialog.dismiss();
            mQuitDialog = null;
        }

        if (mCameraDialog != null) {
            mCameraDialog.dismiss();
            mCameraDialog = null;
        }

//        if (mAttendeeList != null) {
//            mAttendeeList.clear();
//            mAttendeeList = null;
//        }

        if (mAttendeeMap != null) {
            mAttendeeMap.clear();
        }

        if (mCurrentShowedSV != null) {
            mCurrentShowedSV.clear();
            mCurrentShowedSV = null;
        }

        if (mDocs != null) {
            mDocs.clear();
            mDocs = null;
        }

        if (mHostRequestUsers != null) {
            mHostRequestUsers.clear();
            mHostRequestUsers = null;
        }

        if (mPendingPermissionUpdateList != null) {
            mPendingPermissionUpdateList.clear();
            mPendingPermissionUpdateList = null;
        }

        if (remeberRemoteVideo != null) {
            remeberRemoteVideo.clear();
            remeberRemoteVideo = null;
        }

        mMenuButtonGroup = null;
        mVideoLayout.removeAllViews();
        mSubWindowLayout.removeAllViews();
        mContentLayoutMain.removeAllViews();
        GlobalHolder.getInstance().setCurrentMeetingID(-1);
        // call gc
        //System.gc();
        lock = null;
        mHandler.removeCallbacks(openRemoteCameraRunnable);
        t.cancel();
        t = null;
        openRemoteCameraRunnable = null;
        System.gc();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        V2Log.i(TAG, "onNewIntent was called!");
        Conference newConf = (Conference) intent.getExtras().get("conf");
        if (newConf != null && newConf.getId() != conf.getId()) {
            // Pop up dialog to exit conference
            showQuitDialog(
                    mContext.getText(R.string.in_meeting_quit_text_for_new).toString().replace("[]", conf.getName()));
        }
    }

    @Override
    public void onPause() {
        V2Log.d(TAG, "onPause was called!");
        super.onPause();
        isMoveTaskBack = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isWindowFocus = hasFocus;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        V2Log.d(TAG, "onSaveInstanceState was called!");
        if (mQuitDialog != null && mQuitDialog.isShowing()) {
            mQuitDialog.dismiss();
        }

        if (mSettingWindow != null && mSettingWindow.isShowing()) {
            mSettingWindow.dismiss();
        }

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();// 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        if (!isScreenOn)
            isMoveTaskBack = false;

        if (!isMoveTaskBack)
            isMoveTaskBack = true;
        else
            moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SUB_ACTIVITY_CODE_SHARE_DOC && resultCode != Activity.RESULT_CANCELED) {
            if (data != null) {
                if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_NOT) {
                    Toast.makeText(this, R.string.conference_toast_sharing_failed, Toast.LENGTH_LONG).show();
                    return;
                }
                String filePath = data.getStringExtra("checkedImage");
                v2ConferenceRequest.shareDoc(conf, filePath, null);
                v2ConferenceRequest.modifyGroupLayout(conf);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        VideoCaptureDevInfo captureInfo = VideoCaptureDevInfo.CreateVideoCaptureDevInfo();
//        if (captureInfo != null) {
//            int displayRotation = GlobalConfig.getDisplayRotation(this);
//            VideoRecorder.DisplayRotation = displayRotation;
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isPopupWindowShow = false;
        return super.onTouchEvent(event);
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(JNIService.JNI_BROADCAST_NEW_CONF_MESSAGE);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
        filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_REMOVED_SIP_CALL);
        filter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);
        filter.addAction(PublicIntent.PREPARE_FINISH_APPLICATION);
        filter.addAction(PublicIntent.NOTIFY_CONFERENCE_ACTIVITY);
        filter.addAction(JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO_TO_MOBILE);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO_TO_MOBILE);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_VOD_OPEN_VIDEO);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_CONF_VOD_OPEN_VIDEO);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_APPSHARE_CREATE);
        filter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_APPSHARE_DESTORY);
        filter.addAction(JNIService.JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        if (JNIService.JNI_BROADCAST_NEW_CONF_MESSAGE.equals(intent.getAction())) {
            MessageObject msgObj = intent.getParcelableExtra("msgObj");
            long msgID = msgObj.messageColsID;
            long groupID = msgObj.remoteGroupID;
            VMessage vm = ChatMessageProvider.loadGroupMessageById(V2GlobalConstants.GROUP_TYPE_CONFERENCE,
                    groupID, msgID);
            if (mMessageContainer == null) {
                initMsgLayout();
            }

            mMessageContainer.addNewMessage(vm);
            if (mSubWindowLayout.getVisibility() == View.VISIBLE
                    && mSubWindowLayout.getChildAt(0) == mMessageContainer) {
                mChatMsgNotification.setVisibility(View.GONE);
            } else {
                mChatMsgNotification.setVisibility(View.VISIBLE);
            }
        } else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {

            long confID = intent.getLongExtra("gid", 0);
            if (confID == conf.getId()) {
                Group confGroup = GlobalHolder.getInstance().findGroupById(conf.getId());
                // load conference attendee list
                List<Attendee> list = new ArrayList<Attendee>();
                List<User> l = confGroup.getUsers();
                for (User u : l) {
                    int key = (int) u.getmUserId();
                    if (mAttendeeMap.get(key) == null) {
                        Attendee at = new Attendee(u);
                        putNewAttendee(at);
                        list.add(at);
                    }
                }

                if (mAttendeeContainer != null) {
                    mAttendeeContainer.addNewAttendee(list);
                }
            }
        } else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
            GroupUserObject obj = intent.getParcelableExtra("obj");
            if (obj == null) {
                V2Log.e(TAG,
                        "Received the broadcast to quit the conference group , but given GroupUserObject is null!");
                return;
            }

            if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT
                    || obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                if (mInvitionContainer != null) {
                    mInvitionContainer.updateMultilevelListView(obj, false);
                }
            } else if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                Message.obtain(mHandler, USER_DELETE_GROUP, obj).sendToTarget();
            }
        } else if (JNIService.JNI_BROADCAST_GROUP_USER_ADDED.equals(intent.getAction())) {
            GroupUserObject obj = intent.getParcelableExtra("obj");
            if (obj == null) {
                V2Log.e(TAG,
                        "Received the broadcast to quit the conference group , but given GroupUserObject is null!");
                return;
            }

            if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT
                    || obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                if (mInvitionContainer != null) {
                    mInvitionContainer.updateMultilevelListView(obj, true);
                }
            } else if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                Message.obtain(mHandler, GROUP_ADD_USER, obj).sendToTarget();
            }
        } else if (JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION.equals(intent.getAction())) {
            NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
            if (code != NetworkStateCode.CONNECTED) {
                V2Log.e(TAG, "CONNECT STATE IS ERROR!");
                finish();
            }
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_REMOVED_SIP_CALL.equals(intent.getAction())) {
            // finish();
        } else if (JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO.equals(intent.getAction())) {
            long uid = intent.getLongExtra("uid", -1);

            int key = (int) uid;
            Attendee attendee = mAttendeeMap.get(key);
            if (attendee != null) {
                attendee.setUser(GlobalHolder.getInstance().getUser(uid));
                if (mAttendeeContainer != null) {
                    mAttendeeContainer.updateDisplay();
                }
            }
        } else if (JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION.equals(intent.getAction())) {

            long uid = intent.getExtras().getLong("uid");
            Attendee at = mAttendeeMap.get((int) uid);
            // If exited user is not attendee in conference, then return
            if (at == null) {
                return;
            }
            User user = GlobalHolder.getInstance().getUser(uid);
            UserStatusObject us = (UserStatusObject) intent.getExtras().get("status");

            if (us != null) {
                User.Status st = User.Status.fromInt(us.getStatus());
                // If client applciation exit directly, we don't receive exit
                // conference notification
                if (st == User.Status.OFFLINE && user != null) {
                    Message.obtain(mHandler, ATTENDEE_ENTER_OR_EXIT_LISTNER, 0, 0, user).sendToTarget();
                }
            }
        } else if (PublicIntent.PREPARE_FINISH_APPLICATION.equals(intent.getAction())) {
            // Listen quit request to make sure close all device
            finish();
        } else if (PublicIntent.NOTIFY_CONFERENCE_ACTIVITY.equals(intent.getAction())) {
            // from VideoMsgChattingLayout 聊天打开图片
            isMoveTaskBack = false;
        } else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
            if (intent.hasExtra("state")) {
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    V2Log.i(TAG, "插入耳机");
                    headsetAndBluetoothHeadsetHandle();
                } else if (state == 0) {
                    V2Log.i(TAG, "拔出耳机");
                    headsetAndBluetoothHeadsetHandle();
                }
            }
        } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {

            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            if (state == BluetoothProfile.STATE_CONNECTED) {
                isBluetoothHeadsetConnected = true;
                V2Log.i(TAG, "蓝牙耳机已连接");
                headsetAndBluetoothHeadsetHandle();
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                V2Log.i("TAG", "蓝牙耳机已断开");
                isBluetoothHeadsetConnected = false;
                headsetAndBluetoothHeadsetHandle();
            }
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO.equals(intent.getAction())) {
            long nDstUserID = intent.getLongExtra("nDstUserID", -1);
            String dstDeviceID = intent.getStringExtra("dstDeviceID");
            if (isVoiceActivation) {
                Attendee attendee = mAttendeeMap.get((int) nDstUserID);
                if (attendee != null) {
                    List<UserDeviceConfig> devices = getAttendeeDevices(attendee);
                    if (devices != null) {
                        for (int j = 0; j < devices.size(); j++) {
                            V2Log.d(TAG, "语音激励切换用户，准备关闭，用户视频设备列表 --：" + dstDeviceID);
                            if (devices.get(j).getDeviceID().equals(dstDeviceID)) {
                                V2Log.d(TAG, "语音激励切换用户，关闭上一个用户视频--：" + dstDeviceID);
                                closeAttendeeVideo(devices.get(j));
                                break;
                            }
                        }
                    }
                }
            }
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO.equals(intent.getAction())) {
            if (isVoiceActivation) {
                ArrayList<V2ConfSyncVideoJNIObject> extra = intent.getParcelableArrayListExtra("syncOpen");
                if (extra == null) {
                    return;
                }
                V2Log.i(TAG, "收到一个新的语音激励同步请求");
                mHandler.removeCallbacks(mVoiceActiveRunnable);
                mHandler.postDelayed(mVoiceActiveRunnable, 500);
                for (int i = 0; i < extra.size(); i++) {
                    mVoiceActivationObj = extra.get(i);
                }
            }
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_CLOSE_VIDEO_TO_MOBILE.equals(intent.getAction())) {
            // 传来的是设备ID
            String dstDeviceID = intent.getStringExtra("sDstMediaID");
            UserDeviceConfig userDeviceConfig = null;

            if (dstDeviceID == null) {
                return;
            }

            for (SurfaceViewConfig sw : mCurrentShowedSV) {
                if (dstDeviceID.equals(sw.udc.getDeviceID())) {
                    userDeviceConfig = sw.udc;
                    break;
                }
            }
            if (userDeviceConfig == null) {
                return;
            }

            if (userDeviceConfig.getType() == V2GlobalConstants.EVIDEODEVTYPE_VIDEOMIXER) {
                isMixVideoShow = false;
            }
            closeVideo(userDeviceConfig);
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_CONF_SYNC_OPEN_VIDEO_TO_MOBILE.equals(intent.getAction())) {
            final ArrayList<V2ConfSyncVideoJNIObject> extra = intent.getParcelableArrayListExtra("syncOpen");
            if (extra == null) {
                return;
            }

            Message.obtain(mHandler, SYNC_MOBILE_VIDEO_LISTENER, extra).sendToTarget();
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_CONF_VOD_OPEN_VIDEO.equals(intent.getAction())) {
            String deviceID = intent.getStringExtra("deviceID");
            if (TextUtils.isEmpty(deviceID)) {
                return;
            }

            V2Log.i(TAG, "Get chair man current open device id is : " + deviceID);
            if (isVoiceActivation) {
                mHandler.sendEmptyMessage(CHAIR_MAN_OPEN_LISTENER);
            }
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_APPSHARE_CREATE.equals(intent.getAction())) {
            appShareDeviceID = intent.getStringExtra("videoDeviceID");
            mMenuAppShareButton.setVisibility(View.VISIBLE);
            mAppShareButtonNotification.setVisibility(View.VISIBLE);
        } else if (JNIService.JNI_BROADCAST_CONFERENCE_APPSHARE_DESTORY.equals(intent.getAction())) {
            mMenuAppShareButton.setVisibility(View.GONE);
            appShareChangeVisibility(false);
        } else if (JNIService.JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION.equals(intent.getAction())) {
            String docID = intent.getStringExtra("fileID");
            // WHITEBOARD_3f175531-cf59-48fd-9b7c-4929249e6e8f_2
            if (!TextUtils.isEmpty(docID)) {
                String[] spArrs = docID.split("_");
                if (spArrs.length == 3) {
                    String targetDoc = spArrs[1];
                    // int pageNum = Integer.valueOf(spArrs[2]);
                    if (mDocs != null) {
                        V2Doc v2Doc = mDocs.get(targetDoc);
                        if (v2Doc != null) {
                            V2Log.d("LeftShareDocLayout",
                                    "Doc Down failed , id : " + v2Doc.getId() + " name : " + v2Doc.getDocName());
                            mDocContainer.updatePageDownState(v2Doc.getId());
                            v2Doc.getActivatePage().setDownLoadFailed(true);
                            mDocContainer.updateCurrentDoc();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        Log.e(TAG, "msg what-->" + msg.what);
        switch (msg.what) {
            case ConferenceHelper.REQUEST_CLOSE_VIDEO_DEVICE:
                Log.e(TAG, "REQUEST_CLOSE_VIDEO_DEVICE- SUCCESS");
                RequestCloseUserVideoDeviceResponse response = (RequestCloseUserVideoDeviceResponse) msg.obj;
                UserDeviceConfig userDeviceConfig1 = (UserDeviceConfig) response.callerObject;
                userDeviceConfig1.doClose();
                map.remove(userDeviceConfig1.getUserID());
                break;
            case ONLY_SHOW_LOCAL_VIDEO:
                // Make sure open local camera after service bound and holder
                // created
                if (mLocalHolderIsCreate) {
                    showLocalSurViewOnly();
                    LocalCameraAdjustExist(true);
                    LocalCameraAdjustExist(false);
                }
                break;
            case REQUEST_OPEN_DEVICE_RESPONSE:
                Log.e(TAG, "response open device");
                break;
            case REQUEST_CLOSE_DEVICE_RESPONSE:
                Log.e(TAG, "response close device");
                break;
            case CONF_USER_DEVICE_EVENT:
                // recordUserDevice((ConfUserDeviceInfo) msg.obj);
                break;
            case USER_DELETE_GROUP: {
                GroupUserObject obj = (GroupUserObject) msg.obj;
                int key = (int) obj.getmUserId();
                Attendee removed = mAttendeeMap.get(key);
                removeAttendee(removed);

                if (mAttendeeContainer != null) {
                    mAttendeeContainer.removeAttendee(removed);
                }
            }
            break;
            case GROUP_ADD_USER:
                GroupUserObject ro1 = (GroupUserObject) msg.obj;
                int addUserID = (int) ro1.getmUserId();
                if (mAttendeeMap.get(addUserID) == null) {
                    Attendee newAttendee = new Attendee(GlobalHolder.getInstance().getUser(addUserID));
                    putNewAttendee(newAttendee);
                    if (mAttendeeContainer != null) {
                        mAttendeeContainer.addNewAttendee(newAttendee);
                    }
                }
                break;
            case ATTENDEE_DEVICE_LISTENER: {
                Log.e(TAG, "ATTENDEE_DEVICE_LISTENER");
                // need to update user device when remote user disable device
                Object[] obj = (Object[]) (((AsyncResult) msg.obj).getResult());
                @SuppressWarnings("unchecked")
                List<UserDeviceConfig> list = (List<UserDeviceConfig>) obj[1];
                Integer uid = (Integer) obj[0];
                Log.e(TAG, "uid->" + uid);
                if (mAttendeeMap != null) {
                    Attendee at = mAttendeeMap.get(uid);
                    if (at != null) {
                        V2Log.d(TAG, "ATTENDEE_DEVICE_LISTENER --> Update User Device , user id : " + uid);
                        // updateAttendeeDevice(at, list);
                    }
                    UserDeviceConfig userDeviceConfig = list.get(0);
                    // for (UserDeviceConfig userDeviceConfig : list) {
                    Log.e(TAG, "userDeviceConfig->" + userDeviceConfig.getDeviceID());
                    //showAttendee(at, userDeviceConfig);
                    //openAttendeeVideo(userDeviceConfig);

//                        if (userDeviceConfig.isShowing()) {
//                            closeAttendeeVideo(userDeviceConfig);
//                        }
//                        Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, userDeviceConfig).sendToTarget();
                    // }
                }
            }
            break;
            case ATTENDEE_ENTER_OR_EXIT_LISTNER:
                User ut = (User) (((AsyncResult) msg.obj).getResult());
                Attendee at = mAttendeeMap.get((int) ut.getmUserId());
                if (msg.arg1 == 1) {
                    Log.e(TAG, "enter a new attendee");
                    // for non-register user construct temp attendee
                    if (at == null) {
                        at = new Attendee(ut);
                        putNewAttendee(at);
                    }

                    if (at.getType() == Attendee.TYPE_MIXED_VIDEO) {
                        V2Log.i(TAG, "Successfully received a new attendee change callback. callback is : "
                                + "OnConfMemberEnterCallback ! Add Type is : TYPE_MIXED_VIDEO");
                    } else {
                        V2Log.i(TAG, "Successfully received a new attendee change callback. callback is : "
                                + "OnConfMemberEnterCallback ! Add Type is : TYPE_ATTENDEE");
                    }
                    doHandleNewUserEntered(at);
                    if (ll_right_remotes != null) {
                        ll_right_remotes.bringToFront();
                    }
                } else {

                    if (at != null && at.getType() == Attendee.TYPE_MIXED_VIDEO) {
                        V2Log.i(TAG, "Successfully received a new attendee change callback. callback is : "
                                + "OnConfMemberExitCallback ! Add Type is : TYPE_MIXED_VIDEO");
                    } else {
                        V2Log.i(TAG, "Successfully received a new attendee change callback. callback is : "
                                + "OnConfMemberExitCallback ! Add Type is : TYPE_ATTENDEE");
                    }

                    if (at != null && at.isRapidInitiation()) {
                        Log.e(TAG, "remove a new attendee");
                        removeAttendee(at);
                    }

                    doHandleUserExited(at);
                    User user = null;
                    for (User tempUser : mHostRequestUsers) {
                        if (tempUser.getmUserId() == ut.getmUserId()) {
                            user = tempUser;
                            break;
                        }
                    }

                    if (user != null) {
                        mHostRequestUsers.remove(user);
                        if (mConferenceMsgDialog != null && mConferenceMsgDialog.isShowing()) {
                            mConferenceMsgDialog.deleteFromList(user);
                        }
                    }
                }
                updateOnLineCount();
                break;
            case REQUEST_OPEN_OR_CLOSE_DEVICE:
                if (msg.arg1 == TAG_CLOSE_DEVICE) {
                    v2ConferenceRequest.requestCloseVideoDevice(cg, (UserDeviceConfig) msg.obj,
                            new HandlerWrap(mHandler, REQUEST_CLOSE_DEVICE_RESPONSE, null));
                } else if (msg.arg1 == TAG_OPEN_DEVICE) {
                    v2ConferenceRequest.requestOpenVideoDevice(cg, (UserDeviceConfig) msg.obj,
                            new HandlerWrap(mHandler, REQUEST_OPEN_DEVICE_RESPONSE, null));
                }
                break;
            case NOTIFICATION_KICKED: {
                int result = msg.arg1;
                V2Log.d(TAG, "收到退出会议的信令，错误码是: " + result);
                int resource = R.string.conversations_kick_notification;
                if (result == V2GlobalConstants.CONF_ERROR_CONFOVER) {
                    resource = R.string.confs_is_deleted_notification;
                } else if (result == V2GlobalConstants.CONF_ERROR_SERVERDISCONNECT) {
                    resource = R.string.error_can_not_connect_server;
                }
                Toast.makeText(mContext, resource, Toast.LENGTH_LONG).show();
                // Do quit action
                finish();
            }
            break;
            case NOTIFY_HOST_PERMISSION_REQUESTED:
                PermissionRequestIndication rri = (PermissionRequestIndication) (((AsyncResult) msg.obj).getResult());
                if (rri.getUid() != GlobalHolder.getInstance().getCurrentUserId()) {
                    mHostRequestUsers.add(GlobalHolder.getInstance().getUser(rri.getUid()));

                    if (mConferenceMsgDialog != null && mConferenceMsgDialog.isShowing()) {
                        mConferenceMsgDialog.addToList(GlobalHolder.getInstance().getUser(rri.getUid()));
                    }

                    if (mConferenceMsgDialog == null || !mConferenceMsgDialog.isShowing()) {
                        mMsgNotification.setVisibility(View.VISIBLE);
                        if (mConfMsgRedDot != null) {
                            mConfMsgRedDot.setVisibility(View.VISIBLE);
                        }

                        hasUnreadChiremanControllMsg = true;
                    }

                    PermissionUpdateIndication pui = new PermissionUpdateIndication(rri.getUid(), rri.getType(),
                            rri.getState());

                    updateAttendeePermissionState(pui);

                    if (moreWindow != null && moreWindow.isShowing()) {
                        updateMoreWindowDisplay();
                    }
                }
                break;
            // user permission updated
            case NOTIFY_USER_PERMISSION_UPDATED:
                PermissionUpdateIndication ind = (PermissionUpdateIndication) (((AsyncResult) msg.obj).getResult());

                if (ind.getType() == ConferencePermission.CONTROL.intValue()) {
                    if (ind.getState() == PermissionState.GRANTED.intValue()) {
                    } else if (ind.getState() == PermissionState.APPLYING.intValue()) {
                    } else if (ind.getState() == PermissionState.NORMAL.intValue()) {
                        // 取消申请
                        User user = null;
                        for (User tempUser : mHostRequestUsers) {
                            if (tempUser.getmUserId() == ind.getUid()) {
                                user = tempUser;
                                break;
                            }
                        }

                        if (user != null) {
                            mHostRequestUsers.remove(user);
                        }

                        if (mConferenceMsgDialog != null && mConferenceMsgDialog.isShowing()) {
                            mConferenceMsgDialog.deleteFromList(user);
                        }

                        if (mHostRequestUsers.size() == 0) {
                            hasUnreadChiremanControllMsg = false;
                            mMsgNotification.setVisibility(View.GONE);
                            if (mConfMsgRedDot != null) {
                                mConfMsgRedDot.setVisibility(View.GONE);
                            }
                        }

                    }
                }

                // 更新自己的图标显示
                if (ind.getUid() == GlobalHolder.getInstance().getCurrentUserId()) {
                    if (ConferencePermission.CONTROL.intValue() == ind.getType()) {
                        // 显示提示信息
                        showCurrentAttendeeLectureStateToast(PermissionState.fromInt(ind.getState()), true);
                    } else if (ConferencePermission.SPEAKING.intValue() == ind.getType()) {
                        // 更新自己的发言图标
                        updateSpeakerState(PermissionState.fromInt(ind.getState()) == PermissionState.GRANTED
                                && ConferencePermission.SPEAKING.intValue() == ind.getType());
                    }
                }

                // 更新参会人列表里的主讲和发言状态
                if (!updateAttendeePermissionState(ind) && mPendingPermissionUpdateList != null) {
                    mPendingPermissionUpdateList.add(ind);
                }

                if (ind.getUid() == GlobalHolder.getInstance().getCurrentUserId()) {
                    if (ConferencePermission.CONTROL.intValue() == ind.getType()) {
                        if (moreWindow != null && moreWindow.isShowing()) {
                            updateMoreWindowDisplay();
                        }

                        if (mDocContainer != null) {
                            if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                                mDocContainer.updateLectureStateGranted(true);
                                if (mCurrentLecturerActivateDocIdStr != null) {
                                    V2Doc v2doc = mDocs.get(mCurrentLecturerActivateDocIdStr);
                                    if (v2doc != null) {
                                        v2ConferenceRequest.modifyGroupLayout(conf);
                                        ds.switchDoc(currentAttendee.getAttId(),
                                                mDocs.get(mCurrentLecturerActivateDocIdStr), true, null);
                                    }
                                }

                            } else {
                                mDocContainer.updateLectureStateGranted(false);
                            }
                            mDocContainer.updateSyncStatus(
                                    isSyn && (currentAttendee.getLectureState() != Attendee.LECTURE_STATE_GRANTED));
                            mDocContainer.updateCurrentDoc();
                        }
                    }
                }
                break;
            case NEW_DOC_NOTIFICATION:
                V2Log.uiCall(TAG, "NEW_DOC_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case DOC_PAGE_LIST_NOTIFICATION:
                V2Log.uiCall(TAG, "DOC_PAGE_LIST_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case DOC_ADDED_ONE_PAGE_NOTIFICATION:
                V2Log.uiCall(TAG, "DOC_ADDED_ONE_PAGE_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case DOC_TURN_PAGE_NOTIFICATION:
                V2Log.uiCall(TAG, "DOC_TURN_PAGE_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION:
                V2Log.uiCall(TAG, "DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case DOC_CLOSED_NOTIFICATION:
                V2Log.uiCall(TAG, "DOC_CLOSED_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case DOC_PAGE_CANVAS_NOTIFICATION:
                V2Log.uiCall(TAG, "DOC_PAGE_CANVAS_NOTIFICATION");
                handleDocNotification((AsyncResult) (msg.obj), msg.what);
                break;
            case INVITATION_STATE_NOTIFICATION:
                canInvitation = msg.arg1 == 1;
                cg.setCanInvitation(canInvitation);

                if (currentAttendee.isChairMan()) {
                    return;
                }

                if (!cg.isCanInvitation()) {
                    View view = initInvitionContainer();
                    if (mSubWindowLayout.getVisibility() == View.VISIBLE && mSubWindowLayout.getChildAt(0) == view
                            && conf.getCreator() != GlobalHolder.getInstance().getCurrentUserId()) {
                        // close invitation view, remote forbbien invitation
                        showOrHideSubWindow(view, false, false);
                        Toast.makeText(mContext, R.string.error_no_permission_to_invitation, Toast.LENGTH_SHORT).show();
                    }
                    if (mMenuInviteAttendeeButton != null) {
                        mMenuInviteAttendeeButton.setEnabled(false);
                        ((ImageView) mMenuInviteAttendeeButton)
                                .setImageResource(R.drawable.video_menu_invite_attendee_button_disenable);
                    }
                } else {
                    if (mMenuInviteAttendeeButton != null) {
                        mMenuInviteAttendeeButton.setEnabled(true);
                        ((ImageView) mMenuInviteAttendeeButton)
                                .setImageResource(R.drawable.video_menu_invite_attendee_button);
                    }
                }

                break;
            case SYNC_STATE_NOTIFICATION: // 语音激励携带的视频同步
                isSyn = msg.arg1 == 1;
                cg.setSyn(isSyn);
                V2Log.uiCall(TAG, "SYNC_STATE_NOTIFICATION");
                V2Log.d("DOC_TEST", "同步是否已经开启 : " + isSyn);
                V2Log.i(TAG, "同步是否已经开启 : " + isSyn);
                if (isSyn) {
                    isFreeMode = false;
                    // 同步前把已经打开的视频都关掉
                    closeAllAttendeeDevice();
                }

                if (mDocContainer != null) {
                    mDocContainer.updateSyncStatus(
                            isSyn && (currentAttendee.getLectureState() != Attendee.LECTURE_STATE_GRANTED));
                }
                break;
            case SYNC_MOBILE_VIDEO_LISTENER:
                ArrayList<V2ConfSyncVideoJNIObject> extra = (ArrayList<V2ConfSyncVideoJNIObject>) msg.obj;
                if (isSyn || isVoiceActivation) {
                    for (int i = 0; i < extra.size(); i++) {
                        V2ConfSyncVideoJNIObject syncObj = extra.get(i);
                        for (Attendee attendee : mAttendeeList) {

                            if (attendee.getAttId() != syncObj.getDstUserID()
                                    && attendee.getAttId() != syncObj.getDstDeviceID().hashCode()) {
                                continue;
                            }

                            if (attendee.isSelf()) {
                                break;
                            }

                            if (attendee.getType() == Attendee.TYPE_MIXED_VIDEO) {
                                isMixVideoShow = true;
                            }

                            List<UserDeviceConfig> list = getAttendeeDevices(attendee);
                            if (list == null) {
                                V2Log.e(TAG,
                                        "无法打开用户 -" + attendee.getUser().getDisplayName() + "- 视频，没有获取到视频! 同步失败");
                                break;
                            }

                            for (UserDeviceConfig udc : list) {
                                if (udc.getDeviceID().equals(syncObj.getDstDeviceID())) {
                                    Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, udc).sendToTarget();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                break;
            case VOICEACTIVATION_NOTIFICATION: // 语音激励开启与关闭
                V2Log.d(V2Log.UI_MESSAGE, "CLASS = ConferenceActivity  MESSAGE = VOICEACTIVATION_NOTIFICATION");
                isVoiceActivation = msg.arg1 == V2GlobalConstants.CONF_VOICE_ACTIVATION_YES;
                V2Log.i(TAG, "语音激励是否已经开启 : " + isVoiceActivation);
                if (isSyn && isVoiceActivation) {
                    closeAllAttendeeDevice();
                    // 打开主席的视频
                    mHandler.sendEmptyMessage(CHAIR_MAN_OPEN_LISTENER);
                }
                break;
            case VIDEO_MIX_NOTIFICATION:
                // create mixed video
                if (msg.arg1 == 1) {
                    V2Log.i(TAG, "Successfully received a new mix video callback. callback is : "
                            + "OnCreateVideoMixerCallback ! Add Mix Video");
                    mMixVideoChildCount = 0;
                    MixVideo mv = (MixVideo) (((AsyncResult) msg.obj).getResult());
                    int key = mv.getId().hashCode();
                    if (mAttendeeMap.get(key) == null) {
                        AttendeeMixedDevice amd = new AttendeeMixedDevice(mv);
                        putNewAttendee(amd);
                        // Notify attendee list mixed video is created
                        if (mAttendeeContainer != null) {
                            mAttendeeContainer.updateEnteredAttendee(amd);
                        }
                    }
                    // destroy mixed video
                } else if (msg.arg1 == 2) {
                    V2Log.i(TAG, "Successfully received a new mix video callback. callback is : "
                            + "OnDestroyVideoMixerCallback ! Remove Mix Video");
                    MixVideo mv = (MixVideo) (((AsyncResult) msg.obj).getResult());
                    destoryMixVideo(mv);
                    mMixVideoChildCount = 0;
                    // 移除混合视频中的子视频
                } else if (msg.arg1 == 4) {
                    if (!isMixVideoShow) {
                        return;
                    }
                    mMixVideoChildCount--;
                    MixVideo.MixVideoDevice mvd = (MixVideo.MixVideoDevice) (((AsyncResult) msg.obj).getResult());
                    String id = mvd.getMx().getId();
                    AttendeeMixedDevice attendee = (AttendeeMixedDevice) mAttendeeMap.get(id.hashCode());
                    if (attendee != null) {
                        attendee.removeUsingDevice(mvd);
                        if (mMixVideoChildCount == 0) {
                            removeSurfaceView(id);

                            List<UserDeviceConfig> dList = GlobalHolder.getInstance()
                                    .getAttendeeDevice(attendee.getAttId());
                            showOrCloseMixVideo(dList.get(0));
                        }
                    }
                    // 添加混合视频中的子视频
                } else if (msg.arg1 == 3) {
                    if (!isMixVideoShow) {
                        return;
                    }

                    mMixVideoChildCount++;
                    MixVideo.MixVideoDevice mvd = (MixVideo.MixVideoDevice) (((AsyncResult) msg.obj).getResult());
                    String id = mvd.getMx().getId();
                    AttendeeMixedDevice attendee = (AttendeeMixedDevice) mAttendeeMap.get(id.hashCode());
                    if (attendee != null) {
                        attendee.setUsingDevice(mvd);
                    }
                }
                break;
            case CHAIR_MAN_CHANGE_LISTENER:
                RequestConfChairManChange chair = (RequestConfChairManChange) (((AsyncResult) msg.obj).getResult());
                if (chair.confID == conf.getId()) {
                    conf.setChairman(chair.chairManID);

                    for (Attendee attendee : mAttendeeList) {
                        if (attendee.isChairMan()) {
                            attendee.setChairMan(false);
                            break;
                        }
                    }

                    Attendee newChair = mAttendeeMap.get((int) chair.chairManID);
                    if (newChair != null) {
                        newChair.setChairMan(true);
                        if (newChair.getAttId() != GlobalHolder.getInstance().getCurrentUserId()) {
                            mChairmanControl.setVisibility(View.INVISIBLE);
                        } else {
                            mChairmanControl.setVisibility(View.VISIBLE);
                            updateSpeakerState(true);
                        }

                        if (mAttendeeContainer != null) {
                            mAttendeeContainer.updateDisplay();
                        }
                    }
                }
                break;
            case CHAIR_MAN_OPEN_LISTENER:
                if (isChairManVideoOpen == null) {
                    adjustContentLayout();
                    mHandler.postDelayed(voiceActiveRunnable, 500);
                } else {
                    // 如果主席视频已经打开,需要检测默认视频是否已经有变化
                    long chairman = conf.getChairman();
                    Attendee attendee = mAttendeeMap.get((int) chairman);
                    if (attendee != null) {
                        List<UserDeviceConfig> devices = getAttendeeDevices(attendee);
                        if (devices != null) {
                            for (int i = 0; i < devices.size(); i++) {
                                final UserDeviceConfig temp = devices.get(i);
                                if (temp.isDefault()) {
                                    if (!temp.getDeviceID().equals(isChairManVideoOpen)) {
                                        isChairManVideoOpen = temp.getDeviceID();
                                        removeSurfaceView(attendee.getAttId());
                                        adjustContentLayout();
                                        mHandler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, temp).sendToTarget();
                                            }
                                        }, 1000);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case ATTENDEE_OPEN_VIDEO:
                Log.e(TAG, "attendee open video");
                UserDeviceConfig udc = (UserDeviceConfig) msg.obj;
                openAttendeeVideo(udc);
                break;
            case ATTENDEE_REMOVE_VIDEO:
                Log.e(TAG, "attendee remove video");
                String deviceID = (String) msg.obj;
                removeSurfaceView(deviceID);
                break;
        }
    }

    LinearLayout video_layout2;

    @Override
    public void initViewAndListener() {
        setContentView(R.layout.activity_in_metting2);
        unbinder = ButterKnife.bind(this);
        video_layout2 = (LinearLayout) findViewById(R.id.video_layout2);
        mRootContainer = (RelativeLayout) findViewById(R.id.video_layout_root);
        mContentLayoutMain = (FrameLayout) findViewById(R.id.in_meeting_content_main);
        // 用于隐藏输入法
        mContentLayoutMain.setOnTouchListener(mContentLayoutMainTouchListener);

        mSubWindowLayout = new FrameLayout(mContext);
        mContentLayoutMain.addView(mSubWindowLayout);
        mSubWindowLayout.setVisibility(View.GONE);

        mTitleBar = findViewById(R.id.in_meeting_tools_bar);

        // -----init title tool , left to right-----

        /**
         * Control left menu display and hide.. menu list: show invitation
         * layout button show message layout button show attendee list layout
         * button show document layout button
         */
        mMenuButton = (ImageView) findViewById(R.id.in_meeting_menu_button);
        mMenuButton.setOnClickListener(mMenuButtonListener);

        // show current conference name
        mGroupNameTV = (TextView) findViewById(R.id.in_meeting_name);

        /**
         *
         * This component has the following features 1.Control the message
         * 2.Control whether have voice 3.Control invite permissions
         */
        mChairmanControl = (ImageView) findViewById(R.id.iv_chairman_control);
        mChairmanControl.setOnClickListener(mChairmanControlButtonOnClickListener);
        // unread flag
        mMsgNotification = findViewById(R.id.host_request_msg_notificator);
        mMsgNotification.setVisibility(View.GONE);

        /**
         *
         * This component has the following features 1.Control the permission of
         * the speaker's apply and release 2.Control local video quality 3.exit
         * meeting.
         */
        mMoreIV = findViewById(R.id.in_meeting_feature);
        mMoreIV.setOnClickListener(mMoreIVOnClickListener);

        // request speak or mute button
        mSpeakerIV = (ImageView) findViewById(R.id.speaker_iv);
        mSpeakerIV.setOnClickListener(mSpeakIVOnClickListener);

        // control local camera
        mCameraIV = (ImageView) findViewById(R.id.iv_camera);
        mCameraIV.setOnClickListener(onClickMCameraIV);

        text_close_camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WaitDialogBuilder.showNormalWithHintProgress(ConferenceActivity2.this);
                startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_NOTIFY_TEACHER_LEAVE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WaitDialogBuilder.dismissDialog();
                        isOpByMine = true;
                        finish();
                    }
                }, 2000);

            }
        });
        //text_close_camera.setOnClickListener(onClickMCameraIV);
        // ----init title tool ----

        // ----------- left menu list ------------

        mMenuButtonContainer = findViewById(R.id.in_meeting_menu_layout);
        mMenuSparationLine = findViewById(R.id.in_meeting_video_separation_line0);

        // show invition button
        mMenuInviteAttendeeButton = findViewById(R.id.in_meeting_menu_show_invition_attendees_button);
        mMenuInviteAttendeeButton.setTag(LEFT_MENU_BUTTON_INVITE);
        mMenuInviteAttendeeButton.setOnClickListener(mMenuItemButtonOnClickListener);

        // show attendee list layout button
        mMenuAttendeeButton = findViewById(R.id.in_meeting_menu_show_attendees_button);
        mMenuAttendeeButton.setTag(LEFT_MENU_BUTTON_ATTENDEE);
        mMenuAttendeeButton.setOnClickListener(mMenuItemButtonOnClickListener);

        // show message layout button
        mMenuMessageButton = findViewById(R.id.in_meeting_menu_show_msg_button);
        mMenuMessageButton.setTag(LEFT_MENU_BUTTON_CHAT);
        mMenuMessageButton.setOnClickListener(mMenuItemButtonOnClickListener);
        // unread flag
        mChatMsgNotification = findViewById(R.id.chat_request_msg_notificator);
        mChatMsgNotification.setVisibility(View.GONE);

        // show document display button
        mMenuDocButton = findViewById(R.id.in_meeting_menu_show_doc_button);
        mMenuDocButton.setTag(LEFT_MENU_BUTTON_DOC);
        mMenuDocButton.setOnClickListener(mMenuItemButtonOnClickListener);

        mMenuAppShareButton = findViewById(R.id.in_meeting_menu_show_app_share_button_ly);
        mMenuAppShareButton.setTag(LEFT_MENU_BUTTON_APPSHARE);
        mMenuAppShareButton.setOnClickListener(mMenuItemButtonOnClickListener);
        mAppShareButtonNotification = findViewById(R.id.in_meeting_menu_show_app_share_button_notificator);

        mMenuButtonGroup = new View[]{mMenuInviteAttendeeButton, mMenuMessageButton, mMenuAttendeeButton,
                mMenuDocButton, mMenuAppShareButton};

        // ----------- left menu list ------------

        // ------------ local camera surface view -----------------------
        mLocalSurface = (ConferenceSurfaceView) findViewById(R.id.local_surface_view);
        //mLocalSurface.setZOrderOnTop(false);
        //mLocalSurface.setZOrderMediaOverlay(false);
        mLocalSurface.getHolder().addCallback(mLocalCameraSHCallback);

        mLocalSurfaceViewLy = (RelativeLayout) findViewById(R.id.local_surface_view_ly);
        //mLocalSurfaceViewLy.setOnTouchListener(mLocalCameraOnTouchListener);

        mConverseLocalCameraButton = findViewById(R.id.converse_camera_button);
        mConverseLocalCameraButton.setOnClickListener(mConverseCameraOnClickListener);
        // ------------ local camera surface view -----------------------

        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mAppShareSurfaceViewLy = (RelativeLayout) findViewById(R.id.ws_conference_appShare_surfaceView_ly);
        mAppShareSurfaceView = (ConferenceSurfaceView) findViewById(R.id.ws_conference_appShare_surfaceView);
        mAppShareSurfaceView.setTag("exist");
        mAppShareSurfaceView.setZOrderMediaOverlay(true);
        mAppShareDeleteBT = findViewById(R.id.ws_conference_appShare_surfaceView_delete);
        mAppShareDeleteBT.setOnClickListener(mAppShareDeleteBTOclick);
        mAppShareSurfaceView.getHolder().addCallback(mAppShareSHCallback);
        mAppShareSurfaceViewLy.setTag(new VideoPlayer());

        addNormalPlaceView();
        startTimer();
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    private boolean initConferenceDate() {
        conf = (Conference) this.getIntent().getExtras().get("conf");
        cg = (ConferenceGroup) GlobalHolder.getInstance().getGroupById(conf.getId());
        if (cg == null) {
            //创建一个conferenceGroup 11
            User owner = GlobalHolder.getInstance().getUser(conf.getCreator());
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = sf.parse(conf.getStartTimeStr());
                cg = new ConferenceGroup(11, "tvl", owner, date, owner);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //V2Log.e("Get null ConferenceGroup Object from GlobleHoder , the programm had destory!");
            //return false;
        }

        initCourseStartTime();
        initAttendeeList(cg);
        if (currentAttendee == null) {
            currentAttendee = new Attendee(GlobalHolder.getInstance().getCurrentUser());
            currentAttendee.setSelf(true);
            putNewAttendee(currentAttendee);
        }

//        currentAttendee.setChairMan(GlobalHolder.getInstance()
// .findGroupById(conf.getId()).getOwnerUser()
//                .getmUserId() == currentAttendee.getAttId());

        currentAttendee.setChairMan(conf.getCreator() == currentAttendee.getAttId());

        // 默认设置自己不是主讲，主讲会在入会后广播过来
        if (currentAttendee.isChairMan()) {
            currentAttendee.setLectureState(Attendee.LECTURE_STATE_GRANTED);
        } else {
            currentAttendee.setLectureState(Attendee.LECTURE_STATE_NOT);
        }

        currentAttendee.setSpeakingState(currentAttendee.isChairMan());
        return true;
    }

    private boolean mLocalCameraChangeEnd = true;
    private int mLocalCameraButtom;
    private int mLocalCameraRight;

    private void initService() {
        AnimationHepler.getInstance().setAnimaListener(new AnimationHepler.AnimaListener() {
            @Override
            public void animaLoadEnd(int animaTargetID, Animation animation) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        mLocalSurfaceViewLy.getLayoutParams();
                if (mAttendeeContainer.mIsOwnerVideoOpen) {
                    layoutParams.bottomMargin = mLocalCameraButtom;
                    layoutParams.rightMargin = mLocalCameraRight;
                } else {
                    mLocalCameraButtom = layoutParams.bottomMargin;
                    mLocalCameraRight = layoutParams.rightMargin;
                    layoutParams.bottomMargin = -layoutParams.height;
                    layoutParams.rightMargin = -layoutParams.width;
                }
                ((ViewGroup) mLocalSurfaceViewLy.getParent()).updateViewLayout(mLocalSurfaceViewLy, layoutParams);
                mLocalCameraChangeEnd = true;
            }
        });
        CommonCallBack.getInstance().setNotifyChatInterToReplace(this);
        bindService(new Intent(mContext, ConferencMessageSyncService.class), mLocalServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void init() {
        // 当用户进入会议，通知点对点音视频界面退出
        Intent i = new Intent(PublicIntent.BROADCAST_JOINED_CONFERENCE_NOTIFICATION);
        i.addCategory(PublicIntent.DEFAULT_CATEGORY);
        i.putExtra("confid", conf.getId());
        sendBroadcast(i);

        mGroupNameTV.setText(cg.getName());
        mPendingPermissionUpdateList = new ArrayList<>();
        mHostRequestUsers = new HashSet<>();
        mCurrentShowedSV = new ArrayList<>();

        popupRootView = (RelativeLayout) View.inflate(mContext, R.layout.in_meeting_setting_pop_up_window, null);

        if (blueadapter != null
                && (BluetoothProfile.STATE_CONNECTED == blueadapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                || BluetoothProfile.STATE_CONNECTED == blueadapter
                .getProfileConnectionState(BluetoothProfile.HEALTH)
                || BluetoothProfile.STATE_CONNECTED == blueadapter.getProfileConnectionState(BluetoothProfile.A2DP))) {
            isBluetoothHeadsetConnected = true;
        }

        if (cg.getOwnerUser().getmUserId() != GlobalHolder.getInstance().getCurrentUserId()) {
            mChairmanControl.setVisibility(View.INVISIBLE);
        }
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(mDisMetr);
    }

    private void initAttendeeList(Group confGroup) {
        List<User> l = confGroup.getUsers();
        for (User u : l) {
            if (u.getmUserId() != GlobalHolder.getInstance().getCurrentUserId()) {
                Attendee at = new Attendee(u);
                putNewAttendee(at);
            }
        }
    }

    /**
     * Update speaker icon and state
     *
     * @param flag
     */
    private void updateSpeakerState(boolean flag) {
        currentAttendee.setSpeakingState(flag);
        // set flag to speaking icon
        if (!flag) {
            mSpeakerIV.setImageResource(R.drawable.btn_conference_titlebar_mute_close);
        } else {
            mSpeakerIV.setImageResource(R.drawable.btn_conference_titlebar_mute_open);
        }
    }

    private void showCurrentAttendeeLectureStateToast(PermissionState newLectureState, boolean reject) {
        int oldLectureState = currentAttendee.getLectureState();
        if (oldLectureState == Attendee.LECTURE_STATE_APPLYING) {
            if (newLectureState == PermissionState.GRANTED) {
                isFreeMode = false;
                // 同意主讲申请
                V2Toast.makeText(mContext, R.string.confs_toast_get_control_permission, Toast.LENGTH_SHORT).show();
                // Apply speaking
                if (!currentAttendee.isSpeaking()) {
                    doApplyOrReleaseSpeak(!currentAttendee.isSpeaking());
                    // Make sure update start after send request,
                    // because update state will update isSpeaking value
                    updateSpeakerState(!currentAttendee.isSpeaking());
                }
            } else if (newLectureState == PermissionState.NORMAL) {
                if (reject) {
                    // 20141221 1 拒绝主讲申请
                    Toast.makeText(mContext, R.string.confs_toast_reject_control_permission, Toast.LENGTH_SHORT).show();
                } else {
                    // 20141221 1 取消主讲申请
                    Toast.makeText(mContext, R.string.confs_toast_cancel_control_permission, Toast.LENGTH_SHORT).show();

                }
            }

        } else if (oldLectureState == Attendee.LECTURE_STATE_GRANTED) {
            if (newLectureState == PermissionState.NORMAL) {
                // 主动释放主讲
                Toast.makeText(mContext, R.string.confs_toast_release_control_permission, Toast.LENGTH_SHORT).show();
            }
        } else if (oldLectureState == Attendee.LECTURE_STATE_NOT) {
            if (newLectureState == PermissionState.APPLYING) {// 申请中
                // 如果不是主席才提示
                if (!currentAttendee.isChairMan()) {
                    Toast.makeText(mContext, R.string.confs_title_button_request_host_name, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateMCameraIVState(boolean flag) {
        Drawable drawableTop = null;
        // set flag to speaking icon
        if (flag) {
            mCameraIV.setImageResource(R.drawable.btn_conference_titlebar_camera_open);
            drawableTop = getResources().getDrawable(R.drawable.n_camera);
            openLocalCamera();
            mLocalSurfaceViewLy.setVisibility(View.VISIBLE);
        } else {
            mCameraIV.setImageResource(R.drawable.btn_conference_titlebar_camera_close);
            drawableTop = getResources().getDrawable(R.drawable.n_camera_off);
            closeLocalCamera();
            mLocalSurfaceViewLy.setVisibility(View.GONE);
        }
        text_close_camera.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
    }

    /**
     * 用于显示或隐藏左侧栏按钮的子布局
     *
     * @param content
     * @param isDoc      该标记用于区分是否是点击文档按钮进行的操作
     * @param isAppShare 该标记用于区分是否是点击应用程序共享按钮进行的操作
     */
    private void showOrHideSubWindow(final View content, final boolean isDoc, boolean isAppShare) {
        View currentChild = null;
        if (mSubWindowLayout.getChildCount() > 0) {
            currentChild = mSubWindowLayout.getChildAt(0);
        }

        if (content == null && currentChild == null) {
            return;
        }

        int visible = View.VISIBLE;
        // Update content
        if (currentChild != content) {
            if (isAppShare) {
                return;
            }

            if (currentChild != null) {
                String tag = (String) currentChild.getTag();
                if (tag.equals(LEFT_MENU_BUTTON_INVITE)) {
                    mInvitionContainer.hideKeyBoard();
                } else if (tag.equals(LEFT_MENU_BUTTON_ATTENDEE)) {
                    mAttendeeContainer.hideKeyBoard();
                } else if (tag.equals(LEFT_MENU_BUTTON_CHAT)) {
                    mMessageContainer.hideKeyBoard();
                }
            }
            mSubWindowLayout.removeAllViews();
            if (content != null) {
                FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                fl.leftMargin = 0;
                fl.topMargin = 0;
                mSubWindowLayout.addView(content, fl);
            }
            // If content is different, always visible
            visible = View.VISIBLE;
        } else {
            // Otherwise check current visibility state.
            if (mSubWindowLayout.getVisibility() == View.VISIBLE) {
                String tag = (String) currentChild.getTag();
                if (tag.equals(LEFT_MENU_BUTTON_INVITE)) {
                    mInvitionContainer.hideKeyBoard();
                } else if (tag.equals(LEFT_MENU_BUTTON_ATTENDEE)) {
                    mAttendeeContainer.hideKeyBoard();
                } else if (tag.equals(LEFT_MENU_BUTTON_CHAT)) {
                    mMessageContainer.hideKeyBoard();
                }
                visible = View.GONE;
            } else if (mSubWindowLayout.getVisibility() == View.GONE) {
                visible = View.VISIBLE;
            }
        }

        // Show or hide sub window with animation
        Animation animation;
        if (visible == View.GONE) {
            animation = new ScaleAnimation(1.0F, 0.0f, 1.0F, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f);
        } else {
            animation = new ScaleAnimation(0.0F, 1.0f, 1.0F, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f);
        }
        animation.setDuration(400);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isDoc) {
                    // If current user is chairman or has get host rights then
                    // show shared doc button
                    if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                        if (content != null) {
                            ((LeftShareDocLayout) content).updateLectureStateGranted(true);
                        }

                        if (mCurrentLecturerActivateDocIdStr != null) {
                            V2Doc v2doc = mDocs.get(mCurrentLecturerActivateDocIdStr);
                            if (v2doc != null
                                    && !v2doc.getActivatePage().getDocId().equals(mCurrentLecturerActivateDocIdStr)) {
                                v2ConferenceRequest.modifyGroupLayout(conf);
                                ds.switchDoc(currentAttendee.getAttId(), mDocs.get(mCurrentLecturerActivateDocIdStr),
                                        true, null);
                            }
                        }
                    } else {
                        if (content != null) {
                            ((LeftShareDocLayout) content).updateLectureStateGranted(false);
                        }
                    }
                }
                isChangingSubLayout = false;
            }
        });

        mSubWindowLayout.setVisibility(visible);
        mSubWindowLayout.startAnimation(animation);
        adjustContentLayout();
    }

    private View initMsgLayout() {
        if (mMessageContainer == null) {
            mMessageContainer = new LeftMessageChattingLayout(this, cg);
            mMessageContainer.setListener(mLeftSubViewListener);
            mMessageContainer.requestScrollToNewMessage();
        }
        return mMessageContainer;
    }

    private View initAttendeeContainer() {
        if (mAttendeeContainer == null) {
            mAttendeeContainer = new LeftAttendeeListLayout(conf, this);
            mAttendeeContainer.setAttendsList(mAttendeeList);

            mAttendeeContainer.bringToFront();
            mAttendeeContainer.setListener(mLeftSubViewListener);

            // Initialize speaking
            if (currentAttendee.isSpeaking()) {
                currentAttendee.setSpeakingState(true);
                mAttendeeContainer.updateDisplay();
            }
            // Update pending attendee state
            for (PermissionUpdateIndication ind : mPendingPermissionUpdateList) {
                updateAttendeePermissionState(ind);
            }

            mPendingPermissionUpdateList.clear();
        } else {
            mAttendeeContainer.updateStatist();
        }
        return mAttendeeContainer;
    }

    private View initDocLayout(boolean isLeftMenuClick) {
        if (mDocContainer == null) {
            if (!isLeftMenuClick) {
                mDocContainer = new LeftShareDocLayout(this, mDocs, mCurrentLecturerActivateDocIdStr, false);
            } else {
                mDocContainer = new LeftShareDocLayout(this, mDocs, mCurrentLecturerActivateDocIdStr, true);
            }
            mDocContainer.setListener(mLeftSubViewListener);

            Group g = GlobalHolder.getInstance().findGroupById(conf.getId());
            if (g != null && g instanceof ConferenceGroup) {
                mDocContainer.updateSyncStatus(((ConferenceGroup) g).isSyn()
                        && (currentAttendee.getLectureState() != Attendee.LECTURE_STATE_GRANTED));
                mDocContainer.updateCurrentDoc();
            }
        } else {
            mDocContainer.updateCurrentDoc();
        }
        return mDocContainer;
    }

    private View initInvitionContainer() {
        if (mInvitionContainer == null) {
            mInvitionContainer = new LeftInvitionAttendeeLayout(this, conf);
            mInvitionContainer.setListener(mLeftSubViewListener);

        }
        return mInvitionContainer;
    }

    /**
     * Call this before {@link #adjustContentLayout}
     */
    private void requestSubViewFixed() {
        Integer flag = (Integer) mSubWindowLayout.getTag();
        if (flag == null) {
            mSubWindowLayout.setTag(Integer.valueOf(TAG_SUB_WINDOW_STATE_FIXED));
        } else {
            mSubWindowLayout.setTag(Integer.valueOf(flag.intValue() | TAG_SUB_WINDOW_STATE_FIXED));
        }

    }

    /**
     * Call this before {@link #adjustContentLayout}
     */
    private void requestSubViewFloat() {
        Integer flag = (Integer) mSubWindowLayout.getTag();
        if (flag == null) {
            mSubWindowLayout.setTag(TAG_SUB_WINDOW_STATE_FLOAT);
        } else {
            mSubWindowLayout.setTag(flag & TAG_SUB_WINDOW_STATE_FLOAT);
        }
    }

    /**
     * Call this before {@link #adjustContentLayout}
     */
    private void requestSubViewFillScreen() {
        Integer flag = (Integer) mSubWindowLayout.getTag();
        if (flag == null) {
            mSubWindowLayout.setTag(TAG_SUB_WINDOW_STATE_FULL_SCRREN);
        } else {
            mSubWindowLayout.setTag(flag | TAG_SUB_WINDOW_STATE_FULL_SCRREN);
        }
    }

    /**
     * Call this before {@link #adjustContentLayout}
     */
    private void requestSubViewRestore() {
        Integer flag = (Integer) mSubWindowLayout.getTag();
        if (flag == null) {
            mSubWindowLayout.setTag(TAG_SUB_WINDOW_STATE_RESTORED);
        } else {
            mSubWindowLayout.setTag(flag & TAG_SUB_WINDOW_STATE_RESTORED);
        }
    }

    private int getSubViewWindowState() {
        Integer flag = (Integer) mSubWindowLayout.getTag();
        if (flag == null) {
            return 0;
        } else {
            return flag;
        }
    }

    private void updateMoreWindowDisplay() {
        if (mRequestButtonName == null || mRequestButtonImage == null) {
            return;
        }

        if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_APPLYING) {
            mRequestButtonName
                    .setTextColor(mContext.getResources().getColor(R.color.conference_host_requesting_text_color));
            mRequestButtonImage.setImageResource(R.drawable.host_requesting);
            mRequestButtonName.setText(R.string.confs_title_button_request_host_name);
        } else if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
            mRequestButtonName
                    .setTextColor(mContext.getResources().getColor(R.color.conference_acquired_host_text_color));
            mRequestButtonImage.setImageResource(R.drawable.host_required);
            mRequestButtonName.setText(R.string.confs_title_button_release_host_name);
        } else if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_NOT) {
            mRequestButtonName.setTextColor(mContext.getResources().getColor(R.color.common_item_text_color_black));
            mRequestButtonImage.setImageResource(R.drawable.host_request);
            mRequestButtonName.setText(R.string.confs_title_button_request_host_name);
        }
    }

    private void showConferenceMsgDialog() {
        if (mConferenceMsgDialog == null) {
            mConferenceMsgDialog = new ConferenceMsgDialog(mContext, mHostRequestUsers, v2ConferenceRequest);
        } else {
            mConferenceMsgDialog.resetList(mHostRequestUsers);
        }

        mConferenceMsgDialog.show();

        mMsgNotification.setVisibility(View.GONE);
        hasUnreadChiremanControllMsg = false;
    }

    @SuppressWarnings("deprecation")
    private void showLocalSurViewOnly() {
        UserDeviceConfig defDevice = null;
        List<UserDeviceConfig> attendeeDevices = GlobalHolder.getInstance()
                .getAttendeeDevice(currentAttendee.getAttId());
        if (attendeeDevices != null) {
            boolean isAdd = true;
            for (int i = 0; i < attendeeDevices.size(); i++) {
                UserDeviceConfig temp = attendeeDevices.get(i);
                if (temp.isDefault()) {
                    defDevice = temp;
                    isAdd = false;
                    break;
                }
            }

            if (isAdd) {
                defDevice = new UserDeviceConfig(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(),
                        currentAttendee.getAttId(), "", null);
                // Make sure current user device is enable
                defDevice.setEnable(true);
                attendeeDevices.add(defDevice);
            }
        } else {
            defDevice = new UserDeviceConfig(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(),
                    currentAttendee.getAttId(), "", null);
            // Make sure current user device is enable
            defDevice.setEnable(true);
            ArrayList<UserDeviceConfig> deveces = new ArrayList<>();
            deveces.add(defDevice);
            GlobalHolder.getInstance().updateUserDevice(currentAttendee.getAttId(), deveces);
        }

        // layout must before open device
        defDevice.setSVHolder(mLocalSurface);
        LayoutParams layoutParams = mLocalSurfaceViewLy.getLayoutParams();
        int mPreLayoutWidth = layoutParams.width;
        int mPreLayoutHeight = layoutParams.height;
        GlobalConfig.setVideoRecordParams(mContext, mLocalSurface.getHolder());
        VideoCaptureDevInfo captureInfo = VideoCaptureDevInfo.CreateVideoCaptureDevInfo();
        if (captureInfo != null) {
            int level = LocalSharedPreferencesStorage.getConfigIntValue(mContext,
                    String.valueOf(GlobalHolder.getInstance().getCurrentUserId()) + ":videoMass",
                    V2GlobalConstants.CONF_CAMERA_MASS_LOW);
            //GlobalConfig.setGlobalVideoLevel(mContext, V2GlobalConstants.CONF_CAMERA_CUSTOMER);
//            openLocalCamera();
            defDevice.setShowing(true);
        }
    }

    /**
     * 设置默认视频参数
     */
    private setPBeans setDefultType() {
        setPBeans beans = null;
        beans = new setPBeans(512, 15, 768, 432, 2, 2);
        return beans;

    }

    private void setVideoConfig() {
        setPBeans beans = null;
        boolean f = (DataUtil.getData("MediaEncodeType", ConferenceActivity2.this) == null);
        if (f) {//服务器上不存在就配置默认值
            beans = setDefultType();

        } else {//取服务器数据
            queryPhoneInfoBean mqueryPhoneInfoBean = (queryPhoneInfoBean) DataUtil.getData("MediaEncodeType", ConferenceActivity2.this);
            beans = setVideoType(mqueryPhoneInfoBean);
        }
        MainApplication.setP(beans);
        GlobalConfig.setGlobalVideoLevel(ConferenceActivity2.this, V2GlobalConstants.CONF_CAMERA_CUSTOMER);
    }

    /**
     * 设置动态获取的视频参数
     */
    private setPBeans setVideoType(queryPhoneInfoBean mqueryPhoneInfoBean) {
        setPBeans beans = null;
        int malv;
        int zhenlv;
        String resolving;
        malv = Integer.parseInt(mqueryPhoneInfoBean.getP_p_rate());
        zhenlv = Integer.parseInt(mqueryPhoneInfoBean.getP_p_frame());
        resolving = mqueryPhoneInfoBean.getP_p_resolving();
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

    private List<UserDeviceConfig> getAttendeeDevices(Attendee att) {
        return GlobalHolder.getInstance().getAttendeeDevice(att.getAttId());
    }

    private void closeLocalCamera() {
        isOpeningLocal = false;
        try {
            Message.obtain(mHandler, REQUEST_OPEN_OR_CLOSE_DEVICE, 0, 0,
                    new UserDeviceConfig(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(),
                            GlobalHolder.getInstance().getCurrentUserId(), "", null))
                    .sendToTarget();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLocalCamera() {
        if (isOpeningLocal) {
            return;
        }
        isOpeningLocal = true;
        V2Log.i(TAG, "开始打开本地视频！");
        Message.obtain(mHandler, REQUEST_OPEN_OR_CLOSE_DEVICE, 1, 0,
                new UserDeviceConfig(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(),
                        GlobalHolder.getInstance().getCurrentUserId(), "", null))
                .sendToTarget();
        mHandler.postDelayed(openLocalTimeOut, 5 * 1000);
    }

    private Runnable openLocalTimeOut = new Runnable() {

        @Override
        public void run() {
            if (!VideoRecorder.isOpenCamera && isWindowFocus) {
                V2Log.d("DEBUG", "打开本地视频超时，重新调用本地视频打开！ isOpen : " + VideoRecorder.isOpenCamera
                        + " | isWindowFocus : " + isWindowFocus);
                doReverseCamera();
                doReverseCamera();
            } else {
                V2Log.d("DEBUG", "成功打开本地视频 ！ isOpen : " + VideoRecorder.isOpenCamera);
                VideoRecorder.isOpenCamera = false;
                if (mServiceBound && !isOpenLocalVideoFinish) {
                    v2ConferenceRequest.notifyAllMessage(cg.getGroupID());
                    isOpenLocalVideoFinish = true;
                }
            }
        }
    };

    private int mContentWidth = -1;
    private int mContentHeight = -1;
    private int leftMenuListWidth = -1;
    private int preContentHeight = 0;
    private int mLastButtomMargin = -1;

    public void adjustPan(int height, boolean isChange) {
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) mLocalSurfaceViewLy.getLayoutParams();
        Rect r = new Rect();
        mRootContainer.getDrawingRect(r);
        if (isChange) {
            preContentHeight = mContentHeight;
            mContentHeight = height;

            mLastButtomMargin = rl.bottomMargin;
            rl.bottomMargin = 0;
            ((ViewGroup) mLocalSurfaceViewLy.getParent()).updateViewLayout(mLocalSurfaceViewLy, rl);
        } else {
            if (preContentHeight != 0)
                mContentHeight = preContentHeight;

            if (mLastButtomMargin != -1) {
                rl.bottomMargin = mLastButtomMargin;
                ((ViewGroup) mLocalSurfaceViewLy.getParent()).updateViewLayout(mLocalSurfaceViewLy, rl);
            }
        }
        adjustContentLayout();
        V2Log.d(TAG, "adjustPan --> mContentHeight : " + mContentHeight
                + " isChange : " + isChange);
        if (mMessageContainer != null) {
            int height2 = mMessageContainer.getHeight();
            V2Log.d(TAG, "adjustPan --> finally mContentHeight : " + height2);
        }
    }

    public void adjustContentLayout() {
        int width, height = 0;
        int marginLeft = 0;

        // Calculate offset
        if (mMenuButtonContainer.getVisibility() == View.VISIBLE) {
            if (leftMenuListWidth == -1) {
                mMenuButtonContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                leftMenuListWidth = mMenuButtonContainer.getMeasuredWidth();
                V2Log.i(TAG, "第一次计算左边菜单栏的宽度 ： " + leftMenuListWidth);
            }
            marginLeft = leftMenuListWidth;
        }

        if (mContentWidth == -1 || mContentHeight == -1) {
            mContentLayoutMain.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mContentWidth = mContentLayoutMain.getWidth();
            // mContentHeight = mContentLayoutMain.getHeight();

            int targetHeight;
            if (GlobalConfig.SCREEN_WIDTH > GlobalConfig.SCREEN_HEIGHT) {
                targetHeight = GlobalConfig.SCREEN_HEIGHT;
            } else {
                targetHeight = GlobalConfig.SCREEN_WIDTH;
            }

            mTitleBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int titleHeight = mTitleBar.getHeight();
            Rect rect = new Rect();
            mTitleBar.getWindowVisibleDisplayFrame(rect);
            V2Log.d(TAG, "titleHeight : " + titleHeight);
            V2Log.d(TAG, "titleHeight top : " + rect.top);
            V2Log.d(TAG, "mContentHeight : " + (targetHeight - titleHeight - rect.top));
            // E人E本适配
            Rect contentLayoutMain = new Rect();
            mContentLayoutMain.getGlobalVisibleRect(contentLayoutMain);
            V2Log.d(TAG, "mContentLayoutMain top : " + contentLayoutMain.top);
            V2Log.d(TAG, "mContentLayoutMain buttom : " + contentLayoutMain.bottom);

            mContentHeight = contentLayoutMain.bottom - contentLayoutMain.top;
            V2Log.i(TAG, "第一次计算mContentWidth的宽度 ： " + mContentWidth);
            V2Log.i(TAG, "第一次计算mContentHeight的高度 ： " + mContentHeight);
        }

        if (mSubWindowLayout.getVisibility() == View.VISIBLE) {
            FrameLayout.LayoutParams subWindowParams = (FrameLayout.LayoutParams) mSubWindowLayout.getLayoutParams();

            int flag = getSubViewWindowState();
            // If sub window request full screen
            if ((flag & TAG_SUB_WINDOW_STATE_FULL_SCRREN) == TAG_SUB_WINDOW_STATE_FULL_SCRREN) {
                width = (mContentWidth - marginLeft);
            } else {
                width = (mContentWidth - marginLeft) / 2;
            }
            height = mContentHeight;

            V2Log.i(TAG, "最终菜单子布局 mContentWidth ：" + width);
            V2Log.i(TAG, "最终菜单子布局mContentHeight ：" + height);

            if (subWindowParams == null) {
                subWindowParams = new FrameLayout.LayoutParams(width, height);
            } else {
                subWindowParams.width = width;
                subWindowParams.height = height;
            }
            subWindowParams.leftMargin = marginLeft;
            mContentLayoutMain.updateViewLayout(mSubWindowLayout, subWindowParams);

            // Update left offset for video layout
            if ((flag & TAG_SUB_WINDOW_STATE_FIXED) == TAG_SUB_WINDOW_STATE_FIXED) {
                marginLeft += width;
            }
        }

        V2Log.i(TAG, "布局调整 ， mContentWidth ：" + mContentWidth);
        V2Log.i(TAG, "布局调整 ， mContentHeight ：" + mContentHeight);
        V2Log.i(TAG, "布局调整 ， marginLeft ：" + marginLeft);

        // Update width and height for video layout
        if (judgeIsBigPad())
            width = GlobalConfig.SCREEN_WIDTH - marginLeft;
        else
            width = GlobalConfig.SCREEN_HEIGHT - marginLeft;
        height = mContentHeight;

        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(width, height);
        videoParams.leftMargin = marginLeft;
        mVideoLayout.setLayoutParams(videoParams);
        adjustVideoLayout(width, height);
    }

    public void adjustVideoLayout(int containerW, int containerH) {
//        int marginTop = 0;
//        int marginLeft = 0;
//        int size = mCurrentShowedSV.size();
//        // 横向排列
//        // int rows = size / mVideoMaxCols + (size % mVideoMaxCols == 0 ? 0:1);
//        // int cols = size > 1 ? mVideoMaxCols : size;
//        // 纵向排列
//        int rows = size > 1 ? mVideoMaxRows : size;
//        int cols = size / mVideoMaxRows + (size % mVideoMaxRows == 0 ? 0 : 1);
//        if (size == 0) {
//            V2Log.e(TAG, "adjustVideoLayout --> No remote device need to show size:" + size);
//            return;
//        }
//
//        V2Log.i(TAG, "----------------start--------------------------");
//        V2Log.i(TAG, "视频块总体布局  width = " + containerW);
//        V2Log.i(TAG, "视频块总体布局  height = " + containerH);
//        V2Log.i(TAG, "视频块总体布局   marginLeft = " + marginLeft);
//        V2Log.i(TAG, "开始调整视频布局  , 视频数  = " + size);
//        V2Log.i(TAG, "开始调整视频布局  , 行数  = " + rows);
//        V2Log.i(TAG, "开始调整视频布局  , 列数  = " + cols);
//
//        // int[] po = new int[2];
//        // mVideoLayout.getLocationInWindow(po);
//        // will set layout width parameter. At here measure doesn't work with
//        // layoutparameter
//        int normalW = containerW / cols, normalH = normalW / 4 * 3;
//        if (normalH * rows > containerH) {
//            normalH = containerH / rows;
//            normalW = normalH / 3 * 4;
//        }
//
//        int fixedWidth = normalW;
//        int fixedHeight = normalH;
//        fixedWidth -= fixedWidth % 16;
//        fixedHeight -= fixedHeight % 16;
//
//        marginTop = Math.abs(containerH - fixedHeight * rows) / 2;
//        marginLeft = Math.abs(containerW - fixedWidth * cols) / 2;
//
//        int index = 0;
//        for (SurfaceViewConfig sw : mCurrentShowedSV) {
//            RelativeLayout v = sw.getView();
//            RelativeLayout.LayoutParams childP = new RelativeLayout.LayoutParams(
//                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//            ConferenceSurfaceView surfaceChild = (ConferenceSurfaceView) v.getChildAt(0);
//            surfaceChild.setLayoutParams(childP);
//            V2Log.d(TAG, "conference surface fixedWidth : " + fixedWidth + " | fixedHeight : " + fixedHeight);
//            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(fixedWidth, fixedHeight);
//            // int row = index / mVideoMaxCols;
//            // int column = index % mVideoMaxCols;
//            int column = index / mVideoMaxRows;
//            int row = index % mVideoMaxRows;
//            p.leftMargin = marginLeft + column * fixedWidth;
//            p.topMargin = marginTop + row * fixedHeight;
//            mVideoLayout.updateViewLayout(v, p);
//            index++;
//        }
//        V2Log.i(TAG, "----------------over--------------------------");
    }

    /**
     * 标准的会议提示对话框
     *
     * @param content
     */
    private void showQuitDialog(String content) {
        if (mQuitDialog == null) {
            Resources res = getResources();
            mQuitDialog = DialogManager.getInstance()
                    .showNormalModeDialog(DialogManager.getInstance().new DialogInterface(mContext,
                            res.getString(R.string.in_meeting_quit_notification), content,
                            res.getString(R.string.in_meeting_quit_quit_button),
                            res.getString(R.string.in_meeting_quit_cancel_button)) {

                        @Override
                        public void confirmCallBack() {
                            mQuitDialog.dismiss();
                            WaitDialogBuilder.showNormalWithHintProgress(ConferenceActivity2.this);
                            //结束当前课时
                            String uid = GlobalHolder.getInstance().getCurrentUser().getTvlUid();
                            // Log.e(TAG, "uid-->" + uid + " confId-->" + conf.getId() + " carrange_id-->" + carrange_id);
                            BussinessManger.getInstance(ConferenceActivity2.this).closeCourseTime(uid
                                    , CourseInfoSingleton.getCourseInfo().getCarrange_id(), CourseInfoSingleton.getCourseInfo().getCourse_id(), "" + conf.getId(), new SimpleResponseListener<BaseResponse>() {
                                        @Override
                                        protected void onSuccess(BaseResponse t) {
                                            Log.e(TAG, "closeCourseTime onSuccess-->" + t.toString());
                                            ToastUtil.ShowToast_short(ConferenceActivity2.this, "该课时已结束");
                                            startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_NOTIFY_CLOSE_CLASS);

                                            //通知页面刷新
                                            Intent intent = new Intent(HTab0.ON_PAGE_REFRESH_ACTION);
                                            sendBroadcast(intent);
                                        }

                                        @Override
                                        protected void onError(ErrorResponse response) {
                                            Log.e(TAG, "closeCourseTime onError-->" + response.toString());
                                            ToastUtil.ShowToast_short(ConferenceActivity2.this, "结束课时失败");
                                        }
                                    });

                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    WaitDialogBuilder.dismissDialog();
                                    isOpByMine = true;
                                    finish();
                                }
                            }, 2000);

                        }

                        @Override
                        public void cannelCallBack() {
                            mQuitDialog.dismiss();
                        }
                    });
        }
        DialogManager.getInstance().setDialogContent(content);
        mQuitDialog.show();
    }

    /**
     * 进入会议时，提示用户是否禁用本地视频
     */
    private void showMuteCameraDialog() {
        if (mCameraDialog == null) {
            Resources res = getResources();
            mCameraDialog = DialogManager.getInstance()
                    .showNormalModeDialog(DialogManager.getInstance().new DialogInterface(mContext,
                            res.getString(R.string.in_meeting_quit_notification),
                            res.getString(R.string.in_meeting_mute_hit_content),
                            res.getString(R.string.in_meeting_mute_button),
                            res.getString(R.string.in_meeting_mute_cancel_button)) {

                        @Override
                        public void confirmCallBack() {
                            isMuteCamera = true;
                            v2ConferenceRequest.enableVideoDev("", false);
                            updateMCameraIVState(false);
                            if (mCameraDialog != null) {
                                mCameraDialog.dismiss();
                                mCameraDialog = null;
                            }
                        }

                        @Override
                        public void cannelCallBack() {
                            v2ConferenceRequest.enableVideoDev("", true);
                            if (mCameraDialog != null) {
                                mCameraDialog.dismiss();
                                mCameraDialog = null;
                            }
                        }
                    });
        }
        mCameraDialog.show();
    }

    /**
     * reverse local camera from back to front or from front to back
     */
    private void doReverseCamera() {
        VideoCaptureDevInfo captureInfo = VideoCaptureDevInfo.CreateVideoCaptureDevInfo();
        if (captureInfo == null) {
            return;
        }

        boolean flag = VideoCaptureDevInfo.CreateVideoCaptureDevInfo().reverseCamera();
        if (flag) {
            v2ConferenceRequest.updateCameraParameters(new CameraConfiguration(new VideoPlayer()), null);
        }
    }

    /**
     * Use to suspend or resume current conference's all video device.<br>
     * If current activity do stop then suspend, current activity do start then
     * resume.
     */
    private void suspendOrResume(boolean resume) {
        // 现在暂不做关闭和打开远端视频的操作，因为会导致本地视频窗口被远端遮盖
        if (resume) {
            if (!isFirstOpen) {
                resumeRemoteCameras();
            }
            isFirstOpen = false;
            // Send speaking status
            doApplyOrReleaseSpeak(currentAttendee.isSpeaking());
            // Make sure update start after send request,
            // because update state will update isSpeaking value
            updateSpeakerState(currentAttendee.isSpeaking());
            // Resume audio
            v2ConferenceRequest.updateAudio(true);
            // 打开本地视频
            if (mAttendeeContainer != null) {
                if (mAttendeeContainer.mIsOwnerVideoOpen
                        && !isOpeningLocal) {
                    LocalCameraAdjustExist(true);
                    LocalCameraAdjustExist(false);
                }
            }
            // Adjust content layout
            adjustContentLayout();
            if (mDocContainer != null) {
                mDocContainer.updateCurrentDoc();
            }
        } else {
            // suspend audio
            v2ConferenceRequest.updateAudio(false);
            if (mDocContainer != null) {
                mDocContainer.cleanAllResource();
            }
        }
    }

    private void LocalCameraAdjustExist(boolean isRemove) {
        if (isRemove) {
            closeLocalCamera();
            if (mLocalSurface.getParent() != null) {
                mLocalSurfaceViewLy.removeView(mLocalSurface);
                mLocalSurface.setTag("remove");
            }
        } else {
            String existFlag = (String) mLocalSurface.getTag();
            if (existFlag != null && "remove".equals(existFlag)) {
                //  mLocalSurface.setZOrderOnTop(false);
                // mLocalSurface.setZOrderMediaOverlay(false);
                mLocalSurfaceViewLy.addView(mLocalSurface);
                mLocalSurface.setTag(null);
                mConverseLocalCameraButton.bringToFront();
                // make sure local camera is first of all
                //mLocalSurfaceViewLy.bringToFront();
            }
            openLocalCamera();
        }
    }

    private void doApplyOrReleaseSpeak(boolean flag) {
        if (!flag) {
            v2ConferenceRequest.applyForReleasePermission(ConferencePermission.SPEAKING, null);
        } else {
            v2ConferenceRequest.applyForControlPermission(ConferencePermission.SPEAKING, null);
        }
    }

    /**
     * Handle event which new user entered conference
     *
     * @param att
     */
    private void doHandleNewUserEntered(Attendee att) {

        Log.e(TAG, "doHandleNewUserEntered-->" + att.toString());
        if (att == null) {
            return;
        }

        att.setJoined(true);
        if (conf.getChairman() == att.getAttId()) {
            att.setChairMan(true);
        }

        if (mAttendeeContainer != null) {
            mAttendeeContainer.updateEnteredAttendee(att);
        }

        getStudentInfo(att);

        // openRemoteCameraRunnable.setAttendee(att);
        // mHandler.post(openRemoteCameraRunnable);
    }

    /**
     * Handle event which user exited conference
     *
     * @param att
     */
    private void doHandleUserExited(Attendee att) {
        if (att == null) {
            V2Log.e(TAG, "doHandleUserExited --> Attendee is null");
            return;
        }

        removeSurfaceView(att.getAttId());
        att.setJoined(false);
        att.setSpeakingState(false);
        att.setLectureState(Attendee.LECTURE_STATE_NOT);
        if (att.getLectureState() == Attendee.LECTURE_STATE_NOT) {
            hasUnreadChiremanControllMsg = false;
            mMsgNotification.setVisibility(View.GONE);
            if (mConfMsgRedDot != null) {
                mConfMsgRedDot.setVisibility(View.GONE);
            }
        }

        if (conf.getChairman() == att.getAttId()) {
            att.setChairMan(true);
            isChairManVideoOpen = null;
        }

        if (mAttendeeContainer != null) {
            mAttendeeContainer.updateExitedAttendee(att);
        }

        openRemoteCameraRunnable.setAttendee(att);
        mHandler.post(openRemoteCameraRunnable);
    }

    private void putNewAttendee(Attendee newAttendee) {
        mAttendeeList.add(newAttendee);
        mAttendeeMap.put((int) newAttendee.getAttId(), newAttendee);
    }

    private void removeAttendee(Attendee newAttendee) {
        mAttendeeList.remove(newAttendee);
        mAttendeeMap.remove((int) newAttendee.getAttId());
    }

    /**
     * 移除视频块时，所有视频块都要重新加载一次，不然会重叠，但会导致闪烁，所以将宽和高变0
     *
     * @param deviceID
     */
    private boolean removeSurfaceView(final String deviceID) {
        for (int i = 0; i < mCurrentShowedSV.size(); i++) {
            SurfaceViewConfig sw = mCurrentShowedSV.get(i);
            if (sw.udc.getDeviceID().equals(deviceID)) {
                switch (sw.showIndex) {
                    case 1:
                        SURFACE_VIEW_ONE = false;
                        break;
                    case 2:
                        SURFACE_VIEW_TWO = false;
                        break;
                    case 3:
                        SURFACE_VIEW_THREE = false;
                        break;
                    case 4:
                        SURFACE_VIEW_FOUR = false;
                        break;
                    default:
                        break;
                }

                sw.udc.getSVHolder().clearScreen();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(0, 0);
                mVideoLayout.updateViewLayout(sw.getView(), layoutParams);
                ConferenceSurfaceView surfaceChild = (ConferenceSurfaceView)
                        sw.getView().getChildAt(0);
                surfaceChild.setLayoutParams(layoutParams);
                surfaceChild.setVisibility(View.GONE);
                mCurrentShowedSV.remove(sw);
                sw.clear();
                adjustContentLayout();
                return true;
            }
        }
        return false;
    }

    /**
     * 当用户退出会议时调用
     *
     * @param attendeeID
     */
    private void removeSurfaceView(long attendeeID) {
        List<SurfaceViewConfig> temp = new ArrayList<>();
        for (int i = 0; i < mCurrentShowedSV.size(); i++) {
            SurfaceViewConfig sw = mCurrentShowedSV.get(i);
            if (attendeeID == sw.at.getAttId()) {
                switch (sw.showIndex) {
                    case 1:
                        SURFACE_VIEW_ONE = false;
                        break;
                    case 2:
                        SURFACE_VIEW_TWO = false;
                        break;
                    case 3:
                        SURFACE_VIEW_THREE = false;
                        break;
                    case 4:
                        SURFACE_VIEW_FOUR = false;
                        break;
                    default:
                        break;
                }
                sw.udc.getSVHolder().clearScreen();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(0, 0);
                mVideoLayout.updateViewLayout(sw.getView(), layoutParams);
                sw.clear();
                temp.add(sw);
            }
        }

        for (int j = 0; j < temp.size(); j++) {
            SurfaceViewConfig sw = temp.get(j);
            mCurrentShowedSV.remove(sw);
        }
        adjustContentLayout();
    }

    private void destoryMixVideo(MixVideo mv) {
        AttendeeMixedDevice amd = new AttendeeMixedDevice(mv);
        // Remove from attendee list, because chairman closed mixed
        // video device
        removeAttendee(amd);
        // Notify attendee list remove mixed video device
        if (mAttendeeContainer != null) {
            mAttendeeContainer.updateExitedAttendee(amd);
        }
        UserDeviceConfig mixedUDC = amd.getDefaultDevice();
        Message.obtain(mHandler, ATTENDEE_REMOVE_VIDEO, mixedUDC.getDeviceID()).sendToTarget();
    }

    private SurfaceViewConfig addSurfaceView(UserDeviceConfig udc) {
        RelativeLayout surfaceRoot;
        boolean isCreate = false;
        int showIndex;
        if (!SURFACE_VIEW_ONE) {
            surfaceRoot = mSurfaceViewRootCache.get(1);
            if (surfaceRoot == null) {
                surfaceRoot = (RelativeLayout) findViewById(R.id.ws_conference_activity_surface1);
                mSurfaceViewRootCache.put(1, surfaceRoot);
            } else {
                isCreate = true;
            }
            SURFACE_VIEW_ONE = true;
            showIndex = 1;
        } else if (!SURFACE_VIEW_TWO) {
            surfaceRoot = mSurfaceViewRootCache.get(2);
            if (surfaceRoot == null) {
                surfaceRoot = (RelativeLayout) findViewById(R.id.ws_conference_activity_surface2);
                mSurfaceViewRootCache.put(2, surfaceRoot);
            } else {
                isCreate = true;
            }
            SURFACE_VIEW_TWO = true;
            showIndex = 2;
        } else if (!SURFACE_VIEW_THREE) {
            surfaceRoot = mSurfaceViewRootCache.get(3);
            if (surfaceRoot == null) {
                surfaceRoot = (RelativeLayout) findViewById(R.id.ws_conference_activity_surface3);
                mSurfaceViewRootCache.put(3, surfaceRoot);
            } else {
                isCreate = true;
            }
            SURFACE_VIEW_THREE = true;
            showIndex = 3;
        } else if (!SURFACE_VIEW_FOUR) {
            surfaceRoot = mSurfaceViewRootCache.get(4);
            if (surfaceRoot == null) {
                surfaceRoot = (RelativeLayout) findViewById(R.id.ws_conference_activity_surface4);
                mSurfaceViewRootCache.put(4, surfaceRoot);
            } else {
                isCreate = true;
            }
            SURFACE_VIEW_FOUR = true;
            showIndex = 4;
        } else {
            return null;
        }

        ConferenceSurfaceView surfaceChild = (ConferenceSurfaceView) surfaceRoot.getChildAt(0);
        surfaceRoot.removeView(surfaceChild);
        surfaceChild.setZOrderMediaOverlay(true);
        surfaceRoot.addView(surfaceChild, 0);

        VideoPlayer vp = new VideoPlayer();
        udc.setVp(vp);
        Attendee attendee = mAttendeeMap.get((int) udc.getUserID());
        if (udc.getType() == V2GlobalConstants.EVIDEODEVTYPE_VIDEOMIXER) {
            if (udc.getSVHolder() == null)
                udc.setSVHolder(surfaceChild);
            vp.setLayout(((AttendeeMixedDevice) attendee).getMV().getType().toIntValue());
        } else {
            udc.setSVHolder(surfaceChild);
        }

        SurfaceHolderObserver observer;
        if (surfaceRoot.getTag() == null) {
            observer = new SurfaceHolderObserver(cg, cs, udc);
            if (isCreate) {
                observer.isCreate = true;
            }
            surfaceRoot.setTag(observer);
        } else {
            observer = (SurfaceHolderObserver) surfaceRoot.getTag();
            observer.setUdc(udc);
        }
        SurfaceViewConfig sw = new SurfaceViewConfig(this, attendee, udc, observer);
        sw.setView(surfaceRoot);
        sw.showIndex = showIndex;
        mCurrentShowedSV.add(sw);

        surfaceRoot.setVisibility(View.VISIBLE);
        surfaceChild.setVisibility(View.VISIBLE);
        return sw;
    }

    private boolean checkVideoExceedMaminum() {
        return mCurrentShowedSV.size() >= 4;
    }

    private void showOrCloseMixVideo(UserDeviceConfig udc) {
        if (udc == null) {
            V2Log.e(TAG, "showOrCloseMixVideo --> can't not open or close device");
            return;
        }

        if (udc.isShowing()) {
            closeVideo(udc);
        } else {
            if (isSyn && isMixVideoShow) {
                Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, udc).sendToTarget();
            } else if (!isSyn) {
                Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, udc).sendToTarget();
            }
        }
    }

    /**
     * show remote user video
     *
     * @param udc
     */
    private void showOrCloseAttendeeVideo(UserDeviceConfig udc) {
        if (udc == null) {
            V2Log.e(TAG, "showOrCloseAttendeeVideo --> can't not open or close device");
            return;
        }
        // if already opened attendee's video, switch action to close
        if (udc.isShowing()) {
            closeAttendeeVideo(udc);
        } else {
            Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, udc).sendToTarget();
        }

    }

    public void openChairManVideo(String deviceID) {
        long chairman = conf.getChairman();
        Attendee attendee = mAttendeeMap.get((int) chairman);
        if (attendee != null) {
            List<UserDeviceConfig> devices = getAttendeeDevices(attendee);
            if (devices != null) {
                if (deviceID == null) {
                    for (int i = 0; i < devices.size(); i++) {
                        UserDeviceConfig temp = devices.get(i);
                        if (temp.isDefault()) {
                            isChairManVideoOpen = temp.getDeviceID();
                            Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, temp).sendToTarget();
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getDeviceID().equals(deviceID)) {
                            Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, devices.get(i)).sendToTarget();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据设备信息，打开指定的用户视频。
     *
     * @param udc
     * @return
     */
    public boolean openAttendeeVideo(UserDeviceConfig udc) {
//        if (udc == null) {
//            V2Log.e(TAG, "openAttendeeVideo --> can't not open device");
//            return false;
//        }
//
//        if (checkVideoExceedMaminum()) {
//            Toast.makeText(mContext, R.string.error_exceed_support_video_count, Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//
//        if (udc.isShowing()) {
//            V2Log.e(TAG, "openAttendeeVideo --> can't not open device , device is showing");
//            return false;
//        }
//        SurfaceViewConfig sw = addSurfaceView(udc);
//        if (sw != null) {
//            adjustContentLayout();
//            V2Log.i(TAG, "openAttendeeVideo udc.id =" + udc.getDeviceID() + " udc.isShowing =" + udc.isShowing());
//        } else {
//            V2Log.e(TAG, "openAttendeeVideo --> can't add a new surfaceview , get null!");
//            return false;
//        }
//
//        udc.setShowing(true);
//        if (mAttendeeContainer != null) {
//            mAttendeeContainer.updateDeviceDisplay(udc);
//        }
        return true;
    }

    /**
     * 根据设备信息，关闭当前正在显示的指定视频。但不包括混合视频
     *
     * @param udc
     * @return
     */
    public boolean closeAttendeeVideo(UserDeviceConfig udc) {
        if (udc == null || udc.getType() == V2GlobalConstants.EVIDEODEVTYPE_VIDEOMIXER) {
            V2Log.e(TAG, "closeAttendeeVideo --> can't not open or close device");
            return false;
        }
        return closeVideo(udc);
    }

    /**
     * 根据设备信息，关闭当前正在显示的指定视频。
     *
     * @param udc
     * @return
     */
    public boolean closeVideo(UserDeviceConfig udc) {
        // if already opened attendee's video, switch action to close
        if (udc.isShowing()) {
            udc.setShowing(false);
            removeSurfaceView(udc.getDeviceID());
            if (isChairManVideoOpen != null && isChairManVideoOpen.equals(udc.getDeviceID())) {
                isChairManVideoOpen = null;
            }

            if (mAttendeeContainer != null) {
                mAttendeeContainer.updateDeviceDisplay(udc);
            }
            return true;
        } else {
            return false;
        }
    }

    public void closeAllAttendeeDevice() {
        mHandler.removeCallbacks(voiceActiveRunnable);
        Object[] surfaceViewWArray = mCurrentShowedSV.toArray();
        for (int i = 0; i < surfaceViewWArray.length; i++) {
            UserDeviceConfig udc = ((SurfaceViewConfig) surfaceViewWArray[i]).udc;
            closeVideo(udc);
            // if (udc.isShowing()) {
            // udc.setShowing(false);
            // removeSurfaceView(udc.getDeviceID());
            // if (isChairManVideoOpen != null &&
            // isChairManVideoOpen.equals(udc.getDeviceID())) {
            // isChairManVideoOpen = null;
            // }
            // Attendee belongsAttendee = mAttendeeMap.get((int)
            // udc.getUserID());
            // List<UserDeviceConfig> attendeeDevices =
            // getAttendeeDevices(belongsAttendee);
            // if (attendeeDevices != null) {
            // for (int j = 0; j < attendeeDevices.size(); j++) {
            // UserDeviceConfig temp = attendeeDevices.get(j);
            // if (temp.getDeviceID().equals(udc.getDeviceID())) {
            // temp.doClose();
            // break;
            // }
            // }
            // }
            //
            // if (mAttendeeContainer != null) {
            // mAttendeeContainer.updateDeviceDisplay(udc);
            // }
            // }
        }
    }


    /**
     * Update attendee state
     *
     * @param ind
     */
    private boolean updateAttendeePermissionState(PermissionUpdateIndication ind) {
        int key = (int) ind.getUid();
        if (mAttendeeMap == null) {
            return false;
        }
        Attendee attendee = mAttendeeMap.get(key);

        if (attendee != null) {
            ConferencePermission conferencePermission = ConferencePermission.fromInt(ind.getType());
            PermissionState permissionState = PermissionState.fromInt(ind.getState());

            if (conferencePermission == ConferencePermission.SPEAKING) {
                if (permissionState == PermissionState.GRANTED) {
                    attendee.setSpeakingState(true);
                } else {
                    attendee.setSpeakingState(false);
                }
            } else if (conferencePermission == ConferencePermission.CONTROL) {
                if (permissionState == PermissionState.GRANTED) {
                    attendee.setLectureState(Attendee.LECTURE_STATE_GRANTED);
                    // 取消自己的默认主讲
                    if (currentAttendee.isChairMan()) {
                        if (currentAttendee.getAttId() != attendee.getAttId()) {
                            if (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                                currentAttendee.setLectureState(Attendee.LECTURE_STATE_NOT);
                            }
                        }
                    }

                } else if (permissionState == PermissionState.APPLYING) {
                    attendee.setLectureState(Attendee.LECTURE_STATE_APPLYING);
                } else if (permissionState == PermissionState.NORMAL) {
                    attendee.setLectureState(Attendee.LECTURE_STATE_NOT);
                }
            }

            if (mAttendeeContainer != null) {
                mAttendeeContainer.updateDisplay();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param at
     * @param devices
     */
    private void updateAttendeeDevice(Attendee at, List<UserDeviceConfig> devices) {
        boolean closeFlag = false;
        for (int i = 0; i < mCurrentShowedSV.size(); i++) {
            SurfaceViewConfig svw = mCurrentShowedSV.get(i);
            if (at.getAttId() != svw.at.getAttId()) {
                continue;
            }

            for (int j = 0; j < devices.size(); j++) {
                UserDeviceConfig ud = devices.get(j);
                // If remote user disable local camera device which
                // local user already opened
                if (svw.udc.getDeviceID().equals(ud.getDeviceID())) {
                    if (!ud.isEnable()) {
                        closeFlag = true;
                        break;
                    }
                }
            }

            // Need to close video
            if (closeFlag) {
                Message.obtain(mHandler, ATTENDEE_REMOVE_VIDEO, svw.udc.getDeviceID()).sendToTarget();
                break;
            }
        }

        if (mAttendeeContainer == null) {
            initAttendeeContainer();
        }
        mAttendeeContainer.resetAttendeeDevices(at, devices);
    }

    public void handleDocNotification(AsyncResult res, int opt) {
        if (mDocs == null) {
            mDocs = new OrderedHashMap<>();
        }

        V2Doc v2Doc;
        String docId = null;
        V2Doc.Doc doc;
        Page page;

        // V2ShapeMeta shape = null;
        switch (opt) {
            case NEW_DOC_NOTIFICATION:
                V2Log.d("DOC_TEST", "-NEW_DOC_NOTIFICATION-");
                v2Doc = (V2Doc) res.getResult();
                V2Log.d("DOC_TEST", "接收新的文档：" + v2Doc.getDocName() + " 文档ID：" + v2Doc.getId());
                if (mDocContainer == null) {
                    mDocContainer = (LeftShareDocLayout) initDocLayout(false);
                }
                docId = v2Doc.getId();
                V2Doc cacheDoc = mDocs.get(docId);
                if (cacheDoc == null) {
                    v2Doc = new V2Doc(docId, v2Doc.getDocName(), null, 0, null);
                    mDocs.put(docId, v2Doc);
                } else {
                    cacheDoc.updateV2Doc(v2Doc);
                    v2Doc = cacheDoc;
                }

                if (mDocContainer != null) {
                    mDocContainer.addDoc(v2Doc);
                }
                break;
            case DOC_PAGE_LIST_NOTIFICATION:
                V2Log.d("DOC_TEST", "-DOC_PAGE_LIST_NOTIFICATION-");
                doc = (V2Doc.Doc) res.getResult();
                if (doc == null) {
                    V2Log.d("DOC_TEST", " --> DOC_PAGE_LIST_NOTIFICATION : 收到一个空的文档对象！");
                    return;
                } else {
                    V2Log.d("DOC_TEST", "接收到文档：" + doc.getDocId() + " 的页数列表：" + doc.getPageSize());
                    Integer flag = GlobalHolder.getInstance().mShareDocIds.get(doc.getDocId());
                    if (flag == null || flag == V2GlobalConstants.DOC_TYPE_BLACK) {
                        return;
                    }
                }

                docId = doc.getDocId();
                v2Doc = mDocs.get(docId);
                if (v2Doc == null) {
                    // return;
                    // Put fake doc, because page events before doc event;
                    V2Log.d("DOC_TEST", "没有从文档集合中找到该文档：" + docId + " 可能Page Events 比 Doc Event来提前的，手动创建一个空对象放入");
                    v2Doc = new V2Doc(docId, null, null, 0, null);
                    mDocs.put(docId, v2Doc);
                } else {
                    v2Doc.updateDoc(doc);
                }

                if (mDocContainer != null) {
                    mDocContainer.updateLayoutPageInformation();
                    mDocContainer.updatePageButton();
                }
                break;
            case DOC_ADDED_ONE_PAGE_NOTIFICATION:
                V2Log.d("DOC_TEST", "-DOC_ADDED_ONE_PAGE_NOTIFICATION-");
                page = (Page) res.getResult();
                Integer flag = GlobalHolder.getInstance().mShareDocIds.get(page.getDocId());
                if (flag == null || flag == V2GlobalConstants.DOC_TYPE_BLACK) {
                    return;
                }

                docId = page.getDocId();
                v2Doc = mDocs.get(docId);
                if (v2Doc == null) {
                    v2Doc = mDocs.get(docId);
                    if (v2Doc == null) {
                        v2Doc = new V2Doc(docId, null, null, 0, null);
                        mDocs.put(docId, v2Doc);
                    }
                }

                v2Doc.addPage(page);
                if (mDocContainer != null) {
                    mDocContainer.updateLayoutPageInformation();
                    mDocContainer.updatePageButton();
                }
                break;
            case DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION:
                V2Log.d("DOC_TEST", "-DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION-");
                page = (Page) res.getResult();
                V2Log.d("DOC_TEST", "接收到文档：" + page.getDocId() + " 当前正在显示的文档页是：" + page.getFilePath());
                Integer blackFlag = GlobalHolder.getInstance().mShareDocIds.get(page.getDocId());
                if (blackFlag == null || blackFlag == V2GlobalConstants.DOC_TYPE_BLACK) {
                    return;
                }

                docId = page.getDocId();
                v2Doc = mDocs.get(docId);
                if (v2Doc == null) {
                    v2Doc = mDocs.get(docId);
                    if (v2Doc == null) {
                        v2Doc = new V2Doc(docId, null, null, 0, null);
                        mDocs.put(docId, v2Doc);
                    }
                }

                v2Doc.addPage(page);
                if (page.equals(mCurrentLecturerActivateDocPage)) {
                    if (mDocContainer != null) {
                        mDocContainer.updateCurrentDoc();
                    }
                }
                break;
            case DOC_TURN_PAGE_NOTIFICATION:// 上下翻页
                V2Log.d("DOC_TEST",
                        "-DOC_TURN_PAGE_NOTIFICATION- CONNECT : " + GlobalHolder.getInstance().isServerConnected());
                page = (Page) res.getResult();
                Integer blackFlags = GlobalHolder.getInstance().mShareDocIds.get(page.getDocId());
                if (blackFlags == null || blackFlags == V2GlobalConstants.DOC_TYPE_BLACK) {
                    return;
                }

                V2Log.d("DOC_TEST", "mCurrentLecturerActivateDocIdStr : " + mCurrentLecturerActivateDocIdStr
                        + " | isFreeMode : " + isFreeMode + " | doc id : " + page.getDocId());
                if (mDocContainer != null && !TextUtils.isEmpty(mCurrentLecturerActivateDocIdStr) && isFreeMode) {
                    // Record current activate Id;
                    mCurrentLecturerActivateDocIdStr = page.getDocId();
                    mCurrentLecturerActivateDocPage = page;
                    return;
                }

                docId = page.getDocId();
                v2Doc = mDocs.get(docId);
                if (v2Doc == null) {
                    v2Doc = mDocs.get(docId);
                    if (v2Doc == null) {
                        v2Doc = new V2Doc(docId, null, null, 0, null);
                        mDocs.put(docId, v2Doc);
                    }
                }

                V2Log.d("DOC_TEST", "接收到服务器消息，当前激活文档是 ：" + v2Doc.getDocName() + " -- " + "激活页是：" + page.getNo());
                if (v2Doc.getId().equals(mCurrentLecturerActivateDocIdStr)
                        && page.getNo() == mCurrentLecturerActivateDocPage.getNo()) {
                    return;
                }

                v2Doc.addPage(page);
                v2Doc.setActivatePageNo(page.getNo());
                mDocContainer.updateCurrentDoc(v2Doc);

                // Record current activate Id;
                mCurrentLecturerActivateDocIdStr = docId;
                mCurrentLecturerActivateDocPage = page;
                break;
            case DOC_CLOSED_NOTIFICATION:
                v2Doc = (V2Doc) res.getResult();
                if (v2Doc == null) {
                    return;
                }

                Integer closeBlackFlag = GlobalHolder.getInstance().mShareDocIds.get(v2Doc.getId());
                if (closeBlackFlag == null || closeBlackFlag == V2GlobalConstants.DOC_TYPE_BLACK) {
                    return;
                }

                if (mDocs.get(v2Doc.getId()) == null) {
                    return;
                }

                V2Log.d("DOC_TEST", "接收到服务器消息，删除当前文档 ：" + v2Doc.getDocName());
                V2Doc nextDoc = null;
                boolean isLecturer = (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED);
                if (!isLecturer) {
                    // 如果移动端是主讲，则会收到激活回调，不需要手动去翻页
                    nextDoc = mDocs.removeAndShowNext(v2Doc.getId());
                    if (nextDoc != null)
                        V2Log.d("DOC_TEST", "删除后显示下一个文档 ：" + nextDoc.getDocName());
                }

                v2Doc = mDocs.remove(v2Doc.getId());
                if (mDocContainer != null) {
                    mDocContainer.closeDoc(v2Doc);
                }

                if (nextDoc != null && mDocContainer != null) {
                    mDocContainer.updateCurrentDoc(nextDoc);
                }
                break;
            case DOC_PAGE_CANVAS_NOTIFICATION:
                // shape = (V2ShapeMeta) res.getResult();
                // docId = shape.getDocId();
                //
                // v2Doc = mDocs.get(docId);
                // if (v2Doc == null) {
                // return;
                // }
                //
                // page = v2Doc.getPage(shape.getPageNo());
                // if (page == null) {
                // page = new Page(shape.getPageNo(), docId, null);
                // page.addMeta(shape);
                // v2Doc.addPage(page);
                // } else {
                // page.addMeta(shape);
                // }

                break;
        }
    }

    /**
     * 处理会议中耳机与蓝牙的处理
     */
    @SuppressWarnings("deprecation")
    private void headsetAndBluetoothHeadsetHandle() {

        if (audioManager == null) {
            audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }

        if (audioManager.isWiredHeadsetOn()) {
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(false);
                V2Log.i(TAG, "切换到了有线耳机");
            } else {
                V2Log.i(TAG, "当前已是耳机模式");
            }
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
            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
                V2Log.i(TAG, "切换到了外放");
            } else {
                V2Log.i(TAG, "当前已是外放模式");
            }
        }
    }

    private boolean judgeIsBigPad() {
        double src = GlobalConfig.SCREEN_WIDTH / GlobalConfig.SCREEN_HEIGHT;
        double des = 3 / 4;
        return (GlobalConfig.PROGRAM_IS_PAD && src != des);
    }

    private void appShareChangeVisibility(boolean isVisibility) {
        if (isVisibility) {
//            if (mAppShareButtonNotification.getVisibility() != View.GONE) {
//                mAppShareButtonNotification.setVisibility(View.GONE);
//            }

            if (mAppShareSurfaceViewLy.getVisibility() != View.VISIBLE) {
                mAppShareSurfaceViewLy.setVisibility(View.VISIBLE);
                String isExist = (String) mAppShareSurfaceView.getTag();
                if (isExist == null) {
                    mAppShareSurfaceView.setZOrderMediaOverlay(true);
                    mAppShareSurfaceViewLy.addView(mAppShareSurfaceView);
                    mAppShareSurfaceView.setZOrderMediaOverlay(true);
                    mAppShareSurfaceView.setTag("exist");
                    mAppShareDeleteBT.bringToFront();
                }
                // 本地视频关闭
                mLocalSurfaceViewLy.setVisibility(View.GONE);
                LocalCameraAdjustExist(true);
                remeberRemoteVideo.clear();
                // 记住已经打开的远端视频
                Object[] surfaceViewWArray = mCurrentShowedSV.toArray();
                for (int i = 0; i < surfaceViewWArray.length; i++) {
                    UserDeviceConfig udc = ((SurfaceViewConfig) surfaceViewWArray[i]).udc;
                    remeberRemoteVideo.add(udc);
                }
                // 远端视频
                closeAllAttendeeDevice();
            }
        } else {
            if (mAppShareSurfaceViewLy.getVisibility() == View.VISIBLE) {
                isChangingSubLayout = false;
                mAppShareSurfaceViewLy.setVisibility(View.GONE);
                String isExist = (String) mAppShareSurfaceView.getTag();
                if (isExist != null) {
                    mAppShareSurfaceViewLy.removeView(mAppShareSurfaceView);
                    mAppShareSurfaceView.setTag(null);
                }
                // 本地视频打开
                mLocalSurfaceViewLy.setVisibility(View.VISIBLE);
                LocalCameraAdjustExist(false);
                // 打开之前记住的远端视频
                for (int i = 0; i < remeberRemoteVideo.size(); i++) {
                    Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, remeberRemoteVideo.get(i)).sendToTarget();
                }
            }
        }
    }

    private class ChairmanControlButtonOnClickListener implements OnClickListener {
        public void onClick(View v) {
            if (!isPopupWindowShow) {
                showChairmanControlPopWindow(v);
            } else {
                isPopupWindowShow = false;
            }
        }

        private void showChairmanControlPopWindow(View v) {
            if (mChairControlWindow == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.in_meeting_chairman_control_pop_up_window, null);

                ImageView arrow = (ImageView) view.findViewById(R.id.arrow);
                int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                arrow.measure(widthSpec, heightSpec);
                arrowWidth = arrow.getMeasuredWidth();

                CheckBox slience = (CheckBox) view.findViewById(R.id.cb_slience);
                CheckBox invitation = (CheckBox) view.findViewById(R.id.cb_invitation);
                CheckBox conferenceMessage = (CheckBox) view.findViewById(R.id.conference_message);

                mConfMsgRedDot = view.findViewById(R.id.host_request_msg_notificator);
                slience.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        v2ConferenceRequest.muteConf();
                        mChairControlWindow.dismiss();

                    }
                });

                invitation.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        v2ConferenceRequest.updateConferenceAttribute(conf, cg.isSyn(), isChecked, null);
                        mChairControlWindow.dismiss();
                    }
                });

                conferenceMessage.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mChairControlWindow.dismiss();
                        // 您当前没有会议消息
                        if (mHostRequestUsers != null && mHostRequestUsers.size() > 0) {
                            showConferenceMsgDialog();
                        } else {
                            Toast.makeText(ConferenceActivity2.this, R.string.conference_toast_no_meeting,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });

                // set
                mChairControlWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mChairControlWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mChairControlWindow.setFocusable(false);
                mChairControlWindow.setTouchable(true);
                mChairControlWindow.setOutsideTouchable(true);
                mChairControlWindow.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mChairmanControl.setBackgroundResource(R.drawable.btn_conference_titlebar_more);
                    }
                });
            }

            mChairmanControl.setBackgroundResource(R.drawable.btn_conference_titlebar_more_press);
            if (mConfMsgRedDot != null) {
                if (hasUnreadChiremanControllMsg) {
                    mConfMsgRedDot.setVisibility(View.VISIBLE);
                } else {
                    mConfMsgRedDot.setVisibility(View.GONE);
                }
            }

            int[] pos = new int[2];
            v.getLocationInWindow(pos);
            pos[0] += v.getMeasuredWidth() / 2 - 15 * GlobalConfig.GLOBAL_DENSITY_VALUE - arrowWidth / 2;
            pos[1] += v.getMeasuredHeight();

            mChairControlWindow.setAnimationStyle(R.style.TitleBarPopupWindowAnim);
            mChairControlWindow.showAtLocation(v, Gravity.NO_GRAVITY, pos[0], pos[1]);
            isPopupWindowShow = true;
        }
    }

    private class LeftMenuButtonOnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            if (mMenuButtonContainer.getVisibility() == View.GONE) {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.nonam_scale_y_0_100);
                animation.setDuration(400);
                mMenuButtonContainer.startAnimation(animation);
                mMenuButtonContainer.setVisibility(View.VISIBLE);
                ((ImageView) view).setImageResource(R.drawable.video_menu_button);
                // If menu layout is visible, hide line of below at menu button
                mMenuSparationLine.setVisibility(View.GONE);

            } else {
                // Do not hide other window
                // showOrHidenAttendeeContainer(View.GONE);
                // showOrHidenMsgContainer(View.GONE);
                // showOrHidenDocContainer(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.nonam_scale_y_100_0);
                animation.setDuration(400);
                mMenuButtonContainer.startAnimation(animation);
                mMenuButtonContainer.setVisibility(View.GONE);
                ((ImageView) view).setImageResource(R.drawable.video_menu_button_pressed);

                // If menu layout is invisible, show line of below at menu
                // button
                mMenuSparationLine.setVisibility(View.VISIBLE);
            }
            adjustContentLayout();
        }
    }

    private class AppShareSHCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int arg1, int width, int height) {
            V2Log.d(TAG, "AppShareSHCallback--> surfaceChanged");
            VideoPlayer player = (VideoPlayer) mAppShareSurfaceViewLy.getTag();
            if (player == null)
                return;
            player.setSurfaceViewSize(width, height);
            if (appShareSHState == SurfaceViewState.CLOSED || appShareSHState == SurfaceViewState.CLOSING) {
                appShareSHState = SurfaceViewState.SHOWING;
                player.setSurfaceHolder(holder);
                player.setSuspended(false);
                AppShareRequest.getInstance().AppShareStartView(V2GlobalConstants.GROUP_TYPE_CONFERENCE,
                        conf.getId(), conf.getChairman(), appShareDeviceID, player);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            V2Log.d(TAG, "AppShareSHCallback--> surfaceCreated");
            VideoPlayer player = (VideoPlayer) mAppShareSurfaceViewLy.getTag();
            if (player == null)
                return;
            if (appShareSHState == SurfaceViewState.CLOSED || appShareSHState == SurfaceViewState.CLOSING) {
                appShareSHState = SurfaceViewState.SHOWING;
                player.setSurfaceHolder(holder);
                player.setSuspended(false);
                AppShareRequest.getInstance().AppShareStartView(V2GlobalConstants.GROUP_TYPE_CONFERENCE,
                        conf.getId(), conf.getChairman(), appShareDeviceID, player);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            V2Log.d(TAG, "AppShareSHCallback--> surfaceDestroyed");
            VideoPlayer player = (VideoPlayer) mAppShareSurfaceViewLy.getTag();
            if (player == null)
                return;
            if ((appShareSHState == SurfaceViewState.SHOWED || appShareSHState == SurfaceViewState.SHOWING)) {
                appShareSHState = SurfaceViewState.CLOSING;
                player.setSuspended(false);
                isChangingSubLayout = false;
            }
        }

    }

    private class AppShareDeleteBTOclick implements OnClickListener {
        @Override
        public void onClick(View view) {
            appShareChangeVisibility(false);
            int backGroundColor = 0;
            ColorDrawable drawable = (ColorDrawable) mMenuAppShareButton.getBackground();
            if (drawable == null || drawable.getColor() == unSelectedColor) {
                backGroundColor = selectedColor;
            } else {
                backGroundColor = unSelectedColor;
            }
            mMenuAppShareButton.setBackgroundColor(backGroundColor);
        }
    }

    private int selectedColor = -1;
    private int unSelectedColor = Color.rgb(255, 255, 255);

    private class LeftMenuItemButtonOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (isChangingSubLayout) {
                return;
            }
            isChangingSubLayout = true;

            boolean isDoc = false;
            boolean isAppShare = false;
            View content = null;
            // Call cancel full screen request first.
            // because we can't make sure user press invitation layout or not,
            // we have to restore first.
            // 请求弹出的窗口为正常模式
            requestSubViewRestore();
            if (v.getTag().equals(LEFT_MENU_BUTTON_INVITE)) {
                if (cg.isCanInvitation() || currentAttendee.isChairMan()) {
                    // Make sure invitation layout fill full screen
                    // 请求弹出的窗口为全屏模式
                    requestSubViewFillScreen();
                    content = initInvitionContainer();
                    content.setTag(LEFT_MENU_BUTTON_INVITE);
                } else {
                    Toast.makeText(mContext, R.string.error_no_permission_to_invitation, Toast.LENGTH_SHORT).show();
                }

            } else if (v.getTag().equals(LEFT_MENU_BUTTON_ATTENDEE)) {
                content = initAttendeeContainer();
                content.setTag(LEFT_MENU_BUTTON_ATTENDEE);
                // If last state is fixed
                if (mAttendeeContainer.getWindowSizeState()) {
                    requestSubViewFixed();
                }

            } else if (v.getTag().equals(LEFT_MENU_BUTTON_CHAT)) {
                content = initMsgLayout();
                content.setTag(LEFT_MENU_BUTTON_CHAT);
                // If last state is fixed
                if (mMessageContainer.getWindowSizeState()) {
                    requestSubViewFixed();
                }
                mChatMsgNotification.setVisibility(View.GONE);
            } else if (v.getTag().equals(LEFT_MENU_BUTTON_DOC)) {
                if (mDocContainer != null) {
                    if (mSubWindowLayout.getVisibility() == View.VISIBLE) {
                        View currentChild = mSubWindowLayout.getChildAt(0);
                        if (currentChild == null) {
                            mDocContainer.mCurrentIsVisible = true;
                        } else {
                            if (currentChild instanceof LeftShareDocLayout) {
                                mDocContainer.mCurrentIsVisible = false;
                            } else {
                                mDocContainer.mCurrentIsVisible = true;
                            }
                        }
                    } else {
                        mDocContainer.mCurrentIsVisible = true;
                    }
                }

                content = initDocLayout(true);
                content.setTag(LEFT_MENU_BUTTON_DOC);
                if (mDocContainer.isFullScreenSize()) {
                    requestSubViewFillScreen();
                } else {
                    requestSubViewFixed();
                }
                isDoc = true;
            } else if (v.getTag().equals(LEFT_MENU_BUTTON_APPSHARE)) {
                appShareChangeVisibility(true);
                isAppShare = true;
            }

            if (selectedColor == -1) {
                selectedColor = mContext.getResources().getColor(R.color.confs_common_bg);
            }

            for (View button : mMenuButtonGroup) {
                int backGroundColor = 0;
                if (button == v) {
                    ColorDrawable drawable = (ColorDrawable) v.getBackground();
                    if (drawable == null || drawable.getColor() == unSelectedColor) {
                        backGroundColor = selectedColor;
                    } else {
                        backGroundColor = unSelectedColor;
                    }
                } else {
                    backGroundColor = unSelectedColor;
                }
                button.setBackgroundColor(backGroundColor);
            }

            showOrHideSubWindow(content, isDoc, isAppShare);
            // Make sure local camera is first front of all
            // mLocalSurfaceViewLy.bringToFront();
        }
    }

    private class SpeakIVOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            doApplyOrReleaseSpeak(!currentAttendee.isSpeaking());
            // Make sure update start after send request,
            // because update state will update isSpeaking value
            updateSpeakerState(!currentAttendee.isSpeaking());
        }
    }

    private class CameraIVOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (isMuteCamera) {
                isMuteCamera = false;
                v2ConferenceRequest.enableVideoDev("", true);
                updateMCameraIVState(true);
            } else {
                isMuteCamera = true;
                v2ConferenceRequest.enableVideoDev("", false);
                updateMCameraIVState(false);
                // 当自己禁用了本地视频可以被远端查看，当本地视频出现在混合视频中，需要手动调接口通知底层关闭绘制
                if (mCurrentShowedSV.size() > 0) {
                    boolean isBreak = false;
                    Object[] surfaceViewWArray = mCurrentShowedSV.toArray();
                    for (int i = 0; i < surfaceViewWArray.length; i++) {
                        UserDeviceConfig udc = ((SurfaceViewConfig) surfaceViewWArray[i]).udc;
                        if (udc.getType() == V2GlobalConstants.EVIDEODEVTYPE_VIDEOMIXER) {
                            long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
                            AttendeeMixedDevice mixAttendee = (AttendeeMixedDevice) mAttendeeMap
                                    .get((int) udc.getUserID());
                            MixVideo mv = mixAttendee.getMV();
                            List<UserDeviceConfig> childDevices = mixAttendee.getDevices();
                            if (childDevices != null) {
                                for (int j = 0; j < childDevices.size(); j++) {
                                    UserDeviceConfig tempChild = childDevices.get(j);
                                    if (tempChild.getUserID() == currentUserId) {
                                        // 如果混合视频中有自己的视频，并且摄像头禁用。则需要调一个接口
                                        VideoMixerRequest.getInstance().delVideoMixerDevID(mv.getId(), currentUserId,
                                                tempChild.getDeviceID());
                                        isBreak = true;
                                        break;
                                    }
                                }

                                if (isBreak) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class QuitButtonOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            showQuitDialog(mContext.getText(R.string.in_meeting_quit_text).toString());
        }
    }

    private class LectureButtonOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            ((ImageView) view.findViewById(R.id.conference_activity_request_host_iv))
                    .setImageResource(R.drawable.logout_button);

            ((TextView) view.findViewById(R.id.conference_activity_request_host_tv))
                    .setText(R.string.confs_title_button_request_host_name);

            if ((currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED
                    || currentAttendee.getLectureState() == Attendee.LECTURE_STATE_APPLYING)) {
                // updateCurrentAttendeeLectureState(PermissionState.NORMAL,
                // false);
                v2ConferenceRequest.applyForReleasePermission(ConferencePermission.CONTROL, null);
                // 释放主讲
            } else {
                // updateCurrentAttendeeLectureState(PermissionState.APPLYING,
                // false);
                currentAttendee.setLectureState(Attendee.LECTURE_STATE_APPLYING);
                if (mAttendeeContainer != null) {
                    mAttendeeContainer.updateDisplay();
                }
                currentAttendee.setLectureState(Attendee.LECTURE_STATE_APPLYING);
                v2ConferenceRequest.applyForControlPermission(ConferencePermission.CONTROL, null);
                // 申请主讲中
            }
        }
    }

    private class MoreIVOnClickListener implements OnClickListener {
        @Override
        public void onClick(View anchor) {
            if (!isPopupWindowShow) {
                showMorePopupWindow(anchor);
            } else {
                isPopupWindowShow = false;
            }
        }
    }

    private void initMorePopupWindow() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.conference_pop_up_window, null);

        layout.findViewById(R.id.conference_activity_request_host_button)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        moreWindow.dismiss();
                        mLectureButtonListener.onClick(v);
                    }

                });

        layout.findViewById(R.id.video_quality).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                moreWindow.dismiss();
                mSettingButtonOnClickListener.onClick(v);
            }

        });

        layout.findViewById(R.id.conference_activity_logout_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                moreWindow.dismiss();
                mQuitButtonOnClickListener.onClick(v);
            }

        });

        mRequestButtonName = (TextView) layout.findViewById(R.id.conference_activity_request_host_tv);
        mRequestButtonImage = (ImageView) layout.findViewById(R.id.conference_activity_request_host_iv);

        LinearLayout itemContainer = (LinearLayout) layout.findViewById(R.id.common_pop_window_container);

        itemContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        View arrow = layout.findViewById(R.id.common_pop_up_arrow_up);
        arrow.bringToFront();

        mRequestButtonName = (TextView) layout.findViewById(R.id.conference_activity_request_host_tv);
        mRequestButtonImage = (ImageView) layout.findViewById(R.id.conference_activity_request_host_iv);

        arrow.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int height = itemContainer.getMeasuredHeight() + arrow.getMeasuredHeight();

        moreWindow = new PopupWindow(layout, LayoutParams.WRAP_CONTENT, height, true);
        moreWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        moreWindow.setFocusable(false);
        moreWindow.setTouchable(true);
        moreWindow.setOutsideTouchable(true);
        moreWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                mMoreIV.setBackgroundResource(R.drawable.btn_conference_titlebar_more_feature);
            }
        });
    }

    private void showMorePopupWindow(View anchor) {
        if (moreWindow == null) {
            initMorePopupWindow();
        }

        mMoreIV.setBackgroundResource(R.drawable.btn_conference_titlebar_more_feature_press);
        int[] pos = new int[2];
        anchor.getLocationInWindow(pos);
        pos[1] += anchor.getMeasuredHeight() - anchor.getPaddingBottom();
        // calculate arrow offset
        View arrow = moreWindow.getContentView().findViewById(R.id.common_pop_up_arrow_up);
        RelativeLayout.LayoutParams arrowRL = (RelativeLayout.LayoutParams) arrow.getLayoutParams();
        arrowRL.rightMargin = mDisMetr.widthPixels - pos[0] - (anchor.getMeasuredWidth() / 2)
                - arrow.getMeasuredWidth();
        arrow.setLayoutParams(arrowRL);

        moreWindow.setAnimationStyle(R.style.TitleBarPopupWindowAnim);
        int marginRight = DensityUtils.dip2px(mContext, 5);
        moreWindow.showAtLocation(anchor, Gravity.END | Gravity.TOP, marginRight, pos[1]);
        isPopupWindowShow = true;
        updateMoreWindowDisplay();
    }

    private class SettingButtonOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            showSettingWindow();
        }

        private void showSettingWindow() {
            if (mSettingWindow == null) {
                if (popupHeight == -1 || popupWidth == -1) {
                    popupHeight = (int) getResources().getDimension(R.dimen.conference_activity_popup_height);
                    popupWidth = (int) getResources().getDimension(R.dimen.conference_activity_popup_width);
                }

                mSettingWindow = new PopupWindow(popupRootView, popupWidth, popupHeight);
                mSettingWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mSettingWindow.setFocusable(false);
                mSettingWindow.setTouchable(true);
                mSettingWindow.setOutsideTouchable(true);
                mSettingWindow.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        int level = V2GlobalConstants.CONF_CAMERA_MASS_LOW;
                        switch (radios.getCheckedRadioButtonId()) {
                            case R.id.ws_meeting_setting_popup_low_button:
                                level = V2GlobalConstants.CONF_CAMERA_MASS_LOW;
                                break;
                            case R.id.ws_meeting_setting_popup_middle_button:
                                level = V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE;
                                break;
                            case R.id.ws_meeting_setting_popup_high_button:
                                level = V2GlobalConstants.CONF_CAMERA_MASS_HIGH;
                                break;
                        }

                        VideoCaptureDevInfo captureInfo = VideoCaptureDevInfo.CreateVideoCaptureDevInfo();
                        if (captureInfo != null) {
                            closeLocalCamera();
                            // GlobalConfig.setGlobalVideoLevel(mContext, V2GlobalConstants.CONF_CAMERA_CUSTOMER);
                            openLocalCamera();
                        }
                        mSettingWindow.dismiss();
                    }
                });
                radios = (RadioGroup) popupRootView.findViewById(R.id.ws_meeting_setting_popup);
            }

            int level = LocalSharedPreferencesStorage.getConfigIntValue(mContext,
                    String.valueOf(GlobalHolder.getInstance().getCurrentUserId()) + ":videoMass",
                    V2GlobalConstants.CONF_CAMERA_MASS_LOW);
            switch (level) {
                case V2GlobalConstants.CONF_CAMERA_MASS_LOW:
                    radios.check(R.id.ws_meeting_setting_popup_low_button);
                    break;
                case V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE:
                    radios.check(R.id.ws_meeting_setting_popup_middle_button);
                    break;
                case V2GlobalConstants.CONF_CAMERA_MASS_HIGH:
                    radios.check(R.id.ws_meeting_setting_popup_high_button);
                    break;
            }
            // calculate arrow offset
            int locationWdith = 0;
            int locationHeight = 0;
            V2Log.i(TAG, "popupWidth : " + popupWidth + " -- popupHeight : " + popupHeight);
            if (judgeIsBigPad()) {
                locationWdith = (GlobalConfig.SCREEN_WIDTH - popupWidth) / 2;
                locationHeight = (GlobalConfig.SCREEN_HEIGHT - popupHeight) / 2;
            } else {
                locationWdith = (GlobalConfig.SCREEN_HEIGHT - popupWidth) / 2;
                locationHeight = (GlobalConfig.SCREEN_WIDTH - popupHeight) / 2;
            }
            mSettingWindow.showAtLocation(mRootContainer, Gravity.NO_GRAVITY, locationWdith, locationHeight);
        }
    }

    private class ConverseCameraOnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            doReverseCamera();
        }
    }

    private class LocalServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName cname, IBinder binder) {
            V2Log.i(TAG, " Service bound in video meeting");
            mServiceBound = true;
            ConferencMessageSyncService cms = ((ConferencMessageSyncService.LocalBinder) binder).getService();
            v2ConferenceRequest = cms.getConferenceService();

            ds = cms.getDocService();

            // register listener for conference service
            v2ConferenceRequest.registerPermissionUpdateListener(mHandler, NOTIFY_USER_PERMISSION_UPDATED, null);
            v2ConferenceRequest.registerAttendeeEnterOrExitListener(mHandler, ATTENDEE_ENTER_OR_EXIT_LISTNER, null);
            v2ConferenceRequest.registerKickedConfListener(mHandler, NOTIFICATION_KICKED, null);
            v2ConferenceRequest.registerSyncStateListener(mHandler, SYNC_STATE_NOTIFICATION, null);
            v2ConferenceRequest.registerInvitationStateListener(mHandler, INVITATION_STATE_NOTIFICATION, null);
            v2ConferenceRequest.registerVoiceActivationListener(mHandler, VOICEACTIVATION_NOTIFICATION, null);
            v2ConferenceRequest.registerVideoMixerListener(mHandler, VIDEO_MIX_NOTIFICATION, null);
            v2ConferenceRequest.registerLectureRequestListener(mHandler, NOTIFY_HOST_PERMISSION_REQUESTED, null);
            v2ConferenceRequest.registerAttendeeDeviceListener(mHandler, ATTENDEE_DEVICE_LISTENER, null);
            v2ConferenceRequest.registerChairManChangeListener(mHandler, CHAIR_MAN_CHANGE_LISTENER, null);

            // Register listen to document notification
            ds.registerNewDocNotification(mHandler, NEW_DOC_NOTIFICATION, null);
            ds.registerDocPageListNotification(mHandler, DOC_PAGE_LIST_NOTIFICATION, null);
            ds.registerDocDisplayNotification(mHandler, DOC_DOWNLOADE_COMPLETE_ONE_PAGE_NOTIFICATION, null);

            ds.registerdocPageActiveNotification(mHandler, DOC_TURN_PAGE_NOTIFICATION, null);
            ds.registerDocClosedNotification(mHandler, DOC_CLOSED_NOTIFICATION, null);

            ds.registerDocPageAddedNotification(mHandler, DOC_ADDED_ONE_PAGE_NOTIFICATION, null);
            ds.registerPageCanvasUpdateNotification(mHandler, DOC_PAGE_CANVAS_NOTIFICATION, null);
//            v2ConferenceRequest.notifyAllMessage(cg.getGroupID());
            suspendOrResume(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName cname) {
            mServiceBound = false;
        }

    }

    private class LocalCameraSHCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
            V2Log.d("DEBUG", "surfaceChanged ");
            mLocalHolderIsCreate = true;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            V2Log.d("DEBUG", "surfaceCreated ");
            if (!mLocalHolderIsCreate) {
                V2Log.d(TAG, "Create new holder " + holder);
                mLocalHolderIsCreate = true;
                Message.obtain(mHandler, ONLY_SHOW_LOCAL_VIDEO).sendToTarget();
            } else {
                openLocalCamera();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            V2Log.d("DEBUG", "surfaceDestroyed ");
            closeLocalCamera();
        }

    }

    private class LocalCameraOnTouchListener implements OnTouchListener {

        private long lastPressedTime = 0;
        int lastX;
        int lastY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
            } else if (action == MotionEvent.ACTION_MOVE) {
                updateParameters(view, event);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
            } else if (action == MotionEvent.ACTION_UP) {
                long currTime = System.currentTimeMillis();
                if (currTime - lastPressedTime < 400) {
                    zoom(view);
                    lastPressedTime = 0;
                } else {
                    lastPressedTime = System.currentTimeMillis();
                }
                view.performClick();
            }
            return true;
        }

        private void zoom(View view) {
            int width = 0;
            int height = 0;
            FrameLayout.LayoutParams vl = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (view.getTag() == null || view.getTag().toString().equals("out")) {
                width = vl.width / 2;
                view.setTag("in");
            } else {
                width = vl.width * 2;
                view.setTag("out");
                int[] location = new int[2];
                view.getLocationInWindow(location);

                // If zoom out
                if (location[0] < vl.width + 16) {
                    vl.rightMargin -= (vl.width + 16 - location[0]);
                }
                height = width / 4 * 3;
                if (location[1] < height + 16) {
                    vl.bottomMargin -= (height + 16 - location[1]);
                }
            }

            width -= width % 16;
            height = width / 4 * 3;
            height -= height % 16;

            vl.width = width;
            vl.height = height;
            view.setLayoutParams(vl);
        }

        private void updateParameters(View view, MotionEvent event) {
            FrameLayout.LayoutParams rl = (FrameLayout.LayoutParams) view.getLayoutParams();
            Rect r = new Rect();
            mRootContainer.getDrawingRect(r);

            rl.bottomMargin -= (event.getRawY() - lastY);
            rl.rightMargin -= (event.getRawX() - lastX);
            if (rl.bottomMargin < 0) {
                rl.bottomMargin = 0;
            }
            if (rl.rightMargin < 0) {
                rl.rightMargin = 0;
            }
            if ((r.right - r.left - view.getWidth()) < rl.rightMargin) {
                rl.rightMargin = r.right - r.left - view.getWidth();
            }

            if ((r.bottom - r.top) - (rl.bottomMargin + view.getHeight()) <= 5) {
                rl.bottomMargin = r.bottom - r.top - view.getHeight() - 5;
            }

            ((ViewGroup) view.getParent()).updateViewLayout(view, rl);
            // make sure draging view is first front of all
            view.bringToFront();
        }
    }

    private class ContentLayoutMainTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            MessageUtil.hideKeyBoard(mContext, v.getWindowToken());
            return false;
        }

    }

    private class LeftSubViewListener
            implements LeftShareDocLayout.DocListener, LeftAttendeeListLayout.VideoAttendeeActionListener,
            LeftMessageChattingLayout.ChattingListener, LeftInvitionAttendeeLayout.Listener {

        @Override
        public void requestSendMsg(VMessage vm) {
            cs.requestSendChatMessage(vm);
        }

        @Override
        public void requestChattingViewFixedLayout(View v) {
            requestSubViewFixed();
            adjustContentLayout();
        }

        @Override
        public void requestChattingViewFloatLayout(View v) {
            requestSubViewFloat();
            adjustContentLayout();
        }

        // 参会人列表拖动响应
        @Override
        public void OnAttendeeDragged(Attendee at, UserDeviceConfig udc, int x, int y) {
            // if (at == null || udc == null) {
            // return;
            // }
            // if (at.getAttId() ==
            // GlobalHolder.getInstance().getCurrentUserId()
            // || !at.isJoined()) {
            // return;
            // }
            //
            // for (ConfSurfaceView sw : mCurrentShowedSV) {
            // if (sw.udc.getDeviceID().equals(udc.getDeviceID())) {
            // return;
            // }
            // }
            //
            // for (ConfSurfaceView sw : mCurrentShowedSV) {
            // int[] lo = new int[2];
            // Rect r = new Rect();
            // sw.getView().getDrawingRect(r);
            // sw.getView().getLocationOnScreen(lo);
            // r.left = lo[0];
            // r.right = lo[0] + r.right;
            // r.top = lo[1];
            // r.bottom = lo[1] + r.bottom;
            // if (r.contains(x, y)) {
            // boolean flag = showOrCloseAttendeeVideo(sw.udc);
            // // update opened video view background
            // mAttendeeContainer.updateCurrentSelectedBg(flag, sw.at,
            // sw.udc);
            // sw.at = at;
            // sw.udc = udc;
            // // update new opened video view background
            // flag = showOrCloseAttendeeVideo(udc);
            // mAttendeeContainer.updateCurrentSelectedBg(flag, at, udc);
            //
            // return;
            // }
            // }
            //
            // OnAttendeeClicked(at, udc);
            // if (udc.isShowing()) {
            // mAttendeeContainer.updateCurrentSelectedBg(true, at, udc);
            // }
        }

        @Override
        public void OnAttendeeClicked(Attendee at, UserDeviceConfig udc) {
            showAttendee(at, udc);
        }

        @Override
        public void requestAttendeeViewFixedLayout(View v) {
            requestSubViewFixed();
            adjustContentLayout();
        }

        @Override
        public void requestAttendeeViewFloatLayout(View v) {
            requestSubViewFloat();
            adjustContentLayout();
        }

        @Override
        public void updateDoc(V2Doc doc, Page p) {
            // If current user is host
            boolean isLecturer = (currentAttendee.getLectureState() == Attendee.LECTURE_STATE_GRANTED);
            V2Log.d("DOC_TEST", "移动端切换文档，通知其他端跟随！ isLecturer -> " + isLecturer);
            if (!isLecturer) {
                if (doc.getId().equals(mCurrentLecturerActivateDocIdStr)
                        && p.getNo() == mCurrentLecturerActivateDocPage.getNo()) {
                    // 当用户又重新切换到与主席同一个文档并且是同一页的时候，继续跟随
                    isFreeMode = false;
                } else {
                    isFreeMode = true;
                }
            } else {
                mCurrentLecturerActivateDocIdStr = doc.getId();
                mCurrentLecturerActivateDocPage = p;
            }
            ds.switchDoc(currentAttendee.getAttId(), doc, isLecturer, null);
        }

        @Override
        public void requestShareImageDoc(View v) {
            isMoveTaskBack = false;
            Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
            intent.putExtra("fromPlace", "Conference");
            startActivityForResult(intent, SUB_ACTIVITY_CODE_SHARE_DOC);
        }

        @Override
        public void requestDocViewFixedLayout(View v) {
            // If current doc layout size is full screen, then ignore
            // reques fixed size reqeust.
            if (mDocContainer.isFullScreenSize()) {
                return;
            }
            requestSubViewFixed();
            adjustContentLayout();

        }

        @Override
        public void requestDocViewFloatLayout(View v) {
            // If current doc layout size is full screen, then ignore
            // reques fixed size reqeust.
            if (mDocContainer.isFullScreenSize()) {
                return;
            }
            requestSubViewFloat();
            adjustContentLayout();
        }

        @Override
        public void requestDocViewFillParent(View v) {
            requestSubViewFillScreen();
            adjustContentLayout();
        }

        @Override
        public void requestDocViewRestore(View v) {
            requestSubViewRestore();
            // Request fixed size for doc layout
            requestSubViewFixed();
            adjustContentLayout();

        }

        @Override
        public void requestInvitation(Conference conf, List<User> attendUsers, boolean isNotify) {
            if (attendUsers == null || attendUsers.size() <= 0) {
                if (isNotify) {
                    Toast.makeText(mContext, R.string.warning_no_attendee_selected, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // ignore call back;
            v2ConferenceRequest.inviteAttendee(conf, attendUsers, null);
            // Hide invitation layout
            mMenuInviteAttendeeButton.performClick();
        }

        @Override
        public void requestShareDocClose(View v) {
            V2Doc preV2Doc = null;
            V2Doc nextV2Doc = null;
            boolean finded = false;
            for (String key : mDocs.keyOrderList()) {
                V2Doc v2doc = mDocs.get(key);
                if (v2doc == null) {
                    continue;
                }

                if (finded) {
                    nextV2Doc = v2doc;
                    break;
                }
                if (v2doc.getId().equals(mCurrentLecturerActivateDocIdStr)) {
                    finded = true;
                }

                if (!finded) {
                    preV2Doc = v2doc;
                }
            }

            String willCloseDocIDStr = mCurrentLecturerActivateDocIdStr;
            if (nextV2Doc != null) {
                // 找到下一个文档
                mDocContainer.updateCurrentDoc(nextV2Doc);
                updateDoc(nextV2Doc, nextV2Doc.getActivatePage());
                V2Log.i("DOC_TEST", "打开下一个文档：" + nextV2Doc.getId() + " - name : " + nextV2Doc.getDocName());
            } else {
                if (preV2Doc != null) {
                    // 找到上一个文档
                    mDocContainer.updateCurrentDoc(preV2Doc);
                    updateDoc(preV2Doc, preV2Doc.getActivatePage());
                    V2Log.i("DOC_TEST", "打开上一个文档：" + preV2Doc.getId() + " - name : " + preV2Doc.getDocName());
                }
            }
            v2ConferenceRequest.closeShareDoc(conf, willCloseDocIDStr);
        }

        @Override
        public boolean checkSurfaceViewMax() {
            return checkVideoExceedMaminum();
        }
    }

    @Override
    public void notifyChatInterToReplace(VMessage vm) {
        V2Log.d("binaryReplace", " 接收到替换沙漏的回调 , uuid is : " + vm.getUUID());
        if (mMessageContainer == null) {
            initMsgLayout();
        }
        mMessageContainer.notifyReplaceImage(vm);
    }

    public void showAttendee(Attendee at, UserDeviceConfig udc) {
        if (isSyn) {
            Toast.makeText(getApplicationContext(), R.string.conference_toast_chairman_synchronous_video,
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!mLocalCameraChangeEnd) {
            return;
        }

        if (at.getAttId() == GlobalHolder.getInstance().getCurrentUserId()) {
            if (mAttendeeContainer.mIsOwnerVideoOpen) {
                mAttendeeContainer.mIsOwnerVideoOpen = false;
            } else {
                mAttendeeContainer.mIsOwnerVideoOpen = true;
            }
            mAttendeeContainer.updateDisplay();

            mLocalCameraChangeEnd = false;
            if (mAttendeeContainer.mIsOwnerVideoOpen) {
                AnimationHepler.getInstance().loadAnimation(mContext, mLocalSurfaceViewLy.getId(),
                        R.anim.alpha_from_0_to_1, mLocalSurfaceViewLy);
            } else {
                AnimationHepler.getInstance().loadAnimation(mContext, mLocalSurfaceViewLy.getId(),
                        R.anim.alpha_from_1_to_0, mLocalSurfaceViewLy);
            }
        } else {
            Log.e(TAG, "at type-->" + at.getType());
            if (at.getType() != Attendee.TYPE_MIXED_VIDEO) {
                int key = (int) at.getAttId();
                Attendee attendee = mAttendeeMap.get(key);
                if (attendee != at) {
                    List<UserDeviceConfig> devices = getAttendeeDevices(attendee);
                    if (devices != null) {
                        for (int i = 0; i < devices.size(); i++) {
                            UserDeviceConfig userDeviceConfig = devices.get(i);
                            if (udc.getDeviceID().equals(userDeviceConfig.getDeviceID())) {
                                userDeviceConfig.setShowing(!udc.isShowing());
                                break;
                            }
                        }
                    }
                }

                if (at.isJoined()) {
                    V2Log.d(TAG, " response item click successfully! is showing : " + udc.isShowing());
                    showOrCloseAttendeeVideo(udc);
                }
            } else {
                showOrCloseMixVideo(udc);
            }
        }
    }

    private Runnable mVoiceActiveRunnable = new Runnable() {

        @Override
        public void run() {
            if (!isVoiceActivation || mVoiceActivationObj == null) {
                return;
            }

            V2Log.d(TAG, "语音激励正在切换视频 ！！！");
            Attendee attendee = mAttendeeMap.get((int) mVoiceActivationObj.getDstUserID());
            if (attendee != null) {
                List<UserDeviceConfig> devices = getAttendeeDevices(attendee);
                if (devices != null) {
                    for (int j = 0; j < devices.size(); j++) {
                        V2Log.d(TAG, "语音激励切换用户，准备打开，用户视频设备列表 --：" + devices.get(j).getDeviceID());
                        if (devices.get(j).getDeviceID().equals(mVoiceActivationObj.getDstDeviceID())) {
                            if (conf.getChairman() == mVoiceActivationObj.getDstUserID()) {
                                mHandler.sendEmptyMessage(CHAIR_MAN_OPEN_LISTENER);
                            } else {
                                if (isChairManVideoOpen != null) {
                                    // 关闭已经打开的主席视频
                                    long chairman = conf.getChairman();
                                    Attendee chairMain = mAttendeeMap.get((int) chairman);
                                    if (attendee != null) {
                                        List<UserDeviceConfig> chairMainDevice = getAttendeeDevices(chairMain);
                                        if (chairMainDevice != null) {
                                            for (int k = 0; k < chairMainDevice.size(); k++) {
                                                if (chairMainDevice.get(k).getDeviceID().equals(isChairManVideoOpen)) {
                                                    chairMainDevice.get(k).setShowing(true);
                                                    closeAttendeeVideo(chairMainDevice.get(k));
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    isChairManVideoOpen = null;
                                }
                                V2Log.d(TAG, "语音激励切换用户，打开新用户视频：" + mVoiceActivationObj.getDstDeviceID());
                                Message.obtain(mHandler, ATTENDEE_OPEN_VIDEO, devices.get(j)).sendToTarget();
                            }
                            break;
                        }
                    }
                }
            }
            mVoiceActivationObj = null;
        }
    };

    private Runnable voiceActiveRunnable = new Runnable() {

        @Override
        public void run() {
            if (isVoiceActivation) {
                openChairManVideo(null);
            }
        }
    };


    /*******************************************************************************
     * *******************************************************************************
     * <p>
     * 我们自己写的多人视频代码
     */

    Map<Long, UserDeviceConfig> map = new HashMap<>();
    Map<Long, StudentSurfaceLayout> map_attendee_surface = new HashMap<>();
    Map<Long, VipUserView> map_attendee_vip_user = new HashMap<>();

    ReentrantLock lock = new ReentrantLock();

    void addNormalPlaceView() {
        final VipUserView vipUserView = new VipUserView(ConferenceActivity2.this);
        vipUserView.setNormalPlaceBtn();
        vipUserView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToastUtil.ShowToast_short(ConferenceActivity2.this, "普通席位");

            }
        });
        video_headers_layout.addView(vipUserView);
    }

    class OpenRemoteCameraRunnable implements Runnable {

        public Attendee getAttendee() {
            return attendee;
        }

        public void setAttendee(Attendee attendee) {
            this.attendee = attendee;
        }

        private Attendee attendee;


        @Override
        public void run() {
            try {
                Log.e(TAG, "OpenRemoteCameraRunnable run...");
                lock.lock();
                if (attendee != null) {
                    if (!attendee.isJoined()) {
                        UserDeviceConfig udc = map.get(attendee.getAttId());
                        if (udc != null) {
                            ConferenceHelper.getConferenceHelper().requestCloseVideoDevice(udc, mHandler);
                        }
                        //退出的人是主席
                        if (isChairManVideoOpen != null && isChairManVideoOpen.equals(udc.getDeviceID())) {
                            isChairManVideoOpen = null;
                        }
                        StudentSurfaceLayout studentSurfaceLayout = map_attendee_surface.get(attendee.getAttId());
                        if (studentSurfaceLayout != null) {
                            video_layout2.removeView(studentSurfaceLayout);
                            map_attendee_surface.remove(attendee.getAttId());
                        }
                        System.gc();
                    } else {
                        Log.e(TAG, "joinAttdee -->" + attendee.getAttId() + " " + attendee.getUser().getDisplayName());
                        VipUserView vipUserView = new VipUserView(ConferenceActivity2.this);
                        if (map_attendee_vip_user.get(attendee.getAttId()) != null) {
                            VipUserView vipUserViewold = map_attendee_vip_user.get(attendee.getAttId());
                            video_headers_layout.removeView(vipUserViewold);
                            map_attendee_vip_user.remove(attendee.getAttId());
                        }
                        video_headers_layout.addView(vipUserView, 0);
                        map_attendee_vip_user.put(attendee.getAttId(), vipUserView);
                        vipUserView.setAttendee(attendee);
                        StudentSurfaceLayout studentSurfaceLayout = new StudentSurfaceLayout(ConferenceActivity2.this);
                        User user = attendee.getUser();
                        if (user != null) {
                            Log.e(TAG, "student join1-->" + user.toString());
                            user = GlobalHolder.getInstance().getUser(user.getmUserId());
                            Log.e(TAG, "student join2-->" + user.toString());
                        }
                        studentSurfaceLayout.setStudentName(CourseInfoSingleton.getCourseInfo().getStudentNamePars().get(attendee.getAttId()));
                        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
                        int surfaceHeight = screenHeight / 4;
                        int surfaceWidth = surfaceHeight * 4 / 3;
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(surfaceWidth, surfaceHeight);
                        layoutParams.leftMargin = 5;
                        layoutParams.bottomMargin = 5;
                        studentSurfaceLayout.setLayoutParams(layoutParams);
                        if (map_attendee_surface.get(attendee.getAttId()) != null) {
                            StudentSurfaceLayout studentSurfaceLayoutOld = map_attendee_surface.get(attendee.getAttId());
                            video_layout2.removeView(studentSurfaceLayoutOld);
                        }
                        video_layout2.addView(studentSurfaceLayout);
                        map_attendee_surface.put(attendee.getAttId(), studentSurfaceLayout);

                        studentSurfaceLayout.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
                                int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
                                if (v.getWidth() < screenWidth / 2) {
                                    int surfaceWidth = screenWidth / 2;
                                    int surfaceHeight = surfaceWidth * 3 / 4;
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(surfaceWidth, surfaceHeight);
                                    v.setLayoutParams(layoutParams);
                                } else {
                                    int surfaceHeight = screenHeight / 4;
                                    int surfaceWidth = surfaceHeight * 4 / 3;
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(surfaceWidth, surfaceHeight);
                                    v.setLayoutParams(layoutParams);
                                }
                            }
                        });

                        int type = V2GlobalConstants.GROUP_TYPE_CONFERENCE;
                        long conferenceId = conf.getId();
                        long attendeeId = attendee.getAttId();
                        String deviceId = attendeeId + ":Camera";
                        UserDeviceConfig userDeviceConfig = new UserDeviceConfig(type, conferenceId, attendeeId, deviceId, null);
                        map.put(attendee.getAttId(), userDeviceConfig);
                        userDeviceConfig.setVp(new VideoPlayer());
                        userDeviceConfig.setSVHolder(studentSurfaceLayout.getSurfaceView());
                        SurfaceHolderObserver observer = new SurfaceHolderObserver(cg, cs, userDeviceConfig);
                        userDeviceConfig.getSVHolder().getHolder().addCallback(observer);
                        userDeviceConfig.setShowing(true);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

    }

    int talkTime = 0;
    Timer t;

    public void initCourseStartTime() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sf.parse(conf.getStartTimeStr());
            long t = System.currentTimeMillis() - date.getTime();
            if (t < 0) {
                t = 0;
            }
            talkTime = (int) (t / 1000);
            Log.i("tvliao", "mTimeLine-" + talkTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 开始计时
     */
    public void startTimer() {
        t = new Timer();
        t.schedule(new TimerTask() {

                       @Override
                       public void run() {
                           long currentTime = System.currentTimeMillis();
                           Date date = new Date(currentTime);
                           Date dateEnd = null;
                           String endTime = CourseInfoSingleton.getCourseInfo().getEndTime();
                           if (!TextUtils.isEmpty(endTime)) {
                               try {
                                   dateEnd = new Date(Long.parseLong(endTime));
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                           }
                           if (dateEnd != null) {
                               //1487854800000
                               long curTimestamp = date.getTime();
                               //long curTimestamp = 1487855305133l;
                               long endTimestamp = dateEnd.getTime();
                               //Log.e(TAG, "curTimestamp-->" + curTimestamp);
                               //Log.e(TAG, "endTimestamp-->" + endTimestamp);
                               if (curTimestamp - endTimestamp > 0) {
                                   Looper.prepare();
                                   AlertMsgUtils.show(ConferenceActivity2.this, "您预设的下课时间已到，请注意安排时间", new AlertMsgUtils.OnDialogBtnClickListener() {

                                       @Override
                                       public void onConfirm(Dialog dialog) {
                                           dialog.dismiss();
                                       }
                                   });
                                   Looper.loop();
                               }
                           }

                           talkTime++;
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   StringBuilder sb = new StringBuilder();
                                   String minutes = "00";
                                   String secs = "00";
                                   String hours = "00";
                                   if (talkTime < 10) {
                                       secs = "0" + talkTime;
                                   } else if (talkTime >= 10 && talkTime < 60) {
                                       secs = talkTime + "";
                                   } else if (talkTime >= 60) {
                                       int min = talkTime / 60;
                                       int sec = talkTime % 60;
                                       int hour = 0;
                                       if (min < 10) {
                                           minutes = "0" + min;
                                       } else if (min >= 10 && min <= 59) {
                                           minutes = "" + min;
                                       } else if (min >= 60) {
                                           hour = min / 60;
                                           min = min % 60;
                                           if (hour < 10) {
                                               hours = "0" + hour;
                                           } else {
                                               hours = hour + "";
                                           }
                                           if (min < 10) {
                                               minutes = "0" + min;
                                           } else {
                                               minutes = "" + min;
                                           }
                                       }
                                       if (sec < 10) {
                                           secs = "0" + sec;
                                       } else {
                                           secs = sec + "";
                                       }
                                   }
                                   sb.append(hours)
                                           .append(":")
                                           .append(minutes).append(":").append(secs);
                                   text_talk_minute.setText(sb.toString());
                               }
                           });
                           return;
                       }
                   }

                , 0, 1000);
    }

    MsgReceiver msgReceiver = new MsgReceiver();

    //接收消息广播  学生举手申请发言
    class MsgReceiver extends BroadcastReceiver {

        String tag = "MsgReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case JNIService.JNI_BROADCAST_NEW_MESSAGE:
                    Log.e(tag, "receive new msg");
                    MessageObject msgObj = intent.getParcelableExtra("msgObj");
                    final long remoteID = msgObj.rempteUserID;
                    long msgID = msgObj.messageColsID;
                    VMessage m = ChatMessageProvider.loadUserMessageById(remoteID, msgID);
                    String getInfo = m.getPlainText();
                    JSONObject json = JSONObject.parseObject(getInfo);
                    if (json == null)
                        return;
                    Log.e(tag, "msg body-->" + json.toString());
                    try {
                        int type = json.getInteger("type");
                        switch (type) {
                            case ConstantParams.MESSAGE_TYPE_APPLY_SPEAK:
                                Log.e(tag, remoteID + "apply speak");
                                final RaiseHandPopWindow raiseHandPopWindow = new RaiseHandPopWindow(ConferenceActivity2.this, new RaiseHandPopWindow.OnHandClickListener() {
                                    @Override
                                    public void agree(RaiseHandPopWindow popWindow) {
                                        popWindow.dismiss();
                                        sendMsg(ConstantParams.MESSAGE_TYPE_AGREE_SPEAK, remoteID);
                                        isJY = false;
                                        Drawable drawableTop = getResources().getDrawable(R.mipmap.all_mute);
                                        text_jy.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
                                    }
                                });
                                final VipUserView vipUserView = map_attendee_vip_user.get(remoteID);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        raiseHandPopWindow.showAsDropDown(vipUserView, 20, 0);
                                    }
                                }, 1000);

                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }


    /**
     * 获取学生信息
     * <p>
     * 是否为vip用户 0,否 1，是（result为：0104 并且角色为学生时 有返）
     */

    public void getStudentInfo(final Attendee att) {
        final User user = att.getUser();
        String tvluid = user.getTvlUid();
        Log.e(TAG, "tvluid-->" + tvluid);
        BussinessManger.getInstance(ConferenceActivity2.this).getStudentInfo(tvluid, CourseInfoSingleton.getCourseInfo().getCourse_id(), new SimpleResponseListener<BaseResponse<StudentInfo>>() {
            @Override
            protected void onSuccess(BaseResponse<StudentInfo> t) {
                Log.e(TAG, "getStudentInfo onSuccess-->" + t.toString());
                if (t.code.equals("0000")) {
                    StudentInfo studentInfo = t.data;
                    if (studentInfo != null && !TextUtils.isEmpty(studentInfo.userId)) {
                        User user1 = new User();
                        user1.setmUserId(Long.parseLong("11" + studentInfo.userId));
                        //user1.setNickName(studentInfo.userName);
                        att.setUser(user1);
                        if (studentInfo.isVip.equals("1")) {
                            //isvip
                            openRemoteCameraRunnable.setAttendee(att);
                            mHandler.post(openRemoteCameraRunnable);
                        }
                    }
                }
            }

            @Override
            protected void onError(ErrorResponse response) {
                Log.e(TAG, "getStudentInfo onError-->" + response.toString());
            }
        });
    }


    /**
     * 给学生发消息
     *
     * @param type      消息类型
     * @param studentId 学生ID
     */
    void sendMsg(int type, long studentId) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("fromID", GlobalHolder.getInstance().getCurrentUserId());//自己的userId
            json.put("timeStamp", DataUtil.getDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("message", json.toString());
        User user = GlobalHolder.getInstance().getUser(studentId);
        if (user != null) {
            Log.e("remote tv", user.getmUserId() + " " + user.getAccount() + " " + user.getNickName());
            new MessageSendUtil(ConferenceActivity2.this).sendMessageToRemote(json.toString(),
                    user);//tv的UserId
        }
    }


    List<String> vipIds;

    public void getAllStudents() {
        BussinessManger.getInstance(ConferenceActivity2.this).getStudentList(CourseInfoSingleton.getCourseInfo().getCourse_id(), new SimpleResponseListener<BaseResponse<List<StudentInfo2>>>() {
            @Override
            protected void onSuccess(BaseResponse<List<StudentInfo2>> response) {
                Log.e(TAG, "getStudentList onSuccess-->" + response.toString());
                List<StudentInfo2> list = response.data;
                if (list == null) {
                    return;
                }
                vipIds = new ArrayList<>();
                for (StudentInfo2 studentInfo2 : list) {
                    if (studentInfo2.is_vip == 1) {
                        String userid = "11" + studentInfo2.user_id;
                        vipIds.add(userid);
                        User user = GlobalHolder.getInstance().getUser(Long.parseLong(userid));
                        // user.setNickName(studentInfo2.user_name);
                        // user.setCommentName(studentInfo2.user_name);
                        Attendee attendee = new Attendee(user);
                        CourseInfoSingleton.getCourseInfo().getStudentNamePars().put(attendee.getAttId(), studentInfo2.user_name);
                        CourseInfoSingleton.getCourseInfo().getStudentImgPars().put(attendee.getAttId(), studentInfo2.picurl);
                        initVipUserViews(attendee);
                    }
                }
            }

            @Override
            protected void onError(ErrorResponse response) {
                Log.e(TAG, "getStudentList onError-->" + response.toString());

            }
        });
    }

    public void initVipUserViews(Attendee attendee) {
        VipUserView vipUserView = new VipUserView(ConferenceActivity2.this);
        if (map_attendee_vip_user.get(attendee.getAttId()) != null) {
            VipUserView vipUserViewold = map_attendee_vip_user.get(attendee.getAttId());
            video_headers_layout.removeView(vipUserViewold);
            map_attendee_vip_user.remove(attendee.getAttId());
        }
        video_headers_layout.addView(vipUserView, 0);
        map_attendee_vip_user.put(attendee.getAttId(), vipUserView);
        vipUserView.setAttendee(attendee);
    }


    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    isOpByMine = true;
                    startMessgaeServiceForSendMsg(ConstantParams.MESSAGE_TYPE_TEACHER_IN_BACKGROUND);
                    //currentAttendee.setSpeakingState(false);
                    doApplyOrReleaseSpeak(false);
                } else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {
                    //表示长按home键,显示最近使用的程序列表
                }
            }
        }
    };


    boolean isFirstOpen = true;

    public void resumeRemoteCameras() {
        Log.e(TAG, "resumeRemoteCameras");
        video_layout2.removeAllViews();
        for (Long id : map_attendee_surface.keySet()) {
            StudentSurfaceLayout studentSurfaceLayout = map_attendee_surface.get(id);
            studentSurfaceLayout.bringToFront();
            studentSurfaceLayout.getSurfaceView().setVisibility(View.GONE);
            studentSurfaceLayout.getSurfaceView().setVisibility(View.VISIBLE);
            studentSurfaceLayout.getSurfaceView().setZOrderOnTop(true);
            studentSurfaceLayout.getSurfaceView().setZOrderMediaOverlay(true);
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            int surfaceHeight = screenHeight / 4;
            int surfaceWidth = surfaceHeight * 4 / 3;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(surfaceWidth, surfaceHeight);
            layoutParams.leftMargin = 5;
            layoutParams.bottomMargin = 5;
            studentSurfaceLayout.setLayoutParams(layoutParams);
            video_layout2.addView(studentSurfaceLayout);
        }
    }

    List<Attendee> vips = new ArrayList<>();

    void updateOnLineCount() {
        int onlineCount = 0;
        vips.clear();
        for (int i = 0; i < mAttendeeList.size(); i++) {
            Attendee attendee = mAttendeeList.get(i);
            if (!attendee.isChairMan() && attendee.isJoined()) {
                //当前在线的学生总人数
                onlineCount++;
                for (String vipId : vipIds) {
                    //在线的vip
                    if (attendee.getAttId() == Long.parseLong(vipId)) {
                        vips.add(attendee);
                    }
                }
            }
        }
        Log.e(TAG, "vip_count-->" + vips.size());
        Log.e(TAG, "onLine count-->" + onlineCount);
        VipUserView vipUserView = (VipUserView) video_headers_layout.getChildAt(video_headers_layout.getChildCount() - 1);
        vipUserView.setOnLineText(onlineCount - vips.size() > 0 ? onlineCount - vips.size() : 0);
    }


    /*
    给在线的学生发消息
     * @param type
     */
    private void startMessgaeServiceForSendMsg(int type) {
        ArrayList<Long> list = new ArrayList<>();
        for (int i = 0; i < mAttendeeList.size(); i++) {
            Attendee attendee = mAttendeeList.get(i);
            if (!attendee.isChairMan() && attendee.isJoined()) {
                list.add(attendee.getAttId());
            }
        }
        Intent intent = new Intent(MessageService.ACTION_MSG_RECEIVER);
        intent.putExtra("students", list);
        intent.putExtra("type", type);
        sendBroadcast(intent);
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
