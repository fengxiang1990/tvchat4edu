package com.bizcom.vo;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

public class AudioVideoMessageBean  implements Parcelable , Comparable<AudioVideoMessageBean>{

	public static final int TYPE_AUDIO = 0;
	public static final int TYPE_VIDEO = 1;
	public static final int TYPE_SIP = 3;
	public static final int TYPE_ALL = 2;
	
	public static final int STATE_CALL_OUT = 3; 
	public static final int STATE_CALL_IN = 4; 
	
	public static final int STATE_ANSWER_CALL = 5; 
	public static final int STATE_NO_ANSWER_CALL = 6;
	
	public static final int REPLY_REJECT = 9;
	public static final int REPLY_ACCEPT = 10;
	
	public AudioVideoMessageBean() {
		super();
	}
	public int isCancelByMine = 0;
	public String name;
	public long holdingTime;
	public long fromUserID;
	public long toUserID;
	public long remoteUserID;
	public int callNumbers;
	public int mediaType;
	public int meidaState;
	public int readState;
	public int isCallOut; //是否是主动拨出去
	public ArrayList<ChildMessageBean> mChildBeans = new ArrayList<ChildMessageBean>();
	public boolean isCheck;
	public ImageView userIcon;
	
	public AudioVideoMessageBean(String name, long holdingTime,
			long fromUserID, long toUserID, long remoteUserID, int callNumbers,
			int mediaType, int readState , int isCallOut , int meidaState) {
		super();
		this.name = name;
		this.holdingTime = holdingTime;
		this.fromUserID = fromUserID;
		this.toUserID = toUserID;
		this.remoteUserID = remoteUserID;
		this.callNumbers = callNumbers;
		this.mediaType = mediaType;
		this.readState = readState;
		this.meidaState = meidaState;
		this.isCallOut = isCallOut;
	}

	
	public static class ChildMessageBean implements Parcelable{
		public int childMediaType;
		public int childReadState;
		public int childMediaState;
		public long childHoldingTime;
		public long childSaveDate;
		public int childISCallOut; //是否是主动拨出去
		public long messageId;
		public int isCancelByMine = 0;
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		public ChildMessageBean(){};

		public ChildMessageBean(int childMediaType, int childReadState, int childMediaState,
				long childHoldingTime, long childSaveDate , int childISCallOut,long messageId) {
			super();
			this.childMediaType = childMediaType;
			this.childReadState = childReadState;
			this.childMediaState = childMediaState;
			this.childHoldingTime = childHoldingTime;
			this.childSaveDate = childSaveDate;
			this.childISCallOut = childISCallOut;
			this.messageId = messageId;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {

			dest.writeInt(childMediaType);
			dest.writeInt(childReadState);
			dest.writeInt(childMediaState);
			dest.writeLong(childHoldingTime);
			dest.writeLong(childSaveDate);
			dest.writeInt(childISCallOut);
			dest.writeLong(messageId);
		}
		
		public static final Parcelable.Creator<ChildMessageBean> CREATOR = new Creator<ChildMessageBean>() {

			@Override
			public ChildMessageBean[] newArray(int i) {
				return new ChildMessageBean[i];
			}

			@Override
			public ChildMessageBean createFromParcel(Parcel parcel) {
				return new ChildMessageBean(parcel.readInt(), parcel.readInt(), parcel.readInt(),parcel.readLong(),parcel.readLong(), parcel.readInt(),parcel.readLong());
			}
		};

		@Override
		public String toString() {
			return "ChildMessageBean [childMediaType=" + childMediaType
					+ ", childReadState=" + childReadState
					+ ", childMediaState=" + childMediaState
					+ ", childHoldingTime=" + childHoldingTime
					+ ", childSaveDate=" + childSaveDate + ", childISCallOut="
					+ childISCallOut + ", mediaID=" + messageId + "]";
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(name);
		dest.writeLong(holdingTime);
		dest.writeLong(fromUserID);
		dest.writeLong(toUserID);
		dest.writeLong(remoteUserID);
		dest.writeInt(callNumbers);
		dest.writeInt(mediaType);
		dest.writeInt(readState);
		dest.writeInt(meidaState);
		dest.writeInt(isCallOut);
	}
	
	public static final Parcelable.Creator<AudioVideoMessageBean> CREATOR = new Creator<AudioVideoMessageBean>() {

		@Override
		public AudioVideoMessageBean[] newArray(int i) {
			return new AudioVideoMessageBean[i];
		}

		@Override
		public AudioVideoMessageBean createFromParcel(Parcel parcel) {
			return new AudioVideoMessageBean(parcel.readString(),
					parcel.readLong(),parcel.readLong(),parcel.readLong(),parcel.readLong(),
					parcel.readInt(), parcel.readInt(), parcel.readInt() , parcel.readInt() , parcel.readInt());
		}
	};

	@Override
	public int compareTo(AudioVideoMessageBean another) {
		if(mChildBeans.size() <=0 || another.mChildBeans.size() <= 0)
			return 0;
		
		ChildMessageBean loaclChild = mChildBeans.get(0);
		ChildMessageBean childMessageBean = another.mChildBeans.get(0);
		if (loaclChild.childSaveDate > childMessageBean.childSaveDate) 
			return -1;
		else
			return 1;
	}

	@Override
	public String toString() {
		return "AudioVideoMessageBean [name=" + name + ", holdingTime="
				+ holdingTime + ", fromUserID=" + fromUserID + ", toUserID="
				+ toUserID + ", remoteUserID=" + remoteUserID
				+ ", callNumbers=" + callNumbers + ", mediaType=" + mediaType
				+ ", meidaState=" + meidaState + ", readState=" + readState
				+ ", isCallOut=" + isCallOut + ", mChildBeans=" + mChildBeans
				+ ", isCheck=" + isCheck + ", userIcon=" + userIcon + "]";
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getHoldingTime() {
		return holdingTime;
	}

	public void setHoldingTime(long holdingTime) {
		this.holdingTime = holdingTime;
	}

	public long getFromUserID() {
		return fromUserID;
	}

	public void setFromUserID(long fromUserID) {
		this.fromUserID = fromUserID;
	}

	public long getToUserID() {
		return toUserID;
	}

	public void setToUserID(long toUserID) {
		this.toUserID = toUserID;
	}

	public long getRemoteUserID() {
		return remoteUserID;
	}

	public void setRemoteUserID(long remoteUserID) {
		this.remoteUserID = remoteUserID;
	}

	public int getCallNumbers() {
		return callNumbers;
	}

	public void setCallNumbers(int callNumbers) {
		this.callNumbers = callNumbers;
	}

	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	public int getMeidaState() {
		return meidaState;
	}

	public void setMeidaState(int meidaState) {
		this.meidaState = meidaState;
	}

	public int getReadState() {
		return readState;
	}

	public void setReadState(int readState) {
		this.readState = readState;
	}

	public int getIsCallOut() {
		return isCallOut;
	}

	public void setIsCallOut(int isCallOut) {
		this.isCallOut = isCallOut;
	}

	public ArrayList<ChildMessageBean> getmChildBeans() {
		return mChildBeans;
	}

	public void setmChildBeans(ArrayList<ChildMessageBean> mChildBeans) {
		this.mChildBeans = mChildBeans;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}
	
	
}
