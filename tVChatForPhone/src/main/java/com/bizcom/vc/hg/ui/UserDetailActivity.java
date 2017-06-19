package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.alibaba.fastjson.JSONObject;
import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.DialogManager;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.util.CorlorUtil;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.widget.MProgressDialog;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by malong on 2016/12/12.
 */
public class UserDetailActivity extends Activity {

    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_USER = "extra_user";

    public static final int TYPE_USER_DETAIL = 0;
    public static final int TYPE_ADD_FRIEND = 1;
    private final int TYPE_UPDATE_COMMENT = 2;

    private final int TYPE_UPDATE_REMARK = 3;
    private final int DELETE_CONTACT_USER = 4;

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.img_header)
    SimpleDraweeView imgHeader;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.text_account)
    TextView textAccount;
    @BindView(R.id.text_remark)
    TextView textRemark;
    @BindView(R.id.ll_remark)
    LinearLayout llRemark;
    @BindView(R.id.ll_sendTotv)
    LinearLayout llSendTotv;
    @BindView(R.id.button_delete)
    TextView buttonDelete;
    @BindView(R.id.button_call)
    TextView buttonCall;
    @BindView(R.id.view_line2)
    View viewLine2;
    @BindView(R.id.view_line1)
    View viewLine1;
    @BindView(R.id.text_tv_delete)
    TextView textTvDelete;
    @BindView(R.id.firstNameText)
    TextView firstNameText;
    @BindView(R.id.edit_remark)
    EditText editRemark;
    @BindView(R.id.ll_remark_edit)
    LinearLayout llRemarkEdit;


    private MProgressDialog dialog;
    private Dialog deleteContactDialog;
    private EditText etDlg;
    private User mUser;
    private long userId;

    private String currentUserId;
    private String currentUid;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置contentFeature,可使用切换动画
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition explode = TransitionInflater.from(this).inflateTransition(android.R.transition.explode);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_user_detail);
        ButterKnife.bind(this);

        userId = getIntent().getLongExtra(EXTRA_USER, -1);
        mUser = GlobalHolder.getInstance().getUser(userId);

        if (TextUtils.isEmpty(mUser.getAccount())) {
            showLoadingDialog();
            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, userId);
            mHandler.postDelayed(mUiRunnable, 500);
        } else {
            initData();
        }
    }

    Runnable mUiRunnable = new Runnable() {
        @Override
        public void run() {
            mUser = GlobalHolder.getInstance().getUser(userId);
            if (TextUtils.isEmpty(mUser.getAccount())) {
                mHandler.postDelayed(this, 500);
            } else {
                if (dialog != null) {
                    dialog.dismiss();
                }
                initData();
            }
        }
    };

    private void showLoadingDialog() {
        dialog = new MProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("");
        dialog.setMessage("请稍后");
        dialog.show();
    }

    private void initData() {
        firstNameText.setText(MessageUtil.getFirstLetterName(mUser.getDisplayName()));
        SimpleDraweeViewUtils.display(imgHeader, firstNameText, mUser.getmAvatarLocation());

        textName.setText(mUser.getNickName());
        textAccount.setText(mUser.getAccount());
        textRemark.setText(mUser.getCommentName());

        currentUserId = String.valueOf(GlobalHolder.getInstance().getCurrentUserId());
        currentUid = currentUserId.substring(2, currentUserId.length());

        if (!GlobalHolder.getInstance().isFriend(mUser)) {
            textTitle.setVisibility(View.INVISIBLE);
            llRemark.setVisibility(View.GONE);
            llSendTotv.setVisibility(View.GONE);
            llRemarkEdit.setVisibility(View.VISIBLE);
            viewLine1.setVisibility(View.GONE);
            viewLine2.setVisibility(View.GONE);
            buttonDelete.setText("添加好友");

        } else {
            llRemarkEdit.setVisibility(View.GONE);
            llRemark.setVisibility(View.VISIBLE);
            viewLine1.setVisibility(View.VISIBLE);
            buttonDelete.setText("删除好友");


            if (MainApplication.mTvInfoBean == null) {
                llSendTotv.setVisibility(View.GONE);
                viewLine2.setVisibility(View.GONE);
            } else {
                checkFriendIsBindTv();
            }
        }
    }

    @OnClick({R.id.ll_remark, R.id.ll_sendTotv, R.id.button_delete, R.id.button_call, R.id.img_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_remark:
                Intent it = new Intent(UserDetailActivity.this, UpdateUserCommentNameActivity.class);
                it.putExtra(EXTRA_USER, mUser.getmUserId());
                startActivityForResult(it, TYPE_UPDATE_COMMENT);

                break;
            case R.id.ll_sendTotv:
                if (TextUtils.equals("发送好友到TV", textTvDelete.getText().toString())) {
                    if (String.valueOf(mUser.getmUserId()).equals("11" + MainApplication.mTvInfoBean.getUid())) {
                        Toast.makeText(UserDetailActivity.this, "不能发送已绑定的TV", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i("tvliao", "mUser.getmUserId()-" + mUser.getmUserId() + "--" + MainApplication.mTvInfoBean.getUid());

                    showAddTvDlg(String.valueOf(mUser.getmUserId()));
                } else {
                    showDeleteTvDlg(String.valueOf(mUser.getmUserId()));
                }
                break;
            case R.id.button_delete:
                if (GlobalHolder.getInstance().isFriend(mUser)) {
                    showDeleteContactDialog(mUser.getmUserId());
                } else {
                    String remark = editRemark.getText().toString().trim();
                    FriendUtil.addConstant(UserDetailActivity.this, mUser.getmUserId(),remark, new FriendUtil.AddFriendCallback() {
                        @Override
                        public void onResult(boolean isSuccess) {
                            if (isSuccess) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        initData();
                                    }
                                });
                            }
                        }
                    });
                }

                break;
            case R.id.button_call:
                if (mUser.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
                    Toast.makeText(UserDetailActivity.this, "不能呼叫自己", Toast.LENGTH_SHORT).show();
                } else {
                    startVideoCall(mUser.getmUserId());
                }

                break;
            case R.id.img_back:
                onBackPressed();
