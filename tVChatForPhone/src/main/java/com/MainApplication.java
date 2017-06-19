package com;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.V2.jni.AppShareRequest;
import com.V2.jni.AudioRequest;
import com.V2.jni.ChatRequest;
import com.V2.jni.ConfRequest;
import com.V2.jni.FileRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.ImRequest;
import com.V2.jni.InteractionRequest;
import com.V2.jni.NativeInitializer;
import com.V2.jni.SipRequest;
import com.V2.jni.VideoMixerRequest;
import com.V2.jni.VideoRequest;
import com.V2.jni.WBRequest;
import com.V2.jni.WebManagerRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.RequestQueue;
import com.bizcom.db.V2techBaseProvider;
import com.bizcom.db.provider.DatabaseProvider;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.V2ImRequest;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.ListPageUtil;
import com.bizcom.util.LogRecorder;
import com.bizcom.util.Notificator;
import com.bizcom.util.StorageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.LoginActivity;
import com.bizcom.vc.activity.SplashActivity;
import com.bizcom.vc.activity.main.MainActivity;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.beans.setPBeans;
import com.bizcom.vc.hg.util.GetPhoneNumber;
import com.bizcom.vc.hg.util.PhoneInfo;
import com.bizcom.vc.hg.web.MVolley;
import com.bizcom.vc.hg.web.Web;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DaoConfig;
import com.lidroid.xutils.exception.DbException;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.shdx.tvchat.phone.BuildConfig;
import com.shdx.tvchat.phone.R;
import com.tencent.bugly.crashreport.CrashReport;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.ShareSDK;

//import cn.jpush.android.api.JPushInterface;

public class MainApplication extends Application {

    public static Display mDisplay = null;
    public static boolean hasDataPrepared = false;// 判断 好友列表是否加载完成
    private static final String TAG = MainApplication.class.getSimpleName();
    private Vector<WeakReference<Activity>> list = new Vector<>();
    private LocalActivityLifecycleCallBack callback;
    private boolean needCopy;
    private int startedActivityCount = 0;
    private boolean isAlreadyStart;
    public static boolean isAlreadLogin;
    private LogRecorder mLogRecorder;
    private static DbUtils mDbUtils;
    private static setPBeans b;
    public static boolean isLogout = false;
    public static TvInfoBeans mTvInfoBean;
    // 初始化数据是否加载完成
    public static boolean IsInitDataLoadingFinish = false;

    public static DisplayImageOptions imgOptions;

    /**
     * 通讯录数据
     */
    public static List<PhoneFriendItem> SourceDateList = new ArrayList<PhoneFriendItem>();
    /***
     * 通讯录中在tv聊注册过的用户数据
     */
    public static List<User> mContactUserList = new ArrayList<User>();
    /**
     * 最近通话数据
     */
    public static List<User> mLatelyUserList = new ArrayList<User>();

    public static RequestQueue queue;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        V2Log.IS_DEBUG = true;
        queue = MVolley.newRequestQueue(this);
        ZXingLibrary.initDisplayOpinion(this);
        ShareSDK.initSDK(getApplicationContext());
        Web.init(getApplicationContext());
        initDatabse();
        isAlreadyStart = false;
        GlobalConfig.PROGRAM_IS_PAD = getResources().getConfiguration().smallestScreenWidthDp >= 600;
        PublicIntent.DEFAULT_CATEGORY = getPackageName();
        initGloblePath();
        initConfigSP();
        Fresco.initialize(this);
        try {
            GlobalConfig.GLOBAL_VERSION_NAME = this.getPackageManager().getPackageInfo(this.getPackageName(),
                    0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        initBugLy();
        initImageLoader();
        initHZPYDBFile();
        initDPI();
        initResource();
        DatabaseProvider.init(getApplicationContext());
        GlobalConfig.APPLICATION_CONTEXT = getApplicationContext();
        // Load native library
        System.loadLibrary("event");
        System.loadLibrary("v2vi");
        System.loadLibrary("v2ve");
        System.loadLibrary("v2client");
        JPushInterface.setDebugMode(false); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this); // 初始化 JPush
        V2Log.i(TAG, "SAVE GLOBAL PATH : " + GlobalConfig.getGlobalPath());
        // Initialize native library
        NativeInitializer.getIntance().initialize(getApplicationContext(), GlobalConfig.getGlobalPath());
        // // Start service
        startService(new Intent(this, JNIService.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            callback = new LocalActivityLifecycleCallBack();
            registerActivityLifecycleCallbacks(callback);
        } else {
            Toast.makeText(getApplicationContext(), "System version is too low, can't be started", Toast.LENGTH_LONG)
                    .show();
            uninitForExitProcess();
        }
    }

    /**
     * 初始化imageloader
     */
    public void initImageLoader() {

        File cacheDir = StorageUtils.getOwnCacheDirectory(this, "tvchatImg/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).memoryCacheExtraOptions(150, 150) // maxwidth,
                // max
                // height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3)// 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // 可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024).discCacheSize(50 * 1024 * 1024) // 50M
                .tasksProcessingOrder(QueueProcessingType.LIFO).discCacheFileCount(100) // 缓存的文件数量
                .diskCache(new UnlimitedDiskCache(cacheDir))// 自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout
                // (5
                // s),
                // readTimeout
                // (30
                // s)超时时间
                .writeDebugLogs() // Remove for releaseapp
                .build();// 开始构建
        ImageLoader.getInstance().init(config);

        imgOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar)
                .showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(true)
                .considerExifParams(true).displayer(new RoundedBitmapDisplayer(360))
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).cacheInMemory(false) // default
                // 设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default 设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(imgOptions).memoryCache(new WeakMemoryCache());
    }

