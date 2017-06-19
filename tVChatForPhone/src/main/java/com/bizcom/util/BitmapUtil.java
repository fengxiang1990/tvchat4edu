package com.bizcom.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.config.GlobalConfig;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class BitmapUtil {

    public static Bitmap loadAvatarFromPath(int type, String path) {
        boolean isOwnerAvatar = true;
        if (path == null) {
            isOwnerAvatar = false;
        } else {
            File f = new File(path);
            if (!f.exists()) {
                isOwnerAvatar = false;
            }

            if (f.isDirectory()) {
                isOwnerAvatar = false;
            }
        }

        Bitmap temp = null;
        if (!TextUtils.isEmpty(path)) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            temp = BitmapFactory.decodeFile(path, opt);
            if (temp == null) {
                isOwnerAvatar = false;
                V2Log.i(" bitmap object is null");
            }
        }

        Bitmap avatar;
        if (isOwnerAvatar) {
            V2Log.d("decode owner avatar file result: width " + temp.getWidth() + "  height:" + temp.getHeight());
            avatar = temp;//getCompressdAvatar(type, temp);
            V2Log.d("decode owner avatar bitmap result: width " + avatar.getWidth() + "  height:" + avatar.getHeight());
        } else {
            avatar = BitmapFactory.decodeResource(GlobalConfig.APPLICATION_CONTEXT.getResources(),
                    R.drawable.avatar);
            V2Log.d("decode default avatar bitmap result: width " + avatar.getWidth() +
                    "  height:" + avatar.getHeight() + " type is : " + type);
        }
        return avatar;
    }

    public static Bitmap getPhoneFriendAvatar(int type) {
        if (type == V2GlobalConstants.AVATAR_ORG) {
            return BitmapFactory.decodeResource(GlobalConfig.APPLICATION_CONTEXT.getResources(),
                    R.drawable.avatar_phonefriend);
        } else {
            return BitmapFactory.decodeResource(GlobalConfig.APPLICATION_CONTEXT.getResources(),
                    R.drawable.avatar_phonefriend);
        }
    }

    public static Bitmap getCompressdAvatar(int type, Bitmap temp) {
        int avatarSize;
        if (type == V2GlobalConstants.AVATAR_ORG) {
            avatarSize = getOrgAvatarSize();
        } else {
            avatarSize = getAvatarSize();
        }
        temp = Bitmap.createScaledBitmap(temp, avatarSize, avatarSize, true);
        return temp;
    }

    /**
     * 获取相应DPI下的头像大小像素 ldpi是120，mdpi是160，hdpi是240，xhdpi是320
     *
     * @return
     */
    public static int getAvatarSize() {
        int width;
        if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_XHIGH) {
            width = 94;
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_MEDIUM) {
            width = (int) (94 * 0.5);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_LOW) {
            width = (int) (94 * 0.375);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_HIGH) {
            width = (int) (94 * 0.75);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXHIGH) {
            width = (int) (94 * 1.5);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXXHIGH) {
            width = (int) (94 * 2);
        } else {
            width = 94;
        }
        return width;
    }

    /**
     * 获取相应DPI下的头像大小像素
     *
     * @return
     */
    public static int getOrgAvatarSize() {
        int width;
        if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_XHIGH) {
            width = 65;
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_MEDIUM) {
            width = (int) (65 * 0.5);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_LOW) {
            width = (int) (65 * 0.375);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == DisplayMetrics.DENSITY_HIGH) {
            width = (int) (65 * 0.75);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXHIGH) {
            width = (int) (65 * 1.5);
        } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXXHIGH) {
            width = (int) (65 * 2);
        } else {
            width = 65;
        }
        return width;
    }

    public static Bitmap getCompressedBitmap(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            throw new NullPointerException(" file is null");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        BitmapFactory.decodeFile(filePath, options);
        V2Log.d("BitmapUtil", "before decode width : " + options.outWidth);
        V2Log.d("BitmapUtil", "before decode height : " + options.outHeight);
        // 计算压缩比
        if (options.outWidth >= 4096 || options.outHeight >= 4096) {
            options.inSampleSize = 8;
        } else if (options.outWidth >= 2048 || options.outHeight >= 2048) {
            options.inSampleSize = 4;
        } else if (options.outWidth >= 500 || options.outHeight >= 500) {
            options.inSampleSize = 2;
        } else {
            options.inSampleSize = 1;
        }

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        options.inPurgeable = true;
        options.inPreferredConfig = null;
        Bitmap bit = BitmapFactory.decodeFile(filePath, options);
        V2Log.d("BitmapUtil", "after decode width : " + options.outWidth);
        V2Log.d("BitmapUtil", "after decode height : " + options.outHeight);
        if (options.outWidth < 500 && options.outHeight < 500) {
            bit = ThumbnailUtils.extractThumbnail(bit, options.outWidth, options.outHeight,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } else {
            bit = ThumbnailUtils.extractThumbnail(bit, options.outWidth / 2, options.outHeight / 2,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }

        if (options.outWidth < 50 || options.outHeight < 50) {
            if (GlobalConfig.GLOBAL_DENSITY_LEVEL >= DisplayMetrics.DENSITY_HIGH) {
                int[] reslut = checkMaxValue(options.outWidth * 3, options.outHeight * 3);
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bit, reslut[0], reslut[1], false);
                bit.recycle();
                return createScaledBitmap;
            }
        } else if (options.outWidth < 100 || options.outHeight < 100) {
            if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXXHIGH) {
                int[] reslut = checkMaxValue(options.outWidth * 6, options.outHeight * 6);
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bit, reslut[0], reslut[1], false);
                bit.recycle();
                return createScaledBitmap;
            } else if (GlobalConfig.GLOBAL_DENSITY_LEVEL >= DisplayMetrics.DENSITY_HIGH) {
                int[] reslut = checkMaxValue(options.outWidth * 2, options.outHeight * 2);
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bit, reslut[0], reslut[1], false);
                bit.recycle();
                return createScaledBitmap;
            }
        } else if (options.outWidth < 200 || options.outHeight < 200) {
            if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXXHIGH) {
                int[] reslut = checkMaxValue(options.outWidth * 2, options.outHeight * 2);
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bit, reslut[0], reslut[1], false);
                bit.recycle();
                return createScaledBitmap;
            }
            // else if (GlobalConfig.GLOBAL_DENSITY_LEVEL >
            // DisplayMetrics.DENSITY_HIGH){
            // Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bit,
            // bit.getWidth() * 2, bit.getHeight() * 2, false);
            // bit.recycle();
            // return createScaledBitmap;
            // }
        }
        V2Log.d("BitmapUtil", "finally decode width : " + bit.getWidth());
        V2Log.d("BitmapUtil", "finally decode height : " + bit.getHeight());
        return bit;
    }

    private static int[] checkMaxValue(int width, int height) {
        int[] arr = new int[2];
        if (width > GlobalConfig.BITMAP_MAX_SIZE) {
            arr[0] = GlobalConfig.BITMAP_MAX_SIZE;
        } else {
            arr[0] = width;
        }

        if (height > GlobalConfig.BITMAP_MAX_SIZE) {
            arr[1] = GlobalConfig.BITMAP_MAX_SIZE;
        } else {
            arr[1] = height;
        }
        return arr;
    }

    public static Bitmap getCompressedBitmap(Resources res, int resID) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeResource(res, resID, options);
        // 计算压缩比
        if (options.outWidth >= 4096 || options.outHeight >= 4096) {
            options.inSampleSize = 8;
        } else if (options.outWidth >= 2048 || options.outHeight >= 2048) {
            options.inSampleSize = 4;
        } else {
            options.inSampleSize = 1;
        }

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        options.inPurgeable = true;
        options.inPreferredConfig = null;
        Bitmap bit = BitmapFactory.decodeResource(res, resID, options);
        return bit;
    }

    /**
     * 获取会议中文档显示用的Bitmap
     *
     * @param filePath
     * @param r
     * @return
     */
    public static Bitmap getDocCompressBitmap(String filePath, int[] r) {
        if (TextUtils.isEmpty(filePath)) {
            throw new NullPointerException(" file is null");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        BitmapFactory.decodeFile(filePath, options);

        double wr = (double) options.outWidth / GlobalConfig.SCREEN_WIDTH;
        double hr = (double) options.outHeight / GlobalConfig.SCREEN_HEIGHT;
        double ratio = Math.min(wr, hr);
        float cross = 1.0f;
        while ((cross * 2) <= ratio) {
            cross *= 2;
        }
        options.inSampleSize = (int) cross;
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        options.inPurgeable = true;
        options.inPreferredConfig = null;
        Bitmap bit = BitmapFactory.decodeFile(filePath, options);
        if (bit.getWidth() < 200 || bit.getHeight() < 200) {
            if (GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXXHIGH) {
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bit, bit.getWidth() * 2, bit.getHeight() * 2,
                        false);
                bit.recycle();
                r[0] = createScaledBitmap.getWidth();
                r[1] = createScaledBitmap.getHeight();
                return createScaledBitmap;
            }
        }
        r[0] = bit.getWidth();
        r[1] = bit.getHeight();
        return bit;
    }

    /**
     * 根据指定的宽和高，生成相应的缩略图
     *
     * @param imagePath
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        BitmapFactory.decodeFile(imagePath, options);

        if (options.outWidth >= 2048 || options.outHeight >= 2048) {
            options.inSampleSize = 8;
        } else if (options.outWidth > 1080 || options.outHeight > 1080) {
            options.inSampleSize = 4;
        } else {
            options.inSampleSize = 2;
        }

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        options.inPurgeable = true;
        options.inPreferredConfig = null;

        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    // public static void getCompressedBitmapBounds(String file, int[] r) {
    //
    // BitmapFactory.Options options = new BitmapFactory.Options();
    // options.inJustDecodeBounds = true;
    // options.inPreferredConfig = Bitmap.Config.ALPHA_8;
    // double wr = (double) options.outWidth / GlobalConfig.SCREEN_WIDTH;
    // double hr = (double) options.outHeight / GlobalConfig.SCREEN_HEIGHT;
    // double ratio = Math.min(wr, hr);
    // float cross = 1.0f;
    // while ((cross * 2) <= ratio) {
    // cross *= 2;
    // }
    // options.inSampleSize = (int) cross;
    // options.inDither = false;
    // options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
    // options.inPurgeable = true;
    // options.inPreferredConfig = null;
    //
    // BitmapFactory.decodeFile(file, options);
    // r[0] = options.outWidth;
    // r[1] = options.outHeight;
    // }

    public static void getFullBitmapBounds(String file, int[] r) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        BitmapFactory.decodeFile(file, options);
        r[0] = options.outWidth;
        r[1] = options.outHeight;
    }

    /**
     * 获取bitmap图像的旋转度
     *
     * @param imgpath
     * @return
     */
    public static int getBitmapRotation(String imgpath) {
        int digree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imgpath);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }
        return digree;
    }

    /**
     * 将bitmap对象转化为二进制数组
     *
     * @param bmp
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static void recycled(Bitmap bm){
        if(bm!=null && !bm.isRecycled()){
            bm.recycle();
            bm=null;
        }
    }
}
