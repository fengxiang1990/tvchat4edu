package com.bizcom.vc.hg.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.hg.ui.HTab2;
import com.bizcom.vc.hg.ui.HgAddFriendActivity;
import com.bizcom.vc.hg.ui.HomeActivity;
import com.bizcom.vc.hg.ui.MipcaActivityCapture;
import com.bizcom.vc.hg.ui.PersonalCenterActivity;
import com.bizcom.vc.hg.ui.SearchFriendActivity;
import com.bizcom.vc.hg.ui.ThirdTabUnBind;
import com.bizcom.vo.enums.NetworkStateCode;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.List;

public class HeadLayoutManagerHG {

    public static int TITLE_BAR_TYPE_PLUS = 1;
    public static int TITLE_BAR_TYPE_MORE = 2;

    private View search;
    private Activity context;
    public View rootContainer;
    private View mNetworkNotificationView;
    private boolean mIsMainActivity;

    private List<Wrapper> additionList;
    private List<Wrapper> normalList;
    private int mTab2CurrentIndex = 0;

    // R.drawable.title_bar_item_help_button
    //    private int[] imgs = new int[]{R.drawable.title_bar_item_detail_button, R.drawable.title_bar_item_setting_button,
    //            R.drawable.title_bar_item_about_button};
    private int[] imgs = new int[3];
    // R.string.title_bar_item_help
    private int[] items = new int[]{R.string.title_bar_item_detail, R.string.title_bar_item_setting,
            R.string.title_bar_item_about};

    private int[] plusImgs = new int[]{R.mipmap.ic_add_friend,
            R.mipmap.ic_scan_friend};

    private String[] plusItems = {"添加好友",
            "扫码加好友"};

    private PopupWindow mPlusPopWindow;
    private PopupWindow mMorePopWindow;
    private int mPopWindowMarginRight;
    private int mPopWindowChildPadding;

    private DisplayMetrics dm;

    public HeadLayoutManagerHG(Context context, View rootContainer, boolean isMainActivity) {
        this.context = (Activity) context;
        this.rootContainer = rootContainer;
        this.mIsMainActivity = isMainActivity;
        initHeadLayout();
    }

    //设置TV协助帮助按钮的显示
    public void setTVHelpVisibility(int vi) {
        this.rootContainer.findViewById(R.id.img_tv_help).setVisibility(vi);
    }

    //设置搜索按钮的显示
    public void setSearchVisibility(int vi) {
        this.rootContainer.findViewById(R.id.titleSearch).setVisibility(vi);
    }

    //设置搜索按钮的显示
    public void setAddVisibility(int vi) {
        this.rootContainer.findViewById(R.id.titleAdd).setVisibility(vi);
    }

    public void setSearchAction(int visibility, int page) {
        mTab2CurrentIndex = page;
        if(search!=null){
            search.setVisibility(visibility);
        }
    }

    private void initHeadLayout() {
        normalList = new ArrayList<>();
        additionList = new ArrayList<>();
        if (mIsMainActivity) {
            this.rootContainer.findViewById(R.id.titlePerson).setVisibility(View.VISIBLE);
            this.rootContainer.findViewById(R.id.titleAdd).setVisibility(View.VISIBLE);
            this.rootContainer.findViewById(R.id.titleSearch).setVisibility(View.VISIBLE);
            this.rootContainer.findViewById(R.id.back).setVisibility(View.GONE);

        } else {
            this.rootContainer.findViewById(R.id.titlePerson).setVisibility(View.GONE);
            this.rootContainer.findViewById(R.id.titleAdd).setVisibility(View.GONE);
            this.rootContainer.findViewById(R.id.titleSearch).setVisibility(View.GONE);
            this.rootContainer.findViewById(R.id.back).setVisibility(View.VISIBLE);
        }

        final View searchButton = this.rootContainer.findViewById(R.id.titleAdd);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopPlusWindow(searchButton);
            }
        });


        ImageView moreButton = (ImageView) this.rootContainer.findViewById(R.id.titlePerson);
        moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //                showPopMoreWindow(v);
                Intent i = new Intent(context, PersonalCenterActivity.class);
                i.putExtra("titleText", "个人中心");
                context.startActivityForResult(i, HomeActivity.HomeRequestCode);
                context.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        search = this.rootContainer.findViewById(R.id.titleSearch);
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SearchFriendActivity.class);
                i.putExtra(SearchFriendActivity.EXTRA_CURRENT_INDEX,mTab2CurrentIndex);
                context.startActivity(i);
            }
        });
        View back = this.rootContainer.findViewById(R.id.back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				Intent i = new Intent(context, HSearcActivity.class);
                context.finish();
            }
        });


        //        // Initialise popupWindow mPopWindowChildPadding
        //        mPopWindowChildPadding = DensityUtils.dip2px(context, 3);
        //        mPopWindowMarginRight = DensityUtils.dip2px(context, 22);
        //
        //        dm = new DisplayMetrics();
        //        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    public void updateTitle(int resId) {
        TextView tv = (TextView) rootContainer.findViewById(R.id.ws_common_activity_title_content);
        tv.setText(resId);
    }

    public void updateTitle(String res) {
        TextView tv = (TextView) rootContainer.findViewById(R.id.ws_common_activity_title_content);
        if (tv != null && !TextUtils.isEmpty(res)) {
            tv.setText(res);
        }
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
        int horizontalPadding = DensityUtils.dip2px(context, 15);
        int verticalPadding = DensityUtils.dip2px(context, 10);
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
            ivLL.width = DensityUtils.dip2px(context, 20);
            ll.addView(iv, ivLL);

            TextView tv = new TextView(context);
            tv.setText(plusItems[i]);
            tv.setTextColor(Color.rgb(123, 123, 123));
            LinearLayout.LayoutParams tvLL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvLL.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

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
            int id = view.getId();
            switch (id) {
                case R.mipmap.ic_scan_friend://扫码加好友
                    Intent i = new Intent(context, MipcaActivityCapture.class);
                    i.putExtra("titleText", "扫一扫");
                    context.startActivityForResult(i, HTab2.SCANNIN_USER_CODE);
//                    Intent i = new Intent(context, HgScanToAddFriend.class);
//                    i.putExtra("titleText", "扫一扫");
//                    context.startActivityForResult(i, HomeActivity.SCANNIN_GREQUEST_CODE);
                    break;
                case R.mipmap.ic_add_friend://号码加好友
                    Intent i2 = new Intent(context, HgAddFriendActivity.class);
                    i2.putExtra("titleText", "添加好友");
                    context.startActivity(i2);
                    break;
            }
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
        pos[1] += anchor.getMeasuredHeight();
        // calculate arrow offset
        mPlusPopWindow.setAnimationStyle(R.style.PopupAnimation);
        mPlusPopWindow.showAtLocation(anchor, Gravity.TOP | Gravity.RIGHT, 20, pos[1]);

        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = 0.7f;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context. getWindow().setAttributes(lp);
        mPlusPopWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = context.getWindow().getAttributes();
                lp.alpha = 1f;
                context. getWindow().setAttributes(lp);
            }
        });
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
