package com.bizcom.vc.hg.ui;

import com.cgs.utils.ToastUtil;
import com.shdx.tvchat.phone.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
public class FirstTab2 extends Fragment {
		private WebView mWebView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			View v=inflater.inflate(R.layout.hg_first_tab_two, container, false);
			
			  mWebView=(WebView)v.findViewById(R.id.wv);
		       mWebView.setWebViewClient(new WebViewClient(){
		    	   @Override
		    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    		   
		    		   view.loadUrl(url);
		    		return true;
		    	}
		       });
		       mWebView.loadUrl("http://180.166.2.3/protocal/tvChat.html");
			return v;
		}

	}


