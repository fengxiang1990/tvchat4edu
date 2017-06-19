// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SecondTab3_ViewBinding<T extends SecondTab3> implements Unbinder {
  protected T target;

  @UiThread
  public SecondTab3_ViewBinding(T target, View source) {
    this.target = target;

    target.imageNoContactPermission = Utils.findRequiredViewAsType(source, R.id.image_no_contact_permission, "field 'imageNoContactPermission'", ImageView.class);
    target.viewContact = Utils.findRequiredViewAsType(source, R.id.view_contact, "field 'viewContact'", FrameLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.imageNoContactPermission = null;
    target.viewContact = null;

    this.target = null;
  }
}
