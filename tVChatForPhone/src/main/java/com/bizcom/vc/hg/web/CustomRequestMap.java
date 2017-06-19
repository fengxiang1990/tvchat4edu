package com.bizcom.vc.hg.web;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.bizcom.vc.hg.util.TestDES;

import android.text.TextUtils;
import android.util.Log;

public class CustomRequestMap extends CustomRequestPost {

	private Map<String, Object> reqMap;

	public CustomRequestMap(String url, Map<String, Object> reqMap, final CustomListener listener) {
		super(url, listener, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				listener.onResponse(ERROE, null, null);
			}
		});
		this.reqMap = reqMap;
	}

	@Override
	public byte[] getBody() {
		try {
			String data = new JSONObject(reqMap).toJSONString();
//			String methodName=(String) reqMap.get("methodName");

//			if(TextUtils.equals(methodName, "serviceAddr")){
				return data.getBytes(PROTOCOL_CHARSET);
//			}else{
//				data=TestDES.encode(data);
//				String timeStamp=System.currentTimeMillis()+"";
//				String sign= new MD5().getMD5ofStr(timeStamp+methodName);
//				Map<String, Object> m=new HashMap<String, Object>();
//				m.put("data", data);
//				m.put("timeStamp", timeStamp);
//				m.put("sign", sign);
//
//				String lastData =new JSONObject(m).toJSONString();
//				return lastData.getBytes(PROTOCOL_CHARSET);
//			}
			


		} catch (UnsupportedEncodingException uee) {
			VolleyLog.wtf("Unsupported Encoding of %s using %s", "", PROTOCOL_CHARSET);
			return null;
		} catch (Exception e) {
			VolleyLog.wtf("Parse to json string error");
			return null;
		}
	}
}
