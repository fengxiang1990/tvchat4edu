package com.bizcom.vc.hg.ui;

import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class GuideActivity extends Activity{
	
	private GuideActivity mContext;
	private HeadLayoutManagerHG mHeadLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if(getIntent().getIntExtra("type", 0)==2){
			setContentView(R.layout.hg_activity_guide);
		}else{
			setContentView(R.layout.hg_activity_guide2);
		}
		
		mContext=this;
		initView();
	}

	private void initView() {
		String titleText = getIntent().getStringExtra("titleText");
		mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout) , false);
		mHeadLayoutManager.updateTitle(titleText);
		
	}

}
