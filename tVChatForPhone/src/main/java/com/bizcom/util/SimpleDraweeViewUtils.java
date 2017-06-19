package com.bizcom.util;

import java.io.File;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * @author maronghua
 * @since 2016-2
 */
public final class SimpleDraweeViewUtils {

    /**
     * 显示图片
     */
    public final static void display(SimpleDraweeView v, int drawableResouceId) {
        if (v != null) {
            if (drawableResouceId != -1 && drawableResouceId != 0) {
                display(v, Uri.parse("res://" + v.getContext().getPackageName() + "/" + drawableResouceId));
            } else {
                v.setImageURI(Uri.EMPTY);
            }
        }
    }

    /**
     * 显示图片
     */
    public final static void display(SimpleDraweeView v, Uri uri) {
        if (v != null) {
            v.setImageURI(uri);
        }
    }

    /**
     * 显示图片
     */
    public final static void display(SimpleDraweeView v, String filePathOrNetwordImageUrl) {
        if (v != null) {
            if (TextUtils.isEmpty(filePathOrNetwordImageUrl)) {
                v.setImageURI(Uri.EMPTY);

            } else {
                if (filePathOrNetwordImageUrl.startsWith("http://")) {
                    display(v, Uri.parse(filePathOrNetwordImageUrl));

                } else if (filePathOrNetwordImageUrl.startsWith("file://")) {
                    display(v, Uri.parse(filePathOrNetwordImageUrl));

                } else {
                    display(v, Uri.fromFile(new File(filePathOrNetwordImageUrl)));
                }
            }
        }
    }

    /**
     * 显示图片
     */
    public final static void display(SimpleDraweeView v, TextView textName, String filePathOrNetwordImageUrl) {
        if (v != null) {
            if (TextUtils.isEmpty(filePathOrNetwordImageUrl)) {
                v.setImageURI(Uri.EMPTY);
                textName.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);

            } else {
                if (filePathOrNetwordImageUrl.startsWith("http://")) {
                    display(v, Uri.parse(filePathOrNetwordImageUrl));

                } else if (filePathOrNetwordImageUrl.startsWith("file://")) {
                    display(v, Uri.parse(filePathOrNetwordImageUrl));

                } else {
                    display(v, Uri.fromFile(new File(filePathOrNetwordImageUrl)));
                }

                textName.setVisibility(View.GONE);
                v.setVisibility(View.VISIBLE);
            }
        }
    }

}
