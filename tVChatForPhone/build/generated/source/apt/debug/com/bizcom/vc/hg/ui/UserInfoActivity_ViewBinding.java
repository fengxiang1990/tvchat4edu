// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class UserInfoActivity_ViewBinding<T extends UserInfoActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  private View view2131689949;

  private View view2131689948;

  private View view2131689771;

  private View view2131689950;

  private View view2131689969;

  @UiThread
  public UserInfoActivity_ViewBinding(final T target, View source) {
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
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    target.text_nickname = Utils.findRequiredViewAsType(source, R.id.text_nickname, "field 'text_nickname'", TextView.class);
    view = Utils.findRequiredView(source, R.id.img_header2, "field 'img_header2' and method 'showPicSelection'");
    target.img_header2 = Utils.castView(view, R.id.img_header2, "field 'img_header2'", SimpleDraweeView.class);
    view2131689949 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.showPicSelection();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_modify_photo, "method 'img_modify_photo'");
    view2131689948 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.img_modify_photo();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_qrcode, "method 'qrcode'");
    view2131689771 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.qrcode();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_modify_nickname, "method 'modifyName'");
    view2131689950 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.modifyName();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_logout, "method 'logout'");
    view2131689969 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.logout();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_back = null;
    target.text_title = null;
    target.text_nickname = null;
    target.img_header2 = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689949.setOnClickListener(null);
    view2131689949 = null;
    view2131689948.setOnClickListener(null);
    view2131689948 = null;
    view2131689771.setOnClickListener(null);
    view2131689771 = null;
    view2131689950.setOnClickListener(null);
    view2131689950 = null;
    view2131689969.setOnClickListener(null);
    view2131689969 = null;

    this.target = null;
  }
}
