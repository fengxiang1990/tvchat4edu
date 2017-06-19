package com.bizcom.vc.activity.conference;

import com.bizcom.util.DateUtil;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.MessageUtil.ChatTextViewClick;
import com.bizcom.vc.widget.cus.span.ClickSpanMovementMethod;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageLinkTextItem;
import com.config.GlobalConfig;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConferenceMessageBodyView extends LinearLayout {

	private View rootView;
	private TextView senderTV;
	private LinearLayout mMsgBodyContainer;
	private TextView mMsgBodyTV;

	private VMessage mVMessage;
	private ConferenceGroup conf;
	private Context mContext;

	public interface ClickListener {
		public void onMessageClicked(VMessage v);
	}

	public interface ActionListener {
		public void NotChangeTaskToBack();
	}

	public ConferenceMessageBodyView(Context context, VMessage m) {
		super(context);
		if (m == null)
			return;
		this.mVMessage = m;
		this.mContext = context;
		initLayout();
		setViewContent();
	}
	
	private void initLayout() {
		rootView = LayoutInflater.from(this.mContext).inflate(
				R.layout.conference_message_body, null, false);
		senderTV = (TextView) rootView
				.findViewById(R.id.conference_message_sender);
		senderTV.setTextColor(Color.BLUE);
		mMsgBodyContainer = (LinearLayout) rootView
				.findViewById(R.id.conference_message_body_ly);
		mMsgBodyTV = new TextView(this.getContext());
		mMsgBodyTV.setBackgroundColor(Color.TRANSPARENT);
		mMsgBodyTV.setTextColor(Color.BLACK);
		mMsgBodyTV.setSelected(false);
		mMsgBodyTV.setClickable(true);
		LinearLayout.LayoutParams ll1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mMsgBodyContainer.addView(mMsgBodyTV, ll1);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addView(rootView, ll);
	}

	private void setViewContent() {
		mMsgBodyContainer.setTag(this.mVMessage);
		mMsgBodyTV.setText("");
		if (!GlobalConfig.PROGRAM_IS_PAD
				&& this.mVMessage.getFromUser().getDisplayName().length() > 15) {
			senderTV.setText(this.mVMessage.getFromUser().getDisplayName()
					.subSequence(0, 15)
					+ mContext.getString(R.string.conversation_ellipsis)
					+ "  "
					+ DateUtil.getStringDate(mVMessage.getDate().getTime()));
		} else {
			senderTV.setText(this.mVMessage.getFromUser().getDisplayName()
					+ "  "
					+ DateUtil.getStringDate(mVMessage.getDate().getTime()));
		}
		MessageUtil.buildChatTextView(getContext(), mMsgBodyTV,
				mVMessage, new ChatTextViewClick() {

					@Override
					public void imageItemClick(VMessageImageItem imageItem) {
						// notify conferenceActivity chanage flag isMoveToBack
						Intent notify = new Intent();
						notify.addCategory(PublicIntent.DEFAULT_CATEGORY);
						notify.setAction(PublicIntent.NOTIFY_CONFERENCE_ACTIVITY);
						mContext.sendBroadcast(notify);

						Intent i = new Intent();
						i.addCategory(PublicIntent.DEFAULT_CATEGORY);
						i.setAction(PublicIntent.START_VIDEO_IMAGE_GALLERY);
						if (imageItem != null)
							i.putExtra("imageID", imageItem.getUuid());
						// type 0: is not group image view
						// type 1: group image view
						i.putExtra("cid", mVMessage.getId());
						i.putExtra("type", conf.getGroupType());
						i.putExtra("gid", conf.getGroupID());
						mContext.startActivity(i);
					}

					@Override
					public void LinkItemClick(VMessageLinkTextItem link) {
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						Uri content_url = Uri.parse("http://" + link.getUrl());
						intent.setData(content_url);
						mContext.startActivity(intent);
						
						((ConferenceActivity)mContext).isMoveTaskBack = false;
					}

					@Override
					public void imageFrush(TextView mMsgBodyTV,
							VMessageImageItem imageItem, Bitmap result,
							ChatTextViewClick imageItemCallBack) {
						setViewContent();
					}
				});
		mMsgBodyTV.setMovementMethod(ClickSpanMovementMethod.getInstance());
	}

	public void updateView(VMessage vm) {
		if (vm == null) {
			V2Log.e("Can't not update data vm is null");
			return;
		}

		boolean isAddView = false;
		if (mMsgBodyContainer != null) {
			mMsgBodyContainer.removeAllViews();
			isAddView = true;
		}
		this.mVMessage = vm;
		setViewContent();

		if (isAddView) {
			LinearLayout.LayoutParams ll1 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			mMsgBodyContainer.addView(mMsgBodyTV, ll1);
		}
	}

	public VMessage getVMessage() {
		return this.mVMessage;
	}

	public void recycle() {
		mVMessage.recycleAllImageMessage();
	}

	public void setConf(ConferenceGroup conf) {
		this.conf = conf;
	}
}
