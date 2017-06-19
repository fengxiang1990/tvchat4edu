package com.bizcom.vc.hg.ui;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.MainApplication;
import com.alibaba.fastjson.JSON;
import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.bo.UserStatusObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.service.JNIService;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.adapter.FriendAdapter;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.view.GridViewWithHeaderAndFooter;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.WebConfig;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.widget.StartVideoSelectWindow;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SecondTab1 extends Fragment {

    private static final int ITEM_TYPE_USER = 0;
    private static final int UPDATE_USER_SIGN = 8;
    private static final int UPDATE_USER_SIGN2 = 7;
    private static final int UPDATE_USER_SIGN3 = 9;
    private static final int DELETE_CONTACT_USER = 4;
    public static final String NEW_MSG = "SecondTab1";

    private HomeActivity mContext;

    private LocalHandler mHandler = new LocalHandler(this);
    private LocalReceiver receiver = new LocalReceiver();

    private BitmapManager.BitmapChangedListener mUserAvatarChangedListener = new UserAvatarChangedListener();

    private IntentFilter intentFilter;


    private boolean isCallBack;
    // 是否是第一次加载数据
    private boolean firstLoadData;

    @BindView(R.id.view_empty)
    LinearLayout viewEmpty;
    @BindView(R.id.loading_gif)
    SimpleDraweeView loadingGif;
    //    @BindView(R.id.image_bottom)
//    ImageView imageBottom;
    private GridViewWithHeaderAndFooter gd;
    private List<User> data;
    private FriendAdapter mAdapter;
    public static User RemoteUser = null;
    public static final String titleTextStr = "我的好友";
    StartVideoSelectWindow startVideoSelectWindow;
    View rootView;

    private void initGridView(View v) {
        gd = (GridViewWithHeaderAndFooter) v.findViewById(R.id.gd);
        gd.setVisibility(View.GONE);
        gd.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new FriendAdapter(data, this.getActivity());
        View footview = getActivity().getLayoutInflater().inflate(R.layout.activity_friend_footview, null);
        gd.addFooterView(footview);
        gd.setAdapter(mAdapter);
        gd.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= data.size())
                    return;
                final User u = data.get(position);

                if (MainApplication.mTvInfoBean == null) {
                    startVideoCall(u.getmUserId());
                } else {
                    startVideoSelectWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
                    startVideoSelectWindow.setOnSelectedListener(new StartVideoSelectWindow.OnSelectedListener() {
                        @Override
                        public void onSlected(int status) {
                            switch (status) {
                                case StartVideoSelectWindow.OnSelectedListener.START_VIDEO_FROM_PHONE:
                                    startVideoCall(u.getmUserId());
                                    startVideoSelectWindow.dismiss();
                                    break;
                                case StartVideoSelectWindow.OnSelectedListener.START_VIDEO_FROM_TV:
                                    startVideoCallByTv(u.getmUserId());
                                    startVideoSelectWindow.dismiss();
                                    break;
                            }
                        }
                    });
                }
            }
        });


        gd.setOnItemLongClickListener(new OnItemLongClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position >= data.size())
                    return true;

                Intent it = new Intent(mContext, UserDetailActivity.class);
                it.putExtra(UserDetailActivity.EXTRA_USER, data.get(position).getmUserId());
