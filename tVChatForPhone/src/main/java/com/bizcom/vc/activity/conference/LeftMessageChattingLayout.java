package com.bizcom.vc.activity.conference;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.vc.adapter.CommonAdapter;
import com.bizcom.vc.adapter.CommonAdapter.CommonAdapterItemDateAndViewWrapper;
import com.bizcom.vc.adapter.VMessageDataAndViewWrapper;
import com.bizcom.vc.widget.cus.edittext.PasteEditText;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.meesage.VMessage;
import com.shdx.tvchat.phone.R;

public class LeftMessageChattingLayout extends LinearLayout {

	private ConferenceGroup conf;
	private ChattingListener listener;
	private List<CommonAdapterItemDateAndViewWrapper> ItemDataAndViewWrapperList;
	private CommonAdapter adapter;
	private Context mContext;

	private View rootView;
	private ListView mMsgListView;
	private View mSendButton;
	private PasteEditText mInputMessageET;
	private View mPinButton;
	private OnClickListener mPinButtonOnClickListener = new PinButtonOnClickListener();
	private CommonAdapter.CommonAdapterGetViewListener mListViewAdapterGetViewListener = new ListViewAdapterGetViewListener();
	private SendButtonOnClickListener mSendButtonOnClickListener = new SendButtonOnClickListener();

	public interface ChattingListener {
		public void requestSendMsg(VMessage vm);

		public void requestChattingViewFixedLayout(View v);

		public void requestChattingViewFloatLayout(View v);
	};

	public LeftMessageChattingLayout(Context context, ConferenceGroup conf) {
		super(context);
		initLayout();
		initData();
		this.conf = conf;
		mContext = context;
	}

	private void initLayout() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.video_msg_chatting_layout, null, false);

		mPinButton = view.findViewById(R.id.video_msg_chatting_pin_button);
		mPinButton.setOnClickListener(mPinButtonOnClickListener);

		this.addView(view, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));

		this.mMsgListView = (ListView) view
				.findViewById(R.id.video_msg_container);
		this.mInputMessageET = (PasteEditText) view
				.findViewById(R.id.video_msg_chatting_layout_msg_content);
		this.mSendButton = view
				.findViewById(R.id.video_msg_chatting_layout_send_button);
		this.mSendButton.setOnClickListener(mSendButtonOnClickListener);

		rootView = this;
	}

	private void initData() {
		ItemDataAndViewWrapperList = new ArrayList<CommonAdapterItemDateAndViewWrapper>();
		adapter = new CommonAdapter(ItemDataAndViewWrapperList,
				mListViewAdapterGetViewListener);
		mMsgListView.setAdapter(adapter);
	}

	public void setListener(ChattingListener listener) {
		this.listener = listener;
	}

	public void requestScrollToNewMessage() {
		if (ItemDataAndViewWrapperList.size() <= 0) {
			return;
		}
		mMsgListView.setSelection(ItemDataAndViewWrapperList.size() - 1);
	}

	public void addNewMessage(VMessage vm) {
		ItemDataAndViewWrapperList.add(new VMessageDataAndViewWrapper(vm));
		adapter.notifyDataSetChanged();
		requestScrollToNewMessage();
	}

	/**
	 * Used to manually request FloatLayout, Because when this layout will hide,
	 * call this function to inform interface
	 */
	public void requestFloatLayout() {
		if ("float".equals(mPinButton.getTag())) {
			return;
		}
		if (this.listener != null) {
			this.listener.requestChattingViewFloatLayout(rootView);
		}

		mPinButton.setTag("float");
		((ImageView) mPinButton)
				.setImageResource(R.drawable.pin_button_selector);
	}

	public boolean getWindowSizeState() {
		String str = (String) mPinButton.getTag();
		if (str == null || str.equals("float")) {
			return false;
		} else {
			return true;
		}
	}
	
	public void hideKeyBoard(){
		MessageUtil.hideKeyBoard(getContext(), mInputMessageET.getWindowToken());
	}
	
	private class SendButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			if (listener != null) {
				if (mInputMessageET.getText() == null
						|| mInputMessageET.getText().toString().trim()
								.isEmpty()) {
					return;
				}
				VMessage vm = MessageUtil.buildChatMessage(mContext,
						String.valueOf(mInputMessageET.getText()), conf.getGroupType(),
						conf.getGroupID(), null);
				addNewMessage(vm);
				listener.requestSendMsg(vm);
			}
		}
	}

	private class PinButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {

			if (view.getTag().equals("float")) {
				if (listener != null) {
					listener.requestChattingViewFixedLayout(rootView);
				}
			} else {
				if (listener != null) {
					listener.requestChattingViewFloatLayout(rootView);
				}
			}

			if (view.getTag().equals("float")) {
				view.setTag("fix");
				((ImageView) view)
						.setImageResource(R.drawable.pin_fixed_button_selector);
			} else {
				view.setTag("float");
				((ImageView) view)
						.setImageResource(R.drawable.pin_button_selector);
			}
		}

	};

	private class ListViewAdapterGetViewListener implements
			CommonAdapter.CommonAdapterGetViewListener {

		@Override
		public View getView(CommonAdapterItemDateAndViewWrapper wr,
				View convertView, ViewGroup vg) {

			VMessage vm = (VMessage) wr.getItemObject();
			if (convertView == null) {
				ConferenceMessageBodyView mv = new ConferenceMessageBodyView(
						getContext(), vm);
				mv.setConf(conf);
				convertView = mv;
			} else {
				((ConferenceMessageBodyView) convertView).updateView(vm);
			}
			return convertView;
		}
	};

	public void notifyReplaceImage(VMessage vm) {
		
		V2Log.d("binaryReplace", " 接收到替换沙漏的回调 , uuid is : " + vm.getUUID());
		if(ItemDataAndViewWrapperList == null)
			return ;
		
		for (int i = 0; i < ItemDataAndViewWrapperList.size(); i++) {
			VMessageDataAndViewWrapper wrapper = (VMessageDataAndViewWrapper) ItemDataAndViewWrapperList
					.get(i);
			VMessage replaced = (VMessage) wrapper.getItemObject();
			if (replaced.getUUID().equals(vm.getUUID())) {
				V2Log.e("binaryReplace", "LeftMessageChattingLayout -- "
						+ "Recevice Binary data from server , and replaced wait! id is : " + vm.getmXmlDatas());
				replaced.setImageItems(vm.getImageItems());
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
				break;
			}
		}
	}
}
