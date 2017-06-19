package com.bizcom.vc.hg.view;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.DialogManager;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.ui.HgAddFriendsConfirmActivity;
import com.bizcom.vc.hg.ui.UpdateTvInfoActivity;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.web.LinkInfo;
import com.bizcom.vc.widget.cus.CustomDialog;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

public class SortAdapter extends BaseAdapter implements SectionIndexer {
    private List<PhoneFriendItem> list = null;
    private Context mContext;
    private Dialog mShareDialog;

    public SortAdapter(Context mContext, List<PhoneFriendItem> list) {
        this.mContext = mContext;
        this.list = list;
    }

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
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sort_listview, null);
            viewHolder.tv_firstNameText = (TextView) view.findViewById(R.id.firstNameText);
            viewHolder.tv_nameTxet = (TextView) view.findViewById(R.id.tv_nameTxet);
            viewHolder.tv_number = (TextView) view.findViewById(R.id.tv_numberText);
            viewHolder.mImageAddFriendFinish = (ImageView) view.findViewById(R.id.ic_add_friend_finish);
            viewHolder.mImageAddFriend = (ImageView) view.findViewById(R.id.ic_lately_addFriends);
            viewHolder.mImageShare = (ImageView) view.findViewById(R.id.ic_share);
            viewHolder.view = view.findViewById(R.id.view);
            viewHolder.icon = (SimpleDraweeView) view.findViewById(R.id.icon);
            viewHolder.tv1 = (TextView) view.findViewById(R.id.tvState);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (list.size() - 1 == position) {
            viewHolder.view.setVisibility(View.VISIBLE);
        } else {
            viewHolder.view.setVisibility(View.GONE);
        }

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);

        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == 0 && mContent.isHasRegsited()) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText("已注册");
        } else if (!mContent.isHasRegsited() && position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.getSortLetters());
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }


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
                loadImage(true, viewHolder, mContent.getPicUrl());
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
                FriendUtil.addConstant(mContext, mContent.getUserId(), new FriendUtil.AddFriendCallback() {
                    @Override
                    public void onResult(boolean isSuccess) {
                        if (isSuccess) {
                            showDialogEditCommentName(mContent.getUserId(), mContent.getName());
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

    public void showDialogEditCommentName(final long userId, final String name) {
        AlertMsgUtils.showEditCommentNameConfirm(mContext, name, new AlertMsgUtils.OnDialogStringBtnClickListener() {
            @Override
            public void onConfirm(final String commentName) {
                if (!TextUtils.isEmpty(commentName)) {
                    User user = GlobalHolder.getInstance().getUser(userId);
                    user.setCommentName(commentName);
                    new V2ImRequest().updateUserInfo(user, new HandlerWrap(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            Toast.makeText(mContext, "修改备注成功", Toast.LENGTH_SHORT).show();
                            MainApplication.updateContactUserCommentName(userId,commentName);
                            Intent intent = new Intent();
                            intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            intent.putExtra("modifiedUser", userId);
                            mContext.sendBroadcast(intent);
                        }
                    }, 0, null));
                }
            }
        });
    }


    final static class ViewHolder {
        public View view;
        TextView tvLetter;
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
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            PhoneFriendItem item = list.get(i);
            String sortStr = item.getSortLetters();
            if (!item.isHasRegsited()) {
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * 提取英文的首字母，非英文字母用#代替。
     *
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String sortStr = str.trim().substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

    @Override
    public Object[] getSections() {
        return null;
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
            View linkBt = mShareDialog.findViewById(R.id.shareToLink);
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
            linkBt.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mShareDialog.dismiss();
                    ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(LinkInfo.H5_CODE);
                    Toast.makeText(mContext, "链接已复制到剪切板", Toast.LENGTH_SHORT).show();
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
    private void loadImage(boolean b, ViewHolder viewHolder, String picurl) {
        if (b) {
            SimpleDraweeViewUtils.display(viewHolder.icon, viewHolder.tv_firstNameText, picurl);
        } else {
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