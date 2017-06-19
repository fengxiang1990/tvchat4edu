package com.bizcom.vc.hg.util;

import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.MainApplication;
import com.bizcom.vo.User;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shdx.tvchat.phone.R;

import java.io.File;

/**
 * 头像显示工具类
 * Created by admin on 2016/12/9.
 */

    public class UserHeaderImgHelper {

    public static void display(ImageView view, User user) {
        if (TextUtils.isEmpty(user.getmAvatarLocation()) && TextUtils.isEmpty(user.getAvatarPath())) {
            ImageLoader.getInstance().displayImage("",
                    view, MainApplication.imgOptions);
            return;
        }
        if (user.getmAvatarLocation().contains("http://")
                || user.getmAvatarLocation().contains("https://")) {
            ImageLoader.getInstance().displayImage(user.getmAvatarLocation(),
                    view, MainApplication.imgOptions);
        } else {
            File file = new File(user.getAvatarPath());
            if (file.exists()) {
                ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(),
                        view, MainApplication.imgOptions);
            }
        }
    }

    public static void display(SimpleDraweeView view, User user) {
        if (TextUtils.isEmpty(user.getmAvatarLocation()) && TextUtils.isEmpty(user.getAvatarPath())) {
            RoundingParams params = new RoundingParams();
            params.setRoundAsCircle(true);
            GenericDraweeHierarchyBuilder builder = GenericDraweeHierarchyBuilder.newInstance(view.getResources());
            builder.setPlaceholderImage(R.drawable.avatar);
            builder.setRoundingParams(params);
            GenericDraweeHierarchy hierarchy = builder.build();
            view.setHierarchy(hierarchy);
            return;
        }
        if (user.getmAvatarLocation().contains("http://")
                || user.getmAvatarLocation().contains("https://")) {
            view.setImageURI(Uri.parse(user.getmAvatarLocation()));
        } else {
            File file = new File(user.getAvatarPath());
            if (file.exists()) {
                view.setImageURI(Uri.fromFile(file));
            }
        }
    }
}
