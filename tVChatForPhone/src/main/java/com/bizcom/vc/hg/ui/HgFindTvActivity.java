package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HgFindTvActivity extends Activity {
    private Context mContext;
    private IBussinessManager manager;

    @BindView(R.id.img_search)
    ImageView img_search;

    @BindView(R.id.img_clear)
    ImageView img_clear;

    @BindView(R.id.et)
    EditText et;

    @BindView(R.id.text_cancel)
    TextView text_cancel;


    @BindView(R.id.tv_ll)
    LinearLayout tv_ll;

    @BindView(R.id.tv_img)
    SimpleDraweeView tv_img;

    @BindView(R.id.tv_name)
    TextView tv_name;

    @BindView(R.id.tv_account)
    TextView tv_account;

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hg_set_tv_activity);
        unbinder = ButterKnife.bind(this);
        mContext = this;
        manager = BussinessManger.getInstance(mContext);
        et.setLongClickable(false);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if ((actionId == 0 || actionId == 3) && event != null) {
                    search();
                }
                return false;
            }
        });
    }


    private void initdata(String userName) {

        BussinessManger.getInstance(mContext).queryByTvSn(new OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {

                    tvRealId = String.valueOf(obj);
                    checkTVInfo(tvRealId);
                } else {
                    tv_ll.setVisibility(View.GONE);
                    String msg = String.valueOf(obj);
                    if (TextUtils.equals("null", msg)) {
                        ToastUtil.ShowToast_long(mContext, "没有搜索到这个人喔~");
                    } else {
                        ToastUtil.ShowToast_long(mContext, "没有搜索到这个人喔~");
                    }
                }
            }
        }, userName);
    }

    @OnClick(R.id.img_search)
    void imgSearchClick() {
        search();
    }

    @OnClick(R.id.img_clear)
    void imgclear() {
        et.setText("");
    }

    @OnClick(R.id.text_cancel)
    void cancel() {
        finish();
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_DEL:
//                delete();
//                break;
//            case KeyEvent.KEYCODE_SEARCH:
//                search();
//                break;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    void search() {
        String tvId = et.getText() == null ? "" : String.valueOf(et.getText());
        if (TextUtils.isEmpty(tvId)) {
            ToastUtil.ShowToast_long(mContext, "请输入正确的TV号码");
            return;
        }
        initdata(tvId);
    }

    void delete() {
        String str = String.valueOf(et.getText());
        int focusIndex = et.getSelectionEnd();
        Log.d("tag", "focusIndex" + focusIndex);
        if (focusIndex != str.length()) {
            String str1 = str.substring(0, focusIndex - 1);
            String str2 = str.substring(focusIndex, str.length());
            et.setText(str1 + str2);
            et.setSelection(focusIndex - 1);
        } else {

            if (str.length() >= 2) {
                str = str.substring(0, str.length() - 1);
            } else {
                str = "";
            }
            et.setText(str);
            et.setSelection(et.getText().length());
        }


    }

    String tvId;
    String tvUid;

    String tvRealId = "";

    @OnClick(R.id.tv_ll)
    void tvllClick() {
//        AlertMsgUtils.showConfirm(HgFindTvActivity.this, "确定", "取消", "绑定TV:" + String.valueOf(tv_account.getText()), new AlertMsgUtils.OnDialogBtnClickListener() {
//            @Override
//            public void onConfirm(Dialog dialog) {
//                dialog.dismiss();
//                bindTv(tvRealId, tvUid);
//            }
//        });

        bindTv(tvRealId, tvUid);
    }


    private void checkTVInfo(final String tvId) {
        WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.loding_progress));
        manager.queryTvByTvId(new OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                String msg = String.valueOf(obj).trim();
                Log.e("checkTVInfo", "msg-->" + msg);
                WaitDialogBuilder.dismissDialog();
                if (isSuccess) {
                    et.clearFocus();
                    Map<String, Object> m = (Map<String, Object>) obj;
                    tvUid = m.get("uid") + "";
                    String nickName = m.get("nickName") + "";
                    String userName = m.get("userName") + "";
                    String picurl = m.get("picurl") + "";
                    tv_ll.setVisibility(View.VISIBLE);
                    tv_name.setText(nickName);
                    tv_account.setText(userName);
                    if (!TextUtils.isEmpty(picurl)) {
                        tv_img.setImageURI(Uri.parse(picurl));
                    }
                } else {
                    tv_ll.setVisibility(View.GONE);
                    if (TextUtils.equals("null", msg)) {
                        ToastUtil.ShowToast_long(mContext, "没有搜索到这个人喔~");
                    } else {
                        ToastUtil.ShowToast_long(mContext, "没有搜索到这个人喔~");
                    }

                }

            }
        }, tvId, BussinessManger.CHANNEL);

    }

    private void bindTv(final String tvid, final String tvUid) {
        final String uid = GlobalHolder.getInstance().getCurrentUser().getTvlUid();
        WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.loding_progress));
        Log.e("tv bind", "uid-->" + uid + "  tvUid-->" + tvUid + " tvid-->" + tvid);
        manager.binding(new OnResponseListener() {
            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                WaitDialogBuilder.dismissDialog();
                if (isSuccess) {
                    isBindSuccess = true;
                    FriendUtil.addConstantNoLoading(HgFindTvActivity.this, Long.parseLong(String.valueOf("11" + tvUid)), new FriendUtil.AddFriendCallback() {
                        @Override
                        public void onResult(boolean isSuccess) {
                            if (isSuccess) {
                                // ToastUtil.ShowToast_short(HgFindTvActivity.this,"您已绑定成功，并已加"+tvid+"电视为好友");
                                //好友添加成功
                                showEditTvCommenName(tvid);
                            }else{
                                refrshBindInfo();
                            }

                        }
                    });
                } else {
                    ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                }
            }
        }, uid, tvid, BussinessManger.CHANNEL);
    }


    void showEditTvCommenName(String tvid) {
        AlertMsgUtils.showEditTVCommentNameConfirm(HgFindTvActivity.this, "您已绑定TV:" + String.valueOf(tv_account.getText()) + ", 并已添加为好友,请修好友备注:", new AlertMsgUtils.OnDialogStringBtnClickListener() {
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
                refrshBindInfo();
            }
        });

    }


    boolean isBindSuccess = false;

    void refrshBindInfo() {
        // 发消息通知tv刷新，并跳转到列表页
        if (isBindSuccess) {
            Intent in = getIntent();
            in.putExtra("tvUid", tvUid);
            setResult(1, in);
            finish();
        }
    }
}
