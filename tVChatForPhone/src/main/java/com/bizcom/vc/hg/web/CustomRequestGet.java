package com.bizcom.vc.hg.web;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bizcom.vc.hg.web.Web.WebStatus;



public class CustomRequestGet extends Request<String> {

	/** Callback interface for delivering parsed responses. */

	/**  网络异常 */
	public static final int ERROE = 0xFFFFFFFF;
	/**  网络未连接 */
	public static final int ERROE_DISCONNECT = 0xFFFFFFFE;
	/**  服务器异常 */
	public static final int ERROE_SERVER = 0xFFFFFFFD;
	/**  连接超时 */
	public static final int ERROE_TIMEOUT = 0xFFFFFFFC;
	
	/** Charset for request. */
	public static final String PROTOCOL_CHARSET = "utf-8";
	/** Content type for request. */
	private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", PROTOCOL_CHARSET);

	private CustomListener  listener;

	public CustomRequestGet(String url, final CustomListener customListener, ErrorListener errorListener) {
		super(Method.GET, url, errorListener);
		this.listener = customListener;
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		
		String jsonString = "";
		try {
			jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			
			return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
			
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (Exception e) {
			return Response.error(new ParseError(e));
		}
	}

	@Override
	protected void deliverResponse(String response) {
		if (listener != null) {
			
			listener.onResponse(WebStatus.SUCCESS, response, "有响应！");
		}
		
	}

	@Override
	public String getBodyContentType() {
		return PROTOCOL_CONTENT_TYPE;
	}


	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {

		if(TextUtils.isEmpty(cookie)){
			return super.getHeaders();
		}else{
			Map<String,String>mHeaders=new HashMap<String,String>(1);
			mHeaders.put("Cookie",cookie);
			return mHeaders;
		}

	}

private  String cookie="";
	public void  setCookie(String cookie){
		this.cookie=cookie;
	}
}