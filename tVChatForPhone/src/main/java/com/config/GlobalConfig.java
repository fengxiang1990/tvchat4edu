package com.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.MainApplication;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.beans.setPBeans;
import com.bizcom.vo.UserDeviceConfig;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import v2av.VideoCaptureDevInfo;
import v2av.VideoCaptureDevInfo.CapParams;
import v2av.VideoEncoder;
import v2av.VideoPlayer;
import v2av.VideoRecorder;

public class GlobalConfig {

	public static final String KEY_LOGGED_IN = "LoggedIn";

	public static final String DATA_SAVE_FILE_NAME = "hg_tvChat";

	public static final String DEFAULT_CONFIG_FILE = "v2platform.cfg";

	public static final String DEFAULT_CONFIG_LOG_FILE = "log_options.xml";

	public static String GLOBAL_VERSION_NAME = "1.3.0.1";

	public static int MESSAGE_RECV_BINARY_TYPE_TEXT = 0;

	public static boolean isLogined;

	public static boolean isFirstLauncher;

	public static Context APPLICATION_CONTEXT;
	/**
	 * 当前登录设备的密度的水平
	 */
	public static int GLOBAL_DENSITY_LEVEL = DisplayMetrics.DENSITY_XHIGH;

	/**
	 * 当前登录设备的密度的值
	 */
	public static float GLOBAL_DENSITY_VALUE;

	/**
	 * xxxhdip的数值
	 */
	public static int DENSITY_XXXHIGH = 640;

	/**
	 * xxhdip的数值
	 */
	public static int DENSITY_XXHIGH = 480;
	/**
	 * 屏幕英寸数
	 */
	public static double SCREEN_INCHES = 0;

	/**
	 * 屏幕宽度(竖屏)
	 */
	public static int SCREEN_WIDTH = 0;

	/**
	 * 屏幕高度(竖屏)
	 */
	public static int SCREEN_HEIGHT = 0;

	/**
	 * Bitmap所支持的最大值
	 */
	public static int BITMAP_MAX_SIZE = 4096;

	/**
	 * 线程池最大线程数
	 */
	public static int THREAD_POOL_MAX_SIZE = 10;

	/**
	 * 表明当前设备是否是Pad
	 */
	public static boolean PROGRAM_IS_PAD = false;

	/**
	 * 用来记录聊天界面是否已经被打开
	 */
	public static boolean CHAT_INTERFACE_OPEN = false;

	/**
	 * 登陆的时候获取的服务器时间
	 */
	public static long LONGIN_SERVER_TIME = 0;

	/**
	 * 登陆的时候记录的本地时间
	 */
	public static long LONGIN_LOCAL_TIME = 0;

	/**
	 * 默认的手机存储路径
	 */
	public static String DEFAULT_GLOBLE_PATH = "";

	/**
	 * 默认的sd卡存储路径
	 */
	public static String SDCARD_GLOBLE_PATH = "";

	/**
	 * 登陆用户的ID
	 */
	public static String LOGIN_USER_ID = "";

	/**
	 * 传输文件的同时最大限制
	 */
	public static final int MAX_TRANS_FILE_SIZE = 5;

	/**
	 * Get from Server , For distinguish different server
	 */
	public static String SERVER_DATABASE_ID = "";

	/**
	 * 中文及全角字符和汉字
	 */
	public static String CHINESE_REGEX = "[\u4e00-\u9fa5\u3000-\u301e\ufe10-\ufe19\ufe30-\ufe44\ufe50-\ufe6b\uff01-\uffee]";

	public static HashMap<String, String> CHINESE_CHAR_ARRAY = new HashMap<>();

	private static SparseArray<EmojiWraper> EMOJI_ARRAY = new SparseArray<>();

	public static int[] GLOBAL_FACE_ARRAY = new int[]{0, R.drawable.face_1,
			R.drawable.face_2, R.drawable.face_3, R.drawable.face_4,
			R.drawable.face_5, R.drawable.face_6, R.drawable.face_7,
			R.drawable.face_8, R.drawable.face_9, R.drawable.face_10,
			R.drawable.face_11, R.drawable.face_12, R.drawable.face_13,
			R.drawable.face_14, R.drawable.face_15, R.drawable.face_16,
			R.drawable.face_17, R.drawable.face_18, R.drawable.face_19,
			R.drawable.face_20, R.drawable.face_21, R.drawable.face_22,
			R.drawable.face_23, R.drawable.face_24, R.drawable.face_25,
			R.drawable.face_26, R.drawable.face_27, R.drawable.face_28,
			R.drawable.face_29, R.drawable.face_30, R.drawable.face_31,
			R.drawable.face_32, R.drawable.face_33, R.drawable.face_34,
			R.drawable.face_35, R.drawable.face_36, R.drawable.face_37,
			R.drawable.face_38, R.drawable.face_39, R.drawable.face_40,
			R.drawable.face_41, R.drawable.face_42, R.drawable.face_43,
			R.drawable.face_44, R.drawable.face_45};

