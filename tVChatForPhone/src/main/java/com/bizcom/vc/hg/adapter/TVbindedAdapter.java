package com.bizcom.vc.hg.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.MainApplication;
import com.bizcom.util.DialogManager;
import com.bizcom.vc.hg.beans.HasBindedItem;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.ui.HgStartVideoByTvActivity;
import com.bizcom.vc.hg.ui.HomeActivity;
import com.bizcom.vc.hg.util.CorlorUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.bizcom.vo.User;
import com.bizcom.vo.User.Status;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shdx.tvchat.phone.R;

import java.util.List;

public class TVbindedAdapter extends BaseAdapter {
    class ViewTag {
        ImageView tv1;
        TextView tv2;
        TextView tv3;
        TextView tvStatus;
        View ll_startCom;
        View ll_delete;
        View ll2;
        View ll_sendTotv;
        View view;
    }

    private List<HasBindedItem> data;
    private HomeActivity mCon;
    private LayoutInflater mInflater;
    private IBussinessManager manager;
    private Dialog deleteContactDialog;
    private TvInfoBeans mbeans;
    private String currentUserId;
    private String currentUid;


    public TVbindedAdapter(List<HasBindedItem> data, Context mCon, TvInfoBeans mbeans) {
        super();
        this.data = data;
        this.mbeans = mbeans;
        this.mCon = (HomeActivity) mCon;
        mInflater = LayoutInflater.from(mCon);
        manager = BussinessManger.getInstance(mCon);

        currentUserId = String.valueOf(GlobalHolder.getInstance().getCurrentUserId());
        if (currentUserId.length() < 2)
            return;
        currentUid = currentUserId.substring(2, currentUserId.length());
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final HasBindedItem mHasBindedItem = data.get(position);
        ViewTag tag;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hg_tvbinded_listitem1, null);
            tag = new ViewTag();
            tag.tv1 = (ImageView) convertView.findViewById(R.id.tv_firstNameTxet);
            tag.tvStatus = (TextView) convertView.findViewById(R.id.tvStatus);
            tag.tv2 = (TextView) convertView.findViewById(R.id.tv_nameTxet);
            tag.tv3 = (TextView) convertView.findViewById(R.id.tv_numberText);
            tag.ll_delete = convertView.findViewById(R.id.ll_delete);
            tag.ll_startCom = convertView.findViewById(R.id.ll_startCom);
            tag.ll2 = convertView.findViewById(R.id.ll2);
            tag.view = convertView.findViewById(R.id.view);
            tag.ll_sendTotv = convertView.findViewById(R.id.ll_send);

