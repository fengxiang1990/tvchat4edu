package com.V2.jni.ind;

import android.os.Parcel;
import android.os.Parcelable;

import com.bizcom.util.FileUtils;

public class FileJNIObject extends JNIObjectInd implements Parcelable {

	public BoUserInfoBase user;
	public String vMessageID;
	public String fileId;
	public String fileName;
	public long fileSize;
	public int fileType;
	public int linetype;

	// For crowd file type
	public String url;

	/**
	 * 
	 * @param user
	 * @param fileId
	 * @param fileName
	 * @param fileSize
	 * @param linetype
	 * 			 2: offline file 1: online file
	 * @param url
	 */
	public FileJNIObject(BoUserInfoBase user , String fileId, String fileName,
			long fileSize, int linetype, String url) {
		super();
		this.user = user;
		this.fileId = fileId;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.linetype = linetype;
		this.url = url;
		if (fileName != null && !fileName.isEmpty()) {
			fileType = FileUtils.getFileType(fileName).intValue();
		}
	}
	
	public FileJNIObject(Parcelable user, String vMessageID , String szFileID, String szFileName,
			long nFileBytes, int linetype, int fileType , String url) {
		this.user = (BoUserInfoBase) user;
		this.vMessageID = vMessageID;
		this.fileId = szFileID;
		this.fileName = szFileName;
		this.fileSize = nFileBytes;
		this.fileType = fileType;
		this.linetype = linetype;
		this.mType = JNIIndType.FILE;
		this.url = url;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(user, 0);
		dest.writeString(vMessageID);
		dest.writeString(fileId);
		dest.writeString(fileName);
		dest.writeLong(fileSize);
		dest.writeInt(fileType);
		dest.writeInt(linetype);
		dest.writeString(url);
	}

	public static final Parcelable.Creator<FileJNIObject> CREATOR = new Creator<FileJNIObject>() {

		@Override
		public FileJNIObject[] newArray(int i) {
			return new FileJNIObject[i];
		}

		@Override
		public FileJNIObject createFromParcel(Parcel parcel) {
			return new FileJNIObject(parcel.readParcelable(BoUserInfoBase.class
					.getClassLoader()), parcel.readString(),parcel.readString(),
					parcel.readString(), parcel.readLong(), parcel.readInt(),
					parcel.readInt(), parcel.readString());
		}
	};
}
