package com.bizcom.vc.activity.setting;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

public class SettingAuthenticationFragment extends Fragment {

	private static final int UPDATEUSER_CALLBACK = 0;
	private View rootView;
	// rg_authentication
	private RadioGroup rgAutentication;
	private RadioGroup rgMasstication;
	private V2ImRequest service;
	private boolean isVideoMass;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATEUSER_CALLBACK:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((SettingActivity) getActivity())
				.setCurrentVisibleFragment(SettingAuthenticationFragment.class.getSimpleName());
		Bundle arguments = getArguments();
		if (arguments != null) {
			isVideoMass = arguments.getBoolean("isVideoMass", false);
			if (!isVideoMass) {
				service = new V2ImRequest();
			}
		} else {
			((SettingActivity) getActivity()).replaceFragment(SettingTabFragment.class.getSimpleName());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = View.inflate(getActivity(), R.layout.tab_fragment_setting_authentication, null);
		View functionLy = rootView.findViewById(R.id.ws_activity_main_title_functionLy);
		if(functionLy != null){
			functionLy.setVisibility(View.INVISIBLE);
		}
		initView(rootView);
		return rootView;
	}

	private void initView(View rootView) {
		TextView titleContent = (TextView) rootView.findViewById(R.id.ws_common_activity_title_content);
		rgAutentication = (RadioGroup) rootView.findViewById(R.id.rg_authentication);
		rgMasstication = (RadioGroup) rootView.findViewById(R.id.rg_video_mass);
		if (isVideoMass) {
			rgMasstication.setVisibility(View.VISIBLE);
			rgAutentication.setVisibility(View.GONE);
			titleContent.setText(getText(R.string.confs_camear_quanlity));
		} else {
			rgMasstication.setVisibility(View.GONE);
			rgAutentication.setVisibility(View.VISIBLE);
			titleContent.setText(getResources().getString(R.string.friendManagementActivity_titlebar_left_text2));
		}
		TextView backButton = (TextView) rootView.findViewById(R.id.ws_common_activity_title_left_button);
		((SettingActivity)getActivity()).setComBackTextTV(backButton);
		
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((SettingActivity) getActivity()).replaceFragment(SettingTabFragment.class.getSimpleName());
			}
		});
		rootView.findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);

		if (isVideoMass) {
			long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
			int videoMass = LocalSharedPreferencesStorage.getConfigIntValue(getActivity(),
					String.valueOf(currentUserId) + ":videoMass", V2GlobalConstants.CONF_CAMERA_MASS_LOW);
			switch (videoMass) {
			case V2GlobalConstants.CONF_CAMERA_MASS_LOW:
				rgMasstication.check(R.id.rb_mass_low);
				break;
			case V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE:
				rgMasstication.check(R.id.rb_mass_middle);
				break;
			case V2GlobalConstants.CONF_CAMERA_MASS_HIGH:
				rgMasstication.check(R.id.rb_mass_high);
				break;
			}
		} else {
			User currentUser = GlobalHolder.getInstance().getCurrentUser();
			switch (currentUser.getAuthtype()) {
			case 0:
				rgAutentication.check(R.id.rb_allow_anybogy);
				break;
			case 1:
				rgAutentication.check(R.id.rb_require_authorization);
				break;
			case 2:
				rgAutentication.check(R.id.rb_unallow_anybogy);
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		if (!GlobalHolder.getInstance().checkServerConnected(getActivity())) {
            if (isVideoMass) {
                int videoMass = V2GlobalConstants.CONF_CAMERA_MASS_LOW;
                switch (rgMasstication.getCheckedRadioButtonId()) {
                    case R.id.rb_mass_low:
                        videoMass = V2GlobalConstants.CONF_CAMERA_MASS_LOW;
                        break;
                    case R.id.rb_mass_middle:
                        videoMass = V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE;
                        break;
                    case R.id.rb_mass_high:
                        videoMass = V2GlobalConstants.CONF_CAMERA_MASS_HIGH;
                        break;
                }
                GlobalConfig.setGlobalVideoLevel(getActivity(), videoMass);
            } else {
                int authtype = 0;
                switch (rgAutentication.getCheckedRadioButtonId()) {
                    case R.id.rb_allow_anybogy:// 允许任何人
                        authtype = 0;
                        break;
                    case R.id.rb_require_authorization:// 需要验证
                        authtype = 1;
                        break;
                    case R.id.rb_unallow_anybogy:// 不允许任何人
                        authtype = 2;
                        break;
                    default:
                        authtype = 0;
                        break;
                }
                User currentUser = GlobalHolder.getInstance().getCurrentUser();
                currentUser.setAuthtype(authtype);
                service.updateUserInfo(currentUser, new HandlerWrap(mHandler, UPDATEUSER_CALLBACK, null));
            }
		}

		if (service != null) {
			service.clearCalledBack();
			service = null;
		}

		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		((ViewGroup) rootView.getParent()).removeView(rootView);
	}
}
