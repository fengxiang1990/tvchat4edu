package com.bizcom.vo.meesage;

import java.util.UUID;

import android.text.TextUtils;

import com.bizcom.util.V2Log;
import com.config.GlobalConfig;

public class VMessageAudioItem extends VMessageAbstractItem {

	private String audioFilePath;
	private String extension;
	private int seconds;
	private boolean isPlaying;
	private int readState;
	private boolean isReceive;
	private boolean isStartPlay;

	/**
	 * XmlParser used
	 * 
	 * @param vm
	 * @param uuid
	 * @param extension
	 * @param seconds
	 */
	public VMessageAudioItem(VMessage vm, String uuid, String extension,
			int seconds) {
		this(vm, uuid, null, extension, seconds, -1);
	}

	/**
	 * MessageBuilder buildAudioMessage used
	 * 
	 * @param vm
	 * @param uuid
	 * @param audioFilePath
	 * @param seconds
	 * @param readState
	 */
	public VMessageAudioItem(VMessage vm, String uuid, String audioFilePath,
			int seconds, int readState) {
		this(vm, uuid, audioFilePath, null, seconds, readState);
	}

	public VMessageAudioItem(VMessage vm, String uuid, String audioFilePath,
			String extension, int seconds, int readState) {
		super(vm , ITEM_TYPE_AUDIO);
		this.uuid = uuid;
		this.audioFilePath = audioFilePath;
		this.extension = extension;
		this.seconds = seconds;
		this.readState = readState;

		if (uuid == null)
			this.uuid = UUID.randomUUID().toString();

		if (TextUtils.isEmpty(audioFilePath) && !TextUtils.isEmpty(extension))
			this.audioFilePath = GlobalConfig.getGlobalAudioPath() + "/" + uuid
					+ extension;

		if (!TextUtils.isEmpty(audioFilePath) && TextUtils.isEmpty(extension)) {
			int start = this.audioFilePath.lastIndexOf(".");
			if (start != -1) {
				this.extension = this.audioFilePath.substring(start);
			}
		}
	}

	public String getAudioFilePath() {
		return audioFilePath;
	}

	public void setAudioFilePath(String audioFilePath) {
		this.audioFilePath = audioFilePath;
	}

	public int getSeconds() {
		return seconds;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public int getReadState() {
		return readState;
	}

	public void setReadState(int readState) {
		this.readState = readState;
	}

	public boolean isReceive() {
		return isReceive;
	}

	public void setReceive(boolean isReceive) {
		this.isReceive = isReceive;
	}
	
	public boolean isStartPlay() {
		return isStartPlay;
	}

	public void setStartPlay(boolean isStartPlay) {
		this.isStartPlay = isStartPlay;
	}

	/**
	 */
	public String toXmlItem() {
		long userID = 0;
		// if toUser is null , mean group no user
		if (vm.getToUser() != null) {
			userID = vm.getToUser().getmUserId();
		}
		if (vm.getFromUser() == null) {
			V2Log.e(" audo message item to xml failed no from user");
			return "";
		}
		String xml = "<TAudioChatItem NewLine=\"True\" FileExt=\"" + extension
				+ "\" FileID=\"" + uuid + "\" RecvUserID=\"" + userID
				+ "\" Seconds=\"" + seconds + "\" SendUserID=\""
				+ vm.getFromUser().getmUserId() + "\"/>";
		return xml;
	}

}
