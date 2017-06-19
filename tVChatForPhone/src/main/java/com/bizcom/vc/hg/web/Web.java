package com.bizcom.vc.hg.web;

import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.bizcom.vc.hg.web.interf.IBussinessManager;

/**
 * Web接口入口类
 * 
 * @author zhoukang
 */
public final class Web {
	private static RequestQueue mRequestQueue;
	private static ImageLoader mImageLoader;
	private static IBussinessManager mBusinessManager;

	/**
	 * 初始化Web
	 * 
	 * @param context
	 */
	public static void init(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
		int memClass = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		mImageLoader = new ImageLoader(mRequestQueue, new ImageCache() {

			@Override
			public void putBitmap(String url, Bitmap bitmap) {
				// TODO Auto-generated method stub

			}

			@Override
			public Bitmap getBitmap(String url) {
				// TODO Auto-generated method stub
				return null;
			}
		});

		
	}

	/**
	 * 获取HTTP网络操作接口
	 * 
	 * @param url
	 *            HTTP POST请求的url
	 * @return
	 */
	public static IQuery getQuery(String url) {
		return new Query(url, mRequestQueue);
	}



	public static class WebStatus {
		public static final int UNKNOW_ERROR = -1; // 未知错误
		public static final int SUCCESS = 0; // 成功
		public static final int NO_DATA = 1; // 没有记录
		public static final int PARAM_ERROR = 2; // 参数异常
		public static final int SYSTEM_ERROR = 3; // 系统异常
		public static final int JSON_ERROR = 4; // JSON解析异常
	}


	/**
	 * 获取pakageInfo
	 * @return
	 */
	public static PackageInfo getAppPackageInfo(Context context) {
		PackageManager manager;

		PackageInfo info = null;

		manager = context.getPackageManager();

		try {

			info = manager.getPackageInfo(context.getPackageName(), 0);

		} catch (NameNotFoundException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}
		//		info.versionCode;
		//
		//		info.versionName;
		//
		//		info.packageName;
		//
		//		info.signatures;
		return info;
	}

//	public static String getMyUUID(Context mContext){
//
//		final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);   
//
//		final String tmDevice, tmSerial, androidId;   
//
//		tmDevice = "" + tm.getDeviceId();  
//
//		tmSerial = "" + tm.getSimSerialNumber();   
//
//		androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);   
//
//		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());   
//
//		String uniqueId = deviceUuid.toString();
//
//
//		return uniqueId;
//
//	}
	
	
	/**

	* getSerialNumber

	* @return result is same to getSerialNumber1()

	*/

	public static String getSerialNumber(){

	    String serial = null;

	    try {

	    Class<?> c =Class.forName("android.os.SystemProperties");

	       Method get =c.getMethod("get", String.class);

	       serial = (String)get.invoke(c, "ro.serialno");
	       System.out.println("");

	    } catch (Exception e) {

	       e.printStackTrace();

	    }

	    return serial;

	}
}
