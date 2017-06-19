package com.bizcom.vc.hg.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.cgs.utils.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DataUtil {
    // 保存数据key
    private static final String APPSPNAME = "tvChatData";


    public static String getDate() {
        SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dff.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        String ee = dff.format(new Date());
        try {
            Date date = dff.parse(ee);
            return date.getTime()+"";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis()+"";
    }

    /**
     * 保存复杂对象到SharedPreferences中
     *
     * @param o
     * @param keyName
     * @param context
     */
    public static void saveData(Object o, String keyName, Context context) {
        SharedPreferences spf_Preferences = context.getSharedPreferences(
                APPSPNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf_Preferences.edit();
        // 对byte[]进行Base64编码
        String payCityMapBase64;
        try {
            ByteArrayOutputStream toByte = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(toByte);
            oos.writeObject(o);
            payCityMapBase64 = new String(Base64Coder.encode(toByte
                    .toByteArray()));
            editor.putString(keyName, payCityMapBase64);
            editor.commit();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 读取复杂对象SharedPreferences
     *
     * @param keyName
     * @param context
     * @return
     */
    public static Object getData(String keyName, Context context) {
        try {
            SharedPreferences spf_Preferences = context.getSharedPreferences(
                    APPSPNAME, Context.MODE_PRIVATE);
            if (spf_Preferences.getString(keyName, null) != null) {
                byte[] base64Bytes = Base64Coder.decode(spf_Preferences
                        .getString(keyName, null));
                ByteArrayInputStream bais = new ByteArrayInputStream(
                        base64Bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                return ois.readObject();
            }
        } catch (StreamCorruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OptionalDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}