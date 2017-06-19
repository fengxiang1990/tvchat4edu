// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MipcaActivityCapture_ViewBinding<T extends MipcaActivityCapture> implements Unbinder {
  protected T target;

  private View view2131690309;

  private View view2131689954;

  @UiThread
  public MipcaActivityCapture_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.header_bg = Utils.findRequiredViewAsType(source, R.id.header_bg, "field 'header_bg'", LinearLayout.class);
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    view = Utils.findRequiredView(source, R.id.text_right1, "field 'text_right1' and method 'text_right1'");
    target.text_right1 = Utils.castView(view, R.id.text_right1, "field 'text_right1'", TextView.class);
    view2131690309 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.text_right1();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_back, "method 'back'");
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.header_bg = null;
    target.text_title = null;
    target.text_right1 = null;

    view2131690309.setOnClickListener(null);
    view2131690309 = null;
    view2131689954.setOnClickListener(null);
    view2131689954 = null;

    this.target = null;
  }
}
