package com.bizcom.vc.widget.cus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class V2ImageView extends ImageView {

	protected BitmapResultCallBack callBack;
	private boolean callOnce;
	public V2ImageView(Context context) {
		super(context);
	}

	public V2ImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public V2ImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);
		} catch (Exception e) {
			e.printStackTrace();
			if(callBack != null){
				callBack.onFailed();
			}
		}
		
		if(callBack != null && callOnce){
			callBack.onSuccess();
			callOnce = false;
		}
	}

	public void setImageBitmap(Bitmap bm, BitmapResultCallBack callBack) {
		this.callBack = callBack;
		callOnce = true;
		super.setImageBitmap(bm);
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		this.callBack = null;
		super.setImageBitmap(bm);
	}
	
	@Override
	public void setImageResource(int resId) {
		this.callBack = null;
		super.setImageResource(resId);
	}

	public interface BitmapResultCallBack {

		public void onSuccess();

		public void onFailed();
	}
}
