package com.bizcom.vc.activity.conversation;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.MainApplication;
import com.V2.jni.GroupRequest;
import com.V2.jni.ind.FileJNIObject;
import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.V2ChatRequest;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.jni.FileTransStatusIndication;
import com.bizcom.request.jni.FileTransStatusIndication.FileTransProgressStatusIndication;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.request.util.AsyncResult;
import com.bizcom.request.util.FileOperationEnum;
import com.bizcom.service.FileService;
import com.bizcom.service.JNIService;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.MessageUtil.ChatTextViewClick;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.yykEmojiModule.Emoji;
import com.bizcom.util.yykEmojiModule.FaceFragment;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.activity.crow.CrowdDetailActivity;
import com.bizcom.vc.activity.crow.CrowdFilesActivity.CrowdFileActivityType;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.listener.CommonCallBack.CommonNotifyChatInterToReplace;
import com.bizcom.vc.listener.CommonCallBack.CommonUpdateCrowdFileStateInterface;
import com.bizcom.vc.listener.CommonCallBack.CommonUpdateMessageBodyPopupWindowInterface;
import com.bizcom.vc.listener.CommonCallBack.CrowdFileExeType;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.FileInfoBean;
import com.bizcom.vo.Group;
import com.bizcom.vo.OrgGroup;
import com.bizcom.vo.User;
import com.bizcom.vo.enums.NetworkStateCode;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageLinkTextItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ConversationTextActivity extends BaseActivity implements CommonUpdateMessageBodyPopupWindowInterface,
        CommonUpdateCrowdFileStateInterface, CommonNotifyChatInterToReplace, FaceFragment.OnEmojiClickListener {

    private static final String TAG = ConversationTextActivity.class.getSimpleName();
    private static final int VOICE_DIALOG_FLAG_RECORDING = 1;
    private static final int VOICE_DIALOG_FLAG_CANCEL = 2;
    private static final int VOICE_DIALOG_FLAG_WARING_FOR_TIME_TOO_SHORT = 3;
    private static final int BATCH_COUNT = 10;

    private final int START_LOAD_MESSAGE = 1;
    private final int SEND_MESSAGE = 4;
    private final int PLAY_NEXT_UNREAD_MESSAGE = 7;
    private final int REQUEST_DEL_MESSAGE = 8;
    private final int ADAPTER_NOTIFY = 9;
    private final int FILE_STATUS_LISTENER = 20;
    private final int RECORD_STATUS_LISTENER = 21;
    private final int RECORD_MIC_LEVEL = 22;

    /**
     * for activity result
     */
    private static final int SELECT_PICTURE_CODE = 100;
    protected static final int RECEIVE_SELECTED_FILE = 1000;

    private int offset = 0;
    private long mCurrentLoginUserID;
    private long mRemoteChatUserID;
    private long mRemoteGroupID;
    private long mRmoteGroupOwnerUserID;

    private User mCurrentLoginUser;
    private User mRemoteChatUser;
    private int currentConversationViewType;

    private boolean isLoading;
    private boolean mLoadedAllMessages;

    private LinearLayout mFaceLayout;
    private RelativeLayout mToolLayout;

    private View mSendButtonTV;
    private TextView mReturnButtonTV;
    private EditText mMessageET;
    private TextView mUserTitleTV;
    private ImageView mMoreFeatureIV;
    private View mAdditionFeatureContainer;

    private View mSelectFileButtonIVLY;
    private View mVideoCallButtonLY;
    private View mAudioCallButtonLY;
    private View mButtonCreateMettingLY;

    private ImageView mAudioSpeakerIV;
    private TextView mShowCrowdDetailButton;

    private Button mButtonRecordAudio;
    private TextView tips;

    private MediaPlayer mediaPlayer = null;

    private V2ChatRequest mChat = new V2ChatRequest();
    private V2CrowdGroupRequest mGroupChat = new V2CrowdGroupRequest();

    private ListView mMessagesContainer;
    private MessageAdapter adapter;

    private List<VMessage> messageArray;
    private HashMap<String, VMessage> messageMapArray;
    private ArrayList<FileInfoBean> mCheckedList;
    // private HashMap<String, ImageFlushCacheBean> imageFlushCaches = new
    // HashMap<String , ConversationTextActivity.ImageFlushCacheBean>();

    private int currentItemPos = 0;

    private int activityResult = 0;
    private String activityResultImageFilePath;

    private ConversationNotificationObject cov = null;

    /**
     * 录音留言相关成员变量
     */
    private View recordDialogRootView;
    private Dialog mRecordDialog = null;
    private ImageView mVolume;
    private View mSpeakingLayout;
    private View mPreparedCancelLayout;
    private View mWarningLayout;

    private boolean isNeedReload; // 用于onNewIntent判断是否需要重新加载界面聊天数据
    private boolean sendFile; // 用于从个人信息中传递过来的文件，只发送一次
    private boolean isComingNewMessage; // 当界面处在onStop时，来新消息了，再次返回时，需要自动滚动到底部

    private boolean isCreate;
    private boolean isStopped;

    /**
     * 播放录音留言相关变量
     */
    private VMessage playingAudioMessage;
    private MessageBodyView showingPopupWindow;

    private String recordingFileID;
    private long starttime = 0; // 记录真正开始录音时的开始时间
    private long lastTime = 0; // 记录每次录音时的当前时间(毫秒值) ， 用于判断用户点击录音的频率
    private boolean realRecoding;
    private boolean cannelRecoding;
    private boolean timeOutRecording;
    private boolean breakRecord;
    private boolean isDown;

    private long recordTimes = 0;
    private boolean successRecord;
    private int count = 11;
    private Timer mTimer;

    /**
     * 当聊天界面进入onStop状态，需要确定再次回来时需要加载多少条数据来恢复原样
     */
    private int ReloadMessageSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contact_message);
        super.setNeedAvatar(true);
        super.setNeedBroadcast(true);
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);
        GlobalConfig.CHAT_INTERFACE_OPEN = true;
        currentActivityName = TAG;
        messageArray = new ArrayList<>();
        messageMapArray = new HashMap<>();
        initService();
        initExtraObject();
        // initalize vioce function that showing dialog
        createVideoDialog();
        // request ConversationTabFragment to update
        requestUpdateTabFragment();
        android.os.Message m = android.os.Message.obtain(mHandler, START_LOAD_MESSAGE);
        mHandler.sendMessageDelayed(m, 500);
        isCreate = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        offset = 0;
        android.os.Message.obtain(mHandler, START_LOAD_MESSAGE, ReloadMessageSize).sendToTarget();
    }

    @Override
    protected void onStart() {
        super.onStart();
        V2Log.d(TAG, "entry onStart....");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(V2GlobalConstants.MESSAGE_NOTIFICATION_ID);
        chanageAudioFlag();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "entry onStop....");
        isStopped = true;
        // 清空已加载的消息，释放内存
        isLoading = false;
        ReloadMessageSize = messageArray.size();
        if (mCheckedList != null)
            mCheckedList.clear();
        // 清除Lru消息集合中图片的缓存
        MessageUtil.clearLruCache();
        // 停止录音
        breakRecording();
        // 停止播放录音
        // if (GlobalHolder.getInstance().isInAudioCall() ||
        // GlobalHolder.getInstance().isInVideoCall()) {
        if (playingAudioMessage != null) {
            V2Log.d(TAG, "检测到有正在播放的语音，需要停止，ID是 : " + playingAudioMessage.getId());
            stopCurrentAudioPlaying();
        }
        // }
        messageArray.clear();
        messageMapArray.clear();
        Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
        System.gc();
    }

    @Override
    public void onBackPressed() {
        V2Log.d(TAG, "entry onBackPressed");
        checkMessageEmpty();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        V2Log.d(TAG, "entry onDestroy....");
        finishWork();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        V2Log.d(TAG, "entry onNewIntent....");
        ConversationNotificationObject tempCov = intent.getParcelableExtra("obj");
        if (tempCov != null)
            cov = tempCov;
        else
            cov = new ConversationNotificationObject(Conversation.TYPE_CONTACT, 1);
        if (isNeedReload) {
            V2Log.d(TAG, "entry onNewIntent , reloading chating datas...");
            mRemoteChatUserID = 0;
            mRemoteGroupID = 0;
            mRmoteGroupOwnerUserID = 0;
            mRemoteChatUser = null;
            mLoadedAllMessages = false;
            currentItemPos = 0;
            offset = 0;
            messageArray.clear();
            messageMapArray.clear();
            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
            initConversationInfos();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        if (requestCode == SELECT_PICTURE_CODE) {
            String filePath = data.getStringExtra("checkedImage");
            if (filePath == null) {
                V2Toast.makeText(mContext, R.string.error_contact_messag_invalid_image_path, V2Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (isStopped) {
                activityResult = requestCode;
                activityResultImageFilePath = filePath;
            } else {
                handleActivityResult(requestCode);
            }
        } else if (requestCode == RECEIVE_SELECTED_FILE) {
            mCheckedList = data.getParcelableArrayListExtra("checkedFiles");
            if (mCheckedList == null || mCheckedList.size() <= 0)
                return;

            if (isStopped) {
                activityResult = requestCode;
            } else {
                handleActivityResult(requestCode);
            }
        }
    }

    private void handleActivityResult(int activityResult) {
        if (activityResult == SELECT_PICTURE_CODE) {
            VMessage vim = MessageUtil.buildImageMessage(cov.getConversationType(), mRemoteGroupID, mCurrentLoginUser,
                    mRemoteChatUser, activityResultImageFilePath);
            if (vim == null) {
                return;
            }
            // Send message to server
            sendMessageToRemote(vim);
            activityResultImageFilePath = null;
        } else if (activityResult == RECEIVE_SELECTED_FILE) {
            switch (currentConversationViewType) {
                case V2GlobalConstants.GROUP_TYPE_CROWD:
                    for (FileInfoBean bean : mCheckedList) {
                        if (bean == null || TextUtils.isEmpty(bean.filePath))
                            continue;
                        VMessage vm = MessageUtil.buildFileMessage(cov.getConversationType(), mRemoteGroupID,
                                mCurrentLoginUser, mRemoteChatUser, bean);
                        bean.fileUUID = vm.getFileItems().get(0).getUuid();
                        // // Save message
                        vm.setmXmlDatas(vm.toXml());
                        vm.setDate(new Date(GlobalConfig.getGlobalServerTime()));

                        ChatMessageProvider.saveChatMessage(vm);
                        ChatMessageProvider.saveFileVMessage(vm);

                        addMessageToContainer(vm);
                        GlobalHolder.getInstance().changeGlobleTransFileMember(V2GlobalConstants.FILE_TRANS_SENDING,
                                mContext, true, mRemoteGroupID, "ConversationP2PTextActivity onActivity crowd");
                        GroupRequest.getInstance().FileTransUploadGroupFile(vm.getMsgCode(), vm.getGroupId(),
                                vm.getFileItems().get(0).toXmlItem());

                    }

                    Intent intent = new Intent(this, FileService.class);
                    intent.putExtra("gid", mRemoteGroupID);
                    startService(intent);
                    break;
                case V2GlobalConstants.GROUP_TYPE_USER:
                    startSendMoreFile();
                    break;
            }
            mCheckedList.clear();
        }
    }

    private void requestUpdateTabFragment() {
        // Intent i = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
        // i.addCategory(PublicIntent.DEFAULT_CATEGORY);
        // long conversationID;
        // if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER)
        // conversationID = remoteChatUserID;
        // else
        // conversationID = remoteGroupID;
        // i.putExtra("obj", new
        // ConversationNotificationObject(Conversation.TYPE_CONTACT,
        // conversationID, false));
        // mContext.sendBroadcast(i);
    }

    /**
     * 注册广播
     */
    private void initService() {
        CommonCallBack.getInstance().setMessageBodyPopup(this);
        CommonCallBack.getInstance().setCrowdFileState(this);
        CommonCallBack.getInstance().setNotifyChatInterToReplace(this);
        mChat.registerFileTransStatusListener(this.mHandler, FILE_STATUS_LISTENER, null);
        mChat.registerP2PRecordResponseListener(this.mHandler, RECORD_STATUS_LISTENER, null);
        mChat.registerP2PRecordMicResponseListener(this.mHandler, RECORD_MIC_LEVEL, null);
        mGroupChat.registerFileTransStatusListener(this.mHandler, FILE_STATUS_LISTENER, null);
    }

    private void initExtraObject() {
        adapter = new MessageAdapter();
        mMessagesContainer.setAdapter(adapter);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            V2Log.e(TAG, "Receive a null Bundle Object , finish activity!");
            super.onBackPressed();
            return;
        }

        cov = (ConversationNotificationObject) bundle.get("obj");
        if (cov == null) {
            V2Log.e(TAG, "Get null ConversationNotificationObject Object from Bundle , finish activity!");
            super.onBackPressed();
            return;
        }

        cov = getIntent().getParcelableExtra("obj");
        initConversationInfos();

        FaceFragment faceFragment = FaceFragment.Instance();
        getFragmentManager().beginTransaction().add(R.id.contact_message_face_item_ly, faceFragment).commit();
    }

    private void initConversationInfos() {
        mCurrentLoginUserID = GlobalHolder.getInstance().getCurrentUserId();
        mCurrentLoginUser = GlobalHolder.getInstance().getUser(mCurrentLoginUserID);
        if (cov.getConversationType() == Conversation.TYPE_CONTACT) {
            currentConversationViewType = V2GlobalConstants.GROUP_TYPE_USER;
            mRemoteChatUserID = cov.getExtId();
            mRemoteChatUser = GlobalHolder.getInstance().getUser(mRemoteChatUserID);

            if (mRemoteChatUser == null) {
                V2Log.e(TAG, "Get null remoteChatUser Object from GlobleHolder, finish activity!");
                super.onBackPressed();
                return;
            }

            mUserTitleTV.setText(mRemoteChatUser.getDisplayName());
            mButtonCreateMettingLY.setVisibility(View.GONE);
            mShowCrowdDetailButton.setBackgroundResource(R.drawable.ws_btn_chatmessage_contactdetail__selector);
        } else if (cov.getConversationType() == Conversation.TYPE_GROUP) {
            currentConversationViewType = V2GlobalConstants.GROUP_TYPE_CROWD;
            mRemoteGroupID = cov.getExtId();
            Group group = GlobalHolder.getInstance().getGroupById(mRemoteGroupID);
            if (group == null) {
                V2Log.e(TAG, "Get null CrowdGroup Object from GlobleHolder, finish activity!");
                super.onBackPressed();
                return;
            }
            mRmoteGroupOwnerUserID = group.getOwnerUser().getmUserId();
            mVideoCallButtonLY.setVisibility(View.GONE);
            mAudioCallButtonLY.setVisibility(View.GONE);
            mShowCrowdDetailButton.setBackgroundResource(R.drawable.ws_btn_chatmessage_groupdetail_selector);
            mUserTitleTV.setText(group.getName());
        } else if (cov.getConversationType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT
                || cov.getConversationType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
            if (cov.getConversationType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                currentConversationViewType = V2GlobalConstants.GROUP_TYPE_DEPARTMENT;
                OrgGroup departmentGroup = (OrgGroup) GlobalHolder.getInstance().getGroupById(cov.getExtId());
                if (departmentGroup == null) {
                    V2Log.e(TAG, "Get null OrgGroup Object from GlobleHolder, finish activity!");
                    super.onBackPressed();
                    return;
                }
                mUserTitleTV.setText(departmentGroup.getName());
                mShowCrowdDetailButton.setVisibility(View.GONE);
            } else {
                currentConversationViewType = V2GlobalConstants.GROUP_TYPE_DISCUSSION;
                DiscussionGroup discussionGroup = (DiscussionGroup) GlobalHolder.getInstance()
                        .getGroupById(cov.getExtId());
                if (discussionGroup == null) {
                    V2Log.e(TAG, "Get null DiscussionGroup Object from GlobleHolder, finish activity!");
                    super.onBackPressed();
                    return;
                }
                mUserTitleTV.setText(discussionGroup.getName());
                mShowCrowdDetailButton.setBackgroundResource(R.drawable.ws_btn_chatmessage_groupdetail_selector);
            }
            mRemoteGroupID = cov.getExtId();
            mVideoCallButtonLY.setVisibility(View.GONE);
            mAudioCallButtonLY.setVisibility(View.GONE);
            mSelectFileButtonIVLY.setVisibility(View.GONE);
        }
        GlobalHolder.getInstance().setChatState(true, cov.getExtId());
    }

    /**
     * 用于接收从个人信息传递过来的文件
     */
    private void initSendFile() {
        mCheckedList = this.getIntent().getParcelableArrayListExtra("checkedFiles");
        if (mCheckedList != null && mCheckedList.size() > 0) {
            startSendMoreFile();
            mCheckedList.clear();
        }
    }

    /**
     * 初始化录音留言对话框
     */
    public void createVideoDialog() {
        if (mRecordDialog != null) {
            return;
        }

        mRecordDialog = new Dialog(mContext, R.style.MessageVoiceDialog);
        mRecordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater flater = LayoutInflater.from(mContext);
        recordDialogRootView = flater.inflate(R.layout.message_voice_dialog, null);
        mRecordDialog.setContentView(recordDialogRootView);
        mVolume = (ImageView) recordDialogRootView.findViewById(R.id.message_voice_dialog_voice_volume);
        mSpeakingLayout = recordDialogRootView.findViewById(R.id.message_voice_dialog_listening_container);
        mPreparedCancelLayout = recordDialogRootView.findViewById(R.id.message_voice_dialog_cancel_container);
        mWarningLayout = recordDialogRootView.findViewById(R.id.message_voice_dialog_warning_container);
        mRecordDialog.setCancelable(true);
        mRecordDialog.setCanceledOnTouchOutside(false);
        mRecordDialog.setOwnerActivity(this);
        mRecordDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK) {
                    breakRecording();
                    return true;
                }
                return false;
            }
        });
        mRecordDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
            }
        });
        recordDialogRootView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(JNIService.JNI_BROADCAST_NEW_MESSAGE);
        filter.addAction(JNIService.JNI_BROADCAST_MESSAGE_SENT_RESULT);
        filter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
        filter.addAction(PublicIntent.BROADCAST_CROWD_FILE_ACTIVITY_SEND_NOTIFICATION);
        filter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
        filter.addAction(JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION);
        filter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_UPDATED);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
        filter.addAction(JNIService.JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION);
        filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        if (JNIService.JNI_BROADCAST_NEW_MESSAGE.equals(intent.getAction())) {
            if (isStopped) {
                ReloadMessageSize++;
                isComingNewMessage = true;
                return;
            }

            MessageObject msgObj = intent.getParcelableExtra("msgObj");
            int groupType = msgObj.groupType;
            long groupID = msgObj.remoteGroupID;
            long remoteID = msgObj.rempteUserID;
            long msgID = msgObj.messageColsID;
            if (currentConversationViewType == groupType) {
                switch (currentConversationViewType) {
                    case V2GlobalConstants.GROUP_TYPE_CROWD:
                    case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                    case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                        isNeedReload = groupID != mRemoteGroupID;
                        break;
                    case V2GlobalConstants.GROUP_TYPE_USER:
                        isNeedReload = remoteID != mRemoteChatUserID;
                        break;
                }
            } else
                isNeedReload = true;
            // 用于onNewIntent判断是否需要重新加载界面聊天数据，以及是否阻断广播 , true 后台
            boolean isAppBack = ((MainApplication) mContext.getApplicationContext()).isRunningBackgound();
            if (!isNeedReload) {
                if (!isLoading) {
                    boolean result = queryAndAddMessage((int) msgID);
                    if (result) {
                        offset += 1;
                        if (!isAppBack) {
                            CommonCallBack.getInstance().executeNotifyCrowdDetailActivity();
                        }
                    }
                } else {
                    // FIXME 如果此时来新消息了，但还在加载数据库中，则需要处理
                }
            }
        } else if (JNIService.JNI_BROADCAST_MESSAGE_SENT_RESULT.equals(intent.getAction())) {
            String msgUID = intent.getStringExtra("MsgUID");
            Result result = (Result) intent.getSerializableExtra("result");
            int binaryType = intent.getIntExtra("binaryType", -1);
            VMessage vm = messageMapArray.get(msgUID);
            if (vm != null) {
                if (result == Result.SUCCESS) {
                    vm.setState(VMessageAbstractItem.TRANS_SENT_SUCCESS);
                } else {
                    vm.setState(VMessageAbstractItem.TRANS_SENT_FALIED);
                }

                if (binaryType != GlobalConfig.MESSAGE_RECV_BINARY_TYPE_TEXT) {
                    List<VMessageAbstractItem> items = vm.getItems();
                    for (int j = 0; j < items.size(); j++) {
                        VMessageAbstractItem item = items.get(j);
                        if (result == Result.SUCCESS)
                            item.setState(VMessageAbstractItem.TRANS_SENT_SUCCESS);
                        else
                            item.setState(VMessageAbstractItem.TRANS_SENT_FALIED);
                    }
                }

                if (vm.isResendMessage() && result == Result.SUCCESS) {
                    messageArray.remove(vm);
                    vm.isUpdateDate = true;
                    vm.setDate(new Date(GlobalConfig.getGlobalServerTime()));
                    vm.setmXmlDatas(vm.toXml());
                    ChatMessageProvider.deleteMessage(mContext, vm, false);
                    vm = ChatMessageProvider.saveChatMessage(vm);
                    messageArray.add(vm);
                    Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                    scrollToBottom();
                } else {
                    Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                }
            }
            // handler kicked event
        } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
            GroupUserObject obj = intent.getParcelableExtra("group");
            if (obj == null) {
                V2Log.e(TAG, "Received the broadcast to quit the crowd group , but crowd id is wroing... ");
                return;
            }

            if (obj.getmGroupId() == mRemoteGroupID) {
                showShortToast(R.string.discussion_error_removed);
                // 这里不能掉onBackPressed(),会出现异常！
                finish();
            }
        } else if ((PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION.equals(intent.getAction()))) {
            finish();
        } else if ((JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION.equals(intent.getAction()))) {
            if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                ArrayList<FileJNIObject> list = intent.getParcelableArrayListExtra("fileJniObjects");
                long groupID = intent.getLongExtra("groupID", -1l);
                if (list == null || list.size() <= 0 || groupID == -1l) {
                    V2Log.e("ConversationView : May receive new group files failed.. get empty collection");
                    return;
                }
                // 自己上传文件不提示
                if (list.get(0).user.mId == mCurrentLoginUserID || groupID != mRemoteGroupID)
                    return;

                if (isStopped) {
                    ReloadMessageSize++;
                    isComingNewMessage = true;
                    return;
                }

                for (FileJNIObject fileJNIObject : list) {
                    User user = GlobalHolder.getInstance().getUser(list.get(0).user.mId);
                    VMessage vm = new VMessage(cov.getConversationType(), mRemoteGroupID, user, null,
                            new Date(GlobalConfig.getGlobalServerTime()));
                    vm.setUUID(fileJNIObject.vMessageID);
                    VMessageFileItem item = new VMessageFileItem(vm, fileJNIObject.fileName,
                            VMessageFileItem.STATE_FILE_SENT, fileJNIObject.fileId);
                    item.setFileSize(fileJNIObject.fileSize);
                    item.setUuid(fileJNIObject.fileId);
                    addMessageToContainer(vm);
                }
            }
        } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED)) {
            GroupUserObject obj = (GroupUserObject) intent.getExtras().get("obj");
            if (obj == null) {
                V2Log.e(TAG,
                        "JNI_BROADCAST_GROUP_USER_REMOVED --> Update Conversation failed that the user removed ... given GroupUserObject is null");
                return;
            }

            if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                if (obj.getmUserId() == mRemoteChatUserID && mRemoteChatUser.isOutOrg) {
                    finish();
                    MessageUtil.hideKeyBoard(mContext, mMessageET.getWindowToken());
                }
            } else if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER) {
                        if (obj.getmUserId() == mRemoteChatUserID) {
                            finish();
                        }
                    } else {
                        if (mRmoteGroupOwnerUserID == obj.getmUserId()) {
                            finish();
                        }
                    }
                }
            } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                DiscussionGroup discussionGroup = (DiscussionGroup) GlobalHolder.getInstance()
                        .getGroupById(cov.getExtId());
                if (discussionGroup != null) {
                    mUserTitleTV.setText(String.valueOf(discussionGroup.getName()));
                }
            }
        } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_USER_ADDED)) {
            if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                DiscussionGroup discussionGroup = (DiscussionGroup) GlobalHolder.getInstance()
                        .getGroupById(cov.getExtId());
                mUserTitleTV.setText(String.valueOf(discussionGroup.getName()));
            }
        } else if (intent.getAction().equals(PublicIntent.BROADCAST_CROWD_FILE_ACTIVITY_SEND_NOTIFICATION)) {
            String fileID = intent.getStringExtra("fileID");
            int exeType = intent.getIntExtra("exeType", -1);
            for (int i = 0; i < messageArray.size(); i++) {
                VMessage tempVm = messageArray.get(i);
                if (tempVm.getFileItems().size() > 0) {
                    VMessageFileItem vMessageFileItem = tempVm.getFileItems().get(0);
                    if (vMessageFileItem.getUuid().equals(fileID)) {
                        if (exeType == VMessageAbstractItem.STATE_FILE_SENDING)
                            vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_SENDING);
                        else
                            vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_SENT_FALIED);
                        isComingNewMessage = true;
                        break;
                    }
                }
            }
            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
        } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION)) {
            String fileID = intent.getStringExtra("fileID");
            int transType = intent.getIntExtra("transType", -1);
            if (fileID == null || transType == -1)
                return;

            for (int i = 0; i < messageArray.size(); i++) {
                VMessage vm = messageArray.get(i);
                if (vm.getFileItems().size() > 0) {
                    VMessageFileItem vfi = vm.getFileItems().get(0);
                    if (vfi.getUuid().equals(fileID)) {
                        switch (transType) {
                            case V2GlobalConstants.FILE_TRANS_SENDING:
                                vfi.setDownloadedSize(0);
                                vfi.setState(VMessageFileItem.STATE_FILE_SENT_FALIED);
                                break;
                            case V2GlobalConstants.FILE_TRANS_DOWNLOADING:
                                vfi.setDownloadedSize(0);
                                vfi.setState(VMessageFileItem.STATE_FILE_DOWNLOADED_FALIED);
                                break;
                            default:
                                break;
                        }
                        break;
                    }
                }
            }
            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
        } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION)) {
            NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
            if (code != NetworkStateCode.CONNECTED) {
                for (int i = 0; i < messageArray.size(); i++) {
                    VMessage tempVm = messageArray.get(i);
                    if (tempVm.getFileItems().size() > 0) {
                        VMessageFileItem vMessageFileItem = tempVm.getFileItems().get(0);
                        if (vMessageFileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING
                                || vMessageFileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING) {
                            vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED);
                        }

                        if (vMessageFileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING
                                || vMessageFileItem.getState() == VMessageAbstractItem.STATE_FILE_SENDING) {
                            vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_SENT_FALIED);
                        }
                        Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                    }
                }
            }
        } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_UPDATED)) {
            if (cov.getConversationType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                OrgGroup departmentGroup = (OrgGroup) GlobalHolder.getInstance().getGroupById(cov.getExtId());
                mUserTitleTV.setText(departmentGroup.getName());
            } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                DiscussionGroup discussionGroup = (DiscussionGroup) GlobalHolder.getInstance()
                        .getGroupById(cov.getExtId());
                mUserTitleTV.setText(discussionGroup.getName());
            } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                CrowdGroup crowdGroup = (CrowdGroup) GlobalHolder.getInstance().getGroupById(cov.getExtId());
                mUserTitleTV.setText(crowdGroup.getName());
            }
        } else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
            Long uid = intent.getLongExtra("modifiedUser", -1);
            if (uid == -1l) {
                V2Log.e("ConversationTextActivity BROADCAST_USER_COMMENT_NAME_NOTIFICATION ---> update user comment name failed , get id is -1");
                return;
            }

            if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER && uid == mRemoteChatUserID) {
                mRemoteChatUser = GlobalHolder.getInstance().getUser(uid);
                if (mRemoteChatUser != null) {
                    mUserTitleTV.setText(mRemoteChatUser.getDisplayName());
                }
            }
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case START_LOAD_MESSAGE:
                if (isLoading) {
                    break;
                }
                isLoading = true;
                Integer counts = (Integer) msg.obj;
                loadMessagePart(counts);
                endLoadMore();
                break;
            case SEND_MESSAGE:
                VMessage sendMessage = (VMessage) msg.obj;
                mChat.requestSendChatMessage(sendMessage);
                break;
            case PLAY_NEXT_UNREAD_MESSAGE:
                playNextUnreadMessage();
                break;
            case REQUEST_DEL_MESSAGE:
                VMessage message = (VMessage) msg.obj;
                deleteMessage(message);
                adapter.notifyDataSetChanged();
                break;
            case FILE_STATUS_LISTENER:
                FileTransStatusIndication ind = (FileTransStatusIndication) (((AsyncResult) msg.obj).getResult());
                if (ind.indType == FileTransStatusIndication.IND_TYPE_PROGRESS) {
                    FileTransProgressStatusIndication progress = (FileTransProgressStatusIndication) ind;
                    updateFileProgressView(ind.uuid, ((FileTransProgressStatusIndication) ind).nTranedSize,
                            progress.progressType);
                }
                break;
            case RECORD_STATUS_LISTENER:
                int result = msg.arg1;
                int recordType = msg.arg2;
                String fileID = (String) (((AsyncResult) msg.obj).getResult());
                if (result == Result.SUCCESS.value()) {
                    if (recordType != V2GlobalConstants.RECORD_TYPE_START) {
                        String filePath = GlobalConfig.getGlobalAudioPath() + "/" + fileID + ".mp3";
                        File f = new File(filePath);
                        V2Log.d(TAG, "此次录音状态 success : " + successRecord + " | isStopped : " + isStopped
                                + " | 该文件是否存在 exist : " + f.exists() + " | 文件路径 : " + filePath);
                        if (successRecord && !isStopped && f.exists()) {
                            V2Log.d(TAG, "the record file sending successfully! id is : " + fileID);
                            VMessage vm = MessageUtil.buildAudioMessage(cov.getConversationType(), mRemoteGroupID,
                                    mCurrentLoginUser, mRemoteChatUser, filePath, (int) recordTimes);
                            // Send message to server
                            sendMessageToRemote(vm);
                        } else {
                            // delete audio file
                            if (f.exists())
                                f.delete();
                        }
                        successRecord = false;
                        recordTimes = 0;
                    }
                } else {
                    if (recordType == V2GlobalConstants.RECORD_TYPE_START) {
                        V2Log.e(TAG, "record failed! error code is : " + result);
                        breakRecording();
                        String filePath = GlobalConfig.getGlobalAudioPath() + "/" + fileID + ".mp3";
                        File f = new File(filePath);
                        if (f.exists())
                            f.delete();
                    } else {
                        recordTimes = 0;
                        successRecord = false;
                    }
                }
                break;
            case RECORD_MIC_LEVEL:
                int micLevel = Math.abs(msg.arg1);
                // double level = micLevel / 4;
                updateVoiceVolume(micLevel);
                break;
            case ADAPTER_NOTIFY:
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void initViewAndListener() {
        initListViewSytle();
        // init title
        mUserTitleTV = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mReturnButtonTV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        setComBackImageTV(mReturnButtonTV);
        mReturnButtonTV.setOnTouchListener(mHiddenOnTouchListener);

        findViewById(R.id.ws_activity_main_title_functionLy).setVisibility(View.INVISIBLE);
        mShowCrowdDetailButton = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
        mShowCrowdDetailButton.setVisibility(View.VISIBLE);
        mShowCrowdDetailButton.setOnClickListener(mShowCrowdDetailListener);
        // init title

        mSendButtonTV = findViewById(R.id.message_send);
        mSendButtonTV.setOnClickListener(sendMessageButtonListener);

        mMessageET = (EditText) findViewById(R.id.message_text);
        mMessageET.addTextChangedListener(mPasteWatcher);

        mMoreFeatureIV = (ImageView) findViewById(R.id.contact_message_plus);
        mMoreFeatureIV.setOnClickListener(moreFeatureButtonListenr);

        View mSmileIconButton = findViewById(R.id.message_smile_icon_layout);
        mSmileIconButton.setOnClickListener(mSmileIconListener);

        View mSelectImageButtonIV = findViewById(R.id.contact_message_send_image_button_layout);
        mSelectImageButtonIV.setOnClickListener(selectImageButtonListener);
        View mSelectFileButtonIV = findViewById(R.id.contact_message_send_file_button_layout);
        mSelectFileButtonIV.setOnClickListener(mfileSelectionButtonListener);
        View mVideoCallButton = findViewById(R.id.contact_message_video_call_button_layout);
        mVideoCallButton.setOnClickListener(mVideoCallButtonListener);
        View mAudioCallButton = findViewById(R.id.contact_message_audio_call_button_layout);
        mAudioCallButton.setOnClickListener(mAudioCallButtonListener);
        mAudioSpeakerIV = (ImageView) findViewById(R.id.contact_message_speaker);
        mAudioSpeakerIV.setOnClickListener(mMessageTypeSwitchListener);
        mButtonRecordAudio = (Button) findViewById(R.id.message_button_audio_record);
        setRecordAudioTouchListener();
        View mButtonCreateMetting = findViewById(R.id.contact_message_create_metting_button_layout);
        mButtonCreateMetting.setOnClickListener(mButtonCreateMettingListener);

        mAdditionFeatureContainer = findViewById(R.id.contact_message_sub_feature_ly);
        mFaceLayout = (LinearLayout) findViewById(R.id.contact_message_face_item_ly);
        mToolLayout = (RelativeLayout) findViewById(R.id.contact_message_sub_feature_ly_inner);

        // 聊天底部功能按钮的父包裹控件
        mSelectFileButtonIVLY = findViewById(R.id.relativelayout2);
        mVideoCallButtonLY = findViewById(R.id.relativelayout3);
        mAudioCallButtonLY = findViewById(R.id.relativelayout4);
        mButtonCreateMettingLY = findViewById(R.id.relativelayout5);
    }

    private void initListViewSytle() {
        mMessagesContainer = (ListView) findViewById(R.id.conversation_message_list);
        mMessagesContainer.setOnTouchListener(mHiddenOnTouchListener);
        mMessagesContainer.setOnScrollListener(scrollListener);
    }

    private void endLoadMore() {
        if (activityResult == 0) {
            adapter.notifyDataSetChanged();
            if (isComingNewMessage) {
                scrollToBottom();
                isComingNewMessage = false;
            } else {
                scrollToPos(currentItemPos);
            }
        } else {
            handleActivityResult(activityResult);
            activityResult = 0;
            // scrollToBottom();
            adapter.notifyDataSetChanged();
        }
        isLoading = false;
        // 处理从个人信息传递过来的文件
        if (!sendFile) {
            sendFile = true;
            initSendFile();
        }
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {
        if (targetUser == null) {
            return;
        }

        for (int i = 0; i < messageArray.size(); i++) {
            VMessage vMessage = messageArray.get(i);
            long fromUserID = vMessage.getFromUser().getmUserId();
            if (fromUserID == targetUser.getmUserId()) {
                vMessage.isUpdateAvatar = true;
                Bitmap avatarBitmap = vMessage.getFromUser().getAvatarBitmap();
                if (avatarBitmap != null && !avatarBitmap.isRecycled())
                    avatarBitmap.recycle();
            }
        }
        Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
    }

    ;

    // private void startFlushImage(TextView mMsgBodyTV,
    // VMessageImageItem imageItem, Bitmap result , final ChatTextViewClick
    // imageItemCallBack) {
    // String content = " ";
    // mMsgBodyTV.append(content);
    // Drawable dr = new BitmapDrawable(mContext.getResources(), result);
    // dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
    // ClickableImageSpan is = new ClickableImageSpan(dr, imageItem) {
    //
    // @Override
    // public void onClick(VMessageImageItem imageItem) {
    // imageItemCallBack.imageItemClick(imageItem);
    // }
    // };
    //
    // SpannableStringBuilder builder = new SpannableStringBuilder("v2tech");
    // builder.setSpan(is, 0, "v2tech".length(),
    // Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    // mMsgBodyTV.append(builder, 0, "v2tech".length());
    // V2Log.e("test", "mMsgBodyTV len : " + mMsgBodyTV.getText().length());
    // }

    private void chanageAudioFlag() {
        isStopped = false;
        // recover record all flag
        starttime = 0;
        lastTime = 0;
        realRecoding = false;
        cannelRecoding = false;
        isDown = false;
        mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
    }

    private void checkMessageEmpty() {
        boolean isDelete = false;
        if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER) {
            isDelete = ChatMessageProvider.getNewestMessage(mCurrentLoginUserID, mRemoteChatUserID) == null
                    ? true : false;
        } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_CROWD) {
            isDelete = ChatMessageProvider.getNewestGroupMessage(V2GlobalConstants.GROUP_TYPE_CROWD,
                    mRemoteGroupID) == null ? true : false;
        } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
            isDelete = ChatMessageProvider.getNewestGroupMessage(V2GlobalConstants.GROUP_TYPE_DEPARTMENT,
                    mRemoteGroupID) == null ? true : false;
        } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
            isDelete = ChatMessageProvider.getNewestGroupMessage(V2GlobalConstants.GROUP_TYPE_DISCUSSION,
                    mRemoteGroupID) == null ? true : false;
        }

        Intent i = new Intent();
        i.setAction(PublicIntent.CHAT_SYNC_MESSAGE_INTERFACE);
        i.addCategory(PublicIntent.DEFAULT_CATEGORY);
        i.putExtra("groupType", currentConversationViewType);
        i.putExtra("groupID", mRemoteGroupID);
        i.putExtra("remoteUserID", mRemoteChatUserID);
        i.putExtra("isDelete", isDelete);
        sendBroadcast(i);
    }

    private void finishWork() {
        releasePlayer();

        MessageUtil.clearLruCache();
        messageArray.clear();
        messageArray = null;
        messageMapArray.clear();
        messageMapArray = null;
        if (mCheckedList != null) {
            mCheckedList.clear();
            mCheckedList = null;
        }

//		mFaceLayout.removeAllViews();
//		mFaceLayout = null;
        mToolLayout.removeAllViews();
        mToolLayout = null;

        GlobalConfig.CHAT_INTERFACE_OPEN = false;
        GlobalHolder.getInstance().setChatState(false, -1);
        mChat.removeRegisterFileTransStatusListener(this.mHandler, FILE_STATUS_LISTENER, null);
        mChat.removeP2PRecordResponseListener(this.mHandler, RECORD_STATUS_LISTENER, null);
        mChat.removeP2PRecordMicResponseListener(this.mHandler, RECORD_MIC_LEVEL, null);
        mGroupChat.unRegisterFileTransStatusListener(this.mHandler, FILE_STATUS_LISTENER, null);
    }

    private void scrollToBottom() {
        mMessagesContainer.post(new Runnable() {

            @Override
            public void run() {
                mMessagesContainer.setSelection(messageArray.size() - 1);
            }
        });
    }

    private void scrollToPos(final int pos) {
        V2Log.d(TAG, "currentItemPos:-- " + currentItemPos);
        if (pos < 0) {
            V2Log.d(TAG, "没有加载到数据 :" + pos);
            return;
        }

        if (pos >= messageArray.size()) {
            V2Log.d(TAG, "参数不合法或没有加载到数据 :" + pos);
            return;
        }

        if (isCreate) {
            isCreate = false;
            mMessagesContainer.setSelection(pos);
        } else {
            if ((LastFistItem >= messageArray.size() || LastFistItem < 0)) {
                // //
                // 次为了解决setSelection无效的问题，虽然能解决，但会造成界面卡顿。直接setSelection而不notifyDataSetChanged即可
                // mMessagesContainer.post(new Runnable() {
                //
                // @Override
                // public void run() {
                mMessagesContainer.setSelection(pos);
                // }

                // });
            } else {
                mMessagesContainer.setAdapter(adapter);
                mMessagesContainer.setSelectionFromTop(LastFistItem, LastFistItemOffset);
            }

        }
    }

    private void setRecordAudioTouchListener() {
        mButtonRecordAudio.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDown = true;
                        cannelRecoding = false;
                        stopCurrentAudioPlaying();
                        showOrCloseVoiceDialog();
                        long currentTime = System.currentTimeMillis();
                        mHandler.postDelayed(preparedRecoding, 250);
                        if (currentTime - lastTime < 250) {
                            V2Log.d(TAG, "间隔太短，取消录音");
                        }
                        lastTime = currentTime;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Rect r = new Rect();
                        int[] location = new int[2];
                        view.getLocationInWindow(location);
                        view.getDrawingRect(r);
                        // check if touch position out of button than cancel send
                        // voice
                        // message
                        if (isDown) {
                            if (timeOutRecording) {
                                mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
                            } else {
                                if (r.contains((int) event.getX(), (int) event.getY())) {
                                    updateCancelSendVoiceMsgNotification(VOICE_DIALOG_FLAG_RECORDING);
                                } else {
                                    if ((int) event.getY() > r.bottom) {
                                        breakRecord = true;
                                        sendVoiceRecord(view, event);
                                        isDown = false;
                                    } else {
                                        updateCancelSendVoiceMsgNotification(VOICE_DIALOG_FLAG_CANCEL);
                                    }
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        isDown = false;
                        sendVoiceRecord(view, event);
                        break;
                }
                return true;
            }

            private boolean sendVoiceRecord(View view, MotionEvent event) {
                // audio message send by 计时器
                if (timeOutRecording) {
                    V2Log.d(TAG, "audio message send by 计时器，ignore the up event once");
                    timeOutRecording = false;
                    return true;
                }

                // entry normal process , stop recording state 进入正常结束流程
                if (realRecoding) {
                    V2Log.d(TAG, "开始发送录音留言！");
                    // 计算录音时间
                    long seconds = (System.currentTimeMillis() - starttime);
                    // recover all flag 复原标记位
                    lastTime = 0;
                    starttime = 0;
                    realRecoding = false;
                    // Remove timer
                    mHandler.removeCallbacks(timeOutMonitor);
                    // recover button show state
                    mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
                    // check if touch position out of button than cancel
                    // send voice message
                    Rect rect = new Rect();
                    view.getDrawingRect(rect);
                    if (rect.contains((int) event.getX(), (int) event.getY()) && seconds > 1500) {
                        successRecord = true;
                        recordTimes = seconds / 1000;
                        V2Log.d(TAG, "此次录音留言符合要求，可以发送！");
                    } else {
                        if (seconds < 1500) {
                            updateCancelSendVoiceMsgNotification(VOICE_DIALOG_FLAG_WARING_FOR_TIME_TOO_SHORT);
                        } else {
                            if (breakRecord) {
                                successRecord = true;
                                recordTimes = seconds / 1000;
                                V2Log.d(TAG, "此次录音留言符合要求，可以发送！");
                            } else {
                                V2Toast.makeText(mContext, R.string.contact_message_message_cancelled,
                                        V2Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    // stop recording and sending
                    V2Log.w("AudioRequest", "invoking stop recording! id is : " + recordingFileID);
                    stopRecording(recordingFileID);
                    recordingFileID = null;

                    if (seconds < 1500) {
                        // Send delay message for close dialog
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showOrCloseVoiceDialog();
                            }

                        }, 1000);
                    } else {
                        showOrCloseVoiceDialog();
                    }

                    if (mTimer != null) {
                        V2Log.d(TAG, "时间没到，手动停止，恢复原状");
                        mTimer.cancel();
                        mTimer.purge();
                        mTimer = null;
                        count = 11;
                    } else {
                        mHandler.removeCallbacks(mUpdateSurplusTime);
                    }

                } else { // beacuse click too much quick , stop recording..
                    // 此判断是为了防止对话框叠加
                    if (!breakRecord) {
                        cannelRecoding = true;
                        Log.d(TAG, "由于间隔太短，显示short对话框");
                        mHandler.removeCallbacks(preparedRecoding);
                        updateCancelSendVoiceMsgNotification(VOICE_DIALOG_FLAG_WARING_FOR_TIME_TOO_SHORT);
                        showOrCloseVoiceDialog();
                    } else {
                        breakRecord = false;
                        mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
                    }
                }
                return true;
            }
        });
    }

    private boolean startReocrding(String filePath) {
        // MP3
        V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_RECORD_START, filePath);
        return true;
    }

    private void stopRecording(String fileID) {
        V2ChatRequest.invokeNative(V2ChatRequest.NATIVE_RECORD_STOP, fileID);
    }

    /**
     * 异常终止录音
     */
    private void breakRecording() {

        if (realRecoding) {
            breakRecord = true;
            lastTime = 0;
            starttime = 0;
            realRecoding = false;
            // Hide voice dialog
            showOrCloseVoiceDialog();
            stopRecording(recordingFileID);
            recordingFileID = null;
            starttime = 0;
            mHandler.removeCallbacks(timeOutMonitor);
            mHandler.removeCallbacks(mUpdateSurplusTime);
        }
    }

    private void showOrCloseVoiceDialog() {
        if (mRecordDialog == null) {
            createVideoDialog();
        }

        if (mRecordDialog.isShowing()) {
            mRecordDialog.dismiss();
            mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
        } else {
            tips = (TextView) mSpeakingLayout.findViewById(R.id.message_voice_dialog_listening_container_tips);
            tips.setText(R.string.contact_message_voice_dialog_text);
            mRecordDialog.show();
            recordDialogRootView.setVisibility(View.VISIBLE);
            updateCancelSendVoiceMsgNotification(VOICE_DIALOG_FLAG_RECORDING);
        }
    }

    private Runnable preparedRecoding = new Runnable() {
        @Override
        public void run() {

            if (!cannelRecoding) {
                realRecoding = true;
                recordingFileID = UUID.randomUUID().toString();
                // GlobalConfig.getGlobalAudioPath() + "/" +
                // UUID.randomUUID().toString() + ".mp3";
                boolean resultReocrding = startReocrding(recordingFileID);
                if (resultReocrding) {
                    starttime = System.currentTimeMillis();
                    // Start timer
                    mHandler.postDelayed(timeOutMonitor, 59 * 1000);
                    // start timer for prompt surplus time
                    mHandler.postDelayed(mUpdateSurplusTime, 48 * 1000);
                } else
                    breakRecording();
            }
        }
    };

    private Runnable mUpdateSurplusTime = new Runnable() {

        @Override
        public void run() {
            V2Log.d(TAG, "entry surplus time ...");
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {

                    runOnUiThread(new Runnable() {

                        public void run() {
                            if (count == 0) {
                                V2Log.d(TAG, "time is over.");
                                mTimer.cancel();
                                mTimer.purge();
                                mTimer = null;
                                count = 11;
                                return;
                            }
                            String str = mContext.getText(R.string.contact_message_tips_rest_seconds).toString();
                            str = str.replace("[]", (count - 1) + "");
                            tips.setText(str);
                            count--;
                        }
                    });
                }
            }, 0, 1000);
        }
    };

    private Runnable timeOutMonitor = new Runnable() {

        @Override
        public void run() {
            stopRecording(recordingFileID);
            // send
            timeOutRecording = true;
            realRecoding = false;
            successRecord = true;
            recordTimes = 60;
            recordingFileID = null;
            starttime = 0;
            mHandler.removeCallbacks(mUpdateSurplusTime);
            showOrCloseVoiceDialog();
        }
    };

    private boolean playNextUnreadMessage() {
        if (playingAudioMessage == null || messageArray == null || messageArray.size() == 0)
            return false;

        int startIndex = messageArray.indexOf(playingAudioMessage);
        if (startIndex < 0 || startIndex > messageArray.size())
            return false;

        for (int i = startIndex; i < messageArray.size(); i++) {
            VMessage vm = messageArray.get(i);
            List<VMessageAudioItem> items = vm.getAudioItems();
            if (items.size() > 0) {
                VMessageAudioItem audio = items.get(0);
                if (audio.getReadState() == VMessageAbstractItem.STATE_UNREAD) {
                    V2Log.d(TAG, "start palying next aduio item , id is : " + vm.getId());
                    mMessagesContainer.setSelection(messageArray.indexOf(vm));
                    audio.setStartPlay(true);
                    audio.setReadState(VMessageAbstractItem.STATE_READED);
                    ChatMessageProvider.updateBinaryAudioItem(audio);
                    Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                    return true;
                }
            }
        }
        return false;
    }

    private synchronized boolean startAudioPlaying(String fileName) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                initMediaPlayerListener(mediaPlayer);
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            stopAudioPlaying();
        }
        return true;
    }

    private void stopAudioPlaying() {
        if (mediaPlayer != null) {
            playingAudioMessage.getAudioItems().get(0).setPlaying(false);

            mediaPlayer.stop();
            mediaPlayer.reset();
            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
        }

    }

    private void stopCurrentAudioPlaying() {
        if (playingAudioMessage != null && playingAudioMessage.getAudioItems().size() > 0) {
            stopAudioPlaying();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initMediaPlayerListener(MediaPlayer mediaPlayer) {
        mediaPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                V2Log.e(TAG, "当前语音播放出错！ ID :" + playingAudioMessage.getId() + " | type : " + what + " -- error code : "
                        + extra);
                V2Log.e(TAG, "----------------------------------------");
                stopAudioPlaying();
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                V2Log.d(TAG, "开始播放点击的语音，ID : " + playingAudioMessage.getId());
                mp.start();
                playingAudioMessage.getAudioItems().get(0).setPlaying(true);
            }
        });

        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                V2Log.d(TAG, "当前语音播放完毕! ID : " + playingAudioMessage.getId());
                V2Log.d(TAG, "----------------------------------------");
                stopAudioPlaying();
                if (playingAudioMessage.isLocal()) {
                    playingAudioMessage = null;
                } else {
                    Message.obtain(mHandler, PLAY_NEXT_UNREAD_MESSAGE).sendToTarget();
                }
            }
        });
    }

    private void updateCancelSendVoiceMsgNotification(int flag) {
        if (flag == VOICE_DIALOG_FLAG_CANCEL) { // 松开手指，取消发送
            if (mSpeakingLayout != null) {
                mSpeakingLayout.setVisibility(View.GONE);
            }

            if (mWarningLayout != null) {
                mWarningLayout.setVisibility(View.GONE);
            }

            if (mPreparedCancelLayout != null) {
                mPreparedCancelLayout.setVisibility(View.VISIBLE);
            }
            mButtonRecordAudio.setText(R.string.contact_message_button_up_to_cancel);
        } else if (flag == VOICE_DIALOG_FLAG_RECORDING) {
            if (mSpeakingLayout != null) {
                mSpeakingLayout.setVisibility(View.VISIBLE);
            }
            if (mPreparedCancelLayout != null) {
                mPreparedCancelLayout.setVisibility(View.GONE);
            }
            if (mWarningLayout != null) {
                mWarningLayout.setVisibility(View.GONE);
            }
            mButtonRecordAudio.setText(R.string.contact_message_button_up_to_send);
        } else if (flag == VOICE_DIALOG_FLAG_WARING_FOR_TIME_TOO_SHORT) {
            if (mSpeakingLayout != null) {
                mSpeakingLayout.setVisibility(View.GONE);
            }
            if (mPreparedCancelLayout != null) {
                mPreparedCancelLayout.setVisibility(View.GONE);
            }
            if (mWarningLayout != null) {
                mWarningLayout.setVisibility(View.VISIBLE);
            }
            mButtonRecordAudio.setText(R.string.contact_message_button_send_audio_msg);
        }

    }

    private void updateVoiceVolume(int vol) {
        if (mVolume != null) {
            int resId;
            if (vol <= 45) {
                resId = R.drawable.message_voice_volume_4;
            } else if (45 < vol && vol <= 55) {
                resId = R.drawable.message_voice_volume_3;
            } else if (55 < vol && vol <= 60) {
                resId = R.drawable.message_voice_volume_2;
            } else {
                resId = R.drawable.message_voice_volume_1;
            }
            mVolume.setImageResource(resId);
        }
    }

    private void startVoiceCall() {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            return;
        }

        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.P2P_CONNECT_AUDIO, mRemoteChatUserID, false, null, null);
    }

    private void startVideoCall() {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            return;
        }

        GlobalConfig.startP2PConnectChat(mContext , ConversationP2PAVActivity.P2P_CONNECT_VIDEO , mRemoteChatUserID , false , null , null);
    }

    private OnClickListener sendMessageButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            doSendMessage();
        }
    };

    private OnClickListener mMessageTypeSwitchListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            String tag = (String) view.getTag();
            if (tag != null) {
                if (tag.equals("speaker")) {
                    MessageUtil.hideKeyBoard(mContext, mMessageET.getWindowToken());
                    view.setTag("keyboard");
                    ((ImageView) view).setImageResource(R.drawable.message_keyboard);
                    mButtonRecordAudio.setVisibility(View.VISIBLE);
                    mMessageET.setVisibility(View.INVISIBLE);
                    if (mMoreFeatureIV.getTag() != null) {
                        if (!mMoreFeatureIV.getTag().equals("plus")) {
                            mMoreFeatureIV.setImageResource(R.drawable.message_plus);
                            mMoreFeatureIV.setTag("plus");
                            mAdditionFeatureContainer.setVisibility(View.GONE);
                            mToolLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    mFaceLayout.setVisibility(View.GONE);
                } else if (tag.equals("keyboard")) {
                    view.setTag("speaker");
                    ((ImageView) view).setImageResource(R.drawable.speaking_button);
                    mButtonRecordAudio.setVisibility(View.INVISIBLE);
                    mMessageET.setVisibility(View.VISIBLE);
                }
            }
        }

    };

    private OnClickListener moreFeatureButtonListenr = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMoreFeatureIV.getTag() == null || mMoreFeatureIV.getTag().equals("plus")) {
                mMoreFeatureIV.setImageResource(R.drawable.message_minus);
                mMoreFeatureIV.setTag("minus");
                mAdditionFeatureContainer.setVisibility(View.VISIBLE);
                if (!mMessageET.isShown()) {
                    mAudioSpeakerIV.setTag("speaker");
                    mAudioSpeakerIV.setImageResource(R.drawable.speaking_button);
                    mButtonRecordAudio.setVisibility(View.GONE);
                    mMessageET.setVisibility(View.VISIBLE);
                }

                mToolLayout.setVisibility(View.VISIBLE);
                mFaceLayout.setVisibility(View.GONE);
            } else {
                mMoreFeatureIV.setImageResource(R.drawable.message_plus);
                mMoreFeatureIV.setTag("plus");
                mAdditionFeatureContainer.setVisibility(View.GONE);
                mToolLayout.setVisibility(View.VISIBLE);
                mFaceLayout.setVisibility(View.GONE);
            }
        }
    };

    private OnClickListener selectImageButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ConversationTextActivity.this, ConversationSelectImageActivity.class);
            // startActivity(v , intent);
            startActivityForResult(intent, SELECT_PICTURE_CODE);
        }
    };

    private OnClickListener mSmileIconListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
