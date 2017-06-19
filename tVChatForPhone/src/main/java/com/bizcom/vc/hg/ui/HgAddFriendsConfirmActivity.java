package com.bizcom.vc.hg.ui;

import java.util.List;

import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.util.CorlorUtil;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vo.FriendGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HgAddFriendsConfirmActivity extends Activity {
	private Context mContext;
	private EditText et_dlg;
	private TextView tv_message_dlg;
	private View bt_dlg_ok;
	private View bt_dlg_cancel;
	private V2ContactsRequest contactService = new V2ContactsRequest();
	private HeadLayoutManagerHG mHeadLayoutManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hg_dialog_add_friends);
		mContext=this;
		initView();
	}
	private void initView() {

		mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout) , false);
		mHeadLayoutManager.updateTitle(getIntent().getStringExtra("titleText"));
		final User u=GlobalHolder.getInstance().getExistUser(getIntent().getLongExtra("userId", -1));

		et_dlg=(EditText)findViewById(R.id.et_dlg);
		bt_dlg_cancel=findViewById(R.id.cancel);
		tv_message_dlg=(TextView)findViewById(R.id.tv_message_dlg);
		bt_dlg_ok=findViewById(R.id.ok);
		tv_message_dlg.setText(Html.fromHtml("确定将"+CorlorUtil.CreatColortext(u.getDisplayName())+"添加为好友？"));

		bt_dlg_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String forwordName = et_dlg.getText()==null?"":et_dlg.getText().toString();
				if(!TextUtils.isEmpty(forwordName))u.setNickName(forwordName);

				addConstants(u.getmUserId());

			}
		});
		bt_dlg_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				setResult(-1);
			}
		});
	}


	/**
	 * 发送添加好友请求
	 * 
	 * @param addUserId
	 *            被添加的用户id
	 */
	private void addConstants(long addUserId) {
		long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
		if (currentUserId != addUserId) {// 不是添加自己
			User detailUser = GlobalHolder.getInstance().getUser(addUserId);
			// 判断好友关系
			List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
			boolean isRelation=false;
			for (Group group : friendGroup) {
				if (group.findUser(detailUser) != null) {
					isRelation = true;
					break;
				}
			}
			if (!isRelation) {
				WaitDialogBuilder.showNormalWithHintProgress(mContext);
				List<Group> listFriendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
				contactService.addContact(new FriendGroup(listFriendGroup.get(0).getGroupID(), ""), detailUser, "",
						detailUser.getDisplayName(), new HandlerWrap(handler, 0, null));
			} else {
				Toast.makeText(mContext, R.string.contacts_detail2_friended, Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(mContext, R.string.contacts_detail2_friend_me, Toast.LENGTH_SHORT).show();
		}
	}

	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				doResult(msg);
				break;


			}
		}
	};
	protected void doResult(Message msg) {
		WaitDialogBuilder.dialog.dismiss();
		JNIResponse res = (JNIResponse) msg.obj;
		if (res.getResult() == JNIResponse.Result.SUCCESS) {

			Toast.makeText(mContext, R.string.contacts_detail2_added_successfully,
					Toast.LENGTH_LONG).show();
			setResult(1);
			finish();

		} else {
			Toast.makeText(mContext, R.string.contacts_detail2_added_failed, Toast.LENGTH_SHORT)
			.show();
		}

	}

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		hideSoft() ;
	}

	public void hideSoft() {

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()){
			imm.hideSoftInputFromWindow(et_dlg.getWindowToken(), 0);
		}
	}
}
