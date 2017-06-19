// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SecondTab1_ViewBinding<T extends SecondTab1> implements Unbinder {
  protected T target;

  private View view2131690313;

  @UiThread
  public SecondTab1_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.viewEmpty = Utils.findRequiredViewAsType(source, R.id.view_empty, "field 'viewEmpty'", LinearLayout.class);
    target.loadingGif = Utils.findRequiredViewAsType(source, R.id.loading_gif, "field 'loadingGif'", SimpleDraweeView.class);
    view = Utils.findRequiredView(source, R.id.button_add_friend, "method 'onClick'");
    view2131690313 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.viewEmpty = null;
    target.loadingGif = null;

    view2131690313.setOnClickListener(null);
    view2131690313 = null;

    this.target = null;
  }
}
