package com.bizcom.vc.hg.ui;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.bizcom.vc.hg.adapter.VideItemListAdapter;
import com.bizcom.vc.hg.beans.VideoItem;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.shdx.tvchat.phone.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FirstTab1 extends Fragment {
	private PullToRefreshListView mList;
	private List<VideoItem> data;
	private VideItemListAdapter mAdapter;
	private Context mContext;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext=getActivity();
		View v=inflater.inflate(R.layout.hg_first_tab_one, container, false);

		initdata();

		initView(v);
		setListener();
		return v;
	}

	private void initView(View v) {
		mList=(PullToRefreshListView) v.findViewById(R.id.mList);
		ILoadingLayout startLabels = mList  
				.getLoadingLayoutProxy();  
		startLabels.setPullLabel("");// 刚下拉时，显示的提示  
		startLabels.setRefreshingLabel("");// 刷新时  
		startLabels.setReleaseLabel("");// 下来达到一定距离时，显示的提示 
		mAdapter=new VideItemListAdapter(data, mContext);
		mList.setAdapter(mAdapter);
		
	}

	private void initdata() {
		data=new ArrayList<VideoItem>();

		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
		data.add(new VideoItem("", "美食", "南瓜芝士焗饭", 69+"人看过"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setListener() {
		mList.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh(PullToRefreshBase refreshView) {


			}
		});

	}

}
