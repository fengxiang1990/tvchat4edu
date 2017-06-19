package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.MainApplication;
import com.bizcom.vc.hg.adapter.MlistAdapter;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.cgs.utils.ToastUtil;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoukang on 16/10/8.
 */

public class HgSearchTXLActivity extends Activity {
    private List<PhoneFriendItem> data;
    private ListView mListView;
    private EditText et;
    private HgSearchTXLActivity mContext;
    private MlistAdapter mMListAdapter;
    private HeadLayoutManagerHG mHeadLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hg_activity_search_lxr);

        mContext = this;
        initListView();
        mHeadLayoutManager = new HeadLayoutManagerHG(mContext, findViewById(R.id.head_layout), false);
        mHeadLayoutManager.updateTitle("查找联系人");
        et = (EditText) findViewById(R.id.et);
        et.setImeOptions(EditorInfo.IME_ACTION_SEND);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    searchData();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.searchIm).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchData();

            }
        });

    }
    private void searchData() {
        String search = et.getText().toString().trim();
        if (et.getText() != null && !TextUtils.isEmpty(search)) {

            data.clear();
            data.addAll(search(search));
            mMListAdapter.notifyDataSetChanged();

        } else {
            ToastUtil.ShowToast_long(mContext, "请输入正确的查询条件");
        }


    }



    private void initListView() {
        data = new ArrayList<PhoneFriendItem>();
        mListView = (ListView) findViewById(R.id.mListView);
        mMListAdapter = new MlistAdapter(mContext, data);

        mListView.setAdapter(mMListAdapter);
    }


    /**
     * 筛选
     *
     * @param str
     * @return
     */
    protected List<PhoneFriendItem> search(String str) {
        if (TextUtils.isEmpty(str)) {
            data.clear();
            return new ArrayList<PhoneFriendItem>();
        }
        List<PhoneFriendItem> mlis = null;
        mlis = new ArrayList<PhoneFriendItem>();
        for (int i = 0; i < MainApplication.SourceDateList.size(); i++) {
            PhoneFriendItem mPhoneFriendItem = MainApplication.SourceDateList.get(i);
            if (mPhoneFriendItem.getPhoneNum().contains(str)
                    || mPhoneFriendItem.getName().contains(str)
                    ) {
                mlis.add(mPhoneFriendItem);
            }
        }
        return mlis == null ? (new ArrayList<PhoneFriendItem>()) : mlis;

    }


}