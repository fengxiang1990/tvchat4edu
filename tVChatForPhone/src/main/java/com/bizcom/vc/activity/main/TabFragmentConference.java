package com.bizcom.vc.activity.main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.MainApplication;
import com.V2.jni.ImRequest;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.db.provider.ConversationProvider;
import com.bizcom.request.V2ConferenceRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestEnterConfResponse;
import com.bizcom.request.util.ConferencMessageSyncService;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.Notificator;
import com.bizcom.util.SearchUtils;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.conference.ConferenceActivity;
import com.bizcom.vc.activity.conference.GroupLayout;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.listener.ConferenceListener;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TabFragmentConference extends Fragment implements TextWatcher, ConferenceListener {
    private static final String TAG = TabFragmentConference.class.getSimpleName();
    private static final int FILL_CONFS_LIST = 0x0001;
    private static final int REQUEST_ENTER_CONF = 0x0002;
    private static final int REQUEST_ENTER_CONF_CLICK = 0x0004;
    private static final int REQUEST_ENTER_CONF_RESPONSE = 0x0003;
    private static final int REQUEST_UPDATE_ADAPTER = 0x0005;

    private static int POST_DELAY_ENTER_CONF = 3000;

    private View rootView;
    private View subTabLayout;
    private ListView mConferenceListView;
    private OnItemClickListener mConferenceListViewOnItemClickListener = new ConferenceListViewOnItemClickListener();
    private OnItemLongClickListener mConferenceListViewOnItemLongClickListener = new ConferenceListViewOnItemLongClickListener();
    private LocalHandler mHandler = new LocalHandler(this);
    private BroadcastReceiver receiver;
    private ConferenceListViewAdapter adapter = new ConferenceListViewAdapter();

    private Context mContext;
    private V2ConferenceRequest mConferenceService;
    private Set<Conversation> mUnreadConvList = new HashSet<>();
    ;
    private List<Conversation> mItemList = new ArrayList<>();
    private List<Conversation> searchList = new ArrayList<>();
    private Conversation currentClickConversation;

    private boolean mIsStartedSearch;
    // Use to mark which conference user entered..
    private boolean isFrist = true;
    private boolean isCallBack;
    private SearchUtils mSearchUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        V2Log.i(TAG, "TabFragmentConference onCreate()");
        super.onCreate(savedInstanceState);
        mConferenceService = new V2ConferenceRequest();
        mSearchUtils = new SearchUtils();
        mContext = getActivity();
        initBroadcast();
        Message.obtain(mHandler, FILL_CONFS_LIST).sendToTarget();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        V2Log.i(TAG, "TabFragmentConference onCreateView()");
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.tab_fragment_conversations, container, false);
            mConferenceListView = (ListView) rootView.findViewById(R.id.conversations_list_container);
            mConferenceListView.setAdapter(adapter);

            mConferenceListView.setOnItemClickListener(mConferenceListViewOnItemClickListener);
            mConferenceListView.setOnItemLongClickListener(mConferenceListViewOnItemLongClickListener);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        V2Log.i(TAG, "TabFragmentConference onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        V2Log.i(TAG, "TabFragmentConference onResume()");
        super.onResume();
        if (!isCallBack) {
            isCallBack = true;
            CommonCallBack.getInstance().executeUpdateConversationState();
            V2Log.w("JNIService", "executeUpdateConversationState conference");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        V2Log.i(TAG, "TabFragmentConference onDestroy()");

        mUnreadConvList.clear();
        mUnreadConvList = null;
        mItemList.clear();
        mItemList = null;
        searchList.clear();
        searchList = null;

        currentClickConversation = null;
        subTabLayout = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        mConferenceListView = null;
        try {
            if (receiver != null)
                mContext.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        V2Log.i(TAG, "TabFragmentConference onDestroyView()");
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            adapter.notifyDataSetChanged();
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
                List<Object> conversations = new ArrayList<Object>();
                for (int i = 0; i < mItemList.size(); i++) {
                    conversations.add(mItemList.get(i));
                }
                mSearchUtils.receiveList = conversations;
                isFrist = false;
            }

            searchList.clear();
            searchList = mSearchUtils.startConversationSearch(s);
            mIsStartedSearch = mSearchUtils.mIsStartedSearch;
            adapter.notifyDataSetChanged();
        }

        if (subTabLayout != null) {
            if (mIsStartedSearch)
                subTabLayout.setVisibility(View.GONE);
            else
                subTabLayout.setVisibility(View.VISIBLE);
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

    /**
     * According to mCurrentTabFlag, initialize different intent filter
     */
    private void initBroadcast() {
        receiver = new ConferenceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
        intentFilter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
        intentFilter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
        intentFilter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);

        intentFilter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_INVATITION);
        intentFilter.addAction(JNIService.JNI_BROADCAST_CONFERENCE_REMOVED);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
        intentFilter.addAction(JNIService.JNI_BROADCAST_GROUPS_LOADED);
        intentFilter.addAction(PublicIntent.BROADCAST_NEW_CONFERENCE_NOTIFICATION);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /**
     * According populateType to fill the List Data. The data from server!
     *
     * @param list
     */
    private void populateConversation(List<Group> list) {
        // 清空界面数据
        mItemList.clear();
        mUnreadConvList.clear();
        mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);

        for (int i = list.size() - 1; i >= 0; i--) {
            Group g = list.get(i);
            if (TextUtils.isEmpty(g.getName())) {
                V2Log.e(TAG, "Recv bad conference , no name , id : " + g.getGroupID());
                continue;
            } else {
                V2Log.d(TAG, "Recv new conference, name: " + g.getName() + " | id : " + g.getGroupID());
            }

            Conversation cov = new ConferenceConversation(g, false);
            mItemList.add(cov);
            sortAndUpdate();
        }
    }

    /**
     * Add a new conversation to current list.
     *
     * @param newConference
     * @param isRead
     */
    private void addConversation(Group newConference, boolean isRead) {
        if (newConference == null) {
            V2Log.e(TAG, "addConversation --> Add new conversation failed ! Given Group is null");
            return;
        }

        Conversation cov = null;
        boolean isAdd = true;
        for (Conversation item : mItemList) {
            if (item.getExtId() == newConference.getGroupID()) {
                cov = item;
                isAdd = false;
                break;
            }
        }

        if (isAdd) {
            cov = new ConferenceConversation(newConference, false);
            V2Log.d(TAG, "addConversation -- Successfully sendFriendToTv a new conversation , type is : " + cov.getType()
                    + " and id is : " + cov.getExtId() + " and name is : " + cov.getName() + " | isRead : " + isRead);
            mItemList.add(0, cov);
        } else {
            V2Log.d(TAG, "addConversation -- The Group Conversation already exist, type is : " + cov.getType()
                    + " and id is : " + cov.getExtId() + " and name is : " + cov.getName() + " | isRead : " + isRead);
        }

        if (isRead)
            cov.setReadFlag(V2GlobalConstants.READ_STATE_UNREAD);
        else
            cov.setReadFlag(V2GlobalConstants.READ_STATE_READ);
        // Update unread conversation list
        updateUnreadConversation(cov);
        Collections.sort(mItemList);
        mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
        scrollToTop();
    }

    /**
     * Remove conversation from mConvList by id.
     *
     * @param conversationID <ul>
     *                       <li>ConferenceConversation : conversationID mean
     *                       ConferenceGroup's id DiscussionGroup's's id
     *                       </ul>
     */
    private void removeConversation(long conversationID) {
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation temp = mItemList.get(i);
            if (temp.getExtId() == conversationID) {
                // update readState
                temp.setReadFlag(V2GlobalConstants.READ_STATE_READ);
                updateUnreadConversation(temp);
                // remove item
                Conversation removed = mItemList.remove(i);
                if (removed == null)
                    V2Log.e(TAG, "Delete Conversation Failed...id is : " + conversationID);
                // clear all system notification
                Notificator.cancelSystemNotification(getActivity(), V2GlobalConstants.MESSAGE_NOTIFICATION_ID);
                break;
            }
        }
        sortAndUpdate();
    }

    private void scrollToTop() {
        mConferenceListView.post(new Runnable() {

            @Override
            public void run() {
                mConferenceListView.setSelection(0);
            }
        });
    }

    /**
     * Update main activity to show or hide notificator , and update
     * conversation read state in database
     *
     * @param cov
     */
    private void updateUnreadConversation(Conversation cov) {
        int ret;
        if (cov.getReadFlag() == V2GlobalConstants.READ_STATE_READ) {
            boolean flag = mUnreadConvList.remove(cov);
            if (flag) {
                ret = V2GlobalConstants.READ_STATE_READ;
            } else {
                return;
            }
        } else {
            boolean flag = mUnreadConvList.add(cov);
            if (flag) {
                ret = V2GlobalConstants.READ_STATE_UNREAD;
            } else {
                return;
            }
        }

        // update conversation date and flag to database
        ConversationProvider.updateConversationToDatabase(cov, ret);
    }

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
        enterConference.putExtra("initFragment", 3);
        Notificator.updateSystemNotification(mContext, creator == null ? "" : creator.getDisplayName(),
                mContext.getString(R.string.conversation_attend_the_meeting) + " < " + conf.getName() + " >", false,
                enterConference, V2GlobalConstants.VIDEO_NOTIFICATION_ID, conf.getId());
    }

    /**
     * @return true mean don't sending , false sending
     */
    private boolean checkSendingState() {
        if (GlobalHolder.getInstance().isInMeeting() || GlobalHolder.getInstance().isInAudioCall()
                || GlobalHolder.getInstance().isInVideoCall()) {
            return true;
        }
        return false;
    }

    private void startConferenceActivity(Conference conf) {
        // Set current state to in meeting state
        GlobalHolder.getInstance().setMeetingState(true, conf.getId());
        Intent enterConference = new Intent(mContext, ConferenceActivity.class);
        enterConference.putExtra("conf", conf);
        enterConference.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(enterConference);
    }

    private void sortAndUpdate() {
        Collections.sort(mItemList);
        mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
    }

    /**
     * This request from main activity
     *
     * @see com.bizcom.vc.listener.ConferenceListener#requestJoinConf(com.bizcom.vo.Conference
     *)
     */
    @Override
    public boolean requestJoinConf(Conference conf) {
        if (conf == null) {
            return false;
        }

        Message.obtain(mHandler, REQUEST_ENTER_CONF, conf.getId()).sendToTarget();
        // This request from main activity
        // We need to update notificator for conversation
        for (int i = 0; i < mItemList.size(); i++) {
            Conversation item = mItemList.get(i);
            // update main activity notificator
            item.setReadFlag(V2GlobalConstants.READ_STATE_READ);
            updateUnreadConversation(item);
        }
        return true;
    }

    private class ConferenceListViewOnItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapters, View v, int pos, long id) {
            if (AlgorithmUtil.isFastClick()) {
                return;
            }

            if (mIsStartedSearch)
                currentClickConversation = searchList.get(pos);
            else
                currentClickConversation = mItemList.get(pos);

            Message.obtain(mHandler, REQUEST_ENTER_CONF_CLICK, currentClickConversation.getExtId()).sendToTarget();
            // update main activity notificator
            currentClickConversation.setReadFlag(V2GlobalConstants.READ_STATE_READ);
            updateUnreadConversation(currentClickConversation);
            mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
        }
    }

    private class ConferenceListViewOnItemLongClickListener implements OnItemLongClickListener {

        private DeleteConferenceOnClickListener mDeleteConferenceOnClickListener = new DeleteConferenceOnClickListener();

        @Override
        public boolean onItemLongClick(AdapterView<?> adapter, View v, int pos, long id) {
            String[] item;
            currentClickConversation = mItemList.get(pos);

            item = new String[]{mContext.getResources().getString(R.string.conversations_delete_conf)};
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mItemList.get(pos).getName()).setItems(item, mDeleteConferenceOnClickListener);
            AlertDialog ad = builder.create();
            ad.show();

            return true;
        }

        private class DeleteConferenceOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int which) {
                if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                    dialog.dismiss();
                    return;
                }
                Group g = GlobalHolder.getInstance().getGroupById(currentClickConversation.getExtId());
                if (g != null) {
                    mConferenceService.quitConference(
                            new Conference(currentClickConversation.getExtId(), g.getOwnerUser().getmUserId()), null);
                    removeConversation(currentClickConversation.getExtId());
                }
                dialog.dismiss();
            }
        }

    }

    class ConferenceListViewAdapter extends BaseAdapter {

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
            else
                return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (mIsStartedSearch)
                return searchList.get(position).getExtId();
            else
                return mItemList.get(position).getExtId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupLayout glt;
            Conversation cov;
            if (mIsStartedSearch) {
                cov = searchList.get(position);
            } else {
                cov = mItemList.get(position);
            }

            if (convertView == null) {
                glt = new GroupLayout(mContext, cov);
                convertView = glt;
            } else {
                glt = (GroupLayout) convertView;
            }

            Group conference = GlobalHolder.getInstance().getGroupById(cov.getExtId());
            if (position == mItemList.size() - 1) {
                glt.updateGroupContent(conference, cov.getReadFlag(), false);
            } else {
                glt.updateGroupContent(conference, cov.getReadFlag(), true);
            }
            return convertView;
        }
    }

    private static class LocalHandler extends Handler {
        private final WeakReference<TabFragmentConference> mFragment;

        public LocalHandler(TabFragmentConference fragment) {
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
            case FILL_CONFS_LIST:
                List<Group> gl = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONFERENCE);
                if (gl != null && gl.size() > 0) {
                    populateConversation(gl);
                }
                break;
            case REQUEST_ENTER_CONF_CLICK:
                // 会议Tab与消息Tab进会的时候延迟一样
                // POST_DELAY_ENTER_CONF = 0;
            case REQUEST_ENTER_CONF:
                final long confID = (Long) msg.obj;
                if (GlobalHolder.getInstance().isInMeeting()) {
                    return;
                }

                WaitDialogBuilder.showNormalWithHintProgress(mContext, mContext.getResources().getString(R.string.requesting_enter_conference));
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mContext.startService(new Intent(mContext, ConferencMessageSyncService.class));
                        mConferenceService.requestEnterConference(new Conference(confID),
                                new HandlerWrap(mHandler, REQUEST_ENTER_CONF_RESPONSE, null));
                        // POST_DELAY_ENTER_CONF = 4000;
                    }
                }, POST_DELAY_ENTER_CONF);
                break;
            case REQUEST_ENTER_CONF_RESPONSE:
                WaitDialogBuilder.dismissDialog();
                JNIResponse response = (JNIResponse) msg.obj;
                if (response.getResult() == JNIResponse.Result.SUCCESS) {
                    RequestEnterConfResponse recr = (RequestEnterConfResponse) msg.obj;
                    Conference c = recr.getConf();
                    startConferenceActivity(c);
                } else {
                    V2Log.d(TAG, "Request enter conf response , code is : " + response.getResult().name());
                    int errResId;
                    if (response.getResult() == RequestEnterConfResponse.Result.ERR_CONF_LOCKDOG_NORESOURCE) {
                        errResId = R.string.error_request_enter_conference_no_resource;
                    } else if (response.getResult() == RequestEnterConfResponse.Result.ERR_CONF_NO_EXIST) {
                        errResId = R.string.error_request_enter_conference_not_exist;
                    } else if (response.getResult() == RequestEnterConfResponse.Result.TIME_OUT) {
                        errResId = R.string.error_request_enter_conference_time_out;
                    } else {
                        errResId = R.string.error_request_enter_conference_time_out;
                    }
                    Toast.makeText(mContext, errResId, Toast.LENGTH_SHORT).show();
                    mContext.stopService(new Intent(mContext, ConferencMessageSyncService.class));
                }
                break;
            case REQUEST_UPDATE_ADAPTER:
                adapter.notifyDataSetChanged();
                break;
        }
    }

    class ConferenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (JNIService.JNI_BROADCAST_GROUP_NOTIFICATION.equals(intent.getAction())) {
                int type = intent.getExtras().getInt("gtype");
                if ((type == V2GlobalConstants.GROUP_TYPE_CONFERENCE)) {
                    Message.obtain(mHandler, FILL_CONFS_LIST).sendToTarget();
                }
                // From this broadcast, user has already read conversation
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {
                int groupType = intent.getIntExtra("gtype", -1);
                if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    V2Log.d(TAG, "start update all conference group info");
                    mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
                }
            } else if (JNIService.JNI_BROADCAST_CONFERENCE_INVATITION.equals(intent.getAction())) {
                long gid = intent.getLongExtra("gid", 0);
                Group invateConf = GlobalHolder.getInstance().getGroupById(gid);
                if (invateConf != null) {
                    addConversation(invateConf, false);

                    Conference c = new Conference((ConferenceGroup) invateConf);
                    updateConferenceNotification(c);
                }
            } else if (JNIService.JNI_BROADCAST_CONFERENCE_REMOVED.equals(intent.getAction())) {
                long confId = intent.getLongExtra("gid", 0);
                // Remove conference conversation from list
                removeConversation(confId);
                // This broadcast is sent after create conference successfully
            } else if (PublicIntent.BROADCAST_NEW_CONFERENCE_NOTIFICATION.equals(intent.getAction())) {
                Group conf = GlobalHolder.getInstance().getGroupById(intent.getLongExtra("newGid", 0));
                // Add conference to conversation list
                addConversation(conf, false);
                Conference c = new Conference((ConferenceGroup) conf);
                startConferenceActivity(c);
            } else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
                Long uid = intent.getLongExtra("modifiedUser", -1);
                if (uid == -1l) {
                    V2Log.e("ConversationsTabFragment BROADCAST_USER_COMMENT_NAME_NOTIFICATION ---> update user comment name failed , get id is -1");
                    return;
                }

                for (int i = 0; i < mItemList.size(); i++) {
                    ConferenceConversation con = (ConferenceConversation) mItemList.get(i);
                    if (con.getGroup().getOwnerUser() != null && con.getGroup().getOwnerUser().getmUserId() == uid) {
                        mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
                    }
                }
            } else if (JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO.equals(intent.getAction())) {
                long uid = intent.getLongExtra("uid", -1);
                if (uid == -1)
                    return;

                for (int i = 0; i < mItemList.size(); i++) {
                    ConferenceConversation con = (ConferenceConversation) mItemList.get(i);
                    User owner = con.getGroup().getOwnerUser();
                    if (owner != null && owner.getmUserId() == uid) {
                        mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
                        break;
                    }
                }
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
                GroupUserObject obj = (GroupUserObject) intent.getExtras().get("obj");
                if (obj == null) {
                    V2Log.e(TAG,
                            "JNI_BROADCAST_GROUP_USER_ADDED --> Received the broadcast to quit the crowd group , but crowd id is wroing... ");
                    return;
                }

                if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    List<Conversation> removed = new ArrayList<>();
                    for (int i = 0; i < mItemList.size(); i++) {
                        ConferenceConversation con = (ConferenceConversation) mItemList.get(i);
                        User owner = con.getGroup().getOwnerUser();
                        if (owner != null && owner.getmUserId() == obj.getmUserId()) {
                            removed.add(mItemList.get(i));
                        }
                    }

                    for (int i = 0; i < removed.size(); i++) {
                        mItemList.remove(removed.get(i));
                    }
                    mHandler.sendEmptyMessage(REQUEST_UPDATE_ADAPTER);
                }
            } else if (JNIService.JNI_BROADCAST_GROUPS_LOADED.equals(intent.getAction())) {
                // 检测会议的创建人是否是组织外的
                boolean isOutOrg = true;
                for (int i = 0; i < mItemList.size(); i++) {
                    ConferenceConversation con = (ConferenceConversation) mItemList.get(i);
                    User ownerUser = con.getGroup().getOwnerUser();
                    User confUser = GlobalHolder.getInstance().getUser(ownerUser.getmUserId());
                    Set<Group> confUserBelongs = confUser.getBelongsGroup();
                    Iterator<Group> confIter = confUserBelongs.iterator();
                    while (confIter.hasNext()) {
                        Group temp = confIter.next();
                        if (temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT
                                || temp.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                            Group result = temp.findUser(ownerUser.getmUserId());
                            if (result != null) {
                                isOutOrg = false;
                                break;
                            }
                        }
                    }

                    if (isOutOrg) {
                        ImRequest.getInstance().ImGetUserBaseInfo(ownerUser.getmUserId());
                    }
                }
            }
        }
    }
}