//                finish();
                break;
        }
    }

    private void checkFriendIsBindTv() {
        final String fuid = String.valueOf(mUser.getmUserId());
        final String mFuid = fuid.substring(2, fuid.length());
        BussinessManger.getInstance(this).queryTvUserFriend(new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    JSONObject mJson = (JSONObject) obj;
                    Log.i("tvliao", "mJson-" + mJson.toString());
                    String code = mJson.getString("code");
                    if (TextUtils.equals(code, "0012")) {
                        llSendTotv.setVisibility(View.VISIBLE);
                        viewLine2.setVisibility(View.VISIBLE);
                        textTvDelete.setText("发送好友到TV");

                    } else if (TextUtils.equals(code, "0023")) {
                        llSendTotv.setVisibility(View.VISIBLE);
                        viewLine2.setVisibility(View.VISIBLE);
                        textTvDelete.setText("从TV端删除");

                    } else {
                        llSendTotv.setVisibility(View.GONE);
                        viewLine2.setVisibility(View.GONE);
                    }
                }

            }

        }, MainApplication.mTvInfoBean.getTvId(), mFuid, currentUid, BussinessManger.CHANNEL);
    }

    private void showDeleteContactDialog(final long uid) {
        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(this, null,
                        "确定将" + CorlorUtil.CreatColortext(GlobalHolder.getInstance().getExistUser(uid).getDisplayName())
                                + "从好友列表删除吗？",
                        getText(R.string.conversation_quit_dialog_confirm_text),
                        getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        // 删除好友
                        WaitDialogBuilder.showNormalWithHintProgress(UserDetailActivity.this);
                        deleteContactDialog.dismiss();
                        User u = GlobalHolder.getInstance().getExistUser(uid);
                        new V2ContactsRequest().delContact(u, new HandlerWrap(mHandler, DELETE_CONTACT_USER, null));
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();
    }

    protected void showAddTvDlg(final String fuid) {

        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(this, null,
                        "确定将" + CorlorUtil.CreatColortext(GlobalHolder.getInstance().getExistUser(Long.parseLong(fuid)).getDisplayName()) + "发送到TV好友吗？",
                        getText(R.string.conversation_quit_dialog_confirm_text),
                        getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        deleteContactDialog.dismiss();
                        sendFriendToTv(fuid);
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();

    }


    protected void showDeleteTvDlg(final String fuid) {

        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(this, null,
                        "确定将" + CorlorUtil.CreatColortext(GlobalHolder.getInstance().getExistUser(Long.parseLong(fuid)).getDisplayName()) + "从TV端删除吗？",
                        getText(R.string.conversation_quit_dialog_confirm_text),
                        getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        deleteContactDialog.dismiss();
                        final String mFuid = fuid.substring(2, fuid.length());
                        deleteFriendToTv("[" + mFuid + "]");
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();

    }

    public void deleteFriendToTv(final String frends) {
        String uid = String.valueOf(GlobalHolder.getInstance().getCurrentUserId());
        if (uid != null) {
            uid = uid.substring(2, uid.length());
        }

        final String finalUid = uid;
        WaitDialogBuilder.showNormalWithHintProgress(UserDetailActivity.this);
        BussinessManger.getInstance(UserDetailActivity.this).syncFrendsBatchToTv(frends, MainApplication.mTvInfoBean.getUid(), uid, BussinessManger.CHANNEL, "1", new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                WaitDialogBuilder.dismissDialog();
                WaitDialogBuilder.clearWaitDialog();
                String[] strs = frends.replace("[", "").replace("]", "").split(",");
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < strs.length; i++) {
                    String str = "11" + strs[i];
                    sb.append(str).append(",");
                }
                sb.append("]");
                String ids = sb.toString().replace(",]", "]");
                if (isSuccess) {
                    SharedPreferences preferences = getSharedPreferences("tvl", MODE_PRIVATE);
                    Date date = new Date();
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                    String sync_time = sf.format(date);
                    preferences.edit().putString("sync_time", sync_time)
                            .putInt("sync_num", strs.length).commit();

                    textTvDelete.setText("发送好友到TV");
                    ToastUtil.ShowToast_long(UserDetailActivity.this, "删除好友成功！");
                    BussinessManger.getInstance(UserDetailActivity.this).notifyTvBatchDelFrends(Long.parseLong(finalUid), ids, Integer.parseInt("11" + MainApplication.mTvInfoBean.getUid()));
                } else {
                    ToastUtil.ShowToast_long(UserDetailActivity.this, String.valueOf(obj));
                }

            }

        });
    }

    protected void sendFriendToTv(final String fuid) {
        final String mFuid = fuid.substring(2, fuid.length());
        WaitDialogBuilder.showNormalWithHintProgress(UserDetailActivity.this);

        BussinessManger.getInstance(this).syncFrendsBatchToTv("[" + mFuid + "]", MainApplication.mTvInfoBean.getUid(), currentUid, BussinessManger.CHANNEL, "0", new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                WaitDialogBuilder.dismissDialog();
                WaitDialogBuilder.clearWaitDialog();
                if (isSuccess) {
                    textTvDelete.setText("从TV端删除");
                    ToastUtil.ShowToast_long(UserDetailActivity.this, "发送好友成功！");
                    notyfyTv(fuid, ConstantParams.MESSAGE_TYPE_FRIEND_ADD);
                    Intent intent = new Intent();
                    intent.setAction(HTab3.EXTRA_SEND_TO_TV);
                    sendBroadcast(intent);
                } else {
                    ToastUtil.ShowToast_long(UserDetailActivity.this, String.valueOf(obj));
                }

            }
        });

    }

    //通知TV刷新
    protected void notyfyTv(String toId, int type) {
        BussinessManger.getInstance(this).notifyTv(type, -1, -1, Long.parseLong(toId), Long.parseLong("11" + MainApplication.mTvInfoBean.getUid()));
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case TYPE_UPDATE_REMARK:
                    WaitDialogBuilder.dismissDialog();
                    WaitDialogBuilder.clearWaitDialog();

                    Intent intent = new Intent();
                    intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                    intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    intent.putExtra("modifiedUser", mUser.getmUserId());
                    sendBroadcast(intent);

                    textRemark.setText(mUser.getCommentName());
                    ToastUtil.ShowToast_short(UserDetailActivity.this, "修改成功");
                    break;

                case DELETE_CONTACT_USER:
                    JNIResponse response = (JNIResponse) msg.obj;
                    if (response.getResult() == JNIResponse.Result.SUCCESS) {

                        Intent it = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
                        it.addCategory(PublicIntent.DEFAULT_CATEGORY);
                        ConversationNotificationObject obj = new ConversationNotificationObject(
                                Conversation.TYPE_VERIFICATION_MESSAGE, Conversation.SPECIFIC_VERIFICATION_ID, false);
                        it.putExtra("obj", obj);
                        sendBroadcast(it);

                        WaitDialogBuilder.dismissDialog();
                        WaitDialogBuilder.clearWaitDialog();
                        Toast.makeText(UserDetailActivity.this, "删除成功", Toast.LENGTH_SHORT)
                                .show();
//                        finish();
                        onBackPressed();
                    } else if (response.getResult() == JNIResponse.Result.TIME_OUT) {
                        Toast.makeText(UserDetailActivity.this, getString(R.string.contacts_delete_net_failed), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(UserDetailActivity.this, getString(R.string.contacts_delete_failed), Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TYPE_UPDATE_COMMENT) {
            if (resultCode == RESULT_OK) {
                textRemark.setText(mUser.getCommentName());
            }
        }
    }

    /**
     * 发起视频聊天
     *
     * @param remoteUserId 对方的id
     */
    private void startVideoCall(long remoteUserId) {
        if (GlobalHolder.getInstance().checkServerConnected(this)) {
            ToastUtil.ShowToast_long(this, "服务器连接异常,请稍后再试");
            return;
        }
        GlobalConfig.startP2PConnectChat(this, ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId, false,
                null, null);
    }

    public void hideSoft(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }
}
