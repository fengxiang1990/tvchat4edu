package com.bizcom.vc.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.request.util.BitmapManager;
import com.bizcom.service.JNIService;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Toast;
import com.bizcom.vc.activity.conference.ConferenceActivity;
import com.bizcom.vc.activity.conversation.ConversationTextActivity;
import com.bizcom.vc.activity.main.HeadLayoutManager;
import com.bizcom.vo.User;
import com.config.PublicIntent;
import com.shdx.tvchat.phone.R;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;

public abstract class BaseActivity extends Activity {

    private boolean isNeedBroadcast;
    private boolean isNeedHandler;
    private boolean isNeedAvatar;

    protected String currentActionType;
    protected String currentActivityName = "";
    protected BaseActivity mContext;

    protected Handler mHandler;
    protected LocalReceiver localReceiver;
    protected HeadLayoutManager mHeadLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        if (isNeedBroadcast) {
            initReceiver();
        }

        if (isNeedHandler) {
            mHandler = new LocalHandler(this);
        }

        if (isNeedAvatar) {
            BitmapManager.getInstance().registerBitmapChangedListener(mAvatarChangedListener);
        }

        View functionLy = findViewById(R.id.ws_activity_main_title_functionLy);
        if (functionLy != null) {
            functionLy.setVisibility(View.INVISIBLE);
        }
        initViewAndListener();
    }

    @Override
    protected void onDestroy() {
        if (isNeedBroadcast) {
            try {
                unregisterReceiver(localReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isNeedHandler)
            mHandler.removeCallbacksAndMessages(null);

        if (isNeedAvatar) {
            BitmapManager.getInstance().unRegisterBitmapChangedListener(mAvatarChangedListener);
        }
        super.onDestroy();
    }

    private void initReceiver() {
        localReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
        filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
        addBroadcast(filter);
        registerReceiver(localReceiver, filter);
    }

    public void setNeedBroadcast(boolean isNeedBroadcast) {
        this.isNeedBroadcast = isNeedBroadcast;
    }

    public void setNeedHandler(boolean isNeedHandler) {
        this.isNeedHandler = isNeedHandler;
    }

    public void setNeedAvatar(boolean isNeedAvatar) {
        this.isNeedAvatar = isNeedAvatar;
    }

    public abstract void addBroadcast(IntentFilter filter);

    public abstract void receiveBroadcast(Intent intent);

    public abstract void receiveMessage(Message msg);

    public abstract void initViewAndListener();

    public abstract void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm);

    protected void setComBackImageTV(TextView mBackTV) {
        mBackTV.setBackgroundResource(R.drawable.title_bar_back_button_selector);
    }

    protected void setComBackTextTV(TextView mBackTV) {
        mBackTV.setText(R.string.common_return_name);
    }

    protected void setComRightStyle(String mTitleText, boolean isMainActivity) {
        mHeadLayoutManager = new HeadLayoutManager(mContext, findViewById(R.id.ws_common_activity_title_ly), isMainActivity);
        mHeadLayoutManager.updateTitle(mTitleText);
    }

    protected void showShortToast(int res) {
        V2Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(int res) {
        V2Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

//	public void startActivity(View mClickView, Intent intent) {
//		intent.putExtra(WaveCompat.IntentKey.BACKGROUND_COLOR, R.color.common_item_text_color_blue);
//		WaveTouchHelper.bindWaveTouchHelper(mClickView, this, intent);
//	}


    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            receiveBroadcast(intent);
        }
    }

    private BitmapManager.BitmapChangedListener mAvatarChangedListener = new BitmapManager.BitmapChangedListener() {

        @Override
        public void notifyAvatarChanged(User targetUser, Bitmap newAvatar) {
            receiveNewAvatar(targetUser, newAvatar);
        }
    };

    private static class LocalHandler extends Handler {
        private final WeakReference<BaseActivity> mActivity;

        public LocalHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().receiveMessage(msg);
        }
    }

    /**
     * 获取点击事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if ((currentActivityName.equals(ConferenceActivity.class.getSimpleName()))
                || (currentActivityName.equals(ConversationTextActivity.class.getSimpleName()))) {
            return super.dispatchTouchEvent(ev);
        } else {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View view = getCurrentFocus();
                if (view != null && isHideInput(view, ev)) {
                    MessageUtil.hideKeyBoard(mContext, view.getWindowToken());
                    ;
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定是否需要隐藏
     *
     * @param v
     * @param ev
     * @return
     */
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] location = new int[2];
            v.getLocationInWindow(location);
            int left = location[0], top = location[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            float targetX = ev.getX();
            float targetY = ev.getY();
            if (targetX > left && targetX < right && targetY > top && targetY < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
