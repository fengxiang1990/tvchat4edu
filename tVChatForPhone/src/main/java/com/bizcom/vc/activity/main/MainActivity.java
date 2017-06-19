package com.bizcom.vc.activity.main;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.MainApplication;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.service.JNIService;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.LoginActivity;
import com.bizcom.vc.listener.ConferenceListener;
import com.bizcom.vc.listener.NotificationListener;
import com.bizcom.vo.Conference;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.VFile.State;
import com.bizcom.vo.enums.NetworkStateCode;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements NotificationListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private HeadLayoutManager mHeadLayoutManager;
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private LocalReceiver receiver = new LocalReceiver();
	private ConferenceListener mConfListener;
	private Fragment[] fragments;
	private TabHostOnTabChangeListener mOnTabChangeListener = new TabHostOnTabChangeListener();

	private Context mContext;
	private Conference conf;
	private boolean exitedFlag = false;

	private TabWrap[] mTabClasses = new TabWrap[] {
			new TabWrap(V2GlobalConstants.TAG_COV, R.string.tab_conversation_name, R.string.tab_conversation_name,
					R.drawable.buttomtab_message_selector, TabFragmentMessage.class.getName()),
			new TabWrap(V2GlobalConstants.TAG_ORG, R.string.tab_org_name, R.string.tab_org_name,
					R.drawable.buttomtab_org_selector, TabFragmentOrganization.class.getName()),
			new TabWrap(V2GlobalConstants.TAG_CONF, R.string.tab_conference_name, R.string.tab_conference_name,
					R.drawable.buttomtab_conference_selector, TabFragmentConference.class.getName()), };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		if (savedInstanceState != null) {
			finish();
			return;
		}
		mHeadLayoutManager = new HeadLayoutManager(mContext, findViewById(R.id.head_layout) , true);
		mHeadLayoutManager.updateTitle(mTabClasses[0].mTabTitle);
		updateFileState();
		initialiseTabHost();
		int index = getIntent().getIntExtra("initFragment", 0);
		intialiseViewPager(index);

		initBroadcast();
		MainApplication.isAlreadLogin = true;
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (conf != null) {
			mConfListener.requestJoinConf(conf);
			conf = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		int index = intent.getIntExtra("initFragment", 0);
		// mViewPager.setCurrentItem(index);
		mViewPager.setCurrentItem(index, true);
		if (intent.getExtras() != null) {
			conf = (Conference) intent.getExtras().get("conf");
		}
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GlobalHolder.getInstance().clearAll();
		if (mViewPager == null) {
			V2Log.d(TAG, "V2tech was illegal !");
			super.finish();
			return;
		}
		// unregister for headset
		unregisterReceiver(receiver);
		((MainApplication) getApplication()).uninitForExitProcess();
		Intent i = new Intent(mContext, LoginActivity.class);
		startActivity(i);
		V2Log.d(TAG, "V2tech process was destroyed!");
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			TabFragmentOrganization org = (TabFragmentOrganization) fragments[1];
			org.GroupTabItemUpEvent(MotionEvent.ACTION_UP);
		}

		View v = getCurrentFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && v != null) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		if (v != null) {
			v.clearFocus();
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 初始化底部导航页
	 */
	private void initialiseTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		for (TabWrap tabWrap : mTabClasses) {
			TabHost.TabSpec tabSpec = this.mTabHost.newTabSpec(tabWrap.mTabName).setIndicator(getTabView(tabWrap));
			tabSpec.setContent(new TabFactory(this));
			mTabHost.addTab(tabSpec);
		}

		mTabHost.setOnTabChangedListener(mOnTabChangeListener);
	}

	/**
	 * 初始化ViewPager内容，并指定初始的显示位置
	 *
	 * @param index
	 */
	private void intialiseViewPager(int index) {
		fragments = new Fragment[mTabClasses.length];
		for (int i = 0; i < mTabClasses.length; i++) {
			TabWrap tabWrap = mTabClasses[i];
			Bundle bundle = new Bundle();
			bundle.putString("tag", tabWrap.mTabName);
			Fragment fragment = Fragment.instantiate(this, tabWrap.mFragmentClassName, bundle);

			if (fragment instanceof ConferenceListener && tabWrap.mTabName.equals(V2GlobalConstants.TAG_CONF)) {
				mConfListener = (ConferenceListener) fragment;
			}
			fragments[i] = fragment;
		}

		mViewPager = (ViewPager) super.findViewById(R.id.viewpager);

		MyFragmentPagerAdapter mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);

		mViewPager.setOnPageChangeListener(listenerOfPageChange);
		// 保留子视图的个数，防止频繁创建和销毁
		mViewPager.setOffscreenPageLimit(5);
		mViewPager.setCurrentItem(index);
	}

	private void initBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		filter.addAction(PublicIntent.FINISH_APPLICATION);
		filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
		mContext.registerReceiver(receiver, filter);
	}

	private void requestQuit() {
		if (exitedFlag) {
			((MainApplication) this.getApplicationContext()).requestQuit();
		} else {
			exitedFlag = true;
			Toast.makeText(this, R.string.quit_promption, Toast.LENGTH_SHORT).show();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					exitedFlag = false;
				}

			}, 2500);
		}
	}

	/**
	 * 获取底部导航页的View对象
	 *
	 * @param tabWrap
	 * @return
	 */
	private View getTabView(TabWrap tabWrap) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.tab_widget_view, null, false);
		ImageView iv = (ImageView) v.findViewById(R.id.tab_image);
		if (iv != null) {
			iv.setImageDrawable(this.getResources().getDrawable(tabWrap.mDrawableId));
			iv.bringToFront();
		}

		TextView tv = (TextView) v.findViewById(R.id.tab_name);
		if (tv != null) {
			tv.setText(this.getResources().getText(tabWrap.mShowText));
			tv.bringToFront();
		}

		tabWrap.mViewNotificator = v.findViewById(R.id.tab_notificator);
		tabWrap.mViewNotificator.setVisibility(View.INVISIBLE);
		return v;
	}

	public void requestEnterConf(Conference conf) {
		mConfListener.requestJoinConf(conf);
	}

	public void updateNotificator(final int tabType, final boolean whetherDisplay) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				View noticator;
				if (tabType == Conversation.TYPE_CONTACT) {
					noticator = mTabClasses[0].mViewNotificator;
				} else {
					V2Log.e(TAG, "Error TabFragment Type Value : " + tabType);
					return;
				}

				if (whetherDisplay) {
					noticator.setVisibility(View.VISIBLE);
				} else {
					noticator.setVisibility(View.GONE);
				}
			}
		});
	}

	private TextWatcher listenerOfSearchTextWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable edit) {

			((TextWatcher) ((FragmentPagerAdapter) mViewPager.getAdapter()).getItem(mTabHost.getCurrentTab()))
					.afterTextChanged(edit);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			((TextWatcher) ((FragmentPagerAdapter) mViewPager.getAdapter()).getItem(mTabHost.getCurrentTab()))
					.beforeTextChanged(arg0, arg1, arg2, arg3);
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			((TextWatcher) ((FragmentPagerAdapter) mViewPager.getAdapter()).getItem(mTabHost.getCurrentTab()))
					.onTextChanged(arg0, arg1, arg2, arg3);
		}

	};

	/**
	 * Detecting all VMessageFileItem Object that state is sending or
	 * downloading in the database , and change their status to failed..
	 */
	public void updateFileState() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<VMessageFileItem> loadFileMessages = ChatMessageProvider.loadFileMessages(-1, -1);
				if (loadFileMessages != null) {
					for (VMessageFileItem fileItem : loadFileMessages) {
						V2Log.d(TAG, "Iterator VMessageFileItem -- name is : " + fileItem.getFileName() + " state : "
								+ State.fromInt(fileItem.getState()));
						if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING
								|| fileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING)
							fileItem.setState(VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED);
						else if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_SENDING
								|| fileItem.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING)
							fileItem.setState(VMessageAbstractItem.STATE_FILE_SENT_FALIED);
						int update = ChatMessageProvider.updateFileItemState(mContext, fileItem);
						if (update == -1) {
							V2Log.e(TAG, "update file state failed... file id is : " + fileItem.getUuid());
						}
					}
				} else
					V2Log.e(TAG, "load all files failed... get null");
			}
		}).start();
	}

	class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (PublicIntent.FINISH_APPLICATION.equals(action)) {
				exitedFlag = true;
				boolean isDelay = intent.getBooleanExtra("delay", false);
				if (isDelay) {
					Toast.makeText(mContext, mContext.getText(R.string.user_logged_with_deleted), Toast.LENGTH_LONG)
							.show();
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							requestQuit();
						}
					}, 3000);
				} else {
					requestQuit();
				}
			} else if (JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION.equals(action)) {
				V2Log.d("CONNECT", "MainActivity Receiver Broadcast ! Globle Connection State is : "
						+ GlobalHolder.getInstance().isServerConnected());
				NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
				if (code != null) {
					V2Log.d("CONNECT",
							"MainActivity Receiver Broadcast ! receiver Connection State is : " + code.name());
				}

				V2Log.d("CONNECT", "--------------------------------------------------------------------");
				if (mHeadLayoutManager != null) {
					mHeadLayoutManager.updateConnectState(code);
				} else {
					V2Log.d("CONNECT", "TitleBar is null !");
				}
				V2Log.d("CONNECT", "--------------------------------------------------------------------");
			}
		}
	}

	private class TabHostOnTabChangeListener implements TabHost.OnTabChangeListener {
		/**
		 * (non-Javadoc)
		 *
		 * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
		 */
		public void onTabChanged(String tag) {
			int pos = mTabHost.getCurrentTab();
			if (mViewPager == null) {
				V2Log.e(" MainActivity state is illegal");
				return;
			}
			// 恢复搜索状态
			TabWrap tab = mTabClasses[pos];
			Fragment fragment = fragments[pos];
			if (tab.mTabName.equals(V2GlobalConstants.TAG_CONF)) {
				((TabFragmentConference) fragment).updateSearchState();
			} else if (tab.mTabName.equals(V2GlobalConstants.TAG_COV)) {
				((TabFragmentMessage) fragment).updateSearchState();
			}

			mViewPager.setCurrentItem(pos);
			mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
		}

	}

	private ViewPager.OnPageChangeListener listenerOfPageChange = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			if (arg0 == 1) {
				Fragment fragment = fragments[1];
				((TabFragmentOrganization) fragment).setIsUpdate(false);
			} else if (arg0 == 0) {
				Fragment fragment = fragments[1];
				((TabFragmentOrganization) fragment).setIsUpdate(true);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int pos) {
			mTabHost.setCurrentTab(pos);
			mHeadLayoutManager.updateTitle(mTabClasses[pos].mTabTitle);
		}

	};

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			return fragments[position];
		}

		/**
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return fragments.length;
		}
	}

	class TabWrap {
		String mTabName;
		int mTabTitle;
		int mShowText;
		int mDrawableId;
		String mFragmentClassName;
		View mViewNotificator;

		public TabWrap(String tabName, int tabTitle, int showText, int drawableId, String clsName,
				View viewNotificator) {
			super();
			this.mTabName = tabName;
			this.mTabTitle = tabTitle;
			this.mShowText = showText;
			this.mDrawableId = drawableId;
			this.mFragmentClassName = clsName;
			this.mViewNotificator = viewNotificator;
		}

		public TabWrap(String tabName, int tabTitle, int showText, int drawableId, String clsName) {
			this(tabName, tabTitle, showText, drawableId, clsName, null);
		}

	}

	/**
	 * A simple factory that returns dummy views to the Tabhost
	 *
	 */
	class TabFactory implements TabContentFactory {

		private final Context mContext;

		/**
		 * @param context
		 */
		public TabFactory(Context context) {
			mContext = context;
		}

		@Override
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}
}
