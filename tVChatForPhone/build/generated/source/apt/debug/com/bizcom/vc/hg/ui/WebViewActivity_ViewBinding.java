// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WebViewActivity_ViewBinding<T extends WebViewActivity> implements Unbinder {
  protected T target;

  private View view2131689954;

  @UiThread
  public WebViewActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.text_title = Utils.findRequiredViewAsType(source, R.id.text_title, "field 'text_title'", TextView.class);
    target.webView = Utils.findRequiredViewAsType(source, R.id.webview, "field 'webView'", WebView.class);
    view = Utils.findRequiredView(source, R.id.img_back, "method 'back'");
    view2131689954 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.back();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.text_title = null;
    target.webView = null;

    view2131689954.setOnClickListener(null);
    view2131689954 = null;

    this.target = null;
  }
}
