// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ThirdTabBinded_ViewBinding<T extends ThirdTabBinded> implements Unbinder {
  protected T target;

  private View view2131690307;

  private View view2131690378;

  @UiThread
  public ThirdTabBinded_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.textTvId = Utils.findRequiredViewAsType(source, R.id.tvTVid, "field 'textTvId'", TextView.class);
    target.text_tv_id = Utils.findRequiredViewAsType(source, R.id.text_tv_id, "field 'text_tv_id'", TextView.class);
    target.img_tv_header = Utils.findRequiredViewAsType(source, R.id.img_tv_header, "field 'img_tv_header'", SimpleDraweeView.class);
    target.text_sync = Utils.findRequiredViewAsType(source, R.id.text_sync, "field 'text_sync'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_sync, "method 'sync'");
    view2131690307 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.sync();
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_detail, "method 'bindTvDetail'");
    view2131690378 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.bindTvDetail();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.textTvId = null;
    target.text_tv_id = null;
    target.img_tv_header = null;
    target.text_sync = null;

    view2131690307.setOnClickListener(null);
    view2131690307 = null;
    view2131690378.setOnClickListener(null);
    view2131690378 = null;

    this.target = null;
  }
}
