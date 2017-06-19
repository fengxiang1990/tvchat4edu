package com.bizcom.vc.activity.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.DataBaseContext;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.util.DateUtil;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.activity.main.TabFragmentMessage;
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
import java.util.Collections;
import java.util.List;

public class VoiceMessageActivity extends Activity {

	public static final String TAG = VoiceMessageActivity.class.getSimpleName();
	private TextView callBack;
	private TextView deleteOperator;
	private RelativeLayout deleteLayout;
	private CheckBox selectedAll;
	private ListView mVoicesList;
	private VoiceBaseAdapter adapter;
	private List<AudioVideoMessageBean> mListItem;
	private SparseArray<AudioVideoMessageBean> deleteList;
	private Context mContext;
	private ViewHolder holder = null;
	private VoiceReceiverBroadcast receiver;
	private boolean isVisibile;
	private boolean isEditing;
	private boolean isExecuteSelectAll = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specificitem_voice);
		mContext = this;
		findview();
		initReceiver();
		setListener();
		init();
	}

	private void findview() {
        TextView mTitleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContent.setText(R.string.specificItem_voice_title);
        callBack = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        callBack.setBackgroundResource(R.drawable.title_bar_back_button_selector);
        deleteOperator = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
        deleteOperator.setText(R.string.specificItem_voice_deleteOperator);
        deleteOperator.setVisibility(View.INVISIBLE);

        mVoicesList = (ListView) findViewById(R.id.specific_voice_listview);
		deleteLayout = (RelativeLayout) findViewById(R.id.specific_voice_delete);
		selectedAll = (CheckBox) findViewById(R.id.specific_voice_delete_all);
	}

	private void setListener() {

		callBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mVoicesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (isEditing) {

					CheckBox selected = (CheckBox) view.findViewById(R.id.ws_specific_voice_check);
					if (selected.isChecked()) {
						mListItem.get(position).isCheck = false;
						selected.setChecked(false);
						deleteList.remove(position);
					} else {
						deleteList.put(position, mListItem.get(position));
						mListItem.get(position).isCheck = true;
						selected.setChecked(true);
					}

					if (deleteList.size() == mListItem.size())
						selectedAll.setChecked(true);
					else {
						isExecuteSelectAll = false;
						selectedAll.setChecked(false);
					}

				} else {
					if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
						return;
					}

					AudioVideoMessageBean audioVideoMessageBean = mListItem.get(position);
					if (audioVideoMessageBean.mediaType == AudioVideoMessageBean.TYPE_AUDIO) {

                        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.
                                P2P_CONNECT_AUDIO, audioVideoMessageBean.remoteUserID, false, null, null);
					} else if(audioVideoMessageBean.mediaType == AudioVideoMessageBean.TYPE_SIP){
                        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.
                                P2P_CONNECT_SIP, audioVideoMessageBean.remoteUserID, false, null, String.valueOf(audioVideoMessageBean.remoteUserID));
                    } else {
                        GlobalConfig.startP2PConnectChat(mContext, ConversationP2PAVActivity.
                                P2P_CONNECT_VIDEO, audioVideoMessageBean.remoteUserID, false, null, null);
					}
					// update conversation state
					updateConversationState();
				}
			}
		});

		mVoicesList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				isEditing = true;
				deleteLayout.setVisibility(View.VISIBLE);
				deleteOperator.setVisibility(View.VISIBLE);
                callBack.setBackgroundResource(R.color.common_color_transparent);
                callBack.setText(R.string.specificItem_voice_cannelOperator);

				isVisibile = true;
				adapter.notifyDataSetChanged();
				return false;
			}
		});

		deleteOperator.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (deleteList.size() <= 0) {
					Toast.makeText(mContext, getResources().getString(R.string.contacts_voice_message_toast_select_removed), Toast.LENGTH_SHORT).show();
					return;
				}

				for (int i = 0; i < deleteList.size(); i++) {
					int key = deleteList.keyAt(i);
					AudioVideoMessageBean value = deleteList.valueAt(i);
					mListItem.remove(value);
					deleteList.remove(key);
					String where = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + " = ?";
					String[] selectionArgs = new String[] { String.valueOf(value.remoteUserID) };
					int delete = getContentResolver().delete(ContentDescriptor.HistoriesMedia.CONTENT_URI, where,
							selectionArgs);
					if (delete == 0)
						Log.e(TAG, "delete failed...");
				}

				if (mListItem.size() <= 0) {
					isEditing = false;
					deleteLayout.setVisibility(View.GONE);
					deleteOperator.setVisibility(View.INVISIBLE);
                    callBack.setText(R.string.specificItem_voice_cannelOperator);
                    callBack.setBackgroundResource(R.drawable.title_bar_back_button_selector);

					isVisibile = false;
				}
				adapter.notifyDataSetChanged();
			}
		});

		selectedAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (isExecuteSelectAll) {
					if (isChecked) {
						for (int i = 0; i < mListItem.size(); i++) {
							mListItem.get(i).isCheck = true;
							deleteList.put(i, mListItem.get(i));
						}
					} else {
						for (int i = 0; i < mListItem.size(); i++) {
							mListItem.get(i).isCheck = false;
							deleteList.remove(i);
						}
					}
					adapter.notifyDataSetChanged();
				} else {

					if (isChecked) {
						for (int i = 0; i < mListItem.size(); i++) {
							mListItem.get(i).isCheck = true;
							deleteList.put(i, mListItem.get(i));
						}
						adapter.notifyDataSetChanged();
					}
					isExecuteSelectAll = true;
				}
			}
		});
	}

	private void initReceiver() {
		receiver = new VoiceReceiverBroadcast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConversationP2PAVActivity.P2P_BROADCAST_MEDIA_UPDATE);
		intentFilter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onBackPressed() {
        if(isEditing){
            isEditing = false;
            deleteLayout.setVisibility(View.GONE);
            deleteOperator.setVisibility(View.INVISIBLE);
            callBack.setBackgroundResource(R.drawable.title_bar_back_button_selector);
            callBack.setText("");
            isVisibile = false;
            adapter.notifyDataSetChanged();
        } else {
            setResult(TabFragmentMessage.ACTIVITY_RETURN_VOICE_RECORD);
            super.onBackPressed();
        }
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		BitmapManager.getInstance().unRegisterBitmapChangedListener(bitmapChangedListener);
		if (mListItem != null && mListItem.size() <= 0)
			notificateConversationUpdate();
		mListItem = null;
		deleteList = null;
		super.onDestroy();
	}

	private void init() {
		// 异步去加载数据库
		new AsyncTask<Void, Void, List<AudioVideoMessageBean>>() {

			@Override
			protected List<AudioVideoMessageBean> doInBackground(Void... params) {
				return MediaRecordProvider.loadMediaHistoriesMessage(GlobalHolder.getInstance().getCurrentUserId(),
						AudioVideoMessageBean.TYPE_ALL);
			}

			@Override
			protected void onPostExecute(List<AudioVideoMessageBean> result) {
				super.onPostExecute(result);

				if (result == null) {
					return;
				}

				Collections.sort(result);
				mListItem = result;
				adapter = new VoiceBaseAdapter();
				mVoicesList.setAdapter(adapter);
			}
		}.execute();

		deleteList = new SparseArray<AudioVideoMessageBean>();
		BitmapManager.getInstance().registerBitmapChangedListener(this.bitmapChangedListener);
	}

	/**
	 * 通知ConversationTabFragment 更新会话列表
	 */
	private void notificateConversationUpdate() {

		Intent i = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
		i.addCategory(PublicIntent.DEFAULT_CATEGORY);
		ConversationNotificationObject obj = new ConversationNotificationObject(Conversation.TYPE_VOICE_MESSAGE,
				Conversation.SPECIFIC_VOICE_ID, true);
		i.putExtra("obj", obj);
		mContext.sendBroadcast(i);
	}

	class VoiceBaseAdapter extends BaseAdapter {

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
		public View getView(final int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(VoiceMessageActivity.this, R.layout.activity_specificitem_voice_adapter,
						null);
				holder.voiceName = (TextView) convertView.findViewById(R.id.ws_common_conversation_layout_topContent);
				holder.unreadNumber = (TextView) convertView.findViewById(R.id.ws_specific_voice_unreadNumber);
				holder.watchDetail = (TextView) convertView.findViewById(R.id.specific_voice_watchDetail);
				holder.voiceHoldingTime = (TextView) convertView
						.findViewById(R.id.ws_common_conversation_layout_belowContent);
				holder.directionIcon = (ImageView) convertView.findViewById(R.id.ws_specific_voice_direction_icon);
				holder.headIcon = (CustomAvatarImageView) convertView.findViewById(R.id.ws_common_avatar);
				holder.selected = (CheckBox) convertView.findViewById(R.id.ws_specific_voice_check);

				holder.unreadNumber.setVisibility(View.VISIBLE);
				holder.directionIcon.setVisibility(View.VISIBLE);
				// holder.notifyIcon = (ImageView) convertView
				// .findViewById(R.id.group_list_conference_notificator);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			final AudioVideoMessageBean audioVideoMessageBean = mListItem.get(position);
			if (audioVideoMessageBean.mediaType == AudioVideoMessageBean.TYPE_SIP) {
				holder.headIcon.setImageResource(R.drawable.avatar);
				holder.voiceName.setText(audioVideoMessageBean.name);
			} else {
				User remoteUser = GlobalHolder.getInstance().getUser(audioVideoMessageBean.remoteUserID);
				holder.headIcon.setImageBitmap(remoteUser.getAvatarBitmap());
				holder.voiceName.setText(audioVideoMessageBean.name);
			}
			// 处理时间显示
			// if (audioVideoMessageBean.holdingTime >= 0) {
			// String time = DateUtil
			// .getDates(audioVideoMessageBean.holdingTime);
			// if (audioVideoMessageBean.mediaType ==
			// AudioVideoMessageBean.TYPE_AUDIO)
			// holder.voiceHoldingTime.setText("[音频] " + time);
			// else
			// holder.voiceHoldingTime.setText("[视频] " + time);
			// } else {
			// if (audioVideoMessageBean.mediaType ==
			// AudioVideoMessageBean.TYPE_AUDIO)
			// holder.voiceHoldingTime.setText("[音频] ");
			// else
			// holder.voiceHoldingTime.setText("[视频] ");
			// }
			String time = null;
			ChildMessageBean childMessageBean = audioVideoMessageBean.mChildBeans.get(0);
			if (childMessageBean != null)
				time = DateUtil.getStringDate(childMessageBean.childSaveDate);
			if (!TextUtils.isEmpty(time)) {
				if (audioVideoMessageBean.mediaType == AudioVideoMessageBean.TYPE_AUDIO)
					holder.voiceHoldingTime
							.setText(getResources().getString(R.string.contact_message_auto_voice) + time);
				else if (audioVideoMessageBean.mediaType == AudioVideoMessageBean.TYPE_VIDEO)
					holder.voiceHoldingTime
							.setText(getResources().getString(R.string.contact_message_auto_video) + time);
				else
					holder.voiceHoldingTime.setText(getResources().getString(R.string.contact_message_auto_sip) + time);
			} else {
				if (audioVideoMessageBean.mediaType == AudioVideoMessageBean.TYPE_AUDIO)
					holder.voiceHoldingTime.setText(getResources().getString(R.string.contact_message_auto_voice));
				else
					holder.voiceHoldingTime.setText(getResources().getString(R.string.contact_message_auto_video));
			}
			// 处理图标
            // 区分是主动拨出还是接收
			if (audioVideoMessageBean.isCallOut == AudioVideoMessageBean.STATE_CALL_OUT) {
                // 区分是对方对通话是否进行了处理
                if(audioVideoMessageBean.meidaState == AudioVideoMessageBean.STATE_ANSWER_CALL){
                    // 区分是对方接受了还是拒绝了
                    if(audioVideoMessageBean.holdingTime <= 0){
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_refuse);
                    } else {
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_callout);
                    }
                } else {
                    holder.directionIcon.setImageResource(R.drawable.vs_voice_refuse);
                }

				holder.voiceName.setTextColor(Color.BLACK);
				holder.unreadNumber.setVisibility(View.GONE);
				audioVideoMessageBean.callNumbers = 0;
			} else {
                // 处理icon
                if(audioVideoMessageBean.meidaState == AudioVideoMessageBean.STATE_ANSWER_CALL) {
                    // 区分是对方接受了还是拒绝了
                    if (audioVideoMessageBean.holdingTime <= 0) {
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_coming_refuse);
                    } else {
                        holder.directionIcon.setImageResource(R.drawable.vs_voice_listener);
                    }
                } else {
                    holder.directionIcon.setImageResource(R.drawable.vs_voice_coming_refuse);
                }


				// 处理未读数量和字体颜色
				if (audioVideoMessageBean.readState == V2GlobalConstants.READ_STATE_UNREAD) {
					holder.unreadNumber.setVisibility(View.VISIBLE);
					holder.voiceName.setTextColor(Color.RED);
					holder.unreadNumber.setText(" ( " + audioVideoMessageBean.callNumbers + " )");
					holder.unreadNumber.setTextColor(Color.RED);
				} else {
					holder.voiceName.setTextColor(Color.BLACK);
					holder.unreadNumber.setVisibility(View.GONE);
					audioVideoMessageBean.callNumbers = 0;
				}
			}

			if (isVisibile) {
				holder.selected.setVisibility(View.VISIBLE);
				holder.watchDetail.setVisibility(View.INVISIBLE);
			} else {
				holder.selected.setVisibility(View.GONE);
				holder.watchDetail.setVisibility(View.VISIBLE);
			}

			if (mListItem.get(position).isCheck) {
				holder.selected.setChecked(true);
			} else {
				holder.selected.setChecked(false);
			}

			holder.watchDetail.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					AudioVideoMessageBean bean = mListItem.get(position);
					ArrayList<ChildMessageBean> mChildBeans = bean.mChildBeans;

					// holder.notifyIcon.setVisibility(View.INVISIBLE);
					bean.callNumbers = 0;

					ContentValues values = new ContentValues();
					values.put(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE,
							V2GlobalConstants.READ_STATE_READ);
					String where = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE + "= ? and "
							+ ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + "= ?";
					String[] selectionArgs = new String[] { String.valueOf(V2GlobalConstants.READ_STATE_UNREAD),
							String.valueOf(audioVideoMessageBean.remoteUserID) };
					DataBaseContext context = new DataBaseContext(VoiceMessageActivity.this);
					context.getContentResolver().update(ContentDescriptor.HistoriesMedia.CONTENT_URI, values, where,
							selectionArgs);

					updateConversationState();

					Intent intent = new Intent(mContext, VoiceMessageDetailActivity.class);
					intent.putParcelableArrayListExtra("messages", mChildBeans);
					intent.putExtra("remoteUserID", bean.remoteUserID);
					intent.putExtra("mediaType", bean.mediaType);
					startActivity(intent);
					finish();
				}
			});
			return convertView;
		}
	}

	class ViewHolder {

		public TextView voiceName;
		public TextView unreadNumber;
		public TextView watchDetail;
		public TextView voiceHoldingTime;
		public ImageView directionIcon;
		public CustomAvatarImageView headIcon;
		public CheckBox selected;
		// public ImageView notifyIcon;
	}

	private boolean isFresh;

	class VoiceReceiverBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			long remoteID = intent.getLongExtra("remoteID", -1l);
			if (remoteID == -1l) {
				Log.e(TAG, "get remoteID is -1 ... update failed!!");
				return;
			}

			String selections = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID + "= ? ";
			String[] selectionArgs = new String[] { String.valueOf(remoteID) };
			VideoBean newestMediaMessage = MediaRecordProvider.getNewestMediaMessage(selections, selectionArgs);
			if (newestMediaMessage == null) {
				Log.e(TAG, "get newest remoteID " + remoteID + " --> VideoBean is NULL ... update failed!!");
				return;
			}
			ChildMessageBean childBean = new ChildMessageBean();
			for (int i = 0; i < mListItem.size(); i++) {
				AudioVideoMessageBean target = mListItem.get(i);
				if (target.remoteUserID == remoteID) {
					target.holdingTime = newestMediaMessage.endDate - newestMediaMessage.startDate;
					target.mediaType = newestMediaMessage.mediaType;
					target.meidaState = newestMediaMessage.mediaState;
					target.readState = newestMediaMessage.readSatate;
					if (GlobalHolder.getInstance().getCurrentUserId() == newestMediaMessage.formUserID)
						target.isCallOut = AudioVideoMessageBean.STATE_CALL_OUT;
					else
						target.isCallOut = AudioVideoMessageBean.STATE_CALL_IN;
					if (target.readState == V2GlobalConstants.READ_STATE_UNREAD) {

						target.callNumbers += 1;
						// holder.notifyIcon.setVisibility(View.VISIBLE);
					}

					childBean.childMediaType = target.mediaType;
					childBean.childISCallOut = target.isCallOut;
					childBean.childHoldingTime = target.holdingTime;
					childBean.childSaveDate = newestMediaMessage.startDate;
					childBean.childReadState = target.readState;
					childBean.childMediaState = target.meidaState;
					target.mChildBeans.add(0, childBean);
					mListItem.remove(i);
					mListItem.add(0, target);
					adapter.notifyDataSetChanged();
					isFresh = true;
					return;
				}
			}

			if (!isFresh) {
				AudioVideoMessageBean bean = new AudioVideoMessageBean();
				if (newestMediaMessage.formUserID != GlobalHolder.getInstance().getCurrentUserId())
					bean.isCallOut = AudioVideoMessageBean.STATE_CALL_IN;
				else
					bean.isCallOut = AudioVideoMessageBean.STATE_CALL_OUT;

				if (newestMediaMessage.readSatate == V2GlobalConstants.READ_STATE_UNREAD) {
					bean.callNumbers += 1;
				}
				bean.name = GlobalHolder.getInstance().getUser(newestMediaMessage.remoteUserID).getDisplayName();
				bean.fromUserID = newestMediaMessage.formUserID;
				bean.toUserID = newestMediaMessage.toUserID;
				bean.remoteUserID = newestMediaMessage.remoteUserID;
				bean.readState = newestMediaMessage.readSatate;
				bean.mediaType = newestMediaMessage.mediaType;
				bean.holdingTime = newestMediaMessage.endDate - newestMediaMessage.startDate;
				mListItem.add(0, bean);

				childBean.childMediaType = bean.mediaType;
				childBean.childISCallOut = bean.isCallOut;
				childBean.childHoldingTime = bean.holdingTime;
				childBean.childSaveDate = newestMediaMessage.startDate;
				childBean.childReadState = bean.readState;
				childBean.childMediaState = bean.mediaType;
				bean.mChildBeans.add(0, childBean);

				isFresh = true;
				adapter.notifyDataSetChanged();
			}

		}
	}

	private void updateConversationState() {

		boolean isBroadcast = true;
		for (AudioVideoMessageBean bean : mListItem) {
			if (bean.callNumbers > 0) {
				isBroadcast = false;
				break;
			}
		}

		if (isBroadcast) {
			Intent i = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			ConversationNotificationObject obj = new ConversationNotificationObject(Conversation.TYPE_VOICE_MESSAGE,
					Conversation.SPECIFIC_VOICE_ID, false);
			obj.setMsgID(0);
			i.putExtra("obj", obj);
			i.putExtra("isFresh", false);
			i.putExtra("isDelete", false);
			mContext.sendBroadcast(i);
		}
	}

	private BitmapManager.BitmapChangedListener bitmapChangedListener = new BitmapManager.BitmapChangedListener() {

		@Override
		public void notifyAvatarChanged(User user, Bitmap bm) {
			for (AudioVideoMessageBean bean : mListItem) {
				User remoteUser = GlobalHolder.getInstance().getUser(bean.remoteUserID);
				Bitmap avatarBitmap = remoteUser.getAvatarBitmap();
				if (avatarBitmap != null && !avatarBitmap.isRecycled()) {
					holder.headIcon.setImageBitmap(avatarBitmap);
				}
			}
		}
	};
}
