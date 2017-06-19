package com.bizcom.vc.hg.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bizcom.util.MessageUtil;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.ui.HgAddFriendsConfirmActivity;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.widget.cus.CustomDialog;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

public class SearchContactAdapter extends BaseAdapter {
    private List<PhoneFriendItem> list = null;
    private Context mContext;
    private Dialog mShareDialog;

    public SearchContactAdapter(Context mContext, List<PhoneFriendItem> list) {
        this.mContext = mContext;
        this.list = list;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(List<PhoneFriendItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final PhoneFriendItem mContent = list.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_search_friend_listview, null);
            viewHolder.tv_firstNameText = (TextView) view.findViewById(R.id.firstNameText);
            viewHolder.tv_nameTxet = (TextView) view.findViewById(R.id.tv_nameTxet);
            viewHolder.tv_number = (TextView) view.findViewById(R.id.tv_numberText);
            viewHolder.mImageAddFriendFinish = (ImageView) view.findViewById(R.id.ic_add_friend_finish);
            viewHolder.mImageAddFriend = (ImageView) view.findViewById(R.id.ic_lately_addFriends);
            viewHolder.mImageShare = (ImageView) view.findViewById(R.id.ic_share);
            viewHolder.view = view.findViewById(R.id.view);
            viewHolder.icon = (SimpleDraweeView) view.findViewById(R.id.icon);
            viewHolder.tv1 = (TextView) view.findViewById(R.id.tvState);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (list.size() - 1 == position) {
            viewHolder.view.setVisibility(View.VISIBLE);
        } else {
            viewHolder.view.setVisibility(View.GONE);
        }

//        String displayName = mContent.getFirstName();
//        String s = displayName.substring(0, 1);
//        if (!checkIfNum(s)) {
//            viewHolder.tv_firstNameText.setText(s);
//        } else {
//            viewHolder.tv_firstNameText.setBackgroundResource(R.drawable.root_main_tongxunlu_unnuor);
//            viewHolder.tv_firstNameText.setText("");
//        }
        viewHolder.tv_firstNameText.setText(MessageUtil.getFirstLetterName(mContent.getFirstName()));

        viewHolder.tv_nameTxet.setText(mContent.getName());
        String numStr = mContent.getPhoneNum();
        viewHolder.tv_number.setText(numStr.replace("+86", ""));

        viewHolder.mImageAddFriend.setVisibility(View.GONE);
        viewHolder.mImageAddFriendFinish.setVisibility(View.GONE);
        viewHolder.mImageShare.setVisibility(View.GONE);

        if (mContent.isHasRegsited()) {
            if (mContent.isIfFriends()) {
                User user = GlobalHolder.getInstance().getUser(mContent.getUserId());
                loadImage(true, viewHolder, user.getmAvatarLocation());
                viewHolder.mImageAddFriendFinish.setVisibility(View.VISIBLE);
            } else {
                loadImage(true, viewHolder,mContent.getPicUrl());
                viewHolder.mImageAddFriend.setVisibility(View.VISIBLE);
            }

        } else {
            loadImage(false, viewHolder, mContent.getPicUrl());
            viewHolder.mImageShare.setVisibility(View.VISIBLE);
        }

        if (mContent.isStatus()) {
            viewHolder.tv1.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tv1.setVisibility(View.GONE);
        }

        viewHolder.mImageAddFriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FriendUtil.addConstant(mContext, mContent.getUserId(),  new FriendUtil.AddFriendCallback() {
                    @Override
                    public void onResult(boolean isSuccess) {
                        if (isSuccess) {
                            mContent.setIfFriends(true);
                            notifyDataSetChanged();
                        }
                    }
                });

            }
        });
        viewHolder.mImageShare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showShareDialog();

            }
        });

        return view;

    }

    final static class ViewHolder {
        //        public LinearLayout ll_video;
        public View view;
        TextView tv_firstNameText;
        SimpleDraweeView icon;
        TextView tv_nameTxet;
        TextView tv_number;
        ImageView mImageAddFriend;
        ImageView mImageAddFriendFinish;
        ImageView mImageShare;
        TextView tv1;
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

    private boolean checkIfNum(String s) {
        try {
            int i = Integer.parseInt(s);

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private void showShareDialog() {
        if (mShareDialog == null) {
            mShareDialog = new CustomDialog(mContext, R.style.windStyle);
            mShareDialog.setContentView(R.layout.hg_share);
            Window window = mShareDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            View cannelButtonContent = mShareDialog.findViewById(R.id.cancel);
            View qqBt = mShareDialog.findViewById(R.id.shareToQQ);
            View wxBt = mShareDialog.findViewById(R.id.shareToWX);
            qqBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();

                    shareToQQ();
                }
            });

            wxBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                    shareToWx();
                }

            });
            cannelButtonContent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                }

            });
        }

        mShareDialog.show();
    }


    //有图片加载图片
    private void loadImage(boolean b, SearchContactAdapter.ViewHolder viewHolder, String picurl) {
        if (b) {
            SimpleDraweeViewUtils.display(viewHolder.icon,viewHolder.tv_firstNameText, picurl);
        } else {
            viewHolder.icon.setVisibility(View.GONE);
            viewHolder.tv_firstNameText.setVisibility(View.VISIBLE);
        }
    }

    protected void shareToQQ() {

        QQ.ShareParams sp = new QQ.ShareParams();
        sp.setTitle("来自TV聊的分享");
        sp.setImageUrl("http://117.144.248.59/tvlLogo.png");
        sp.setText("我正在TV聊app中向您发出聊天邀请，赶紧下载吧，和我一起体验TV视频聊天的乐趣及更多体验功能!");
        sp.setTitleUrl(LinkInfo.H5_CODE);
        sp.setShareType(Platform.SHARE_TEXT);
        Platform plate = ShareSDK.getPlatform(QQ.NAME);
        plate.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {

            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                ToastUtil.ShowToast_long(mContext, "分享成功");
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
        sp.setTitle("来自TV聊的分享");
        sp.setText("我正在TV聊app中向您发出聊天邀请，赶紧下载吧，和我一起体验TV视频聊天的乐趣及更多体验功能!");
        sp.setImageUrl("http://117.144.248.59/tvlLogo.png");
        sp.setTitleUrl(LinkInfo.H5_CODE);
        sp.setUrl(LinkInfo.H5_CODE);
        sp.setShareType(shareType);
        Platform plate = ShareSDK.getPlatform(cn.sharesdk.wechat.friends.Wechat.NAME);
        plate.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {
                ToastUtil.ShowToast_long(mContext, "分享失败");
            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                ToastUtil.ShowToast_long(mContext, "分享成功");
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {

            }
        });
        plate.share(sp);
    }
}