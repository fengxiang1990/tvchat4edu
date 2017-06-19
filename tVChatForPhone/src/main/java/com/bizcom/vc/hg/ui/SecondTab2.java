package com.bizcom.vc.hg.ui;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MainApplication;
import com.bizcom.db.provider.MediaRecordProvider;
import com.bizcom.request.V2ImRequest;
import com.bizcom.util.DensityUtils;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.ConversationP2PAVActivity;
import com.bizcom.vc.hg.util.DialogUtil;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;

public class SecondTab2 extends BaseFragment {

    private boolean hasPrepared = false;
    public LatelyAdapter mAdapter;
    public List<User> mCurrentUser = new ArrayList<User>();
    private FragmentActivity mContext;
    private View v;
    public ImageView emptyView;
    public TextView textEmpty;
    public ListView lv;
    public View oldSel;//上一个长按选中的item

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        v = inflater.inflate(R.layout.hg_2nd_tab_two, container, false);
        initView();
        hasPrepared = true;
        ButterKnife.bind(this, v);
        return v;
    }

    private void initView() {
        emptyView = (ImageView) v.findViewById(R.id.lately_empty_view);
        textEmpty = (TextView) v.findViewById(R.id.text_empty);
        textEmpty.setText("赶快跟好友进行视频通话吧~");
        v.findViewById(R.id.tab_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (oldSel != null) {
                    oldSel.setVisibility(View.GONE);
                    oldSel = null;
                }
            }
        });
        lv = (ListView) v.findViewById(R.id.lv);
        mAdapter = new LatelyAdapter(getActivity(), mCurrentUser, this);
        mAdapter.setType(getType());
        lv.setAdapter(mAdapter);
        if(getType()==LatelyAdapter.TYPE_LATELY_SEARCH){
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) emptyView.getLayoutParams();
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            emptyView.setLayoutParams(lp);
            emptyView.setImageResource(R.mipmap.ic_add_friend_search);
            emptyView.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
            lv.setPadding(0,0,0,0);
        }
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (i >= mCurrentUser.size())
                    return false;

//                if (oldSel != null) {
//                    oldSel.setVisibility(View.GONE);
//                }
//                View selItem = view.findViewById(R.id.tv_lately_delete);
//                selItem.setVisibility(View.VISIBLE);
//                oldSel = selItem;

                AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                final String[] items={"删除该记录",};
                builder.setItems(items,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        int result = MediaRecordProvider.deleteMediaMessage(mAdapter.getItemId(i));
                        if (result > 0) {
                            Toast.makeText(mContext, "记录删除成功~", Toast.LENGTH_SHORT).show();
                            mCurrentUser.remove(mAdapter.getItem(i));
                            notifyAdapter();
                        } else {
                            Toast.makeText(mContext, "记录删除失败~", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setCancelable(true);
                AlertDialog dialog=builder.create();
                dialog.show();

                return true;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= mCurrentUser.size())
                    return;

                GlobalConfig.startP2PConnectChat(getActivity(),
                        ConversationP2PAVActivity.P2P_CONNECT_VIDEO, id,
                        false, null, null);
            }
        });
    }

    public int getType() {
        return LatelyAdapter.TYPE_LATELY;
    }

    public void getData() {
        // 异步去加载数据库
        new AsyncTask<Void, Void, List<AudioVideoMessageBean>>() {

            @Override
            protected List<AudioVideoMessageBean> doInBackground(Void... params) {
                return MediaRecordProvider.loadMediaHistoriesMessage(GlobalHolder.getInstance().getCurrentUserId(),
                        AudioVideoMessageBean.TYPE_ALL);
            }

            @Override
            protected void onPostExecute(List<AudioVideoMessageBean> result) {
                super.onPostExecute(result);
                if (result == null) {
                    return;
                }
                Collections.sort(result);

                for (AudioVideoMessageBean item : result) {
                    dispatch(item);
                }

//                Collections.sort(mCurrentUser);
                MainApplication.mLatelyUserList = mCurrentUser;
                notifyAdapter();
            }
        }.execute();
    }

    public void dispatch(AudioVideoMessageBean item) {
        User mUser = null;
        long userID = item.getRemoteUserID();
//        if(!GlobalHolder.getInstance().isFriend(userID)){
//            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, userID);
//        }
        mUser = GlobalHolder.getInstance().getExistUser(userID);
        if (!mCurrentUser.contains(mUser)) mCurrentUser.add(mUser);
//        mUser = GlobalHolder.getInstance().getExistUser(userID);
//
//        if(mUser!=null){
//            if (!mCurrentUser.contains(mUser)) mCurrentUser.add(mUser);
//        }else{
//            V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO, userID);
//        }

    }

    public void notifyAdapter() {
        if (mCurrentUser.size() - 1 < 0) {
            emptyView.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            textEmpty.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnCall(Object obj) {
        if (!hasPrepared || mCurrentUser == null) return;
        mCurrentUser.clear();
        //获取数据
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();

        MainApplication.loadVoiceMediaData();
    }
}
