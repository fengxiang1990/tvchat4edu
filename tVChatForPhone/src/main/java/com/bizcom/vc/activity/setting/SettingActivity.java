package com.bizcom.vc.activity.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bizcom.util.MessageUtil;
import com.shdx.tvchat.phone.R;

public class SettingActivity extends FragmentActivity {

	private String currentVisibleFragment = SettingTabFragment.class.getSimpleName();
	private Fragment settingTabFragment;
	private Fragment settingAuthenticationActivity;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_root_layout);
		settingTabFragment = new SettingTabFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.container, settingTabFragment).commit();
	}

	public void replaceFragment(String fragmentName) {
		replaceFragment(fragmentName, false);
	}

	public void replaceFragment(String fragmentName, boolean isVideoMass) {
		if (fragmentName.equals(SettingAuthenticationFragment.class.getSimpleName())) {
			if (settingAuthenticationActivity == null)
				settingAuthenticationActivity = new SettingAuthenticationFragment();
			Bundle bundle = new Bundle();
			bundle.putBoolean("isVideoMass", isVideoMass);
			settingAuthenticationActivity.setArguments(bundle);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, settingAuthenticationActivity)
					.commit();
		} else if (fragmentName.equals(SettingTabFragment.class.getSimpleName())) {
			getSupportFragmentManager().beginTransaction().replace(R.id.container, settingTabFragment).commit();
		} else if (fragmentName.equals(SettingUpdatePasswordFragment.class.getSimpleName())) {
            Fragment settingUpdatePasswordActivity = new SettingUpdatePasswordFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.container, settingUpdatePasswordActivity)
					.commit();
		}
	}

	@Override
	public void onBackPressed() {
		if (currentVisibleFragment.equals(SettingTabFragment.class.getSimpleName())) {
			finish();
		} else {
			replaceFragment(SettingTabFragment.class.getSimpleName());
		}
	}

	public void setCurrentVisibleFragment(String currentVisibleFragment) {
		this.currentVisibleFragment = currentVisibleFragment;
	}
	
	protected void setComBackImageTV(TextView mBackTV){
		mBackTV.setBackgroundResource(R.drawable.title_bar_back_button_selector);;
	}
	
	protected void setComBackTextTV(TextView mBackTV){
		mBackTV.setText(getResources().getString(R.string.common_back));
	}

	/**
	 * 获取点击事件
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View view = getCurrentFocus();
			if (view != null && isHideInput(view, ev)) {
				MessageUtil.hideKeyBoard(this, view.getWindowToken());
				;
				view.clearFocus();
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 判定是否需要隐藏
	 * 
	 * @param v
	 * @param ev
	 * @return
	 */
	private boolean isHideInput(View v, MotionEvent ev) {
		if (v != null && (v instanceof EditText)) {
			int[] location = new int[2];
			v.getLocationInWindow(location);
			int left = location[0], top = location[1], bottom = top + v.getHeight(), right = left + v.getWidth();
			float targetX = ev.getX();
			float targetY = ev.getY();
			if (targetX > left && targetX < right && targetY > top && targetY < bottom) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}
}
