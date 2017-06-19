package com.bizcom.vo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.util.DateUtil;
import com.config.GlobalHolder;

public class Conference implements Parcelable {

	private long id;

	private String name;
	private String startTime;
	private List<User> invitedList;
	private Date startDate;
	private long creator;
	private long chairman;
	private boolean isCanInvitation = true;

	public Conference(ConferenceGroup cg) {
		this(cg.getGroupID(), cg.getOwnerUser().getmUserId(), cg.getName(), cg
				.getCreateDate(), null, null);
	}

	public Conference(long id) {
		this(id, 0, null, null, null, null);
	}

	public Conference(long id, long creator) {
		this(id, creator, null, null, null, null);
	}

	public Conference(String name, Date startTime, Date endTime,
			List<User> invitedList) {
		this(0, 0, name, startTime, endTime, invitedList);
	}

	public Conference(long id, long creator, String name, Date startTime,
			Date endTime, List<User> invitedList) {
		this.id = id;
		this.creator = creator;
		this.name = name;
		this.startDate = startTime;
		this.invitedList = invitedList;

		if (startTime != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			this.startTime = format.format(startTime);
		}
	}

	public Conference(Parcel par) {
		id = par.readLong();
		name = par.readString();
		startTime = par.readString();
		creator = par.readLong();
		chairman = par.readLong();
		int ir = par.readInt();
		if (ir == 1) {
			isCanInvitation = true;
		} else {
			isCanInvitation = false;
		}
	}

	public static final Parcelable.Creator<Conference> CREATOR = new Parcelable.Creator<Conference>() {
		public Conference createFromParcel(Parcel in) {
			return new Conference(in);
		}

		public Conference[] newArray(int size) {
			return new Conference[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel par, int flag) {
		par.writeLong(id);
		par.writeString(name);
		par.writeString(this.startTime);
		par.writeLong(creator);
		par.writeLong(chairman);
		par.writeInt(isCanInvitation ? 1 : 0);
	}

	public boolean isCanInvitation() {
		return isCanInvitation;
	}

	public void setCanInvitation(boolean isCanInvitation) {
		this.isCanInvitation = isCanInvitation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Conference other = (Conference) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStartTimeStr() {
		if (this.startTime == null) {
			if (this.startDate != null) {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm",
						Locale.CHINA);
				this.startTime = df.format(startDate);
			}
		}
		return this.startTime;
	}

	public long getChairman() {
		return chairman;
	}

	public void setChairman(long chairman) {
		this.chairman = chairman;
	}

	public long getCreator() {
		return creator;
	}

	public void setCreator(long creator) {
		this.creator = creator;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public Date getDate() {
		if (startDate == null && this.startTime != null
				&& this.startTime.trim().length() <= 16) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm",
					Locale.CHINA);
			try {
				startDate = df.parse(this.startTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (startDate == null) {
			return new Date();
		} else {
			return startDate;
		}
	}

	public long getId() {
		return this.id;
	}

	/**
	 * <conf canaudio="1" candataop="1" canvideo="1" conftype="0" haskey="0" //
	 * id="0" key="" // layout="1" lockchat="0" lockconf="0" lockfiletrans="0"
	 * mode="2" // pollingvideo="0" // subject="ss" // chairuserid='0'
	 * chairnickname=''> // </conf>
	 * 
	 * @return
	 */
	public String getConferenceConfigXml() {
		User loggedUser = GlobalHolder.getInstance().getCurrentUser();
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<conf canaudio=\"1\" candataop=\"1\" canvideo=\"1\" conftype=\"0\" haskey=\"0\" ")
				.append(" id=\"0\" key=\"\" layout=\"14\" lockchat=\"0\" lockconf=\"0\" lockfiletrans=\"0\" mode=\"2\" pollingvideo=\"0\" ")
				.append(" syncdesktop=\"0\" voiceactivation=\"0\" syncdocument=\"1\" syncvideo=\"0\" ")
				.append("subject=\"")
				.append(EscapedcharactersProcessing.convert(this.name))
				.append("\" ")
				.append("chairuserid=\"")
				.append(GlobalHolder.getInstance().getCurrentUserId())
				.append("\" ")
				.append("chairnickname=\"")
				.append(loggedUser == null ? "" : EscapedcharactersProcessing
						.convert(loggedUser.getDisplayName())).append("\" ")
				.append("starttime=\"").append(getDate().getTime() / 1000)
				.append("\" >").append("</conf>");
		return sb.toString();

	}

	public String getInvitedAttendeesXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (User u : this.invitedList) {
			sb.append("<user id=\"")
					.append(u.getmUserId())
					.append("\" ")
					.append("nickname=\"")
					.append(EscapedcharactersProcessing.convert(u
							.getDisplayName())).append("\"/>");
		}

		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * <conf canoper='0' chairuserid='17' createuserid='17' endtime='0'
	 * id='514015216076' inviteuser='1' layout='1' starttime='1401521340'
	 * subject=' 斤斤计较斤斤计较' syncdesktop='0' voiceactivation='0'/>
	 * 
	 * @return
	 */
	public static Conference formConferenceConfigXml(String str) {
		long id = 0;
		long createor = 0;
		long chairman = 0;
		String subject = "";
		String startTime = "";
		int start = str.indexOf("chairuserid='");
		if (start != -1) {
			int end = str.indexOf("'", start + 13);
			if (end != -1) {
				chairman = Long.valueOf(str.substring(start + 13, end));
			}
		}

		start = str.indexOf("createuserid='");
		if (start != -1) {
			int end = str.indexOf("'", start + 14);
			if (end != -1) {
				createor = Long.valueOf(str.substring(start + 14, end));
			}
		}

		start = str.indexOf(" id='");
		if (start != -1) {
			int end = str.indexOf("'", start + 5);
			if (end != -1) {
				id = Long.valueOf(str.substring(start + 5, end));
			}
		}

		start = str.indexOf(" starttime='");
		if (start != -1) {
			int end = str.indexOf("'", start + 12);
			if (end != -1) {
				startTime = str.substring(start + 12, end);
			}
		}

		start = str.indexOf(" subject='");
		if (start != -1) {
			int end = str.indexOf("'", start + 10);
			if (end != -1) {
				subject = str.substring(start + 10, end);
			}
		}

		Conference conf = new Conference(id);
		conf.setChairman(chairman);
		conf.setCreator(createor);
		conf.setName(subject);
		conf.setStartTime(DateUtil.getStandardDate(new Date(Long
				.valueOf(startTime) * 1000)));
		return conf;
	}
}
