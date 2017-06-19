// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SyncFrendsToTvActivity$SyncTabFragment_ViewBinding<T extends SyncFrendsToTvActivity.SyncTabFragment> implements Unbinder {
  protected T target;

  private View view2131690307;

  @UiThread
  public SyncFrendsToTvActivity$SyncTabFragment_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.ll_empty = Utils.findRequiredViewAsType(source, R.id.ll_empty, "field 'll_empty'", LinearLayout.class);
    target.recyclerView = Utils.findRequiredViewAsType(source, R.id.recyclerView, "field 'recyclerView'", RecyclerView.class);
    view = Utils.findRequiredView(source, R.id.btn_sync, "field 'btn_sync' and method 'syncClick'");
    target.btn_sync = Utils.castView(view, R.id.btn_sync, "field 'btn_sync'", Button.class);
    view2131690307 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.syncClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.ll_empty = null;
    target.recyclerView = null;
    target.btn_sync = null;

    view2131690307.setOnClickListener(null);
    view2131690307 = null;

    this.target = null;
  }
}