//                startActivity(it);
//                View view = arg1.findViewById(R.id.test_view);
//                startActivity(it, ActivityOptions.makeSceneTransitionAnimation(mContext, view, "sharedView").toBundle());
                startActivity(it, ActivityOptions.makeSceneTransitionAnimation(mContext).toBundle());
                return true;
            }
        });

    }

    private void notifyAdapter() {
        if (data.size() - 1 < 0) {
            viewEmpty.setVisibility(View.VISIBLE);
            gd.setVisibility(View.GONE);
        } else {
            viewEmpty.setVisibility(View.GONE);
            gd.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.hg_2nd_tab_one, container, false);
        ButterKnife.bind(this, rootView);
        showLoadingGif();
        data = new ArrayList<User>();
        startVideoSelectWindow = new StartVideoSelectWindow(getActivity());
        initGridView(rootView);

        firstLoadData = true;
        initReceiver();
        BitmapManager.getInstance().registerBitmapChangedListener(mUserAvatarChangedListener);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (HomeActivity) getActivity();

    }

    @Override
    public void onResume() {
        super.onResume();

        updateSecondTab2Data();

        if (!isCallBack) {
            isCallBack = true;
            CommonCallBack.getInstance().executeUpdateConversationState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        intentFilter = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        try {
            if (receiver != null)
                getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BitmapManager.getInstance().unRegisterBitmapChangedListener(mUserAvatarChangedListener);
    }

    private void showLoadingGif() {
        if (loadingGif == null) {
            return;
        }

        Uri uri = Uri.parse("res://drawable/" + R.drawable.loading_app);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        loadingGif.setController(controller);
        loadingGif.setVisibility(View.VISIBLE);
    }

    private void initReceiver() {
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
            intentFilter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
            intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
            intentFilter.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
            intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
            intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
            intentFilter.addAction(JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION);
            intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
            intentFilter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);
            intentFilter.addAction(PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP);
            intentFilter.addAction(JNIService.JNI_BROADCAST_NEW_MESSAGE);
        }
        getActivity().registerReceiver(receiver, intentFilter);
    }

    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_USER_SIGN:

                ((HomeActivity) mContext).displayIcon();
                updateDataBase();
                break;
            case UPDATE_USER_SIGN2:
                updateDataBase();

                break;
            case UPDATE_USER_SIGN3:
                updateDataBase();
                stopLoadingGif();

                break;
            case DELETE_CONTACT_USER:
                WaitDialogBuilder.dismissDialog();
                JNIResponse response = (JNIResponse) msg.obj;
                if (response.getResult() == JNIResponse.Result.SUCCESS) {

                    Intent intent = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
                    intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    ConversationNotificationObject obj = new ConversationNotificationObject(
                            Conversation.TYPE_VERIFICATION_MESSAGE, Conversation.SPECIFIC_VERIFICATION_ID, false);
                    intent.putExtra("obj", obj);
                    mContext.sendBroadcast(intent);

                } else if (response.getResult() == JNIResponse.Result.TIME_OUT) {
                    Toast.makeText(mContext, mContext.getString(R.string.contacts_delete_net_failed), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.contacts_delete_failed), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10000:
                    updateHTab3Data();
                    updateSecondTab3Data();
                    updateSecondTab2Data();
                    break;
            }
        }
    };

    /**
     * 把查詢到的好友列表插入數據庫
     */
    private void updateDataBase() {
        MainApplication.hasDataPrepared = true;
        for (User u : data) {
            u.setmStatusToIntValue(u.getmStatus().toIntValue());
        }
        Collections.sort(data);
        notifyAdapter();
        new Thread() {
            public void run() {
                try {
                    MainApplication.getDbUtils().deleteAll(User.class);
                    MainApplication.getDbUtils().saveAll(data);
                    handler.sendEmptyMessage(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 通知其他地方刷新
     */
    public void updateHTab3Data() {
        HTab3 mhtab3 = ((HomeActivity) mContext).getHTab3();
        ThirdTabBinded tabBinded = mhtab3.getThirdTabBinded();
        updateDataCallback callBack = tabBinded;
        if (callBack != null)
            callBack.OnCall(null);

    }

    /**
     * 通知其他地方刷新
     */
    public void updateSecondTab3Data() {
        HTab2 mhtab2 = ((HomeActivity) mContext).getHTab2();
        SecondTab3 tab3 = mhtab2.getSecondTab3();
        updateDataCallback callBack1 = tab3;
        if (callBack1 != null)
            callBack1.OnCall(null);
    }

    /**
     * 通知其他地方刷新
     */
    public void updateSecondTab2Data() {
        HTab2 mhtab2 = ((HomeActivity) mContext).getHTab2();
        SecondTab2 tab2 = mhtab2.getSecondTab2();
        updateDataCallback callBack2 = tab2;
        if (callBack2 != null)
            callBack2.OnCall(null);
    }

    /**
     * 通知Second2,3 不是好友的情况下 刷新在线状态
     */
    private void notifyIsNotFriendUpdateDate(long uid) {
        boolean isHasDataSecond2 = false;
        for (User u : MainApplication.mLatelyUserList) {
            if (u.getmUserId() == uid) {
                isHasDataSecond2 = true;
            }
        }
        if (isHasDataSecond2) {
            updateSecondTab2Data();
        }

        boolean isHasDataSecond3 = false;
        for (PhoneFriendItem u : MainApplication.SourceDateList) {
            if (u.getUserId() == uid) {
                isHasDataSecond3 = true;
            }
        }
        if (isHasDataSecond3) {
            updateSecondTab3Data();
        }
    }

    private void fillContactsMultilevelGroup() {
        new ContactsAsyncTaskLoader().execute();
    }

    @OnClick(R.id.button_add_friend)
    public void onClick() {
        Intent i2 = new Intent(mContext, HgAddFriendActivity.class);
        startActivity(i2);
    }

    private static class LocalHandler extends Handler {
        private final WeakReference<SecondTab1> mActivity;

        public LocalHandler(SecondTab1 fragment) {
            mActivity = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().receiveMessage(msg);
        }
    }

    private int mContactReceiveCount = 0;

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION)) {
                Log.i("tvliao", "JNI_BROADCAST_GROUP_NOTIFICATION");
                int groupType = intent.getIntExtra("gtype", -1);
                if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                    Log.i("tvliao", "JNI_BROADCAST_GROUP_NOTIFICATION-GROUP_TYPE_CONTACT");
                    mContactReceiveCount++;
//                    fillContactsMultilevelGroup();

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mContactReceiveCount <= 1) {
                                stopLoadingGif();
                            }
                        }
                    }, 2000);
                } else if (groupType == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
                }
            } else if (JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION.equals(intent.getAction())) {
                Log.i("tvliao", "JNI_BROADCAST_USER_STATUS_NOTIFICATION");
                UserStatusObject uso = (UserStatusObject) intent.getExtras().get("status");
                if (uso != null) {
                    boolean friend = GlobalHolder.getInstance().isFriend(uso.getUid());
                    if (friend) {
                        Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
                    }
                    notifyIsNotFriendUpdateDate(uso.getUid());

                }
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION.equals(intent.getAction())) {
                Log.i("tvliao", "JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION");
                int groupType = intent.getIntExtra("gtype", -1);
                if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                    Log.i("tvliao", "JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION-GROUP_TYPE_CONTACT");
                    fillContactsMultilevelGroup();
                    MainApplication.loadVoiceMediaData();
                    MainApplication.loadContactFriend(mContext);

                    Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
                }
            } else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
                Log.i("tvliao", "JNI_BROADCAST_GROUP_USER_REMOVED");
                GroupUserObject obj = (GroupUserObject) intent.getExtras().get("obj");
                if (obj == null) {
                    return;
                }

                if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT
                        || obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                    for (int i = 0; i < data.size(); i++) {
                        User temp = data.get(i);
                        if (temp.getAccountType() == ITEM_TYPE_USER && temp.getmUserId() == obj.getmUserId()) {
                            data.remove(i);
                            break;
                        }
                    }
                }
                Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();

            } else if (JNIService.JNI_BROADCAST_GROUP_USER_ADDED.equals(intent.getAction())) {
                Log.i("tvliao", "JNI_BROADCAST_GROUP_USER_ADDED");
                GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
                if (guo == null) {

                    return;
                }
                if (guo.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                    User user = GlobalHolder.getInstance().getUser(guo.getmUserId());

                    data.add(user);
                    Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
                }
                // Contacts group is updated
            } else if (JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO.equals(intent.getAction())) {
                Log.i("tvliao", "JNI_BROADCAST_USER_UPDATE_BASE_INFO");
                long uid = intent.getLongExtra("uid", -1);
                if (uid == -1)
                    return;
                Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
            } else if (PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION.equals(intent.getAction())) {
                Log.i("tvliao", "BROADCAST_USER_COMMENT_NAME_NOTIFICATION");
                Long uid = intent.getLongExtra("modifiedUser", -1);
                if (uid == -1) {
                    return;
                }

                Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
            } else if (PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP.equals(intent.getAction())) {
                Log.i("tvliao", "BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP");
                Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
            } else if (JNIService.JNI_BROADCAST_NEW_MESSAGE.equals(intent.getAction())) {
                MessageObject msgObj = intent.getParcelableExtra("msgObj");
                Log.i("tvPhone", "msgObj-" + msgObj.toString());
                dispatchMessage(intent);
            }
        }
    }

    private class UserAvatarChangedListener implements BitmapManager.BitmapChangedListener {

        @Override
        public void notifyAvatarChanged(User user, Bitmap bm) {
            Message.obtain(mHandler, UPDATE_USER_SIGN).sendToTarget();
        }
    }

    private class ContactsAsyncTaskLoader extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected List<User> doInBackground(Void... params) {
            List<Group> mContactGroupList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
            List<User> users = null;
            if (mContactGroupList != null && mContactGroupList.size() > 0) {
                users = mContactGroupList.get(0).getUsers();
            }
            return users;
        }

        @Override
        protected void onPostExecute(List<User> result) {
            if (result == null)
                return;
            Collections.sort(result);
            data.clear();
            data.addAll(result);
            if (data != null && data.size() - 1 >= 0) {
                Message.obtain(mHandler, UPDATE_USER_SIGN3).sendToTarget();
            }
        }

    }

    private void stopLoadingGif() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Animatable am = loadingGif.getController().getAnimatable();
                if (am != null && am.isRunning()) {
                    am.stop();
                }
                loadingGif.setVisibility(View.GONE);
                MainApplication.IsInitDataLoadingFinish = true;
            }
        }, 0);
    }


    /**
     * 处理收到的好友消息
     *
     * @param intent
     */
    public void dispatchMessage(final Intent intent) {
        try {
            //		{"result":0,"fromID":"39","timeStamp":"1472110619534","tvId":"85","type":9,"toID":"1195"}
            // intent.setAction(SecondTab1.class.getName());
            MessageObject msgObj = intent.getParcelableExtra("msgObj");
            long remoteID = msgObj.rempteUserID;
            long msgID = msgObj.messageColsID;
            VMessage m = ChatMessageProvider.loadUserMessageById(remoteID, msgID);
            String getInfo = m.getPlainText();
            JSONObject json = new JSONObject(getInfo);
            int type = json.getInt("type");
            if (type == ConstantParams.MESSAGE_TYPE_OTHER_CHANGE_NAME) {
                long mfromID = Long.parseLong("11" + json.get("fromID"));
                RemoteUser = GlobalHolder.getInstance().getExistUser(mfromID);
            } else {
                Intent i = new Intent();
                i.putExtra("msgObj", msgObj);
                i.setAction(NEW_MSG);
                getActivity().sendBroadcast(i);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static ConnectedMessageHandle connectedMessageHandle;

    public static void setConnectedMessageHandle(ConnectedMessageHandle cmh) {
        SecondTab1.connectedMessageHandle = cmh;
    }

    public interface ConnectedMessageHandle {
        /**
         * @param type
         * @param mutNum 如果发送的类型表情，则该参数有效
         */
        public void handleConnectedMessage(int type, int mutNum);
    }

    /**
     * 发起视频聊天
     *
     * @param remoteUserId 对方的id
     */
    private void startVideoCall(long remoteUserId) {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            ToastUtil.ShowToast_long(mContext, "服务器连接异常,请稍后再试");
            return;
        }
        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId, false,
                null, null);
    }


    /**
     * 发起视频聊天
     * <p>
     * 手机通过tv呼叫第三方
     */
    private void startVideoCallByTv(final long remoteUserId) {
        try {
            final User remoteUser = GlobalHolder.getInstance().getExistUser(remoteUserId);
            if (MainApplication.mTvInfoBean != null) {
                if (("11" + MainApplication.mTvInfoBean.getUid()).equals(String.valueOf(remoteUserId))) {
                    ToastUtil.ShowToast_long(getActivity(), "不能向自己绑定的TV发起视频聊天");
                    return;
                }
            }
            if (GlobalHolder.getInstance().checkServerConnected(getActivity())) {
                ToastUtil.ShowToast_long(getActivity(), "服务器连接异常,请稍后再试");
                return;
            }
            String uid = GlobalHolder.getInstance().getCurrentUser().getmUserId() + "";
            uid = uid.substring(2, uid.length());

            BussinessManger.getInstance(getActivity()).queryTvByUid(new IBussinessManager.OnResponseListener() {

                @Override
                public void onResponse(boolean isSuccess, int what, Object obj) {
                    if (isSuccess) {
                        TvInfoBeans mTvInfoBeans = JSON.parseObject(String.valueOf(obj), TvInfoBeans.class);
                        if (mTvInfoBeans != null) {
                            //Log.e("SecondTab1","tv info-->"+mTvInfoBeans.getNickName()+" "+mTvInfoBeans.getUid()+" "+mTvInfoBeans.getTvId());
                            //if (remoteUser.getmStatus() == User.Status.ONLINE) {
                            Intent in = new Intent(getActivity(), HgStartVideoByTvActivity.class);
                            in.putExtra("inCall", false);
                            in.putExtra("titleText", "视频通话");
                            in.putExtra("tvId", mTvInfoBeans.getTvId());
                            in.putExtra("toID", String.valueOf(remoteUserId));
                            in.putExtra("tvUid", mTvInfoBeans.getUid());
                            getActivity().startActivity(in);
                            //}
//                            else {
//                                ToastUtil.ShowToast_long(getActivity(), "对方不在线");
//                            }
                        } else {
                            ToastUtil.ShowToast_long(getActivity(), "您还没有绑定TV");
                        }
                    }

                }
            }, uid, WebConfig.channel);

        } catch (Exception e) {
            ToastUtil.ShowToast_long(getActivity(), "服务器连接异常,请稍后再试");
        }

    }
}
