package com.bizcom.vc.hg.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bizcom.util.MessageUtil;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.vo.User;
import com.bizcom.vo.User.Status;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.util.List;

public class FriendAdapter extends BaseAdapter {
    private List<User> data;
    private LayoutInflater mInflater;


    public FriendAdapter(List<User> data, Context mCon) {
        super();
        this.data = data;
        mInflater = LayoutInflater.from(mCon);
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User myFriendItem = data.get(position);
        ViewTag tag;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hg_gd_item, null);
            tag = new ViewTag();
            tag.tv1 = (TextView) convertView.findViewById(R.id.tvState);
            tag.im = (SimpleDraweeView) convertView.findViewById(R.id.tvFistName);
            tag.tv3 = (TextView) convertView.findViewById(R.id.name);
            tag.tv4 = (TextView) convertView.findViewById(R.id.phone);
            tag.tvIcon = (ImageView) convertView.findViewById(R.id.imIcon);
            tag.tv_firstNameText = (TextView) convertView.findViewById(R.id.firstNameText);

            convertView.setTag(tag);
        } else {
            tag = (ViewTag) convertView.getTag();
        }

        tag.tv3.setText(myFriendItem.getDisplayName());
        tag.tv4.setText(myFriendItem.getAccount());


        if (myFriendItem.getAccount().length() == 11) {
            tag.tvIcon.setVisibility(View.GONE);
        } else {
            tag.tvIcon.setVisibility(View.VISIBLE);
        }

        if (myFriendItem.getmStatusToIntValue() == Status.ONLINE.toIntValue()) {
            tag.tv1.setVisibility(View.VISIBLE);
        } else {
            tag.tv1.setVisibility(View.GONE);
        }

        tag.tv_firstNameText.setText(MessageUtil.getFirstLetterName(myFriendItem.getDisplayName()));
        SimpleDraweeViewUtils.display(tag.im,tag.tv_firstNameText, myFriendItem.getmAvatarLocation());

        return convertView;
    }

    class ViewTag {
        TextView tv_firstNameText;
        TextView tv1;
        SimpleDraweeView im;
        TextView tv3;
        TextView tv4;
        ImageView tvIcon;
    }
}
