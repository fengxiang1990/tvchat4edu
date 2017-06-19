package com.bizcom.vc.activity.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		ImageView mLogoAbout = (ImageView) findViewById(R.id.ws_about_logo);
		TextView mAboutName = (TextView) findViewById(R.id.ws_about_name);
		TextView mCompanyName = (TextView) findViewById(R.id.ws_about_company_name);
		TextView mVersion = (TextView) findViewById(R.id.about_version);
		TextView mTitleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
		TextView mBackTV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        mBackTV.setBackgroundResource(R.drawable.title_bar_back_button_selector);
        mBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

		mLogoAbout.setImageResource(R.drawable.logo_about);
		mAboutName.setText(getResources().getString(R.string.app_name));
		mCompanyName.setText(R.string.login_title_copyright);
		mVersion.setText(GlobalConfig.GLOBAL_VERSION_NAME);
        mTitleContent.setText(R.string.about_title_content);
	}
}
