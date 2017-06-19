package com.bizcom.vc.hg.adapter;

import java.util.List;
import java.util.Map;

import com.bizcom.vc.hg.beans.MyFriendItem;
import com.bizcom.vc.hg.beans.Simple_gridItem;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SimpleGridAdapter extends BaseAdapter{
	private List<Simple_gridItem> data;
	private Context mCon;
	private LayoutInflater mInflater;


	public SimpleGridAdapter(List<Simple_gridItem> data, Context mCon) {
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
		Simple_gridItem item=data.get(position);
		ViewTag tag;
		if (convertView == null) {
			convertView=mInflater.inflate(R.layout.hg_simple_gd_view_item, null);
			tag = new ViewTag();
			tag.im=(ImageView) convertView.findViewById(R.id.im);
			tag.tv=(TextView) convertView.findViewById(R.id.tv);


			convertView.setTag(tag);
		} else {
			tag = (ViewTag) convertView.getTag();
		}

		tag.im.setBackgroundResource(item.getRid());
		tag.tv.setText(item.getText());
		return convertView;
	}



	class ViewTag {
		ImageView im;
		TextView tv;
	}
}
