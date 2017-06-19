package com.bizcom.vc.hg.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bizcom.bo.MessageObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vc.hg.web.ConstantParams;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.cgs.utils.ToastUtil;
import com.config.GlobalHolder;
import com.google.gson.Gson;
import com.shdx.tvchat.phone.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HgStartVideoByTvActivity extends Activity implements OnClickListener {

    String tag = "HgStartVideoByTv";
    private HgStartVideoByTvActivity mCon;
    private View hangUpBt;
    private String tvUid;
    private String toID;
    private String tvId;
    private User remoteUser;
    private IntentFilter intentFilter;
    private LocalReceiver receiver = new LocalReceiver();
    private TextView text_remote_username;
    private TextView text_talk_minute;
    private TextView img_voice;
    private TextView img_expressions;
    private TextView img_skin;
    private TextView img_change;
    private int time = 0;
    private View ll_bottom;
    private View ll_expandskin;
    private RecyclerView recyclerView;
    private View ll_menu;
    private View text_talk_wait;
    private TextView text_getback;
    LinearLayoutManager linearLayoutManager;
    NodeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hg_video_start_inviting);
        initExpressions();
        initSkins();
        mCon = this;
        tvId = getIntent().getStringExtra("tvId");
        toID = getIntent().getStringExtra("toID");
        tvUid = getIntent().getStringExtra("tvUid");
        remoteUser = GlobalHolder.getInstance().getExistUser(Long.parseLong(toID));
        initView();
        initReceiver();
        initData();
        startTimer();
    }

    //判断是否接通
    private boolean UnResceve = true;

    int talkTime = 0;
    Timer t = new Timer();

    /**
     * 开始计时 超过60秒关闭该界面
     */
    public void startTimer() {
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                time++;

                //接通了
                if (!UnResceve) {
                    talkTime++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder sb = new StringBuilder();
                            String minutes = "00";
                            String secs = "00";
                            if (talkTime < 10) {
                                secs = "0" + talkTime;
                            } else if (talkTime >= 10 && talkTime < 60) {
                                secs = talkTime + "";
                            } else if (talkTime >= 60) {
                                int min = talkTime / 60;
                                int sec = talkTime % 60;
                                if (min < 10) {
                                    minutes = "0" + min;
                                } else if (min > 10) {
                                    minutes = "" + min;
                                }
                                if (sec < 10) {
                                    secs = "0" + sec;
                                } else {
                                    secs = sec + "";
                                }
                            }
                            sb.append(minutes).append(":").append(secs);
                            text_talk_minute.setText(sb.toString());
                        }
                    });
                    return;
                }

                if (time >= 60) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (UnResceve) {
                                CallToPhoen(ConstantParams.MESSAGE_TYPE_HANGHP);
                                ToastUtil.ShowToast_short(mCon, "对方暂时无法接通");
                                finish();
                            }

                        }
                    });
                    t.cancel();
                }
            }
        }, 0, 1000);
    }


    private void initReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(SecondTab1.NEW_MSG);
        registerReceiver(receiver, intentFilter);
    }

    private void initData() {
        CallTV(ConstantParams.MESSAGE_TYPE_VIDEO_START);

    }

    private void initView() {
        ll_menu = findViewById(R.id.ll_menu);
        text_talk_wait = findViewById(R.id.text_talk_wait);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        ll_expandskin = findViewById(R.id.ll_expandskin);
        text_getback = (TextView) findViewById(R.id.text_getback);
        ll_bottom = findViewById(R.id.ll_bottom);
        text_remote_username = (TextView) findViewById(R.id.text_remote_username);
        text_talk_minute = (TextView) findViewById(R.id.text_talk_minute);
        hangUpBt = findViewById(R.id.im2);
        img_voice = (TextView) findViewById(R.id.img_voice);
        img_expressions = (TextView) findViewById(R.id.img_expressions);
        img_skin = (TextView) findViewById(R.id.img_skin);
        img_change = (TextView) findViewById(R.id.img_change);
        text_remote_username.setText(remoteUser.getDisplayName());
        hangUpBt.setOnClickListener(this);
        img_voice.setOnClickListener(this);
        img_change.setOnClickListener(this);
        img_expressions.setOnClickListener(this);
        img_skin.setOnClickListener(this);
        text_getback.setOnClickListener(this);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new NodeAdapter(expressions);
        recyclerView.setAdapter(adapter);
    }


    private void CallTV(int type) {

        WaitDialogBuilder.showNormalWithHintProgress(mCon);
        String fromId = String.valueOf(GlobalHolder.getInstance().mCurrentUserId);
        fromId = fromId.substring(2, fromId.length());
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("result", ConstantParams.MESSAGE_RESULT_SUCCESS);
            json.put("fromID", fromId);
            json.put("tvId", tvId);
            json.put("toID", toID);
            json.put("timeStamp", System.currentTimeMillis() + "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        new MessageSendUtil(mCon).sendMessageToRemote(json.toString(),
                GlobalHolder.getInstance().getUser(Long.parseLong(String.valueOf("11" + tvUid))));

        handler.postDelayed(new Runnable() {
            public void run() {
                WaitDialogBuilder.dismissDialog();
            }
        }, 3000);

    }

    //发消息直接通知手机
    private void CallToPhoen(int type) {
        String fromId = String.valueOf(GlobalHolder.getInstance().mCurrentUserId);
        fromId = fromId.substring(2, fromId.length());
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("result", ConstantParams.MESSAGE_RESULT_SUCCESS);
            json.put("fromID", fromId);
            json.put("tvId", tvId);
            json.put("toID", toID);
            json.put("timeStamp", System.currentTimeMillis() + "");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        User user = GlobalHolder.getInstance().getUser(Long.parseLong(String.valueOf("11" + tvUid)));
        new MessageSendUtil(mCon).sendMessageToRemote(json.toString(),
                user);

    }


