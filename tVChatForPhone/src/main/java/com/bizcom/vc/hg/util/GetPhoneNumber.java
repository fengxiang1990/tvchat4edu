package com.bizcom.vc.hg.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Created by 某宅 on 2016/6/26.
 */
public class GetPhoneNumber {


	public static List<PhoneInfo> getNumber(Context context) {
		List<PhoneInfo> list = new ArrayList<PhoneInfo>();
		Cursor cursor = null;
		try{
			cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null, null, null, null);
			String number;
			String name;
			while (cursor.moveToNext()) {
				number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				PhoneInfo phoneInfo = new PhoneInfo(name, number);
				list.add(phoneInfo);
			}
			if(cursor!=null){
				cursor.close();
				cursor=null;
			}
		}catch(Exception e){
			System.out.println("");

		}finally{
			if(cursor!=null){
				cursor.close();
				cursor=null;
			}
			
		}

		return list;
	}

}