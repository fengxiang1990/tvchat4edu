package com.bizcom.vc.activity.main;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bizcom.util.DensityUtils;
import com.bizcom.vc.activity.SipActivity;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.activity.search.SearchLocalActivity;
import com.bizcom.vo.enums.NetworkStateCode;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

public class HeadLayoutManager {

    public static int TITLE_BAR_TYPE_PLUS = 1;
    public static int TITLE_BAR_TYPE_MORE = 2;

    private Context context;
    private View rootContainer;
    private View mNetworkNotificationView;
    private boolean mIsMainActivity;

    private List<Wrapper> additionList;
    private List<Wrapper> normalList;

    // R.drawable.title_bar_item_help_button
//    private int[] imgs = new int[]{R.drawable.title_bar_item_detail_button, R.drawable.title_bar_item_setting_button,
//            R.drawable.title_bar_item_about_button};
    private int[] imgs = new int[3];
    // R.string.title_bar_item_help
    private int[] items = new int[]{R.string.title_bar_item_detail, R.string.title_bar_item_setting,
            R.string.title_bar_item_about};

//    private int[] plusImgs = new int[]{R.drawable.conversation_video_button,
//            R.drawable.conversation_discussion_button,
//            R.drawable.conversation_seach_member_button};
    private int[] plusImgs = new int[]{
            R.drawable.conversation_video_button,
            R.drawable.conversation_discussion_button,
            R.drawable.conversation_seach_member_button,
            R.drawable.conversation_framgent_gray_button_bg};

//    private int[] plusItems = new int[]{R.string.conversation_popup_menu_video_call_button,
//            R.string.conversation_popup_menu_discussion_board_create_button, R.string.conversation_popup_menu_member_search_button,};
    private int[] plusItems = new int[]{
            R.string.conversation_popup_menu_video_call_button,
            R.string.conversation_popup_menu_discussion_board_create_button,
            R.string.conversation_popup_menu_member_search_button,
            R.string.title_bar_item_sip,};

    private PopupWindow mPlusPopWindow;
    private PopupWindow mMorePopWindow;
    private int mPopWindowMarginRight;
    private int mPopWindowChildPadding;

    private DisplayMetrics dm;

    public HeadLayoutManager(Context context, View rootContainer, boolean isMainActivity) {
        this.context = context;
        this.rootContainer = rootContainer;
        this.mIsMainActivity = isMainActivity;
        initHeadLayout();
    }