	private static VideoCaptureDevInfo videoCaptureDevInfo;

	static {
		String preFix = "/:";
		String suffFix = ":/";
		for (int i = 1; i < GLOBAL_FACE_ARRAY.length; i++) {
			char c = (char) i;
			if (c == '\n') {
				c += 100;
			}
			c += 100;
			EMOJI_ARRAY.put(GLOBAL_FACE_ARRAY[i], new EmojiWraper(preFix + c
					+ suffFix, GLOBAL_FACE_ARRAY[i]));
		}
	}

	public static void saveLogoutFlag(Context context) {
		LocalSharedPreferencesStorage.putIntValue(context, KEY_LOGGED_IN, 0);
	}

	public static String getEmojiStrByIndex(int index) {
		if (index <= 0 || index >= GLOBAL_FACE_ARRAY.length) {
			return null;
		}
		EmojiWraper wrapper = EMOJI_ARRAY.get(GLOBAL_FACE_ARRAY[index]);
		if (wrapper != null) {
			return wrapper.emojiStr;
		}
		return null;
	}

	public static int getDrawableIndexByEmoji(String str) {
		for (int i = 1; i < GLOBAL_FACE_ARRAY.length; i++) {
			EmojiWraper wrapper = EMOJI_ARRAY.get(GLOBAL_FACE_ARRAY[i]);
			if (wrapper == null) {
				continue;
			}
			if (wrapper.emojiStr.equals(str)) {
				return i;
			}
		}
		return -1;
	}

	public static String getEmojiStr(int id) {
		EmojiWraper wrapper = EMOJI_ARRAY.get(id);
		if (wrapper != null) {
			return wrapper.emojiStr;
		}
		return null;
	}

	public static String getGlobalCrashPath(Context mContext) {
		String saveData;
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
		if (!sdExist) {
			// --data/data/com.v2tech
			saveData = mContext.getFilesDir().getAbsolutePath();
		} else {
			saveData = android.os.Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		}
		return saveData + File.separator + DATA_SAVE_FILE_NAME + "_XLog"
				+ File.separator;
	}

	public static String getGlobalRootPath() {
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
		if (!sdExist) {// 如果不存在,
			// --data/data/com.v2tech
			return DEFAULT_GLOBLE_PATH + File.separator + DATA_SAVE_FILE_NAME;
		} else {
			// --mnt/sdcard
			return SDCARD_GLOBLE_PATH + File.separator + DATA_SAVE_FILE_NAME;
		}
	}

	public static String getGlobalPath() {
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
		if (!sdExist) {// 如果不存在,
			// --data/data/com.v2tech
			return DEFAULT_GLOBLE_PATH + File.separator + DATA_SAVE_FILE_NAME
					+ File.separator + SERVER_DATABASE_ID;
		} else {
			// --mnt/sdcard
			return SDCARD_GLOBLE_PATH + File.separator + DATA_SAVE_FILE_NAME
					+ File.separator + SERVER_DATABASE_ID;
		}
	}

	public static String getGlobalUserPath() {
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
		if (!sdExist) {// 如果不存在,
			// --data/data/com.v2tech
			return DEFAULT_GLOBLE_PATH + File.separator + DATA_SAVE_FILE_NAME
					+ File.separator + SERVER_DATABASE_ID + File.separator
					+ "Users" + File.separator + LOGIN_USER_ID;
		} else {
			// --mnt/sdcard
			return SDCARD_GLOBLE_PATH + File.separator + DATA_SAVE_FILE_NAME
					+ File.separator + SERVER_DATABASE_ID + File.separator
					+ "Users" + File.separator + LOGIN_USER_ID;
		}
	}

