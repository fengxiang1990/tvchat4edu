package com.bizcom.vc.hg.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MainApplication;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.widget.cus.CustomDialog;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shdx.tvchat.phone.R;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

/**
 * Created by zhoukang on 16/10/17.
 */

public class MlistAdapter extends BaseAdapter {


    private List<PhoneFriendItem> list = null;
    private Context mContext;
    private Dialog mShareDialog;

    public MlistAdapter(Context mContext, List<PhoneFriendItem> list) {
        this.mContext = mContext;
        this.list = list;
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
        MlistAdapter.ViewHolder viewHolder = null;
        final PhoneFriendItem mContent = list.get(position);
        if (view == null) {
            viewHolder = new MlistAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sort_listview, null);
            viewHolder.tv_firstNameText = (TextView) view.findViewById(R.id.firstNameText);
            viewHolder.tv_nameTxet = (TextView) view.findViewById(R.id.tv_nameTxet);
            viewHolder.tv_number = (TextView) view.findViewById(R.id.tv_numberText);
            viewHolder.ll1 = (LinearLayout) view.findViewById(R.id.ll1);
            viewHolder.ll2 = (LinearLayout) view.findViewById(R.id.ll2);
//            viewHolder.ll_addFriends = (LinearLayout) view.findViewById(R.id.ll_addFriends);
//            viewHolder.ll_invate = (LinearLayout) view.findViewById(R.id.ll_invate);
//            viewHolder.ll_video = (LinearLayout) view.findViewById(R.id.ll_video);
            viewHolder.view = view.findViewById(R.id.view);
            viewHolder.tv_hasRegsted = view.findViewById(R.id.tv_hasRegsted);
            viewHolder.icon = (ImageView) view.findViewById(R.id.icon);


            viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            view.setTag(viewHolder);
        } else {
            viewHolder = (MlistAdapter.ViewHolder) view.getTag();
        }

        if(list.size()-1==position){
            viewHolder.view.setVisibility(View.VISIBLE);
        }else{
            viewHolder.view.setVisibility(View.GONE);
        }


        String displayName = mContent.getFirstName();
        String s=displayName.substring(0, 1);
        if(!checkIfNum(s)){
            viewHolder.tv_firstNameText.setText(s);
        }else{
            viewHolder.tv_firstNameText.setBackgroundResource(R.drawable.root_main_tongxunlu_unnuor);
            viewHolder.tv_firstNameText.setText("");
        }



        viewHolder.tv_nameTxet.setText(mContent.getName());
        String numStr=mContent.getPhoneNum();
        viewHolder.tv_number.setText(numStr.replace("+86",""));

        if(mContent.isHasRegsited()){
            loadImage(true, viewHolder, mContent.getUserId());
            viewHolder.tv_hasRegsted.setVisibility(View.GONE);
            viewHolder.ll1.setVisibility(View.GONE);
            viewHolder.ll2.setVisibility(View.VISIBLE);
//            if(mContent.isIfFriends()){
//                viewHolder.ll_addFriends.setVisibility(View.GONE);
//            }else{
//                viewHolder.ll_addFriends.setVisibility(View.VISIBLE);
//            }

        }else{
            loadImage(false,viewHolder,mContent.getUserId());
            viewHolder.tv_hasRegsted.setVisibility(View.VISIBLE);
            viewHolder.ll2.setVisibility(View.GONE);
            viewHolder.ll1.setVisibility(View.VISIBLE);
        }


//        viewHolder.ll_addFriends.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent in=new Intent(mContext,HgAddFriendsConfirmActivity.class);
//                in.putExtra("userId", mContent.getUserId());
//                in.putExtra("titleText", "添加好友");
//                mContext.startActivity(in);
//
//            }
//        });
//        viewHolder.ll_video.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if(!mContent.isStatus()){
//
//                    ToastUtil.ShowToast_long(mContext, "当前用户不在线");
//                    return;
//                }
//
//                startVideoCall(mContent.getUserId());
//            }
//        });
//        viewHolder.ll_invate.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                showShareDialog();
//
//            }
//        });

        return view;

    }

    final static class ViewHolder {
//        public LinearLayout ll_video;
//        public LinearLayout ll_invate;
//        public LinearLayout ll_addFriends;
        public View view;
        public View tv_hasRegsted;
        TextView tvLetter;
        TextView tv_firstNameText;
        ImageView icon;
        TextView tv_nameTxet;
        TextView tv_number;
        LinearLayout ll1;
        LinearLayout ll2;
    }




    /**
     * 发起视频聊天
     *
     * @param remoteUserId
     *            对方的id
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
            int i=Integer.parseInt(s);

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
            cannelButtonContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                }

            });
        }

        mShareDialog.show();
    }


    //有图片加载图片   
    private void loadImage(boolean b, MlistAdapter.ViewHolder viewHolder, long userId) {
        if(b){
            User u=GlobalHolder.getInstance().getUser(userId);
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.tv_firstNameText.setVisibility(View.GONE);
            ImageLoader.getInstance().displayImage( u.getmAvatarLocation(),
                    viewHolder.icon, MainApplication.imgOptions);
        }else{
            viewHolder.icon.setVisibility(View.GONE);
            viewHolder.tv_firstNameText.setVisibility(View.VISIBLE);
        }
    }
    protected void shareToQQ() {

        cn.sharesdk.tencent.qq.QQ.ShareParams sp = new cn.sharesdk.tencent.qq.QQ.ShareParams();
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
