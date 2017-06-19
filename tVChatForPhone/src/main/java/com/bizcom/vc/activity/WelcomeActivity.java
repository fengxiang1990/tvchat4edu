package com.bizcom.vc.activity;

import java.util.ArrayList;
import java.util.List;

import com.bizcom.util.BitmapUtil;
import com.bizcom.util.LocalSharedPreferencesStorage;
import com.bizcom.vc.hg.util.DataUtil;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.IBussinessManager.OnResponseListener;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class WelcomeActivity extends Activity implements OnClickListener, OnPageChangeListener {

    public static final int WELCOMEOK = 9728;
    private ViewPager vp;
    private PagerAdapter vpAdapter;
    private List<View> views;
    private View mEntryTV;

    // 引导图片资源
    private int[] pics = {R.mipmap.welcome_one, R.mipmap.welcome_two, R.mipmap.welcome_three,
            R.mipmap.welcome_four};
    //	// 底部小店图片
//	private ImageView[] dots;
    // 记录当前选中位置
    private int currentIndex;

    private boolean mEntryVisibile;
    private int mOnceInvoke = -1;
    private int mPageScrollItem;
    private View SText;
    private Context mContext;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        setContentView(R.layout.splash_welcome);
        mContext = this;
        views = new ArrayList<View>();
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        // 初始化引导图片列表
        for (int i = 0; i < pics.length; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(mParams);
            Bitmap decodeBitmapFromRes = BitmapUtil.getCompressedBitmap(getResources(), pics[i]);
            iv.setImageBitmap(decodeBitmapFromRes);
            // 全屏
            // iv.setBackgroundDrawable(new
            // BitmapDrawable(decodeBitmapFromRes));
            // iv.setImageResource(pics[i]);
            views.add(iv);
        }
        vp = (ViewPager) findViewById(R.id.viewpager);
        // 初始化Adapter
        vpAdapter = new ViewPagerAdapter(views);
        vp.setAdapter(vpAdapter);
        // 绑定回调
        vp.setOnPageChangeListener(this);
        vp.setOffscreenPageLimit(4);
        // 初始化底部小点
        initDots();
        // 初始化进入按钮
        mEntryTV = findViewById(R.id.ws_welcome_entry);
        SText = findViewById(R.id.SText);
        mEntryTV.setTag(-1);
        mEntryTV.setOnClickListener(this);
        SText.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vp.removeAllViews();
        vp = null;
        vpAdapter = null;
        views.clear();
        views = null;
        pics = null;
//		dots = null;
    }

    private void initDots() {
//		LinearLayout ll = (LinearLayout) findViewById(R.id.ws_welcome_dots);
//		dots = new ImageView[pics.length];
        // 循环取得小点图片
//		for (int i = 0; i < pics.length; i++) {
//			dots[i] = (ImageView) ll.getChildAt(i);
//			dots[i].setEnabled(true);// 都设为灰色
//			dots[i].setOnClickListener(this);
//			dots[i].setTag(i);// 设置位置tag，方便取出与当前位置对应
//		}

        currentIndex = 0;
//		dots[currentIndex].setEnabled(false);// 设置为白色，即选中状态
    }

    /**
     * 设置当前的引导页
     */
    private void setCurView(int position) {
        if (position < 0 || position >= pics.length) {
            return;
        }

        vp.setCurrentItem(position);
    }

    /**
     * 这只当前引导小点的选中
     */
    private void setCurDot(int positon) {
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }

//		dots[positon].setEnabled(false);
//		dots[currentIndex].setEnabled(true);
        currentIndex = positon;
        if (positon == pics.length - 1 && positon != 0) {
//            mEntryTV.setVisibility(View.VISIBLE);
        } else {
//            mEntryTV.setVisibility(View.GONE);
        }
    }

    // 当滑动状态改变时调用
    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (arg0 == 1) {
            mOnceInvoke = arg0;
        }

        if (arg0 == 0 && mEntryVisibile) {
//            mEntryTV.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 当当前页面被滑动时调用 arg0:当前往下一个item滑动的那个item的位置标记。 arg1:当前页面偏移的百分比
     * arg2:当前页面偏移的像素位置
     */
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        mPageScrollItem = arg0;
        if (mEntryVisibile && mOnceInvoke != -1) {
            invoke(mOnceInvoke);
            mOnceInvoke = -1;
        }
    }

    /**
     * 当新的页面被选中时调用 arg0:当前被选中item的位置
     */
    @Override
    public void onPageSelected(int arg0) {
        if (arg0 == pics.length - 1 && arg0 != 0) {
            mEntryVisibile = true;
        } else {
            mEntryVisibile = false;
        }
        // 设置底部小点选中状态
        setCurDot(arg0);
    }

    private void invoke(int arg0) {
        if (mPageScrollItem == 2 && mEntryVisibile) {
            if (arg0 == 1) {
//                mEntryTV.setVisibility(View.GONE);
            } else if (arg0 == 0) {
//                mEntryTV.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.SText:
                close();
                break;
            case R.id.ws_welcome_entry:
                int position = (Integer) v.getTag();
                if (position == -1) {

                    close();

                } else {
                    setCurView(position);
                    setCurDot(position);
                }
                break;

        }

    }

//	private void getMediaEncodeType() {
//		BussinessManger.getInstance(this).getMediaEncodeType(new OnResponseListener() {
//			
//			@Override
//			public void onResponse(boolean isSuccess, int what, Object obj) {
//				if(isSuccess){
//					//记录获取是否失败 获取失败的话 在启动要重新get一次
//					LocalSharedPreferencesStorage.putBooleanValue(mContext, "getMediaEncodeTypeOK", true);
//					
//					DataUtil.saveData(obj, "MediaEncodeType", mContext);
//				}else{
//					LocalSharedPreferencesStorage.putBooleanValue(mContext, "getMediaEncodeTypeOK", false);
//					ToastUtil.ShowToast_long(mContext, String.valueOf(obj));
//				}
//				
//				close();
//			}
//		},android.os.Build.MODEL);
//		
//	}

    protected void close() {
        GlobalConfig.isFirstLauncher = false;
        setResult(WELCOMEOK);
        finish();

    }

    class ViewPagerAdapter extends PagerAdapter {

        // 界面列表
        private List<View> views;

        public ViewPagerAdapter(List<View> views) {
            this.views = views;
        }

        // // 销毁arg1位置的界面
        // @Override
        // public void destroyItem(View arg0, int arg1, Object arg2) {
        // ((ViewPager) arg0).removeView(views.get(arg1));
        // }

        @Override
        public void finishUpdate(View arg0) {

        }

        // 获得当前界面数
        @Override
        public int getCount() {
            if (views != null) {
                return views.size();
            }
            return 0;
        }

        // 初始化arg1位置的界面
        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1), 0);

            if (arg1 == views.size() - 1) {
                views.get(arg1).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        close();
                    }
                });
            }

            return views.get(arg1);
        }

        // 判断是否由对象生成界面
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (arg0 == arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }
}