package com.bizcom.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<>();
	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
//        catchNomalLogToFile();
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				V2Log.e(TAG, "error : ", e);
			}
			// 退出程序
			System.exit(0);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// 使用Toast来显示异常信息
	new Handler().post(new Runnable() {
		
		@Override
		public void run() {
			Toast.makeText(mContext,"很抱歉，出现未知错误", Toast.LENGTH_LONG).show();
			
		}
	});
		// 收集设备参数信息
		collectDeviceInfo(mContext);
		// 保存日志文件
        saveCrashInfoToFile(ex);
		// catchLog();
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			V2Log.e(TAG, "collectDeviceInfo an error occured when collect package info", e);
		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				V2Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				V2Log.e(TAG, "collectDeviceInfo an error occured when collect crash info", e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	public String saveCrashInfo2File(Throwable ex) {
        V2Log.e(TAG , " crash info : " + ex.getLocalizedMessage());
//		StringBuffer sb = new StringBuffer();
//		for (Map.Entry<String, String> entry : infos.entrySet()) {
//			String key = entry.getKey();
//			String value = entry.getValue();
//			sb.append(key + "=" + value + "\n");
//		}
//
//		Writer writer = new StringWriter();
//		PrintWriter printWriter = new PrintWriter(writer);
//		ex.printStackTrace(printWriter);
//		Throwable cause = ex.getCause();
//		while (cause != null) {
//			cause.printStackTrace(printWriter);
//			cause = cause.getCause();
//		}
//		printWriter.close();
//		String result = writer.toString();
//		sb.append(result);
//		try {
//			long timestamp = System.currentTimeMillis();
//			String time = formatter.format(new Date());
//			String fileName = "crash-" + time + "-" + timestamp + ".log";
//			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//				String path = GlobalConfig.getGlobalRootPath() + "/crash/";
//				File dir = new File(path);
//				if (!dir.exists()) {
//					dir.mkdirs();
//				}
//
//				File desFile = new File(path + fileName);
//				if (!desFile.exists()) {
//					desFile.createNewFile();
//				}
//				FileOutputStream fos = new FileOutputStream(desFile);
//				fos.write(sb.toString().getBytes());
//				fos.close();
//			}
//			return fileName;
//		} catch (Exception e) {
//			Log.e(TAG, "an error occured while writing file...", e);
//		}
		return null;
	}

    private String saveCrashInfoToFile(Throwable ex){
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = GlobalConfig.getGlobalCrashPath(mContext);
//                CommBar__XLog
                File dir = new File(path);
                File desFile;
                if (!dir.exists()) {
                    boolean mkdirRes = dir.mkdirs();
                    if(mkdirRes){
                        desFile = new File(path + fileName);
                        if (!desFile.exists()) {
                            desFile.createNewFile();
                        }
                    } else {
                        desFile = new File(GlobalConfig.getGlobalRootPath() + fileName);
                        if (!desFile.exists()) {
                            desFile.createNewFile();
                        }
                    }
                } else {
                    desFile = new File(path + fileName);
                    if (!desFile.exists()) {
                        desFile.createNewFile();
                    }
                }

                FileOutputStream fos = new FileOutputStream(desFile);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            V2Log.e(TAG, "saveCrashInfoToFile an error occured while writing file...", e);
        }
        return null;
    }

	// 把所有的log输出到文件
	private void catchNomalLogToFile() {
        GlobalConfig.initConfigLogFile();
        File createLogFile = new File(GlobalConfig.getGlobalCrashPath(mContext),
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt");
        V2Log.d(TAG, "Catching log to  " + createLogFile.getAbsolutePath());
//		String cmd = "logcat -v time -d -f  " + createLogFile.getName();
		String cmd = "logcat -b system -b main -b events -b radio -v time -f " + createLogFile.getAbsolutePath();
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}