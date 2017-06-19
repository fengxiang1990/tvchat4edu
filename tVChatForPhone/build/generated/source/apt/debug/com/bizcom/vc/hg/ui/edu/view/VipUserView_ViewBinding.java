// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui.edu.view;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class VipUserView_ViewBinding<T extends VipUserView> implements Unbinder {
  protected T target;

  @UiThread
  public VipUserView_ViewBinding(T target, View source) {
    this.target = target;

    target.img_vip = Utils.findRequiredViewAsType(source, R.id.img_vip, "field 'img_vip'", SimpleDraweeView.class);
    target.text_vip_name = Utils.findRequiredViewAsType(source, R.id.text_vip_name, "field 'text_vip_name'", TextView.class);
    target.text_vip_status = Utils.findRequiredViewAsType(source, R.id.text_vip_status, "field 'text_vip_status'", TextView.class);
    target.img_vip_status = Utils.findRequiredViewAsType(source, R.id.img_vip_status, "field 'img_vip_status'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_vip = null;
    target.text_vip_name = null;
    target.text_vip_status = null;
    target.img_vip_status = null;

    this.target = null;
  }
}
