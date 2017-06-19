package com.bizcom.vc.activity.conference;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.bizcom.util.DensityUtils;
import com.bizcom.util.V2Log;
import com.config.GlobalConfig;

public class SoftwareHelper {

	private static final String TAG = ConferenceActivity.class.getSimpleName();
	private static SoftwareHelper helper;
	public static boolean isInStop = false;
	public static boolean softwareShow = false;

	// For more information, see
	// https://code.google.com/p/android/issues/detail?id=5497
	// To use this class, simply invoke assistActivity() on an Activity that
	// already has its content view set.
	public static SoftwareHelper getInstance() {
		if (helper == null) {
			helper = new SoftwareHelper();
		}
		return helper;
	}

	private Activity activity;
	private View mChildOfContent;
	private int usableHeightPrevious;
	private FrameLayout.LayoutParams frameLayoutParams;

	public void assistActivity(Activity activity) {
		this.activity = activity;
		FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
		mChildOfContent = content.getChildAt(0);
		mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				if (!isInStop)
					possiblyResizeChildOfContent();
			}
		});
		frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
	}

	public SoftwareHelper() {
	}

	private void possiblyResizeChildOfContent() {
		if (!softwareShow) {
			V2Log.d(TAG, "软键盘没有出现");
			return;
		}

		if (GlobalConfig.PROGRAM_IS_PAD) {
			int usableHeightNow = computeUsableHeight();
			if (usableHeightNow != usableHeightPrevious) {
				int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
				V2Log.d(TAG, "usableHeightNow: " + usableHeightNow);
				V2Log.d(TAG, "usableHeightSansKeyboard: " + usableHeightSansKeyboard);
				int heightDifference = usableHeightSansKeyboard - usableHeightNow;
				if (heightDifference > (usableHeightSansKeyboard / 4)) {
					// keyboard probably just became visible
					V2Log.d(TAG, "heightDifference > usableHeightSansKeyboard / 4");
					frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
					mChildOfContent.setLayoutParams(frameLayoutParams);
					((ConferenceActivity) activity).adjustPan(
							(usableHeightSansKeyboard - heightDifference) - DensityUtils.dip2px(activity, 55), true);
				} else {
					V2Log.d(TAG, "heightDifference < usableHeightSansKeyboard / 4");
					// keyboard probably just became hidden
					frameLayoutParams.height = usableHeightNow;
					mChildOfContent.setLayoutParams(frameLayoutParams);
					((ConferenceActivity) activity).adjustPan(usableHeightNow, false);
				}
				usableHeightPrevious = usableHeightNow;
			}
		}
	}

	private int computeUsableHeight() {
		Rect r = new Rect();
		mChildOfContent.getWindowVisibleDisplayFrame(r);
		return (r.bottom - r.top);
	}

}
