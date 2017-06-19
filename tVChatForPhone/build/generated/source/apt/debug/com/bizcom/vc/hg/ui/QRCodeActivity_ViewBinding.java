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

public class QRCodeActivity_ViewBinding<T extends QRCodeActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  private View view2131689879;

  @UiThread
  public QRCodeActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.user_header_icon = Utils.findRequiredViewAsType(source, R.id.user_head_icon, "field 'user_header_icon'", SimpleDraweeView.class);
    target.tv_name = Utils.findRequiredViewAsType(source, R.id.tv_name, "field 'tv_name'", TextView.class);
    target.tv_phone = Utils.findRequiredViewAsType(source, R.id.tv_phone, "field 'tv_phone'", TextView.class);
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    target.img_code = Utils.findRequiredViewAsType(source, R.id.img_code, "field 'img_code'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.img_back, "method 'back'");
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_create, "method 'createCode'");
    view2131689879 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.createCode();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.user_header_icon = null;
    target.tv_name = null;
    target.tv_phone = null;
    target.text_title = null;
    target.img_code = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689879.setOnClickListener(null);
    view2131689879 = null;

    this.target = null;
  }
}
