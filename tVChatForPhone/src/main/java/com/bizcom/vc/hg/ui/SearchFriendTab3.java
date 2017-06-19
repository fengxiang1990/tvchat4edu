package com.bizcom.vc.hg.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.vc.hg.beans.PhoneFriendItem;
import com.bizcom.vc.hg.util.FriendUtil;
import com.bizcom.vc.hg.view.CharacterParser;
import com.bizcom.vc.hg.view.PinyinComparator;
import com.bizcom.vc.hg.view.SearchContactAdapter;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFriendTab3 extends BaseFragment {

    private List<PhoneFriendItem> data = new ArrayList<PhoneFriendItem>();
    private ListView sortListView;
    private SearchContactAdapter adapter;
    private ImageView emptyView;
    public TextView textEmpty;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_search_contact, container, false);
        initView(v);
        updateEmptyView("");

        return v;
    }

    public void initView(View v) {
        pinyinComparator = new PinyinComparator();
        emptyView = (ImageView) v.findViewById(R.id.emptyView);
        textEmpty = (TextView) v.findViewById(R.id.text_empty);
        sortListView = (ListView) v.findViewById(R.id.country_lvcountry);
        adapter = new SearchContactAdapter(this.getActivity(), data);
        sortListView.setAdapter(adapter);
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhoneFriendItem user = (PhoneFriendItem) adapter.getItem(position);
                if(user.isHasRegsited()){
                    FriendUtil.startVideoCall(getActivity(), user.getUserId());
                }else {
                    Toast.makeText(getActivity(),"该用户未注册",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void onSearch(String search) {
        data.clear();
        if (!TextUtils.isEmpty(search)) {
            for (PhoneFriendItem u : MainApplication.SourceDateList) {
                String name = u.getName();
                if (u.getPhoneNum().contains(search) || name.indexOf(search) != -1 || CharacterParser.getInstance().getSelling(name).startsWith(search)) {
                    data.add(u);
                }
            }
        }
        Collections.sort(data, pinyinComparator);

        if (adapter != null) {
            notifyAdapter(search);
        }
    }

    private void notifyAdapter(String search) {
        adapter.notifyDataSetChanged();
        updateEmptyView(search);
    }

    private void updateEmptyView(String search) {
        if (data.size() - 1 < 0) {
            emptyView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(search)) {
                emptyView.setImageResource(R.mipmap.ic_add_friend_search);
                textEmpty.setVisibility(View.GONE);
            } else {
                emptyView.setImageResource(R.mipmap.ic_add_friend_empty);
                textEmpty.setVisibility(View.VISIBLE);
            }

        } else {
            emptyView.setVisibility(View.GONE);
            textEmpty.setVisibility(View.GONE);
        }
    }

}
