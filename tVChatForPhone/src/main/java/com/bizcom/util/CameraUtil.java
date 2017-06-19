package com.bizcom.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by fengxiang on 2016/5/13.
 */
public class CameraUtil {

    public static int DEFAULT_IMG_WIDTH = 200;//默认的图片宽高
    public static final int PHOTO_CAMERA = 1;// 拍照
    public static final int PHOTO_ALBUM = 2; // 相册
    public static final int PHOTO_RESULT = 3;// 结果
    public static Uri uritempFile;
    public static final int IMAGE_WIDTH_SCREEN_LIMIT = 5;//图片宽度为屏幕宽度的1/5


    //相册
    public static void getImageFromAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        activity.startActivityForResult(intent, PHOTO_ALBUM);
    }

    //相机
    public static void getImageFromCamera(Activity activity, String imgNameByCamera) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment
                    .getExternalStorageDirectory(), imgNameByCamera)));
            activity.startActivityForResult(intent, PHOTO_CAMERA);
        } else {
            Toast.makeText(activity, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 收缩图片
     *
     * @param uri
     */
    public static void startPhotoZoom(Activity activity, Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        int aspectX = 1;
        int aspectY = 1;
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        int height = DEFAULT_IMG_WIDTH * aspectY / aspectX;
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", DEFAULT_IMG_WIDTH);
        intent.putExtra("outputY", height);
        /**
         * 此方法返回的图片只能是小图片（sumsang测试为高宽160px的图片）
         * 故将图片保存在Uri中，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
         */
        intent.putExtra("return-data", false);
        //uritempFile为Uri类变量，实例化uritempFile
        uritempFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), getImageName()));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        activity.startActivityForResult(intent, PHOTO_RESULT);
    }

    public static String getImageName() {
        return "zlapp_" + System.currentTimeMillis() + ".jpg";
    }


    public static Bitmap getBitmapFromUri(Activity activity, Uri uri,int width,int height) {
        Bitmap bitmap = null;
        try {
            // 读取uri所在的图片
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            return getSmallBitmap(bitmap,width,height);
        } catch (Exception e) {
            Log.e("[Android]", e.getMessage());
            Log.e("[Android]", "目录为：" + uri);
            e.printStackTrace();
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    public static Bitmap getBitmapFromUri(Activity activity, Uri uri) {
        Bitmap bitmap = null;
        int screenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();
        DEFAULT_IMG_WIDTH  = screenWidth / IMAGE_WIDTH_SCREEN_LIMIT;
        try {
            // 读取uri所在的图片
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            return getSmallBitmap(bitmap,DEFAULT_IMG_WIDTH,DEFAULT_IMG_WIDTH);
        } catch (Exception e) {
            Log.e("[Android]", e.getMessage());
            Log.e("[Android]", "目录为：" + uri);
            e.printStackTrace();
            return null;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * Bitmap缩小的方法
     */
    public static Bitmap getSmallBitmap(Bitmap bitmap,int width,int height) {
        Matrix matrix = new Matrix();
       // Log.e("width,height1",bitmap.getWidth() +"  "+ bitmap.getHeight());
       // Log.e("width,height2",width +"  "+ height);
        float scaleX  = Float.parseFloat(width+"") / Float.parseFloat(bitmap.getWidth()+"");
        float scaleY  = Float.parseFloat(height+"") / Float.parseFloat(bitmap.getHeight()+"");
        matrix.postScale(scaleX, scaleY); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    /**
     * 通过Uri返回File文件
     * 注意：通过相机的是类似content://media/external/images/media/97596
     * 通过相册选择的：file:///storage/sdcard0/DCIM/Camera/IMG_20150423_161955.jpg
     * 通过查询获取实际的地址
     *
     * @param uri
     * @return
     */
    public static File getFileByUri(Context context, Uri uri) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA}, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            Log.i("sendFriendToTv product", "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }

    //Bitmap对象保存味图片文件
    static String  temp_file_name = "";

    public static  File saveBitmapFile(Bitmap bitmap) {
        temp_file_name = new Date().getTime() + ".jpg";
        File file = new File(Environment
                .getExternalStorageDirectory() + "/" + temp_file_name);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
