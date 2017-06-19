package com.bizcom.vc.activity.conference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bizcom.util.DensityUtils;
import com.bizcom.util.V2Log;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.ContactConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupLayout extends LinearLayout {
    private static final String TAG = GroupLayout.class.getSimpleName();

    private CustomAvatarImageView mAvatarIV;
    // 头像上显示的红点提示
    private ImageView mNotificatorIV;
    private TextView mTopContentTV;
    private TextView mBelowConentTV;
    //时间显示上下两行
    private TextView mTopDateTV;
    private TextView mMiddleContentTV;
    private TextView mBelowDateTV;
    private TextView mSearchLocalBlack;
    //每个Item之间的分割线
    private View mDividerTV;
    private View mConvGroupLayoutLy;

    public GroupLayout(Context context, Conversation group) {
        super(context);
        init(group);
    }

    private void init(Conversation mConv) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.conversation_view, null, true);
        mConvGroupLayoutLy = view.findViewById(R.id.common_conversation_ly);
        mAvatarIV = (CustomAvatarImageView) view.findViewById(R.id.ws_common_avatar);
        mNotificatorIV = (ImageView) view.findViewById(R.id.ws_common_conversation_layout_notificator);
        mNotificatorIV.bringToFront();
        mTopContentTV = (TextView) view.findViewById(R.id.ws_common_conversation_layout_topContent);
        mMiddleContentTV = (TextView) view.findViewById(R.id.ws_common_conversation_middle_content);
        mBelowConentTV = (TextView) view.findViewById(R.id.ws_common_conversation_layout_belowContent);
        mSearchLocalBlack = (TextView) view.findViewById(R.id.common_conversation_search_black);

        mTopDateTV = (TextView) view.findViewById(R.id.common_conversation_time_year);
        mBelowDateTV = (TextView) view.findViewById(R.id.common_conversation_time_hour);
        mDividerTV = view.findViewById(R.id.common_conversation_divier);

        switch (mConv.getType()) {
            case Conversation.TYPE_CONFERNECE:
                if (((ConferenceConversation) mConv).getGroup().getOwnerUser().getmUserId() != GlobalHolder.getInstance()
                        .getCurrentUserId())
                    setConferenceIcon(R.drawable.ic_list_conversation_conference);
                else
                    setConferenceIcon(R.drawable.conference_icon_self);
                break;
            case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                mAvatarIV.setImageResource(R.drawable.chat_group_icon);
                mTopDateTV.setVisibility(View.INVISIBLE);
                mBelowDateTV.setVisibility(View.INVISIBLE);
                break;
            case Conversation.TYPE_GROUP:
            case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                if (mConv.getType() == Conversation.TYPE_GROUP) {
                    mAvatarIV.setImageResource(R.drawable.chat_group_icon);
                } else {
                    mAvatarIV.setImageResource(R.drawable.chat_group_discussion_icon);
                }
                mTopContentTV.setVisibility(View.INVISIBLE);
                mBelowConentTV.setVisibility(View.INVISIBLE);
                mTopDateTV.setVisibility(View.INVISIBLE);
                mBelowDateTV.setVisibility(View.INVISIBLE);
                mMiddleContentTV.setVisibility(View.VISIBLE);
                break;
            // case Conversation.TYPE_GROUP:
            // mAvatarIV.setImageResource(R.drawable.chat_group_icon);
            // mTopDateTV.setVisibility(View.INVISIBLE);
            // mBelowDateTV.setVisibility(View.INVISIBLE);
            // if (interfaceType == INTERFACE_TYPE_ORG && mGroupLayout != null) {
            // RelativeLayout.LayoutParams layoutParams =
            // (RelativeLayout.LayoutParams) mGroupLayout.getLayoutParams();
            // if (layoutParams != null) {
            // layoutParams.leftMargin += 35;
            // mGroupLayout.setLayoutParams(layoutParams);
            // }
            // }
            // break;
            case Conversation.TYPE_VOICE_MESSAGE:
                mAvatarIV.setImageResource(R.drawable.root_my_accepty);
                break;
            case Conversation.TYPE_VERIFICATION_MESSAGE:
                mAvatarIV.setImageResource(R.drawable.vs_message_verification);
                break;
            case Conversation.TYPE_SEARCH_TAB:
                updateSearchLocalTabLy();
                break;
            case Conversation.TYPE_SEARCH_MORE:
                updateSearchLocalMoreLy();
                break;
        }
        addView(view);
    }

    private void initNickName(Conversation mConv) {
        User currentUser = null;
        if (mConv.getType() == V2GlobalConstants.GROUP_TYPE_USER) {
            ContactConversation con = (ContactConversation) mConv;
            currentUser = con.getUser();
        }

        if (currentUser != null) {
            boolean isFriend = GlobalHolder.getInstance().isFriend(currentUser);
            String nickName = currentUser.getCommentName();
            if (isFriend && !TextUtils.isEmpty(nickName))
                mTopContentTV.setText(nickName);
            else
                mTopContentTV.setText(mConv.getName());
        } else {
            V2Log.e(TAG,
                    "updateName ---> get current user is null or id is -1 , please check conversation user is exist");
        }
    }

    public void updateVoiceItem(Conversation mConv) {
        if (mConv.getReadFlag() == V2GlobalConstants.READ_STATE_UNREAD) {
            mNotificatorIV.setVisibility(View.VISIBLE);
        } else {
            mNotificatorIV.setVisibility(View.GONE);
        }
        String[] dateCovString = mConv.getDateCovString();
        setConversationDate(dateCovString);
    }

    /**
     * 用于更新群组和会议
     *
     * @param group
     * @param readFlag
     * @param isVisibileDivier
     */
    public void updateGroupContent(Group group, int readFlag, boolean isVisibileDivier) {
        if (group == null || group.getGroupType() == V2GlobalConstants.GROUP_TYPE_DISCUSSION)
            return;

        User currentUser = group.getOwnerUser();
        if (currentUser == null)
            return;

        if (group.getGroupType() == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
            // 未读和已读标识
            if (readFlag == V2GlobalConstants.READ_STATE_READ) {
                updateConversationNotificator(false);
            } else {
                updateConversationNotificator(true);
            }

            // 头像
            if (currentUser.getmUserId() != GlobalHolder.getInstance().getCurrentUserId())
                setConferenceIcon(R.drawable.ic_list_conversation_conference);
            else
                setConferenceIcon(R.drawable.conference_icon_self);
        }

        if (TextUtils.isEmpty(currentUser.getDisplayName())) {
            currentUser = GlobalHolder.getInstance().getUser(currentUser.getmUserId());
        }

        mMiddleContentTV.setText(group.getName());
        mTopContentTV.setText(group.getName());
        String[] strCovFormatDate = group.getStrCovFormatDate();
        setConversationDate(strCovFormatDate);
        mBelowConentTV.setText(
                getResources().getString(R.string.conference_groupLayout_creation) + currentUser.getDisplayName());

        // 是否显示分割线
        if (isVisibileDivier) {
            mDividerTV.setVisibility(View.VISIBLE);
        } else {
            mDividerTV.setVisibility(View.INVISIBLE);
        }
    }

    public void update(Conversation mConv, boolean isFromCrowd, boolean isVisibileDivier) {
        if (mConv.getType() == V2GlobalConstants.GROUP_TYPE_USER) {
            Bitmap bp = ((ContactConversation) mConv).getAvatar();
            updateIcon(bp);
            initNickName(mConv);
        } else {
            mMiddleContentTV.setText(mConv.getName());
            mTopContentTV.setText(mConv.getName());
        }
        mBelowConentTV.setText(mConv.getMsg());
        String[] dateCovString = mConv.getDateCovString();
        setConversationDate(dateCovString);

        if (!isFromCrowd) {
            if (mConv.getReadFlag() == V2GlobalConstants.READ_STATE_UNREAD) {
                mNotificatorIV.setVisibility(View.VISIBLE);
            } else {
                mNotificatorIV.setVisibility(View.GONE);
            }
        }

        // 是否显示分割线
        if (isVisibileDivier) {
            mDividerTV.setVisibility(View.VISIBLE);
        } else {
            mDividerTV.setVisibility(View.INVISIBLE);
        }
    }

    public void updateIcon(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            mAvatarIV.setImageBitmap(bitmap);
        }
    }

    public void updateCrowdLayout() {
        mTopDateTV.setVisibility(View.VISIBLE);
        mBelowDateTV.setVisibility(View.VISIBLE);
    }

    public void updateDiscussionLayout(boolean isShowContactLayout) {
        if (isShowContactLayout) {
            mTopContentTV.setVisibility(View.VISIBLE);
            mBelowConentTV.setVisibility(View.VISIBLE);
            mTopDateTV.setVisibility(View.VISIBLE);
            mMiddleContentTV.setVisibility(View.INVISIBLE);
        } else {
            mTopContentTV.setVisibility(View.INVISIBLE);
            mBelowConentTV.setVisibility(View.INVISIBLE);
            mTopDateTV.setVisibility(View.INVISIBLE);
            mMiddleContentTV.setVisibility(View.VISIBLE);
        }
    }

    public void updateConversationNotificator(boolean readFlag) {
        if (readFlag) {
            mNotificatorIV.setVisibility(View.VISIBLE);
        } else {
            mNotificatorIV.setVisibility(View.GONE);
        }
    }

    public void updateSearchLocalConversation(Conversation mConv, String searchTabName,
                                              boolean isEndIndex , String keyString) {
        if (isEndIndex) {
            mSearchLocalBlack.setVisibility(View.VISIBLE);
        } else {
            mSearchLocalBlack.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams mAvatarParams = (RelativeLayout.LayoutParams)
                mAvatarIV.getLayoutParams();
        mAvatarParams.leftMargin = 0;
        mAvatarIV.setLayoutParams(mAvatarParams);

        if (searchTabName != null) {
            if (mConv.getType() == Conversation.TYPE_SEARCH_TAB) {
                updateSearchLocalTabLy();
                mMiddleContentTV.setText(searchTabName);
                mMiddleContentTV.setTextColor(getResources().getColor(R.color.searchlocal_message_final_tab));
            } else {
                updateSearchLocalMoreLy();
                mAvatarIV.setImageResource(R.drawable.ic_searchlocal_hint);
                mAvatarIV.setOval(false);
                mMiddleContentTV.setText(searchTabName);
                mMiddleContentTV.setTextColor(getResources().getColor(R.color.common_item_text_color_blue));
            }
        } else {
            // 恢复默认的设置
            mAvatarIV.setOval(true);
            mConvGroupLayoutLy.setBackgroundResource(R.drawable.ws_com_list_item_selector);
            mTopDateTV.setVisibility(View.GONE);
            mDividerTV.setVisibility(View.VISIBLE);
            mMiddleContentTV.setTextColor(getResources().getColor(R.color.common_item_text_color_black));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    mMiddleContentTV.getLayoutParams();
            layoutParams.leftMargin = DensityUtils.dip2px(getContext(), 15);
            mMiddleContentTV.setLayoutParams(layoutParams);
            RelativeLayout.LayoutParams mDividerParams = (RelativeLayout.LayoutParams)
                    mDividerTV.getLayoutParams();
            mDividerParams.leftMargin = DensityUtils.dip2px(getContext(), 70);
            mDividerTV.setLayoutParams(mDividerParams);

            if (mConv.getType() == Conversation.TYPE_SEARCH_NORMAL) {
                mTopContentTV.setVisibility(View.GONE);
                mMiddleContentTV.setVisibility(View.VISIBLE);
                mBelowConentTV.setVisibility(View.GONE);
                mAvatarIV.setVisibility(View.VISIBLE);

                Bitmap bp = ((ContactConversation) mConv).getAvatar();
                updateIcon(bp);
                SpannableStringBuilder text = highLight(mConv.getName(), keyString);
                mMiddleContentTV.setText(text);
            } else if (mConv.getType() == Conversation.TYPE_DISCUSSION
                    || mConv.getType() == Conversation.TYPE_CONFERNECE) {
                mMiddleContentTV.setVisibility(View.VISIBLE);
                mAvatarIV.setVisibility(View.VISIBLE);
                mTopContentTV.setVisibility(View.GONE);
                mBelowConentTV.setVisibility(View.GONE);

                if(mConv.getType() == Conversation.TYPE_DISCUSSION){
                    SpannableStringBuilder text = highLight(mConv.getName(), keyString);
                    mMiddleContentTV.setText(text);
                    mAvatarIV.setImageResource(R.drawable.chat_group_discussion_icon);
                } else {
                    SpannableStringBuilder text = highLight(mConv.getName(), keyString);
                    mMiddleContentTV.setText(text);
                    Group group = ((ConferenceConversation) mConv).getGroup();
                    User currentUser = group.getOwnerUser();
                    if (currentUser.getmUserId() != GlobalHolder.getInstance().getCurrentUserId())
                        mAvatarIV.setImageResource(R.drawable.ic_list_conversation_conference);
                    else
                        mAvatarIV.setImageResource(R.drawable.conference_icon_self);
                }
            } else if (mConv.getType() == Conversation.TYPE_CONTACT) {
                mTopContentTV.setVisibility(View.VISIBLE);
                mMiddleContentTV.setVisibility(View.GONE);
                mBelowConentTV.setVisibility(View.VISIBLE);
                mAvatarIV.setVisibility(View.VISIBLE);
                mTopDateTV.setVisibility(View.VISIBLE);

                if(mConv.getSearchLocalMsgCovType() != -1){
                    switch (mConv.getSearchLocalMsgCovType()){
                        case Conversation.TYPE_CONTACT:
                            Bitmap bp = ((ContactConversation) mConv).getAvatar();
                            updateIcon(bp);
                            break;
                        case Conversation.TYPE_DISCUSSION:
                            mAvatarIV.setImageResource(R.drawable.chat_group_discussion_icon);
                            break;
                        case Conversation.TYPE_DEPARTMENT:
                            mAvatarIV.setImageResource(R.drawable.chat_group_icon);
                            break;
                    }
                    String[] dateCovString = mConv.getDateCovString();
                    setConversationDate(dateCovString);
                    mTopContentTV.setText(mConv.getName());
                    mBelowConentTV.setText(mConv.getMsg());
                }
            }
        }
    }

    public void updateSearchLocalTabLy() {
        mAvatarIV.setVisibility(View.GONE);
        mTopContentTV.setVisibility(View.GONE);
        mBelowConentTV.setVisibility(View.GONE);
        mConvGroupLayoutLy.setBackgroundResource(R.color.common_activity_top_backgroud);
        mMiddleContentTV.setVisibility(View.VISIBLE);
        mMiddleContentTV.setTextColor(getResources().getColor(R.color.common_item_text_color_gray));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                mMiddleContentTV.getLayoutParams();
        layoutParams.leftMargin = 0;
        mMiddleContentTV.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams mDividerParams = (RelativeLayout.LayoutParams)
                mDividerTV.getLayoutParams();
        mDividerParams.leftMargin = DensityUtils.dip2px(getContext(), 15);
        mDividerTV.setLayoutParams(mDividerParams);
        mTopDateTV.setVisibility(View.GONE);
        mBelowDateTV.setVisibility(View.GONE);
    }

    public void updateSearchLocalMoreLy() {
        mAvatarIV.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams mAvatarParams = (RelativeLayout.LayoutParams)
                mAvatarIV.getLayoutParams();
        mAvatarParams.leftMargin = DensityUtils.dip2px(getContext(), 15);
        mAvatarIV.setLayoutParams(mAvatarParams);
        mMiddleContentTV.setVisibility(View.VISIBLE);
        mDividerTV.setVisibility(View.GONE);
        mTopContentTV.setVisibility(View.GONE);
        mBelowConentTV.setVisibility(View.GONE);
        mTopDateTV.setVisibility(View.GONE);
        mBelowDateTV.setVisibility(View.GONE);
    }

    private void setConferenceIcon(int resID) {
        Object tag = mAvatarIV.getTag();
        if (tag != null) {
            int id = (int) mAvatarIV.getTag();
            if (id == resID)
                return;
        }

        mAvatarIV.setImageResource(resID);
        mAvatarIV.setTag(resID);
    }

    private void setConversationDate(String[] dates) {
        if (dates != null) {
            if (dates.length == 1) {
//              RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mGroupDateTV.getLayoutParams();
//				layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//				mTopDateTV.setLayoutParams(layoutParams);
                mTopDateTV.setText(dates[0]);
                mBelowDateTV.setVisibility(View.INVISIBLE);
            } else {
                mTopDateTV.setText(dates[0]);
                mBelowDateTV.setText(dates[1]);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTopDateTV.getLayoutParams();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams.width,
                        layoutParams.height);
                mTopDateTV.setLayoutParams(params);
                mBelowDateTV.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 关键字高亮显示
     *
     * @param target 需要高亮的关键字
     * @param text   需要显示的文字
     * @return spannable 处理完后的结果，记得不要toString()，否则没有效果
     */
    public static SpannableStringBuilder highLight(String text, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
//        CharacterStyle span = null;
//        Pattern p = Pattern.compile(target);
//        Matcher m = p.matcher(text);
//        while (m.find()) {
//            span = new ForegroundColorSpan(Color.RED);// 需要重复！
//            spannable.setSpan(span, m.start(), m.end(),
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
        return spannable;
    }
}
