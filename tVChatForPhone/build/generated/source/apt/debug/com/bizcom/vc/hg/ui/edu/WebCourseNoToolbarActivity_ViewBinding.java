// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui.edu;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WebCourseNoToolbarActivity_ViewBinding<T extends WebCourseNoToolbarActivity> implements Unbinder {
  protected T target;

  @UiThread
  public WebCourseNoToolbarActivity_ViewBinding(T target, View source) {
    this.target = target;

    target.webview = Utils.findRequiredViewAsType(source, R.id.webview, "field 'webview'", BridgeWebView.class);
    target.imgview = Utils.findRequiredViewAsType(source, R.id.imgview, "field 'imgview'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.webview = null;
    target.imgview = null;

    this.target = null;
  }
}
