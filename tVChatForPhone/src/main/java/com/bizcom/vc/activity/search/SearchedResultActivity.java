package com.bizcom.vc.activity.search;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2ImRequest;
import com.bizcom.service.JNIService;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.adapter.SimpleBaseAdapter;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Crowd;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.SearchedResult;
import com.bizcom.vo.SearchedResult.SearchedResultItem;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.List;

public class SearchedResultActivity extends BaseActivity {

	private ListView mListView;
	private TextView mReturnButton;

	private SearchedResult sr;
	private List<SearchedResult.SearchedResultItem> mList;

	private LocalAdapter adapter;
	private int searchType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_searchserver_result);
		super.setNeedAvatar(true);
		super.setNeedBroadcast(true);
		super.setNeedHandler(true);
		super.onCreate(savedInstanceState);

		if (getIntent().getExtras() != null) {
			sr = (SearchedResult) getIntent().getExtras().get("result");
			searchType = getIntent().getIntExtra("searchType", -1);
			if (sr != null) {
				mList = sr.getList();
				adapter = new LocalAdapter(this, mList);
				mListView.setAdapter(adapter);
			}
		}
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void addBroadcast(IntentFilter filter) {
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_UPDATED);
		filter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		String action = intent.getAction();
		if (JNIService.JNI_BROADCAST_GROUP_UPDATED.equals(action)) {
			long gid = intent.getLongExtra("gid", 0);
			for (int i = 0; i < mList.size(); i++) {
				SearchedResultItem item = mList.get(i);
				if (item.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP && item.id == gid) {
					Group searchGroup = GlobalHolder.getInstance().getGroupById(item.id);
					if (searchGroup != null) {
						item.name = searchGroup.getName();
						adapter.notifyDataSetChanged();
					}
					break;
				}
			}
		} else if (PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION.equals(action)) {
			if (searchType != V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP) {
				return;
			}

			GroupUserObject obj = intent.getParcelableExtra("group");
			if (obj == null || obj.getmUserId() != -1) {
				return;
			}

			long gid = obj.getmGroupId();
			for (int i = 0; i < mList.size(); i++) {
				SearchedResultItem item = mList.get(i);
				if (item.id == gid) {
					mList.remove(i);
					adapter.notifyDataSetChanged();
					break;
				}
			}
		} else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(action)) {
			if (searchType != V2GlobalConstants.SEARCH_REQUEST_TYPE_USER) {
				return;
			}

			GroupUserObject obj = intent.getParcelableExtra("obj");
			if (obj == null || obj.getmType() != V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				return;
			}

			long uid = obj.getmUserId();
			for (int i = 0; i < mList.size(); i++) {
				SearchedResultItem item = mList.get(i);
				if (item.id == uid) {
					mList.remove(i);
					adapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}

	@Override
	public void receiveMessage(Message msg) {

	}

	@Override
	public void initViewAndListener() {
		mListView = (ListView) findViewById(R.id.search_result_list_view);
		mListView.setOnItemClickListener(mItemClickListener);

		TextView mTitleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
		mTitleContent.setText(getResources().getString(R.string.search_title_result));
		mReturnButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		setComBackImageTV(mReturnButton);
		mReturnButton.setOnClickListener(mReturnButtonListener);
		findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {
		if (targetUser == null || bnewAvatarm == null)
			return;
		for (SearchedResultItem item : mList) {
			if (item.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_USER) {
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}

	private OnClickListener mReturnButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			SearchedResult.SearchedResultItem item = mList.get(position);
			if (item.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP) {
				long cid = item.id;
				Intent i = new Intent();
				CrowdGroup crowd = (CrowdGroup) GlobalHolder.getInstance().getGroupById(cid);
				if (crowd == null) {
					i.setAction(PublicIntent.SHOW_CROWD_APPLICATION_ACTIVITY);
					User u = new User(item.creator, item.creatorName);
					Crowd cr = new Crowd(item.id, u, item.name, item.brief);
					cr.setAuth(item.authType);
					i.putExtra("crowd", cr);
					i.putExtra("isFromSearch", true);
					// set disable authentication
					i.putExtra("authdisable", false);
				} else {
					i.putExtra("obj", new ConversationNotificationObject(Conversation.TYPE_GROUP, crowd.getGroupID()));
					i.setAction(PublicIntent.START_CONVERSACTION_ACTIVITY);
				}
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				startActivity(i);
			} else if (item.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_USER) {
				boolean isGetInfo = false;
				List<Group> orgList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_DEPARTMENT);
				User u = new User(item.id);
				for (int i = 0; i < orgList.size(); i++) {
					Group g = orgList.get(i);
					if (g.findUser(u) != null) {
						isGetInfo = true;
					}
				}

				List<Group> contactsList = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
				User contactTemp = new User(item.id);
				for (int i = 0; i < contactsList.size(); i++) {
					Group g = contactsList.get(i);
					if (g.findUser(contactTemp) != null) {
						isGetInfo = true;
					}
				}

				if (u.getmUserId() == GlobalHolder.getInstance().getCurrentUserId())
					isGetInfo = true;

				if (!isGetInfo) {
					V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, item.id);
				}
                Intent intent = new Intent(mContext , ContactDetail2.class);
                intent.putExtra("uid", u.getmUserId());
                intent.putExtra("fromActivity" , "SearchResultActivity");
                intent.putExtra("isOutOrg" , !isGetInfo);
                startActivity(intent);
			}
		}

	};

	class LocalAdapter extends SimpleBaseAdapter<SearchedResultItem> {

		ViewItem item = null;

		class ViewItem {
			CustomAvatarImageView iv;
			TextView tv;
		}

		public LocalAdapter(Context mContext, List<SearchedResultItem> list) {
			super(mContext, mList);
		}

        @Override
        protected int compareToItem(ListItem currentItem, ListItem another) {
            return 0;
        }

        @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			item = null;
			if (view == null) {
				view = View.inflate(mContext, R.layout.common_conversation_layout, null);
				item = new ViewItem();

				View iconLy = view.findViewById(R.id.ws_common_conversation_layout_icon_ly);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.common_margin_horizontal_border);
				layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
				iconLy.setLayoutParams(layoutParams);

				view.findViewById(R.id.ws_common_conversation_layout_contentLayout).setVisibility(View.INVISIBLE);
				item.iv = (CustomAvatarImageView) view.findViewById(R.id.ws_common_avatar);
				item.tv = (TextView) view.findViewById(R.id.ws_common_conversation_middle_content);
				item.tv.setVisibility(View.VISIBLE);
                view.findViewById(R.id.ws_common_conversation_buttomdivider).setVisibility(View.VISIBLE);
				view.setTag(item);
			} else {
				item = (ViewItem) view.getTag();
			}
			SearchedResult.SearchedResultItem srItem = mList.get(position);
			item.tv.setText(srItem.name);
			if (srItem.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP) {
				item.iv.setImageResource(R.drawable.chat_group_icon);
			} else if (srItem.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_USER) {
				User user = GlobalHolder.getInstance().getUser(srItem.id);
				item.iv.setImageBitmap(user.getAvatarBitmap());
			}
			return view;
		}

	}

}
