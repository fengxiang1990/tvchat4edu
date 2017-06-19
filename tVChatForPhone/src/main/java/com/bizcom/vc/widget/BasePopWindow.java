package com.bizcom.vc.widget;

import android.app.Service;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

public abstract class BasePopWindow extends PopupWindow {

    LayoutInflater inflater;
    Context context;
    View view;
    Handler handler = new Handler();

    public BasePopWindow(Context context) {
        super(context);
        Log.i("pop", "create base popWindow");
        this.setWidth(LayoutParams.MATCH_PARENT);
        this.setHeight(LayoutParams.MATCH_PARENT);
        this.setBackgroundDrawable(new BitmapDrawable());// 这样设置才能点击屏幕外dismiss窗口
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        super.update();
    }

    //这个方法必须在子类加载view后 调用super.setView(view);
    //否则无动画效果
    public void setView(View view) {
        this.view = view;
    }

    public abstract void initView();

    public abstract void initData();

    public abstract void initEvent();

    @Override
    public void showAsDropDown(View anchor) {
        if (isShowing()) {
            dismiss();
            return;
        }
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(0, 0, -300, 0);
        animation.setDuration(250);
        view.setAnimation(animation);
        animation.start();
        super.showAsDropDown(anchor);

    }

    @Override
    public void dismiss() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -300);
        animation.setDuration(250);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                dismiss2();
            }
        }, 280);

    }

    public void dismiss2() {
        super.dismiss();
    }
}
