// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.flyco.tablayout.SlidingTabLayout;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SyncFrendsToTvActivity_ViewBinding<T extends SyncFrendsToTvActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  @UiThread
  public SyncFrendsToTvActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.img_back, "field 'img_back' and method 'back'");
    target.img_back = Utils.castView(view, R.id.img_back, "field 'img_back'", ImageView.class);
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    target.mSlidingTab = Utils.findRequiredViewAsType(source, R.id.slidingTab, "field 'mSlidingTab'", SlidingTabLayout.class);
    target.mPager = Utils.findRequiredViewAsType(source, R.id.mPager, "field 'mPager'", ViewPager.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_back = null;
    target.text_title = null;
    target.mSlidingTab = null;
    target.mPager = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;

    this.target = null;
  }
}
