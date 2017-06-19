package com.bizcom.vc.widget.cus.edittext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AutoCompleteTextView;

import com.bizcom.vc.widget.cus.MultilevelListView;
import com.shdx.tvchat.phone.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClearEditText extends AutoCompleteTextView implements OnFocusChangeListener, TextWatcher {
	private MultilevelListView mGroupListView;
	private Pattern mPattern;

	/**
	 * 是否需要过滤,如果该标记被设置在Tag里,说明不需要过滤.反之需要
	 */
	public static String TAG_IS_PATTERN = "pattern";

    /**
     * 表明该控件是在Contactetail中
     */
    public static String TAG_IS_CONTACT_DETAIL = "contactdetail";

	/**
	 * 过滤的字符
	 */
	private String mRegexPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
	
	/**
	 * 不需要过滤的特殊字符串
	 */
	private String mSpecificChars = "";

	/**
	 * 删除按钮的引用
	 */
	private Drawable mClearDrawable;
	/**
	 * 控件是否有焦点
	 */
	private boolean hasFoucs;

	/**
	 * 判断是否需要清除图标
	 */
	private boolean hasClear;

	/**
	 * 判断是否需要显示清除图标
	 */
	private boolean hasShowClear = true;

    private TextWatcherCallBack mTextWatcher;

    private String mTag;

    public ClearEditText(Context context) {
		this(context, null);
	}

	public ClearEditText(Context context, AttributeSet attrs) {
		// 这里构造方法也很重要，不加这个很多属性不能再XML里面定义
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mPattern = Pattern.compile(mRegexPattern);
        // 个人资料界面不需要清除图标
        Object tag = getTag();
        if (tag != null) {
            mTag = (String) tag;
        }

        if (mTag != null && mTag.equals(TAG_IS_CONTACT_DETAIL)) {
            hasClear = false;
        } else {
            hasClear = true;
            // 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
            mClearDrawable = getCompoundDrawables()[2];
            if (mClearDrawable == null) {
                mClearDrawable = getResources().getDrawable(R.drawable.txt_search_clear);
            }
            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        }
		// 默认设置隐藏图标
		setClearIconVisible(false);
		// 设置焦点改变的监听
		setOnFocusChangeListener(this);
		// 设置输入框里面内容发生改变的监听
		addTextChangedListener(this);
		performClick();
	}

	/**
	 * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件 当我们按下的位置 在 EditText的宽度 -
	 * 图标到控件右边的间距 - 图标的宽度 和 EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mClearDrawable == null || !isEnabled()) {
				return super.onTouchEvent(event);
			}

			boolean touchable = (event.getX() < (getWidth() - getPaddingRight() + mClearDrawable.getIntrinsicWidth())
					&& event.getX() > (getWidth() - getTotalPaddingRight() - mClearDrawable.getIntrinsicWidth()));

			if (touchable) {
				this.setText("");
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		this.hasFoucs = hasFocus;
        String nothing = getResources().getString(R.string.contacts_user_detail_item_nothing);
		if (hasFocus) {
			String text = getText().toString().trim();
			if (TextUtils.isEmpty(text))
				setClearIconVisible(false);
			else {
                if (mTag != null && mTag.equals(TAG_IS_CONTACT_DETAIL)) {
                    String content = getText().toString();
                    if(content.equals(nothing)){
                        setText("");
                    }
                } else {
                    setClearIconVisible(true);
                }
			}
		} else {
			setClearIconVisible(false);
            if (mTag != null && mTag.equals(TAG_IS_CONTACT_DETAIL)) {
                if(TextUtils.isEmpty(getText())){
                    setText(nothing);
                }
            }
		}
	}

	/**
	 * 当输入框里面内容发生变化的时候回调的方法
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		boolean flag = false;
		CharSequence beforeFilte = s;
		// 删除的图标
		if (hasFoucs && hasClear) {
			setClearIconVisible(!TextUtils.isEmpty(s.toString().trim()));
		}

		// 过滤emoji
		CharSequence addedString = s.subSequence(start, start + after);
		if (!TextUtils.isEmpty(addedString)) {
			boolean containsEmoji = containsEmoji(addedString.toString());
			if (containsEmoji) {
				flag = true;
				s = s.toString().replace(addedString, "");
			}
		}
		// 是否需要过滤
		if (mPattern != null && (mTag == null || !mTag.equals(TAG_IS_PATTERN))) {
			if (!TextUtils.isEmpty(s) && !s.toString().equals(mSpecificChars)) {
				Matcher m = mPattern.matcher(s);
				while (m.find()) {
					flag = true;
					s = s.toString().replace(m.group(), "");
				}
			}

			if (mGroupListView != null) {
				if (s.toString().isEmpty()) {
					mGroupListView.clearTextFilter();
				} else {
					mGroupListView.setFilterText(s.toString());
				}
			}

			if (flag && !beforeFilte.equals(s)) {
				setText(s);
				setSelection(s.length());
			}
		}

        if(mTextWatcher != null){
            mTextWatcher.onTextChangedCallBack(this , s , start , count , after);
        }
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	public void addTextListener(MultilevelListView mGroupListView) {
		this.mGroupListView = mGroupListView;
	}
	
	public void setSpecificChars(String mSpecificChars) {
		this.mSpecificChars = mSpecificChars;
	}
	
	public void setRegexPattern(String mRegexPattern) {
		this.mRegexPattern = mRegexPattern;
		mPattern = Pattern.compile(mRegexPattern);
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 * 
	 * @param visible
	 */
	public void setClearIconVisible(boolean visible) {
		Drawable right = null;
		if (hasShowClear && visible) {
			right = mClearDrawable;
		}
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
		getCompoundDrawables();
	}

	public void setHasFoucs(boolean hasFoucs) {
		this.hasFoucs = hasFoucs;
	}

	public void setHasShowClear(boolean hasShowClear) {
		this.hasShowClear = hasShowClear;
	}

    public void setTextWatcher(TextWatcherCallBack mTextWatcher) {
        this.mTextWatcher = mTextWatcher;
    }

    @Override
    public void setTag(Object tag) {
        mTag = (String) tag;
        super.setTag(tag);
    }

    /**
	 * 检测是否有emoji字符
	 * 
	 * @param source
	 * @return
	 */
	public static boolean containsEmoji(String source) {
		int len = source.length();
		for (int i = 0; i < len; i++) {
			Character codePoint = source.charAt(i);
			if (isEmojiCharacter(codePoint)) {
				// do nothing，判断到了这里表明，确认有表情字符
				return true;
			}
		}
		return false;
	}

	private static boolean isEmojiCharacter(char codePoint) {
		return (codePoint == 9786 || codePoint == 55357 || codePoint == 9995 || codePoint == 9994 || codePoint == 9757
				|| codePoint == 56840 || codePoint == 10060 || codePoint == 10093 || codePoint == 9728
				|| codePoint == 9748 || codePoint == 10024 || codePoint == 11088 || codePoint == 9889
				|| codePoint == 9729 || codePoint == 9924 || codePoint == 10071 || codePoint == 10067
				|| codePoint == 10084 || codePoint == 9996 || codePoint == 11093 || codePoint == 9917
				|| codePoint == 55356);
	}

    public interface TextWatcherCallBack{

        void onTextChangedCallBack(ClearEditText editText , CharSequence s, int start, int count, int after);
    }
}
