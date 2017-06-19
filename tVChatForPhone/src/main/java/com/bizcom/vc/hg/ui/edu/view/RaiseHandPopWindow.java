package com.bizcom.vc.hg.ui.edu.view;

import android.app.Service;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSONObject;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

/**
 * Created by admin on 2017/2/10.
 */

public class RaiseHandPopWindow extends PopupWindow {


    Context context;
    LayoutInflater inflater;
    long studentId = 0;

    SimpleDraweeView simpleDraweeView;
    OnHandClickListener listener;
    public RaiseHandPopWindow(Context context,final OnHandClickListener listener) {
        super(context);
        Log.e("pop", "create  RaiseHandPopWindow");
        this.listener = listener;
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        BitmapDrawable drawable = new BitmapDrawable();
        this.setBackgroundDrawable(drawable);// 这样设置才能点击屏幕外dismiss窗口
        this.setOutsideTouchable(false);
        // this.setFocusable(true);
        inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        super.update();
        View view = inflater.inflate(R.layout.pop_raise_hand_layout, null);
        setContentView(view);
        simpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.img_hand_gif);
        Uri uri = Uri.parse("asset:///" + "raisehand.gif");
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        simpleDraweeView.setController(controller);
        LinearLayout ll_box = (LinearLayout) view.findViewById(R.id.ll_raise_hand);
        ll_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.agree(RaiseHandPopWindow.this);
            }
        });

    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }


    public interface OnHandClickListener{
        void agree(RaiseHandPopWindow popWindow);
    }
}