    /**
     * 创建SharedPreferences配置文件
     */
    private void initConfigSP() {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        Editor ed = sp.edit();
        ed.putInt("LoggedIn", 0);
        ed.apply();

        boolean isAppFirstLoad = sp.getBoolean("isAppFirstLoad", true);
        if (isAppFirstLoad) {
            File file = new File(GlobalConfig.getGlobalRootPath());
            if (file.exists()) {
                GlobalConfig.recursionDeleteOlderFiles(file);
            }

            Editor editor = sp.edit();
            editor.putBoolean("isAppFirstLoad", false);
            editor.apply();
        }

        GlobalConfig.initConfigFile(false);
    }

    /**
     * 初始化一些默认资源的名字
     */
    private void initResource() {
        Resources resources = getApplicationContext().getResources();
        GlobalConfig.Resource.CONTACT_DEFAULT_GROUP_NAME = resources.getText(R.string.contacts_default_group_name)
                .toString();
        GlobalConfig.Resource.MIX_VIDEO_DEFAULT_NAME = this.getApplicationContext().getResources()
                .getText(R.string.vo_attendee_mixed_device_mix_video).toString();
    }

    /**
     * 初始化程序数据存储目录
     */
    private void initGloblePath() {
        boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
        if (!sdExist) {
            // --data/data/com.v2tech
            boolean isCreated = createSaveDir(getApplicationContext().getFilesDir().getParent());
            if (isCreated) {
                GlobalConfig.DEFAULT_GLOBLE_PATH = getApplicationContext().getFilesDir().getParent();
                V2Log.i(TAG, "SD can't be used , save path is ：" + GlobalConfig.DEFAULT_GLOBLE_PATH);
            } else {
                V2Log.e(TAG, "Programm Will Not Live , Save Dir Create All Failed!");
            }
        } else {
            File sdRoot = getApplicationContext().getExternalFilesDir(null);
            if (sdRoot == null) {
                boolean isCreated = createSaveDir(StorageUtil.getAbsoluteSdcardPath());
                if (isCreated) {
                    GlobalConfig.SDCARD_GLOBLE_PATH = StorageUtil.getAbsoluteSdcardPath();
                    V2Log.i(TAG, "SD can use , save path is ：" + GlobalConfig.SDCARD_GLOBLE_PATH);
                } else {
                    V2Log.i(TAG, "SD can use , but create dir failed! change to mobile self memory");
                    boolean isCreateMemory = createSaveDir(getApplicationContext().getFilesDir().getParent());
                    if (isCreateMemory) {
                        GlobalConfig.DEFAULT_GLOBLE_PATH = getApplicationContext().getFilesDir().getParent();
                        V2Log.i(TAG, "save path is ：" + GlobalConfig.DEFAULT_GLOBLE_PATH);
                    } else {
                        V2Log.e(TAG, "Programm Will Not Live , Save Dir Create All Failed!");
                    }
                }
            } else {
                boolean isCreated = createSaveDir(sdRoot.getAbsolutePath());
                if (isCreated) {
                    GlobalConfig.SDCARD_GLOBLE_PATH = sdRoot.getAbsolutePath();
                    V2Log.i(TAG, "SD Android Dir can use , save path is ：" + GlobalConfig.SDCARD_GLOBLE_PATH);
                } else {
                    V2Log.i(TAG, "SD Android Dir can use , but create dir failed! change to SD memory");
                    boolean isCreatedSD = createSaveDir(StorageUtil.getAbsoluteSdcardPath());
                    if (isCreatedSD) {
                        GlobalConfig.SDCARD_GLOBLE_PATH = StorageUtil.getAbsoluteSdcardPath();
                        V2Log.i(TAG, "save path is ：" + GlobalConfig.SDCARD_GLOBLE_PATH);
                    } else {
                        V2Log.i(TAG, "SD can use , but create dir failed! change to mobile self memory");
                        boolean isCreateMemory = createSaveDir(getApplicationContext().getFilesDir().getParent());
                        if (isCreateMemory) {
                            GlobalConfig.DEFAULT_GLOBLE_PATH = getApplicationContext().getFilesDir().getParent();
                            V2Log.i(TAG, "save path is ：" + GlobalConfig.DEFAULT_GLOBLE_PATH);
                        } else {
                            V2Log.e(TAG, "Programm Will Not Live , Save Dir Create All Failed!");
                        }
                    }
                }
            }
        }
    }

