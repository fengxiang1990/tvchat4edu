package com.bizcom.vc.hg.ui.edu;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.bizcom.vc.hg.util.DataUtil;
import com.bizcom.vc.hg.util.MessageSendUtil;
import com.bizcom.vo.User;
import com.config.GlobalHolder;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by admin on 2017/2/16.
 */

public class MessageService extends Service {

    String tag = "MessageService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(tag, "onCreate");
        IntentFilter intentFilter = new IntentFilter(ACTION_MSG_RECEIVER);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(tag, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(tag, "onDestroy");
        unregisterReceiver(receiver);
    }

    public static String ACTION_MSG_RECEIVER = "com.edu.service.msgrec";

    MsgReceiver receiver = new MsgReceiver();

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(tag, "onReceive");
            final ArrayList<Long> studentInfo2s = (ArrayList<Long>) intent.getSerializableExtra("students");
            final int type = intent.getIntExtra("type", 0);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    notifyAllStudent(type, studentInfo2s);
                }
            });


        }
    }

    ExecutorService executorService = Executors.newCachedThreadPool();

    Handler handler = new Handler();

    /**
     * 给所有学生发消息
     *
     * @param type 消息类型
     */
    public void notifyAllStudent(final int type, ArrayList<Long> studentList) {
        Log.e(tag, "notifyAllStudent-->" + type);
        try {
            BlockingQueue<Long> queue = new ArrayBlockingQueue<>(studentList.size());
            for (Long studentInfo2 : studentList) {
                Log.e(tag, "put-->" + studentInfo2.toString());
                queue.put(studentInfo2);
            }

            Long id = null;
            while ((id = queue.take()) != null) {
                final long remoteId = id;
                Log.e(tag, "sendMsg-->" + remoteId);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendMsg(type, remoteId);
                    }
                }, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给学生发消息
     *
     * @param type      消息类型
     * @param studentId 学生ID
     */
    void sendMsg(int type, long studentId) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("fromID", GlobalHolder.getInstance().getCurrentUserId());//自己的userId
            json.put("timeStamp", DataUtil.getDate

                    ());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("message", json.toString());
        User user = GlobalHolder.getInstance().getUser(studentId);
        if (user != null) {
            Log.e("remote tv", user.getmUserId() + " " + user.getAccount() + " " + user.getNickName());
            new MessageSendUtil(MessageService.this).sendMessageToRemote(json.toString(),
                    user);//tv的UserId
        }
    }
}
