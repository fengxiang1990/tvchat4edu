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

public class UpdateTvInfoActivity_ViewBinding<T extends UpdateTvInfoActivity> implements Unbinder {
  protected T target;

  private View view2131689949;

  private View view2131689950;

  private View view2131689954;

  private View view2131689948;

  private View view2131689953;

  @UiThread
  public UpdateTvInfoActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    view = Utils.findRequiredView(source, R.id.img_header2, "field 'img_header2' and method 'showPicSelection'");
    target.img_header2 = Utils.castView(view, R.id.img_header2, "field 'img_header2'", SimpleDraweeView.class);
    view2131689949 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.showPicSelection();
      }
    });
    target.text_nickname = Utils.findRequiredViewAsType(source, R.id.text_nickname, "field 'text_nickname'", TextView.class);
    target.text_tvnum = Utils.findRequiredViewAsType(source, R.id.text_tvnum, "field 'text_tvnum'", TextView.class);
    view = Utils.findRequiredView(source, R.id.img_modify_nickname, "method 'updateNickName'");
    view2131689950 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.updateNickName();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_back, "method 'back'");
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
    view = Utils.findRequiredView(source, R.id.img_modify_photo, "method 'img_modify_photo'");
    view2131689948 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.img_modify_photo();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_unbind, "method 'unbind'");
    view2131689953 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.unbind();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.text_title = null;
    target.img_header2 = null;
    target.text_nickname = null;
    target.text_tvnum = null;

    view2131689949.setOnClickListener(null);
    view2131689949 = null;
    view2131689950.setOnClickListener(null);
    view2131689950 = null;
    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689948.setOnClickListener(null);
    view2131689948 = null;
    view2131689953.setOnClickListener(null);
    view2131689953 = null;

    this.target = null;
  }
}
