package com.bizcom.vc.adapter;

import java.util.List;

import com.bizcom.vc.adapter.view.ContactUserView;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.User;
import com.bizcom.vo.User.DeviceType;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseCreateAdapter extends SimpleBaseAdapter<User> {

	private static final int PAD_LAYOUT = 1;

	private Context mContext;
	private int landLayout;
	private List<User> mUserListArray;

	public BaseCreateAdapter(Context mContext, List<User> mUserListArray, int landLayout) {
		this.mUserListArray = mUserListArray;
		this.landLayout = landLayout;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return mUserListArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserListArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mUserListArray.get(position).getmUserId();
	}

    @Override
    protected int compareToItem(ListItem currentItem, ListItem another) {
        return 0;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewTag tag = null;
		User user = mUserListArray.get(position);
		if (convertView == null) {
			tag = new ViewTag();

			if (landLayout == PAD_LAYOUT) {
				convertView = new ContactUserView(mContext, user, false);
				tag.headIcon = ((ContactUserView) convertView).getmPhotoIV();
				tag.name = ((ContactUserView) convertView).getmUserNameTV();
				tag.mSignature = ((ContactUserView) convertView).getmUserSignatureTV();
				tag.statusIcon = ((ContactUserView) convertView).getmStatusIV();
			} else {
				convertView = getAttendeeView(tag, user);
			}
			convertView.setTag(tag);

		} else {
			tag = (ViewTag) convertView.getTag();
		}

		updateView(tag, user);
		return convertView;
	}

	private void updateView(ViewTag tag, User user) {

		tag.headIcon.setImageBitmap(user.getAvatarBitmap());
		tag.name.setText(user.getDisplayName());
		if (tag.mSignature != null)
			tag.mSignature.setText(user.getSignature() == null ? "" : user.getSignature());

		if (tag.statusIcon != null) {
			DeviceType deviceType = user.getDeviceType();
			if (deviceType == DeviceType.CELL_PHONE) {
				tag.statusIcon.setImageResource(R.drawable.cell_phone_user);
			} else {
				switch (user.getmStatus()) {
				case ONLINE:
					tag.statusIcon.setImageResource(R.drawable.online);
					break;
				case LEAVE:
					tag.statusIcon.setImageResource(R.drawable.leave);
					break;
				case BUSY:
					tag.statusIcon.setImageResource(R.drawable.busy);
					break;
				case DO_NOT_DISTURB:
					tag.statusIcon.setImageResource(R.drawable.do_not_distrub);
					break;
				default:
					break;
				}
				if (user.getmStatus() == User.Status.OFFLINE || user.getmStatus() == User.Status.HIDDEN) {
					tag.statusIcon.setVisibility(View.GONE);
				} else {
					tag.statusIcon.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private View getAttendeeView(ViewTag tag, final User u) {
		final LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);

		CustomAvatarImageView iv = new CustomAvatarImageView(mContext);
        iv.setOval(true);
		tag.headIcon = iv;
		iv.setImageBitmap(u.getAvatarBitmap());
		ll.addView(iv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		TextView tv = new TextView(mContext);
		tag.name = tv;

		tv.setText(u.getDisplayName());
		tv.setEllipsize(TruncateAt.END);
		tv.setSingleLine(true);
		tv.setTextSize(8);
		tv.setMaxWidth(80);
		ll.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		ll.setPadding(5, 5, 5, 5);
		return ll;
	}

	class ViewTag {
		CustomAvatarImageView headIcon;
		ImageView statusIcon;
		TextView name;
		TextView mSignature;
	}
}
