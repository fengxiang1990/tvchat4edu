package com.bizcom.vc.activity.contacts;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.message.MessageAuthenticationActivity;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.FriendGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.List;

public class InputRemarkActivity extends BaseActivity {
    private static final int SELECT_GROUP_REQUEST_CODE = 0;
    private static final int ADD_CONTACT_USER = 1;

    // for control
    // R.id.right_text_view
    private TextView tvRightTextView;
    // R.id.tv_back
    private TextView tvBack;
    // R.id.select_group
    private RelativeLayout rlSelectGroup;
    // R.id.comment_name_et
    private ClearEditText commentNameET;
    // R.id.tv_group_name
    private TextView tvGroupName;

    private long mUid;
    private User detailUser;
    private String verificationInfo;
    private String startedCause;
    private V2ContactsRequest contactService = new V2ContactsRequest();
    private String selectGroupName;
    private long selectGroupID;
    private String fromActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setNeedAvatar(false);
        setNeedHandler(true);
        setNeedBroadcast(false);
        setContentView(R.layout.activity_contact_add_friend_management);
        super.onCreate(savedInstanceState);
        WaitDialogBuilder.clearWaitDialog();
        List<Group> listFriendGroup = GlobalHolder.getInstance().getGroup(
                V2GlobalConstants.GROUP_TYPE_CONTACT);
        selectGroupName = listFriendGroup.get(0).getName();
        selectGroupID = listFriendGroup.get(0).getGroupID();
        startedCause = this.getIntent().getStringExtra("cause");
        if ((startedCause != null)
                && startedCause.equals("access_friend_authentication")) {
            mUid = this.getIntent().getLongExtra("remoteUserID", 0);
            tvRightTextView
                    .setText(R.string.friendManagementActivity_titlebar_right_text);
            tvBack.setText(R.string.friendManagementActivity_titlebar_left_text);
            // 别人加我，我需要验证的情况
            fromActivity = "ContactDetail";
        } else if ((startedCause != null)
                && startedCause.equals("ContactDetail2")) {
            mUid = this.getIntent().getLongExtra("uid", 0);
            verificationInfo = this.getIntent().getStringExtra(
                    "verificationInfo");
            tvRightTextView
                    .setText(R.string.friendManagementActivity_titlebar_right_text1);
            tvBack.setText(R.string.friendManagementActivity_titlebar_left_text1);
            // 我加别人，别人不需要验证的情况
            fromActivity = "ContactDetail2";
        } else {
            mUid = this.getIntent().getLongExtra("uid", 0);
            verificationInfo = this.getIntent().getStringExtra(
                    "verificationInfo");
            tvRightTextView
                    .setText(R.string.friendManagementActivity_titlebar_right_text1);
            tvBack.setText(R.string.friendManagementActivity_titlebar_left_text2);
            // 我加别人，别人需要验证的情况
            fromActivity = "InputAuthenticationActivity";
        }
        detailUser = GlobalHolder.getInstance().getUser(mUid);
        commentNameET.setHint(detailUser.getDisplayName());
        tvGroupName.setText(selectGroupName);
    }

    private void hindSoftInput(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && v != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactService.clearCalledBack();
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
            case ADD_CONTACT_USER:
                WaitDialogBuilder.dismissDialog();
                WaitDialogBuilder.clearWaitDialog();
                JNIResponse res = (JNIResponse) msg.obj;
                if (res.getResult() == JNIResponse.Result.SUCCESS) {
                    if (detailUser.getAuthtype() == 0) {
                        AddFriendHistroysHandler
                                .addOtherNoNeedAuthentication(
                                        getApplicationContext(), detailUser);
                        Toast.makeText(
                                InputRemarkActivity.this,
                                R.string.contacts_detail2_added_successfully,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(
                            InputRemarkActivity.this,
                            R.string.contacts_detail2_added_failed,
                            Toast.LENGTH_SHORT).show();
                }

                // 实现越级跳
                Intent i = new Intent(InputRemarkActivity.this,
                        ContactDetail2.class);
                i.putExtra("nickName", commentNameET.getText().toString());
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
        }
    }

    @Override
    public void initViewAndListener() {
        connectView();
        bindViewEvent();
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    private void connectView() {
        tvRightTextView = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
        tvRightTextView.setText(getResources().getString(
                R.string.crowd_application_send_button));
        tvBack = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        tvBack.setText(getResources().getString(
                R.string.friendManagementActivity_titlebar_left_text2));
        TextView titleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
        titleContent.setText(getResources().getString(
                R.string.contacts_update_group_add_friends));
        findViewById(R.id.ws_activity_main_title_functionLy).setVisibility(View.INVISIBLE);

        rlSelectGroup = (RelativeLayout) findViewById(R.id.select_group);
        rlSelectGroup.setVisibility(View.GONE);
        commentNameET = (ClearEditText) findViewById(R.id.comment_name_et);
        tvGroupName = (TextView) findViewById(R.id.tv_group_name);
    }

    private void bindViewEvent() {
        findViewById(R.id.layout).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hindSoftInput(v);
                return false;
            }
        });


        // 返回
        tvBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        // 选择分组
        rlSelectGroup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent();
                i.setClass(InputRemarkActivity.this,
                        SelectJionGroupActivity.class);
                i.putExtra("from", "addFriend");
                i.putExtra("groupID", selectGroupID);
                startActivityForResult(i, 0);
            }
        });

        // 发送
        tvRightTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageUtil.hideKeyBoard(InputRemarkActivity.this, commentNameET.getWindowToken());
                if (GlobalHolder.getInstance().isServerConnected()) {
                    if (fromActivity.equals("ContactDetail")) {
                        contactService.acceptAddedAsContact(selectGroupID, mUid);
                        // 实现越级跳
                        MessageUtil.hideKeyBoard(InputRemarkActivity.this, commentNameET.getWindowToken());
                        Intent i = new Intent(InputRemarkActivity.this,
                                MessageAuthenticationActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

                        User user = GlobalHolder.getInstance().getUser(detailUser
                                .getmUserId());
                        user.setCommentName(commentNameET.getText().toString());

                        Intent intent = new Intent();
                        intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                        intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                        intent.putExtra("modifiedUser", detailUser.getmUserId());
                        sendBroadcast(intent);
                    } else {
                        if (detailUser.getAuthtype() == 0) {
                            WaitDialogBuilder.showNormalWithHintProgress(mContext);
                            contactService.addContact(new FriendGroup(
                                            selectGroupID, ""), detailUser, "",
                                    commentNameET.getText().toString(), new HandlerWrap(mHandler, ADD_CONTACT_USER, null));
                        } else if (detailUser.getAuthtype() == 1) {
                            // 检测一下是否已经成为好友
                            boolean isFriend = GlobalHolder.getInstance()
                                    .isFriend(detailUser.getmUserId());
                            if (!isFriend) {
                                AddFriendHistroysHandler
                                        .addOtherNeedAuthentication(
                                                getApplicationContext(),
                                                detailUser, verificationInfo,
                                                true);
                                contactService.addContact(new FriendGroup(
                                                selectGroupID, ""), detailUser,
                                        verificationInfo, commentNameET
                                                .getText().toString(), null);
                                Intent i = new Intent();
                                i.setAction(PublicIntent.BROADCAST_ADD_OTHER_FRIEND_WAITING_NOTIFICATION);
                                i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                                sendBroadcast(i);

                                Toast.makeText(
                                        InputRemarkActivity.this,
                                        R.string.friendManagementActivity_toast_text_applySentSuccess,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                        InputRemarkActivity.this,
                                        R.string.authenticationActivity_send_success_hint,
                                        Toast.LENGTH_SHORT).show();
                            }

                            // 实现越级跳
                            Intent i = new Intent(InputRemarkActivity.this,
                                    ContactDetail2.class);
                            i.putExtra("nickName", commentNameET.getText().toString());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else if (detailUser.getAuthtype() == 2) {
                            // 不让任何人加为好
                            Toast.makeText(
                                    InputRemarkActivity.this,
                                    R.string.friendManagementActivity_toast_text_reduseAddAsFriend,
                                    Toast.LENGTH_SHORT).show();
                            // 实现越级跳
                            Intent i = new Intent(InputRemarkActivity.this,
                                    ContactDetail2.class);
                            i.putExtra("nickName", commentNameET.getText().toString());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }
                } else {
                    Toast.makeText(
                            InputRemarkActivity.this,
                            R.string.friendManagementActivity_toast_text_applySentFail,
                            Toast.LENGTH_SHORT).show();
                    // 实现越级跳
                    Intent i = new Intent(InputRemarkActivity.this,
                            ContactDetail2.class);
                    i.putExtra("nickName", commentNameET.getText().toString());
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_GROUP_REQUEST_CODE) {
            if (resultCode == SelectJionGroupActivity.SELECT_GROUP_RESPONSE_CODE_DONE) {
                if (data != null) {
                    selectGroupName = data.getStringExtra("groupName");
                    selectGroupID = data.getLongExtra("groupID", 0);
                    tvGroupName.setText(selectGroupName);
                }
            } else if (resultCode == SelectJionGroupActivity.SELECT_GROUP_RESPONSE_CODE_CANCEL) {
            }
        }

    }

}
