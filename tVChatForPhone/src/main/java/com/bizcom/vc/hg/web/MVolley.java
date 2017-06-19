package com.bizcom.vc.hg.web;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;

import java.io.File;

/**
 * Created by admin on 2016/12/17.
 */

public class MVolley {

    private static final String DEFAULT_CACHE_DIR = "MVolley";

    public MVolley() {
    }

    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, (HttpStack) null);
    }

    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cache;
        if (context.getCacheDir() == null) {
            cache = context.getExternalCacheDir();
        } else {
            cache = context.getCacheDir();
        }
        File cacheDir = new File(cache, "volley");
        String userAgent = "volley/0";

        try {
            String network = context.getPackageName();
            PackageInfo queue = context.getPackageManager().getPackageInfo(network, 0);
            userAgent = network + "/" + queue.versionCode;
        } catch (PackageManager.NameNotFoundException var6) {
            ;
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        BasicNetwork network1 = new BasicNetwork((HttpStack) stack);
        RequestQueue queue1 = new RequestQueue(new DiskBasedCache(cacheDir), network1);
        queue1.start();
        return queue1;
    }

}
