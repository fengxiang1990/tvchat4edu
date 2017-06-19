package com.bizcom.vc.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bizcom.vc.adapter.view.ContactUserView;
import com.bizcom.vo.User;
import com.shdx.tvchat.phone.R;

public class LeftInvitionAttendeeAdapter extends SimpleBaseAdapter<User> {

	private static final int PAD_LAYOUT = 1;

	private int landLayout;

	public LeftInvitionAttendeeAdapter(Context mContext, List<User> mUserListArray, int landLayout) {
		super(mContext, mUserListArray);
		this.landLayout = landLayout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewTag tag = null;
		User user = datasList.get(position);
		if (convertView == null) {
			tag = new ViewTag();

			if (landLayout == PAD_LAYOUT) {
				convertView = new ContactUserView(mContext, user, false);
				tag.headIcon = ((ContactUserView) convertView).getmPhotoIV();
				tag.name = ((ContactUserView) convertView).getmUserNameTV();
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
	}

	private View getAttendeeView(ViewTag tag, final User u) {
		final LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);

		ImageView iv = new ImageView(mContext);
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

    @Override
    protected int compareToItem(ListItem currentItem, ListItem another) {
        return 0;
    }

    class ViewTag {
		ImageView headIcon;
		TextView name;
	}
}
