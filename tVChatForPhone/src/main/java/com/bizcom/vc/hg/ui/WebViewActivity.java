package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by fengxiang on 2016/12/19.
 */

public class WebViewActivity extends Activity {

    String tag = "WebViewActivity";
    Unbinder unbinder;

    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.webview)
    WebView webView;

    String url;
    long time1;

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_web);
        unbinder = ButterKnife.bind(this);
        text_title.setVisibility(View.GONE);
        url = getIntent().getStringExtra("url");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true); // 支持缩放

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(tag, "webView onPageStarted");
                time1 = System.currentTimeMillis();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(tag, "webView onPageFinished");
                long consume = System.currentTimeMillis() - time1;
                Log.d(tag, "load the page consume:" + consume + "ms");
            }
        });
        webView.loadUrl(url);
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }

}
