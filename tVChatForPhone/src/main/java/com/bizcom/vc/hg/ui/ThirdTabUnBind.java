package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.util.CorlorUtil;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.google.gson.Gson;
import com.shdx.tvchat.phone.R;

import java.util.Map;

public class ThirdTabUnBind extends Fragment implements View.OnClickListener {
    public final static int SCANNIN_GREQUEST_CODE = 1;
    private static final String TAG = ThirdTabUnBind.class.getSimpleName();

    private Context mContext;
    private View rootView;
    private View bt_scan;
    private TextView tv1;
    private TextView tv2;
    private IBussinessManager manager;
    private Dialog avatarDialog;
    private Dialog deleteContactDialog;
    private TextView tv5;
    private TextView tv4;
    private TextView tv3;
    private View ll_content;
    private TextView bt_serTv;
    private WebView mWeb;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "onAttach");
        manager = BussinessManger.getInstance(mContext);
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.thir_tab_unbind, container, false);
        initView();
        return rootView;
    }

    private void initView() {
        bt_serTv = (TextView) rootView.findViewById(R.id.bt_serTv);
        bt_scan = rootView.findViewById(R.id.bt_scan);
        bt_scan.setOnClickListener(this);
        bt_serTv.setOnClickListener(this);
        mWeb = (WebView) rootView.findViewById(R.id.wv);
        mWeb.getSettings().setJavaScriptEnabled(true);//
        mWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return true;
            }
        });
        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(".apk")) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    view.loadUrl(url);
                }

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }
        });


        mWeb.loadUrl("http://tvl.hongguaninfo.com/tvAssist/index.html");

    }

    private void initText() {

        ll_content = rootView.findViewById(R.id.ll_content);
        tv1 = (TextView) rootView.findViewById(R.id.tv1);
        tv2 = (TextView) rootView.findViewById(R.id.tv2);
        tv3 = (TextView) rootView.findViewById(R.id.tv3);
        tv4 = (TextView) rootView.findViewById(R.id.tv4);
        tv5 = (TextView) rootView.findViewById(R.id.tv5);
        ll_content.setVisibility(View.GONE);
        tv1.setText(Html.fromHtml(
                "1 选中" + CorlorUtil.CreatColortext("TV聊图标") + "并点击" + CorlorUtil.CreatColortext("确定键") + "进入"));
        tv2.setText(Html.fromHtml("2 点击【" + CorlorUtil.CreatColortext("开通") + "】按钮，审核通过后，安装"
                + CorlorUtil.CreatColortext("摄像头") + "即可使用"));
        tv3.setText(Html.fromHtml("3 在【" + CorlorUtil.CreatColortext("TV协助") + "】中点击扫一扫电视"
                + CorlorUtil.CreatColortext("左下角的二维码") + "或者输入【" + CorlorUtil.CreatColortext("我的设备") + "】中"
                + CorlorUtil.CreatColortext("TV聊号码") + ",即可绑定账号到TV"));
        tv4.setText(Html.fromHtml("4 添加好友，在TV界面点击【" + CorlorUtil.CreatColortext("添加好友") + "】，在TV聊手机端上点击"
                + CorlorUtil.CreatColortext("+") + "或【" + CorlorUtil.CreatColortext("手机通讯录") + "】添加后将好友【"
                + CorlorUtil.CreatColortext("发送到TV") + "】"));
        tv5.setText(Html.fromHtml("5 选中" + CorlorUtil.CreatColortext("好友头像") + "按" + CorlorUtil.CreatColortext("确定键")
                + "即可发起视频通话邀请体验高清视频通话"));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//		Log.e("Scan", "Scan3");
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String text = bundle.getString("result");
                    checkTVInfo(text);
                }
                break;
            case 0:
                if (resultCode == 1) {
                    notifyAllSet(data.getStringExtra("tvUid"));
                }
                break;
        }
    }

    String tvAccount ="";
    private void checkTVInfo(final String tvId) {
        WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.loding_progress));
        manager.queryTvByTvId(new OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                WaitDialogBuilder.dismissDialog();
                if (isSuccess) {
                    Map<String, Object> data = (Map<String, Object>) obj;
                    //showConfirmDialog(String.valueOf(data.get("nickName")), String.valueOf(data.get("userName")), tvId,
                    //        String.valueOf(data.get("uid")));
                    tvAccount = String.valueOf(data.get("userName"));
                    bindTv(tvId, String.valueOf(data.get("uid")));
                } else {
                    ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                }

            }
        }, tvId, BussinessManger.CHANNEL);

    }

    boolean isBindSuccess = false;

    private void bindTv(final String tvId, final String tvUid) {
        String userId = GlobalHolder.getInstance().getCurrentUserId() + "";

        String uid = userId.substring(2, userId.length());

        WaitDialogBuilder.showNormalWithHintProgress(getActivity(), getResources().getString(R.string.loding_progress));
        manager.binding(new OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    //绑定成功
                    isBindSuccess = true;
                    //notifyAllSet(tvUid);

                    //绑定成功后 加好友
                    FriendUtil.addConstantNoLoading(getActivity(), Long.parseLong(String.valueOf("11" + tvUid)), new FriendUtil.AddFriendCallback() {
                        @Override
                        public void onResult(boolean isSuccess) {
                            //加好友成功
                            if (isSuccess) {
                                // ToastUtil.ShowToast_short(getActivity(),"您已绑定成功，并已加"+tvId+"电视为好友");
                                showEditTvCommenName(tvId, tvUid);
                            }else{
                                notifyAllSet(tvUid);
                            }
                        }
                    });
                }
                WaitDialogBuilder.dismissDialog();
                ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                WaitDialogBuilder.dismissDialog();
            }

        }, uid, tvId, BussinessManger.CHANNEL);
    }

    void showEditTvCommenName(String tvid, final String tvUid) {
        AlertMsgUtils.showEditTVCommentNameConfirm(getActivity(), "您已绑定TV:" + tvAccount + ", 并已添加为好友,请修好友备注:", new AlertMsgUtils.OnDialogStringBtnClickListener() {
            @Override
            public void onConfirm(final String commentName) {
                if (!TextUtils.isEmpty(commentName)) {
                    User user = GlobalHolder.getInstance().getUser(Long.parseLong("11" + tvUid));
                    user.setCommentName(commentName);
                    new V2ImRequest().updateUserInfo(user, new HandlerWrap(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            Toast.makeText(mContext, "修改备注成功", Toast.LENGTH_SHORT).show();
                            MainApplication.updateContactUserCommentName(Long.parseLong("11" + tvUid), commentName);
                            Intent intent = new Intent();
                            intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            intent.putExtra("modifiedUser", Long.parseLong("11" + tvUid));
                            mContext.sendBroadcast(intent);
                        }
                    }, 0, null));
                }
                notifyAllSet(tvUid);
            }
        });

    }

    protected void notifyAllSet(String tvUid) {
       // if (isBindSuccess) {
            HomeActivity mActivity = (HomeActivity) mContext;
            HTab3 htab3 = mActivity.getHTab3();
            htab3.doooo();
            notyfyTvBindOk(tvUid);
       // }
    }

    protected void notyfyTvBindOk(String tvUid) {

        BussinessManger.getInstance(mContext).notifyTv(ConstantParams.MESSAGE_TYPE_BIND, -1, -1, -1l, Long.parseLong(String.valueOf("11" + tvUid)));

    }

    private void showPdialog(final String tvid, final String tvUid) {

        avatarDialog = showMdialog("立即绑定", "取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarDialog.dismiss();

                // bindTv(tvid,tvUid);
            }

        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarDialog.dismiss();
            }
        });
        avatarDialog.show();
    }

    private void showConfirmDialog(String nickName, String userName, final String tvid, final String tvUid) {
        String tvName = "";
        if (!TextUtils.isEmpty(nickName)) {
            tvName = nickName;
        } else if (!TextUtils.isEmpty(userName)) {
            tvName = userName;
        } else if (!TextUtils.isEmpty(tvid)) {
            tvName = tvid;
        }

        AlertMsgUtils.showConfirm(mContext, mContext.getText(R.string.conversation_quit_dialog_confirm_text).toString(),
                mContext.getText(R.string.conversation_quit_dialog_cancel_text).toString(),
                "绑定TV:" + Html.fromHtml(CorlorUtil.CreatColortext(tvName)), new AlertMsgUtils.OnDialogBtnClickListener() {
                    @Override
                    public void onConfirm(Dialog dialog) {
                        bindTv(tvid, tvUid);
                        dialog.dismiss();
                    }
                });
    }

    private Dialog showMdialog(String s1, String s2, View.OnClickListener l1, View.OnClickListener l2) {
        Dialog log = new Dialog(mContext, R.style.ContactUserDetailVoiceCallDialog);
        log.setCancelable(true);
        log.setCanceledOnTouchOutside(true);
        log.setContentView(R.layout.c_dialog);
        TextView tv1 = (TextView) log.findViewById(R.id.contacts_user_detail_avatar_dialog_1);
        tv1.setText(s1);
        tv1.setOnClickListener(l1);
        TextView tv2 = (TextView) log.findViewById(R.id.contacts_user_detail_avatar_dialog_2);
        tv2.setText(s2);
        tv2.setOnClickListener(l2);
        return log;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_scan:
                Intent i = new Intent(mContext, MipcaActivityCapture.class);
                i.putExtra("titleText", "扫一扫");
                i.putExtra("isScanTv", true);
                startActivityForResult(i, SCANNIN_GREQUEST_CODE);

                break;
            case R.id.bt_serTv:
                Intent i2 = new Intent(mContext, HgFindTvActivity.class);
                i2.putExtra("titleText", "TV协助");
                startActivityForResult(i2, 0);

                break;

        }

    }
}
