package com.bizcom.vc.hg.ui.edu.bridgehandler;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.widget.cus.CustomDialog;
import com.cgs.utils.ToastUtil;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.shdx.tvchat.phone.R;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

/**
 * Created by admin on 2017/1/16.
 */

public class ShareHandler implements BridgeHandler {

    String tag = "ShareHandler";


    Activity activity;

    public ShareHandler(Activity activity) {
        this.activity = activity;
    }

    String title;
    String url;
    String imgUrl;
    String desc;

    @Override
    public void handler(String data, CallBackFunction function) {
        Log.e(tag, "data from web-->" + data);
        if (!TextUtils.isEmpty(data)) {
            JSONObject jsonObject = JSON.parseObject(data);
            title = jsonObject.getString("title");
            url = jsonObject.getString("url");
            imgUrl = jsonObject.getString("imgUrl");
            desc = jsonObject.getString("desc");
        }
        showShareDialog();
    }


    CustomDialog mShareDialog;

    private void showShareDialog() {
        if (mShareDialog == null) {
            mShareDialog = new CustomDialog(activity, R.style.windStyle);
            mShareDialog.setContentView(R.layout.hg_share);
            Window window = mShareDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            View cannelButtonContent = mShareDialog.findViewById(R.id.cancel);
            View qqBt = mShareDialog.findViewById(R.id.shareToQQ);
            View wxBt = mShareDialog.findViewById(R.id.shareToWX);
            View linkBt = mShareDialog.findViewById(R.id.shareToLink);
            qqBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                    shareToQQ();
                }
            });

            wxBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                    shareToWx();
                }

            });
            linkBt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                    ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(url);
                    Toast.makeText(activity, "链接已复制到剪切板", Toast.LENGTH_SHORT).show();
                }
            });

            cannelButtonContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                }

            });
        }

        mShareDialog.show();
    }


    protected void shareToQQ() {
        cn.sharesdk.tencent.qq.QQ.ShareParams sp = new cn.sharesdk.tencent.qq.QQ.ShareParams();
//        sp.setTitle("来自TV聊的分享");
//        sp.setImageUrl("http://117.144.248.59/tvlLogo.png");
//        sp.setText("我正在TV聊app中向您发出聊天邀请，赶紧下载吧，和我一起体验TV视频聊天的乐趣及更多体验功能!");
//        sp.setTitleUrl(LinkInfo.H5_CODE);
        sp.setTitle(title);
        sp.setImageUrl(imgUrl);
        sp.setText(desc);
        sp.setTitleUrl(url);
        sp.setShareType(Platform.SHARE_TEXT);
        Platform plate = ShareSDK.getPlatform(QQ.NAME);
        plate.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {

            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                ToastUtil.ShowToast_long(activity, "分享成功");
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {

            }
        });
        plate.share(sp);
    }

    protected void shareToWx() {
        int shareType = Platform.SHARE_WEBPAGE;//分享链接
        cn.sharesdk.wechat.friends.Wechat.ShareParams sp = new cn.sharesdk.wechat.friends.Wechat.ShareParams();
//        sp.setTitle("来自TV聊的分享");
//        sp.setText("我正在TV聊app中向您发出聊天邀请，赶紧下载吧，和我一起体验TV视频聊天的乐趣及更多体验功能!");
//        sp.setImageUrl("http://117.144.248.59/tvlLogo.png");
//        sp.setTitleUrl(LinkInfo.H5_CODE);
//        sp.setUrl(LinkInfo.H5_CODE);
        sp.setTitle(title);
        sp.setImageUrl(imgUrl);
        sp.setText(desc);
        sp.setTitleUrl(url);
        sp.setUrl(url);
        sp.setShareType(shareType);
        Platform plate = ShareSDK.getPlatform(cn.sharesdk.wechat.friends.Wechat.NAME);
        plate.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {
                ToastUtil.ShowToast_long(activity, "分享失败");
            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                ToastUtil.ShowToast_long(activity, "分享成功");
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {

            }
        });
        plate.share(sp);
    }

}
