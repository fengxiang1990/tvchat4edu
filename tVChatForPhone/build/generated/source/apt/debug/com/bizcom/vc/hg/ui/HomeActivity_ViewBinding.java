// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class HomeActivity_ViewBinding<T extends HomeActivity> implements Unbinder {
  protected T target;

  @UiThread
  public HomeActivity_ViewBinding(T target, View source) {
    this.target = target;

    target.rb0 = Utils.findRequiredViewAsType(source, R.id.rb0, "field 'rb0'", RadioButton.class);
    target.rb1 = Utils.findRequiredViewAsType(source, R.id.rb1, "field 'rb1'", RadioButton.class);
    target.rb2 = Utils.findRequiredViewAsType(source, R.id.rb2, "field 'rb2'", RadioButton.class);
    target.rb3 = Utils.findRequiredViewAsType(source, R.id.rb3, "field 'rb3'", RadioButton.class);
    target.content = Utils.findRequiredViewAsType(source, R.id.content, "field 'content'", FrameLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.rb0 = null;
    target.rb1 = null;
    target.rb2 = null;
    target.rb3 = null;
    target.content = null;

    this.target = null;
  }
}
