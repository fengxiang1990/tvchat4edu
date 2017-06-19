package com.bizcom.vc.activity.search;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.V2ConferenceRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestEnterConfResponse;
import com.bizcom.request.util.ConferencMessageSyncService;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AnimationHepler;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.SearchUtils;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.util.WaitLayoutBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.conference.ConferenceActivity;
import com.bizcom.vc.activity.conference.GroupLayout;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.adapter.SimpleBaseAdapter;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.ContactConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.CrowdConversation;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DepartmentConversation;
import com.bizcom.vo.DiscussionConversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class SearchLocalActivity extends BaseActivity {

    private static final int REQUEST_ENTER_CONF = 0x0002;
    private static final int REQUEST_ENTER_CONF_RESPONSE = 0x0003;
    private static final String TAG = SearchLocalActivity.class.getSimpleName();

    public static final int ITEM_TYPE_TAB = 0;
    public static final int ITEM_TYPE_NORMAL = 1;
    public static final int ITEM_TYPE_MORE = 2;

    private static final String ITEM_SORT_CONTACT = String.valueOf(100);
    private static final String ITEM_SORT_CONTACT_CHILD = String.valueOf(101);
    private static final String ITEM_SORT_CONTACT_MORE = String.valueOf(102);
    private static final String ITEM_SORT_GROUP = String.valueOf(200);
    private static final String ITEM_SORT_GROUP_CHILD = String.valueOf(201);
    private static final String ITEM_SORT_GROUP_MORE = String.valueOf(202);
    private static final String ITEM_SORT_MESSAGE = String.valueOf(300);
    private static final String ITEM_SORT_MESSAGE_CHILD = String.valueOf(301);
    private static final String ITEM_SORT_MESSAGE_MORE = String.valueOf(302);
    private static final String ITEM_SORT_CONF = String.valueOf(400);
    private static final String ITEM_SORT_CONF_CHILD = String.valueOf(401);
    private static final String ITEM_SORT_CONF_MORE = String.valueOf(402);

    private SimpleBaseAdapter.ListItem mContactsTab;
    private SimpleBaseAdapter.ListItem mContactsMore;
    private SimpleBaseAdapter.ListItem mGroupTab;
    private SimpleBaseAdapter.ListItem mGroupTabMore;
    private SimpleBaseAdapter.ListItem mMessageTab;
    private SimpleBaseAdapter.ListItem mMessageTabMore;
    private SimpleBaseAdapter.ListItem mConfTab;
    private SimpleBaseAdapter.ListItem mConfTabMore;

    private List<SimpleBaseAdapter.ListItem> mContatsResult;
    private List<SimpleBaseAdapter.ListItem> mGroupResult;
    private List<SimpleBaseAdapter.ListItem> mMessageResult;
    private List<SimpleBaseAdapter.ListItem> mConfResult;
    private Vector<String> searchlines = new Vector<>();
    private String mLastSearchContent = null;
    private V2ConferenceRequest mConferenceService;

    private View mSearchHintLy;
    private ListView mSearchResults;
    private SearchlocalAdapter mFirstSearchAdapter;
    private SearchlocalAdapter mSecondSearchAdapter;

    private View mFirstSearchTab;
    private ListView mSecondSearchTab;

    private SparseArray<List<SimpleBaseAdapter.ListItem>> mGlobalDatas;
    private SparseArray<List<SimpleBaseAdapter.ListItem>> mMessageGlobalDatas = new SparseArray<>();
    private List<SimpleBaseAdapter.ListItem> mDataLists;
    private List<SimpleBaseAdapter.ListItem> mSecondDataLists;

    private CommonAnimationListener mCommonAnimationListener = new CommonAnimationListener();
    private TextWatcherCallBackImp mTextWatcherCallBackImp = new TextWatcherCallBackImp();
    private SearchListResultItemClickListener mSearchListResultItemClickListener = new SearchListResultItemClickListener();
    private String mPercentReplaced;

    private SearchUtils mContactsSearchUtils;
    private SearchUtils mDisGroupSearchUtils;
    private SearchUtils mConfSearchUtils;
    private boolean isFrist = true;
    private boolean isSearching;
    private boolean isInSecondResultTab;
    private boolean isInMessageResult;
    private String mCurrentSearching;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setNeedAvatar(true);
        setNeedHandler(true);
        setNeedBroadcast(true);
        setContentView(R.layout.activity_searchlocal);
        super.onCreate(savedInstanceState);
        mPercentReplaced = buildPercentReplaced();
        res = getResources();
        mDataLists = new ArrayList<>();
        mFirstSearchAdapter = new SearchlocalAdapter(mContext, mDataLists);
        mSearchResults.setAdapter(mFirstSearchAdapter);
        mSearchResults.setOnItemClickListener(mSearchListResultItemClickListener);

        mSecondDataLists = new ArrayList<>();
        mSecondSearchAdapter = new SearchlocalAdapter(mContext, mSecondDataLists);
        mSecondSearchTab.setAdapter(mSecondSearchAdapter);
        mSecondSearchTab.setOnItemClickListener(mSearchListResultItemClickListener);
        initDatas();
        mContactsSearchUtils = new SearchUtils();
        mDisGroupSearchUtils = new SearchUtils();
        mConfSearchUtils = new SearchUtils();
        mConferenceService = new V2ConferenceRequest();
        AnimationHepler.getInstance().setAnimaListener(mCommonAnimationListener);
    }

    private void initDatas() {
        mGlobalDatas = new SparseArray<>();
        // 联系人数据源
        List<Group> mOrgGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DEPARTMENT);
        List<User> mOrgUsers = builListUser(mOrgGroup);
        List<Group> mContactGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
        List<User> mContactsUsers = builListUser(mContactGroup);
        // 1.去重
        List<User> target = new ArrayList<>();
        target.addAll(mOrgUsers);
        for (int i = 0; i < mContactsUsers.size(); i++) {
            User temp = mContactsUsers.get(i);
            if (!target.contains(temp)) {
                target.add(temp);
            }
        }
        // 2.构造数据体
        List<SimpleBaseAdapter.ListItem> mContats = new ArrayList<>();
        for (int i = 0; i < target.size(); i++) {
            User user = target.get(i);
            ContactConversation contactsNormal = new ContactConversation(user.getmUserId());
            contactsNormal.setConversationType(Conversation.TYPE_SEARCH_NORMAL);
            mContats.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, user.getmUserId(), contactsNormal, ITEM_SORT_CONTACT_CHILD));
        }
        mGlobalDatas.put(V2GlobalConstants.GROUP_TYPE_CONTACT, mContats);
        // 群组数据源
        List<Group> mDisGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DISCUSSION);
        List<SimpleBaseAdapter.ListItem> mDiscussion = new ArrayList<>();
        for (int i = 0; i < mDisGroup.size(); i++) {
            Group group = mDisGroup.get(i);
            DiscussionConversation disConv = new DiscussionConversation(group);
            mDiscussion.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, group.getGroupID(), disConv, ITEM_SORT_GROUP_CHILD));
        }
        mGlobalDatas.put(V2GlobalConstants.GROUP_TYPE_CROWD, mDiscussion);
        // 会议数据源
        List<Group> mConfGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONFERENCE);
        List<SimpleBaseAdapter.ListItem> mConf = new ArrayList<>();
        for (int i = 0; i < mConfGroup.size(); i++) {
            Group group = mConfGroup.get(i);
            ConferenceConversation confConv = new ConferenceConversation(group, false);
            mConf.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, group.getGroupID(), confConv, ITEM_SORT_CONF_CHILD));
        }
        mGlobalDatas.put(V2GlobalConstants.GROUP_TYPE_CONFERENCE, mConf);

        // tab
        CrowdConversation contactsTab = new CrowdConversation(new CrowdGroup(-10, res.getString(R.string.tab_org_name), null));
        contactsTab.setConversationType(Conversation.TYPE_SEARCH_TAB);
        mContactsTab = mFirstSearchAdapter.new ListItem(ITEM_TYPE_TAB, -10, contactsTab, ITEM_SORT_CONTACT);
        CrowdConversation contactsMore = new CrowdConversation(new CrowdGroup(-11,
                res.getString(R.string.common_more) + res.getString(R.string.tab_org_name), null));
        contactsMore.setConversationType(Conversation.TYPE_SEARCH_MORE);
        mContactsMore = mFirstSearchAdapter.new ListItem(ITEM_TYPE_MORE, -11, contactsMore, ITEM_SORT_CONTACT_MORE);

        CrowdConversation groupTab = new CrowdConversation(new CrowdGroup(-12, res.getString(R.string.tab_group_name), null));
        groupTab.setConversationType(Conversation.TYPE_SEARCH_TAB);
        mGroupTab = mFirstSearchAdapter.new ListItem(ITEM_TYPE_TAB, -12, groupTab, ITEM_SORT_GROUP);
        CrowdConversation groupTabMore = new CrowdConversation(new CrowdGroup(-13, res.getString(R.string.common_more) + res.getString(R.string.tab_group_name), null));
        groupTabMore.setConversationType(Conversation.TYPE_SEARCH_MORE);
        mGroupTabMore = mFirstSearchAdapter.new ListItem(ITEM_TYPE_MORE, -13, groupTabMore, ITEM_SORT_GROUP_MORE);

        CrowdConversation messageTab = new CrowdConversation(new CrowdGroup(-14, res.getString(R.string.tab_conversation_name), null));
        messageTab.setConversationType(Conversation.TYPE_SEARCH_TAB);
        mMessageTab = mFirstSearchAdapter.new ListItem(ITEM_TYPE_TAB, -14, messageTab, ITEM_SORT_MESSAGE);
        CrowdConversation messageMore = new CrowdConversation(new CrowdGroup(-15, res.getString(R.string.common_more) + res.getString(R.string.tab_conversation_name), null));
        messageMore.setConversationType(Conversation.TYPE_SEARCH_MORE);
        mMessageTabMore = mFirstSearchAdapter.new ListItem(ITEM_TYPE_MORE, -15, messageMore, ITEM_SORT_MESSAGE_MORE);

        CrowdConversation confTab = new CrowdConversation(new CrowdGroup(-16, res.getString(R.string.tab_conference_name), null));
        confTab.setConversationType(Conversation.TYPE_SEARCH_TAB);
        mConfTab = mFirstSearchAdapter.new ListItem(ITEM_TYPE_TAB, -16, confTab, ITEM_SORT_CONF);
        CrowdConversation confMore = new CrowdConversation(new CrowdGroup(-17, res.getString(R.string.common_more) + res.getString(R.string.tab_conference_name), null));
        confMore.setConversationType(Conversation.TYPE_SEARCH_MORE);
        mConfTabMore = mFirstSearchAdapter.new ListItem(ITEM_TYPE_MORE, -17, confMore, ITEM_SORT_CONF_MORE);
    }

    private List<User> builListUser(List<Group> mGroups) {
        List<User> globalDatas = new ArrayList<>();
        for (int i = 0; i < mGroups.size(); i++) {
            Group group = mGroups.get(i);
            getGroupUsers(globalDatas, group);
        }
        return globalDatas;
    }

    private List<SimpleBaseAdapter.ListItem> buildMessageListItem(SparseArray<List<VMessage>> messages) {
        mMessageGlobalDatas.clear();
        List<SimpleBaseAdapter.ListItem> meesageTypes = new ArrayList<>();
        List<SimpleBaseAdapter.ListItem> singleMsglist;
        for (int j = 0; j < messages.size(); j++) {
            List<VMessage> msglist = messages.valueAt(j);
            if (msglist.size() > 0) {
                singleMsglist = new ArrayList<>();
                int keyID = messages.keyAt(j);
                for (int i = 0; i < msglist.size(); i++) {
                    VMessage vMessage = msglist.get(i);
                    Conversation cov;
                    if (vMessage.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER) {
                        User mRemoteUser;
                        if (vMessage.isLocal()) {
                            mRemoteUser = GlobalHolder.getInstance().getUser(vMessage.getToUser().getmUserId());
                        } else {
                            mRemoteUser = GlobalHolder.getInstance().getUser(vMessage.getFromUser().getmUserId());
                        }
                        cov = new ContactConversation(mRemoteUser.getmUserId());
                        cov.setDate(vMessage.getDate());
                        cov.setConversationType(Conversation.TYPE_CONTACT);
                        cov.setSearchLocalMsgCovType(Conversation.TYPE_CONTACT);
                        cov.setMsg(MessageUtil.getMixedConversationContent(mContext, vMessage));
                        if (i == 0) {
                            ContactConversation firstCov = new ContactConversation(mRemoteUser.getmUserId());
                            firstCov.setDate(vMessage.getDate());
                            firstCov.setConversationType(Conversation.TYPE_CONTACT);
                            firstCov.setSearchLocalMsgCovType(Conversation.TYPE_CONTACT);
                            if (msglist.size() == 1) {
                                firstCov.setMsg(MessageUtil.getMixedConversationContent(mContext, vMessage));
                            } else {
                                String msg = String.format(res.getString(R.string.searchlocal_search_first_hint_content), String.valueOf(msglist.size()));
                                firstCov.setMsg(msg);
                            }
                            meesageTypes.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, mRemoteUser.getmUserId(), firstCov, ITEM_SORT_MESSAGE_CHILD));
                        }
                        singleMsglist.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, mRemoteUser.getmUserId(), cov, ITEM_SORT_MESSAGE_CHILD));
                    } else if (vMessage.getMsgCode() == V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
                        Group group = GlobalHolder.getInstance().getGroupById(vMessage.getGroupId());
                        cov = new DiscussionConversation(group);
                        cov.setDate(vMessage.getDate());
                        cov.setConversationType(Conversation.TYPE_CONTACT);
                        cov.setSearchLocalMsgCovType(Conversation.TYPE_DISCUSSION);
                        cov.setMsg(MessageUtil.getMixedConversationContent(mContext, vMessage));
                        if (i == 0) {
                            DiscussionConversation firstCov = new DiscussionConversation(group);
                            firstCov.setDate(vMessage.getDate());
                            firstCov.setConversationType(Conversation.TYPE_CONTACT);
                            firstCov.setSearchLocalMsgCovType(Conversation.TYPE_DISCUSSION);
                            if (msglist.size() == 1) {
                                firstCov.setMsg(MessageUtil.getMixedConversationContent(mContext, vMessage));
                            } else {
                                String msg = String.format(res.getString(R.string.searchlocal_search_first_hint_content), String.valueOf(msglist.size()));
                                firstCov.setMsg(msg);
                            }
                            meesageTypes.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, group.getGroupID(), firstCov, ITEM_SORT_MESSAGE_CHILD));
                        }
                        singleMsglist.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, group.getGroupID(), cov, ITEM_SORT_MESSAGE_CHILD));
                    } else if (vMessage.getMsgCode() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                        Group group = GlobalHolder.getInstance().getGroupById(vMessage.getGroupId());
                        cov = new DepartmentConversation(group);
                        cov.setDate(vMessage.getDate());
                        cov.setConversationType(Conversation.TYPE_CONTACT);
                        cov.setSearchLocalMsgCovType(Conversation.TYPE_DEPARTMENT);
                        cov.setMsg(MessageUtil.getMixedConversationContent(mContext, vMessage));
                        ((DepartmentConversation) cov).setShowContact(true);
                        if (i == 0) {
                            DepartmentConversation firstCov = new DepartmentConversation(group);
                            firstCov.setDate(vMessage.getDate());
                            firstCov.setConversationType(Conversation.TYPE_CONTACT);
                            firstCov.setSearchLocalMsgCovType(Conversation.TYPE_DEPARTMENT);
                            if (msglist.size() == 1) {
                                firstCov.setMsg(MessageUtil.getMixedConversationContent(mContext, vMessage));
                            } else {
                                String msg = String.format(res.getString(R.string.searchlocal_search_first_hint_content), String.valueOf(msglist.size()));
                                firstCov.setMsg(msg);
                            }
                            firstCov.setShowContact(true);
                            meesageTypes.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, group.getGroupID(), firstCov, ITEM_SORT_MESSAGE_CHILD));
                        }
                        singleMsglist.add(mFirstSearchAdapter.new ListItem(ITEM_TYPE_NORMAL, group.getGroupID(), cov, ITEM_SORT_MESSAGE_CHILD));
                    }
                }
                mMessageGlobalDatas.put(keyID, singleMsglist);
            }
        }
        return meesageTypes;
    }

    private String buildPercentReplaced() {
        char one = (char) 1;
        char two = (char) 1;
        char three = (char) 1;
        return String.valueOf(one) + String.valueOf(two) + String.valueOf(three);
    }

    private List<SimpleBaseAdapter.ListItem> startSingleSearching(List<SimpleBaseAdapter.ListItem> mDatas, SearchUtils mSearchUtils, String s) {
        if (isFrist) {
            mSearchUtils.clearAll();
            List<Object> conversations = new ArrayList<>();
            for (int i = 0; i < mDatas.size(); i++) {
                conversations.add(mDatas.get(i));
            }
            mSearchUtils.receiveList = conversations;
        }
        return mSearchUtils.startGlobalLocalSearch(s);
    }

    private void getGroupUsers(List<User> temp, Group group) {
        List<Group> childGroup = group.getChildGroup();
        if (childGroup.size() > 0) {
            for (int i = 0; i < childGroup.size(); i++) {
                getGroupUsers(temp, childGroup.get(i));
            }
        }

        for (int j = 0; j < group.getUsers().size(); j++) {
            User user = group.getUsers().get(j);
            if (!temp.contains(user)) {
                temp.add(user);
            }
        }
    }

    private void switchResultTab(boolean isEnterSecondResultTab) {
        isInSecondResultTab = isEnterSecondResultTab;
        mFirstSearchTab.setVisibility(View.VISIBLE);
        mSecondSearchTab.setVisibility(View.VISIBLE);
        if (isInSecondResultTab) {
            AnimationHepler.getInstance().loadAnimation(mContext,
                    mFirstSearchTab.getId(), R.anim.slide_out_to_left, mFirstSearchTab);
            AnimationHepler.getInstance().loadAnimation(mContext,
                    mSecondSearchTab.getId(), R.anim.slide_in_from_right, mSecondSearchTab);
        } else {
            AnimationHepler.getInstance().loadAnimation(mContext,
                    mSecondSearchTab.getId(), R.anim.slide_out_to_right, mSecondSearchTab);
            AnimationHepler.getInstance().loadAnimation(mContext,
                    mFirstSearchTab.getId(), R.anim.slide_in_from_left, mFirstSearchTab);
        }
    }

    @Override
    public void onBackPressed() {
        if (isInMessageResult) {
            isInMessageResult = false;
            if (!isInSecondResultTab) {
                mFirstSearchTab.setVisibility(View.VISIBLE);
                mSecondSearchTab.setVisibility(View.GONE);
                mSecondDataLists.clear();
                mSecondSearchAdapter.notifyDataSetChanged();
            } else {
                mSecondDataLists.clear();
                mSecondDataLists.addAll(mMessageResult);
                mSecondSearchAdapter.notifyDataSetChanged();
            }
        } else {
            if (isInSecondResultTab) {
                mSecondDataLists.clear();
                mSecondSearchAdapter.notifyDataSetChanged();
                switchResultTab(false);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WaitLayoutBuilder.clearWaitLayout();
        mConferenceService.clearCalledBack();
    }

    @Override
    public void addBroadcast(IntentFilter filter) {

    }

    @Override
    public void receiveBroadcast(Intent intent) {

    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case REQUEST_ENTER_CONF:
                final long confID = (Long) msg.obj;
                if (GlobalHolder.getInstance().isInMeeting()) {
                    return;
                }

                WaitDialogBuilder.showNormalWithHintProgress(mContext, mContext.getResources().getString(R.string.requesting_enter_conference));
                int POST_DELAY_ENTER_CONF = 3000;
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
                    Conference conf = recr.getConf();

                    GlobalHolder.getInstance().setMeetingState(true, conf.getId());
                    Intent enterConference = new Intent(mContext, ConferenceActivity.class);
                    enterConference.putExtra("conf", conf);
                    enterConference.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(enterConference);
                } else {
                    V2Log.d(TAG, "SearchLocal Request enter conf response , code is : " + response.getResult().name());
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
        }
    }

    @Override
    public void initViewAndListener() {
        TextView mTitleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContent.setText(getResources().getString(R.string.common_search));
        TextView mBackIV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        mBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setComBackImageTV(mBackIV);

        FrameLayout mSearchResultLy = (FrameLayout) findViewById(R.id.ws_searchlocal_resultLy);
        WaitLayoutBuilder.initWaitLayout(mContext, mSearchResultLy);

        mSearchHintLy = findViewById(R.id.ws_searchlocal_hintly);
        ClearEditText mSearchToolET = (ClearEditText) findViewById(R.id.ws_searchlocal_edittext);
        mSearchToolET.setTextWatcher(mTextWatcherCallBackImp);
        mSearchToolET.setTag(ClearEditText.TAG_IS_PATTERN);
        mSearchResults = (ListView) findViewById(R.id.ws_searchlocal_results);

        mFirstSearchTab = findViewById(R.id.ws_searchlocal_first_tab);
        mSecondSearchTab = (ListView) findViewById(R.id.ws_searchlocal_sencond_tab);
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    private class TextWatcherCallBackImp implements ClearEditText.TextWatcherCallBack {

        @Override
        public void onTextChangedCallBack(ClearEditText editText, CharSequence s, int start, int count, int after) {
            if (s.toString().equals(mCurrentSearching)) {
                return;
            }
            mCurrentSearching = s.toString();
            V2Log.d(TAG, "current search string :" + mCurrentSearching);
            // 判断是否是反向搜索
            boolean isReverseSearch = mLastSearchContent != null
                    && s.toString().length() < mLastSearchContent.length();
            if (isSearching) {
                if (TextUtils.isEmpty(s)) {
                    mSearchHintLy.setVisibility(View.VISIBLE);
                    mSearchResults.setVisibility(View.GONE);
                    isFrist = true;
                    searchlines.clear();
                    mContactsSearchUtils.clearAll();
                } else {
                    if (isReverseSearch && searchlines.size() > 0) {
                        searchlines.remove(searchlines.size() - 1);
                    } else {
                        boolean isAdd = true;
                        for (int i = 0; i < searchlines.size(); i++) {
                            String temp = searchlines.get(i);
                            if (temp.equals(s.toString())) {
                                isAdd = false;
                                break;
                            }
                        }

                        if (isAdd) {
                            V2Log.d(TAG, " successfully sendFriendToTv : " + s.toString());
                            searchlines.add(s.toString());
                        }
                    }
                }
                return;
            }

            if (TextUtils.isEmpty(s)) {
                mSearchHintLy.setVisibility(View.VISIBLE);
                mSearchResults.setVisibility(View.GONE);
                isFrist = true;
                searchlines.clear();
                mContactsSearchUtils.clearAll();
            } else {
                mSearchHintLy.setVisibility(View.GONE);
                mSearchResults.setVisibility(View.VISIBLE);
                try {
                    WaitLayoutBuilder.showNormalWithHintProgress(mContext, true);
                    new SearchLocalTask().execute(s.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SearchlocalAdapter extends SimpleBaseAdapter<SimpleBaseAdapter.ListItem> {

        public SearchlocalAdapter(Context mContext, List<SimpleBaseAdapter.ListItem> mDataLists) {
            super(mContext, mDataLists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (datasList == null || datasList.size() <= 0) {
                return convertView;
            }

            ListItem listItem = datasList.get(position);
            GroupLayout layout;
            if (convertView == null) {
                layout = new GroupLayout(mContext, (Conversation) listItem.mEntity);
                convertView = layout;
            } else {
                layout = (GroupLayout) convertView;
            }

            Conversation cov = (Conversation) listItem.mEntity;
            boolean isEndIndex = false;
            if (listItem.mTag != null && listItem.mTag.equals(mCurrentSearching)
                    && position != mDataLists.size() - 1) {
                isEndIndex = true;
            }

            switch (listItem.mType) {
                case ITEM_TYPE_TAB:
                    if (isInMessageResult) {
                        ListItem temp = datasList.get(position + 1);
                        List<SimpleBaseAdapter.ListItem> currentCovMsgs = mMessageGlobalDatas.get((int) temp.mId);
                        String msg = String.format(res.getString(R.string.searchlocal_search_final_hint_content), String.valueOf(currentCovMsgs.size()));
                        layout.updateSearchLocalConversation(cov, msg, isEndIndex, mCurrentSearching);
                    } else {
                        layout.updateSearchLocalConversation(cov, cov.getName(), isEndIndex, mCurrentSearching);
                    }
                    break;
                case ITEM_TYPE_MORE:
                    layout.updateSearchLocalConversation(cov, cov.getName(), isEndIndex, mCurrentSearching);
                    break;
                case ITEM_TYPE_NORMAL:
                    layout.updateSearchLocalConversation(cov, null, isEndIndex, mCurrentSearching);
            }
            return convertView;
        }

        @Override
        protected int compareToItem(ListItem currentItem, ListItem another) {
            if (currentItem == null && another == null)
                return 0;

            if (currentItem == null)
                return 1;

            if (another == null)
                return -1;

            if (currentItem.mId == another.mId)
                return 0;

            String currentSort = (String) currentItem.sortFlag;
            String anotherSort = (String) another.sortFlag;
            String currentSortFlag = String.valueOf(currentSort.charAt(0));
            String otherSortFlag = String.valueOf(anotherSort.charAt(0));
            if (currentSortFlag.equals(otherSortFlag)) {
                int currentSortFlagValue = Integer.valueOf(currentSort);
                int otherSortFlagValue = Integer.valueOf(anotherSort);
                if (currentSortFlagValue < otherSortFlagValue) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                int currentSortFlagFirst = Integer.valueOf(currentSortFlag);
                int otherSortFlagFirst = Integer.valueOf(otherSortFlag);
                if (currentSortFlagFirst < otherSortFlagFirst) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    class SearchLocalTask extends AsyncTask<String, Void, List<SimpleBaseAdapter.ListItem>> {

        @Override
        protected List<SimpleBaseAdapter.ListItem> doInBackground(String... params) {
            String search = params[0];
            isSearching = true;
            if (mContatsResult != null) {
                mContatsResult.clear();
            }

            if (mGroupResult != null) {
                mGroupResult.clear();
            }

            if (mMessageResult != null) {
                mMessageResult.clear();
            }

            if (mConfResult != null) {
                mConfResult.clear();
            }

            List<SimpleBaseAdapter.ListItem> targetResult = new ArrayList<>();
            V2Log.d(TAG, "current task searching : " + search);
            List<SimpleBaseAdapter.ListItem> mContactsData = mGlobalDatas.get(V2GlobalConstants.GROUP_TYPE_CONTACT);
            mContatsResult = startSingleSearching(mContactsData, mContactsSearchUtils, search);
            if (mContatsResult != null && mContatsResult.size() > 0) {
                targetResult.add(mContactsTab);
                if (mContatsResult.size() > 3) {
                    targetResult.addAll(mContatsResult.subList(0, 3));
                    targetResult.add(mContactsMore);
                    mContactsMore.mTag = mCurrentSearching;
                } else {
                    mContatsResult.get(mContatsResult.size() - 1).mTag = mCurrentSearching;
                    targetResult.addAll(mContatsResult);
                }
            }

            List<SimpleBaseAdapter.ListItem> mGroupData = mGlobalDatas.get(V2GlobalConstants.GROUP_TYPE_CROWD);
            mGroupResult = startSingleSearching(mGroupData, mDisGroupSearchUtils, search);
            if (mGroupResult != null && mGroupResult.size() > 0) {
                targetResult.add(mGroupTab);
                if (mGroupResult.size() > 3) {
                    targetResult.addAll(mGroupResult.subList(0, 3));
                    targetResult.add(mGroupTabMore);
                    mGroupTabMore.mTag = mCurrentSearching;
                } else {
                    mGroupResult.get(mGroupResult.size() - 1).mTag = mCurrentSearching;
                    targetResult.addAll(mGroupResult);
                }
            }

            String target = EscapedcharactersProcessing.convertPercent(search, mPercentReplaced);
            SparseArray<List<VMessage>> result = ChatMessageProvider.querySearchChatMessages(target);
            mMessageResult = buildMessageListItem(result);
            if (mMessageResult != null && mMessageResult.size() > 0) {
                targetResult.add(mMessageTab);
                if (mMessageResult.size() > 3) {
                    targetResult.addAll(mMessageResult.subList(0, 3));
                    targetResult.add(mMessageTabMore);
                    mMessageTabMore.mTag = mCurrentSearching;
                } else {
                    mMessageResult.get(mMessageResult.size() - 1).mTag = mCurrentSearching;
                    targetResult.addAll(mMessageResult);
                }
            }

            List<SimpleBaseAdapter.ListItem> mConfItems = mGlobalDatas.get(V2GlobalConstants.GROUP_TYPE_CONFERENCE);
            mConfResult = startSingleSearching(mConfItems, mConfSearchUtils, search);
            if (mConfResult != null && mConfResult.size() > 0) {
                targetResult.add(mConfTab);
                if (mConfResult.size() > 3) {
                    targetResult.addAll(mConfResult.subList(0, 3));
                    targetResult.add(mConfTabMore);
                    mConfTabMore.mTag = mCurrentSearching;
                } else {
                    mConfResult.get(mConfResult.size() - 1).mTag = mCurrentSearching;
                    targetResult.addAll(mConfResult);
                }
            }
            return targetResult;
        }

        @Override
        protected void onPostExecute(List<SimpleBaseAdapter.ListItem> mResults) {
            super.onPostExecute(mResults);
            WaitLayoutBuilder.showNormalWithHintProgress(mContext, false);
            isFrist = false;
            mDataLists.clear();
            V2Log.d(TAG, " waiting searchlines size : " + searchlines.size());
            if (searchlines.size() > 0) {
                String remove = searchlines.remove(0);
                WaitLayoutBuilder.showNormalWithHintProgress(mContext, true);
                V2Log.d(TAG, " execute next search task : " + remove);
                new SearchLocalTask().execute(remove);
            } else {
                if (mResults != null) {
                    mDataLists.addAll(mResults);
                    Collections.sort(mDataLists);
                    mFirstSearchAdapter.notifyDataSetChanged();
                }
                isSearching = false;
            }
            V2Log.d(TAG, "final results size : " + mDataLists.size());
        }
    }

    class SearchListResultItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SimpleBaseAdapter.ListItem listItem;
            if (isInSecondResultTab || isInMessageResult) {
                listItem = mSecondDataLists.get(position);
            } else {
                listItem = mDataLists.get(position);
            }
            if (listItem.mType == ITEM_TYPE_NORMAL) {
                Conversation mEntity = (Conversation) listItem.mEntity;
                if (mEntity.getType() == Conversation.TYPE_DISCUSSION) {
                    Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("obj", new ConversationNotificationObject(mEntity.getType(), mEntity.getExtId()));
                    startActivity(i);
                } else if (mEntity.getType() == Conversation.TYPE_SEARCH_NORMAL) {
                    Intent i = new Intent();
                    i.setClass(mContext, ContactDetail2.class);
                    i.putExtra("uid", mEntity.getExtId());
                    startActivity(i);
                } else if (mEntity.getType() == Conversation.TYPE_CONFERNECE) {
                    Message.obtain(mHandler, REQUEST_ENTER_CONF, mEntity.getExtId()).sendToTarget();
                } else if (mEntity.getType() == Conversation.TYPE_CONTACT) {
                    if (isInSecondResultTab) {
                        if (!isInMessageResult) {
                            List<SimpleBaseAdapter.ListItem> listItems = mMessageGlobalDatas.get((int) mEntity.getExtId());
                            if (listItems != null) {
                                if (listItems.size() == 1) {
                                    Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
                                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    i.putExtra("obj", new ConversationNotificationObject(mEntity.getSearchLocalMsgCovType(), mEntity.getExtId()));
                                    startActivity(i);
                                } else {
                                    isInMessageResult = true;
                                    mSecondDataLists.clear();
                                    mSecondDataLists.add(mMessageTab);
                                    mSecondDataLists.addAll(listItems);
                                    mSecondSearchAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
                            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            i.putExtra("obj", new ConversationNotificationObject(mEntity.getSearchLocalMsgCovType(), mEntity.getExtId()));
                            startActivity(i);
                        }
                    } else {
                        if (isInMessageResult) {
                            Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
                            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            i.putExtra("obj", new ConversationNotificationObject(mEntity.getSearchLocalMsgCovType(), mEntity.getExtId()));
                            startActivity(i);
                        } else {
                            List<SimpleBaseAdapter.ListItem> listItems = mMessageGlobalDatas.get((int) mEntity.getExtId());
                            if (listItems != null) {
                                if (listItems.size() == 1) {
                                    Intent i = new Intent(PublicIntent.START_CONVERSACTION_ACTIVITY);
                                    i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    i.putExtra("obj", new ConversationNotificationObject(mEntity.getSearchLocalMsgCovType(), mEntity.getExtId()));
                                    startActivity(i);
                                } else {
                                    mSecondSearchTab.setVisibility(View.VISIBLE);
                                    mFirstSearchTab.setVisibility(View.GONE);
                                    isInMessageResult = true;
                                    mSecondDataLists.clear();
                                    mSecondDataLists.add(mMessageTab);
                                    mSecondDataLists.addAll(listItems);
                                    mSecondSearchAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            } else if (listItem.mType == ITEM_TYPE_MORE) {
                if (listItem == mMessageTabMore) {
                    mSecondDataLists.addAll(mMessageResult);
                } else if (listItem == mContactsMore) {
                    mSecondDataLists.addAll(mContatsResult);
                } else if (listItem == mGroupTabMore) {
                    mSecondDataLists.addAll(mGroupResult);
                } else if (listItem == mConfTabMore) {
                    mSecondDataLists.addAll(mConfResult);
                }
                mSecondSearchAdapter.notifyDataSetChanged();
                switchResultTab(true);
            }
        }
    }

    class CommonAnimationListener implements AnimationHepler.AnimaListener {

        @Override
        public void animaLoadEnd(int animaID, Animation animation) {
            if (isInSecondResultTab) {
                if (animaID == mFirstSearchTab.getId()) {
                    mFirstSearchTab.setVisibility(View.GONE);
                }

                if (animaID == mSecondSearchTab.getId()) {
                    mSecondSearchTab.setVisibility(View.VISIBLE);
                }
            } else {
                if (animaID == mFirstSearchTab.getId()) {
                    mFirstSearchTab.setVisibility(View.VISIBLE);
                }

                if (animaID == mSecondSearchTab.getId()) {
                    mSecondSearchTab.setVisibility(View.GONE);
                }
            }
        }
    }
}
