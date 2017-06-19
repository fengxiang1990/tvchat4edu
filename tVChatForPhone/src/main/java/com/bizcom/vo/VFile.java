package com.bizcom.vo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Date;

import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.config.GlobalConfig;

public class VFile {

	protected String id;
	protected String path;
	protected long size;
	protected State state;
	protected long proceedSize;
	protected String name;
	protected User uploader;
	protected Date startTime;
	protected int flag;

	public enum State {
		UNKNOWN(-1), REMOVED(-2), 
		DOWNLOADED(VMessageAbstractItem.STATE_FILE_DOWNLOADED), 
		DOWNLOADING(VMessageAbstractItem.STATE_FILE_DOWNLOADING), 
		DOWNLOAD_PAUSE(VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING), 
		UPLOADED(VMessageAbstractItem.STATE_FILE_SENT), 
		UPLOADING(VMessageAbstractItem.STATE_FILE_SENDING), 
		UPLOAD_PAUSE(VMessageAbstractItem.STATE_FILE_PAUSED_SENDING), 
		DOWNLOAD_FAILED(VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED), 
		UPLOAD_FAILED(VMessageAbstractItem.STATE_FILE_SENT_FALIED);

		private int state;

		private State(int state) {
			this.state = state;
		}

		public static State fromInt(int state) {
			switch (state) {
			case VMessageAbstractItem.STATE_FILE_DOWNLOADED:
				return DOWNLOADED;
			case VMessageAbstractItem.STATE_FILE_DOWNLOADING:
				return DOWNLOADING;
			case VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING:
				return DOWNLOAD_PAUSE;
			case VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED:
				return DOWNLOAD_FAILED;
			case VMessageAbstractItem.STATE_FILE_SENT:
				return UPLOADED;
			case VMessageAbstractItem.STATE_FILE_SENDING:
				return UPLOADING;
			case VMessageAbstractItem.STATE_FILE_PAUSED_SENDING:
				return UPLOAD_PAUSE;
			case VMessageAbstractItem.STATE_FILE_SENT_FALIED:
				return UPLOAD_FAILED;
			case -2:
				return REMOVED;
			default:
				return UNKNOWN;
			}
		}

		public int intValue() {
			return state;
		}
	}

	public VFile() {
		this.state = State.UNKNOWN;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public long getProceedSize() {
		return proceedSize;
	}

	public String getProceedSizeStr() {
		Format df = new DecimalFormat("#.0");
		if (proceedSize >= 1073741824) {
			return (df.format((double) proceedSize / (double) 1073741824))
					+ "GB";
		} else if (proceedSize >= 1048576) {
			return (df.format((double) proceedSize / (double) 1048576)) + "MB";
		} else if (proceedSize >= 1024) {
			return (df.format((double) proceedSize / (double) 1024)) + "KB";
		} else {
			return proceedSize + "B";
		}
	}

	public void setProceedSize(long proceedSize) {
		this.proceedSize = proceedSize;
		if (this.proceedSize == size) {
			if (this.state == State.DOWNLOADING) {
				this.state = State.DOWNLOADED;
			} else if (this.state == State.UPLOADING) {
				this.state = State.UPLOADED;
			}
		}

	}
	
	/**
	 * 获取文件大小
	 * 
	 * @param totalSpace
	 * @return
	 */
	public String getFileSize(float totalSpace) {

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

	public String getSpeedStr(long proceedSize) {
		if (startTime == null) {
			startTime = new Date(GlobalConfig.getGlobalServerTime());
			return "";
		}

		int divder = (int) (GlobalConfig.getGlobalServerTime() - startTime
				.getTime()) / 1000;
		long speed = (proceedSize / (divder == 0 ? 1 : divder));

		Format df = new DecimalFormat("#.0");
		if (speed >= 1073741824) {
			return (df.format((double) speed / (double) 1073741824)) + "GB";
		} else if (speed >= 1048576) {
			return (df.format((double) speed / (double) 1048576)) + "MB";
		} else if (speed >= 1024) {
			return (df.format((double) speed / (double) 1024)) + "KB";
		} else {
			return speed + "B";
		}
	}

	public String getFileSizeStr() {
		Format df = new DecimalFormat("#.0");
		if (size >= 1073741824) {
			return (df.format((double) size / (double) 1073741824)) + "GB";
		} else if (size >= 1048576) {
			return (df.format((double) size / (double) 1048576)) + "MB";
		} else if (size >= 1024) {
			return (df.format((double) size / (double) 1024)) + "KB";
		} else {
			return size + "B";
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUploader() {
		return uploader;
	}

	public void setUploader(User uploader) {
		this.uploader = uploader;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

}
