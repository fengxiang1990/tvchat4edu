package com.bizcom.vc.widget.cus.span;

import com.bizcom.vo.meesage.VMessageLinkTextItem;

import android.text.style.ClickableSpan;
import android.view.View;

public abstract class ClickableLinkSpan extends ClickableSpan {

	private VMessageLinkTextItem link;

	public VMessageLinkTextItem getLink() {
		return link;
	}

	public void setLink(VMessageLinkTextItem link) {
		this.link = link;
	}

	public ClickableLinkSpan(VMessageLinkTextItem link) {
		this.link = link;
	}

	@Override
	public void onClick(View widget) {
		onClick(link);
	}

	public abstract void onClick(VMessageLinkTextItem link);
}
