package com.bizcom.vc.activity.crow;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

/**
 * <ul>
 * Intent key:<br>
 * cid : discussion board id<br>
 * </ul>
 *
 * @author jiangzhen
 * @see PublicIntent#SHOW_DISCUSSION_BOARD_TOPIC_ACTIVITY
 */
public class DiscussionBoardTopicUpdateActivity extends BaseActivity {

    private final static int REQUEST_UPDATE_CROWD_DONE = 1;

    private ClearEditText mContentET;
    private DiscussionGroup crowd;
    private V2CrowdGroupRequest service = new V2CrowdGroupRequest();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setNeedBroadcast(true);
        setNeedHandler(true);
        setNeedAvatar(false);
        setContentView(R.layout.discussion_board_topic_update_activity);
        super.onCreate(savedInstanceState);
        crowd = (DiscussionGroup) GlobalHolder.getInstance().getGroupById(V2GlobalConstants.GROUP_TYPE_DISCUSSION,
                getIntent().getExtras().getLong("cid"));

        mContentET.setText(crowd.getName());
        if (mContentET.getText() != null) {
            mContentET.setSelection(mContentET.getText().length());
        }
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        if (intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
            GroupUserObject obj = intent.getParcelableExtra("group");
            if (obj == null) {
                return;
            }

            if (obj.getmGroupId() == crowd.getGroupID()) {
                // 这里不能掉onBackPressed(),会出现异常！
                finish();
            }
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case REQUEST_UPDATE_CROWD_DONE:
                WaitDialogBuilder.dismissDialog();
                Toast.makeText(DiscussionBoardTopicUpdateActivity.this, R.string.crowd_content_udpate_succeed,
                        Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    public void initViewAndListener() {
        View functionLy = findViewById(R.id.ws_activity_main_title_functionLy);
        if (functionLy != null) {
            functionLy.setVisibility(View.INVISIBLE);
        }
        TextView mTitleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContent.setText(getResources().getString(R.string.discussion_board_topic_title));
        TextView mUpdateButton = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
        mUpdateButton.setText(getResources().getString(R.string.discussion_board_topic_udpate_button));
        mUpdateButton.setOnClickListener(mUpdateButtonListener);
        TextView mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        mReturnButton.setText(getResources().getString(R.string.common_back));
        mReturnButton.setOnClickListener(mReturnButtonListener);

        mContentET = (ClearEditText) findViewById(R.id.dicussion_board_topic_et);
        mContentET.setTag(ClearEditText.TAG_IS_PATTERN);
        mContentET.setHasShowClear(false);
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.clearCalledBack();
    }

    private OnClickListener mReturnButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private OnClickListener mUpdateButtonListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                return;
            }
            WaitDialogBuilder.showNormalWithHintProgress(mContext);
            String content = mContentET.getText().toString();
            crowd.setName(content);
            service.updateDiscussion(crowd, new HandlerWrap(mHandler, REQUEST_UPDATE_CROWD_DONE, null));
        }

    };

}
