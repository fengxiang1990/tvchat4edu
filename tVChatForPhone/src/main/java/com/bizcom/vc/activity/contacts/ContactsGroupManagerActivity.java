package com.bizcom.vc.activity.contacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.jni.GroupServiceJNIResponse;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.adapter.CommonAdapter;
import com.bizcom.vc.adapter.CommonAdapter.CommonAdapterGetViewListener;
import com.bizcom.vc.adapter.CommonAdapter.CommonAdapterItemDateAndViewWrapper;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.ContactGroup;
import com.bizcom.vo.Group;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsGroupManagerActivity extends Activity {

	private static final int CREATE_GROUP_DONE = 1;
	private static final int UPDATE_GROUP_DONE = 2;
	private static final int REMOVE_GROUP_DONE = 3;

	private TextView mDialogTitleTV;
	private ClearEditText mGroupNameET;
	private Dialog mDialog;
	private Context mContext;
	private ListView mListView;

	private List<CommonAdapterItemDateAndViewWrapper> mDataset;
	private BaseAdapter adapter;

	private V2ContactsRequest contactService = new V2ContactsRequest();

	private boolean changed;
	private boolean inDeleteMode = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_contacts_group);
		mListView = (ListView) findViewById(R.id.contacts_group_listview);

		View finishButton = findViewById(R.id.contacts_group_title_button);
		finishButton.setOnClickListener(finishClickListener);

		View createGroupButton = findViewById(R.id.contacts_group_add_button);
		createGroupButton.setOnClickListener(createGroupButtonClickListener);

		List<Group> listGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
		mDataset = convert(listGroup);
		adapter = new CommonAdapter(mDataset, converter);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(mClickListener);
		mListView.setOnItemLongClickListener(mLongClickListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDataset != null) {
			mDataset.clear();
			mDataset = null;
		}
		mDialog = null;
		contactService.clearCalledBack();
	}

	@Override
	public void finish() {
		if (changed) {
			Intent i = new Intent(PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP);
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			mContext.sendBroadcast(i);
		}
		super.finish();
	}

	@Override
	public void onBackPressed() {
//		setResult(TabFragmentOrganization.ACTIVITY_RETURN_GROUP_CHANGE);
		super.onBackPressed();
	}

	private List<CommonAdapterItemDateAndViewWrapper> convert(List<Group> listGroup) {
		List<CommonAdapterItemDateAndViewWrapper> ds = new ArrayList<CommonAdapterItemDateAndViewWrapper>(
				listGroup.size() - 1);
		for (int i = 1; i < listGroup.size(); i++) {
			final Group g = listGroup.get(i);
			if (((ContactGroup) g).isDefault()) {
				continue;
			}
			ds.add(new LocalItemWrapper(g, false));
		}
		return ds;
	}

	private ContactGroup currentClickGroup = null;

	private void showDialog() {
		if (mDialog == null) {
			mDialog = new Dialog(this, R.style.ContactUserActionDialog);

			mDialog.setContentView(R.layout.activity_contacts_group_dialog);
			mDialogTitleTV = (TextView) mDialog.findViewById(R.id.contacts_group_title);
			mGroupNameET = (ClearEditText) mDialog.findViewById(R.id.contacts_group_name);
			mGroupNameET.setOnFocusChangeListener(textListener);
			final Button cancelB = (Button) mDialog.findViewById(R.id.contacts_group_cancel_button);
			cancelB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mGroupNameET.getWindowToken(), 0);
					mGroupNameET.setError(null);
					mDialog.dismiss();
				}

			});
			final Button confirmButton = (Button) mDialog.findViewById(R.id.contacts_group_confirm_button);
			confirmButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
						return;
					}

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mGroupNameET.getWindowToken(), 0);

					if (mGroupNameET.getText().toString().trim().isEmpty()) {
						mGroupNameET
								.setError(mContext.getText(R.string.activiy_contact_group_dialog_group_name_required));
						mGroupNameET.requestFocus();
						return;
					}

					String groupName = EscapedcharactersProcessing.convert(mGroupNameET.getText().toString());
					if (currentClickGroup == null) {
						ContactGroup newGroup = new ContactGroup(0, groupName);
						updateGroup(newGroup, OPT.CREATE);
					} else {
						currentClickGroup.setName(groupName);
						updateGroup(currentClickGroup, OPT.UPDATE);
					}
					WaitDialogBuilder.showNormalWithHintProgress(mContext);
					if (mDialog != null) {
						mDialog.dismiss();
					}
				}
			});
		}

		if (currentClickGroup != null) {
			mDialogTitleTV.setText(R.string.activiy_contact_group_dialog_title_update);
			mGroupNameET.setText(currentClickGroup.getName());
		} else {
			mGroupNameET.setText("");
			// mGroupNameET.setText(R.string.activiy_contact_group_name_content);
			mDialogTitleTV.setText(R.string.activiy_contact_group_dialog_title_create);
		}
		mDialog.show();
	}

	private void updateGroup(ContactGroup group, OPT opt) {
		if (opt == OPT.CREATE) {
			contactService.createGroup(group, new HandlerWrap(mLocalHandler, CREATE_GROUP_DONE, null));
		} else if (opt == OPT.UPDATE) {
			contactService.updateGroup(group, new HandlerWrap(mLocalHandler, UPDATE_GROUP_DONE, null));
		} else {
			contactService.removeGroup(group, new HandlerWrap(mLocalHandler, REMOVE_GROUP_DONE, null));
		}
	}

	private OnClickListener finishClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			onBackPressed();
		}
	};

	private OnClickListener createGroupButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			currentClickGroup = null;
			showDialog();
		}

	};

	private OnClickListener deleteGroupButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
				return;
			}
			WaitDialogBuilder.showNormalWithHintProgress(mContext);
			updateGroup((ContactGroup) view.getTag(), OPT.DELETE);
		}

	};

	private OnFocusChangeListener textListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View view, boolean flag) {
			if (flag && mGroupNameET.getText().equals(mContext.getText(R.string.activiy_contact_group_name_content))) {
				mGroupNameET.setText("");
			} else if (!flag) {
				mGroupNameET.setText(R.string.activiy_contact_group_name_content);
			}
		}

	};

	private OnItemClickListener mClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
			if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
				return;
			}

			currentClickGroup = (ContactGroup) mDataset.get(pos).getItemObject();
			showDialog();
		}

	};

	private OnItemLongClickListener mLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (!inDeleteMode) {
				inDeleteMode = true;
				adapter.notifyDataSetChanged();
				return true;
			}
			return true;
		}

	};

	private CommonAdapterGetViewListener converter = new CommonAdapterGetViewListener() {

		@Override
		public View getView(CommonAdapterItemDateAndViewWrapper wr, View view, ViewGroup vg) {
			LocalItemWrapper liw = (LocalItemWrapper) wr;
			if (view == null) {
				view = LayoutInflater.from(mContext).inflate(R.layout.activity_contacts_group_adapter_item, null,
						false);
			}
			TextView tv = (TextView) view.findViewById(R.id.contacts_group_item_name);
			tv.setText(((Group) liw.getItemObject()).getName());
			View v = view.findViewById(R.id.contacts_group_item_adapter_delelte_button);
			v.setTag(wr.getItemObject());
			View deleteModeView = view.findViewById(R.id.contacts_group_delete_icon);
			deleteModeView.setTag(liw);
			deleteModeView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					LocalItemWrapper liw = (LocalItemWrapper) v.getTag();
					if (liw.isShowDeleteButton())
						liw.setShowDeleteButton(false);
					else
						liw.setShowDeleteButton(true);
					adapter.notifyDataSetChanged();
				}

			});

			if (((ContactGroup) wr.getItemObject()).isDefault()) {
				v.setVisibility(View.INVISIBLE);
				deleteModeView.setVisibility(View.GONE);
			} else {
				v.setVisibility(View.VISIBLE);
				v.setOnClickListener(deleteGroupButtonClickListener);
				deleteModeView.setVisibility(View.VISIBLE);
			}

			if (inDeleteMode) {
				deleteModeView.setVisibility(View.VISIBLE);
				if (liw.showDeleteButton) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.INVISIBLE);
				}
			} else {
				deleteModeView.setVisibility(View.GONE);
				v.setVisibility(View.INVISIBLE);
			}

			return view;
		}

	};

	private Handler mLocalHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			JNIResponse res = (JNIResponse) msg.obj;
			WaitDialogBuilder.dismissDialog();
			switch (msg.what) {
			case CREATE_GROUP_DONE:
				if (res.getResult() == JNIResponse.Result.SUCCESS) {
					final Group g = ((GroupServiceJNIResponse) res).g;
					mDataset.add(new LocalItemWrapper(g, false));
				}
				break;
			case UPDATE_GROUP_DONE:
				adapter.notifyDataSetChanged();
				break;
			case REMOVE_GROUP_DONE:
				if (res.getResult() == JNIResponse.Result.SUCCESS) {
					for (int i = 0; i < mDataset.size(); i++) {
						Group g = (Group) mDataset.get(i).getItemObject();
						if (((GroupServiceJNIResponse) res).g.getGroupID() == g.getGroupID()) {
							mDataset.remove(i);
							break;
						}
					}
				}
				break;

			}
			if (res.getResult() == JNIResponse.Result.SUCCESS) {
				adapter.notifyDataSetChanged();
			}
			changed = true;
		}
	};

	class LocalItemWrapper implements CommonAdapterItemDateAndViewWrapper {

		private Group g;
		private boolean showDeleteButton;

		public LocalItemWrapper(Group g, boolean showDeleteButton) {
			super();
			this.g = g;
			this.showDeleteButton = showDeleteButton;
		}

		@Override
		public Object getItemObject() {
			return g;
		}

		@Override
		public long getItemLongId() {
			return g.getGroupID();
		}

		@Override
		public View getView() {
			return null;
		}

		public Group getG() {
			return g;
		}

		public void setG(Group g) {
			this.g = g;
		}

		public boolean isShowDeleteButton() {
			return showDeleteButton;
		}

		public void setShowDeleteButton(boolean showDeleteButton) {
			this.showDeleteButton = showDeleteButton;
		}

	}

	enum OPT {
		CREATE, UPDATE, DELETE;
	}

}
