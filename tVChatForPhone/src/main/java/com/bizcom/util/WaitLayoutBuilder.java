package com.bizcom.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bizcom.vc.widget.cus.V2ImageView;
import com.shdx.tvchat.phone.R;

/**
 * Created by wangzhiguo on 16/1/19.
 */
public class WaitLayoutBuilder {

    private static ViewGroup mParent;
    private static LinearLayout mRootView;
    private static V2ImageView mIconIV;
    private static TextView mTextView;
    private static RotateAnimation animation;

    public static void initWaitLayout(Context mContext , ViewGroup parent){
        if (mRootView == null) {
            mRootView = new LinearLayout(mContext);
            mRootView.setGravity(Gravity.CENTER);
            mRootView.setOrientation(LinearLayout.VERTICAL);
            mRootView.setVisibility(View.GONE);

            mIconIV = new V2ImageView(mContext);
            mIconIV.setImageResource(R.drawable.spin_black_70);
            mRootView.addView(mIconIV);

            mTextView = new TextView(mContext);
            mRootView.addView(mTextView);
            mTextView.setPadding(0 , DensityUtils.dip2px(mContext , 10f) , 0 , 0);

            animation = new RotateAnimation(0f, 359f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(500);
            animation.setRepeatCount(RotateAnimation.INFINITE);
            animation.setRepeatMode(RotateAnimation.RESTART);
            LinearInterpolator lin = new LinearInterpolator();
            animation.setInterpolator(lin);
        }

        mParent = parent;
        mParent.addView(mRootView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT
                , FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mRootView.setLayoutParams(params);
    }

    public static void showNormalWithHintProgress(Context mContext,
                                                  boolean show) {
        showNormalWithHintProgress(mContext, null, show);
    }

    public static void showNormalWithHintProgress(Context mContext,
                                                  String hint, boolean show) {
        if (!show) {
            dismissProgressLayout();
            return ;
        }

        if (!TextUtils.isEmpty(hint)) {
            mTextView.setText(hint);
            mTextView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.GONE);
        }

        mRootView.setVisibility(View.VISIBLE);
        mIconIV.startAnimation(animation);
    }

    public static void clearWaitLayout() {
        if (mRootView != null) {
            if(mParent != null){
                mParent.removeView(mRootView);
                mParent = null;
            }
            dismissProgressLayout();
            mRootView = null;
            mIconIV = null;
            mTextView = null;
            animation = null;
        }
    }

    private static void dismissProgressLayout() {
        if (mRootView != null) {
            animation.cancel();
            mRootView.setVisibility(View.GONE);
        }
    }
}
