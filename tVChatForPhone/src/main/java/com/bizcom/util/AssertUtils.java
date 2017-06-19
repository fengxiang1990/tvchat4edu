package com.bizcom.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;

public class AssertUtils {

	public static String getAssertValue(Context mContext, String targetKey) {
		AssetManager assets = mContext.getAssets();
		BufferedReader reader = null;
		String value = null;
		int count = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(assets.open("Settings.ini")));
			String len;
			while ((len = reader.readLine()) != null) {
				if (count == 0) {
					count++;
				} else {
					String[] split = len.split("=");
					String key = split[0];
					if (key.equals(targetKey)) {
						value = split[1];
						break;
					}
				}
			}
			return value;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
