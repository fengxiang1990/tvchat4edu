package com.bizcom.vc.activity.conversation;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html.ImageGetter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.FileUtils;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.MessageUtil.ChatTextViewClick;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.crow.CrowdFilesActivity.CrowdFileActivityType;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vc.widget.cus.span.ClickSpanMovementMethod;
import com.bizcom.vo.FileDownLoadBean;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageFileItem.FileType;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageLinkTextItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.Date;
import java.util.List;

@SuppressLint("InflateParams")
public class MessageBodyView extends LinearLayout {

	private static final String TAG = MessageBodyView.class.getSimpleName();
	private static final int MESSAGE_TYPE_TEXT = 11;
	private static final int MESSAGE_TYPE_IMAGE = 12;
	private static final int MESSAGE_TYPE_FILE = 13;
	private static final int MESSAGE_TYPE_AUDIO = 14;
	private static final int SENDING_CIRCLE_RATE = 600;

	private int messageType = MESSAGE_TYPE_TEXT;
	private VMessage mMsg;

	private View rootView;
	private LinearLayout mContentContainer;
	private CustomAvatarImageView mHeadIcon;
	private TextView mShowtimeTV;
	private View failedIcon;
	private View unReadIcon;

	private ImageView micRecordIV;
	private TextView mShowRecordTimeTV;

	private TextView mContentTV;
	private View sendingIcon;

	private PopupWindow pw;
	private RelativeLayout popWindow;
	private TextView pwReDownloadTV;
	private TextView pwResendTV;
	private TextView pwCopyTV;

	/**
	 * The flag decide that Whether should to display the Time View
	 */
	private boolean isShowTime;

	private Handler localHandler;
	private Runnable popupWindowListener = null;

	private ClickListener callback;

	private long lastUpdateTime;
	private int instantProgressWidth = 0;
	private int popupWindowWidth;
	private int popupWindowHeight;

	private RotateAnimation anima;
	private MessageBodyType bodyType;
	
	public MessageBodyView(Context context, VMessage m, boolean isShowTime) {
		super(context);
		initMessageBody(m, isShowTime);
	}

	public void initMessageBody(VMessage m, boolean isShowTime) {
		if (m == null) {
			V2Log.e(TAG, "Given VMessage Object is null!");
			return;
		}

		mMsg = m;
		this.isShowTime = isShowTime;
		this.localHandler = new Handler();
		messageType = getMessgeBodyType(mMsg);
		initView();
		populateMessage();
		initPopupWindow();
	}

	@SuppressLint("InflateParams")
	private void initView() {
		removeAllViews();
		if (mMsg.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER) {
			this.bodyType = MessageBodyType.SINGLE_USER_TYPE;
			rootView = LayoutInflater.from(getContext()).inflate(R.layout.message_body, null, false);
		} else {
			this.bodyType = MessageBodyType.GROUP_TYPE;
			rootView = LayoutInflater.from(getContext()).inflate(R.layout.crowd_message_body, null, false);
		}

		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addView(rootView, ll);

		anima = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anima.setDuration(SENDING_CIRCLE_RATE);
		anima.setRepeatCount(RotateAnimation.INFINITE);

		LinearLayout mLocalMessageContainter = (LinearLayout) rootView.findViewById(R.id.message_body_left_user_ly);
		LinearLayout mRemoteMessageContainter = (LinearLayout) rootView.findViewById(R.id.message_body_remote_ly);

		mShowtimeTV = (TextView) rootView.findViewById(R.id.message_body_time_text);
		if (!mMsg.isLocal()) {
			mHeadIcon = (CustomAvatarImageView) rootView.findViewById(R.id.conversation_message_body_icon_left);
			mContentContainer = (LinearLayout) rootView.findViewById(R.id.messag_body_content_ly_left);
			mShowRecordTimeTV = (TextView) rootView.findViewById(R.id.message_body_video_item_second_left);
			failedIcon = rootView.findViewById(R.id.message_body_failed_item_left);
			unReadIcon = rootView.findViewById(R.id.message_body_unread_icon_left);
			sendingIcon = rootView.findViewById(R.id.message_body_sending_icon_left);
			mLocalMessageContainter.setVisibility(View.VISIBLE);
			mRemoteMessageContainter.setVisibility(View.INVISIBLE);
		} else {
			mHeadIcon = (CustomAvatarImageView) rootView.findViewById(R.id.conversation_message_body_icon_right);
			mContentContainer = (LinearLayout) rootView.findViewById(R.id.messag_body_content_ly_right);
			mShowRecordTimeTV = (TextView) rootView.findViewById(R.id.message_body_video_item_second_right);
			failedIcon = rootView.findViewById(R.id.message_body_failed_item_right);
			unReadIcon = rootView.findViewById(R.id.message_body_unread_icon_right);
			sendingIcon = rootView.findViewById(R.id.message_body_sending_icon_right);
			mLocalMessageContainter.setVisibility(View.INVISIBLE);
			mRemoteMessageContainter.setVisibility(View.VISIBLE);
		}

		failedIcon.setVisibility(View.GONE);
		unReadIcon.setVisibility(View.GONE);
		sendingIcon.setVisibility(View.GONE);
		mShowRecordTimeTV.setVisibility(View.GONE);

		if (isShowTime) {
			mShowtimeTV.setVisibility(View.VISIBLE);
			mShowtimeTV.setText(mMsg.getStringDate());
		} else {
			mShowtimeTV.setVisibility(View.GONE);
		}

		if (mContentTV != null && mContentTV.getParent() != null) {
			LinearLayout mContentContainer = (LinearLayout) mContentTV.getParent();
			mContentContainer.removeView(mContentTV);
		}
		mContentContainer.removeView(mContentTV);
		mContentContainer.setTag(this.mMsg);
		updateChatUser();
		initListener();

		if (mMsg.getFromUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			if (mMsg.getFileItems().size() <= 0) {
				if (mMsg.getState() == VMessageAbstractItem.TRANS_TRANSING) {
					updateSendingAnima(true);
				} else {
					updateSendingAnima(false);
				}
			} else {
				updateSendingAnima(false);
			}
		} else {
			if (mMsg.getAudioItems().size() > 0) {
				if (mMsg.getAudioItems().get(0).getState() == VMessageAbstractItem.TRANS_WAIT_RECEIVE) {
					updateSendingAnima(true);
				} else {
					updateSendingAnima(false);
				}
			} else {
				updateSendingAnima(false);
			}
		}
	}

