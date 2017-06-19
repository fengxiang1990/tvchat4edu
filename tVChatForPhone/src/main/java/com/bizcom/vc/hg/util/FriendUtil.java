package com.bizcom.vc.hg.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vo.FriendGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.List;

/**
 * Created by HG-MALONG on 2016/12/19.
 */

public class FriendUtil {

    private static V2ContactsRequest contactService = new V2ContactsRequest();

    public interface AddFriendCallback {
        void onResult(boolean isSuccess);
    }

    public interface ResultCallback {
        void onResult(boolean isSuccess, User user);
    }

    private static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i("tvliao", "addContact-handleMessage" + msg.toString());
            JNIResponse res = (JNIResponse) msg.obj;
            switch (msg.what) {
                case 1000:
                    if (res == null) {
                        onResult.onResult(false);
                        return;
                    }
                    if (res.getResult() == JNIResponse.Result.SUCCESS) {
                        if (onResult != null) {
                            onResult.onResult(true);
                        }
                    } else {
                        if (onResult != null) {
                            onResult.onResult(false);
                        }
                    }
                    return;
            }

            WaitDialogBuilder.dismissDialog();
            WaitDialogBuilder.clearWaitDialog();

            if (res.getResult() == JNIResponse.Result.SUCCESS) {
                Toast.makeText(mContext, R.string.contacts_detail2_added_successfully,
                        Toast.LENGTH_SHORT).show();
                if (onResult != null) {
                    onResult.onResult(true);
                }
            } else {
                Toast.makeText(mContext, R.string.contacts_detail2_added_failed, Toast.LENGTH_SHORT)
                        .show();
                if (onResult != null) {
                    onResult.onResult(false);
                }
            }
        }
    };

    private static Context mContext;
    private static AddFriendCallback onResult;

    /**
     * 发送添加好友请求
     *
     * @param addUserId 被添加的用户id
     */
    public static void addConstantNoLoading(final Context context, long addUserId, final AddFriendCallback Result) {
        mContext = context;
        onResult = Result;

        long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
        if (currentUserId != addUserId) {// 不是添加自己
            User detailUser = GlobalHolder.getInstance().getUser(addUserId);
            // 判断好友关系
            List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
            boolean isRelation = false;
            for (Group group : friendGroup) {
                if (group.findUser(detailUser) != null) {
                    isRelation = true;
                    break;
                }
            }
            if (!isRelation) {
                List<Group> listFriendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
                contactService.addContact(new FriendGroup(listFriendGroup.get(0).getGroupID(), ""), detailUser, "",
                        "", new HandlerWrap(mHandler, 1000, null));

            } else {
                mHandler.sendEmptyMessage(1000);
               // Toast.makeText(mContext, R.string.contacts_detail2_friended, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, R.string.contacts_detail2_friend_me, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 发送添加好友请求
     *
     * @param addUserId 被添加的用户id
     */
    public static void addConstant(final Context context, long addUserId, final AddFriendCallback Result) {
        mContext = context;
        onResult = Result;

        long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
        if (currentUserId != addUserId) {// 不是添加自己
            User detailUser = GlobalHolder.getInstance().getUser(addUserId);
            // 判断好友关系
            List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
            boolean isRelation = false;
            for (Group group : friendGroup) {
                if (group.findUser(detailUser) != null) {
                    isRelation = true;
                    break;
                }
            }
            if (!isRelation) {
                WaitDialogBuilder.showNormalWithHintProgress(context);
                List<Group> listFriendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
                contactService.addContact(new FriendGroup(listFriendGroup.get(0).getGroupID(), ""), detailUser, "",
                        "", new HandlerWrap(mHandler, 1, null));

            } else {
                Toast.makeText(mContext, R.string.contacts_detail2_friended, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, R.string.contacts_detail2_friend_me, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送添加好友请求
     *
     * @param addUserId 被添加的用户id
     */
    public static void addConstant(final Context context, long addUserId, String commentName, final AddFriendCallback Result) {
        mContext = context;
        onResult = Result;

        long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
        if (currentUserId != addUserId) {// 不是添加自己
            User detailUser = GlobalHolder.getInstance().getUser(addUserId);
            // 判断好友关系
            List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
            boolean isRelation = false;
            for (Group group : friendGroup) {
                if (group.findUser(detailUser) != null) {
                    isRelation = true;
                    break;
                }
            }
            if (!isRelation) {
                WaitDialogBuilder.showNormalWithHintProgress(context);
                List<Group> listFriendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
                contactService.addContact(new FriendGroup(listFriendGroup.get(0).getGroupID(), ""), detailUser, "",
                        commentName, new HandlerWrap(mHandler, 1, null));

            } else {
                Toast.makeText(mContext, R.string.contacts_detail2_friended, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, R.string.contacts_detail2_friend_me, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发起视频聊天
     *
     * @param remoteUserId 对方的id
     */
    public static void startVideoCall(Context mContext, long remoteUserId) {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            ToastUtil.ShowToast_long(mContext, "服务器连接异常,请稍后再试");
            return;
        }
        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId, false,
                null, null);
    }

    public static void getUser(final long uid, final ResultCallback result) {
        if (!GlobalHolder.getInstance().isFriend(uid)) {
            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, uid);

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {

                int count = 0;

                @Override
                public void run() {
                    User u = GlobalHolder.getInstance().getUser(uid);
                    if (TextUtils.isEmpty(u.getAccount())) {
                        if (count < 10) {
                            handler.postDelayed(this, 300);
                            count++;
                        } else {
                            result.onResult(false, null);
                        }

                    } else {
                        if (result != null) {
                            result.onResult(true, u);
                        }
                    }
                }
            };

            handler.post(runnable);
        }else{
            if (result != null) {
                result.onResult(true, GlobalHolder.getInstance().getUser(uid));
            }
        }
    }
}
