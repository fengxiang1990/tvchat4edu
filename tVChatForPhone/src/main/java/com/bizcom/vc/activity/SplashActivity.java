package com.bizcom.vc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.MainApplication;
import com.bizcom.db.provider.SearchContentProvider;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.main.MainActivity;
import com.config.GlobalConfig;

import java.util.HashMap;


public class SplashActivity extends Activity {
	private static final String TAG = SplashActivity.class.getSimpleName();
	private boolean isFward = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 为了解决安装APK后直接点打开所造成的异常
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			V2Log.d("TAG", "FLAG_ACTIVITY_BROUGHT_TO_FRONT WAS CALL!");
			isFward = true;
			finish();
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				initSearchMap();
				forward();
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!isFward) {
			((MainApplication) getApplication()).uninitForExitProcess();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
//		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
//		JPushInterface.onPause(this);
	}

	private void initSearchMap() {
		try {
			HashMap<String, String> allChinese = SearchContentProvider.queryAll(this);
			if (allChinese == null) {
				V2Log.e("loading dataBase data is fialed...");
				return;
			}
			GlobalConfig.CHINESE_CHAR_ARRAY.putAll(allChinese);
			V2Log.d(TAG, " 成功加载完搜索所用的数据库字段数据！");
		} catch (Exception e) {
			V2Log.e(TAG, " 加载搜索数据库字段数据时发生异常！");
			e.printStackTrace();
		} finally {
			SearchContentProvider.closedDataBase();
		}
	}

	private void forward() {
		int flag = LocalSharedPreferencesStorage.getConfigIntValue(this, GlobalConfig.KEY_LOGGED_IN, 0);
		if (flag == 1) {
			startActivity(new Intent(this, MainActivity.class));
			isFward = true;
		} else {
			startActivity(new Intent(this, LoginActivity.class));
			isFward = true;
		}
		finish();
	}
}
