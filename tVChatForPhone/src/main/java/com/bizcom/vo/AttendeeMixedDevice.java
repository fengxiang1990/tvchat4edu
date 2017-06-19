package com.bizcom.vo;

import java.util.ArrayList;
import java.util.List;

import com.bizcom.vo.MixVideo.MixVideoDevice;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

public class AttendeeMixedDevice extends Attendee {

	private MixVideo mv;
	private UserDeviceConfig[] udcs;

	public AttendeeMixedDevice(MixVideo mv) {
		super();
		this.mv = mv;
		if (this.mv != null) {
			MixVideo.MixVideoDevice[] uds = mv.getUdcs();
			// 这里+1处理是因为，集合中第一个位置代表混合视频id；其余剩下四个代表混合视频中子视频的用户id和设备id
			udcs = new UserDeviceConfig[uds.length + 1];
			ArrayList<UserDeviceConfig> tmep = new ArrayList<>();
			for (int i = 0; i < uds.length + 1; i++) {
				udcs[i] = new UserDeviceConfig(0, 0, mv.getId().hashCode(), mv.getId(), null,
						V2GlobalConstants.EVIDEODEVTYPE_VIDEOMIXER);
				udcs[i].setEnable(true);
				tmep.add(udcs[i]);
			}
			GlobalHolder.getInstance().updateUserDevice(mv.getId().hashCode(), tmep);
		}
		this.isJoined = true;
	}

	public MixVideo getMV() {
		return this.mv;
	}

	@Override
	public long getAttId() {
		return mv.getId().hashCode();
	}

	public UserDeviceConfig getDefaultDevice() {
		if (udcs != null && udcs.length > 0) {
			return udcs[0];
		}
		return null;
	}

	public List<UserDeviceConfig> getDevices() {
		List<UserDeviceConfig> l = null;
		if (udcs.length > 0) {
			l = new ArrayList<UserDeviceConfig>();
			for (int i = 0; i < udcs.length; i++) {
				l.add(udcs[i]);
			}
		}
		return l;
	}

	public UserDeviceConfig getDevice(String deviceID) {
		for (int i = 0; i < udcs.length; i++) {
			UserDeviceConfig temp = udcs[i];
			if (temp.getDeviceID().equals(deviceID)) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * 添加一个占用标记位
	 * 
	 * @param mvd
	 */
	public void setUsingDevice(MixVideoDevice mvd) {
		for (int i = 1; i < udcs.length; i++) {
			UserDeviceConfig temp = udcs[i];
			if (temp.getUserID() == mv.getId().hashCode()) {
				temp.setUserID(mvd.getUdc().getUserID());
				temp.setDeviceID(mvd.getUdc().getDeviceID());
				break;
			}
		}
	}

	/**
	 * 移除一个集合中已经占用的标记位
	 * 
	 * @param mvd
	 */
	public void removeUsingDevice(MixVideoDevice mvd) {
		for (int i = 1; i < udcs.length; i++) {
			UserDeviceConfig temp = udcs[i];
			if (temp.getDeviceID().equals(mvd.getUdc().getDeviceID())) {
				temp.setUserID(mv.getId().hashCode());
				temp.setDeviceID(mv.getId());
				break;
			}
		}
	}

	@Override
	public String getAttName() {
		return mv.getWidth() + " x " + mv.getHeight() + ")";
	}

	@Override
	public int getType() {
		return TYPE_MIXED_VIDEO;
	}

	public String getMixSize() {
		return "(" + mv.getWidth() + "*" + mv.getHeight() + ")";
	}
}
