package com.bizcom.vc.widget;


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.shdx.tvchat.phone.R;

/**
 * Created by admin on 2017/1/4.
 */

public class LoadingDialog extends AlertDialog {

    ImageView img_loading;
    TextView text_msg;

    public LoadingDialog(Context context) {
        super(context);
    }

    private CharSequence mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.loading_layout, null);
        img_loading = (ImageView) view.findViewById(R.id.img_loading);
        text_msg = (TextView) view.findViewById(R.id.text_msg);
        setView(view);
        if (mMessage != null) {
            setMessage(mMessage);
        }
        handler.post(new RotateRunnable(img_loading));
        super.onCreate(savedInstanceState);
    }

    Handler handler = new Handler();


    class RotateRunnable implements Runnable {
        ImageView img;

        public RotateRunnable(ImageView img) {
            this.img = img;
        }

        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(img, "rotation", 0f, 360f);
            animator.setDuration(1333);
            animator.setRepeatCount(-1);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }
    }

    @Override
    public void setMessage(CharSequence message) {
        if (text_msg != null) {
            text_msg.setText(message);
        } else {
            mMessage = message;
        }
    }


    public static LoadingDialog show(Context context, CharSequence title,
                                     CharSequence message) {
        return show(context, title, message, false);
    }

    public static LoadingDialog show(Context context, CharSequence title,
                                     CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static LoadingDialog show(Context context, CharSequence title,
                                     CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static LoadingDialog show(Context context, CharSequence title,
                                     CharSequence message, boolean indeterminate,
                                     boolean cancelable, OnCancelListener cancelListener) {
        LoadingDialog dialog = new LoadingDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

}
