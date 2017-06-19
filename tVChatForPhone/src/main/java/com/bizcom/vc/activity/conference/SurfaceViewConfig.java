package com.bizcom.vc.activity.conference;

import android.content.Context;
import android.widget.RelativeLayout;

import com.bizcom.vc.widget.MarqueeTextView;
import com.bizcom.vo.Attendee;
import com.bizcom.vo.AttendeeMixedDevice;
import com.bizcom.vo.UserDeviceConfig;
import com.shdx.tvchat.phone.R;

public class SurfaceViewConfig {

	public int showIndex;
	public Attendee at;
	public UserDeviceConfig udc;
	public SurfaceHolderObserver observer;
	private Context mContext;
	private RelativeLayout rl;

	public SurfaceViewConfig(Context mContext, Attendee at, UserDeviceConfig udc, SurfaceHolderObserver observer) {
		this.mContext = mContext;
		this.at = at;
		this.udc = udc;
		this.observer = observer;
		this.udc.getSVHolder().getHolder().addCallback(observer);
	}

	public RelativeLayout getView() {
		return rl;
	}

	public void setView(RelativeLayout rl) {
		this.rl = rl;
		MarqueeTextView tv = (MarqueeTextView) this.rl.getChildAt(1);
		if (at.getType() == Attendee.TYPE_MIXED_VIDEO) {
			AttendeeMixedDevice mix = (AttendeeMixedDevice) at;
			tv.setText(
					mContext.getResources().getString(R.string.vo_attendee_mixed_device_mix_video) + mix.getMixSize());
		} else if (at.getType() == Attendee.TYPE_ATTENDEE) {
			if (udc.isUserFirstDev()) {
				tv.setText(at.getAttName());
			} else {
				String deciceName;
				String deviceID = udc.getDeviceID();
				if (deviceID != null) {
                    try {
                        int start = deviceID.indexOf(':');
                        int end = deviceID.indexOf('_');
                        deciceName = at.getAttName() + " - " + deviceID.substring(start + 1, end);
                        tv.setText(deciceName);
                    } catch(Exception e){
                        tv.setText(at.getAttName());
                    }
				}
			}
		}
	}

	public void clear() {
		udc.doClose();
	}
}
