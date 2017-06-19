package com.bizcom.vc.hg.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.bizcom.util.V2Toast;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.bizcom.vc.activity.contacts.clipavatar.ClipAvatarActivity;
import com.bizcom.vc.activity.conversation.ConversationSelectImageActivity;
import com.bizcom.vc.widget.PopSelectPicture;
import com.cgs.utils.ToastUtil;
import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.io.IOException;

/**
 * 用于处理拍照的activity
 * 不集成上传功能，上传功能交给子类实现
 * Created by fxa on 2016/5/23.
 */
public abstract class CameraWithNoUploadActivity extends BaseActivity implements PopSelectPicture.OnPicturePopClickListener {

    String tag = "NoUploadCamera";
    protected PopSelectPicture popSelectPicture;
    public static final int ACTIVITY_PICK_CAMERA = 0x002;
    public static final int ACTIVITY_PICK_PHOTO = 0x003;

    public static final String EXTRA_KEY_IMAGE_PATH = "extra_key_image_path";
    public static final int ACTIVITY_PICK_AVATAR_SIZE = 0x004;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        popSelectPicture = new PopSelectPicture(this);
        popSelectPicture.setOnPicturePopClickListener(this);
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
                }
                break;
            case ACTIVITY_PICK_AVATAR_SIZE:
                if (data != null) {
                    boolean isCamera = data.getBooleanExtra("fromPlace", false);
                    if (!isCamera) {
                        Bitmap photo = data.getParcelableExtra("data");
                        if (photo != null) {
                            onCamera(photo);
                        } else {
                            Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
                            startActivityForResult(intent, ACTIVITY_PICK_PHOTO);
                        }
                    } else {
                        Bitmap photo = data.getParcelableExtra("data");
                        if (photo != null) {
                            onCamera(photo);
                        }
                        File temp = new File(GlobalConfig.getGlobalPicsPath() + "/avatar.png");
                        if (temp.exists()) {
                            temp.delete();
                        }
                    }
                }
                break;
            case ContactDetail2.ACTIVITY_PICK_AVATAR_SIZE_CANCEL:
                break;
            default:
                break;
        }
        switch (resultCode) {
            case ContactDetail2.ACTIVITY_PICK_AVATAR_SIZE_CANCEL:
                break;
        }
    }


    public abstract void onCamera(Bitmap bitmap);

}