	private int oldLen = 0;

	public void initTextView() {
		if (mContentTV == null) {
			mContentTV = new TextView(getContext());
			mContentTV.setBackgroundColor(Color.TRANSPARENT);
            if (mMsg.isLocal()){
                mContentTV.setTextColor(Color.WHITE);
            } else {
                mContentTV.setTextColor(Color.BLACK);
            }
			mContentTV.setGravity(Gravity.CENTER_VERTICAL);
            int padding = getContext().getResources().getDimensionPixelSize(R.dimen.messageBodyView_content_padding_horizontal);
            mContentTV.setPadding(padding , 0 , padding , 0);
			// 该设置用于选择的内容背景的颜色，
			mContentTV.setHighlightColor(Color.TRANSPARENT);
			mContentTV.setOnLongClickListener(messageLongClickListener);
			mContentTV.setOnTouchListener(touchListener);
			mContentTV.setSelected(false);
			mContentTV.setMovementMethod(ClickSpanMovementMethod.getInstance());
		} else {
			oldLen = mContentTV.getText().length();
			mContentTV.setText("");
            if (mMsg.isLocal()){
                mContentTV.setTextColor(Color.WHITE);
            } else {
                mContentTV.setTextColor(Color.BLACK);
            }
		}

		MessageUtil.buildChatTextView(getContext(), mContentTV, mMsg, textViewClick);
		if (oldLen > mContentTV.getText().length()) {
			mContentTV.setLayoutParams(mContentTV.getLayoutParams());
		}

		if (mContentTV.getParent() == null) {
			LinearLayout.LayoutParams contentLayout = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			mContentContainer.addView(mContentTV, contentLayout);
		}

		if (mMsg.getFromUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			if (mMsg.getImageItems().size() > 0) {
				VMessageImageItem imageItem = mMsg.getImageItems().get(0);
				if (imageItem.getState() == VMessageAbstractItem.TRANS_SENT_FALIED) {
					updateFailedFlag(true);
				} else {
					updateFailedFlag(false);
				}
			} else {
				if (mMsg.getState() == VMessageAbstractItem.TRANS_SENT_FALIED) {
					updateFailedFlag(true);
				} else {
					updateFailedFlag(false);
				}
			}
		} else {
			updateFailedFlag(false);
		}
	}

	ChatTextViewClick textViewClick = new ChatTextViewClick() {
		@Override
		public void imageItemClick(VMessageImageItem imageItem) {
			if (!pw.isShowing())
				callback.onImageItemClick(mMsg, imageItem);
		}

		@Override
		public void LinkItemClick(VMessageLinkTextItem link) {
			callback.onLinkMessageClicked(link);
		}

		@Override
		public void imageFrush(TextView mMsgBodyTV, VMessageImageItem imageItem, Bitmap result,
				ChatTextViewClick imageItemCallBack) {
			// if(callback != null)
			// callback.requestFlushImage(mMsgBodyTV, imageItem, result ,
			// imageItemCallBack);
			initTextView();
		}
	};

	ImageGetter imageGetter = new ImageGetter() {

		@Override
		public Drawable getDrawable(String source) {
			int id = Integer.parseInt(source);
			Drawable drawable = getResources().getDrawable(id);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			return drawable;
		}
	};

