package com.bizcom.vc.activity.search;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.V2SearchRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.SearchedResult;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

public class SearchActivity extends BaseActivity {

	private static final int SEARCH_DONE = 1;

	private State mState = State.DONE;
	private ClearEditText mSearchedText;
    private TextView mTitleText;

	private V2ImRequest usService = new V2ImRequest();
	private V2CrowdGroupRequest crowdService = new V2CrowdGroupRequest();

	private int mType = V2GlobalConstants.SEARCH_REQUEST_TYPE_USER;
	private V2SearchRequest searchService = new V2SearchRequest();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_searchserver);
		super.setNeedAvatar(false);
		super.setNeedBroadcast(false);
		super.setNeedHandler(true);
		super.onCreate(savedInstanceState);
		initViewContent();
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

    @Override
	public void onResume() {
        super.onResume();
        MessageUtil.showKeyBoard(mSearchedText);
    }

    @Override
	public void onBackPressed() {
		View v = getCurrentFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && v != null) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		super.onBackPressed();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	@Override
	protected void onDestroy() {
		usService.clearCalledBack();
		crowdService.clearCalledBack();
		searchService.clearCalledBack();
		super.onDestroy();
	}

	@Override
	public void addBroadcast(IntentFilter filter) {

	}

	@Override
	public void receiveBroadcast(Intent intent) {

	}

	@Override
	public void receiveMessage(Message msg) {
		switch (msg.what) {
		case SEARCH_DONE:
			WaitDialogBuilder.dismissDialog();
			synchronized (mState) {
				mState = State.DONE;
			}
			JNIResponse jni = (JNIResponse) msg.obj;
			if (jni.getResult() == JNIResponse.Result.SUCCESS) {
				SearchedResult result = (SearchedResult) jni.resObj;
				if (result.getList().size() <= 0) {
					switch (mType) {
					case V2GlobalConstants.SEARCH_REQUEST_TYPE_USER:
						showShortToast(R.string.search_result_toast_no_member_entry);
						break;
					case V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP:
						showShortToast(R.string.search_result_toast_no_crowd_entry);
						break;
					}
				} else {
					Intent i = new Intent();
					i.setClass(getApplicationContext(), SearchedResultActivity.class);
					i.putExtra("result", result);
					i.putExtra("searchType", mType);
					startActivity(i);
				}
			} else {
				showShortToast(R.string.search_result_toast_error);
			}
			break;
		}
	}

	@Override
	public void initViewAndListener() {
		mTitleText = (TextView) findViewById(R.id.ws_common_activity_title_content);
		mTitleText.setText(getResources().getString(R.string.search_title_crowd));
        TextView mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		setComBackImageTV(mReturnButton);
		mReturnButton.setOnClickListener(mReturnButtonListener);
		findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);

		mSearchedText = (ClearEditText) findViewById(R.id.search_text);
        TextView mSearchButton = (TextView) findViewById(R.id.search_search_button);
		mSearchButton.setOnClickListener(mSearchButtonListener);
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

	}

	private void initViewContent() {
		int activityType = getIntent().getIntExtra("type", 0);
		if (activityType == 0) {
			mType = V2GlobalConstants.SEARCH_REQUEST_TYPE_USER;
		} else if (activityType == 1) {
			mType = V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP;
		}

		int titleRid = R.string.search_title_crowd;
//		int contentRid = R.string.search_content_text_crowd;
//		int belowHit = R.string.search_crowd_rules_tips;
		switch (mType) {
		case V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP:
			titleRid = R.string.search_title_crowd;
//			contentRid = R.string.search_content_text_crowd;
//			belowHit = R.string.search_crowd_rules_tips;
			break;
		case V2GlobalConstants.SEARCH_REQUEST_TYPE_USER:
			titleRid = R.string.search_search_button;
//			contentRid = R.string.search_content_text_member;
//			belowHit = R.string.search_rules_tips;
			break;
		default:
			break;
		}

		mTitleText.setText(titleRid);
//		mContentText.setText(contentRid);
//		mEditTextBelowText.setText(belowHit);
	}

	private OnClickListener mSearchButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			synchronized (mState) {
				if (mState == State.SEARCHING) {
					return;
				}

				if (TextUtils.isEmpty(mSearchedText.getText())) {
					mSearchedText.setError(SearchActivity.this.getText(R.string.search_text_required));
					return;
				}

				if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
					return;
				}
				mState = State.SEARCHING;

			}

			V2SearchRequest.SearchParameter par = searchService.generateSearchPatameter(
					mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_USER ? V2GlobalConstants.SEARCH_REQUEST_TYPE_USER
							: V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP,
					mSearchedText.getText().toString(), 1);
			WaitDialogBuilder.showNormalWithHintProgress(SearchActivity.this,
					getResources().getString(R.string.status_searching));
			searchService.search(par, new HandlerWrap(mHandler, SEARCH_DONE, null));
		}

	};

	private OnClickListener mReturnButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	enum State {
		DONE, SEARCHING,
	}
}
