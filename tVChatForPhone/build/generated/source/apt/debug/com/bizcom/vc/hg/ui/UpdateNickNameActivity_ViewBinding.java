// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class UpdateNickNameActivity_ViewBinding<T extends UpdateNickNameActivity> implements Unbinder {
  protected T target;

  private View view2131690309;

  private View view2131689954;

  private View view2131689891;

  @UiThread
  public UpdateNickNameActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    view = Utils.findRequiredView(source, R.id.text_right1, "field 'text_right1' and method 'save'");
    target.text_right1 = Utils.castView(view, R.id.text_right1, "field 'text_right1'", TextView.class);
    view2131690309 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.save();
      }
    });
    target.editText = Utils.findRequiredViewAsType(source, R.id.edit_nickname, "field 'editText'", EditText.class);
    view = Utils.findRequiredView(source, R.id.img_back, "method 'back'");
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_clear, "method 'clear'");
    view2131689891 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.clear();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.text_title = null;
    target.text_right1 = null;
    target.editText = null;

    view2131690309.setOnClickListener(null);
    view2131690309 = null;
    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689891.setOnClickListener(null);
    view2131689891 = null;

    this.target = null;
  }
}
