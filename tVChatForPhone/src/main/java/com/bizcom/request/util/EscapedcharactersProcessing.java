package com.bizcom.request.util;


import android.content.Intent;
import android.text.TextUtils;

public class EscapedcharactersProcessing {

    public static String convert(String str) {
        if (!TextUtils.isEmpty(str)) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch (c) {
                    case '<':
                        buf.append("&lt;");
                        break;
                    case '>':
                        buf.append("&gt;");
                        break;
                    case 0xD:
                        buf.append("&#x0D;");
                        break;
                    case 0xA:
                        buf.append("&#x0A;");
                        break;
                    case 0x9:
                        buf.append("&#x09;");
                        break;
                    case '&':
                        buf.append("&amp;");
                        break;
                    case '\'':
                        buf.append("&apos;");
                        break;
                    case '"':
                        buf.append("&quot;");
                        break;
                    default:
                        buf.append(c);
                        break;
                }
            }
            return buf.toString();
        }
        return str;
    }

    public static String convertAmp(String str) {
        if (!TextUtils.isEmpty(str)) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch (c) {
                    case '&':
                        buf.append("&amp;");
                        break;
                    default:
                        buf.append(c);
                }
            }
            return buf.toString();
        }
        return str;
    }

    public static String reverse(String str) {
        if (!TextUtils.isEmpty(str)) {
            str = str.replace("&lt;", "<");
            str = str.replace("&gt;", ">");
            str = str.replace("&#x0D;", "0xD");
            str = str.replace("&#x0A;", "0xA");
            str = str.replace("&#x09;", "0x9");
            str = str.replace("&amp;", "&");
            str = str.replace("&apos;", "'");
            str = str.replace("&quot;", "\"");
            return str;
        }
        return str;
    }

    public static String convertPercent(String str , String percentReplaced) {
        if (!TextUtils.isEmpty(str)) {
            str = str.replace("%" , percentReplaced);
            return str;
        }
        return str;
    }
}
