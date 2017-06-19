package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.main.TabFragmentOrganization;
import com.bizcom.vc.hg.bean.MessageBean;
import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.hg.web.interf.BaseResponse;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.ErrorResponse;
import com.bizcom.vc.hg.web.interf.SimpleResponseListener;
import com.bizcom.vc.hg.web.models.UserCenterCallLong;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PersonalCenterActivity extends Activity {

    String tag = "PersonalCenterActivity";
    private PersonalCenterActivity mContext;
    private TextView tv_name_icon;
    private TextView tv_phone_num;
    private LocalReceiver localReceiver;

    Unbinder unbinder;

    @BindView(R.id.img_back)
    ImageView img_back;

    @BindView(R.id.user_head_icon)
    SimpleDraweeView user_head_icon;

    @BindView(R.id.text_friend_num)
    TextView text_friend_num;

    @BindView(R.id.text_talk_minute_num)
    TextView text_talk_minute_num;

    @BindView(R.id.text_contact_always_num)
    TextView text_contact_always_num;

    @BindView(R.id.text_version)
    TextView text_version;

    @BindView(R.id.text_msg_num)
    TextView text_msg_num;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hpersonal_center);
        unbinder = ButterKnife.bind(this);
        initReceiver();
        mContext = this;
        initView();
    }

    private void initReceiver() {
        localReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        filter.addAction(JNIService.JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION);
        registerReceiver(localReceiver, filter);
    }

    private void initView() {
        tv_name_icon = (TextView) findViewById(R.id.tv_name_icon);
        tv_phone_num = (TextView) findViewById(R.id.tv_phone_num);
        tv_name_icon.setText(GlobalHolder.getInstance().getCurrentUser().getDisplayName() + "");
        tv_phone_num.setText(GlobalHolder.getInstance().getCurrentUser().getMobile() + "");
        img_back.setImageResource(R.mipmap.back_left_white);
        String pkgName = getPackageName();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(pkgName, 0);
            if (packageInfo != null) {
                String versionName = packageInfo.versionName;
                text_version.setText("版本号:" + versionName);
            }
        } catch (Exception e) {
            text_version.setText("未获取到版本信息");
            e.printStackTrace();
        }

    }


    @OnClick(R.id.img_next)
    void next() {
        Intent intent = new Intent(mContext, UserInfoActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ll_edit)
    void edit() {
        Intent intent = new Intent(mContext, UserInfoActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }


    @OnClick(R.id.ll_modify_pwd)
    void modifyPwd() {
        changePwd();
    }

    @OnClick(R.id.ll_message)
    void messageClick() {
        message();
    }

    @OnClick(R.id.ll_help)
    void helpClick() {
        guid("使用帮助");
    }

    @OnClick(R.id.ll_about)
    void aboutClick() {
        aboutUs("关于我们");
    }

    //客服热线
    @OnClick(R.id.ll_customer_service)
    void customerServiceClick() {
        final String tel = "4001014561";
        String telStr = "客服热线:400-1014-561";
        AlertMsgUtils.showConfirm(mContext, "拨打", "取消", telStr, new AlertMsgUtils.OnDialogBtnClickListener() {
            @Override
            public void onConfirm(Dialog dialog) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + tel);
                intent.setData(data);
                startActivity(intent);
            }
        });
    }


    protected void guid(String text) {

        Intent i = new Intent(mContext, AboutUsActivity.class);
        i.putExtra("titleText", text);
        i.putExtra("webUrl", LinkInfo.HELP_CODE);
        mContext.startActivity(i);

    }

    protected void aboutUs(String text) {
        Intent i = new Intent(mContext, AboutUsActivity.class);
        i.putExtra("titleText", text);
        i.putExtra("webUrl", LinkInfo.H5_CODE);
        mContext.startActivity(i);

    }

    protected void message() {
        Intent i = new Intent(mContext, HgMessageActivity.class);
        i.putExtra("titleText", "消息");
        mContext.startActivity(i);

    }

    protected void set() {
        Intent i = new Intent(mContext, Setp.class);
        mContext.startActivity(i);

    }

    protected void changePwd() {
        Intent i = new Intent(mContext, ChangePwdActivity.class);
        i.putExtra("titleText", "修改密码");
        mContext.startActivity(i);

    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        try {
            unregisterReceiver(localReceiver);
        } catch (IllegalArgumentException e) {
        }
    }


    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (JNIService.JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION.equals(action)) {
                LocalSharedPreferencesStorage.putBooleanValue(mContext, "isAutoLogin", false);
                WaitDialogBuilder.clearWaitDialog();
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10:
                    int noReadSize = 0;
                    for (MessageBean messageBean : messageAllList) {
                        if (messageBean.getState() == 1) {
                            noReadSize++;
                        }
                    }
                    if (noReadSize == 0) {
                        text_msg_num.setVisibility(View.INVISIBLE);
                    } else {
                        text_msg_num.setVisibility(View.VISIBLE);
                        text_msg_num.setText(noReadSize + "");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        user = GlobalHolder.getInstance().getCurrentUser();
        String name = user.getDisplayName();
        tv_name_icon.setText(name);
        tv_phone_num.setText(user.getAccount());
        UserHeaderImgHelper.display(user_head_icon, user);
        getMessages();
        String uid = user.getTvlUid();
        BussinessManger.getInstance(this).getUserCenterCallLong(uid, "", new SimpleResponseListener<BaseResponse<UserCenterCallLong>>() {
            @Override
            protected void onSuccess(BaseResponse<UserCenterCallLong> response) {
                if (response.code.equals("0000")) {
                    UserCenterCallLong centerCallLong = response.data;
                    if (centerCallLong != null) {
                        text_contact_always_num.setText(centerCallLong.touch);
                        text_talk_minute_num.setText(centerCallLong.conversation);
                    }
                }
            }

            @Override
            protected void onError(ErrorResponse response) {
                Toast.makeText(PersonalCenterActivity.this, response.message, Toast.LENGTH_SHORT).show();
            }
        });
        try {
            List<Group> mContactGroupList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
            if (mContactGroupList != null && mContactGroupList.size() > 0) {
                List<User> users = mContactGroupList.get(0).getUsers();
//                for (int i = 0; i < users.size(); i++) {
//                    User user = users.get(i);
//                    Log.e(tag,"user-->"+user.toString());
//                }
                text_friend_num.setText("" + users.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //	/**
    //	 * 获取消息
    //	 */

    List<MessageBean> messageAllList = new ArrayList<MessageBean>();

    private void getMessages() {
        new Thread() {
            public void run() {
                try {
                    messageAllList.clear();
                    MessageBean mb;
                    // 获取视频通话消息
                    List<AudioVideoMessageBean> videoMessageBeans = MediaRecordProvider.loadMediaHistoriesMessage(
                            GlobalHolder.getInstance().getCurrentUserId(), AudioVideoMessageBean.TYPE_ALL);
                    for (AudioVideoMessageBean vm : videoMessageBeans) {
                        if (vm.name == null) {
                            break;
                        }
                        for (AudioVideoMessageBean.ChildMessageBean childMessageBean : vm.mChildBeans) {
                            mb = new MessageBean();
                            mb.setMessageType(MessageBean.MESSAGE_TYPE_VIDEO);
                            mb.setId(childMessageBean.messageId);
                            mb.setName(vm.name);
                            mb.setRemoteUserID(vm.remoteUserID);
                            mb.setCallDuration(childMessageBean.childHoldingTime);
                            mb.setHandleDate(childMessageBean.childSaveDate);
                            mb.setCallType(childMessageBean.childISCallOut);
                            mb.setState(childMessageBean.childReadState);
                            mb.isCancelByMine = childMessageBean.isCancelByMine;
                            messageAllList.add(mb);// 已读消息直接追加
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(10);
            }
        }.start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
}
