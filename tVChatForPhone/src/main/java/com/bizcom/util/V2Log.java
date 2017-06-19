package com.bizcom.util;

import android.util.Log;

import com.bizcom.util.xlog.XLog;

/**
 * TODO add file log output
 *
 * @author 28851274
 */
public class V2Log {

    public static boolean IS_DEBUG = true;
    public static final String TAG = "V2TECH";
    // callback log tag
    public static final String JNI_CALLBACK = "JNI_CALLBACK";
    public static final String JNI_REQUEST = "JNI_REQUEST";
    public static final String JNISERVICE_CALLBACK = "JNISERVICE_CALLBACK";
    public static final String SERVICE_CALLBACK = "SERVICE_CALLBACK";
    public static final String UI_MESSAGE = "UI_MESSAGE";
    public static final String UI_BROADCAST = "UI_BROADCAST";
    public static final String UI_V2AV = "V2AV";

    // xml 解析 log
    public static final String XML_ERROR = "XML_ERROR";

    public static boolean isDebuggable = false;

    // 使用默认标签log
    public static void i(String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.d(TAG, msg);
    }

    public static void w(String msg) {
        V2Log.w(TAG, msg);
    }

    public static void e(String msg) {
        V2Log.e(TAG, "[V2-TECH-ERROR]" + msg);
    }

    // 正常log
    public static void i(String tag, String msg) {
        if (!IS_DEBUG) {
            return;
        }

        Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (!IS_DEBUG) {
            return;
        }

        Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void jniCall(String methodName, String content) {
        Log.i(V2Log.JNI_CALLBACK, " METHOD = " + methodName + " --> " + content);
    }

    public static void uiCall(String className, String content) {
        Log.i(V2Log.UI_MESSAGE, " CLASS = " + className + "  MESSAGE = " + content);
    }

    public static void serviceCall(String className, String content) {
        Log.i(V2Log.SERVICE_CALLBACK, " className + --> " + "content");
    }

    // 带异常的log
    public static void e(String tag, String msg, Exception e) {
        Log.e(tag, msg, e);
    }
}
