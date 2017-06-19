package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bizcom.request.V2SearchRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vo.SearchedResult;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HgAddFriendActivity extends Activity {

    @BindView(R.id.image_avatar)
    SimpleDraweeView imageAvatar;

    @BindView(R.id.text_name)
    TextView textName;

    @BindView(R.id.text_phone)
    TextView textPhone;

    @BindView(R.id.img_search)
    ImageView img_search;

    @BindView(R.id.img_clear)
    ImageView img_clear;

    @BindView(R.id.et)
    EditText et;

    @BindView(R.id.text_cancel)
    TextView text_cancel;

    @BindView(R.id.view_add_friend)
    LinearLayout viewAddFriend;

    @BindView(R.id.image_icon)
    ImageView imageIcon;
    @BindView(R.id.text_empty)
    TextView textEmpty;
    @BindView(R.id.firstNameText)
    TextView firstNameText;

    private V2SearchRequest searchService = new V2SearchRequest();// 搜索好友时的请求服务
    private HgAddFriendsActivity.State mState = HgAddFriendsActivity.State.DONE;// 默认搜索状态为完成
    private final int SEARCH_DONE = 10;// 搜索好友完成
    private String mSearch;
    private User mUser;
    private Context mContext;

    Unbinder unbinder;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hg_set_phone_activity);
        unbinder = ButterKnife.bind(this);
        mContext = this;
        imageIcon.setImageResource(R.mipmap.ic_add_friend_search);

        et.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    search();
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageUtil.showKeyBoard(et);
            }
        }, 250);
    }

    @OnClick(R.id.img_search)
    void imgSearchClick() {
        search();
    }

    @OnClick(R.id.img_clear)
    void imgclear() {
        et.setText("");
        imageIcon.setVisibility(View.VISIBLE);
        imageIcon.setImageResource(R.mipmap.ic_add_friend_search);
        viewAddFriend.setVisibility(View.GONE);
        textEmpty.setVisibility(View.GONE);
    }

    @OnClick(R.id.text_cancel)
    void cancel() {
        MessageUtil.hideKeyBoard(HgAddFriendActivity.this, et.getWindowToken());
        finish();
    }


    private void search() {
        mSearch = et.getText().toString().trim();
        if (mSearch != null && !TextUtils.isEmpty(mSearch) && mSearch.length() >= 7) {
            search(mSearch);
        } else {
            ToastUtil.ShowToast_long(mContext, "请输入至少7位的账号");
        }
    }

    @OnClick(R.id.view_add_friend)
    public void onClick() {
        if (mUser != null) {
            if (mUser.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
                ToastUtil.ShowToast_long(mContext, "不能添加自己为好友");
            }
//            else if (GlobalHolder.getInstance().isFriend(mUser)) {
//                ToastUtil.ShowToast_long(mContext, "对方已是您的好友");
//            }
            else {
                Intent in = new Intent(mContext, UserDetailActivity.class);
                in.putExtra(UserDetailActivity.EXTRA_USER, mUser.getmUserId());
                in.putExtra(UserDetailActivity.EXTRA_TYPE, UserDetailActivity.TYPE_ADD_FRIEND);
                startActivity(in);
            }
        }
    }

    enum State {
        DONE, SEARCHING,
    }

    private void search(String searText) {
//        synchronized (mState) {
        if (mState == HgAddFriendsActivity.State.SEARCHING) {
            return;
        }
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            return;
        }
        mState = HgAddFriendsActivity.State.SEARCHING;

//        }
        WaitDialogBuilder.showNormalWithHintProgress(mContext, getResources().getString(R.string.status_searching));
        V2SearchRequest.SearchParameter par = searchService
                .generateSearchPatameter(V2GlobalConstants.SEARCH_REQUEST_TYPE_USER, searText, 1);// 1为查找第一页。
        searchService.search(par, new HandlerWrap(handler, SEARCH_DONE, null));

    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_DONE:// 添加好友-搜索完毕
                    WaitDialogBuilder.dismissDialog();
                    WaitDialogBuilder.clearWaitDialog();
//                    synchronized (mState) {
                    mState = HgAddFriendsActivity.State.DONE;
//                    }
                    JNIResponse jni = (JNIResponse) msg.obj;
                    boolean isHasData = false;
                    if (jni.getResult() == JNIResponse.Result.SUCCESS) {
                        SearchedResult result = (SearchedResult) jni.resObj;
                        for (SearchedResult.SearchedResultItem srItem : result.getList()) {
                            User u = GlobalHolder.getInstance().getUser(srItem.id);
                            if (u.getAccount().equals(mSearch)) {
                                mUser = u;
                                isHasData = true;

                                firstNameText.setText(MessageUtil.getFirstLetterName(u.getDisplayName()));
                                SimpleDraweeViewUtils.display(imageAvatar,firstNameText, u.getmAvatarLocation());
                                textName.setText(u.getDisplayName());
                                textPhone.setText(u.getAccount());
                            }
                        }
                    }

                    if (!isHasData) {
                        imageIcon.setVisibility(View.VISIBLE);
                        imageIcon.setImageResource(R.mipmap.ic_add_friend_empty);
                        textEmpty.setVisibility(View.VISIBLE);
                        viewAddFriend.setVisibility(View.GONE);
                    } else {
                        imageIcon.setVisibility(View.GONE);
                        viewAddFriend.setVisibility(View.VISIBLE);
                        textEmpty.setVisibility(View.GONE);

                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(
                                        HgAddFriendActivity.this
                                                .getCurrentFocus()
                                                .getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    break;
            }
        }
    };


    @OnClick(R.id.btn_delete)
    void btn_delete() {
        String str = String.valueOf(et.getText());
        if (str.length() >= 2) {
            str = str.substring(0, str.length() - 1);
        } else {
            str = "";

            imageIcon.setVisibility(View.VISIBLE);
            imageIcon.setImageResource(R.mipmap.ic_add_friend_search);
            viewAddFriend.setVisibility(View.GONE);
            textEmpty.setVisibility(View.GONE);
        }
        et.setText(str);
        et.setSelection(et.getText().length());
    }

    @OnClick(R.id.btn_search)
    void btn_search() {
        search();
    }

}
