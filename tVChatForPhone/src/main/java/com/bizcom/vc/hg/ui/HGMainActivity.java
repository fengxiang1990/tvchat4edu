package com.bizcom.vc.hg.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.bo.MessageObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.V2SearchRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.V2Log;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.FriendGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.SearchedResult;
import com.bizcom.vo.SearchedResult.SearchedResultItem;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shdx.tvchat.phone.R;

/**
 * 
 * @author Administrator
 * 
 */
public class HGMainActivity extends Activity {

	private LocalReceiver receiver = new LocalReceiver();
	// 好友头像变动监听
	private BitmapManager.BitmapChangedListener mUserAvatarChangedListener = new UserAvatarChangedListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initReceiver();
	}

	/**
	 * 初始化主页
	 */

	/**
	 * 注册监听广播
	 */
	private void initReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		intentFilter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_NOTIFICATION);
		intentFilter
				.addAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_ADDED);
		intentFilter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
		intentFilter
				.addAction(JNIService.JNI_BROADCAST_USER_STATUS_NOTIFICATION);
		intentFilter
				.addAction(JNIService.JNI_BROADCAST_GROUP_USER_UPDATED_NOTIFICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);
		intentFilter
				.addAction(PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP);
		intentFilter.addAction(PublicIntent.FINISH_APPLICATION);
		intentFilter.addAction(JNIService.JNI_BROADCAST_NEW_MESSAGE);
		registerReceiver(receiver, intentFilter);
		BitmapManager.getInstance().registerBitmapChangedListener(
				mUserAvatarChangedListener);
		CommonCallBack.getInstance().executeUpdateConversationState();
	}



	/**
	 * 异步获取推送的所有好友【只在程序新的启动时执行一次】
	 */
	private class ContactsAsyncTaskLoader extends
			AsyncTask<Void, Void, List<User>> {

		@Override
		protected List<User> doInBackground(Void... params) {
			List<Group> mContactGroupList = GlobalHolder.getInstance()
					.getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
			if (mContactGroupList != null && mContactGroupList.size() > 0) {
				return mContactGroupList.get(0).getUsers();
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<User> resultList) {
//			friendList.clear();
//			showFriendList.clear();
//			if (resultList != null && resultList.size() > 0) {
//				// 获得所有好友
//				friendList.addAll(resultList);
//				if (currentFriendPage == tabFriendPage1) {// 当前展示的页面是收藏页
//					showFriendList.addAll(resultList);
//					friendAdapert.notifyDataSetChanged();
//				}
//			} else {
//				handler.sendEmptyMessage(FRIEND_NO_DATA);
//			}
		}
	}


	/**
	 * 监听广播【获取列表/添加好友/删除好友/消息到达】
	 */
	private class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
			case JNIService.JNI_BROADCAST_GROUP_NOTIFICATION:
				int groupType = intent.getIntExtra("gtype", -1);
				if (groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
					new ContactsAsyncTaskLoader().execute();
				}
				break;

			}
		}
	}


	/**
	 * 好友头像变动监听
	 * 
	 */
	private class UserAvatarChangedListener implements
			BitmapManager.BitmapChangedListener {

		@Override
		public void notifyAvatarChanged(User user, Bitmap bm) {
			// 在这里接收到新的头像和用户的信息
			System.out.println(user);
		}

	}
}
