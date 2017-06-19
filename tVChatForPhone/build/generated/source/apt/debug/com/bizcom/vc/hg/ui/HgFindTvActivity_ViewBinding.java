// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class HgFindTvActivity_ViewBinding<T extends HgFindTvActivity> implements Unbinder {
  protected T target;

  private View view2131689890;

  private View view2131689891;

  private View view2131689892;

  private View view2131690369;

  @UiThread
  public HgFindTvActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.img_search, "field 'img_search' and method 'imgSearchClick'");
    target.img_search = Utils.castView(view, R.id.img_search, "field 'img_search'", ImageView.class);
    view2131689890 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.imgSearchClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_clear, "field 'img_clear' and method 'imgclear'");
    target.img_clear = Utils.castView(view, R.id.img_clear, "field 'img_clear'", ImageView.class);
    view2131689891 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.imgclear();
      }
    });
    target.et = Utils.findRequiredViewAsType(source, R.id.et, "field 'et'", EditText.class);
    view = Utils.findRequiredView(source, R.id.text_cancel, "field 'text_cancel' and method 'cancel'");
    target.text_cancel = Utils.castView(view, R.id.text_cancel, "field 'text_cancel'", TextView.class);
    view2131689892 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.cancel();
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_ll, "field 'tv_ll' and method 'tvllClick'");
    target.tv_ll = Utils.castView(view, R.id.tv_ll, "field 'tv_ll'", LinearLayout.class);
    view2131690369 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.tvllClick();
      }
    });
    target.tv_img = Utils.findRequiredViewAsType(source, R.id.tv_img, "field 'tv_img'", SimpleDraweeView.class);
    target.tv_name = Utils.findRequiredViewAsType(source, R.id.tv_name, "field 'tv_name'", TextView.class);
    target.tv_account = Utils.findRequiredViewAsType(source, R.id.tv_account, "field 'tv_account'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_search = null;
    target.img_clear = null;
    target.et = null;
    target.text_cancel = null;
    target.tv_ll = null;
    target.tv_img = null;
    target.tv_name = null;
    target.tv_account = null;

    view2131689890.setOnClickListener(null);
    view2131689890 = null;
    view2131689891.setOnClickListener(null);
    view2131689891 = null;
    view2131689892.setOnClickListener(null);
    view2131689892 = null;
    view2131690369.setOnClickListener(null);
    view2131690369 = null;

    this.target = null;
  }
}
