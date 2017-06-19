package com.bizcom.vc.adapter.view;

import com.bizcom.util.DensityUtils;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Administrator on 2015/4/30.
 */
public class SipNumberImageView extends ImageView {
	private Paint mPressPaint;

	private int mWidth;
	private int mHeight;

	private int mPressAlpha;
	private int mPressColor;
	private int mRadius;
	private int mShapeType;
	private int mBorderWidth;
	private int mBorderColor;
	private Rect rect = new Rect();

	public String count;
	private boolean isDrawing;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(isDrawing){
				return ;
			}
			if(msg.what == MotionEvent.ACTION_DOWN){
				mPressPaint.setAlpha(mPressAlpha);
				invalidate();
			} else if(msg.what == MotionEvent.ACTION_UP){
				mPressPaint.setAlpha(0);
				invalidate();	
			}
		}
	};

	public SipNumberImageView(Context context) {
		super(context);
		init(context, null);
	}

	public SipNumberImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SipNumberImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		// 初始化默认值
		mPressAlpha = 64;
		mPressColor = getResources().getColor(R.color.sip_number_press);
		mRadius = 16;
		mShapeType = 1;
		mBorderWidth = 0;
		mBorderColor = getResources().getColor(R.color.common_item_text_color_gray);
		// 获取控件的属性值
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MLImageView);
			mPressColor = array.getColor(R.styleable.MLImageView_press_color, mPressColor);
			mPressAlpha = array.getInteger(R.styleable.MLImageView_press_alpha, mPressAlpha);
			mRadius = array.getDimensionPixelSize(R.styleable.MLImageView_radius, mRadius);
			mShapeType = array.getInteger(R.styleable.MLImageView_shape_type, mShapeType);
			mBorderWidth = array.getDimensionPixelOffset(R.styleable.MLImageView_border_width, mBorderWidth);
			mBorderColor = array.getColor(R.styleable.MLImageView_border_color, mBorderColor);
			array.recycle();
		}

		// 按下的画笔设置
		mPressPaint = new Paint();
		mPressPaint.setAntiAlias(true);
		mPressPaint.setStyle(Paint.Style.FILL);
		mPressPaint.setColor(mPressColor);
		mPressPaint.setAlpha(0);
		mPressPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

		setClickable(true);
		setDrawingCacheEnabled(true);
		setWillNotDraw(false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		getLocalVisibleRect(rect);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		isDrawing = true;
		// super.onDraw(canvas);
		// 获取当前控件的 drawable
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}
		// 这里 get 回来的宽度和高度是当前控件相对应的宽度和高度（在 xml 设置）
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		// 获取 bitmap，即传入 imageview 的 bitmap
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		drawDrawable(canvas, bitmap);
		drawTop(canvas);
		drawPress(canvas);
		drawBorder(canvas);
		isDrawing = false;
	}

	private Paint circlePaint;
	private Paint numberPaint;

	private void drawTop(Canvas canvas) {
		if (circlePaint == null)
			circlePaint = new Paint();
		if (getId() == R.id.sip_number_call) {
			circlePaint.setColor(Color.GREEN);
		} else {
			circlePaint.setColor(Color.WHITE);
		}
		circlePaint.setStyle(Paint.Style.FILL);
		circlePaint.setAntiAlias(true);
		circlePaint.setStrokeWidth(2);

		canvas.drawCircle(getLeft() + mWidth / 2, getTop() + mHeight / 2,
				mWidth / 2 - DensityUtils.dip2px(getContext(), 1), circlePaint);
		if (numberPaint == null)
			numberPaint = new Paint();
		numberPaint.setColor(Color.BLUE);
		numberPaint.setAntiAlias(true);
		numberPaint.setDither(true);
		if (getId() == R.id.sip_number_call) {
			numberPaint.setTextSize(DensityUtils.sp2px(getContext(), 24));
		} else {
			numberPaint.setTextSize(DensityUtils.sp2px(getContext(), 40));
		}
		Typeface createFromAsset = Typeface.createFromAsset(getContext().getAssets(), "number_fornt.otf");
		numberPaint.setTypeface(createFromAsset);

		if (getId() == R.id.sip_number_call) {
			canvas.drawText(count, rect.right - mWidth / 2 - DensityUtils.dip2px(getContext(), 24),
					rect.bottom - mHeight / 2 + DensityUtils.dip2px(getContext(), 9), numberPaint);
		} else if (getId() == R.id.sip_number_minus) {
			canvas.drawText(count, rect.right - mWidth / 2 - DensityUtils.dip2px(getContext(), 7),
					rect.bottom - mHeight / 2 + DensityUtils.dip2px(getContext(), 7), numberPaint);
		} else {
			canvas.drawText(count, rect.right - mWidth / 2 - DensityUtils.dip2px(getContext(), 10),
					rect.bottom - mHeight / 2 + DensityUtils.dip2px(getContext(), 12), numberPaint);
		}
	}

	private void drawDrawable(Canvas canvas, Bitmap bitmap) {
		// 画笔
		Paint paint = new Paint();
		// 颜色设置
		paint.setColor(0xffffffff);
		// 抗锯齿
		paint.setAntiAlias(true);
		// Paint 的 Xfermode，PorterDuff.Mode.SRC_IN 取两层图像的交集部门, 只显示上层图像。
		PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
		// 标志
		int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
				| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
		canvas.saveLayer(0, 0, mWidth, mHeight, null, saveFlags);

		if (mShapeType == 0) {
			// 画遮罩，画出来就是一个和空间大小相匹配的圆
			canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, paint);
		} else {
			// 当ShapeType = 1 时 图片为圆角矩形
			RectF rectf = new RectF(0, 0, getWidth(), getHeight());
			canvas.drawRoundRect(rectf, mRadius, mRadius, paint);
		}

		paint.setXfermode(xfermode);

		// 空间的大小 / bitmap 的大小 = bitmap 缩放的倍数
		float scaleWidth = ((float) getWidth()) / bitmap.getWidth();
		float scaleHeight = ((float) getHeight()) / bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// bitmap 缩放
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		// draw 上去
		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.restore();
	}

	private void drawPress(Canvas canvas) {

		if (mShapeType == 0) {
			canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mPressPaint);
		} else if (mShapeType == 1) {
			RectF rectF = new RectF(0, 0, mWidth, mHeight);
			canvas.drawRoundRect(rectF, mRadius, mRadius, mPressPaint);
		}
	}

	private void drawBorder(Canvas canvas) {
		if (mBorderWidth > 0) {
			Paint paint = new Paint();
			paint.setStrokeWidth(mBorderWidth);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(mBorderColor);
			paint.setAntiAlias(true);
			if (mShapeType == 0) {
				canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, paint);
			} else {
				// 当ShapeType = 1 时 图片为圆角矩形
				RectF rectf = new RectF(0, 0, getWidth(), getHeight());
				canvas.drawRoundRect(rectf, mRadius, mRadius, paint);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHandler.obtainMessage(MotionEvent.ACTION_DOWN).sendToTarget();
			break;
		case MotionEvent.ACTION_UP:
			mHandler.obtainMessage(MotionEvent.ACTION_UP).sendToTarget();
			break;
		}
		return super.onTouchEvent(event);
	}

}
