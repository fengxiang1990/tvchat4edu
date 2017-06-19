package com.bizcom.util;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bizcom.vc.widget.MProgressDialog;
import com.shdx.tvchat.phone.R;

public class WaitDialogBuilder {
    public static View rootView;
    private static TextView hitView;
    private static ImageView iconView;
    private static RotateAnimation animation;
    public static MProgressDialog dialog;

    public static Dialog showNormalWithHintProgress(Context mContext) {
        return showNormalWithHintProgress(mContext, mContext.getResources()
                .getString(R.string.util_progress_waiting));
    }

    public static Dialog showNormalWithHintProgress(Context mContext, String hint) {
        try {
            dialog = new MProgressDialog(mContext);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("");
            dialog.setMessage(TextUtils.isEmpty(hint) ? "" : hint);
            dialog.show();
            return dialog;
        } catch (Exception e) {

        }
        return null;
    }

    public static Dialog showNormalWithHintProgress(Context mContext, String hint, boolean cancelable) {
        try {
            dialog = new MProgressDialog(mContext);
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("");
            dialog.setMessage(TextUtils.isEmpty(hint) ? "" : hint);
            dialog.show();
            return dialog;
        } catch (Exception e) {

        }
        return null;
    }

//        public static Dialog showNormalWithHintProgress(Context mContext,
//                                                    String hint, boolean show) {
//        if (!show) {
//            dismissDialog();
//            return null;
//        }
//        dialog = new LoadingDialog(mContext);
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setTitle("");
//        dialog.setMessage(TextUtils.isEmpty(hint) ? "" : hint);
//        dialog.show();
//        return dialog;
//    }
//    public static Dialog showNormalWithHintProgress(Context mContext,
//                                                    String hint, boolean show) {
//        if (!show) {
//            dismissDialog();
//            return null;
//        }
//
//        if (dialog == null) {
//            rootView = View.inflate(mContext,
//                    R.layout.ws_common_full_screen_waiting_dialog, null);
//            dialog = new Dialog(mContext, R.style.InMeetingQuitDialog);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(rootView);
//            dialog.setCancelable(true);
//            dialog.setCanceledOnTouchOutside(false);
//            hitView = (TextView) dialog
//                    .findViewById(R.id.ws_waitting_dialog_hint);
//            iconView = (ImageView) dialog
//                    .findViewById(R.id.ws_waitting_dialog_icon);
//        }
//
//        if (animation == null) {
//            animation = new RotateAnimation(0f, 359f,
//                    Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            animation.setDuration(500);
//            animation.setRepeatCount(RotateAnimation.INFINITE);
//            animation.setRepeatMode(RotateAnimation.RESTART);
//            LinearInterpolator lin = new LinearInterpolator();
//            animation.setInterpolator(lin);
//        }
//
//        if (hint != null) {
//            hitView.setText(hint);
//            hitView.setVisibility(View.VISIBLE);
//        } else {
//            hitView.setVisibility(View.GONE);
//        }
//
//        if (!dialog.isShowing()) {
//            iconView.startAnimation(animation);
//
//            try {
//                dialog.show();
//            } catch (Exception e) {
//            }
//        }
//        return dialog;
//    }

    public static void changeHintText(String hintText) {
        if (hitView != null) {
            hitView.setText(hintText);
        }
    }

    public static void clearWaitDialog() {
        if (dialog != null) {
            dialog = null;
            rootView = null;
            hitView = null;
            iconView = null;
            animation = null;
        }
    }

    public static void dismissDialog() {
//        if (animation != null)
//            animation.cancel();
        if (WaitDialogBuilder.dialog != null) {
            WaitDialogBuilder.dialog.dismiss();
//            if (dialog.isShowing()) {
//                dialog.dismiss();
//            }
        }
    }
}
