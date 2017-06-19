package com.bizcom.vc.hg.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by admin on 2016/12/8.
 */

public class UpdateUserCommentNameActivity extends BaseActivity {

    private final int TYPE_UPDATE_REMARK = 3;

    Unbinder unbinder;
    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.text_right1)
    TextView text_right1;

    @BindView(R.id.edit_nickname)
    EditText editText;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void addBroadcast(IntentFilter filter) {

    }

    @Override
    public void receiveBroadcast(Intent intent) {

    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageUtil.showKeyBoard(editText);
            }
        },250);
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case TYPE_UPDATE_REMARK:
                WaitDialogBuilder.dismissDialog();
                Intent intent = new Intent();
                intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                intent.putExtra("modifiedUser", user.getmUserId());
                sendBroadcast(intent);

                setResult(RESULT_OK);
                finish();

                ToastUtil.ShowToast_short(UpdateUserCommentNameActivity.this, "修改成功");

                break;
        }
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }

    @OnClick(R.id.img_clear)
    void clear() {
        editText.setText("");
    }


    @OnClick(R.id.text_right1)
    void save() {
        String nickName = String.valueOf(editText.getText());
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(mContext, "备注不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //匹配中文
        String regexStr = "[\u4E00-\u9FA5]";
        Pattern p = Pattern.compile(regexStr);
        Matcher m = p.matcher(nickName);
        if (m.find()) {
            if (nickName.length() > 12) {
                Toast.makeText(mContext, "只能输入12个汉字或24个字母", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (nickName.length() > 24) {
                Toast.makeText(mContext, "只能输入12个汉字或24个字母", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        updateUserComment();

    }

    private void updateUserComment() {
        WaitDialogBuilder.showNormalWithHintProgress(UpdateUserCommentNameActivity.this);
        user.setCommentName(editText.getText().toString());
        MessageUtil.hideKeyBoard(UpdateUserCommentNameActivity.this, editText.getWindowToken());
        new V2ImRequest().updateUserInfo(user, new HandlerWrap(mHandler, TYPE_UPDATE_REMARK, null));
    }

    @Override
    public void initViewAndListener() {
        setContentView(R.layout.activity_update_nickname);
        unbinder = ButterKnife.bind(this);
        text_title.setText("备注");
        text_right1.setVisibility(View.VISIBLE);
        long userId = getIntent().getLongExtra(UserDetailActivity.EXTRA_USER, -1);
        user = GlobalHolder.getInstance().getUser(userId);

        if (user != null) {
            editText.setText(user.getCommentName());
        }

    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

}