    private boolean createSaveDir(String path) {
        // 创建数据文件夹，如果不成功则程序不能正常运行！
        boolean isExistRootDir = createDefSaveDir(path);
        if (isExistRootDir) {
            boolean isExistTestFile = createTestFile(path);
            if (isExistTestFile) {
                return true;
            }
        }
        return false;
    }

    private boolean createDefSaveDir(String path) {
        File target = new File(path, GlobalConfig.DATA_SAVE_FILE_NAME);
        if (!target.exists()) {
            File temp = new File(path, GlobalConfig.DATA_SAVE_FILE_NAME + "_" + System.currentTimeMillis());
            boolean isCreateDir = temp.mkdirs();
            if (isCreateDir && temp.exists()) {
                boolean isRename = temp.renameTo(target);
                if (isRename) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean createTestFile(String path) {
        String targetPath = path + File.separator + GlobalConfig.DATA_SAVE_FILE_NAME;
        File temp = new File(targetPath, GlobalConfig.DATA_SAVE_FILE_NAME + "_" + System.currentTimeMillis());
        try {
            boolean isTestCreate = temp.createNewFile();
            if (isTestCreate && temp.exists()) {
                temp.delete();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 初始化搜索用到的hzpy.db文件
     */
    private void initHZPYDBFile() {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            // 获得.db文件的绝对路径 data/data/com.v2tech/databases
            String DATABASE_FILENAME = "hzpy.db";
            String parent = getDatabasePath(DATABASE_FILENAME).getParent();
            File dir = new File(parent);
            // 如果目录不存在，创建这个目录
            if (!dir.exists())
                dir.mkdir();
            String databaseFilename = getDatabasePath(DATABASE_FILENAME).getPath();
            File file = new File(databaseFilename);
            // 目录中不存在 .db文件，则从res\raw目录中复制这个文件到该目录
            if (file.exists()) {
                is = getResources().openRawResource(R.raw.hzpy);
                if (is == null) {
                    V2Log.e("readed sqlite file failed... inputStream is null");
                    return;
                }
                String md5 = AlgorithmUtil.getFileMD5(is);
                String currentMD5 = AlgorithmUtil.getFileMD5(new FileInputStream(file));
                needCopy = !md5.equals(currentMD5);
            }

            if (!(file.exists()) || needCopy) {
                // 获得封装.db文件的InputStream对象
                is = getResources().openRawResource(R.raw.hzpy);
                if (is == null) {
                    V2Log.e("readed sqlite file failed... inputStream is null");
                    return;
                }
                fos = new FileOutputStream(databaseFilename);
                byte[] buffer = new byte[1024];
                int count = 0;
                // 开始复制.db文件
                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
            V2Log.e("loading HZPY.db SQListe");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            needCopy = false;
        }
    }

    private void initDPI() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        GlobalConfig.GLOBAL_DENSITY_LEVEL = metrics.densityDpi;
        GlobalConfig.GLOBAL_DENSITY_VALUE = metrics.density;
        GlobalConfig.SCREEN_WIDTH = metrics.widthPixels;
        GlobalConfig.SCREEN_HEIGHT = metrics.heightPixels;
        V2Log.i(TAG, "Init user device DPI LEVEL: " + GlobalConfig.GLOBAL_DENSITY_LEVEL + " and DPI VALUE : "
                + GlobalConfig.GLOBAL_DENSITY_VALUE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        GlobalConfig.SCREEN_INCHES = Math.sqrt(x + y);
    }

    @Override
    public void onLowMemory() {
        // 后台进程已经被全部回收，但系统内存还是低
        V2Log.e(TAG, "MainApplication.onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        V2Log.e(TAG, "onTrimMemory called , level is : " + level);
        switch (level) {
            case Application.TRIM_MEMORY_RUNNING_MODERATE:
                break;
            case Application.TRIM_MEMORY_RUNNING_LOW:
                break;
            case Application.TRIM_MEMORY_RUNNING_CRITICAL:
                break;
            case Application.TRIM_MEMORY_UI_HIDDEN:
                break;
            case Application.TRIM_MEMORY_BACKGROUND:
                break;
            case Application.TRIM_MEMORY_MODERATE:
                break;
            case Application.TRIM_MEMORY_COMPLETE:// 下个被回收进程就是此进程
                break;
        }
        System.gc();
        super.onTrimMemory(level);
    }

    public void requestQuit() {
//        Handler mHandler = new Handler();
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });

        Intent it = new Intent(MainApplication.this, LoginActivity.class);
        it.putExtra("exist", true);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(it);

        for (int i = 0; i < list.size(); i++) {
            WeakReference<Activity> w = list.get(i);
            Object obj = w.get();
            if (obj != null) {
                Activity a = ((Activity) obj);
                Log.i("tvliao", "getName()-" + a.getClass().getName());
                if (!TextUtils.equals(a.getClass().getName(), LoginActivity.class.getName())) {
                    a.finish();
                }
            }
        }
    }

    public void uninitForExitProcess() {
        super.onTerminate();
        V2Log.d("uninitForExitProcess was call , isLogout : " + isLogout);
        GlobalConfig.saveLogoutFlag(getApplicationContext());
        if (V2techBaseProvider.mSQLitDatabaseHolder != null && V2techBaseProvider.mSQLitDatabaseHolder.isOpen()) {
            V2techBaseProvider.mSQLitDatabaseHolder.close();
        }

        if (isLogout) {
            GlobalConfig.isLogined = false;
        } else {
            if (mLogRecorder != null) {
                mLogRecorder.stop();
            }
            stopService(new Intent(this, JNIService.class));
            Notificator.cancelAllSystemNotification(getApplicationContext());
            ImRequest.getInstance().unInitialize();
            GroupRequest.getInstance().unInitialize();
            VideoRequest.getInstance().unInitialize();
            ConfRequest.getInstance().unInitialize();
            AudioRequest.getInstance().unInitialize();
            WBRequest.getInstance().unInitialize();
            ChatRequest.getInstance().unInitialize();
            VideoMixerRequest.getInstance().unInitialize();
            FileRequest.getInstance().unInitialize();
            SipRequest.getInstance().UnInitialize();
            AppShareRequest.getInstance().unInitialize();
            InteractionRequest.getInstance().unInitialize();
            WebManagerRequest.getInstance().unInitialize();
            unregisterActivityLifecycleCallbacks(callback);
            System.exit(0);
            System.gc();
        }
    }

    /**
     * This function is used to determine whether the program runs in the
     * background
     *
     * @return true mean running in the backgroup , otherwise false.
     */
    public boolean isRunningBackgound() {
        return startedActivityCount == 0;
    }

    public class LocalActivityLifecycleCallBack implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // if (!(activity instanceof SplashActivity)) {
            // if (GlobalConfig.PROGRAM_IS_PAD || activity instanceof
            // ConferenceActivity) {
            // activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // } else {
            // if (activity instanceof ConversationSelectImageActivity
            // && GlobalHolder.getInstance().isInMeeting()) {
            // activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // } else {
            // activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // }
            // }
            // }
            String saveState;
            if (savedInstanceState == null) {
                saveState = "true";
            } else {
                saveState = "false";
            }
            V2Log.d(TAG, "添加一个新的activity : " + activity.getClass().getName() + " | List Size : " + list.size()
                    + " | savedInstanceState : " + saveState);
            if (list.size() == 0 && savedInstanceState != null) {
                if (!activity.getClass().getSimpleName().equals(SplashActivity.class.getSimpleName())) {
                    V2Log.d(TAG, "检测到savedInstanceState携带有数据 , 销毁 : " + activity.getClass().getName());
                    // if (!isAlreadyStart) {
                    // Intent intent =
                    // getPackageManager().getLaunchIntentForPackage(getPackageName());
                    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // startActivity(intent);
                    // V2Log.d(TAG, "程序被系统杀死，开始重启");
                    // int pid = android.os.Process.myPid();
                    // android.os.Process.killProcess(pid);
                    // }

                    activity.finish();
                    if (activity.getClass().getSimpleName().equals(MainActivity.class.getSimpleName())) {
                        V2Log.d(TAG, "MainActivity isAlreadyStart : " + isAlreadyStart);
                        if (!isAlreadyStart) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            isAlreadyStart = true;
                            V2Log.d(TAG, "程序被系统杀死，开始重启");
                        }

                        V2Log.d(TAG, "结束程序");
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                }
            }
            list.add(0, new WeakReference<>(activity));
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            for (int i = 0; i < list.size(); i++) {
                WeakReference<Activity> w = list.get(i);
                Object obj = w.get();
                if (obj != null && obj == activity) {
//                    list.remove(i--);
                    V2Log.d(TAG, "销毁一个activity : " + activity.getClass().getName());
//                    WaitDialogBuilder.clearWaitDialog();
//                    DialogManager.getInstance().clearDialogObject();
//                    System.gc();
                }
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activity instanceof LoginActivity || activity instanceof SplashActivity) {
                return;
            }
            // 测试证明activity跳转时，先start后stop
            startedActivityCount++;
            if (startedActivityCount == 1) {
                Notificator.cancelAllSystemNotification(getApplicationContext());
            }

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity instanceof LoginActivity || activity instanceof SplashActivity) {
                return;
            }
            startedActivityCount--;
        }

    }

    private void initDatabse() {
        try {
            DaoConfig config = new DaoConfig(getApplicationContext());
            // 创建数据库的名称
            config.setDbName("WifiCheckDB");
            // 数据库的版本号
            config.setDbVersion(1);
            mDbUtils = DbUtils.create(config);
            mDbUtils.createTableIfNotExist(User.class);
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    public static DbUtils getDbUtils() {
        return mDbUtils;

    }

    public static void setP(setPBeans b1) {
        b = b1;
    }

    public static setPBeans getP() {
        return b;
    }

    public static void loadVoiceMediaData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AudioVideoMessageBean> result = MediaRecordProvider.loadMediaHistoriesMessage(GlobalHolder.getInstance().getCurrentUserId(),
                        AudioVideoMessageBean.TYPE_ALL);

                if (result == null) {
                    return;
                }

                for (AudioVideoMessageBean item : result) {
                    long userID = item.getRemoteUserID();
                    if (!GlobalHolder.getInstance().isFriend(userID)) {
                        V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, userID);
                    }
                }

            }
        }).start();
    }

    public static void loadContactFriend(Context context) {
        mContactUserList.clear();
        List<PhoneInfo> ml = GetPhoneNumber.getNumber(context);
        if (ml != null && !ml.isEmpty()) {
            ListPageUtil pageUtil = new ListPageUtil(ml, 50);
            for (int i = 0; i < pageUtil.getPageCount(); i++) {
                loadContactFriendPage(pageUtil.getPagedList(i + 1), context);
            }
        }
    }

    public static void loadContactFriendPage(List<PhoneInfo> ml, Context context) {
        StringBuilder sb = new StringBuilder().append("[");
        for (PhoneInfo mPhoneInfo : ml) {
            String number = mPhoneInfo.getNumber();
            number = number.replace(" ", "").replace("+86", "");
            sb.append("\"").append(number).append("\"").append(",");
        }
        if (TextUtils.equals(String.valueOf(sb.charAt(sb.length() - 1)), ",")) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        BussinessManger.getInstance(context).getContactFriends(sb.toString(), GlobalHolder.getInstance().getCurrentUserId(), new IBussinessManager.OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    JSONObject json = (JSONObject) obj;
                    JSONArray jsonArr = json.getJSONArray("data");
                    for (int i = 0; i < jsonArr.size(); i++) {
                        JSONObject jsonUser = jsonArr.getJSONObject(i);
                        long userid = jsonUser.getLong("id");
                        String account = jsonUser.getString("account");
                        String pic = jsonUser.getString("picurl");
                        String nickName = jsonUser.getString("nickname");
                        String commentName = jsonUser.getString("alias");
                        User u = new User(account, Long.parseLong("11" + userid), pic, nickName, commentName);
                        mContactUserList.add(u);
                    }
                    Log.i("tvliao", "onResponse-" + json.toString());

                    for (User u : mContactUserList) {
                        Log.i("tvliao", "mContactUserList-" + u.toString());
                    }
                }
            }
        });
    }

    public static void updateContactUserCommentName(long userId, String commentName) {
        for (User u : mContactUserList) {
            if (u.getmUserId() == userId) {
                u.setCommentName(commentName);
            }
        }
    }


    public void initBugLy() {
        // 获取当前包名
        String packageName = getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG);
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(this, "df7574d42e", true, strategy);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }


}
