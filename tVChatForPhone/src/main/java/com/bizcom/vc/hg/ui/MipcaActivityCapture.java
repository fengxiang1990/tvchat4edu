package com.bizcom.vc.hg.ui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.vc.hg.view.HeadLayoutManagerHG;
import com.bizcom.vc.hg.view.ImageUtil;
import com.shdx.tvchat.phone.R;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class MipcaActivityCapture extends FragmentActivity {

    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private String titleText;
    private HeadLayoutManagerHG mHeadLayoutManager;

    Unbinder unbinder;

    @BindView(R.id.header_bg)
    LinearLayout header_bg;

    @BindView(R.id.text_title)
    TextView text_title;

    @BindView(R.id.text_right1)
    TextView text_right1;

    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int REQUEST_CODE = 111;
    /**
     * 选择系统图片Request Code
     */
    public static final int REQUEST_IMAGE = 112;
    private CaptureFragment captureFragment;

    /**
     * Called when the activity is first created.
     */

    boolean isScanTv = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        unbinder = ButterKnife.bind(this);
        header_bg.setBackgroundColor(Color.TRANSPARENT);
        titleText = getIntent().getStringExtra("titleText");
        text_title.setText(titleText);
        text_right1.setVisibility(View.VISIBLE);
        text_right1.setText("相册");
        isScanTv = getIntent().getBooleanExtra("isScanTv", false);
        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.qr_my_scan);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
        initBeepSound();
        //Intent intent = new Intent();
        //startActivityForResult(intent, REQUEST_CODE);

        if(!cameraIsCanUse()){
            Toast.makeText(MipcaActivityCapture.this,"您的摄像头权限未打开",Toast.LENGTH_LONG).show();
        }

    }

    public static boolean isOpen = false;


    @OnClick(R.id.text_right1)
    void text_right1() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @OnClick(R.id.img_back)
    void back() {
        finish();
    }


    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            result(result);
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            result("");
        }
    };


    void result(String resultString) {
        playBeepSoundAndVibrate();
        if (!TextUtils.isEmpty(resultString)) {
            if (isScanTv) {
                //兼容老版本TV二维码
                if (resultString.contains("abcd=")) {
                    int index = resultString.indexOf("abcd=");
                    resultString = resultString.substring(index + 5, resultString.length());
                    Log.e("scan", "scan-->" + resultString);
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", resultString);
                    resultIntent.putExtras(bundle);
                    this.setResult(RESULT_OK, resultIntent);
                    MipcaActivityCapture.this.finish();
                } else {
                    Toast.makeText(MipcaActivityCapture.this, "二维码无法识别", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (resultString.contains("abcd=")) {
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", resultString);
                    resultIntent.putExtras(bundle);
                    this.setResult(RESULT_OK, resultIntent);
                    MipcaActivityCapture.this.finish();
                    return;
                }

                if (!resultString.contains("tvl:") &&
                        !resultString.contains("http://") &&
                        !resultString.contains("https://")) {
                    Toast.makeText(MipcaActivityCapture.this, "二维码无法识别", Toast.LENGTH_SHORT).show();
                }
                if (!resultString.contains("tvl:") &&
                        (resultString.contains("http://") || resultString.contains("https://"))) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(resultString);
                    intent.setData(content_url);
                    startActivity(intent);
                }
                if (resultString.contains("tvl:")) {
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", resultString.replace("tvl:", ""));
                    resultIntent.putExtras(bundle);
                    this.setResult(RESULT_OK, resultIntent);
                }
            }
        } else {
            Toast.makeText(MipcaActivityCapture.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        }
        MipcaActivityCapture.this.finish();
    }


    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE:
                if (data != null) {
                    Uri uri = data.getData();
                    try {
                        CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                            @Override
                            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                                // Toast.makeText(MipcaActivityCapture.this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                                result(result);
                            }

                            @Override
                            public void onAnalyzeFailed() {
                                Toast.makeText(MipcaActivityCapture.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

}