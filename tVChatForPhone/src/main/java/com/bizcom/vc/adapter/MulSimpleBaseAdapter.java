package com.bizcom.vc.adapter;

import android.util.SparseArray;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class MulSimpleBaseAdapter extends BaseAdapter{

    protected ArrayList<Integer> TypeList = new ArrayList<>();
    protected List<ListItem> datasList;

    public MulSimpleBaseAdapter(List<ListItem> datasList) {
        this.datasList = datasList;
    }

    public void AddItemType(int mResource) {
        TypeList.add(mResource);
    }

    @Override
    public int getItemViewType(int position) {
        return datasList.get(position).mType;
    }

    @Override
    public int getViewTypeCount() {
        if (TypeList.size() == 0)
            return 1;
        else
            return TypeList.size();
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
        return 0;
    }

    protected abstract int compareToItem(ListItem currentItem , ListItem another);

    protected abstract void updateItemDatas(SparseArray<Object> mMap , Object obj);

    public class ListItem implements Comparable<ListItem> {
        /** 类型 */
        public int mType;
        /** 键值对应Map */
        public SparseArray<Object> mMap;
        public long mId;
        public String sortFlag;

        public ListItem(int type, SparseArray<Object> map, long mId , String sortFlag) {
            mType = type;
            mMap = map;
            this.mId = mId;
            this.sortFlag = sortFlag;
        }

        public void updateItemData(Object obj) {
            updateItemDatas(mMap , obj);
        }

        @Override
        public int compareTo(ListItem another) {
            return compareToItem(this , another);
        }
    }

    public class ViewHolder {
        public ArrayList<Object> List_Object = new ArrayList<>();
        public ArrayList<Integer> List_id = new ArrayList<>();
        public SparseArray<Object> List_Map_Onject = new SparseArray<>();
    }
}
