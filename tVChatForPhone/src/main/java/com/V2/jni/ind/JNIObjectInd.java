package com.V2.jni.ind;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class indication JNI returned result wrapper
 * @author jiangzhen
 *
 */
public abstract class JNIObjectInd implements Parcelable{
	
	public enum JNIIndType {
		AUDIO,CHAT_TEXT,CHAT_BINARY,APP,CONF,FILE,VIDEO,WR,GROUP,VIDEO_MIXED;
	}

	protected JNIIndType mType;
	
	/**
	 * <p> Type of indication.  <br>
	 * com.V2.jni.V2GlobalEnum.REQUEST_TYPE_CONF<br>
	 * com.V2.jni.V2GlobalEnum.REQUEST_TYPE_IM<br>
	 * </p>
	 * 
	 */
	protected int mRequestType;
	public JNIIndType getType() {
		return this.mType;
	}
	
	public int getRequestType() {
		return this.mRequestType;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}
}
