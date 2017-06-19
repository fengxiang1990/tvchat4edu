package com.V2.jni.ind;

import android.os.Parcel;
import android.os.Parcelable;

public class V2ConfSyncVideoJNIObject extends JNIObjectInd implements
		Parcelable {
	// xml的节点属性
	public String DstDeviceID;
	public String DstUserID;
	// xml的节点名称
	private String tag = "video";

	public V2ConfSyncVideoJNIObject(Parcel parcel) {
		super();
		mType = JNIIndType.values()[parcel.readInt()];
		DstDeviceID = parcel.readString();
		DstUserID = parcel.readString();
	}

	public V2ConfSyncVideoJNIObject(String dstDeviceID, String dstUserID) {
		super();
		mType = JNIIndType.CONF;
		DstDeviceID = dstDeviceID;
		DstUserID = dstUserID;
	}

	public V2ConfSyncVideoJNIObject() {
		this(null, null);
	}

	public String getDstDeviceID() {
		return DstDeviceID;
	}

	public void setDstDeviceID(String dstDeviceID) {
		DstDeviceID = dstDeviceID;
	}

	public long getDstUserID() {
		return Long.valueOf(DstUserID);
	}

	public void setDstUserID(String dstUserID) {
		DstUserID = dstUserID;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mType.ordinal());
		dest.writeString(DstDeviceID);
		dest.writeString(DstUserID);
	}

	public static final Parcelable.Creator<V2ConfSyncVideoJNIObject> CREATOR = new Parcelable.Creator<V2ConfSyncVideoJNIObject>() {
		public V2ConfSyncVideoJNIObject createFromParcel(Parcel in) {
			return new V2ConfSyncVideoJNIObject(in);
		}

		public V2ConfSyncVideoJNIObject[] newArray(int size) {
			return new V2ConfSyncVideoJNIObject[size];
		}
	};
}
