package com.bizcom.vc.hg.adapter;

import java.util.List;

import com.bizcom.vc.hg.beans.MyFriendItem;
import com.bizcom.vc.hg.beans.VideoItem;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideItemListAdapter extends BaseAdapter{
	private List<VideoItem> data;
	private Context mCon;
	private LayoutInflater mInflater;


	public VideItemListAdapter(List<VideoItem> data, Context mCon) {
		super();
		this.data = data;
		this.mCon = mCon;
		mInflater=LayoutInflater.from(mCon);
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
		VideoItem mItem=data.get(position);
		ViewTag tag;
		if (convertView == null) {
			convertView=mInflater.inflate(R.layout.hg_tujian_item, null);
			tag = new ViewTag();
			tag.tv1=(TextView) convertView.findViewById(R.id.tv_tuijian_tv1);
			tag.tv2=(TextView) convertView.findViewById(R.id.tv_tuijian_tv2);
			tag.tv3=(TextView) convertView.findViewById(R.id.tv_tuijian_tv3);
			tag.im=(ImageView) convertView.findViewById(R.id._im);


			convertView.setTag(tag);
		} else {
			tag = (ViewTag) convertView.getTag();
		}

		tag.tv1.setText(mItem.getText1());
		tag.tv2.setText(mItem.getText2());
		tag.tv3.setText(mItem.getText3());
		return convertView;
	}



	class ViewTag {
		TextView tv1;
		TextView tv2;
		TextView tv3;
		ImageView im;
	}
}
