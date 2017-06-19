// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.flyco.tablayout.SlidingTabLayout;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SearchFriendActivity_ViewBinding<T extends SearchFriendActivity> implements Unbinder {
  protected T target;

  private View view2131689891;

  private View view2131689892;

  @UiThread
  public SearchFriendActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.mInputSearch = Utils.findRequiredViewAsType(source, R.id.et, "field 'mInputSearch'", EditText.class);
    target.slidingTab = Utils.findRequiredViewAsType(source, R.id.slidingTab, "field 'slidingTab'", SlidingTabLayout.class);
    target.mPager = Utils.findRequiredViewAsType(source, R.id.mPager, "field 'mPager'", ViewPager.class);
    view = Utils.findRequiredView(source, R.id.img_clear, "method 'onClick'");
    view2131689891 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.text_cancel, "method 'onClick'");
    view2131689892 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.mInputSearch = null;
    target.slidingTab = null;
    target.mPager = null;

    view2131689891.setOnClickListener(null);
    view2131689891 = null;
    view2131689892.setOnClickListener(null);
    view2131689892 = null;

    this.target = null;
  }
}
