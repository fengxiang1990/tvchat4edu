package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.DialogManager;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.SimpleDraweeViewUtils;
import com.bizcom.vc.hg.util.CorlorUtil;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.util.List;

/**
 * Created by HG on 2016/11/1.
 */

public class LatelyAdapter extends BaseAdapter {
    private String num = "0123456789";// 用于判断首字母是否含有数据
    private List<User> list = null;
    private Activity activity;
    private LayoutInflater mInflater;
    private SecondTab2 secondTab2;
    private Dialog deleteContactDialog;

    public final static int TYPE_LATELY = 0;
    public final static int TYPE_LATELY_SEARCH = 1;
    private int mType = 0;

    public LatelyAdapter(Activity activity, List<User> list, SecondTab2 secondTab2) {
        this.activity = activity;
        this.list = list;
        mInflater = LayoutInflater.from(activity);
        this.secondTab2 = secondTab2;
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < list.size()) {
            return list.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        if (i < list.size()) {
            User user = list.get(i);
            if (user != null) {
                return user.getmUserId();
            }
        }
        return -1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.activity_lately_item, null);
            viewHolder.tv_firstNameText = (TextView) view.findViewById(R.id.tv_lately_firstNameText);
            viewHolder.icon = (SimpleDraweeView) view.findViewById(R.id.icon);
            viewHolder.tv_nameTxet = (TextView) view.findViewById(R.id.tv_nameTxet);
            viewHolder.tv_number = (TextView) view.findViewById(R.id.tv_numberText);
            viewHolder.addFriend = (ImageView) view.findViewById(R.id.ic_lately_addFriends);
            viewHolder.tv1 = (TextView) view.findViewById(R.id.tvState);
            viewHolder.mImageAddFriendFinish = (ImageView) view.findViewById(R.id.ic_add_friend_finish);
//            viewHolder.videoStart = view.findViewById(R.id.ll_lately_video);
            viewHolder.delLately = view.findViewById(R.id.tv_lately_delete);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (list != null && !list.isEmpty()) {
            final User u = list.get(i);
            String portraitPath = u.getmAvatarLocation();
            if (TextUtils.isEmpty(portraitPath)) {// 服务器图片是否存在
                viewHolder.tv_firstNameText.setVisibility(View.VISIBLE);
                viewHolder.icon.setVisibility(View.GONE);
                viewHolder.tv_firstNameText.setText(MessageUtil.getFirstLetterName(u.getDisplayName()));
            } else {
                viewHolder.tv_firstNameText.setVisibility(View.GONE);
                viewHolder.icon.setVisibility(View.VISIBLE);
                SimpleDraweeViewUtils.display(viewHolder.icon, portraitPath);
            }
            viewHolder.tv_nameTxet.setText(u.getDisplayName());
            viewHolder.tv_number.setText(u.getAccount());
            boolean isf = GlobalHolder.getInstance().isFriend(u);
            if (isf) {
                viewHolder.addFriend.setVisibility(View.GONE);
                viewHolder.mImageAddFriendFinish.setVisibility(View.VISIBLE);
            } else {
                viewHolder.addFriend.setVisibility(View.VISIBLE);
                viewHolder.mImageAddFriendFinish.setVisibility(View.GONE);
                viewHolder.addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteContactDialog = DialogManager.getInstance()
                                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(activity, null,
                                        "是否添加" + CorlorUtil.CreatColortext(u.getDisplayName()) + "为好友？",
                                        activity.getText(R.string.conversation_quit_dialog_confirm_text),
                                        activity.getText(R.string.conversation_quit_dialog_cancel_text)) {

                                    @Override
                                    public void confirmCallBack() {
                                        deleteContactDialog.dismiss();
                                        FriendUtil.addConstant(activity, u.getmUserId(), new FriendUtil.AddFriendCallback() {
                                            @Override
                                            public void onResult(boolean isSuccess) {
                                                if (isSuccess) {
                                                    if (mType == TYPE_LATELY_SEARCH) {
                                                        notifyDataSetChanged();
                                                    }
                                                    showDialogEditCommentName(u.getmUserId(), u.getNickName());
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void cannelCallBack() {
                                        deleteContactDialog.dismiss();
                                    }
                                });

                        deleteContactDialog.show();


                    }
                });
            }

            if (u.getmStatus() == User.Status.ONLINE) {
                viewHolder.tv1.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv1.setVisibility(View.GONE);
            }
            viewHolder.delLately.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int result = MediaRecordProvider.deleteMediaMessage(u.getmUserId());
                    if (result > 0) {
                        view.setVisibility(View.GONE);
                        Toast.makeText(activity, "记录删除成功~", Toast.LENGTH_SHORT).show();
                        secondTab2.mCurrentUser.remove(u);
                        secondTab2.notifyAdapter();
                    } else {
                        Toast.makeText(activity, "记录删除失败~", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return view;
    }

    private class ViewHolder {
        TextView tv1;
        TextView tv_firstNameText;
        SimpleDraweeView icon;
        TextView tv_nameTxet;
        TextView tv_number;
        ImageView addFriend;
        ImageView mImageAddFriendFinish;
        //        View videoStart;
        View delLately;
    }


    public void showDialogEditCommentName(final long userId, final String name) {
        AlertMsgUtils.showEditCommentNameConfirm(activity, name, new AlertMsgUtils.OnDialogStringBtnClickListener() {
            @Override
            public void onConfirm(final String commentName) {
                if (!TextUtils.isEmpty(commentName)) {
                    User user = GlobalHolder.getInstance().getUser(userId);
                    user.setCommentName(commentName);
                    new V2ImRequest().updateUserInfo(user, new HandlerWrap(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            Toast.makeText(activity, "修改备注成功", Toast.LENGTH_SHORT).show();
                            MainApplication.updateContactUserCommentName(userId, commentName);
                            Intent intent = new Intent();
                            intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                            intent.putExtra("modifiedUser", userId);
                            activity.sendBroadcast(intent);
                        }
                    }, 0, null));
                }
            }
        });
    }
}
