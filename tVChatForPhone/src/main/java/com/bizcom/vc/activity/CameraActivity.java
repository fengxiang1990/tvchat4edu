package com.bizcom.vc.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.bizcom.request.V2ImRequest;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.V2Toast;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.activity.contacts.clipavatar.ClipAvatarActivity;
import com.bizcom.vc.activity.conversation.ConversationSelectImageActivity;
import com.bizcom.vc.widget.PopSelectPicture;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.io.IOException;

/**
 * 用于处理拍照的activity
 * Created by fxa on 2016/5/23.
 */
public abstract class CameraActivity extends BaseActivity implements PopSelectPicture.OnPicturePopClickListener {

    String tag = "CamearActivity";
    protected PopSelectPicture popSelectPicture;
    public static final int ACTIVITY_PICK_CAMERA = 0x002;
    public static final int ACTIVITY_PICK_PHOTO = 0x003;

    public static final String EXTRA_KEY_IMAGE_PATH = "extra_key_image_path";
    public static final int ACTIVITY_PICK_AVATAR_SIZE = 0x004;

    OnTvPhotoSelectedListener onTvPhotoSelectedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        popSelectPicture = new PopSelectPicture(this);
        popSelectPicture.setOnPicturePopClickListener(this);
    }

    public void setListener(OnTvPhotoSelectedListener listener) {
        onTvPhotoSelectedListener = listener;
    }

    @Override
    public void onPicturePopClick(int status) {
        switch (status) {
            case PopSelectPicture.OnPicturePopClickListener.TakeCamera:
                takePicture();
                popSelectPicture.dismiss();
                break;
            case PopSelectPicture.OnPicturePopClickListener.PickFromPics:
                Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
                startActivityForResult(intent, ACTIVITY_PICK_PHOTO);
                popSelectPicture.dismiss();
                break;
        }
    }


    /**
     * 裁剪图片方法实现
     *
     * @param filePath
     * @param isFromCamera
     */
    public void startPhotoZoom(String filePath, boolean isFromCamera) {
        Intent intent = new Intent(mContext, ClipAvatarActivity.class);
        if (TextUtils.isEmpty(filePath))
            return;
        intent.putExtra(EXTRA_KEY_IMAGE_PATH, filePath);
        intent.putExtra("fromPlace", isFromCamera);
        startActivityForResult(intent, ACTIVITY_PICK_AVATAR_SIZE);
    }


    private void takePicture() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File cahce = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            Log.d(tag, "cahce-->" + cahce.getAbsolutePath());
            File file = new File(cahce.getAbsolutePath() + File.separator + "pic_temp.png");
            Log.d(tag, "file-->" + file.getAbsolutePath());
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(file));
                startActivityForResult(intent, ACTIVITY_PICK_CAMERA);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            ToastUtil.ShowToast_short(this, "sd卡不可用");
        }

    }

    protected ProgressDialog pg;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(tag, "requestCode-->" + requestCode);
        Log.e(tag, "resultCode-->" + resultCode);
        Log.e(tag, "data-->" + data);
        switch (requestCode) {
            case ACTIVITY_PICK_CAMERA:
                File cahce = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                Log.e(tag, "cahce-->" + cahce.getAbsolutePath());
                File file = new File(cahce.getAbsolutePath() + File.separator + "pic_temp.png");
                Log.e(tag, "file-->" + file.getAbsolutePath());
                if (file.exists()) {
                    startPhotoZoom(file.getAbsolutePath(), true);
                } else {
                    if (pg != null) {
                        pg.dismiss();
                    }
                }
                break;
            case ACTIVITY_PICK_PHOTO:
                if (data != null) {
                    String filePath = data.getStringExtra("checkedImage");
                    if (filePath == null) {
                        V2Toast.makeText(mContext, R.string.error_contact_messag_invalid_image_path, V2Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    startPhotoZoom(filePath, false);
                } else {
                    if (pg != null) {
                        pg.dismiss();
                    }
                }
                break;
            case ACTIVITY_PICK_AVATAR_SIZE:
                if (data != null) {
                    if (onTvPhotoSelectedListener == null) {
                        if (pg == null) {
                            pg = ProgressDialog.show(CameraActivity.this, "", "请求中...");
                        }
                        pg.show();
                    }
                    boolean isCamera = data.getBooleanExtra("fromPlace", false);
                    if (!isCamera) {
                        Bitmap photo = data.getParcelableExtra("data");
                        if (photo != null) {
                            if (onTvPhotoSelectedListener != null) {
                                onTvPhotoSelectedListener.onTvPhotoSelected(photo);
                            } else {
                                if (!GlobalHolder.getInstance().checkServerConnected(mContext)) {
                                    byte[] bitmap2Bytes = BitmapUtil.Bitmap2Bytes(photo);
                                    V2ImRequest.invokeNative(V2ImRequest.NATIVE_CHANGE_OWNER_AVATAR, bitmap2Bytes,
                                            bitmap2Bytes.length, ".png");
                                }
                            }
                        } else {
                            Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
                            startActivityForResult(intent, ACTIVITY_PICK_PHOTO);
                        }
                    } else {
                        Bitmap photo = data.getParcelableExtra("data");
                        if (photo != null) {
                            if (onTvPhotoSelectedListener != null) {
                                onTvPhotoSelectedListener.onTvPhotoSelected(photo);
                            } else {
                                byte[] bitmap2Bytes = BitmapUtil.Bitmap2Bytes(photo);
                                V2ImRequest.invokeNative(V2ImRequest.NATIVE_CHANGE_OWNER_AVATAR, bitmap2Bytes,
                                        bitmap2Bytes.length, ".png");
                            }
                        }

                        File temp = new File(GlobalConfig.getGlobalPicsPath() + "/avatar.png");
                        if (temp.exists()) {
                            temp.delete();
                        }
                    }
                } else {
                    if (pg != null) {
                        pg.dismiss();
                    }
                }
                break;
            case ContactDetail2.ACTIVITY_PICK_AVATAR_SIZE_CANCEL:
                if (pg != null) {
                    pg.dismiss();
                }
                break;
            default:
                if (pg != null) {
                    pg.dismiss();
                }
                break;
        }
        switch (resultCode) {
            case ContactDetail2.ACTIVITY_PICK_AVATAR_SIZE_CANCEL:
                if (pg != null) {
                    pg.dismiss();
                }
                break;
        }
    }


    public interface OnTvPhotoSelectedListener {
        void onTvPhotoSelected(Bitmap bitmap);
    }

}
