package com.bizcom.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.shdx.tvchat.phone.R;

/**
 * Created by admin on 2016/12/15.
 */

public class SyncingDialog {

    Context context;
    ImageView btn_close;
    SimpleDraweeView gifView1;
    SimpleDraweeView gifView2;
    AlertDialog dialog;
    public boolean doing = true;

    public SyncingDialog(Context context) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_cover, null);
        gifView1 = (SimpleDraweeView) view.findViewById(R.id.gif1);
        gifView2 = (SimpleDraweeView) view.findViewById(R.id.gif2);
        btn_close = (ImageView) view.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        builder.setView(view);
        dialog = builder.create();
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
    }


    public void showGif1() {
        Uri uri = Uri.parse("res:///" + R.drawable.zq);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        gifView1.setController(controller);
        dialog.show();
    }

    Handler handler = new Handler();

    public void showGif2() {
        gifView1.setVisibility(View.INVISIBLE);
        gifView2.setVisibility(View.VISIBLE);
        btn_close.setVisibility(View.VISIBLE);
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable final Animatable anim) {
                if (anim != null) {
                    // 其他控制逻辑
                    anim.start();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            anim.stop();
                        }
                    }, 500);
                }
            }
        };

        Uri uri2 = Uri.parse("res:///" + R.drawable.rt);
        DraweeController controller2 = Fresco.newDraweeControllerBuilder()
                .setUri(uri2)
                .setControllerListener(controllerListener)
                .setAutoPlayAnimations(true)
                .build();
        gifView2.setController(controller2);
        dialog.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 2000);
    }
}
