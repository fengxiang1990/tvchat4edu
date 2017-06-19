package com.bizcom.vc.hg.util;


import com.shdx.tvchat.phone.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogUtil {

	

	public static Dialog initDlg(Context mContext,String hint) {

		TextView hitView = null;
		ImageView iconView = null;
		Dialog dialog = null;
		if (dialog == null) {
			View rootView = View.inflate(mContext,
					R.layout.ws_common_full_screen_waiting_dialog, null);
			dialog = new Dialog(mContext, R.style.InMeetingQuitDialog);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(rootView);
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);
			hitView = (TextView) dialog
					.findViewById(R.id.ws_waitting_dialog_hint);
			iconView = (ImageView) dialog
					.findViewById(R.id.ws_waitting_dialog_icon);
		}

		Animation animation = null;
		if (animation == null) {
			animation = new RotateAnimation(0f, 359f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(500);
			animation.setRepeatCount(RotateAnimation.INFINITE);
			animation.setRepeatMode(RotateAnimation.RESTART);
			LinearInterpolator lin = new LinearInterpolator();
			animation.setInterpolator(lin);
		}

		if (hint != null) {
			hitView.setText(hint);
			hitView.setVisibility(View.VISIBLE);
		} else {
			hitView.setVisibility(View.GONE);
		}

		if (!dialog.isShowing()) {
			iconView.startAnimation(animation);
			
			try {
				dialog.show();
			} catch (Exception e) {
			}
		}
		return dialog;
		
	}
}
