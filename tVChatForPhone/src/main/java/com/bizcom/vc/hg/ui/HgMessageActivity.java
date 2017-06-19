package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.MainApplication;
import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.util.DateUtil;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.bean.MessageBean;
import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.AudioVideoMessageBean.ChildMessageBean;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shdx.tvchat.phone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HgMessageActivity extends Activity {

    private HgMessageActivity mContext;
    private HeadLayoutManagerHG mHeadLayoutManager;
    private com.handmark.pulltorefresh.library.PullToRefreshListView mListView;
    private LayoutInflater mInflater;
    private MessageListAdpter mAdpter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hg_activity_message);

        mInflater = getLayoutInflater();
        mContext = this;
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getData();
        for (MessageBean bean : messageAllList) {
            if (bean.getState() == 1) {
                readMsg(bean);
            }
        }
        mAdpter.notifyDataSetChanged();

    }

    private void initView() {
        emptyView = getLayoutInflater().inflate(R.layout.empty_view2, null);
        String titleText = getIntent().getStringExtra("titleText");
        mAdpter = new MessageListAdpter();
        mListView = (com.handmark.pulltorefresh.library.PullToRefreshListView) findViewById(R.id.mList);
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mAdpter);
        findViewById(R.id.img_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.text_title)).setText(titleText);

    }


    class ViewTag {
        public SwipeLayout swipeLayout;
        public TextView tv_dName;
        public TextView tv_account;
        public TextView tv_call_status; //呼入 呼出状态
        public TextView tv_duration;
        public TextView tv_date;
        public SimpleDraweeView tv_Icon;
        public View ll_dowith_call;//包括呼入呼出
        public View text_not_owith_call; //未接电话
    }


    class MessageListAdpter extends BaseSwipeAdapter {

        @Override
        public int getCount() {
            return messageAllList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public void fillValues(int position, View convertView) {
            final MessageBean messageBean = messageAllList.get(position);
            final ViewTag tag;
            tag = new ViewTag();
            tag.tv_Icon = (SimpleDraweeView) convertView.findViewById(R.id.tv_Icon);
            tag.tv_dName = (TextView) convertView.findViewById(R.id.tv_dName);
            tag.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            tag.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);

            tag.tv_account = (TextView) convertView.findViewById(R.id.tv_account);
            tag.tv_call_status = (TextView) convertView.findViewById(R.id.tv_call_status);
            tag.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);

            //通过话 显示
            tag.ll_dowith_call = convertView.findViewById(R.id.ll_dowith_call);
            //未接电话时显示
            tag.text_not_owith_call = convertView.findViewById(R.id.text_not_owith_call);

            tag.swipeLayout = (SwipeLayout) convertView
                    .findViewById(getSwipeLayoutResourceId(position));
            convertView.setTag(tag);


            final User remoteUser = GlobalHolder.getInstance().getUser(messageBean.getRemoteUserID());

            if (messageBean.getCallType() == 4) {//4 呼入  3呼出
                tag.tv_call_status.setText("呼入");
            } else {
                tag.tv_call_status.setText("呼出");
            }
            UserHeaderImgHelper.display(tag.tv_Icon,remoteUser);
            //ImageLoader.getInstance().displayImage(remoteUser.getmAvatarLocation(), tag.tv_Icon, MainApplication.imgOptions);
            tag.tv_dName.setText(remoteUser.getDisplayName());
            tag.tv_account.setText(remoteUser.getAccount());
            tag.tv_date.setText(CreateDurationTime(messageBean.getHandleDate()));
            if (messageBean.getCallDuration() > 0) {
                tag.ll_dowith_call.setVisibility(View.VISIBLE);
                tag.text_not_owith_call.setVisibility(View.GONE);
                tag.tv_duration.setText(DateUtil.calculateTime(messageBean.getCallDuration()));
            } else {
                //被呼入尚未未接通就取消 未接
                if (messageBean.isCancelByMine == 1 && messageBean.getCallType() == 4) {
                    tag.ll_dowith_call.setVisibility(View.VISIBLE);
                    tag.text_not_owith_call.setVisibility(View.GONE);
                }
                //呼出尚未未接通就取消
                if (messageBean.isCancelByMine == 1 && messageBean.getCallType() == 3) {
                    tag.tv_duration.setText("已取消");
                } else {
                    tag.ll_dowith_call.setVisibility(View.GONE);
                    tag.text_not_owith_call.setVisibility(View.VISIBLE);
                }
            }


            //tag.tv_duration.setText(Html.fromHtml(info));
            //			tag.tv_Icon.setBackgroundResource(resid);

//            tag.im.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    tag.swipeLayout.close();
//                    if (remoteUser.getmStatus() == Status.ONLINE) {
//                        startVideoCall(remoteUser.getmUserId());
//                    } else {
//                        ToastUtil.ShowToast_long(mContext, "当前用户不在线");
//                    }
//
//                }
//            });


//		swipeLayout.addSwipeListener(new SimpleSwipeListener() {
//		@Override
//		public void onOpen(SwipeLayout layout) {
////			Toast.makeText(mContext, "Open", Toast.LENGTH_SHORT).show();
//		}
//	});
            // 添加删除布局的点击事件
            tag.swipeLayout.findViewById(R.id.delete).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    messageAllList.remove(messageBean);

                    String where = ContentDescriptor.HistoriesMedia.Cols.ID + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(messageBean.getId())};
                    int delete = getContentResolver().delete(ContentDescriptor.HistoriesMedia.CONTENT_URI, where,
                            selectionArgs);
                    if (delete == 0) {
//				ToastUtil.ShowToast_long(mContext, "删除失败");
                    } else {
//				ToastUtil.ShowToast_long(mContext, "删除成功");

                    }
                    tag.swipeLayout.close();
                    notifyDataSetChanged();

                }
            });

        }

        @Override
        public View generateView(int position, ViewGroup parent) {
            View convertView = null;

            convertView = mInflater.inflate(R.layout.hg_message_listitem, parent, false);

            return convertView;
        }

        @Override
        public int getSwipeLayoutResourceId(int arg0) {
            return R.id.swipe1;
        }


    }

    private void startVideoCall(long remoteUserId) {
        if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
            ToastUtil.ShowToast_long(mContext, "服务器连接异常,请稍后再试");
            return;
        }
        GlobalConfig.startP2PConnectChat(mContext,
                ConversationP2PAVActivity.P2P_CONNECT_VIDEO, remoteUserId,
                false, null, null);
    }
    //	/**
    //	 * 获取消息
    //	 */

    List<MessageBean> messageAllList = new ArrayList<MessageBean>();

    private void getData() {
        try {
            messageAllList.clear();
            MessageBean mb;
//			// 获取认证消息
//			List<FriendMAData> fDatas = VerificationProvider.loadFriendsVerifyMessages();
//			for (FriendMAData fdt : fDatas) {
//				// if (fdt.state == 0) {
//				// isUnReadMessage = true;// 有未读认证消息
//				// }
//				mb = new MessageBean();
//				mb.setMessageType(MessageBean.MESSAGE_TYPE_FRIEND);
//				mb.setId(fdt._id);
//				mb.setRemoteUserID(fdt.remoteUserID);
//				mb.setName(fdt.name);
//				messageAllList.sendFriendToTv(mb);
//			}

            // 获取视频通话消息
            List<AudioVideoMessageBean> videoMessageBeans = MediaRecordProvider.loadMediaHistoriesMessage(
                    GlobalHolder.getInstance().getCurrentUserId(), AudioVideoMessageBean.TYPE_ALL);
            // Bitmap photoBitmap;
            for (AudioVideoMessageBean vm : videoMessageBeans) {
                if (vm.name == null) {
                    break;
                }
                for (ChildMessageBean childMessageBean : vm.mChildBeans) {
                    // System.out.println("视频消息：" + vm.mChildBeans.size() + "=="
                    // + childMessageBean);
                    mb = new MessageBean();
                    mb.setMessageType(MessageBean.MESSAGE_TYPE_VIDEO);
                    mb.setId(childMessageBean.messageId);
                    mb.setName(vm.name);
                    mb.setRemoteUserID(vm.remoteUserID);
                    mb.setCallDuration(childMessageBean.childHoldingTime);
                    mb.setHandleDate(childMessageBean.childSaveDate);
                    mb.setCallType(childMessageBean.childISCallOut);
                    mb.setState(childMessageBean.childReadState);
                    mb.isCancelByMine = childMessageBean.isCancelByMine;
                    messageAllList.add(mb);// 已读消息直接追加

                }
            }

            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    mAdpter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
        }
    }

    public String CreateDurationTime(long holdingTime) {
        Date date = new Date(holdingTime);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        String dateStr = sdf.format(date);
        return dateStr;
    }


    //消息标记为已读
    public void readMsg(MessageBean messageBean) {
        String where = ContentDescriptor.HistoriesMedia.Cols.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(messageBean.getId())};
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE, 0);
        int update = getContentResolver().update(ContentDescriptor.HistoriesMedia.CONTENT_URI, mContentValues, where, selectionArgs);
        messageBean.setState(0);
    }

}
