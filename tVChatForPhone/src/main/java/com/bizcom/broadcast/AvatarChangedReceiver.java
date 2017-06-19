package com.bizcom.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bizcom.bo.UserAvatarObject;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.service.JNIService;

public class AvatarChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (JNIService.JNI_BROADCAST_USER_AVATAR_CHANGED_NOTIFICATION.equals(intent.getAction())) {
			UserAvatarObject uao = (UserAvatarObject) intent.getExtras().get("avatar");
			if (uao != null) {
				BitmapManager.getInstance().loadUserAvatarAndNotify(uao);
			}
		}
	}
}
