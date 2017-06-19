// Generated code from Butter Knife. Do not modify!
package com.bizcom.vc.hg.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.shdx.tvchat.phone.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SearchFriendTab1_ViewBinding<T extends SearchFriendTab1> implements Unbinder {
  protected T target;

  @UiThread
  public SearchFriendTab1_ViewBinding(T target, View source) {
    this.target = target;

    target.textEmpty = Utils.findRequiredViewAsType(source, R.id.text_empty, "field 'textEmpty'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.textEmpty = null;

    this.target = null;
  }
}
