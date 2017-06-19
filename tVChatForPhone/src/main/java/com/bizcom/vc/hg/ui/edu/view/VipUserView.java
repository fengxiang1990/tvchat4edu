package com.bizcom.vc.hg.ui.edu.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bizcom.request.V2ImRequest;
import com.bizcom.vc.hg.ui.edu.CourseInfoSingleton;
import com.bizcom.vc.hg.util.DataUtil;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vo.Attendee;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by admin on 2017/1/17.
 */

public class VipUserView extends LinearLayout {

    String tag = "VipUserView";
    Unbinder unbinder;
    Attendee attendee;

    public VipUserView(Context context) {
        super(context);
        init(context);
    }

    public VipUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @BindView(R.id.img_vip)
    SimpleDraweeView img_vip;

    @BindView(R.id.text_vip_name)
    TextView text_vip_name;

    @BindView(R.id.text_vip_status)
    TextView text_vip_status;

    @BindView(R.id.img_vip_status)
    ImageView img_vip_status;

    void init(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.vip_user_item, null);
        unbinder = ButterKnife.bind(this, view);
        addView(view);
        text_vip_name.setMaxEms(5);
        view.setClickable(true);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(tag, "onClick");
                if (attendee != null) {
                    Log.e(tag, "onClick" + attendee.getAttId());
                    sendMsg(context, ConstantParams.MESSAGE_TYPE_NOT_AGREE_SPEAK, attendee.getAttId());
                }
            }
        });


    }

    public void setOnLineText(int count) {
        text_vip_name.setText("普通席位(" + count + ")");
    }

    public void setNormalPlaceBtn() {
        text_vip_name.setMaxEms(5);
        text_vip_name.setText("普通席位(0)");
        img_vip.setImageURI("res:///" + R.mipmap.ordinary);
        text_vip_status.setVisibility(GONE);
        img_vip_status.setVisibility(GONE);
    }

    public void setAttendee(Attendee attendee) {
        text_vip_name.setMaxEms(4);
        this.attendee = attendee;
        if (attendee != null) {
            User user = attendee.getUser();
            user = GlobalHolder.getInstance().getUser(user.getmUserId());
            if (user != null) {
                //UserHeaderImgHelper.display(img_vip, user);
                if (!TextUtils.isEmpty(CourseInfoSingleton.getCourseInfo().getStudentImgPars().get(attendee.getAttId()))) {
                    img_vip.setImageURI(Uri.parse(CourseInfoSingleton.getCourseInfo().getStudentImgPars().get(attendee.getAttId())));
                }

                text_vip_name.setText(CourseInfoSingleton.getCourseInfo().getStudentNamePars().get(attendee.getAttId()));
            }
            if (attendee.isSpeaking()) {
                img_vip_status.setImageResource(R.mipmap.speak);
            } else {
                img_vip_status.setImageResource(R.mipmap.prohibition);
            }
            if (attendee.isJoined()) {
                text_vip_status.setVisibility(GONE);
                img_vip_status.setVisibility(VISIBLE);
            } else {
                text_vip_status.setVisibility(VISIBLE);
                img_vip_status.setVisibility(GONE);
            }
        }

        handler.post(new StatusCheckThread(attendee));
        //handler.post(new InvokeNotFrendRunnable(attendee));
    }


    Handler handler = new Handler();

    class InvokeNotFrendRunnable implements Runnable {
        Attendee attendee;

        public InvokeNotFrendRunnable(Attendee attendee) {
            this.attendee = attendee;
        }

        @Override
        public void run() {
            User user = attendee.getUser();
            if (TextUtils.isEmpty(user.getAccount())) {
                V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, user.getmUserId());
                handler.postDelayed(this, 2000);
            } else {
                UserHeaderImgHelper.display(img_vip, user);
                // text_vip_name.setText(user.getDisplayName());
            }

        }
    }

    class StatusCheckThread implements Runnable {
        Attendee attendee;

        public StatusCheckThread(Attendee attendee) {
            this.attendee = attendee;
        }

        @Override
        public void run() {
            if (attendee.isSpeaking()) {
                img_vip_status.setImageResource(R.mipmap.speak);
            } else {
                img_vip_status.setImageResource(R.mipmap.prohibition);
            }
            if (attendee.isJoined()) {
                text_vip_status.setVisibility(GONE);
                img_vip_status.setVisibility(VISIBLE);
            } else {
                text_vip_status.setVisibility(VISIBLE);
                img_vip_status.setVisibility(GONE);
            }
            if (attendee.isJoined()) {
                text_vip_status.setVisibility(GONE);
                img_vip_status.setVisibility(VISIBLE);
            } else {
                text_vip_status.setVisibility(VISIBLE);
                img_vip_status.setVisibility(GONE);
            }
            handler.postDelayed(this, 2000);
        }
    }


    /**
     * 给学生发消息
     *
     * @param type      消息类型
     * @param studentId 学生ID
     */
    void sendMsg(Context context, int type, long studentId) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("fromID", GlobalHolder.getInstance().getCurrentUserId());//自己的userId
            json.put("timeStamp", DataUtil.getDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("message", json.toString());
        User user = GlobalHolder.getInstance().getUser(studentId);
        if (user != null) {
            Log.e("remote tv", user.getmUserId() + " " + user.getAccount() + " " + user.getNickName());
            new MessageSendUtil(context).sendMessageToRemote(json.toString(),
                    user);//tv的UserId
        }
    }
}