//    //发送消息
//    private void sendMsg(int type) {
//        String fromId = String.valueOf(GlobalHolder.getInstance().mCurrentUserId);
//        fromId = fromId.substring(2, fromId.length());
//        JSONObject json = new JSONObject();
//        try {
//            json.put("type", type);
//            json.put("result", ConstantParams.MESSAGE_RESULT_SUCCESS);
//            json.put("fromID", fromId);
//            json.put("tvId", tvId);
//            json.put("toID", toID);
//            json.put("timeStamp", System.currentTimeMillis() + "");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        new MessageSendUtil(mCon).sendMessageToRemote(json.toString(),
//                GlobalHolder.getInstance().getUser(Long.parseLong(toID)));
//
//    }

    boolean hasVoice = true;

    @Override
    public void onClick(View v) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int leftMargin = screenWidth / 12;
        switch (v.getId()) {
            case R.id.im2:
                CallToPhoen(ConstantParams.MESSAGE_TYPE_HANGHP);
                finish();
                break;
            case R.id.img_voice:
                if (hasVoice) {
                    hasVoice = false;
                    Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.n_mute_off);
                    BitmapDrawable drawable0 = new BitmapDrawable(getResources(), bitmap0);
                    img_voice.setCompoundDrawablesWithIntrinsicBounds(null, drawable0, null, null);
                } else {
                    hasVoice = true;
                    Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.n_mute);
                    BitmapDrawable drawable0 = new BitmapDrawable(getResources(), bitmap0);
                    img_voice.setCompoundDrawablesWithIntrinsicBounds(null, drawable0, null, null);
                }
                CallTV(ConstantParams.MESSAGE_TYPE_MUTE);
                break;
            case R.id.img_skin:
                text_getback.setText("收回皮肤");
                layoutParams.leftMargin = leftMargin;
                recyclerView.setLayoutParams(layoutParams);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.n_peel);
                BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                text_getback.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                ll_bottom.setVisibility(View.GONE);
                ll_expandskin.setVisibility(View.VISIBLE);
                adapter.data = skins;
                adapter.notifyDataSetChanged();
                break;
            case R.id.img_expressions:
                text_getback.setText("收回表情");
                layoutParams.leftMargin = 0;
                recyclerView.setLayoutParams(layoutParams);
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.n_expression);
                BitmapDrawable drawable1 = new BitmapDrawable(getResources(), bitmap1);
                text_getback.setCompoundDrawablesWithIntrinsicBounds(null, drawable1, null, null);
                ll_bottom.setVisibility(View.GONE);
                ll_expandskin.setVisibility(View.VISIBLE);
                adapter.data = expressions;
                adapter.notifyDataSetChanged();
                break;
            case R.id.img_change:
                CallTV(ConstantParams.MESSAGE_TYPE_CHANGVIDEO);
                break;
            case R.id.text_getback:
                ll_bottom.setVisibility(View.VISIBLE);
                ll_expandskin.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (ll_expandskin.getVisibility() == View.VISIBLE) {
                ll_bottom.setVisibility(View.VISIBLE);
                ll_expandskin.setVisibility(View.GONE);
                return false;
            }
        }
        return false;
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SecondTab1.NEW_MSG)) {
                try {
                    MessageObject msgObj = intent.getParcelableExtra("msgObj");
                    long remoteID = msgObj.rempteUserID;
                    long msgID = msgObj.messageColsID;
                    VMessage m = ChatMessageProvider.loadUserMessageById(remoteID, msgID);
                    String getInfo = m.getPlainText();
                    JSONObject json = new JSONObject(getInfo);
                    int type = json.getInt("type");
                    Log.i("tvPhone", "json-" + json.toString());
                    Log.i("tvPhone", "msgObj-" + msgObj.toString());
                    switch (type) {
                        case ConstantParams.MESSAGE_TYPE_MUTE:
                            if (hasVoice) {
                                hasVoice = false;
                                Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.n_mute_off);
                                BitmapDrawable drawable0 = new BitmapDrawable(getResources(), bitmap0);
                                img_voice.setCompoundDrawablesWithIntrinsicBounds(null, drawable0, null, null);
                            } else {
                                hasVoice = true;
                                Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.n_mute);
                                BitmapDrawable drawable0 = new BitmapDrawable(getResources(), bitmap0);
                                img_voice.setCompoundDrawablesWithIntrinsicBounds(null, drawable0, null, null);
                            }
                            break;
                        case ConstantParams.MESSAGE_TYPE_VIDEO_NO_ANSWER:
                            ToastUtil.ShowToast_short(mCon, "对方没有应答");
                            finish();
                            break;
                        case ConstantParams.MESSAGE_TYPE_VIDEO_REFUSE:
                            ToastUtil.ShowToast_short(mCon, "视频通话被拒接");
                            finish();
                            break;
                        case ConstantParams.MESSAGE_TYPE_CHANGVIDEO:
                            break;
                        case ConstantParams.MESSAGE_TYPE_VIDEO_CONNECTED://接通
                            UnResceve = false;
                            ll_menu.setVisibility(View.VISIBLE);
                            text_talk_minute.setVisibility(View.VISIBLE);
                            text_talk_wait.setVisibility(View.GONE);
                            break;
                        case ConstantParams.MESSAGE_TYPE_HANGHP:// 表情
                            finish();
                            break;
                        case ConstantParams.MESSAGE_TYPE_THEME:// 主题
                            break;
                    }
                } catch (Exception e) {
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        t.cancel();
        try {
            if (receiver != null) {
                unregisterReceiver(receiver);
                if (!UnResceve) {
                    CallToPhoen(ConstantParams.MESSAGE_TYPE_HANGHP);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    class NodeAdapter extends RecyclerView.Adapter<ViewHolder> {

        public List<Node> data;

        public NodeAdapter(List<Node> data) {
            this.data = data;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(HgStartVideoByTvActivity.this).inflate(R.layout.item_expression_or_skin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Node node = data.get(position);
            holder.img.setImageResource(node.resId);
            holder.text.setText(node.name);
            holder.itemView.setOnClickListener(new MyExpSkinClickListener(node, position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    boolean isNodeClickable = true;

    Handler handler = new Handler();

    class MyExpSkinClickListener implements OnClickListener {

        public MyExpSkinClickListener(Node node, int position) {
            Log.d(tag, "MyExpSkinClickListener create");
            this.node = node;
            this.position = position;
        }

        Node node;
        int position;

        @Override
        public void onClick(View v) {
            if (isNodeClickable) {
                isNodeClickable = false;
                sendExpOrSkin(node, position);
            } else {
                if (node.type == Node.TYPE_EXPRESSION) {
                    ToastUtil.ShowToast_short(HgStartVideoByTvActivity.this, "两次发送表情时间间隔不能小于3秒");
                } else if (node.type == Node.TYPE_SKIN) {
                    ToastUtil.ShowToast_short(HgStartVideoByTvActivity.this, "两次切换皮肤时间间隔不能小于3秒");
                }
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isNodeClickable = true;
                }
            }, 3000);
        }
    }


    void sendExpOrSkin(Node node, int position) {
        String fromId = String.valueOf(GlobalHolder.getInstance().mCurrentUserId);
        fromId = fromId.substring(2, fromId.length());
        Map<String, Object> map = new HashMap<>();
        map.put("fromID", fromId);
        map.put("data", position);
        map.put("tvId", tvId);
        map.put("toID", toID);
        map.put("timeStamp", System.currentTimeMillis());
        if (node.type == Node.TYPE_EXPRESSION) {
            map.put("type", ConstantParams.MESSAGE_TYPE_MULTI);
        } else if (node.type == Node.TYPE_SKIN) {
            map.put("type", ConstantParams.MESSAGE_TYPE_THEME);
        }
        String msg = new Gson().toJson(map);
        User user = GlobalHolder.getInstance().getUser(Long.parseLong(String.valueOf("11" + tvUid)));
        Log.e(tag, "user-->" + user.getNickName() + " " + user.getAccount());
        boolean result = new MessageSendUtil(HgStartVideoByTvActivity.this).sendMessageToRemote(msg, user);
        Log.e(tag, "msg-->" + msg);
        Log.e(tag, "result-->" + result);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView img;
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }

    List<Node> expressions = new ArrayList<>();
    List<Node> skins = new ArrayList<>();

    void initExpressions() {
        Node exp1 = new Node(Node.TYPE_EXPRESSION);
        exp1.resId = R.mipmap.bq_qq;
        exp1.name = "亲亲";

        Node exp2 = new Node(Node.TYPE_EXPRESSION);
        exp2.resId = R.mipmap.bq_dz;
        exp2.name = "赞";

        Node exp3 = new Node(Node.TYPE_EXPRESSION);
        exp3.resId = R.mipmap.bq_xk;
        exp3.name = "笑哭了";

        Node exp4 = new Node(Node.TYPE_EXPRESSION);
        exp4.resId = R.mipmap.bq_dg;
        exp4.name = "生日快乐";


        Node exp5 = new Node(Node.TYPE_EXPRESSION);
        exp5.resId = R.mipmap.bq_xh;
        exp5.name = "鲜花";

        expressions.add(exp4);
        expressions.add(exp5);
        expressions.add(exp1);
        expressions.add(exp3);
        expressions.add(exp2);


    }

    void initSkins() {
        Node skin1 = new Node(Node.TYPE_SKIN);
        skin1.resId = R.mipmap.zt_hy;
        skin1.name = "海洋";

        Node skin2 = new Node(Node.TYPE_SKIN);
        skin2.resId = R.mipmap.zt_sl;
        skin2.name = "森林";

        Node skin3 = new Node(Node.TYPE_SKIN);
        skin3.resId = R.mipmap.zt_jc;
        skin3.name = "剧场";
        skins.add(skin3);
        skins.add(skin1);
        skins.add(skin2);

    }

    public static class Node {
        public static final int TYPE_EXPRESSION = 1;
        public static final int TYPE_SKIN = 2;

        public int type;
        public String url1;
        public String url2;
        public int resId;
        public String name;

        public Node(int type) {
            this.type = type;
        }
    }
}
