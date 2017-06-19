package com.bizcom.vc.activity;

import com.bizcom.vc.adapter.view.SipNumberImageView;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.User;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SipActivity extends BaseActivity {

	private ClearEditText sipInputET;
	private SipNumberImageView mNumberOne;
	private SipNumberImageView mNumberTwo;
	private SipNumberImageView mNumberThree;
	private SipNumberImageView mNumberFour;
	private SipNumberImageView mNumberFive;
	private SipNumberImageView mNumberSix;
	private SipNumberImageView mNumberSeven;
	private SipNumberImageView mNumberEight;
	private SipNumberImageView mNumberNine;
	private SipNumberImageView mNumberZero;
	private SipNumberImageView mNumberStar;
	private SipNumberImageView mNumberSp;
	private SipNumberImageView mNumberAdd;
	private SipNumberImageView mNumberMinus;
	private SipNumberImageView mNumberCall;
	private Context mContext;
	private Bitmap bitmapDrawable;
	private OnClickListener mNumberButtonClickListener = new NumberButtonClickListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
		setContentView(R.layout.tab_fragment_sip);
		setNeedAvatar(false);
		setNeedBroadcast(false);
		setNeedHandler(false);
		super.onCreate(savedInstanceState);
		mContext = this;
		initNumberView();
	}

	@Override
	public void addBroadcast(IntentFilter filter) {

	}

	@Override
	public void receiveBroadcast(Intent intent) {

	}

	@Override
	public void receiveMessage(Message msg) {

	}

	@Override
	public void initViewAndListener() {
		TextView titleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
		titleContent.setText(getText(R.string.title_bar_item_sip));
		TextView returnBackTV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		setComBackTextTV(returnBackTV);
		returnBackTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);

		sipInputET = (ClearEditText) findViewById(R.id.sip_text_edit);
		sipInputET.setTag(ClearEditText.TAG_IS_PATTERN);
		TextView sipCall = (TextView) findViewById(R.id.sip_call);
		sipCall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String target = sipInputET.getText().toString().trim();
				if (!TextUtils.isEmpty(target)) {
					Intent iv = new Intent(mContext, ConversationP2PAVActivity.class);
					iv.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					iv.putExtra("sipNumber", target);
					iv.putExtra("is_coming_call", false);
					iv.putExtra("voice", true);
					iv.putExtra("sip", true);
					mContext.startActivity(iv);
					sipInputET.setText("");
				} else {
					Toast.makeText(mContext, getText(R.string.sip_activity_address).toString(), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

	};

	private void initNumberView() {
		mNumberOne = (SipNumberImageView) findViewById(R.id.sip_number_one);
		mNumberTwo = (SipNumberImageView) findViewById(R.id.sip_number_two);
		mNumberThree = (SipNumberImageView) findViewById(R.id.sip_number_three);
		mNumberFour = (SipNumberImageView) findViewById(R.id.sip_number_four);
		mNumberFive = (SipNumberImageView) findViewById(R.id.sip_number_five);
		mNumberSix = (SipNumberImageView) findViewById(R.id.sip_number_six);
		mNumberSeven = (SipNumberImageView) findViewById(R.id.sip_number_seven);
		mNumberEight = (SipNumberImageView) findViewById(R.id.sip_number_eight);
		mNumberNine = (SipNumberImageView) findViewById(R.id.sip_number_nine);
		mNumberZero = (SipNumberImageView) findViewById(R.id.sip_number_zero);
		mNumberStar = (SipNumberImageView) findViewById(R.id.sip_number_star);
		mNumberSp = (SipNumberImageView) findViewById(R.id.sip_number_sp);
		mNumberAdd = (SipNumberImageView) findViewById(R.id.sip_number_add);
		mNumberMinus = (SipNumberImageView) findViewById(R.id.sip_number_minus);
		mNumberCall = (SipNumberImageView) findViewById(R.id.sip_number_call);
		initCounterResources("1", mNumberOne);
		initCounterResources("2", mNumberTwo);
		initCounterResources("3", mNumberThree);
		initCounterResources("4", mNumberFour);
		initCounterResources("5", mNumberFive);
		initCounterResources("6", mNumberSix);
		initCounterResources("7", mNumberSeven);
		initCounterResources("8", mNumberEight);
		initCounterResources("9", mNumberNine);
		initCounterResources("0", mNumberZero);
		initCounterResources("*", mNumberStar);
		initCounterResources("#", mNumberSp);
		initCounterResources("+", mNumberAdd);
		initCounterResources("-", mNumberMinus);
		initCounterResources(getText(R.string.sip_activity_call).toString(), mNumberCall);
	}

	private void initCounterResources(final String count, SipNumberImageView mTextView) {
		bitmapDrawable = null;
		if (bitmapDrawable == null) {
			int bitmapX = 200;
			int bitmapY = 200;
			bitmapDrawable = Bitmap.createBitmap(bitmapX, bitmapY, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmapDrawable);
			if (mTextView.getId() == R.id.sip_number_call) {
				canvas.drawColor(Color.GREEN);
			} else {
				canvas.drawColor(getResources().getColor(R.color.sip_number_border));
			}
			canvas.save();
		}
		mTextView.count = count;
		mTextView.setTag(count);
		mTextView.setOnClickListener(mNumberButtonClickListener);
		mTextView.setImageBitmap(bitmapDrawable);
	}

	private class NumberButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String count = (String) v.getTag();
			if ("-".equals(count)) {
				String text = sipInputET.getText().toString().trim();
				if (!TextUtils.isEmpty(text)) {
					String newText = text.substring(0, text.length() - 1);
					sipInputET.setText(newText.trim());
					sipInputET.setSelection(newText.trim().length());
				}
			} else if ("+".equals(count)) {
				sipInputET.append("+");
			} else if ("*".equals(count)) {
				sipInputET.append("*");
			} else if ("#".equals(count)) {
				sipInputET.append("#");
			} else if (getText(R.string.sip_activity_call).toString().equals(count)) {
				String target = sipInputET.getText().toString().trim();
				if (!TextUtils.isEmpty(target)) {
					Intent iv = new Intent(mContext, ConversationP2PAVActivity.class);
					iv.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					iv.putExtra("sipNumber", target);
					iv.putExtra("is_coming_call", false);
					iv.putExtra("voice", true);
					iv.putExtra("sip", true);
					mContext.startActivity(iv);
					sipInputET.setText("");
				} else {
					Toast.makeText(mContext, getText(R.string.sip_activity_address).toString(), Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				sipInputET.append(count);
			}
		}
	}
}
