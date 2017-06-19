package com.bizcom.vc.hg.ui;

import java.util.ArrayList;
import java.util.List;

import com.MainApplication;
import com.bizcom.vc.hg.beans.setPBeans;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.cgs.utils.ToastUtil;
import com.lidroid.xutils.exception.DbException;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import v2av.VideoEncoder;
import v2av.VideoPlayer;

public class Setp extends Activity{



	private EditText malv;
	private EditText zhenlv;
	private EditText wid;
	private EditText hei;
	private EditText ifSoft1;
	private EditText ifSoft2;
	private Button comfir;
	private HeadLayoutManagerHG mHeadLayoutManager;
	private TextView ti;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setp);
		initView();
	}

	private void initView() {
		mHeadLayoutManager = new HeadLayoutManagerHG(this, findViewById(R.id.head_layout) , false);
		mHeadLayoutManager.updateTitle("设置");

		setPBeans beans = MainApplication.getP();
		ti=(TextView)findViewById(R.id.ti);
		malv=(EditText)findViewById(R.id.malv);
		zhenlv=(EditText)findViewById(R.id.zhenlv);
		wid=(EditText)findViewById(R.id.wid);
		hei=(EditText)findViewById(R.id.hei);
		ifSoft1=(EditText)findViewById(R.id.ifSoft1);
		ifSoft2=(EditText)findViewById(R.id.ifSoft2);
		
		ti.setText(android.os.Build.MODEL);
		if(beans!=null){
			wid.setText(String.valueOf( beans.getWid()));
			hei .setText(String.valueOf( beans.getHei()));
			malv.setText(String.valueOf( beans.getMalv()));
			zhenlv.setText(String.valueOf(beans.getZhenlv()));
			ifSoft1.setText(String.valueOf(beans.getF1()));
			ifSoft2.setText(String.valueOf(beans.getF2()));

		}



		comfir=(Button)findViewById(R.id.comfir);
		comfir.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					String m=malv.getText().toString();
					String z=zhenlv.getText().toString();
					String w=wid.getText().toString();
					String h=hei.getText().toString();
					String if1=ifSoft1.getText().toString();
					String if2=ifSoft2.getText().toString();
					setPBeans mBeans=new setPBeans(
							Integer.parseInt(m)
							, 
							Integer.parseInt(z)
							, 
							Integer.parseInt(w)
							, Integer.parseInt(h)
							, Integer.parseInt(if1)
							, Integer.parseInt(if2)
							);
							MainApplication.setP(mBeans);;
							ToastUtil.ShowToast_long(getApplicationContext(), "设置成功");
							finish();
				} catch (Exception e) {
					ToastUtil.ShowToast_long(getApplicationContext(), "你设置有误");
				}
			}
		});
	}
}
