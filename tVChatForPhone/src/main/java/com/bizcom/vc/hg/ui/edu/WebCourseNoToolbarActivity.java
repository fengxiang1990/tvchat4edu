package com.bizcom.vc.hg.ui.edu;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by admin on 2017/1/16.
 */

public class WebCourseNoToolbarActivity extends WebActivity {

    Unbinder unbinder;


    @BindView(R.id.webview)
    BridgeWebView webview;

    @BindView(R.id.imgview)
    ImageView imgview;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.course_fragment);
        unbinder = ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra(CourseConfig.WEB_TYPE, 0);
        switch (type) {
            case CourseConfig.ADD_COURSE:
                webview.loadUrl(CourseConfig.ADD_COURSE_URL);
                break;
        }
        url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            webview.loadUrl(url);
        }
    }


    @Override
    protected BridgeWebView getWebView() {
        return webview;
    }

    @Override
    protected ImageView getImageView() {
        return imgview;
    }
}