	private void initListener() {
		mContentContainer.setOnLongClickListener(messageLongClickListener);
		mContentContainer.setOnTouchListener(touchListener);
		mContentContainer.setOnClickListener(mAudioMssageClickListener);
	}

	@SuppressLint("InflateParams")
	private void initPopupWindow() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popWindow = (RelativeLayout) inflater.inflate(R.layout.message_selected_pop_up_window, null, false);
		pw = new PopupWindow(popWindow, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		pw.setFocusable(true);
		pw.setTouchable(true);
		pw.setOutsideTouchable(true);
		pwResendTV = (TextView) popWindow.findViewById(R.id.contact_message_pop_up_item_resend);
		pwResendTV.setOnClickListener(mResendButtonListener);

		pwReDownloadTV = (TextView) popWindow.findViewById(R.id.contact_message_pop_up_item_redownload);
		pwReDownloadTV.setOnClickListener(mResendButtonListener);

		pwCopyTV = (TextView) popWindow.findViewById(R.id.contact_message_pop_up_item_copy);
		pwCopyTV.setOnClickListener(mCopyButtonListener);

		TextView pwDeleteTV = (TextView) popWindow.findViewById(R.id.contact_message_pop_up_item_delete);
		pwDeleteTV.setOnClickListener(mDeleteButtonListener);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		popWindow.getChildAt(0).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		popupWindowHeight = popWindow.getChildAt(0).getMeasuredHeight();
		popupWindowWidth = popWindow.getChildAt(0).getMeasuredWidth();
	}

	private void populateMessage() {
		List<VMessageAudioItem> audioItems = mMsg.getAudioItems();
		if (audioItems.size() > 0) {
			mContentContainer.removeAllViews();
			populateAudioMessage(audioItems);
			VMessageAudioItem vMessageAudioItem = audioItems.get(0);
			if (vMessageAudioItem.isPlaying()
					&& vMessageAudioItem.getState() != VMessageAbstractItem.TRANS_SENT_FALIED) {
				startVoiceAnimation();
			} else {
				stopVoiceAnimation();
			}
			return;
		}

		List<VMessageFileItem> fileItems = mMsg.getFileItems();
		if (fileItems.size() > 0) {
			mContentContainer.removeAllViews();
			populateFileItem(fileItems);
			return;
		}
		initTextView();
	}

