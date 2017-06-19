// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class NickNamePhotoSettingActivity_ViewBinding<T extends NickNamePhotoSettingActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  private View view2131689712;

  private View view2131689713;

  private View view2131689716;

  private View view2131689718;

  @UiThread
  public NickNamePhotoSettingActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.img_back, "field 'img_back' and method 'back'");
    target.img_back = Utils.castView(view, R.id.img_back, "field 'img_back'", ImageView.class);
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
    target.recyclerView = Utils.findRequiredViewAsType(source, R.id.recyclerView, "field 'recyclerView'", RecyclerView.class);
    view = Utils.findRequiredView(source, R.id.img_camera_bg, "field 'img_camera_bg' and method 'imgCameraBgClick'");
    target.img_camera_bg = Utils.castView(view, R.id.img_camera_bg, "field 'img_camera_bg'", SimpleDraweeView.class);
    view2131689712 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.imgCameraBgClick();
      }
    });
    target.edit_nickname = Utils.findRequiredViewAsType(source, R.id.edit_nickname, "field 'edit_nickname'", EditText.class);
    view = Utils.findRequiredView(source, R.id.img_camera, "field 'img_camera' and method 'imgCameraClick'");
    target.img_camera = Utils.castView(view, R.id.img_camera, "field 'img_camera'", ImageView.class);
    view2131689713 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.imgCameraClick();
      }
    });
    target.img_shaizi_gif = Utils.findRequiredViewAsType(source, R.id.img_shaizi_gif, "field 'img_shaizi_gif'", SimpleDraweeView.class);
    view = Utils.findRequiredView(source, R.id.btn_getname, "method 'getName'");
    view2131689716 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.getName();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_ok, "method 'okClick'");
    view2131689718 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.okClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_back = null;
    target.recyclerView = null;
    target.img_camera_bg = null;
    target.edit_nickname = null;
    target.img_camera = null;
    target.img_shaizi_gif = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689712.setOnClickListener(null);
    view2131689712 = null;
    view2131689713.setOnClickListener(null);
    view2131689713 = null;
    view2131689716.setOnClickListener(null);
    view2131689716 = null;
    view2131689718.setOnClickListener(null);
    view2131689718 = null;

    this.target = null;
  }
}
