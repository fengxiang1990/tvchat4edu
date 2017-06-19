package com.bizcom.db.provider;

import android.content.Context;

import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.DataBaseContext;
import com.bizcom.db.V2techBaseProvider;
import com.bizcom.util.V2Log;

public class DatabaseProvider {
	
	public static DataBaseContext mContext;
	public static void init(Context context){
        ContentDescriptor.AUTHORITY = context.getPackageName();
        ContentDescriptor.initInstance();
        V2Log.d("MainApplication" , "ContentDescriptor.AUTHORITY : " + ContentDescriptor.AUTHORITY);
        V2techBaseProvider.AUTHORITY = context.getPackageName();
		mContext = new DataBaseContext(context);
	}
}
