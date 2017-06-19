package com.bizcom.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapLruCache<K> extends LruCache<K, Bitmap> {

	public BitmapLruCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
		super.entryRemoved(evicted, key, oldValue, newValue);
		if(oldValue != null && !oldValue.isRecycled()){
			oldValue.recycle();
			oldValue = null;
		}
	}
}
