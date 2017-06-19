package com.bizcom.vc.activity;

import android.content.pm.ActivityInfo;

public class ConversationP2PAVActivityAdapter {

	private ConversationP2PAVActivity mActivity;

	public ConversationP2PAVActivityAdapter(
			ConversationP2PAVActivity conversationP2PAVActivity) {
		mActivity = conversationP2PAVActivity;
	}

	public void setScreenOrientation() {
		if (mActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			mActivity
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
}
