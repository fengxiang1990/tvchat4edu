package com.bizcom.util.yykEmojiModule;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.config.GlobalConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EmojiUtil {
    private static ArrayList<Emoji> emojiList;

    public static ArrayList<Emoji> getEmojiList() {
        if (emojiList == null) {
            emojiList = generateEmojis();
        }
        return emojiList;
    }

    private static ArrayList<Emoji> generateEmojis() {
        String preFix = "/:";
        String suffFix = ":/";
        ArrayList<Emoji> list = new ArrayList<>();
        for (int i = 1; i < GlobalConfig.GLOBAL_FACE_ARRAY.length; i++) {
            char c = (char) i;
            if (c == '\n') {
                c += 100;
            }
            c += 100;

            Emoji emoji = new Emoji();
            emoji.setImageUri(GlobalConfig.GLOBAL_FACE_ARRAY[i]);
            emoji.setContent(preFix + c + suffFix);
            list.add(emoji);
        }
        return list;
    }


    static {
        emojiList = generateEmojis();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId) {
//        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
//        // 调用上面定义的方法计算inSampleSize值
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        // 使用获取到的inSampleSize值再次解析图片
//        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId);
    }


    public static void handlerEmojiText(TextView comment, String content, Context context) throws IOException {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "/:(\\S+?):/";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        Iterator<Emoji> iterator;
        Emoji emoji;
        while (m.find()) {
            iterator = emojiList.iterator();
            String tempText = m.group();
            while (iterator.hasNext()) {
                emoji = iterator.next();
                if (tempText.equals(emoji.getContent())) {
                    //转换为Span并设置Span的大小
                    sb.setSpan(new ImageSpan(context, GlobalConfig.getDrawableIndexByEmoji(emoji.getContent())),
                            m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                }
            }
        }
        comment.setText(sb);
    }
}
