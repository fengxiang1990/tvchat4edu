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

public class HgAddFriendActivity_ViewBinding<T extends HgAddFriendActivity> implements Unbinder {
  protected T target;

  private View view2131689890;

  private View view2131689891;

  private View view2131689892;

  private View view2131690352;

  private View view2131690366;

  private View view2131690368;

  @UiThread
  public HgAddFriendActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.imageAvatar = Utils.findRequiredViewAsType(source, R.id.image_avatar, "field 'imageAvatar'", SimpleDraweeView.class);
    target.textName = Utils.findRequiredViewAsType(source, R.id.text_name, "field 'textName'", TextView.class);
    target.textPhone = Utils.findRequiredViewAsType(source, R.id.text_phone, "field 'textPhone'", TextView.class);
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
    view = Utils.findRequiredView(source, R.id.view_add_friend, "field 'viewAddFriend' and method 'onClick'");
    target.viewAddFriend = Utils.castView(view, R.id.view_add_friend, "field 'viewAddFriend'", LinearLayout.class);
    view2131690352 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick();
      }
    });
    target.imageIcon = Utils.findRequiredViewAsType(source, R.id.image_icon, "field 'imageIcon'", ImageView.class);
    target.textEmpty = Utils.findRequiredViewAsType(source, R.id.text_empty, "field 'textEmpty'", TextView.class);
    target.firstNameText = Utils.findRequiredViewAsType(source, R.id.firstNameText, "field 'firstNameText'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_delete, "method 'btn_delete'");
    view2131690366 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.btn_delete();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_search, "method 'btn_search'");
    view2131690368 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.btn_search();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.imageAvatar = null;
    target.textName = null;
    target.textPhone = null;
    target.img_search = null;
    target.img_clear = null;
    target.et = null;
    target.text_cancel = null;
    target.viewAddFriend = null;
    target.imageIcon = null;
    target.textEmpty = null;
    target.firstNameText = null;

    view2131689890.setOnClickListener(null);
    view2131689890 = null;
    view2131689891.setOnClickListener(null);
    view2131689891 = null;
    view2131689892.setOnClickListener(null);
    view2131689892 = null;
    view2131690352.setOnClickListener(null);
    view2131690352 = null;
    view2131690366.setOnClickListener(null);
    view2131690366 = null;
    view2131690368.setOnClickListener(null);
    view2131690368 = null;

    this.target = null;
  }
}
