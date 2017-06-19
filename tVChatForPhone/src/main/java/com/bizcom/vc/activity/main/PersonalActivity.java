package com.bizcom.vc.activity.main;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

public class PersonalActivity extends BaseActivity {

    private PersonalOwnerOnClickListener mPersonalOwnerOnClickListener = new PersonalOwnerOnClickListener();
    private PersonalSettingOnClickListener mPersonalSettingOnClickListener = new PersonalSettingOnClickListener();
    private PersonalAboutOnClickListener mPersonalAboutOnClickListener = new PersonalAboutOnClickListener();


    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_personal_main);
        super.onCreate(savedInstanceState);
        mContext = this;
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

    @Override
    public void initViewAndListener() {
        TextView mTitleLeftTV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        setComBackImageTV(mTitleLeftTV);
        mTitleLeftTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView mTitleContentTV = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContentTV.setText(getResources().getString(R.string.personal_title_content));

        View mPersonalOwnerTV = findViewById(R.id.ws_personal_owner);
        mPersonalOwnerTV.setOnClickListener(mPersonalOwnerOnClickListener);
        View mPersonalSettingTV = findViewById(R.id.ws_personal_setting);
        mPersonalSettingTV.setOnClickListener(mPersonalSettingOnClickListener);
        View mPersonalAboutTV = findViewById(R.id.ws_personal_about);
        mPersonalAboutTV.setOnClickListener(mPersonalAboutOnClickListener);
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }

    class PersonalOwnerOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(mContext, ContactDetail2.class);
            intent.putExtra("uid", GlobalHolder.getInstance().getCurrentUserId());
            startActivity(intent);
        }

    }

    class PersonalSettingOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(PublicIntent.START_SETTING_ACTIVITY);
            startActivity(intent);
        }

    }

    class PersonalAboutOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(PublicIntent.START_ABOUT_ACTIVITY);
            startActivity(intent);
        }
    }
}
