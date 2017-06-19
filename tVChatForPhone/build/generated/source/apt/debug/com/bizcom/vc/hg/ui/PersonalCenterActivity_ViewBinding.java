// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PersonalCenterActivity_ViewBinding<T extends PersonalCenterActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  private View view2131689772;

  private View view2131689765;

  private View view2131689779;

  private View view2131689780;

  private View view2131689782;

  private View view2131689783;

  private View view2131689784;

  @UiThread
  public PersonalCenterActivity_ViewBinding(final T target, View source) {
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
    target.user_head_icon = Utils.findRequiredViewAsType(source, R.id.user_head_icon, "field 'user_head_icon'", SimpleDraweeView.class);
    target.text_friend_num = Utils.findRequiredViewAsType(source, R.id.text_friend_num, "field 'text_friend_num'", TextView.class);
    target.text_talk_minute_num = Utils.findRequiredViewAsType(source, R.id.text_talk_minute_num, "field 'text_talk_minute_num'", TextView.class);
    target.text_contact_always_num = Utils.findRequiredViewAsType(source, R.id.text_contact_always_num, "field 'text_contact_always_num'", TextView.class);
    target.text_version = Utils.findRequiredViewAsType(source, R.id.text_version, "field 'text_version'", TextView.class);
    target.text_msg_num = Utils.findRequiredViewAsType(source, R.id.text_msg_num, "field 'text_msg_num'", TextView.class);
    view = Utils.findRequiredView(source, R.id.img_next, "method 'next'");
    view2131689772 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.next();
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_edit, "method 'edit'");
    view2131689765 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.edit();
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_modify_pwd, "method 'modifyPwd'");
    view2131689779 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.modifyPwd();
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_message, "method 'messageClick'");
    view2131689780 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.messageClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_help, "method 'helpClick'");
    view2131689782 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.helpClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_about, "method 'aboutClick'");
    view2131689783 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.aboutClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_customer_service, "method 'customerServiceClick'");
    view2131689784 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.customerServiceClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_back = null;
    target.user_head_icon = null;
    target.text_friend_num = null;
    target.text_talk_minute_num = null;
    target.text_contact_always_num = null;
    target.text_version = null;
    target.text_msg_num = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;
    view2131689772.setOnClickListener(null);
    view2131689772 = null;
    view2131689765.setOnClickListener(null);
    view2131689765 = null;
    view2131689779.setOnClickListener(null);
    view2131689779 = null;
    view2131689780.setOnClickListener(null);
    view2131689780 = null;
    view2131689782.setOnClickListener(null);
    view2131689782 = null;
    view2131689783.setOnClickListener(null);
    view2131689783 = null;
    view2131689784.setOnClickListener(null);
    view2131689784 = null;

    this.target = null;
  }
}
