package com.bizcom.vc.hg.util;

import java.io.File;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

import com.bizcom.util.FileUtils;

public class DownloadService extends Service {
    private DownloadManager dm;
    private long enqueue;
    private BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String dUrl = intent.getStringExtra("url");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/TV聊.apk")),
                            "application/vnd.android.package-archive");
                    startActivity(intent);
                    stopSelf();
                }
            };

            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            startDownload(dUrl);
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void startDownload(String url) {
//        File f = new File(Environment.DIRECTORY_DOWNLOADS, "TV聊.apk");
//        if (f.exists()) f.delete();
        // 下载前先尝试删除已下载过的apk文件
        FileUtils.deleteFiles(Environment.getExternalStorageDirectory() + "/download/TV聊.apk");

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));
        request.setMimeType("application/vnd.android.package-archive");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "TV聊.apk");
        enqueue = dm.enqueue(request);
    }
}