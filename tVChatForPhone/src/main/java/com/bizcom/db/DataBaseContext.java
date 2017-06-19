package com.bizcom.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.MainApplication;
import com.bizcom.util.V2Log;
import com.config.GlobalConfig;

import java.io.File;
import java.io.IOException;

/**
 * 用于支持对存储在SD卡上的数据库的访问
 **/
public class DataBaseContext extends ContextWrapper {

	private static final String TAG = MainApplication.class.getSimpleName();

	/**
	 * 构造函数
	 * 
	 * @param base
	 *            上下文环境
	 */
	public DataBaseContext(Context base) {
		super(base);
	}

	/**
	 * 获得数据库路径，如果不存在，则创建对象对象
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public File getDatabasePath(String name) {
		String dbDir = GlobalConfig.getGlobalDataBasePath();
		File dirFile = new File(dbDir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		String dbPath = dbDir + File.separator + name;// 数据库路径
		V2Log.d(TAG, "存用户储数据库文件的根路径 : " + dbPath);
		// 数据库文件是否创建成功
		// 判断文件是否存在，不存在则创建该文件
		File dbFile = new File(dbPath);
		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();// 创建文件
				dbFile.canRead();
				dbFile.canWrite();
			} catch (IOException e) {
				V2Log.e(TAG, "the createNewFile was failed.....");
				e.printStackTrace();
			}
		}
		return dbFile;
	}

	/**
	 * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
	 * 
	 * @param name
	 * @param mode
	 * @param factory
	 */
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			SQLiteDatabase.CursorFactory factory) {
		synchronized (DataBaseContext.class) {
			File file = getDatabasePath(name);
			if (file == null)
				throw new RuntimeException("创建数据库失败");
			return SQLiteDatabase.openOrCreateDatabase(file, null);
		}
	}

	/**
	 * Android 4.0会调用此方法获取数据库。
	 * 
	 * @see android.content.ContextWrapper#openOrCreateDatabase(java.lang.String,
	 *      int, android.database.sqlite.SQLiteDatabase.CursorFactory,
	 *      android.database.DatabaseErrorHandler)
	 * @param name
	 * @param mode
	 * @param factory
	 * @param errorHandler
	 */
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory, DatabaseErrorHandler errorHandler) {
		synchronized (DataBaseContext.class) {
			File file = getDatabasePath(name);
			if (file == null)
				throw new RuntimeException("创建数据库失败");
			return SQLiteDatabase.openOrCreateDatabase(file, null);
		}
	}
}
