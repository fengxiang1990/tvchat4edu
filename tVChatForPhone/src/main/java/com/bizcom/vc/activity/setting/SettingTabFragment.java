package com.bizcom.vc.activity.setting;

import com.bizcom.request.V2ImRequest;
import com.bizcom.service.JNIService;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingTabFragment extends Fragment {
	private Context mContext;
	private View rootView;
	private View mVideoMass;
	private View mUpdatePassword;
	private TextView mQuitButton;
	private TextView mCannelTV;

	private RelativeLayout rlAuthentication;
	private TextView tvSettingBack;
    private TextView mAuthShowTV;
    private TextView mVideoMassShowTV;

	private BroadcastReceiver localReceiver;
	private boolean isLoading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((SettingActivity) getActivity()).setCurrentVisibleFragment(SettingTabFragment.class.getSimpleName());
		initReceiver();
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.tab_fragment_setting, container, false);
		View functionLy = rootView.findViewById(R.id.ws_activity_main_title_functionLy);
		if(functionLy != null){
			functionLy.setVisibility(View.INVISIBLE);
		}
		connectView();
		bindViewEnvent();
		return rootView;
	}

    @Override
    public void onResume() {
        super.onResume();
        long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
        int videoMass = LocalSharedPreferencesStorage.getConfigIntValue(getActivity(),
                String.valueOf(currentUserId) + ":videoMass", V2GlobalConstants.CONF_CAMERA_MASS_LOW);
        switch (videoMass) {
            case V2GlobalConstants.CONF_CAMERA_MASS_LOW:
                mVideoMassShowTV.setText(R.string.confs_camear_quanlity_low);
                break;
            case V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE:
                mVideoMassShowTV.setText(R.string.confs_camear_quanlity_middle);
                break;
            case V2GlobalConstants.CONF_CAMERA_MASS_HIGH:
                mVideoMassShowTV.setText(R.string.confs_camear_quanlity_high);
                break;
        }

        User currentUser = GlobalHolder.getInstance().getCurrentUser();
        switch (currentUser.getAuthtype()) {
            case 0:
                mAuthShowTV.setText(R.string.crowd_detail_qulification_rule_no_rule);
                break;
            case 1:
                mAuthShowTV.setText(R.string.crowd_detail_qulification_rule_qulification);
                break;
            case 2:
                mAuthShowTV.setText(R.string.crowd_detail_qulification_rule_never);
                break;
        }
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(localReceiver);
		} catch (IllegalArgumentException e) {
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		((ViewGroup) rootView.getParent()).removeView(rootView);
	}

	private void connectView() {
		TextView titleContent = (TextView) rootView.findViewById(R.id.ws_common_activity_title_content);
		titleContent.setText(getResources().getString(R.string.date_time_picker_set));
		tvSettingBack = (TextView) rootView.findViewById(R.id.ws_common_activity_title_left_button);
		((SettingActivity)getActivity()).setComBackTextTV(tvSettingBack);
		rootView.findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);

		rlAuthentication = (RelativeLayout) rootView.findViewById(R.id.setting_person_auth);
		mVideoMass = rootView.findViewById(R.id.setting_video_mass);
		mUpdatePassword = rootView.findViewById(R.id.setting_update_password);
		mCannelTV = (TextView) rootView.findViewById(R.id.setting_quit_cannel);
		mQuitButton = (TextView) rootView.findViewById(R.id.setting_quit_button);

        mAuthShowTV = (TextView) rootView.findViewById(R.id.setting_person_auth_show);
        mVideoMassShowTV = (TextView) rootView.findViewById(R.id.setting_video_mass_show);
	}

	private void initReceiver() {
		localReceiver = new LocalReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		filter.addAction(JNIService.JNI_BROADCAST_USER_LOG_OUT_NOTIFICATION);
		getActivity().registerReceiver(localReceiver, filter);
	}

	private void bindViewEnvent() {
		tvSettingBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLoading) {
					return;
				}
				((Activity) mContext).finish();
			}
		});

		mQuitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (isLoading) {
					return;
				}
				GlobalConfig.saveLogoutFlag(getActivity());
				Intent i = new Intent();
				i.setAction(PublicIntent.FINISH_APPLICATION);
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				mContext.sendBroadcast(i);
			}
		});

		rlAuthentication.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isLoading) {
					return;
				}
				((SettingActivity) mContext).replaceFragment(SettingAuthenticationFragment.class.getSimpleName(),
						false);
			}
		});

		mVideoMass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLoading) {
					return;
				}
				((SettingActivity) mContext).replaceFragment(SettingAuthenticationFragment.class.getSimpleName(), true);
			}
		});
		
		mUpdatePassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLoading) {
					return;
				}
				((SettingActivity) mContext).replaceFragment(SettingUpdatePasswordFragment.class.getSimpleName(), false);
			}
		});

		mCannelTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLoading) {
					return;
				}
				isLoading = true;
				WaitDialogBuilder.showNormalWithHintProgress(getActivity(),
						getText(R.string.setting_logouting).toString());
				V2ImRequest.invokeNative(V2ImRequest.NATIVE_CANNEL_LOGIN, 0);
			}
		});
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
}
