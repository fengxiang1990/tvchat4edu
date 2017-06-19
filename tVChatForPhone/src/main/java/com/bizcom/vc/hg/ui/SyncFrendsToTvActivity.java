package com.bizcom.vc.hg.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.alibaba.fastjson.JSON;
import com.bizcom.util.AlertMsgUtils;
import com.bizcom.util.SyncingDialog;
import com.bizcom.vc.hg.beans.HasBindedItem;
import com.bizcom.vc.hg.beans.TvInfoBeans;
import com.bizcom.vc.hg.beans.TvInfoBeans2;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vo.User;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.flyco.tablayout.SlidingTabLayout;
import com.lidroid.xutils.exception.DbException;
import com.shdx.tvchat.phone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.bizcom.db.provider.DatabaseProvider.mContext;

/**
 * Created by admin on 2016/12/14.
 */


public class SyncFrendsToTvActivity extends FragmentActivity {

    static String tag = "SyncFrendsToTvActivity";
    String mTabStr[] = new String[]{"未同步", "已同步"};

    Unbinder unbinder;

    @BindView(R.id.img_back)
    ImageView img_back;

    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.slidingTab)
    SlidingTabLayout mSlidingTab;

    @BindView(R.id.mPager)
    ViewPager mPager;

    static Handler hanlder = new Handler();
    static ArrayList<Fragment> listViews; //Tab页面列表
    static TvInfoBeans mbeans;
    static String uid;
    static List<HasBindedItem> mBindedListData = new ArrayList<>();
    static List<HasBindedItem> unBindedListData = new ArrayList<>();
    //好友列表
    static private List<User> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_frends_totv);
        unbinder = ButterKnife.bind(this);
        text_title.setText("同步");
        mbeans = (TvInfoBeans) getIntent().getSerializableExtra("beans");
        uid = String.valueOf(GlobalHolder.getInstance().getCurrentUserId());
        if (uid != null) {
            uid = uid.substring(2, uid.length());
        }
        intiFragments();
        initViewPager();
        loadSyncFrends(this);
        hanlder.post(new RetryRunnable(this));
    }


    static boolean isRunning = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }

    class TabWrap {
        String mTabName;
        String mFragmentClassName;

        public TabWrap(String mTabName, String mFragmentClassName) {
            super();
            this.mTabName = mTabName;
            this.mFragmentClassName = mFragmentClassName;
        }

    }

    private SyncFrendsToTvActivity.TabWrap[] mTabClasses = new SyncFrendsToTvActivity.TabWrap[]{
            new SyncFrendsToTvActivity.TabWrap("unbind", SyncTabFragment.class.getName()),
            new SyncFrendsToTvActivity.TabWrap("bind", SyncTabFragment.class.getName()),
    };

    private void intiFragments() {
        listViews = new ArrayList<>();
        for (int i = 0; i < mTabClasses.length; i++) {
            SyncFrendsToTvActivity.TabWrap tabWrap = mTabClasses[i];
            Bundle bundle = new Bundle();
            bundle.putString("tag", tabWrap.mTabName);
            Fragment fragment = Fragment.instantiate(SyncFrendsToTvActivity.this, tabWrap.mFragmentClassName, bundle);
            listViews.add(fragment);
        }
    }

    private void initViewPager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPager.setAdapter(new MainFragmentPagerAdapter(fragmentManager, listViews));
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(2);
        mPager.setOnPageChangeListener(new SyncFrendsToTvActivity.MyOnPageChangeListener());
        mSlidingTab.setViewPager(mPager, mTabStr);
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @SuppressWarnings("deprecation")
        @Override
        public void onPageSelected(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public static SyncTabFragment getSecondTab1() {
        return (SyncTabFragment) listViews.get(0);
    }

    public static SyncTabFragment getSecondTab2() {
        return (SyncTabFragment) listViews.get(1);
    }

    public static class SyncTabFragment extends Fragment {

        Unbinder unbinder;

        @BindView(R.id.ll_empty)
        LinearLayout ll_empty;

        @BindView(R.id.recyclerView)
        RecyclerView recyclerView;

        @BindView(R.id.btn_sync)
        Button btn_sync;

        GridLayoutManager layoutManager;

        FrendSyncAdapter adapter;

        String tag;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_sync_tab, container, false);
            unbinder = ButterKnife.bind(this, view);
            layoutManager = new GridLayoutManager(getActivity(), 4);
            recyclerView.setLayoutManager(layoutManager);
            tag = getArguments().getString("tag");
            Log.e(tag, "SyncTabFragment tag-->" + tag);
            adapter = new FrendSyncAdapter(tag, this, getActivity());
            recyclerView.setAdapter(adapter);
            if (tag.equals("unbind")) {
                btn_sync.setText("同步到TV(0/20)");
                btn_sync.setEnabled(false);
            }
            if (tag.equals("bind")) {
                btn_sync.setText("从TV删除");
                btn_sync.setEnabled(false);
            }
            return view;
        }

        @OnClick(R.id.btn_sync)
        void syncClick() {
            if (tag.equals("unbind")) {
                StringBuilder sb = new StringBuilder("[");
                for (HasBindedItem bindedItem : unBindedListData) {
                    if (bindedItem.isSelected) {
                        String id = bindedItem.getUserId().substring(2, bindedItem.getUserId().length());
                        sb.append(id).append(",");
                    }
                }
                sb.append("]");
                String frends = sb.toString().replace(",]", "]");
                syncFrends(getActivity(), frends, "0");

            }
            if (tag.equals("bind")) {
                StringBuilder sb = new StringBuilder("[");
                for (HasBindedItem bindedItem : mBindedListData) {
                    if (bindedItem.isSelected) {
                        String id = bindedItem.getUserId().substring(2, bindedItem.getUserId().length());
                        sb.append(id).append(",");
                    }
                }
                sb.append("]");
                String frends = sb.toString().replace(",]", "]");
                syncFrends(getActivity(), frends, "1");
            }
        }

    }


    static class FrendSyncAdapter extends RecyclerView.Adapter<MyViewHolder> {

        Context context;
        String tag;
        SyncTabFragment syncTabFragment;

        public FrendSyncAdapter(String tag, SyncTabFragment syncTabFragment, Context context) {
            this.context = context;
            this.tag = tag;
            this.syncTabFragment = syncTabFragment;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_select_frend, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            HasBindedItem user = null;
            if (tag.equals("bind")) {
                user = mBindedListData.get(position);
            }
            if (tag.equals("unbind")) {
                user = unBindedListData.get(position);
            }
            if (user != null) {
                if (!TextUtils.isEmpty(user.getImageUrl())) {
                    holder.img_pic.setImageURI(Uri.parse(user.getImageUrl()));
                } else {
                    Uri uri = Uri.parse("res:///" + R.drawable.root_center_user_head);
                    holder.img_pic.setImageURI(uri);
                }
                holder.text_name.setText(user.getUserName());
                holder.text_account.setText(user.getPhoneNum());
                holder.itemView.setOnClickListener(new UserSelectedListener(user));
                if (user.isSelected) {
                    holder.img_check.setImageResource(R.mipmap.check);
                } else {
                    holder.img_check.setImageResource(R.mipmap.ncheck);
                }
            }

        }

        @Override
        public int getItemCount() {
            int size = 0;
            if (tag.equals("bind")) {
                size = mBindedListData.size();
            }
            if (tag.equals("unbind")) {
                size = unBindedListData.size();
            }
            if (size > 0) {
                syncTabFragment.ll_empty.setVisibility(View.GONE);
            } else {
                syncTabFragment.ll_empty.setVisibility(View.VISIBLE);
            }
            return size;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        Unbinder unbinder;

        public MyViewHolder(View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

        @BindView(R.id.img_pic)
        SimpleDraweeView img_pic;

        @BindView(R.id.img_check)
        ImageView img_check;

        @BindView(R.id.text_name)
        TextView text_name;

        @BindView(R.id.text_account)
        TextView text_account;

    }


    static class RetryRunnable implements Runnable {

        Context context;

        public RetryRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Log.e(tag, "RetryRunnable run");
            //数据为空重新记载
            if (unBindedListData.size() == 0 && mBindedListData.size() == 0) {
                loadSyncFrends(context);
            }
            if (isRunning)
                hanlder.postDelayed(this, 5000);
        }
    }

    static void loadSyncFrends(Context context) {
        BussinessManger.getInstance(context).queryTvUserFriend(new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                if (isSuccess) {
                    call(JSON.parseArrayObject(String.valueOf(obj), TvInfoBeans2.class));
                } else {
                    ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
                }
            }

        }, mbeans.getTvId(), BussinessManger.CHANNEL);
    }


    static void call(List<TvInfoBeans2> tvInfoBeans2s) {
        try {
            List<User> mlis = MainApplication.getDbUtils().findAll(User.class);
            if (mlis != null && mlis.size() > 0) {
                data.clear();
                data.addAll(mlis);
                compare(tvInfoBeans2s);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    static void compare(List<TvInfoBeans2> dataList) {
        if (dataList == null) return;

        for (int i = 0; i < dataList.size(); i++) {
            for (int j = dataList.size() - 1; j > i; j--) {
                if (dataList.get(i).getUid().equals(dataList.get(j).getUid())) {
                    dataList.remove(j);
                }
            }
        }

        mBindedListData.clear();
        unBindedListData.clear();
        for (User user : data) {
            HasBindedItem mBindedItem = null;
            long userId = user.getmUserId();
            String uid = (String.valueOf(userId)).substring(2, (String.valueOf(userId)).length());
            String muid = GlobalHolder.getInstance().getCurrentUserId() + "";
            for (TvInfoBeans2 mbItem : dataList) {
                String uid1 = mbItem.getUid();
                Log.e(tag, "mbItem-->" + mbItem.toString());
                //对比是否已发送
                if (uid1 != null && uid.trim().equals(uid1)) {
                    if (TextUtils.equals(muid, "11" + mbItem.getOwner_uid())) {
                        Log.e(tag, "mbItem 2-->" + mbItem.toString());
                        //是自己发送
                        mBindedItem = new HasBindedItem(user.getDisplayName(), user.getmUserId() + "", user.getAccount(), user.getmAvatarLocation(), user.getmStatusToIntValue(), 1);
                        //我同步到电视端的好友
                        if (!TextUtils.equals(mBindedItem.getUserId(), "11" + mbeans.getUid())) {//屏蔽tv本身出现在tv协助好友列表
                            mBindedListData.add(mBindedItem);
                        }
                    } else {
                        mBindedItem = new HasBindedItem(user.getDisplayName(), user.getmUserId() + "", user.getAccount(), user.getmAvatarLocation(), user.getmStatusToIntValue(), 1);
                    }
                }

            }

            Log.e(tag, "user-->" + user.getAccount() + " " + user.getNickName());
            if (mBindedItem == null) {
                //未发送
                mBindedItem = new HasBindedItem(user.getDisplayName(), user.getmUserId() + "", user.getAccount(), user.getmAvatarLocation(), user.getmStatusToIntValue(), 0);
                Log.e(tag, "mBindedItem-->" + mBindedItem.toString());
                //我还没有同步到电视视端的好友
                if (!TextUtils.equals(mBindedItem.getUserId(), "11" + mbeans.getUid())) {
                    //屏蔽tv本身出现在tv协助好友列表
                    unBindedListData.add(mBindedItem);
                }
            }


        }
        getSecondTab1().adapter.notifyDataSetChanged();
        getSecondTab2().adapter.notifyDataSetChanged();
    }


    static class UserSelectedListener implements View.OnClickListener {
        HasBindedItem item;

        public UserSelectedListener(HasBindedItem item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            int toBindCount = 0;
            int toUnBindCount = 0;
            for (HasBindedItem bindedItem : unBindedListData) {
                if (bindedItem.isSelected) {
                    toBindCount++;
                }
            }
            for (HasBindedItem bindedItem : mBindedListData) {
                if (bindedItem.isSelected) {
                    toUnBindCount++;
                }
            }
            if (item.isSelected) {
                item.isSelected = false;
                if(unBindedListData.contains(item)){
                    toBindCount--;
                }else if(mBindedListData.contains(item)){
                    toUnBindCount--;
                }
                if (toBindCount >= 20 || toUnBindCount >= 20) {
                    Toast.makeText(mContext, "最多只能选20个", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (toBindCount >= 20 || toUnBindCount >= 20) {
                    Toast.makeText(mContext, "最多只能选20个", Toast.LENGTH_SHORT).show();
                } else {
                    item.isSelected = true;
                }

            }
            getSecondTab1().adapter.notifyDataSetChanged();
            getSecondTab2().adapter.notifyDataSetChanged();
            resetBtns(false);
        }
    }

    //同步成功后重置按钮
    //netok  是否访问服务器执行
    static void resetBtns(boolean netok) {
        int toBindCount = 0;
        int toUnBindCount = 0;
        if (netok) {
            unBindedListData.clear();
            mBindedListData.clear();
        }
        for (HasBindedItem bindedItem : unBindedListData) {
            if (bindedItem.isSelected) {
                toBindCount++;
            }
        }
        for (HasBindedItem bindedItem : mBindedListData) {
            if (bindedItem.isSelected) {
                toUnBindCount++;
            }
        }
        if (toBindCount > 0) {
            getSecondTab1().btn_sync.setEnabled(true);
        } else {
            getSecondTab1().btn_sync.setEnabled(false);
        }
        if (toUnBindCount > 0) {
            getSecondTab2().btn_sync.setEnabled(true);
        } else {
            getSecondTab2().btn_sync.setEnabled(false);
        }
        getSecondTab1().btn_sync.setText("同步到TV(" + toBindCount + "/" + 20 + ")");
        if (toUnBindCount == 0) {
            getSecondTab2().btn_sync.setText("从TV删除");
        } else {
            getSecondTab2().btn_sync.setText("从TV删除(" + toUnBindCount + ")");
        }
    }

    static void syncFrends(final Context context, final String frends, final String type) {
        final SyncingDialog syncingDialog = new SyncingDialog(context);
        syncingDialog.showGif1();
        String tvUid = mbeans.getUid();
        if (TextUtils.isEmpty(tvUid)) {
            return;
        }
        BussinessManger.getInstance(context).syncFrendsBatchToTv(frends, tvUid, uid, BussinessManger.CHANNEL, type, new IBussinessManager.OnResponseListener() {

            @Override
            public void onResponse(boolean isSuccess, int what, Object obj) {
                syncingDialog.showGif2();
                String[] strs = frends.replace("[", "").replace("]", "").split(",");
                Log.d(tag, "strs size-->" + strs.length);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < strs.length; i++) {
                    String str = "11" + strs[i];
                    sb.append(str).append(",");
                }
                sb.append("]");
                String ids = sb.toString().replace(",]", "]");
                if (isSuccess) {
                    SharedPreferences preferences = context.getSharedPreferences("tvl", MODE_PRIVATE);
                    Date date = new Date();
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                    String sync_time = sf.format(date);
                    preferences.edit().putString("sync_time", sync_time)
                            .putInt("sync_num", strs.length).commit();
                    if (type.equals("0")) {
                        ToastUtil.ShowToast_long(context, "同步好友成功");
                        BussinessManger.getInstance(context).notifyTvBatchAddFrends(Long.parseLong(uid), ids, Integer.parseInt("11" + mbeans.getUid()));
                        if (context instanceof SyncFrendsToTvActivity) {
                            ((SyncFrendsToTvActivity) context).mPager.setCurrentItem(1);
                        }
                    } else {
                        ToastUtil.ShowToast_long(context, "删除TV好友成功");
                        BussinessManger.getInstance(context).notifyTvBatchDelFrends(Long.parseLong(uid), ids, Integer.parseInt("11" + mbeans.getUid()));
                    }
                    resetBtns(true);
                    loadSyncFrends(context);
                } else {
                    AlertMsgUtils.show(context, String.valueOf(obj), new AlertMsgUtils.OnDialogBtnClickListener() {
                        @Override
                        public void onConfirm(Dialog dialog) {
                            loadSyncFrends(context);
                            dialog.dismiss();
                        }
                    });
                }

            }

        });

    }

}

