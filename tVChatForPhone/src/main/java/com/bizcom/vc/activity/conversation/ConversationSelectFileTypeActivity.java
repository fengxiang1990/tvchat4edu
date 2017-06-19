package com.bizcom.vc.activity.conversation;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.service.JNIService;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vo.FileInfoBean;
import com.bizcom.vo.User;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ConversationSelectFileTypeActivity extends BaseActivity implements OnClickListener {

	public static final int CANCEL = 0;
	public static final int NORMAL_SELECT_FILE = 1;
	public static final int SEND_SELECT_FILE = 3;

	private RelativeLayout entryImage;
	private RelativeLayout entryFile;
	private TextView backKey;
	private ArrayList<FileInfoBean> mCheckedList;

	private TextView selectedFileSize;
	private TextView sendButton;
	private long totalSize;
	private int lastSize; // 通过该变量来判断是否要累加totalSize
	private long uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_selectfile_entry);
		super.setNeedAvatar(false);
		super.setNeedBroadcast(true);
		super.setNeedHandler(false);
		super.onCreate(savedInstanceState);

		findview();
		mCheckedList = new ArrayList<FileInfoBean>();
		uid = getIntent().getLongExtra("uid", -1);
	}

	@Override
	public void addBroadcast(IntentFilter filter) {
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		String action = intent.getAction();
		if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(action)) {
			GroupUserObject obj = intent.getParcelableExtra("obj");
			if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				if (obj.getmUserId() == uid) {
					finish();
				}
			}
		}
	}

	@Override
	public void receiveMessage(Message msg) {

	}

	@Override
	public void initViewAndListener() {

	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(this, ConversationSelectFileActivity.class);
		intent.putParcelableArrayListExtra("checkedFiles", mCheckedList);
		switch (v.getId()) {
		case R.id.selectfile_entry_image:
			intent.putExtra("type", "image");
			intent.putExtra("uid", uid);
			startActivityForResult(intent, NORMAL_SELECT_FILE);
			break;
		case R.id.selectfile_entry_file:
			intent.putExtra("type", "file");
			intent.putExtra("uid", uid);
			startActivityForResult(intent, NORMAL_SELECT_FILE);
			break;
		case R.id.ws_common_activity_title_left_button:
			setResult(1000);
			mCheckedList.clear();
			onBackPressed();
			break;
		case R.id.selectfile_message_send:
			Intent sendIntent = new Intent();
			sendIntent.putParcelableArrayListExtra("checkedFiles", mCheckedList);
			setResult(1000, sendIntent);
			onBackPressed();
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case NORMAL_SELECT_FILE:
			if(data == null){
				return ;
			}
			mCheckedList = data.getParcelableArrayListExtra("checkedFiles");
			if (mCheckedList != null && mCheckedList.size() > 0 && mCheckedList.size() != lastSize) {

				totalSize = 0;
				lastSize = mCheckedList.size();
				sendButton.setClickable(true);
				sendButton.setOnClickListener(this);
				sendButton.setTextColor(Color.WHITE);
				sendButton.setBackgroundResource(R.drawable.conversation_selectfile_send_able);
				for (FileInfoBean bean : mCheckedList) {

					totalSize += bean.fileSize;
				}
				sendButton.setText(String.format(getResources().getString(R.string.conversation_select_file_send),
						mCheckedList.size()));
				selectedFileSize.setText(getResources().getString(R.string.conversation_select_file_entry_chosen)
						+ getFileSize(totalSize));
				sendButton.setText(String.format(getResources().getString(R.string.conversation_select_file_entry_send),
						mCheckedList.size()));

			} else if (mCheckedList != null && mCheckedList.size() == 0) {
				lastSize = 0;
				sendButton.setClickable(false);
				sendButton.setOnClickListener(this);
				sendButton.setTextColor(Color.GRAY);
				sendButton.setBackgroundResource(R.drawable.button_bg_noable);
				selectedFileSize.setText(R.string.conversation_select_file_entry_selected);
				sendButton.setText(R.string.conversation_select_file_entry_send_out);
			}
			break;
		case CANCEL:
			setResult(1000);
			onBackPressed();
			break;
		case SEND_SELECT_FILE:
			mCheckedList = data.getParcelableArrayListExtra("checkedFiles");
			Intent sendIntent = new Intent();
			sendIntent.putParcelableArrayListExtra("checkedFiles", mCheckedList);
			setResult(1000, sendIntent);
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private void findview() {
		backKey = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        backKey.setBackgroundResource(R.drawable.title_bar_back_button_selector);
        TextView mTitleContentTV = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContentTV.setText(R.string.tab_selectfile_name);

		entryImage = (RelativeLayout) findViewById(R.id.selectfile_entry_image);
		entryFile = (RelativeLayout) findViewById(R.id.selectfile_entry_file);

		selectedFileSize = (TextView) findViewById(R.id.selectfile_entry_size);
		sendButton = (TextView) findViewById(R.id.selectfile_message_send);
		sendButton.setBackgroundResource(R.drawable.button_bg_noable);
		entryImage.setOnClickListener(this);
		entryFile.setOnClickListener(this);
		backKey.setOnClickListener(this);

	}

	/**
	 * 获取文件大小
	 * 
	 * @param totalSpace
	 * @return
	 */
	private static String getFileSize(long totalSpace) {

		BigDecimal filesize = new BigDecimal(totalSpace);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
		if (returnValue > 1)
			return (returnValue + "MB");
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
		return (returnValue + "  KB ");
	}
}
