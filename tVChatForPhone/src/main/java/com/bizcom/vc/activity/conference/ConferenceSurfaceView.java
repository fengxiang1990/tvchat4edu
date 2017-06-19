package com.bizcom.vc.activity.conference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.bizcom.util.V2Log;

public class ConferenceSurfaceView extends SurfaceView {

	public ConferenceSurfaceView(Context context) {
		super(context);
		init();
	}

	public ConferenceSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ConferenceSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	public void clearScreen() {
		// 避免不可预知的问题，绘图函数用try...catch处理
		Canvas canvas = null;
		try {
			canvas = getHolder().lockCanvas();// 锁住画布
			if (canvas != null) {
				/** 在此处绘图 */
				canvas.drawColor(Color.BLACK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// // 防止绘图过程出错而导致直接跳过“提交画布并解锁”，所以在finally中提交画布并解锁
		// // 如果不“提交画布并解锁”，下次锁住画布时会出现异常：画布未解锁
		finally {
			if (canvas != null)
				getHolder().unlockCanvasAndPost(canvas);// 解锁并提交画布
		}
	}
}
