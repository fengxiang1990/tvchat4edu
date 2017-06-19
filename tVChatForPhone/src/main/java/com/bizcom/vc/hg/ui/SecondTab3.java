package com.bizcom.vc.hg.ui;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.util.GetPhoneNumber;
import com.bizcom.vc.hg.util.PhoneInfo;
import com.bizcom.vc.hg.view.PinyinComparator;
import com.bizcom.vc.hg.view.SideBar;
import com.bizcom.vc.hg.view.SideBar.OnTouchingLetterChangedListener;
import com.bizcom.vc.hg.view.SortAdapter;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SecondTab3 extends BaseFragment {

    @BindView(R.id.image_no_contact_permission)
    ImageView imageNoContactPermission;
    @BindView(R.id.view_contact)
    FrameLayout viewContact;

    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private List<User> mContactDatas = new ArrayList<User>();

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    /**
     * 注册过的用户
     */
    private FragmentActivity mContext;
    private List<PhoneInfo> ml = new ArrayList<PhoneInfo>();
    private boolean hasPrepared = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View v = inflater.inflate(R.layout.hg_2nd_tab_three, container, false);
        initView(v);

        initResover();
        hasPrepared = true;
        ButterKnife.bind(this, v);
        return v;
    }

    private void initResover() {
        Uri uri = Uri.parse("content://com.android.contacts/data");
        getContext().getContentResolver().registerContentObserver(uri, true, mContactObserver);
    }

    /**
     * 系统联系人数据库监控器
     */
    public ContentObserver mContactObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            OnCall(null);
        }
    };

    public void initView(View v) {
        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar) v.findViewById(R.id.sidrbar);
        dialog = (TextView) v.findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.setSelection(position);
                }

            }
        });

        sortListView = (ListView) v.findViewById(R.id.country_lvcountry);
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhoneFriendItem user = (PhoneFriendItem) adapter.getItem(position);
                if (user.isHasRegsited()) {
                    FriendUtil.startVideoCall(mContext, user.getUserId());
                } else {
                    Toast.makeText(mContext, "该用户未注册", Toast.LENGTH_SHORT).show();
                }

            }
        });
        MainApplication.SourceDateList = new ArrayList<PhoneFriendItem>();

        adapter = new SortAdapter(this.getActivity(), MainApplication.SourceDateList);
        sortListView.setAdapter(adapter);
    }

    private void initLocalData() {
        ml = GetPhoneNumber.getNumber(mContext);
        initDatas();
    }

    private void initDatas() {
//        if(!MainApplication.mContactUserList.isEmpty()){
        new SearchLocalTask().execute();
//        }
    }

    @Override
    public void OnCall(Object obj) {
        if (!hasPrepared) return;

        initLocalData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!hasPrepared) return;

        if (MainApplication.SourceDateList != null && MainApplication.SourceDateList.isEmpty()) {
            initLocalData();
        }
    }

    class SearchLocalTask extends AsyncTask<String, Void, List<PhoneFriendItem>> {

        @Override
        protected List<PhoneFriendItem> doInBackground(String... params) {
            List<PhoneFriendItem> mitems = new ArrayList<PhoneFriendItem>();
            if (ml == null && ml.size() - 1 < 0) return mitems;

            mContactDatas.clear();
            mContactDatas.addAll(MainApplication.mContactUserList);

            for (PhoneInfo mPhoneInfo : ml) {
                String number = mPhoneInfo.getNumber();
                number = number.replace(" ", "");
                number = number.replace("+86", "");
                //剔除自己
                if (TextUtils.equals(GlobalHolder.getInstance().getCurrentUser().getMobile(), number))
                    continue;
                PhoneFriendItem itm = null;
                boolean isOnline = false;
                boolean isReg = false;
                boolean isFriend = false;
                long userId = -1l;
                String name = "";
                String picUrl = "";
                for (User u : mContactDatas) {
                    if (TextUtils.equals(u.getAccount(), number)) {
                        userId = u.getmUserId();
                        name = u.getDisplayName();
                        isFriend = GlobalHolder.getInstance().isFriend(userId);
                        picUrl = u.getmAvatarLocation();
                        isReg = true;
                    }
                }
                if (TextUtils.isEmpty(name)) {
                    name = mPhoneInfo.getName();
                }

                itm = new PhoneFriendItem(isReg, name, number, userId, isOnline, isFriend, picUrl);
                mitems.add(itm);
            }

            return mitems;
        }

        @Override
        protected void onPostExecute(List<PhoneFriendItem> mResults) {
            super.onPostExecute(mResults);

            if (mResults.size() <= 0) {
                imageNoContactPermission.setVisibility(View.VISIBLE);
                viewContact.setVisibility(View.GONE);
            } else {
                imageNoContactPermission.setVisibility(View.GONE);
                viewContact.setVisibility(View.VISIBLE);

                //		// 根据a-z进行排序源数据
                Collections.sort(mResults, pinyinComparator);

                MainApplication.SourceDateList.clear();
                MainApplication.SourceDateList.addAll(mResults);
                adapter.notifyDataSetChanged();
            }
        }
    }

}
