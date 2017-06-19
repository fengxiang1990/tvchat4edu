package com.bizcom.vc.activity.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bizcom.request.V2ContactsRequest;
import com.bizcom.util.MessageUtil;
import com.bizcom.vc.activity.message.MessageAuthenticationActivity;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

public class InputAuthenticationActivity extends Activity {
	// 两个标题
	private ImageView mHeadIconIV;
	// contact_user_detail_head_icon
	private TextView mNameTitleIV;
	// contact_user_detail_title
	private TextView mSignTV;
	// R.id.tv_left
	private TextView tvLeft;
	// R.id.right_text_view
	private TextView tvRightTextView;
	// R.id.tv_title
	private TextView tvTitle;
	// R.id.tv_input
	private ClearEditText etInput;

	String startedCause;
	long mUid;
	User detailUser;
	private V2ContactsRequest contactService = new V2ContactsRequest();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_add_authentication);
		findViewById(R.id.layout).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hindSoftInput(v);
				return false;
			}
		});
		;

		startedCause = this.getIntent().getStringExtra("cause");
		connectView();
		bindViewEnvent();
		if ((startedCause != null)
				&& startedCause.equals("refuse_friend_authentication")) {
			mUid = this.getIntent().getLongExtra("remoteUserID", 0);
			tvLeft.setText(R.string.authenticationActivity_titlebar_back);
			tvTitle.setText(R.string.authenticationActivity_titlebar_title);
			etInput.setHint(R.string.authenticationActivity_objection_hint);
		} else {
			mUid = this.getIntent().getLongExtra("uid", 0);
			tvLeft.setText(R.string.authenticationActivity_titlebar_back1);
			tvTitle.setText(R.string.authenticationActivity_titlebar_title1);
			etInput.setHint(R.string.authenticationActivity_authentication_info_hint);

		}
		detailUser = GlobalHolder.getInstance().getUser(mUid);
		if (detailUser.getAvatarBitmap() != null) {
			mHeadIconIV.setImageBitmap(detailUser.getAvatarBitmap());
		}
		mNameTitleIV.setText(detailUser.getDisplayName());
		mSignTV.setText(detailUser.getSignature());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		contactService.clearCalledBack();
	}

	private void hindSoftInput(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && v != null) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	private void connectView() {
		tvLeft = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		tvLeft.setText(getResources().getString(
				R.string.contacts_user_detail_return_button));
		tvTitle = (TextView) findViewById(R.id.ws_common_activity_title_content);
		tvTitle.setText(getResources().getString(
				R.string.authenticationActivity_titlebar_title1));
		tvRightTextView = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
		tvRightTextView.setText(getResources().getString(
				R.string.contacts_authentication_next));
		etInput = (ClearEditText) findViewById(R.id.et_input);

		mHeadIconIV = (ImageView) findViewById(R.id.ws_common_avatar);
		mNameTitleIV = (TextView) findViewById(R.id.ws_common_contact_conversation_topContent);
		mSignTV = (TextView) findViewById(R.id.ws_common_contact_conversation_belowContent);
		if ((startedCause != null)
				&& startedCause.equals("refuse_friend_authentication")) {
			tvRightTextView.setText(R.string.contacts_authentication_complete);
		} else {
			// 下一步
			tvRightTextView.setText(R.string.contacts_authentication_next);
		}
	}

	private void bindViewEnvent() {
		// 返回
		tvLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(ContactDetail2.START_AUTHENTICATION_ACTIVITY);
				onBackPressed();
			}
		});

		if ((startedCause != null)
				&& startedCause.equals("refuse_friend_authentication")) {
			tvRightTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {

                    MessageUtil.hideKeyBoard(InputAuthenticationActivity.this , etInput.getWindowToken());
					// 拒绝加为好友
					contactService.refuseAddedAsContact(30, mUid, etInput
							.getText().toString());
					AddFriendHistroysHandler.addMeRefuse(
							getApplicationContext(), mUid, etInput.getText()
									.toString());
					// 实现越级跳
					Intent i = new Intent(InputAuthenticationActivity.this,
							MessageAuthenticationActivity.class);
					i.putExtra("isReturnAuth", true);
					i.putExtra("remoteUserID", mUid);
					setResult(ContactDetail2.START_AUTHENTICATION_ACTIVITY, i);
					onBackPressed();
				}
			});
		} else {
			tvRightTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
                    MessageUtil.hideKeyBoard(InputAuthenticationActivity.this , etInput.getWindowToken());
					Intent i = new Intent(InputAuthenticationActivity.this,
							InputRemarkActivity.class);
					i.putExtra("uid", mUid);
					i.putExtra("verificationInfo", etInput.getText().toString());
					InputAuthenticationActivity.this.startActivity(i);
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