	/**
	 * Now only handle one audio message
	 *
	 * @param audioItems
	 */
	private void populateAudioMessage(List<VMessageAudioItem> audioItems) {
		final VMessageAudioItem item = audioItems.get(0);
		if (item.getReadState() == VMessageAbstractItem.STATE_READED)
			unReadIcon.setVisibility(View.GONE);
		else {
			if (item.getState() == VMessageAbstractItem.TRANS_WAIT_RECEIVE
					|| item.getState() == VMessageAbstractItem.TRANS_SENT_FALIED) {
				unReadIcon.setVisibility(View.GONE);
			} else {
				unReadIcon.setVisibility(View.VISIBLE);
			}
		}

		if (item.getState() == VMessageAbstractItem.TRANS_SENT_FALIED)
			updateFailedFlag(true);
		else
			updateFailedFlag(false);

		mShowRecordTimeTV.setVisibility(View.VISIBLE);
		mShowRecordTimeTV.setText(item.getSeconds() + "''");

		RelativeLayout audioRoot = new RelativeLayout(getContext());
		micRecordIV = new ImageView(getContext());
		micRecordIV.setId(micRecordIV.hashCode());

		RelativeLayout.LayoutParams micIvLy = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		TextView tv = new TextView(getContext());
		tv.setId(tv.hashCode());
		for (int in = 0; in < item.getSeconds() && in < 40; in++) {
			tv.append(" ");
		}

		RelativeLayout.LayoutParams tvIvLy = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		if (mMsg.isLocal()) {
			micRecordIV.setImageResource(R.drawable.voice_message_mic_icon_self_selector);
			audioRoot.addView(tv, tvIvLy);

			micIvLy.addRule(RelativeLayout.RIGHT_OF, tv.getId());
			audioRoot.addView(micRecordIV, micIvLy);

		} else {
			micIvLy.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			micIvLy.addRule(RelativeLayout.CENTER_IN_PARENT);
			micRecordIV.setImageResource(R.drawable.voice_message_mic_icon_selector);
			audioRoot.addView(micRecordIV, micIvLy);

			tvIvLy.addRule(RelativeLayout.RIGHT_OF, micRecordIV.getId());
			audioRoot.addView(tv, tvIvLy);
		}

		audioRoot.setOnClickListener(mAudioMssageClickListener);
		audioRoot.setOnLongClickListener(messageLongClickListener);
		audioRoot.setOnTouchListener(touchListener);
		mContentContainer.addView(audioRoot, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

	}

	private void populateFileItem(List<VMessageFileItem> fileItems) {
		// 获取文件类型
		VMessageFileItem item = fileItems.get(0);
		String itemName = item.getFileName();
		FileType fileType = FileUtils.getFileType(itemName);
		item.setFileType(fileType);
		// 获取相应的View
		View fileRootView = LayoutInflater.from(getContext()).inflate(R.layout.message_body_file_item, null, false);
		ImageView fileIcon = (ImageView) fileRootView.findViewById(R.id.message_body_file_item_icon_ly);
		TextView fileName = (TextView) fileRootView.findViewById(R.id.message_body_file_item_file_name);
		TextView fileSize = (TextView) fileRootView.findViewById(R.id.message_body_file_item_file_size);
		// 设置View显示内容
		fileName.setText(item.getFileName());
		fileIcon.setBackgroundResource(FileUtils.adapterFileIcon(item.getFileType()));
		fileSize.setText(item.getFileSizeStr());
		fileRootView.setOnClickListener(fileMessageItemClickListener);
		fileRootView.setOnLongClickListener(messageLongClickListener);
		fileRootView.setOnTouchListener(touchListener);
		fileRootView.setTag(item);
		mContentContainer.setGravity(Gravity.CENTER_VERTICAL);
		mContentContainer.addView(fileRootView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		if (item.getState() == VMessageAbstractItem.STATE_FILE_SENT_FALIED
				|| item.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED) {
			updateFailedFlag(true);
		} else {
			updateFailedFlag(false);
		}
		updateFileItemView(item, fileRootView);
	}

	private void showPopupWindow(final View anchor) {
		if (popupWindowListener == null) {
			popupWindowListener = new Runnable() {

				@Override
				public void run() {
					if (messageType == MESSAGE_TYPE_TEXT)
						pwCopyTV.setVisibility(View.VISIBLE);
					else
						pwCopyTV.setVisibility(View.GONE);

					if (failedIcon.getVisibility() == View.VISIBLE) {
						if (mMsg.isLocal()) {
							pwResendTV.setVisibility(View.VISIBLE);
							pwReDownloadTV.setVisibility(View.GONE);
						} else {
							if(messageType == MESSAGE_TYPE_AUDIO){
								pwReDownloadTV.setVisibility(View.GONE);
							} else {
								pwReDownloadTV.setVisibility(View.VISIBLE);
							}
							pwResendTV.setVisibility(View.GONE);
						}
					} else {
						pwResendTV.setVisibility(View.GONE);
						pwReDownloadTV.setVisibility(View.GONE);
					}

					int offsetX = rawX - (popupWindowWidth / 3);
					int offsetY = rawY - (popupWindowHeight / 2) * 3;
					if (offsetY < 0)
						offsetY = Math.abs(offsetY);
					pw.showAtLocation((View) anchor.getParent(), Gravity.NO_GRAVITY, offsetX, offsetY);
				}
			};
		}

		localHandler.postDelayed(popupWindowListener, 200);
	}

	public void setCallback(ClickListener cl) {
		this.callback = cl;
	}

	public ClickListener getCallback() {
		return callback;
	}

	public void updateAudioReadState(boolean flag, VMessageAudioItem item) {
		if (!flag) {
			unReadIcon.setVisibility(View.GONE);
			item.setReadState(VMessageAbstractItem.STATE_READED);
		} else {
			unReadIcon.setVisibility(View.VISIBLE);
			item.setReadState(VMessageAbstractItem.STATE_UNREAD);
		}
		ChatMessageProvider.updateBinaryAudioItem(item);
	}

	public void updateFailedFlag(boolean flag) {
		if (!flag) {
			failedIcon.setVisibility(View.GONE);
		} else {
			failedIcon.setVisibility(View.VISIBLE);
		}
	}

	public void updateSendingAnima(boolean isBegin) {
		if (!isBegin) {
			sendingIcon.setVisibility(View.GONE);
			sendingIcon.clearAnimation();
		} else {
			sendingIcon.setVisibility(View.VISIBLE);
			sendingIcon.startAnimation(anima);
		}
	}

	public void startVoiceAnimation() {
		if (mMsg.isLocal())
			micRecordIV.setImageResource(R.drawable.conversation_local_speaking);
		else
			micRecordIV.setImageResource(R.drawable.conversation_remote_speaking);

		AnimationDrawable drawable = (AnimationDrawable) micRecordIV.getDrawable();
		if (!drawable.isRunning())
			drawable.start();
	}

	private int getMessgeBodyType(VMessage vm) {
		List<VMessageImageItem> imageItems = vm.getImageItems();
		if (imageItems.size() > 0) {
			return MESSAGE_TYPE_IMAGE;
		}

		List<VMessageAudioItem> audioItems = vm.getAudioItems();
		if (audioItems.size() > 0) {
			return MESSAGE_TYPE_AUDIO;
		}

		List<VMessageFileItem> fileItems = vm.getFileItems();
		if (fileItems.size() > 0) {
			return MESSAGE_TYPE_FILE;
		}
		return MESSAGE_TYPE_TEXT;
	}

	public void stopVoiceAnimation() {
		if (mMsg.isLocal())
			micRecordIV.setImageResource(R.drawable.conversation_local_speaking);
		else
			micRecordIV.setImageResource(R.drawable.conversation_remote_speaking);

		AnimationDrawable drawable = (AnimationDrawable) micRecordIV.getDrawable();
		drawable.stop();

		if (mMsg.isLocal())
			micRecordIV.setImageResource(R.drawable.voice_message_mic_icon_self_selector);
		else
			micRecordIV.setImageResource(R.drawable.voice_message_mic_icon_selector);
	}

	public void updateView(VMessage vm) {
		long oldUserID = mMsg.getFromUser().getmUserId();
		String olderUuid = mMsg.getUUID();
		boolean isLocal = mMsg.isLocal();
		int oldMessageType = messageType;
		messageType = getMessgeBodyType(vm);
		if (oldMessageType != messageType) {
			mContentContainer.removeAllViews();
		}
		// 重新将VMesage赋值
		mMsg = vm;
		isShowTime = vm.isShowTime();
		if (isLocal != mMsg.isLocal() && !olderUuid.equals(mMsg.getUUID())) {
			initView();
		} else {
			if (oldUserID != mMsg.getFromUser().getmUserId()) {
				updateChatUser();
			} else {
				if (mMsg.isUpdateAvatar) {
					updateAvatar(vm.getFromUser().getAvatarBitmap());
					vm.isUpdateAvatar = false;
				}
			}

			if (isShowTime) {
				if (mShowtimeTV.getVisibility() == View.GONE) {
					mShowtimeTV.setVisibility(View.VISIBLE);
				}
				mShowtimeTV.setText(mMsg.getStringDate());
			} else {
				if (mShowtimeTV.getVisibility() == View.VISIBLE) {
					mShowtimeTV.setVisibility(View.GONE);
				}
			}

			if (mMsg.getFromUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
				if (mMsg.getFileItems().size() <= 0) {
					if (mMsg.getState() == VMessageAbstractItem.TRANS_TRANSING) {
						updateSendingAnima(true);
					} else {
						updateSendingAnima(false);
					}
				} else {
					updateSendingAnima(false);
				}
			} else {
				if (mMsg.getAudioItems().size() > 0) {
					if (mMsg.getAudioItems().get(0).getState() == VMessageAbstractItem.TRANS_WAIT_RECEIVE) {
						updateSendingAnima(true);
					} else {
						updateSendingAnima(false);
					}
				} else {
					updateSendingAnima(false);
				}
			}

			if (vm.isUpdateDate) {
				updateDate();
				vm.isUpdateDate = false;
			}

			unReadIcon.setVisibility(View.GONE);
			mShowRecordTimeTV.setVisibility(View.GONE);
			mContentContainer.setTag(vm);
		}
		populateMessage();
	}

	public void updateView(VMessageFileItem vfi) {
		if (vfi == null || mMsg.getFileItems().size() < 0
				|| !vfi.getUuid().equals(mMsg.getFileItems().get(0).getUuid())) {
			return;
		}

		View fileRootView = mContentContainer.getChildAt(0);
		updateFileItemView(vfi, fileRootView);

	}

	public void updateDate() {
		mMsg.setDate(new Date(GlobalConfig.getGlobalServerTime()));
		if (isShowTime && mMsg.getDate() != null) {
			mShowtimeTV.setVisibility(View.VISIBLE);
			mShowtimeTV.setText(mMsg.getStringDate());
		} else {
			mShowtimeTV.setVisibility(View.GONE);
		}
	}

	private void updateChatUser() {
		if (mMsg.getFromUser() != null) {
			User fromUser = GlobalHolder.getInstance().getUser(mMsg.getFromUser().getmUserId());
			if (fromUser != null) {
				mMsg.setFromUser(fromUser);
			} else {
				V2Log.e(" MessageBody doesn't receve user[" + mMsg.getFromUser().getmUserId()
						+ "] information from server");
			}
		}

		if (!mMsg.isLocal()) {
			User fromUser = mMsg.getFromUser();
			if (fromUser != null) {
				if (bodyType == MessageBodyType.GROUP_TYPE) {
					TextView mNameTV = (TextView) rootView.findViewById(R.id.message_body_person_name_left);
					mNameTV.setText(fromUser.getDisplayName());
				}
				updateAvatar(fromUser.getAvatarBitmap());
			}
		} else {
			User localUser = GlobalHolder.getInstance().getCurrentUser();
			updateAvatar(localUser.getAvatarBitmap());
		}
	}

	public void dissmisPopupWindow() {
		if (pw.isShowing())
			pw.dismiss();
	}

	private void updateFileItemView(VMessageFileItem vfi, View rootView) {
		TextView mFileTransState = (TextView) rootView.findViewById(R.id.message_body_file_item_state);
		ImageView mFileTransStateBT = (ImageView) rootView
				.findViewById(R.id.message_body_file_item_progress_action_button);
		boolean showProgressLayout = updateFileItemStateText(vfi, mFileTransState, mFileTransStateBT);
		if (showProgressLayout) {
			rootView.findViewById(R.id.message_body_file_item_progress_layout).setVisibility(View.VISIBLE);
			TextView progress = (TextView) rootView.findViewById(R.id.message_body_file_item_progress_size);
			TextView speed = (TextView) rootView.findViewById(R.id.message_body_file_item_progress_speed);
			final View iv = rootView.findViewById(R.id.message_body_file_item_progress_state);
			final android.view.ViewGroup.LayoutParams params = iv.getLayoutParams();
			FileDownLoadBean bean = GlobalHolder.getInstance().mGlobleFileProgress.get(vfi.getUuid());
			if (bean != null) {
				vfi.setDownloadedSize(bean.currentLoadSize);
			}
			// 设置 已下载/文件大小 显示状态
			progress.setText(vfi.getDownloadSizeStr() + "/" + vfi.getFileSizeStr());
			// 设置速度
			if (vfi.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING
					|| vfi.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING) {
				speed.setText("0KB/S");
			} else {
				if (bean != null) {
					V2Log.e(TAG, "lastLoadTime : " + bean.lastLoadTime + " lastLoadSize : " + bean.lastLoadSize
							+ " currentLoadSize : " + bean.currentLoadSize);
					lastUpdateTime = bean.lastLoadTime;
					vfi.setDownloadedSize(bean.currentLoadSize);
					long sec = (System.currentTimeMillis() - lastUpdateTime);
					long size = vfi.getDownloadedSize() - bean.lastLoadSize;
					vfi.setSpeed((size / sec) * 1000);
					speed.setText(vfi.getSpeedStr());
				} else {
					lastUpdateTime = System.currentTimeMillis();
					speed.setText("0KB/S");
				}
			}
			// 设置进度
			final float percent = (float) ((double) vfi.getDownloadedSize() / (double) vfi.getFileSize());
			final ViewGroup progressC = (ViewGroup) rootView
					.findViewById(R.id.message_body_file_item_progress_state_ly);
			ViewTreeObserver viewTreeObserver = progressC.getViewTreeObserver();
			viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					if (instantProgressWidth == 0) {
						instantProgressWidth = progressC.getMeasuredWidth();
						params.width = (int) (instantProgressWidth * percent);
						iv.setLayoutParams(params);
					}
					return true;
				}
			});
			// progressC.measure(View.MeasureSpec.makeMeasureSpec(0,
			// View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
			// .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
			// width = progressC.getMeasuredWidth();
			params.width = (int) (instantProgressWidth * percent);
			iv.setLayoutParams(params);

		} else {
			rootView.findViewById(R.id.message_body_file_item_progress_layout).setVisibility(View.GONE);
		}
	}

	private boolean updateFileItemStateText(VMessageFileItem vfi, TextView view, ImageView actionButton) {
		String strState = "";
		boolean showProgressLayout = false;
		if (vfi.getVm().getMsgCode() == V2GlobalConstants.GROUP_TYPE_CROWD)
			actionButton.setVisibility(View.GONE);
		else
			actionButton.setVisibility(View.VISIBLE);
		if (vfi.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING) {
			strState = getContext().getResources().getText(R.string.contact_message_file_item_downloading).toString();
			actionButton.setImageResource(R.drawable.message_file_pause_button);
			showProgressLayout = true;
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_SENDING) {
			// 区分上传与发送
			if (MessageBodyType.GROUP_TYPE == bodyType)
				strState = getContext().getResources().getText(R.string.contact_message_file_item_uploading).toString();
			else
				strState = getContext().getResources().getText(R.string.contact_message_file_item_sending).toString();
			actionButton.setImageResource(R.drawable.message_file_pause_button);
			showProgressLayout = true;
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING
				|| vfi.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING) {
			strState = getContext().getResources().getText(R.string.contact_message_file_item_pause).toString();
			if (vfi.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING) {
				actionButton.setImageResource(R.drawable.message_file_upload_button);
			} else {
				actionButton.setImageResource(R.drawable.message_file_download_button);
			}
			showProgressLayout = true;
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_SENT_FALIED) {
			// 区分上传与发送
			if (MessageBodyType.GROUP_TYPE == bodyType)
				strState = getContext().getResources().getText(R.string.contact_message_file_item_upload_failed)
						.toString();
			else
				strState = getContext().getResources().getText(R.string.contact_message_file_item_sent_failed)
						.toString();
			// Show failed icon
			actionButton.setVisibility(View.GONE);
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED) {
			// 区分上传与发送
			if (MessageBodyType.GROUP_TYPE == bodyType)
				strState = getContext().getResources().getText(R.string.contact_message_file_item_uploaded).toString();
			else {
				strState = getContext().getResources().getText(R.string.contact_message_file_item_download_failed)
						.toString();
				// Show failed icon
				actionButton.setVisibility(View.GONE);
			}
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_MISS_DOWNLOAD) {
			strState = getContext().getResources().getText(R.string.contact_message_file_item_miss_download).toString();
			actionButton.setVisibility(View.GONE);
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_UNDOWNLOAD) {
			strState = getContext().getResources().getText(R.string.contact_message_file_item_miss_download).toString();
			actionButton.setVisibility(View.GONE);
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_SENT) {
			// 区分上传与发送
			if (MessageBodyType.GROUP_TYPE == bodyType)
				strState = getContext().getResources().getText(R.string.contact_message_file_item_uploaded).toString();
			else
				strState = getContext().getResources().getText(R.string.contact_message_file_item_sent).toString();
			actionButton.setVisibility(View.GONE);
		} else if (vfi.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED) {
			strState = getContext().getResources().getText(R.string.contact_message_file_item_downloaded).toString();
			actionButton.setVisibility(View.GONE);
		}

		view.setText(strState);
		return showProgressLayout;
	}

	private OnLongClickListener messageLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View anchor) {
			CommonCallBack.getInstance().executeUpdatePopupWindowState(MessageBodyView.this);
			showPopupWindow(mContentContainer);
			return true;
		}
	};

	private int rawX;
	private int rawY;
	private OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				rawX = (int) event.getRawX();
				rawY = (int) event.getRawY();
			}
			return false;
		}
	};

	public void updateAvatar(Bitmap bmp) {
		mHeadIcon.setImageBitmap(bmp);
	}

	private OnClickListener fileMessageItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (AlgorithmUtil.isFastClick())
				return;

			VMessageFileItem item = (VMessageFileItem) view.getTag();

			if (callback != null) {
				if (mMsg.getMsgCode() == V2GlobalConstants.GROUP_TYPE_CROWD) {
					if (item.getState() == VMessageAbstractItem.STATE_FILE_SENDING
							|| item.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING
							|| item.getState() == VMessageAbstractItem.STATE_FILE_SENT_FALIED)
						callback.onCrowdFileMessageClicked(CrowdFileActivityType.CROWD_FILE_UPLOING_ACTIVITY);
					else
						callback.onCrowdFileMessageClicked(CrowdFileActivityType.CROWD_FILE_ACTIVITY);
				} else {
					if (item.getState() == VMessageFileItem.STATE_FILE_DOWNLOADED) {
						String fileName = item.getFileName();
						String postfixName = fileName.substring(fileName.indexOf("."));
						if (postfixName.equals(".gif")) {
							callback.onImageGifFileClick(item.getFilePath());
						} else {
							FileUtils.openFile(item.getFilePath());
						}
					} else {
						if (item.getState() == VMessageFileItem.STATE_FILE_UNDOWNLOAD) {
							long key;
							if (mMsg.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER)
								key = mMsg.getToUser().getmUserId();
							else
								key = mMsg.getGroupId();
							boolean flag = GlobalHolder.getInstance().changeGlobleTransFileMember(
									V2GlobalConstants.FILE_TRANS_DOWNLOADING, getContext(), true, key,
									"MessageBodyView fileMessageItemClickListener");
							if (!flag)
								return;

							item.setState(VMessageFileItem.STATE_FILE_DOWNLOADING);
							ChatMessageProvider.updateFileItemState(getContext(), item);
							updateView(item);
							callback.requestDownloadFile(view, item.getVm(), item);
							return;
						} else if (item.getState() == VMessageFileItem.STATE_FILE_SENDING) {
							callback.requestPauseTransFile(view, item.getVm(), item);
							item.setState(VMessageFileItem.STATE_FILE_PAUSED_SENDING);
						} else if (item.getState() == VMessageFileItem.STATE_FILE_PAUSED_SENDING) {
							callback.requestResumeTransFile(view, item.getVm(), item);
							item.setState(VMessageFileItem.STATE_FILE_SENDING);
						} else if (item.getState() == VMessageFileItem.STATE_FILE_DOWNLOADING) {
							callback.requestPauseDownloadFile(view, item.getVm(), item);
							item.setState(VMessageFileItem.STATE_FILE_PAUSED_DOWNLOADING);
						} else if (item.getState() == VMessageFileItem.STATE_FILE_PAUSED_DOWNLOADING) {
							callback.requestResumeDownloadFile(view, item.getVm(), item);
							item.setState(VMessageFileItem.STATE_FILE_DOWNLOADING);
						}
						ChatMessageProvider.updateFileItemState(getContext(), item);
						updateView(item);
					}
				}
			}
		}

	};

	private OnClickListener mAudioMssageClickListener = new OnClickListener() {

		@Override
		public void onClick(View anchor) {
			if (callback != null) {
				List<VMessageAudioItem> al = mMsg.getAudioItems();
				if (al != null && al.size() > 0) {
					VMessageAudioItem audioItem = al.get(0);
					if (audioItem.isPlaying()) {
						callback.requestStopAudio(mMsg, audioItem);
					} else {
						updateAudioReadState(false, audioItem);
						callback.requestPlayAudio(MessageBodyView.this, mMsg, audioItem);
						audioItem.setPlaying(true);
					}
				}
			}
		}
	};

	private OnClickListener mDeleteButtonListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (callback != null) {
				callback.requestDelMessage(mMsg);
			}
			pw.dismiss();
		}

	};

	private OnClickListener mCopyButtonListener = new OnClickListener() {
		@Override
		public void onClick(View view) {

			ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("label", MessageUtil.getMixedConversationCopyedContent(mMsg));

			clipboard.setPrimaryClip(clip);
			pw.dismiss();
		}

	};

	private OnClickListener mResendButtonListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (LocalSharedPreferencesStorage.checkCurrentAviNetwork(getContext())) {
				if (callback != null) {
					View fileRootView = mContentContainer.getChildAt(0);
					if (mMsg.isLocal()) {
						if (mMsg.getItems().size() > 0
								&& mMsg.getItems().get(0).getType() == VMessageFileItem.ITEM_TYPE_FILE) {
							long key;
							if (mMsg.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER)
								key = mMsg.getToUser().getmUserId();
							else
								key = mMsg.getGroupId();
							boolean flag = GlobalHolder.getInstance().changeGlobleTransFileMember(
									V2GlobalConstants.FILE_TRANS_SENDING, getContext(), true, key,
									"MessageBodyView mResendButtonListener");
							if (!flag)
								return;
						}

						failedIcon.setVisibility(View.GONE);
						callback.reSendMessageClicked(mMsg);

						if (mMsg.getItems().size() > 0
								&& mMsg.getItems().get(0).getType() == VMessageFileItem.ITEM_TYPE_FILE) {
							VMessageFileItem fileItem = (VMessageFileItem) mMsg.getItems().get(0);
							lastUpdateTime = 0;
							fileItem.setDownloadedSize(0);
							updateFileItemView(fileItem, fileRootView);
						}
					} else {
						if (mMsg.getItems().size() > 0
								&& mMsg.getItems().get(0).getType() == VMessageFileItem.ITEM_TYPE_FILE) {
							failedIcon.setVisibility(View.GONE);
							mMsg.setState(VMessageAbstractItem.TRANS_TRANSING);
							VMessageFileItem fileItem = (VMessageFileItem) mMsg.getItems().get(0);
							callback.requestDownloadFile(fileRootView, mMsg, fileItem);
							fileItem.setState(VMessageAbstractItem.STATE_FILE_DOWNLOADING);
							fileItem.setDownloadedSize(0);
							updateFailedFlag(false);
							updateFileItemView(fileItem, fileRootView);
							ChatMessageProvider.updateFileItemState(getContext(), fileItem);
							ChatMessageProvider.updateChatMessageState(getContext(), mMsg);
						}
					}
				}
			} else {
				Toast.makeText(getContext(), R.string.conversation_message_bodyView_network_connection_not_availabl,
						Toast.LENGTH_SHORT).show();
			}
			pw.dismiss();
		}

	};

	public interface ClickListener {
		void onLinkMessageClicked(VMessageLinkTextItem link);

		void onImageItemClick(VMessage v, VMessageImageItem imageItem);

		void onImageGifFileClick(String filePath);

		void onCrowdFileMessageClicked(CrowdFileActivityType openType);

		void reSendMessageClicked(VMessage v);

		void requestDelMessage(VMessage v);

		void requestPlayAudio(MessageBodyView view, VMessage vm, VMessageAudioItem vai);

		void requestStopAudio(VMessage vm, VMessageAudioItem vai);

		void requestDownloadFile(View v, VMessage vm, VMessageFileItem vfi);

		void requestPauseDownloadFile(View v, VMessage vm, VMessageFileItem vfi);

		void requestResumeDownloadFile(View v, VMessage vm, VMessageFileItem vfi);

		void requestPauseTransFile(View v, VMessage vm, VMessageFileItem vfi);

		void requestResumeTransFile(View v, VMessage vm, VMessageFileItem vfi);

		void requestFlushImage(TextView mMsgBodyTV, VMessageImageItem imageItem, Bitmap result,
				ChatTextViewClick imageItemCallBack);
	}
}

enum MessageBodyType {

	SINGLE_USER_TYPE(0), GROUP_TYPE(1), UNKNOWN(2);
	private int type;

	MessageBodyType(int type) {
		this.type = type;
	}

	public static MessageBodyType fromInt(int code) {
		switch (code) {
		case 0:
			return SINGLE_USER_TYPE;
		case 1:
			return GROUP_TYPE;
		default:
			return UNKNOWN;

		}
	}

	public int intValue() {
		return type;
	}
}
