package com.bizcom.vc.widget.cus;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

public class CustomDialog extends Dialog {

	public static final int TIME_OUT = 10000;
	private Timer timer = new Timer();
	private RotateAnimation animation;
	public int cusWidth = 0;
	public int cusHeight = 0;

	public CustomDialog(Context context, int theme, RotateAnimation animation) {
		super(context, theme);
		this.animation = animation;
	}

	public CustomDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Context context) {
		super(context);
	}

	public void initTimeOut() {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				animation.cancel();
				dismiss();
				Looper.prepare();
				Toast.makeText(getContext(), "网络不佳 , 连接超时", Toast.LENGTH_SHORT)
						.show();
				Looper.loop();
			}
		}, TIME_OUT);
	}

	public void cannelTimeOut() {
		timer.cancel();
	}

	@Override
	public void show() {
		if (cusWidth != 0 && cusHeight != 0) {
			getWindow().setLayout(cusWidth , cusHeight);
			cusWidth = 0;
			cusHeight = 0;
		}
		super.show();
	}
}
