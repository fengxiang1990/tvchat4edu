package com.bizcom.vc.activity.conference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.util.MessageUtil;
import com.bizcom.vc.adapter.LeftInvitionAttendeeAdapter;
import com.bizcom.vc.widget.cus.MultilevelListView;
import com.bizcom.vc.widget.cus.MultilevelListView.ItemData;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.Conference;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public class LeftInvitionAttendeeLayout extends LinearLayout {

	private static final int UPDATE_ATTENDEES = 2;
	private static final int START_GROUP_SELECT = 6;
	private static final int DOING_SELECT_GROUP = 7;
	private static final int END_GROUP_SELECT = 8;

	private static final int PAD_LAYOUT = 1;
	private static final int PHONE_LAYOUT = 0;

	private Context mContext;
	private LocalHandler mLocalHandler = new LocalHandler();

	private ClearEditText searchedTextET;
	// private ListView mContactsContainer;
	private MultilevelListView mGroupListView;
	private EditText mConfTitleET;
	private EditText mConfStartTimeET;
	private View mInvitionButton;
	private Drawable mIconSearchClear;

	private AdapterView<ListAdapter> mAttendeeContainer;
	private LeftInvitionAttendeeAdapter mAdapter;

	private List<Group> mGroupList;

	// Used to save current selected user
	private Set<User> mAttendeeList = new HashSet<User>();
	private List<User> mUserListArray = new ArrayList<User>();

	private Conference conf;

	private int landLayout = PAD_LAYOUT;

	private Listener listener;

	private ProgressDialog mWaitingDialog;

	public interface Listener {
		public void requestInvitation(Conference conf, List<User> attendUsers, boolean isNotify);
	}

	public LeftInvitionAttendeeLayout(Context context, Conference conf) {
		super(context);
		this.conf = conf;
		mGroupList = new ArrayList<Group>();
		initLayout();
	}

	@SuppressWarnings("unchecked")
	private void initLayout() {
		mContext = getContext();
		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_invition_attendee_layout, null, false);

		mGroupListView = (MultilevelListView) view.findViewById(R.id.conf_create_contacts_list);
		mGroupListView.setMultilevelType(MultilevelListView.MULTILEVEL_TYPE_CONF);
		mGroupListView.setShowedCheckedBox(true);
		mGroupListView.setTextFilterEnabled(true);
		mGroupListView.setListener(mListener);
		mGroupListView.setIgnoreCurrentUser(true);

		mAttendeeContainer = (AdapterView<ListAdapter>) view.findViewById(R.id.conference_attendee_container);
		mAttendeeContainer.setOnItemClickListener(mItemClickedListener);
		landLayout = mAttendeeContainer.getTag().equals("vertical") ? PAD_LAYOUT : PHONE_LAYOUT;
		mAdapter = new LeftInvitionAttendeeAdapter(mContext, mUserListArray, landLayout);
		mAttendeeContainer.setAdapter(mAdapter);

		mConfTitleET = (EditText) view.findViewById(R.id.conference_create_conf_name);
		mConfTitleET.setEnabled(false);
		mConfStartTimeET = (EditText) view.findViewById(R.id.conference_create_conf_start_time);
		mConfStartTimeET.setEnabled(false);
		mConfStartTimeET.setEllipsize(TruncateAt.END);

		searchedTextET = (ClearEditText) view.findViewById(R.id.ws_common_create_search);
		searchedTextET.addTextChangedListener(textChangedListener);

		mInvitionButton = view.findViewById(R.id.video_invition_attendee_ly_invition_button);
		mInvitionButton.setOnClickListener(confirmButtonListener);

		final Resources res = getResources();
		mIconSearchClear = res.getDrawable(R.drawable.txt_search_clear);
		searchedTextET.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		searchedTextET.addTextChangedListener(tbxSearch_TextChanged);
		searchedTextET.setOnTouchListener(txtSearch_OnTouch);
		this.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		initData();

		new LoadContactsAT().execute();
	}

	private OnTouchListener txtSearch_OnTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				int curX = (int) event.getX();
				if (curX > v.getWidth() - 38 && !TextUtils.isEmpty(searchedTextET.getText())) {
					searchedTextET.setText("");
					int cacheInputType = searchedTextET.getInputType();// backup
					searchedTextET.setInputType(InputType.TYPE_NULL);// disable
					searchedTextET.onTouchEvent(event);// call native handler
					searchedTextET.setInputType(cacheInputType);// restore input
					return true;// consume touch even
				}
				break;
			}
			return false;
		}
	};
	private TextWatcher tbxSearch_TextChanged = new TextWatcher() {
		private boolean isnull = true;

		@Override
		public void afterTextChanged(Editable s) {
			if (TextUtils.isEmpty(s)) {
				if (!isnull) {
					searchedTextET.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					isnull = true;
				}
			} else {
				if (isnull) {
					searchedTextET.setCompoundDrawablesWithIntrinsicBounds(null, null, mIconSearchClear, null);
					isnull = false;
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	public void setListener(Listener l) {
		this.listener = l;
	}

	public boolean isScreenLarge() {
		final int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		return screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	public void hideKeyBoard() {
		MessageUtil.hideKeyBoard(getContext(), searchedTextET.getWindowToken());
	}

	public void updateMultilevelListView(GroupUserObject obj, boolean isAdd) {
		if (isAdd) {
			mGroupListView.addUser(GlobalHolder.getInstance().getGroupById(obj.getmType(), obj.getmGroupId()),
					GlobalHolder.getInstance().getUser(obj.getmUserId()));
		} else {
			mGroupListView.removeItem(GlobalHolder.getInstance().getUser(obj.getmUserId()));
		}
	}

	private void initData() {
		mConfTitleET.setText(conf.getName());
		mConfStartTimeET.setText(conf.getStartTimeStr());
	}

	private void updateUserToAttendList(final User u) {
		if (u == null) {
			return;
		}
		boolean remove = false;
		for (User tu : mAttendeeList) {
			if (tu.getmUserId() == u.getmUserId()) {
				mAttendeeList.remove(tu);
				remove = true;
				break;
			}
		}

		if (remove) {
			removeAttendee(u);
		} else {
			addAttendee(u);
		}

	}

	private void removeAttendee(User u) {
		mAttendeeList.remove(u);
		mUserListArray.remove(u);
		mAdapter.notifyDataSetChanged();
	}

	private void addAttendee(User u) {
		if (u.isCurrentLoggedInUser()) {
			return;
		}
		boolean ret = mAttendeeList.add(u);
		if (!ret) {
			return;
		}

		mUserListArray.add(u);
		mAdapter.notifyDataSetChanged();
	}

	private void selectGroup(Group selectGroup, boolean addOrRemove) {
		List<Group> subGroups = selectGroup.getChildGroup();
		for (int i = 0; i < subGroups.size(); i++) {
			selectGroup(subGroups.get(i), addOrRemove);
		}
		List<User> list = selectGroup.getUsers();
		for (int i = 0; i < list.size(); i++) {
			if (addOrRemove) {
				addAttendee(list.get(i));
			} else {
				removeAttendee(list.get(i));
			}
		}
	}
	
	public void cleanAllResource(){
		if(mGroupList != null){
			mGroupList.clear();
			mGroupList = null;
		}
		
		if(mAttendeeList != null){
			mAttendeeList.clear();
			mAttendeeList = null;
		}
		
		if(mUserListArray != null){
			mUserListArray.clear();
			mUserListArray = null;
		}
		
		mWaitingDialog = null;
		mGroupListView = null;
		conf = null;
	}

	private TextWatcher textChangedListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (TextUtils.isEmpty(s.toString())) {
				mGroupListView.clearTextFilter();
			} else {
				mGroupListView.setFilterText(s.toString());
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

	};

	private MultilevelListView.MultilevelListViewListener mListener = new MultilevelListView.MultilevelListViewListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, ItemData item) {
			return false;
		}

		@Override
		public void onItemClicked(AdapterView<?> parent, View view, int position, long id, ItemData item) {
			Object obj = item.getObject();
			if (obj instanceof User) {
				Message.obtain(mLocalHandler, UPDATE_ATTENDEES, (User) obj).sendToTarget();
				mGroupListView.updateCheckItem((User) obj, !item.isChecked());
			}

		}

		public void onCheckboxClicked(View view, ItemData item) {
			CheckBox cb = (CheckBox) view;
			Object obj = item.getObject();
			if (obj instanceof User) {
				User user = (User) obj;
				Message.obtain(mLocalHandler, UPDATE_ATTENDEES, user).sendToTarget();
				mGroupListView.updateCheckItem(user, !item.isChecked());

				Set<Group> belongsGroup = user.getBelongsGroup();
				for (Group group : belongsGroup) {
					List<User> users = group.getUsers();
					mGroupListView.checkBelongGroupAllChecked(group, users);
				}
			} else {
				Message.obtain(mLocalHandler, START_GROUP_SELECT, cb.isChecked() ? 1 : 2, 0, (Group) obj)
						.sendToTarget();
				mGroupListView.updateCheckItem((Group) obj, !item.isChecked());
			}
		}
	};

	private OnClickListener confirmButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			List<User> attends = new ArrayList<User>(mAttendeeList);
			if (listener != null) {
				listener.requestInvitation(conf, attends, true);
			}

			if (mAttendeeContainer.getChildCount() > 0) {
				mAttendeeContainer.removeAllViewsInLayout();
				mAdapter.notifyDataSetChanged();
			}

			for (int i = 0; i < mGroupListView.getGroupList().size(); i++) {
				mGroupListView.updateAllGroupItemCheck(mGroupListView.getGroupList().get(i));
			}

			mAttendeeList.clear();
			mUserListArray.clear();
		}
	};

	private OnItemClickListener mItemClickedListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			User user = mUserListArray.get(position);
			mGroupListView.updateCheckItem(user, false);
			Message.obtain(mLocalHandler, UPDATE_ATTENDEES, user).sendToTarget();
		}

	};

	class LoadContactsAT extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mGroupList.clear();
			mGroupList.addAll(GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT));
			mGroupList.addAll(GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DEPARTMENT));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mGroupListView.setGroupList(mGroupList);
		}

	};

	class LocalHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_ATTENDEES:
				updateUserToAttendList((User) msg.obj);
				break;
			case START_GROUP_SELECT: {
				mWaitingDialog = ProgressDialog.show(mContext, "",
						mContext.getResources().getString(R.string.notification_watiing_process), true);
				Message.obtain(this, DOING_SELECT_GROUP, msg.arg1, msg.arg2, msg.obj).sendToTarget();
				break;
			}
			case DOING_SELECT_GROUP:
				selectGroup((Group) msg.obj, msg.arg1 == 1 ? true : false);
				Message.obtain(this, END_GROUP_SELECT).sendToTarget();
				break;
			case END_GROUP_SELECT:
				mWaitingDialog.dismiss();
				mWaitingDialog = null;
				break;
			}
		}

	}

}
