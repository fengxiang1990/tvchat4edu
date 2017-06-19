package com.bizcom.vc.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.content.Context;
import android.util.SparseArray;
import android.widget.BaseAdapter;

public abstract class SimpleBaseAdapter<T> extends BaseAdapter {
	protected Context mContext;
	protected List<T> datasList;

	public SimpleBaseAdapter() {
		super();
	}

	public SimpleBaseAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		this.datasList = new ArrayList<T>();
	}

	public SimpleBaseAdapter(Context mContext, List<T> list) {
		super();
		this.mContext = mContext;
		this.datasList = list;
	}

	@Override
	public int getCount() {
		return datasList.size();
	}

	@Override
	public Object getItem(int position) {
		return datasList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    protected abstract int compareToItem(ListItem currentItem , ListItem another);

    public class ListItem implements Comparable<ListItem> {
        /** 类型 */
        public int mType;
        public long mId;
        public Object mEntity;
        public Object mTag;
        public Object sortFlag;


        public ListItem(int type, long mId , Object mEntity , Object sortFlag) {
            mType = type;
            this.mId = mId;
            this.sortFlag = sortFlag;
            this.mEntity = mEntity;
        }

        @Override
        public int compareTo(ListItem another) {
            return compareToItem(this , another);
        }
    }
}
