// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.flyco.tablayout.SlidingTabLayout;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class HTab0_ViewBinding<T extends HTab0> implements Unbinder {
  protected T target;

  @UiThread
  public HTab0_ViewBinding(T target, View source) {
    this.target = target;

    target.mSlidingTab = Utils.findRequiredViewAsType(source, R.id.slidingTab, "field 'mSlidingTab'", SlidingTabLayout.class);
    target.mPager = Utils.findRequiredViewAsType(source, R.id.mPager, "field 'mPager'", ViewPager.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.mSlidingTab = null;
    target.mPager = null;

    this.target = null;
  }
}
