package com.bizcom.vc.activity.crow;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.DialogManager;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vo.DiscussionGroup;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

/**
 * <ul>
 * Display discussion board detail.
 * </ul>
 * <ul>
 * Intent key: cid : discussion board id
 * </ul>
 *
 * @author 28851274
 * @see PublicIntent#SHOW_DISCUSSION_BOARD_DETAIL_ACTIVITY
 */
public class DiscussionBoardDetailActivity extends Activity {

    private final static int TYPE_UPDATE_MEMBERS = 3;
    private final static int REQUEST_QUIT_CROWD_DONE = 2;

    private TextView mNameTV;
    private TextView mMembersCountsTV;

    private View mQuitButton;
    private View mShowTopicButton;
    private View mShowMembersButton;
    private TextView mReturnButton;

    private LocalReceiver localReceiver;
    private DiscussionGroup crowd;
    private V2CrowdGroupRequest service = new V2CrowdGroupRequest();

    private Context mContext;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussion_board_detail_activity);
        View functionLy = findViewById(R.id.ws_activity_main_title_functionLy);
        if (functionLy != null) {
            functionLy.setVisibility(View.INVISIBLE);
        }
        mContext = this;
        TextView titleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
        titleContent.setText(getResources().getString(R.string.discussion_board_detail_settings));
        mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        mReturnButton.setText(getResources().getString(R.string.common_back));
        mReturnButton.setOnClickListener(mReturnButtonListener);
        findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);

        mNameTV = (TextView) findViewById(R.id.discussion_board_detail_name);
        mMembersCountsTV = (TextView) findViewById(R.id.discussion_board_detail_members);

        mQuitButton = findViewById(R.id.discussion_board_detail_button);
        mQuitButton.setOnClickListener(mQuitButtonListener);

        mShowTopicButton = findViewById(R.id.discussion_board_detail_update_name_button);
        mShowTopicButton.setOnClickListener(mTopicButtonListener);

        mShowMembersButton = findViewById(R.id.discussion_board_detail_invitation_members_button);
        mShowMembersButton.setOnClickListener(mShowMembersButtonListener);

        crowd = (DiscussionGroup) GlobalHolder.getInstance().getGroupById(V2GlobalConstants.GROUP_TYPE_DISCUSSION,
                getIntent().getLongExtra("cid", 0));

        mNameTV.setText(crowd.getName());

        mMembersCountsTV.setText(crowd.getUsers().size() + "");
        initReceiver();
    }

    private void initReceiver() {
        localReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_UPDATED);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
        filter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
        filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
        this.registerReceiver(localReceiver, filter);
    }

    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_UPDATED)) {
                long crowdId = intent.getLongExtra("gid", 0);
                // Update content
                if (crowdId == crowd.getGroupID()) {
                    DiscussionGroup crowd = (DiscussionGroup) GlobalHolder.getInstance()
                            .getGroupById(V2GlobalConstants.GROUP_TYPE_DISCUSSION, crowdId);
                    if (crowd != null) {
                        mNameTV.setText(crowd.getName());
                        mMembersCountsTV.setText(crowd.getUsers().size() + "");
                    }
                }
            } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_USER_ADDED)) {
                GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
                if (guo.getmGroupId() == crowd.getGroupID()) {
                    DiscussionGroup newGroup = (DiscussionGroup) GlobalHolder.getInstance()
                            .getGroupById(V2GlobalConstants.GROUP_TYPE_DISCUSSION, crowd.getGroupID());
                    if (newGroup != null) {
                        crowd = newGroup;
                        mMembersCountsTV.setText(String.valueOf(crowd.getUsers().size()));
                        mNameTV.setText(String.valueOf(crowd.getName()));
                    }
                }
            } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED)) {
                DiscussionGroup newGroup = (DiscussionGroup) GlobalHolder.getInstance()
                        .getGroupById(V2GlobalConstants.GROUP_TYPE_DISCUSSION, crowd.getGroupID());
                if (newGroup != null) {
                    crowd = newGroup;
                    mMembersCountsTV.setText(String.valueOf(crowd.getUsers().size()));
                    mNameTV.setText(String.valueOf(crowd.getName()));
                }
            } else if (intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mNameTV.setText(crowd.getName());
        mMembersCountsTV.setText(crowd.getUsers().size() + "");
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null) {
            mDialog.dismiss();
        }

        service.clearCalledBack();
        unregisterReceiver(localReceiver);
        super.onDestroy();
    }

    private void showDialog() {
        mDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mContext, null,
                        mContext.getText(R.string.discussion_board_detail_quit_confirm_title),
                        mContext.getText(R.string.activiy_contact_group_button_confirm),
                        mContext.getText(R.string.activiy_contact_group_button_cancel)) {

                    @Override
                    public void confirmCallBack() {
                        if (!GlobalHolder.getInstance().isServerConnected()) {
                            mDialog.dismiss();
                            Toast.makeText(DiscussionBoardDetailActivity.this, R.string.common_networkIsDisconnection_failed_no_network,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mDialog.dismiss();
                        service.quitDiscussionBoard(crowd,
                                new HandlerWrap(mLocalHandler, REQUEST_QUIT_CROWD_DONE, null));
                        WaitDialogBuilder.showNormalWithHintProgress(mContext);
                    }

                    @Override
                    public void cannelCallBack() {
                        mDialog.dismiss();
                    }
                });
        mDialog.show();
    }

    private void handleQuitDone() {
        // Remove cache crowd
        GlobalHolder.getInstance().removeGroup(V2GlobalConstants.GROUP_TYPE_DISCUSSION, crowd.getGroupID());
        onBackPressed();
    }

    private OnClickListener mQuitButtonListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            showDialog();
        }

    };

    private OnClickListener mReturnButtonListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            onBackPressed();
        }

    };

    private OnClickListener mTopicButtonListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            Intent i = new Intent(PublicIntent.SHOW_DISCUSSION_BOARD_TOPIC_ACTIVITY);
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.putExtra("cid", crowd.getGroupID());
            startActivityForResult(i, 100);
        }

    };

    private OnClickListener mShowMembersButtonListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            Intent i = new Intent(DiscussionBoardDetailActivity.this, GroupMemberActivity.class);
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.putExtra("cid", crowd.getGroupID());
            i.putExtra("memberType", GroupMemberActivity.GROUP_MEMBER_TYPE_DISCUSSION);
            startActivityForResult(i, TYPE_UPDATE_MEMBERS);
        }

    };

    private Handler mLocalHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_QUIT_CROWD_DONE:
                    WaitDialogBuilder.dismissDialog();
                    JNIResponse res = (JNIResponse) msg.obj;
                    if (res.getResult() == JNIResponse.Result.SUCCESS) {
                        handleQuitDone();
                    } else {
                        Toast.makeText(DiscussionBoardDetailActivity.this, R.string.error_discussion_board_quit_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    };

    enum State {
        NONE, PENDING;
    }

}