	public static String getGlobalUserAvatarPath() {
		return getGlobalPath() + File.separator + "Users" + File.separator
				+ LOGIN_USER_ID + File.separator + "avatars";
	}

	public static String getGlobalPicsPath() {
		return getGlobalPath() + File.separator + "Users" + File.separator
				+ LOGIN_USER_ID + File.separator + "Images";
	}

	public static String getGlobalAudioPath() {
		return getGlobalPath() + File.separator + "Users" + File.separator
				+ LOGIN_USER_ID + File.separator + "audios";
	}

	public static String getGlobalFilePath() {
		return getGlobalPath() + File.separator + "Users" + File.separator
				+ LOGIN_USER_ID + File.separator + "files";
	}

	public static String getGlobalDataBasePath() {
		return getGlobalPath() + File.separator + "Users" + File.separator
				+ LOGIN_USER_ID;
	}

	public static long getGlobalServerTime() {
		return (SystemClock.elapsedRealtime() - GlobalConfig.LONGIN_LOCAL_TIME + GlobalConfig.LONGIN_SERVER_TIME * 1000);
	}

	public static int getAdapterScreenWidth() {
		if (PROGRAM_IS_PAD || GlobalHolder.getInstance().isInMeeting()) {
			return SCREEN_HEIGHT;
		} else {
			return SCREEN_WIDTH;
		}
	}

	public static int getAdapterScreenHeight() {
		if (PROGRAM_IS_PAD || GlobalHolder.getInstance().isInMeeting()) {
			return SCREEN_WIDTH;
		} else {
			return SCREEN_HEIGHT;
		}
	}

	public static void recordLoginTime(long serverTime) {
		GlobalConfig.LONGIN_LOCAL_TIME = SystemClock.elapsedRealtime();
		GlobalConfig.LONGIN_SERVER_TIME = serverTime;
	}

	public static int getLruCacheMaxSize() {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		return maxMemory / 8;
	}

	public static int getDisplayRotation(Activity mActivity) {
		if (Build.VERSION.SDK_INT > 7) {
			int rotation = mActivity.getWindowManager().getDefaultDisplay()
					.getRotation();
			switch (rotation) {
				case Surface.ROTATION_0:
					return 0;
				case Surface.ROTATION_90:
					return 90;
				case Surface.ROTATION_180:
					return 180;
				case Surface.ROTATION_270:
					return 270;
			}
		}
		return 0;
	}

