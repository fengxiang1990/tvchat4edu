package com.bizcom.util;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import java.util.HashMap;

public class AnimationHepler {

    private static HashMap<Animation, Integer> mAnimationCache;
    private AnimaListener listener;
    private static CommonAnimationListener commonAnimationListener;

    private AnimationHepler() {
    }

    private static AnimationHepler animaHelper;

    public static AnimationHepler getInstance() {
        if (animaHelper == null) {
            synchronized (AnimationHepler.class) {
                if (animaHelper == null) {
                    animaHelper = new AnimationHepler();
                    mAnimationCache = new HashMap<>();
                    commonAnimationListener = animaHelper.new CommonAnimationListener();
                }
            }
        }
        return animaHelper;
    }

    public void setAnimaListener(AnimaListener listen) {
        listener = listen;
    }

    public Animation loadAnimation(Context mContext, int animaTargetID, int resID , View view) {
        Animation anim = AnimationUtils.loadAnimation(mContext, resID);
        view.startAnimation(anim);
        if (listener != null) {
            mAnimationCache.put(anim, animaTargetID);
            anim.setAnimationListener(commonAnimationListener);
        }
        return anim;
    }

    public interface AnimaListener {

        void animaLoadEnd(int animaTargetID, Animation animation);
    }

    class CommonAnimationListener implements AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (listener != null) {
                listener.animaLoadEnd(mAnimationCache.remove(animation), animation);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
