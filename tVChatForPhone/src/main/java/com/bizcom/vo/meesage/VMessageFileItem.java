package com.bizcom.vo.meesage;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.UUID;

import android.text.TextUtils;

import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.util.FileUtils;
import com.config.GlobalConfig;

public class VMessageFileItem extends VMessageAbstractItem {

	private String filePath;

	private String fileName;

	private long fileSize;

	private float progress;

	private long downloadedSize;

	private float speed;

	private FileType fileType;

	// Always send offline file
	private int transType = 2;

	private String url;

	public VMessageFileItem(VMessage vm, String filePath, int fileState) {
		this(vm, null, filePath, null, 0, fileState, 0, 0, 0, FileType.UNKNOW,
				2, null);
	}

	public VMessageFileItem(VMessage vm, String fileName, int fileState,
			String uuid) {
		this(vm, uuid, null, fileName, 0, fileState, 0, 0, 0, FileType.UNKNOW,
				2, null);
	}

	public VMessageFileItem(VMessage vm, String fileID, long fileSize,
			int fileState, String fileName, FileType fileType) {
		this(vm, fileID, null, fileName, fileSize, fileState, 0, 0, 0,
				fileType, 2, null);
	}

	public VMessageFileItem(VMessage vm, String fileID, long fileSize,
			int fileState, String fileName, FileType fileType, String url) {
		this(vm, fileID, null, fileName, fileSize, fileState, 0, 0, 0,
				fileType, 2, url);
	}

	public VMessageFileItem(VMessage vm, String uuid, String filePath,
			String fileName, long fileSize, int fileState, float progress,
			long downloadedSize, float speed, FileType fileType, int transType,
			String url) {
		super(vm, ITEM_TYPE_FILE);
		this.uuid = uuid;
		this.filePath = filePath;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.state = fileState;
		this.progress = progress;
		this.downloadedSize = downloadedSize;
		this.speed = speed;
		this.fileType = fileType;
		this.transType = transType;
		this.url = url;

		if (TextUtils.isEmpty(uuid))
			this.uuid = UUID.randomUUID().toString();

		if (!TextUtils.isEmpty(fileName) && TextUtils.isEmpty(filePath)) {
			this.filePath = GlobalConfig.getGlobalFilePath() + "/" + fileName;
		}

		if (TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(filePath)) {
			int start = filePath.lastIndexOf("/");
			if (start != -1)
				this.fileName = filePath.substring(start + 1);
		}

		if (fileType == FileType.UNKNOW && !TextUtils.isEmpty(this.fileName))
			this.fileType = FileUtils.getFileType(this.fileName);

		if (fileSize == 0 && !TextUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (file != null && file.isFile())
				this.fileSize = file.length();
		}
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long size) {
		this.fileSize = size;
	}

	public String getFileSizeStr() {
		Format df = new DecimalFormat("#.0");
		if (fileSize >= 1073741824) {
			return (df.format((double) fileSize / (double) 1073741824)) + "GB";
		} else if (fileSize >= 1048576) {
			return (df.format((double) fileSize / (double) 1048576)) + "MB";
		} else if (fileSize >= 1024) {
			return (df.format((double) fileSize / (double) 1024)) + "KB";
		} else {
			return fileSize + "B";
		}
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	@Override
	public String toXmlItem() {

		if (filePath == null)
			fileName = getFilePath();

		StringBuilder sb = new StringBuilder();
		sb.append(
				"<file id=\"" + uuid + "\" name=\""
						+ EscapedcharactersProcessing.convert(filePath)
						+ "\" url=\"" + EscapedcharactersProcessing.convert(url) + "\" />").append("\n");
		return sb.toString();
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public long getDownloadedSize() {
		return downloadedSize;
	}

	public String getDownloadSizeStr() {
		Format df = new DecimalFormat("#.0");
		if (downloadedSize >= 1073741824) {
			return (df.format((double) downloadedSize / (double) 1073741824))
					+ "GB";
		} else if (downloadedSize >= 1048576) {
			return (df.format((double) downloadedSize / (double) 1048576))
					+ "MB";
		} else if (downloadedSize >= 1024) {
			return (df.format((double) downloadedSize / (double) 1024)) + "KB";
		} else {
			return downloadedSize + "B";
		}
	}

	public void setDownloadedSize(long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}

	public String getSpeedStr() {
		return getFileSize(speed) + "/S";
	}

	public float getSpeed() {
		return speed;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param totalSpace
	 * @return
	 */
	private String getFileSize(float totalSpace) {

		BigDecimal filesize = new BigDecimal(totalSpace);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		if (returnValue > 1)
			return (returnValue + "MB");
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		if (returnValue > 1)
			return (returnValue + "KB");
		else
			return (totalSpace + "B");
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public int getTransType() {
		return transType;
	}

	public void setTransType(int transType) {
		this.transType = transType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public enum FileType {
		IMAGE(1), WORD(2), EXCEL(3), PDF(4), PPT(5), ZIP(6), VIS(7), VIDEO(8), PACKAGE(
				9), HTML(10), AUDIO(11), TEXT(12), UNKNOW(13);
		private int fileType;

		private FileType(int fileType) {
			this.fileType = fileType;
		}

		public static FileType fromInt(int fileType) {
			switch (fileType) {
			case 1:
				return IMAGE;
			case 2:
				return WORD;
			case 3:
				return EXCEL;
			case 4:
				return PDF;
			case 5:
				return PPT;
			case 6:
				return ZIP;
			case 7:
				return VIS;
			case 8:
				return VIDEO;
			case 9:
				return PACKAGE;
			case 10:
				return HTML;
			case 11:
				return AUDIO;
			case 12:
				return TEXT;
			default:
				return UNKNOW;
			}
		}

		public int intValue() {
			return fileType;
		}
	}
}
