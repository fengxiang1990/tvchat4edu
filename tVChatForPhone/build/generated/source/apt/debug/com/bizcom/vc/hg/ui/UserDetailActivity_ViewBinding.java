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

public class UserDetailActivity_ViewBinding<T extends UserDetailActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  private View view2131689961;

  private View view2131689964;

  private View view2131689967;

  private View view2131689968;

  @UiThread
  public UserDetailActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.img_back, "field 'imgBack' and method 'onClick'");
    target.imgBack = Utils.castView(view, R.id.img_back, "field 'imgBack'", ImageView.class);
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    target.textTitle = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'textTitle'", TextView.class);
    target.imgHeader = Utils.findRequiredViewAsType(source, R.id.img_header, "field 'imgHeader'", SimpleDraweeView.class);
    target.textName = Utils.findRequiredViewAsType(source, R.id.text_name, "field 'textName'", TextView.class);
    target.textAccount = Utils.findRequiredViewAsType(source, R.id.text_account, "field 'textAccount'", TextView.class);
    target.textRemark = Utils.findRequiredViewAsType(source, R.id.text_remark, "field 'textRemark'", TextView.class);
    view = Utils.findRequiredView(source, R.id.ll_remark, "field 'llRemark' and method 'onClick'");
    target.llRemark = Utils.castView(view, R.id.ll_remark, "field 'llRemark'", LinearLayout.class);
    view2131689961 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_sendTotv, "field 'llSendTotv' and method 'onClick'");
    target.llSendTotv = Utils.castView(view, R.id.ll_sendTotv, "field 'llSendTotv'", LinearLayout.class);
    view2131689964 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.button_delete, "field 'buttonDelete' and method 'onClick'");
    target.buttonDelete = Utils.castView(view, R.id.button_delete, "field 'buttonDelete'", TextView.class);
    view2131689967 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.button_call, "field 'buttonCall' and method 'onClick'");
    target.buttonCall = Utils.castView(view, R.id.button_call, "field 'buttonCall'", TextView.class);
    view2131689968 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    target.viewLine2 = Utils.findRequiredView(source, R.id.view_line2, "field 'viewLine2'");
    target.viewLine1 = Utils.findRequiredView(source, R.id.view_line1, "field 'viewLine1'");
    target.textTvDelete = Utils.findRequiredViewAsType(source, R.id.text_tv_delete, "field 'textTvDelete'", TextView.class);
    target.firstNameText = Utils.findRequiredViewAsType(source, R.id.firstNameText, "field 'firstNameText'", TextView.class);
    target.editRemark = Utils.findRequiredViewAsType(source, R.id.edit_remark, "field 'editRemark'", EditText.class);
    target.llRemarkEdit = Utils.findRequiredViewAsType(source, R.id.ll_remark_edit, "field 'llRemarkEdit'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.imgBack = null;
    target.textTitle = null;
    target.imgHeader = null;
    target.textName = null;
    target.textAccount = null;
    target.textRemark = null;
    target.llRemark = null;
    target.llSendTotv = null;
    target.buttonDelete = null;
    target.buttonCall = null;
    target.viewLine2 = null;
    target.viewLine1 = null;
    target.textTvDelete = null;
    target.firstNameText = null;
    target.editRemark = null;
    target.llRemarkEdit = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689961.setOnClickListener(null);
    view2131689961 = null;
    view2131689964.setOnClickListener(null);
    view2131689964 = null;
    view2131689967.setOnClickListener(null);
    view2131689967 = null;
    view2131689968.setOnClickListener(null);
    view2131689968 = null;

    this.target = null;
  }
}
