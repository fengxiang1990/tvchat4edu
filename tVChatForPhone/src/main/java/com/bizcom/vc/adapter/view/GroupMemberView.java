package com.bizcom.vc.adapter.view;

import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.User;
import com.bizcom.vo.User.Status;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GroupMemberView extends LinearLayout {

	private final int PHOTO_ID = 0x0001;
	private Context mContext;
	private ImageView mDeleteIV;
	private CustomAvatarImageView mPhotoIV;
	private ImageView mStatusIV;
	private TextView mNameTV;
	private TextView mDeleteButtonTV;
	private RelativeLayout mContentLayout;
	private User mUser;
	private RelativeLayout phothRL;

	public GroupMemberView(Context context, User user, final ClickListener callBack, boolean isInDeleteMode,
			User ownerUser) {
		super(context);
		mContext = context;
		this.mUser = user;
		this.setOrientation(LinearLayout.VERTICAL);
		LinearLayout root = new LinearLayout(context);
		root.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		int margin = (int) context.getResources().getDimension(R.dimen.conversation_view_margin);
		rootParams.leftMargin = margin;
		rootParams.rightMargin = margin;
		rootParams.topMargin = margin;
		rootParams.bottomMargin = margin;
		rootParams.gravity = Gravity.CENTER_VERTICAL;

		// Add delete icon
		mDeleteIV = new ImageView(mContext);
		Options opts = new Options();
		opts.outWidth = (int) getResources().getDimension(R.dimen.common_delete_icon_width);
		opts.outHeight = (int) getResources().getDimension(R.dimen.common_delete_icon_height);
		Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete, opts);
		mDeleteIV.setImageBitmap(bit);
		if (isInDeleteMode && user.getmUserId() != ownerUser.getmUserId()) {
			mDeleteIV.setVisibility(View.VISIBLE);
		} else {
			mDeleteIV.setVisibility(View.GONE);
		}

		int padding = (int) getResources().getDimension(R.dimen.common_delete_icon_padding);
		mDeleteIV.setPadding(padding, padding, padding, padding);
		mDeleteIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mUser.isShowDelete) {
					callBack.changeDeletedMembers(false, mUser);
					mUser.isShowDelete = false;
					mDeleteButtonTV.setVisibility(View.GONE);
				} else {
					callBack.changeDeletedMembers(true, mUser);
					mUser.isShowDelete = true;
					mDeleteButtonTV.setVisibility(View.VISIBLE);
				}
			}

		});
		root.addView(mDeleteIV, rootParams);

		phothRL = new RelativeLayout(context);
		root.addView(phothRL, rootParams);

		mStatusIV = new ImageView(context);
		mPhotoIV = new CustomAvatarImageView(context);
        mPhotoIV.setId(PHOTO_ID);
		mPhotoIV.setImageBitmap(user.getAvatarBitmap());
        mPhotoIV.setOval(true);
		updateUserStatus(user);

		RelativeLayout.LayoutParams mPhotoParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		phothRL.addView(mPhotoIV, mPhotoParams);

		RelativeLayout.LayoutParams mStatusParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mStatusParams.addRule(RelativeLayout.ALIGN_BOTTOM, PHOTO_ID);
		mStatusParams.addRule(RelativeLayout.ALIGN_RIGHT, PHOTO_ID);
		phothRL.addView(mStatusIV, mStatusParams);

		mContentLayout = new RelativeLayout(context);
		root.addView(mContentLayout, rootParams);
		mNameTV = new TextView(context);
		mNameTV.setText(user.getDisplayName());
		mNameTV.setGravity(Gravity.CENTER_VERTICAL);
		mNameTV.setTextColor(context.getResources().getColor(R.color.common_item_text_color_black));
		mNameTV.setSingleLine();
		mNameTV.setEllipsize(TruncateAt.END);
		RelativeLayout.LayoutParams mNameParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mNameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		mNameParams.addRule(RelativeLayout.LEFT_OF, 2);
		mNameParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mContentLayout.addView(mNameTV, mNameParams);

		// Add delete button
		mDeleteButtonTV = new TextView(mContext);
		mDeleteButtonTV.setText(R.string.crowd_members_delete);
		mDeleteButtonTV.setVisibility(View.GONE);
		mDeleteButtonTV.setTextColor(Color.WHITE);
		mDeleteButtonTV.setBackgroundResource(R.drawable.rounded_crowd_members_delete_button);
		mDeleteButtonTV.setGravity(Gravity.CENTER_VERTICAL);
		mDeleteButtonTV.setPadding(20, 10, 20, 10);
		mDeleteButtonTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!GlobalHolder.getInstance().isServerConnected()) {
					Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
					return;
				}
				v.setVisibility(View.GONE);
				callBack.removeMember(mUser);
			}

		});

		RelativeLayout.LayoutParams mDeleteButtonTVLP = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mDeleteButtonTVLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mDeleteButtonTVLP.addRule(RelativeLayout.CENTER_VERTICAL);
		mDeleteButtonTVLP.rightMargin = margin;
		mDeleteButtonTVLP.leftMargin = margin;

		mContentLayout.addView(mDeleteButtonTV, mDeleteButtonTVLP);

		this.addView(root, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		LinearLayout lineBottom = new LinearLayout(context);
		lineBottom.setBackgroundColor(mContext.getResources().getColor(R.color.common_line_color));
		this.addView(lineBottom, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
	}

	public void update(boolean isInDeleteMode, User user, User groupOwnerUser) {
		if (isInDeleteMode) {
			if (user.getmUserId() != groupOwnerUser.getmUserId()) {
				mDeleteIV.setVisibility(View.VISIBLE);
			} else {
				mDeleteIV.setVisibility(View.GONE);
			}

			if (user.isShowDelete) {
				mDeleteButtonTV.setVisibility(View.VISIBLE);
			} else {
				mDeleteButtonTV.setVisibility(View.GONE);
			}
		} else {
			mDeleteIV.setVisibility(View.GONE);
			mDeleteButtonTV.setVisibility(View.GONE);
		}

		this.mUser = user;
		mNameTV.setText(user.getDisplayName());
		mPhotoIV.setImageBitmap(user.getAvatarBitmap());
		updateUserStatus(user);
	}

	private void updateUserStatus(User user) {
		User.DeviceType dType = user.getDeviceType();
		User.Status st = user.getmStatus();
		if (dType == User.DeviceType.CELL_PHONE) {
			mStatusIV.setImageResource(R.drawable.cell_phone_user);
		} else {
			switch (st) {
			case ONLINE:
				mStatusIV.setImageResource(R.drawable.online);
				break;
			case LEAVE:
				mStatusIV.setImageResource(R.drawable.leave);
				break;
			case BUSY:
				mStatusIV.setImageResource(R.drawable.busy);
				break;
			case DO_NOT_DISTURB:
				mStatusIV.setImageResource(R.drawable.do_not_distrub);
				break;
			case OFFLINE:
				mStatusIV.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}

		if (st == Status.OFFLINE) {
			if (mStatusIV.getVisibility() == View.VISIBLE) {
				mStatusIV.setVisibility(View.GONE);
			}
		} else {
			if (mStatusIV.getVisibility() == View.GONE)
				mStatusIV.setVisibility(View.VISIBLE);
		}
	}

	public interface ClickListener {

		public void changeDeletedMembers(boolean isAdd, User user);

		public void removeMember(User user);
	}
}
