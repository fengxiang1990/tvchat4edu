package com.bizcom.vc.hg.ui;

import java.util.ArrayList;
import java.util.List;

import com.MainApplication;
import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.DialogManager;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.util.DialogManager.DialogInterface;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.adapter.FriendAdapter;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView.OnEditorActionListener;

public class HSearcActivity extends Activity {

    private User deleteUser = null;
    private static final int DELETE_CONTACT_USER = 4;
    private boolean hasPrepared;//判断好友列表是否准备好
    private HSearcActivity mContext;
    private HeadLayoutManagerHG mHeadLayoutManager;
    private String titleText = "";
    private EditText et;
    private GridView gd;
    private List<User> data;
    private FriendAdapter mAdapter;
    private Dialog deleteContactDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DELETE_CONTACT_USER:
                    WaitDialogBuilder.dismissDialog();
                    JNIResponse response = (JNIResponse) msg.obj;
                    if (response.getResult() == JNIResponse.Result.SUCCESS) {

                        Intent intent = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
                        intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                        ConversationNotificationObject obj = new ConversationNotificationObject(
                                Conversation.TYPE_VERIFICATION_MESSAGE, Conversation.SPECIFIC_VERIFICATION_ID, false);
                        intent.putExtra("obj", obj);
                        mContext.sendBroadcast(intent);
                        data.remove(deleteUser);

                        mAdapter.notifyDataSetChanged();

                    } else if (response.getResult() == JNIResponse.Result.TIME_OUT) {
                        Toast.makeText(mContext, mContext.getString(R.string.contacts_delete_net_failed), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.contacts_delete_failed), Toast.LENGTH_SHORT)
                                .show();
                    }

                    break;

            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsearc);
        hasPrepared = MainApplication.hasDataPrepared;
        et = (EditText) findViewById(R.id.et);
        et.setOnEditorActionListener(new EtPasswordOnEditorActionListener());
        gd = (GridView) findViewById(R.id.gd);
        titleText = getIntent().getStringExtra("titleText");
        mContext = this;
        if (savedInstanceState != null) {
            finish();
            return;
        }
        mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout), false);
        mHeadLayoutManager.updateTitle(titleText);

        data = new ArrayList<User>();
        gd.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new FriendAdapter(data, mContext);
        gd.setAdapter(mAdapter);

        gd.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position >= data.size())
                    return;
                User u = data.get(position);
                if (u.getmStatusToIntValue() == 0) {

                    ToastUtil.ShowToast_long(mContext, "当前用户不在线");
                    return;
                }
                startVideoCall(u.getmUserId());


            }
        });
        gd.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                showDeleteContactDialog(data.get(position).getmUserId());
                return true;
            }
        });

        setListener();
    }

    private void setListener() {
        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (hasPrepared) {
                    data.clear();
                    data.addAll(search(String.valueOf(s)));
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * 筛选
     *
     * @param str
     * @return
     */
    protected List<User> search(String str) {
        if (TextUtils.isEmpty(str)) {
            data.clear();
            return new ArrayList<User>();
        }
        List<User> mlis = null;
        try {
            mlis = MainApplication.getDbUtils()
                    .findAll(Selector.from(User.class)
                            .where("mAccount", "like", "%" + str + "%")
                            .or("mMobile", "like", "%" + str + "%")
                            .or("mCommentName", "like", "%" + str + "%")
                            .or("mTelephone", "like", "%" + str + "%")
                            .or("mNickName", "like", "%" + str + "%"));
        } catch (DbException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mlis == null ? (new ArrayList<User>()) : mlis;

    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }


    private void showDeleteContactDialog(final long uid) {
        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mContext, null,
                        mContext.getText(R.string.contacts_delete_confirm),
                        mContext.getText(R.string.conversation_quit_dialog_confirm_text),
                        mContext.getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        // 删除好友
                        deleteUser = GlobalHolder.getInstance().getExistUser(uid);
                        deleteContactDialog.dismiss();
                        new V2ContactsRequest().delContact(deleteUser, new HandlerWrap(mHandler, DELETE_CONTACT_USER, null));
                        WaitDialogBuilder.showNormalWithHintProgress(mContext);
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();
    }

    /**
     * 发起视频聊天
     *
     * @param remoteUserId 对方的id
     */
    private void startVideoCall(long remoteUserId) {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            ToastUtil.ShowToast_long(mContext, "服务器连接异常,请稍后再试");
            return;
        }
        GlobalConfig.startP2PConnectChat(mContext,
                ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId,
                false, null, null);
    }


    private class EtPasswordOnEditorActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

            }
            return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
        }

    }


    public void hideSoft() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        hideSoft();
    }
}