//			if (mFaceLayout.getChildCount() <= 0) {
//				// init faceItem
//				for (int i = 1; i < GlobalConfig.GLOBAL_FACE_ARRAY.length; i++) {
//					ImageView iv = new ImageView(mContext);
//					iv.setImageResource(GlobalConfig.GLOBAL_FACE_ARRAY[i]);
//					iv.setTag(i + "");
//					iv.setPadding(20, 10, 0, 10);
//					iv.setScaleType(ScaleType.FIT_XY);
//					iv.setOnClickListener(mFaceSelectListener);
//					mFaceLayout.addView(iv);
//				}
//			}
            if (mFaceLayout.getVisibility() == View.GONE) {
                mToolLayout.setVisibility(View.GONE);
                mFaceLayout.setVisibility(View.VISIBLE);
                mAdditionFeatureContainer.setVisibility(View.VISIBLE);
                mMoreFeatureIV.setImageResource(R.drawable.message_plus);
                mMoreFeatureIV.setTag("plus");
            } else {
                mFaceLayout.setVisibility(View.GONE);
                mAdditionFeatureContainer.setVisibility(View.GONE);
            }

        }

    };

    private OnClickListener mFaceSelectListener = new OnClickListener() {

        @Override
        public void onClick(View smile) {
            Editable et = mMessageET.getEditableText();
            String str = et.toString() + " ";
            String[] len = str.split("((/:){1}(.){1}(:/){1})");
            if (len.length > 10) {
                V2Toast.makeText(mContext, R.string.error_contact_message_face_too_much, V2Toast.LENGTH_SHORT).show();
                return;
            }

            String emoji = GlobalConfig
                    .getEmojiStr(GlobalConfig.GLOBAL_FACE_ARRAY[Integer.parseInt(smile.getTag().toString())]);
            mMessageET.append(emoji);
        }

    };

    private View.OnClickListener mfileSelectionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (LocalSharedPreferencesStorage.checkCurrentAviNetwork(mContext)) {
                Intent intent = null;
                if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER) {
                    intent = new Intent(ConversationTextActivity.this, ConversationSelectFileTypeActivity.class);
                    intent.putExtra("uid", mRemoteChatUserID);
                } else if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                    intent = new Intent(ConversationTextActivity.this, ConversationSelectFileActivity.class);
                    intent.putExtra("type", "crowdFile");
                    intent.putExtra("uid", mRemoteGroupID);
                } else {
                    return;
                }
                startActivityForResult(intent, RECEIVE_SELECTED_FILE);
            } else {
                V2Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, V2Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private View.OnClickListener mVideoCallButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (!LocalSharedPreferencesStorage.checkCurrentAviNetwork(mContext)) {
                V2Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, V2Toast.LENGTH_SHORT).show();
                return;
            }
            startVideoCall();
        }
    };

    private View.OnClickListener mAudioCallButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            startVoiceCall();
        }
    };

    private View.OnClickListener mButtonCreateMettingListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent i = new Intent(PublicIntent.START_CONFERENCE_CREATE_ACTIVITY);
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.putExtra("gid", mRemoteGroupID);
            startActivity(i);
        }
    };

    private TextWatcher mPasteWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable edit) {
            if (TextUtils.isEmpty(edit.toString())) {
                mSendButtonTV.setVisibility(View.INVISIBLE);
                mMoreFeatureIV.setVisibility(View.VISIBLE);
            } else {
                mSendButtonTV.setVisibility(View.VISIBLE);
                mMoreFeatureIV.setVisibility(View.INVISIBLE);
            }

            mMessageET.removeTextChangedListener(this);
            MessageUtil.buildChatPasteMessageContent(mContext, mMessageET);
            mMessageET.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence ch, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

    };

    private void doSendMessage() {
        VMessage vm = null;
        if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER)
            vm = MessageUtil.buildChatMessage(mContext, String.valueOf(mMessageET.getText()), currentConversationViewType, mRemoteGroupID,
                    mRemoteChatUser);
        else
            vm = MessageUtil.buildChatMessage(mContext, String.valueOf(mMessageET.getText()), currentConversationViewType, mRemoteGroupID, null);
        if (vm != null)
            sendMessageToRemote(vm);
    }

    /**
     * send chat message to remote
     *
     * @param vm
     */
    private void sendMessageToRemote(final VMessage vm) {
        // Save message
        vm.setmXmlDatas(vm.toXml());
        vm.setState(VMessageAbstractItem.TRANS_TRANSING);
        ChatMessageProvider.saveChatMessage(vm);
        ChatMessageProvider.saveFileVMessage(vm);
        ChatMessageProvider.saveBinaryVMessage(vm);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                offset++;
                addNewMessage(vm);
                scrollToBottom();
                Message.obtain(mHandler, SEND_MESSAGE, vm).sendToTarget();
            }
        });
    }

    private void addMessageToContainer(final VMessage msg) {
        // make offset
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                offset++;
                addNewMessage(msg);
            }
        });
    }

    private void addNewMessage(VMessage msg) {
        if (messageArray == null) {
            return;
        }
        messageArray.add(msg);
        messageMapArray.put(msg.getUUID(), msg);
        Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
    }

    private void removedNewMessage(VMessage msg) {
        if (messageArray == null) {
            return;
        }
        messageMapArray.remove(msg.getUUID());
        messageArray.remove(msg);
    }

    /**
     * 发送文件
     */
    private void startSendMoreFile() {
        for (int i = 0; i < mCheckedList.size(); i++) {
            FileInfoBean bean = mCheckedList.get(i);
            if (TextUtils.isEmpty(bean.filePath))
                return;

            GlobalHolder.getInstance().changeGlobleTransFileMember(V2GlobalConstants.FILE_TRANS_SENDING, mContext, true,
                    mRemoteChatUserID, "ConversationP2PTextActivity onActivity");
            VMessage vim = MessageUtil.buildFileMessage(cov.getConversationType(), mRemoteGroupID, mCurrentLoginUser,
                    mRemoteChatUser, bean);
            sendMessageToRemote(vim);
        }
    }

    private int LastFistItem;
    private int LastFistItemOffset;
    private boolean scrolled = false;
    private OnScrollListener scrollListener = new OnScrollListener() {
        int lastFirst = 0;
        boolean isUPScroll = false;

        @Override
        public void onScroll(AbsListView view, int first, int allVisibleCount, int allCount) {
            if (!scrolled) {
                return;
            }

            View v = mMessagesContainer.getChildAt(0);
            LastFistItem = mMessagesContainer.getFirstVisiblePosition();
            LastFistItemOffset = (v == null) ? 0 : v.getTop();
            if (first <= 2 && isUPScroll && !mLoadedAllMessages) {
                android.os.Message.obtain(mHandler, START_LOAD_MESSAGE).sendToTarget();
                currentItemPos = first;
                // Do not clean image message state when loading message
            }

            // Calculate scrolled direction
            isUPScroll = first < lastFirst;
            lastFirst = first;
        }

        @Override
        public void onScrollStateChanged(AbsListView av, int state) {
            scrolled = state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
            // if(!scrolled && !isFlush){
            // mHandler.post(new Runnable() {
            //
            // @Override
            // public void run() {
            // isFlush = true;
            // V2Log.i("test", "滑动停止，开始刷新");
            // for (Entry<String, ImageFlushCacheBean> entry :
            // imageFlushCaches.entrySet()) {
            // ImageFlushCacheBean imageFlushCacheBean = entry.getValue();
            // startFlushImage(imageFlushCacheBean.imageTV,
            // imageFlushCacheBean.imageItem,
            // imageFlushCacheBean.result ,
            // imageFlushCacheBean.imageItemCallBack);
            // }
            // imageFlushCaches.clear();
            // isFlush = false;
            // }
            // });
            // }
        }

    };

    private MessageBodyView.ClickListener listener = new MessageBodyView.ClickListener() {

        @Override
        public void onLinkMessageClicked(VMessageLinkTextItem link) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("http://" + link.getUrl());
            intent.setData(content_url);
            startActivity(intent);
        }

        @Override
        public void onImageItemClick(VMessage v, VMessageImageItem imageItem) {
            Intent i = new Intent();
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.setAction(PublicIntent.START_VIDEO_IMAGE_GALLERY);
            i.putExtra("uid1", mCurrentLoginUserID);
            i.putExtra("uid2", mRemoteChatUserID);
            i.putExtra("cid", v.getUUID());
            if (imageItem != null)
                i.putExtra("imageID", imageItem.getUuid());
            // type 0: is not group image view
            // type 1: group image view
            i.putExtra("type", currentConversationViewType);
            i.putExtra("gid", mRemoteGroupID);
            mContext.startActivity(i);
            return;
        }

        public void onCrowdFileMessageClicked(CrowdFileActivityType type) {
            Intent i = new Intent(PublicIntent.START_CROWD_FILES_ACTIVITY);
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.putExtra("cid", mRemoteGroupID);
            i.putExtra("crowdFileActivityType", type);
            startActivity(i);
        }

        ;

        @Override
        public void requestPlayAudio(MessageBodyView view, VMessage vm, VMessageAudioItem vai) {
            if (vai != null && vai.getAudioFilePath() != null) {
                V2Log.d(TAG, "-----------------------------");
                V2Log.d(TAG, "准备播放点击的语音" + vm.getId() + ", 先检测是否有正在播放的");
                if (playingAudioMessage != null) {
                    V2Log.d(TAG, "检测到有正在播放的语音，需要停止，ID是 : " + playingAudioMessage.getId());
                    stopCurrentAudioPlaying();
                } else {
                    V2Log.d(TAG, "没有检测到有正在播放的语音");
                }

                playingAudioMessage = vm;
                playingAudioMessage.getAudioItems().get(0).setPlaying(true);
                startAudioPlaying(vai.getAudioFilePath());
                Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
            }
        }

        @Override
        public void reSendMessageClicked(VMessage v) {
            v.setState(VMessageAbstractItem.TRANS_TRANSING);
            v.setResendMessage(true);
            List<VMessageAbstractItem> items = v.getItems();
            for (int i = 0; i < items.size(); i++) {
                VMessageAbstractItem item = items.get(i);
                if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FILE) {
                    item.setState(VMessageAbstractItem.STATE_FILE_SENDING);
                    ChatMessageProvider.updateFileItemState(mContext, (VMessageFileItem) item);
                } else
                    item.setState(VMessageAbstractItem.TRANS_TRANSING);
            }

            // 重新发送失败的图片，需要修改其uuid
            if (v.getImageItems().size() > 0) {
                messageMapArray.remove(v.getUUID());
                String uuid = UUID.randomUUID().toString();
                v.setmOldUUID(v.getUUID());
                v.setUUID(uuid);
                messageMapArray.put(uuid, v);
                List<VMessageImageItem> imageItems = v.getImageItems();
                for (int j = 0; j < imageItems.size(); j++) {
                    imageItems.get(j).setmOldUUID(imageItems.get(j).getUuid());
                    imageItems.get(j).setUuid(UUID.randomUUID().toString());
                }
            }

            int update = ChatMessageProvider.updateChatMessageState(mContext, v);
            if (update <= 0)
                V2Log.e(TAG, "Update chatMessage state failed...message uuid is : " + v.getUUID());
            Message.obtain(mHandler, SEND_MESSAGE, v).sendToTarget();
            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
        }

        @Override
        public void requestDelMessage(VMessage v) {
            List<VMessageAbstractItem> items = v.getItems();
            for (int i = 0; i < items.size(); i++) {
                VMessageAbstractItem item = items.get(i);
                if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FILE) {
                    VMessageFileItem vfi = (VMessageFileItem) item;
                    switch (item.getState()) {
                        case VMessageAbstractItem.STATE_FILE_SENDING:
                        case VMessageAbstractItem.STATE_FILE_PAUSED_SENDING:
                            if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER)
                                GlobalHolder.getInstance().changeGlobleTransFileMember(V2GlobalConstants.FILE_TRANS_SENDING,
                                        mContext, false, mRemoteChatUserID, "ConversationP2PText REQUEST_DEL_MESSAGE");
                            else
                                GlobalHolder.getInstance().changeGlobleTransFileMember(V2GlobalConstants.FILE_TRANS_SENDING,
                                        mContext, false, mRemoteGroupID, "ConversationP2PText REQUEST_DEL_MESSAGE");
                            if (item.getState() == VMessageAbstractItem.STATE_FILE_SENDING) {
                                mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_CANCEL_SENDING);
                            }
                            break;
                        case VMessageAbstractItem.STATE_FILE_DOWNLOADING:
                        case VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING:
                            if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER)
                                GlobalHolder.getInstance().changeGlobleTransFileMember(
                                        V2GlobalConstants.FILE_TRANS_DOWNLOADING, mContext, false, mRemoteChatUserID,
                                        "ConversationP2PText REQUEST_DEL_MESSAGE");
                            else
                                GlobalHolder.getInstance().changeGlobleTransFileMember(
                                        V2GlobalConstants.FILE_TRANS_DOWNLOADING, mContext, false, mRemoteGroupID,
                                        "ConversationP2PText REQUEST_DEL_MESSAGE");
                            if (item.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING) {
                                mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_CANCEL_DOWNLOADING);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            Message.obtain(mHandler, REQUEST_DEL_MESSAGE, v).sendToTarget();
        }

        @Override
        public void requestStopAudio(VMessage vm, VMessageAudioItem vai) {
            V2Log.d(TAG, "手动再次点击停止播放语音 , ID: " + vm.getId());
            V2Log.d(TAG, "---------------------------------------");
            vai.setPlaying(false);
            stopAudioPlaying();
        }

        @Override
        public void requestDownloadFile(View v, VMessage vm, VMessageFileItem vfi) {
            if (vfi == null) {
                return;
            }

            mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_START_DOWNLOAD);
            int index = messageArray.indexOf(vm);
            if (index == messageArray.size() - 1) {
                scrollToBottom();
            }
        }

        @Override
        public void requestPauseTransFile(View v, VMessage vm, VMessageFileItem vfi) {
            if (vfi == null) {
                return;
            }
            mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_PAUSE_SENDING);
        }

        @Override
        public void requestResumeTransFile(View v, VMessage vm, VMessageFileItem vfi) {
            if (vfi == null) {
                return;
            }
            mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_RESUME_SEND);
        }

        @Override
        public void requestPauseDownloadFile(View v, VMessage vm, VMessageFileItem vfi) {
            if (vfi == null) {
                return;
            }
            mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_PAUSE_DOWNLOADING);
            vfi.setState(VMessageFileItem.STATE_FILE_PAUSED_DOWNLOADING);
        }

        @Override
        public void requestResumeDownloadFile(View v, VMessage vm, VMessageFileItem vfi) {
            if (vfi == null) {
                return;
            }
            mChat.requestFile2UpdateStateOperation(vfi, FileOperationEnum.OPERATION_RESUME_DOWNLOAD);
        }

        @Override
        public void onImageGifFileClick(String filePath) {
            Intent i = new Intent();
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.setAction(PublicIntent.START_VIDEO_IMAGE_GALLERY);
            i.putExtra("onlyOpenGif", true);
            i.putExtra("filePath", filePath);
            mContext.startActivity(i);
        }

        @Override
        public void requestFlushImage(TextView mMsgBodyTV, VMessageImageItem imageItem, Bitmap result,
                                      final ChatTextViewClick imageItemCallBack) {
            // if (scrolled) {
            // ImageFlushCacheBean bean = new ImageFlushCacheBean(mMsgBodyTV,
            // imageItem, result , imageItemCallBack);
            // imageFlushCaches.put(imageItem.getUuid(), bean);
            // V2Log.i("test",
            // "当前用户正在滑动，停止刷新图片，加入缓存 : " + imageFlushCaches.size());
            // } else {
            // startFlushImage(mMsgBodyTV, imageItem, result ,
            // imageItemCallBack);
            // }
        }
    };

    private List<VMessage> loadMessage(int batchCount) {
        List<VMessage> array = null;
        switch (currentConversationViewType) {
            case V2GlobalConstants.GROUP_TYPE_USER: {
                array = ChatMessageProvider.loadMessageByPage(Conversation.TYPE_CONTACT, mCurrentLoginUserID,
                        mRemoteChatUserID, batchCount, offset);
                if (array != null) {
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i).getFileItems().size() > 0) {
                            VMessageFileItem vMessageFileItem = array.get(i).getFileItems().get(0);
                            File check = new File(vMessageFileItem.getFilePath());
                            if (!check.isFile() && !check.exists()
                                    && vMessageFileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED) {
                                vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_UNDOWNLOAD);
                                ChatMessageProvider.updateFileItemState(mContext, vMessageFileItem);
                            }
                        }
                    }
                }
            }
            break;
            case V2GlobalConstants.GROUP_TYPE_CROWD:
                array = ChatMessageProvider.loadGroupMessageByPage(Conversation.TYPE_GROUP, mRemoteGroupID,
                        batchCount, offset);
                break;
            case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                array = ChatMessageProvider.loadGroupMessageByPage(V2GlobalConstants.GROUP_TYPE_DEPARTMENT,
                        mRemoteGroupID, batchCount, offset);
                break;
            case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                array = ChatMessageProvider.loadGroupMessageByPage(V2GlobalConstants.GROUP_TYPE_DISCUSSION,
                        mRemoteGroupID, batchCount, offset);
                break;
            default:
                break;
        }

        if (array != null) {
            offset += array.size();
        }
        return array;
    }

    private boolean queryAndAddMessage(final int msgId) {

        VMessage m;
        if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER)
            m = ChatMessageProvider.loadUserMessageById(mRemoteChatUserID, msgId);
        else
            m = ChatMessageProvider.loadGroupMessageById(currentConversationViewType, mRemoteGroupID, msgId);
        if (m == null || (m.getFromUser().getmUserId() != this.mRemoteChatUserID && m.getGroupId() == 0)
                || (m.getGroupId() != this.mRemoteGroupID)) {
            return false;
        }

        // 使当前正在显示popupWindow消失
        if (showingPopupWindow != null)
            showingPopupWindow.dissmisPopupWindow();
        addNewMessage(m);
        if (!isStopped)
            this.scrollToBottom();
        else
            isComingNewMessage = true;

        return true;
    }

    private void deleteMessage(VMessage vm) {
        removedNewMessage(vm);
        boolean isDeleteOther = true;
        // if (currentConversationViewType == V2GlobalEnum.GROUP_TYPE_CROWD
        // && vm.getFileItems().size() > 0
        // && vm.getFromUser().getmUserId() == GlobalHolder.getInstance()
        // .getCurrentUserId()
        // && vm.getFileItems().get(0).getState() ==
        // VMessageAbstractItem.STATE_FILE_SENDING) {
        // isDeleteOther = false;
        // }
        ChatMessageProvider.deleteMessage(mContext, vm, isDeleteOther);
        if (!isStopped) {
            List<VMessage> messagePages = ChatMessageProvider.loadGroupMessageByPage(Conversation.TYPE_GROUP,
                    mRemoteGroupID, 1, messageArray.size());
            if (messagePages != null && messagePages.size() > 0) {
                VMessage vMessage = messagePages.get(0);
                messageArray.add(0, vMessage);
                messageMapArray.put(vMessage.getUUID(), vMessage);
            }
            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
            V2Log.d(TAG, "现在集合长度：" + messageArray.size());
        }
    }

    private void updateFileProgressView(String uuid, long tranedSize, int progressType) {
        if (messageArray != null) {
            for (int i = 0; i < messageArray.size(); i++) {
                VMessage vm = messageArray.get(i);
                if (vm.getFileItems().size() > 0) {
                    VMessageFileItem item = vm.getFileItems().get(0);
                    if (item.getUuid().equals(uuid)) {
                        VMessageFileItem vfi = ((VMessageFileItem) item);
                        if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_CROWD
                                && vfi.getState() == VMessageAbstractItem.STATE_FILE_SENT) {
                            return;
                        } else {
                            switch (progressType) {
                                case FileTransStatusIndication.IND_TYPE_PROGRESS_END:
                                    if (vfi.getState() == VMessageAbstractItem.STATE_FILE_SENDING) {
                                        vfi.setState(VMessageAbstractItem.STATE_FILE_SENT);
                                    } else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING) {
                                        vfi.setState(VMessageAbstractItem.STATE_FILE_DOWNLOADED);
                                    }
                                    break;
                            }
                            vfi.setDownloadedSize(tranedSize);
                            Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                        }
                    }
                } else if (vm.getAudioItems().size() > 0) {
                    VMessageAudioItem item = vm.getAudioItems().get(0);
                    if (item.getUuid().equals(uuid)) {
                        Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                    }
                }
            }
        }
    }

    /**
     * update all sending or downloading file state to failed..
     */
    public void executeUpdateFileState() {
        if (messageArray == null) {
            V2Log.e(TAG, "executeUpdateFileState is failed ... because messageArray is null");
            return;
        }

        for (int i = 0; i < messageArray.size(); i++) {
            VMessage vm = messageArray.get(i);
            if (vm.getFileItems().size() > 0) {
                List<VMessageFileItem> fileItems = vm.getFileItems();
                for (int j = 0; j < fileItems.size(); j++) {
                    VMessageFileItem item = fileItems.get(j);
                    switch (item.getState()) {
                        case VMessageAbstractItem.STATE_FILE_DOWNLOADING:
                        case VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING:
                            item.setState(VMessageFileItem.STATE_FILE_DOWNLOADED_FALIED);
                            break;
                        case VMessageAbstractItem.STATE_FILE_SENDING:
                        case VMessageAbstractItem.STATE_FILE_PAUSED_SENDING:
                            item.setState(VMessageFileItem.STATE_FILE_SENT_FALIED);
                            break;
                        default:
                            break;
                    }
                    if (!isStopped)
                        Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                }
            }
        }
    }

    @Override
    public void updateMessageBodyPopupWindow(MessageBodyView view) {
        showingPopupWindow = view;
    }

    @Override
    public void updateCrowdFileState(String fileID, VMessage vm, CrowdFileExeType type) {
        if (type == null || messageArray == null) {
            ReloadMessageSize++;
            isComingNewMessage = true;
            return;
        }

        switch (type) {
            case DELETE_FILE:
                if (vm != null) {
                    // VMessage remove = messageMapArray.get(vm.getUUID());
                    // if (remove != null) {
                    // deleteMessage(vm);
                    // Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                    isComingNewMessage = true;
                    // }
                }
                break;
            case UPDATE_FILE:
                for (int i = 0; i < messageArray.size(); i++) {
                    VMessage tempVm = messageArray.get(i);
                    if (tempVm.getFileItems().size() > 0) {
                        VMessageFileItem vMessageFileItem = tempVm.getFileItems().get(0);
                        if (vMessageFileItem.getUuid().equals(fileID)) {
                            VMessageFileItem transItem = vm.getFileItems().get(0);
                            vMessageFileItem.setState(transItem.getState());
                            break;
                        }
                    }
                }
                Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                break;
            default:
                break;
        }
    }

    @Override
    public void notifyChatInterToReplace(final VMessage vm) {
        if (messageArray == null)
            return;

        for (int i = 0; i < messageArray.size(); i++) {
            VMessage replaced = messageArray.get(i);
            if (replaced.getUUID().equals(vm.getUUID())) {
                if (vm.getAudioItems().size() > 0) {
                    replaced.setAudioItems(vm.getAudioItems());
                } else {
                    V2Log.e("binaryReplace", "ConversationTextActivity -- "
                            + "Recevice Binary data from server , and replaced wait! id is : " + vm.getmXmlDatas());
                    replaced.setImageItems(vm.getImageItems());
                }
                Message.obtain(mHandler, ADAPTER_NOTIFY).sendToTarget();
                scrollToBottom();
                break;
            }
        }
    }

    public List<VMessage> loadMessagePart(Integer counts) {
        List<VMessage> array = null;
        if (counts != null) {
            array = loadMessage(counts);
        } else {
            array = loadMessage(BATCH_COUNT);
        }

        if (array == null || array.size() == 0) {
            mLoadedAllMessages = true;
            isLoading = false;
            // 处理从个人信息传递过来的文件
            if (!sendFile) {
                sendFile = true;
                initSendFile();
            }

            handleActivityResult(activityResult);
            return null;
        }

        int loadSize = array.size();
        if (messageArray == null) {
            messageArray = new ArrayList<VMessage>();
        }

        if (loadSize < BATCH_COUNT) {
            mLoadedAllMessages = true;
        }

        V2Log.d(TAG, "此次一共加载了 ： " + loadSize + " 条数据！");
        for (int i = 0; i < array.size(); i++) {
            VMessage vm = array.get(i);
            // 群文件处理,如果是远端用户上传的文件，则强制更改状态为已上传，因为群中远端文件只有一种状态
            if (vm.getFileItems().size() > 0 && vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_CROWD) {
                VMessageFileItem fileItem = vm.getFileItems().get(0);
                // 如果该文件时其他人上传的，则在下载的时候，强制将聊天界面的状态改成已上传
                if (vm.getFromUser().getmUserId() != GlobalHolder.getInstance().getCurrentUserId()
                        && fileItem.getState() != VMessageAbstractItem.STATE_FILE_SENT) {
                    fileItem.setState(VMessageAbstractItem.STATE_FILE_SENT);
                } else { // 如果该文件是自己上传的，则在下载的时候，强制将聊天界面的状态改成已上传
                    if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED
                            || fileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING
                            || fileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED
                            || fileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING) {
                        fileItem.setState(VMessageAbstractItem.STATE_FILE_SENT);
                    }
                }
            }

            messageArray.add(0, vm);
            messageMapArray.put(vm.getUUID(), vm);
        }

        if (counts == null) {
            LastFistItem = LastFistItem + loadSize;
            currentItemPos = loadSize - 1;
            if (currentItemPos == -1)
                currentItemPos = 0;
            V2Log.d(TAG, "当前LastFistItem是 ： " + LastFistItem);
            V2Log.d(TAG, "当前滑动到的pos是 ： " + currentItemPos);
        }
        return array;
    }

    @Override
    public void onEmojiDelete() {
        int action = KeyEvent.ACTION_DOWN;
        int code = KeyEvent.KEYCODE_DEL;
        KeyEvent event = new KeyEvent(action, code);
        mMessageET.onKeyDown(KeyEvent.KEYCODE_DEL, event);
    }

    @Override
    public void onEmojiClick(Emoji emoji) {
        if (emoji != null) {
            Editable et = mMessageET.getEditableText();
            String str = et.toString() + " ";
            String[] len = str.split("((/:){1}(.){1}(:/){1})");
            if (len.length > 10) {
                V2Toast.makeText(mContext, R.string.error_contact_message_face_too_much, V2Toast.LENGTH_SHORT).show();
                return;
            }

                mMessageET.append(emoji.getContent());
        }
    }

    private OnTouchListener mHiddenOnTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent arg1) {
            MessageUtil.hideKeyBoard(mContext, mMessageET.getWindowToken());
            if (mReturnButtonTV == view) {
                onBackPressed();
            }

            if (arg1.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
            }
            return false;
        }

    };

    private OnClickListener mShowCrowdDetailListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (currentConversationViewType == V2GlobalConstants.GROUP_TYPE_USER) {
                Intent i = new Intent();
                i.setClass(mContext, ContactDetail2.class);
                i.putExtra("uid", mRemoteChatUserID);
                startActivity(i);
            } else {
                Group g = GlobalHolder.getInstance().getGroupById(cov.getExtId());
                if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
                    Intent i = new Intent(mContext, CrowdDetailActivity.class);
                    i.putExtra("cid", cov.getExtId());
                    startActivity(i);
                } else if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                    Intent i = new Intent();
                    i.setAction(PublicIntent.SHOW_DISCUSSION_BOARD_DETAIL_ACTIVITY);
                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    i.putExtra("cid", cov.getExtId());
                    startActivity(i);
                }
            }
        }
    };

    class MessageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messageArray.size();
        }

        @Override
        public Object getItem(int pos) {
            return messageArray.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup v) {
            MessageBodyView mv = null;
            VMessage vm = messageArray.get(pos);
            if (convertView == null) {
                mv = new MessageBodyView(mContext, vm, vm.isShowTime());
                mv.setCallback(listener);
                convertView = mv;
            } else {
                // 当正在播放录音，进入onStop后会重新加载数据，这时需要找出正在播放的那个item
                if (playingAudioMessage != null && vm.getUUID().equals(playingAudioMessage.getUUID())) {
                    if (playingAudioMessage.getAudioItems().get(0).isPlaying()) {
                        vm.getAudioItems().get(0).setPlaying(true);
                    } else {
                        vm.getAudioItems().get(0).setPlaying(false);
                    }
                }

                mv = (MessageBodyView) convertView;
                mv.updateView(vm);

                if (vm.getAudioItems().size() > 0) {
                    VMessageAudioItem vMessageAudioItem = vm.getAudioItems().get(0);
                    if (vMessageAudioItem.isStartPlay()) {
                        mv.getCallback().requestPlayAudio(mv, vm, vMessageAudioItem);
                        vMessageAudioItem.setStartPlay(false);
                    }
                }
            }
            return convertView;
        }
    }

    class ImageFlushCacheBean {
        public TextView imageTV;
        public VMessageImageItem imageItem;
        public Bitmap result;
        public ChatTextViewClick imageItemCallBack;

        public ImageFlushCacheBean(TextView imageTV, VMessageImageItem imageItem, Bitmap result,
                                   ChatTextViewClick imageItemCallBack) {
            super();
            this.imageTV = imageTV;
            this.imageItem = imageItem;
            this.result = result;
            this.imageItemCallBack = imageItemCallBack;
        }
    }

    // private void refresh() {
    // mHandler.postDelayed(new Runnable() {
    // @Override
    // public void run() {
    // Tasks.executeInBackground(mContext, new BackgroundWork<Void>() {
    //
    // @Override
    // public Void doInBackground() throws Exception {
    // loadMessagePart(null);
    // return null;
    // }
    // }, new Completion<Void>() {
    // @Override
    // public void onSuccess(Context context, Void vo) {
    // mMessagesContainer.setRefreshSuccess("加载成功"); // 通知加载成功
    // endLoadMore();
    // }
    //
    // @Override
    // public void onError(Context context, Exception e) {
    // // mMessagesContainer.setRefreshSuccess("加载失败"); //
    // // 通知加载成功
    // }
    // });
    // }
    // }, 2 * 1000);
    // }
}
