package com.bizcom.vc.widget;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shdx.tvchat.phone.R;


/**
 * Created by fengxiang on 2016/3/21.
 */
public class StartVideoSelectWindow extends PopupWindow {

    LayoutInflater inflater;
    Context context;
    View view;

    private OnSelectedListener onSelectedListener;

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }


    public StartVideoSelectWindow(Context context) {
        super(context);
        Log.e("pop", "create  PopSelectPicture");
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(true);

        inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        update();
        initView();
    }


    public void initView() {
        view = inflater.inflate(R.layout.pop_take_picture, null);
        setContentView(view);
        TextView btn_take = (TextView) view.findViewById(R.id.btn_take);
        TextView btn_pick = (TextView) view.findViewById(R.id.btn_pick);
        View btn_close = view.findViewById(R.id.btn_close);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btn_pick.setText("使用手机发起视频通话");
        btn_take.setText("使用TV发起视频通话");
        btn_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSelectedListener != null) {
                    onSelectedListener.onSlected(OnSelectedListener.START_VIDEO_FROM_PHONE);
                }
            }
        });

        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSelectedListener != null) {
                    onSelectedListener.onSlected(OnSelectedListener.START_VIDEO_FROM_TV);
                }
            }
        });
        view.findViewById(R.id.view_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setAnimationStyle(R.style.PopupAnimation1);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (isShowing()) {
            dismiss();
            return;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            // 设置背景颜色变暗
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.alpha = 0.7f;
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            activity.getWindow().setAttributes(lp);
            setOnDismissListener(new OnDismissListener() {


                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                    lp.alpha = 1f;
                    activity.getWindow().setAttributes(lp);
                }
            });
        }

        super.showAtLocation(parent, gravity, x, y);
    }

    public interface OnSelectedListener {

        int START_VIDEO_FROM_PHONE = 1;

        int START_VIDEO_FROM_TV = 2;

        void onSlected(int status);
    }
}
