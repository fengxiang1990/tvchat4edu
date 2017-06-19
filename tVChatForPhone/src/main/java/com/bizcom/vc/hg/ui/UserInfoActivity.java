package com.bizcom.vc.hg.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.MainApplication;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.LoginActivity;
import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vc.widget.MProgressDialog;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by admin on 2016/12/8.
 */

public class UserInfoActivity extends CameraWithNoUploadActivity implements BitmapManager.BitmapChangedListener {

    String tag = "UserInfoActivity";

    Unbinder unbinder;

    @BindView(R.id.img_back)
    ImageView img_back;

    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.text_nickname)
    TextView text_nickname;

    @BindView(R.id.img_header2)
    SimpleDraweeView img_header2;


    public static final String UPDATE_USER_HEADER_IMG = "com.tvl.update.header";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setNeedBroadcast(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        unbinder = ButterKnife.bind(this);
        text_title.setText("个人信息");
        img_back.setImageResource(R.mipmap.back_left_white);
        BitmapManager.getInstance().registerBitmapChangedListener(this);
    }

    @Override
    public void onCamera(Bitmap bitmap) {
        WaitDialogBuilder.showNormalWithHintProgress(mContext);
        byte[] bitmap2Bytes = BitmapUtil.Bitmap2Bytes(bitmap);
        Log.e("receiveMessage", "bitmap2Bytes-->" + bitmap2Bytes.length);
        if (!GlobalHolder.getInstance().checkServerConnected(mContext)) {
            V2ImRequest.invokeNative(V2ImRequest.NATIVE_CHANGE_OWNER_AVATAR, bitmap2Bytes,
                    bitmap2Bytes.length, ".png");
        }
    }

    @OnClick(R.id.img_modify_photo)
    void img_modify_photo() {
        popSelectPicture.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @OnClick(R.id.img_header2)
    void showPicSelection() {
        popSelectPicture.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(tag, "onResume");
        User user = GlobalHolder.getInstance().getCurrentUser();
        Log.e(tag, "path-->" + user.getAvatarPath());
        Log.e(tag, "location-->" + user.getmAvatarLocation());

        if (user != null) {
            text_nickname.setText(user.getDisplayName());
            UserHeaderImgHelper.display(img_header2, user);
        }
    }


    @OnClick(R.id.img_qrcode)
    void qrcode() {
        startActivity(new Intent(mContext, QRCodeActivity.class));
    }

    @OnClick(R.id.img_modify_nickname)
    void modifyName() {
        Intent intent = new Intent(mContext, UpdateNickNameActivity.class);
        startActivity(intent);
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(JNIService.JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        String action = intent.getAction();
        if (JNIService.JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION.equals(action)) {
//            if (mProgressDialog != null) {
//                mProgressDialog.dismiss();
//            }

            Log.i("tvliao", "userInfo-JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION");
        }
    }

    @Override
    public void receiveMessage(Message msg) {

    }

    MProgressDialog mProgressDialog;

    @OnClick(R.id.btn_logout)
    void logout() {
        AlertMsgUtils.showConfirm(this, "确定", "取消", "是否退出应用?", new AlertMsgUtils.OnDialogBtnClickListener() {
            @Override
            public void onConfirm(Dialog dialog) {
                dialog.dismiss();
                if (mProgressDialog == null) {
                    mProgressDialog = new MProgressDialog(UserInfoActivity.this);
                    mProgressDialog.setMessage(getText(R.string.setting_logouting).toString());
                }
                mProgressDialog.show();
                getSharedPreferences("tvl", MODE_PRIVATE).edit().putBoolean("isLogin", false).commit();
                V2ImRequest.invokeNative(V2ImRequest.NATIVE_CANNEL_LOGIN, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(tag, "onDestroy");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }

    @Override
    public void initViewAndListener() {

    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }


    @Override
    public void notifyAvatarChanged(final User user, Bitmap bm) {
        if (user.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   UserHeaderImgHelper.display(img_header2, user);
                   WaitDialogBuilder.dismissDialog();
                   sendBroadcast(new Intent(UPDATE_USER_HEADER_IMG));
               }
           });

        }

    }
}
