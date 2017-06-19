package com.bizcom.vo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

/**
 * As conference's attendee object. <br>
 * 
 * @author 28851274
 * 
 */
public class Attendee implements Comparable<Attendee>,Parcelable {

	public static final int LECTURE_STATE_NOT = 0;
	public static final int LECTURE_STATE_APPLYING = 1;
	public static final int LECTURE_STATE_GRANTED = 2;

	public final static int TYPE_ATTENDEE = 1;
	public final static int TYPE_MIXED_VIDEO = 2;

	private User user;
	private boolean isSelf;
	protected boolean isChairMan;
	protected boolean isJoined;
	private boolean isSpeaking;
	private int lectureState = LECTURE_STATE_NOT;
	public String[] showingDevices = new String[4];

	protected Attendee() {

	}

	public Attendee(User user) {
		this(user, null, false, false);
	}

	public Attendee(User user, boolean isSelf, boolean isChairMan) {
		this(user, null, isSelf, isChairMan);
	}

	public Attendee(User user, List<UserDeviceConfig> mDevices) {
		this(user, mDevices, false, false);
	}

	public Attendee(User user, List<UserDeviceConfig> mDevices, boolean isSelf, boolean isChairMan) {
		super();
		this.setUser(user);
		this.isSelf = isSelf;
		this.isChairMan = isChairMan;
	}

	protected Attendee(Parcel in) {
		isSelf = in.readByte() != 0;
		isChairMan = in.readByte() != 0;
		isJoined = in.readByte() != 0;
		isSpeaking = in.readByte() != 0;
		lectureState = in.readInt();
		showingDevices = in.createStringArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (isSelf ? 1 : 0));
		dest.writeByte((byte) (isChairMan ? 1 : 0));
		dest.writeByte((byte) (isJoined ? 1 : 0));
		dest.writeByte((byte) (isSpeaking ? 1 : 0));
		dest.writeInt(lectureState);
		dest.writeStringArray(showingDevices);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<Attendee> CREATOR = new Creator<Attendee>() {
		@Override
		public Attendee createFromParcel(Parcel in) {
			return new Attendee(in);
		}

		@Override
		public Attendee[] newArray(int size) {
			return new Attendee[size];
		}
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attendee other = (Attendee) obj;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}

	public long getAttId() {
		if (getUser() != null) {
			return getUser().getmUserId();
		}
		return 0;
	}

	public String getAttName() {
		if (getUser() != null) {
			if (!TextUtils.isEmpty(getUser().getCommentName()))
				return getUser().getCommentName();
			else
				return getUser().getDisplayName();

		}
		return null;
	}

	public boolean isRapidInitiation() {
		if (getUser() != null) {
			return getUser().isRapidInitiation();
		}
		return false;
	}

	public String getAbbraName() {
		if (getUser() != null) {
			return getUser().getArra();
		}
		return null;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isSelf() {
		return isSelf;
	}

	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
		this.isJoined = true;
	}

	public boolean isChairMan() {
		return isChairMan;
	}

	public void setChairMan(boolean isChairMan) {
		this.isChairMan = isChairMan;
	}

	public boolean isJoined() {
		return isJoined;
	}

	public void setJoined(boolean isJoined) {
		this.isJoined = isJoined;
	}

	public Bitmap getAvatar() {
		if (getUser() == null) {
			return null;
		}
		Bitmap map = this.getUser().getAvatarBitmap();
		return map;
	}

	public int getType() {
		return TYPE_ATTENDEE;
	}

	public boolean isSpeaking() {
		return isSpeaking;
	}

	public void setSpeakingState(boolean isSpeaking) {
		this.isSpeaking = isSpeaking;
	}

	public int getLectureState() {
		return lectureState;
	}

	public void setLectureState(int lectureState) {
		this.lectureState = lectureState;
	}

	@Override
	public int compareTo(Attendee attendee) {
		if (this.getUser() == null) {
			return 1;
		}
		if (attendee.getUser() == null) {
			return -1;
		}

		if (this.getUser().getmUserId() == attendee.getUser().getmUserId()) {
			return 0;
		} else {
			return 1;
		}
	}

	public User getUser() {
		return user;
	}

}
