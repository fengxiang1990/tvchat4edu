package com.bizcom.vc.activity.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.DataBaseContext;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.util.DateUtil;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.AudioVideoMessageBean.ChildMessageBean;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.User;
import com.bizcom.vo.VideoBean;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;

public class VoiceMessageDetailActivity extends Activity implements OnClickListener {

	public static final String TAG = VoiceMessageDetailActivity.class.getSimpleName();
	private LinearLayout videoCall;
	private LinearLayout vioceCall;
	private TextView clearRecord;
	private TextView returnBack;
	private TextView userName;
	private TextView userRemark;
	private CustomAvatarImageView userIcon;
	private ListView mListView;
	private ArrayList<ChildMessageBean> mListItem;
	private ChildMessageBean childBean;
	private VoiceDetailBaseAdapter adapter;
	private long remoteID;
	private VoiceDetailReceiverBroadcast receiver;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specifitem_voicedetail);
		mContext = this;
		findview();
		initReceiver();
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void findview() {
        returnBack = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        returnBack.setBackgroundResource(R.drawable.title_bar_back_button_selector);
        TextView mTitleContentTV = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContentTV.setText(R.string.specificItem_voiceDetail_Title);

        videoCall = (LinearLayout) findViewById(R.id.specific_voiceDetail_video_call_bottom_button);
        vioceCall = (LinearLayout) findViewById(R.id.specific_voiceDetail_vioce_call_bottom_button);
        mListView = (ListView) findViewById(R.id.specific_voiceDetail_listview);
        clearRecord = (TextView) findViewById(R.id.specific_voiceDetail_clearAll);
		userName = (TextView) findViewById(R.id.specific_voiceDetail_name);
		userRemark = (TextView) findViewById(R.id.specific_voiceDetail_remark);
		userIcon = (CustomAvatarImageView) findViewById(R.id.ws_common_avatar);

		returnBack.setOnClickListener(this);
		clearRecord.setOnClickListener(this);
		videoCall.setOnClickListener(this);
		vioceCall.setOnClickListener(this);
	}

	private void initReceiver() {
		receiver = new VoiceDetailReceiverBroadcast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConversationP2PAVActivity.P2P_BROADCAST_MEDIA_UPDATE);
		intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		registerReceiver(receiver, intentFilter);
	}

	private void init() {

		remoteID = getIntent().getLongExtra("remoteUserID", -1l);
		recordType = getIntent().getIntExtra("mediaType", -1);
		if (remoteID == -1l){
			Toast.makeText(getApplicationContext(), "获取用户信息失败", Toast.LENGTH_SHORT).show();
			finish();
			return ;
		}
		BitmapManager.getInstance().registerBitmapChangedListener(this.bitmapChangedListener);

		String remoteName = "";
		if(recordType == AudioVideoMessageBean.TYPE_SIP){
			remoteName = String.valueOf(remoteID);
			userRemark.setText("");
			videoCall.setVisibility(View.GONE);
		} else {
			User user = GlobalHolder.getInstance().getUser(remoteID);
			remoteName = user.getDisplayName();
			userRemark.setText(user.getSignature());
		}
		userName.setText(remoteName);
		User remoteUser = GlobalHolder.getInstance().getUser(remoteID);
		if (remoteUser.getAvatarBitmap() != null) {
			userIcon.setImageBitmap(remoteUser.getAvatarBitmap());
		}

		mListItem = getIntent().getParcelableArrayListExtra("messages");
		adapter = new VoiceDetailBaseAdapter();
		mListView.setAdapter(adapter);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(mContext, VoiceMessageActivity.class);
		startActivity(intent);
		finish();
	}

	class VoiceDetailBaseAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mListItem.size();
		}

		@Override
		public Object getItem(int position) {
			return mListItem.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(VoiceMessageDetailActivity.this,
						R.layout.activity_specifitem_voicedetail_adapter, null);
				holder.directionIcon = (ImageView) convertView
						.findViewById(R.id.specific_voiceDetail_adapter_direction);
				holder.state = (TextView) convertView.findViewById(R.id.specific_voiceDetail_adapter_state);
				holder.holdTime = (TextView) convertView.findViewById(R.id.specific_voiceDetail_adapter_holdTime);
				holder.saveTime = (TextView) convertView.findViewById(R.id.specific_voiceDetail_saveTime);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			childBean = mListItem.get(position);
            long time = childBean.childHoldingTime;
            String holdTime = time <= 0 ? "" : DateUtil.calculateTime(time);
            holder.holdTime.setText(holdTime);
			holder.saveTime.setText(DateUtil.getStringDate(childBean.childSaveDate));
            // 区分是主动拨出还是接收
			if (childBean.childISCallOut == AudioVideoMessageBean.STATE_CALL_OUT) {
                if (childBean.childMediaType == AudioVideoMessageBean.TYPE_AUDIO)
                    holder.state.setText(R.string.conversation_voice_detail_voice_call_out);
                else if(childBean.childMediaType == AudioVideoMessageBean.TYPE_VIDEO)
                    holder.state.setText(R.string.conversation_voice_detail_video_call_out);
                else
                    holder.state.setText(R.string.conversation_voice_detail_sip_call_out);
                // 区分是对方对通话是否进行了处理
                if(childBean.childMediaState == AudioVideoMessageBean.STATE_ANSWER_CALL){
                    // 区分是对方接受了还是拒绝了
                    if(childBean.childHoldingTime <= 0){
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_refuse);
                        holder.state.setTextColor(Color.RED);
                        holder.holdTime.setTextColor(Color.RED);
                        holder.holdTime.setText(R.string.conversation_voice_detail_voice_call_in_reply_reject);
                    } else {
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_callout);
                        holder.state.setTextColor(Color.GRAY);
                        holder.holdTime.setTextColor(Color.GRAY);
                        holder.holdTime.setText(DateUtil.calculateTime(childBean.childHoldingTime));
                    }
                } else {
                    holder.directionIcon.setImageResource(R.drawable.vs_voice_refuse);
                    holder.state.setTextColor(Color.RED);
                    holder.holdTime.setTextColor(Color.RED);
                    holder.holdTime.setText(R.string.conversation_voice_detail_voice_call_in_no_reply);
                }
			} else {
                if (childBean.childMediaType == AudioVideoMessageBean.TYPE_AUDIO)
                    holder.state.setText(R.string.conversation_voice_detail_voice_call_in_reply);
                else if(childBean.childMediaType == AudioVideoMessageBean.TYPE_VIDEO)
                    holder.state.setText(R.string.conversation_voice_detail_video_call_in_reply);
                else
                    holder.state.setText(R.string.conversation_voice_detail_sip_call_in_reply);
				if (childBean.childMediaState == AudioVideoMessageBean.STATE_ANSWER_CALL) {
                    if(childBean.childHoldingTime <= 0){
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_coming_refuse);
                        holder.state.setTextColor(Color.RED);
                        holder.holdTime.setTextColor(Color.RED);
                        holder.holdTime.setText(R.string.conversation_voice_detail_voice_call_in_reply_reject);
                    } else {
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_listener);
                        holder.state.setTextColor(Color.GRAY);
                        holder.holdTime.setTextColor(Color.GRAY);
                        holder.holdTime.setText(DateUtil.calculateTime(childBean.childHoldingTime));
                    }
				} else {
					holder.directionIcon.setImageResource(R.drawable.vs_voice_coming_refuse);
					holder.state.setTextColor(Color.RED);
					holder.holdTime.setTextColor(Color.RED);
                    holder.holdTime.setText(R.string.conversation_voice_detail_voice_call_in_no_reply);
				}
			}
			return convertView;
		}
	}

	class ViewHolder {

		public TextView state;
		public TextView holdTime;
		public TextView saveTime;
		public ImageView directionIcon;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ws_common_activity_title_left_button:
			Intent intent = new Intent(this, VoiceMessageActivity.class);
			startActivity(intent);
			finish();
			mListItem.clear();
			break;
		case R.id.specific_voiceDetail_clearAll:
			mListItem.clear();
			adapter.notifyDataSetChanged();
			Uri url = ContentDescriptor.HistoriesMedia.CONTENT_URI;
			String where = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + "= ?";
			String[] selectionArgs = new String[] { String.valueOf(remoteID) };
			getContentResolver().delete(url, where, selectionArgs);
			// Toast.makeText(getApplicationContext(), "本次共删除" + result + "条记录",
			// Toast.LENGTH_SHORT)
			// .show();
			break;
		case R.id.specific_voiceDetail_video_call_bottom_button:
			if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
				return;
			}

            GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.
                    P2P_CONNECT_VIDEO, remoteID, false, null, null);
			break;
		case R.id.specific_voiceDetail_vioce_call_bottom_button:
			if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
				return;
			}

            if(recordType == AudioVideoMessageBean.TYPE_SIP){
                GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.
                        P2P_CONNECT_SIP, remoteID, false, null, String.valueOf(remoteID));
            } else {
                GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.
                        P2P_CONNECT_AUDIO, remoteID, false, null, null);
            }
			break;
		}
	}

	class VoiceDetailReceiverBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			long receiveRemoteID = intent.getLongExtra("remoteID", -1l);
			if (remoteID == -1l) {
				Log.e(TAG, "get remoteID is -1 ... update failed!!");
				return;
			}

			if (remoteID != receiveRemoteID)
				return;

			String selections = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + "= ? ";
			String[] selectionArgs = new String[] { String.valueOf(remoteID) };
			VideoBean newestMediaMessage = MediaRecordProvider.getNewestMediaMessage(selections, selectionArgs);
			if (newestMediaMessage == null) {
				Log.e(TAG, "get newest remoteID " + remoteID + " --> VideoBean is NULL ... update failed!!");
				return;
			}
			ChildMessageBean newChild = new ChildMessageBean();
			newChild.childMediaType = newestMediaMessage.mediaType;
			newChild.childReadState = newestMediaMessage.readSatate;
			newChild.childMediaState = newestMediaMessage.mediaState;
			newChild.childHoldingTime = newestMediaMessage.endDate - newestMediaMessage.startDate;
			newChild.childSaveDate = newestMediaMessage.startDate;
			if (newestMediaMessage.formUserID != GlobalHolder.getInstance().getCurrentUserId())
				newChild.childISCallOut = AudioVideoMessageBean.STATE_CALL_IN;
			else
				newChild.childISCallOut = AudioVideoMessageBean.STATE_CALL_OUT;

			mListItem.add(0, newChild);
			adapter.notifyDataSetChanged();

			// update conversation state
			Intent i = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			ConversationNotificationObject obj = new ConversationNotificationObject(Conversation.TYPE_VOICE_MESSAGE,
					Conversation.SPECIFIC_VOICE_ID, false);
			i.putExtra("obj", obj);
			mContext.sendBroadcast(i);

			// update database state
			ContentValues values = new ContentValues();
			values.put(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE,
					V2GlobalConstants.READ_STATE_READ);
			String where = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE + "= ? and "
					+ ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + "= ?";
			String[] args = new String[] { String.valueOf(V2GlobalConstants.READ_STATE_UNREAD),
					String.valueOf(remoteID) };
			DataBaseContext con = new DataBaseContext(VoiceMessageDetailActivity.this);
			con.getContentResolver().update(ContentDescriptor.HistoriesMedia.CONTENT_URI, values, where, args);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		BitmapManager.getInstance().unRegisterBitmapChangedListener(bitmapChangedListener);
	}

	private BitmapManager.BitmapChangedListener bitmapChangedListener = new BitmapManager.BitmapChangedListener() {

		@Override
		public void notifyAvatarChanged(User user, Bitmap bm) {
			Bitmap avatarBitmap = user.getAvatarBitmap();
			if (avatarBitmap != null && !avatarBitmap.isRecycled()) {
				userIcon.setImageBitmap(avatarBitmap);
			}
		}
	};
	private int recordType;
}
