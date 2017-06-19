package com.bizcom.vc.hg.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
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

public class UpdateNickNameActivity extends BaseActivity {

    String tag = "UpdateNickNameActivity";
    Unbinder unbinder;
    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.text_right1)
    TextView text_right1;

    @BindView(R.id.edit_nickname)
    EditText editText;

    int type = 0; //0 用户  1 TV
    String tvname;
    String tvuid;

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
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case NickNamePhotoSettingActivity.UPDATE_NICKNAME:
                JNIResponse rlr = (JNIResponse) msg.obj;
                V2Log.d("JNIResponse -->" + rlr.getResult().name());
                if (rlr.getResult() != JNIResponse.Result.SUCCESS) {
                    Toast.makeText(mContext, "修改昵称失败，请重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (rlr.getResult() == JNIResponse.Result.SUCCESS) {
                    BussinessManger.getInstance(mContext).updateNickNameByUid(new IBussinessManager.OnResponseListener() {
                        @Override
                        public void onResponse(boolean isSuccess, int what, Object obj) {
                            if (isSuccess) {
                                Toast.makeText(mContext, "修改昵称成功", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(mContext, "修改昵称失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, String.valueOf(editText.getText()), user.getTvlUid());
                }

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

    V2ImRequest imService = new V2ImRequest();

    @OnClick(R.id.text_right1)
    void save() {
        String nickName = String.valueOf(editText.getText());
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(mContext, "昵称不能为空", Toast.LENGTH_SHORT).show();
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

        if (type == 1) {
            updateTVNickName();
        } else if (type == 0) {
            Log.d(tag, "user-->" + user);
            if (user != null) {
                user.setNickName(nickName);
                imService.updateUserInfo(user, new HandlerWrap(mHandler, NickNamePhotoSettingActivity.UPDATE_NICKNAME, null));
            }
        }

    }


    User user;

    @Override
    public void initViewAndListener() {
        setContentView(R.layout.activity_update_nickname);
        unbinder = ButterKnife.bind(this);
        type = getIntent().getIntExtra("type", 0);
        tvname = getIntent().getStringExtra("tvname");
        tvuid = getIntent().getStringExtra("tvuid");
        text_title.setText("昵称");
        text_right1.setVisibility(View.VISIBLE);
        user = GlobalHolder.getInstance().getCurrentUser();
        if (type == 0) {
            if (user != null) {
                editText.setText(user.getDisplayName());
            }
        } else if (type == 1) {
            editText.setText(tvname);
        }

    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }


    void updateTVNickName() {
        BussinessManger.getInstance(UpdateNickNameActivity.this).updateNickNameByUid(new IBussinessManager.OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    if (isSuccess) {
                        Toast.makeText(mContext, "修改昵称成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(mContext, "修改昵称失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        }, editText.getText().toString(), tvuid);
    }
}