    private void initHeadLayout() {
        normalList = new ArrayList<>();
        additionList = new ArrayList<>();

        this.rootContainer.findViewById(R.id.ws_common_activity_title_right_button).setVisibility(View.INVISIBLE);
        this.rootContainer.findViewById(R.id.ws_activity_main_title_functionLy).setVisibility(View.VISIBLE);

        View searchButton = this.rootContainer.findViewById(R.id.ws_activity_main_function_title_person);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SearchLocalActivity.class);
                context.startActivity(i);
            }
        });

        View plusButton = this.rootContainer.findViewById(R.id.ws_activity_main_function_title_plus);
        plusButton.setVisibility(View.VISIBLE);
        plusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(0.5f);
                showPopPlusWindow(v);
            }
        });

        View moreButton = this.rootContainer.findViewById(R.id.ws_common_activity_title_function_more_button);
        moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                showPopMoreWindow(v);
                context.startActivity(new Intent(context, PersonalActivity.class));
            }
        });

        if (mIsMainActivity) {
            mNetworkNotificationView = this.rootContainer.findViewById(R.id.title_bar_notification);
        } else {
            TextView mLeftTitleTV = (TextView) this.rootContainer.
                    findViewById(R.id.ws_common_activity_title_left_button);
            mLeftTitleTV.setBackgroundResource(R.drawable.title_bar_back_button_selector);
            mLeftTitleTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) context).onBackPressed();
                }
            });
        }

        // Initialise popupWindow mPopWindowChildPadding
        mPopWindowChildPadding = DensityUtils.dip2px(context, 3);
        mPopWindowMarginRight = DensityUtils.dip2px(context, 22);

        dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    public void updateTitle(int resId) {
        TextView tv = (TextView) rootContainer.findViewById(R.id.ws_common_activity_title_content);
        tv.setText(resId);
    }

    public void updateTitle(String res) {
        TextView tv = (TextView) rootContainer.findViewById(R.id.ws_common_activity_title_content);
        tv.setText(res);
    }

    public void addAdditionalPopupMenuItem(View v, Object obj) {
        additionList.add(new Wrapper(v, obj));
    }

    public void addNormalPopupMenuItem(View v, Object obj) {
        normalList.add(new Wrapper(v, obj));
    }

    private void initMoreitemLayout(ViewGroup vg) {
        for (int i = 0; i < imgs.length; i++) {
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);

            ImageView iv = new ImageView(context);
            iv.setImageResource(imgs[i]);
            // iv.setPadding(10, 5, 5, 10);
            iv.setPadding(10, mPopWindowChildPadding, 5, mPopWindowChildPadding);
            LinearLayout.LayoutParams ivLL = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            ivLL.gravity = Gravity.RIGHT;
            ivLL.weight = 0.3F;

            ll.addView(iv, ivLL);

            TextView tv = new TextView(context);
            tv.setText(items[i]);
            // tv.setPadding(10, 15, 5, 15);
            tv.setPadding(10, mPopWindowChildPadding, 5, mPopWindowChildPadding);
            tv.setTextColor(Color.rgb(123, 123, 123));
            LinearLayout.LayoutParams tvLL = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvLL.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            tvLL.weight = 0.7F;

            ll.addView(tv, tvLL);
            ll.setOnClickListener(plusItemClickListener);

            ll.setId(items[i]);
            ll.setPadding(0, mPopWindowChildPadding, 15, mPopWindowChildPadding);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = 20;
            vg.addView(ll, lp);

            if (i != imgs.length - 1) {
                LinearLayout line = new LinearLayout(context);
                line.setBackgroundResource(R.color.common_line_color);
                LinearLayout.LayoutParams lineLL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        1);
                vg.addView(line, lineLL);
            }
        }
    }

    private void initPlusItemLayout() {
        int horizontalPadding = DensityUtils.dip2px(context , 15);
        int verticalPadding = DensityUtils.dip2px(context , 10);
        for (int i = 0; i < plusImgs.length; i++) {
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setBackgroundResource(R.drawable.ws_com_list_item_selector);

            ImageView iv = new ImageView(context);
            iv.setImageResource(plusImgs[i]);
            LinearLayout.LayoutParams ivLL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ivLL.leftMargin = horizontalPadding;
            ivLL.rightMargin = horizontalPadding;
            ivLL.topMargin = verticalPadding;
            ivLL.bottomMargin = verticalPadding;
            ivLL.width = DensityUtils.dip2px(context , 20);
            ll.addView(iv, ivLL);

            TextView tv = new TextView(context);
            tv.setText(plusItems[i]);
            tv.setTextColor(Color.rgb(123, 123, 123));
            LinearLayout.LayoutParams tvLL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvLL.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;

            ll.addView(tv, tvLL);
            ll.setOnClickListener(titleBarMenuItemClickListener);

            ll.setId(plusImgs[i]);
            addAdditionalPopupMenuItem(ll, null);
        }

    }

    public void updateConnectState(NetworkStateCode code) {
        if (mNetworkNotificationView == null) {
            return;
        }

        if (code != NetworkStateCode.CONNECTED) {
            mNetworkNotificationView.setVisibility(View.VISIBLE);
        } else {
            mNetworkNotificationView.setVisibility(View.GONE);
        }
    }

    public void dismiss() {
        additionList.clear();
        normalList.clear();
    }

    public void dismissPlusWindow() {
        if (mPlusPopWindow != null && mPlusPopWindow.isShowing()) {
            mPlusPopWindow.dismiss();
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        Window window = ((Activity) context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        window.setAttributes(lp);
    }

    private OnClickListener titleBarMenuItemClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            dismissPlusWindow();
            if (GlobalHolder.getInstance().checkServerConnected(context)) {
                return;
            }

            Intent i = new Intent();
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int id = view.getId();
            switch (id) {
                case R.drawable.conversation_video_button:
                    i.setAction(PublicIntent.START_CONFERENCE_CREATE_ACTIVITY);
                    break;
                case R.drawable.conversation_discussion_button:
                    i.setAction(PublicIntent.START_DISCUSSION_BOARD_CREATE_ACTIVITY);
                    break;
                case R.drawable.conversation_seach_member_button:
                    i.setAction(PublicIntent.START_SEARCH_ACTIVITY);
                    // For member search
                    i.putExtra("type", 0);
                    break;
                case R.drawable.conversation_framgent_gray_button_bg:
    				i.setClass(context, SipActivity.class);
    				break;
                case R.drawable.conversation_sms_button:
                    break;
                case R.drawable.conversation_email_button:
                    break;
                case R.drawable.conversation_files_button:
                    break;
            }
            context.startActivity(i);
        }
    };

    private OnClickListener plusItemClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int id = v.getId();
            switch (id) {
                case R.string.title_bar_item_setting:
                    intent.setAction(PublicIntent.START_SETTING_ACTIVITY);
                    break;
                case R.string.title_bar_item_detail:
                    intent.setClass(context, ContactDetail2.class);
                    intent.putExtra("uid", GlobalHolder.getInstance().getCurrentUserId());
                    break;
                case R.string.title_bar_item_about:
                    intent.setAction(PublicIntent.START_ABOUT_ACTIVITY);
                    break;
                default:
                    return;
            }
            context.startActivity(intent);
            mMorePopWindow.dismiss();
        }

    };

    private void showPopPlusWindow(View anchor) {
        if (mPlusPopWindow == null) {
            initPlusItemLayout();
            if (additionList.size() <= 0) {
                return;
            }

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.title_bar_pop_up_window, null);
            LinearLayout itemContainer = (LinearLayout) layout.findViewById(R.id.common_pop_window_container);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.LEFT;
//            int padding = DensityUtils.dip2px(context, 8);
            for (int i = 0; i < additionList.size(); i++) {
//                if (i == 0) {
//                    additionList.get(i).v.setPadding(0, padding, 0, padding);
//                } else if (i == additionList.size() - 1) {
//                    additionList.get(i).v.setPadding(0, padding, 0, padding);
//                }
                itemContainer.addView(additionList.get(i).v, lp);

                if (i != additionList.size() - 1) {
                    LinearLayout line = new LinearLayout(context);
                    line.setBackgroundResource(R.color.common_line_color);
                    LinearLayout.LayoutParams lineLL = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    itemContainer.addView(line, lineLL);
                }
            }

            mPlusPopWindow = buildPopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int[] pos = new int[2];
        anchor.getLocationInWindow(pos);
        pos[1] += anchor.getMeasuredHeight() - anchor.getPaddingBottom();
        // calculate arrow offset
        mPlusPopWindow.setAnimationStyle(R.style.TitleBarPopupWindowAnim);
        mPlusPopWindow.showAtLocation(anchor, Gravity.TOP | Gravity.RIGHT, mPopWindowMarginRight, pos[1]);
    }

    private void showPopMoreWindow(View anchor) {
        if (mMorePopWindow == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.title_bar_pop_up_window, null);
            LinearLayout itemContainer = (LinearLayout) layout.findViewById(R.id.common_pop_window_container);

            initMoreitemLayout(itemContainer);

            itemContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            View arrow = layout.findViewById(R.id.common_pop_up_arrow_up);
            arrow.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            int height = itemContainer.getMeasuredHeight() + arrow.getMeasuredHeight();

            mMorePopWindow = buildPopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, height);

        }

        int[] pos = new int[2];
        anchor.getLocationInWindow(pos);
        pos[1] += anchor.getMeasuredHeight() - anchor.getPaddingBottom();
        // calculate arrow offset
        View arrow = mMorePopWindow.getContentView().findViewById(R.id.common_pop_up_arrow_up);
        arrow.bringToFront();

        RelativeLayout.LayoutParams arrowRL = (RelativeLayout.LayoutParams) arrow.getLayoutParams();
        arrowRL.rightMargin = dm.widthPixels - pos[0] - (anchor.getMeasuredWidth() / 2) - arrow.getMeasuredWidth();
        arrow.setLayoutParams(arrowRL);

        mMorePopWindow.setAnimationStyle(R.style.TitleBarPopupWindowAnim);
        mMorePopWindow.showAtLocation(anchor, Gravity.RIGHT | Gravity.TOP, mPopWindowMarginRight, pos[1]);
    }

    private PopupWindow buildPopupWindow(View view, int width, int height) {

        final PopupWindow pw = new PopupWindow(view, width, height, true);
        pw.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                pw.dismiss();
            }

        });
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        pw.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        pw.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        pw.setFocusable(true);
        pw.setTouchable(true);
        pw.setOutsideTouchable(true);
        pw.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        return pw;
    }

    class Wrapper {
        View v;
        Object o;

        public Wrapper(View v, Object o) {
            super();
            this.v = v;
            this.o = o;
        }

    }

    interface TitleBarMenuItemClickListener {
        public void onClick(View view, Object obj);
    }

}
