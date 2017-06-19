package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.shdx.tvchat.phone.R;

public class AboutUsActivity extends Activity implements DownloadListener {

    String tag = "AboutUsActivity";
    private AboutUsActivity mContext;
    private WebView mWeb;
    private TextView vCodeText;
    private ImageView img_back;
    private TextView text_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hg_activity_about_us);
        mContext = this;
        initView();
    }


    private String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {

        }

        return version;
    }

    private void initView() {
        img_back = (ImageView) findViewById(R.id.img_back);
        text_title = (TextView) findViewById(R.id.text_title);
        String webUrl = getIntent().getStringExtra("webUrl");
        String titleText = getIntent().getStringExtra("titleText");
        text_title.setText(titleText);
        vCodeText = (TextView) findViewById(R.id.vCodeText);
        vCodeText.setText("当前版本: V" + getVersionName());
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        if(TextUtils.equals(webUrl, LinkInfo.HELP_CODE)){
//            vCodeText.setVisibility(View.VISIBLE);
//        }else{
//            vCodeText.setVisibility(View.GONE);
//        }

        mWeb = (WebView) findViewById(R.id.mWeb);
        mWeb.getSettings().setJavaScriptEnabled(true);// 可用JS
        mWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWeb.setDownloadListener(this);
//		mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

                return true;
            }
        });


        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tmast")) {
                    Log.d(tag, "url-->" + url);
                    if (isPkgInstalled("com.tencent.android.qqdownloader")) {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
//                WaitDialogBuilder.showNormalWithHintProgress(mContext,
//                        getResources().getString(R.string.loding_progress), false);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                WaitDialogBuilder.showNormalWithHintProgress(mContext,
//                        getResources().getString(R.string.loding_progress), true);

            }
        });

        mWeb.loadUrl(webUrl);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 解决webview跳转到其他页面不能返回的问题
        if (mWeb.canGoBack()) {
            mWeb.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (mWeb != null) {
                mWeb.clearHistory();
                mWeb.removeAllViews();
                mWeb = null;
            }
        } catch (Exception e) {
        }

    }

    public boolean isPkgInstalled(String packageName) {

        if (packageName == null || "".equals(packageName))
            return false;
        android.content.pm.ApplicationInfo info = null;
        try {
            info = getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }


    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
