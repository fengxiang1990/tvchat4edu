package com.bizcom.vc.widget.cus.span;

import com.bizcom.vo.meesage.VMessageImageItem;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public abstract class ClickableImageSpan extends ImageSpan {
	private VMessageImageItem imageItem;

	public VMessageImageItem getImageItem() {
		return imageItem;
	}

	public void setImageItem(VMessageImageItem imageItem) {
		this.imageItem = imageItem;
	}

	public ClickableImageSpan(Drawable b, VMessageImageItem imageItem) {
		super(b);
		this.imageItem = imageItem;
	}

	public abstract void onClick(VMessageImageItem imageItem);
}