            convertView.setTag(tag);
        } else {
            tag = (ViewTag) convertView.getTag();
        }

        tag.tv1.setBackgroundResource(R.drawable.root_main_tongxunlu_unnuor);
        ImageLoader.getInstance().displayImage(mHasBindedItem.getImageUrl(), tag.tv1, MainApplication.imgOptions);
        if (mHasBindedItem.getStatus() == 1) {

            tag.tvStatus.setText("在线");
            tag.tvStatus.setBackgroundResource(R.drawable.online_bg);

        } else if (mHasBindedItem.getStatus() == 0) {
            tag.tvStatus.setText("离线");
            tag.tvStatus.setBackgroundResource(R.drawable.outline_bg);
        }


        String displayName = mHasBindedItem.getUserName();
        tag.tv2.setText(displayName);
        tag.tv3.setText(mHasBindedItem.getPhoneNum());

        if (TextUtils.equals(mHasBindedItem.getUserId(), "11" + mbeans.getUid())) {
            tag.ll2.setVisibility(View.GONE);
            tag.ll_sendTotv.setVisibility(View.GONE);
        } else if (mHasBindedItem.getType() == 0) {
            tag.ll2.setVisibility(View.GONE);
            tag.ll_sendTotv.setVisibility(View.VISIBLE);
        } else if (mHasBindedItem.getType() == 1) {

            tag.ll2.setVisibility(View.VISIBLE);
            tag.ll_sendTotv.setVisibility(View.GONE);
            tag.ll_delete.setVisibility(View.VISIBLE);

        } else if (mHasBindedItem.getType() == 2) {
            tag.ll2.setVisibility(View.VISIBLE);
            tag.ll_sendTotv.setVisibility(View.GONE);
            tag.ll_delete.setVisibility(View.GONE);
        }
        if (position > 0 && position == data.size() - 1) {
            tag.view.setVisibility(View.VISIBLE);
        } else {
            tag.view.setVisibility(View.GONE);
        }


        tag.ll_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDeleDlg(mHasBindedItem.getUserId());
            }
        });
        tag.ll_startCom.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mHasBindedItem.getStatus() != 1) {

                    ToastUtil.ShowToast_long(mCon, "当前用户不在线");
                    return;
                }

                try {
                    startVideoCall(mHasBindedItem.getUserId());

                } catch (ClassCastException e) {
                    ToastUtil.ShowToast_long(mCon, "获取用户信息失败");
                }


            }
        });


        tag.ll_sendTotv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showAddDlg(mHasBindedItem.getUserId());

            }
        });
        tag.ll_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showDeleDlg(mHasBindedItem.getUserId());
            }
        });

        return convertView;
    }


    protected void showDeleDlg(final String fuid) {

        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mCon, null,
                        "确定将" + CorlorUtil.CreatColortext(GlobalHolder.getInstance().getExistUser(Long.parseLong(fuid)).getDisplayName()) + "从TV好友列表删除吗？",
                        mCon.getText(R.string.conversation_quit_dialog_confirm_text),
                        mCon.getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        deleteContactDialog.dismiss();
                        delete(fuid);
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();

    }

    protected void showAddDlg(final String fuid) {

        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mCon, null,
                        "确定将" + CorlorUtil.CreatColortext(GlobalHolder.getInstance().getExistUser(Long.parseLong(fuid)).getDisplayName()) + "发送到TV好友吗？",
                        mCon.getText(R.string.conversation_quit_dialog_confirm_text),
                        mCon.getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        deleteContactDialog.dismiss();

                        add(fuid);
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();

    }


    protected void add(final String fuid) {
        final String mFuid = fuid.substring(2, fuid.length());
        manager.addTvFriend(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {

                if (isSuccess) {
                    ToastUtil.ShowToast_long(mCon, "发送好友成功！");
                    notyfyTv(fuid, ConstantParams.MESSAGE_TYPE_FRIEND_ADD);
                    mCon.getHTab3().doooo();
                } else {
                    ToastUtil.ShowToast_long(mCon, String.valueOf(obj));
                }

            }

        }, mFuid, mbeans.getTvId(), currentUid, BussinessManger.CHANNEL);

    }


    protected void delete(final String fuid) {
        final String mFuid = fuid.substring(2, fuid.length());
        manager.delTvFriend(new OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {

                if (isSuccess) {
                    ToastUtil.ShowToast_long(mCon, "删除成功!");
                    notyfyTv(fuid, ConstantParams.MESSAGE_TYPE_FRIEND_DEL);
                    mCon.getHTab3().doooo();
                } else {
                    ToastUtil.ShowToast_long(mCon, String.valueOf(obj));
                }

            }

        }, mFuid, mbeans.getTvId(), currentUid, BussinessManger.CHANNEL);

    }


    /**
     * 发起视频聊天
     * <p>
     * 手机通过tv呼叫第三方
     */
    private void startVideoCall(String remoteUserId) {
        try {


            User remoteUser = GlobalHolder.getInstance().getExistUser(Long.parseLong(remoteUserId));
            if (GlobalHolder.getInstance().checkServerConnected(mCon)) {
                ToastUtil.ShowToast_long(mCon, "服务器连接异常,请稍后再试");
                return;
            }

            if (remoteUser.getmStatus() == Status.ONLINE) {
                Intent in = new Intent(mCon, HgStartVideoByTvActivity.class);
                in.putExtra("inCall", false);
                in.putExtra("titleText", "视频通话");
                in.putExtra("tvId", mbeans.getTvId());
                in.putExtra("toID", remoteUserId);
                in.putExtra("tvUid", mbeans.getUid());
                mCon.startActivity(in);
            } else {
                ToastUtil.ShowToast_long(mCon, "对方不在线");
            }
        } catch (Exception e) {
            ToastUtil.ShowToast_long(mCon, "服务器连接异常,请稍后再试");
        }

    }

    //通知TV刷新
    protected void notyfyTv(String toId, int type) {
        BussinessManger.getInstance(mCon).notifyTv(type, -1, -1, Long.parseLong(toId), Long.parseLong("11" + mbeans.getUid()));


    }

}
