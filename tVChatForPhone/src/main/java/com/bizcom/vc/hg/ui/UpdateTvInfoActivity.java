package com.bizcom.vc.hg.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.alibaba.fastjson.JSON;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.util.BitmapUtils;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.WebConfig;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by admin on 2016/12/13.
 */

public class UpdateTvInfoActivity extends CameraActivity implements CameraActivity.OnTvPhotoSelectedListener {


    String tag = "UpdateTvInfoActivity";
    Unbinder unbinder;

    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.img_header2)
    SimpleDraweeView img_header2;

    @BindView(R.id.text_nickname)
    TextView text_nickname;

    @BindView(R.id.text_tvnum)
    TextView text_tvnum;


    public static final String UNBIND_SUCCESS_RECEIVER = "com.hg.ui.txxx.unbind";
    public static final String UPDATE_SUCCESS_RECEIVER = "com.hg.ui.txxx.update";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_tv_info);
        unbinder = ButterKnife.bind(this);
        text_title.setText("TV信息");
        super.setListener(this);

    }

    @OnClick(R.id.img_modify_nickname)
    void updateNickName() {
        Intent intent = new Intent(this, UpdateNickNameActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("tvname", text_nickname.getText().toString());
        intent.putExtra("tvuid", mTvInfoBeans.getUid());
        startActivity(intent);
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }

    @OnClick(R.id.img_modify_photo)
    void img_modify_photo() {
        popSelectPicture.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @OnClick(R.id.img_header2)
    void showPicSelection() {
        popSelectPicture.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }


    @OnClick(R.id.btn_unbind)
    void unbind() {
        AlertMsgUtils.showConfirm(UpdateTvInfoActivity.this, "确定", "取消", "您确定与该TV用户解除绑定关系？", new AlertMsgUtils.OnDialogBtnClickListener() {

            @Override
            public void onConfirm(Dialog dialog) {
                dialog.dismiss();
                unBind();
            }
        });

    }

    @Override
    public void addBroadcast(IntentFilter filter) {

    }

    @Override
    public void receiveBroadcast(Intent intent) {

    }

    @Override
    public void receiveMessage(Message msg) {

    }

    User user;
    String uid;

    @Override
    public void initViewAndListener() {

    }

    @Override
    public void onResume() {
        super.onResume();
        user = GlobalHolder.getInstance().getCurrentUser();
        long userId = user.getmUserId();
        String userID = userId + "";
        if (userID.length() <= 2)
            return;
        uid = userID.substring(2, userID.length());
        doooo();
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    protected void notyfyTvUnBindOk(String tvUid) {
        BussinessManger.getInstance(mContext).notifyTv(ConstantParams.MESSAGE_TYPE_UNBIND, -1, -1, -1l, Long.parseLong(String.valueOf("11" + tvUid)));
    }

    protected void unBind() {

        BussinessManger.getInstance(UpdateTvInfoActivity.this).removeBingding(new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {

                if (isSuccess) {
                    notyfyTvUnBindOk(MainApplication.mTvInfoBean.getUid());
                    MainApplication.mTvInfoBean = null;
                    sendBroadcast(new Intent(UNBIND_SUCCESS_RECEIVER));

                    finish();
                }
                ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
            }
        }, uid, mTvInfoBeans.getTvId(), BussinessManger.CHANNEL);

    }

    TvInfoBeans mTvInfoBeans;

    public void doooo() {
        BussinessManger.getInstance(UpdateTvInfoActivity.this).queryTvByUid(new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    Log.d(tag, "queryTvByUid-->" + String.valueOf(obj));
                    mTvInfoBeans = JSON.parseObject(String.valueOf(obj), TvInfoBeans.class);
                    text_nickname.setText(mTvInfoBeans.getNickName());
                    text_tvnum.setText(mTvInfoBeans.getUserName());

                    //  long tvUid= Long.parseLong("11" + mTvInfoBeans.getUid());
                    // Log.e(tag,"tvUid-->"+tvUid);
                    // User user = GlobalHolder.getInstance().getUser(tvUid);
                    // Log.e(tag,"user-->"+user);
                    // if (user != null) {
                    //     Log.e(tag,"user account-->"+user.getAccount());
                    //     text_tvnum.setText(TextUtils.isEmpty(user.getAccount())?"":user.getAccount());
                    // }

                    if (!TextUtils.isEmpty(mTvInfoBeans.getPicurl())) {
                        img_header2.setImageURI(Uri.parse(mTvInfoBeans.getPicurl()));
                    }
                    Intent intent = new Intent(UPDATE_SUCCESS_RECEIVER);
                    sendBroadcast(intent);
                }

            }
        }, uid, WebConfig.channel);

    }

    Handler handler = new Handler();

    @Override
    public void onTvPhotoSelected(Bitmap bitmap) {
        String str = BitmapUtils.bitmapToBase64(bitmap);
        String fileName = java.lang.System.currentTimeMillis() + ".png";
        BussinessManger.getInstance(UpdateTvInfoActivity.this).updateTvPhoto(mTvInfoBeans.getUid(), fileName, str, WebConfig.channel, new IBussinessManager.OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                Toast.makeText(UpdateTvInfoActivity.this, String.valueOf(obj), Toast.LENGTH_SHORT).show();
                doooo();
            }
        });

    }

}
