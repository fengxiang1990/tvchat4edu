package com.bizcom.vc.activity.main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.MainApplication;
import com.V2.jni.ind.BoUserInfoBase;
import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.db.provider.ConversationProvider;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.db.provider.VerificationProvider;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.Notificator;
import com.bizcom.util.SearchUtils;
import com.bizcom.util.SearchUtils.ScrollItem;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.activity.conference.GroupLayout;
import com.bizcom.vc.activity.contacts.AddFriendHistroysHandler;
import com.bizcom.vc.activity.message.MessageAuthenticationActivity;
import com.bizcom.vc.activity.message.VoiceMessageActivity;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.listener.NotificationListener;
import com.bizcom.vo.AddFriendHistorieNode;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.ContactConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.ConversationFirendAuthenticationData;
import com.bizcom.vo.ConversationFirendAuthenticationData.VerificationMessageType;
import com.bizcom.vo.CrowdConversation;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DepartmentConversation;
import com.bizcom.vo.DiscussionConversation;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.MediaRecordConversation;
import com.bizcom.vo.OrgGroup;
import com.bizcom.vo.User;
import com.bizcom.vo.VideoBean;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageQualification;
import com.bizcom.vo.meesage.VMessageQualification.QualificationState;
import com.bizcom.vo.meesage.VMessageQualification.ReadState;
import com.bizcom.vo.meesage.VMessageQualification.Type;
import com.bizcom.vo.meesage.VMessageQualificationApplicationCrowd;
import com.bizcom.vo.meesage.VMessageQualificationInvitationCrowd;
import com.bizcom.vo.meesage.VMessageTextItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TabFragmentMessage extends Fragment implements TextWatcher {
    private static final String TAG = TabFragmentMessage.class.getSimpleName();
    public static final int ACTIVITY_RETURN_UPDATE_CHAT_CONVERSATION = 17;
    public static final int ACTIVITY_RETURN_VOICE_RECORD = 19;

    private static final int UPDATE_CONVERSATION = 9;
    private static final int NEW_MESSAGE_UPDATE = 11;
    private static final int REMOVE_CONVERSATION = 12;
    private static final int UPDATE_VERIFICATION_MESSAGE = 18;

    private VerificationMessageType currentMessageType = null;

    private View rootView;
    private Context mContext;
    private NotificationListener notificationListener;
    private MediaPlayer mChatPlayer;

    private BroadcastReceiver receiver;

    private Set<Conversation> mUnreadConvList = new HashSet<>();
    private List<ScrollItem> mItemList = new ArrayList<>();
    private List<ScrollItem> searchList = new ArrayList<>();
    private SparseIntArray offlineCov = new SparseIntArray();

    private LocalHandler mHandler = new LocalHandler(this);
    private ListView mConversationsListView;
    private ConversationsAdapter adapter = new ConversationsAdapter();

    /**
     * This tag is used to limit the database load times
     */
    private boolean isLoadedCov;
    private boolean isLoading;

    private boolean mIsStartedSearch;
    private boolean isUpdateGroup;
    private boolean isUpdateDeparment;
    private boolean isCallBack;
    private boolean isCreate;
    private boolean isNeedToNotifyShowRead;
    /**
     * 用于搜索
     */
    private boolean isFrist = true;

    /**
     * This tag is used to save current click the location of item.
     */
    private Conversation currentClickConversation;

    /**
     * The two special Items that were showed in Message Interface , them don't
     * saved in database. VerificationItem item used to display verification
     * messages VoiceItem item used to display phone message
     */
    private ScrollItem verificationItem;
    private ScrollItem voiceItem;

    private Conversation voiceMessageItem;
    private GroupLayout voiceLayout;
    private Conversation verificationMessageItemData;

    private ExecutorService service;
    private Resources res;

    private FriendVerificationCache friendCache;
    private VMessageQualification lastVerificationMsg;
    private boolean CrowdVerificationCache;

    // 会议
    private SparseIntArray mLeaveOutConferences = new SparseIntArray();
    private SearchUtils mSearchUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        V2Log.i(TAG, "TabFragmentMessage onCreate()");
        mContext = getActivity();
        res = getResources();
        service = Executors.newCachedThreadPool();
        mSearchUtils = new SearchUtils();
        initBroadcast();
        notificationListener = (NotificationListener) getActivity();
        BitmapManager.getInstance().registerBitmapChangedListener(this.bitmapChangedListener);
        isCreate = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        V2Log.i(TAG, "TabFragmentMessage onCreateView()");
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.tab_fragment_conversations, container, false);
            mConversationsListView = (ListView) rootView.findViewById(R.id.conversations_list_container);
            mConversationsListView.setAdapter(adapter);
            mConversationsListView.setOnItemClickListener(mItemClickListener);
            mConversationsListView.setOnItemLongClickListener(mItemLongClickListener);
            mConversationsListView.setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        V2Log.i(TAG, "TabFragmentMessage onDestroy()");
        super.onDestroy();
        if (mUnreadConvList != null) {
            mUnreadConvList.clear();
            mUnreadConvList = null;
        }

        if (mItemList != null) {
            mItemList.clear();
            mItemList = null;
        }

        if (searchList != null) {
            searchList.clear();
            searchList = null;
        }

        if (offlineCov != null) {
            offlineCov.clear();
            offlineCov = null;
        }

        if (mChatPlayer != null) {
            mChatPlayer.release();
            mChatPlayer = null;
        }
        service.shutdown();
        service = null;
        isUpdateGroup = false;
        try {
            if (receiver != null)
                getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BitmapManager.getInstance().unRegisterBitmapChangedListener(this.bitmapChangedListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    @Override
    public void onDestroyView() {
        V2Log.i(TAG, "TabFragmentMessage onDestroyView()");
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void onStart() {
        V2Log.i(TAG, "TabFragmentMessage onStart()");
        super.onStart();
        if (isCreate) {
            initSpecificationItem();
            loadUserConversation();
            sortAndUpdate();
        }
        isCreate = false;
    }

    @Override
    public void onStop() {
        V2Log.i(TAG, "TabFragmentMessage onStop()");
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RETURN_VOICE_RECORD) {
            MediaRecordProvider.updateAllRecordToReaded();
            if (MediaRecordProvider.queryIsHaveUnreadMessage()) {
                updateUnreadConversation(voiceItem, V2GlobalConstants.READ_STATE_UNREAD);
            } else {
                updateUnreadConversation(voiceItem, V2GlobalConstants.READ_STATE_READ);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

        if (TextUtils.isEmpty(s)) {
            mSearchUtils.clearAll();
            mIsStartedSearch = mSearchUtils.mIsStartedSearch;
            isFrist = true;
            searchList.clear();
            adapter.notifyDataSetChanged();
        } else {
            if (isFrist) {
                mSearchUtils.clearAll();
                List<Object> conversations = new ArrayList<>();
                for (int i = 0; i < mItemList.size(); i++) {
                    conversations.add(mItemList.get(i));
                }
                mSearchUtils.receiveList = conversations;
                isFrist = false;
            }

            searchList.clear();
            searchList = mSearchUtils.startScrollItemSearch(s);
            mIsStartedSearch = mSearchUtils.mIsStartedSearch;
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    public void updateSearchState() {
        mIsStartedSearch = false;
        searchList.clear();
        adapter.notifyDataSetChanged();
    }

    private void initSpecificationItem() {
        // 判断只有消息界面，才添加这两个特殊item
        initVoiceItem();
        initVerificationItem();
        // init voice or video item
        VideoBean newestMediaMessage = MediaRecordProvider.getNewestMediaMessage();
        if (newestMediaMessage != null) {
            voiceMessageItem.setIshouldAdd(true);
            updateVoiceSpecificItemState(newestMediaMessage);
        }

        // init sendFriendToTv friend verification item
        switch (isHaveVerificationMessage()) {
            case CONTACT_TYPE:
                verificationMessageItemData.setIshouldAdd(true);
                AddFriendHistorieNode tempNode = VerificationProvider.getNewestFriendVerificationMessage();
                updateFriendVerificationConversation(tempNode);
                break;
            case CROWD_TYPE:
                verificationMessageItemData.setIshouldAdd(true);
                updateCrowdVerificationConversation(false);
                break;
            default:
                break;
        }
    }

    /**
     * According to mCurrentTabFlag, initialize different intent filter
     */
    private void initBroadcast() {
        receiver = new ConversationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
        intentFilter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
        intentFilter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUPS_LOADED);
        intentFilter.addAction(JNIService.JNI_BROADCAST_OFFLINE_MESSAGE_END);
        intentFilter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);

        // Group
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_UPDATED);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
        // Crowd
        intentFilter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
        intentFilter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
        // Conference
        intentFilter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_REMOVED);
        intentFilter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_INVATITION);

        intentFilter.addAction(ConversationP2PAVActivity.P2P_BROADCAST_MEDIA_UPDATE);
        intentFilter.addAction(JNIService.JNI_BROADCAST_NEW_MESSAGE);
        intentFilter.addAction(JNIService.JNI_BROADCAST_CONTACTS_AUTHENTICATION);
        intentFilter.addAction(JNIService.JNI_BROADCAST_NEW_QUALIFICATION_MESSAGE);
        intentFilter.addAction(JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION);
        intentFilter.addAction(PublicIntent.REQUEST_UPDATE_CONVERSATION);
        intentFilter.addAction(PublicIntent.CHAT_SYNC_MESSAGE_INTERFACE);
        intentFilter.addAction(PublicIntent.BROADCAST_ADD_OTHER_FRIEND_WAITING_NOTIFICATION);
        intentFilter.addAction(PublicIntent.BROADCAST_AUTHENTIC_TO_CONVERSATIONS_TAB_FRAGMENT_NOTIFICATION);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void sortAndUpdate() {
        Collections.sort(mItemList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Load local conversation list
     */
    private void loadUserConversation() {
        if (isLoadedCov || isLoading) {
            return;
        }
        isLoading = true;
        service.execute(new Runnable() {
            @Override
            public void run() {
                List<Conversation> tempList = new ArrayList<>();
                tempList = ConversationProvider.loadUserConversation(tempList);
                tempList = ConversationProvider.loadConfInviteConversation(tempList);
                if (verificationMessageItemData.isIshouldAdd()) {
                    tempList.add(verificationMessageItemData);
                    verificationMessageItemData.setAddedItem(true);
                    verificationMessageItemData.setIshouldAdd(false);
                }

                if (voiceMessageItem.isIshouldAdd()) {
                    tempList.add(voiceMessageItem);
                    voiceMessageItem.setAddedItem(true);
                    voiceMessageItem.setIshouldAdd(false);
                }

                V2Log.d(TAG, "本地聊天数据加载完毕...数量: " + tempList.size());
                fillUserAdapter(tempList);
            }
        });

    }

    /**
     * 判断数据库是否有验证消息
     */
    private VerificationMessageType isHaveVerificationMessage() {
        long crowdTime = 0;
        long friendTime = 0;
        VMessageQualification nestQualification = VerificationProvider.getNewestCrowdVerificationMessage();
        AddFriendHistorieNode friendNode = VerificationProvider.getNewestFriendVerificationMessage();

        if (nestQualification == null && friendNode == null)
            return VerificationMessageType.UNKNOWN;

        if (nestQualification != null)
            crowdTime = nestQualification.getmTimestamp().getTime();

        if (friendNode != null)
            friendTime = friendNode.saveDate;

        if (crowdTime > friendTime)
            return VerificationMessageType.CROWD_TYPE;
        else
            return VerificationMessageType.CONTACT_TYPE;
    }

    private void fillUserAdapter(final List<Conversation> list) {
        V2Log.d(TAG, "fillUserAdapter .... friendCache : " + friendCache + " | CrowdVerificationCache : "
                + CrowdVerificationCache);
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < list.size(); i++) {
                    Conversation cov = list.get(i);
                    if (cov == null) {
                        V2Log.e(TAG, "when execute fillUserAdapter , get null Conversation , index :" + i);
                        continue;
                    }

                    switch (cov.getType()) {
                        case Conversation.TYPE_DEPARTMENT:
                            ((DepartmentConversation) cov).setShowContact(true);
                            break;
                        case Conversation.TYPE_GROUP:
                            ((CrowdConversation) cov).setShowContact(true);
                            break;
                        case Conversation.TYPE_DISCUSSION:
                            ((DiscussionConversation) cov).setShowContact(true);
                            break;
                        case Conversation.TYPE_VERIFICATION_MESSAGE:
                            addVerificationConversation(true, true);
                            continue;
                        case Conversation.TYPE_VOICE_MESSAGE:
                            addVoiceConversation(true);
                            continue;
                    }

                    GroupLayout layout = new GroupLayout(mContext, cov);
                    /**
                     * 除了个人Conversation布局，其他组类Conversation布局默认可能不会显示时间，或者内容
                     * 所以这里需要将布局改变为个人的Conversation布局
                     */
                    if (cov.getType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION)
                        layout.updateDiscussionLayout(true);
                    else
                        layout.updateCrowdLayout();
                    ScrollItem newItem = new ScrollItem(cov, layout, false);
                    mItemList.add(newItem);
                    updateUnreadConversation(newItem, cov.getReadFlag());
                }

                adapter.notifyDataSetChanged();
                scrollToTop();
                isLoadedCov = true;
                isLoading = false;
                checkLeaveOutConference();

                if (friendCache != null && CrowdVerificationCache) {
                    updateVerificationConversation();
                } else {
                    if (friendCache != null) {
                        updateFriendVerificationConversation(friendCache.isNotify, friendCache.isNotifyBar,
                                friendCache.remoteUserName, friendCache.tempNode);
                        friendCache = null;
                    }

                    if (CrowdVerificationCache) {
                        updateCrowdVerificationConversation(false);
                        CrowdVerificationCache = false;
                    }
                }
                if (!isCallBack) {
                    isCallBack = true;
                    CommonCallBack.getInstance().executeUpdateConversationState();
                    V2Log.w("JNIService", "executeUpdateConversationState message");
                }
            }
        });
    }

    /**
     * 初始化通话消息item对象
     */
    private void initVoiceItem() {
        voiceMessageItem = new MediaRecordConversation(Conversation.TYPE_VOICE_MESSAGE, Conversation.SPECIFIC_VOICE_ID);
        voiceMessageItem.setAddedItem(false);
        voiceMessageItem.setIshouldAdd(false);
        voiceMessageItem.setName(res.getString(R.string.specificItem_voice_title));
        voiceLayout = new GroupLayout(mContext, voiceMessageItem);
        voiceMessageItem.setReadFlag(V2GlobalConstants.READ_STATE_READ);
        voiceItem = new ScrollItem(voiceMessageItem, voiceLayout, false);
    }

    /**
     * 初始化验证消息item对象
     */
    private void initVerificationItem() {
        verificationMessageItemData = new ConversationFirendAuthenticationData(Conversation.TYPE_VERIFICATION_MESSAGE,
                Conversation.SPECIFIC_VERIFICATION_ID);
        verificationMessageItemData.setAddedItem(false);
        verificationMessageItemData.setIshouldAdd(false);
        verificationMessageItemData.setName(res.getString(R.string.group_create_group_qualification));
        GroupLayout verificationMessageItemLayout = new GroupLayout(mContext, verificationMessageItemData);
        verificationMessageItemLayout.update(verificationMessageItemData, false, false);
        verificationMessageItemData.setReadFlag(V2GlobalConstants.READ_STATE_READ);
        verificationItem = new ScrollItem(verificationMessageItemData, verificationMessageItemLayout, false);
    }

    /**
     * 更新通话消息item状态
     */
    private void updateVoiceSpecificItemState(VideoBean newestMediaMessage) {
        if (newestMediaMessage != null && newestMediaMessage.startDate != 0) {
            voiceMessageItem.setDate(new Date(newestMediaMessage.startDate));
            voiceLayout.updateVoiceItem(voiceMessageItem);
            if (newestMediaMessage.readSatate == V2GlobalConstants.READ_STATE_UNREAD) {
                sendVoiceNotify();
            }

            if (MediaRecordProvider.queryIsHaveUnreadMessage()) {
                updateUnreadConversation(voiceItem, V2GlobalConstants.READ_STATE_UNREAD);
            } else {
                updateUnreadConversation(voiceItem, V2GlobalConstants.READ_STATE_READ);
            }
        }
    }

    /**
     * 更验证消息item状态
     */
    private boolean updateVerificationConversation() {
        long crowdTime = 0;
        long friendTime = 0;
        VMessageQualification nestQualification = VerificationProvider.getNewestCrowdVerificationMessage();
        AddFriendHistorieNode friendNode = VerificationProvider.getNewestFriendVerificationMessage();

        if (nestQualification == null && friendNode == null) {
            removeConversation(verificationItem.cov.getExtId(), false);
            return false;
        }

        if (nestQualification != null) {
            crowdTime = nestQualification.getmTimestamp().getTime();
        }

        if (friendNode != null) {
            friendTime = friendNode.saveDate;
        }

        boolean isCrowd = false;
        if (crowdTime > friendTime) {
            updateCrowdVerificationConversation(false);
            isCrowd = true;
        } else
            updateFriendVerificationConversation(friendNode);
        return isCrowd;
    }

    /**
     * update verification conversation content
     */
    private void updateFriendVerificationConversation(AddFriendHistorieNode tempNode) {
        updateFriendVerificationConversation(true, true, null, tempNode);
    }

    /**
     * update verification conversation content
     *
     * @param isNotify       此标记是为了更改好友备注后，刷新消息界面时不会有声音
     * @param isNotifyBar    此标记是为了更改好友备注后，刷新消息界面时，如果在后台，不会更新通知栏
     * @param remoteUserName 为了处理组织外的用户
     */
    private void updateFriendVerificationConversation(boolean isNotify, boolean isNotifyBar, String remoteUserName,
                                                      AddFriendHistorieNode tempNode) {
        if (!isLoadedCov) {
            if (friendCache == null)
                friendCache = new FriendVerificationCache(isNotifyBar, isNotifyBar, remoteUserName, tempNode);
            else {
                friendCache.isNotify = isNotify;
                friendCache.isNotifyBar = isNotifyBar;
                friendCache.remoteUserName = remoteUserName;
                friendCache.tempNode = tempNode;
            }
            return;
        }

        if (tempNode == null) {
            V2Log.e(TAG, "update Friend verification conversation failed ... given AddFriendHistorieNode is null");
            return;
        }

        boolean hasUnread = false;
        if (tempNode.readState == ReadState.UNREAD.intValue())
            hasUnread = true;

        String name = null;
        if (!TextUtils.isEmpty(remoteUserName)) {
            if (!GlobalConfig.PROGRAM_IS_PAD)
                if (remoteUserName.length() > 3)
                    name = remoteUserName.subSequence(0, 3) + res.getString(R.string.conversation_ellipsis);
                else
                    name = remoteUserName;
        } else {
            User user = GlobalHolder.getInstance().getUser(tempNode.remoteUserID);
            if (!TextUtils.isEmpty(user.getDisplayName()))
                name = user.getDisplayName();
            else
                name = tempNode.remoteUserNickname;
            if (!GlobalConfig.PROGRAM_IS_PAD)
                if (name.length() > 3)
                    name = name.subSequence(0, 3) + res.getString(R.string.conversation_ellipsis);
        }
        String msg = buildFriendVerificationMsg(tempNode, name);
        if (verificationMessageItemData != null) {
            verificationMessageItemData.setMsg(msg);
            verificationMessageItemData.setDate(new Date(tempNode.saveDate));
        }

        addVerificationConversation(true, false);
        if (hasUnread) {
            if (((MainApplication) mContext.getApplicationContext()).isRunningBackgound() && isNotifyBar) {
                updateVerificationStateBar(msg, VerificationMessageType.CONTACT_TYPE, tempNode.remoteUserID);
            }
            updateUnreadConversation(verificationItem, V2GlobalConstants.READ_STATE_UNREAD);

            if (isNotify) {
                sendVoiceNotify();
            }
        } else {
            updateUnreadConversation(verificationItem, V2GlobalConstants.READ_STATE_READ);
        }
        sortAndUpdate();
    }

    /**
     * update verification conversation content
     *
     * @param isRepeate 是否需要过滤
     * @return 群组的id
     */
    private long updateCrowdVerificationConversation(boolean isRepeate) {
        if (!isLoadedCov) {
            CrowdVerificationCache = true;
            return -1;
        }

        VMessageQualification msg = updateCrowdVerificationContent(isRepeate);
        if (msg == null)
            return -1;
        addVerificationConversation(true, false);
        if (msg.getReadState() == VMessageQualification.ReadState.UNREAD) {
            updateUnreadConversation(verificationItem, V2GlobalConstants.READ_STATE_UNREAD);
        } else {
            updateUnreadConversation(verificationItem, V2GlobalConstants.READ_STATE_READ);
        }
        sortAndUpdate();
        return msg.getmCrowdGroup().getGroupID();
    }

    private VMessageQualification updateCrowdVerificationContent(boolean isRepeate) {
        VMessageQualification msg = VerificationProvider.getNewestCrowdVerificationMessage();
        if (msg == null) {
            V2Log.e(TAG, "update Crowd verification conversation failed ... given VMessageQualification is null");
            return null;
        }

        if (isRepeate) {
            if (lastVerificationMsg != null && lastVerificationMsg.getId() == msg.getId()
                    && lastVerificationMsg.getReadState() == msg.getReadState()) {
                return lastVerificationMsg;
            }
        }
        lastVerificationMsg = msg;
        V2Log.d(TAG, "updateCrowdVerificationConversation --> get newest msg cols id is : " + msg.getId());
        String content = "";
        switch (msg.getType()) {
            case CROWD_INVITATION:
                VMessageQualificationInvitationCrowd invitation = (VMessageQualificationInvitationCrowd) msg;
                String invitationName = null;
                User inviteUser = invitation.getInvitationUser();
                CrowdGroup crowdGroup = invitation.getCrowdGroup();
                if (inviteUser == null || crowdGroup == null)
                    content = null;
                else {
                    if (TextUtils.isEmpty(invitation.getInvitationUser().getDisplayName())) {
                        User user = GlobalHolder.getInstance().getUser(invitation.getInvitationUser().getmUserId());
                        if (user.isFromService()) {
                            invitationName = user.getDisplayName();
                        }
                    } else {
                        User user = invitation.getInvitationUser();
                        invitationName = user.getDisplayName();
                        if (!GlobalConfig.PROGRAM_IS_PAD)
                            if (invitationName.length() > 3)
                                invitationName = user.getDisplayName().subSequence(0, 3)
                                        + res.getString(R.string.conversation_ellipsis);
                            else if (invitationName.length() > 15)
                                invitationName = invitationName.subSequence(0, 15)
                                        + res.getString(R.string.conversation_ellipsis);
                    }

                    String inviteGroupName = crowdGroup.getName();
                    if (!GlobalConfig.PROGRAM_IS_PAD)
                        if (inviteGroupName.length() > 3)
                            inviteGroupName = crowdGroup.getName().subSequence(0, 3)
                                    + res.getString(R.string.conversation_ellipsis);
                        else if (inviteGroupName.length() > 15)
                            inviteGroupName = crowdGroup.getName().subSequence(0, 15)
                                    + res.getString(R.string.conversation_ellipsis);

                    if (invitation.getQualState() == QualificationState.BE_ACCEPTED) {
                        content = crowdGroup.getName() + res.getString(R.string.conversation_agree_with_your_application);
                    } else if ((invitation.getQualState() == QualificationState.BE_REJECT)
                            || (invitation.getQualState() == QualificationState.WAITING_FOR_APPLY)) {
                        content = crowdGroup.getName() + res.getString(R.string.conversation_deny_your_application);
                    } else {
                        content = invitationName
                                + String.format(res.getString(R.string.conversation_invite_to_join), inviteGroupName);
                    }
                }
                break;
            case CROWD_APPLICATION:
                VMessageQualificationApplicationCrowd apply = (VMessageQualificationApplicationCrowd) msg;
                String applyName = null;
                User applyUser = apply.getApplicant();
                CrowdGroup applyGroup = apply.getCrowdGroup();
                if (applyUser == null || applyGroup == null)
                    content = null;
                else {
                    if (TextUtils.isEmpty(applyUser.getDisplayName())) {
                        User user = GlobalHolder.getInstance().getUser(apply.getApplicant().getmUserId());
                        if (user.isFromService()) {
                            applyName = user.getDisplayName();
                        }
                    } else {
                        User user = apply.getApplicant();
                        applyName = user.getDisplayName();
                        if (!GlobalConfig.PROGRAM_IS_PAD)
                            if (applyName.length() > 3)
                                applyName = user.getDisplayName().subSequence(0, 3)
                                        + res.getString(R.string.conversation_ellipsis);
                            else if (applyName.length() > 15)
                                applyName = applyName.subSequence(0, 15) + res.getString(R.string.conversation_ellipsis);
                    }

                    String applyGroupName = applyGroup.getName();
                    if (!GlobalConfig.PROGRAM_IS_PAD)
                        if (applyGroupName.length() > 3)
                            applyGroupName = applyGroup.getName().subSequence(0, 3)
                                    + res.getString(R.string.conversation_ellipsis);
                        else if (applyGroupName.length() > 15)
                            applyGroupName = applyGroupName.subSequence(0, 15)
                                    + res.getString(R.string.conversation_ellipsis);

                    if (apply.getQualState() == QualificationState.BE_REJECT)
                        content = applyName
                                + String.format(res.getString(R.string.conversation_refused_to_join), applyGroupName);
                    else if (apply.getQualState() == QualificationState.BE_ACCEPTED)
                        content = applyName
                                + String.format(res.getString(R.string.conversation_agree_to_join), applyGroupName);
                    else
                        content = applyName
                                + String.format(res.getString(R.string.crowd_invitation_apply_join), applyGroupName);
                }
                break;
            default:
                break;
        }

        if (msg.getmTimestamp() == null)
            msg.setmTimestamp(new Date(GlobalConfig.getGlobalServerTime()));
        verificationMessageItemData.setMsg(content);
        verificationMessageItemData.setDate(msg.getmTimestamp());
        return msg;
    }

    /**
     * 构建好友验证信息显示的内容
     */
    private String buildFriendVerificationMsg(AddFriendHistorieNode tempNode, String name) {
        String content = null;
        // 别人加我：允许任何人：0已添加您为好友，需要验证：1未处理，2已同意，3已拒绝
        // 我加别人：允许认识人：4你们已成为了好友，需要验证：5等待对方验证，4被同意（你们已成为了好友），6拒绝了你为好友
        if ((tempNode.fromUserID == tempNode.remoteUserID) && (tempNode.ownerAuthType == 0)) {// 别人加我允许任何人
            content = String.format(res.getString(R.string.friend_has_added), name);
        } else if ((tempNode.fromUserID == tempNode.remoteUserID) && (tempNode.ownerAuthType == 1)) {// 别人加我不管我有没有处理
            if (tempNode.addState == 0) {
                content = String.format(res.getString(R.string.friend_apply_add_you_friend), name);
            } else if (tempNode.addState == 1) { // 我加别人已被同意或我加别人不需验证
                content = String.format(res.getString(R.string.friend_relation), name);
            } else if (tempNode.addState == 2) {
                content = String.format(res.getString(R.string.friend_was_owner_reject_apply), name);
            }
        } else if (tempNode.fromUserID == tempNode.ownerUserID) {// 我加别人等待验证
            if (tempNode.addState == 0) {
                content = String.format(res.getString(R.string.friend_apply_add_waiting_verify), name);
            } else if (tempNode.addState == 1) {
                content = String.format(res.getString(R.string.friend_relation), name);
            } else if (tempNode.addState == 2) {
                content = String.format(res.getString(R.string.friend_was_reject_apply), name);
            }
        }
        return content;
    }

    /**
     * update group type conversation according groupType and groupID
     */
    private void updateGroupConversation(int groupType, long groupID) {
        if (!isLoadedCov) {
            V2Log.e(TAG, "updateGroupConversation --> fill adapter isn't finish ");
            return;
        }
        VMessage vm = ChatMessageProvider.getNewestGroupMessage(groupType, groupID);
        if (vm == null) {
            V2Log.e(TAG, "updateGroupConversation --> update failed.. Didn't find message " + groupID);
            if (!GlobalHolder.getInstance().isOfflineLoaded() && offlineCov.get((int) groupID, -1) == -1)
                offlineCov.put((int) groupID, groupType);
            return;
        }

        ScrollItem currentItem = null;
        Conversation existedCov = null;
        boolean foundFlag = false;
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation cov = mItemList.get(i).cov;
            if (cov.getExtId() == groupID) {
                foundFlag = true;
                existedCov = cov;
                currentItem = mItemList.get(i);
                break;
            }
        }

        boolean isAdd = false;
        if (foundFlag) {
            Group group = GlobalHolder.getInstance().getGroupById(groupID);
            if (group != null)
                existedCov.setName(group.getName());
            existedCov.setDate(vm.getDate());
            CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
            existedCov.setMsg(newMessage);
        } else {
            ScrollItem newItem = makeNewGroupItem(vm, groupType, groupID);
            currentItem = newItem;
            mItemList.add(0, newItem);
            isAdd = true;
        }

        if (vm.getFromUser() == null) {
            V2Log.e(TAG,
                    "updateGroupConversation --> update group conversation state failed..."
                            + "becauser VMessage fromUser is null...please checked , group type is : " + groupType
                            + " groupID is :" + groupID);
            return;
        }

        long remoteID = vm.getFromUser().getmUserId();
        if (GlobalHolder.getInstance().getCurrentUserId() != remoteID) {
            if (!GlobalHolder.getInstance().isChatInterfaceOpen(groupID)) {
                updateUnreadConversation(currentItem, V2GlobalConstants.READ_STATE_UNREAD);
            }
            // Update status bar
            updateStatusBar(vm);
        } else {
            updateUnreadConversation(currentItem, V2GlobalConstants.READ_STATE_READ);
        }

        // Update view
        sortAndUpdate();
        if (isAdd)
            scrollToTop();
    }

    /**
     * Update conversation according to message id and remote user id, This
     * request call only from new message broadcast or request update
     * conversation broadcast
     */
    private void updateUserConversation(long remoteUserID, long msgId) {
        if (!isLoadedCov) {
            V2Log.e(TAG, "updateUserConversation --> fill adapter isn't finish !");
            return;
        }

        VMessage vm = ChatMessageProvider.loadUserMessageById(remoteUserID, msgId);
        if (vm == null) {
            V2Log.e(TAG, "updateUserConversation --> update failed.. Didn't find message " + remoteUserID);
            if (!GlobalHolder.getInstance().isOfflineLoaded() && offlineCov.get((int) remoteUserID, -1) == -1)
                offlineCov.put((int) remoteUserID, V2GlobalConstants.GROUP_TYPE_USER);
            return;
        }

        updateUserConversation(vm);
    }

    /**
     * Update conversation according to message id and remote user id, This
     * request call only from new message broadcast or request update
     * conversation broadcast
     */
    private void updateUserConversation(VMessage vm) {
        if (vm == null || vm.getFromUser() == null) {
            return;
        }

        long extId;
        if (vm.getFromUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId())
            extId = vm.getToUser().getmUserId();
        else
            extId = vm.getFromUser().getmUserId();

        ScrollItem currentItem = null;
        Conversation existedCov = null;
        boolean foundFlag = false;
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation cov = mItemList.get(i).cov;
            if (cov.getExtId() == extId) {
                foundFlag = true;
                existedCov = cov;
                currentItem = mItemList.get(i);
                break;
            }
        }

        boolean isAdd = false;
        /**
         * foundFlag : true 代表当前ListView中并未包含该Conversation，需要加入数据库。 false
         * 代表已存在，仅需要展示
         */
        if (foundFlag) {
            CharSequence mixedContent = MessageUtil.getMixedConversationContent(mContext, vm);
            existedCov.setMsg(mixedContent);
            existedCov.setDate(vm.getDate());
        } else {
            // 展示到界面
            existedCov = new ContactConversation(extId);
            existedCov.setMsg(MessageUtil.getMixedConversationContent(mContext, vm));
            existedCov.setDate(vm.getDate());
            ConversationProvider.saveConversation(vm);
            // 添加到ListView中
            GroupLayout viewLayout = new GroupLayout(mContext, existedCov);

            ScrollItem newItem = new ScrollItem(existedCov, viewLayout, false);
            currentItem = newItem;
            mItemList.add(0, newItem);
            isAdd = true;
        }

        long remoteID = vm.getFromUser().getmUserId();
        if (GlobalHolder.getInstance().getCurrentUserId() != remoteID) {
            if (!GlobalHolder.getInstance().isChatInterfaceOpen(remoteID)) {
                updateUnreadConversation(currentItem, V2GlobalConstants.READ_STATE_UNREAD);
            }
            // Update status bar
            updateStatusBar(vm);
        } else {
            updateUnreadConversation(currentItem, V2GlobalConstants.READ_STATE_READ);
        }

        // Update view
        sortAndUpdate();
        if (isAdd)
            scrollToTop();
    }

    /**
     * 添加验证消息item到界面
     */
    private void addVerificationConversation(boolean isFirstIndex, boolean isFromFillAdatper) {
        if (verificationMessageItemData.isAddedItem() && !isFromFillAdatper) {
            return;
        }

        boolean isAdd = true;
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).cov.getExtId() == verificationMessageItemData.getExtId()) {
                isAdd = false;
                break;
            }
        }

        if (isAdd) {
            VMessageQualification nestQualification = VerificationProvider.getNewestCrowdVerificationMessage();
            AddFriendHistorieNode friendNode = VerificationProvider.getNewestFriendVerificationMessage();
            if (nestQualification == null && friendNode == null) {
                return;
            }

            if (isFirstIndex)
                mItemList.add(0, verificationItem);
            else
                mItemList.add(verificationItem);
            verificationMessageItemData.setAddedItem(true);
            sortAndUpdate();
            scrollToTop();
        }
    }

    /**
     * 添加音视频记录item到界面
     *
     * @param isFirstIndex 是否添加到列表顶部
     */
    private void addVoiceConversation(boolean isFirstIndex) {
        if (voiceMessageItem.isAddedItem()) {
            // 如果为true，说明需要调整位置到列表的最上端。
            if (isFirstIndex) {
                mItemList.remove(voiceItem);
                voiceMessageItem.setAddedItem(false);
            } else {
                return;
            }
        }

        boolean isAdd = true;
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).cov.getExtId() == voiceMessageItem.getExtId()) {
                isAdd = false;
                break;
            }
        }

        if (isAdd) {
            boolean isHaveVoiceRecord = MediaRecordProvider.queryIsHaveMediaMessages();
            if (!isHaveVoiceRecord) {
                return;
            }

            if (isFirstIndex)
                mItemList.add(0, voiceItem);
            else
                mItemList.add(voiceItem);
            voiceMessageItem.setAddedItem(true);
            sortAndUpdate();
            scrollToTop();
        }
    }

    private void addConfConversation(ConferenceGroup newConference, User inviteUser, boolean isRead) {
        if (!isLoadedCov) {
            V2Log.e(TAG, "addConfConversation --> fill adapter isn't finish !");
            return;
        }

        ScrollItem confitem = null;
        boolean isAdd = true;
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).cov.getType() == Conversation.TYPE_CONFERNECE) {
                ConferenceConversation temp = (ConferenceConversation) mItemList.get(i).cov;
                if ((temp.getInviteUser() != null && temp.getInviteUser().getmUserId() == inviteUser.getmUserId())
                        && temp.getExtId() == newConference.getGroupID()) {
                    isAdd = false;
                    confitem = mItemList.get(i);
                    break;
                }
            }
        }

        if (isAdd) {
            ConferenceConversation confCov = new ConferenceConversation(newConference, true);
            confCov.setInviteUser(inviteUser);
            Date mInviteDate = new Date(GlobalConfig.getGlobalServerTime());
            confCov.setInviteDate(mInviteDate);
            GroupLayout confLayout = new GroupLayout(mContext, confCov);
            confitem = new ScrollItem(confCov, confLayout, false);
            mItemList.add(0, confitem);
            ConversationProvider.saveConfInviteConversation(newConference, inviteUser, mInviteDate,
                    V2GlobalConstants.READ_STATE_UNREAD);
        }

        if (isRead)
            updateUnreadConversation(confitem, V2GlobalConstants.READ_STATE_READ);
        else
            updateUnreadConversation(confitem, V2GlobalConstants.READ_STATE_UNREAD);
        sortAndUpdate();
        scrollToTop();
    }

    /**
     * Update main activity to show or hide notificator , and update
     * conversation read state in database
     */
    private void updateUnreadConversation(ScrollItem scrollItem, int readState) {
        Conversation cov = scrollItem.cov;
        updateUnreadConversation(cov, readState);
    }

    /**
     * Update main activity to show or hide notificator , and update
     * conversation read state in database
     */
    private void updateUnreadConversation(Conversation cov, int readState) {
        int ret;
        cov.setReadFlag(readState);
        if (readState == V2GlobalConstants.READ_STATE_UNREAD) {
            boolean flag = mUnreadConvList.add(cov);
            if (flag) {
                ret = V2GlobalConstants.READ_STATE_UNREAD;
            } else {
                return;
            }
        } else {
            boolean flag = mUnreadConvList.remove(cov);
            if (flag) {
                ret = V2GlobalConstants.READ_STATE_READ;
            } else {
                return;
            }
        }

        // 这里需要判断一下,如果界面还没收到服务器信息,列表在隐藏状态,就不能显示红点
        if (mConversationsListView.getVisibility() != View.VISIBLE) {
            isNeedToNotifyShowRead = true;
        } else {
            if (mUnreadConvList.size() > 0) {
                notificationListener.updateNotificator(Conversation.TYPE_CONTACT, true);
            } else {
                notificationListener.updateNotificator(Conversation.TYPE_CONTACT, false);
            }
        }

        // 修改数据库中数据读取状态，但音视频通讯记录不在此更新，在onitemclick
        ConversationProvider.updateConversationToDatabase(cov, ret);
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * update crowd group name in Message Interface
     */
    private void updateMessageGroupName() {
        service.execute(new Runnable() {

            @Override
            public void run() {
                while (!isLoadedCov) {
                    SystemClock.sleep(1000);
                    V2Log.w(TAG, "waiting for crowd fill adapter ......");
                }

                for (int i = 0; i < mItemList.size(); i++) {
                    ScrollItem item = mItemList.get(i);
                    V2Log.i(TAG, "current iterator conversation item type is : " + item.cov.getType());
                    if (item.cov.getType() == Conversation.TYPE_GROUP) {
                        CrowdConversation crowd = (CrowdConversation) item.cov;
                        Group newGroup = GlobalHolder.getInstance().getGroupById(crowd.getExtId());
                        if (newGroup != null) {
                            crowd.setGroup(newGroup);
                            updateUnreadConversation(crowd, V2GlobalConstants.READ_STATE_READ);
                            VMessage vm = ChatMessageProvider.getNewestGroupMessage(
                                    V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getExtId());
                            updateGroupInfo(crowd, vm);
                        }
                    }
                }
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < mItemList.size(); i++) {
                            V2Log.i(TAG, "Conversation Type : " + mItemList.get(i).cov.getType());
                        }
                        sortAndUpdate();
                    }
                });
            }
        });
    }

    private void updateGroupInfo(final Conversation con, final VMessage vm) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (vm != null) {
                    con.setDate(vm.getDate());
                    CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
                    con.setMsg(newMessage);
                } else
                    V2Log.w(TAG, "没有获取到最新VMessage对象! 更新内容失败");
                adapter.notifyDataSetChanged();
                V2Log.i(TAG, "UPDATE GROUP ITEM INFOS SUCCESSFULLY, " + "GROUP TYPE IS : " + con.getType()
                        + " NAME IS :" + con.getName());
            }
        });
    }

    /**
     * update department group name in Message Interface
     */
    private void updateDepartmentGroupName() {
        service.execute(new Runnable() {

            @Override
            public void run() {
                while (!isLoadedCov) {
                    SystemClock.sleep(1000);
                    V2Log.w(TAG, "waiting for department fill adapter ......");
                }

                for (int i = 0; i < mItemList.size(); i++) {
                    ScrollItem item = mItemList.get(i);
                    if (item.cov.getType() != Conversation.TYPE_DEPARTMENT)
                        continue;

                    final DepartmentConversation department = (DepartmentConversation) item.cov;
                    Group newGroup = GlobalHolder.getInstance().getGroupById(department.getExtId());
                    if (newGroup != null) {
                        department.setGroup(newGroup);
                        final VMessage vm = ChatMessageProvider.getNewestGroupMessage(
                                V2GlobalConstants.GROUP_TYPE_DEPARTMENT, department.getExtId());
                        department.setName(newGroup.getName());
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (vm != null) {
                                    department.setName(department.getName());
                                    department.setDate(vm.getDate());
                                    CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
                                    department.setMsg(newMessage);
                                }
                                adapter.notifyDataSetChanged();
                                V2Log.d(TAG, "Successfully updated the DEPARTMENT_GROUP infos , " + "group name is :"
                                        + department.getName());
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Remove conversation from mConvList by id.
     *
     * @param conversationID       <ul>
     *                             <li>ContactConversation : conversationID mean User's id
     *                             <li>ConferenceConversation : conversationID mean
     *                             ConferenceGroup's id
     *                             <li>CrowdConversation : conversationID mean CrowdGroup's id
     *                             <li>DepartmentConversation : conversationID mean OrgGroup's's
     *                             id
     *                             <li>DiscussionConversation : conversationID mean
     *                             DiscussionGroup's's id
     *                             </ul>
     * @param isDeleteVerification 在删除会话的时候，是否连带删除验证消息
     */
    private void removeConversation(long conversationID, boolean isDeleteVerification) {
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation temp = mItemList.get(i).cov;
            if (temp.getExtId() == conversationID) {
                ScrollItem removeItem = mItemList.get(i);
                // update readState
                updateUnreadConversation(removeItem, V2GlobalConstants.READ_STATE_READ);
                // remove item
                ScrollItem removed = mItemList.remove(i);
                if (removed == null)
                    V2Log.e(TAG, "Delete Conversation Failed...id is : " + conversationID);

                if (temp.getType() == Conversation.TYPE_VERIFICATION_MESSAGE) {
                    verificationMessageItemData.setAddedItem(false);
                    int friend = VerificationProvider.deleteFriendVerificationMessage(-1);
                    int group = VerificationProvider.deleteCrowdVerificationMessage(-1, -1);
                    if (friend + group > 0) {
                        V2Log.d(TAG, "Successfully delete verification , update conversaton!");
                    }
                } else if (temp.getType() == Conversation.TYPE_VOICE_MESSAGE) {
                    voiceMessageItem.setAddedItem(false);
                    MediaRecordProvider.deleteMediaMessage(-1);
                } else if (temp.getType() == Conversation.TYPE_CONFERNECE) {
                    // delete conversation
                    ConversationProvider.deleteConversation(mContext, temp);
                } else {
                    // delete conversation
                    ConversationProvider.deleteConversation(mContext, temp);
                    // delete messages
                    if (temp.getType() == Conversation.TYPE_CONTACT) {
                        ChatMessageProvider.deleteMessageByID(Conversation.TYPE_CONTACT, 0, temp.getExtId(), false);
                    } else {
                        // clear the crowd group all chat database messges
                        ChatMessageProvider.deleteMessageByID(temp.getType(), temp.getExtId(), 0, false);
                    }
                    V2Log.d(TAG, " Successfully remove contact conversation , id is : " + conversationID);
                }
                break;
            }
        }

        if (isDeleteVerification) {
            removeVerificationMessage(conversationID);
        }
        sortAndUpdate();
    }

    private void removeVerificationMessage(long id) {
        // clear the crowd group all verification database messges
        int friend = VerificationProvider.deleteFriendVerificationMessage(id);
        int group = VerificationProvider.deleteCrowdVerificationMessage(id, -1);
        if (friend + group > 0) {
            updateVerificationConversation();
            V2Log.e(TAG, "Successfully delete verification , update conversaton!");
        }

        // clear the voice messages
        int voices = MediaRecordProvider.deleteMediaMessage(id);
        boolean voiceflag = MediaRecordProvider.queryIsHaveMediaMessages(id);
        if (voices > 0 && !voiceflag) {
            mItemList.remove(voiceItem);
            voiceMessageItem.setAddedItem(false);
        }
    }

    /**
     * 更新通知栏的聊天消息
     *
     * @param vm 聊天消息对象
     */
    private void updateStatusBar(VMessage vm) {
        if (checkSendingState()) {
            return;
        }

        if (!((MainApplication) mContext.getApplicationContext()).isRunningBackgound()) {
            return;
        }

        String content;
        if (vm.getAudioItems().size() > 0) {
            content = mContext.getResources().getString(R.string.receive_voice_notification);
        } else if (vm.getImageItems().size() > 0) {
            content = mContext.getResources().getString(R.string.receive_image_notification);
        } else if (vm.getFileItems().size() > 0) {
            content = mContext.getResources().getString(R.string.receive_file_notification);
        } else {
            StringBuilder sb = new StringBuilder();
            for (VMessageAbstractItem item : vm.getItems()) {
                if (item.getType() == VMessageAbstractItem.ITEM_TYPE_TEXT) {
                    if (item.isNewLine() && sb.length() != 0) {
                        sb.append("\n");
                    }

                    VMessageTextItem textItem = (VMessageTextItem) item;
                    if (!TextUtils.isEmpty(textItem.getText()))
                        sb.append(textItem.getText());
                } else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FACE) {
                    sb.append(mContext.getResources().getString(R.string.receive_face_notification));
                }
            }
            content = sb.toString();
        }
        Intent resultIntent = new Intent();
        resultIntent.setAction(PublicIntent.START_CONVERSACTION_ACTIVITY);
        resultIntent.addCategory(PublicIntent.DEFAULT_CATEGORY);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (vm.getMsgCode()) {
            case V2GlobalConstants.GROUP_TYPE_USER:
                resultIntent.putExtra("obj",
                        new ConversationNotificationObject(Conversation.TYPE_CONTACT, vm.getFromUser().getmUserId()));
                break;
            case V2GlobalConstants.GROUP_TYPE_CROWD:
                resultIntent.putExtra("obj", new ConversationNotificationObject(Conversation.TYPE_GROUP, vm.getGroupId()));
                break;
            case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                resultIntent.putExtra("obj",
                        new ConversationNotificationObject(V2GlobalConstants.GROUP_TYPE_DEPARTMENT, vm.getGroupId()));
                break;
            case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                resultIntent.putExtra("obj",
                        new ConversationNotificationObject(V2GlobalConstants.GROUP_TYPE_DISCUSSION, vm.getGroupId()));
                break;
            default:
                break;
        }

        if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER) {
            Notificator.updateSystemNotification(mContext, vm.getFromUser().getDisplayName(), content, false,
                    resultIntent, V2GlobalConstants.MESSAGE_NOTIFICATION_ID, vm.getFromUser().getmUserId());
        } else {
            Group group = GlobalHolder.getInstance().getGroupById(vm.getGroupId());
            if (group == null) {
                V2Log.e(TAG, "Update ChatMessage Notificator failed ... get Group Object from GlobleHolder is null"
                        + "groupType is : " + vm.getMsgCode() + " groupID is : " + vm.getGroupId());
                return;
            }
            Notificator.updateSystemNotification(mContext, group.getName(), content, false, resultIntent,
                    V2GlobalConstants.MESSAGE_NOTIFICATION_ID, group.getGroupID());
        }
    }

    /**
     * 更新通知栏的验证消息
     *
     * @param msg      通知内容
     * @param type     好友验证或群验证
     * @param remoteID 验证消息携带的远端用户id或群组id
     */
    private void updateVerificationStateBar(String msg, VerificationMessageType type, long remoteID) {

        if (checkSendingState()) {
            return;
        }

        // 发通知
        Intent i = new Intent(getActivity(), MessageAuthenticationActivity.class);
        i = startAuthenticationActivity(i, type);
        Notificator.updateSystemNotification(mContext, res.getString(R.string.status_bar_notification), msg, false, i,
                V2GlobalConstants.MESSAGE_NOTIFICATION_ID, remoteID);
    }

    /**
     * 更新通知栏的会议消息
     *
     * @param conf
     */
    private void updateConferenceNotification(Conference conf) {
        if (checkSendingState()) {
            return;
        }

        if (!((MainApplication) mContext.getApplicationContext()).isRunningBackgound()) {
            return;
        }

        Intent enterConference = new Intent(mContext, MainActivity.class);
        User creator = GlobalHolder.getInstance().getUser(conf.getCreator());
        enterConference.putExtra("conf", conf);
        enterConference.putExtra("initFragment", 2);
        Notificator.updateSystemNotification(mContext, creator == null ? "" : creator.getDisplayName(),
                mContext.getString(R.string.conversation_attend_the_meeting) + " < " + conf.getName() + " >", false,
                enterConference, V2GlobalConstants.VIDEO_NOTIFICATION_ID, conf.getId());
    }

    /**
     * 创建一个新的群组类型的会话消息
     *
     * @param vm
     * @param groupType
     * @param groupID
     * @return
     */
    private ScrollItem makeNewGroupItem(VMessage vm, int groupType, long groupID) {

        Group group = GlobalHolder.getInstance().getGroupById(groupID);
        Conversation cov;
        switch (groupType) {
            case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                if (group == null) {
                    V2Log.e(TAG, "makeNewGroupItem ---> get department is null , id is :" + groupID);
                    group = new OrgGroup(groupID, null);
                }
                cov = new DepartmentConversation(group);
                ((DepartmentConversation) cov).setShowContact(true);
                break;
            case V2GlobalConstants.GROUP_TYPE_CROWD:
                if (group == null) {
                    V2Log.e(TAG, "makeNewGroupItem ---> get crowdGroup is null , id is :" + groupID);
                    group = new CrowdGroup(groupID, null, null);
                }
                cov = new CrowdConversation(group);
                ((CrowdConversation) cov).setShowContact(true);
                break;
            case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                if (group == null) {
                    V2Log.e(TAG, "makeNewGroupItem ---> get discussionGroup is null , id is :" + groupID);
                    group = new DiscussionGroup(groupID, null, null);
                }
                cov = new DiscussionConversation(group);
                ((DiscussionConversation) cov).setShowContact(true);
                break;
            default:
                throw new RuntimeException("makeNewGroupItem ---> invalid groupType : " + groupType);
        }

        cov.setDate(vm.getDate());
        CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
        cov.setMsg(newMessage);
        ConversationProvider.saveConversation(vm);
        GroupLayout viewLayout = new GroupLayout(mContext, cov);

        if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION)
            viewLayout.updateDiscussionLayout(true);
        else
            viewLayout.updateCrowdLayout();
        // 添加到ListView中
        V2Log.d(TAG, "makeNewGroupItem --> Successfully sendFriendToTv a new conversation , type is : " + cov.getType()
                + " and id is : " + cov.getExtId() + " and name is : " + cov.getName());

        return new ScrollItem(cov, viewLayout, false);
    }

    private boolean isOutOrgShow;

    /**
     * 当所有组织信息和组织内用户信息获取完毕后，检测当前验证消息显示的是否是组织外用户的群验证消息。
     */
    private void checkEmptyVerificationMessage() {
        VerificationMessageType messageType = isHaveVerificationMessage();
        if (messageType == VerificationMessageType.CROWD_TYPE) {
            VMessageQualification crowdVerificationMessage = VerificationProvider.getNewestCrowdVerificationMessage();
            if (crowdVerificationMessage != null) {
                long uid = -1;
                if (crowdVerificationMessage.getType() == Type.CROWD_APPLICATION) {
                    VMessageQualificationApplicationCrowd applyMsg = (VMessageQualificationApplicationCrowd) crowdVerificationMessage;
                    if (applyMsg.getApplicant() != null)
                        uid = applyMsg.getApplicant().getmUserId();
                } else {
                    VMessageQualificationInvitationCrowd inviteMsg = (VMessageQualificationInvitationCrowd) crowdVerificationMessage;
                    if (inviteMsg.getInvitationUser() != null)
                        uid = inviteMsg.getInvitationUser().getmUserId();
                }

                if (uid != -1) {
                    User remote = GlobalHolder.getInstance().getUser(uid);
                    if (!remote.isFromService()) {
                        // The user info need to get
                        isOutOrgShow = true;
                        V2Log.e(TAG, "The current show Verification info need to update!");
                    }
                }
            }
        }
    }

    /**
     * 登陆后检测数据库里是否存有等待好友验证的消息，并且已经与他成为好友
     */
    private void checkWaittingFriendExist() {
        List<Long> remoteUsers = VerificationProvider.getFriendWaittingVerifyMessage();
        if (remoteUsers != null && remoteUsers.size() > 0) {
            for (int i = 0; i < remoteUsers.size(); i++) {
                V2Log.d(TAG, "Waitting sendFriendToTv friend id is : " + remoteUsers.get(i));
                boolean isFinish = false;
                User user = GlobalHolder.getInstance().getUser(remoteUsers.get(i));
                if (!user.isFromService()) {
                    break;
                }

                Set<Group> belongsGroup = user.getBelongsGroup();
                Iterator<Group> iterator = belongsGroup.iterator();
                while (iterator.hasNext()) {
                    Group next = iterator.next();
                    if (next.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                        isFinish = true;
                        break;
                    }
                }

                if (isFinish) {
                    V2Log.d(TAG, "发现有等待的好友验证的消息已变为成为好友，用户id : " + user.getmUserId());
                    AddFriendHistroysHandler.becomeFriendHanler(mContext, user);
                    int update = VerificationProvider.updateFriendQualicationReadState(user.getmUserId(),
                            ReadState.UNREAD);
                    if (update <= 0) {
                        V2Log.e(TAG, "更新等待的好友验证失败！");
                    }
                }
            }
        }
    }

    /**
     * 当所有信息都已接收完毕，检测消息界面中存在的讨论组和群会话，它们是否存在，如不删除点击就会报错
     */
    private void checkGroupIsExist() {
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation cov = mItemList.get(i).cov;
            if (cov.getType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
                Group crowd = GlobalHolder.getInstance().getGroupById(cov.getExtId());
                if (crowd == null)
                    Message.obtain(mHandler, REMOVE_CONVERSATION, cov.getExtId()).sendToTarget();
            } else if (cov.getType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                Group crowd = GlobalHolder.getInstance().getGroupById(cov.getExtId());
                if (crowd == null)
                    Message.obtain(mHandler, REMOVE_CONVERSATION, cov.getExtId()).sendToTarget();
            }
        }

        List<VMessageQualification> crowdQualMsgs = VerificationProvider
                .queryCrowdQualMessageList(GlobalHolder.getInstance().getCurrentUser());
        if (crowdQualMsgs != null) {
            for (int i = 0; i < crowdQualMsgs.size(); i++) {
                VMessageQualification vMessageQualification = crowdQualMsgs.get(i);
                if (vMessageQualification.getQualState() == QualificationState.ACCEPTED) {
                    V2Log.d("test", "checkGroupIsExist .........");
                    CrowdGroup mCrowdGroup = vMessageQualification.getmCrowdGroup();
                    if (mCrowdGroup != null) {
                        Group temp = GlobalHolder.getInstance().findGroupById(mCrowdGroup.getGroupID());
                        if (temp == null) {
                            VerificationProvider.deleteCrowdQualMessage(vMessageQualification.getId());
                        }
                    }
                }
            }
            updateVerificationConversation();
        }
    }

    /**
     * The verification conversation may be repeated when justk user login , So
     * you need to check it and find out why
     */
    private void checkRepeatVerification() {
        service.execute(new Runnable() {

            @Override
            public void run() {
                while (!isLoadedCov) {
                    SystemClock.sleep(1000);
                    V2Log.w(TAG, "checkRepeatVerification --> waiting for message interface fill adapter ......");
                }

                boolean isFound = false;
                for (int j = 0; j < mItemList.size(); j++) {
                    Conversation cov = mItemList.get(j).cov;
                    if (cov.getExtId() == verificationMessageItemData.getExtId()) {
                        if (!isFound) {
                            isFound = true;
                        } else {
                            V2Log.i(TAG, "Rmove repeate verification conversation successfully!");
                            mItemList.remove(j);
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    sortAndUpdate();
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * 检测当前程序状态,确定是否应该发送通知栏消息
     *
     * @return true mean don't sending , false sending
     */
    private boolean checkSendingState() {
        return (GlobalHolder.getInstance().isInMeeting() || GlobalHolder.getInstance().isInAudioCall()
                || GlobalHolder.getInstance().isInVideoCall());
    }

    /**
     * 在填充listView的时候,如果来新的会议邀请消息,需要等待填充完列表再添加,防止重复
     */
    private void checkLeaveOutConference() {
        if (mLeaveOutConferences.size() >= 0) {
            for (int i = 0; i < mLeaveOutConferences.size(); i++) {
                int confGroupID = mLeaveOutConferences.keyAt(i);
                int inviteUserID = mLeaveOutConferences.valueAt(i);
                Group leaveConf = GlobalHolder.getInstance().getGroupById(confGroupID);
                User mInviteUser = GlobalHolder.getInstance().getUser(inviteUserID);
                V2Log.e(TAG, "pick up a leaved conf , name is : " + leaveConf.getName());
                addConfConversation((ConferenceGroup) leaveConf, mInviteUser, false);
            }
        }
    }

    public void sendVoiceNotify() {
        if (mChatPlayer == null)
            mChatPlayer = MediaPlayer.create(mContext, R.raw.chat_audio);
        if (!mChatPlayer.isPlaying())
            mChatPlayer.start();
    }

    private Intent startAuthenticationActivity(Intent intent, VerificationMessageType messageType) {

        if (currentMessageType != null) {
            messageType = currentMessageType;
        }
        if (messageType == VerificationMessageType.CONTACT_TYPE)
            intent.putExtra("isFriendActivity", true);
        else
            intent.putExtra("isFriendActivity", false);
        currentMessageType = null;
        return intent;
    }

    private void scrollToTop() {
        if (mConversationsListView == null || mItemList.size() < 0)
            return;

        mConversationsListView.post(new Runnable() {

            @Override
            public void run() {
                mConversationsListView.setSelection(0);
            }
        });
    }

    class FriendVerificationCache {

        public boolean isNotify;
        public boolean isNotifyBar;
        public String remoteUserName;
        public AddFriendHistorieNode tempNode;

        public FriendVerificationCache(boolean isNotify, boolean isNotifyBar, String remoteUserName,
                                       AddFriendHistorieNode tempNode) {
            super();
            this.isNotify = isNotify;
            this.isNotifyBar = isNotifyBar;
            this.remoteUserName = remoteUserName;
            this.tempNode = tempNode;
        }

    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapters, View v, int pos, long id) {

            if (AlgorithmUtil.isFastClick()) {
                return;
            }

            currentClickConversation = mItemList.get(pos).cov;
            ScrollItem item;
            if (mIsStartedSearch)
                item = searchList.get(pos);
            else
                item = mItemList.get(pos);
            Conversation cov = item.cov;
            if (cov.getType() == Conversation.TYPE_VOICE_MESSAGE) {
                Intent intent = new Intent(mContext, VoiceMessageActivity.class);
                startActivityForResult(intent, ACTIVITY_RETURN_VOICE_RECORD);
            } else if (cov.getType() == Conversation.TYPE_VERIFICATION_MESSAGE) {
                Intent intent = new Intent(mContext, MessageAuthenticationActivity.class);
                VerificationMessageType messageType = isHaveVerificationMessage();
                intent = startAuthenticationActivity(intent, messageType);
                startActivity(intent);
            } else if (cov.getType() == Conversation.TYPE_CONFERNECE) {
                ConferenceConversation confCov = (ConferenceConversation) cov;
                ((MainActivity) mContext).requestEnterConf(new Conference((ConferenceGroup) confCov.getGroup()));
            } else {
                Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
                i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("obj", new ConversationNotificationObject(cov.getType(), cov.getExtId()));
                startActivityForResult(i, ACTIVITY_RETURN_UPDATE_CHAT_CONVERSATION);
            }

            // update main activity notificator
            updateUnreadConversation(item, V2GlobalConstants.READ_STATE_READ);
        }
    };

    private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapter, View v, int pos, long id) {
            String[] item;
            currentClickConversation = mItemList.get(pos).cov;
            // Conversation.TYPE_CONTACT
            item = new String[]{mContext.getResources().getString(R.string.conversations_delete_conversaion)};

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mItemList.get(pos).cov.getName()).setItems(item, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        removeConversation(currentClickConversation.getExtId(), false);
                    }
                    dialog.dismiss();
                }
            });
            AlertDialog ad = builder.create();
            ad.show();
            return true;
        }
    };

    private BitmapManager.BitmapChangedListener bitmapChangedListener = new BitmapManager.BitmapChangedListener() {

        @Override
        public void notifyAvatarChanged(User user, Bitmap bm) {
            for (ScrollItem item : mItemList) {
                if (Conversation.TYPE_CONTACT == item.cov.getType() && item.cov.getExtId() == user.getmUserId()) {
                    ((GroupLayout) item.gp).updateIcon(bm);
                }
            }

        }
    };

    class ConversationsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mIsStartedSearch)
                return searchList == null ? 0 : searchList.size();
            else
                return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            if (mIsStartedSearch)
                return searchList.get(position);
            else {
                if (mItemList.size() <= 0)
                    return null;
                return mItemList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            if (mIsStartedSearch)
                return searchList.get(position).cov.getExtId();
            else {
                if (mItemList.size() <= 0)
                    return 0;
                return mItemList.get(position).cov.getExtId();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mIsStartedSearch) {
                return searchList.get(position).gp;
            } else {
                // 用ArrayList集合具有一定的线程安全问题
                if (position >= mItemList.size())
                    return convertView;

                ScrollItem scrollItem = mItemList.get(position);
                GroupLayout groupLayout = (GroupLayout) scrollItem.gp;
                Conversation cov = scrollItem.cov;
                if (position == mItemList.size() - 1) {
                    groupLayout.update(cov, false, false);
                } else {
                    groupLayout.update(cov, false, true);
                }
                return groupLayout;
            }
        }

    }

    class CommonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (JNIService.JNI_BROADCAST_GROUP_NOTIFICATION.equals(intent.getAction())) {
                int type = intent.getExtras().getInt("gtype");
                if (((type == V2GlobalConstants.GROUP_TYPE_CROWD)) && !isUpdateGroup) {
                    isUpdateGroup = true;
                    updateMessageGroupName();
                } else if (type == V2GlobalConstants.GROUP_TYPE_DEPARTMENT && !isUpdateDeparment) {
                    isUpdateDeparment = true;
                    updateDepartmentGroupName();
                }
                // From this broadcast, user has already read conversation
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {
                int gType = intent.getIntExtra("gtype", -1);
                if (gType != V2GlobalConstants.GROUP_TYPE_DEPARTMENT)
                    return;
                for (int i = 0; i < mItemList.size(); i++) {
                    ScrollItem item = mItemList.get(i);
                    Conversation currentConversation = item.cov;
                    V2Log.d(TAG, "current iterator conversation id is : " + "" + currentConversation.getExtId()
                            + " | type is : " + currentConversation.getType());
                    switch (currentConversation.getType()) {
                        case Conversation.TYPE_VERIFICATION_MESSAGE:
                            VerificationMessageType type = isHaveVerificationMessage();
                            Message obtain = Message.obtain(mHandler, UPDATE_VERIFICATION_MESSAGE);
                            obtain.arg1 = type.intValue();
                            obtain.sendToTarget();
                            break;
                        case Conversation.TYPE_DEPARTMENT:
                            Group depart = GlobalHolder.getInstance().getGroupById(currentConversation.getExtId());
                            ((DepartmentConversation) currentConversation).setGroup(depart);

                            V2Log.d(TAG, "update department group successful , id is : " + currentConversation.getExtId()
                                    + " name is : " + currentConversation.getName());
                            break;
                        case Conversation.TYPE_DISCUSSION:
                            DiscussionConversation discussion = (DiscussionConversation) currentConversation;
                            Group newGroup = GlobalHolder.getInstance().getGroupById(discussion.getExtId());
                            if (newGroup != null) {
                                discussion.setGroup(newGroup);
                                VMessage vm = ChatMessageProvider.getNewestGroupMessage(
                                        V2GlobalConstants.GROUP_TYPE_DISCUSSION, discussion.getExtId());
                                if (vm != null) {
                                    discussion.setDate(vm.getDate());
                                    CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
                                    discussion.setMsg(newMessage);
                                } else
                                    V2Log.w(TAG, "没有获取到最新VMessage对象! 更新内容失败");
                                V2Log.i(TAG, "update discussion group successful , id is : "
                                        + currentConversation.getExtId() + " name is : " + currentConversation.getName());
                            } else {
                                removeConversation(discussion.getExtId(), false);
                                V2Log.w(TAG, "没有获取到讨论组对象! 更新失败！ id is : " + discussion.getExtId());
                            }
                            break;
                        case Conversation.TYPE_GROUP:
                            Group crowd = GlobalHolder.getInstance().getGroupById(currentConversation.getExtId());
                            ((CrowdConversation) currentConversation).setGroup(crowd);
                            V2Log.d(TAG, "update crowd group successful , id is : " + currentConversation.getExtId()
                                    + " name is : " + currentConversation.getName());
                            break;
                        case Conversation.TYPE_CONFERNECE:
                            ConferenceConversation confCov = (ConferenceConversation) currentConversation;
                            long mInviteID = confCov.getInviteUser().getmUserId();
                            User mInviteUser = GlobalHolder.getInstance().getUser(mInviteID);
                            ConferenceGroup conf = (ConferenceGroup) GlobalHolder.getInstance()
                                    .getGroupById(currentConversation.getExtId());
                            if(conf == null){
                                removeConversation(confCov.getExtId(), false);
                            } else {
                                confCov.setInviteUser(mInviteUser);
                                confCov.setGroup(conf);
                                V2Log.d(TAG, "update conference group successful , id is : " + currentConversation.getExtId()
                                        + " name is : " + currentConversation.getName());
                            }
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            } else if (JNIService.JNI_BROADCAST_OFFLINE_MESSAGE_END.equals(intent.getAction())) {
                GlobalHolder.getInstance().setOfflineLoaded(true);
                V2Log.d(TAG, "All offline messages has received. Globle flag change to true!");
                for (int i = 0; i < offlineCov.size(); i++) {
                    long key = offlineCov.keyAt(i);
                    V2Log.i(TAG, "off line conversaion id is : " + key);
                    int value = offlineCov.valueAt(i);
                    if (value == V2GlobalConstants.GROUP_TYPE_USER) {
                        VMessage vm = ChatMessageProvider.getNewestMessage(
                                GlobalHolder.getInstance().getCurrentUserId(), key);
                        updateUserConversation(vm);
                    } else {
                        updateGroupConversation(value, key);
                    }
                }

                lastVerificationMsg = null;
            } else if (JNIService.JNI_BROADCAST_GROUPS_LOADED.equals(intent.getAction())) {
                GlobalHolder.getInstance().setGroupLoaded();
                V2Log.d(TAG, "All group and group user info has received. Globle flag change to true!");
                checkWaittingFriendExist();
                checkGroupIsExist();
                checkEmptyVerificationMessage();
                checkRepeatVerification();
                mConversationsListView.setVisibility(View.VISIBLE);
                if (isNeedToNotifyShowRead && mUnreadConvList.size() > 0) {
                    notificationListener.updateNotificator(Conversation.TYPE_CONTACT, true);
                    isNeedToNotifyShowRead = false;
                }
            }
        }
    }

    class ConversationReceiver extends CommonReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            String action = intent.getAction();
            if (action.equals(JNIService.JNI_BROADCAST_NEW_MESSAGE)) {
                sendVoiceNotify();
                MessageObject msgObj = intent.getParcelableExtra("msgObj");
                Message.obtain(mHandler, NEW_MESSAGE_UPDATE, msgObj).sendToTarget();
            } else if (action.equals(JNIService.JNI_BROADCAST_CONTACTS_AUTHENTICATION)) {
                long uid = intent.getLongExtra("uid", -1);
                if (uid == -1)
                    return;

                AddFriendHistorieNode node = VerificationProvider.getNewestFriendVerificationMessage();
                if (node == null) {
                    V2Log.d(TAG, "update friend verification message content failed... get null");
                    return;
                }

                V2Log.d(TAG, "having new friend verification message coming ... update..");

                boolean isOutORG = intent.getBooleanExtra("isOutORG", false);
                if (isOutORG) {
                    BoUserInfoBase v2User = intent.getParcelableExtra("v2User");
                    updateFriendVerificationConversation(true, true, v2User.getNickName(), node);
                } else
                    updateFriendVerificationConversation(node);
            } else if (JNIService.JNI_BROADCAST_NEW_QUALIFICATION_MESSAGE.equals(intent.getAction())) {
                long msgId = intent.getLongExtra("msgId", 0);
                boolean isNotifyVoice = intent.getBooleanExtra("isNotifyVoice", true);
                if (msgId == 0l) {
                    V2Log.d(TAG, "update crowd verification message content failed... get 0 message id");
                    return;
                }

                V2Log.d(TAG, "having new crowd verification message coming ... update..");
                long remoteID = updateCrowdVerificationConversation(true);

                if (isNotifyVoice)
                    sendVoiceNotify();
                if (((MainApplication) mContext.getApplicationContext()).isRunningBackgound()) {
                    updateVerificationStateBar(verificationMessageItemData.getMsg().toString(),
                            VerificationMessageType.CROWD_TYPE, remoteID);

                }
            } else if (PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION.equals(intent.getAction())
                    || intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
                GroupUserObject obj = intent.getParcelableExtra("group");
                if (obj == null) {
                    V2Log.e(TAG, "Received the broadcast to quit the crowd group , but crowd id is wroing... ");
                    return;
                }

                GlobalHolder.getInstance().removeGroup(obj.getmType(), obj.getmGroupId());
                removeConversation(obj.getmGroupId(), true);
            } else if ((JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION.equals(intent.getAction()))) {
                long groupID = intent.getLongExtra("groupID", -1l);
                if (groupID == -1l) {
                    V2Log.e(TAG, "May receive new group upload files failed.. get empty collection");
                    return;
                }

                long uploader = intent.getLongExtra("uploader", -1);
                if (uploader != GlobalHolder.getInstance().mCurrentUserId)
                    sendVoiceNotify();
                updateGroupConversation(V2GlobalConstants.GROUP_TYPE_CROWD, groupID);
            } else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
                Long uid = intent.getLongExtra("modifiedUser", -1);
                String fromPlace = intent.getStringExtra("fromPlace");
                if (uid == -1l) {
                    V2Log.e(TAG,
                            "BROADCAST_USER_COMMENT_NAME_NOTIFICATION ---> update user comment name failed , get id is -1");
                    return;
                }

                long crowdTime = 0;
                long friendTime = 0;
                VMessageQualification nestQualification = VerificationProvider.getNewestCrowdVerificationMessage();
                AddFriendHistorieNode friendNode = VerificationProvider.getNewestFriendVerificationMessage();

                if (nestQualification == null && friendNode == null) {
                    removeConversation(verificationItem.cov.getExtId(), false);
                    return;
                }

                if (nestQualification != null) {
                    crowdTime = nestQualification.getmTimestamp().getTime();
                }

                if (friendNode != null) {
                    friendTime = friendNode.saveDate;
                }

                if (crowdTime > friendTime) {
                    updateCrowdVerificationConversation(false);
                } else {
                    if (TextUtils.isEmpty(fromPlace))
                        updateFriendVerificationConversation(false, true, null, friendNode);
                    else
                        updateFriendVerificationConversation(false, false, null, friendNode);
                }
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_ADDED.equals(intent.getAction())) {
                GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
                if (guo == null) {
                    V2Log.e(TAG,
                            "JNI_BROADCAST_GROUP_USER_ADDED --> Received the broadcast to quit the crowd group , but crowd id is wroing... ");
                    return;
                }

                for (ScrollItem item : mItemList) {
                    Conversation con = item.cov;
                    if (con.getExtId() == guo.getmUserId()) {
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
                GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
                if (guo == null) {
                    V2Log.e(TAG,
                            "JNI_BROADCAST_GROUP_USER_REMOVED --> Update Conversation failed that the user removed ... given GroupUserObject is null");
                    return;
                }

                if (guo.getmType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
                    VerificationProvider.deleteCrowdVerificationMessage(guo.getmGroupId(), guo.getmUserId());
                    // clear the voice messages
                    int voices = MediaRecordProvider.deleteMediaMessage(guo.getmUserId());
                    boolean voiceflag = MediaRecordProvider.queryIsHaveMediaMessages(guo.getmUserId());
                    if (voices > 0 && !voiceflag) {
                        mItemList.remove(voiceItem);
                        voiceMessageItem.setAddedItem(false);
                    }
                    updateVerificationConversation();
                    removeConversation(guo.getmUserId(), false);
                } else if (guo.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                    Group rootOrg = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DEPARTMENT).get(0);
                    if (rootOrg.findUser(guo.getmUserId()) == null) {
                        V2Log.d(TAG, "成功删除一位好友, 并且是组织外的 ");
                        removeConversation(guo.getmUserId(), false);
                    }
                    VerificationProvider.deleteFriendVerificationMessage(guo.getmUserId());
                    updateVerificationConversation();
                } else if (guo.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    VerificationProvider.deleteFriendVerificationMessage(guo.getmUserId());
                    VerificationProvider.deleteCrowdQualMessageByRemoteUser(guo.getmUserId(), true);
                    // clear the voice messages
                    int voices = MediaRecordProvider.deleteMediaMessage(guo.getmUserId());
                    boolean voiceflag = MediaRecordProvider.queryIsHaveMediaMessages(guo.getmUserId());
                    if (voices > 0 && !voiceflag) {
                        mItemList.remove(voiceItem);
                        voiceMessageItem.setAddedItem(false);
                    }

                    List<ScrollItem> removed = new ArrayList<>();
                    for (int i = 0; i < mItemList.size(); i++) {
                        ScrollItem scrollItem = mItemList.get(i);
                        Conversation cov = scrollItem.cov;
                        if (cov.getType() == V2GlobalConstants.GROUP_TYPE_CROWD) {
                            CrowdConversation crowd = (CrowdConversation) cov;
                            if (crowd.getGroup().getOwnerUser().getmUserId() == guo.getmUserId()) {
                                removed.add(scrollItem);
                            }
                        } else if (cov.getType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                            DiscussionConversation dis = (DiscussionConversation) cov;
                            if (dis.getGroup().getOwnerUser().getmUserId() == guo.getmUserId()) {
                                removed.add(scrollItem);
                            }
                        } else if (cov.getType() == V2GlobalConstants.GROUP_TYPE_USER) {
                            if (cov.getExtId() == guo.getmUserId()) {
                                removed.add(scrollItem);
                            }
                        } else if (cov.getType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
                            ConferenceConversation conf = (ConferenceConversation) cov;
                            if (conf.getInviteUser().getmUserId() == guo.getmUserId()) {
                                removed.add(scrollItem);
                            }
                        }
                    }

                    for (int i = 0; i < removed.size(); i++) {
                        mItemList.remove(removed.get(i));
                        ConversationProvider.deleteConversation(mContext, removed.get(i).cov);
                        updateUnreadConversation(removed.get(i), V2GlobalConstants.READ_STATE_READ);
                    }
                    updateVerificationConversation();
                }
            } else if (PublicIntent.BROADCAST_ADD_OTHER_FRIEND_WAITING_NOTIFICATION.equals(intent.getAction())) {
                addVerificationConversation(true, false);
                Message msg = Message.obtain(mHandler, UPDATE_VERIFICATION_MESSAGE);
                msg.arg1 = VerificationMessageType.CONTACT_TYPE.intValue();
                msg.sendToTarget();
            } else if (JNIService.JNI_BROADCAST_GROUP_UPDATED.equals(intent.getAction())) {
                long gid = intent.getLongExtra("gid", 0);
                Group g = GlobalHolder.getInstance().getGroupById(gid);
                if (g == null) {
                    V2Log.e(TAG, "Update Group Infos Failed... Because get null goup , id is : " + gid);
                    return;
                }

                if (g.getGroupType() == V2GlobalConstants.GROUP_TYPE_CROWD
                        || g.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                    for (int i = 0; i < mItemList.size(); i++) {
                        ScrollItem item = mItemList.get(i);
                        if (item.cov.getExtId() == g.getGroupID()) {
                            updateVerificationConversation();
                            break;
                        }
                    }
                }
            } else if (PublicIntent.REQUEST_UPDATE_CONVERSATION.equals(intent.getAction())) {
                // 来自验证界面
                boolean isAuthen = intent.getBooleanExtra("isAuthen", false);
                if (isAuthen) {
                    updateVerificationConversation();
                    return;
                }

                ConversationNotificationObject uao = (ConversationNotificationObject) intent.getExtras().get("obj");
                if (uao == null) {
                    return;
                }

                boolean fromCrowdTab = intent.getBooleanExtra("fromCrowdTab", false);
                if (fromCrowdTab) {
                    for (ScrollItem scrollItem : mItemList) {
                        Conversation cov = scrollItem.cov;
                        if (uao.getConversationType() == cov.getType() && uao.getExtId() == cov.getExtId()) {
                            VMessage vm = ChatMessageProvider.getNewestGroupMessage(cov.getType(),
                                    cov.getExtId());
                            if (vm != null) {
                                cov.setDate(vm.getDate());
                                CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
                                cov.setMsg(newMessage);
                                ConversationProvider.updateConversationToDatabase(cov,
                                        V2GlobalConstants.READ_STATE_READ);
                                updateUnreadConversation(scrollItem, V2GlobalConstants.READ_STATE_READ);
                            }
                            break;
                        }
                    }
                    return;
                }

                // delete Empty message conversation
                if (uao.isDeleteConversation()) {
                    removeConversation(uao.getExtId(), false);
                    return;
                }

                Message.obtain(mHandler, UPDATE_CONVERSATION, uao).sendToTarget();
            } else if (ConversationP2PAVActivity.P2P_BROADCAST_MEDIA_UPDATE.equals(intent.getAction())) {
                long remoteID = intent.getLongExtra("remoteID", -1l);
                if (remoteID == -1l) {
                    Log.e(TAG, "get remoteID is -1 ... update failed!!");
                    return;
                }

                String selections = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + "= ? ";
                String[] selectionArgs = new String[]{String.valueOf(remoteID)};
                VideoBean newestMediaMessage = MediaRecordProvider.getNewestMediaMessage(selections, selectionArgs);
                if (newestMediaMessage == null) {
                    Log.e(TAG, "get newest remoteID " + remoteID + " --> VideoBean is NULL ... update failed!!");
                    return;
                }
                updateVoiceSpecificItemState(newestMediaMessage);
                addVoiceConversation(true);
            } else if (JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO.equals(intent.getAction())) {
                if (isOutOrgShow) {
                    isOutOrgShow = false;
                    updateCrowdVerificationConversation(true);
                }
            } else if (PublicIntent.BROADCAST_AUTHENTIC_TO_CONVERSATIONS_TAB_FRAGMENT_NOTIFICATION
                    .equals(intent.getAction())) {
                int tabType = intent.getIntExtra("tabType", -1);
                boolean isOtherShowPrompt = intent.getBooleanExtra("isOtherShowPrompt", false);
                if (tabType == MessageAuthenticationActivity.PROMPT_TYPE_FRIEND) {
                    VerificationProvider.updateCrowdAllQualicationMessageReadStateToRead(false);
                    // if (isOtherShowPrompt) {
                    // VerificationProvider
                    // .updateCrowdAllQualicationMessageReadStateToRead(true);
                    // }
                } else {
                    VerificationProvider.updateCrowdAllQualicationMessageReadStateToRead(true);
                    // if (isOtherShowPrompt) {
                    // VerificationProvider
                    // .updateCrowdAllQualicationMessageReadStateToRead(false);
                    // }
                }
                // 更新验证会话最新内容
                updateVerificationConversation();
                // 更新红点
                if (isOtherShowPrompt) {
                    // 此段代码可以实现，如果另一方有未读的消息，即使时间晚，但也可以显示出来，点击时能跳到相应的那一方
                    // if (tabType ==
                    // MessageAuthenticationActivity.PROMPT_TYPE_FRIEND) {
                    // updateCrowdVerificationConversation(false);
                    // currentMessageType = VerificationMessageType.CROWD_TYPE;
                    // } else {
                    // AddFriendHistorieNode friendNode = VerificationProvider
                    // .getNewestFriendVerificationMessage();
                    // updateFriendVerificationConversation(friendNode);
                    // currentMessageType =
                    // VerificationMessageType.CONTACT_TYPE;
                    // }
                    updateUnreadConversation(verificationItem, V2GlobalConstants.READ_STATE_UNREAD);
                } else {
                    currentMessageType = null;
                    updateUnreadConversation(verificationItem, V2GlobalConstants.READ_STATE_READ);
                }
            } else if (PublicIntent.CHAT_SYNC_MESSAGE_INTERFACE.equals(intent.getAction())) {
                int groupType = intent.getIntExtra("groupType", -1);
                long groupID = intent.getLongExtra("groupID", -1);
                long remoteUserID = intent.getLongExtra("remoteUserID", -1);

                long conversationID;
                if (V2GlobalConstants.GROUP_TYPE_USER == groupType) {
                    conversationID = remoteUserID;
                } else {
                    conversationID = groupID;
                }

                boolean isDelete = intent.getBooleanExtra("isDelete", false);
                if (isDelete) {
                    removeConversation(conversationID, false);
                    return;
                } else {
                    updateConversationToCreate(groupType, groupID, remoteUserID);
                }

                VMessage vm;
                for (int i = 0; i < mItemList.size(); i++) {
                    Conversation cov = mItemList.get(i).cov;
                    if (cov.getExtId() == conversationID) {
                        if (V2GlobalConstants.GROUP_TYPE_USER == groupType) {
                            vm = ChatMessageProvider.getNewestMessage(
                                    GlobalHolder.getInstance().getCurrentUserId(), remoteUserID);
                        } else {
                            vm = ChatMessageProvider.getNewestGroupMessage(groupType, groupID);
                        }

                        if (vm != null) {
                            cov.setDate(vm.getDate());
                            CharSequence msg = MessageUtil.getMixedConversationContent(mContext, vm);
                            cov.setMsg(msg);
                        }

                        updateUnreadConversation(mItemList.get(i), V2GlobalConstants.READ_STATE_READ);
                        sortAndUpdate();
                    }
                }
            } else if (JNIService.JNI_BROADCAST_CONFERENCE_INVATITION.equals(intent.getAction())) {
                sendVoiceNotify();
                long gid = intent.getLongExtra("gid", 0);
                long inviteUserID = intent.getLongExtra("invite", -1);
                User mInviteUser = GlobalHolder.getInstance().getUser(inviteUserID);
                ConferenceGroup invateConf = (ConferenceGroup) GlobalHolder.getInstance().getGroupById(gid);
                V2Log.d(TAG, "JNI_BROADCAST_CONFERENCE_INVATITION isLoading : " + isLoading + " inviteUserID : "
                        + inviteUserID);
                if (invateConf != null) {
                    if (isLoading) {
                        mLeaveOutConferences.put((int) gid, (int) inviteUserID);
                    } else {
                        addConfConversation(invateConf, mInviteUser, false);
                    }

                    Conference c = new Conference(invateConf);
                    updateConferenceNotification(c);
                }
            } else if (JNIService.JNI_BROADCAST_CONFERENCE_REMOVED.equals(intent.getAction())) {
                long confId = intent.getLongExtra("gid", 0);
                // Remove conference conversation from list
                removeConversation(confId, false);
            }
        }
    }

    private static class LocalHandler extends Handler {
        private final WeakReference<TabFragmentMessage> mFragment;

        public LocalHandler(TabFragmentMessage fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mFragment.get() == null) {
                return;
            }
            mFragment.get().receiveMessage(msg);
        }
    }

    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_CONVERSATION:
                ConversationNotificationObject uno = (ConversationNotificationObject) msg.obj;
                if (uno == null)
                    return;
                long target = uno.getExtId();
                for (int i = 0; i < mItemList.size(); i++) {
                    Conversation cov = mItemList.get(i).cov;
                    if (cov.getExtId() == target) {
                        updateUnreadConversation(mItemList.get(i), V2GlobalConstants.READ_STATE_READ);
                        return;
                    }
                }
                break;
            case NEW_MESSAGE_UPDATE:
                MessageObject msgObj = (MessageObject) msg.obj;
                int groupType = msgObj.groupType;
                long groupID = msgObj.remoteGroupID;
                long remoteID = msgObj.rempteUserID;
                long msgID = msgObj.messageColsID;
                if (groupType == V2GlobalConstants.GROUP_TYPE_USER) {
                    updateUserConversation(remoteID, msgID);
                } else {
                    updateGroupConversation(groupType, groupID);
                }
                break;
            case REMOVE_CONVERSATION:
                long extId = (Long) msg.obj;
                removeConversation(extId, true);
                break;
            case UPDATE_VERIFICATION_MESSAGE:
                if (msg.arg1 == VerificationMessageType.CROWD_TYPE.intValue()) {
                    updateCrowdVerificationConversation(false);
                } else {
                    AddFriendHistorieNode node = VerificationProvider.getNewestFriendVerificationMessage();
                    if (node != null)
                        updateFriendVerificationConversation(node);
                }
                break;
        }
    }

    public void updateConversationToCreate(int groupType, long groupID, long remoteUserID) {
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation cov = mItemList.get(i).cov;
            if (cov.getType() == V2GlobalConstants.GROUP_TYPE_USER) {
                if (cov.getExtId() == remoteUserID) {
                    return;
                }
            } else {
                if (cov.getExtId() == groupID) {
                    return;
                }
            }
        }

        ScrollItem newItem;
        VMessage vm;
        if (V2GlobalConstants.GROUP_TYPE_USER == groupType) {
            vm = ChatMessageProvider.getNewestMessage(GlobalHolder.getInstance().getCurrentUserId(),
                    remoteUserID);
            if (vm == null)
                return;
            ContactConversation contact = new ContactConversation(remoteUserID);
            contact.setMsg(MessageUtil.getMixedConversationContent(mContext, vm));
            contact.setDate(vm.getDate());
            // 添加到ListView中
            GroupLayout viewLayout = new GroupLayout(mContext, contact);
            newItem = new ScrollItem(contact, viewLayout, false);
        } else {
            vm = ChatMessageProvider.getNewestGroupMessage(groupType, groupID);
            if (vm == null)
                return;
            newItem = makeNewGroupItem(vm, groupType, groupID);
            if (newItem == null) {
                V2Log.e(TAG, "updateConversationToCreate --> make new group item failed!");
                return;
            }
        }
        mItemList.add(0, newItem);
        ConversationProvider.saveConversation(vm);
        adapter.notifyDataSetChanged();
    }

}