	/**
	 * 创建必须存在的默认配置文件
	 */
	public static void initConfigFile(boolean isDelete) {
		File optionsFile = new File(GlobalConfig.getGlobalRootPath(),
				GlobalConfig.DEFAULT_CONFIG_LOG_FILE);
		if (!optionsFile.exists()) {
			if (isDelete) {
				boolean delete = optionsFile.delete();
				if (delete) {
					V2Log.i("MainApplication",
							"Delete DEFAULT_CONFIG_LOG_FILE folder that name is 'log_options' successfully!");
				} else {
					V2Log.i("MainApplication",
							"Delete DEFAULT_CONFIG_LOG_FILE folder that name is 'log_options' failed!");
				}
			}

			File temp = new File(GlobalConfig.getGlobalRootPath(),
					GlobalConfig.DATA_SAVE_FILE_NAME + "_"
							+ System.currentTimeMillis());
			try {
				temp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (temp.exists()) {
				temp.renameTo(optionsFile);
				V2Log.i("MainApplication",
						"Create DEFAULT_CONFIG_LOG_FILE folder that name is 'log_options' successfully!");
			} else {
				V2Log.i("MainApplication",
						"Create DEFAULT_CONFIG_LOG_FILE folder that name is 'log_options' failed!");
			}
		}

		String content = "<xml><path>log</path><v2platform><outputdebugstring>0</outputdebugstring>"
				+ "<level>5</level><basename>v2platform</basename><path>log</path><size>1024</size></v2platform></xml>";
		OutputStream os = null;
		try {
			os = new FileOutputStream(optionsFile);
			os.write(content.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		File cfgFile = new File(GlobalConfig.getGlobalRootPath(),
				GlobalConfig.DEFAULT_CONFIG_FILE);
		String contentCFG = "<v2platform><C2SProxy><ipv4 value=''/><tcpport value=''/></C2SProxy></v2platform>";
		if (!cfgFile.exists()) {
			if (isDelete) {
				boolean delete = cfgFile.delete();
				if (delete) {
					V2Log.i("MainApplication",
							"Delete DEFAULT_CONFIG_FILE folder that name is 'v2platform' successfully!");
				} else {
					V2Log.i("MainApplication",
							"Delete DEFAULT_CONFIG_FILE folder that name is 'v2platform' failed!");
				}
			}

			File cfgFileTemp = new File(GlobalConfig.getGlobalRootPath(),
					GlobalConfig.DEFAULT_CONFIG_FILE + "_"
							+ System.currentTimeMillis());
			try {
				cfgFileTemp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (cfgFileTemp.exists()) {
				cfgFileTemp.renameTo(optionsFile);
				V2Log.i("MainApplication",
						"Create DEFAULT_CONFIG_FILE folder that name is 'v2platform' successfully!");
			} else {
				V2Log.i("MainApplication",
						"Create DEFAULT_CONFIG_FILE folder that name is 'v2platform' failed!");
			}
		}

		OutputStream os1 = null;
		try {
			os1 = new FileOutputStream(cfgFile);
			os1.write(contentCFG.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os1 != null) {
				try {
					os1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void initConfigLogFile() {
		// 创建日志文件文件夹
		String logRootDir = getGlobalCrashPath(APPLICATION_CONTEXT);
		String LOG_DIR_NAME = DATA_SAVE_FILE_NAME + "_XLog";
		String logDir = logRootDir + File.separator + LOG_DIR_NAME;
		File target = new File(logDir);
		if (target.exists()) {
			V2Log.e(MainApplication.class.getSimpleName(),
					"v2tech log folder already exist , : "
							+ target.getAbsolutePath());
		} else {
			File temp = new File(logRootDir, LOG_DIR_NAME + "_"
					+ System.currentTimeMillis());
			temp.mkdirs();
			if (temp.exists()) {
				temp.renameTo(target);
			} else {
				V2Log.e(MainApplication.class.getSimpleName(),
						"Create folder that name is 'v2tech' failed! The application can't run!");
			}
		}
	}

	public static void setGlobalVideoLevel(Context mContext, int videoLevel) {
		//VideoPlayer.EnableMediaCodec(true);
		//VideoEncoder.EnableMediaCodec(false);
		int videoWidth = 0;
		int videoHeight = 0;
		int videoFps = 20;
		int videoBitrate = 5;
		int level = V2GlobalConstants.CONF_CAMERA_MASS_LOW;
		switch (videoLevel) {
			case V2GlobalConstants.CONF_CAMERA_MASS_LOW:

				videoFps = 15;
				videoWidth = 768;
				videoHeight = 432;
				videoBitrate = 512;
				level = V2GlobalConstants.CONF_CAMERA_MASS_LOW;
				break;
			case V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE:
				videoWidth = 320;
				videoHeight = 240;
				videoBitrate = 128;
				level = V2GlobalConstants.CONF_CAMERA_MASS_MIDDLE;
				break;
			case V2GlobalConstants.CONF_CAMERA_MASS_HIGH:
				videoFps = 20;
				videoWidth = 768;
				videoHeight = 432;
				videoBitrate = 1024;
				level = V2GlobalConstants.CONF_CAMERA_MASS_HIGH;
				break;
			case V2GlobalConstants.CONF_CAMERA_CUSTOMER:
				setPBeans beans = MainApplication.getP();
				if (beans != null) {
					videoWidth = beans.getWid();
					videoHeight = beans.getHei();
					videoBitrate = beans.getMalv();
					videoFps = beans.getZhenlv();
					if (beans.getF1() == 1) {
						VideoEncoder.EnableMediaCodec(true);//false 软编   true 硬编
					} else {
						VideoEncoder.EnableMediaCodec(false);
					}
					if (beans.getF2() == 1) {
						VideoPlayer.EnableMediaCodec(false);
					} else {
						VideoPlayer.EnableMediaCodec(false);
					}
				} else {
					videoFps = 15;
					videoWidth = 1280;
					videoHeight = 720;
					videoBitrate = 1024;
					setPBeans mBeans = new setPBeans(videoBitrate, videoFps, videoWidth, videoHeight, 1, 1);
					MainApplication.setP(mBeans);
				}
				level = V2GlobalConstants.CONF_CAMERA_MASS_HIGH;
				break;
			default:
				break;
		}
		long currentUserId = GlobalHolder.getInstance().getCurrentUserId();
		LocalSharedPreferencesStorage.putIntValue(mContext,
				String.valueOf(currentUserId) + ":videoMass", level);
		videoCaptureDevInfo = VideoCaptureDevInfo.CreateVideoCaptureDevInfo();
		if (videoCaptureDevInfo != null) {
			videoCaptureDevInfo.SetCapParams(
					videoWidth, videoHeight, videoBitrate, videoFps,
					ImageFormat.NV21);
		}
	}

	public static CapParams geVideoCapParams() {
		return videoCaptureDevInfo.GetCapParams();

	}

	@SuppressWarnings("deprecation")
	public static void setVideoRecordParams(Activity mContext,
											SurfaceHolder surfaceHolder) {
		VideoRecorder.VideoPreviewSurfaceHolder = surfaceHolder;
		VideoRecorder.VideoPreviewSurfaceHolder
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		VideoRecorder.mIsScreenOriatationPortrait = mContext.getResources()
//				.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		VideoRecorder.mDisplay = mContext.getWindow().getWindowManager().getDefaultDisplay();
	}

	public static void startP2PConnectChatByJNI(Context mContext,
												int mConnectType, long mRemoteChatUserID, boolean isInComing,
												String sessionID, String deviceID, String sipNumber,String data) {
		startP2PConnectChatAll(mContext, mConnectType, mRemoteChatUserID,
				isInComing, sessionID, deviceID, sipNumber, data);
	}

	public static void startP2PConnectChat(Context mContext, int mConnectType,
										   long mRemoteChatUserID, boolean isInComing, String sessionID,
										   String sipNumber) {
		startP2PConnectChatAll(mContext, mConnectType, mRemoteChatUserID,
				isInComing, sessionID, "", sipNumber,null);
	}

	public static void startP2PConnectChatAll(Context mContext,
											  int mConnectType, long mRemoteChatUserID, boolean isInComing,
											  String sessionID, String deviceID, String sipNumber,String data) {
		if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
			return;
		}

		if (mConnectType == ConversationP2PAVActivity.P2P_CONNECT_AUDIO
				|| mConnectType == ConversationP2PAVActivity.P2P_CONNECT_SIP) {
			GlobalHolder.getInstance().setAudioState(true, mRemoteChatUserID);
		} else if (mConnectType == ConversationP2PAVActivity.P2P_CONNECT_VIDEO) {
			GlobalHolder.getInstance().setVideoState(true, mRemoteChatUserID);
		}

		Intent iv = new Intent();
		iv.addCategory(PublicIntent.DEFAULT_CATEGORY);
		iv.setAction(PublicIntent.START_P2P_CONVERSACTION_ACTIVITY);
		iv.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		iv.putExtra("uid", mRemoteChatUserID);
		iv.putExtra("is_coming_call", isInComing);
		iv.putExtra("data", data);
		if (sessionID != null) {
			iv.putExtra("sessionID", sessionID);
		}

		if (mConnectType == ConversationP2PAVActivity.P2P_CONNECT_VIDEO) {
			iv.putExtra("voice", false);
			UserDeviceConfig udc = GlobalHolder.getInstance()
					.getUserDefaultDevice(mRemoteChatUserID);
			if (udc != null) {
				iv.putExtra("device", udc.getDeviceID());
			} else {
				iv.putExtra("device", deviceID);
			}
		} else {
			if (mConnectType == ConversationP2PAVActivity.P2P_CONNECT_SIP) {
				iv.putExtra("sipNumber", sipNumber);
				iv.putExtra("sip", true);
			}
			iv.putExtra("voice", true);
		}
		mContext.startActivity(iv);
	}

	public static void recursionDeleteOlderFiles(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File temp = files[i];
				if (temp.exists()) {
					if (temp.isDirectory()) {
						recursionDeleteOlderFiles(temp);
					}

					boolean delete = temp.delete();
					V2Log.i("MainApplication", "文件 - " + temp.getAbsolutePath()
							+ " - 删除是否成功  : " + delete);
				}
			}
		}
	}

	static class EmojiWraper {
		String emojiStr;
		int id;

		public EmojiWraper(String emojiStr, int id) {
			super();
			this.emojiStr = emojiStr;
			this.id = id;
		}

	}

	public static class Resource {
		public static String CONTACT_DEFAULT_GROUP_NAME = "";
		public static String MIX_VIDEO_DEFAULT_NAME = "";
	}
}
