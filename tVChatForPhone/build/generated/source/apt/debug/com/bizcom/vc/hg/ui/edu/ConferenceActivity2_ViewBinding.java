// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui.edu;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ConferenceActivity2_ViewBinding<T extends ConferenceActivity2> implements Unbinder {
  protected T target;

  private View view2131689835;

  private View view2131689836;

  private View view2131689837;

  private View view2131689840;

  private View view2131689839;

  private View view2131689841;

  @UiThread
  public ConferenceActivity2_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.img_header = Utils.findRequiredViewAsType(source, R.id.img_header, "field 'img_header'", SimpleDraweeView.class);
    target.video_headers_layout = Utils.findRequiredViewAsType(source, R.id.video_headers_layout, "field 'video_headers_layout'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.text_jingyin, "field 'text_jingyin' and method 'text_jingyin'");
    target.text_jingyin = Utils.castView(view, R.id.text_jingyin, "field 'text_jingyin'", TextView.class);
    view2131689835 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.text_jingyin();
      }
    });
    view = Utils.findRequiredView(source, R.id.text_touping, "field 'text_text_touping' and method 'text_touping'");
    target.text_text_touping = Utils.castView(view, R.id.text_touping, "field 'text_text_touping'", TextView.class);
    view2131689836 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.text_touping();
      }
    });
    view = Utils.findRequiredView(source, R.id.text_manage_attendee, "field 'text_manage_attendee' and method 'text_manage_attendee'");
    target.text_manage_attendee = Utils.castView(view, R.id.text_manage_attendee, "field 'text_manage_attendee'", TextView.class);
    view2131689837 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.text_manage_attendee();
      }
    });
    view = Utils.findRequiredView(source, R.id.text_gd, "field 'text_gd' and method 'gd'");
    target.text_gd = Utils.castView(view, R.id.text_gd, "field 'text_gd'", TextView.class);
    view2131689840 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.gd();
      }
    });
    view = Utils.findRequiredView(source, R.id.text_jy, "field 'text_jy' and method 'text_jy'");
    target.text_jy = Utils.castView(view, R.id.text_jy, "field 'text_jy'", TextView.class);
    view2131689839 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.text_jy();
      }
    });
    target.text_close_camera = Utils.findRequiredViewAsType(source, R.id.text_close_camera, "field 'text_close_camera'", TextView.class);
    view = Utils.findRequiredView(source, R.id.text_voice_manager, "field 'text_voice_manager' and method 'text_voice_manager'");
    target.text_voice_manager = Utils.castView(view, R.id.text_voice_manager, "field 'text_voice_manager'", TextView.class);
    view2131689841 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.text_voice_manager();
      }
    });
    target.text_username = Utils.findRequiredViewAsType(source, R.id.text_username, "field 'text_username'", TextView.class);
    target.text_course_name = Utils.findRequiredViewAsType(source, R.id.text_course_name, "field 'text_course_name'", TextView.class);
    target.text_talk_minute = Utils.findRequiredViewAsType(source, R.id.text_talk_minute, "field 'text_talk_minute'", TextView.class);
    target.ll_top = Utils.findRequiredViewAsType(source, R.id.ll_top, "field 'll_top'", LinearLayout.class);
    target.ll_right_remotes = Utils.findOptionalViewAsType(source, R.id.ll_right_remotes, "field 'll_right_remotes'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.img_header = null;
    target.video_headers_layout = null;
    target.text_jingyin = null;
    target.text_text_touping = null;
    target.text_manage_attendee = null;
    target.text_gd = null;
    target.text_jy = null;
    target.text_close_camera = null;
    target.text_voice_manager = null;
    target.text_username = null;
    target.text_course_name = null;
    target.text_talk_minute = null;
    target.ll_top = null;
    target.ll_right_remotes = null;

    view2131689835.setOnClickListener(null);
    view2131689835 = null;
    view2131689836.setOnClickListener(null);
    view2131689836 = null;
    view2131689837.setOnClickListener(null);
    view2131689837 = null;
    view2131689840.setOnClickListener(null);
    view2131689840 = null;
    view2131689839.setOnClickListener(null);
    view2131689839 = null;
    view2131689841.setOnClickListener(null);
    view2131689841 = null;

    this.target = null;
  }
}
