// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

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

public class SyncFrendsToTvActivity$MyViewHolder_ViewBinding<T extends SyncFrendsToTvActivity.MyViewHolder> implements Unbinder {
  protected T target;

  @UiThread
  public SyncFrendsToTvActivity$MyViewHolder_ViewBinding(T target, View source) {
    this.target = target;

    target.img_pic = Utils.findRequiredViewAsType(source, R.id.img_pic, "field 'img_pic'", SimpleDraweeView.class);
    target.img_check = Utils.findRequiredViewAsType(source, R.id.img_check, "field 'img_check'", ImageView.class);
    target.text_name = Utils.findRequiredViewAsType(source, R.id.text_name, "field 'text_name'", TextView.class);
    target.text_account = Utils.findRequiredViewAsType(source, R.id.text_account, "field 'text_account'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_pic = null;
    target.img_check = null;
    target.text_name = null;
    target.text_account = null;

    this.target = null;
  }
}
