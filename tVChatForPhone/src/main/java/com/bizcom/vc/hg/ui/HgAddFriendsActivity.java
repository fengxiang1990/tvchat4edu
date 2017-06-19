package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bizcom.request.V2SearchRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.adapter.AddFriendsAdapter;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vo.SearchedResult;
import com.bizcom.vo.SearchedResult.SearchedResultItem;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;

public class HgAddFriendsActivity extends Activity {
	private boolean hasPrepared = false;
	private Context mContext;
	private V2SearchRequest searchService = new V2SearchRequest();// 搜索好友时的请求服务
	/** 好友TAB----------- */
	private State mState = State.DONE;// 默认搜索状态为完成
	private final int SEARCH_DONE = 10;// 搜索好友完成
	private ListView mList;
	private EditText et;
	private ArrayList<User> data;
	private HeadLayoutManagerHG mHeadLayoutManager;
	private AddFriendsAdapter adapter;
	private View searchIm;
	public final String action = "HgAddFriendsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hg_add_friends);
		mContext = this;
		initData();
		initView();
		initListener();
		hasPrepared = true;
	}

	private void initData() {
		data = new ArrayList<User>();

	}

	private void initListener() {
		mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout), false);
		mHeadLayoutManager.updateTitle(getIntent().getStringExtra("titleText"));
		et = (EditText) findViewById(R.id.et);
		et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
			}
		});
		searchIm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String search = et.getText().toString().trim();
				if (et.getText() != null && !TextUtils.isEmpty(search)) {
					if (hasPrepared) {
						data.clear();
						search(search);
					}
				} else {
					ToastUtil.ShowToast_long(mContext, "请输入正确的查询条件");
				}

			}
		});

	}

	private void initView() {
		searchIm = findViewById(R.id.searchIm);
		mList = (ListView) findViewById(R.id.mList);
		adapter = new AddFriendsAdapter(data, mContext);
		mList.setAdapter(adapter);

	}

	enum State {
		DONE, SEARCHING,
	}

	private void search(String searText) {
		synchronized (mState) {
			if (mState == State.SEARCHING) {
				return;
			}
			if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
				return;
			}
			mState = State.SEARCHING;

		}
		V2SearchRequest.SearchParameter par = searchService
				.generateSearchPatameter(V2GlobalConstants.SEARCH_REQUEST_TYPE_USER, searText, 1);// 1为查找第一页。
		WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.status_searching));
		searchService.search(par, new HandlerWrap(handler, SEARCH_DONE, null));

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SEARCH_DONE:// 添加好友-搜索完毕
				WaitDialogBuilder.dismissDialog();
				synchronized (mState) {
					mState = State.DONE;
				}
				JNIResponse jni = (JNIResponse) msg.obj;
				if (jni.getResult() == JNIResponse.Result.SUCCESS) {
					SearchedResult result = (SearchedResult) jni.resObj;
					for (SearchedResultItem srItem : result.getList()) {
						User u = GlobalHolder.getInstance().getUser(srItem.id);
						data.add(u);
					}

					adapter.notifyDataSetChanged();
				}
				break;
			}
		};
	};

	protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
		if (requestCode == 0 && resultCode == 1) {
			finish();
		}
	};

	class LocalReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		hideSoft();
	}

	public void hideSoft() {

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}
}
